package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConstraintDictAddDTO {
    @NotBlank(message = "约束代码不能为空")
    @Size(max = 50, message = "约束代码最长50字符")
    private String code;

    @NotBlank(message = "约束名称不能为空")
    @Size(max = 100, message = "约束名称最长100字符")
    private String name;

    @NotBlank(message = "约束大类不能为空")
    @Size(max = 30, message = "约束大类最长30字符")
    private String category;

    private String description;

    @NotBlank(message = "severity不能为空")
    @Pattern(regexp = "^(HARD|SOFT)$", message = "severity只能是HARD或SOFT")
    private String severity = "HARD";

    @Size(max = 50, message = "check_field最长50字符")
    private String checkField;

    @Size(max = 20, message = "check_operator最长20字符")
    private String checkOperator;

    @Size(max = 100, message = "check_value最长100字符")
    private String checkValue;

    @Size(max = 50, message = "extra_field最长50字符")
    private String extraField;

    @Size(max = 20, message = "extra_operator最长20字符")
    private String extraOperator;

    @Size(max = 100, message = "extra_value最长100字符")
    private String extraValue;

    @Min(value = 0, message = "排序号不能为负数")
    private Integer sortOrder = 0;

    @NotNull(message = "启用状态不能为空")
    private Boolean isActive = true;
}
