package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.*;
import com.haifeng.common.mapper.algorithm.ProvinceConfigMapper;
import com.haifeng.common.mapper.algorithm.SafetyLevelDictMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.service.algorithm.safety.calculator.ConstraintWeightCalculator;
import com.haifeng.common.service.algorithm.safety.calculator.ScoreBasedCalculator;
import com.haifeng.common.service.algorithm.safety.dto.ConstraintWeightResult;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcContext;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyLevelServiceImpl implements SafetyLevelService {

    private final ConstraintWeightCalculator constraintWeightCalculator;
    private final ScoreBasedCalculator scoreBasedCalculator;
    private final SafetyLevelDictMapper safetyLevelDictMapper;
    private final ProvinceConfigMapper provinceConfigMapper;
    private final ScoreRankMapper scoreRankMapper;

    @Override
    public SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                                  AdmissionMajorScore major,
                                                  AdmissionGroup group,
                                                  List<AdmissionGroup> historyGroups,
                                                  List<String> userConstraints) {
        // 1. 约束权重计算
        ConstraintWeightResult weightResult = constraintWeightCalculator.calculate(
                userConstraints,
                group.getConstraints(),
                major.getConstraints()
        );

        // 如果被阻止，直接返回
        if (weightResult.isBlocked()) {
            return SafetyCalcResult.blocked(weightResult.getReason());
        }

        // 2. 检查历史数据
        if (historyGroups == null || historyGroups.isEmpty()) {
            return SafetyCalcResult.noData();
        }

        // 3. 查询同分密度
        BigDecimal density = null;
        if (gaokao.getScore() != null && gaokao.getGaokaoProvince() != null
                && gaokao.getSubjectType() != null && gaokao.getGaokaoYear() != null) {
            density = scoreRankMapper.selectDensity(
                    gaokao.getGaokaoProvince(),
                    gaokao.getGaokaoYear(),
                    gaokao.getSubjectType(),
                    gaokao.getScore()
            );
        }

        // 4. 查询省份配置
        ProvinceConfig provinceConfig = null;
        if (gaokao.getGaokaoProvince() != null) {
            provinceConfig = provinceConfigMapper.selectByProvince(gaokao.getGaokaoProvince());
        }

        // 5. 计算基础分
        double baseScore = scoreBasedCalculator.calculate(gaokao, historyGroups, density, provinceConfig);

        // 6. 应用约束权重
        double finalScore = baseScore * weightResult.getWeight().doubleValue();

        // 7. Clamp 并转换
        finalScore = Math.min(Math.max(finalScore, 0.01), 0.99);
        BigDecimal safetyLevel = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);

        // 8. 获取等级信息
        SafetyLevelDict levelDict = getLevelByCoefficient(safetyLevel);
        String levelShort = levelDict != null ? levelDict.getNameShort() : "稳";
        String description = levelDict != null ? levelDict.getDescription() : "";

        return SafetyCalcResult.builder()
                .safetyLevel(safetyLevel)
                .levelShort(levelShort)
                .safetyDescription(description)
                .build();
    }

    @Override
    public SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                                 AdmissionMajorScore major,
                                                 AdmissionGroup group,
                                                 List<String> userConstraints,
                                                 SafetyCalcContext context) {
        // 1. 约束权重计算（使用 context 中预聚合的 severityMap，避免重复查询字典）
        ConstraintWeightResult weightResult = calculateWeightWithContext(
                userConstraints,
                group.getConstraints(),
                major.getConstraints(),
                context != null ? context.getSeverityMap() : Collections.emptyMap()
        );

        if (weightResult.isBlocked()) {
            return SafetyCalcResult.blocked(weightResult.getReason());
        }

        // 2. 检查专业历史数据
        List<MajorHistoryItem> majorHistory = context != null ? context.getMajorHistory() : null;
        if (majorHistory == null || majorHistory.isEmpty()) {
            return SafetyCalcResult.noData();
        }

        // 3. 将 MajorHistoryItem 转换为 AdmissionGroup 以复用 ScoreBasedCalculator
        List<AdmissionGroup> historyGroups = convertToAdmissionGroups(majorHistory, context.getReformYear());

        // 4. 计算基础分（使用 context 中预聚合的 density、provinceConfig）
        double baseScore = scoreBasedCalculator.calculate(
                gaokao,
                historyGroups,
                context.getDensity(),
                context.getProvinceConfig()
        );

        // 5. 应用约束权重
        double finalScore = baseScore * weightResult.getWeight().doubleValue();

        // 6. Clamp 并转换
        finalScore = Math.min(Math.max(finalScore, 0.01), 0.99);
        BigDecimal safetyLevel = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);

        // 7. 获取等级信息
        SafetyLevelDict levelDict = getLevelByCoefficient(safetyLevel);
        String levelShort = levelDict != null ? levelDict.getNameShort() : "稳";
        String description = levelDict != null ? levelDict.getDescription() : "";

        return SafetyCalcResult.builder()
                .safetyLevel(safetyLevel)
                .levelShort(levelShort)
                .safetyDescription(description)
                .build();
    }

    /**
     * 使用预聚合的 severityMap 计算约束权重，避免每条专业重复查询字典
     */
    private ConstraintWeightResult calculateWeightWithContext(List<String> userConstraints,
                                                              List<String> groupConstraints,
                                                              List<String> majorConstraints,
                                                              java.util.Map<String, String> severityMap) {
        if (userConstraints == null || userConstraints.isEmpty()) {
            return ConstraintWeightResult.ok();
        }
        if (severityMap == null) severityMap = Collections.emptyMap();

        BigDecimal weightSoftGroup = new BigDecimal("0.6");
        BigDecimal weightSoftBoth = new BigDecimal("0.3");

        // 步骤1：专业组约束检查
        boolean groupHasSoft = false;
        if (groupConstraints != null) {
            for (String code : userConstraints) {
                if (!groupConstraints.contains(code)) continue;
                String severity = severityMap.get(code);
                if ("HARD".equals(severity)) {
                    return ConstraintWeightResult.blocked("专业组限制：" + code);
                }
                if ("SOFT".equals(severity)) {
                    groupHasSoft = true;
                }
            }
        }

        // 步骤2：专业明细约束检查
        if (majorConstraints != null) {
            for (String code : userConstraints) {
                if (!majorConstraints.contains(code)) continue;
                String severity = severityMap.get(code);
                if ("HARD".equals(severity)) {
                    return ConstraintWeightResult.blocked("专业限制：" + code);
                }
                if ("SOFT".equals(severity)) {
                    return ConstraintWeightResult.softWeight(groupHasSoft ? weightSoftBoth : weightSoftGroup);
                }
            }
        }

        if (groupHasSoft) {
            return ConstraintWeightResult.softWeight(weightSoftGroup);
        }
        return ConstraintWeightResult.ok();
    }

    /**
     * 将 MajorHistoryItem 转换为 AdmissionGroup（仅填充 ScoreBasedCalculator 需要的字段）
     * subjects 字段根据 reformYear 区分新旧高考：record.year >= reformYear 视为新高考
     */
    private List<AdmissionGroup> convertToAdmissionGroups(List<MajorHistoryItem> items, Short reformYear) {
        List<AdmissionGroup> groups = new ArrayList<>(items.size());
        for (MajorHistoryItem item : items) {
            AdmissionGroup g = new AdmissionGroup();
            g.setYear(item.getYear());
            g.setMinScore(item.getMinScore());
            g.setMinRank(item.getMinRank());
            g.setAvgScore(item.getAvgScore());
            g.setAvgRank(item.getAvgRank());
            g.setMaxScore(item.getMaxScore());
            g.setMaxRank(item.getMaxRank());
            g.setAdmissionCount(item.getAdmissionCount());
            // subjects 用于 calcQualityMod 中区分新旧高考
            boolean isNewGaokao = reformYear != null && item.getYear() != null
                    && item.getYear() >= reformYear;
            g.setSubjects(isNewGaokao ? Collections.singletonList("物理") : Collections.emptyList());
            groups.add(g);
        }
        return groups;
    }

    @Override
    public SafetyLevelDict getLevelByCoefficient(BigDecimal coefficient) {
        if (coefficient == null) {
            return null;
        }
        // 特殊处理：系数为0时返回"禁"
        if (coefficient.compareTo(BigDecimal.ZERO) == 0) {
            return SafetyLevelDict.builder()
                    .level((short) 0)
                    .code("BLOCKED")
                    .name("不可报考")
                    .nameShort("禁")
                    .color("#999999")
                    .description("存在硬性报考限制，不可报考")
                    .build();
        }
        return safetyLevelDictMapper.selectByCoefficient(coefficient);
    }
}
