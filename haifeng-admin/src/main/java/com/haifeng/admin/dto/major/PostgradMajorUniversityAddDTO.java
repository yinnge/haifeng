package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 考研专业-大学关联新增DTO
 */
@Data
public class PostgradMajorUniversityAddDTO {

    /**
     * 考研专业ID
     */
    @NotNull(message = "考研专业不能为空")
    private Long postgradMajorId;

    /**
     * 大学ID
     */
    @NotNull(message = "大学不能为空")
    private Long universityId;

    /**
     * 排序权重（数值越大越靠前）
     */
    private Integer sortOrder;
}
