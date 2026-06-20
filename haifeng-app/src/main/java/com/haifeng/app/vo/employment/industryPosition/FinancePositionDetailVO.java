package com.haifeng.app.vo.employment.industryPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancePositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String institutionName;

    private String institutionCategory;

    private String institutionType;

    private String institutionLogo;

    private String branchName;

    private String positionName;

    private String positionCategory;

    private String recruitmentType;

    private String province;

    private String city;

    private String workLocation;

    private Boolean isRemote;

    private String educationRequirement;

    private String degreeRequirement;

    private String majorRequirement;

    private List<String> majorPreference;

    private Integer ageLimit;

    private String workExperience;

    private Integer recruitmentCount;

    private List<String> certRequirements;

    private String languageRequirement;

    private String computerRequirement;

    private String otherRequirement;

    private Integer salaryMin;

    private Integer salaryMax;

    private String salaryText;

    private String benefits;

    private String examContent;

    private OffsetDateTime examTime;

    private String interviewRounds;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private String applyLink;

    private String positionStatus;

    private String contactInfo;

    private String remark;

    private String content;
}
