package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class CivilPositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String positionName;
    private String examType;
    private String recruitingDept;
    private String minEducation;
    private String workLocation;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
}
