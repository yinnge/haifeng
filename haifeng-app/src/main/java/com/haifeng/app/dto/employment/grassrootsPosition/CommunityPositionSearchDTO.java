package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String educationRequirement;
    private String majorRequirement;
    private String politicalStatus;
    private String workExperience;
    private String positionStatus;

    private Integer ageLimitMin;
    private Integer ageLimitMax;
}
