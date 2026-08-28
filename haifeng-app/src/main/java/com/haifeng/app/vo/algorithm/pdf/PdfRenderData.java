package com.haifeng.app.vo.algorithm.pdf;

import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
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

    /** 用户高考档案（供封面展示选科/线差等信息） */
    private MemberGaokaoContextVO memberProfile;

    /** 生成时间（格式化字符串） */
    private String generatedAt;

    /** Logo base64 data URI */
    private String logoDataUri;

    // ===================== AI 全局分析（HTML） =====================

    /** 第一部分：学生画像 */
    private String studentProfileHtml;

    /** 第二部分：院校分析 HTML */
    private String universityAnalysisHtml;

    /** 第二部分：专业分析 HTML */
    private String majorAnalysisHtml;

    /** 第二部分：城市分析 HTML */
    private String cityAnalysisHtml;

    /** 第二部分：综合考虑 - 排名表 HTML */
    private String rankingTableHtml;

    /** 第二部分：综合考虑 - 排序理由 HTML */
    private String reasoningHtml;

    /** 第二部分：综合考虑 - 优先级图表 Base64 */
    private String chartBase64;

    /** 第三部分：SWOT 象限分析 HTML */
    private String swotHtml;

    /** 第四部分：推荐填报梯队顺序 HTML */
    private String recommendationHtml;

    // ===================== 第六、七、八、九部分 =====================

    /** 第六部分：大学专项拆解列表 */
    private List<HtmlPartResult> sixthPartResults;

    /** 第七部分：专业专项拆解列表 */
    private List<HtmlPartResult> seventhPartResults;

    /** 第八部分：城市专项拆解列表 */
    private List<HtmlPartResult> eighthPartResults;

    /** 第九部分：综合评判 */
    private HtmlPartResult ninthPartResult;

    // ===================== 第十部分：就业前景与展望 =====================

    /** 10.1 央国企对口方向 HTML（AI 生成） */
    private String soeDirectionHtml;

    /** 10.2 体制内适配分析 HTML（AI 生成） */
    private String civilServiceHtml;

    /** 10.2 体制内适配度柱状图 Base64 */
    private String civilServiceChartBase64;

    /** 10.3 民营企业赛道列表（每个专业一项，contentMd 已转 HTML） */
    private List<HtmlPartResult> privateSectorResults;

    /** 第二部分：赛道分类研判 HTML */
    private String trackAnalysisHtml;

    /** 第二部分：政策红利分析 HTML */
    private String policyAnalysisHtml;

    // ===================== 汇总表（按档位分组） =====================

    /** 按档位分组的汇总数据 */
    private List<LevelGroupSummary> levelGroupSummaries;

    /** 各档位限制个数 */
    private WishPlanLimitVO levelLimits;

    // ===================== 各校详情 =====================

    private List<GroupRenderData> groups;

    // ===================== 第十一部分：官方数据源与查询渠道 =====================

    /** 11.1 用户省份对应的省级教育考试院（null 时模板跳过该卡片） */
    private ProvinceExamSiteVO provinceExamSite;

    /** 11.2 院校官网列表（去重、仅含查到 website 的） */
    private List<UniversitySiteVO> universitySites;

    // ===================== 嵌套类 =====================

    /**
     * 按档位分组的汇总数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelGroupSummary {
        /** 档位简称：搏/冲/稳/保/垫 */
        private String levelShort;
        /** 档位中文名：大胆冲刺/可以冲击/较为稳妥/比较安全/高度保底 */
        private String levelName;
        /** 安全系数范围描述：0.00 ~ 0.30 */
        private String rangeText;
        /** 颜色标识 */
        private String color;
        /** 浅色背景（用于卡片背景） */
        private String bgColor;
        /** 已选个数 */
        private Integer selectedCount;
        /** 该档位下的专业列表 */
        private List<SummaryRow> majors;
    }

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
        /** 历史录取分（最近3年） */
        private List<HistoryScoreRender> historyScores;
    }

    /**
     * 通用 HTML 结果（第六、七、八、九部分共用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HtmlPartResult {
        /** 标识ID（大学名称/专业名称/城市名称） */
        private String identifier;
        /** 标题 */
        private String title;
        /** 分析内容（Markdown 格式） */
        private String contentMd;
    }

    /** 省级教育考试院信息（11.1） */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProvinceExamSiteVO {
        private String province;
        private String siteName;
        private String url;
    }

    /** 院校官网信息（11.2） */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UniversitySiteVO {
        private String universityName;
        private String website;
    }
}
