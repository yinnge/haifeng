package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
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

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
