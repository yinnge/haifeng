package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MajorPostgradDirectionImportDTO {

    @ExcelProperty("本科专业名称")
    private String majorName;

    @ExcelProperty("考研专业名称")
    private String postgradMajorName;

    @ExcelProperty("排序权重")
    private Integer sortOrder;
}
