package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CommunityPositionAddDTO {
    @NotBlank(message = "街道办事处乡镇不能为空")
    @Size(max = 200, message = "街道办事处乡镇长度不能超过200")
    private String streetOffice;
    @Size(max = 200)
    private String communityName;
    @Size(max = 200)
    private String supervisingDept;
    @Size(max = 100)
    private String district;
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 200, message = "岗位名称长度不能超过200")
    private String positionName;
    @NotBlank(message = "岗位类型不能为空")
    @Size(max = 50, message = "岗位类型长度不能超过50")
    private String positionType;
    @NotBlank(message = "用工形式不能为空")
    @Size(max = 30, message = "用工形式长度不能超过30")
    private String employmentType;
    @NotBlank(message = "省份不能为空")
    @Size(max = 30, message = "省份长度不能超过30")
    private String province;
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市长度不能超过50")
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
