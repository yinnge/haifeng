# PDF 模块 AI 流式接口（MVP）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 haifeng-app 实现 `/api/v1/app/algorithm/pdf/chat/stream` SSE 流式接口骨架，接入 DeepSeek（OpenAI 兼容协议），具备多 api-key 池（用户哈希绑定 + 失败回退）与每日调用次数限制（绑定 `system_settings.api_number`）。

**Architecture:** Controller (`PdfPlanController`) → Service (`AiChatServiceImpl`) → 通用组件（`ApiKeyPool` 选 key、`AiQuotaService` 计数限流、`WebClient` 发送 SSE 流式请求）。配额、key 健康度均存 Redis。

**Tech Stack:** Spring Boot 3 (servlet 模式) + Spring `WebClient`（spring-boot-starter-webflux 仅作为客户端）+ Reactor `Flux<ServerSentEvent>` + Spring Data Redis + MyBatis-Plus。

**对应 Spec:** `docs/superpowers/specs/2026-06-13-pdf-ai-stream-api-design.md`

---

## File Structure

| 路径 | 类型 | 职责 |
|---|---|---|
| `haifeng-common/pom.xml` | 修改 | 加 `spring-boot-starter-webflux` 依赖 |
| `haifeng-app/src/main/resources/application.yml` | 修改 | 加 `deepseek` 配置块 |
| `haifeng-common/.../config/DeepSeekProperties.java` | 新建 | `@ConfigurationProperties("deepseek")` |
| `haifeng-common/.../config/DeepSeekWebClientConfig.java` | 新建 | `WebClient` Bean |
| `haifeng-common/.../service/ai/ApiKeyPool.java` | 新建 | 一致性哈希 + 健康度冷却 + 顺序回退 |
| `haifeng-common/.../service/ai/AiQuotaService.java` | 新建 | Redis 每日计数器 + 限额查询缓存 |
| `haifeng-common/.../exception/QuotaExceededException.java` | 新建 | 配额超额业务异常 |
| `haifeng-common/.../exception/GlobalExceptionHandler.java` | 修改 | 加 `QuotaExceededException` handler（HTTP 429）|
| `haifeng-common/.../response/ResultCode.java` | 修改 | 加 `AI_QUOTA_EXCEEDED` 与 `AI_ALL_KEYS_FAILED` 业务码 |
| `haifeng-app/.../vo/algorithm/pdf/ChatMessage.java` | 新建 | role + content |
| `haifeng-app/.../dto/algorithm/pdf/AiChatRequestDTO.java` | 新建 | `List<ChatMessage> messages` |
| `haifeng-app/.../service/algorithm/pdf/AiChatService.java` | 新建 | service 接口 |
| `haifeng-app/.../service/impl/algorithm/pdf/AiChatServiceImpl.java` | 新建 | DeepSeek 调用 + 配额 + key 选择 |
| `haifeng-app/.../controller/algorithm/pdf/PdfPlanController.java` | 新建 | SSE 端点 |
| `haifeng-common/src/test/java/.../service/ai/ApiKeyPoolTest.java` | 新建 | 单元测试 |
| `haifeng-common/src/test/java/.../service/ai/AiQuotaServiceTest.java` | 新建 | 单元测试 |
| `haifeng-app/src/test/java/.../service/impl/algorithm/pdf/AiChatServiceImplTest.java` | 新建 | 单元测试 |

---

## Task 1: 添加 webflux 依赖与配置

**Files:**
- Modify: `haifeng-common/pom.xml`
- Modify: `haifeng-app/src/main/resources/application.yml`
- Modify: `haifeng-app/src/main/resources/application-dev.yml`（如已存在 .env 引用）
- Modify: `.env`（仅本地，添加示例 key）

- [ ] **Step 1: 在 `haifeng-common/pom.xml` 添加 webflux 依赖**

在 `<!-- Spring Boot Web -->` 块下方插入：

```xml
        <!-- Spring Boot WebFlux（仅用作 WebClient HTTP 客户端） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
```

- [ ] **Step 2: 在 `haifeng-app/src/main/resources/application.yml` 顶层追加 deepseek 配置**

把现有内容保留，在文件末尾追加：

```yaml
deepseek:
  base-url: https://api.deepseek.com
  model: deepseek-v4-flash
  api-keys: ${DEEPSEEK_API_KEYS:}
  max-tokens: 4096
  temperature: 1.0
  key-cooldown-seconds: 300
  timeout-seconds: 60
```

- [ ] **Step 3: 在 `.env` 添加 `DEEPSEEK_API_KEYS` 占位**

