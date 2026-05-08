package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 文化旅游数据导入DTO (Sheet9: culture)
 */
@Data
public class CultureExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("世界遗产数量(项)")
    private Integer worldHeritageCount;

    @ExcelProperty("年游客量(万人次)")
    private BigDecimal annualTourists;

    @ExcelProperty("A级景区数量(家)")
    private Integer aScenicCount;

    @ExcelProperty(value = "核心景点(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> coreAttractions;
}
