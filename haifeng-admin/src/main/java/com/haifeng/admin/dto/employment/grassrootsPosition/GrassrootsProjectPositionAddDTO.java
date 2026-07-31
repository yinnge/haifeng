package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionAddDTO {
    @NotBlank(message = "项目类型不能为空")
    @Size(max = 30, message = "项目类型长度不能超过30")
    private String projectType;
    @NotBlank(message = "招募年份不能为空")
    @Size(max = 10, message = "招募年份长度不能超过10")
    private String year;
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 200, message = "岗位名称长度不能超过200")
    private String positionName;
    @NotBlank(message = "服务类型不能为空")
    @Size(max = 50, message = "服务类型长度不能超过50")
    private String serviceType;
    @Size(max = 200)
    private String organizingDept;
    @Size(max = 200)
    private String serviceUnit;
    @NotBlank(message = "省份不能为空")
    @Size(max = 30, message = "省份长度不能超过30")
    private String province;
    @Size(max = 50)
    private String city;
    @Size(max = 50)
    private String county;
    @Size(max = 100)
    private String township;
    @NotBlank(message = "服务期限不能为空")
    @Size(max = 30, message = "服务期限长度不能超过30")
    private String servicePeriod;
    @Size(max = 30)
    private String serviceStartDate;
    @Size(max = 30)
    private String serviceEndDate;
    @NotBlank(message = "学历要求不能为空")
    @Size(max = 30, message = "学历要求长度不能超过30")
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
