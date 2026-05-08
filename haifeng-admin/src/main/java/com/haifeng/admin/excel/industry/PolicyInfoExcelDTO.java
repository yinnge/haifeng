package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.util.List;

/**
 * 政策信息导入DTO (Sheet4: policy_info)
 */
@Data
public class PolicyInfoExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("政策概览")
    private String policyOverview;

    @ExcelProperty(value = "国家政策(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> nationalPolicies;

    @ExcelProperty("政策亮点")
    private String policyHighlights;
}