如果 `.env` 已经有该字段则跳过。否则追加：

```
DEEPSEEK_API_KEYS=sk-replace-with-real-key
```

（多 key 用英文逗号分隔，例：`sk-aaa,sk-bbb,sk-ccc`）

- [ ] **Step 4: 验证依赖能解析**

Run: `mvn -pl haifeng-common -am dependency:resolve -q` 在项目根目录
Expected: 无报错，BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add haifeng-common/pom.xml haifeng-app/src/main/resources/application.yml .env
git commit -m "feat(pdf): add webflux dep and deepseek config block"
```

---

## Task 2: 添加业务码

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/response/ResultCode.java`

- [ ] **Step 1: 在 ResultCode 中追加业务码**

在 `EXPORT_FAILED(1030, "导出失败"),` 之后、`TOTP_REQUIRED(...)` 之前插入：

```java
    AI_QUOTA_EXCEEDED(1040, "今日 AI 调用次数已用完"),
    AI_ALL_KEYS_FAILED(1041, "AI 服务暂不可用，请稍后再试"),
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/response/ResultCode.java
git commit -m "feat(pdf): add AI quota and key failure result codes"
```

---

## Task 3: DeepSeekProperties 配置类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/config/DeepSeekProperties.java`

- [ ] **Step 1: 创建 DeepSeekProperties**

```java
package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek（OpenAI 兼容协议）配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {

    /** DeepSeek API base URL */
    private String baseUrl = "https://api.deepseek.com";

    /** 模型名 */
    private String model = "deepseek-v4-flash";

    /** API key 列表（application.yml 中用逗号分隔，Spring 自动拆分） */
    private List<String> apiKeys = new ArrayList<>();

    /** max_tokens */
    private Integer maxTokens = 4096;

    /** temperature */
    private Double temperature = 1.0;

    /** 单 key 失败后的冷却秒数 */
    private Integer keyCooldownSeconds = 300;

    /** 请求超时秒数 */
    private Integer timeoutSeconds = 60;
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/config/DeepSeekProperties.java
git commit -m "feat(pdf): add DeepSeekProperties config class"
```

---

## Task 4: DeepSeekWebClientConfig

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/config/DeepSeekWebClientConfig.java`

- [ ] **Step 1: 创建 WebClient Bean**

```java
package com.haifeng.common.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek WebClient 配置。
 * 不在此处注入 api-key，每次请求由 service 动态注入 Authorization header。
 */
@Configuration
@RequiredArgsConstructor
public class DeepSeekWebClientConfig {

    private final DeepSeekProperties properties;

    @Bean("deepSeekWebClient")
    public WebClient deepSeekWebClient() {
        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) timeout.toMillis())
                .responseTimeout(timeout)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout.toSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout.toSeconds(), TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/config/DeepSeekWebClientConfig.java
git commit -m "feat(pdf): add DeepSeek WebClient bean"
```

---

## Task 5: QuotaExceededException + 全局异常处理

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/exception/QuotaExceededException.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 QuotaExceededException**

```java
package com.haifeng.common.exception;

import com.haifeng.common.response.ResultCode;

/**
 * 当日 AI 调用次数超额异常。
 * 由 GlobalExceptionHandler 映射为 HTTP 429。
 */
public class QuotaExceededException extends BusinessException {

