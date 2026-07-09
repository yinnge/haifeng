package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GaokaoConfigDetailVO {
    private BigDecimal defaultDensityK;
    private BigDecimal defaultLineSteepness;
    private BigDecimal defaultRankSteepness;
    private BigDecimal newGaokaoLineWeight;
    private BigDecimal newGaokaoRankWeight;
    private BigDecimal oldGaokaoLineWeight;
    private BigDecimal oldGaokaoRankWeight;
    private BigDecimal weightSoftGroup;
    private BigDecimal weightSoftBoth;
    private List<BigDecimal> yearWeights;
    private OffsetDateTime createdAt;
}
