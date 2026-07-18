package com.haifeng.admin.excel.algorithm.admission;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdmissionImportDTO {

    // ==================== 专业组字段 ====================
    @ExcelProperty("大学名")
    private String universityName;

    @ExcelProperty("年份")
    private Short year;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("批次")
    private String batch;

    @ExcelProperty("省招代码")
    private String enrollmentCode;

    @ExcelProperty("专业组代码")
    private String groupCode;

    @ExcelProperty("专业组名称")
    private String groupName;

    @ExcelProperty("专业组简介")
    private String groupDescription;

    @ExcelProperty("科目")
    private String subjectsStr;

    @ExcelProperty("选科类型")
    private String requirementType;

    // ==================== 专业明细字段 ====================
    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("层次")
    private String educationLevel;

    @ExcelProperty("学制")
    private String duration;

    @ExcelProperty("学费")
    private String tuition;

    @ExcelProperty("专业简介")
    private String majorDescription;

    @ExcelProperty("报考限制条件")
    private String constraintsStr;

    @ExcelProperty("录取人数")
    private Integer admissionCount;

    @ExcelProperty("最低分")
    private Integer minScore;

    @ExcelProperty("中位分")
    private BigDecimal avgScore;

    @ExcelProperty("最高分")
    private Integer maxScore;

    @ExcelProperty("最低位次")
    private Integer minRank;

    @ExcelProperty("中位位次")
    private Integer avgRank;

    @ExcelProperty("最高位次")
    private Integer maxRank;
}
