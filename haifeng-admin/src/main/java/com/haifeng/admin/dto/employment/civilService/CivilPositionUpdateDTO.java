package com.haifeng.admin.dto.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CivilPositionUpdateDTO {
    private String positionName;
    private String examType;
    private String recruitingDept;
    private String deptCode;
    private String positionCode;
    private String affiliatedBureau;
    private String majorRequirement;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String workExperience;
    private String grassrootsExperience;
    private String examCategory;
    private String interviewRatio;
    private Integer recruitmentCount;
    private Boolean hasProfessionalTest;
    private String workLocation;
    private String workLocationDetail;
    private String householdRequirement;
    private String householdLocation;
    private String positionIntro;
    private String remark;
    private String officialWebsite;
    private String contactPhone;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
    private Integer applicantCount;
    private Integer sortOrder;
}
