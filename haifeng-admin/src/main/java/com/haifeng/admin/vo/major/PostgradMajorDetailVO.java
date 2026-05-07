package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PostgradMajorDetailVO {

    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private Integer popularity;

    private Integer difficulty;

    private String brief;

    private String introduction;

    private String[] examSubjects;

    private String[] admissionRequirements;

    private Integer crossExamDifficulty;

    private String crossExamDescription;

    private String[] crossExamFactors;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
