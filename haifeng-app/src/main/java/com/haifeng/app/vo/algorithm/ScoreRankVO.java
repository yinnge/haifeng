package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 位次查询响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRankVO {

    /**
     * 位次
     */
    private Integer rank;

    /**
     * 同分人数
     */
    private Integer sameScoreCount;

    /**
     * 数据实际年份（当年无数据时回溯的最近一年；等于查询年份则为 null 或当前年）
     */
    private Integer dataYear;
}
