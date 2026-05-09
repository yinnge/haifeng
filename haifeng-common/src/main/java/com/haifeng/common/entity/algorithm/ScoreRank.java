package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 一分一段位次表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_score_rank")
public class ScoreRank {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 年份
     */
    private Short year;

    /**
     * 科目类型（物理类/历史类/理科/文科）
     */
    private String subjectType;

    /**
     * 分数
     */
    private Short score;

    /**
     * 位次
     */
    private Integer rank;

    /**
     * 同分人数
     */
    private Integer sameScoreCount;

    /**
     * 累计人数
     */
    private Integer cumulativeCount;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
