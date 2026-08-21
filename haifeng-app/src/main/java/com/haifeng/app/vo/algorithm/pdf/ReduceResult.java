package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reduce 阶段全局研判结果（序列化为 JSONB 存入 reduce_result）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReduceResult {

    /** 第一部分：学生画像（保留原逻辑） */
    private String studentProfile;

    /** 第二部分：外部宏观全景研判 */
    private MacroAnalysisVO macroAnalysis;

    /** 第三部分：SWOT 象限分析 */
    private String swot;

    /** 第三部分：推荐填报梯队顺序 */
    private String recommendation;
}
