package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ReportMajorComposeExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("学科名称")
    private String subjectName;

    @ExcelProperty("占比(%)")
    private Double percentage;
}
