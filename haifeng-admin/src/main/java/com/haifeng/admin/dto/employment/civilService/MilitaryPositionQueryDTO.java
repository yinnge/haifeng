package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String positionStatus;
}