    public QuotaExceededException() {
        super(ResultCode.AI_QUOTA_EXCEEDED);
    }
}
```

- [ ] **Step 2: 在 GlobalExceptionHandler 中追加 handler**

在 `handleBusinessException` 之后插入：

```java
    /**
     * AI 配额超额 - 返回 HTTP 429
     */
    @ExceptionHandler(QuotaExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public R<Void> handleQuotaExceededException(QuotaExceededException e) {
        log.warn("AI 调用次数超额: {}", e.getMsg());
        return R.fail(e.getCode(), e.getMsg());
    }
```

注意：`QuotaExceededException` 是 `BusinessException` 的子类，Spring 优先匹配更具体的 handler，所以放在 `handleBusinessException` 之后即可。

- [ ] **Step 3: 编译验证**

Run: `mvn -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/exception/
git commit -m "feat(pdf): add QuotaExceededException with HTTP 429 mapping"
```

---

## Task 6: ApiKeyPool 单元测试（先写测试）

**Files:**
- Create: `haifeng-common/src/test/java/com/haifeng/common/service/ai/ApiKeyPoolTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ApiKeyPoolTest {

    private DeepSeekProperties properties;
    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

    @BeforeEach
    void setup() {
        properties = new DeepSeekProperties();
        properties.setKeyCooldownSeconds(300);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private ApiKeyPool buildPool(List<String> keys) {
        properties.setApiKeys(keys);
        ApiKeyPool pool = new ApiKeyPool(properties, redisTemplate);
        pool.init();
        return pool;
    }

    @Test
    void emptyKeys_failsFastOnInit() {
        properties.setApiKeys(Collections.emptyList());
        ApiKeyPool pool = new ApiKeyPool(properties, redisTemplate);
        assertThatThrownBy(pool::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sameUserId_returnsSameKey() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2", "k3"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        String first = pool.pickKey(123L);
        String second = pool.pickKey(123L);
        assertThat(first).isEqualTo(second);
    }

    @Test
    void differentUserIds_distributeAcrossKeys() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2", "k3"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        Set<String> chosen = new HashSet<>();
        for (long uid = 0; uid < 100; uid++) {
            chosen.add(pool.pickKey(uid));
        }
        assertThat(chosen.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void markUnhealthy_writesCooldownToRedis() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2"));
        pool.markUnhealthy("k1");
        verify(valueOps).set(eq("ai:key:cooldown:k1"), eq("1"), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void pickKey_skipsCooldownKey() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2"));
        // 强制 hash(0) -> "k1"，但 k1 在冷却中
        when(redisTemplate.hasKey("ai:key:cooldown:k1")).thenReturn(true);
        when(redisTemplate.hasKey("ai:key:cooldown:k2")).thenReturn(false);

        // 试探多个 userId 让首选命中 k1
        String picked = null;
        for (long uid = 0; uid < 50; uid++) {
            // 找到一个会被分到 k1 的 uid 即可
            picked = pool.pickKey(uid);
            assertThat(picked).isEqualTo("k2");
        }
    }

    @Test
    void allKeysCooldown_stillReturnsFirstChoice() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(true);

        String picked = pool.pickKey(123L);
        assertThat(picked).isIn("k1", "k2");
    }

    @Test
    void orderedFallback_returnsAllKeysWithFirstChoiceFirst() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2", "k3"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        List<String> ordered = pool.orderedFallback(123L);
        assertThat(ordered).hasSize(3);
        assertThat(ordered).containsExactlyInAnyOrder("k1", "k2", "k3");
        assertThat(ordered.get(0)).isEqualTo(pool.pickKey(123L));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn -pl haifeng-common -Dtest=ApiKeyPoolTest test`
Expected: FAIL（找不到 `ApiKeyPool` 类）

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/test/java/com/haifeng/common/service/ai/ApiKeyPoolTest.java
git commit -m "test(pdf): add ApiKeyPool unit tests (failing)"
```

---

## Task 7: 实现 ApiKeyPool

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/ai/ApiKeyPool.java`

- [ ] **Step 1: 实现 ApiKeyPool**

```java
package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API key 池：
 * - 一致性哈希按 userId 选 key（命中缓存）
 * - markUnhealthy 进入 Redis 冷却
 * - pickKey 跳过冷却中的 key；全部冷却时仍返回首选
 * - orderedFallback 返回首选 + 其余顺序 key（流式调用失败重试用）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyPool {

    private static final String COOLDOWN_KEY_PREFIX = "ai:key:cooldown:";

    private final DeepSeekProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private List<String> keys;

    @PostConstruct
    public void init() {
        List<String> raw = properties.getApiKeys();
        if (raw == null || raw.isEmpty()) {
            throw new IllegalStateException(
                    "deepseek.api-keys is empty; please set DEEPSEEK_API_KEYS in env or application.yml");
        }
        List<String> filtered = new ArrayList<>();
        for (String k : raw) {
            if (k != null && !k.isBlank()) {
                filtered.add(k.trim());
            }
        }
        if (filtered.isEmpty()) {
            throw new IllegalStateException("deepseek.api-keys contains only blanks");
        }
        this.keys = Collections.unmodifiableList(filtered);
        log.info("ApiKeyPool initialized with {} keys", keys.size());
    }

    /** 选 key：首选 = hash(userId) % N；若冷却则按顺序找下一个非冷却的；全部冷却返回首选 */
    public String pickKey(Long userId) {
        int n = keys.size();
        int firstIdx = indexFor(userId);
        for (int i = 0; i < n; i++) {
            String candidate = keys.get((firstIdx + i) % n);
            if (!isCoolingDown(candidate)) {
                return candidate;
            }
        }
        return keys.get(firstIdx);
    }

    /** 写冷却标记 */
    public void markUnhealthy(String key) {
        redisTemplate.opsForValue().set(
                COOLDOWN_KEY_PREFIX + key,
                "1",
                properties.getKeyCooldownSeconds().longValue(),
                TimeUnit.SECONDS);
        log.warn("ApiKey marked unhealthy: ...{}", maskKey(key));
    }

    /** 返回按"首选 → 其余"顺序排列的所有 key */
    public List<String> orderedFallback(Long userId) {
        int n = keys.size();
        int firstIdx = indexFor(userId);
        List<String> ordered = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ordered.add(keys.get((firstIdx + i) % n));
        }
        return ordered;
    }

    private int indexFor(Long userId) {
        long uid = userId == null ? 0L : userId;
        // Math.floorMod 处理负数
        return (int) Math.floorMod(uid, keys.size());
    }

    private boolean isCoolingDown(String key) {
        Boolean has = redisTemplate.hasKey(COOLDOWN_KEY_PREFIX + key);
        return Boolean.TRUE.equals(has);
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return key.substring(key.length() - 4);
    }
}
```

- [ ] **Step 2: 运行测试验证通过**

Run: `mvn -pl haifeng-common -Dtest=ApiKeyPoolTest test`
Expected: PASS — Tests run: 7, Failures: 0

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/ai/ApiKeyPool.java
git commit -m "feat(pdf): implement ApiKeyPool with consistent hashing and Redis cooldown"
```

---

## Task 8: AiQuotaService 单元测试（先写测试）

**Files:**
- Create: `haifeng-common/src/test/java/com/haifeng/common/service/ai/AiQuotaServiceTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.haifeng.common.service.ai;

import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AiQuotaServiceTest {

    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    private final SystemSettingsMapper settingsMapper = mock(SystemSettingsMapper.class);

    private AiQuotaService service;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new AiQuotaService(redisTemplate, settingsMapper);
    }

    @Test
    void firstCall_setsTtlToEndOfDay() {
        when(valueOps.get("sys:api_number")).thenReturn(null);
        when(settingsMapper.selectList(any())).thenReturn(Collections.singletonList(
                SystemSettings.builder().apiNumber(5).build()));
        when(valueOps.increment(anyString())).thenReturn(1L);

        service.incrAndCheck(42L);

        verify(redisTemplate).expireAt(anyString(), any(java.util.Date.class));
    }

    @Test
    void underLimit_passes() {
        when(valueOps.get("sys:api_number")).thenReturn(5);
        when(valueOps.increment(anyString())).thenReturn(3L);

        service.incrAndCheck(42L);
        // no exception
    }

    @Test
    void atLimit_passes() {
        when(valueOps.get("sys:api_number")).thenReturn(5);
        when(valueOps.increment(anyString())).thenReturn(5L);

        service.incrAndCheck(42L);
    }

    @Test
    void overLimit_throwsQuotaExceeded() {
        when(valueOps.get("sys:api_number")).thenReturn(5);
        when(valueOps.increment(anyString())).thenReturn(6L);

        assertThatThrownBy(() -> service.incrAndCheck(42L))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void apiNumberLimit_cachedInRedis_skipsDb() {
        when(valueOps.get("sys:api_number")).thenReturn(7);

        int limit = service.getApiNumberLimit();

        org.assertj.core.api.Assertions.assertThat(limit).isEqualTo(7);
        verifyNoInteractions(settingsMapper);
    }

    @Test
    void apiNumberLimit_cacheMiss_loadsFromDbAndCaches() {
        when(valueOps.get("sys:api_number")).thenReturn(null);
        when(settingsMapper.selectList(any())).thenReturn(Arrays.asList(
                SystemSettings.builder().apiNumber(9).build()));

        int limit = service.getApiNumberLimit();

        org.assertj.core.api.Assertions.assertThat(limit).isEqualTo(9);
        verify(valueOps).set(eq("sys:api_number"), eq(9), eq(5L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }

    @Test
    void apiNumberLimit_dbEmpty_fallbacksTo3() {
        when(valueOps.get("sys:api_number")).thenReturn(null);
        when(settingsMapper.selectList(any())).thenReturn(Collections.emptyList());

        int limit = service.getApiNumberLimit();

        org.assertj.core.api.Assertions.assertThat(limit).isEqualTo(3);
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn -pl haifeng-common -Dtest=AiQuotaServiceTest test`
Expected: FAIL（找不到 `AiQuotaService` 类）

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/test/java/com/haifeng/common/service/ai/AiQuotaServiceTest.java
git commit -m "test(pdf): add AiQuotaService unit tests (failing)"
```

---

## Task 9: 实现 AiQuotaService

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/ai/AiQuotaService.java`

- [ ] **Step 1: 实现 AiQuotaService**

```java
package com.haifeng.common.service.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 调用配额：
 * - INCR pdf:ai:quota:{userId}:{yyyyMMdd}（首次写入时设 TTL 到当日 23:59:59）
 * - 上限来源：system_settings.api_number（缓存 Redis 5 分钟，默认 3）
 * - 超额抛 QuotaExceededException（HTTP 429）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuotaService {

    private static final String QUOTA_KEY_PREFIX = "pdf:ai:quota:";
    private static final String API_NUMBER_CACHE_KEY = "sys:api_number";
    private static final long API_NUMBER_CACHE_TTL_MIN = 5L;
    private static final int DEFAULT_API_NUMBER = 3;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemSettingsMapper settingsMapper;

    /** 自增并校验。超额抛 QuotaExceededException。 */
    public void incrAndCheck(Long userId) {
        int limit = getApiNumberLimit();
        String key = quotaKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);
        long current = count == null ? 0L : count;

        if (current == 1L) {
            // 第一次 INCR：设 TTL 到当日末
            redisTemplate.expireAt(key, endOfTodayDate());
        }

        if (current > limit) {
            log.warn("AI quota exceeded for userId={}, current={}, limit={}", userId, current, limit);
            throw new QuotaExceededException();
        }
    }

    /** 上限：先查 Redis 缓存，再查 DB，DB 无记录兜底 DEFAULT_API_NUMBER */
    public int getApiNumberLimit() {
        Object cached = redisTemplate.opsForValue().get(API_NUMBER_CACHE_KEY);
        if (cached instanceof Number) {
            return ((Number) cached).intValue();
        }
        List<SystemSettings> rows = settingsMapper.selectList(Wrappers.<SystemSettings>lambdaQuery());
        int limit = DEFAULT_API_NUMBER;
        if (rows != null && !rows.isEmpty() && rows.get(0).getApiNumber() != null) {
            limit = rows.get(0).getApiNumber();
        }
        redisTemplate.opsForValue().set(API_NUMBER_CACHE_KEY, limit,
                API_NUMBER_CACHE_TTL_MIN, TimeUnit.MINUTES);
        return limit;
    }

    private String quotaKey(Long userId) {
        return QUOTA_KEY_PREFIX + userId + ":" + LocalDate.now().format(DATE_FMT);
    }

    private Date endOfTodayDate() {
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}
```

- [ ] **Step 2: 运行测试验证通过**

Run: `mvn -pl haifeng-common -Dtest=AiQuotaServiceTest test`
Expected: PASS — Tests run: 7, Failures: 0

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/ai/AiQuotaService.java
git commit -m "feat(pdf): implement AiQuotaService with daily Redis counter"
```

---

## Task 10: ChatMessage VO + AiChatRequestDTO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/ChatMessage.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/AiChatRequestDTO.java`

- [ ] **Step 1: 创建 ChatMessage**

```java
package com.haifeng.app.vo.algorithm.pdf;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAI 协议消息：role + content
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @NotBlank(message = "role 不能为空")
    @Pattern(regexp = "system|user|assistant", message = "role 必须是 system / user / assistant")
    private String role;

    @NotBlank(message = "content 不能为空")
    private String content;
}
```

- [ ] **Step 2: 创建 AiChatRequestDTO**

```java
package com.haifeng.app.dto.algorithm.pdf;

import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AiChatRequestDTO {

    @NotEmpty(message = "messages 不能为空")
    @Valid
    private List<ChatMessage> messages;
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/ \
        haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/
git commit -m "feat(pdf): add ChatMessage VO and AiChatRequestDTO"
```

---

## Task 11: AiChatService 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/AiChatService.java`

- [ ] **Step 1: 创建接口**

```java
package com.haifeng.app.service.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.AiChatRequestDTO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface AiChatService {

    /**
     * 流式调用 DeepSeek，返回 SSE 流。
     * @param userId 当前会员 ID
     * @param request 入参（仅 messages）
     */
    Flux<ServerSentEvent<String>> streamChat(Long userId, AiChatRequestDTO request);
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/AiChatService.java
git commit -m "feat(pdf): add AiChatService interface"
```

---

## Task 12: AiChatServiceImpl 单元测试（先写测试）

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImplTest.java`

- [ ] **Step 1: 写失败测试**

测试用 `WebClient` 的 `MockWebServer`（okhttp3）会引入新依赖；改用更轻量的方式：直接 mock `WebClient` 的 builder chain。这里我们采用第二种思路——把"实际 HTTP 调用部分"抽到一个 `protected` 方法 `callDeepSeekRaw(String key, String body)` 返回 `Flux<String>`，测试时 spy 这个方法。

```java
package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.AiChatRequestDTO;
import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.service.ai.AiQuotaService;
import com.haifeng.common.service.ai.ApiKeyPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

class AiChatServiceImplTest {

    private ApiKeyPool keyPool = mock(ApiKeyPool.class);
    private AiQuotaService quotaService = mock(AiQuotaService.class);
    private WebClient webClient = mock(WebClient.class);
    private DeepSeekProperties properties;

    @BeforeEach
    void setup() {
        properties = new DeepSeekProperties();
        properties.setModel("deepseek-v4-flash");
        properties.setMaxTokens(4096);
        properties.setTemperature(1.0);
    }

    /** 子类化以替换 callDeepSeekRaw */
    static class TestableImpl extends AiChatServiceImpl {
        private final List<Flux<String>> queue;
        private final AtomicInteger calls = new AtomicInteger(0);

        TestableImpl(ApiKeyPool keyPool, AiQuotaService quotaService,
                     WebClient webClient, DeepSeekProperties properties,
                     List<Flux<String>> queue) {
            super(keyPool, quotaService, webClient, properties);
            this.queue = queue;
        }

        @Override
        protected Flux<String> callDeepSeekRaw(String key, String body) {
            int idx = calls.getAndIncrement();
            return queue.get(Math.min(idx, queue.size() - 1));
        }

        int callCount() { return calls.get(); }
    }

    @Test
    void quotaExceeded_propagates() {
        doThrow(new com.haifeng.common.exception.QuotaExceededException())
                .when(quotaService).incrAndCheck(1L);

        TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
                Collections.singletonList(Flux.empty()));

        AiChatRequestDTO dto = new AiChatRequestDTO();
        dto.setMessages(Collections.singletonList(new ChatMessage("user", "hi")));

        StepVerifier.create(service.streamChat(1L, dto))
                .expectError(com.haifeng.common.exception.QuotaExceededException.class)
                .verify();
    }

    @Test
    void firstKeySucceeds_streamsAndDoesNotMarkUnhealthy() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList("k1", "k2"));

        TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
                Collections.singletonList(
                        Flux.just(
                                "{\"choices\":[{\"delta\":{\"content\":\"你\"}}]}",
                                "{\"choices\":[{\"delta\":{\"content\":\"好\"}}]}",
                                "[DONE]"
                        )));

        AiChatRequestDTO dto = new AiChatRequestDTO();
        dto.setMessages(Collections.singletonList(new ChatMessage("user", "hi")));

        StepVerifier.create(service.streamChat(1L, dto).map(ServerSentEvent::data))
                .expectNext("{\"content\":\"你\"}")
                .expectNext("{\"content\":\"好\"}")
                .expectNext("[DONE]")
                .verifyComplete();

        verify(keyPool, never()).markUnhealthy(anyString());
    }

    @Test
    void firstKeyFails_fallbackToSecond() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList("k1", "k2"));

        TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
                Arrays.asList(
                        Flux.error(new RuntimeException("401 Unauthorized")),
                        Flux.just("{\"choices\":[{\"delta\":{\"content\":\"ok\"}}]}", "[DONE]")
                ));

        AiChatRequestDTO dto = new AiChatRequestDTO();
        dto.setMessages(Collections.singletonList(new ChatMessage("user", "hi")));

        StepVerifier.create(service.streamChat(1L, dto).map(ServerSentEvent::data))
                .expectNext("{\"content\":\"ok\"}")
                .expectNext("[DONE]")
                .verifyComplete();

        verify(keyPool).markUnhealthy("k1");
    }

    @Test
    void allKeysFail_throwsBusinessException() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList("k1", "k2"));

        TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
                Arrays.asList(
                        Flux.error(new RuntimeException("fail1")),
                        Flux.error(new RuntimeException("fail2"))
                ));

        AiChatRequestDTO dto = new AiChatRequestDTO();
        dto.setMessages(Collections.singletonList(new ChatMessage("user", "hi")));

        StepVerifier.create(service.streamChat(1L, dto))
                .expectErrorMatches(t -> t instanceof com.haifeng.common.exception.BusinessException
                        && ((com.haifeng.common.exception.BusinessException) t).getCode() == 1041)
                .verify();

        verify(keyPool).markUnhealthy("k1");
        verify(keyPool).markUnhealthy("k2");
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn -pl haifeng-app -Dtest=AiChatServiceImplTest test`
Expected: FAIL（找不到 `AiChatServiceImpl` 类）

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImplTest.java
git commit -m "test(pdf): add AiChatServiceImpl unit tests (failing)"
```

