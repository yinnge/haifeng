package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnterprisePositionVO {
    private Long id;
    private Long enterpriseId;
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
    private LocalDateTime deadline;
    private String positionStatus;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
