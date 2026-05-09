package com.haifeng.admin.excel.algorithm.config;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BatchScoreLineImportDTO {
    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("年份")
    private Short year;

    @ExcelProperty("科类")
    private String subjectType;

    @ExcelProperty("批次")
    private String batch;

    @ExcelProperty("分数线")
    private Integer scoreLine;

    @ExcelProperty("位次线")
    private Integer rankLine;

    @ExcelProperty("备注")
    private String remark;
}
