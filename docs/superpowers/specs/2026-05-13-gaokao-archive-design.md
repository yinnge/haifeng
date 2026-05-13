# 高考档案模块设计

> 日期：2026-05-13
> 模块：haifeng-app / algorithm
> 状态：待实施

## 1. 概述

### 1.1 功能目标

为用户提供高考档案管理功能，作为志愿填报算法的核心输入数据。用户可以录入高考基本信息、选科情况、身体条件等，系统自动计算位次、改革模式、线差等关键数据。

### 1.2 权限要求

- 所有接口需要 `@RequireLogin` 注解
- 一个用户只能有一条高考档案记录

## 2. 数据库设计

### 2.1 表结构：t_member_gaokao

文件位置：`haifeng-admin/src/main/resources/db/migration/apps_V17__member_gaokao__tables.sql`

```sql
CREATE TABLE IF NOT EXISTS t_member_gaokao (
    id                      BIGINT          PRIMARY KEY,
    member_id               BIGINT          NOT NULL UNIQUE,

    -- 一、高考基本信息（必填）
    gaokao_year             SMALLINT,                           -- 高考年份
    gaokao_province         VARCHAR(30),                        -- 高考省份
    score                   INTEGER,                            -- 高考总分
    rank                    INTEGER,                            -- 位次（可自动计算或用户自定义）

    -- 二、改革模式（系统根据省份+年份自动判断）
    reform_model            VARCHAR(20),                        -- 3+3 / 3+1+2 / 传统文理

    -- 三、选科信息（必填，与分数字段一一对应）
    subject_type            VARCHAR(20),                        -- 第一科目
    second_subject_type     VARCHAR(20),                        -- 第二科目
    third_subject_type      VARCHAR(20),                        -- 第三科目

    -- 四、各科成绩（可选）
    score_chinese           INTEGER,                            -- 语文
    score_math              INTEGER,                            -- 数学
    score_english           INTEGER,                            -- 外语
    score_subject_1         INTEGER,                            -- 第一科目分数
    score_subject_2         INTEGER,                            -- 第二科目分数
    score_subject_3         INTEGER,                            -- 第三科目分数

    -- 五、外语语种（可选）
    foreign_language        VARCHAR(20),                        -- 英语/日语/俄语/德语/法语/西班牙语

    -- 六、身体视觉条件（可选，全部允许 NULL）
    is_color_blind          BOOLEAN,                            -- 色盲
    is_color_weak           BOOLEAN,                            -- 色弱
    vision_left             NUMERIC(3,1),                       -- 左眼裸眼视力
    vision_right            NUMERIC(3,1),                       -- 右眼裸眼视力
    has_smell_disorder      BOOLEAN,                            -- 嗅觉迟钝

    -- 七、身体指标（可选）
    height_cm               INTEGER,                            -- 身高（厘米）
    weight_kg               NUMERIC(5,1),                       -- 体重（公斤）
    is_left_handed          BOOLEAN,                            -- 左利手
    has_tattoo              BOOLEAN,                            -- 纹身
    has_scar                BOOLEAN,                            -- 面部明显疤痕
    has_stutter             BOOLEAN,                            -- 口吃

    -- 八、身份条件（可选）
    is_fresh_graduate       BOOLEAN,                            -- 应届生
    political_status        VARCHAR(20),                        -- 政治面貌
    household_type          VARCHAR(20),                        -- 户籍类型
    is_poverty_county       BOOLEAN,                            -- 国家级贫困县户籍

    -- 九、批次与线差
    batch                   VARCHAR(50),                        -- 用户选择的批次名称
    batch_data_year         SMALLINT,                           -- 批次数据来源年份
    batch_line_score        INTEGER,                            -- 省控线
    score_above_line        INTEGER,                            -- 线差 = score - batch_line_score

    -- 审计字段
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_mg_member ON t_member_gaokao (member_id);
CREATE INDEX idx_mg_province_year ON t_member_gaokao (gaokao_province, gaokao_year);
CREATE INDEX idx_mg_score ON t_member_gaokao (score DESC NULLS LAST);

-- 外键
ALTER TABLE t_member_gaokao
    ADD CONSTRAINT fk_mg_member
    FOREIGN KEY (member_id) REFERENCES t_member(id);

-- 触发器
CREATE TRIGGER trg_mg_updated_at
    BEFORE UPDATE ON t_member_gaokao
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_gaokao IS '用户高考档案表：一人一条，志愿算法核心输入';
COMMENT ON COLUMN t_member_gaokao.member_id IS '关联会员表ID（唯一）';
COMMENT ON COLUMN t_member_gaokao.gaokao_year IS '高考年份';
COMMENT ON COLUMN t_member_gaokao.gaokao_province IS '高考省份';
COMMENT ON COLUMN t_member_gaokao.score IS '高考总分';
COMMENT ON COLUMN t_member_gaokao.rank IS '位次（系统查询或用户自定义）';
COMMENT ON COLUMN t_member_gaokao.reform_model IS '改革模式（3+3/3+1+2/传统文理）';
COMMENT ON COLUMN t_member_gaokao.subject_type IS '第一科目';
COMMENT ON COLUMN t_member_gaokao.second_subject_type IS '第二科目';
COMMENT ON COLUMN t_member_gaokao.third_subject_type IS '第三科目';
COMMENT ON COLUMN t_member_gaokao.batch IS '用户选择的批次名称';
COMMENT ON COLUMN t_member_gaokao.batch_data_year IS '批次数据来源年份（用于算法判断）';
COMMENT ON COLUMN t_member_gaokao.batch_line_score IS '省控线';
COMMENT ON COLUMN t_member_gaokao.score_above_line IS '线差（总分-省控线）';
```

