package com.haifeng.common.service.algorithm.safety.calculator;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.GaokaoConfig;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import com.haifeng.common.mapper.algorithm.GaokaoConfigMapper;
import com.haifeng.common.service.algorithm.ProvinceReformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreBasedCalculator {

    private final ProvinceReformService provinceReformService;
    private final GaokaoConfigMapper gaokaoConfigMapper;

    // 年份权重：最近1年权重1.0，依次递减
    private double[] YEAR_WEIGHTS;

    // 默认参数
    private double DEFAULT_DENSITY_K;
    private double DEFAULT_LINE_STEEPNESS;
    private double DEFAULT_RANK_STEEPNESS;

    // 新高考权重
    private double NEW_GAOKAO_LINE_WEIGHT;
    private double NEW_GAOKAO_RANK_WEIGHT;

    // 旧高考权重
    private double OLD_GAOKAO_LINE_WEIGHT;
    private double OLD_GAOKAO_RANK_WEIGHT;

    @PostConstruct
    void init() {
        GaokaoConfig config = gaokaoConfigMapper.selectSingleton();
        if (config == null) {
            log.error("gaokao_config 表无数据，无法初始化 ScoreBasedCalculator");
            throw new IllegalStateException("gaokao_config 表无数据，无法初始化 ScoreBasedCalculator");
        }
        if (config.getYearWeights() == null || config.getYearWeights().isEmpty()) {
            log.error("gaokao_config.year_weights 为空");
            throw new IllegalStateException("gaokao_config.year_weights 为空");
        }
        List<BigDecimal> yw = config.getYearWeights();
        YEAR_WEIGHTS = new double[yw.size()];
        for (int i = 0; i < yw.size(); i++) {
            YEAR_WEIGHTS[i] = yw.get(i).doubleValue();
        }
        DEFAULT_DENSITY_K = config.getDefaultDensityK().doubleValue();
        DEFAULT_LINE_STEEPNESS = config.getDefaultLineSteepness().doubleValue();
        DEFAULT_RANK_STEEPNESS = config.getDefaultRankSteepness().doubleValue();
        NEW_GAOKAO_LINE_WEIGHT = config.getNewGaokaoLineWeight().doubleValue();
        NEW_GAOKAO_RANK_WEIGHT = config.getNewGaokaoRankWeight().doubleValue();
        OLD_GAOKAO_LINE_WEIGHT = config.getOldGaokaoLineWeight().doubleValue();
        OLD_GAOKAO_RANK_WEIGHT = config.getOldGaokaoRankWeight().doubleValue();
    }

    /**
     * 计算基础分
     *
     * @param gaokao        用户档案
     * @param historyGroups 历史专业组数据（近5年）
     * @param density       同分密度
     * @param provinceConfig 省份配置（可为null，使用默认值）
     * @return 基础分 0.01~0.99
     */
    public double calculate(MemberGaokao gaokao,
                            List<AdmissionGroup> historyGroups,
                            BigDecimal density,
                            ProvinceConfig provinceConfig) {
        if (historyGroups == null || historyGroups.isEmpty()) {
            return 0.5; // 无数据返回中性值
        }

        // 获取配置参数
        double densityK = provinceConfig != null && provinceConfig.getDensityK() != null
                ? provinceConfig.getDensityK().doubleValue() : DEFAULT_DENSITY_K;
        double lineSteepness = provinceConfig != null && provinceConfig.getLineSteepness() != null
                ? provinceConfig.getLineSteepness().doubleValue() : DEFAULT_LINE_STEEPNESS;
        double rankSteepness = provinceConfig != null && provinceConfig.getRankSteepness() != null
                ? provinceConfig.getRankSteepness().doubleValue() : DEFAULT_RANK_STEEPNESS;

        // 判断新旧高考
        boolean isNewGaokao = isNewGaokao(gaokao.getGaokaoProvince(), gaokao.getGaokaoYear());
        double lineWeight = isNewGaokao ? NEW_GAOKAO_LINE_WEIGHT : OLD_GAOKAO_LINE_WEIGHT;
        double rankWeight = isNewGaokao ? NEW_GAOKAO_RANK_WEIGHT : OLD_GAOKAO_RANK_WEIGHT;

        // 计算加权平均线差和位次
        double weightedLineDiff = 0;
        double weightedRank = 0;
        double totalLineWeight = 0;
        double totalRankWeight = 0;
        int validYears = 0;

        int currentYear = gaokao.getGaokaoYear();
        Integer userLineDiff = gaokao.getScoreAboveLine();
        Integer userRank = gaokao.getRank();

        for (AdmissionGroup group : historyGroups) {
            int yearAgo = currentYear - group.getYear();
            if (yearAgo < 1 || yearAgo > 5) continue;

            double w = YEAR_WEIGHTS[yearAgo - 1];
            validYears++;

            // 线差计算：最低分 - 批次线（需要额外查询批次线，这里用 min_score 近似）
            if (group.getMinScore() != null && gaokao.getBatchLineScore() != null) {
                int histLineDiff = group.getMinScore() - gaokao.getBatchLineScore();
                if (histLineDiff > 0) {
                    weightedLineDiff += histLineDiff * w;
                    totalLineWeight += w;
                }
            }

            // 位次计算：优先使用 avg_rank，降级使用 min_rank * 0.85
            double effRank = 0;
            if (group.getAvgRank() != null && group.getAvgRank() > 0) {
                effRank = group.getAvgRank();
            } else if (group.getMinRank() != null && group.getMinRank() > 0) {
                effRank = group.getMinRank() * 0.85;
            }
            if (effRank > 0) {
                weightedRank += effRank * w;
                totalRankWeight += w;
            }
        }

        // 计算 ratio
        Double lineDiffRatio = null;
        if (userLineDiff != null && totalLineWeight > 0) {
            double avgHistLine = weightedLineDiff / totalLineWeight;
            if (avgHistLine > 0) {
                lineDiffRatio = Math.min(userLineDiff / avgHistLine, 3.0);
            }
        }

        Double rankRatio = null;
        if (userRank != null && userRank > 0 && totalRankWeight > 0) {
            double avgHistRank = weightedRank / totalRankWeight;
            if (avgHistRank > 0) {
                rankRatio = Math.min(avgHistRank / userRank, 3.0);
            }
        }

        // 计算基础分
        Double lineScore = lineDiffRatio != null ? asymmetricSigmoid(lineDiffRatio, lineSteepness) : null;
        Double rankScore = rankRatio != null ? asymmetricSigmoid(rankRatio, rankSteepness) : null;

        double baseScore;
        if (lineScore != null && rankScore != null) {
            baseScore = (lineScore * lineWeight + rankScore * rankWeight) / (lineWeight + rankWeight);
        } else if (lineScore != null) {
            baseScore = lineScore;
        } else if (rankScore != null) {
            baseScore = rankScore;
        } else {
            baseScore = 0.5;
        }

        // 风险修正
        double result = baseScore;

        // 1. 波动风险
        double volatility = calcVolatility(historyGroups, currentYear, gaokao.getBatchLineScore());
        result = pullToCenter(result, volatility * 0.5);

        // 2. 招生计划变化
        Double planRatio = calcPlanRatio(historyGroups, currentYear);
        if (planRatio != null) {
            result = applyPlanModifier(result, planRatio);
        }

        // 3. 同分密度惩罚
        if (density != null && density.doubleValue() > 0) {
            double modifier = 1.0 - ((density.doubleValue() - 0.5) * densityK);
            modifier = Math.max(modifier, 0.70);
            result = result * modifier;
        }

        // 4. 数据质量修正
        double qualityMod = calcQualityMod(validYears, volatility, isNewGaokao, historyGroups,
                currentYear, gaokao.getGaokaoProvince());
        result = pullToCenter(result, 1.0 - qualityMod);

        // Clamp
        return Math.min(Math.max(result, 0.01), 0.99);
    }

    private boolean isNewGaokao(String province, Short gaokaoYear) {
        return provinceReformService.isNewGaokao(province, gaokaoYear);
    }

    private double asymmetricSigmoid(double ratio, double steepness) {
        if (ratio <= 0) return 0.01;
        double adjusted = ratio < 1.0 ? Math.pow(ratio, 2.2) : ratio;
        return 1.0 / (1.0 + Math.exp(-steepness * (adjusted - 1.0)));
    }

    private double pullToCenter(double score, double strength) {
        strength = Math.min(strength, 1.0);
        return score * (1 - strength) + 0.5 * strength;
    }

    private double calcVolatility(List<AdmissionGroup> history, int currentYear, Integer batchLine) {
        if (history.size() < 2 || batchLine == null) return 0.5;

        double[] lineDiffs = history.stream()
                .filter(g -> g.getMinScore() != null)
                .filter(g -> {
                    int ago = currentYear - g.getYear();
                    return ago >= 1 && ago <= 5;
                })
                .mapToDouble(g -> g.getMinScore() - batchLine)
                .filter(d -> d > 0)
                .toArray();

        if (lineDiffs.length < 2) return 0.5;

        double avg = 0;
        for (double d : lineDiffs) avg += d;
        avg /= lineDiffs.length;

        if (avg <= 0) return 0.5;

        double variance = 0;
        for (double d : lineDiffs) variance += Math.pow(d - avg, 2);
        variance /= lineDiffs.length;

        double cv = Math.sqrt(variance) / avg;
        return Math.min(cv / 0.35, 1.0);
    }

    private Double calcPlanRatio(List<AdmissionGroup> history, int currentYear) {
        // 当前年份的计划
        Integer currentPlan = history.stream()
                .filter(g -> g.getYear() == currentYear && g.getAdmissionCount() != null)
                .map(AdmissionGroup::getAdmissionCount)
                .findFirst().orElse(null);

        if (currentPlan == null || currentPlan <= 0) return null;

        // 近3年均值
        double avgHist = history.stream()
                .filter(g -> {
                    int ago = currentYear - g.getYear();
                    return ago >= 1 && ago <= 3 && g.getAdmissionCount() != null && g.getAdmissionCount() > 0;
                })
                .mapToInt(AdmissionGroup::getAdmissionCount)
                .average().orElse(0.0);

        if (avgHist <= 0) return null;
        return currentPlan / avgHist;
    }

    private double applyPlanModifier(double score, double planRatio) {
        if (planRatio <= 0) return score;
        if (planRatio < 0.7) return score * 0.75 + 0.25 * 0.30;
        if (planRatio < 0.9) return score * 0.85 + 0.15 * 0.40;
        if (planRatio > 1.3) return Math.min(score * 1.05 + 0.02, 0.99);
        return score;
    }

    private double calcQualityMod(int yearCount, double volatility, boolean isNewGaokao,
                                   List<AdmissionGroup> history, int currentYear,
                                   String province) {
        double mod = 1.0;

        // 年份数量
        if (yearCount == 1) mod *= 0.60;
        else if (yearCount == 2) mod *= 0.78;
        else if (yearCount == 3) mod *= 0.88;
        else if (yearCount == 4) mod *= 0.95;

        // 新旧高考数据比例
        long newCount = history.stream()
                .filter(g -> {
                    int ago = currentYear - g.getYear();
                    return ago >= 1 && ago <= 5;
                })
                .filter(g -> g.getYear() != null
                        && provinceReformService.isNewGaokao(province, g.getYear()))
                .count();
        double newRatio = yearCount > 0 ? (double) newCount / yearCount : 0;

        if (newRatio == 0) mod *= 0.75;
        else if (newRatio < 0.5) mod *= 0.85;
        else if (newRatio < 1.0) mod *= 0.92;

        // 高波动
        if (volatility > 0.7) mod *= 0.80;
        else if (volatility > 0.5) mod *= 0.90;

        return Math.min(Math.max(mod, 0.40), 1.0);
    }
}
