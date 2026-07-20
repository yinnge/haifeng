package com.haifeng.common.entity.company;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.StringListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_enterprise_position", autoResultMap = true)
public class EnterprisePosition {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long enterpriseId;

    private String positionName;

    private String recruitmentType;

    private String positionRequirement;

    @TableField(typeHandler = StringListTypeHandler.class)
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

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