### 2.2 关键设计决策

| 决策点 | 结论 | 理由 |
|--------|------|------|
| 主键类型 | BIGINT（雪花算法） | 符合项目规范 |
| 布尔字段默认值 | 全部允许 NULL | 避免默认值干扰算法查询 |
| 新增 reform_model | 存储改革模式类型 | 便于后续算法权重映射 |
| 新增 batch_data_year | 标记批次数据来源年份 | 解决跨年份数据准确性问题 |
| 位次存储 | 只存最终值 | 不区分系统查询还是用户自定义 |

## 3. API 接口设计

### 3.1 接口列表

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/app/gaokao/reform-model` | 获取改革模式及可选科目 | @RequireLogin |
| GET | `/api/v1/app/gaokao/rank` | 查询位次 | @RequireLogin |
| GET | `/api/v1/app/gaokao/batch-lines` | 获取批次列表及省控线 | @RequireLogin |
| POST | `/api/v1/app/gaokao/archive` | 保存高考档案 | @RequireLogin |
| GET | `/api/v1/app/gaokao/archive` | 查询我的高考档案 | @RequireLogin |

### 3.2 接口详情

#### 3.2.1 获取改革模式

```
GET /api/v1/app/gaokao/reform-model?province=广东&year=2024

Response:
{
  "code": 200,
  "data": {
    "reformModel": "3+1+2",
    "subjects": {
      "first": ["物理", "历史"],
      "second": ["化学", "生物", "政治", "地理"]
    }
  }
}
```

#### 3.2.2 查询位次

```
GET /api/v1/app/gaokao/rank?province=广东&year=2024&subjectType=物理类&score=600

Response:
{
  "code": 200,
  "data": {
    "rank": 12580,
    "sameScoreCount": 156
  }
}

// 未找到时返回 null，前端提示用户手动输入
```

#### 3.2.3 获取批次列表

```
GET /api/v1/app/gaokao/batch-lines?province=广东&year=2024&subjectType=物理类

Response:
{
  "code": 200,
  "data": {
    "dataYear": 2024,           // 数据来源年份
    "isCurrentYear": true,      // 是否为当年数据
    "batches": [
      { "batch": "本科批", "scoreLine": 445, "rankLine": 168000 },
      { "batch": "专科批", "scoreLine": 200, "rankLine": 350000 }
    ]
  }
}

// 当年无数据时，返回最近5年可用数据，isCurrentYear=false
```

#### 3.2.4 保存高考档案

```
POST /api/v1/app/gaokao/archive

Request Body:
{
  // 必填
  "gaokaoYear": 2024,
  "gaokaoProvince": "广东",
  "score": 600,
  "rank": 12580,
  "reformModel": "3+1+2",
  "subjectType": "物理",
  "secondSubjectType": "化学",
  "thirdSubjectType": "生物",
  "batch": "本科批",
  "batchDataYear": 2024,
  "batchLineScore": 445,
  "scoreAboveLine": 155,

  // 可选
  "scoreChinese": 120,
  "scoreMath": 135,
  "scoreEnglish": 140,
  "scoreSubject1": 95,
  "scoreSubject2": 85,
  "scoreSubject3": 80,
  "foreignLanguage": "英语",
  "isColorBlind": false,
  "isColorWeak": false,
  // ... 其他可选字段
}

