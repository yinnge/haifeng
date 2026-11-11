package com.haifeng.common.service.algorithm.safety.calculator;

import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.GaokaoConfigMapper;
import com.haifeng.common.service.algorithm.safety.dto.ConstraintWeightResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 约束名称匹配：专业明细的 constraints 存的是字典 name，
 * 用户触发的是字典 code，算法必须先做 code -> name 映射再比对。
 */
@ExtendWith(MockitoExtension.class)
class ConstraintWeightCalculatorTest {

    @Mock
    private ConstraintDictMapper constraintDictMapper;

    @Mock
    private GaokaoConfigMapper gaokaoConfigMapper;

    @InjectMocks
    private ConstraintWeightCalculator calculator;

    @Test
    void dbVersionBlocksWhenUserCodeMapsToMajorConstraintName() {
        when(constraintDictMapper.selectSeverityByCodes(List.of("NO_COLOR_BLIND")))
                .thenReturn(List.of(dict("NO_COLOR_BLIND", "色盲不可报考", "HARD")));

        ConstraintWeightResult result = calculator.calculate(
                List.of("NO_COLOR_BLIND"),
                null,
                List.of("色盲不可报考")
        );

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.getReason()).contains("色盲不可报考");
    }

    @Test
    void memoryVersionBlocksWhenUserCodeMapsToMajorConstraintName() {
        ConstraintWeightResult result = calculator.calculate(
                List.of("NO_COLOR_BLIND"),
                null,
                List.of("色盲不可报考"),
                null,
                Map.of("色盲不可报考", "HARD")
        );

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.getReason()).contains("色盲不可报考");
    }

    @Test
    void memoryVersionAppliesSoftWeightWhenNamesMatch() {
        ConstraintWeightResult result = calculator.calculate(
                List.of("NO_COLOR_BLIND"),
                null,
                List.of("色盲不可报考"),
                null,
                Map.of("色盲不可报考", "SOFT")
        );

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getWeight()).isEqualByComparingTo(new BigDecimal("0.6"));
    }

    @Test
    void memoryVersionDoesNotMatchCodeAgainstStoredName() {
        ConstraintWeightResult result = calculator.calculate(
                List.of("NO_COLOR_BLIND"),
                null,
                List.of("色盲不可报考"),
                null,
                Map.of("NO_COLOR_BLIND", "HARD")
        );

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getWeight()).isEqualByComparingTo(BigDecimal.ONE);
    }

    private ConstraintDict dict(String code, String name, String severity) {
        return ConstraintDict.builder()
                .code(code)
                .name(name)
                .severity(severity)
                .build();
    }
}
