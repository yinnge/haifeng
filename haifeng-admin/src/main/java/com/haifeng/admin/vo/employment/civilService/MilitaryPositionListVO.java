package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;

@Data
public class MilitaryPositionListVO {
    private Long id;
    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String workLocation;
    private String salaryRange;
    private String regDeadline;
    private String positionStatus;
}
