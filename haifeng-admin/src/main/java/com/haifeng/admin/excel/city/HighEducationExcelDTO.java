package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 高等教育资源导入DTO (Sheet3: high_education)
 */
@Data
public class HighEducationExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("高校总数")
    private Integer totalColleges;

    @ExcelProperty("双一流高校数量")
    private Integer doubleFirstClassCount;

    @ExcelProperty("在校生数量(万)")
    private BigDecimal undergraduateCount;

    @ExcelProperty("研究生数量(万)")
    private BigDecimal graduateCount;
}
