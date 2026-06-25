package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ReportProspectsExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("综合就业率")
    private String employmentRate;

    @ExcelProperty("硕士平均起薪")
    private String masterSalary;

    @ExcelProperty("继续深造率")
    private String furtherStudyRate;

    @ExcelProperty("进入世界500强")
    private String fortune500Rate;

    @ExcelProperty("年薪增长率")
    private String salaryGrowthRate;

    @ExcelProperty("海外深造占比")
    private String overseasRate;
}
