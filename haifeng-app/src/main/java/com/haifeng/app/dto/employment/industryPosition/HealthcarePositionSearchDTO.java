package com.haifeng.app.dto.employment.industryPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthcarePositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String institutionType;

    private String institutionLevel;

    private String institutionNature;

    private String department;

    private String positionCategory;

    private String province;

    private String city;

    private String district;

    private Integer ageLimit;

    private String positionStatus;

    private String educationRequirement;

    private String degreeRequirement;

    private String majorRequirement;
}
