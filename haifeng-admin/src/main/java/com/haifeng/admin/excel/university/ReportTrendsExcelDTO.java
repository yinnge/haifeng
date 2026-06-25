package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReportTrendsExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty(value = "高速增长赛道", converter = StringArrayConverter.class)
    private List<String> highGrowthTracks;

    @ExcelProperty(value = "核心政策导向", converter = StringArrayConverter.class)
    private List<String> policyOrientations;

    @ExcelProperty(value = "就业环境分析", converter = StringArrayConverter.class)
    private List<String> environmentAnalysis;
}
