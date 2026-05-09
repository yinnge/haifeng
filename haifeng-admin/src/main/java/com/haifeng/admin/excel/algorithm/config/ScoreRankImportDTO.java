package com.haifeng.admin.excel.algorithm.config;

import com.alibaba.excel.annotation.ExcelProperty;
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

    @ExcelProperty("位次")
    private Integer rank;

    @ExcelProperty("同分人数")
    private Integer sameScoreCount;

    @ExcelProperty("累计人数")
    private Integer cumulativeCount;
}
