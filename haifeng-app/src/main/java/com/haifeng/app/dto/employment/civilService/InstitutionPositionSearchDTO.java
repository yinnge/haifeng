package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String province;
    private String examCategory;
    private String positionType;
    private String educationRequirement;
    private String degreeRequirement;
    private String positionStatus;
    private String specialPosition;
    private Integer ageLimit;
}