Response:
{
  "code": 200,
  "msg": "保存成功",
  "data": { "id": 1234567890 }
}
```

#### 3.2.5 查询我的高考档案

```
GET /api/v1/app/gaokao/archive

Response:
{
  "code": 200,
  "data": {
    "id": 1234567890,
    "gaokaoYear": 2024,
    "gaokaoProvince": "广东",
    "score": 600,
    "rank": 12580,
    "reformModel": "3+1+2",
    // ... 完整档案信息
  }
}

// 未创建档案时 data 为 null
```

## 4. 核心业务逻辑

### 4.1 改革模式判断

```java
/**
 * 根据省份和高考年份判断改革模式
 *
 * 逻辑：
 * 1. 查询该省份所有改革配置，按 reformYear 升序
 * 2. 找到 reformYear <= gaokaoYear 的最新配置
 * 3. 返回对应的 reformModel
 *
 * 边界处理：高考年份 >= 改革年份 时使用新模式
 */
public String getReformModel(String province, Integer gaokaoYear) {
    List<ProvinceReform> reforms = provinceReformMapper.selectList(
        new LambdaQueryWrapper<ProvinceReform>()
            .eq(ProvinceReform::getProvince, province)
            .orderByAsc(ProvinceReform::getReformYear)
    );

    // 倒序遍历，找到第一个 reformYear <= gaokaoYear 的配置
    for (int i = reforms.size() - 1; i >= 0; i--) {
        ProvinceReform reform = reforms.get(i);
        if (reform.getReformYear() == null) {
            return "传统文理";
        }
        if (gaokaoYear >= reform.getReformYear()) {
            return reform.getReformModel();
        }
    }
    return "传统文理";
}
```

### 4.2 可选科目映射

| 改革模式 | 首选科目（subject_type） | 再选科目（second/third） |
|----------|--------------------------|--------------------------|
| 3+3 | 物理、化学、生物、政治、历史、地理（6选3） | 同首选池，不可重复 |
| 3+1+2 | 物理、历史（2选1） | 化学、生物、政治、地理（4选2） |
| 传统文理 | 文科、理科（2选1） | 无需选择（NULL） |

### 4.3 批次查询逻辑

```java
/**
 * 获取批次列表
 * 优先当年数据，否则返回最近5年数据
 */
public BatchLineListVO getBatchLines(String province, Integer year, String subjectType) {
    // 1. 先查当年数据
    List<BatchScoreLine> lines = batchScoreLineMapper.selectList(
        new LambdaQueryWrapper<BatchScoreLine>()
            .eq(BatchScoreLine::getProvince, province)
            .eq(BatchScoreLine::getYear, year)
            .eq(BatchScoreLine::getSubjectType, subjectType)
    );

    if (!lines.isEmpty()) {
        return new BatchLineListVO(year, true, convert(lines));
    }

    // 2. 当年无数据，查最近5年
    lines = batchScoreLineMapper.selectList(
        new LambdaQueryWrapper<BatchScoreLine>()
            .eq(BatchScoreLine::getProvince, province)
            .eq(BatchScoreLine::getSubjectType, subjectType)
            .ge(BatchScoreLine::getYear, year - 5)
            .orderByDesc(BatchScoreLine::getYear)
    );

    Integer dataYear = lines.isEmpty() ? null : lines.get(0).getYear();
    return new BatchLineListVO(dataYear, false, convert(lines));
}
```

### 4.4 档案保存逻辑

```java
/**
 * 保存高考档案（新增或更新）
 * 一人一条记录
 */
