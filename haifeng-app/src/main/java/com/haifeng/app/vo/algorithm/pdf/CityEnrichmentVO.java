package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 城市增强数据（PDF 报告用）
 * <p>从 t_city + t_city_detail 提取，供 AI 分析与 PDF 静态展示使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityEnrichmentVO {

    private String cityName;
    private String cityLevel;
    private BigDecimal gdp;
    private BigDecimal gdpGrowthRate;
    private Integer fortune500Count;
    private List<String> mainIndustries;
    private List<String> emergingIndustries;
    private String industryDescription;
    private BigDecimal avgSalary;
    private BigDecimal unemploymentRate;
}
