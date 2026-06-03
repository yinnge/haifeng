# 安全系数计算设计文档

> 日期：2026-05-17
> 模块：haifeng-common / haifeng-app / algorithm / safety
> 状态：待实施

## 1. 概述

### 1.1 功能目标

为专业组和专业明细分页查询添加安全系数计算，帮助用户评估录取概率。

### 1.2 核心需求

1. **安全系数范围**：0.00 ~ 1.00，保留两位小数
2. **等级映射**：根据系数映射到「搏/冲/稳/保/垫/禁」
3. **专业组系数**：= max(所有专业明细的安全系数)
4. **两阶段计算**：
   - 任务一：约束权重计算（HARD→0，SOFT→0.6/0.3）
   - 任务二：全方位计算（线差、位次、密度、波动、计划等）

## 2. 数据结构

### 2.1 VO 字段扩展

**AdmissionGroupPageVO** 和 **AdmissionMajorPageVO** 新增：

```java
private BigDecimal safetyLevel;      // 安全系数 0.00~1.00（已添加）
private String levelShort;           // 等级简称：搏/冲/稳/保/垫/禁
private String safetyDescription;    // 说明（约束原因或数据不足提示）
```

### 2.2 新建表 t_province_config

```sql
CREATE TABLE t_province_config (
    province        VARCHAR(20)     PRIMARY KEY,
    density_k       NUMERIC(4,3)    DEFAULT 0.150,
    line_steepness  NUMERIC(4,2)    DEFAULT 2.80,
    rank_steepness  NUMERIC(4,2)    DEFAULT 2.40,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

COMMENT ON TABLE t_province_config IS '省份算法配置表';
COMMENT ON COLUMN t_province_config.density_k IS '同分密度惩罚系数';
COMMENT ON COLUMN t_province_config.line_steepness IS '线差 Sigmoid 陡度';
COMMENT ON COLUMN t_province_config.rank_steepness IS '位次 Sigmoid 陡度';
```

### 2.3 已有表 t_safety_level_dict

| level | code | name_short | 系数范围 | color |
|-------|------|------------|----------|-------|
| 1 | REACH_HIGH | 搏 | 0.00~0.30 | #FF4D4F |
| 2 | REACH | 冲 | 0.30~0.50 | #FFA940 |
| 3 | MATCH | 稳 | 0.50~0.70 | #FADB14 |
| 4 | SAFE | 保 | 0.70~0.85 | #52C41A |
| 5 | FLOOR | 垫 | 0.85~1.00 | #1890FF |

特殊：safetyLevel=0 时，levelShort='禁'

## 3. 服务层架构

### 3.1 包结构

```
haifeng-common/src/main/java/com/haifeng/common/
├── entity/algorithm/
│   ├── ProvinceConfig.java
│   └── SafetyLevelDict.java
├── mapper/algorithm/
│   ├── ProvinceConfigMapper.java
│   └── SafetyLevelDictMapper.java
└── service/algorithm/safety/
    ├── SafetyLevelService.java
    ├── SafetyLevelServiceImpl.java
    ├── calculator/
    │   ├── ConstraintWeightCalculator.java
    │   └── ScoreBasedCalculator.java
    └── dto/
        ├── SafetyCalcContext.java
        ├── SafetyCalcResult.java
        └── ConstraintWeightResult.java
```

### 3.2 核心接口

```java
public interface SafetyLevelService {

    /**
     * 计算专业明细的安全系数
     */
    SafetyCalcResult calculateMajorSafety(
        MemberGaokao gaokao,
        AdmissionMajorScore major,
        AdmissionGroup group,
        List<AdmissionGroup> historyGroups,
        List<String> userConstraints
    );

    /**
     * 根据系数获取等级信息
     */
    SafetyLevelDict getLevelByCoefficient(BigDecimal coefficient);
}
```

## 4. 约束权重计算规则

### 4.1 ConstraintWeightCalculator

