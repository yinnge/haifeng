package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CivilPositionDetailVO {
    private Long id;
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
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
