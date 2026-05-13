# 约束匹配器设计文档

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

## 概述

**目标**：实现一个约束匹配服务，根据用户高考档案（t_member_gaokao）实时计算触发的约束列表（t_constraint_dict）。

**范围限定**：
- 只做用户档案到约束的映射
- 不做与专业组/专业明细的比较
- 不返回 severity/description
- 代码位于 admin 端 algorithm 模块

## 核心需求

| 项目 | 内容 |
|------|------|
| 输入 | memberId |
| 输出 | `List<String>` 约束 code 列表 |
| 运算符 | EQ、NE、GT、GE、LT、LE、IN、NOT_IN、IS_NULL、IS_NOT_NULL |
| 条件关系 | check_* + extra_* 是 AND 关系，最多2个条件 |
| 存储 | 不存储，实时计算 |
| 暴露方式 | 内部 Service 方法，不暴露 REST 接口 |

## 技术方案

采用 **策略模式**，为每种运算符创建独立的策略类。

### 包结构

```
haifeng-admin/src/main/java/com/haifeng/admin/
└── service/
    └── algorithm/
        └── matcher/
            ├── ConstraintMatcherService.java
            ├── ConstraintMatcherServiceImpl.java
            ├── FieldValueExtractor.java
            └── operator/
                ├── OperatorStrategy.java
                ├── OperatorStrategyFactory.java
                ├── EqStrategy.java
                ├── NeStrategy.java
                ├── GtStrategy.java
                ├── GeStrategy.java
                ├── LtStrategy.java
                ├── LeStrategy.java
                ├── InStrategy.java
                ├── NotInStrategy.java
                ├── IsNullStrategy.java
                └── IsNotNullStrategy.java
```

## 详细设计

### 1. OperatorStrategy（策略接口）

```java
public interface OperatorStrategy {
    /**
     * 判断字段值是否满足条件
     * @param fieldValue  用户档案中的字段值（可能为 null）
     * @param checkValue  约束字典中配置的值
     * @return true=条件满足（触发约束）
     */
    boolean evaluate(Object fieldValue, String checkValue);
}
```

### 2. OperatorStrategyFactory（策略工厂）

```java
@Component
public class OperatorStrategyFactory {
    private final Map<String, OperatorStrategy> strategies = new HashMap<>();

    @PostConstruct
    public void init() {
        strategies.put("EQ", new EqStrategy());
        strategies.put("NE", new NeStrategy());
        strategies.put("GT", new GtStrategy());
        strategies.put("GE", new GeStrategy());
        strategies.put("LT", new LtStrategy());
        strategies.put("LE", new LeStrategy());
        strategies.put("IN", new InStrategy());
        strategies.put("NOT_IN", new NotInStrategy());
        strategies.put("IS_NULL", new IsNullStrategy());
        strategies.put("IS_NOT_NULL", new IsNotNullStrategy());
    }

    public OperatorStrategy getStrategy(String operator) {
        return strategies.get(operator);
    }
}
```

### 3. ConstraintMatcherService（对外接口）

```java
public interface ConstraintMatcherService {
    /**
     * 根据用户档案匹配触发的约束列表
     * @param memberId 会员ID
     * @return 触发的约束 code 列表
     */
    List<String> matchConstraints(Long memberId);
}
```

### 4. ConstraintMatcherServiceImpl（核心实现）

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstraintMatcherServiceImpl implements ConstraintMatcherService {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final OperatorStrategyFactory strategyFactory;
    private final FieldValueExtractor fieldExtractor;

    @Override
    public List<String> matchConstraints(Long memberId) {
        // 1. 查询用户档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            return Collections.emptyList();
        }

        // 2. 查询所有启用的约束
        List<ConstraintDict> constraints = constraintDictMapper.selectActiveList();

        // 3. 遍历匹配
        List<String> triggeredCodes = new ArrayList<>();
        for (ConstraintDict constraint : constraints) {
            if (isTriggered(gaokao, constraint)) {
                triggeredCodes.add(constraint.getCode());
            }
        }

        return triggeredCodes;
    }

    private boolean isTriggered(MemberGaokao gaokao, ConstraintDict constraint) {
        // 主条件判断
        boolean mainResult = evaluateCondition(
            gaokao,
            constraint.getCheckField(),
            constraint.getCheckOperator(),
            constraint.getCheckValue()
        );

        // 无额外条件，直接返回主条件结果
        if (!StringUtils.hasText(constraint.getExtraField())) {
            return mainResult;
        }

        // 有额外条件，AND 关系
        boolean extraResult = evaluateCondition(
            gaokao,
            constraint.getExtraField(),
            constraint.getExtraOperator(),
            constraint.getExtraValue()
        );

        return mainResult && extraResult;
    }

    private boolean evaluateCondition(MemberGaokao gaokao,
                                       String field,
                                       String operator,
                                       String value) {
        Object fieldValue = fieldExtractor.extract(gaokao, field);

        OperatorStrategy strategy = strategyFactory.getStrategy(operator);
        if (strategy == null) {
            log.warn("未知运算符: {}", operator);
            return false;
        }

        return strategy.evaluate(fieldValue, value);
    }
}
```

### 5. FieldValueExtractor（字段值提取器）

```java
@Component
public class FieldValueExtractor {

    private static final Map<String, Function<MemberGaokao, Object>> FIELD_GETTERS = new HashMap<>();

