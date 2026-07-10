# C 端专业管理 API 文档（专业列表 / 详情 / 统计 / 排行 / 考研专业 / 大学↔考研关联）

## 功能概述

本模块实现 C 端专业管理 8 类只读展示接口。访问权限按子功能分级：专业列表、分类统计完全公开；专业详情、考研专业列表/详情需要登录；薪资就业排行、大学→考研专业、考研专业→大学需要 Pro 及以上会员。所有接口不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 专业列表 | 专业分页列表（多条件筛选 + 名称/代码模糊） | 公开访问 |
| 专业详情 | 联表 t_major + t_major_detail 返回完整专业信息 | 登录用户 |
| 专业分类统计 | 按 major_category 分组统计专业数量 | 公开访问 |
| 薪资就业排行 | 按薪资/就业率动态排序的专业列表 | Pro 及以上 |
| 考研专业列表 | 考研专业分页列表（多条件筛选） | 登录用户 |
| 考研专业详情 | 考研专业完整信息 | 登录用户 |
| 大学→考研专业 | 按院校查考研专业列表（按学位类型筛选） | Pro 及以上 |
| 考研专业→大学 | 按考研专业查大学列表（按院校类型筛选） | Pro 及以上 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，无需 Token |
| 登录用户 | 需携带有效 Access Token；由 `@RequireLogin` 切面校验 |
| Pro 及以上 | 需 `member_type ∈ {pro, vip}`；由 `@RequirePro` 切面校验 |

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
| 400 | 参数错误 | 字段级校验失败 |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 |
| 403 | 无权限 | 普通用户访问 Pro 接口 |
| 404 | 资源不存在 | 专业/考研专业不存在 |
| 500 | 服务器内部错误 | 未预期异常 |

### 分页参数（BasePageQueryDTO）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–100 |

### 数据可见性规则

- 所有数据均按 `status = 1` 过滤（status=0 视为下架/未发布）
- 专业详情：t_major 不存在或 status=0 返回 404 "专业不存在"；t_major_detail 未配置返回 404 "专业详情不存在"
- 考研专业详情：不存在或 status=0 返回 404 "考研专业不存在"
- 大学→考研专业 / 考研专业→大学：上游 id 不存在时返回空分页（不返回 404）

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `major_category` → `majorCategory`）
- `TEXT[]` 字段通过 `JacksonTypeHandler` 双向转换
- 雪花算法生成的 ID 在 JSON 中以数字返回（注意前端 long 精度）

---

## 1. 专业列表

**功能描述**：分页查询专业，支持名称/代码模糊查询 + 2 个字段精准筛选（AND 组合）。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/major/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **name** | String | 否 | **模糊（LIKE %name%）** | 专业名称 |
| **code** | String | 否 | **模糊（LIKE %code%）** | 专业代码 |
| majorType | String | 否 | **精准（=）** | 专业类型 |
| majorCategory | String | 否 | **精准（=）** | 专业类别 |

> 多个筛选条件传入时按 AND 组合（同时满足）；空参数视为不参与筛选。

### 排序规则

`id DESC`

### 请求示例

