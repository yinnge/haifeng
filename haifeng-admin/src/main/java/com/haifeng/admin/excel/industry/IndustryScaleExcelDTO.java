package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发展规模导入DTO (Sheet1: industry_scale)
 */
@Data
public class IndustryScaleExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("发展规模(万亿元)")
    private BigDecimal scaleValue;

    @ExcelProperty("发展规模标签")
    private String scaleLabel;

    @ExcelProperty(value = "发展规模描述(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> scaleDescriptions;
}
