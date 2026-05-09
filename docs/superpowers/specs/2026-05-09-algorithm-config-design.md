# 算法配置管理模块设计文档

## 概述

实现算法配置管理父模块下的三个子模块：
- 省份高考改革配置
- 一分一段位次
- 批次分数线

所有模块仅限管理员访问，支持分页展示、增删改查、硬删除、批量删除。一分一段和批次分数线额外支持Excel导入。

## 1. 数据库设计

### 1.1 Flyway迁移文件

文件：`V12__algorithm_config.sql`

### 1.2 表结构

#### t_province_reform（省份高考改革配置）

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键（雪花算法） |
| province | VARCHAR(20) | NOT NULL UNIQUE | 省份 |
| reform_year | SMALLINT | | 新高考首届年份（NULL=尚未改革） |
| reform_model | VARCHAR(20) | | 改革模式（3+1+2 / 3+3 / 传统文理） |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | 创建时间 |

#### t_score_rank（一分一段位次）

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键（雪花算法） |
| province | VARCHAR(20) | NOT NULL | 省份 |
| year | SMALLINT | NOT NULL | 年份 |
| subject_type | VARCHAR(20) | NOT NULL | 科类（物理类/历史类/文科/理科/不分文理） |
| score | SMALLINT | NOT NULL | 分数 |
| rank | INTEGER | NOT NULL | 位次 |
| same_score_count | INTEGER | | 同分人数 |
| cumulative_count | INTEGER | | 累计人数 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |

**唯一约束**：`uk_score_rank (province, year, subject_type, score)`

**索引**：
- `idx_sr_lookup (province, year, subject_type, score)` - 精确查位次
- `idx_sr_rank_lookup (province, year, subject_type, rank)` - 按位次反查分数

#### t_batch_score_line（批次分数线）

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键（雪花算法） |
| province | VARCHAR(20) | NOT NULL | 省份 |
| year | SMALLINT | NOT NULL | 年份 |
| subject_type | VARCHAR(20) | NOT NULL | 科类 |
| batch | VARCHAR(50) | NOT NULL | 批次名称 |
| score_line | INTEGER | NOT NULL | 省控分数线 |
| rank_line | INTEGER | | 省控线对应位次 |
| remark | VARCHAR(200) | | 备注 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |

**唯一约束**：`uk_batch_score_line (province, year, subject_type, batch)`

**索引**：
- `idx_bsl_lookup (province, year, subject_type)` - 查询索引
- `idx_bsl_year (year DESC)` - 按年份查询

## 2. 包结构设计

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/algorithm/config/
│   ├── ProvinceReformController.java
│   ├── ScoreRankController.java
│   └── BatchScoreLineController.java
├── service/algorithm/config/
│   ├── ProvinceReformService.java
│   ├── ScoreRankService.java
│   └── BatchScoreLineService.java
├── service/impl/algorithm/config/
│   ├── ProvinceReformServiceImpl.java
│   ├── ScoreRankServiceImpl.java
│   └── BatchScoreLineServiceImpl.java
├── dto/algorithm/config/
│   ├── ProvinceReformAddDTO.java
│   ├── ProvinceReformQueryDTO.java
│   ├── ScoreRankAddDTO.java
│   ├── ScoreRankQueryDTO.java
│   ├── BatchScoreLineAddDTO.java
│   └── BatchScoreLineQueryDTO.java
├── vo/algorithm/config/
│   ├── ProvinceReformListVO.java
│   ├── ProvinceReformDetailVO.java
│   ├── ScoreRankListVO.java
│   ├── ScoreRankDetailVO.java
│   ├── BatchScoreLineListVO.java
│   └── BatchScoreLineDetailVO.java
└── excel/algorithm/config/
    ├── ScoreRankImportDTO.java
    └── BatchScoreLineImportDTO.java

haifeng-common/src/main/java/com/haifeng/common/
├── entity/algorithm/
│   ├── ProvinceReform.java
│   ├── ScoreRank.java
│   └── BatchScoreLine.java
└── mapper/algorithm/
    ├── ProvinceReformMapper.java
    ├── ScoreRankMapper.java
    └── BatchScoreLineMapper.java
