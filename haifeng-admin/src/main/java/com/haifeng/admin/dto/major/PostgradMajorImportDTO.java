package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 考研专业导入DTO (EasyExcel)
 * 注意：数组字段暂用String类型，后续使用converter处理
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

    @ExcelProperty("热门程度")
    private String popularity;

    @ExcelProperty("难度等级")
    private String difficulty;

    @ExcelProperty("专业简介")
    private String brief;

    @ExcelProperty("详细介绍")
    private String introduction;

    @ExcelProperty("考试科目")
    private String examSubjects;

    @ExcelProperty("录取条件")
    private String admissionRequirements;

    @ExcelProperty("跨考因素")
    private String crossExamFactors;

    @ExcelProperty("跨考难度")
    private String crossExamDifficulty;

    @ExcelProperty("跨考说明")
    private String crossExamDescription;
}
