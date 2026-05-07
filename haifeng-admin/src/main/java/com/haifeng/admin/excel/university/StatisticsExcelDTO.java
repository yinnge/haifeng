package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StatisticsExcelDTO {

    @ExcelProperty("实验室名称")
    private String labName;

    @ExcelProperty("统计标签")
    private String label;

    @ExcelProperty("数量")
    private Integer count;
}
