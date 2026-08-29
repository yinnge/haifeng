package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

@Data
public class StatisticsExcelDTO {

    @ExcelProperty("实验室名称")
    private String labName;

    @ExcelProperty("统计标签")
    private String label;

    @ExcelProperty(value = "数量", converter = ExcelIntegerConverter.class)
    private Integer count;
}
