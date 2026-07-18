package com.haifeng.common.service.algorithm.safety.calculator;

import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.entity.algorithm.GaokaoConfig;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.GaokaoConfigMapper;
import com.haifeng.common.service.algorithm.safety.dto.ConstraintWeightResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConstraintWeightCalculator {

    private final ConstraintDictMapper constraintDictMapper;
    private final GaokaoConfigMapper gaokaoConfigMapper;

    /**
     * 计算约束权重
     *
     * @param userConstraints  用户触发的约束 codes
     * @param groupConstraints 专业组的 constraints 数组
     * @param majorConstraints 专业明细的 constraints 数组
     * @return 权重结果
     */
    public ConstraintWeightResult calculate(List<String> userConstraints,
                                            List<String> groupConstraints,
                                            List<String> majorConstraints) {
        if (userConstraints == null || userConstraints.isEmpty()) {
            return ConstraintWeightResult.ok();
        }

        // 每次计算实时加载配置，确保配置更新即时生效
        GaokaoConfig config = gaokaoConfigMapper.selectSingleton();
        BigDecimal weightSoftGroup = config != null ? config.getWeightSoftGroup() : BigDecimal.valueOf(0.6);
        BigDecimal weightSoftBoth = config != null ? config.getWeightSoftBoth() : BigDecimal.valueOf(0.3);

        // 步骤1：专业组约束检查
        List<String> groupIntersection = intersection(userConstraints, groupConstraints);
        boolean groupHasSoft = false;

        if (!groupIntersection.isEmpty()) {
            Map<String, String> severityMap = querySeverity(groupIntersection);

            for (String code : groupIntersection) {
                String severity = severityMap.get(code);
                if ("HARD".equals(severity)) {
                    return ConstraintWeightResult.blocked("专业组限制：" + code);
                }
                if ("SOFT".equals(severity)) {
                    groupHasSoft = true;
                }
            }
        }

        BigDecimal groupWeight = groupHasSoft ? weightSoftGroup : BigDecimal.ONE;

        // 步骤2：专业明细约束检查
        List<String> majorIntersection = intersection(userConstraints, majorConstraints);

        if (!majorIntersection.isEmpty()) {
            Map<String, String> severityMap = querySeverity(majorIntersection);

            for (String code : majorIntersection) {
                String severity = severityMap.get(code);
                if ("HARD".equals(severity)) {
                    return ConstraintWeightResult.blocked("专业限制：" + code);
                }
                if ("SOFT".equals(severity)) {
                    if (groupHasSoft) {
                        return ConstraintWeightResult.softWeight(weightSoftBoth);
                    } else {
                        return ConstraintWeightResult.softWeight(weightSoftGroup);
                    }
                }
            }
        }

        // 步骤3：无约束冲突
        if (groupHasSoft) {
            return ConstraintWeightResult.softWeight(groupWeight);
        }
        return ConstraintWeightResult.ok();
    }

    private List<String> intersection(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            return new ArrayList<>();
        }
        return list1.stream()
                .filter(list2::contains)
                .collect(Collectors.toList());
    }

    private Map<String, String> querySeverity(List<String> codes) {
        List<ConstraintDict> dicts = constraintDictMapper.selectSeverityByCodes(codes);
        return dicts.stream()
                .collect(Collectors.toMap(ConstraintDict::getCode, ConstraintDict::getSeverity));
    }
}
