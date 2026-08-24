package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Reduce 阶段全局研判结果（序列化为 JSONB 存入 reduce_result）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReduceResult {

    /** 第一部分：学生画像 */
    private String studentProfile;

    /** 第二部分：外部宏观全景研判 */
    private MacroAnalysisVO macroAnalysis;

    /** 第三部分：SWOT 象限分析 */
    private String swot;

    /** 第四部分：推荐填报梯队顺序 */
    private String recommendation;

    /** 第六部分：大学专项拆解 */
    private List<HtmlPartResult> sixthPartResults;

    /** 第七部分：专业专项拆解 */
    private List<HtmlPartResult> seventhPartResults;

    /** 第八部分：城市专项拆解 */
    private List<HtmlPartResult> eighthPartResults;

    /** 第九部分：综合评判 */
    private HtmlPartResult ninthPartResult;

    /** 第十部分：央国企对口方向（10.1 AI部分） */
    private HtmlPartResult soeDirectionResult;

    /** 第十部分：体制内适配分析文字（10.2） */
    private HtmlPartResult civilServiceResult;

    /** 第十部分：体制内适配度打分数据（10.2 图表） */
    private List<ScoreItem> civilServiceScores;

    /** 第十部分：体制内适配度柱状图 Base64（生成阶段填充） */
    private String civilServiceChartBase64;

    /** 第十部分：民营企业赛道（10.3，每个专业一项） */
    private List<HtmlPartResult> privateSectorResults;

    /**
     * 体制内适配度打分项（10.2 柱状图数据）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreItem {
        /** 专业组（大学名称等） */
        private String groupName;
        /** 专业名称 */
        private String majorName;
        /** 体制内适配度 0-100 整数 */
        private Integer score;
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
}
