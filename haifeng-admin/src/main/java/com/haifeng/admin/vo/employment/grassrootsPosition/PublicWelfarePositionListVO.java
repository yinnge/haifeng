package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class PublicWelfarePositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String monthlySalary;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String positionStatus;
}
