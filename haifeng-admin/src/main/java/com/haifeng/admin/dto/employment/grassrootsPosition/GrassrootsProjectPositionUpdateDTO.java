package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionUpdateDTO {
    @Size(max = 30)
    private String projectType;
    @Size(max = 10)
    private String year;
    @Size(max = 200)
    private String positionName;
    @Size(max = 50)
    private String serviceType;
    @Size(max = 200)
    private String organizingDept;
    @Size(max = 200)
    private String serviceUnit;
    @Size(max = 30)
    private String province;
    @Size(max = 50)
    private String city;
    @Size(max = 50)
    private String county;
    @Size(max = 100)
    private String township;
    @Size(max = 30)
    private String servicePeriod;
    @Size(max = 30)
    private String serviceStartDate;
    @Size(max = 30)
    private String serviceEndDate;
    @Size(max = 30)
    private String educationRequirement;
    @Size(max = 500)
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    @Size(max = 50)
    private String gradYearRequirement;
    @Size(max = 100)
    private String householdRequirement;
    private String otherRequirement;
    @Size(max = 30)
    private String politicalStatus;
    @Size(max = 500)
    private String examContent;
    private OffsetDateTime examTime;
    @Size(max = 100)
    private String interviewForm;
    @Size(max = 50)
    private String monthlySubsidy;
    @Size(max = 200)
    private String socialInsurance;
    @Size(max = 200)
    private String housingInfo;
    private String otherBenefits;
    private String afterServicePolicy;
    private Boolean canTransferToCivil;
    private Boolean canTransferToInstitution;
    @Size(max = 50)
    private String examBonusPoints;
    @Size(max = 100)
    private String tuitionCompensation;
    @Size(max = 100)
    private String postgradBonus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    @Size(max = 500)
    private String applyLink;
    @Size(max = 20)
    private String positionStatus;
    @Size(max = 50)
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
}
