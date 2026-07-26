package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MilitaryPositionDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
