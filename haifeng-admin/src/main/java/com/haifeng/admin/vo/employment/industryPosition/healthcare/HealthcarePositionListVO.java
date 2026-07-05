package com.haifeng.admin.vo.employment.industryPosition.healthcare;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HealthcarePositionListVO {
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
