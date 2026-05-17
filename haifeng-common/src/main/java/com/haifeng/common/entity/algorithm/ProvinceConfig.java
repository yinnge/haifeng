package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_province_config")
public class ProvinceConfig {

    @TableId(type = IdType.INPUT)
    private String province;

    private BigDecimal densityK;
    private BigDecimal lineSteepness;
    private BigDecimal rankSteepness;
    private OffsetDateTime createdAt;
}
