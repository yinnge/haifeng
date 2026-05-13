# 约束匹配器实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现约束匹配服务，根据用户高考档案实时计算触发的约束 code 列表。

**Architecture:** 采用策略模式，为10种运算符各创建独立策略类。ConstraintMatcherService 作为入口，遍历所有启用约束，通过 FieldValueExtractor 提取字段值，通过 OperatorStrategyFactory 获取策略执行判断。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Java 17

---

## 文件结构

```
haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/
├── ConstraintMatcherService.java          (接口)
├── ConstraintMatcherServiceImpl.java      (实现)
├── FieldValueExtractor.java               (字段提取器)
└── operator/
    ├── OperatorStrategy.java              (策略接口)
    ├── OperatorStrategyFactory.java       (策略工厂)
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

haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/
├── MemberGaokaoMapper.java                (添加方法)
└── ConstraintDictMapper.java              (添加方法)
```

---

### Task 1: 补充 Mapper 方法

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java`

- [ ] **Step 1: 添加 MemberGaokaoMapper.selectByMemberId**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户高考档案 Mapper
 */
@Mapper
public interface MemberGaokaoMapper extends BaseMapper<MemberGaokao> {

    @Select("SELECT * FROM t_member_gaokao WHERE member_id = #{memberId}")
    MemberGaokao selectByMemberId(@Param("memberId") Long memberId);
}
```

- [ ] **Step 2: 添加 ConstraintDictMapper.selectActiveList**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConstraintDictMapper extends BaseMapper<ConstraintDict> {
    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name}")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name} AND code != #{excludeCode}")
    int countByNameExclude(@Param("name") String name, @Param("excludeCode") String excludeCode);

    @Select("SELECT code FROM t_constraint_dict WHERE name = #{name}")
    String selectCodeByName(@Param("name") String name);

    @Select("SELECT * FROM t_constraint_dict WHERE is_active = true ORDER BY sort_order ASC")
    List<ConstraintDict> selectActiveList();
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java
git commit -m "feat(mapper): add selectByMemberId and selectActiveList for constraint matcher"
```

---

### Task 2: 创建策略接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/OperatorStrategy.java`

- [ ] **Step 1: 创建 OperatorStrategy 接口**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

/**
 * 运算符策略接口
 */
public interface OperatorStrategy {

    /**
     * 判断字段值是否满足条件
     *
     * @param fieldValue 用户档案中的字段值（可能为 null）
     * @param checkValue 约束字典中配置的值
     * @return true=条件满足（触发约束）
     */
    boolean evaluate(Object fieldValue, String checkValue);
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/OperatorStrategy.java
git commit -m "feat(matcher): add OperatorStrategy interface"
```

---

### Task 3: 创建10个策略实现类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/EqStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/NeStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/GtStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/GeStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/LtStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/LeStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/InStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/NotInStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/IsNullStrategy.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/IsNotNullStrategy.java`

- [ ] **Step 1: 创建 EqStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

/**
 * 等于策略 (EQ)
 */
public class EqStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        return fieldValue.toString().equals(checkValue);
    }
}
```

- [ ] **Step 2: 创建 NeStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

/**
 * 不等于策略 (NE)
 */
public class NeStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return true;
        }
        return !fieldValue.toString().equals(checkValue);
    }
}
```

- [ ] **Step 3: 创建 GtStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import java.math.BigDecimal;

/**
 * 大于策略 (GT)
 */
