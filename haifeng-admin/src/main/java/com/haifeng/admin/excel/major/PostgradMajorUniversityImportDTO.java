package com.haifeng.admin.excel.major;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

/**
 * 考研专业-大学关联导入DTO (EasyExcel)
 */
@Data
public class PostgradMajorUniversityImportDTO {

    @ExcelProperty("大学名称")
    private String universityName;

    @ExcelProperty("考研专业代码")
    private String postgradMajorCode;

    @ExcelProperty(value = "排序权重", converter = ExcelIntegerConverter.class)
    private Integer sortOrder;
}
