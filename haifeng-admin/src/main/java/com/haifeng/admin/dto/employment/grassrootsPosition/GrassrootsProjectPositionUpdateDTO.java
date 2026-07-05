package com.haifeng.admin.dto.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionUpdateDTO {
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
    private String politicalStatus;
    private String otherRequirement;
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
    private Integer sortOrder;
}
