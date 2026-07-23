package com.haifeng.admin.vo.employment.industryPosition.healthcare;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class HealthcarePositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String institutionName;
    private String positionName;
    private String department;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
    private OffsetDateTime updatedAt;
}
