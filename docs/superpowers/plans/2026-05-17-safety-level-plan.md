# 安全系数计算实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为专业组和专业明细分页查询添加安全系数计算，返回 safetyLevel + levelShort + safetyDescription。

**Architecture:** 分层服务式架构。ConstraintWeightCalculator 处理约束权重（HARD/SOFT），ScoreBasedCalculator 处理线差/位次/密度等全方位计算。SafetyLevelService 整合两者，AdmissionQueryServiceImpl 调用并填充 VO。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL

---

## 文件结构

### 新建文件

| 路径 | 职责 |
|------|------|
| `haifeng-admin/src/main/resources/db/migration/V18__province_config.sql` | 新建 t_province_config 表 |
| `haifeng-common/.../entity/algorithm/ProvinceConfig.java` | 省份配置实体 |
| `haifeng-common/.../entity/algorithm/SafetyLevelDict.java` | 安全等级字典实体 |
| `haifeng-common/.../mapper/algorithm/ProvinceConfigMapper.java` | 省份配置 Mapper |
| `haifeng-common/.../mapper/algorithm/SafetyLevelDictMapper.java` | 等级字典 Mapper |
| `haifeng-common/.../service/algorithm/safety/dto/ConstraintWeightResult.java` | 约束权重结果 DTO |
| `haifeng-common/.../service/algorithm/safety/dto/SafetyCalcResult.java` | 计算结果 DTO |
| `haifeng-common/.../service/algorithm/safety/calculator/ConstraintWeightCalculator.java` | 约束权重计算器 |
| `haifeng-common/.../service/algorithm/safety/calculator/ScoreBasedCalculator.java` | 分数计算器 |
| `haifeng-common/.../service/algorithm/safety/SafetyLevelService.java` | 服务接口 |
| `haifeng-common/.../service/algorithm/safety/SafetyLevelServiceImpl.java` | 服务实现 |

### 修改文件

| 路径 | 修改内容 |
|------|----------|
| `haifeng-app/.../vo/algorithm/admission/AdmissionGroupPageVO.java` | 添加 levelShort, safetyDescription |
| `haifeng-app/.../vo/algorithm/admission/AdmissionMajorPageVO.java` | 添加 levelShort, safetyDescription |
| `haifeng-common/.../mapper/algorithm/ConstraintDictMapper.java` | 添加 selectSeverityByCodes 方法 |
| `haifeng-common/.../mapper/algorithm/ScoreRankMapper.java` | 添加 selectDensity 方法 |
| `haifeng-app/.../service/impl/algorithm/admission/AdmissionQueryServiceImpl.java` | 集成安全系数计算 |

---

### Task 1: 数据库迁移 - t_province_config

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V18__province_config.sql`

- [ ] **Step 1: 创建迁移文件**

```sql
-- V18__province_config.sql
-- 省份算法配置表

