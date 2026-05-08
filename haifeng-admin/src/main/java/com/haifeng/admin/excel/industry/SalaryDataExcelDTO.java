package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 薪资数据导入DTO (Sheet8: salary_data)
 */
@Data
public class SalaryDataExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("薪资分析标题")
    private String salaryAnalysisTitle;

    @ExcelProperty("薪资分析描述")
    private String salaryAnalysisDescription;

    @ExcelProperty("地域薪资差异标题")
    private String regionalSalaryTitle;

    @ExcelProperty("地域薪资差异描述")
    private String regionalSalaryDescription;

    @ExcelProperty("薪资趋势分析")
    private String salaryTrendAnalysis;
}
