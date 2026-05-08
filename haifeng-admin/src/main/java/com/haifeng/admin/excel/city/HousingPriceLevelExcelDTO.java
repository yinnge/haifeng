package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房价水平导入DTO (Sheet2: housing_price_level)
 */
@Data
public class HousingPriceLevelExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("平均房价(万元/平方米)")
    private BigDecimal avgPrice;

    @ExcelProperty("核心区房价(万元/平方米)")
    private BigDecimal coreAreaPrice;

    @ExcelProperty("郊区房价范围(万元/平方米)")
    private String suburbanPriceRange;

    @ExcelProperty("房价涨幅(%)")
    private BigDecimal priceGrowthRate;

    @ExcelProperty("房价收入比(%)")
    private BigDecimal priceIncomeRatio;
}
