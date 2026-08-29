package com.haifeng.admin.excel.algorithm.config;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
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

    @ExcelProperty(value = "分数线", converter = ExcelIntegerConverter.class)
    private Integer scoreLine;

    @ExcelProperty(value = "位次线", converter = ExcelIntegerConverter.class)
    private Integer rankLine;

    @ExcelProperty("备注")
    private String remark;
}
