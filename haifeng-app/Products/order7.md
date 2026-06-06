# C 端院校管理 API 文档（院校列表 / 详情 / 适应指南 / 校园图册）

## 功能概述

本模块实现 C 端院校管理 4 类只读展示接口。访问权限按子功能分级：列表完全公开；详情、图册、5 个适应指南分类需要登录；适应指南"学业规划类"需要 Pro 及以上会员。所有接口不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 院校列表 | 院校分页列表（多条件筛选 + 名称模糊） | 公开访问 |
| 院校详情 | 院校详情（联表 t_universities + t_universities_detail） | 登录用户 |
| 适应指南 · 概览 | 自定义标签 + 院校简要信息 | 登录用户 |
| 适应指南 · 基础生存类 | 校园设施、宿舍、交通 | 登录用户 |
| 适应指南 · 学业规划类 | 学业指导、转专业、学习资源 | **Pro 及以上** |
| 适应指南 · 社交融入类 | 社团、活动、班级宿舍 | 登录用户 |
| 适应指南 · 权益与安全类 | 资助、安全、医疗 | 登录用户 |
| 适应指南 · 周边生活类 | 周边生活服务 | 登录用户 |
| 校园图册 | 按院校分页查询图册（可按类型筛选） | 登录用户 |

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

### 公共分页参数（所有列表接口）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–1000（推荐档位：10/20/30/50/100） |

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | `page < 1` / `size < 10` / `size > 1000` 等字段级校验失败 |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 / 访问需登录接口 |
| 403 | 无权限 | 普通用户访问 academic 接口 |
| 404 | 资源不存在 | universityId 不存在 / 院校已下架（status=0）/ 详情未配置 / 指南未配置 |
| 500 | 服务器内部错误 | 未预期异常 |

### 数据可见性规则

- 所有数据均按 `status = 1` 过滤（status=0 视为下架/未发布）
- 院校相关接口任一上游记录缺失或下架均返回 404，错误信息差异化（"院校不存在"、"院校详情不存在"、"院校适应指南不存在"）
- JSONB 字段（指南各分类的 Map 字段）原样透传，前端按 key 自由渲染

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `province_name` → `provinceName`）
- `TIMESTAMPTZ` 序列化为 ISO-8601 带时区字符串
- `TEXT[]`、JSONB 字段通过 `JacksonTypeHandler` 双向转换
- 雪花算法生成的 ID 在 JSON 中以字符串/数字均可（注意前端 long 精度，必要时按字符串接收）

---

## 1. 院校列表

**功能描述**：分页查询院校，支持名称模糊查询 + 7 个字段精准筛选（AND 组合）。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **name** | String | 否 | **模糊（LIKE %name%）** | 院校名称模糊匹配 |
| provinceName | String | 否 | **精准（=）** | 省份名（如 "北京"、"浙江"） |
| nature | String | 否 | **精准（=）** | 办学性质（如 "公办"、"民办"、"中外合作"） |
| category | String | 否 | **精准（=）** | 院校类型（如 "综合"、"理工"、"师范"） |
| department | String | 否 | **精准（=）** | 主管部门（如 "教育部"、"工业和信息化部"） |
| educationLevel | String | 否 | **精准（=）** | 学历层次（如 "本科"、"专科"） |
| hasDoctorate | Boolean | 否 | **精准（=）** | 是否有博士点 |
| hasMaster | Boolean | 否 | **精准（=）** | 是否有硕士点 |

> 多个筛选条件传入时按 AND 组合（同时满足）；空参数视为不参与筛选。

### 排序规则

`sort_order ASC, id DESC`

### 请求示例

