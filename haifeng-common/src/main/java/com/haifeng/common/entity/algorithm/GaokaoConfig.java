package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BigDecimal> yearWeights;

    private OffsetDateTime createdAt;
}
