package com.haifeng.common.entity.employment.civilService;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "t_civil_position", autoResultMap = true)
public class CivilPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String examType;
    private String recruitingDept;
    private String deptCode;
    private String positionCode;
    private String affiliatedBureau;
    private String majorRequirement;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String workExperience;
    private String grassrootsExperience;
    private String examCategory;
    private String interviewRatio;
    private Integer recruitmentCount;
    private Boolean hasProfessionalTest;
    private String workLocation;
    private String workLocationDetail;
    private String householdRequirement;
    private String householdLocation;
    private String positionIntro;
    private String remark;
    private String officialWebsite;
    private String contactPhone;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
    private Integer applicantCount;

    private Integer sortOrder;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
