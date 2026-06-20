package com.haifeng.app.vo.employment.industryPosition;

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
public class HealthcarePositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

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
}
