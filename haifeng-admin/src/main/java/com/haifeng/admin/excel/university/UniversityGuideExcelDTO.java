package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

import java.util.List;

@Data
public class UniversityGuideExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty(value = "自定义标签", converter = StringArrayConverter.class)
    private List<String> customTags;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty(value = "状态", converter = ExcelIntegerConverter.class)
    private Integer status;
}
