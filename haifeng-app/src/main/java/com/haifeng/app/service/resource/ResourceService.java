package com.haifeng.app.service.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.resource.ResourceQueryDTO;
import com.haifeng.app.vo.resource.ResourceListVO;
import com.haifeng.app.vo.resource.ResourceUrlVO;

import java.util.List;

public interface ResourceService {

    /**
     * 分页查询资源列表（isDeleted=false）；category EQ；排序 sort_order ASC, created_at DESC
     */
    IPage<ResourceListVO> page(ResourceQueryDTO dto);

    /**
     * 查看资源 URL：根据 id 获取 resourceUrl + accessCode，同步 view_count + 1
     * 不存在或已删除 → BusinessException(NOT_FOUND)
     */
    ResourceUrlVO getUrl(Long id);

    /**
     * 获取所有不重复的分类（用于前端下拉筛选）
     */
    List<String> getCategories();
}
