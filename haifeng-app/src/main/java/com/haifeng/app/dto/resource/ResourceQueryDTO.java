package com.haifeng.app.dto.resource;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端资源列表查询 DTO
 * category 精准匹配
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceQueryDTO extends BasePageQueryDTO {

    /** 资源名称模糊查询 */
    private String resourceName;

    /** 资源分类精准匹配 */
    private String category;
}
