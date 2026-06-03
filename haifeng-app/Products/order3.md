# 约束匹配器模块 - API 文档

## 功能概述

约束匹配器模块是高考志愿填报系统的核心算法组件，负责根据用户高考档案动态计算用户触发的约束条件列表。该模块采用**策略模式**设计，支持10种运算符，可灵活扩展。

### 核心功能

1. **约束匹配**：根据用户高考档案（t_member_gaokao）与约束字典（t_constraint_dict）进行动态匹配
2. **多条件支持**：支持主条件 + 额外条件的 AND 组合判断
3. **策略模式**：10种运算符各自独立实现，便于扩展维护
4. **字段提取**：通过字段提取器从用户档案中动态获取字段值

### 业务规则

- 只匹配 `is_active = true` 的约束
- 主条件必须满足才会检查额外条件
- 额外条件与主条件是 AND 关系
- 字段值为 null 时，大部分运算符返回 false（IS_NULL 除外）
- 约束按 `sort_order` 排序后依次匹配

### 应用场景

1. **志愿推荐前置过滤**：根据用户身体条件、身份条件等过滤不可报考的专业
2. **专业组校验**：校验用户是否满足专业组的约束要求
3. **风险提示**：返回 SOFT 类型约束用于前端风险提示

---

## 枚举说明

### OperatorEnum - 运算符类型

| 运算符 | 数据库值 | 说明 | 示例 |
|--------|----------|------|------|
| EQ | EQ | 等于 | `is_color_blind EQ true` |
| NE | NE | 不等于 | `foreign_language NE 英语` |
| GT | GT | 大于 | `height_cm GT 170` |
| GE | GE | 大于等于 | `vision_left GE 4.8` |
| LT | LT | 小于 | `weight_kg LT 50` |
| LE | LE | 小于等于 | `height_cm LE 185` |
| IN | IN | 在列表中 | `political_status IN 中共党员,共青团员` |
| NOT_IN | NOT_IN | 不在列表中 | `household_type NOT_IN 农村` |
| IS_NULL | IS_NULL | 为空 | `vision_left IS_NULL` |
| IS_NOT_NULL | IS_NOT_NULL | 不为空 | `has_tattoo IS_NOT_NULL` |

### SeverityEnum - 约束严重程度

| 枚举值 | 数据库值 | 说明 |
|--------|----------|------|
| HARD | HARD | 硬限制，不可报考 |
| SOFT | SOFT | 软提示，建议谨慎报考 |

### CategoryEnum - 约束类别

| 枚举值 | 数据库值 | 说明 |
|--------|----------|------|
| 视觉 | 视觉 | 色盲、色弱、视力等 |
| 身体指标 | 身体指标 | 身高、体重、左撇子、纹身等 |
| 身份条件 | 身份条件 | 应届生、政治面貌、户籍等 |
| 外语 | 外语 | 外语语种限制 |
| 选科 | 选科 | 选科要求限制 |

---

## 支持的检查字段

### 字段映射表（check_field / extra_field 可用值）

| 字段名（下划线） | 对应实体字段 | 类型 | 说明 |
|------------------|--------------|------|------|
| **视觉条件** |
| is_color_blind | isColorBlind | Boolean | 是否色盲 |
| is_color_weak | isColorWeak | Boolean | 是否色弱 |
| vision_left | visionLeft | BigDecimal | 左眼视力（0.0-5.5） |
| vision_right | visionRight | BigDecimal | 右眼视力（0.0-5.5） |
| has_smell_disorder | hasSmellDisorder | Boolean | 是否嗅觉障碍 |
| **身体指标** |
| height_cm | heightCm | Integer | 身高（厘米） |
| weight_kg | weightKg | BigDecimal | 体重（公斤） |
| is_left_handed | isLeftHanded | Boolean | 是否左撇子 |
| has_tattoo | hasTattoo | Boolean | 是否有纹身 |
| has_scar | hasScar | Boolean | 是否有面部疤痕 |
| has_stutter | hasStutter | Boolean | 是否口吃 |
| **身份条件** |
| is_fresh_graduate | isFreshGraduate | Boolean | 是否应届生 |
| political_status | politicalStatus | String | 政治面貌 |
| household_type | householdType | String | 户籍类型 |
| is_poverty_county | isPovertyCounty | Boolean | 是否贫困县户籍 |
| **外语** |
| foreign_language | foreignLanguage | String | 外语语种 |
| **选科** |
| subject_type | subjectType | String | 第一科目 |
| second_subject_type | secondSubjectType | String | 第二科目 |
| third_subject_type | thirdSubjectType | String | 第三科目 |

