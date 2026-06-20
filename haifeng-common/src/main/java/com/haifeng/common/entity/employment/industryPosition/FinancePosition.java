package com.haifeng.common.entity.employment.industryPosition;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
@TableName(value = "t_finance_position", autoResultMap = true)
public class FinancePosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> majorPreference;

    private Integer ageLimit;

    private String workExperience;

    private Integer recruitmentCount;

    @TableField(typeHandler = JacksonTypeHandler.class)
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

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
