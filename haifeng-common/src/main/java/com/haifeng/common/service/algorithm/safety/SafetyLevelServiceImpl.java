package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.*;
import com.haifeng.common.mapper.algorithm.ProvinceConfigMapper;
import com.haifeng.common.mapper.algorithm.SafetyLevelDictMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.service.algorithm.safety.calculator.ConstraintWeightCalculator;
import com.haifeng.common.service.algorithm.safety.calculator.ScoreBasedCalculator;
import com.haifeng.common.service.algorithm.safety.dto.ConstraintWeightResult;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