---

## 接口列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取用户触发的约束列表 | GET | /api/v1/app/constraint/match | 根据当前用户档案匹配触发的约束 |
| 获取约束详情列表 | POST | /api/v1/app/constraint/details | 根据约束code列表获取约束详情 |
| 校验专业组约束 | POST | /api/v1/app/constraint/check-group | 校验用户是否满足专业组约束 |

---

## 接口详情

### 1. 获取用户触发的约束列表

根据当前登录用户的高考档案，匹配所有触发的约束条件。

**请求**

```
GET /api/v1/app/constraint/match
Authorization: Bearer {accessToken}
```

**请求参数**

无（通过Token识别用户）

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| constraintCodes | String[] | 触发的约束代码列表 |
| totalCount | Integer | 触发的约束总数 |

**响应示例 - 有触发约束**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "constraintCodes": [
            "COLOR_BLIND",
            "VISION_LEFT_LOW",
            "HEIGHT_BELOW_170"
        ],
        "totalCount": 3
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 无触发约束**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "constraintCodes": [],
        "totalCount": 0
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 用户无档案**

```json
{
    "code": 404,
    "msg": "用户高考档案不存在，请先填写档案",
    "data": null,
    "timestamp": 1715587200000
}
```

---

### 2. 获取约束详情列表

根据约束code列表获取约束的完整详情信息。

**请求**

```
POST /api/v1/app/constraint/details
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| codes | String[] | 是 | 非空，最多100个 | 约束代码列表 |

**请求示例**

```json
{
    "codes": ["COLOR_BLIND", "VISION_LEFT_LOW", "HEIGHT_BELOW_170"]
}
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| constraints | Array | 约束详情列表 |
| constraints[].code | String | 约束代码（主键） |
| constraints[].name | String | 约束名称 |
| constraints[].category | String | 约束类别 |
| constraints[].description | String | 约束描述/提示信息 |
| constraints[].severity | String | 严重程度：HARD/SOFT |

**响应示例**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "constraints": [
            {
                "code": "COLOR_BLIND",
                "name": "色盲限制",
                "category": "视觉",
                "description": "色盲考生不能报考化学类、医学类、生物科学类等专业",
                "severity": "HARD"
            },
            {
                "code": "VISION_LEFT_LOW",
                "name": "左眼视力不足",
                "category": "视觉",
                "description": "左眼裸眼视力低于4.8，不宜报考飞行技术、航海技术等专业",
                "severity": "SOFT"
            },
            {
                "code": "HEIGHT_BELOW_170",
                "name": "身高不足170cm",
                "category": "身体指标",
                "description": "身高低于170cm，部分军校、公安类专业可能受限",
                "severity": "SOFT"
            }
        ]
    },
    "timestamp": 1715587200000
}
```

---

### 3. 校验专业组约束

校验当前用户是否满足指定专业组的约束要求，返回冲突的约束列表。

**请求**

```
POST /api/v1/app/constraint/check-group
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| groupId | Integer | 是 | > 0 | 专业组ID |

**请求示例**

