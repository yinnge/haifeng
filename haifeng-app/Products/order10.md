# C 端城市/行业/资源管理 API 文档（城市列表·详情 / 行业列表·详情 / 资源列表·URL）

## 功能概述

本模块实现 C 端城市管理、行业管理、资源管理 3 个只读展示模块，共 8 个接口。列表及分类列表接口无需登录即可访问；城市详情、行业详情、资源 URL 需要登录。所有接口不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 城市列表 | 城市分页列表（名称模糊 + 省份/地区精准筛选） | 公开访问 |
| 城市详情 | 关联 t_city_detail 返回完整城市信息 | 登录用户 |
| 行业列表 | 行业分页列表（分类精准筛选） | 公开访问 |
| 行业详情 | 关联 t_industry_detail 返回完整行业信息 | 登录用户 |
| **行业分类列表** | **获取所有不重复的行业分类** | **公开访问** |
| 资源列表 | 资源分页列表（分类精准筛选） | 公开访问 |
| 资源分类列表 | 获取所有不重复的资源分类 | 公开访问 |
| 查看资源 URL | 返回资源链接和提取码，同步浏览计数 +1 | 登录用户 |

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

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | `page < 1` / `size < 10` / `size > 100` 等字段级校验失败 |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 / 访问需登录接口 |
| 404 | 资源不存在 | 城市/行业/资源不存在或已删除 |
| 500 | 服务器内部错误 | 未预期异常 |

### 分页参数（BasePageQueryDTO）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–100 |

### 数据可见性规则

- 所有数据均按 `is_deleted = false` 过滤（is_deleted=true 视为已删除）
- 城市详情：t_city_detail 中 city_id 对应记录不存在 → 404 "城市详情不存在"
- 行业详情：t_industry_detail 中 industry_id 对应记录不存在 → 404 "行业详情不存在"
- 资源 URL：资源不存在或已删除 → 404 "资源不存在"

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `city_name` → `cityName`）
- JSONB 字段通过 `JacksonTypeHandler` 透传，由前端渲染
- `TEXT[]` 字段通过 `JacksonTypeHandler` 双向转换为 `List<String>`
- 雪花算法生成的 ID 在 JSON 中以数字返回（注意前端 long 精度）

---

## 1. 城市列表

**功能描述**：分页查询城市，支持名称模糊查询 + 省份/地区精准筛选（AND 组合）。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/city/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **cityName** | String | 否 | **模糊（LIKE %cityName%）** | 城市名称 |
| **province** | String | 否 | **精准（=）** | 省份（如 "北京"、"浙江"） |
| **region** | String | 否 | **精准（=）** | 地区（如 "华北"、"华东"） |

> 多个筛选条件传入时按 AND 组合（同时满足）；空参数视为不参与筛选。

### 排序规则

`id ASC`

### 请求示例

```http
GET /api/v1/app/city/list?page=1&size=10&cityName=杭州&province=浙江
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
        "cityName": "杭州市",
        "province": "浙江",
        "region": "华东",
        "cityIntro": "浙江省省会，长三角中心城市……",
        "collegeCount": 47,
        "keyCollegeCount": 8,
        "residentPopulation": 1220.30,
        "gdp": 18753.00
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

**CityListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 城市 ID（用于跳转详情） |
| cityName | String | 城市名称 |
| province | String | 省份 |
| region | String | 地区 |
| cityIntro | String | 城市简介 |
| collegeCount | Integer | 高校数量 |
| keyCollegeCount | Integer | 重点高校数量 |
| residentPopulation | BigDecimal | 常住人口（万人） |
| gdp | BigDecimal | GDP（亿元） |

> `id` 字段供前端跳转详情时用，UI 上通常不渲染。

---

## 2. 城市详情

**功能描述**：根据城市 ID 关联 `t_city_detail` 表查询城市完整信息。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/city/{cityId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| cityId | Long | 是 | **精准（=）** | 路径变量，城市主表 ID |

### 请求示例

```http
GET /api/v1/app/city/1001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "cityName": "杭州市",
    "area": 16853.57,
    "subtitle": "数字经济之都",
    "cityLevel": "省会城市",
    "adminCode": "330100",
    "perCapitaGdp": 15.37,
    "urbanizationRate": 83.50,
    "ruralPopRatio": 16.50,
    "agingRate": 18.20,
    "migrantPopRatio": 25.30,
    "gdpGrowthRate": 5.60,
    "fortune500Count": 8,
    "industryStructure": { "primary": 2.1, "secondary": 30.5, "tertiary": 67.4 },
    "industryDescription": "杭州以数字经济为核心……",
    "mainIndustries": ["互联网", "电子商务", "金融服务"],
    "emergingIndustries": ["人工智能", "生物医药", "新能源"],
    "futurePlan": { "focus": "数字经济", "targetYear": 2030 },
    "highEducation": { "totalUniversities": 47, "keyUniversities": 8 },
    "basicEducation": { "primarySchools": 500, "middleSchools": 300 },
    "enterpriseStats": { "totalEnterprises": 50000, "techEnterprises": 12000 },
    "housingPriceLevel": { "average": 35000, "level": "高" },
    "rentalCost": { "average": 3500, "level": "中高" },
    "housingPolicy": { "talentHousing": "有", "subsidy": "本科1万元/年" },
    "consumption": { "averageMonthly": 5000, "level": "中高" },
    "employment": { "rate": 95.5, "hotIndustries": ["互联网", "金融"] },
    "transportation": { "metroLines": 12, "airports": 1 },
    "medical": { "grade3Hospitals": 30, "communityClinics": 500 },
    "culture": { "museums": 50, "theaters": 20 }
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