public class GtStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        try {
            BigDecimal field = new BigDecimal(fieldValue.toString());
            BigDecimal check = new BigDecimal(checkValue);
            return field.compareTo(check) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

- [ ] **Step 4: 创建 GeStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import java.math.BigDecimal;

/**
 * 大于等于策略 (GE)
 */
public class GeStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        try {
            BigDecimal field = new BigDecimal(fieldValue.toString());
            BigDecimal check = new BigDecimal(checkValue);
            return field.compareTo(check) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

- [ ] **Step 5: 创建 LtStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import java.math.BigDecimal;

/**
 * 小于策略 (LT)
 */
public class LtStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        try {
            BigDecimal field = new BigDecimal(fieldValue.toString());
            BigDecimal check = new BigDecimal(checkValue);
            return field.compareTo(check) < 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

- [ ] **Step 6: 创建 LeStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import java.math.BigDecimal;

/**
 * 小于等于策略 (LE)
 */
public class LeStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        try {
            BigDecimal field = new BigDecimal(fieldValue.toString());
            BigDecimal check = new BigDecimal(checkValue);
            return field.compareTo(check) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

- [ ] **Step 7: 创建 InStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在列表中策略 (IN)
 */
public class InStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null || checkValue == null) {
            return false;
        }
        Set<String> values = Arrays.stream(checkValue.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return values.contains(fieldValue.toString());
    }
}
```

- [ ] **Step 8: 创建 NotInStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 不在列表中策略 (NOT_IN)
 */
public class NotInStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return true;
        }
        if (checkValue == null) {
            return true;
        }
        Set<String> values = Arrays.stream(checkValue.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return !values.contains(fieldValue.toString());
    }
}
```

- [ ] **Step 9: 创建 IsNullStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

/**
 * 为空策略 (IS_NULL)
 */
public class IsNullStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return fieldValue == null;
    }
}
```

- [ ] **Step 10: 创建 IsNotNullStrategy**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

/**
 * 不为空策略 (IS_NOT_NULL)
 */
public class IsNotNullStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return fieldValue != null;
    }
}
```

- [ ] **Step 11: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/
git commit -m "feat(matcher): add 10 operator strategy implementations"
```

---

### Task 4: 创建策略工厂

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/OperatorStrategyFactory.java`

- [ ] **Step 1: 创建 OperatorStrategyFactory**

```java
package com.haifeng.admin.service.algorithm.matcher.operator;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 运算符策略工厂
 */
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

    /**
     * 根据运算符获取策略
     *
     * @param operator 运算符名称
     * @return 策略实例，不存在返回 null
     */
    public OperatorStrategy getStrategy(String operator) {
        return strategies.get(operator);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/OperatorStrategyFactory.java
git commit -m "feat(matcher): add OperatorStrategyFactory"
```

---

### Task 5: 创建字段值提取器

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/FieldValueExtractor.java`

- [ ] **Step 1: 创建 FieldValueExtractor**

```java
package com.haifeng.admin.service.algorithm.matcher;

import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 字段值提取器
 * 从 MemberGaokao 中根据字段名提取对应的值
 */
@Component
public class FieldValueExtractor {

    private static final Map<String, Function<MemberGaokao, Object>> FIELD_GETTERS = new HashMap<>();