```http
GET /api/v1/app/university/list?page=1&size=10&name=清华&provinceName=北京&nature=公办&hasDoctorate=true
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
        "name": "清华大学",
        "tags": ["985", "211", "双一流"],
        "cityName": "北京",
        "educationLevel": "本科",
        "provinceName": "北京",
        "introduction": "清华大学是中国著名高等学府……",
        "imageUrl": "https://cdn.example.com/univ/1001.jpg",
        "nature": "公办",
        "category": "综合",
        "majorCount": 120,
        "hasDoctorate": true,
        "hasMaster": true,
        "department": "教育部"
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

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 院校 ID（用于跳转详情/图册/指南） |
| name | String | 院校名称 |
| tags | String[] | 院校标签 |
| cityName | String | 所在城市 |
| educationLevel | String | 学历层次 |
| provinceName | String | 所在省份 |
| introduction | String | 院校简介（来自主表） |
| imageUrl | String | 院校封面图 URL |
| nature | String | 办学性质 |
| category | String | 院校类型 |
| majorCount | Integer | 专业数量 |
| hasDoctorate | Boolean | 是否有博士点 |
| hasMaster | Boolean | 是否有硕士点 |
| department | String | 主管部门 |

---

## 2. 院校详情

**功能描述**：联表查询 `t_universities` 主表与 `t_universities_detail` 详情表，返回完整院校信息。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/{universityId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| universityId | Long | 是 | **精准（=）** | 路径变量，院校主表 ID |

### 请求示例

```http
GET /api/v1/app/university/1001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "address": "北京市海淀区清华园 1 号",
    "admissionPhone": "010-62770334",
    "website": "https://www.tsinghua.edu.cn",
    "historyGroupScore": 670,
    "scienceGroupScore": 685,
    "carouselImages": [
      "https://cdn.example.com/univ/1001/1.jpg",
      "https://cdn.example.com/univ/1001/2.jpg"
    ],
    "introduction": "清华大学详细介绍（详情表，更完整）……",
    "rankings": { "QS": 17, "THE": 12, "ARWU": 22 },
    "abroadRate": "30%",
    "genderRatio": "6:4",
    "name": "清华大学",
    "nameEn": "Tsinghua University",
    "provinceName": "北京",
    "cityName": "北京",
    "region": "华北",
    "category": "综合",
    "majorCount": 120,
    "educationLevel": "本科",
    "nature": "公办",
    "recommendationRate": 95.50,
    "recommendationYear": 2025,
    "hasDoctorate": true,
    "hasMaster": true,
    "department": "教育部",
    "tags": ["985", "211", "双一流"],
    "famousUnion": "C9 联盟"
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

#### 来自 `t_universities_detail`

| 字段 | 类型 | 说明 |
|------|------|------|
| address | String | 校址 |
| admissionPhone | String | 招生电话 |
| website | String | 官网 URL |
| historyGroupScore | Integer | 历史组录取分 |
| scienceGroupScore | Integer | 物理组录取分 |
| carouselImages | String[] | 详情页轮播图 URL |
| introduction | String | 详细介绍（以详情表为准） |
| rankings | Map<String,Integer> | 各排行榜名次（如 `{"QS":17,...}`） |
| abroadRate | String | 出国比例 |
| genderRatio | String | 男女比例 |

#### 来自 `t_universities`

| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 院校名称 |
| nameEn | String | 英文名 |
| provinceName | String | 省份 |
| cityName | String | 城市 |
| region | String | 大区（华北/华东等） |
| category | String | 院校类型 |
| majorCount | Integer | 专业数量 |
| educationLevel | String | 学历层次 |
| nature | String | 办学性质 |
| recommendationRate | BigDecimal | 推荐率 |
| recommendationYear | Integer | 推荐年份 |
| hasDoctorate | Boolean | 是否有博士点 |
| hasMaster | Boolean | 是否有硕士点 |
| department | String | 主管部门 |
| tags | String[] | 标签 |
| famousUnion | String | 所属联盟（如 C9） |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| universityId 不存在 / status=0 | 404 | 院校不存在 |
| 详情表未配置 | 404 | 院校详情不存在 |

---

## 3. 适应指南（6 个分类子接口）

**功能描述**：将 `t_university_guides` 表的 14 个 JSONB 字段按业务语义拆为 6 个子接口暴露。除 academic 需 Pro 外其余均需登录。

### 通用约定（适用于全部 6 个子接口）

| 项 | 值 |
|----|----|
| URL 前缀 | `/api/v1/app/university/guides/{universityId}` |
| 路径变量 universityId | Long，**精准（=）** 匹配 `t_university_guides.university_id` |
| 数据筛选 | 仅返回 `status = 1` |
| 404 触发 | 该院校没有指南配置 → `院校适应指南不存在`；overview 接口额外检查院校本身存在 → `院校不存在` |
| JSONB 字段类型 | `Map<String, Object>`，原样透传 |

### 3.1 概览（overview）

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/guides/{universityId}/overview` |
| 权限 | 登录用户 |
| 说明 | 自定义标签 + 院校简要信息（联 `t_universities`） |

**响应字段**：

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| customTags | String[] | t_university_guides.custom_tags | 指南自定义标签 |
| name | String | t_universities.name | 院校名称 |
| tags | String[] | t_universities.tags | 院校标签 |
| region | String | t_universities.region | 大区 |
| category | String | t_universities.category | 院校类型 |
| nature | String | t_universities.nature | 办学性质 |
| imageUrl | String | t_universities.image_url | 院校封面图 |

**响应示例**：

```json
{
  "code": 200, "msg": "success",
  "data": {
    "customTags": ["好食堂", "图书馆爆款", "校园美如画"],
    "name": "清华大学",
    "tags": ["985", "211", "双一流"],
    "region": "华北",
    "category": "综合",
    "nature": "公办",
    "imageUrl": "https://cdn.example.com/univ/1001.jpg"
  },
  "timestamp": 1717392000000
}
```

### 3.2 基础生存类（survival）

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/guides/{universityId}/survival` |
| 权限 | 登录用户 |