#### 来自 `t_city_detail`

| 字段 | 类型 | 说明 | 枚举/约束 |
|------|------|------|-----------|
| cityName | String | 城市名称 | — |
| area | BigDecimal | 面积（km²） | — |
| subtitle | String | 副标题 | — |
| cityLevel | String | 城市级别 | **枚举：直辖市 / 省会城市 / 地级市 / 县级市** |
| adminCode | String | 行政区划代码 | — |
| perCapitaGdp | BigDecimal | 人均 GDP（万元） | — |
| urbanizationRate | BigDecimal | 城镇化率（%） | **范围：0–100** |
| ruralPopRatio | BigDecimal | 农村人口比例（%） | **范围：0–100** |
| agingRate | BigDecimal | 老龄化率（%） | **范围：0–100** |
| migrantPopRatio | BigDecimal | 流入人口比例（%） | **范围：0–100** |
| gdpGrowthRate | BigDecimal | GDP 增速（%） | — |
| fortune500Count | Integer | 世界 500 强企业数量 | — |
| industryStructure | Map\<String, Object\> | 产业结构 JSONB | 结构以 DB 实际存储为准 |
| industryDescription | String | 产业描述 | — |
| mainIndustries | List\<String\> | 主导产业 JSONB | 结构以 DB 实际存储为准 |
| emergingIndustries | List\<String\> | 新兴产业 JSONB | 结构以 DB 实际存储为准 |
| futurePlan | Map\<String, Object\> | 未来规划 JSONB | 结构以 DB 实际存储为准 |
| highEducation | Map\<String, Object\> | 高等教育 JSONB | 结构以 DB 实际存储为准 |
| basicEducation | Map\<String, Object\> | 基础教育 JSONB | 结构以 DB 实际存储为准 |
| enterpriseStats | Map\<String, Object\> | 企业统计 JSONB | 结构以 DB 实际存储为准 |
| housingPriceLevel | Map\<String, Object\> | 房价水平 JSONB | 结构以 DB 实际存储为准 |
| rentalCost | Map\<String, Object\> | 租房成本 JSONB | 结构以 DB 实际存储为准 |
| housingPolicy | Map\<String, Object\> | 住房政策 JSONB | 结构以 DB 实际存储为准 |
| consumption | Map\<String, Object\> | 消费水平 JSONB | 结构以 DB 实际存储为准 |
| employment | Map\<String, Object\> | 就业情况 JSONB | 结构以 DB 实际存储为准 |
| transportation | Map\<String, Object\> | 交通 JSONB | 结构以 DB 实际存储为准 |
| medical | Map\<String, Object\> | 医疗 JSONB | 结构以 DB 实际存储为准 |
| culture | Map\<String, Object\> | 文化 JSONB | 结构以 DB 实际存储为准 |

> 所有 JSONB 字段原样透传，前端按 key 自由渲染。

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| cityId 对应详情不存在 | 404 | 城市详情不存在 |

---

## 3. 行业列表

**功能描述**：分页查询行业，支持分类精准筛选。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/industry/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **category** | String | 否 | **精准（=）** | 行业分类（如 "信息技术"、"金融"） |

> 空参数视为不参与筛选，返回全部分类。

### 排序规则

`id ASC`

### 请求示例

