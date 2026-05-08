package com.haifeng.admin.vo.industry;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class IndustryDetailVO {

    // ==================== 主表字段 ====================

    private Long id;

    /**
     * 行业名称
     */
    private String industryName;

    /**
     * 行业分类
     */
    private String category;

    /**
     * 图标样式类名
     */
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
    private String marketScale;

    /**
     * 人才缺口
     */
    private String talentGap;

    /**
     * 投资热度
     */
    private BigDecimal investmentHeat;

    /**
     * 增长趋势
     */
    private String growthTrend;

    /**
     * 市场趋势
     */
    private String marketTrend;

    /**
     * 人才趋势
     */
    private String talentTrend;

    /**
     * 投资趋势
     */
    private String investmentTrend;

    /**
     * 删除状态
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ==================== 详情表字段 ====================

    /**
     * 详情表ID
     */
    private Long detailId;

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
