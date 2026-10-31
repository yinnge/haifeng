package com.haifeng.common.entity.certificate;

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
@TableName("t_competition")
public class Competition {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String compName;

    private String compLevel;

    private String registrationTime;

    @TableField("is_deleted")
    private Boolean isDeleted;

    /** 排序权重（越大越靠前，C 端热点优先展示） */
    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