---

## Task 13: 实现 AiChatServiceImpl

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImpl.java`

- [ ] **Step 1: 实现 service**

```java
package com.haifeng.app.service.impl.algorithm.pdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.haifeng.app.dto.algorithm.pdf.AiChatRequestDTO;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.ai.AiQuotaService;
import com.haifeng.common.service.ai.ApiKeyPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String CHAT_PATH = "/v1/chat/completions";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ApiKeyPool keyPool;
    private final AiQuotaService quotaService;
    private final WebClient webClient;
    private final DeepSeekProperties properties;

    public AiChatServiceImpl(ApiKeyPool keyPool,
                             AiQuotaService quotaService,
                             @Qualifier("deepSeekWebClient") WebClient webClient,
                             DeepSeekProperties properties) {
        this.keyPool = keyPool;
        this.quotaService = quotaService;
        this.webClient = webClient;
        this.properties = properties;
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChat(Long userId, AiChatRequestDTO request) {
        return Mono.fromRunnable(() -> quotaService.incrAndCheck(userId))
                .thenMany(Flux.defer(() -> doStream(userId, request)));
    }

    private Flux<ServerSentEvent<String>> doStream(Long userId, AiChatRequestDTO request) {
        List<String> keys = keyPool.orderedFallback(userId);
        String body = buildRequestBody(request.getMessages());

        return Flux.fromIterable(keys)
                .concatMap(key -> callDeepSeekRaw(key, body)
                        .onErrorResume(err -> {
                            log.warn("DeepSeek call failed with key ...{}: {}",
                                    maskKey(key), err.getMessage());
                            keyPool.markUnhealthy(key);
                            return Flux.error(err);
                        }))
                .onErrorResume(err -> {
                    // concatMap 一旦上一个 inner Flux 失败，下一个会继续；
                    // 真正"全部失败"是这里 Flux.fromIterable 全部走完仍出错时
                    log.error("All DeepSeek keys failed for userId={}", userId);
                    return Flux.error(new BusinessException(ResultCode.AI_ALL_KEYS_FAILED));
                })
                .switchOnFirst((signal, flux) -> {
                    // 占位：concatMap 已正确，这里不做额外处理
                    return flux;
                })
                .map(this::extractDeltaContent)
                .map(content -> ServerSentEvent.<String>builder().data(content).build());
    }

    /**
     * 真正的 HTTP 调用——返回 SSE 行（去掉 `data: ` 前缀，已是 JSON 或 `[DONE]`）。
     * protected 便于单测覆盖。
     */
    protected Flux<String> callDeepSeekRaw(String key, String body) {
        return webClient.post()
                .uri(CHAT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class);
    }

    private String buildRequestBody(List<ChatMessage> messages) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", properties.getModel());
        root.put("stream", true);
        root.put("max_tokens", properties.getMaxTokens());
        root.put("temperature", properties.getTemperature());

        ArrayNode arr = root.putArray("messages");
        // 提示词留空（按需求 MVP）
        ObjectNode sys = arr.addObject();
        sys.put("role", "system");
        sys.put("content", "");
        for (ChatMessage m : messages) {
            ObjectNode n = arr.addObject();
            n.put("role", m.getRole());
            n.put("content", m.getContent());
        }
        try {
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        }
    }

    /**
     * 把 OpenAI 流式 chunk 提炼成给前端的简洁数据。
     * 输入可能是 `{"choices":[{"delta":{"content":"x"}}]}` 或 `[DONE]`
     */
    private String extractDeltaContent(String chunk) {
        if (chunk == null || chunk.isBlank()) {
            return "";
        }
        String trimmed = chunk.trim();
        if ("[DONE]".equals(trimmed)) {
            return "[DONE]";
        }
        try {
            JsonNode node = MAPPER.readTree(trimmed);
            JsonNode delta = node.path("choices").path(0).path("delta").path("content");
            String content = delta.isMissingNode() || delta.isNull() ? "" : delta.asText("");
            ObjectNode out = MAPPER.createObjectNode();
            out.put("content", content);
            return MAPPER.writeValueAsString(out);
        } catch (Exception e) {
            log.debug("Skip non-JSON chunk: {}", trimmed);
            return "";
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return key.substring(key.length() - 4);
    }
}
```

- [ ] **Step 2: 运行测试验证通过**

Run: `mvn -pl haifeng-app -Dtest=AiChatServiceImplTest test`
Expected: PASS — Tests run: 4, Failures: 0

如果"全部 key 失败"测试不通过（concatMap 在中间错误时会终止，需要 `onErrorContinue` 才能继续到下一个 key）——修复方式：将 `concatMap` 内的 `onErrorResume` 改为返回 `Flux.empty()` 之前先 mark + 用一个外层标志位，或者改写为：

```java
return Flux.fromIterable(keys)
        .concatMap(key -> callDeepSeekRaw(key, body)
                .doOnError(err -> {
                    log.warn("DeepSeek call failed with key ...{}: {}", maskKey(key), err.getMessage());
                    keyPool.markUnhealthy(key);
                })
                .onErrorResume(err -> Flux.empty()))
        .switchIfEmpty(Flux.error(new BusinessException(ResultCode.AI_ALL_KEYS_FAILED)))
        .map(this::extractDeltaContent)
        .map(content -> ServerSentEvent.<String>builder().data(content).build());
