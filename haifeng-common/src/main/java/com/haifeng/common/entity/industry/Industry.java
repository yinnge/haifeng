package com.haifeng.common.entity.industry;

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
@TableName(value = "t_industry", autoResultMap = true)
public class Industry {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String industryName;

    private String category;

    private String iconClass;

    private String description;

    private BigDecimal annualGrowthRate;

    private String marketScale;

    private String talentGap;

    private BigDecimal investmentHeat;

    private String growthTrend;

    private String marketTrend;

    private String talentTrend;

    private String investmentTrend;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
