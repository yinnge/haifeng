package com.haifeng.admin.excel.major;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 专业导入DTO (EasyExcel)
 */
@Data
public class MajorImportDTO {

    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("学科名称")
    private String disciplineName;

    @ExcelProperty("专业类型")
    private String majorType;

    @ExcelProperty("学科门类")
    private String majorCategory;

    @ExcelProperty("专业类")
    private String parentCategory;

    @ExcelProperty("专业标签")
    private String majorTags;

    @ExcelProperty("授予学位")
    private String degreeAwarded;

    @ExcelProperty("学制")
    private String studyDuration;

    @ExcelProperty("就业率")
    private BigDecimal employmentRate;

    @ExcelProperty(value = "薪资下限", converter = ExcelIntegerConverter.class)
    private Integer salaryMin;

    @ExcelProperty(value = "薪资上限", converter = ExcelIntegerConverter.class)
    private Integer salaryMax;

    @ExcelProperty("专业描述")
    private String description;
}
