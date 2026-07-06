package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionDetailVO {
    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String workContent;
    private String province;
    private String city;
    private String district;
    private String workLocation;
    private String[] targetGroup;
    private String educationRequirement;
    private String ageRange;
    private String healthRequirement;
    private Integer recruitmentCount;
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    private String monthlySalary;
    private String salarySource;
    private String subsidyStandard;
    private String socialInsuranceInfo;
    private String otherBenefits;
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    private String applyAddress;
    private String requiredDocuments;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;
    private Integer sortOrder;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
