# 算法配置管理模块

## 模块概述

本模块实现高考志愿填报算法所需的基础配置数据管理，属于「算法配置管理」父模块，包含三个子模块：

| 子模块 | 说明 |
|--------|------|
| 省份改革配置 | 管理各省份高考改革年份和模式（3+3/3+1+2/传统文理） |
| 一分一段位次 | 管理各省份各年份的分数-位次对照表，用于查询考生位次 |
| 批次分数线 | 管理各省份各年份各批次的省控分数线 |

## 数据表

### 1. t_province_reform（省份高考改革配置表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| province | VARCHAR(20) | 是 | 省份（唯一） |
| reform_year | SMALLINT | 否 | 新高考首届年份（NULL=尚未改革） |
| reform_model | VARCHAR(20) | 否 | 改革模式（3+1+2 / 3+3 / 传统文理） |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |

### 2. t_score_rank（一分一段位次表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| province | VARCHAR(20) | 是 | 省份 |
| year | SMALLINT | 是 | 年份 |
| subject_type | VARCHAR(20) | 是 | 科类（物理类/历史类/文科/理科/不分文理） |
| score | SMALLINT | 是 | 分数 |
| rank | INTEGER | 是 | 位次 |
| same_score_count | INTEGER | 否 | 同分人数 |
| cumulative_count | INTEGER | 否 | 累计人数 |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |

**唯一约束**：province + year + subject_type + score

### 3. t_batch_score_line（批次分数线表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| province | VARCHAR(20) | 是 | 省份 |
| year | SMALLINT | 是 | 年份 |
| subject_type | VARCHAR(20) | 是 | 科类 |
| batch | VARCHAR(50) | 是 | 批次名称（本科批/提前批/专科批） |
| score_line | INTEGER | 是 | 省控分数线 |
| rank_line | INTEGER | 否 | 省控线对应位次 |
| remark | VARCHAR(200) | 否 | 备注 |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |

**唯一约束**：province + year + subject_type + batch

---

## 一、省份改革配置接口

基础路径：`/api/v1/admin/algorithm/config/province-reform`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增配置 |
| PUT | `/{id}` | 修改配置 |
| DELETE | `/{id}` | 删除配置（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

### 1.1 分页查询列表

**请求**
```
GET /api/v1/admin/algorithm/config/province-reform/page?page=1&size=10
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |

> **注意**：此接口无查询条件，返回全部省份配置

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1890000000000001",
        "province": "上海",
        "reformYear": 2017,
        "reformModel": "3+3"
      },
      {
        "id": "1890000000000030",
        "province": "西藏",
        "reformYear": null,
        "reformModel": "传统文理"
      }
    ],
    "total": 31,
    "size": 10,
    "current": 1,
    "pages": 4
  },
  "timestamp": 1715241600000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID（雪花算法，前端用字符串避免精度丢失） |
| province | String | 省份 |
| reformYear | Integer \| null | 改革年份，未改革为null |
| reformModel | String \| null | 改革模式 |

### 1.2 获取详情

**请求**
```
GET /api/v1/admin/algorithm/config/province-reform/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1890000000000001",
    "province": "上海",
    "reformYear": 2017,
    "reformModel": "3+3",
    "createdAt": "2026-05-09T10:00:00+08:00"
  },
  "timestamp": 1715241600000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| province | String | 省份 |
| reformYear | Integer \| null | 改革年份 |
| reformModel | String \| null | 改革模式 |
| createdAt | String | 创建时间（ISO 8601格式） |

### 1.3 新增配置

**请求**
```
POST /api/v1/admin/algorithm/config/province-reform
Content-Type: application/json

{
  "province": "香港",
  "reformYear": 2021,
  "reformModel": "3+1+2"
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| province | String | 是 | 最大20字符，唯一 | 省份 |
| reformYear | Integer | 否 | - | 改革年份 |
| reformModel | String | 否 | 最大20字符 | 改革模式 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1890000000000032",
  "timestamp": 1715241600000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该省份配置已存在",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.4 修改配置

**请求**
```
PUT /api/v1/admin/algorithm/config/province-reform/{id}
Content-Type: application/json

