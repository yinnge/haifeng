package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 专业详情更新DTO
 */
@Data
public class MajorDetailUpdateDTO {

    /**
     * 课程数量
     */
    @Min(value = 0, message = "课程数量不能小于0")
    private Integer courseCount;

    /**
     * 毕业生规模
     */
    @Size(max = 20, message = "毕业生规模长度不能超过20")
    private String graduateScale;

    /**
     * 男生比例
     */
    @DecimalMin(value = "0", message = "男生比例不能小于0")
    @DecimalMax(value = "100", message = "男生比例不能大于100")
    private BigDecimal maleRatio;

    /**
     * 女生比例
     */
    @DecimalMin(value = "0", message = "女生比例不能小于0")
    @DecimalMax(value = "100", message = "女生比例不能大于100")
    private BigDecimal femaleRatio;

    /**
     * 专业描述
     */
    private String majorDescription;

    /**
     * 培养目标
     */
    private String trainingObjective;

    /**
     * 培养要求
     */
    private String trainingRequirement;

    /**
     * 学科要求
     */
    private String subjectRequirement;

    /**
     * 职业前景
     */
    private String careerProspect;

    /**
     * 主要课程
     */
    private String[] mainCourses;

    /**
     * 知识技能
     */
    private String[] knowledgeSkills;
}
