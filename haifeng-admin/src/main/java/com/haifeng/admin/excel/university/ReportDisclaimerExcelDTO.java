package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ReportDisclaimerExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("免责声明文本")
    private String text;

    @ExcelProperty("更新时间")
    private String updateTime;

    @ExcelProperty("报告版本")
    private String version;

    @ExcelProperty("编制单位")
    private String compileUnit;
}