{
  "province": "香港",
  "reformYear": 2021,
  "reformModel": "3+1+2"
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| province | String | 是 | 最大20字符，唯一 | 省份 |
| reformYear | Integer | 否 | - | 改革年份 |
| reformModel | String | 否 | 最大20字符 | 改革模式 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.5 删除配置

**请求**
```
DELETE /api/v1/admin/algorithm/config/province-reform/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.6 批量删除

**请求**
```
DELETE /api/v1/admin/algorithm/config/province-reform/batch
Content-Type: application/json

["1890000000000001", "1890000000000002"]
```

**请求体**
| 类型 | 说明 |
|------|------|
| Array\<String\> | ID数组 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

---

## 二、一分一段位次接口

基础路径：`/api/v1/admin/algorithm/config/score-rank`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增记录 |
| PUT | `/{id}` | 修改记录 |
| DELETE | `/{id}` | 删除记录（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |
| POST | `/import` | Excel批量导入 |

### 2.1 分页查询列表

**请求**
```
GET /api/v1/admin/algorithm/config/score-rank/page?page=1&size=10&province=广东&year=2025&subjectType=物理类
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10 |
| province | String | 否 | **精确查询** | 省份 |
| year | Integer | 否 | **精确查询** | 年份 |
| subjectType | String | 否 | **精确查询** | 科类 |
| score | Integer | 否 | **精确查询** | 分数 |
| rank | Integer | 否 | **精确查询** | 位次 |

> **注意**：所有查询条件均为**精确匹配**，不支持模糊查询

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1891000000000001",
        "province": "广东",
        "year": 2025,
        "subjectType": "物理类",
        "score": 680,
        "rank": 1500
      }
    ],
    "total": 1000,
    "size": 10,
    "current": 1,
    "pages": 100
  },
  "timestamp": 1715241600000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| province | String | 省份 |
| year | Integer | 年份 |
| subjectType | String | 科类 |
| score | Integer | 分数 |
| rank | Integer | 位次 |

### 2.2 获取详情

**请求**
```
GET /api/v1/admin/algorithm/config/score-rank/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1891000000000001",
    "province": "广东",
    "year": 2025,
    "subjectType": "物理类",
    "score": 680,
    "rank": 1500,
    "sameScoreCount": 50,
    "cumulativeCount": 1550,
    "createdAt": "2026-05-09T10:00:00+08:00"
  },
  "timestamp": 1715241600000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| province | String | 省份 |
| year | Integer | 年份 |
| subjectType | String | 科类 |
| score | Integer | 分数 |
| rank | Integer | 位次 |
| sameScoreCount | Integer \| null | 同分人数 |
| cumulativeCount | Integer \| null | 累计人数 |
| createdAt | String | 创建时间（ISO 8601格式） |

### 2.3 新增记录

**请求**
```
POST /api/v1/admin/algorithm/config/score-rank
Content-Type: application/json

