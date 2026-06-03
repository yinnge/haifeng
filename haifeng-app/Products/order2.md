# 高考档案模块 - API 文档

## 功能概述

高考档案模块为登录用户提供个人高考信息的存储与管理功能，是高考志愿填报系统的核心数据基础。

### 核心功能

1. **改革模式检测**：根据用户省份和高考年份自动判断适用的高考改革模式（3+3、3+1+2、传统文理）
2. **位次查询**：从一分一段表中查询用户分数对应的位次和同分人数
3. **批次分数线查询**：查询省控线数据，支持当年数据不存在时自动回退到近5年数据
4. **档案保存**：保存/更新用户完整的高考档案信息
5. **档案查询**：获取当前登录用户的高考档案

### 业务规则

- **权限控制**：所有接口均需登录（@RequireLogin）
- **改革模式判定**：用户高考年份 >= 省份改革年份时，使用改革后的模式
- **传统文理科目映射**：
  - 文科 → 政治、历史、地理
  - 理科 → 物理、化学、生物
- **批次数据回退**：当年无数据时，自动查询近5年数据供用户选择
- **线差计算**：scoreAboveLine = score - batchLineScore（自动计算）

---

## 枚举说明

### ReformModelEnum - 高考改革模式

| 枚举值 | 数据库值 | 说明 |
|--------|----------|------|
| TRADITIONAL | 传统文理 | 传统文理分科模式 |
| THREE_PLUS_THREE | 3+3 | 上海、浙江等省份的3+3模式 |
| THREE_PLUS_ONE_PLUS_TWO | 3+1+2 | 广东、湖北等省份的3+1+2模式 |

### ForeignLanguageEnum - 外语语种

| 枚举值 | 数据库值 | 说明 |
|--------|----------|------|
| ENGLISH | 英语 | 英语 |
| JAPANESE | 日语 | 日语 |
| RUSSIAN | 俄语 | 俄语 |
| GERMAN | 德语 | 德语 |
| FRENCH | 法语 | 法语 |
| SPANISH | 西班牙语 | 西班牙语 |

### PoliticalStatusEnum - 政治面貌

| 枚举值 | 数据库值 | 说明 |
|--------|----------|------|
| MASSES | 群众 | 群众 |
| LEAGUE_MEMBER | 共青团员 | 共青团员 |
| PARTY_MEMBER | 中共党员 | 中共党员 |
| PROBATIONARY_PARTY_MEMBER | 中共预备党员 | 中共预备党员 |

### HouseholdTypeEnum - 户籍类型

| 枚举值 | 数据库值 | 说明 |
|--------|----------|------|
| URBAN | 城镇 | 城镇户口 |
| RURAL | 农村 | 农村户口 |

---

## 接口列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取改革模式 | GET | /api/v1/app/gaokao/reform-model | 根据省份和年份获取改革模式及可选科目 |
| 查询位次 | GET | /api/v1/app/gaokao/rank | 根据分数查询位次 |
| 获取批次分数线 | GET | /api/v1/app/gaokao/batch-lines | 获取批次分数线列表 |
| 保存高考档案 | POST | /api/v1/app/gaokao/archive | 新增或更新高考档案 |
| 获取我的档案 | GET | /api/v1/app/gaokao/archive | 获取当前用户的高考档案 |

---

## 接口详情

### 1. 获取改革模式

根据省份和高考年份自动判断适用的高考改革模式，并返回可选科目列表。

**请求**

```
GET /api/v1/app/gaokao/reform-model
```

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| province | String | 是 | 非空 | 省份名称 |
| year | Integer | 是 | 2020-2030 | 高考年份 |

**请求示例**

```
GET /api/v1/app/gaokao/reform-model?province=广东&year=2025
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| reformModel | String | 改革模式：3+3 / 3+1+2 / 传统文理 |
| subjects | Object | 可选科目，结构因模式不同而异 |

**响应示例 - 3+1+2 模式（广东2025年）**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "reformModel": "3+1+2",
        "subjects": {
            "first": ["物理", "历史"],
            "second": ["化学", "生物", "政治", "地理"]
        }
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 3+3 模式（上海2025年）**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "reformModel": "3+3",
        "subjects": {
            "first": ["物理", "化学", "生物", "政治", "历史", "地理"]
        }
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 传统文理模式（西藏2025年）**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "reformModel": "传统文理",
        "subjects": {
            "first": ["文科", "理科"]
        }
    },
    "timestamp": 1715587200000
}
```

