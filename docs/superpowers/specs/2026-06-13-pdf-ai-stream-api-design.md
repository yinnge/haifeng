# PDF 模块 AI 流式接口（MVP）设计

**日期**：2026-06-13
**模块**：haifeng-app / PDF（algorithm 二级目录）
**范围**：MVP——只交付 AI 流式接口骨架；PDF 生成、多智能体、tool calling、提示词工程均为后续任务，本次不做。
**身份限制**：仅 VIP（`@RequireVip`）。

---

## 1. 背景与目标

用户将志愿表等信息导出为 PDF 并接入大模型分析。本次先把"接 DeepSeek 的流式 AI 接口"骨架打通：

- DeepSeek（OpenAI 兼容协议）配置类
- 多 api-key 池，按用户哈希绑定，失败回退
- 每用户每天调用次数限制（绑定 `system_settings.api_number`，存 Redis）
- Controller 层只做一个简单的 SSE 流式返回，提示词留空

后续任务（**本次不做**）：Thymeleaf 模板、OpenHTMLtoPDF 渲染、多智能体编排、tool calling、PDF 真正导出。

---

## 2. 关键技术选型

| 项 | 选型 | 备注 |
|---|---|---|
| HTTP 客户端 | Spring `WebClient` | 原生支持 SSE，与 Spring Boot 3 / Reactor 兼容 |
| 流式协议 | SSE (`Flux<ServerSentEvent<String>>`) | OpenAI 协议天然契合 |
| api-key 调度 | 一致性哈希池 + 健康度冷却 + 顺序回退 | 命中缓存 + 失败切换 + 坏 key 不拖累 |
| 配额 | Redis 每日计数器，超额 HTTP 429 | TTL 设到当日 23:59:59 |
| 配置存储 | `application.yml` + `.env`（多 key 用逗号分隔） | 不建数据库表（YAGNI） |
| 入参开放度 | 最小入参，仅 `messages` | 防止前端滥用 `max_tokens` 烧 token |
| 默认 model | `deepseek-v4-flash` | 写 `application.yml`，后续改配置即可 |

---

## 3. 模块结构

```
haifeng-app/src/main/java/com/haifeng/app/
├── controller/algorithm/pdf/
│   └── PdfPlanController.java          # SSE 流式接口
├── service/algorithm/pdf/
│   └── AiChatService.java              # 接口
├── service/impl/algorithm/pdf/
│   └── AiChatServiceImpl.java          # 调用 DeepSeek + 限流 + key 选择
├── dto/algorithm/pdf/
│   └── AiChatRequestDTO.java           # 入参：List<ChatMessage> messages
└── vo/algorithm/pdf/
    └── ChatMessage.java                # role + content

haifeng-common/src/main/java/com/haifeng/common/
├── config/
│   ├── DeepSeekProperties.java         # @ConfigurationProperties("deepseek")
│   └── DeepSeekWebClientConfig.java    # WebClient Bean
└── service/ai/
    ├── ApiKeyPool.java                 # 一致性哈希 + 健康度冷却 + 回退
    └── AiQuotaService.java             # Redis 每日计数器
```

放置原则：通用 AI 能力（key 池、配额、配置）在 common，业务接口（controller/service）在 app。

---

## 4. 配置

`application.yml`：

```yaml
deepseek:
  base-url: https://api.deepseek.com
  model: deepseek-v4-flash
  api-keys: ${DEEPSEEK_API_KEYS}     # 多个用逗号分隔
  max-tokens: 4096
  temperature: 1.0
  key-cooldown-seconds: 300
  timeout-seconds: 60
```

`.env`：

```
DEEPSEEK_API_KEYS=sk-xxx,sk-yyy,sk-zzz
```

`DeepSeekProperties` 用 `@ConfigurationProperties(prefix = "deepseek")` 绑定，`apiKeys` 字段类型 `List<String>`，Spring 自动按逗号拆分。

---

## 5. 组件职责

### 5.1 `DeepSeekWebClientConfig`
暴露一个 `WebClient` Bean，配置 baseUrl、超时、SSE codec。**不在这里塞 api-key**——api-key 每次请求动态注入到 Authorization header。

### 5.2 `ApiKeyPool`（`@Service`）
- `String pickKey(Long userId)`：`hash(userId) % N` 选 key，跳过冷却中的（查 Redis）；全部冷却时按顺序返回首选
- `void markUnhealthy(String key)`：写 `ai:key:cooldown:{key}`，TTL = `keyCooldownSeconds`
- `List<String> orderedFallback(Long userId)`：返回首选 key + 其余 key（失败重试用）
- `@PostConstruct` 校验 `apiKeys` 非空，否则 `IllegalStateException` fail-fast

### 5.3 `AiQuotaService`（`@Service`）
- `void incrAndCheck(Long userId)`：
  - `INCR pdf:ai:quota:{userId}:{yyyyMMdd}`，第一次 INCR 时用 `EXPIREAT` 设 TTL 到当日 23:59:59
  - 若 `INCR 后值 > limit` 则抛 `QuotaExceededException`（**不 DECR**，避免并发竞态绕过限额）
- `int getApiNumberLimit()`：缓存 `system_settings.api_number` 到 Redis（key `sys:api_number`，TTL 5 分钟）；表中无记录则兜底 `3`