{
  "province": "广东",
  "year": 2025,
  "subjectType": "物理类",
  "score": 680,
  "rank": 1500,
  "sameScoreCount": 50,
  "cumulativeCount": 1550
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| province | String | 是 | 最大20字符 | 省份 |
| year | Integer | 是 | - | 年份 |
| subjectType | String | 是 | 最大20字符 | 科类 |
| score | Integer | 是 | - | 分数 |
| rank | Integer | 是 | - | 位次 |
| sameScoreCount | Integer | 否 | - | 同分人数 |
| cumulativeCount | Integer | 否 | - | 累计人数 |

**唯一约束**：province + year + subjectType + score

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1891000000000001",
  "timestamp": 1715241600000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该分数段记录已存在",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.4 修改记录

**请求**
```
PUT /api/v1/admin/algorithm/config/score-rank/{id}
Content-Type: application/json

{
  "province": "广东",
  "year": 2025,
  "subjectType": "物理类",
  "score": 680,
  "rank": 1500,
  "sameScoreCount": 50,
  "cumulativeCount": 1550
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.5 删除记录

**请求**
```
DELETE /api/v1/admin/algorithm/config/score-rank/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.6 批量删除

**请求**
```
DELETE /api/v1/admin/algorithm/config/score-rank/batch
Content-Type: application/json

["1891000000000001", "1891000000000002"]
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.7 Excel批量导入

**请求**
```
POST /api/v1/admin/algorithm/config/score-rank/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**Excel模板列**
| 列名 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 省份 | String | 是 | 省份名称 |
| 年份 | Integer | 是 | 年份 |
| 科类 | String | 是 | 科类（物理类/历史类/文科/理科/不分文理） |
| 分数 | Integer | 是 | 分数 |
| 位次 | Integer | 是 | 位次 |
| 同分人数 | Integer | 否 | 同分人数 |
| 累计人数 | Integer | 否 | 累计人数 |

**Excel示例**
| 省份 | 年份 | 科类 | 分数 | 位次 | 同分人数 | 累计人数 |
|------|------|------|------|------|----------|----------|
| 广东 | 2025 | 物理类 | 750 | 1 | 1 | 1 |
| 广东 | 2025 | 物理类 | 749 | 2 | 3 | 4 |
| 广东 | 2025 | 物理类 | 748 | 5 | 5 | 9 |

**成功响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

**校验错误响应**
```json
{
  "code": 400,
  "msg": "数据校验失败：第2行: 省份不能为空; 第5行: 数据库已存在该记录（省份=广东, 年份=2025, 科类=物理类, 分数=680）",
  "data": null,
  "timestamp": 1715241600000
}
```

**导入规则**：
1. 所有必填字段不能为空
2. Excel内不能有重复记录（省份+年份+科类+分数）
3. 数据库不能存在相同记录，否则**拒绝导入**
4. 任何错误都会导致整个导入回滚

---

## 三、批次分数线接口

基础路径：`/api/v1/admin/algorithm/config/batch-score-line`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增记录 |
| PUT | `/{id}` | 修改记录 |
| DELETE | `/{id}` | 删除记录（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |
| POST | `/import` | Excel批量导入 |

### 3.1 分页查询列表

**请求**
```
GET /api/v1/admin/algorithm/config/batch-score-line/page?page=1&size=10&province=广东&year=2025
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10 |
| province | String | 否 | **精确查询** | 省份 |
| year | Integer | 否 | **精确查询** | 年份 |
| subjectType | String | 否 | **精确查询** | 科类 |
| batch | String | 否 | **精确查询** | 批次 |
| scoreLine | Integer | 否 | **精确查询** | 分数线 |

> **注意**：所有查询条件均为**精确匹配**，不支持模糊查询

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1892000000000001",
        "province": "广东",
        "year": 2025,
        "subjectType": "物理类",
        "batch": "本科批",
        "scoreLine": 445
      }
    ],
    "total": 500,
    "size": 10,
    "current": 1,
    "pages": 50
  },
  "timestamp": 1715241600000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| province | String | 省份 |
| year | Integer | 年份 |
| subjectType | String | 科类 |
| batch | String | 批次 |
| scoreLine | Integer | 分数线 |

### 3.2 获取详情

**请求**
```
GET /api/v1/admin/algorithm/config/batch-score-line/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1892000000000001",
    "province": "广东",
    "year": 2025,
    "subjectType": "物理类",
    "batch": "本科批",
    "scoreLine": 445,
    "rankLine": 200000,
    "remark": "2025年广东物理类本科批省控线",
    "createdAt": "2026-05-09T10:00:00+08:00"
  },
  "timestamp": 1715241600000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| province | String | 省份 |
| year | Integer | 年份 |
| subjectType | String | 科类 |
| batch | String | 批次 |
| scoreLine | Integer | 分数线 |
| rankLine | Integer \| null | 分数线对应位次 |
| remark | String \| null | 备注 |
| createdAt | String | 创建时间（ISO 8601格式） |

