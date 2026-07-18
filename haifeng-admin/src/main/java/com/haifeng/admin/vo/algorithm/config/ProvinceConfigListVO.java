package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ProvinceConfigListVO {
    private String province;
    private BigDecimal densityK;
    private BigDecimal lineSteepness;
    private BigDecimal rankSteepness;
    private OffsetDateTime updatedAt;
}
