package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String positionType;
    private String educationRequirement;
    private String positionStatus;
    private String workLocation;
}
