package com.haifeng.common.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PopulationBucketEnumTest {

    @Test
    void getMin_LT_2000_returnsZero() {
        assertEquals(0, PopulationBucketEnum.LT_2000.getMin());
    }

    @Test
    void getMax_LT_2000_returns2000() {
        assertEquals(2000, PopulationBucketEnum.LT_2000.getMax());
    }

    @Test
    void getMin_BTW_2000_3000_returns2000() {
        assertEquals(2000, PopulationBucketEnum.BTW_2000_3000.getMin());
    }

    @Test
    void getMax_BTW_2000_3000_returns3000() {
        assertEquals(3000, PopulationBucketEnum.BTW_2000_3000.getMax());
    }

    @Test
    void getMin_BTW_3000_4000_returns3000() {
        assertEquals(3000, PopulationBucketEnum.BTW_3000_4000.getMin());
    }

    @Test
    void getMax_BTW_3000_4000_returns4000() {
        assertEquals(4000, PopulationBucketEnum.BTW_3000_4000.getMax());
    }

    @Test
    void getMin_GTE_4000_returns4000() {
        assertEquals(4000, PopulationBucketEnum.GTE_4000.getMin());
    }

    @Test
    void getMax_GTE_4000_returnsNull() {
        assertNull(PopulationBucketEnum.GTE_4000.getMax(),
                "GTE_4000 上界为 +∞,用 null 表示无上界");
    }

    @Test
    void allFourBucketsExist() {
        assertEquals(4, PopulationBucketEnum.values().length);
    }
}
