package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class YearScoreVO {
    private Short year;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private Integer maxScore;
    private Integer maxRank;
    private Integer admissionCount;
}
