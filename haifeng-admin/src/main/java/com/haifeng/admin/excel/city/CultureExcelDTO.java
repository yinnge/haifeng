package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import com.haifeng.common.converter.ExcelIntegerConverter;
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

    @ExcelProperty(value = "世界遗产数量(项)", converter = ExcelIntegerConverter.class)
    private Integer worldHeritageCount;

    @ExcelProperty("年游客量(万人次)")
    private BigDecimal annualTourists;

    @ExcelProperty(value = "A级景区数量(家)", converter = ExcelIntegerConverter.class)
    private Integer aScenicCount;

    @ExcelProperty(value = "核心景点(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> coreAttractions;
}
