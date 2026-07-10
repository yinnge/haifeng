package com.haifeng.app.dto.resource;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "资源名称长度不能超过100")
    private String resourceName;

    /** 资源分类精准匹配 */
    @Size(max = 50, message = "资源分类长度不能超过50")
    private String category;
}
