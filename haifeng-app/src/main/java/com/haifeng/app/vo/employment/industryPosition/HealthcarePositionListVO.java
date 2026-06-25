package com.haifeng.app.vo.employment.industryPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthcarePositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String institutionName;

    private String institutionLevel;

    private String positionName;

    private String department;

    private String positionCategory;

    private String province;

    private String city;

    private String district;

    private Integer ageLimit;

    private Integer recruitmentCount;

    private String salaryRange;

    private String workExperience;

    private String positionStatus;

    private String educationRequirement;

    private String degreeRequirement;

    private String majorRequirement;
}