**科目选择说明**

| 改革模式 | 选科方式 | subjectType | secondSubjectType | thirdSubjectType |
|----------|----------|-------------|-------------------|------------------|
| 3+1+2 | 首选1门 + 再选2门 | 物理/历史 | 化学/生物/政治/地理 | 化学/生物/政治/地理 |
| 3+3 | 任选3门 | 6选1 | 剩余5选1 | 剩余4选1 |
| 传统文理 | 选择文科或理科 | 文科/理科（系统自动映射） | 自动填充 | 自动填充 |

**传统文理自动映射规则**

- 选择"文科"：subjectType=政治, secondSubjectType=历史, thirdSubjectType=地理
- 选择"理科"：subjectType=物理, secondSubjectType=化学, thirdSubjectType=生物

---

### 2. 查询位次

根据省份、年份、科类和分数查询一分一段表中的位次信息。

**请求**

```
GET /api/v1/app/gaokao/rank
```

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| province | String | 是 | 非空 | 省份名称 |
| year | Integer | 是 | - | 高考年份 |
| subjectType | String | 是 | 非空 | 科类（物理类/历史类/理科/文科） |
| score | Integer | 是 | 0-750 | 高考分数 |

**请求示例**

```
GET /api/v1/app/gaokao/rank?province=广东&year=2025&subjectType=物理类&score=620
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| rank | Integer | 位次（名次），无数据时为null |
| sameScoreCount | Integer | 同分人数，无数据时为null |

**响应示例 - 查询成功**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "rank": 8523,
        "sameScoreCount": 156
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 无数据**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "rank": null,
        "sameScoreCount": null
    },
    "timestamp": 1715587200000
}
```

---

### 3. 获取批次分数线

查询指定省份、年份、科类的批次分数线列表。当年无数据时自动回退到近5年数据。

**请求**

```
GET /api/v1/app/gaokao/batch-lines
```

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| province | String | 是 | 非空 | 省份名称 |
| year | Integer | 是 | - | 高考年份 |
| subjectType | String | 是 | 非空 | 科类（物理类/历史类/理科/文科） |

**请求示例**

```
GET /api/v1/app/gaokao/batch-lines?province=广东&year=2025&subjectType=物理类
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| dataYear | Integer | 数据来源年份 |
| isCurrentYear | Boolean | 是否为当年数据 |
| batches | Array | 批次列表 |
| batches[].batch | String | 批次名称 |
| batches[].scoreLine | Integer | 分数线 |
| batches[].rankLine | Integer | 位次线（可能为null） |

**响应示例 - 当年数据**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "dataYear": 2025,
        "isCurrentYear": true,
        "batches": [
            {
                "batch": "本科批",
                "scoreLine": 445,
                "rankLine": 189562
            },
            {
                "batch": "特殊类型招生控制线",
                "scoreLine": 539,
                "rankLine": 68745
            },
            {
                "batch": "专科批",
                "scoreLine": 180,
                "rankLine": 385621
            }
        ]
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 回退到历史年份数据**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "dataYear": 2024,
        "isCurrentYear": false,
        "batches": [
            {
                "batch": "本科批",
                "scoreLine": 442,
                "rankLine": 186523
            },
            {
                "batch": "特殊类型招生控制线",
                "scoreLine": 532,
                "rankLine": 71234
            }
        ]
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 无数据**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "dataYear": null,
        "isCurrentYear": false,
        "batches": []
    },
    "timestamp": 1715587200000
}
```

---

### 4. 保存高考档案

保存或更新当前登录用户的高考档案，每个用户只能有一份档案。

**请求**

```
POST /api/v1/app/gaokao/archive
Content-Type: application/json
```

**请求参数（Body）**

