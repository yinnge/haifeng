package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ReportCareerExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("路径标题")
    private String pathTitle;

    @ExcelProperty("路径描述")
    private String pathDesc;

    @ExcelProperty("阶段小标题")
    private String stageTitle;

    @ExcelProperty("工作年限")
    private String workYears;

    @ExcelProperty("职位名称")
    private String position;

    @ExcelProperty("核心目标")
    private String coreGoal;

    @ExcelProperty("薪资范围(万元/年)")
    private String salaryRange;
}
