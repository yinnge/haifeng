package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty("排序权重")
    private Integer sortOrder;
}
