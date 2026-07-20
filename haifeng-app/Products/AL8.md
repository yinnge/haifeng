# PDF报告模块接口文档

## 概述

本模块实现用户端 PDF 报告的生成与查看功能。报告基于用户志愿方案中勾选导出的专业组，通过 **Map-Reduce AI 编排**生成逐校简评与全局研判，并使用 Thymeleaf + OpenHTMLtoPDF 渲染为 PDF 文件。生成过程通过 SSE（Server-Sent Events）流式返回进度，前端可实时展示 Map/Reduce 各阶段状态。

**端口：** 8080（用户端）

**基础路径：** `/api/v1/app/algorithm/pdf`

**认证要求：** `@RequireLogin`（需登录，JWT Bearer Token）+ `@RequireVip`（VIP 会员）

**特殊说明：**

- 生成接口使用 POST + SSE，不能用浏览器原生 `EventSource`（仅支持 GET），需用 `fetch` + `ReadableStream` 或第三方库
- PDF 文件不落盘，每次查看时用存储的 AI 结果 + 志愿快照表重新渲染
- 每日配额默认 3 次（可通过 `system_settings.api_number` 配置），失败自动退回配额

---

## 接口清单

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/generate/{planId}` | 生成PDF报告（SSE流式返回进度） | Login + VIP |
| GET | `/records` | 历史报告记录列表（分页） | Login + VIP |
| GET | `/records/{recordId}` | 报告记录详情 | Login + VIP |
| GET | `/records/{recordId}/pdf` | 下载/查看PDF（浏览器内联） | Login + VIP |

---

## 1. 生成PDF报告（SSE 流式）

### 1.1 接口信息

| 项 | 值 |
|----|----|
| URL | `POST /api/v1/app/algorithm/pdf/generate/{planId}` |
| 方法 | POST |
| 权限 | 登录用户 + VIP 会员（`@RequireLogin` + `@RequireVip`） |
| 响应类型 | `text/event-stream`（SSE） |
| 请求体 | 无（空 body 即可） |

### 1.2 请求头

```
Authorization: Bearer {accessToken}
Content-Type: application/json
Accept: text/event-stream
```

### 1.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 1.4 请求示例

```
POST /api/v1/app/algorithm/pdf/generate/1001
Authorization: Bearer {accessToken}
Content-Type: application/json
Accept: text/event-stream

