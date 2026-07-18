package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionUpdateDTO {
    @Size(max = 200)
    private String developingUnit;
    @Size(max = 200)
    private String employingUnit;
    @Size(max = 200)
    private String positionName;
    @Size(max = 50)
    private String positionCategory;
    private String workContent;
    @Size(max = 30)
    private String province;
    @Size(max = 50)
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
    @Size(max = 30)
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
