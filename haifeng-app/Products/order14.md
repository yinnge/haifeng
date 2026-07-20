# C 端特殊通道 API 文档（通道内容 / 通道-大学 / 强基数据 / 强基院校）

## 功能概述

本模块实现 C 端特殊招生通道 4 类只读展示接口。访问权限：列表类接口完全公开；详情类接口需要登录。所有接口不加 Redis 缓存（实时读库），仅返回 `is_active = true` 的数据。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 特殊招生通道 | 通道内容分页列表 + 详情（含富文本） | 列表公开 / 详情登录 |
| 通道-大学关联 | 按通道代码查询关联大学列表 + 详情 | 列表公开 / 详情登录 |
| 强基计划数据 | 强基入围/录取数据分页列表 + 详情 | 列表公开 / 详情登录 |
| 强基计划院校 | 强基院校配置详情 | 登录用户 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，无需 Token |
| 登录用户 | 需携带有效 Access Token；由 `@RequireLogin` 切面校验 |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1717392000000
}
```

### 公共分页参数（所有列表接口）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–100（推荐档位：10/20/30/50/100） |

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | 字段级校验失败（如 `channelCode` 为空、`regionTag` 不合法、`size < 10`） |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 / 访问需登录接口 |
| 404 | 资源不存在 | 查询的 ID 不存在 / 数据已禁用（`is_active = false`） |
| 500 | 服务器内部错误 | 未预期异常 |

### 数据可见性规则

- 所有数据均按 `is_active = true` 过滤（`is_active = false` / `NULL` 视为已禁用）
- 详情接口对 `is_active = false` 的记录返回 404
- `TEXT[]`（如 `available_majors`）通过 `JacksonTypeHandler` 双向转换
- `TIMESTAMPTZ` 序列化为 ISO-8601 带时区字符串
- 雪花算法生成的 ID 在 JSON 中以字符串/数字均可

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `channel_code` → `channelCode`）
- `regionTag` 字段使用 `ProvinceEnum` 进行合法性校验，需传入中文省份名（如 "香港"、"澳门"）
- 范围查询：`signupStart` / `signupEnd` 以 ISO-8601 字符串传入

---

## 1. 特殊招生通道内容

### 1.1 通道列表

**功能描述**：分页查询所有活跃的特殊招生通道，仅展示概要字段。无需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/channel/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

#### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| displayType | String | 否 | **精准（=）** | 展示类型：`UNIVERSITY_LIST` / `ARTICLE_ONLY` / `MAJOR_DATA` / `GROUP` |
| channelName | String | 否 | **模糊（LIKE %name%）** | 通道名称模糊匹配 |

#### 排序规则

`sort_order ASC, id DESC`

#### 请求示例

```http
GET /api/v1/app/special/channel/list?page=1&size=10&displayType=UNIVERSITY_LIST&channelName=强基
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 2001,
        "channelCode": "QIANGLIJIHUA",
        "channelName": "强基计划",
        "subtitle": "基础学科招生改革试点",
        "filterLabel": "39 所试点高校",
        "displayType": "UNIVERSITY_LIST"
      },
      {
        "id": 2002,
        "channelCode": "ZONGHECESHU",
        "channelName": "综合评价",
        "subtitle": "多元评价、择优录取",
        "filterLabel": "上海纽约大学等",
        "displayType": "UNIVERSITY_LIST"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 通道 ID（用于跳转详情） |
| channelCode | String | 通道代码（唯一） |
| channelName | String | 通道名称 |
| subtitle | String | 副标题 |
| filterLabel | String | 筛选按钮文字 |
| displayType | String | 展示类型 |

---

### 1.2 通道详情

**功能描述**：查询指定通道的完整内容，含富文本正文。需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/channel/{id}` |
| 权限 | 登录用户（`@RequireLogin`） |

#### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| id | Path | Long | 是 | **精准（=）** | 通道 ID |

#### 请求示例

```http
GET /api/v1/app/special/channel/2001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 2001,
    "channelCode": "QIANGLIJIHUA",
    "channelName": "强基计划",
    "subtitle": "基础学科招生改革试点",
    "filterLabel": "39 所试点高校",
    "displayType": "UNIVERSITY_LIST",
    "content": "<h2>强基计划简介</h2><p>强基计划是……</p>"
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 通道 ID |
| channelCode | String | 通道代码 |
| channelName | String | 通道名称 |
| subtitle | String | 副标题 |
| filterLabel | String | 筛选按钮文字 |
| displayType | String | 展示类型 |
| content | String | 富文本内容（HTML） |

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| id 不存在 / is_active=false | 404 | 特殊通道不存在 |

---

## 2. 通道-大学关联

### 2.1 关联大学列表

**功能描述**：按通道代码分页查询该通道关联的大学列表。无需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/channel-univ/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

#### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **channelCode** | String | **是** | **精准（=）** | 通道代码 |
| channelName | String | 否 | **模糊（LIKE %name%）** | 通道名称模糊匹配 |
| regionTag | String | 否 | **精准（=）** | 地区标签（如 "香港"、"澳门"），需为合法省份名 |
| signupStart | OffsetDateTime | 否 | **范围（>=）** | 报名开始时间晚于某天 |
| signupEnd | OffsetDateTime | 否 | **范围（<=）** | 报名截止时间早于某天 |

> `regionTag` 参数值需通过 `ProvinceEnum` 校验，不合法返回 400。
> 范围查询逻辑：传入 `signupStart` → `signup_start >= ?`，传入 `signupEnd` → `signup_end <= ?`，两者可同时传入。

#### 排序规则

`sort_order ASC, id DESC`

#### 请求示例

```http
GET /api/v1/app/special/channel-univ/list?channelCode=HONGKONG_ZHAOSHENG&page=1&size=10&regionTag=香港
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "universityId": 3001,
        "universityName": "香港大学",
        "year": 2025,
        "regionTag": "香港",
        "signupStart": "2025-03-01T00:00:00+08:00",
        "signupEnd": "2025-06-30T23:59:59+08:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 大学 ID |
| universityName | String | 大学名称 |
| year | Short | 招生年份 |
| regionTag | String | 地区标签（如 "香港"、"澳门"） |
| signupStart | OffsetDateTime | 报名开始时间（ISO-8601 带时区） |
| signupEnd | OffsetDateTime | 报名截止时间（ISO-8601 带时区） |

---

### 2.2 关联大学详情

**功能描述**：查询通道-大学关联的完整信息，含报名官网、简章等。需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/channel-univ/{id}` |
| 权限 | 登录用户（`@RequireLogin`） |

#### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| id | Path | Long | 是 | **精准（=）** | 通道-大学关联记录 ID |

#### 请求示例

```http
GET /api/v1/app/special/channel-univ/3001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 3001,
    "channelCode": "HONGKONG_ZHAOSHENG",
    "channelName": "香港高校招生",
    "universityId": 1001,
    "universityName": "香港大学",
    "year": 2025,
    "regionTag": "香港",
    "signupStart": "2025-03-01T00:00:00+08:00",
    "signupEnd": "2025-06-30T23:59:59+08:00",
    "officialUrl": "https://www.hku.hk/admissions",
    "brochureTitle": "香港大学 2025 年招生简章",
    "brochureContent": "<h2>招生简章</h2><p>……</p>"
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 关联记录 ID |
| channelCode | String | 通道代码 |
| channelName | String | 通道名称 |
| universityId | Long | 大学 ID |
| universityName | String | 大学名称 |
| year | Short | 招生年份 |
| regionTag | String | 地区标签 |
| signupStart | OffsetDateTime | 报名开始时间 |
| signupEnd | OffsetDateTime | 报名截止时间 |
| officialUrl | String | 报名官网 URL |
| brochureTitle | String | 简章标题 |
| brochureContent | String | 简章正文（HTML） |

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| id 不存在 / is_active=false | 404 | 通道-大学关联不存在 |
| 参数校验失败（channelCode 为空） | 400 | 字段级校验信息 |

---

## 3. 强基计划数据

### 3.1 强基数据列表

**功能描述**：分页查询强基计划入围/录取数据。无需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/strong-base-score/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

#### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| year | Short | 否 | **精准（=）** | 招生年份 |
| province | String | 否 | **精准（=）** | 省份，需为合法省份名 |
| subjectType | String | 否 | **精准（=）** | 科类（如 "物理类"、"历史类"、"理科"、"文科"、"综合改革"） |
| entryScoreType | String | 否 | **精准（=）** | 入围分数类型（如 "高考成绩"、"加权成绩"、"校测初试"） |
| universityName | String | 否 | **模糊（LIKE %name%）** | 大学名称 |
| majorName | String | 否 | **模糊（LIKE %name%）** | 专业名称 |
| majorCode | String | 否 | **模糊（LIKE %code%）** | 专业代码 |

> `province` 参数值需通过 `ProvinceEnum` 校验，不合法返回 400。

#### 排序规则

`year DESC, id DESC`

#### 请求示例

```http
GET /api/v1/app/special/strong-base-score/list?page=1&size=10&year=2025&province=北京&subjectType=物理类
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 4001,
        "universityId": 1001,
        "universityName": "清华大学",
        "year": 2025,
        "province": "北京",
        "subjectType": "物理类",
        "majorName": "数学与应用数学",
        "majorCode": "070101",
        "entryScore": 670.00,
        "entryScoreType": "高考成绩",
        "entryRatio": "1:5",
        "admissionScore": 85.50,
        "planCount": 3,
        "admissionCount": 3
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据 ID（用于跳转详情） |
| universityId | Long | 大学 ID |
| universityName | String | 大学名称 |
| year | Short | 年份 |
| province | String | 省份 |
| subjectType | String | 科类 |
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| entryScore | BigDecimal | 入围分数线 |
| entryScoreType | String | 入围分数类型 |
| entryRatio | String | 入围比例（如 "1:5"） |
| admissionScore | BigDecimal | 录取综合分 |
| planCount | Integer | 招生计划数 |
| admissionCount | Integer | 实际录取人数 |

---

### 3.2 强基数据详情

**功能描述**：查询强基入围/录取数据的完整信息，含计算公式和备注。需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/strong-base-score/{id}` |
| 权限 | 登录用户（`@RequireLogin`） |

#### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| id | Path | Long | 是 | **精准（=）** | 数据 ID |

#### 请求示例

```http
GET /api/v1/app/special/strong-base-score/4001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 4001,
    "universityId": 1001,
    "universityName": "清华大学",
    "year": 2025,
    "province": "北京",
    "subjectType": "物理类",
    "majorName": "数学与应用数学",
    "majorCode": "070101",
    "entryScore": 670.00,
    "entryScoreType": "高考成绩",
    "entryFormula": "入围成绩 = 高考文化课成绩",
    "entryRatio": "1:5",
    "admissionScore": 85.50,
    "admissionFormula": "高考成绩×85%+校测成绩×15%",
    "planCount": 3,
    "admissionCount": 3,
    "remark": "2025 年首年招生"
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 数据 ID |
| universityId | Long | 大学 ID |
| universityName | String | 大学名称 |
| year | Short | 年份 |
| province | String | 省份 |
| subjectType | String | 科类 |
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| entryScore | BigDecimal | 入围分数线 |
| entryScoreType | String | 入围分数类型 |
| entryFormula | String | 入围计算公式 |
| entryRatio | String | 入围比例 |
| admissionScore | BigDecimal | 录取综合分 |
| admissionFormula | String | 录取综合分计算公式 |
| planCount | Integer | 招生计划数 |
| admissionCount | Integer | 实际录取人数 |
| remark | String | 备注 |

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| id 不存在 / is_active=false | 404 | 强基数据不存在 |
| province 不合法 | 400 | 省份参数不合法 |

---

## 4. 强基计划院校配置

### 4.1 强基院校详情

**功能描述**：按 `university_id` 查询强基计划院校的配置信息。需登录。

#### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/special/strong-base-univ/{universityId}` |
| 权限 | 登录用户（`@RequireLogin`） |

#### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| universityId | Path | Long | 是 | **精准（=）** | 大学 ID |

#### 请求示例

```http
GET /api/v1/app/special/strong-base-univ/1001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

#### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 5001,
    "universityId": 1001,
    "universityName": "清华大学",
    "isPilot": true,
    "pilotYear": 2020,
    "officialUrl": "https://www.tsinghua.edu.cn/qiangji",
    "signupUrl": "https://bm.chsi.com.cn",
    "testBeforeScore": false,
    "defaultEntryRatio": "1:5",
    "defaultAdmissionFormula": "高考成绩×85%+校测成绩×15%",
    "availableMajors": ["数学与应用数学", "物理学", "化学", "生物科学"],
    "specialNotes": "高考出分后校测"
  },
  "timestamp": 1717392000000
}
```

#### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 配置记录 ID |
| universityId | Long | 大学 ID |
| universityName | String | 大学名称（冗余） |
| isPilot | Boolean | 是否强基试点校 |
| pilotYear | Short | 首次试点年份 |
| officialUrl | String | 强基计划官方页面 URL |
| signupUrl | String | 报名入口 URL |
| testBeforeScore | Boolean | 是否高考出分前校测 |
| defaultEntryRatio | String | 默认入围比例 |
| defaultAdmissionFormula | String | 默认录取综合分公式 |
| availableMajors | String[] | 可选专业列表（TEXT[]） |
| specialNotes | String | 特殊说明 |

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| universityId 未配置强基 | 404 | 强基院校配置不存在 |

---

## 模糊查询 vs 精准查询字段总览

| 接口 | 模糊查询字段 | 精准查询字段 |
|------|---------------|---------------|
| 1.1 通道列表 | `channelName` | `displayType` |
| 1.2 通道详情 | — | `id`（path） |
| 2.1 关联大学列表 | `channelName` | `channelCode`、`regionTag`、`signupStart`（>=）、`signupEnd`（<=） |
| 2.2 关联大学详情 | — | `id`（path） |
| 3.1 强基数据列表 | `universityName`、`majorName`、`majorCode` | `year`、`province`、`subjectType`、`entryScoreType` |
| 3.2 强基数据详情 | — | `id`（path） |
| 4.1 强基院校详情 | — | `universityId`（path） |

> 多筛选字段同时传入按 AND 组合。范围查询期间（`signupStart` / `signupEnd`）为可选，传入则追加对应条件。

---

## 接口路径速查

```
GET  /api/v1/app/special/channel/list                        [公开]   通道列表（按 displayType + channelName 筛选）
GET  /api/v1/app/special/channel/{id}                        [登录]   通道详情（含 content）
GET  /api/v1/app/special/channel-univ/list                   [公开]   关联大学列表（按 channelCode + regionTag 筛选）
GET  /api/v1/app/special/channel-univ/{id}                   [登录]   关联大学详情（含 officialUrl / brochure）
GET  /api/v1/app/special/strong-base-score/list              [公开]   强基数据列表（多条件筛选）
GET  /api/v1/app/special/strong-base-score/{id}              [登录]   强基数据详情（含 formula / remark）
GET  /api/v1/app/special/strong-base-univ/{universityId}     [登录]   强基院校配置详情
```
