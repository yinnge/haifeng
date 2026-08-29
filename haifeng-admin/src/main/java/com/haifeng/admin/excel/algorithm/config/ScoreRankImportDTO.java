package com.haifeng.admin.excel.algorithm.config;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.common.converter.ExcelIntegerConverter;
import lombok.Data;

@Data
public class ScoreRankImportDTO {
    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("年份")
    private Short year;

    @ExcelProperty("科类")
    private String subjectType;

    @ExcelProperty("分数")
    private Short score;

    @ExcelProperty(value = "位次", converter = ExcelIntegerConverter.class)
    private Integer rank;

    @ExcelProperty(value = "同分人数", converter = ExcelIntegerConverter.class)
    private Integer sameScoreCount;

    @ExcelProperty(value = "累计人数", converter = ExcelIntegerConverter.class)
    private Integer cumulativeCount;
}
