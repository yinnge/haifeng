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
public class CommunityPositionListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String communityName;
    private String district;
    private String positionName;
    private String positionType;
    private String province;
    private String city;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String workExperience;
    private String positionStatus;
}
