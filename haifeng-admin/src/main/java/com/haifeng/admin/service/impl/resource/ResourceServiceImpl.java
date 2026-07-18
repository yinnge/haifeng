package com.haifeng.admin.service.impl.resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.resource.ResourceAddDTO;
import com.haifeng.admin.dto.resource.ResourceQueryDTO;
import com.haifeng.admin.dto.resource.ResourceStatusDTO;
import com.haifeng.admin.dto.resource.ResourceUpdateDTO;
import com.haifeng.admin.service.resource.ResourceService;
import com.haifeng.admin.vo.resource.ResourceDetailVO;
import com.haifeng.admin.vo.resource.ResourceListVO;
import com.haifeng.common.entity.resource.Resource;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.resource.ResourceMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;

    @Override
    public IPage<ResourceListVO> page(ResourceQueryDTO dto) {
        Page<Resource> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();

        // 资源名称模糊查询
        if (StringUtils.hasText(dto.getResourceName())) {
            wrapper.like(Resource::getResourceName, dto.getResourceName());
        }
        // 分类模糊查询
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.like(Resource::getCategory, dto.getCategory());
        }
        // 删除状态筛选
        if (dto.getIsDeleted() != null) {
            wrapper.eq(Resource::getIsDeleted, dto.getIsDeleted());
        }

        // 按排序序号升序，更新时间降序
        wrapper.orderByAsc(Resource::getSortOrder)
               .orderByDesc(Resource::getUpdatedAt);

        IPage<Resource> resourcePage = resourceMapper.selectPage(page, wrapper);

        return resourcePage.convert(resource -> ResourceListVO.builder()
                .id(resource.getId())
                .resourceName(resource.getResourceName())
                .category(resource.getCategory())
                .fileType(resource.getFileType())
                .viewCount(resource.getViewCount())
                .sortOrder(resource.getSortOrder())
                .isDeleted(resource.getIsDeleted())
                .updatedAt(resource.getUpdatedAt())
                .build());
    }

    @Override
    public ResourceDetailVO detail(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        ResourceDetailVO vo = ResourceDetailVO.builder()
                .id(resource.getId())
                .resourceName(resource.getResourceName())
                .coverUrl(resource.getCoverUrl())
                .description(resource.getDescription())
                .resourceUrl(resource.getResourceUrl())
                .accessCode(resource.getAccessCode())
                .category(resource.getCategory())
                .fileType(resource.getFileType())
                .viewCount(resource.getViewCount())
                .sortOrder(resource.getSortOrder())
                .isDeleted(resource.getIsDeleted())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();

        if (Boolean.TRUE.equals(resource.getIsDeleted())) {
            log.warn("查看已删除资源详情: id={}", id);
        }

        return vo;
    }

    @Override
    public Long add(ResourceAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Resource resource = Resource.builder()
                .id(id)
                .resourceName(dto.getResourceName())
                .coverUrl(dto.getCoverUrl())
                .description(dto.getDescription())
                .resourceUrl(dto.getResourceUrl())
                .accessCode(dto.getAccessCode())
                .category(dto.getCategory())
                .fileType(dto.getFileType())
                .viewCount(0)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        resourceMapper.insert(resource);

        log.info("新增资源成功: id={}, resourceName={}", id, dto.getResourceName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ResourceUpdateDTO dto) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        resource.setResourceName(dto.getResourceName());
        resource.setCoverUrl(dto.getCoverUrl());
        resource.setDescription(dto.getDescription());
        resource.setResourceUrl(dto.getResourceUrl());
        resource.setAccessCode(dto.getAccessCode());
        resource.setCategory(dto.getCategory());
        resource.setFileType(dto.getFileType());
        if (dto.getSortOrder() != null) {
            resource.setSortOrder(dto.getSortOrder());
        }
        resource.setUpdatedAt(OffsetDateTime.now());

        resourceMapper.updateById(resource);

        log.info("更新资源成功: id={}, resourceName={}", id, dto.getResourceName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, ResourceStatusDTO dto) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        resource.setIsDeleted(dto.getIsDeleted());
        resource.setUpdatedAt(OffsetDateTime.now());

        resourceMapper.updateById(resource);

        log.info("更新资源状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "资源已删除");
        }

        resource.setIsDeleted(true);
        resource.setUpdatedAt(OffsetDateTime.now());
        resourceMapper.updateById(resource);

        log.info("软删除资源成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要删除的资源");
        }

        OffsetDateTime now = OffsetDateTime.now();
        int updated = resourceMapper.batchSoftDelete(ids, now);

        log.info("批量软删除资源成功: 更新数量={}, ids={}", updated, ids);
    }

    @Override
    public List<String> getCategories() {
        List<String> categories = resourceMapper.selectDistinctCategories();
        log.debug("查询资源分类列表: 数量={}", categories.size());
        return categories;
    }
}
