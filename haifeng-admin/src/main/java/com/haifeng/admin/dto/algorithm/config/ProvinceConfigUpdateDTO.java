package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProvinceConfigUpdateDTO {

    @NotNull(message = "同分密度惩罚系数不能为空")
    @DecimalMin(value = "0.000", message = "同分密度惩罚系数不能小于0")
    @DecimalMax(value = "1.000", message = "同分密度惩罚系数不能大于1")
    private BigDecimal densityK;

    @NotNull(message = "线差Sigmoid陡度不能为空")
    @DecimalMin(value = "0.00", message = "线差Sigmoid陡度不能小于0")
    @DecimalMax(value = "10.00", message = "线差Sigmoid陡度不能大于10")
    private BigDecimal lineSteepness;

    @NotNull(message = "位次Sigmoid陡度不能为空")
    @DecimalMin(value = "0.00", message = "位次Sigmoid陡度不能小于0")
    @DecimalMax(value = "10.00", message = "位次Sigmoid陡度不能大于10")
    private BigDecimal rankSteepness;
}