    static {
        // 身体视觉条件
        FIELD_GETTERS.put("is_color_blind", MemberGaokao::getIsColorBlind);
        FIELD_GETTERS.put("is_color_weak", MemberGaokao::getIsColorWeak);
        FIELD_GETTERS.put("vision_left", MemberGaokao::getVisionLeft);
        FIELD_GETTERS.put("vision_right", MemberGaokao::getVisionRight);
        FIELD_GETTERS.put("has_smell_disorder", MemberGaokao::getHasSmellDisorder);

        // 身体指标
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

    /**
     * 从档案中提取字段值
     *
     * @param gaokao    用户高考档案
     * @param fieldName 字段名（下划线命名）
     * @return 字段值，字段不存在返回 null
     */
    public Object extract(MemberGaokao gaokao, String fieldName) {
        if (gaokao == null || fieldName == null) {
            return null;
        }
        Function<MemberGaokao, Object> getter = FIELD_GETTERS.get(fieldName);
        if (getter == null) {
            return null;
        }
        return getter.apply(gaokao);
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/FieldValueExtractor.java
git commit -m "feat(matcher): add FieldValueExtractor"
```

---

### Task 6: 创建约束匹配服务

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/ConstraintMatcherService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/ConstraintMatcherServiceImpl.java`

- [ ] **Step 1: 创建 ConstraintMatcherService 接口**

```java
package com.haifeng.admin.service.algorithm.matcher;

import java.util.List;

/**
 * 约束匹配服务
 */
public interface ConstraintMatcherService {

    /**
     * 根据用户档案匹配触发的约束列表
     *
     * @param memberId 会员ID
     * @return 触发的约束 code 列表
     */
    List<String> matchConstraints(Long memberId);
}
```

- [ ] **Step 2: 创建 ConstraintMatcherServiceImpl 实现**

```java
package com.haifeng.admin.service.algorithm.matcher;

import com.haifeng.admin.service.algorithm.matcher.operator.OperatorStrategy;
import com.haifeng.admin.service.algorithm.matcher.operator.OperatorStrategyFactory;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 约束匹配服务实现
 */
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
        if (memberId == null) {
            return Collections.emptyList();
        }

        // 1. 查询用户档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            log.debug("用户档案不存在，memberId={}", memberId);
            return Collections.emptyList();
        }

        // 2. 查询所有启用的约束
        List<ConstraintDict> constraints = constraintDictMapper.selectActiveList();
        if (constraints == null || constraints.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 遍历匹配
        List<String> triggeredCodes = new ArrayList<>();
        for (ConstraintDict constraint : constraints) {
            if (isTriggered(gaokao, constraint)) {
                triggeredCodes.add(constraint.getCode());
            }
        }

        log.debug("约束匹配完成，memberId={}，触发约束数={}", memberId, triggeredCodes.size());
        return triggeredCodes;
    }

    /**
     * 判断约束是否被触发
     */
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

    /**
     * 执行单个条件判断
     */
    private boolean evaluateCondition(MemberGaokao gaokao,
                                       String field,
                                       String operator,
                                       String value) {
        if (!StringUtils.hasText(field) || !StringUtils.hasText(operator)) {
            return false;
        }

        // 获取字段值
        Object fieldValue = fieldExtractor.extract(gaokao, field);

        // 获取策略并执行
        OperatorStrategy strategy = strategyFactory.getStrategy(operator);
        if (strategy == null) {
            log.warn("未知运算符: {}", operator);
            return false;
        }

        return strategy.evaluate(fieldValue, value);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/ConstraintMatcherService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/ConstraintMatcherServiceImpl.java
git commit -m "feat(matcher): add ConstraintMatcherService with full implementation"
```

---

## 验证步骤

实现完成后，可通过以下方式验证：

1. **启动应用**：确保无编译错误
2. **断点调试**：在 `ConstraintMatcherServiceImpl.matchConstraints` 方法打断点
3. **准备测试数据**：
   - 在 `t_constraint_dict` 添加测试约束，如：
     ```sql
     INSERT INTO t_constraint_dict (code, name, category, severity, check_field, check_operator, check_value, is_active)
     VALUES ('COLOR_BLIND', '色盲限制', '视觉', 'HARD', 'is_color_blind', 'EQ', 'true', true);
     ```
   - 在 `t_member_gaokao` 添加测试用户档案
4. **调用服务**：通过测试代码或其他服务调用 `constraintMatcherService.matchConstraints(memberId)`

---

## 文件清单

| 序号 | 文件路径 | 操作 |
|------|----------|------|
| 1 | `haifeng-common/.../mapper/algorithm/MemberGaokaoMapper.java` | 修改 |
| 2 | `haifeng-common/.../mapper/algorithm/ConstraintDictMapper.java` | 修改 |
| 3 | `haifeng-admin/.../matcher/operator/OperatorStrategy.java` | 新建 |
| 4 | `haifeng-admin/.../matcher/operator/EqStrategy.java` | 新建 |
| 5 | `haifeng-admin/.../matcher/operator/NeStrategy.java` | 新建 |
| 6 | `haifeng-admin/.../matcher/operator/GtStrategy.java` | 新建 |
| 7 | `haifeng-admin/.../matcher/operator/GeStrategy.java` | 新建 |
| 8 | `haifeng-admin/.../matcher/operator/LtStrategy.java` | 新建 |
| 9 | `haifeng-admin/.../matcher/operator/LeStrategy.java` | 新建 |
| 10 | `haifeng-admin/.../matcher/operator/InStrategy.java` | 新建 |
| 11 | `haifeng-admin/.../matcher/operator/NotInStrategy.java` | 新建 |
| 12 | `haifeng-admin/.../matcher/operator/IsNullStrategy.java` | 新建 |
| 13 | `haifeng-admin/.../matcher/operator/IsNotNullStrategy.java` | 新建 |
| 14 | `haifeng-admin/.../matcher/operator/OperatorStrategyFactory.java` | 新建 |
| 15 | `haifeng-admin/.../matcher/FieldValueExtractor.java` | 新建 |
| 16 | `haifeng-admin/.../matcher/ConstraintMatcherService.java` | 新建 |
| 17 | `haifeng-admin/.../matcher/ConstraintMatcherServiceImpl.java` | 新建 |
