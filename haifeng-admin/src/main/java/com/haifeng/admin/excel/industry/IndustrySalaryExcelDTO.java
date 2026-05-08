package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.util.List;

/**
 * 行业薪资导入DTO (Sheet3: industry_salary)
 */
@Data
public class IndustrySalaryExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("薪资范围(万元)")
    private String salaryRange;

    @ExcelProperty("薪资标签")
    private String salaryLabel;

    @ExcelProperty(value = "薪资描述(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> salaryDescriptions;
}
