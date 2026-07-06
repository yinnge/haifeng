package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class MilitaryPositionDetailVO {
    private Long id;
    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String workLocation;
    private String salaryRange;
    private String majorRequirement;
    private String educationRequirement;
    private String regDeadline;
    private String positionStatus;
    private String positionDescription;
    private String[] responsibilities;
    private String[] qualifications;
    private Integer sortOrder;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
