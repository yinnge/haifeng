# C 端企业管理 API 文档（企业列表 / 企业岗位 / 企业↔行业双向关联）

## 功能概述

本模块实现 C 端企业管理 4 个只读接口。访问权限按子功能分级：企业列表完全公开；企业岗位需要登录；企业↔行业双向关联需要 Pro 及以上会员。所有接口不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 企业列表 | 企业分页列表（名称模糊 + 性质/类型/城市/招聘状态精准） | 公开访问 |
| 企业岗位 | 按企业 id 查询岗位列表 | 登录用户 |
| 企业 → 行业 | 按企业 id 批量查询关联行业（用于跳转） | Pro 及以上 |
| 行业 → 企业 | 按行业 id 批量查询关联企业（用于跳转） | Pro 及以上 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，无需 Token |
| 登录用户 | 需携带有效 Access Token；由 `@RequireLogin` 切面校验 |
| Pro 及以上 | 需 `member_type ∈ {pro, vip}`；由 `@RequirePro` 切面校验（已隐含登录） |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1717392000000
}
```

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | 字段级校验失败；`enterpriseIds` / `industryIds` 为空或全部为 null |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 |
| 403 | 无权限 | 普通用户访问 Pro 接口 |
| 404 | 资源不存在 | 企业不存在或已删除 |
| 500 | 服务器内部错误 | 未预期异常 |

### 分页参数（BasePageQueryDTO）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–1000 |

### 数据可见性规则

- 企业、岗位均按 `is_deleted = false` 过滤；中间表无逻辑删除字段，直接查询。
- 企业岗位接口校验企业必须存在且未删除，否则返回 404 "企业不存在"。
- 中间表接口直接使用 `t_enterprise_industry` 冗余的 `industry_name` / `enterprise_name`，不联查主表，避免主表删除导致跳转信息查询失败。
- 中间表接口对入参做去重，重复 ID 只生成一个分组；无关联数据的 ID 返回空数组。

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `enterprise_name` → `enterpriseName`）
- `TEXT[]` 字段（如 `position_tags`）通过 `JacksonTypeHandler` 双向转换为 `List<String>`
- `TIMESTAMPTZ` 字段（如 `deadline`）以 ISO-8601 带时区字符串返回
- 雪花算法生成的 ID 在 JSON 中以数字返回（注意前端 long 精度）

---

## 1. 企业列表

**功能描述**：分页查询企业，支持名称模糊查询 + 4 个字段精准筛选（AND 组合）。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/enterprise/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **enterpriseName** | String | 否 | **模糊（LIKE %enterpriseName%）** | 企业名称 |
| **enterpriseNature** | String | 否 | **精准（=）** | 企业性质（央企/国企/民企/外企/合资） |
| **enterpriseType** | String | 否 | **精准（=）** | 企业类型 |
| **cityName** | String | 否 | **精准（=）** | 城市名称 |
| **recruitmentStatus** | String | 否 | **精准（=）** | 招聘状态（招聘中/已结束 等） |

> 多个筛选条件传入时按 AND 组合（同时满足）；空参数视为不参与筛选。

### 排序规则

`id ASC`

### 请求示例

```http
GET /api/v1/app/enterprise/list?page=1&size=10&enterpriseName=华为&enterpriseNature=民企&cityName=深圳
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1001,
        "cityName": "深圳",
        "enterpriseName": "华为技术有限公司",
        "enterpriseNature": "民企",
        "enterpriseType": "科技",
        "logoUrl": "https://cdn.example.com/enterprise/1001.png",
        "officialWebsite": "https://www.huawei.com",
        "region": "华南",
        "enterpriseScale": "10万人以上",
        "mainBusiness": "通信设备、消费电子、企业业务",
        "enterpriseIntro": "华为是全球领先的 ICT 基础设施和智能终端提供商……"
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

### 响应字段（VO）

**EnterpriseListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 企业 ID（用于跳转岗位/关联行业） |
| cityName | String | 城市名称 |
| enterpriseName | String | 企业名称 |
| enterpriseNature | String | 企业性质 |
| enterpriseType | String | 企业类型 |
| logoUrl | String | Logo 图片 URL |
| officialWebsite | String | 企业官网 |
| region | String | 总部地区 |
| enterpriseScale | String | 企业规模 |
| mainBusiness | String | 主营业务 |
| enterpriseIntro | String | 企业简介 |

> `id` 字段供前端跳转岗位列表与关联行业接口时用。

---

## 2. 企业岗位列表

**功能描述**：根据企业 ID 查询该企业当前所有岗位。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/enterprise/{enterpriseId}/positions` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| enterpriseId | Path | Long | 是 | **精准（=）** | 企业主表 ID |

### 行为说明

1. 先查 `t_enterprise`，校验 `id = enterpriseId` 且 `is_deleted = false`
2. 不存在或已删除 → 404 "企业不存在"
3. 查询 `t_enterprise_position`，条件 `enterprise_id = enterpriseId AND is_deleted = false`，排序 `id ASC`
4. 无岗位时返回空数组（不报错）

### 请求示例

```http
GET /api/v1/app/enterprise/1001/positions
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "positionName": "后端开发工程师",
      "recruitmentType": "校招",
      "positionRequirement": "熟悉 Java 后端开发，了解分布式系统……",
      "positionTags": ["Java", "Spring", "MySQL"],
      "province": "广东",
      "city": "深圳",
      "workLocation": "南山区华为坂田基地",
      "educationRequirement": "本科",
      "majorRequirement": "计算机类",
      "workExperience": "不限",
      "salaryMin": 15,
      "salaryMax": 25,
      "applyLink": "https://career.huawei.com/apply/1001-1",
      "deadline": "2026-07-01T00:00:00+08:00",
      "positionStatus": "招聘中"
    }
  ],
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**EnterprisePositionVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| positionName | String | 岗位名称 |
| recruitmentType | String | 招聘类型（校招/社招/实习） |
| positionRequirement | String | 岗位要求详细描述 |
| positionTags | List\<String\> | 岗位标签 |
| province | String | 省份 |
| city | String | 城市 |
| workLocation | String | 详细工作地点 |
| educationRequirement | String | 学历要求（不限/大专/本科/硕士/博士） |
| majorRequirement | String | 专业要求 |
| workExperience | String | 工作经验要求 |
| salaryMin | Integer | 最低月薪（单位：k） |
| salaryMax | Integer | 最高月薪（单位：k） |
| applyLink | String | 申请链接 |
| deadline | OffsetDateTime | 报名截止日期（ISO-8601 带时区） |
| positionStatus | String | 岗位状态（招聘中/已结束） |

> 列表本身不返回岗位 `id` 与 `enterpriseId`，因为本接口不提供岗位详情跳转。

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| 企业不存在或已删除 | 404 | 企业不存在 |

---

## 3. 企业 → 行业（批量跳转信息）

**功能描述**：根据一组企业 ID，批量查询每个企业关联的行业列表，用于企业列表页对应企业行业标签的跳转。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/enterprise/industries` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| **enterpriseIds** | List\<Long\> | 是 | **批量（IN）** | 企业 ID 列表，逗号分隔或重复参数 |

> 入参会被去重；空、null、全部为 null 时返回 400 "企业ID列表不能为空"。

### 行为说明

1. 校验 `enterpriseIds`：过滤 null + 去重，结果为空 → 400 "企业ID列表不能为空"
2. 查询 `t_enterprise_industry`，条件 `enterprise_id IN (去重后 ids)`
3. 排序：`enterprise_id ASC, sort_order ASC, id ASC`
4. 按请求传入的去重顺序生成分组；某个企业无关联行业 → `industries: []`
5. 直接使用中间表冗余字段 `industry_id` / `industry_name`，不联查 `t_industry`

### 请求示例

```http
GET /api/v1/app/enterprise/industries?enterpriseIds=1001,1002,1003
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

或重复参数：

