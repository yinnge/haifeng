package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.*;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.GaokaoConfigMapper;
import com.haifeng.common.mapper.algorithm.ProvinceConfigMapper;
import com.haifeng.common.mapper.algorithm.SafetyLevelDictMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.service.algorithm.safety.calculator.ConstraintWeightCalculator;
import com.haifeng.common.service.algorithm.safety.calculator.ScoreBasedCalculator;
import com.haifeng.common.service.algorithm.safety.dto.ConstraintWeightResult;
import com.haifeng.common.service.algorithm.safety.dto.SafetyBatchContext;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyLevelServiceImpl implements SafetyLevelService {

    private final ConstraintWeightCalculator constraintWeightCalculator;
    private final ScoreBasedCalculator scoreBasedCalculator;
    private final SafetyLevelDictMapper safetyLevelDictMapper;
    private final ProvinceConfigMapper provinceConfigMapper;
    private final ScoreRankMapper scoreRankMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final GaokaoConfigMapper gaokaoConfigMapper;
    private final SafetyLevelDictCache safetyLevelDictCache;

    @Override
    public SafetyBatchContext buildBatchContext(MemberGaokao gaokao, List<String> userConstraints) {
        // 1. 同分密度（与单条路径相同的查询条件）
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

        // 2. 省配置
        ProvinceConfig provinceConfig = gaokao.getGaokaoProvince() != null
                ? provinceConfigMapper.selectByProvince(gaokao.getGaokaoProvince())
                : null;

        // 3. 约束权重配置（单例）
        GaokaoConfig gaokaoConfig = gaokaoConfigMapper.selectSingleton();

        // 4. 用户约束 severity 映射
        Map<String, String> severityMap = Collections.emptyMap();
        if (userConstraints != null && !userConstraints.isEmpty()) {
            severityMap = constraintDictMapper.selectSeverityByCodes(userConstraints).stream()
                    .collect(Collectors.toMap(ConstraintDict::getCode, ConstraintDict::getSeverity, (a, b) -> a));
        }

        return SafetyBatchContext.builder()
                .density(density)
                .provinceConfig(provinceConfig)
                .gaokaoConfig(gaokaoConfig)
                .severityMap(severityMap)
                .build();
    }

    @Override
    public SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                                  AdmissionMajorScore major,
                                                  AdmissionGroup group,
                                                  List<AdmissionGroup> historyGroups,
                                                  List<String> userConstraints) {
        // 1. 约束权重计算
        ConstraintWeightResult weightResult = constraintWeightCalculator.calculate(
                userConstraints,
                null,
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
                null,
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

    @Override
    public SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                                 AdmissionMajorScore major,
                                                 AdmissionGroup group,
                                                 List<AdmissionGroup> historyGroups,
                                                 List<String> userConstraints,
                                                 SafetyBatchContext ctx) {
        // 1. 约束权重计算（纯内存，使用预取的 GaokaoConfig 与 severityMap）
        ConstraintWeightResult weightResult = constraintWeightCalculator.calculate(
                userConstraints,
                null,
                major.getConstraints(),
                ctx != null ? ctx.getGaokaoConfig() : null,
                ctx != null ? ctx.getSeverityMap() : Collections.emptyMap()
        );

        if (weightResult.isBlocked()) {
            return SafetyCalcResult.blocked(weightResult.getReason());
        }

        // 2. 检查历史数据
        if (historyGroups == null || historyGroups.isEmpty()) {
            return SafetyCalcResult.noData();
        }

        // 3. 计算基础分（使用预取的密度、省配置）
        double baseScore = scoreBasedCalculator.calculate(
                gaokao,
                historyGroups,
                ctx != null ? ctx.getDensity() : null,
                ctx != null ? ctx.getProvinceConfig() : null
        );

        // 4. 应用约束权重
        double finalScore = baseScore * weightResult.getWeight().doubleValue();

        // 5. Clamp 并转换
        finalScore = Math.min(Math.max(finalScore, 0.01), 0.99);
        BigDecimal safetyLevel = BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP);

        // 6. 获取等级信息（使用字典缓存）
        SafetyLevelDict levelDict = getLevelByCoefficientCached(safetyLevel);
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
        // null config -> 权重回退 0.6/0.3，与既有行为一致
        return constraintWeightCalculator.calculate(
                userConstraints, groupConstraints, majorConstraints, null, severityMap);
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

    /**
     * 根据安全系数反查等级字典（使用 SafetyLevelDictCache，无 DB 查询）
     * 语义与 getLevelByCoefficient 一致，含系数为 0 返回"禁"的特殊处理
     */
    private SafetyLevelDict getLevelByCoefficientCached(BigDecimal coefficient) {
        if (coefficient == null) {
            return null;
        }
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
        return safetyLevelDictCache.getByCoefficient(coefficient);
    }
}
