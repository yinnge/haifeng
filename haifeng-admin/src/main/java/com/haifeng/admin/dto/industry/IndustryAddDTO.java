package com.haifeng.admin.dto.industry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class IndustryAddDTO {

    // ==================== 主表字段 ====================

    @NotBlank(message = "行业名称不能为空")
    @Size(max = 100, message = "行业名称不能超过100个字符")
    private String industryName;

    @NotBlank(message = "行业分类不能为空")
    @Size(max = 50, message = "行业分类不能超过50个字符")
    private String category;

    /**
     * 图标样式类名
     */
    @Size(max = 100, message = "图标样式类名不能超过100个字符")
    private String iconClass;

    /**
     * 行业描述
     */
    private String description;

    /**
     * 年增长率（%）
     */
    private BigDecimal annualGrowthRate;

    /**
     * 市场规模
     */
    @Size(max = 50, message = "市场规模不能超过50个字符")
    private String marketScale;

    /**
     * 人才缺口
     */
    @Size(max = 50, message = "人才缺口不能超过50个字符")
    private String talentGap;

    /**
     * 投资热度
     */
    private BigDecimal investmentHeat;

    /**
     * 增长趋势
     */
    @Size(max = 20, message = "增长趋势不能超过20个字符")
    private String growthTrend;

    /**
     * 市场趋势
     */
    @Size(max = 20, message = "市场趋势不能超过20个字符")
    private String marketTrend;

    /**
     * 人才趋势
     */
    @Size(max = 20, message = "人才趋势不能超过20个字符")
    private String talentTrend;

    /**
     * 投资趋势
     */
    @Size(max = 20, message = "投资趋势不能超过20个字符")
    private String investmentTrend;

    // ==================== 详情表字段 ====================

    /**
     * 简短描述
     */
    private String shortDescription;

    /**
     * 详细描述
     */
    private String detailedDescription;

    /**
     * 行业规模数据
     */
    private Map<String, Object> industryScale;

    /**
     * 行业人才需求数据
     */
    private Map<String, Object> industryTalentDemand;

    /**
     * 行业薪资数据
     */
    private Map<String, Object> industrySalary;

    /**
     * 政策信息
     */
    private Map<String, Object> policyInfo;

    /**
     * 发展支持信息
     */
    private Map<String, Object> developmentSupportInfo;

    /**
     * 人才分析
     */
    private Map<String, Object> talentAnalysis;

    /**
     * 人才政策
     */
    private Map<String, Object> talentPolicy;

    /**
     * 薪资数据
     */
    private Map<String, Object> salaryData;
}
