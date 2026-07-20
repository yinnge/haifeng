package com.haifeng.common.entity.special;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.StringArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_strong_base_university", autoResultMap = true)
public class StrongBaseUniversity {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long universityId;

    private String universityName;

    private Boolean isPilot;

    private Short pilotYear;

    private String officialUrl;

    private String signupUrl;

    private Boolean testBeforeScore;

    private String defaultEntryRatio;

    private String defaultAdmissionFormula;

    @TableField(typeHandler = StringArrayTypeHandler.class)
    private String[] availableMajors;

    private String specialNotes;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
