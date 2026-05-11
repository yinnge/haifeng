package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SafetyLevelAddDTO {

    @NotNull(message = "等级不能为空")
    @Min(value = 1, message = "等级最小为1")
    @Max(value = 10, message = "等级最大为10")
    private Short level;

    @NotBlank(message = "代码不能为空")
    @Size(max = 20, message = "代码最长20字符")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Size(max = 30, message = "名称最长30字符")
    private String name;

    @NotBlank(message = "简称不能为空")
    @Size(max = 10, message = "简称最长10字符")
    private String nameShort;

    @NotNull(message = "系数下界不能为空")
    @DecimalMin(value = "0.00", message = "系数下界最小为0")
    @DecimalMax(value = "1.00", message = "系数下界最大为1")
    private BigDecimal minCoefficient;

    @NotNull(message = "系数上界不能为空")
    @DecimalMin(value = "0.00", message = "系数上界最小为0")
    @DecimalMax(value = "1.00", message = "系数上界最大为1")
    private BigDecimal maxCoefficient;

    @Size(max = 20, message = "颜色最长20字符")
    private String color;

    @Size(max = 20, message = "置信度最长20字符")
    private String confidence;

    @Size(max = 150, message = "置信度说明最长150字符")
    private String confidenceReason;

    private String description;
}
