package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

/**
 * 企业统计导入DTO (Sheet7: enterprise_stats)
 */
@Data
public class EnterpriseStatsExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty(value = "企业类别数", converter = ExcelIntegerConverter.class)
    private Integer enterpriseCategories;

    @ExcelProperty(value = "重点企业总数", converter = ExcelIntegerConverter.class)
    private Integer keyEnterpriseCount;

    @ExcelProperty(value = "世界500强企业数量", converter = ExcelIntegerConverter.class)
    private Integer fortune500Count;
}
