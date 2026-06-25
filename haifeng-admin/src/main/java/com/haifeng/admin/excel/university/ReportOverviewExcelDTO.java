package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportOverviewExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty(value = "内容描述", converter = StringArrayConverter.class)
    private List<String> descriptions;
}
