package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectionPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String selectionType;
    private String year;
    private String province;
    private String educationRequirement;
    private String degreeRequirement;
    private String politicalStatus;
    private Integer ageLimit;
    private String positionStatus;
}
