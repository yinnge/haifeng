package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.util.List;

/**
 * 未来规划导入DTO (Sheet8: future_plan)
 */
@Data
public class FuturePlanExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty(value = "目标年份", converter = ExcelIntegerConverter.class)
    private Integer targetYear;

    @ExcelProperty("发展目标")
    private String developmentGoal;

    @ExcelProperty(value = "重点领域(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> keyAreas;
}