#### 必填字段

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| gaokaoYear | Short | 是 | 2020-2030 | 高考年份 |
| gaokaoProvince | String | 是 | 最多30字符 | 高考省份 |
| score | Integer | 是 | 0-750 | 高考总分 |
| rank | Integer | 是 | >= 1 | 位次 |
| reformModel | String | 是 | 枚举值 | 改革模式：3+3 / 3+1+2 / 传统文理 |
| subjectType | String | 是 | 最多20字符 | 第一科目 |
| secondSubjectType | String | 条件必填 | 最多20字符 | 第二科目（3+3、3+1+2必填） |
| thirdSubjectType | String | 条件必填 | 最多20字符 | 第三科目（3+3、3+1+2必填） |
| batch | String | 是 | 最多50字符 | 所在批次 |
| batchDataYear | Short | 是 | - | 批次数据年份 |
| batchLineScore | Integer | 是 | - | 批次省控线 |

#### 可选字段 - 各科成绩

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| scoreChinese | Integer | 否 | 0-150 | 语文成绩 |
| scoreMath | Integer | 否 | 0-150 | 数学成绩 |
| scoreEnglish | Integer | 否 | 0-150 | 外语成绩 |
| scoreSubject1 | Integer | 否 | 0-100 | 第一科目分数 |
| scoreSubject2 | Integer | 否 | 0-100 | 第二科目分数 |
| scoreSubject3 | Integer | 否 | 0-100 | 第三科目分数 |

#### 可选字段 - 外语语种

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| foreignLanguage | String | 否 | 最多20字符 | 外语语种（见枚举） |

#### 可选字段 - 身体条件

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| isColorBlind | Boolean | 否 | - | 是否色盲 |
| isColorWeak | Boolean | 否 | - | 是否色弱 |
| visionLeft | BigDecimal | 否 | 0.0-5.5 | 左眼视力 |
| visionRight | BigDecimal | 否 | 0.0-5.5 | 右眼视力 |
| hasSmellDisorder | Boolean | 否 | - | 是否嗅觉障碍 |
| heightCm | Integer | 否 | 100-250 | 身高（厘米） |
| weightKg | BigDecimal | 否 | 20.0-200.0 | 体重（公斤） |
| isLeftHanded | Boolean | 否 | - | 是否左撇子 |
| hasTattoo | Boolean | 否 | - | 是否有纹身 |
| hasScar | Boolean | 否 | - | 是否有疤痕 |
| hasStutter | Boolean | 否 | - | 是否口吃 |

#### 可选字段 - 身份条件

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| isFreshGraduate | Boolean | 否 | - | 是否应届生 |
| politicalStatus | String | 否 | 最多20字符 | 政治面貌（见枚举） |
| householdType | String | 否 | 最多20字符 | 户籍类型（见枚举） |
| isPovertyCounty | Boolean | 否 | - | 是否贫困县 |

**请求示例 - 3+1+2模式（完整）**

```json
{
    "gaokaoYear": 2025,
    "gaokaoProvince": "广东",
    "score": 620,
    "rank": 8523,
    "reformModel": "3+1+2",
    "subjectType": "物理",
    "secondSubjectType": "化学",
    "thirdSubjectType": "生物",
    "batch": "本科批",
    "batchDataYear": 2025,
    "batchLineScore": 445,
    "scoreChinese": 125,
    "scoreMath": 138,
    "scoreEnglish": 142,
    "scoreSubject1": 89,
    "scoreSubject2": 78,
    "scoreSubject3": 48,
    "foreignLanguage": "英语",
    "isColorBlind": false,
    "isColorWeak": false,
    "visionLeft": 4.8,
    "visionRight": 5.0,
    "hasSmellDisorder": false,
    "heightCm": 175,
    "weightKg": 65.5,
    "isLeftHanded": false,
    "hasTattoo": false,
    "hasScar": false,
    "hasStutter": false,
    "isFreshGraduate": true,
    "politicalStatus": "共青团员",
    "householdType": "城镇",
    "isPovertyCounty": false
}
```

**请求示例 - 3+3模式（最小必填）**

```json
{
    "gaokaoYear": 2025,
    "gaokaoProvince": "上海",
    "score": 580,
    "rank": 3256,
    "reformModel": "3+3",
    "subjectType": "物理",
    "secondSubjectType": "化学",
    "thirdSubjectType": "历史",
    "batch": "本科批",
    "batchDataYear": 2025,
    "batchLineScore": 400
}
```

**请求示例 - 传统文理模式**

