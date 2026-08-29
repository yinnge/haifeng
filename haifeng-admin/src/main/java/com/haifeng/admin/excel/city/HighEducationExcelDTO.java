package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 高等教育资源导入DTO (Sheet3: high_education)
 */
@Data
public class HighEducationExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty(value = "高校总数", converter = ExcelIntegerConverter.class)
    private Integer totalColleges;

    @ExcelProperty(value = "双一流高校数量", converter = ExcelIntegerConverter.class)
    private Integer doubleFirstClassCount;

    @ExcelProperty("在校生数量(万)")
    private BigDecimal undergraduateCount;

    @ExcelProperty("研究生数量(万)")
    private BigDecimal graduateCount;
}
