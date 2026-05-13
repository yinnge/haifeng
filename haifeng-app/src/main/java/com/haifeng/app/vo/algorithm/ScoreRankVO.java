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
}
