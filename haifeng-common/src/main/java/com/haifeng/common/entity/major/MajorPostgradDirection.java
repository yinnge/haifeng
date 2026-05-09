package com.haifeng.common.entity.major;

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
@TableName("t_major_postgrad_direction")
public class MajorPostgradDirection {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long majorId;

    private Long postgradMajorId;

    private String majorName;

    private String postgradMajorName;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
