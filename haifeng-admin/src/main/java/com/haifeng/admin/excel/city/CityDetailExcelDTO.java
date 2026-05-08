package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 城市详情基础字段导入DTO (Sheet0)
 */
@Data
public class CityDetailExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("面积(平方公里)")
    private BigDecimal area;

    @ExcelProperty("副标题")
    private String subtitle;

    @ExcelProperty("城市级别")
    private String cityLevel;

    @ExcelProperty("行政区划代码")
    private String adminCode;

    @ExcelProperty("人均GDP(万元)")
    private BigDecimal perCapitaGdp;

    @ExcelProperty("城镇化率(%)")
    private BigDecimal urbanizationRate;

    @ExcelProperty("农村人口比例(%)")
    private BigDecimal ruralPopRatio;

    @ExcelProperty("老龄化率(%)")
    private BigDecimal agingRate;

    @ExcelProperty("外来人口比例(%)")
    private BigDecimal migrantPopRatio;

    @ExcelProperty("GDP增长率(%)")
    private BigDecimal gdpGrowthRate;

    @ExcelProperty("世界500强企业数量")
    private Integer fortune500Count;

    @ExcelProperty("产业描述")
    private String industryDescription;

    @ExcelProperty(value = "主要产业(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> mainIndustries;

    @ExcelProperty(value = "新兴产业(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> emergingIndustries;
}
