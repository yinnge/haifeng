package com.haifeng.admin.vo.employment.industryPosition.finance;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class FinancePositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String institutionName;
    private String institutionCategory;
    private String positionName;
    private String positionCategory;
    private String recruitmentType;
    private String province;
    private String city;
    private String positionStatus;
    private OffsetDateTime updatedAt;
}
