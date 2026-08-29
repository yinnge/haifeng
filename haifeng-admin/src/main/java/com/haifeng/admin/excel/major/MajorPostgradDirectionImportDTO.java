package com.haifeng.admin.excel.major;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

/**
 * 本科专业-考研方向关联导入DTO (EasyExcel)
 */
@Data
public class MajorPostgradDirectionImportDTO {

    @ExcelProperty("本科专业名称")
    private String majorName;

    @ExcelProperty("考研专业名称")
    private String postgradMajorName;

    @ExcelProperty(value = "排序权重", converter = ExcelIntegerConverter.class)
    private Integer sortOrder;
}
