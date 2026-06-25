package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportPostgraduateExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty(value = "考研方向内容", converter = StringArrayConverter.class)
    private List<String> directions;
}
