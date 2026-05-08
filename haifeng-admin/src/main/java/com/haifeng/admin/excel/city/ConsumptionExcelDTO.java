package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费数据导入DTO (Sheet10: consumption)
 */
@Data
public class ConsumptionExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("人均消费(万元/年)")
    private BigDecimal perCapitaConsumption;

    @ExcelProperty("消费涨幅(%)")
    private BigDecimal consumptionGrowthRate;

    @ExcelProperty("恩格尔系数(%)")
    private BigDecimal engelCoefficient;

    @ExcelProperty("教育支出占比(%)")
    private BigDecimal educationExpenseRatio;

    @ExcelProperty("消费指数")
    private BigDecimal consumptionIndex;

    @ExcelProperty("消费排名(全国)")
    private Integer consumptionRank;
}
