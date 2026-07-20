package com.haifeng.common.service.algorithm.safety.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SafetyCalcResult {

    /** 安全系数 0.00~1.00 */
    private BigDecimal safetyLevel;

    /** 等级简称：搏/冲/稳/保/垫/禁 */
    private String levelShort;

    /** 说明（约束原因或数据不足提示） */
    private String safetyDescription;

    public static SafetyCalcResult blocked(String reason) {
        return SafetyCalcResult.builder()
                .safetyLevel(BigDecimal.ZERO)
                .levelShort("禁")
                .safetyDescription(reason)
                .build();
    }

    public static SafetyCalcResult noData() {
        return SafetyCalcResult.builder()
                .safetyLevel(BigDecimal.ZERO)
                .levelShort("禁")
                .safetyDescription("暂无专业明细数据")
                .build();
    }
}