### 5.4 `AiChatService.streamChat(Long userId, AiChatRequestDTO)` → `Flux<ServerSentEvent<String>>`
1. `aiQuotaService.incrAndCheck(userId)`，超额抛 → 全局异常映射 429
2. `apiKeyPool.orderedFallback(userId)` 拿候选 key 列表
3. `Flux.fromIterable(keys).concatMap(key -> callDeepSeek(key, body))`：用首个 key 发 POST `/v1/chat/completions`（`stream=true`），失败 `onErrorResume` → `markUnhealthy(key)` + 切下一个 key
4. 全部失败 → 抛业务异常 → 全局异常映射 502
5. 把 DeepSeek 返回的 SSE chunk 透传给前端

### 5.5 `PdfPlanController`
- 类级 `@RestController` `@RequestMapping("/api/v1/app/algorithm/pdf")` `@RequireLogin` `@RequireVip`
- 方法 `@PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)`
- 从项目现有获取当前用户的工具拿 `userId`，调 service

---

## 6. 接口契约

### 请求
```
POST /api/v1/app/algorithm/pdf/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "messages": [
    { "role": "user", "content": "你好" }
  ]
}
```

校验：`messages` 非空；每条 `role ∈ {system,user,assistant}`；`content` 非空。

### 响应（SSE，透传 DeepSeek `delta.content`）

```
data: {"content":"你"}

data: {"content":"好"}

data: [DONE]
```

### 服务端 → DeepSeek 请求体

```json
{
  "model": "deepseek-v4-flash",
  "stream": true,
  "max_tokens": 4096,
  "temperature": 1.0,
  "messages": [
    { "role": "system", "content": "" },
    { "role": "user", "content": "..." }
  ]
}
```

提示词留空（按需求文档第 125 行）。

### 错误响应
| 场景 | HTTP | 说明 |
|---|---|---|
| 未登录 | 401 | 走项目现有拦截器 |
| 非 VIP | 403 | `@RequireVip` 切面 |
| 入参非法 | 400 | `@Valid` + 全局异常 |
| 当日超额 | **429** | `{"code":429,"msg":"今日 AI 调用次数已用完","data":null}` |
| 全部 key 失败 | 502 | 业务码格式 |

---

## 7. 数据流

```
前端 EventSource
   │ POST /chat/stream
   ▼
PdfPlanController  ── @RequireLogin @RequireVip
   │ userId
   ▼
AiChatService
   ├─ AiQuotaService.incrAndCheck  ──→ Redis (INCR + EXPIREAT)
   ├─ ApiKeyPool.orderedFallback   ──→ Redis (查冷却)
   └─ WebClient → DeepSeek SSE
        │ Flux<chunk>
        │ onError → markUnhealthy → 切换下一个 key
        ▼
   Flux<ServerSentEvent<String>> ──→ 前端
```

---

## 8. 错误处理与边界

1. **Key 全部失败**：`Flux.fromIterable + concatMap + onErrorResume` 串行尝试，全失败抛业务异常 → 502。失败的 key 同步 `markUnhealthy`。
2. **超额无 DECR**：`INCR` 后判断超额则拒绝且不回退，避免并发绕过。第一次 INCR 时 `EXPIREAT` 当日末。
3. **流式中途断开**：服务端→DeepSeek 异常时已发 chunk 不回滚，配额已扣（合理）；客户端主动断开 → `Flux.doOnCancel` 关闭上游。
4. **`system_settings.api_number` 兜底**：表无记录走默认 `3`。Redis 缓存 5 分钟。
5. **`apiKeys` 空**：`@PostConstruct` fail-fast。
6. **不做**：PDF 生成 / Thymeleaf / OpenHTMLtoPDF / 多智能体 / tool calling / 提示词 / api-key 数据库表。

---

## 9. 测试策略

**单元测试（不打 DeepSeek 真实接口）**

1. `ApiKeyPoolTest`
   - 同 userId 多次 `pickKey` 返回同一 key
   - 不同 userId 大致均匀分布
   - `markUnhealthy` 后冷却期内跳过
   - 全部 key 冷却时仍按顺序回退
   - `apiKeys` 空启动 fail-fast

2. `AiQuotaServiceTest`（mock `RedisTemplate` 或 embedded-redis）
   - 第一次 INCR=1 且 TTL 被设
   - 达到 limit 时拒绝
   - 超额抛 `QuotaExceededException`
   - `system_settings` 缓存命中 / miss 走 DB

3. `AiChatServiceImplTest`（mock WebClient）
   - mock DeepSeek SSE chunk → service Flux 输出对应 chunk
   - 第一个 key 401 → 切第二个 key 成功
   - 全部 key 失败 → Flux 抛业务异常

**不做**：Controller e2e（项目里其它 controller 也没做，保持一致）；真打 DeepSeek 的集成测试。

---

## 10. 后续任务（本次不实现，仅记录）

1. 注册已有 api 为 tool calling
2. 设计多智能体编排架构
3. 为每个智能体写提示词和功能约束
4. 多智能体协作导出 PDF（Thymeleaf + CSS 强行分页）
5. OpenHTMLtoPDF 把 HTML 转 PDF 字节流（A4 / CSS 分页）
6. 若 key 需要动态轮换 / 管理：把 `apiKeys` 从配置迁到数据库表
