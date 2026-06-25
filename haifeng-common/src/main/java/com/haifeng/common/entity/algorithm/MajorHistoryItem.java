package com.haifeng.common.entity.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 专业级历史录取数据
 * 用于安全系数计算，替代 AdmissionGroup 维度以提供更精确的预测
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorHistoryItem {

    /** 专业代码（用于按专业聚合历史数据） */
    private String majorCode;

    /** 录取年份 */
    private Short year;

    /** 最低分 */
    private Integer minScore;

    /** 最低分对应位次 */
    private Integer minRank;

    /** 平均分 */
    private BigDecimal avgScore;

    /** 平均位次 */
    private Integer avgRank;

    /** 最高分 */
    private Integer maxScore;

    /** 最高分对应位次 */
    private Integer maxRank;

    /** 录取人数 */
    private Integer admissionCount;
}