public Long saveArchive(Long memberId, GaokaoArchiveSaveDTO dto) {
    MemberGaokao existing = memberGaokaoMapper.selectOne(
        new LambdaQueryWrapper<MemberGaokao>()
            .eq(MemberGaokao::getMemberId, memberId)
    );

    MemberGaokao entity = convertToEntity(dto);
    entity.setMemberId(memberId);

    // 自动计算线差
    if (dto.getScore() != null && dto.getBatchLineScore() != null) {
        entity.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
    }

    if (existing == null) {
        entity.setId(snowflakeIdGenerator.nextId());
        memberGaokaoMapper.insert(entity);
    } else {
        entity.setId(existing.getId());
        memberGaokaoMapper.updateById(entity);
    }

    return entity.getId();
}
```

### 4.5 算法适配说明

当 `batch_data_year != gaokao_year` 时，算法层需要特殊处理：

1. **检测标记**：读取 `batch_data_year` 判断数据来源
2. **动态查询**：尝试查询 `gaokao_year` 的实际批次数据
3. **降级处理**：如仍无数据，使用档案中的值并标记为"参考值"

## 5. 包结构

### 5.1 haifeng-common（新增）

```
haifeng-common/
├── entity/algorithm/
│   └── MemberGaokao.java
├── mapper/algorithm/
│   └── MemberGaokaoMapper.java
├── enums/
│   ├── ReformModelEnum.java
│   ├── ForeignLanguageEnum.java
│   ├── PoliticalStatusEnum.java
│   └── HouseholdTypeEnum.java
```

### 5.2 haifeng-app（新增）

```
haifeng-app/
├── controller/algorithm/
│   └── GaokaoArchiveController.java
├── service/algorithm/
│   └── GaokaoArchiveService.java
├── service/impl/algorithm/
│   └── GaokaoArchiveServiceImpl.java
├── dto/algorithm/
│   ├── GaokaoArchiveSaveDTO.java
│   ├── ReformModelQueryDTO.java
│   ├── RankQueryDTO.java
│   └── BatchLineQueryDTO.java
├── vo/algorithm/
│   ├── GaokaoArchiveVO.java
│   ├── ReformModelVO.java
│   ├── ScoreRankVO.java
│   └── BatchLineVO.java
```

## 6. 枚举类设计

### 6.1 ReformModelEnum

```java
@Getter
@AllArgsConstructor
public enum ReformModelEnum {
    TRADITIONAL("传统文理"),
    THREE_PLUS_THREE("3+3"),
    THREE_PLUS_ONE_PLUS_TWO("3+1+2");

    @EnumValue
    private final String value;
}
```

### 6.2 ForeignLanguageEnum

```java
@Getter
@AllArgsConstructor
public enum ForeignLanguageEnum {
    ENGLISH("英语"),
    JAPANESE("日语"),
    RUSSIAN("俄语"),
    GERMAN("德语"),
    FRENCH("法语"),
    SPANISH("西班牙语");

    @EnumValue
    private final String value;
}
```

### 6.3 PoliticalStatusEnum

```java
@Getter
@AllArgsConstructor
public enum PoliticalStatusEnum {
    MASSES("群众"),
    LEAGUE_MEMBER("共青团员"),
    PARTY_MEMBER("中共党员"),
    PROBATIONARY_PARTY_MEMBER("中共预备党员");

    @EnumValue
    private final String value;
}
```

### 6.4 HouseholdTypeEnum

```java
@Getter
@AllArgsConstructor
public enum HouseholdTypeEnum {
    URBAN("城镇"),
    RURAL("农村");

    @EnumValue
    private final String value;
}
```

## 7. 实施任务

### 任务1：创建数据表

- 文件：`apps_V17__member_gaokao__tables.sql`
- 包含：建表语句、索引、外键、触发器、注释

### 任务2：实现高考档案模块

1. 创建实体类 `MemberGaokao` 和 Mapper
2. 创建枚举类（4个）
3. 实现 `GaokaoArchiveController`（5个接口）
4. 实现 `GaokaoArchiveService` 及其实现类
5. 创建 DTO/VO 类

## 8. 校验规则

### 8.1 必填字段校验

| 字段 | 校验规则 |
|------|----------|
| gaokaoYear | @NotNull, 2020-2030 |
| gaokaoProvince | @NotBlank |
| score | @NotNull, 0-750 |
| rank | @NotNull, > 0 |
| reformModel | @NotBlank |
| subjectType | @NotBlank |
| batch | @NotBlank |
| batchDataYear | @NotNull |
| batchLineScore | @NotNull |

### 8.2 选科逻辑校验

- 3+1+2 模式：subjectType 必须为 物理/历史，second/third 必须为 化学/生物/政治/地理
- 3+3 模式：三个科目必须从 物理/化学/生物/政治/历史/地理 中选择，不可重复
- 传统文理：subjectType 必须为 文科/理科，second/third 必须为 NULL
