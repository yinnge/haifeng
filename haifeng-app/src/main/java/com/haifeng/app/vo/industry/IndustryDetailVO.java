package com.haifeng.app.vo.industry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * C 端行业详情 VO（任务 2 接口 2，来自 t_industry_detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 来自 t_industry_detail =====
    private String industryName;
    private String shortDescription;
    private String detailedDescription;
    private Map<String, Object> industryScale;
    private Map<String, Object> industryTalentDemand;
    private Map<String, Object> industrySalary;
    private Map<String, Object> policyInfo;
    private Map<String, Object> developmentSupportInfo;
    private Map<String, Object> talentAnalysis;
    private Map<String, Object> talentPolicy;
    private Map<String, Object> salaryData;
}
