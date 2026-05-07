package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty("状态")
    private Integer status;
}