| 字段 | 类型 | 说明 |
|------|------|------|
| campusFacilities | Map<String,Object> | 校园设施 JSONB |
| dormitoryServices | Map<String,Object> | 宿舍服务 JSONB |
| campusTransportation | Map<String,Object> | 校园交通 JSONB |

### 3.3 学业规划类（academic）— **需 Pro**

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/guides/{universityId}/academic` |
| 权限 | **Pro 及以上（`@RequirePro`）** |

| 字段 | 类型 | 说明 |
|------|------|------|
| academicGuidance | Map<String,Object> | 学业指导 JSONB |
| majorTransferGuidelines | Map<String,Object> | 转专业指南 JSONB |
| majorTransferConstriction | Map<String,Object> | 转专业限制 JSONB |
| academicSupportResources | Map<String,Object> | 学习支持资源 JSONB |

> 普通用户调用此接口返回 `403, "权限不足（需要专业版及以上）"`。

### 3.4 社交融入类（social）

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/guides/{universityId}/social` |
| 权限 | 登录用户 |

| 字段 | 类型 | 说明 |
|------|------|------|
| studentOrganizations | Map<String,Object> | 学生组织 JSONB |
| campusEvents | Map<String,Object> | 校园活动 JSONB |
| classDormSocial | Map<String,Object> | 班级宿舍社交 JSONB |

### 3.5 权益与安全类（safety）

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/guides/{universityId}/safety` |
| 权限 | 登录用户 |

| 字段 | 类型 | 说明 |
|------|------|------|
| financialAid | Map<String,Object> | 经济资助 JSONB |
| campusSecurity | Map<String,Object> | 校园安全 JSONB |
| healthServices | Map<String,Object> | 医疗健康 JSONB |

### 3.6 周边生活类（life）

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/guides/{universityId}/life` |
| 权限 | 登录用户 |

| 字段 | 类型 | 说明 |
|------|------|------|
| lifeServices | Map<String,Object> | 周边生活服务 JSONB |

### 指南统一错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| academic 接口普通用户访问 | 403 | 权限不足（需要专业版及以上） |
| overview 接口 universityId 不存在或下架 | 404 | 院校不存在 |
| 其余 5 接口该院校未配置指南 | 404 | 院校适应指南不存在 |

---

## 4. 校园图册

**功能描述**：按院校 ID 分页查询其校园图册图片，可按图片类型精准筛选。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/university/{universityId}/gallery` |
| 权限 | 登录用户（`@RequireLogin`） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| universityId | Path | Long | 是 | **精准（=）** | 院校 ID（限定该院校图册） |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |
| imageType | Query | String | 否 | **精准（=）** | 图片类型（如 "校门"、"教学楼"、"图书馆"、"宿舍"），不传则返回全部类型 |

### 排序规则

`sort_order ASC, id DESC`

### 请求示例

```http
GET /api/v1/app/university/1001/gallery?page=1&size=20&imageType=校门
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
        "imageType": "校门",
        "imageUrl": "https://cdn.example.com/univ/1001/gate-01.jpg"
      },
      {
        "imageType": "校门",
        "imageUrl": "https://cdn.example.com/univ/1001/gate-02.jpg"
      }
    ],
    "total": 2,
    "size": 20,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

| 字段 | 类型 | 说明 |
|------|------|------|
| imageType | String | 图片类型 |
| imageUrl | String | 图片 URL |

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 参数校验失败（如 size<10） | 400 | 字段级校验信息 |

---

## 模糊查询 vs 精准查询字段总览

| 接口 | 模糊查询字段 | 精准查询字段 |
|------|---------------|---------------|
| 1. 院校列表 | `name` | `provinceName, nature, category, department, educationLevel, hasDoctorate, hasMaster` |
| 2. 院校详情 | — | `universityId`（path） |
| 3.1–3.6 指南 6 接口 | — | `universityId`（path） |
| 4. 校园图册 | — | `universityId`（path）、`imageType` |

> 全模块只有 1 个模糊查询字段（院校名称），其余均为精准匹配；多筛选字段同时传入按 AND 组合。

---

## 接口路径速查

```
GET  /api/v1/app/university/list                              [公开]   院校列表（多筛选 + name 模糊）
GET  /api/v1/app/university/{id}/detail                       [登录]   院校详情（联表）
GET  /api/v1/app/university/guides/{id}/overview              [登录]   指南 · 概览
GET  /api/v1/app/university/guides/{id}/survival              [登录]   指南 · 基础生存类
GET  /api/v1/app/university/guides/{id}/academic              [Pro]    指南 · 学业规划类
GET  /api/v1/app/university/guides/{id}/social                [登录]   指南 · 社交融入类
GET  /api/v1/app/university/guides/{id}/safety                [登录]   指南 · 权益与安全类
GET  /api/v1/app/university/guides/{id}/life                  [登录]   指南 · 周边生活类
GET  /api/v1/app/university/{id}/gallery                      [登录]   校园图册（按 imageType 筛选）
```
