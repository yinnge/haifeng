# 特殊通道模块设计规格

## 概述

实现特殊招生通道管理模块，包含4个子模块的后台CRUD功能，仅限管理员访问。

## 模块范围

| 子模块 | 数据表 | 功能 |
|--------|--------|------|
| 特殊招生通道列表 | t_special_channel | 增删改查、禁用 |
| 通道-大学关联 | t_special_channel_university | 增删改查、禁用 |
| 强基计划数据 | t_strong_base_score | 增删改查、禁用 |
| 强基院校配置 | t_strong_base_university | 增删改查（无禁用） |

## 技术决策

| 决策项 | 选择 |
|--------|------|
| 主键类型 | BIGINT + 雪花算法 |
| 删除方式 | 硬删除 |
| 索引条件 | WHERE is_active = TRUE |
| 批量删除参数 | List<Long> ids |
| Controller架构 | 每个子表独立Controller |

---

## 数据库设计

### 1. t_special_channel（特殊招生通道内容表）

```sql
CREATE TABLE IF NOT EXISTS t_special_channel (
    id                  BIGINT          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL UNIQUE,
    channel_name        VARCHAR(50)     NOT NULL,
    subtitle            VARCHAR(200),
    parent_code         VARCHAR(30),
    filter_label        VARCHAR(30),
    display_type        VARCHAR(20)     NOT NULL,
    content             TEXT,
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sc_display_type ON t_special_channel(display_type) WHERE is_active = TRUE;
CREATE INDEX idx_sc_parent ON t_special_channel(parent_code) WHERE is_active = TRUE;
```

**display_type枚举值**：
- `UNIVERSITY_LIST` - 展示大学列表
- `ARTICLE_ONLY` - 只展示文章
- `MAJOR_DATA` - 展示专业级数据
- `GROUP` - 分组父节点

### 2. t_special_channel_university（通道-大学关联表）

```sql
CREATE TABLE IF NOT EXISTS t_special_channel_university (
    id                  BIGINT          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL,
    channel_name        VARCHAR(50)     NOT NULL,
    university_id       BIGINT          NOT NULL,
    university_name     VARCHAR(50)     NOT NULL,
    year                SMALLINT,
    region_tag          VARCHAR(20),
    signup_start        TIMESTAMPTZ,
    signup_end          TIMESTAMPTZ,
    official_url        VARCHAR(500),
    brochure_title      VARCHAR(200),
    brochure_content    TEXT,
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_channel_univ UNIQUE (channel_code, university_id, year)
);

CREATE INDEX idx_scu_channel ON t_special_channel_university(channel_code) WHERE is_active = TRUE;
CREATE INDEX idx_scu_region ON t_special_channel_university(channel_code, region_tag) WHERE is_active = TRUE;
```

### 3. t_strong_base_score（强基计划入围/录取数据表）

```sql
CREATE TABLE IF NOT EXISTS t_strong_base_score (
    id                      BIGINT          PRIMARY KEY,
    university_id           BIGINT          NOT NULL,
    university_name         VARCHAR(50)     NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    subject_type            VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    major_code              VARCHAR(20),
    entry_score             NUMERIC(7,2),
    entry_score_type        VARCHAR(30)     DEFAULT '高考成绩',
    entry_formula           VARCHAR(500),
    entry_ratio             VARCHAR(20),
    admission_score         NUMERIC(7,2),
    admission_formula       VARCHAR(500)    DEFAULT '高考成绩×85%+校测成绩×15%',
    plan_count              INTEGER,
    admission_count         INTEGER,
    remark                  VARCHAR(500),
    is_active               BOOLEAN         DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_strong_base UNIQUE (university_id, year, province, subject_type, major_name)
);

CREATE INDEX idx_sbs_univ_year ON t_strong_base_score(university_id, year DESC);
CREATE INDEX idx_sbs_province ON t_strong_base_score(province, year DESC, subject_type);
```

**entry_score_type枚举值**：
- `高考成绩` - 直接用高考分
- `加权成绩` - 高考重点科目×1.2+其他
- `校测初试` - 高考前校测（复旦等）

### 4. t_strong_base_university（强基计划院校配置表）

