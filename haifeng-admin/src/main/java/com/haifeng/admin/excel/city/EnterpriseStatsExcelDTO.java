package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 企业统计导入DTO (Sheet7: enterprise_stats)
 */
@Data
public class EnterpriseStatsExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("企业类别数")
    private Integer enterpriseCategories;

    @ExcelProperty("重点企业总数")
    private Integer keyEnterpriseCount;

    @ExcelProperty("世界500强企业数量")
    private Integer fortune500Count;
}
