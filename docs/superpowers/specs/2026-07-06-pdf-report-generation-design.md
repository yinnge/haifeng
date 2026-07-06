# PDF 报告生成系统设计

> 日期: 2026-07-06
> 模块: haifeng-app / algorithm / pdf
> 状态: 已确认

## 1. 概述

用户将志愿方案导出为 PDF 报告，接入大模型（DeepSeek）进行多智能体编排分析，生成"全景志愿档案"。采用 Map-Reduce 架构：Map 阶段逐校 AI 简评，Reduce 阶段全局博弈研判。AI 分析结果持久化到数据库，PDF 按需重新渲染。

## 2. 已确认的关键决策

| 决策项 | 选择 | 说明 |
|--------|------|------|
| PDF 存储 | 只存 AI 结果（JSONB） | PDF 不落盘，重新打开时用存储的 AI 结果 + wish 快照表重新渲染 |
| 配额计算 | 1 次 PDF 生成 = 1 额度 | 不按 AI 调用次数算，N+1 次内部调用对用户透明 |
| SSE 内容 | 进度事件 | `{"stage":"map","current":1,"total":10,"university":"xxx"}` 等 |
| Map 并发 | 限流并行 Semaphore=3 | 平衡速度与 API 稳定性 |
| 容错 | 单校失败不阻断 | 失败条目标记 `success=false`，PDF 显示"暂无 AI 简评" |

## 3. 数据库设计

### 3.1 新增表：t_pdf_report

迁移文件：`V25__create_pdf_report_table.sql`

```sql
CREATE TABLE t_pdf_report (
    id              SERIAL          PRIMARY KEY,
    member_id       BIGINT          NOT NULL,
    plan_id         INTEGER         NOT NULL,
    status          SMALLINT        NOT NULL DEFAULT 0,   -- 0=生成中, 1=成功, 2=失败
    map_results     JSONB,                                 -- Map 阶段逐校 AI 简评数组
    reduce_result   JSONB,                                 -- Reduce 阶段全局研判
    plan_snapshot   JSONB,                                 -- 封面页数据快照
    fail_reason     VARCHAR(500),
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

### 3.2 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| member_id | BIGINT | 用户ID |
| plan_id | INTEGER | 关联 t_wish_plan.id |
| status | SMALLINT | 0=生成中, 1=成功, 2=失败 |
| map_results | JSONB | `[{universityId, universityName, cityName, majors:[{majorName, safetyLevel, levelShort}], commentary, success}]` |
| reduce_result | JSONB | `{globalAnalysis, swot, recommendation}` |
| plan_snapshot | JSONB | `{planYear, planProvince, reformModel, userScore, userRank, planBatch}` |
| fail_reason | VARCHAR(500) | status=2 时填写 |
| is_deleted | BOOLEAN | 软删除 |

### 3.3 索引

- `idx_pdf_report_member` ON (member_id) WHERE is_deleted = FALSE
- `idx_pdf_report_member_plan` ON (member_id, plan_id) WHERE is_deleted = FALSE

### 3.4 设计原则

- AI 产出的文本是唯一需要额外存储的内容
- 静态数据（院校详情、专业分数等）从已有 wish 三张快照表重新查询
- `plan_snapshot` 只存封面页少量字段，防止用户修改高考档案后封面变化

## 4. API 端点设计

### 4.1 端点列表

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/app/algorithm/pdf/generate/{planId}` | POST (SSE) | 生成 PDF 报告，流式返回进度，最终事件返回 recordId |
| `/api/v1/app/algorithm/pdf/records` | GET | 查看历史报告记录列表（分页） |
| `/api/v1/app/algorithm/pdf/records/{recordId}` | GET | 查看记录详情（含 AI 分析结果） |
| `/api/v1/app/algorithm/pdf/records/{recordId}/pdf` | GET | 重新渲染并下载 PDF |

### 4.2 Controller 改造

- 保留现有 `PdfPlanController` 路径 `/api/v1/app/algorithm/pdf`
- 删除 3 个维度导出接口（`/plan/{planId}/university`、`/city`、`/major`）
- 将 `/chat/stream` 改造为 `/generate/{planId}`，入参从 `AiChatRequestDTO` 改为 `planId` 路径参数
- 保留 `@RequireLogin` + `@RequireVip` 权限控制

### 4.3 SSE 事件格式

```
{"stage":"quota_checked","recordId":123}
{"stage":"map","current":1,"total":10,"university":"北京交通大学"}
{"stage":"map","current":2,"total":10,"university":"上海理工大学"}
...
{"stage":"map_done"}
{"stage":"reduce","status":"running"}
{"stage":"reduce","status":"done"}
{"stage":"done","recordId":123}
```

错误事件：
```
{"stage":"error","message":"配额不足","code":429}
{"stage":"error","message":"Reduce 阶段失败","recordId":123,"code":500}
```

## 5. Map-Reduce 编排流程

### 5.1 整体流程

