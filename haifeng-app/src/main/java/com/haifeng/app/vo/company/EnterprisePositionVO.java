package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * C 端企业岗位 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterprisePositionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String positionName;
    private String recruitmentType;
    private String positionRequirement;
    private List<String> positionTags;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private String majorRequirement;
    private String workExperience;
    private Integer salaryMin;
    private Integer salaryMax;
    private String applyLink;
    private OffsetDateTime deadline;
    private String positionStatus;
}
