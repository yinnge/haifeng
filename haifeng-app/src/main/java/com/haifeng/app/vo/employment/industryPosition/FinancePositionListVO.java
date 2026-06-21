package com.haifeng.app.vo.employment.industryPosition;

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
public class FinancePositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String institutionName;

    private String institutionCategory;

    private String positionName;

    private String positionCategory;

    private String recruitmentType;

    private String province;

    private String city;

    private Integer ageLimit;

    private String workExperience;

    private Integer salaryMin;

    private Integer salaryMax;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private Boolean isRemote;

    private String workLocation;

    private Integer recruitmentCount;

    private String positionStatus;
}
