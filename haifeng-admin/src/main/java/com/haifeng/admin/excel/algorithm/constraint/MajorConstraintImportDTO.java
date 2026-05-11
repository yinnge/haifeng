package com.haifeng.admin.excel.algorithm.constraint;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MajorConstraintImportDTO {
    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("约束名称")
    private String constraintName;

    @ExcelProperty("备注")
    private String remark;
}