### 3.3 新增记录

**请求**
```
POST /api/v1/admin/algorithm/config/batch-score-line
Content-Type: application/json

{
  "province": "广东",
  "year": 2025,
  "subjectType": "物理类",
  "batch": "本科批",
  "scoreLine": 445,
  "rankLine": 200000,
  "remark": "2025年广东物理类本科批省控线"
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| province | String | 是 | 最大20字符 | 省份 |
| year | Integer | 是 | - | 年份 |
| subjectType | String | 是 | 最大20字符 | 科类 |
| batch | String | 是 | 最大50字符 | 批次（本科批/提前批/专科批） |
| scoreLine | Integer | 是 | - | 分数线 |
| rankLine | Integer | 否 | - | 分数线对应位次 |
| remark | String | 否 | 最大200字符 | 备注 |

**唯一约束**：province + year + subjectType + batch

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1892000000000001",
  "timestamp": 1715241600000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该批次分数线已存在",
  "data": null,
  "timestamp": 1715241600000
}
```

### 3.4 修改记录

**请求**
```
PUT /api/v1/admin/algorithm/config/batch-score-line/{id}
Content-Type: application/json

{
  "province": "广东",
  "year": 2025,
  "subjectType": "物理类",
  "batch": "本科批",
  "scoreLine": 450,
  "rankLine": 195000,
  "remark": "更新后的省控线"
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 3.5 删除记录

**请求**
```
DELETE /api/v1/admin/algorithm/config/batch-score-line/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 3.6 批量删除

**请求**
```
DELETE /api/v1/admin/algorithm/config/batch-score-line/batch
Content-Type: application/json

["1892000000000001", "1892000000000002"]
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 3.7 Excel批量导入

**请求**
```
POST /api/v1/admin/algorithm/config/batch-score-line/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**Excel模板列**
| 列名 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 省份 | String | 是 | 省份名称 |
| 年份 | Integer | 是 | 年份 |
| 科类 | String | 是 | 科类（物理类/历史类/文科/理科/不分文理） |
| 批次 | String | 是 | 批次（本科批/提前批/专科批） |
| 分数线 | Integer | 是 | 省控分数线 |
| 位次线 | Integer | 否 | 分数线对应位次 |
| 备注 | String | 否 | 备注说明 |

**Excel示例**
| 省份 | 年份 | 科类 | 批次 | 分数线 | 位次线 | 备注 |
|------|------|------|------|--------|--------|------|
| 广东 | 2025 | 物理类 | 本科批 | 445 | 200000 | |
| 广东 | 2025 | 物理类 | 专科批 | 180 | 380000 | |
| 广东 | 2025 | 历史类 | 本科批 | 430 | 85000 | |

**成功响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

**校验错误响应**
```json
{
  "code": 400,
  "msg": "数据校验失败：第3行: 批次不能为空; 第6行: 数据库已存在该记录（省份=广东, 年份=2025, 科类=物理类, 批次=本科批）",
  "data": null,
  "timestamp": 1715241600000
}
```

**导入规则**：
1. 所有必填字段不能为空
2. Excel内不能有重复记录（省份+年份+科类+批次）
3. 数据库不能存在相同记录，否则**拒绝导入**
4. 任何错误都会导致整个导入回滚

---

## 四、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 数据校验失败 / 数据已存在 |
| 404 | 记录不存在 |
| 401 | 未登录 / Token过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 五、字段类型规范

为确保前后端数据一致性，所有接口遵循以下类型规范：

| 后端类型 | 前端类型 | 说明 |
|----------|----------|------|
| Long（ID） | String | 避免JavaScript精度丢失 |
| Short/Integer | Number | 数值类型 |
| String | String | 字符串 |
| OffsetDateTime | String | ISO 8601格式，如 `2026-05-09T10:00:00+08:00` |
| null | null | 空值保持null，不转为空字符串 |

> **重要**：所有ID字段（如 `id`）返回为**字符串类型**，前端请求时也应传字符串。
