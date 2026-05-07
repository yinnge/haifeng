package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 考研专业更新DTO
 * 注意：majorCode不可修改，所有字段非必填
 */
@Data
public class PostgradMajorUpdateDTO {

    /**
     * 考研专业ID
     */
    @NotNull(message = "专业ID不能为空")
    private Long id;

    /**
     * 专业名称
     */
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String majorName;

    /**
     * 学位类型（学术学位/专业学位）
     */
    @Pattern(regexp = "学术学位|专业学位", message = "学位类型必须为学术学位或专业学位")
    private String degreeType;

    /**
     * 学科门类
     */
    @Size(max = 50, message = "学科门类长度不能超过50")
    private String disciplineCategory;

    /**
     * 热门程度（热门/一般/冷门）
     */
    @Pattern(regexp = "热门|一般|冷门|", message = "热门程度必须为热门、一般或冷门")
    private String popularity;

    /**
     * 难度等级（高/中/低）
     */
    @Pattern(regexp = "高|中|低|", message = "难度等级必须为高、中或低")
    private String difficulty;

    /**
     * 专业简介
     */
    private String brief;

    /**
     * 详细介绍
     */
    private String introduction;

    /**
     * 考试科目
     */
    private String[] examSubjects;

    /**
     * 录取条件
     */
    private String[] admissionRequirements;

    /**
     * 跨考因素
     */
    private String[] crossExamFactors;

    /**
     * 跨考难度（较易/中等/较难）
     */
    @Pattern(regexp = "较易|中等|较难|", message = "跨考难度必须为较易、中等或较难")
    private String crossExamDifficulty;

    /**
     * 跨考说明
     */
    private String crossExamDescription;

    /**
     * 状态
     */
    private Short status;
}
