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
import java.util.Collections;
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
     * 计算约束权重（DB 版）
     *
     * @param userConstraints  用户触发的约束 codes
     * @param groupConstraints 专业组的约束名称数组（当前业务已下线，通常为 null）
     * @param majorConstraints 专业明细的约束名称数组（存约束字典 name）
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

        // 用户约束是 code，专业明细存的是 name，先做一次 code -> name 映射
        Map<String, String> severityByName = querySeverityByName(userConstraints);
        return applyConstraints(severityByName, groupConstraints, majorConstraints,
                weightSoftGroup, weightSoftBoth);
    }

    /**
     * 计算约束权重（纯内存版本，无 DB 查询）
     * 权重值与 severityMap 由调用方预取（如 SafetyBatchContext），适用于批量计算场景
     *
     * @param userConstraints  用户触发的约束 codes
     * @param groupConstraints 专业组的约束名称数组
     * @param majorConstraints 专业明细的约束名称数组
     * @param config           预取的 GaokaoConfig（null 时权重回退 0.6/0.3）
     * @param severityMap      预取的约束名称 -> HARD/SOFT 映射（key 为约束字典 name）
     * @return 权重结果
     */
    public ConstraintWeightResult calculate(List<String> userConstraints,
                                            List<String> groupConstraints,
                                            List<String> majorConstraints,
                                            GaokaoConfig config,
                                            Map<String, String> severityMap) {
        if (userConstraints == null || userConstraints.isEmpty()) {
            return ConstraintWeightResult.ok();
        }
        if (severityMap == null) {
            severityMap = Collections.emptyMap();
        }

        BigDecimal weightSoftGroup = config != null && config.getWeightSoftGroup() != null
                ? config.getWeightSoftGroup() : BigDecimal.valueOf(0.6);
        BigDecimal weightSoftBoth = config != null && config.getWeightSoftBoth() != null
                ? config.getWeightSoftBoth() : BigDecimal.valueOf(0.3);

        return applyConstraints(severityMap, groupConstraints, majorConstraints,
                weightSoftGroup, weightSoftBoth);
    }

    /**
     * 按约束名称比对用户已触发的约束，计算最终权重。
     *
     * @param severityByName   用户已触发约束的名称 -> HARD/SOFT
     * @param groupConstraints 专业组约束名称数组
     * @param majorConstraints 专业明细约束名称数组
     */
    private ConstraintWeightResult applyConstraints(Map<String, String> severityByName,
                                                    List<String> groupConstraints,
                                                    List<String> majorConstraints,
                                                    BigDecimal weightSoftGroup,
                                                    BigDecimal weightSoftBoth) {
        // 步骤1：专业组约束检查
        boolean groupHasSoft = false;
        if (groupConstraints != null) {
            for (String name : groupConstraints) {
                String severity = severityByName.get(name);
                if (severity == null) {
                    continue;
                }
                if ("HARD".equals(severity)) {
                    return ConstraintWeightResult.blocked("专业组限制：" + name);
                }
                if ("SOFT".equals(severity)) {
                    groupHasSoft = true;
                }
            }
        }

        // 步骤2：专业明细约束检查
        if (majorConstraints != null) {
            for (String name : majorConstraints) {
                String severity = severityByName.get(name);
                if (severity == null) {
                    continue;
                }
                if ("HARD".equals(severity)) {
                    return ConstraintWeightResult.blocked("专业限制：" + name);
                }
                if ("SOFT".equals(severity)) {
                    return ConstraintWeightResult.softWeight(
                            groupHasSoft ? weightSoftBoth : weightSoftGroup);
                }
            }
        }

        // 步骤3：无约束冲突
        if (groupHasSoft) {
            return ConstraintWeightResult.softWeight(weightSoftGroup);
        }
        return ConstraintWeightResult.ok();
    }

    private Map<String, String> querySeverityByName(List<String> codes) {
        List<ConstraintDict> dicts = constraintDictMapper.selectSeverityByCodes(codes);
        return dicts.stream()
                .collect(Collectors.toMap(ConstraintDict::getName, ConstraintDict::getSeverity, (a, b) -> a));
    }
}
