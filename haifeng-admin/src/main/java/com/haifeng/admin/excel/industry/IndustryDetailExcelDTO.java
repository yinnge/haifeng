package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 行业详情基础字段导入DTO (Sheet0)
 */
@Data
public class IndustryDetailExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("简短描述")
    private String shortDescription;

    @ExcelProperty("详细描述")
    private String detailedDescription;
}
