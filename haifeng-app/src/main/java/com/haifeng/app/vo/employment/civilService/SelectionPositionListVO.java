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
public class SelectionPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String selectionType;
    private String year;
    private String province;
    private String organizingDept;
    private String targetUnit;
    private String workLocation;
    private String trainingDirection;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private String universityRequirement;
    private String politicalStatus;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String positionStatus;
}
