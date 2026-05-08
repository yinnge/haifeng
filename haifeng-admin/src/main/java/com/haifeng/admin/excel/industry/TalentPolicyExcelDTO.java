package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.util.List;

/**
 * 人才引进政策导入DTO (Sheet7: talent_policy)
 */
@Data
public class TalentPolicyExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("政策标题")
    private String policyTitle;

    @ExcelProperty(value = "国家级政策(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> nationalPolicies;

    @ExcelProperty(value = "地方级政策(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> localPolicies;

    @ExcelProperty("企业层面描述")
    private String enterpriseDescription;
}
