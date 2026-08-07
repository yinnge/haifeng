package com.haifeng.admin.excel.major;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 考研专业导入DTO (EasyExcel)
 */
@Data
public class PostgradMajorImportDTO {

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("学位类型")
    private String degreeType;

    @ExcelProperty("学科门类")
    private String disciplineCategory;

    @ExcelProperty("热度")
    private String popularity;

    @ExcelProperty("难度")
    private String difficulty;

    @ExcelProperty("专业简介")
    private String brief;

    @ExcelProperty("专业介绍")
    private String introduction;

    @ExcelProperty("考试科目")
    private String examSubjects;

    @ExcelProperty("报考要求")
    private String admissionRequirements;

    @ExcelProperty("跨考因素")
    private String crossExamFactors;

    @ExcelProperty("跨考难度")
    private String crossExamDifficulty;

    @ExcelProperty("跨考说明")
    private String crossExamDescription;
}