```http
GET /api/v1/app/major/list?page=1&size=10&name=计算机&majorCategory=计算机类
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
        "majorCode": "080901",
        "majorName": "计算机科学与技术",
        "disciplineName": "计算机科学与技术",
        "majorCategory": "计算机类",
        "parentCategory": "工学",
        "majorTags": "热门",
        "degreeAwarded": "工学学士",
        "employmentRate": 95.50,
        "salaryMin": 8000,
        "salaryMax": 25000,
        "description": "计算机科学与技术是……"
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

**MajorListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 专业 ID |
| majorCode | String | 专业代码 |
| majorName | String | 专业名称 |
| disciplineName | String | 学科名称 |
| majorCategory | String | 专业类别 |
| parentCategory | String | 门类 |
| majorTags | String | 专业标签 |
| degreeAwarded | String | 授予学位 |
| employmentRate | BigDecimal | 就业率 |
| salaryMin | Integer | 最低薪资 |
| salaryMax | Integer | 最高薪资 |
| description | String | 简介 |

---

## 2. 专业详情

**功能描述**：联表查询 `t_major` 主表与 `t_major_detail` 详情表，返回完整专业信息。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/major/{majorId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| majorId | Long | 是 | **精准（=）** | 路径变量，专业主表 ID |

### 请求示例

```http
GET /api/v1/app/major/1001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "majorName": "计算机科学与技术",
    "majorCode": "080901",
    "disciplineName": "计算机科学与技术",
    "majorCategory": "计算机类",
    "parentCategory": "工学",
    "majorTags": "热门",
    "degreeAwarded": "工学学士",
    "employmentRate": 95.50,
    "salaryMin": 8000,
    "salaryMax": 25000,
    "description": "计算机科学与技术是……",
    "courseCount": 45,
    "graduateScale": "50000-55000人",
    "maleRatio": 72.50,
    "femaleRatio": 27.50,
    "majorDescription": "计算机科学与技术专业……",
    "trainingObjective": "培养具有良好的科学素养……",
    "trainingRequirement": "学生需要掌握……",
    "subjectRequirement": "物理必选",
    "careerProspect": "毕业生可在IT行业……",
    "mainCourses": ["数据结构", "操作系统", "计算机网络"],
    "knowledgeSkills": ["编程能力", "算法设计"]
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

#### 来自 `t_major`

| 字段 | 类型 | 说明 |
|------|------|------|
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| disciplineName | String | 学科名称 |
| majorCategory | String | 专业类别 |
| parentCategory | String | 门类 |
| majorTags | String | 专业标签 |
| degreeAwarded | String | 授予学位 |
| employmentRate | BigDecimal | 就业率 |
| salaryMin | Integer | 最低薪资 |
| salaryMax | Integer | 最高薪资 |
| description | String | 简介 |

#### 来自 `t_major_detail`

| 字段 | 类型 | 说明 |
|------|------|------|
| courseCount | Integer | 开设课程数 |
| graduateScale | String | 毕业规模 |
| maleRatio | BigDecimal | 男生比例 |
| femaleRatio | BigDecimal | 女生比例 |
| majorDescription | String | 专业描述 |
| trainingObjective | String | 培养目标 |
| trainingRequirement | String | 培养要求 |
| subjectRequirement | String | 选科要求 |
| careerProspect | String | 就业前景 |
| mainCourses | String[] | 主要课程 |
| knowledgeSkills | String[] | 知识技能 |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| majorId 不存在 / status=0 | 404 | 专业不存在 |
| 详情表未配置 | 404 | 专业详情不存在 |

---

## 3. 专业分类统计

**功能描述**：按 `major_category` 分组统计专业数量，返回各类别下的专业计数。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/major/category-stats` |
| 权限 | 公开 |

### 请求参数

无

### 排序规则

`count DESC`

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "majorCategory": "计算机类", "count": 38 },
    { "majorCategory": "电子信息类", "count": 25 },
    { "majorCategory": "数学类", "count": 12 }
  ],
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**MajorCategoryStatVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| majorCategory | String | 专业类别 |
| count | Integer | 该类下专业数量 |

---

## 4. 薪资就业排行

**功能描述**：按薪资/就业率动态排序的专业列表，支持专业名称模糊查询与类别精准筛选。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/major/ranking` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 查询方式 | 说明 |
|------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | — | 页码 |
| size | Integer | 否 | 10 | — | 每页条数 |
| **name** | String | 否 | — | **模糊（LIKE %name%）** | 专业名称 |
| majorCategory | String | 否 | — | **精准（=）** | 专业类别 |
| sortBy | String | 否 | employmentRate | **枚举** | 排序字段：employmentRate / salaryMin / salaryMax |
| sortOrder | String | 否 | desc | **枚举** | 排序方向：asc / desc |

