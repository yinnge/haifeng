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
@TableName("t_safety_level_dict")
public class SafetyLevelDict {

    @TableId(type = IdType.INPUT)
    private Short level;
    private String code;
    private String name;
    private String nameShort;
    private BigDecimal minCoefficient;
    private BigDecimal maxCoefficient;
    private String color;
    private String confidence;
    private String confidenceReason;
    private String description;
    @TableLogic
    private Boolean isDeleted;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
