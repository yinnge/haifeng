package com.haifeng.admin.vo.employment.industryPosition.finance;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class FinancePositionListVO {
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
