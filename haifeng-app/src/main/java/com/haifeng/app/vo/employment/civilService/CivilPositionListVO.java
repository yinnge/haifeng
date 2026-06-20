package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CivilPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String examType;
    private String recruitingDept;
    private String majorRequirement;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String examCategory;
    private String workLocation;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
    private Integer applicantCount;
}
