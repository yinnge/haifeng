package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_constraint_dict")
public class ConstraintDict {

    @TableId(type = IdType.INPUT)
    private String code;
    private String name;
    private String category;
    private String description;
    private String severity;
    private String checkField;
    private String checkOperator;
    private String checkValue;
    private String extraField;
    private String extraOperator;
    private String extraValue;
    private Integer sortOrder;
    private Boolean isActive;
    @TableLogic
    private Boolean isDeleted;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
