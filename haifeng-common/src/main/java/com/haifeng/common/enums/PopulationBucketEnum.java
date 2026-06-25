package com.haifeng.common.enums;

import lombok.Getter;

/**
 * 城市常住人口区间桶
 * SQL 条件生成规则:
 *   population &gt;= getMin()
 *   [population &lt; getMax()]  ← 仅当 getMax() != null 时
 */
@Getter
public enum PopulationBucketEnum {

    LT_2000(0, 2000),
    BTW_2000_3000(2000, 3000),
    BTW_3000_4000(3000, 4000),
    GTE_4000(4000, null);

    private final int min;
    private final Integer max;

    PopulationBucketEnum(int min, Integer max) {
        this.min = min;
        this.max = max;
    }
}