```http
GET /api/v1/app/enterprise/industries?enterpriseIds=1001&enterpriseIds=1002&enterpriseIds=1003
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "enterpriseId": 1001,
      "industries": [
        { "industryId": 10, "industryName": "人工智能" },
        { "industryId": 11, "industryName": "通信设备" }
      ]
    },
    {
      "enterpriseId": 1002,
      "industries": [
        { "industryId": 12, "industryName": "互联网" }
      ]
    },
    {
      "enterpriseId": 1003,
      "industries": []
    }
  ],
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**EnterpriseIndustryGroupVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| enterpriseId | Long | 企业 ID |
| industries | List\<IndustryJumpVO\> | 该企业关联的行业列表，可能为空数组 |

**IndustryJumpVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| industryId | Long | 行业 ID（前端据此跳转行业详情） |
| industryName | String | 行业名称 |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户 | 403 | 权限不足（需要专业版及以上） |
| `enterpriseIds` 空 / 全部为 null | 400 | 企业ID列表不能为空 |

---

## 4. 行业 → 企业（批量跳转信息）

**功能描述**：根据一组行业 ID，批量查询每个行业关联的企业列表，用于行业列表页对应行业相关企业的跳转。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/industry/enterprises` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| **industryIds** | List\<Long\> | 是 | **批量（IN）** | 行业 ID 列表，逗号分隔或重复参数 |

> 入参会被去重；空、null、全部为 null 时返回 400 "行业ID列表不能为空"。

### 行为说明

1. 校验 `industryIds`：过滤 null + 去重，结果为空 → 400 "行业ID列表不能为空"
2. 查询 `t_enterprise_industry`，条件 `industry_id IN (去重后 ids)`
3. 排序：`industry_id ASC, sort_order ASC, id ASC`
4. 按请求传入的去重顺序生成分组；某个行业无关联企业 → `enterprises: []`
5. 直接使用中间表冗余字段 `enterprise_id` / `enterprise_name`，不联查 `t_enterprise`

### 请求示例

```http
GET /api/v1/app/industry/enterprises?industryIds=10,11,12
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "industryId": 10,
      "enterprises": [
        { "enterpriseId": 1001, "enterpriseName": "华为技术有限公司" },
        { "enterpriseId": 1003, "enterpriseName": "腾讯科技" }
      ]
    },
    {
      "industryId": 11,
      "enterprises": [
        { "enterpriseId": 1001, "enterpriseName": "华为技术有限公司" }
      ]
    },
    {
      "industryId": 12,
      "enterprises": []
    }
  ],
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**IndustryEnterpriseGroupVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| industryId | Long | 行业 ID |
| enterprises | List\<EnterpriseJumpVO\> | 该行业关联的企业列表，可能为空数组 |

**EnterpriseJumpVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| enterpriseId | Long | 企业 ID（前端据此跳转企业岗位/详情） |
| enterpriseName | String | 企业名称 |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户 | 403 | 权限不足（需要专业版及以上） |
| `industryIds` 空 / 全部为 null | 400 | 行业ID列表不能为空 |

---

## 模糊查询 vs 精准查询字段总览

| 接口 | 模糊查询字段 | 精准查询字段 |
|------|---------------|---------------|
| 1. 企业列表 | `enterpriseName` | `enterpriseNature`、`enterpriseType`、`cityName`、`recruitmentStatus` |
| 2. 企业岗位 | — | `enterpriseId`（path） |
| 3. 企业 → 行业 | — | `enterpriseIds`（query，批量 IN，去重） |
| 4. 行业 → 企业 | — | `industryIds`（query，批量 IN，去重） |

> 全模块只有 1 个模糊查询字段（企业名称），其余均为精准匹配或批量 IN。

---

## 接口路径速查

```
GET  /api/v1/app/enterprise/list                              [公开]   企业列表（enterpriseName 模糊 + 性质/类型/城市/招聘状态 精准）
GET  /api/v1/app/enterprise/{enterpriseId}/positions          [登录]   企业岗位列表
GET  /api/v1/app/enterprise/industries?enterpriseIds=...      [Pro]    企业 → 行业（批量跳转信息）
GET  /api/v1/app/industry/enterprises?industryIds=...         [Pro]    行业 → 企业（批量跳转信息）
```