（空 body）
```

### 1.5 SSE 事件类型

每个 SSE 事件的 `data` 字段为 JSON 字符串，通过 `stage` 字段区分事件类型。前端需监听所有 stage 值并分别处理。

| stage 值 | 说明 | 是否携带 recordId | 后续事件 |
|----------|------|-------------------|----------|
| `quota_checked` | 配额校验通过，已创建报告记录（status=生成中） | 是 | → map |
| `map` | 开始处理某个专业组（Map 阶段逐组并发） | 否 | → map / map_done |
| `map_done` | Map 阶段全部完成（含超时降级） | 否 | → reduce |
| `reduce` | Reduce 阶段状态更新（running / done） | 否 | → done |
| `done` | 报告生成完成，可查看 PDF | 是 | 流关闭 |
| `error` | 生成失败 | 视情况 | 流关闭 |

### 1.6 SSE 事件 payload 详细说明

#### 1.6.1 `quota_checked` 事件

配额校验通过，已在 `t_pdf_report` 表创建一条 status=0（生成中）的记录。

```json
{
  "stage": "quota_checked",
  "recordId": 123
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `quota_checked` |
| recordId | Integer | 报告记录ID（后续可用于查询详情和查看PDF） |

#### 1.6.2 `map` 事件

开始处理第 N 个专业组的 AI 简评。进度事件在并发任务启动前按序发送（current 严格递增），AI 调用仍为并发执行。

```json
{
  "stage": "map",
  "current": 1,
  "total": 3,
  "university": "北京大学01组"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `map` |
| current | Integer | 当前处理的专业组序号（从1开始） |
| total | Integer | 专业组总数 |
| university | String | 专业组名称（groupName，非院校名称） |

#### 1.6.3 `map_done` 事件

Map 阶段全部完成。即使部分专业组 AI 调用失败或超时，仍会发送此事件，后续进入 Reduce 阶段。

```json
{
  "stage": "map_done"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `map_done` |

#### 1.6.4 `reduce` 事件（running）

Reduce 阶段开始，正在调用 AI 生成全局研判。

```json
{
  "stage": "reduce",
  "status": "running"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `reduce` |
| status | String | `running` 表示开始执行 |

#### 1.6.5 `reduce` 事件（done）

Reduce 阶段完成，AI 返回了有效的全局研判结果。

```json
{
  "stage": "reduce",
  "status": "done"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `reduce` |
| status | String | `done` 表示执行完成 |

#### 1.6.6 `done` 事件

报告生成全部完成，记录 status 已更新为 1（成功），可调用 `/records/{recordId}/pdf` 查看 PDF。

```json
{
  "stage": "done",
  "recordId": 123
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `done` |
| recordId | Integer | 报告记录ID |

#### 1.6.7 `error` 事件（无 recordId）

生成失败，且错误发生在 recordId 创建之前或与具体记录无关。

```json
{
  "stage": "error",
  "message": "今日PDF生成次数已用完",
  "code": 429
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `error` |
| message | String | 错误描述 |
| code | Integer | 错误码（见错误码表） |

#### 1.6.8 `error` 事件（有 recordId）

生成失败，且报告记录已创建。recordId 可用于查询失败详情（failReason）。

```json
{
  "stage": "error",
  "message": "Reduce阶段失败",
  "recordId": 123,
  "code": 500
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| stage | String | 固定值 `error` |
| message | String | 错误描述 |
| recordId | Integer | 报告记录ID（记录已标记为 status=2 失败） |
| code | Integer | 错误码（见错误码表） |

### 1.7 事件流时序图

```
客户端发起 POST 请求
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  1. 配额校验                                            │
│     ├─ 通过 → 发送 quota_checked (recordId=123)         │
│     └─ 超额 → 发送 error (code=429) → 流关闭            │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  2. 志愿方案校验                                        │
│     ├─ 存在 → 继续                                      │
│     └─ 不存在 → 发送 error (code=404) → 流关闭          │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  3. 查询可导出专业组                                    │
│     ├─ 有数据 → 继续                                    │
│     └─ 空列表 → 发送 error (code=400) → 流关闭          │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  4. Map 阶段（并发，最多 180 秒）                       │
│     ├─ 每组开始 → 发送 map {current, total, university} │
│     ├─ AI 调用成功 → commentary 存入结果                │
│     ├─ AI 调用失败 → success=false, commentary=null     │
│     └─ 全部完成/超时 → 发送 map_done                    │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  5. 序列化 map_results                                 │
│     ├─ 成功 → 继续                                      │
│     └─ 失败 → 发送 error (code=500, recordId) → 流关闭  │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  6. Reduce 阶段                                         │
│     ├─ 开始 → 发送 reduce {status=running}              │
│     ├─ AI 返回有效 → 发送 reduce {status=done}          │
│     ├─ AI 返回空 → 发送 error (code=500, recordId)      │
│     └─ AI 调用异常 → 发送 error (code=500, recordId)    │
└─────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│  7. 完成                                                │
│     ├─ 写库成功 → 发送 done (recordId=123) → 流关闭     │
│     └─ 写库失败 → 发送 error (code=500, recordId)       │
│                 → 报告标记失败 → 退回配额 → 流关闭       │
└─────────────────────────────────────────────────────────┘
```

### 1.8 错误事件说明

| 错误码 | 触发场景 | 是否有 recordId | 是否退回配额 |
|--------|----------|-----------------|-------------|
| 429 | 今日PDF生成次数已用完 | 否 | 不涉及（未扣配额） |
| 404 | 志愿方案不存在或已删除 | 否 | 是 |
| 400 | 没有可导出的专业组（未勾选导出专业） | 否 | 是 |
| 500 | Map 结果序列化失败 | 是 | 是 |
| 500 | Reduce 阶段调用 AI 失败 | 是 | 是 |
| 500 | Reduce 阶段返回空内容 | 是 | 是 |
| 500 | 保存最终结果失败（步骤8写库异常） | 是 | 是 |
| 500 | 其他未捕获异常 | 否 | 否 |

### 1.9 注意事项

- **POST 请求不能使用浏览器原生 `EventSource`**（仅支持 GET），需用 `fetch` + `ReadableStream` 或第三方库（如 `@microsoft/fetch-event-source`）
- 流在 `done` 或 `error` 事件后自动关闭，无需手动断开
- Map 阶段 180 秒超时，超时后未完成的组降级为 `success=false`、`commentary=null`，继续执行 Reduce 阶段
- 配额每日 0 点重置（Redis key TTL 到当日 23:59:59），默认 3 次/天，可通过 `system_settings.api_number` 配置
- 生成失败时自动退回配额（`decr` 原子操作）
- 收到 `quota_checked` 事件后即可用 `recordId` 轮询 `/records/{recordId}` 查看状态（适用于 SSE 连接意外断开的场景）

---

## 2. 历史报告记录列表（分页）

### 2.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/pdf/records` |
| 方法 | GET |
| 权限 | 登录用户 + VIP 会员（`@RequireLogin` + `@RequireVip`） |

### 2.2 请求头

```
Authorization: Bearer {accessToken}
```

### 2.3 请求参数（Query String）

| 参数名 | 类型 | 必填 | 默认值 | 校验规则 | 说明 |
|--------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | @Min(1) | 页码，从1开始 |
| size | Integer | 否 | 10 | @Min(10), @Max(100) | 每页条数 |
| status | Short | 否 | null | 0/1/2 | 按报告状态过滤：0=生成中, 1=成功, 2=失败 |
| planId | Integer | 否 | null | @Min(1) | 按志愿方案ID过滤 |

**分页选项说明：** 推荐 `10 / 20 / 30 / 50 / 100`

### 2.4 请求示例

```
GET /api/v1/app/algorithm/pdf/records?page=1&size=10&status=1&planId=1001
Authorization: Bearer {accessToken}
```

### 2.5 查询逻辑说明

- 查询当前用户所有未删除的报告记录
- 支持按 `status` 和 `planId` 筛选（可选）
- 按 `created_at` 降序排列（最新生成的在前）
- 仅返回列表字段，不含 mapResults / reduceResult 等大字段

### 2.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 123,
        "planId": 1001,
        "planName": "我的志愿方案1",
        "status": 1,
        "createdAt": "2025-06-25T10:30:00+08:00"
      },
      {
        "id": 122,
        "planId": 1001,
        "planName": "我的志愿方案1",
        "status": 2,
        "createdAt": "2025-06-24T15:20:00+08:00"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1714300000000
}
```

### 2.7 响应示例（空列表）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [],
    "total": 0,
    "size": 10,
    "current": 1,
    "pages": 0
  },
  "timestamp": 1714300000000
}
```

### 2.8 响应字段说明（PdfRecordListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 报告记录ID |
| planId | Integer | 志愿方案ID |
| planName | String | 方案名称（从 t_wish_plan 表查询填充） |
| status | Short | 状态：0=生成中, 1=成功, 2=失败 |
| createdAt | OffsetDateTime | 创建时间 |

### 2.9 注意事项

- 查询结果按 `created_at` 降序排列

---

## 3. 报告记录详情

### 3.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/pdf/records/{recordId}` |
| 方法 | GET |
| 权限 | 登录用户 + VIP 会员（`@RequireLogin` + `@RequireVip`） |

### 3.2 请求头

```
Authorization: Bearer {accessToken}
```

### 3.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| recordId | Integer | 是 | @Min(1) | 报告记录ID |

### 3.4 请求示例

```
GET /api/v1/app/algorithm/pdf/records/123
Authorization: Bearer {accessToken}
```

### 3.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123,
    "planId": 1001,
    "planName": "我的志愿方案1",
    "status": 1,
    "mapResults": "[{\"universityId\":101,\"universityName\":\"北京大学\",\"cityName\":\"北京\",\"groupName\":\"不限选考科目组\",\"groupSnapshotId\":1,\"majors\":[...],\"commentary\":\"...\",\"success\":true}]",
    "reduceResult": "{\"globalAnalysis\":\"...\",\"swot\":\"...\",\"recommendation\":\"...\"}",
    "planSnapshot": "{\"planYear\":2025,\"planProvince\":\"广东\",\"reformModel\":\"3+1+2\",\"userScore\":620,\"userRank\":15000,\"planBatch\":\"本科批\"}",
    "failReason": null,
    "createdAt": "2025-06-25T10:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

### 3.6 响应示例（未找到）

```json
{
  "code": 404,
  "msg": "报告记录不存在",
  "data": null,
  "timestamp": 1714300000000
}
```

### 3.7 响应字段说明（PdfRecordDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 报告记录ID |
| planId | Integer | 志愿方案ID |
| planName | String | 方案名称（从 t_wish_plan 表查询填充） |
| status | Short | 状态：0=生成中, 1=成功, 2=失败 |
| mapResults | String | Map 阶段结果（JSON 字符串，需 `JSON.parse()` 后使用，见第7节） |
| reduceResult | String | Reduce 阶段结果（JSON 字符串，需 `JSON.parse()` 后使用，见第7节） |
| planSnapshot | String | 封面页数据快照（JSON 字符串，需 `JSON.parse()` 后使用，见第7节） |
| failReason | String | 失败原因（仅 status=2 时非 null） |
| createdAt | OffsetDateTime | 创建时间 |

### 3.8 注意事项

- `mapResults` / `reduceResult` / `planSnapshot` 是 JSONB 列，后端以 JSON 字符串返回，前端需 `JSON.parse()` 后使用
- `failReason` 仅在 `status=2`（失败）时有值，可用于展示失败原因
- status=0（生成中）时，`mapResults` / `reduceResult` / `planSnapshot` 可能为 null 或部分填充

---

## 4. 下载/查看PDF（浏览器内联）

### 4.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/pdf/records/{recordId}/pdf` |
| 方法 | GET |
| 权限 | 登录用户 + VIP 会员（`@RequireLogin` + `@RequireVip`） |
| 响应类型 | `application/pdf`（二进制流） |

### 4.2 请求头

```
Authorization: Bearer {accessToken}
```

### 4.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| recordId | Integer | 是 | @Min(1) | 报告记录ID |

### 4.4 请求示例

```
GET /api/v1/app/algorithm/pdf/records/123/pdf
Authorization: Bearer {accessToken}
```

### 4.5 响应头

| 响应头 | 值 | 说明 |
|--------|----|------|
| Content-Type | `application/pdf` | PDF MIME 类型 |
| Content-Disposition | `inline; filename="{动态文件名}.pdf"` | 浏览器内联显示（文件名格式：`海枫报告-{年份}{省份}-{分数}分.pdf`，如 snapshot 为空则退回 `haifeng-report-{recordId}.pdf`） |
| Content-Length | {字节数} | PDF 文件大小 |

### 4.6 响应体

PDF 二进制流（`byte[]`）。

### 4.7 错误响应

| code | 说明 | 触发场景 |
|------|------|---------|
| 404 | 报告记录不存在 | recordId 不存在、已删除、或不属于当前用户 |
| 400 | 报告尚未生成完成 | status != 1（生成中或失败） |
| 500 | PDF渲染失败 | OpenHTMLtoPDF 渲染异常 |

```json
{
  "code": 400,
  "msg": "报告尚未生成完成",
  "data": null,
  "timestamp": 1714300000000
}
```

### 4.8 前端处理建议

- **内联展示：** 可直接用 `<iframe src="...">` 或 `window.open(url)` 在浏览器中展示 PDF
- **下载文件：** 需用 `<a>` 标签 + `download` 属性，或通过 `fetch` 获取 blob 后下载
- **权限限制：** 仅 `status=1`（成功）的记录可查看 PDF，`status=0`（生成中）需等待完成，`status=2`（失败）展示 failReason
- **Token 携带：** 若用 `<iframe>` 或 `<a>` 直接访问，需确保请求头携带 Authorization（可能需通过 fetch + blob 方式处理）

---

## 5. 枚举值说明

### 5.1 报告状态（PdfReportStatus）

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | GENERATING | 生成中 |
| 1 | SUCCESS | 成功 |
| 2 | FAILED | 失败 |

### 5.2 SSE stage 值

| stage 值 | 说明 |
|----------|------|
| `quota_checked` | 配额校验通过，记录已创建 |
| `map` | Map 阶段逐组处理中 |
| `map_done` | Map 阶段全部完成 |
| `reduce` | Reduce 阶段执行中（status: running / done） |
| `done` | 报告生成完成 |
| `error` | 生成失败 |

---

## 6. 错误码说明

| code | 说明 | 触发场景 |
|------|------|---------|
| 200 | 成功 | 正常请求 |
| 400 | 参数错误 / 业务校验失败 | 没有可导出的专业组、报告尚未生成完成（查看PDF时） |
| 401 | 未登录或Token过期 | 未携带/无效JWT |
| 403 | 无权限 | 非VIP会员访问 |
| 404 | 资源不存在 | 志愿方案不存在、报告记录不存在 |
| 429 | 请求过于频繁 | 今日PDF生成次数已用完 |
| 500 | 服务器内部错误 | Map序列化失败、Reduce阶段失败、Reduce返回空内容、PDF渲染失败、其他异常 |

---

## 7. 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1714300000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 成功，其他见错误码说明 |
| msg | String | 状态描述 |
| data | Object | 响应数据，可为 null |
| timestamp | Long | 服务器时间戳（毫秒） |

> **注意：** SSE 生成接口（`POST /generate/{planId}`）不返回此统一格式，而是返回 `text/event-stream` 流，每个事件的 `data` 字段为 JSON 字符串。

---

## 8. SSE 事件 payload 结构说明

以下三个字段在报告详情接口中以 JSON 字符串形式返回，前端需 `JSON.parse()` 后使用。

### 8.1 mapResults（JSON 数组）

Map 阶段逐校 AI 简评结果，每个数组元素对应一个专业组。

```json
[
  {
    "universityId": 101,
    "universityName": "北京大学",
    "cityName": "北京",
    "groupName": "不限选考科目组",
    "groupSnapshotId": 1,
    "majors": [
      {
        "majorName": "计算机科学与技术",
        "safetyLevel": 0.72,
        "levelShort": "保",
        "employmentRate": 0.95,
        "salaryMin": 8000,
        "salaryMax": 15000,
        "majorCategory": "计算机类",
        "careerProspect": "就业前景广阔，可从事软件开发、人工智能等..."
      }
    ],
    "commentary": "**北京大学的计算机科学与技术专业**地处中关村科技核心...",
    "success": true
  }
]
```

| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 院校ID |
| universityName | String | 院校名称 |
| cityName | String | 城市名称 |
| groupName | String | 专业组名称 |
| groupSnapshotId | Integer | 专业组快照ID |
| majors | Array | 专业列表 |
| majors[].majorName | String | 专业名称 |
| majors[].safetyLevel | BigDecimal | 安全系数 0.00~1.00 |
| majors[].levelShort | String | 安全等级简称：搏/冲/稳/保/垫 |
| majors[].employmentRate | BigDecimal | 就业率（可为 null） |
| majors[].salaryMin | Integer | 最低薪资（可为 null） |
| majors[].salaryMax | Integer | 最高薪资（可为 null） |
| majors[].majorCategory | String | 专业大类（可为 null） |
| majors[].careerProspect | String | 就业前景（截断80字，可为 null） |
| commentary | String | AI 简评（~300字 Markdown 格式，失败时为 null） |
| success | Boolean | AI 调用是否成功 |

### 8.2 reduceResult（JSON 对象）

Reduce 阶段全局研判结果。

```json
{
  "globalAnalysis": "## 全局宏观全景研判\n\n从整体填报结构来看...",
  "swot": "## SWOT象限分析\n\n**优势（S）：**...",
  "recommendation": "## 海枫强烈推荐填报梯队顺序\n\n1. 第一梯队：..."
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| globalAnalysis | String | 全局宏观分析（Markdown 格式） |
| swot | String | SWOT 象限分析（Markdown 格式） |
| recommendation | String | 推荐填报梯队顺序（Markdown 格式） |

### 8.3 planSnapshot（JSON 对象）

封面页数据快照，来自志愿方案主表。

```json
{
  "planYear": 2025,
  "planProvince": "广东",
  "reformModel": "3+1+2",
  "userScore": 620,
  "userRank": 15000,
  "planBatch": "本科批"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| planYear | Short | 高考年份 |
| planProvince | String | 高考省份 |
| reformModel | String | 改革模式（如：3+1+2、3+3） |
| userScore | Integer | 用户分数 |
| userRank | Integer | 用户位次 |
| planBatch | String | 录取批次 |

---

## 9. 数据库表结构（简表）

### 9.1 t_pdf_report（PDF报告记录表）

来源：`V25__create_pdf_report_table.sql`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL (PK) | 主键（自增） |
| member_id | BIGINT | 用户ID |
| plan_id | INTEGER | 志愿方案ID（关联 t_wish_plan.id） |
| status | SMALLINT | 状态：0=生成中, 1=成功, 2=失败 |
| map_results | JSONB | Map 阶段逐校 AI 简评 JSON 数组 |
| reduce_result | JSONB | Reduce 阶段全局研判 JSON |
| plan_snapshot | JSONB | 封面页数据快照 JSON |
| fail_reason | VARCHAR(500) | 失败原因（status=2 时填写） |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**索引：**
- `idx_pdf_report_member`：按 member_id 查询（过滤未删除）
- `idx_pdf_report_member_plan`：按 member_id + plan_id 查询（过滤未删除）

---

## 10. 前端对接注意事项

### 10.1 接口调用顺序建议

```
1. 志愿方案模块(AL7)：勾选导出专业（PUT /{planId}/majors/{id}/export 或批量）
2. 志愿方案模块(AL7)：保存导出状态（POST /{planId}/export/save）
3. PDF模块：生成报告（POST /generate/{planId}）—— 监听 SSE 事件流
4. PDF模块：收到 done 事件后，查看PDF（GET /records/{recordId}/pdf）
5. PDF模块：查看历史记录（GET /records）—— 列表展示
6. PDF模块：查看报告详情（GET /records/{recordId}）—— 展示 AI 分析结果
```

### 10.2 SSE 客户端实现（POST + SSE）

浏览器原生 `EventSource` 仅支持 GET 请求，本接口为 POST，需使用 `fetch` + `ReadableStream` 或第三方库。

**方案一：使用 `@microsoft/fetch-event-source`**

```javascript
import { fetchEventSource } from '@microsoft/fetch-event-source';

const controller = new AbortController();

await fetchEventSource('/api/v1/app/algorithm/pdf/generate/1001', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
    'Accept': 'text/event-stream',
  },
  body: JSON.stringify({}), // 空 body
  signal: controller.signal,
  onmessage(event) {
    const data = JSON.parse(event.data);
    switch (data.stage) {
      case 'quota_checked':
        console.log('记录已创建，recordId:', data.recordId);
        break;
      case 'map':
        console.log(`处理中 ${data.current}/${data.total}: ${data.university}`);
        break;
      case 'map_done':
        console.log('Map 阶段完成');
        break;
      case 'reduce':
        console.log('Reduce 阶段:', data.status);
        break;
      case 'done':
        console.log('生成完成，recordId:', data.recordId);
        // 跳转查看 PDF
        break;
      case 'error':
        console.error('生成失败:', data.message, 'code:', data.code);
        break;
    }
  },
  onclose() {
    console.log('SSE 连接关闭');
  },
  onerror(err) {
    console.error('SSE 错误:', err);
    throw err; // 抛出错误以停止重试
  },
});

// 如需取消：controller.abort();
```

**方案二：使用原生 fetch + ReadableStream**

```javascript
const response = await fetch('/api/v1/app/algorithm/pdf/generate/1001', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
    'Accept': 'text/event-stream',
  },
  body: JSON.stringify({}),
});

const reader = response.body.getReader();
const decoder = new TextDecoder();
let buffer = '';

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  buffer += decoder.decode(value, { stream: true });
  const lines = buffer.split('\n');
  buffer = lines.pop();
  for (const line of lines) {
    if (line.startsWith('data:')) {
      const data = JSON.parse(line.slice(5).trim());
      // 处理 data.stage
    }
  }
}
```

### 10.3 配额管理

- 每日默认 3 次（可通过 `system_settings.api_number` 配置），Redis 缓存 5 分钟
- 配额 key：`pdf:report:quota:{userId}:{yyyyMMdd}`，TTL 到当日 23:59:59
- **失败退回配额**：生成过程中任何阶段失败都会调用 `decr` 退回配额（原子操作，不会产生负数）
- **前端应展示剩余次数**：当前接口未提供查询剩余配额的端点，前端可通过记录当日生成次数推算
- 429 错误不应重试（当日已满），500 错误可重试（配额已退回）

### 10.4 状态处理

| status | 说明 | 前端处理 |
|--------|------|---------|
| 0 | 生成中 | 可轮询 `GET /records/{recordId}` 等待状态变更（适用于 SSE 断开场景） |
| 1 | 成功 | 展示「查看PDF」按钮，调用 `GET /records/{recordId}/pdf` |
| 2 | 失败 | 展示 failReason，提供「重新生成」按钮 |

### 10.5 PDF 查看方式

- **内联展示：** `<iframe src="...">` 或 `window.open()` 直接在浏览器中显示
- **下载文件：** `<a href="..." download>` 或 fetch blob 后下载（文件名由后端动态生成，从响应头 `Content-Disposition` 获取）
- **Token 问题：** 若直接用 URL 访问无法携带 Authorization 头，建议用 fetch 获取 blob 后再展示/下载：

```javascript
const response = await fetch(`/api/v1/app/algorithm/pdf/records/${recordId}/pdf`, {
  headers: { 'Authorization': `Bearer ${accessToken}` },
});
const blob = await response.blob();
const url = URL.createObjectURL(blob);
// 内联展示
iframe.src = url;
// 或下载（从响应头获取实际文件名）
const disposition = response.headers.get('Content-Disposition');
const filenameMatch = disposition && disposition.match(/filename="?([^"]+)"?/);
const filename = filenameMatch ? filenameMatch[1] : `haifeng-report-${recordId}.pdf`;
const a = document.createElement('a');
a.href = url;
a.download = filename;
a.click();
URL.revokeObjectURL(url);
```

### 10.6 错误重试策略

| 错误码 | 是否重试 | 说明 |
|--------|---------|------|
| 429 | 不重试 | 当日配额已用完，次日重置 |
| 404 | 不重试 | 志愿方案不存在，需用户检查方案 |
| 400 | 不重试 | 未勾选导出专业，需用户先勾选 |
| 500 | 可重试 | 服务端异常，配额已退回，可重新生成 |

### 10.7 planName 字段

- 列表和详情接口中的 `planName` 字段已从 `t_wish_plan` 表自动填充，无需前端额外调用志愿方案接口获取

### 10.8 JSONB 字段解析

- `mapResults`、`reduceResult`、`planSnapshot` 三个字段在详情接口中以 JSON 字符串形式返回
- 前端需 `JSON.parse()` 后使用，建议封装解析工具函数并处理解析异常
- status=0（生成中）时这些字段可能为 null 或部分填充，前端需做 null 判断

### 10.9 SSE 断线重连

- SSE 连接意外断开时（非正常 done/error 关闭），可通过 `recordId`（从 `quota_checked` 事件获取）轮询 `GET /records/{recordId}` 查看记录状态
- 若 status=1（成功），直接展示 PDF
- 若 status=2（失败），展示 failReason
- 若 status=0（生成中），继续等待（建议轮询间隔 3-5 秒，最多等待 5 分钟）

---

## 11. 文件清单

| 类型 | 文件路径 |
|------|---------|
| Controller | `haifeng-app/.../controller/algorithm/pdf/PdfPlanController.java` |
| Service 接口 | `haifeng-app/.../service/algorithm/pdf/PdfReportService.java` |
| Service 实现 | `haifeng-app/.../service/impl/algorithm/pdf/PdfReportServiceImpl.java` |
| PDF 渲染接口 | `haifeng-app/.../service/algorithm/pdf/PdfRenderService.java` |
| PDF 渲染实现 | `haifeng-app/.../service/impl/algorithm/pdf/PdfRenderServiceImpl.java` |
| AI 对话接口 | `haifeng-app/.../service/algorithm/pdf/AiChatService.java` |
| AI 对话实现 | `haifeng-app/.../service/impl/algorithm/pdf/AiChatServiceImpl.java` |
| 配额服务 | `haifeng-common/.../service/ai/AiQuotaService.java` |
| DTO | `haifeng-app/.../dto/algorithm/pdf/PdfRecordQueryDTO.java` |
| VO（列表） | `haifeng-app/.../vo/algorithm/pdf/PdfRecordListVO.java` |
| VO（详情） | `haifeng-app/.../vo/algorithm/pdf/PdfRecordDetailVO.java` |
| VO（Map结果） | `haifeng-app/.../vo/algorithm/pdf/MapResultItem.java` |
| VO（Reduce结果） | `haifeng-app/.../vo/algorithm/pdf/ReduceResult.java` |
| VO（快照） | `haifeng-app/.../vo/algorithm/pdf/PlanSnapshot.java` |
| VO（导出上下文） | `haifeng-app/.../vo/algorithm/pdf/ExportGroupContextVO.java` |
| VO（渲染数据） | `haifeng-app/.../vo/algorithm/pdf/PdfRenderData.java` |
| VO（城市增强） | `haifeng-app/.../vo/algorithm/pdf/CityEnrichmentVO.java` |
| VO（专业增强） | `haifeng-app/.../vo/algorithm/pdf/MajorEnrichmentVO.java` |
| 工具类 | `haifeng-app/.../util/algorithm/pdf/EnrichmentLoader.java` |
| Entity | `haifeng-common/.../entity/algorithm/pdf/PdfReport.java` |
| Enum | `haifeng-common/.../enums/PdfReportStatus.java` |
| Mapper | `haifeng-common/.../mapper/algorithm/pdf/PdfReportMapper.java` |
| Flyway | `haifeng-admin/.../db/migration/V25__create_pdf_report_table.sql` |
| Thymeleaf 模板 | `haifeng-app/.../resources/templates/pdf-report.html` |
| 格式参照 | `haifeng-app/Products/AL7.md` |
