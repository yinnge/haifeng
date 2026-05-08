package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人才需求导入DTO (Sheet2: industry_talent_demand)
 */
@Data
public class TalentDemandExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("人才需求(万人)")
    private BigDecimal demandValue;

    @ExcelProperty("人才需求标签")
    private String demandLabel;

    @ExcelProperty(value = "人才需求描述(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> demandDescriptions;
}
