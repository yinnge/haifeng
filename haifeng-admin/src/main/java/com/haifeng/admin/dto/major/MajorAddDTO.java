package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 专业新增DTO
 */
@Data
public class MajorAddDTO {

    /**
     * 专业代码
     */
    @NotBlank(message = "专业代码不能为空")
    @Size(max = 20, message = "专业代码长度不能超过20")
    private String majorCode;

    /**
     * 专业名称
     */
    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String majorName;

    /**
     * 学科名称
     */
    @Size(max = 100, message = "学科名称长度不能超过100")
    private String disciplineName;

    /**
     * 专业类型
     */
    @NotBlank(message = "专业类型不能为空")
    @Size(max = 30, message = "专业类型长度不能超过30")
    private String majorType;

    /**
     * 学科门类
     */
    @Size(max = 50, message = "学科门类长度不能超过50")
    private String majorCategory;

    /**
     * 专业类
     */
    @Size(max = 50, message = "专业类长度不能超过50")
    private String parentCategory;

    /**
     * 专业标签
     */
    @Size(max = 50, message = "专业标签长度不能超过50")
    private String majorTags;

    /**
     * 授予学位
     */
    @Size(max = 50, message = "授予学位长度不能超过50")
    private String degreeAwarded;

    /**
     * 就业率
     */
    @DecimalMin(value = "0", message = "就业率不能小于0")
    @DecimalMax(value = "100", message = "就业率不能大于100")
    private BigDecimal employmentRate;

    /**
     * 薪资下限
     */
    @Min(value = 0, message = "薪资下限不能小于0")
    private Integer salaryMin;

    /**
     * 薪资上限
     */
    @Min(value = 0, message = "薪资上限不能小于0")
    private Integer salaryMax;

    /**
     * 专业描述
     */
    private String description;
}
