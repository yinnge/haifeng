package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GrassrootsProjectPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String projectType;
    private String year;
    private String serviceType;
    private String province;
    private String city;
    private String county;
    private String township;
    private String educationRequirement;
    private String majorRequirement;
    private String gradYearRequirement;
    private String politicalStatus;
    private String positionStatus;
}
