package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GaokaoConfigUpdateDTO {

    @NotNull(message = "默认同分密度惩罚系数不能为空")
    @DecimalMin(value = "0.000", message = "默认同分密度惩罚系数不能小于0")
    @DecimalMax(value = "1.000", message = "默认同分密度惩罚系数不能大于1")
    private BigDecimal defaultDensityK;

    @NotNull(message = "默认线差Sigmoid陡度不能为空")
    @DecimalMin(value = "0.00", message = "默认线差Sigmoid陡度不能小于0")
    @DecimalMax(value = "10.00", message = "默认线差Sigmoid陡度不能大于10")
    private BigDecimal defaultLineSteepness;

    @NotNull(message = "默认位次Sigmoid陡度不能为空")
    @DecimalMin(value = "0.00", message = "默认位次Sigmoid陡度不能小于0")
    @DecimalMax(value = "10.00", message = "默认位次Sigmoid陡度不能大于10")
    private BigDecimal defaultRankSteepness;

    @NotNull(message = "新高考线差权重不能为空")
    @DecimalMin(value = "0.00", message = "新高考线差权重不能小于0")
    @DecimalMax(value = "1.00", message = "新高考线差权重不能大于1")
    private BigDecimal newGaokaoLineWeight;

    @NotNull(message = "新高考位次权重不能为空")
    @DecimalMin(value = "0.00", message = "新高考位次权重不能小于0")
    @DecimalMax(value = "1.00", message = "新高考位次权重不能大于1")
    private BigDecimal newGaokaoRankWeight;

    @NotNull(message = "旧高考线差权重不能为空")
    @DecimalMin(value = "0.00", message = "旧高考线差权重不能小于0")
    @DecimalMax(value = "1.00", message = "旧高考线差权重不能大于1")
    private BigDecimal oldGaokaoLineWeight;

    @NotNull(message = "旧高考位次权重不能为空")
    @DecimalMin(value = "0.00", message = "旧高考位次权重不能小于0")
    @DecimalMax(value = "1.00", message = "旧高考位次权重不能大于1")
    private BigDecimal oldGaokaoRankWeight;

    @NotNull(message = "专业组软约束折扣不能为空")
    @DecimalMin(value = "0.0", message = "专业组软约束折扣不能小于0")
    @DecimalMax(value = "1.0", message = "专业组软约束折扣不能大于1")
    private BigDecimal weightSoftGroup;

    @NotNull(message = "专业组+专业软约束折扣不能为空")
    @DecimalMin(value = "0.0", message = "专业组+专业软约束折扣不能小于0")
    @DecimalMax(value = "1.0", message = "专业组+专业软约束折扣不能大于1")
    private BigDecimal weightSoftBoth;

    @NotNull(message = "年份衰减权重不能为空")
    private List<BigDecimal> yearWeights;
}
