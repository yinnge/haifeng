package com.haifeng.admin.vo.major;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class MajorDetailVO {

    // 主表字段
    private Long id;

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private String parentCategory;

    private String majorTags;

    private String degreeAwarded;

    private BigDecimal employmentRate;

    private Integer salaryMin;

    private Integer salaryMax;

    private String description;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    // 详情表字段
    private Long detailId;

    private Integer courseCount;

    private Integer graduateScale;

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
