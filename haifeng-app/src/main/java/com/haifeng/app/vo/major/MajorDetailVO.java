package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** C 端专业详情 VO（spec 任务1接口2，t_major + t_major_detail 合并返回） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ---- 来自 t_major ----
    private String majorName;
    private String majorCode;
    private String disciplineName;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private BigDecimal employmentRate;
    private Integer salaryMin;
    private Integer salaryMax;
    private String description;

    // ---- 来自 t_major_detail ----
    private Integer courseCount;
    private String graduateScale;
    private BigDecimal maleRatio;
    private BigDecimal femaleRatio;
    private String majorDescription;
    private String trainingObjective;
    private String trainingRequirement;
    private String subjectRequirement;
    private String careerProspect;
    private String[] mainCourses;
    private String[] knowledgeSkills;
}
