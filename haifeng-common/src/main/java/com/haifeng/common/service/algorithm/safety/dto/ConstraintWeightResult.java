package com.haifeng.common.service.algorithm.safety.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConstraintWeightResult {

    /** 权重系数：0.0 / 0.3 / 0.6 / 1.0 */
    private BigDecimal weight;

    /** 是否被阻止（不可报考） */
    private boolean blocked;

    /** 原因说明 */
    private String reason;

    public static ConstraintWeightResult blocked(String reason) {
        return ConstraintWeightResult.builder()
                .weight(BigDecimal.ZERO)
                .blocked(true)
                .reason(reason)
                .build();
    }

    public static ConstraintWeightResult ok() {
        return ConstraintWeightResult.builder()
                .weight(BigDecimal.ONE)
                .blocked(false)
                .build();
    }

    public static ConstraintWeightResult softWeight(BigDecimal weight) {
        return ConstraintWeightResult.builder()
                .weight(weight)
                .blocked(false)
                .build();
    }
}
