package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 外部宏观全景研判（Reduce 阶段第二部分）
 * <p>包含院校分析、专业分析、城市分析、综合考虑四个子模块
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacroAnalysisVO {

    /** 2.1 院校分析列表 */
    private List<UniversityAnalysis> universityAnalysis;

    /** 2.2 专业分析列表（去重后） */
    private List<MajorAnalysis> majorAnalysis;

    /** 2.3 城市分析列表（去重后） */
    private List<CityAnalysis> cityAnalysis;

    /** 2.4 综合考虑 */
    private ComprehensiveAnalysis comprehensiveAnalysis;

    // ===================== 嵌套类 =====================

    /**
     * 院校分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UniversityAnalysis {
        /** 院校名称 */
        private String universityName;
        /** 标签（985/211/双一流等） */
        private List<String> tags;
        /** 类别（综合/理工/师范等） */
        private String category;
        /** 办学性质（公办/民办） */
        private String nature;
        /** AI 分析内容（100-200字，Markdown格式） */
        private String aiAnalysis;
    }

    /**
     * 专业分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorAnalysis {
        /** 专业名称 */
        private String majorName;
        /** 学科门类（工学/理学等） */
        private String majorCategory;
        /** 专业类（计算机类等） */
        private String parentCategory;
        /** 专业标签 */
        private String majorTags;
        /** 授予学位 */
        private String degreeAwarded;
        /** 就业率（%） */
        private BigDecimal employmentRate;
        /** 薪资下限（元/月） */
        private Integer salaryMin;
        /** 薪资上限（元/月） */
        private Integer salaryMax;
        /** AI 分析内容（100-200字，Markdown格式） */
        private String aiAnalysis;
    }

    /**
     * 城市分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityAnalysis {
        /** 城市名称 */
        private String cityName;
        /** 城市等级 */
        private String cityLevel;
        /** GDP（亿元） */
        private BigDecimal gdp;
        /** GDP增长率（%） */
        private BigDecimal gdpGrowthRate;
        /** 世界500强企业数量 */
        private Integer fortune500Count;
        /** 主要产业 */
        private List<String> mainIndustries;
        /** 新兴产业 */
        private List<String> emergingIndustries;
        /** 平均薪资 */
        private BigDecimal avgSalary;
        /** 失业率 */
        private BigDecimal unemploymentRate;
        /** AI 分析内容（100-200字，Markdown格式） */
        private String aiAnalysis;
    }

    /**
     * 综合考虑
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComprehensiveAnalysis {
        /** 排名列表（按综合得分降序） */
        private List<RankingItem> ranking;
        /** 优先级柱状图 Base64 图片 */
        private String chartBase64;
        /** 排序理由说明（300-500字，Markdown格式） */
        private String reasoning;
    }

    /**
     * 排名项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingItem {
        /** 排名序号 */
        private Integer rank;
        /** 专业组名称 */
        private String groupName;
        /** 院校名称 */
        private String universityName;
        /** 城市名称 */
        private String cityName;
        /** 专业名称 */
        private String majorName;
        /** 综合得分（0-100） */
        private BigDecimal score;
        /** 档位（搏/冲/稳/保/垫） */
        private String levelShort;
    }
}
