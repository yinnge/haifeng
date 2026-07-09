package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProvinceConfigListVO {
    private String province;
    private BigDecimal densityK;
    private BigDecimal lineSteepness;
    private BigDecimal rankSteepness;
}
