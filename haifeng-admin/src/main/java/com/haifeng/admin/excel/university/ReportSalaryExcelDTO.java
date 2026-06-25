package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ReportSalaryExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("最低薪资(万元/年)")
    private Double minSalary;

    @ExcelProperty("最高薪资(万元/年)")
    private Double maxSalary;
}
