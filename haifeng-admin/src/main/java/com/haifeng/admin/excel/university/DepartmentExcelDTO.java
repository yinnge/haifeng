package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.util.List;

@Data
public class DepartmentExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("院系类型")
    private String departmentType;

    @ExcelProperty("页面主标题")
    private String pageTitle;

    @ExcelProperty(value = "院系标签", converter = StringArrayConverter.class)
    private List<String> tags;

    @ExcelProperty(value = "排序", converter = ExcelIntegerConverter.class)
    private Integer sortOrder;

    @ExcelProperty(value = "状态", converter = ExcelIntegerConverter.class)
    private Integer status;
}