```json
{
    "gaokaoYear": 2025,
    "gaokaoProvince": "西藏",
    "score": 520,
    "rank": 1256,
    "reformModel": "传统文理",
    "subjectType": "理科",
    "batch": "本科一批",
    "batchDataYear": 2024,
    "batchLineScore": 480
}
```

> **注意**：传统文理模式下，只需填写 subjectType（文科/理科），系统会自动映射三个具体科目。

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | Long | 档案ID（雪花算法生成） |

**响应示例**

```json
{
    "code": 200,
    "msg": "success",
    "data": 1923456789012345678,
    "timestamp": 1715587200000
}
```

**错误响应示例**

```json
{
    "code": 400,
    "msg": "高考年份不能为空",
    "data": null,
    "timestamp": 1715587200000
}
```

```json
{
    "code": 400,
    "msg": "改革模式无效",
    "data": null,
    "timestamp": 1715587200000
}
```

---

### 5. 获取我的档案

获取当前登录用户的高考档案详情。

**请求**

```
GET /api/v1/app/gaokao/archive
```

**请求参数**

无（通过Token识别用户）

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 档案ID |
| gaokaoYear | Short | 高考年份 |
| gaokaoProvince | String | 高考省份 |
| score | Integer | 高考总分 |
| rank | Integer | 位次 |
| reformModel | String | 改革模式 |
| subjectType | String | 第一科目 |
| secondSubjectType | String | 第二科目 |
| thirdSubjectType | String | 第三科目 |
| scoreChinese | Integer | 语文成绩 |
| scoreMath | Integer | 数学成绩 |
| scoreEnglish | Integer | 外语成绩 |
| scoreSubject1 | Integer | 第一科目分数 |
| scoreSubject2 | Integer | 第二科目分数 |
| scoreSubject3 | Integer | 第三科目分数 |
| foreignLanguage | String | 外语语种 |
| isColorBlind | Boolean | 是否色盲 |
| isColorWeak | Boolean | 是否色弱 |
| visionLeft | BigDecimal | 左眼视力 |
| visionRight | BigDecimal | 右眼视力 |
| hasSmellDisorder | Boolean | 是否嗅觉障碍 |
| heightCm | Integer | 身高（厘米） |
| weightKg | BigDecimal | 体重（公斤） |
| isLeftHanded | Boolean | 是否左撇子 |
| hasTattoo | Boolean | 是否有纹身 |
| hasScar | Boolean | 是否有疤痕 |
| hasStutter | Boolean | 是否口吃 |
| isFreshGraduate | Boolean | 是否应届生 |
| politicalStatus | String | 政治面貌 |
| householdType | String | 户籍类型 |
| isPovertyCounty | Boolean | 是否贫困县 |
| batch | String | 所在批次 |
| batchDataYear | Short | 批次数据年份 |
| batchLineScore | Integer | 批次省控线 |
| scoreAboveLine | Integer | 线差（自动计算：score - batchLineScore） |

