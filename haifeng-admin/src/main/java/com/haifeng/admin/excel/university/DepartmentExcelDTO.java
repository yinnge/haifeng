package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("状态")
    private Integer status;
}
