package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.util.List;

/**
 * 发展地域支持导入DTO (Sheet5: development_support_info)
 */
@Data
public class DevelopmentSupportExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("地域发展概述")
    private String regionalOverview;

    @ExcelProperty(value = "重点城市(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> keyCities;

    @ExcelProperty(value = "城市政策(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> cityPolicies;
}
