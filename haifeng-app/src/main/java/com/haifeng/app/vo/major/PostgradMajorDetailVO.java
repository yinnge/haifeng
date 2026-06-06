package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研专业详情 VO（spec 任务2接口2） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String majorName;
    private String majorCode;
    private String degreeType;
    private String disciplineCategory;
    private String popularity;
    private String difficulty;
    private String introduction;
    private String[] examSubjects;
    private String[] admissionRequirements;
    private String crossExamDifficulty;
    private String crossExamDescription;
    private String[] crossExamFactors;
}