```sql
CREATE TABLE IF NOT EXISTS t_strong_base_university (
    id                      BIGINT          PRIMARY KEY,
    university_id           BIGINT          NOT NULL UNIQUE,
    university_name         VARCHAR(50)     NOT NULL,
    is_pilot                BOOLEAN         DEFAULT TRUE,
    pilot_year              SMALLINT,
    official_url            VARCHAR(500),
    signup_url              VARCHAR(500),
    test_before_score       BOOLEAN         DEFAULT FALSE,
    default_entry_ratio     VARCHAR(20)     DEFAULT '1:5',
    default_admission_formula VARCHAR(500)  DEFAULT '高考成绩×85%+校测成绩×15%',
    available_majors        TEXT[],
    special_notes           TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sbu_pilot ON t_strong_base_university(is_pilot);
```

---

## 代码结构

### 包结构

```
haifeng-common/
├── entity/special/
│   ├── SpecialChannel.java
│   ├── SpecialChannelUniversity.java
│   ├── StrongBaseScore.java
│   └── StrongBaseUniversity.java
└── mapper/special/
    ├── SpecialChannelMapper.java
    ├── SpecialChannelUniversityMapper.java
    ├── StrongBaseScoreMapper.java
    └── StrongBaseUniversityMapper.java

haifeng-admin/
├── controller/special/
│   ├── SpecialChannelController.java
│   ├── SpecialChannelUnivController.java
│   ├── StrongBaseScoreController.java
│   └── StrongBaseUnivController.java
├── service/special/
│   ├── SpecialChannelService.java
│   ├── SpecialChannelUnivService.java
│   ├── StrongBaseScoreService.java
│   └── StrongBaseUnivService.java
├── service/impl/special/
│   ├── SpecialChannelServiceImpl.java
│   ├── SpecialChannelUnivServiceImpl.java
│   ├── StrongBaseScoreServiceImpl.java
│   └── StrongBaseUnivServiceImpl.java
├── dto/special/
│   ├── SpecialChannelQueryDTO.java
│   ├── SpecialChannelAddDTO.java
│   ├── SpecialChannelBatchDeleteDTO.java
│   ├── SpecialChannelUnivQueryDTO.java
│   ├── SpecialChannelUnivAddDTO.java
│   ├── SpecialChannelUnivBatchDeleteDTO.java
│   ├── StrongBaseScoreQueryDTO.java
│   ├── StrongBaseScoreAddDTO.java
│   ├── StrongBaseScoreBatchDeleteDTO.java
│   ├── StrongBaseUnivQueryDTO.java
│   ├── StrongBaseUnivAddDTO.java
│   └── StrongBaseUnivBatchDeleteDTO.java
└── vo/special/
    ├── SpecialChannelListVO.java
    ├── SpecialChannelDetailVO.java
    ├── SpecialChannelUnivListVO.java
    ├── SpecialChannelUnivDetailVO.java
    ├── StrongBaseScoreListVO.java
    ├── StrongBaseScoreDetailVO.java
    ├── StrongBaseUnivListVO.java
    └── StrongBaseUnivDetailVO.java
```

---

## API设计

### 1. 特殊招生通道 `/api/v1/admin/special/channel`

| 方法 | 路径 | 说明 | 查询条件 |
|------|------|------|----------|
| GET | /page | 分页查询 | displayType(精准)、channelName(模糊) |
| GET | /{id} | 详情 | - |
| POST | / | 新增 | - |
| PUT | /{id} | 修改 | - |
| PUT | /{id}/toggle | 切换状态 | - |
| DELETE | /{id} | 删除(硬) | - |
| DELETE | /batch | 批量删除 | - |

**列表字段**：id, channelCode, channelName, displayType, isActive

### 2. 通道-大学关联 `/api/v1/admin/special/channel-univ`

| 方法 | 路径 | 说明 | 查询条件 |
|------|------|------|----------|
| GET | /page | 分页查询 | 无 |
| GET | /{id} | 详情 | - |
| POST | / | 新增 | - |
| PUT | /{id} | 修改 | - |
| PUT | /{id}/toggle | 切换状态 | - |
| DELETE | /{id} | 删除(硬) | - |
| DELETE | /batch | 批量删除 | - |

**列表字段**：id, channelName, universityName, year, regionTag, isActive

### 3. 强基计划数据 `/api/v1/admin/special/strong-base-score`

