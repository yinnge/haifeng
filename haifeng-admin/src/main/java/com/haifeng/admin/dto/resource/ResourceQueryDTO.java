package com.haifeng.admin.dto.resource;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceQueryDTO extends BasePageQueryDTO {
    /**
     * 资源名称模糊查询
     */
    @Size(max = 50, message = "资源名称查询最长50字符")
    private String resourceName;

    /**
     * 分类模糊查询
     */
    @Size(max = 50, message = "分类查询最长50字符")
    private String category;

    /**
     * 删除状态筛选: true-已删除 false-正常
     */
    private Boolean isDeleted;
}
