package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 行业主表导入DTO
 */
@Data
public class IndustryExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("行业分类")
    private String category;

    @ExcelProperty("图标类名")
    private String iconClass;

    @ExcelProperty("行业描述")
    private String description;

    @ExcelProperty("年增长率(%)")
    private BigDecimal annualGrowthRate;

    @ExcelProperty("市场规模")
    private String marketScale;

    @ExcelProperty("人才缺口")
    private String talentGap;

    @ExcelProperty("投资热度(%)")
    private BigDecimal investmentHeat;

    @ExcelProperty("增长趋势")
    private String growthTrend;

    @ExcelProperty("市场趋势")
    private String marketTrend;

    @ExcelProperty("人才趋势")
    private String talentTrend;

    @ExcelProperty("投资趋势")
    private String investmentTrend;
}
