package com.haifeng.app.vo.employment.grassrootsPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrassrootsProjectPositionListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String positionStatus;
    private String politicalStatus;
}
