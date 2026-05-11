package com.haifeng.admin.excel.company;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 企业-行业关联导入DTO
 */
@Data
public class EnterpriseIndustryExcelDTO {

    @ExcelProperty("企业名称")
    private String enterpriseName;

    @ExcelProperty("行业名称")
    private String industryName;
}
