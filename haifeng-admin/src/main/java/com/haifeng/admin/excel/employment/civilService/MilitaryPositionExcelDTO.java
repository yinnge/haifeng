package com.haifeng.admin.excel.employment.civilService;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;

@Data
public class MilitaryPositionExcelDTO {
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("用人单位") private String employerUnit;
    @ExcelProperty("科室") private String department;
    @ExcelProperty("岗位类型") private String positionType;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty("薪资范围") private String salaryRange;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("报名截止") private String regDeadline;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("岗位描述") private String positionDescription;
    @ExcelProperty(value = "工作职责", converter = StringToArrayConverter.class) private String[] responsibilities;
    @ExcelProperty(value = "任职资格", converter = StringToArrayConverter.class) private String[] qualifications;
    @ExcelProperty("排序") private Integer sortOrder;
}
