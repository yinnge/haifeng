package com.haifeng.common.entity.major;

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
@TableName("t_major")
public class Major {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private String parentCategory;

    private String majorTags;

    private String degreeAwarded;

    private String studyDuration;

    private BigDecimal employmentRate;

    private Integer salaryMin;

    private Integer salaryMax;

    private String description;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
