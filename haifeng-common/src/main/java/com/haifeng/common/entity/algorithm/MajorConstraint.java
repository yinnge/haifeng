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
@TableName("t_major_constraint")
public class MajorConstraint {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