```java
public ConstraintWeightResult calculate(
    List<String> userConstraints,
    List<String> groupConstraints,
    List<String> majorConstraints
);
```

### 4.2 计算流程

```
步骤1：专业组约束检查
    ├── 交集 = userConstraints ∩ groupConstraints
    ├── 查询交集中每个约束的 severity
    │
    ├── 有 HARD → 返回 { weight=0, blocked=true, reason="专业组限制：xxx" }
    └── 有 SOFT → groupWeight = 0.6

步骤2：专业明细约束检查（仅当专业组不是 HARD）
    ├── 交集 = userConstraints ∩ majorConstraints
    ├── 查询交集中每个约束的 severity
    │
    ├── 有 HARD → 返回 { weight=0, blocked=true, reason="专业限制：xxx" }
    ├── 有 SOFT 且 groupWeight=0.6 → 返回 { weight=0.3 }
    └── 有 SOFT 且 groupWeight=1.0 → 返回 { weight=0.6 }

步骤3：无约束冲突
    └── 返回 { weight=1.0, blocked=false }
```

### 4.3 返回结构

```java
@Data
@Builder
public class ConstraintWeightResult {
    private BigDecimal weight;      // 0.0 / 0.3 / 0.6 / 1.0
    private boolean blocked;        // true = 不可报考
    private String reason;          // 原因说明
}
```

## 5. 全方位计算规则

### 5.1 ScoreBasedCalculator

#### 5.1.1 计算维度

| 维度 | 数据来源 | 计算方式 |
|------|----------|----------|
| 线差比 | t_member_gaokao.score_above_line vs 历史线差 | 用户线差 / 历史加权平均线差 |
| 位次比 | t_member_gaokao.rank vs 历史位次 | 历史加权平均位次 / 用户位次 |
| 波动率 | 历史5年线差数据 | 变异系数 CV = 标准差 / 均值 |
| 计划比 | 今年 admission_count vs 近3年均值 | 今年计划 / 历史均值 |
| 同分密度 | t_score_rank | same_score_count / cumulative_count |

#### 5.1.2 年份权重

```java
private static final double[] YEAR_WEIGHTS = {1.0, 0.8, 0.6, 0.4, 0.2};
// 最近1年权重1.0，依次递减
```


#### 5.1.3 基础分计算

```java
// 非对称 Sigmoid
double asymmetricSigmoid(double ratio, double steepness) {
    if (ratio <= 0) return 0.01;
    double adjusted = ratio < 1.0 ? Math.pow(ratio, 2.2) : ratio;
    return 1.0 / (1.0 + Math.exp(-steepness * (adjusted - 1.0)));
}

// 基础分 = 线差分 * 线差权重 + 位次分 * 位次权重
// 新高考（物理类/历史类）：线差权重=0.42, 位次权重=0.50
// 旧高考（理科/文科）：线差权重=0.62, 位次权重=0.30
```

#### 5.1.4 风险修正

```java
// 1. 波动风险：高波动 → 拉向中性值 0.5
private double pullToCenter(double score, double strength) {
    strength = Math.min(strength, 1.0);
    return score * (1 - strength) + 0.5 * strength;
}
result = pullToCenter(result, volatility * 0.5);

// 2. 招生计划变化
if (planRatio < 0.7)  result = result * 0.75 + 0.25 * 0.30;  // 大幅缩招
if (planRatio < 0.9)  result = result * 0.85 + 0.15 * 0.40;  // 小幅缩招
if (planRatio > 1.3)  result = Math.min(result * 1.05 + 0.02, 0.99); // 扩招

// 3. 同分密度惩罚
double modifier = 1.0 - ((density - 0.5) * densityK);  // densityK 默认 0.15
result = result * Math.max(modifier, 0.70);

// 4. 数据质量修正
result = pullToCenter(result, 1.0 - qualityMod);
```

#### 5.1.5 数据质量修正 (qualityMod)

