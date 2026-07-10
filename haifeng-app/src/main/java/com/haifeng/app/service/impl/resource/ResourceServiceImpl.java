package com.haifeng.app.service.impl.resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.resource.ResourceQueryDTO;
import com.haifeng.app.service.resource.ResourceService;
import com.haifeng.app.vo.resource.ResourceListVO;
import com.haifeng.app.vo.resource.ResourceUrlVO;
import com.haifeng.common.entity.resource.Resource;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.resource.ResourceMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;

    @Override
    public IPage<ResourceListVO> page(ResourceQueryDTO dto) {
        Page<Resource> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<Resource>()
                .eq(Resource::getIsDeleted, false)
                .like(StringUtils.hasText(dto.getResourceName()), Resource::getResourceName, dto.getResourceName())
                .eq(StringUtils.hasText(dto.getCategory()), Resource::getCategory, dto.getCategory())
                .orderByAsc("sort_order ASC NULLS LAST")
                .orderByDesc("created_at DESC NULLS LAST");

        IPage<Resource> entityPage = resourceMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public ResourceUrlVO getUrl(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || Boolean.TRUE.equals(resource.getIsDeleted())) {
            log.debug("资源不存在或已删除, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }

        // 原子更新浏览计数
        resourceMapper.incrementViewCount(id);

        return ResourceUrlVO.builder()
                .resourceUrl(resource.getResourceUrl())
                .accessCode(resource.getAccessCode())
                .build();
    }

    private ResourceListVO toListVO(Resource e) {
        return ResourceListVO.builder()
                .id(e.getId())
                .resourceName(e.getResourceName())
                .coverUrl(e.getCoverUrl())
                .description(e.getDescription())
                .category(e.getCategory())
                .fileType(e.getFileType())
                .viewCount(e.getViewCount())
                .build();
    }

    @Override
    public List<String> getCategories() {
        return resourceMapper.selectDistinctCategories();
    }
}