CREATE TABLE IF NOT EXISTS t_province_config (
    province        VARCHAR(20)     PRIMARY KEY,
    density_k       NUMERIC(4,3)    DEFAULT 0.150,
    line_steepness  NUMERIC(4,2)    DEFAULT 2.80,
    rank_steepness  NUMERIC(4,2)    DEFAULT 2.40,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

COMMENT ON TABLE t_province_config IS '省份算法配置表';
COMMENT ON COLUMN t_province_config.province IS '省份名称（主键）';
COMMENT ON COLUMN t_province_config.density_k IS '同分密度惩罚系数，默认0.15';
COMMENT ON COLUMN t_province_config.line_steepness IS '线差Sigmoid陡度，默认2.8';
COMMENT ON COLUMN t_province_config.rank_steepness IS '位次Sigmoid陡度，默认2.4';
COMMENT ON COLUMN t_province_config.created_at IS '创建时间';

-- 初始化所有省份，使用默认值
INSERT INTO t_province_config (province)
SELECT DISTINCT province FROM t_province_reform
ON CONFLICT (province) DO NOTHING;
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/V18__province_config.sql
git commit -m "feat(db): add t_province_config table for safety calculation

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 2: Entity - ProvinceConfig

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ProvinceConfig.java`

- [ ] **Step 1: 创建实体类**

```java
package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_province_config")
public class ProvinceConfig {

    @TableId(type = IdType.INPUT)
    private String province;

    private BigDecimal densityK;
    private BigDecimal lineSteepness;
    private BigDecimal rankSteepness;
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ProvinceConfig.java
git commit -m "feat(entity): add ProvinceConfig entity

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 3: Entity - SafetyLevelDict

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/SafetyLevelDict.java`

- [ ] **Step 1: 创建实体类**

```java
package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_safety_level_dict")
public class SafetyLevelDict {

    @TableId(type = IdType.INPUT)
    private Short level;

    private String code;
    private String name;
    private String nameShort;
    private BigDecimal minCoefficient;
    private BigDecimal maxCoefficient;
    private String color;
    private String confidence;
    private String confidenceReason;
    private String description;
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/SafetyLevelDict.java
git commit -m "feat(entity): add SafetyLevelDict entity

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 4: Mapper - ProvinceConfigMapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ProvinceConfigMapper.java`

- [ ] **Step 1: 创建 Mapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProvinceConfigMapper extends BaseMapper<ProvinceConfig> {

    @Select("SELECT * FROM t_province_config WHERE province = #{province}")
    ProvinceConfig selectByProvince(@Param("province") String province);
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ProvinceConfigMapper.java
git commit -m "feat(mapper): add ProvinceConfigMapper

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 5: Mapper - SafetyLevelDictMapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/SafetyLevelDictMapper.java`

- [ ] **Step 1: 创建 Mapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SafetyLevelDictMapper extends BaseMapper<SafetyLevelDict> {

    @Select("SELECT * FROM t_safety_level_dict WHERE #{coefficient} >= min_coefficient AND #{coefficient} < max_coefficient LIMIT 1")
    SafetyLevelDict selectByCoefficient(@Param("coefficient") BigDecimal coefficient);

    @Select("SELECT * FROM t_safety_level_dict ORDER BY level ASC")
    java.util.List<SafetyLevelDict> selectAll();
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/SafetyLevelDictMapper.java
git commit -m "feat(mapper): add SafetyLevelDictMapper

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 6: 修改 ConstraintDictMapper - 添加批量查询 severity

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java`

- [ ] **Step 1: 添加 selectSeverityByCodes 方法**

在文件末尾 `}` 之前添加：

```java
    @Select("<script>" +
            "SELECT code, severity FROM t_constraint_dict " +
            "WHERE code IN " +
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND is_active = true" +
            "</script>")
    List<ConstraintDict> selectSeverityByCodes(@Param("codes") List<String> codes);
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java
git commit -m "feat(mapper): add selectSeverityByCodes to ConstraintDictMapper

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 7: 修改 ScoreRankMapper - 添加查询同分密度

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ScoreRankMapper.java`

- [ ] **Step 1: 添加 selectDensity 方法**

在文件末尾 `}` 之前添加：

```java
    @Select("SELECT CASE WHEN cumulative_count > 0 THEN " +
            "CAST(same_score_count AS DECIMAL) / cumulative_count " +
            "ELSE NULL END " +
            "FROM t_score_rank " +
            "WHERE province = #{province} AND year = #{year} " +
            "AND subject_type = #{subjectType} AND score = #{score} " +
            "LIMIT 1")
    BigDecimal selectDensity(@Param("province") String province,
                             @Param("year") Short year,
                             @Param("subjectType") String subjectType,
                             @Param("score") Integer score);
```

需要在文件顶部添加 import：

```java
import java.math.BigDecimal;
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ScoreRankMapper.java
git commit -m "feat(mapper): add selectDensity to ScoreRankMapper

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 8: DTO - ConstraintWeightResult

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/dto/ConstraintWeightResult.java`

- [ ] **Step 1: 创建 DTO**

```java
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
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/dto/ConstraintWeightResult.java
git commit -m "feat(dto): add ConstraintWeightResult

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 9: DTO - SafetyCalcResult

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/dto/SafetyCalcResult.java`

- [ ] **Step 1: 创建 DTO**

```java
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
                .safetyLevel(new BigDecimal("0.50"))
                .levelShort("稳")
                .safetyDescription("历史数据不足，仅供参考")
                .build();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/dto/SafetyCalcResult.java
git commit -m "feat(dto): add SafetyCalcResult

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 10: Calculator - ConstraintWeightCalculator

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/calculator/ConstraintWeightCalculator.java`

- [ ] **Step 1: 创建计算器**

```java
package com.haifeng.common.service.algorithm.safety.calculator;

import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.service.algorithm.safety.dto.ConstraintWeightResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConstraintWeightCalculator {

    private static final BigDecimal WEIGHT_SOFT_GROUP = new BigDecimal("0.6");
    private static final BigDecimal WEIGHT_SOFT_BOTH = new BigDecimal("0.3");

    private final ConstraintDictMapper constraintDictMapper;

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

        // 步骤1：专业组约束检查
        List<String> groupIntersection = intersection(userConstraints, groupConstraints);
        boolean groupHasHard = false;
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

        BigDecimal groupWeight = groupHasSoft ? WEIGHT_SOFT_GROUP : BigDecimal.ONE;

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
                    // 专业组已有 SOFT → 0.3，否则 → 0.6
                    if (groupHasSoft) {
                        return ConstraintWeightResult.softWeight(WEIGHT_SOFT_BOTH);
                    } else {
                        return ConstraintWeightResult.softWeight(WEIGHT_SOFT_GROUP);
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
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/calculator/ConstraintWeightCalculator.java
git commit -m "feat(calculator): add ConstraintWeightCalculator

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 11: Calculator - ScoreBasedCalculator

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/calculator/ScoreBasedCalculator.java`

- [ ] **Step 1: 创建计算器**

```java
package com.haifeng.common.service.algorithm.safety.calculator;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class ScoreBasedCalculator {

    // 年份权重：最近1年权重1.0，依次递减
    private static final double[] YEAR_WEIGHTS = {1.0, 0.8, 0.6, 0.4, 0.2};

    // 默认参数
    private static final double DEFAULT_DENSITY_K = 0.15;
    private static final double DEFAULT_LINE_STEEPNESS = 2.8;
    private static final double DEFAULT_RANK_STEEPNESS = 2.4;

    // 新高考权重
    private static final double NEW_GAOKAO_LINE_WEIGHT = 0.42;
    private static final double NEW_GAOKAO_RANK_WEIGHT = 0.50;

    // 旧高考权重
    private static final double OLD_GAOKAO_LINE_WEIGHT = 0.62;
    private static final double OLD_GAOKAO_RANK_WEIGHT = 0.30;

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
        boolean isNewGaokao = isNewGaokao(gaokao.getSubjectType());
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
        double qualityMod = calcQualityMod(validYears, volatility, isNewGaokao, historyGroups, currentYear);
        result = pullToCenter(result, 1.0 - qualityMod);

        // Clamp
        return Math.min(Math.max(result, 0.01), 0.99);
    }

    private boolean isNewGaokao(String subjectType) {
        return "物理类".equals(subjectType) || "历史类".equals(subjectType)
                || "物理".equals(subjectType) || "历史".equals(subjectType);
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
                                   List<AdmissionGroup> history, int currentYear) {
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
                .filter(g -> isNewGaokao(g.getSubjects() != null && !g.getSubjects().isEmpty()
                        ? "物理类" : "理科")) // 简化判断
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
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/calculator/ScoreBasedCalculator.java
git commit -m "feat(calculator): add ScoreBasedCalculator

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 12: Service - SafetyLevelService 接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/SafetyLevelService.java`

- [ ] **Step 1: 创建接口**

```java
package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;

import java.math.BigDecimal;
import java.util.List;

public interface SafetyLevelService {

    /**
     * 计算专业明细的安全系数
     *
     * @param gaokao          用户档案
     * @param major           专业明细
     * @param group           所属专业组
     * @param historyGroups   历史专业组数据（近5年）
     * @param userConstraints 用户触发的约束 codes
     * @return 计算结果
     */
    SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                          AdmissionMajorScore major,
                                          AdmissionGroup group,
                                          List<AdmissionGroup> historyGroups,
                                          List<String> userConstraints);

    /**
     * 根据系数获取等级信息
     *
     * @param coefficient 安全系数
     * @return 等级字典
     */
    SafetyLevelDict getLevelByCoefficient(BigDecimal coefficient);
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/SafetyLevelService.java
git commit -m "feat(service): add SafetyLevelService interface

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 13: Service - SafetyLevelServiceImpl 实现

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/SafetyLevelServiceImpl.java`

- [ ] **Step 1: 创建实现类**

```java
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
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/safety/SafetyLevelServiceImpl.java
git commit -m "feat(service): add SafetyLevelServiceImpl

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 14: 修改 VO - 添加 levelShort 和 safetyDescription

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionGroupPageVO.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionMajorPageVO.java`

- [ ] **Step 1: 修改 AdmissionGroupPageVO**

在 `safetyLevel` 字段后添加：

```java
    /** 等级简称：搏/冲/稳/保/垫/禁 */
    private String levelShort;

    /** 说明（约束原因或数据不足提示） */
    private String safetyDescription;
```

- [ ] **Step 2: 修改 AdmissionMajorPageVO**

在 `safetyLevel` 字段后添加：

```java
    /** 等级简称：搏/冲/稳/保/垫/禁 */
    private String levelShort;

    /** 说明（约束原因或数据不足提示） */
    private String safetyDescription;
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionGroupPageVO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionMajorPageVO.java
git commit -m "feat(vo): add levelShort and safetyDescription to admission VOs

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 15: 集成 - 修改 AdmissionQueryServiceImpl

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 添加依赖注入**

在类的字段区域添加：

```java
    private final SafetyLevelService safetyLevelService;
    private final ConstraintMatcherService constraintMatcherService;
```

添加 import：

```java
import com.haifeng.common.service.algorithm.safety.SafetyLevelService;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
```

- [ ] **Step 2: 修改 pageGroups 方法**

在 `// 4. 判断会员类型` 之前添加：

```java
        // 获取用户约束
        List<String> userConstraints = constraintMatcherService.matchConstraints(gaokao);
```

- [ ] **Step 3: 修改 buildGroupVO 方法**

将方法签名改为：

```java
    private AdmissionGroupPageVO buildGroupVO(AdmissionGroup group,
                                               Map<String, List<AdmissionGroup>> historyMap,
                                               MemberGaokao gaokao,
                                               List<String> userConstraints) {
```

在方法内部，`return AdmissionGroupPageVO.builder()` 之前添加安全系数计算：

```java
        // 计算专业组安全系数 = max(所有专业明细的安全系数)
        BigDecimal maxSafetyLevel = BigDecimal.ZERO;
        String levelShort = "禁";
        String safetyDescription = "";

        // 查询该组下所有专业明细
        List<AdmissionMajorScore> majors = admissionMajorScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionMajorScore>()
                        .eq(AdmissionMajorScore::getGroupId, group.getId())
        );

        for (AdmissionMajorScore major : majors) {
            SafetyCalcResult result = safetyLevelService.calculateMajorSafety(
                    gaokao, major, group, history, userConstraints
            );
            if (result.getSafetyLevel().compareTo(maxSafetyLevel) > 0) {
                maxSafetyLevel = result.getSafetyLevel();
                levelShort = result.getLevelShort();
                safetyDescription = result.getSafetyDescription();
            }
        }

        // 如果没有专业明细，使用默认值
        if (majors.isEmpty()) {
            maxSafetyLevel = new BigDecimal("0.50");
            levelShort = "稳";
            safetyDescription = "暂无专业明细数据";
        }
```

然后在 builder 中添加字段：

```java
                .safetyLevel(maxSafetyLevel)
                .levelShort(levelShort)
                .safetyDescription(safetyDescription)
```

- [ ] **Step 4: 更新 pageGroups 中的 buildGroupVO 调用**

将：

```java
                voList.add(buildGroupVO(group, historyMap, gaokao));
```

改为：

```java
                voList.add(buildGroupVO(group, historyMap, gaokao, userConstraints));
```

- [ ] **Step 5: 修改 pageMajors 方法添加安全系数计算**

在 `// 4. 组装 VO` 之前添加：

```java
        // 获取用户档案和约束
        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        List<String> userConstraints = gaokao != null
                ? constraintMatcherService.matchConstraints(gaokao)
                : Collections.emptyList();

        // 查询历史专业组数据
        Short minYear = (short) (Year.now().getValue() - 4);
        List<GroupKey> keys = Collections.singletonList(
                new GroupKey(group.getUniversityId(), group.getGroupCode())
        );
        List<AdmissionGroup> historyGroups = admissionGroupMapper.selectHistoryByKeys(keys, minYear);
```

- [ ] **Step 6: 修改 buildMajorVO 方法**

将方法签名改为：

```java
    private AdmissionMajorPageVO buildMajorVO(AdmissionMajorScore major,
                                              Map<String, List<Map<String, Object>>> historyMap,
                                              MemberGaokao gaokao,
                                              AdmissionGroup group,
                                              List<AdmissionGroup> historyGroups,
                                              List<String> userConstraints) {
```

在方法内部添加安全系数计算：

```java
        // 计算安全系数
        SafetyCalcResult safetyResult;
        if (gaokao != null) {
            safetyResult = safetyLevelService.calculateMajorSafety(
                    gaokao, major, group, historyGroups, userConstraints
            );
        } else {
            safetyResult = SafetyCalcResult.noData();
        }
```

在 builder 中添加字段：

```java
                .safetyLevel(safetyResult.getSafetyLevel())
                .levelShort(safetyResult.getLevelShort())
                .safetyDescription(safetyResult.getSafetyDescription())
```

- [ ] **Step 7: 更新 pageMajors 中的 buildMajorVO 调用**

将：

```java
        List<AdmissionMajorPageVO> voList = majorPage.getRecords().stream()
                .map(major -> buildMajorVO(major, historyMap))
                .collect(Collectors.toList());
```

改为：

```java
        final MemberGaokao finalGaokao = gaokao;
        final AdmissionGroup finalGroup = group;
        final List<AdmissionGroup> finalHistoryGroups = historyGroups;
        final List<String> finalUserConstraints = userConstraints;

        List<AdmissionMajorPageVO> voList = majorPage.getRecords().stream()
                .map(major -> buildMajorVO(major, historyMap, finalGaokao, finalGroup,
                        finalHistoryGroups, finalUserConstraints))
                .collect(Collectors.toList());
```

- [ ] **Step 8: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(service): integrate safety level calculation into AdmissionQueryServiceImpl

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## 验证

1. 启动 app 服务
2. 使用已有用户登录，确保有高考档案
3. 调用 `GET /api/v1/app/admission/group/page?batch=本科批` 验证：
   - 每条专业组有 safetyLevel + levelShort + safetyDescription
   - 普通用户后10条仍然 masked，但可以看到安全系数
4. 调用 `GET /api/v1/app/admission/major/page?groupId=xxx` 验证专业明细安全系数
5. 测试约束场景：
   - 用户有色盲，专业组要求非色盲 → safetyLevel=0, levelShort='禁'
   - 软约束场景 → safetyLevel 降权