```http
GET /api/v1/app/industry/list?page=1&size=10&category=信息技术
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 2001,
        "industryName": "人工智能",
        "category": "信息技术",
        "description": "人工智能是引领未来的战略性技术……",
        "annualGrowthRate": 25.50,
        "marketScale": "万亿级",
        "talentGap": "超500万",
        "investmentHeat": 92.00
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

**IndustryListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 行业 ID（用于跳转详情） |
| industryName | String | 行业名称 |
| category | String | 行业分类 |
| description | String | 行业描述 |
| annualGrowthRate | BigDecimal | 年增长率（%） |
| marketScale | String | 市场规模 |
| talentGap | String | 人才缺口 |
| investmentHeat | BigDecimal | 投资热度 |

> `id` 字段供前端跳转详情时用，UI 上通常不渲染。

---

## 4. 行业详情

**功能描述**：根据行业 ID 关联 `t_industry_detail` 表查询行业完整信息。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/industry/{industryId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| industryId | Long | 是 | **精准（=）** | 路径变量，行业主表 ID |

### 请求示例

```http
GET /api/v1/app/industry/2001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "industryName": "人工智能",
    "shortDescription": "AI 是引领未来的战略性技术……",
    "detailedDescription": "人工智能（Artificial Intelligence）是研究、开发用于模拟、延伸和扩展人的智能的理论、方法、技术及应用系统的一门新的技术科学……",
    "industryScale": { "year": 2024, "scale": "5000亿", "growth": "25%" },
    "industryTalentDemand": { "totalDemand": "500万+", "hotRoles": ["算法工程师", "数据科学家"] },
    "industrySalary": { "entry": "15-25K", "mid": "30-50K", "senior": "50-100K" },
    "policyInfo": { "nationalPlan": "新一代人工智能发展规划", "subsidies": "研发补贴" },
    "developmentSupportInfo": { "parks": "AI产业园", "incubators": "科技孵化器" },
    "talentAnalysis": { "supplyDemandRatio": "1:3", "educationRequired": "本科及以上" },
    "talentPolicy": { "housingSubsidy": "10-50万", "settlementPolicy": "人才落户" },
    "salaryData": { "nationalAvg": 25000, "tier1City": 35000 }
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

#### 来自 `t_industry_detail`

| 字段 | 类型 | 说明 |
|------|------|------|
| industryName | String | 行业名称 |
| shortDescription | String | 简要描述 |
| detailedDescription | String | 详细描述 |
| industryScale | Map\<String, Object\> | 行业规模 JSONB |
| industryTalentDemand | Map\<String, Object\> | 人才需求 JSONB |
| industrySalary | Map\<String, Object\> | 行业薪资 JSONB |
| policyInfo | Map\<String, Object\> | 政策信息 JSONB |
| developmentSupportInfo | Map\<String, Object\> | 发展支持 JSONB |
| talentAnalysis | Map\<String, Object\> | 人才分析 JSONB |
| talentPolicy | Map\<String, Object\> | 人才政策 JSONB |
| salaryData | Map\<String, Object\> | 薪资数据 JSONB |

> 所有 JSONB 字段原样透传，前端按 key 自由渲染。

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| industryId 对应详情不存在 | 404 | 行业详情不存在 |

---

## 5. 行业分类列表

**功能描述**：获取所有不重复的行业分类（category），用于前端下拉筛选。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/industry/categories` |
| 权限 | 公开 |

### 请求示例

```http
GET /api/v1/app/industry/categories
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": ["信息技术", "金融", "制造业", "医疗健康", "教育培训"],
  "timestamp": 1717392000000
}
```

### 前端使用说明

页面 `onMounted` 时调用此接口，将返回的 `data` 数组直接绑定到 `el-select` 的 `options`，无需硬编码分类选项。

---

## 6. 资源列表

**功能描述**：分页查询学习资源，支持资源名称模糊查询 + 分类精准筛选。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/resource/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **resourceName** | String | 否 | **模糊（LIKE）** | 资源名称（如 "高考"、"考研"） |
| **category** | String | 否 | **精准（=）** | 资源分类（如 "真题"、"教材"、"视频"） |

> 空参数视为不参与筛选，返回全部分类。`resourceName` 和 `category` 可同时传入，按 AND 组合。

### 排序规则

`sort_order ASC, created_at DESC`

### 请求示例

```http
GET /api/v1/app/resource/list?page=1&size=10&resourceName=高考&category=真题
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 3001,
        "resourceName": "2025 高考全国卷真题合集",
        "coverUrl": "https://cdn.example.com/resource/3001.jpg",
        "description": "包含全国 I 卷、II 卷、III 卷真题及答案解析",
        "category": "真题",
        "fileType": "PDF",
        "viewCount": 12580
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

**ResourceListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 资源 ID（用于跳转查看 URL） |
| resourceName | String | 资源名称 |
| coverUrl | String | 封面图 URL |
| description | String | 资源描述 |
| category | String | 资源分类 |
| fileType | String | 文件类型（如 PDF、MP4） |
| viewCount | Integer | 浏览次数 |

> `id` 字段供前端跳转查看资源 URL 时用。

---

## 7. 获取资源分类列表

