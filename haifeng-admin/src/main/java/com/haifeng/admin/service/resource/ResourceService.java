package com.haifeng.admin.service.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.resource.ResourceAddDTO;
import com.haifeng.admin.dto.resource.ResourceQueryDTO;
import com.haifeng.admin.dto.resource.ResourceStatusDTO;
import com.haifeng.admin.dto.resource.ResourceUpdateDTO;
import com.haifeng.admin.vo.resource.ResourceDetailVO;
import com.haifeng.admin.vo.resource.ResourceListVO;

import java.util.List;

public interface ResourceService {

    /**
     * 分页查询资源列表
     */
    IPage<ResourceListVO> page(ResourceQueryDTO dto);

    /**
     * 获取资源详情
     */
    ResourceDetailVO detail(Long id);

    /**
     * 新增资源
     */
    Long add(ResourceAddDTO dto);

    /**
     * 更新资源
     */
    void update(Long id, ResourceUpdateDTO dto);

    /**
     * 更新资源状态（禁用/启用）
     */
    void updateStatus(Long id, ResourceStatusDTO dto);

    /**
     * 硬删除资源
     */
    void delete(Long id);

    /**
     * 批量硬删除资源
     */
    void batchDelete(List<Long> ids);

    /**
     * 获取所有不重复的分类（用于前端下拉筛选）
     */
    List<String> getCategories();
}
