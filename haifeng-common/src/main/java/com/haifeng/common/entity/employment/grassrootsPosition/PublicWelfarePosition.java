package com.haifeng.common.entity.employment.grassrootsPosition;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_public_welfare_position", autoResultMap = true)
public class PublicWelfarePosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String workContent;
    private String province;
    private String city;
    private String district;
    private String workLocation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] targetGroup;

    private String educationRequirement;
    private String ageRange;
    private String healthRequirement;
    private Integer recruitmentCount;
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    private String monthlySalary;
    private String salarySource;
    private String subsidyStandard;
    private String socialInsuranceInfo;
    private String otherBenefits;
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    private String applyAddress;
    private String requiredDocuments;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;

    private Integer sortOrder;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