```java
double mod = 1.0;

// 年份数量
if      (yearCount == 1) mod *= 0.60;
else if (yearCount == 2) mod *= 0.78;
else if (yearCount == 3) mod *= 0.88;
else if (yearCount == 4) mod *= 0.95;

// 新旧高考数据比例
if      (newRatio == 0)   mod *= 0.75;  // 全是旧高考数据
else if (newRatio < 0.5)  mod *= 0.85;
else if (newRatio < 1.0)  mod *= 0.92;

// 高波动
if      (volatility > 0.7) mod *= 0.80;
else if (volatility > 0.5) mod *= 0.90;

return Math.min(Math.max(mod, 0.40), 1.0);
```

### 5.2 最终公式

```
最终安全系数 = clamp(基础分 × 约束权重, 0.01, 0.99)

特殊情况：
- 约束权重 = 0 → 安全系数 = 0.00，levelShort = '禁'
- 无历史数据 → 安全系数 = 0.50，description = "历史数据不足，仅供参考"
```

## 6. 调用流程

```
用户请求 GET /api/v1/app/admission/group/page?batch=本科批
    │
    ├── 1. 获取用户档案 (MemberGaokao)
    ├── 2. 调用 ConstraintMatcherService.matchConstraints() 获取用户约束
    ├── 3. 分页查询专业组 + 历史数据
    │
    └── 4. 对每个专业组：
            │
            ├── 4.1 查询该组下所有专业明细
            │
            ├── 4.2 对每个专业明细：
            │       ├── ConstraintWeightCalculator.calculate() → 约束权重
            │       ├── 若 blocked=true → safetyLevel=0, 跳过后续计算
            │       ├── ScoreBasedCalculator.calculate() → 基础分
            │       └── 最终系数 = 基础分 × 约束权重
            │
            ├── 4.3 专业组安全系数 = max(专业明细安全系数)
            │
            └── 4.4 SafetyLevelService.getLevelByCoefficient() → levelShort + description
```

## 7. 文件清单

### 7.1 新建文件

| 层级 | 文件 | 说明 |
|------|------|------|
| DB | V18__province_config.sql | 新建 t_province_config 表 |
| Entity | ProvinceConfig.java | 省份配置实体 |
| Entity | SafetyLevelDict.java | 安全等级字典实体 |
| Mapper | ProvinceConfigMapper.java | 省份配置 Mapper |
| Mapper | SafetyLevelDictMapper.java | 等级字典 Mapper |
| DTO | SafetyCalcContext.java | 计算上下文 |
| DTO | SafetyCalcResult.java | 计算结果 |
| DTO | ConstraintWeightResult.java | 约束权重结果 |
| Service | SafetyLevelService.java | 接口 |
| Service | SafetyLevelServiceImpl.java | 实现 |
| Calculator | ConstraintWeightCalculator.java | 约束权重计算器 |
| Calculator | ScoreBasedCalculator.java | 分数计算器 |

### 7.2 修改文件

| 文件 | 修改内容 |
|------|----------|
| AdmissionGroupPageVO.java | 添加 levelShort, safetyDescription |
| AdmissionMajorPageVO.java | 添加 levelShort, safetyDescription |
| AdmissionQueryServiceImpl.java | 集成安全系数计算 |
| ConstraintDictMapper.java | 新增批量查询 severity 方法 |
| ScoreRankMapper.java | 新增查询同分密度方法 |
| BatchScoreLineMapper.java | 新增查询省控线方法 |

## 8. 验证方式

1. 启动 app 服务
2. 使用已有用户登录，确保有高考档案
3. 调用 `GET /api/v1/app/admission/group/page?batch=本科批` 验证：
   - 每条专业组有 safetyLevel + levelShort + safetyDescription
   - 普通用户后10条仍然 masked，但可以看到安全系数
4. 调用 `GET /api/v1/app/admission/major/page?groupId=xxx` 验证专业明细安全系数
5. 测试约束场景：
   - 用户有色盲，专业组要求非色盲 → safetyLevel=0, levelShort='禁'
   - 软约束场景 → safetyLevel 降权
