package com.haifeng.app.vo.city;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * C 端城市数据概览 VO（右侧统计卡片，一次聚合查询）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 城市总数（个） */
    private Long cityCount;

    /** 高校总数（所，sum(college_count)） */
    private Long collegeTotal;

    /** 全国常住人口合计（万人） */
    private BigDecimal populationTotal;

    /** 全国GDP合计（亿元） */
    private BigDecimal gdpTotal;
}
