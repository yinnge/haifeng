package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class GuideJsonbExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    // 通用字段，根据不同Sheet动态解析
    @ExcelProperty(value = "字段1", converter = StringArrayConverter.class)
    private List<String> field1;

    @ExcelProperty(value = "字段2", converter = StringArrayConverter.class)
    private List<String> field2;

    @ExcelProperty(value = "字段3", converter = StringArrayConverter.class)
    private List<String> field3;

    @ExcelProperty(value = "字段4", converter = StringArrayConverter.class)
    private List<String> field4;
}
