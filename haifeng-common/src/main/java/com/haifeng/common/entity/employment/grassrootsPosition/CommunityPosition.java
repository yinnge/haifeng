package com.haifeng.common.entity.employment.grassrootsPosition;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName(value = "t_community_position", autoResultMap = true)
public class CommunityPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String streetOffice;
    private String communityName;
    private String supervisingDept;
    private String district;
    private String positionName;
    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String workExperience;
    private String socialWorkCert;
    private String communityExperience;
    private String residenceRequirement;
    private String salaryRange;
    private String salaryComposition;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String applyMethod;
    private String contactPhone;
    private String contactAddress;
    private String remark;
    private String content;

    private Integer sortOrder;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