```
用户 POST /generate/{planId} (SSE)
  │
  ├─ 1. 配额校验 incrAndCheck（1次 = 1额度）
  ├─ 2. 创建 t_pdf_report 记录，status=0(生成中)
  ├─ 3. 查 wish_plan + gaokao_archive → 存 plan_snapshot
  ├─ 4. 查 getExportGroupContexts(planId) → 获取所有可导出专业组
  │
  ├─ 5. Map 阶段（限流并行，Semaphore=3）
  │     每个专业组 → 1 次 AI 调用
  │     输入: {大学名, 城市, 专业名列表, 安全系数/档位}
  │     不给: 历史分数、招生人数等死数据
  │     输出: ~300字院校专业组地缘研判
  │     失败: 标记 success=false，不阻断流程
  │     SSE: {"stage":"map","current":i,"total":n,"university":"xxx"}
  │
  ├─ 6. 存 map_results 到 t_pdf_report
  │
  ├─ 7. Reduce 阶段（1 次 AI 调用）
  │     输入: 所有 Map 结果的浓缩 JSON
  │     不给: 原始分数大表（Java 直接填 HTML）
  │     输出: 全局 SWOT 分析 + 城市 VS 学校博弈 + 推荐填报梯队
  │     SSE: {"stage":"reduce","status":"running"} → {"stage":"reduce","status":"done"}
  │
  ├─ 8. 存 reduce_result，更新 status=1(成功)
  └─ 9. SSE 最终事件: {"stage":"done","recordId":123}
```

### 5.2 Map 阶段输入数据（轻量）

```json
{
  "university": "北京交通大学",
  "city": "北京",
  "majors": [
    {"name": "自动化", "safetyLevel": 0.78, "levelShort": "稳"},
    {"name": "智能芯片", "safetyLevel": 0.65, "levelShort": "冲"}
  ]
}
```

不给 AI 的数据：历史录取分数、招生人数、位次等死数据（由 Java 直接填入 HTML 表格）。

### 5.3 Reduce 阶段输入数据（浓缩大牌堆）

```json
[
  {
    "大学": "北京交通大学",
    "城市": "北京",
    "专业": ["自动化", "智能芯片"],
    "录取概率": "稳/冲",
    "AI简评": "北交大自动化依托轨交红利保研率高，但传统工科在京就业卷..."
  }
]
```

### 5.4 Reduce 阶段输出

- 志愿表 SWOT 全局象限分析（高风险高收益 vs 性价比之王）
- 城市地域红利 VS 学校名气光环的博弈辩证
- 海枫强烈推荐填报梯队顺序

### 5.5 容错策略

| 场景 | 处理 |
|------|------|
| 配额不足 | 直接拒绝，不创建记录，SSE 推 error 事件 |
| 单个大学 Map 失败 | 该条 success=false, commentary=null，PDF 显示"暂无 AI 简评"，继续流程 |
| 所有 Map 失败 | 仍进入 Reduce（commentary 全空），Reduce 基于专业名和概率做分析 |
| Reduce 失败 | 记录 status=2，SSE 推 error 事件，map_results 保留（部分可用） |

### 5.6 AiChatService 改造

当前 `AiChatService` 只有流式方法 `streamChat`，需新增非流式方法：

```java
/**
 * 非流式 AI 调用（Map/Reduce 内部使用）
 * @return AI 完整文本响应
 */
String chatSync(Long userId, List<ChatMessage> messages);
```

- 请求体 `stream: false`
- 复用现有 `ApiKeyPool.orderedFallback` 多 key 轮转
- 复用现有 `AiQuotaService`（但配额在 Controller 层已扣，这里不再 incr）

## 6. PDF 渲染设计（后续实现）

### 6.1 技术栈

- Thymeleaf 模板引擎
- OpenHTMLtoPDF（接收 HTML → PDF 字节流）
- CSS Paged Media（分页控制）

### 6.2 PDF 结构

| 页码 | 内容 | 数据来源 |
|------|------|----------|
| 第 1 页 | 专属封面（Logo + 考生画像） | plan_snapshot（静态，零 AI 成本） |
| 第 2-3 页 | 全局宏观全景研判 | reduce_result（Reduce AI 产出） |
| 第 4 页 | 全盘静态明细汇总大表 | wish 快照表（Java 渲染，零 AI 成本） |
| 第 5 页起 | 一页一校深度透视 | map_results + wish 快照（动静结合） |

### 6.3 重新打开流程

用户调用 `GET /records/{recordId}/pdf`：
1. 查 t_pdf_report 获取 map_results + reduce_result + plan_snapshot
2. 查 wish 快照表获取静态数据（大学详情、专业分数等）
3. Thymeleaf 渲染 HTML → OpenHTMLtoPDF 转 PDF
4. 返回 PDF 字节流

不调用 AI，不扣配额。

## 7. 依赖的现有服务

| 服务 | 方法 | 用途 |
|------|------|------|
| WishPlanService | getExportGroupContexts(planId) | 获取可导出专业组列表 |
| GaokaoArchiveService | getMyArchive() | 获取高考档案（封面数据） |
| UniversityService | detail(universityId) | 获取院校详情（PDF 渲染用） |
| CityService | detail(cityId) | 获取城市详情（PDF 渲染用） |
| AiChatService | chatSync(userId, messages) | 非流式 AI 调用（新增） |
| ApiKeyPool | orderedFallback(userId) | 多 key 轮转（已有） |
| AiQuotaService | incrAndCheck(userId) | 配额校验（已有，改造为按 PDF 次数） |

## 8. 待实现任务清单

1. Entity + Mapper：`PdfReport` entity + `PdfReportMapper`
2. Flyway V25 迁移文件（已创建）
3. AiChatService 新增 `chatSync` 非流式方法
4. AiQuotaService 改造：配额含义从"AI 调用次数"改为"PDF 生成次数"
5. PdfPlanController 改造：删除 3 个维度接口，改造 chat/stream 为 generate/{planId}
6. PdfPlanService 改造：实现 Map-Reduce 编排
7. 多智能体提示词设计（Map 智能体 + Reduce 总控智能体）
8. 历史记录接口：list / detail / pdf 重新渲染
9. PDF 模板：Thymeleaf + CSS 分页 + OpenHTMLtoPDF（后续实现）
