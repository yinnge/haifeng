package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicWelfarePositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private String householdRequirement;
    private Integer maxServiceYears;
    private String positionStatus;
    private String targetGroup;

    private Integer ageRangeMin;
    private Integer ageRangeMax;
}