```

## 3. API接口设计

### 3.1 省份高考改革配置

**基础路径**：`/api/v1/admin/algorithm/config/province-reform`

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /page | 分页列表（展示：province, reform_year, reform_model） |
| GET | /{id} | 详情（所有字段） |
| POST | / | 新增 |
| PUT | /{id} | 修改 |
| DELETE | /{id} | 删除（硬删除） |
| DELETE | /batch | 批量删除（硬删除） |

**查询条件**：无（需求明确无模糊查询功能）

### 3.2 一分一段位次

**基础路径**：`/api/v1/admin/algorithm/config/score-rank`

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /page | 分页列表（展示：province, year, subject_type, score, rank） |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| PUT | /{id} | 修改 |
| DELETE | /{id} | 删除（硬删除） |
| DELETE | /batch | 批量删除（硬删除） |
| POST | /import | Excel导入 |

**查询条件**（精准查询）：province, year, subject_type, score, rank

### 3.3 批次分数线

**基础路径**：`/api/v1/admin/algorithm/config/batch-score-line`

| 方法 | 路径 | 说明 |
|-----|------|------|
| GET | /page | 分页列表（展示：province, year, subject_type, batch, score_line） |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| PUT | /{id} | 修改 |
| DELETE | /{id} | 删除（硬删除） |
| DELETE | /batch | 批量删除（硬删除） |
| POST | /import | Excel导入 |

**查询条件**（精准查询）：province, year, subject_type, batch, score_line

## 4. Excel导入设计

### 4.1 一分一段位次导入

**DTO类**：`ScoreRankImportDTO`

| Excel列名 | 字段 | 类型 | 必填 |
|-----------|------|------|------|
| 省份 | province | String | ✓ |
| 年份 | year | Short | ✓ |
| 科类 | subjectType | String | ✓ |
| 分数 | score | Short | ✓ |
| 位次 | rank | Integer | ✓ |
| 同分人数 | sameScoreCount | Integer | |
| 累计人数 | cumulativeCount | Integer | |

### 4.2 批次分数线导入

**DTO类**：`BatchScoreLineImportDTO`

| Excel列名 | 字段 | 类型 | 必填 |
|-----------|------|------|------|
| 省份 | province | String | ✓ |
| 年份 | year | Short | ✓ |
| 科类 | subjectType | String | ✓ |
| 批次 | batch | String | ✓ |
| 分数线 | scoreLine | Integer | ✓ |
| 位次线 | rankLine | Integer | |
| 备注 | remark | String | |

### 4.3 导入校验规则

1. **表头校验**：检查必填列是否存在
2. **Excel内唯一性校验**：
   - 一分一段：同一Excel内 province+year+subject_type+score 不能重复
   - 批次分数线：同一Excel内 province+year+subject_type+batch 不能重复
3. **数据库重复检查**：导入前检查数据库是否已存在相同唯一键，存在则报错拒绝导入
4. **错误收集**：收集所有错误信息（格式：第X行: 具体错误），一次性返回
5. **事务保证**：使用 `@Transactional(rollbackFor = Exception.class)`，任何错误全部回滚

## 5. 实现要点

### 5.1 通用规范

- ID生成：使用雪花算法（SnowflakeIdGenerator）
- 操作日志：所有写操作使用 `@OperationLog` 注解
- 分页参数：支持 10, 20, 30, 50, 100, 200, 500, 1000
- 响应格式：统一使用 `R<T>` 包装

### 5.2 删除策略

三个模块均使用**硬删除**，直接从数据库删除记录。

### 5.3 省份改革配置预置数据

Flyway迁移文件需包含预置数据：
- 第一批 3+3：上海、浙江（2017）
- 第二批 3+3：北京、天津、山东、海南（2020）
- 第三批 3+1+2：广东、福建、河北等（2021）
- 第四批 3+1+2：吉林、黑龙江、安徽等（2024）
- 第五批 3+1+2：山西、河南、四川等（2025）
- 尚未改革：西藏、新疆（传统文理）

## 6. 文件清单

### 6.1 Flyway迁移

- `V12__algorithm_config.sql`

### 6.2 Entity（haifeng-common）

- `ProvinceReform.java`
- `ScoreRank.java`
- `BatchScoreLine.java`

### 6.3 Mapper（haifeng-common）

- `ProvinceReformMapper.java`
- `ScoreRankMapper.java`
- `BatchScoreLineMapper.java`

### 6.4 Controller（haifeng-admin）

- `ProvinceReformController.java`
- `ScoreRankController.java`
- `BatchScoreLineController.java`

### 6.5 Service（haifeng-admin）

- `ProvinceReformService.java` + `ProvinceReformServiceImpl.java`
- `ScoreRankService.java` + `ScoreRankServiceImpl.java`
- `BatchScoreLineService.java` + `BatchScoreLineServiceImpl.java`

### 6.6 DTO（haifeng-admin）

- `ProvinceReformAddDTO.java`
- `ProvinceReformQueryDTO.java`
- `ScoreRankAddDTO.java`
- `ScoreRankQueryDTO.java`
- `BatchScoreLineAddDTO.java`
- `BatchScoreLineQueryDTO.java`

### 6.7 VO（haifeng-admin）

- `ProvinceReformListVO.java`
- `ProvinceReformDetailVO.java`
- `ScoreRankListVO.java`
- `ScoreRankDetailVO.java`
- `BatchScoreLineListVO.java`
- `BatchScoreLineDetailVO.java`

### 6.8 Excel导入DTO（haifeng-admin）

- `ScoreRankImportDTO.java`
- `BatchScoreLineImportDTO.java`

**总计：1个SQL + 24个Java文件**