### 排序规则

`${sortBy} ${sortOrder} NULLS LAST, id DESC`

### 请求示例

```http
GET /api/v1/app/major/ranking?page=1&size=10&sortBy=salaryMax&sortOrder=desc&majorCategory=计算机类
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
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
        "majorCode": "080901",
        "majorName": "计算机科学与技术",
        "disciplineName": "计算机科学与技术",
        "majorCategory": "计算机类",
        "parentCategory": "工学",
        "majorTags": "热门",
        "degreeAwarded": "工学学士",
        "employmentRate": 95.50,
        "salaryMin": 8000,
        "salaryMax": 25000,
        "description": "计算机科学与技术是……"
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

同 **MajorListVO**（见第 1 节）。

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户 | 403 | 权限不足（需要专业版及以上） |
| sortBy/sortOrder 非法值 | 400 | 字段级校验信息 |

---

## 5. 考研专业列表

**功能描述**：分页查询考研专业，支持名称/代码模糊查询 + 4 个字段精准筛选（AND 组合）。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/postgrad-major/list` |
| 权限 | 登录用户（`@RequireLogin`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **name** | String | 否 | **模糊（LIKE %name%）** | 考研专业名 |
| **code** | String | 否 | **模糊（LIKE %code%）** | 考研专业代码 |
| degreeType | String | 否 | **精准（=）** | 学术学位 / 专业学位 |
| disciplineCategory | String | 否 | **精准（=）** | 学科门类 |
| popularity | String | 否 | **精准（=）** | 热门 / 一般 / 冷门 |
| difficulty | String | 否 | **精准（=）** | 高 / 中 / 低 |

> 多个筛选条件传入时按 AND 组合（同时满足）；空参数视为不参与筛选。

### 排序规则

`id DESC`

### 请求示例

```http
GET /api/v1/app/postgrad-major/list?page=1&size=10&name=计算机&degreeType=学术学位
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
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
        "majorName": "计算机科学与技术",
        "majorCode": "081200",
        "degreeType": "学术学位",
        "disciplineCategory": "工学",
        "popularity": "热门",
        "difficulty": "高",
        "brief": "计算机科学与技术是……",
        "examSubjects": ["政治", "英语一", "数学一", "专业课"]
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

**PostgradMajorListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 考研专业 ID |
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| degreeType | String | 学位类型 |
| disciplineCategory | String | 学科门类 |
| popularity | String | 热门程度 |
| difficulty | String | 难度 |
| brief | String | 简介 |
| examSubjects | String[] | 考试科目 |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |

---

## 6. 考研专业详情

**功能描述**：按考研专业 ID 查询完整信息。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/postgrad-major/{majorId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| majorId | Long | 是 | **精准（=）** | 路径变量，考研专业 ID |

### 请求示例

```http
GET /api/v1/app/postgrad-major/2001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "majorName": "计算机科学与技术",
    "majorCode": "081200",
    "degreeType": "学术学位",
    "disciplineCategory": "工学",
    "popularity": "热门",
    "difficulty": "高",
    "introduction": "计算机科学与技术是研究计算机……",
    "examSubjects": ["政治", "英语一", "数学一", "专业课"],
    "admissionRequirements": ["本科毕业", "学士学位"],
    "crossExamDifficulty": "较难",
    "crossExamDescription": "跨考计算机需要……",
    "crossExamFactors": ["数学基础", "编程能力"]
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**PostgradMajorDetailVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| degreeType | String | 学位类型 |
| disciplineCategory | String | 学科门类 |
| popularity | String | 热门程度 |
| difficulty | String | 难度 |
| introduction | String | 专业介绍 |
| examSubjects | String[] | 考试科目 |
| admissionRequirements | String[] | 报考条件 |
| crossExamDifficulty | String | 跨考难度 |
| crossExamDescription | String | 跨考说明 |
| crossExamFactors | String[] | 跨考因素 |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| majorId 不存在 / status=0 | 404 | 考研专业不存在 |

