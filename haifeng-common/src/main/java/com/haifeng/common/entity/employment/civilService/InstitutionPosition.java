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
@TableName(value = "t_institution_position", autoResultMap = true)
public class InstitutionPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String supervisingDept;
    private String institution;
    private String workLocation;
    private String province;
    private String examCategory;
    private String positionType;
    private String subCategory;
    private String educationRequirement;
    private String degreeRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String salaryRange;
    private String regDeadline;
    private String[] majorRequirements;
    private String specialPosition;
    private String otherRequirement;
    private String otherRequirementDesc;
    private String remarkType;
    private String remarkDesc;
    private String consultationPhone;
    private String supervisionPhone;
    private String positionStatus;
    private String positionTag;
    private String tagText;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
