package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportSubjectDetailExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty(value = "专业标签", converter = StringArrayConverter.class)
    private List<String> tags;

    @ExcelProperty("核心学科")
    private String coreSubject;

    @ExcelProperty("支撑学科")
    private String supportSubject;

    @ExcelProperty("专业定位")
    private String positioning;

    @ExcelProperty(value = "核心课程", converter = StringArrayConverter.class)
    private List<String> coreCourses;

    @ExcelProperty(value = "培养能力", converter = StringArrayConverter.class)
    private List<String> abilities;

    @ExcelProperty(value = "推荐证书", converter = StringArrayConverter.class)
    private List<String> certificates;
}
