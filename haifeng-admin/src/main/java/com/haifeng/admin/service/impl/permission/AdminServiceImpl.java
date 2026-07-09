package com.haifeng.admin.service.impl.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.service.permission.AdminService;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.entity.permission.SysRole;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.mapper.permission.SysRoleMapper;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    @Override
    public IPage<AdminListVO> page(AdminQueryDTO dto) {
        Page<SysAdmin> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getDeleted, false);

        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(SysAdmin::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.eq(SysAdmin::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            wrapper.like(SysAdmin::getRealName, dto.getRealName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysAdmin::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(SysAdmin::getCreatedAt);

        IPage<SysAdmin> adminPage = adminMapper.selectPage(page, wrapper);

        return adminPage.convert(admin -> {
            AdminListVO vo = new AdminListVO();
            BeanUtils.copyProperties(admin, vo);
            return vo;
        });
    }

    @Override
    public AdminDetailVO detail(Long id) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        AdminDetailVO vo = new AdminDetailVO();
        BeanUtils.copyProperties(admin, vo);
        return vo;
    }

    @Override
    public void add(AdminAddDTO dto) {
        // 检查用户名是否重复
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getUsername, dto.getUsername())
                        .eq(SysAdmin::getDeleted, false)
        );
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查手机号是否重复
        count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .eq(SysAdmin::getDeleted, false)
        );
        if (count > 0) {
            throw new BusinessException(400, "手机号已存在");
        }

        // 查询角色
        SysRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null || role.getDeleted()) {
            throw new BusinessException(400, "角色不存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        SysAdmin admin = SysAdmin.builder()
                .id(SnowflakeIdGenerator.nextId())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .realName(dto.getRealName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .avatar(dto.getAvatar())
                .roleId(dto.getRoleId())
                .roleName(role.getRoleName())
                .status(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        adminMapper.insert(admin);
        log.info("新增管理员成功: {}", dto.getUsername());
    }

    @Override
    public void update(Long id, AdminUpdateDTO dto) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 超级管理员角色不可变更（按 role_code 判断，不依赖固定 ID）
        if (dto.getRoleId() != null && !admin.getRoleId().equals(dto.getRoleId())) {
            SysRole currentRole = roleMapper.selectById(admin.getRoleId());
            if (currentRole != null && "super_admin".equals(currentRole.getRoleCode())) {
                throw new BusinessException(400, "超级管理员角色不可变更");
            }
        }

        // 检查用户名是否重复（排除自己）
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getUsername, dto.getUsername())
                        .eq(SysAdmin::getDeleted, false)
                        .ne(SysAdmin::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查手机号是否重复（排除自己）
        count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .eq(SysAdmin::getDeleted, false)
                        .ne(SysAdmin::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "手机号已存在");
        }

        // 查询目标角色
        SysRole targetRole = roleMapper.selectById(dto.getRoleId());
        if (targetRole == null || targetRole.getDeleted()) {
            throw new BusinessException(400, "角色不存在");
        }

        admin.setUsername(dto.getUsername());
        if (StringUtils.hasText(dto.getPassword())) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
            admin.setTokenVersion(admin.getTokenVersion() == null ? 1 : admin.getTokenVersion() + 1);
            String refreshKey = RedisKeyConstant.getRefreshTokenKey(id, JwtUtil.USER_TYPE_ADMIN);
            redisTemplate.delete(refreshKey);
            String versionKey = RedisKeyConstant.getTokenVersionKey(id, JwtUtil.USER_TYPE_ADMIN);
            redisTemplate.opsForValue().increment(versionKey);
        }
        admin.setRealName(dto.getRealName());
        admin.setPhone(dto.getPhone());
        admin.setEmail(dto.getEmail());
        admin.setAvatar(dto.getAvatar());
        admin.setRoleId(dto.getRoleId());
        admin.setRoleName(targetRole.getRoleName());
        if (dto.getStatus() != null) {
            admin.setStatus(dto.getStatus());
        }
        admin.setUpdatedAt(OffsetDateTime.now());

        adminMapper.updateById(admin);
        log.info("更新管理员成功: {}", dto.getUsername());
    }

    @Override
    public void delete(Long id) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 超级管理员不可删除（按 role_code 判断）
        SysRole role = roleMapper.selectById(admin.getRoleId());
        if (role != null && "super_admin".equals(role.getRoleCode())) {
            throw new BusinessException(400, "超级管理员不可删除");
        }

        // 硬删除：从数据库彻底删除
        adminMapper.hardDeleteById(id);
        log.info("硬删除管理员成功: {}", admin.getUsername());
    }

    @Override
    public void toggleStatus(Long id) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 超级管理员不可禁用（按 role_code 判断）
        SysRole role = roleMapper.selectById(admin.getRoleId());
        if (role != null && "super_admin".equals(role.getRoleCode())) {
            throw new BusinessException(400, "超级管理员不可禁用");
        }

        // 切换状态：0→1 或 1→0
        Integer newStatus = admin.getStatus() == 1 ? 0 : 1;
        admin.setStatus(newStatus);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);
        log.info("切换管理员状态成功: {}, 新状态: {}", admin.getUsername(), newStatus == 1 ? "启用" : "禁用");
    }
}
