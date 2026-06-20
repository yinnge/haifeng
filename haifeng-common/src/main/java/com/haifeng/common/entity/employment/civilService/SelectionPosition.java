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
@TableName(value = "t_selection_position", autoResultMap = true)
public class SelectionPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String selectionType;
    private String year;
    private String province;
    private String organizingDept;
    private String targetUnit;
    private String workLocation;
    private String trainingDirection;
    private String grassrootsServiceYears;
    private String trainingPlan;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private String[] majorCategories;
    private String universityRequirement;
    private String[] targetUniversities;
    private String politicalStatus;
    private String studentCadreRequirement;
    private String awardsRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String examSubjects;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String applyLink;
    private String positionStatus;
    private String remark;
    private String contactPhone;
    private String officialLink;
    private String content;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
