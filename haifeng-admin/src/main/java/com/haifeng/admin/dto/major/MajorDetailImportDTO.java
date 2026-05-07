package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 专业详情导入DTO (EasyExcel)
 */
@Data
public class MajorDetailImportDTO {

    /**
     * 专业代码（逻辑外键）
     */
    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("课程数量")
    private Integer courseCount;

    @ExcelProperty("毕业生规模")
    private String graduateScale;

    @ExcelProperty("男生比例")
    private BigDecimal maleRatio;

    @ExcelProperty("女生比例")
    private BigDecimal femaleRatio;

    @ExcelProperty("专业描述")
    private String majorDescription;

    @ExcelProperty("培养目标")
    private String trainingObjective;

    @ExcelProperty("培养要求")
    private String trainingRequirement;

    @ExcelProperty("学科要求")
    private String subjectRequirement;

    @ExcelProperty("职业前景")
    private String careerProspect;

    @ExcelProperty(value = "主要课程", converter = StringToArrayConverter.class)
    private String[] mainCourses;

    @ExcelProperty(value = "知识技能", converter = StringToArrayConverter.class)
    private String[] knowledgeSkills;
}
