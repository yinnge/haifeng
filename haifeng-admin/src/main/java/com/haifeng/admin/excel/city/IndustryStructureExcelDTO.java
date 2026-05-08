package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 产业结构导入DTO (Sheet1: industry_structure)
 */
@Data
public class IndustryStructureExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("第一产业占比(%)")
    private BigDecimal primaryRatio;

    @ExcelProperty("第二产业占比(%)")
    private BigDecimal secondaryRatio;

    @ExcelProperty("第三产业占比(%)")
    private BigDecimal tertiaryRatio;
}
