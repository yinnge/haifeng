package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionAddDTO {
    @NotBlank(message = "开发单位不能为空")
    @Size(max = 200, message = "开发单位长度不能超过200")
    private String developingUnit;
    @Size(max = 200)
    private String employingUnit;
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 200, message = "岗位名称长度不能超过200")
    private String positionName;
    @NotBlank(message = "岗位类别不能为空")
    @Size(max = 50, message = "岗位类别长度不能超过50")
    private String positionCategory;
    private String workContent;
    @NotBlank(message = "省份不能为空")
    @Size(max = 30, message = "省份长度不能超过30")
    private String province;
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市长度不能超过50")
    private String city;
    @Size(max = 50)
    private String district;
    @Size(max = 200)
    private String workLocation;
    private String[] targetGroup;
    @Size(max = 30)
    private String educationRequirement;
    @Size(max = 50)
    private String ageRange;
    @Size(max = 200)
    private String healthRequirement;
    private Integer recruitmentCount;
    @Size(max = 100)
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    @NotBlank(message = "合同期限不能为空")
    @Size(max = 30, message = "合同期限长度不能超过30")
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    @Size(max = 50)
    private String monthlySalary;
    @Size(max = 100)
    private String salarySource;
    @Size(max = 200)
    private String subsidyStandard;
    @Size(max = 200)
    private String socialInsuranceInfo;
    private String otherBenefits;
    @Size(max = 100)
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    @Size(max = 200)
    private String applyAddress;
    private String requiredDocuments;
    @Size(max = 20)
    private String positionStatus;
    @Size(max = 50)
    private String contactPhone;
    @Size(max = 50)
    private String contactPerson;
    private String remark;
    private String content;
    private Integer sortOrder;
}
