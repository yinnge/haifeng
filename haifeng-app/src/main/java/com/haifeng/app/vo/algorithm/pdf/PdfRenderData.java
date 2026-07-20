package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * PDF 渲染综合数据模型
 * <p>合并 AI 分析结果（已转 HTML）+ 快照表静态数据，供 Thymeleaf 模板渲染使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRenderData {

    // ===================== 封面信息 =====================

    private Short planYear;
    private String planProvince;
    private String reformModel;
    private Integer userScore;
    private Integer userRank;
    private String planBatch;

    /** 生成时间（格式化字符串） */
    private String generatedAt;

    /** Logo base64 data URI */
    private String logoDataUri;

    // ===================== AI 全局分析（HTML） =====================

    private String globalAnalysisHtml;
    private String swotHtml;
    private String recommendationHtml;

    // ===================== 汇总表 =====================

    private List<SummaryRow> summaryRows;

    // ===================== 各校详情 =====================

    private List<GroupRenderData> groups;

    // ===================== 嵌套类 =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupRenderData {
        private Integer groupSnapshotId;
        private Long universityId;
        private String universityName;
        private String cityName;
        private String groupCode;
        private String groupName;
        private String category;
        private String nature;
        private List<String> tags;
        private List<String> subjects;
        private List<String> constraintsDescription;
        private Integer groupSortOrder;

        /** AI 评语 HTML（已从 Markdown 转换） */
        private String commentaryHtml;

        /** AI 调用是否成功 */
        private Boolean aiSuccess;

        /** 城市增强数据（产业/GDP/薪资等） */
        private CityEnrichmentVO cityEnrichment;

        /** 该组下可导出的专业列表 */
        private List<MajorRenderData> majors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorRenderData {
        private Long majorId;
        private String majorName;
        private String majorCode;
        private String duration;
        private String tuition;
        private Integer admissionCount;
        private BigDecimal safetyLevel;
        private String levelShort;
        private List<HistoryScoreRender> historyScores;

        /** 专业增强数据（就业率/薪资/就业前景等） */
        private MajorEnrichmentVO majorEnrichment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryScoreRender {
        private Integer year;
        private Integer minScore;
        private Integer minRank;
        private BigDecimal avgScore;
        private Integer avgRank;
        private Integer maxScore;
        private Integer maxRank;
        private Integer admissionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryRow {
        private String universityName;
        private String groupName;
        private String groupCode;
        private String majorName;
        private String majorCode;
        private String levelShort;
        private BigDecimal safetyLevel;
        private String tuition;
        private String cityName;
    }
}
