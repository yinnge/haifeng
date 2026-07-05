package com.haifeng.admin.dto.employment.civilService;

import lombok.Data;

@Data
public class MilitaryPositionUpdateDTO {
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
}
