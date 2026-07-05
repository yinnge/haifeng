package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SelectionPositionListVO {
    private Long id;
    private String positionName;
    private String selectionType;
    private String year;
    private String province;
    private String organizingDept;
    private String targetUnit;
    private String workLocation;
    private String politicalStatus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String positionStatus;
}