```

如初版未通过，请用上面的版本替换 `doStream` 中对应代码块。

- [ ] **Step 3: 调整后再次运行测试**

Run: `mvn -pl haifeng-app -Dtest=AiChatServiceImplTest test`
Expected: PASS — Tests run: 4, Failures: 0

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImpl.java
git commit -m "feat(pdf): implement AiChatServiceImpl with key fallback"
```

---

## Task 14: PdfPlanController

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/pdf/PdfPlanController.java`

- [ ] **Step 1: 创建 controller**

```java
package com.haifeng.app.controller.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.AiChatRequestDTO;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/pdf")
@RequiredArgsConstructor
@RequireLogin
@RequireVip
public class PdfPlanController {

    private final AiChatService aiChatService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody AiChatRequestDTO dto) {
        Long userId = SecurityUtil.getCurrentMemberId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return aiChatService.streamChat(userId, dto);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/pdf/PdfPlanController.java
git commit -m "feat(pdf): add PdfPlanController with SSE chat stream endpoint"
```

---

## Task 15: 整体编译 & 测试 & 启动验证

**Files:** 无新增

- [ ] **Step 1: 全量编译**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 2: 全量单元测试**

Run: `mvn test`
Expected: 所有测试 PASS（含本次新增 ApiKeyPoolTest / AiQuotaServiceTest / AiChatServiceImplTest）

- [ ] **Step 3: 启动应用（人工验证可选）**

确认 `.env` 已设置 `DEEPSEEK_API_KEYS=sk-xxx[,sk-yyy]`。

Run: `mvn -pl haifeng-app spring-boot:run`
Expected: 启动日志含 `ApiKeyPool initialized with N keys`，无报错。

可选：用 `curl` 测一次（需要登录态 token）：
```
curl -N -X POST http://localhost:8080/api/v1/app/algorithm/pdf/chat/stream \
  -H "Authorization: Bearer <vip-user-token>" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"messages":[{"role":"user","content":"你好"}]}'
