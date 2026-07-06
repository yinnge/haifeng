package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectionPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String targetUnit;
    private String organizingDept;
    private String selectionType;
    private String year;
    private String province;
    private String politicalStatus;
    private String positionStatus;
}
