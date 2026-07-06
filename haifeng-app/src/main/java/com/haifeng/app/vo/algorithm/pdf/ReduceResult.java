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

    /** 全局宏观分析 */
    private String globalAnalysis;

    /** SWOT 象限分析 */
    private String swot;

    /** 推荐填报梯队顺序 */
    private String recommendation;
}
