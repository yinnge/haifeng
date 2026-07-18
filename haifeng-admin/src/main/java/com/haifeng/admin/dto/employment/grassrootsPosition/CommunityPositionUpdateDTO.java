package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CommunityPositionUpdateDTO {
    @Size(max = 200)
    private String streetOffice;
    @Size(max = 200)
    private String communityName;
    @Size(max = 200)
    private String supervisingDept;
    @Size(max = 100)
    private String district;
    @Size(max = 200)
    private String positionName;
    @Size(max = 50)
    private String positionType;
    @Size(max = 30)
    private String employmentType;
    @Size(max = 30)
    private String province;
    @Size(max = 50)
    private String city;
    @Size(max = 200)
    private String workLocation;
    @Size(max = 30)
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    @Size(max = 500)
    private String majorRequirement;
    @Size(max = 100)
    private String householdRequirement;
    @Size(max = 30)
    private String politicalStatus;
    @Size(max = 50)
    private String workExperience;
    @Size(max = 50)
    private String socialWorkCert;
    @Size(max = 100)
    private String communityExperience;
    @Size(max = 200)
    private String residenceRequirement;
    @Size(max = 50)
    private String salaryRange;
    @Size(max = 200)
    private String salaryComposition;
    private String benefits;
    @Size(max = 500)
    private String examContent;
    @Size(max = 100)
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    @Size(max = 20)
    private String positionStatus;
    @Size(max = 500)
    private String applyLink;
    private String applyMethod;
    @Size(max = 50)
    private String contactPhone;
    @Size(max = 200)
    private String contactAddress;
    private String remark;
    private String content;
    private Integer sortOrder;
}
