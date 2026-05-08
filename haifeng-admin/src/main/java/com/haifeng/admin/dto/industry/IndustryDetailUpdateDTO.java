package com.haifeng.admin.dto.industry;

import lombok.Data;

import java.util.Map;

@Data
public class IndustryDetailUpdateDTO {

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

    /**
     * 删除状态
     */
    private Boolean isDeleted;
}