```json
{
    "groupId": 12345
}
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| isPass | Boolean | 是否通过校验（无HARD约束冲突） |
| hardConflicts | Array | 硬限制冲突列表（不可报考） |
| softConflicts | Array | 软提示冲突列表（建议谨慎） |
| hardConflicts[].code | String | 约束代码 |
| hardConflicts[].name | String | 约束名称 |
| hardConflicts[].description | String | 约束描述 |

**响应示例 - 校验通过**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "isPass": true,
        "hardConflicts": [],
        "softConflicts": []
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 有硬限制冲突**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "isPass": false,
        "hardConflicts": [
            {
                "code": "COLOR_BLIND",
                "name": "色盲限制",
                "description": "色盲考生不能报考该专业组"
            }
        ],
        "softConflicts": [
            {
                "code": "VISION_LEFT_LOW",
                "name": "左眼视力不足",
                "description": "左眼裸眼视力低于4.8，建议谨慎报考"
            }
        ]
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 专业组不存在**

```json
{
    "code": 404,
    "msg": "专业组不存在",
    "data": null,
    "timestamp": 1715587200000
}
```

---

## 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 参数校验失败 | 检查请求参数格式 |
| 401 | 未登录或Token过期 | 重新登录获取Token |
| 404 | 资源不存在 | 用户档案不存在或专业组不存在 |
| 500 | 服务器内部错误 | 联系管理员 |

### 业务错误码

| 错误码 | 说明 |
|--------|------|
| 1010 | 用户高考档案不存在 |
| 1011 | 专业组不存在 |
| 1012 | 约束代码不存在 |

---

## 约束字典配置示例

### t_constraint_dict 表数据示例

```sql
-- 色盲限制
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('COLOR_BLIND', '色盲限制', '视觉', '色盲考生不能报考化学类、医学类、生物科学类等专业', 'HARD', 'is_color_blind', 'EQ', 'true', true, 1);

-- 色弱限制
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('COLOR_WEAK', '色弱限制', '视觉', '色弱考生不宜报考美术学、艺术设计类等专业', 'SOFT', 'is_color_weak', 'EQ', 'true', true, 2);

-- 左眼视力不足4.8
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('VISION_LEFT_LOW', '左眼视力不足', '视觉', '左眼裸眼视力低于4.8，不宜报考飞行技术、航海技术等专业', 'SOFT', 'vision_left', 'LT', '4.8', true, 3);

-- 身高不足170cm（男生）
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, extra_field, extra_operator, extra_value, is_active, sort_order)
VALUES ('HEIGHT_MALE_170', '男生身高不足170cm', '身体指标', '身高低于170cm，部分军校、公安类专业可能受限', 'SOFT', 'height_cm', 'LT', '170', null, null, null, true, 10);

-- 有纹身
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('HAS_TATTOO', '有纹身', '身体指标', '有纹身考生不能报考军校、公安类专业', 'HARD', 'has_tattoo', 'EQ', 'true', true, 15);

-- 非英语考生
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('NON_ENGLISH', '非英语考生', '外语', '非英语语种考生，部分中外合作办学专业可能受限', 'SOFT', 'foreign_language', 'NE', '英语', true, 20);

-- 非应届生
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('NOT_FRESH', '非应届生', '身份条件', '非应届生不能报考军校、免费师范生等专业', 'HARD', 'is_fresh_graduate', 'EQ', 'false', true, 25);

-- 非党员/团员
INSERT INTO t_constraint_dict (code, name, category, description, severity, check_field, check_operator, check_value, is_active, sort_order)
VALUES ('NOT_PARTY_MEMBER', '非党员/团员', '身份条件', '非党员或团员，部分军校、政法类专业可能有要求', 'SOFT', 'political_status', 'NOT_IN', '中共党员,中共预备党员,共青团员', true, 26);
```

---

## 运算符判断逻辑说明

### EQ（等于）
```java
// fieldValue 为 null 时返回 false
return fieldValue.toString().equals(checkValue);
```

### NE（不等于）
```java
// fieldValue 为 null 时返回 true（null 不等于任何值）
return !fieldValue.toString().equals(checkValue);
```

### GT / GE / LT / LE（数值比较）
```java
// fieldValue 为 null 时返回 false
// 使用 BigDecimal 比较，支持整数和小数
BigDecimal field = new BigDecimal(fieldValue.toString());
BigDecimal check = new BigDecimal(checkValue);
return field.compareTo(check) > 0;  // GT
return field.compareTo(check) >= 0; // GE
return field.compareTo(check) < 0;  // LT
return field.compareTo(check) <= 0; // LE
```

### IN（在列表中）
```java
// checkValue 格式："值1,值2,值3"
// fieldValue 为 null 时返回 false
Set<String> values = Arrays.stream(checkValue.split(","))
    .map(String::trim)
    .collect(Collectors.toSet());
