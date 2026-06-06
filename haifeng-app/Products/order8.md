# C 端院校次模块 API 文档（实验室 / 院系 / 学科评估）

## 功能概述

本模块在院校管理父模块下提供 3 个只读子模块，共 6 个接口。所有接口均需登录，不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 实验室 | 按院校分页查询实验室列表 | 登录用户 |
| 实验室 | 按实验室 id 查询详情 | 登录用户 |
| 院系 | 按院校分页查询院系列表 | 登录用户 |
| 院系 | 按院系 id 查询分析报告 | 登录用户 |
| 学科评估 | 按院校分页查询学科评估明细 | 登录用户 |
| 学科评估 | 按院校查询 9 个等级的数量统计 | 登录用户 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
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

| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未登录或 Token 过期 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 分页参数（BasePageQueryDTO）

| 字段 | 类型 | 必填 | 默认 | 校验 |
|------|------|------|------|------|
| page | int | 否 | 1 | ≥1 |
| size | int | 否 | 10 | 10–1000 |

---

## 1. 实验室列表

`GET /api/v1/app/university/{universityId}/laboratories`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 院校 id |

**Query：** page、size（见通用说明）

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 10001, "name": "人工智能实验室", "labType": "国家重点实验室" },
      { "id": 10002, "name": "智能感知实验室", "labType": "省部级重点实验室" }
    ],
    "total": 2,
    "size": 10,
    "current": 1
  }
}
```

> `id` 字段供前端跳转详情时用，UI 上通常不渲染。

---

## 2. 实验室详情

`GET /api/v1/app/university/laboratories/{labId}`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| labId | Long | 实验室主键（从列表接口获取） |

**响应示例（节选）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "universityName": "清华大学",
    "labType": "国家重点实验室",
    "establishedYear": "1985",
    "region": "北京",
    "department": "计算机系",
    "director": "张某某",
    "staffCount": "120",
    "studentCount": "300",
    "email": "lab@xxx.edu.cn",
    "phone": "010-12345678",
    "introduction": "……",
    "researchDescription": "……",
    "labSpace": "……",
    "openTopics": "……",
    "cooperation": "……",
    "visitingScholars": "……",
    "researchFields": ["AI", "机器学习"],
    "statistics": [{"year": 2024, "papers": 42}],
    "majorEquipment": ["GPU 集群", "光谱仪"],
    "coreTeam": [{"name": "张某某", "title": "教授"}]
  }
}
```

> `researchFields`、`statistics`、`majorEquipment`、`coreTeam` 为 JSONB 字段，结构以 DB 实际存储为准，由前端渲染。

**错误：** 实验室不存在或已下架 → `{"code":404,"msg":"实验室不存在"}`

---

## 3. 院系列表

`GET /api/v1/app/university/{universityId}/departments`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 院校 id |

**Query：** page、size

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 20001, "departmentName": "计算机科学与技术学院", "departmentType": "工学" },
      { "id": 20002, "departmentName": "外国语学院", "departmentType": "文学" }
    ],
    "total": 2,
    "size": 10,
    "current": 1
  }
}
```

---

## 4. 院系分析报告

`GET /api/v1/app/university/departments/{departmentId}/report`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| departmentId | Long | 院系主键（从院系列表接口获取） |

**响应示例（节选，全部 JSONB 字段原样返回，结构以 DB 实际存储为准）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "subtitle": "2024 年度深度分析",
    "overview": {"summary": "……"},
    "subjectsDetail": [{"name": "……"}],
    "postgraduate": {"rate": 0.42},
    "citySalary": [{"city": "北京", "salary": 18000}],
    "salary": [{"level": "毕业生", "value": 12000}],
    "career": [{"industry": "互联网", "rate": 0.55}],
    "trends": {"hotness": "……"},
    "prospects": {"forecast": "……"},
    "disclaimer": {"text": "……"},
    "majorCompose": [{"name": "……", "ratio": 0.3}]
  }
}
```

**错误：** 报告未配置 → `{"code":404,"msg":"院系分析报告不存在"}`

---

## 5. 学科评估明细列表

`GET /api/v1/app/university/{universityId}/subject-evaluations`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 院校 id |

**Query：** page、size

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "disciplineCode": "0812",
        "disciplineName": "计算机科学与技术",
        "evaluationRound": "第四轮",
        "evaluationGrade": "A+"
      },
      {
        "disciplineCode": "0701",
        "disciplineName": "数学",
        "evaluationRound": "第四轮",
        "evaluationGrade": "A"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1
  }
}
```

> 列表按 `evaluation_grade` 的分数学意义顺序（A+ → C-）排序，同等级按 `sort_order` 兜底。

---

## 6. 学科评估等级统计

`GET /api/v1/app/university/{universityId}/subject-evaluations/grade-stats`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 院校 id |

**Query：** 无

**响应示例（固定 9 条，缺失等级 count=0 也返回）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "grade": "A+", "count": 37 },
    { "grade": "A",  "count": 25 },
    { "grade": "A-", "count": 18 },
    { "grade": "B+", "count": 12 },
    { "grade": "B",  "count": 8 },
    { "grade": "B-", "count": 5 },
    { "grade": "C+", "count": 2 },
    { "grade": "C",  "count": 1 },
    { "grade": "C-", "count": 0 }
  ]
}
```

> 返回顺序固定为 `A+, A, A-, B+, B, B-, C+, C, C-`，方便前端直接渲染表格 / 柱状图。
> `universityId` 在系统中不存在时，仍返回 9 条 `count=0` 的记录（不返回 404）。