    static {
        // 身体条件
        FIELD_GETTERS.put("is_color_blind", MemberGaokao::getIsColorBlind);
        FIELD_GETTERS.put("is_color_weak", MemberGaokao::getIsColorWeak);
        FIELD_GETTERS.put("vision_left", MemberGaokao::getVisionLeft);
        FIELD_GETTERS.put("vision_right", MemberGaokao::getVisionRight);
        FIELD_GETTERS.put("has_smell_disorder", MemberGaokao::getHasSmellDisorder);
        FIELD_GETTERS.put("height_cm", MemberGaokao::getHeightCm);
        FIELD_GETTERS.put("weight_kg", MemberGaokao::getWeightKg);
        FIELD_GETTERS.put("is_left_handed", MemberGaokao::getIsLeftHanded);
        FIELD_GETTERS.put("has_tattoo", MemberGaokao::getHasTattoo);
        FIELD_GETTERS.put("has_scar", MemberGaokao::getHasScar);
        FIELD_GETTERS.put("has_stutter", MemberGaokao::getHasStutter);
        // 身份条件
        FIELD_GETTERS.put("is_fresh_graduate", MemberGaokao::getIsFreshGraduate);
        FIELD_GETTERS.put("political_status", MemberGaokao::getPoliticalStatus);
        FIELD_GETTERS.put("household_type", MemberGaokao::getHouseholdType);
        FIELD_GETTERS.put("is_poverty_county", MemberGaokao::getIsPovertyCounty);
        // 外语
        FIELD_GETTERS.put("foreign_language", MemberGaokao::getForeignLanguage);
        // 选科
        FIELD_GETTERS.put("subject_type", MemberGaokao::getSubjectType);
        FIELD_GETTERS.put("second_subject_type", MemberGaokao::getSecondSubjectType);
        FIELD_GETTERS.put("third_subject_type", MemberGaokao::getThirdSubjectType);
    }

    public Object extract(MemberGaokao gaokao, String fieldName) {
        Function<MemberGaokao, Object> getter = FIELD_GETTERS.get(fieldName);
        if (getter == null) {
            return null;
        }
        return getter.apply(gaokao);
    }
}
```

### 6. 策略实现

#### EqStrategy
```java
public class EqStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return false;
        return fieldValue.toString().equals(checkValue);
    }
}
```

#### NeStrategy
```java
public class NeStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return true;
        return !fieldValue.toString().equals(checkValue);
    }
}
```

#### GtStrategy
```java
public class GtStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return false;
        BigDecimal field = new BigDecimal(fieldValue.toString());
        BigDecimal check = new BigDecimal(checkValue);
        return field.compareTo(check) > 0;
    }
}
```

#### GeStrategy
```java
public class GeStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return false;
        BigDecimal field = new BigDecimal(fieldValue.toString());
        BigDecimal check = new BigDecimal(checkValue);
        return field.compareTo(check) >= 0;
    }
}
```

#### LtStrategy
```java
public class LtStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return false;
        BigDecimal field = new BigDecimal(fieldValue.toString());
        BigDecimal check = new BigDecimal(checkValue);
        return field.compareTo(check) < 0;
    }
}
```

#### LeStrategy
```java
public class LeStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return false;
        BigDecimal field = new BigDecimal(fieldValue.toString());
        BigDecimal check = new BigDecimal(checkValue);
        return field.compareTo(check) <= 0;
    }
}
```

#### InStrategy
```java
public class InStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return false;
        Set<String> values = Set.of(checkValue.split(","));
        return values.contains(fieldValue.toString());
    }
}
```

#### NotInStrategy
```java
public class NotInStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) return true;
        Set<String> values = Set.of(checkValue.split(","));
        return !values.contains(fieldValue.toString());
    }
}
```

#### IsNullStrategy
```java
public class IsNullStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return fieldValue == null;
    }
}
```

#### IsNotNullStrategy
```java
public class IsNotNullStrategy implements OperatorStrategy {
    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return fieldValue != null;
    }
}
```

## Mapper 补充

### MemberGaokaoMapper

```java
MemberGaokao selectByMemberId(@Param("memberId") Long memberId);
```

### ConstraintDictMapper

```java
List<ConstraintDict> selectActiveList();
```

SQL:
```sql
SELECT * FROM t_constraint_dict WHERE is_active = true ORDER BY sort_order ASC
```

## 文件清单

| 文件 | 说明 |
|------|------|
| `matcher/ConstraintMatcherService.java` | 对外服务接口 |
| `matcher/ConstraintMatcherServiceImpl.java` | 核心实现 |
| `matcher/FieldValueExtractor.java` | 字段值提取器 |
| `matcher/operator/OperatorStrategy.java` | 策略接口 |
| `matcher/operator/OperatorStrategyFactory.java` | 策略工厂 |
| `matcher/operator/EqStrategy.java` | 等于策略 |
| `matcher/operator/NeStrategy.java` | 不等于策略 |
| `matcher/operator/GtStrategy.java` | 大于策略 |
| `matcher/operator/GeStrategy.java` | 大于等于策略 |
| `matcher/operator/LtStrategy.java` | 小于策略 |
| `matcher/operator/LeStrategy.java` | 小于等于策略 |
| `matcher/operator/InStrategy.java` | 在列表中策略 |
| `matcher/operator/NotInStrategy.java` | 不在列表中策略 |
| `matcher/operator/IsNullStrategy.java` | 为空策略 |
| `matcher/operator/IsNotNullStrategy.java` | 不为空策略 |

## 使用示例

```java
@Autowired
private ConstraintMatcherService constraintMatcherService;

// 在安全系数模块中调用
List<String> triggeredCodes = constraintMatcherService.matchConstraints(memberId);
// 返回: ["COLOR_BLIND", "HEIGHT_MALE_170", ...]
```

## 后续扩展

本模块只负责约束匹配，后续模块将实现：
1. 安全系数计算
2. 专业组/专业明细的约束比对
3. HARD/SOFT 约束的不同展示逻辑
