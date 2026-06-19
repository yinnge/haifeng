package com.haifeng.app.vo.employment.jobIndex;

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
public class JobIndexDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String sourceType;

    private Long sourceId;

    private String categoryLabel;

    private String positionName;

    private String organizationName;

    private String organizationLogo;

    private String province;

    private String city;

    private String educationRequirement;

    private Integer recruitmentCount;

    private String recruitmentType;

    private Integer salaryMin;

    private Integer salaryMax;

    private String salaryText;

    private String positionStatus;

    private OffsetDateTime publishDate;

    private OffsetDateTime regDeadline;

    private Boolean isHot;

    private Integer viewCount;

    private Integer applyCount;
}
