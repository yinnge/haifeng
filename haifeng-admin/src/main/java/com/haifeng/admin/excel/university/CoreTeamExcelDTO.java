package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CoreTeamExcelDTO {

    @ExcelProperty("实验室名称")
    private String labName;

    @ExcelProperty("成员姓名")
    private String memberName;

    @ExcelProperty("职务")
    private String position;

    @ExcelProperty("岗位名称")
    private String jobTitle;
}
