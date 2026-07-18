package com.haifeng.admin.service.impl.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.dto.permission.ModuleAddDTO;
import com.haifeng.admin.dto.permission.ModuleQueryDTO;
import com.haifeng.admin.dto.permission.ModuleUpdateDTO;
import com.haifeng.admin.service.permission.ModuleService;
import com.haifeng.admin.vo.permission.ModuleTreeVO;
import com.haifeng.common.entity.permission.SysModule;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysModuleMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final SysModuleMapper moduleMapper;

    @Override
    public List<ModuleTreeVO> listTree(ModuleQueryDTO dto) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();

        boolean hasFilter = false;

        if (StringUtils.hasText(dto.getKeyword())) {
            hasFilter = true;
            String kw = dto.getKeyword();
            if (Boolean.TRUE.equals(dto.getExactMatch())) {
                wrapper.and(w -> w.eq(SysModule::getModuleName, kw)
                        .or().eq(SysModule::getModuleCode, kw)
                        .or().eq(SysModule::getPath, kw));
            } else {
                wrapper.and(w -> w.like(SysModule::getModuleName, kw)
                        .or().like(SysModule::getModuleCode, kw)
                        .or().like(SysModule::getPath, kw));
            }
        } else {
            if (StringUtils.hasText(dto.getModuleName())) {
                hasFilter = true;
                if (Boolean.TRUE.equals(dto.getExactMatch())) {
                    wrapper.eq(SysModule::getModuleName, dto.getModuleName());
                } else {
                    wrapper.like(SysModule::getModuleName, dto.getModuleName());
                }
            }
            if (StringUtils.hasText(dto.getModuleCode())) {
                hasFilter = true;
                if (Boolean.TRUE.equals(dto.getExactMatch())) {
                    wrapper.eq(SysModule::getModuleCode, dto.getModuleCode());
                } else {
                    wrapper.like(SysModule::getModuleCode, dto.getModuleCode());
                }
            }
            if (StringUtils.hasText(dto.getPath())) {
                hasFilter = true;
                if (Boolean.TRUE.equals(dto.getExactMatch())) {
                    wrapper.eq(SysModule::getPath, dto.getPath());
                } else {
                    wrapper.like(SysModule::getPath, dto.getPath());
                }
            }
        }

        if (dto.getStatus() != null) {
            hasFilter = true;
            wrapper.eq(SysModule::getStatus, dto.getStatus());
        }

        if (dto.getLevel() != null) {
            hasFilter = true;
            wrapper.eq(SysModule::getLevel, dto.getLevel());
        }

        wrapper.orderByAsc(SysModule::getSortOrder);

        List<SysModule> modules = moduleMapper.selectList(wrapper);

        if (hasFilter && !modules.isEmpty()) {
            modules = collectAncestors(modules);
        }

        return buildTree(modules);
    }

    @Override
    public void add(ModuleAddDTO dto) {
        Long count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleName, dto.getModuleName())
                        .eq(SysModule::getDeleted, false)
        );
        if (count > 0) {
            throw new BusinessException(400, "模块名称已存在");
        }

        count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleCode, dto.getModuleCode())
        );
        if (count > 0) {
            throw new BusinessException(400, "模块编码已存在");
        }

        Integer level = dto.getLevel();
        if (dto.getParentId() != null) {
            SysModule parent = moduleMapper.selectById(dto.getParentId());
            if (parent == null || parent.getDeleted()) {
                throw new BusinessException(400, "父模块不存在");
            }
            level = parent.getLevel() + 1;
        }

        OffsetDateTime now = OffsetDateTime.now();
        SysModule module = SysModule.builder()
                .id(SnowflakeIdGenerator.nextId())
                .moduleName(dto.getModuleName())
                .moduleCode(dto.getModuleCode())
                .parentId(dto.getParentId())
                .path(dto.getPath())
                .icon(dto.getIcon())
                .sortOrder(dto.getSortOrder())
                .level(level)
                .description(dto.getDescription())
                .status(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        moduleMapper.insert(module);
        log.info("新增模块成功: {}", dto.getModuleName());
    }

    @Override
    public void update(Long id, ModuleUpdateDTO dto) {
        SysModule module = moduleMapper.selectById(id);
        if (module == null || module.getDeleted()) {
            throw new BusinessException(404, "模块不存在");
        }

        Long count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleName, dto.getModuleName())
                        .eq(SysModule::getDeleted, false)
                        .ne(SysModule::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "模块名称已存在");
        }

        count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleCode, dto.getModuleCode())
                        .ne(SysModule::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "模块编码已存在");
        }

        Integer level = dto.getLevel();
        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new BusinessException(400, "不能将自身设为父模块");
            }
            SysModule newParent = moduleMapper.selectById(dto.getParentId());
            if (newParent == null || newParent.getDeleted()) {
                throw new BusinessException(400, "父模块不存在");
            }
            level = newParent.getLevel() + 1;

            if (!dto.getParentId().equals(module.getParentId())) {
                List<SysModule> allModules = moduleMapper.selectList(
                        new LambdaQueryWrapper<SysModule>().eq(SysModule::getDeleted, false)
                );
                Set<Long> descendantIds = collectDescendantIds(id, allModules);
                if (descendantIds.contains(dto.getParentId())) {
                    throw new BusinessException(400, "不能将子模块设为父模块");
                }
            }
        }

        module.setModuleName(dto.getModuleName());
        module.setModuleCode(dto.getModuleCode());
        module.setParentId(dto.getParentId());
        module.setPath(dto.getPath());
        module.setIcon(dto.getIcon());
        module.setSortOrder(dto.getSortOrder());
        module.setLevel(level);
        module.setDescription(dto.getDescription());
        module.setUpdatedAt(OffsetDateTime.now());

        moduleMapper.updateById(module);
        log.info("更新模块成功: {}", dto.getModuleName());
    }

    @Override
    public void delete(Long id) {
        SysModule module = moduleMapper.selectById(id);
        if (module == null || module.getDeleted()) {
            throw new BusinessException(404, "模块不存在");
        }

        Long childCount = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getParentId, id)
                        .eq(SysModule::getDeleted, false)
        );
        if (childCount > 0) {
            throw new BusinessException(400, "该模块下有 " + childCount + " 个子模块，请先删除");
        }

        moduleMapper.hardDeleteById(id);
        log.info("硬删除模块成功: {}", module.getModuleName());
    }

    @Override
    public void toggleStatus(Long id) {
        SysModule module = moduleMapper.selectById(id);
        if (module == null || module.getDeleted()) {
            throw new BusinessException(404, "模块不存在");
        }

        Integer newStatus = module.getStatus() == 1 ? 0 : 1;
        module.setStatus(newStatus);
        module.setUpdatedAt(OffsetDateTime.now());
        moduleMapper.updateById(module);
        log.info("切换模块状态成功: {}, 新状态: {}", module.getModuleName(), newStatus == 1 ? "启用" : "禁用");
    }

    private List<SysModule> collectAncestors(List<SysModule> matched) {
        Set<Long> allIds = matched.stream().map(SysModule::getId).collect(Collectors.toSet());
        List<SysModule> result = new ArrayList<>(matched);

        List<SysModule> current = matched;
        while (true) {
            Set<Long> parentIds = current.stream()
                    .map(SysModule::getParentId)
                    .filter(Objects::nonNull)
                    .filter(id -> !allIds.contains(id))
                    .collect(Collectors.toSet());
            if (parentIds.isEmpty()) break;

            List<SysModule> parents = moduleMapper.selectBatchIds(parentIds);
            if (parents.isEmpty()) break;

            parents.forEach(p -> allIds.add(p.getId()));
            result.addAll(parents);
            current = parents;
        }
        return result;
    }

    private Set<Long> collectDescendantIds(Long parentId, List<SysModule> allModules) {
        Set<Long> descendantIds = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(parentId);

        Map<Long, List<SysModule>> childrenMap = allModules.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.groupingBy(SysModule::getParentId));

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<SysModule> children = childrenMap.get(currentId);
            if (children != null) {
                for (SysModule child : children) {
                    if (descendantIds.add(child.getId())) {
                        queue.add(child.getId());
                    }
                }
            }
        }
        return descendantIds;
    }

    private List<ModuleTreeVO> buildTree(List<SysModule> modules) {
        List<ModuleTreeVO> voList = modules.stream().map(m -> {
            ModuleTreeVO vo = new ModuleTreeVO();
            BeanUtils.copyProperties(m, vo);
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, ModuleTreeVO> voMap = voList.stream()
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
