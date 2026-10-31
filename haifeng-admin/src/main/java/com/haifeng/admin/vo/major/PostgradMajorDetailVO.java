package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class PostgradMajorDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private String popularity;

    private String difficulty;

    private String brief;

    private String introduction;

    private String[] examSubjects;

    private String[] admissionRequirements;

    private String crossExamDifficulty;

    private String crossExamDescription;

    private String[] crossExamFactors;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 排序权重（越大越靠前，C 端热点优先展示）
     */
    private Integer sortOrder;
}
