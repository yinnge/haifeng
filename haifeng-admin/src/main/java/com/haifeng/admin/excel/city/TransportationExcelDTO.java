package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 交通数据导入DTO (Sheet5: transportation)
 */
@Data
public class TransportationExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("地铁线路(条)")
    private Integer metroLines;

    @ExcelProperty("地铁里程(公里)")
    private BigDecimal metroMileage;

    @ExcelProperty("高速公路里程(公里)")
    private BigDecimal highwayMileage;

    @ExcelProperty("交通世界排名")
    private Integer trafficWorldRank;
}
