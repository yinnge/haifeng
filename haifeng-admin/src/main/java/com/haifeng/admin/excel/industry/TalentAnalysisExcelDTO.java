package com.haifeng.admin.excel.industry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.util.List;

/**
 * 人才需求分析导入DTO (Sheet6: talent_analysis)
 */
@Data
public class TalentAnalysisExcelDTO {

    @ExcelProperty("行业名称")
    private String industryName;

    @ExcelProperty("分析标题")
    private String analysisTitle;

    @ExcelProperty(value = "紧缺岗位(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> shortagePositions;

    @ExcelProperty("学历要求")
    private String educationRequirement;

    @ExcelProperty("专业要求")
    private String majorRequirement;

    @ExcelProperty("人才趋势描述")
    private String talentTrendDescription;
}