**功能描述**：获取所有不重复的资源分类，用于前端下拉筛选。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/resource/categories` |
| 权限 | 公开 |

### 请求示例

```http
GET /api/v1/app/resource/categories
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": ["真题", "教材", "视频", "讲义", "模拟卷"],
  "timestamp": 1717392000000
}
```

### 前端使用说明

页面 `onMounted` 时调用此接口，将返回的 `data` 数组直接绑定到 `el-select` 的 `options`，无需硬编码分类选项。

---

## 8. 查看资源 URL

**功能描述**：根据资源 ID 获取资源的百度网盘链接和提取码，同时原子更新浏览计数 +1。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/resource/{id}/url` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| id | Long | 是 | **精准（=）** | 路径变量，资源主表 ID |

### 行为说明

1. 根据 id 查询 `t_resource`，校验存在且 `is_deleted = false`
2. **原子更新** `view_count = view_count + 1`（SQL 级别，并发安全）
3. 返回 `resourceUrl` 和 `accessCode`

> 如果校验失败（资源不存在或已删除），浏览计数不会 +1。

### 请求示例

```http
GET /api/v1/app/resource/3001/url
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "resourceUrl": "https://pan.baidu.com/s/1aBcDeFgHiJkLmNoPqRs",
    "accessCode": "ab12"
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**ResourceUrlVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| resourceUrl | String | 百度网盘资源链接 |
| accessCode | String | 提取码 |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| 资源不存在或已删除 | 404 | 资源不存在 |

---

## 枚举值与约束说明

以下字段在数据库层面通过 `CHECK` 约束限定取值范围，Java 代码中对应类型为 `String` 或 `BigDecimal`，不做枚举校验，由数据写入方保证合法性。

### 城市详情枚举字段（t_city_detail）

| 字段 | 类型 | 合法取值 | 说明 |
|------|------|----------|------|
| cityLevel | String | `直辖市` / `省会城市` / `地级市` / `县级市` | 城市行政级别 |

### 城市详情约束字段（t_city_detail）

| 字段 | 类型 | 约束范围 | 说明 |
|------|------|----------|------|
| urbanizationRate | BigDecimal | 0 – 100 | 城镇化率（%） |
| ruralPopRatio | BigDecimal | 0 – 100 | 农村人口比例（%） |
| agingRate | BigDecimal | 0 – 100 | 老龄化率（%） |
| migrantPopRatio | BigDecimal | 0 – 100 | 流入人口比例（%） |

### 行业主表枚举字段（t_industry）

| 字段 | 类型 | 合法取值 | 说明 |
|------|------|----------|------|
| growthTrend | String | `上升` / `稳定` / `下降` | 增长趋势 |
| marketTrend | String | `上升` / `稳定` / `下降` | 市场趋势 |
| talentTrend | String | `上升` / `稳定` / `下降` | 人才趋势 |
| investmentTrend | String | `上升` / `稳定` / `下降` | 投资趋势 |

> 以上 4 个趋势字段含义相同但维度不同，取值范围一致。

### 行业主表约束字段（t_industry）

| 字段 | 类型 | 约束范围 | 说明 |
|------|------|----------|------|
| annualGrowthRate | BigDecimal | -100 – 1000 | 年增长率（%） |
| investmentHeat | BigDecimal | 0 – 100 | 投资热度 |

---

## 模糊查询 vs 精准查询字段总览

| 接口 | 模糊查询字段 | 精准查询字段 |
|------|---------------|---------------|
| 1. 城市列表 | `cityName` | `province`、`region` |
| 2. 城市详情 | — | `cityId`（path） |
| 3. 行业列表 | — | `category` |
| 4. 行业详情 | — | `industryId`（path） |
| 5. 行业分类列表 | — | — |
| 6. 资源列表 | `resourceName` | `category` |
| 7. 资源分类列表 | — | — |
| 8. 查看资源 URL | — | `id`（path） |

> 全模块共 2 个模糊查询字段（城市名称、资源名称），其余均为精准匹配；多筛选字段同时传入按 AND 组合。

---

## 接口路径速查

```
GET  /api/v1/app/city/list                    [公开]   城市列表（cityName 模糊 + province/region 精准）
GET  /api/v1/app/city/{cityId}/detail         [登录]   城市详情（关联 t_city_detail）
GET  /api/v1/app/industry/list                [公开]   行业列表（category 精准）
GET  /api/v1/app/industry/{industryId}/detail [登录]   行业详情（关联 t_industry_detail）
GET  /api/v1/app/industry/categories          [公开]   行业分类列表（前端下拉用）
GET  /api/v1/app/resource/list                [公开]   资源列表（resourceName 模糊 + category 精准）
GET  /api/v1/app/resource/categories          [公开]   资源分类列表（前端下拉用）
GET  /api/v1/app/resource/{id}/url            [登录]   查看资源 URL + 浏览计数 +1
```
