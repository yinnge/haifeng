package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.BigDecimalListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "gaokao_config", autoResultMap = true)
public class GaokaoConfig {

    @TableId(type = IdType.INPUT)
    private Short id;

    private BigDecimal defaultDensityK;
    private BigDecimal defaultLineSteepness;
    private BigDecimal defaultRankSteepness;
    private BigDecimal newGaokaoLineWeight;
    private BigDecimal newGaokaoRankWeight;
    private BigDecimal oldGaokaoLineWeight;
    private BigDecimal oldGaokaoRankWeight;
    private BigDecimal weightSoftGroup;
    private BigDecimal weightSoftBoth;

    @TableField(typeHandler = BigDecimalListTypeHandler.class)
    private List<BigDecimal> yearWeights;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