return values.contains(fieldValue.toString());
```

### NOT_IN（不在列表中）
```java
// fieldValue 为 null 时返回 true
Set<String> values = Arrays.stream(checkValue.split(","))
    .map(String::trim)
    .collect(Collectors.toSet());
return !values.contains(fieldValue.toString());
```

### IS_NULL / IS_NOT_NULL
```java
return fieldValue == null;       // IS_NULL
return fieldValue != null;       // IS_NOT_NULL
```

---

## 架构设计

### 类图

```
┌─────────────────────────────────────────────────────────────┐
│                  ConstraintMatcherService                   │
│                        (接口)                                │
├─────────────────────────────────────────────────────────────┤
│ + matchConstraints(memberId: Long): List<String>            │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │ implements
┌─────────────────────────────────────────────────────────────┐
│               ConstraintMatcherServiceImpl                  │
│                        (实现)                                │
├─────────────────────────────────────────────────────────────┤
│ - memberGaokaoMapper: MemberGaokaoMapper                    │
│ - constraintDictMapper: ConstraintDictMapper                │
│ - strategyFactory: OperatorStrategyFactory                  │
│ - fieldExtractor: FieldValueExtractor                       │
├─────────────────────────────────────────────────────────────┤
│ + matchConstraints(memberId): List<String>                  │
│ - isTriggered(gaokao, constraint): boolean                  │
│ - evaluateCondition(gaokao, field, op, value): boolean      │
└─────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│FieldValueExtractor│ │OperatorStrategy- │ │ConstraintDict-   │
│                  │ │    Factory       │ │    Mapper        │
├──────────────────┤ ├──────────────────┤ ├──────────────────┤
│extract(gaokao,   │ │getStrategy(op):  │ │selectActiveList()│
│  fieldName)      │ │  OperatorStrategy│ │                  │
└──────────────────┘ └──────────────────┘ └──────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     OperatorStrategy                        │
│                        (接口)                                │
├─────────────────────────────────────────────────────────────┤
│ + evaluate(fieldValue: Object, checkValue: String): boolean │
└─────────────────────────────────────────────────────────────┘
          ▲
          │ implements (10个策略类)
          │
    ┌─────┴─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
    │           │     │     │     │     │     │     │     │     │
┌───┴───┐ ┌───┴───┐ ... (省略其他策略)
│  Eq   │ │  Ne   │
│Strategy│ │Strategy│
└───────┘ └───────┘
```

### 执行流程

```
1. 接收 memberId
        ↓
2. 查询用户高考档案 (MemberGaokao)
   └── 不存在 → 返回空列表
        ↓
3. 查询所有启用的约束 (ConstraintDict)
   └── is_active = true, ORDER BY sort_order
        ↓
4. 遍历每个约束
   ├── 4.1 提取主条件字段值 (FieldValueExtractor)
   ├── 4.2 获取运算符策略 (OperatorStrategyFactory)
   ├── 4.3 执行主条件判断 (OperatorStrategy.evaluate)
   ├── 4.4 如有额外条件，执行额外条件判断
   └── 4.5 主条件 AND 额外条件 都满足 → 加入触发列表
        ↓
5. 返回触发的约束 code 列表
```

---

## 文件清单

### Service（核心服务）
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/ConstraintMatcherService.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/ConstraintMatcherServiceImpl.java`

### 字段提取器
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/FieldValueExtractor.java`

### 策略接口与工厂
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/OperatorStrategy.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/matcher/operator/OperatorStrategyFactory.java`

