package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 交通数据导入DTO (Sheet5: transportation)
 */
@Data
public class TransportationExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty(value = "地铁线路(条)", converter = ExcelIntegerConverter.class)
    private Integer metroLines;

    @ExcelProperty("地铁里程(公里)")
    private BigDecimal metroMileage;

    @ExcelProperty("高速公路里程(公里)")
    private BigDecimal highwayMileage;

    @ExcelProperty(value = "交通世界排名", converter = ExcelIntegerConverter.class)
    private Integer trafficWorldRank;
}
