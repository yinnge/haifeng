package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CivilPositionListVO {
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
