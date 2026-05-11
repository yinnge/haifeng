package com.haifeng.common.entity.special;

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
@TableName("t_strong_base_score")
public class StrongBaseScore {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long universityId;

    private String universityName;

    private Short year;

    private String province;

    private String subjectType;

    private String majorName;

    private String majorCode;

    private BigDecimal entryScore;

    private String entryScoreType;

    private String entryFormula;

    private String entryRatio;

    private BigDecimal admissionScore;

    private String admissionFormula;

    private Integer planCount;

    private Integer admissionCount;

    private String remark;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
