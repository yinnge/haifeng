package com.haifeng.admin.excel.city;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 基础教育资源导入DTO (Sheet4: basic_education)
 */
@Data
public class BasicEducationExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("学校总数")
    private Integer totalSchools;

    @ExcelProperty("示范学校数量")
    private Integer modelSchoolCount;

    @ExcelProperty("重点学校数量")
    private Integer keySchoolCount;

    @ExcelProperty("教育备注")
    private String educationNote;
}
