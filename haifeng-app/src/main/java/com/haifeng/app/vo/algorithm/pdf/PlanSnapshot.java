package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封面页数据快照（序列化为 JSONB 存入 plan_snapshot）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSnapshot {

    private Short planYear;
    private String planProvince;
    private String reformModel;
    private Integer userScore;
    private Integer userRank;
    private String planBatch;

    /**
     * 用户高考档案（供 AI 分析与 PDF 封面展示）
     * <p>包含选科、各科成绩、身体条件、身份条件、批次线差等可选信息。
     */
    private MemberGaokaoContextVO memberProfile;
}