| 方法 | 路径 | 说明 | 查询条件 |
|------|------|------|----------|
| GET | /page | 分页查询 | universityName, year, province, subjectType(均精准) |
| GET | /{id} | 详情 | - |
| POST | / | 新增 | - |
| PUT | /{id} | 修改 | - |
| PUT | /{id}/toggle | 切换状态 | - |
| DELETE | /{id} | 删除(硬) | - |
| DELETE | /batch | 批量删除 | - |

**列表字段**：id, universityName, year, province, subjectType, isActive

### 4. 强基院校配置 `/api/v1/admin/special/strong-base-univ`

| 方法 | 路径 | 说明 | 查询条件 |
|------|------|------|----------|
| GET | /page | 分页查询 | universityName, isPilot, pilotYear, testBeforeScore(均精准) |
| GET | /{id} | 详情 | - |
| POST | / | 新增 | - |
| PUT | /{id} | 修改 | - |
| DELETE | /{id} | 删除(硬) | - |
| DELETE | /batch | 批量删除 | - |

**列表字段**：id, universityName, isPilot, pilotYear, testBeforeScore
**注意**：无禁用功能

---

## 业务规则

### 唯一性约束

| 表 | 唯一约束 |
|----|----------|
| t_special_channel | channel_code |
| t_special_channel_university | (channel_code, university_id, year) |
| t_strong_base_score | (university_id, year, province, subject_type, major_name) |
| t_strong_base_university | university_id |

### 删除策略

所有表均为**硬删除**，直接从数据库移除记录。

### 操作日志

所有写操作（新增/修改/删除/切换状态）通过 `@OperationLog` 注解自动记录。

---

## 文件清单

### Flyway迁移
- `V14__t_special_channel__tables.sql`

### Entity (haifeng-common)
- `entity/special/SpecialChannel.java`
- `entity/special/SpecialChannelUniversity.java`
- `entity/special/StrongBaseScore.java`
- `entity/special/StrongBaseUniversity.java`

### Mapper (haifeng-common)
- `mapper/special/SpecialChannelMapper.java`
- `mapper/special/SpecialChannelUniversityMapper.java`
- `mapper/special/StrongBaseScoreMapper.java`
- `mapper/special/StrongBaseUniversityMapper.java`

### Controller (haifeng-admin)
- `controller/special/SpecialChannelController.java`
- `controller/special/SpecialChannelUnivController.java`
- `controller/special/StrongBaseScoreController.java`
- `controller/special/StrongBaseUnivController.java`

### Service (haifeng-admin)
- `service/special/SpecialChannelService.java`
- `service/special/SpecialChannelUnivService.java`
- `service/special/StrongBaseScoreService.java`
- `service/special/StrongBaseUnivService.java`
- `service/impl/special/SpecialChannelServiceImpl.java`
- `service/impl/special/SpecialChannelUnivServiceImpl.java`
- `service/impl/special/StrongBaseScoreServiceImpl.java`
- `service/impl/special/StrongBaseUnivServiceImpl.java`

### DTO (haifeng-admin)
- `dto/special/SpecialChannelQueryDTO.java`
- `dto/special/SpecialChannelAddDTO.java`
- `dto/special/SpecialChannelBatchDeleteDTO.java`
- `dto/special/SpecialChannelUnivQueryDTO.java`
- `dto/special/SpecialChannelUnivAddDTO.java`
- `dto/special/SpecialChannelUnivBatchDeleteDTO.java`
- `dto/special/StrongBaseScoreQueryDTO.java`
- `dto/special/StrongBaseScoreAddDTO.java`
- `dto/special/StrongBaseScoreBatchDeleteDTO.java`
- `dto/special/StrongBaseUnivQueryDTO.java`
- `dto/special/StrongBaseUnivAddDTO.java`
- `dto/special/StrongBaseUnivBatchDeleteDTO.java`

### VO (haifeng-admin)
- `vo/special/SpecialChannelListVO.java`
- `vo/special/SpecialChannelDetailVO.java`
- `vo/special/SpecialChannelUnivListVO.java`
- `vo/special/SpecialChannelUnivDetailVO.java`
- `vo/special/StrongBaseScoreListVO.java`
- `vo/special/StrongBaseScoreDetailVO.java`
- `vo/special/StrongBaseUnivListVO.java`
- `vo/special/StrongBaseUnivDetailVO.java`
