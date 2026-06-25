package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ReportBaseInfoExcelDTO {

    @ExcelProperty("院系名称")
    private String departmentName;

    @ExcelProperty("副标题")
    private String subtitle;
}
