package com.haifeng.common.entity.employment.grassrootsPosition;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName(value = "t_grassroots_project_position", autoResultMap = true)
public class GrassrootsProjectPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String serviceStartDate;
    private String serviceEndDate;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String householdRequirement;
    private String otherRequirement;
    private String politicalStatus;
    private String examContent;
    private OffsetDateTime examTime;
    private String interviewForm;
    private String monthlySubsidy;
    private String socialInsurance;
    private String housingInfo;
    private String otherBenefits;
    private String afterServicePolicy;
    private Boolean canTransferToCivil;
    private Boolean canTransferToInstitution;
    private String examBonusPoints;
    private String tuitionCompensation;
    private String postgradBonus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyLink;
    private String positionStatus;
    private String contactPhone;
    private String remark;
    private String content;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