**响应示例 - 有档案**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1923456789012345678,
        "gaokaoYear": 2025,
        "gaokaoProvince": "广东",
        "score": 620,
        "rank": 8523,
        "reformModel": "3+1+2",
        "subjectType": "物理",
        "secondSubjectType": "化学",
        "thirdSubjectType": "生物",
        "scoreChinese": 125,
        "scoreMath": 138,
        "scoreEnglish": 142,
        "scoreSubject1": 89,
        "scoreSubject2": 78,
        "scoreSubject3": 48,
        "foreignLanguage": "英语",
        "isColorBlind": false,
        "isColorWeak": false,
        "visionLeft": 4.8,
        "visionRight": 5.0,
        "hasSmellDisorder": false,
        "heightCm": 175,
        "weightKg": 65.5,
        "isLeftHanded": false,
        "hasTattoo": false,
        "hasScar": false,
        "hasStutter": false,
        "isFreshGraduate": true,
        "politicalStatus": "共青团员",
        "householdType": "城镇",
        "isPovertyCounty": false,
        "batch": "本科批",
        "batchDataYear": 2025,
        "batchLineScore": 445,
        "scoreAboveLine": 175
    },
    "timestamp": 1715587200000
}
```

**响应示例 - 无档案**

```json
{
    "code": 200,
    "msg": "success",
    "data": null,
    "timestamp": 1715587200000
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或Token过期 |
| 500 | 服务器内部错误 |

---

## 数据库表

### t_member_gaokao - 用户高考档案表

```sql
CREATE TABLE IF NOT EXISTS t_member_gaokao (
    id                    BIGINT          PRIMARY KEY,
    member_id             BIGINT          NOT NULL UNIQUE,
    gaokao_year           SMALLINT        NOT NULL,
    gaokao_province       VARCHAR(30)     NOT NULL,
    score                 INTEGER         NOT NULL,
    rank                  INTEGER         NOT NULL,
    reform_model          VARCHAR(20)     NOT NULL,
    subject_type          VARCHAR(20)     NOT NULL,
    second_subject_type   VARCHAR(20),
    third_subject_type    VARCHAR(20),
    score_chinese         INTEGER,
    score_math            INTEGER,
    score_english         INTEGER,
    score_subject_1       INTEGER,
    score_subject_2       INTEGER,
    score_subject_3       INTEGER,
    foreign_language      VARCHAR(20),
    is_color_blind        BOOLEAN,
    is_color_weak         BOOLEAN,
    vision_left           NUMERIC(3,1),
    vision_right          NUMERIC(3,1),
    has_smell_disorder    BOOLEAN,
    height_cm             INTEGER,
    weight_kg             NUMERIC(5,2),
    is_left_handed        BOOLEAN,
    has_tattoo            BOOLEAN,
    has_scar              BOOLEAN,
    has_stutter           BOOLEAN,
    is_fresh_graduate     BOOLEAN,
    political_status      VARCHAR(20),
    household_type        VARCHAR(20),
    is_poverty_county     BOOLEAN,
    batch                 VARCHAR(50)     NOT NULL,
    batch_data_year       SMALLINT        NOT NULL,
    batch_line_score      INTEGER         NOT NULL,
    score_above_line      INTEGER         NOT NULL,
    created_at            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_member_gaokao_member FOREIGN KEY (member_id) REFERENCES t_member(id)
);
```

---

## 使用流程

### 典型用户操作流程

```
1. 用户登录系统
        ↓
2. 调用 GET /reform-model 获取改革模式
   - 输入：省份 + 高考年份
   - 获得：改革模式 + 可选科目列表
        ↓
3. 调用 GET /rank 查询位次
   - 输入：省份 + 年份 + 科类 + 分数
   - 获得：位次 + 同分人数
        ↓
4. 调用 GET /batch-lines 获取批次分数线
   - 输入：省份 + 年份 + 科类
   - 获得：批次列表（注意数据年份）
        ↓
5. 调用 POST /archive 保存档案
   - 整合上述信息 + 可选身体/身份条件
        ↓
6. 调用 GET /archive 查看已保存档案
```

### 批次数据处理说明

1. **当年有数据**：直接使用，`batchDataYear = gaokaoYear`，`isCurrentYear = true`
2. **当年无数据**：
   - 接口返回近5年中最近有数据的年份
   - 前端提示用户"使用的是XXXX年数据"
   - 用户选择批次后，`batchDataYear` 存储实际数据年份
3. **算法使用**：后续志愿推荐算法会根据 `batchDataYear` 是否等于 `gaokaoYear` 来调整权重

---

## 文件清单

### Controller
- `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/GaokaoArchiveController.java`

### Service
- `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/GaokaoArchiveService.java`
- `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java`

### DTO
- `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/GaokaoArchiveSaveDTO.java`

### VO
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ReformModelVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ScoreRankVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/BatchLineVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/BatchLineListVO.java`
- `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/GaokaoArchiveVO.java`

### Entity
- `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/MemberGaokao.java`

### Mapper
- `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java`

### Enum
- `haifeng-common/src/main/java/com/haifeng/common/enums/ReformModelEnum.java`
- `haifeng-common/src/main/java/com/haifeng/common/enums/ForeignLanguageEnum.java`
- `haifeng-common/src/main/java/com/haifeng/common/enums/PoliticalStatusEnum.java`
- `haifeng-common/src/main/java/com/haifeng/common/enums/HouseholdTypeEnum.java`

### Flyway Migration
- `haifeng-admin/src/main/resources/db/migration/apps_V17__member_gaokao__tables.sql`