```
Expected: 收到 SSE 流；调用次数达到上限后，下一次返回 HTTP 429。

- [ ] **Step 4: 最终 commit（如有零碎改动）**

```bash
git status
# 若有未提交改动：
git add -A
git commit -m "chore(pdf): final integration verification"
```

---

## 验收清单

- [ ] `POST /api/v1/app/algorithm/pdf/chat/stream` 存在且返回 `text/event-stream`
- [ ] 类级 `@RequireLogin @RequireVip` 已加（非 VIP 返回 403）
- [ ] 入参为 `{messages: [{role,content}]}`，校验生效
- [ ] DeepSeek 配置走 `application.yml` + `.env`，model = `deepseek-v4-flash`
- [ ] 多 api-key 按 `hash(userId) % N` 选 key，失败切换 + 写 Redis 冷却
- [ ] 每用户每天上限 = `system_settings.api_number`（兜底 3），缓存 5 分钟
- [ ] 超额返回 HTTP 429 + body `code=1040`
- [ ] 全部 key 失败返回 HTTP 200（项目业务码模式）+ body `code=1041`
- [ ] 提示词留空（system message content = ""）
- [ ] 单测全绿：ApiKeyPoolTest（7 个）、AiQuotaServiceTest（7 个）、AiChatServiceImplTest（4 个）

---

## 范围外提醒（不要做）

- ❌ 不做 PDF 生成 / Thymeleaf / OpenHTMLtoPDF
- ❌ 不做多智能体 / tool calling
- ❌ 不做提示词工程
- ❌ 不建 api-key 数据库表
- ❌ 不写 Controller e2e 测试（项目其它 controller 也没写，保持一致）
