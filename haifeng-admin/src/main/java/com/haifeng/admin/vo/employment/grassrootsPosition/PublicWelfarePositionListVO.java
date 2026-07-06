package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionListVO {
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
