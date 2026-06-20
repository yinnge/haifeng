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
public class PublicWelfarePositionListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private Integer recruitmentCount;
    private String monthlySalary;
    private String householdRequirement;
    private String contractPeriod;
}
