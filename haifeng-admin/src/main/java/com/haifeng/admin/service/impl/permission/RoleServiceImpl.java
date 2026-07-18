package com.haifeng.admin.service.impl.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.permission.RoleAddDTO;
import com.haifeng.admin.dto.permission.RoleModuleBindDTO;
import com.haifeng.admin.dto.permission.RoleQueryDTO;
import com.haifeng.admin.dto.permission.RoleUpdateDTO;
import com.haifeng.admin.service.permission.RoleService;
import com.haifeng.admin.vo.permission.ModuleTreeVO;
import com.haifeng.admin.vo.permission.RoleDetailVO;
import com.haifeng.admin.vo.permission.RoleListVO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.entity.permission.SysModule;
import com.haifeng.common.entity.permission.SysRole;
import com.haifeng.common.entity.permission.SysRoleModule;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.mapper.permission.SysModuleMapper;
import com.haifeng.common.mapper.permission.SysRoleMapper;
import com.haifeng.common.mapper.permission.SysRoleModuleMapper;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final SysModuleMapper moduleMapper;
    private final SysRoleModuleMapper roleModuleMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    @Override
    public IPage<RoleListVO> page(RoleQueryDTO dto) {
        Page<SysRole> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getRoleName())) {
            wrapper.like(SysRole::getRoleName, dto.getRoleName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(SysRole::getCreatedAt);

        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);

        return rolePage.convert(role -> {
            RoleListVO vo = new RoleListVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        });
    }

    @Override
    public RoleDetailVO detail(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        RoleDetailVO vo = new RoleDetailVO();
        BeanUtils.copyProperties(role, vo);

        List<SysRoleModule> roleModules = roleModuleMapper.selectList(
                new LambdaQueryWrapper<SysRoleModule>()
                        .eq(SysRoleModule::getRoleId, id)
        );
        List<Long> moduleIds = roleModules.stream()
                .map(SysRoleModule::getModuleId)
                .collect(Collectors.toList());
        vo.setModuleIds(moduleIds);

        if (!moduleIds.isEmpty()) {
            List<SysModule> modules = moduleMapper.selectBatchIds(moduleIds);
            vo.setModules(buildModuleTree(modules));
        } else {
            vo.setModules(new ArrayList<>());
        }

        return vo;
    }

    @Override
    public void add(RoleAddDTO dto) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, dto.getRoleName())
        );
        if (count > 0) {
            throw new BusinessException(400, "角色名称已存在");
        }

        count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, dto.getRoleCode())
        );
        if (count > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        SysRole role = SysRole.builder()
                .id(SnowflakeIdGenerator.nextId())
                .roleName(dto.getRoleName())
                .roleCode(dto.getRoleCode())
                .description(dto.getDescription())
                .status(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        roleMapper.insert(role);
        log.info("新增角色成功: {}", dto.getRoleName());
    }

    @Override
    public void update(Long id, RoleUpdateDTO dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, dto.getRoleName())
                        .ne(SysRole::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "角色名称已存在");
        }

        count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, dto.getRoleCode())
                        .ne(SysRole::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }

        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setUpdatedAt(OffsetDateTime.now());

        roleMapper.updateById(role);
        log.info("更新角色成功: {}", dto.getRoleName());
    }

    @Override
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        // 超级管理员角色不可删除（按 roleCode 判断）
        if ("super_admin".equals(role.getRoleCode())) {
            throw new BusinessException(400, "超级管理员角色不可删除");
        }

        Long adminCount = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getRoleId, id)
        );
        if (adminCount > 0) {
            throw new BusinessException(400, "该角色下仍有 " + adminCount + " 个管理员，请先移除");
        }

        // 硬删除：从数据库彻底删除
        roleMapper.hardDeleteById(id);
        log.info("硬删除角色成功: {}", role.getRoleName());
    }

    @Override
    public void toggleStatus(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        // 切换状态：0→1 或 1→0
        Integer newStatus = role.getStatus() == 1 ? 0 : 1;
        role.setStatus(newStatus);
        role.setUpdatedAt(OffsetDateTime.now());
        roleMapper.updateById(role);
        log.info("切换角色状态成功: {}, 新状态: {}", role.getRoleName(), newStatus == 1 ? "启用" : "禁用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindModules(Long id, RoleModuleBindDTO dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        roleModuleMapper.delete(
                new LambdaQueryWrapper<SysRoleModule>()
                        .eq(SysRoleModule::getRoleId, id)
        );

        List<Long> allModuleIds = new ArrayList<>();
        for (Long moduleId : dto.getModuleIds()) {
            allModuleIds.add(moduleId);
            collectAllDescendantIds(moduleId, allModuleIds);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<SysRoleModule> roleModules = allModuleIds.stream().map(moduleId ->
                SysRoleModule.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .roleId(id)
                        .moduleId(moduleId)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        ).collect(Collectors.toList());
        roleModuleMapper.insertBatch(roleModules);

        log.info("角色绑定模块成功: roleId={}, moduleCount={}", id, allModuleIds.size());

        List<SysAdmin> admins = adminMapper.selectList(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getRoleId, id)
        );
        for (SysAdmin admin : admins) {
            String refreshKey = RedisKeyConstant.getRefreshTokenKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
            redisTemplate.delete(refreshKey);
        }
        log.info("已清除 {} 个管理员的 refreshToken", admins.size());
    }

    private void collectAllDescendantIds(Long parentId, List<Long> result) {
        collectAllDescendantIds(parentId, result, 0);
    }

    private void collectAllDescendantIds(Long parentId, List<Long> result, int depth) {
        if (depth > 5) {
            return;
        }
        List<SysModule> children = moduleMapper.selectList(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getParentId, parentId)
        );
        for (SysModule child : children) {
            if (!result.contains(child.getId())) {
                result.add(child.getId());
                collectAllDescendantIds(child.getId(), result, depth + 1);
            }
        }
    }

    private List<ModuleTreeVO> buildModuleTree(List<SysModule> modules) {
        List<ModuleTreeVO> voList = modules.stream().map(m -> {
            ModuleTreeVO vo = new ModuleTreeVO();
            BeanUtils.copyProperties(m, vo);
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());

        java.util.Map<Long, ModuleTreeVO> voMap = voList.stream()
                .collect(Collectors.toMap(ModuleTreeVO::getId, v -> v));

        List<ModuleTreeVO> tree = new ArrayList<>();
        for (ModuleTreeVO vo : voList) {
            if (vo.getParentId() == null) {
                tree.add(vo);
            } else {
                ModuleTreeVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return tree;
    }
}