---

## 7. 大学→考研专业

**功能描述**：按院校 ID 查询其招生的考研专业列表，可按学位类型精准筛选。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/{universityId}/postgrad-majors` |
| 权限 | Pro 及以上（`@RequirePro`） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| universityId | Path | Long | 是 | **精准（=）** | 院校 ID |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |
| degreeType | Query | String | 否 | **精准（=）** | 学术学位 / 专业学位 |

### 排序规则

`pmu.sort_order ASC, pm.id DESC`

### 请求示例

```http
GET /api/v1/app/university/1001/postgrad-majors?page=1&size=10&degreeType=学术学位
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 2001, "majorName": "计算机科学与技术", "degreeType": "学术学位" },
      { "id": 2002, "majorName": "软件工程", "degreeType": "专业学位" }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**PostgradMajorBriefVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 考研专业 ID（前端据此跳转考研专业详情） |
| majorName | String | 专业名称 |
| degreeType | String | 学位类型 |

> `universityId` 不存在时返回空分页，不返回 404。

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户 | 403 | 权限不足（需要专业版及以上） |

---

## 8. 考研专业→大学

**功能描述**：按考研专业 ID 查询开设该专业的大学列表，可按院校类型精准筛选。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/postgrad-major/{majorId}/universities` |
| 权限 | Pro 及以上（`@RequirePro`） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| majorId | Path | Long | 是 | **精准（=）** | 考研专业 ID |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |
| category | Query | String | 否 | **精准（=）** | 院校类型（综合/理工/师范/...） |

### 排序规则

`pmu.sort_order ASC, u.id DESC`

### 请求示例

```http
GET /api/v1/app/postgrad-major/2001/universities?page=1&size=10&category=理工
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 1001, "name": "清华大学", "category": "综合" },
      { "id": 1002, "name": "北京航空航天大学", "category": "理工" }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**UniversityBriefForPostgradVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 院校 ID（前端据此跳转院校详情） |
| name | String | 院校名称 |
| category | String | 院校类型 |

> `majorId` 不存在时返回空分页，不返回 404。

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户 | 403 | 权限不足（需要专业版及以上） |

---

## 模糊查询 vs 精准查询字段总览

| 接口 | 模糊查询字段 | 精准查询字段 |
|------|---------------|---------------|
| 1. 专业列表 | `name`, `code` | `majorType`, `majorCategory` |
| 2. 专业详情 | — | `majorId`（path） |
| 3. 专业分类统计 | — | — |
| 4. 薪资就业排行 | `name` | `majorCategory`, `sortBy`, `sortOrder` |
| 5. 考研专业列表 | `name`, `code` | `degreeType`, `disciplineCategory`, `popularity`, `difficulty` |
| 6. 考研专业详情 | — | `majorId`（path） |
| 7. 大学→考研专业 | — | `universityId`（path）、`degreeType` |
| 8. 考研专业→大学 | — | `majorId`（path）、`category` |

> 专业列表与考研专业列表各有 2 个模糊查询字段（name、code），其余均为精准匹配；多筛选字段同时传入按 AND 组合。

---

## 接口路径速查

```
GET  /api/v1/app/major/list                              [公开]   专业列表（多筛选 + name/code 模糊）
GET  /api/v1/app/major/{id}/detail                       [登录]   专业详情（联表）
GET  /api/v1/app/major/category-stats                    [公开]   专业分类统计
GET  /api/v1/app/major/ranking                           [Pro]    薪资就业排行（动态排序）
GET  /api/v1/app/postgrad-major/list                     [登录]   考研专业列表（多筛选）
GET  /api/v1/app/postgrad-major/{id}/detail              [登录]   考研专业详情
GET  /api/v1/app/university/{id}/postgrad-majors         [Pro]    大学→考研专业
GET  /api/v1/app/postgrad-major/{id}/universities        [Pro]    考研专业→大学
```
