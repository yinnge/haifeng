package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 就业数据导入DTO (Sheet6: employment)
 */
@Data
public class EmploymentExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("城市失业率(%)")
    private BigDecimal unemploymentRate;

    @ExcelProperty("全国平均失业率(%)")
    private BigDecimal nationalUnemploymentRate;

    @ExcelProperty("第三产业就业占比(%)")
    private BigDecimal tertiaryEmploymentRatio;

    @ExcelProperty("新增就业(万人)")
    private BigDecimal newEmployment;

    @ExcelProperty("平均工资(万元/年)")
    private BigDecimal avgSalary;

    @ExcelProperty("工资排名(全国)")
    private Integer salaryRank;

    @ExcelProperty("技能人才占比(%)")
    private BigDecimal skilledTalentRatio;

    @ExcelProperty("技能人才增长(%)")
    private BigDecimal skilledTalentGrowth;
}
