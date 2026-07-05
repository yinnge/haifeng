package com.haifeng.admin.dto.employment.industryPosition.healthcare;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HealthcarePositionUpdateDTO {
    private String institutionName;
    private String institutionType;
    private String institutionLevel;
    private String institutionNature;
    private String positionName;
    private String department;
    private String positionCategory;
    private String recruitmentType;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String workExperience;
    private String licenseRequirement;
    private String titleRequirement;
    private String internshipRequirement;
    private String researchRequirement;
    private String salaryRange;
    private String benefits;
    private String housingSubsidy;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String examContent;
    private String applyLink;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;
    private Integer sortOrder;
}
