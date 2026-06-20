package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CivilPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String examType;
    private String positionCode;
    private String deptCode;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String examCategory;
}
