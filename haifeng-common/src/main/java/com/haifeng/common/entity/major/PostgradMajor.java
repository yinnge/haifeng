package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_postgrad_major", autoResultMap = true)
public class PostgradMajor {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private String popularity;

    private String difficulty;

    private String brief;

    private String introduction;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] examSubjects;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] admissionRequirements;

    private String crossExamDifficulty;

    private String crossExamDescription;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] crossExamFactors;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
