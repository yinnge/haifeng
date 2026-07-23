package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MilitaryPositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
