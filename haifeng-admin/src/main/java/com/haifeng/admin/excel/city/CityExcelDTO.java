package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市主表导入DTO
 */
@Data
public class CityExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("所属地区")
    private String region;

    @ExcelProperty("城市简介")
    private String cityIntro;

    @ExcelProperty("高校数量")
    private Integer collegeCount;

    @ExcelProperty("重点高校数量")
    private Integer keyCollegeCount;

    @ExcelProperty("常住人口(万人)")
    private BigDecimal residentPopulation;

    @ExcelProperty("GDP(亿元)")
    private BigDecimal gdp;
}