### 策略实现类（10个）
- `haifeng-admin/.../matcher/operator/EqStrategy.java` - 等于
- `haifeng-admin/.../matcher/operator/NeStrategy.java` - 不等于
- `haifeng-admin/.../matcher/operator/GtStrategy.java` - 大于
- `haifeng-admin/.../matcher/operator/GeStrategy.java` - 大于等于
- `haifeng-admin/.../matcher/operator/LtStrategy.java` - 小于
- `haifeng-admin/.../matcher/operator/LeStrategy.java` - 小于等于
- `haifeng-admin/.../matcher/operator/InStrategy.java` - 在列表中
- `haifeng-admin/.../matcher/operator/NotInStrategy.java` - 不在列表中
- `haifeng-admin/.../matcher/operator/IsNullStrategy.java` - 为空
- `haifeng-admin/.../matcher/operator/IsNotNullStrategy.java` - 不为空

### Entity
- `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ConstraintDict.java`
- `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/MemberGaokao.java`

### Mapper
- `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java`
- `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java`

### API接口（haifeng-app）

#### Controller
- `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/ConstraintController.java`

#### Service
- `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/ConstraintService.java`
- `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/ConstraintServiceImpl.java`

#### DTO
- `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/ConstraintCodesDTO.java`
- `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/CheckGroupDTO.java`

#### VO
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ConstraintMatchVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ConstraintDetailVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ConstraintDetailsVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ConstraintConflictVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/CheckGroupResultVO.java`

---

## 数据库表

### t_constraint_dict - 约束条件字典表

```sql
CREATE TABLE IF NOT EXISTS t_constraint_dict (
    code                VARCHAR(50)     PRIMARY KEY,          -- 约束代码（主键）
    name                VARCHAR(100)    NOT NULL UNIQUE,      -- 约束名称
    category            VARCHAR(30)     NOT NULL,             -- 约束大类
    description         TEXT,                                  -- 约束描述/提示信息
    severity            VARCHAR(10)     NOT NULL DEFAULT 'HARD', -- HARD=硬限制/SOFT=软提示
    check_field         VARCHAR(50),                          -- 检查字段（对应t_member_gaokao）
    check_operator      VARCHAR(20),                          -- 判断运算符
    check_value         VARCHAR(100),                         -- 判断值
    extra_field         VARCHAR(50),                          -- 额外条件字段
    extra_operator      VARCHAR(20),                          -- 额外条件运算符
    extra_value         VARCHAR(100),                         -- 额外条件值
    sort_order          INTEGER         DEFAULT 0,            -- 排序顺序
    is_active           BOOLEAN         DEFAULT TRUE,         -- 是否启用
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_severity CHECK (severity IN ('HARD', 'SOFT'))
);

-- 索引
CREATE INDEX idx_cd_category ON t_constraint_dict (category);
CREATE INDEX idx_cd_is_active ON t_constraint_dict (is_active);
```

---

## 使用示例

### 内部服务调用

```java
@Service
@RequiredArgsConstructor
public class VolunteerRecommendService {

    private final ConstraintMatcherService constraintMatcherService;
    private final ConstraintDictMapper constraintDictMapper;

    public void recommendVolunteers(Long memberId) {
        // 1. 获取用户触发的约束列表
        List<String> triggeredCodes = constraintMatcherService.matchConstraints(memberId);

        // 2. 根据约束过滤专业组
        // triggeredCodes 包含该用户不能报考的限制条件代码
        // 在查询专业组时排除这些约束对应的专业组

        // 3. 获取约束详情用于前端展示
        if (!triggeredCodes.isEmpty()) {
            List<ConstraintDict> constraints = constraintDictMapper.selectBatchIds(triggeredCodes);
            // 返回给前端展示风险提示
        }
    }
}
```

---

## 扩展说明

### 添加新运算符

1. 创建新的策略类实现 `OperatorStrategy` 接口
2. 在 `OperatorStrategyFactory.init()` 中注册新策略
3. 在 `t_constraint_dict` 中使用新运算符

### 添加新检查字段

1. 确保 `MemberGaokao` 实体有对应字段
2. 在 `FieldValueExtractor.FIELD_GETTERS` 中添加映射
3. 在 `t_constraint_dict` 中配置新字段的约束规则

---

## 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-05-14 | 初始版本，实现约束匹配器核心功能（haifeng-admin） |
| 1.1.0 | 2026-05-14 | 实现 API 接口（haifeng-app），包含3个接口：匹配约束、获取详情、校验专业组 |
