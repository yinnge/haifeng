package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租房成本导入DTO (Sheet13: rental_cost)
 */
@Data
public class RentalCostExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("市中心租金范围(元/月)")
    private String downtownRentRange;

    @ExcelProperty("郊区租金范围(元/月)")
    private String suburbanRentRange;

    @ExcelProperty("租金收入比(%)")
    private BigDecimal rentIncomeRatio;

    @ExcelProperty("租金涨幅(%)")
    private BigDecimal rentGrowthRate;
}
