package com.haifeng.app.dto.employment.industryPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String schoolType;

    private String schoolNature;

    private String subject;

    private String educationRequirement;

    private String province;

    private String city;

    private String district;

    private String positionStatus;
}
