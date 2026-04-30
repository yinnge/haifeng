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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final SysModuleMapper moduleMapper;

    @Override
    public List<ModuleTreeVO> listTree(ModuleQueryDTO dto) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getDeleted, false);

        if (StringUtils.hasText(dto.getModuleCode())) {
            wrapper.like(SysModule::getModuleCode, dto.getModuleCode());
        }

        wrapper.orderByAsc(SysModule::getSortOrder);

        List<SysModule> modules = moduleMapper.selectList(wrapper);
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

        if (dto.getParentId() != null) {
            SysModule parent = moduleMapper.selectById(dto.getParentId());
            if (parent == null || parent.getDeleted()) {
                throw new BusinessException(400, "父模块不存在");
            }
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
                .level(dto.getLevel())
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

        module.setModuleName(dto.getModuleName());
        module.setModuleCode(dto.getModuleCode());
        module.setParentId(dto.getParentId());
        module.setPath(dto.getPath());
        module.setIcon(dto.getIcon());
        module.setSortOrder(dto.getSortOrder());
        module.setLevel(dto.getLevel());
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

        module.setDeleted(true);
        module.setUpdatedAt(OffsetDateTime.now());
        moduleMapper.updateById(module);
        log.info("删除模块成功: {}", module.getModuleName());
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
