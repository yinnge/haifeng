package com.haifeng.common.entity.employment.civilService;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haifeng.common.config.StringArrayTypeHandler;
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
@TableName(value = "t_military_position", autoResultMap = true)
public class MilitaryPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String workLocation;
    private String salaryRange;
    private String majorRequirement;
    private String educationRequirement;
    private String regDeadline;
    private String positionStatus;
    private String positionDescription;
    @TableField(typeHandler = StringArrayTypeHandler.class)
    private String[] responsibilities;

    @TableField(typeHandler = StringArrayTypeHandler.class)
    private String[] qualifications;

    private Integer sortOrder;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
