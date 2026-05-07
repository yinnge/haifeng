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
@TableName("t_postgrad_major_university")
public class PostgradMajorUniversity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long postgradMajorId;

    private Long universityId;

    private String universityName;

    private String postgradMajorName;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
