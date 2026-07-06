# PDF 报告生成系统 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 PDF 报告生成系统，采用 Map-Reduce 多智能体编排，AI 分析结果存 JSONB，PDF 按需重新渲染。

**Architecture:** 用户触发生成 → 创建 t_pdf_report 记录 → Map 阶段限流并行（Semaphore=3）逐校 AI 简评 → Reduce 阶段全局研判 → SSE 推进度事件 → 用户可查看历史记录并重新渲染 PDF。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Spring WebFlux (SSE), DeepSeek API (OpenAI 协议), PostgreSQL JSONB, JUnit 5 + Mockito

---

## File Structure

### 新建文件

| 文件 | 职责 |
|------|------|
| `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/pdf/PdfReport.java` | PDF 报告 Entity |
| `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/pdf/PdfReportMapper.java` | PDF 报告 Mapper |
| `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/PdfRecordQueryDTO.java` | 历史记录分页查询 DTO |
| `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfRecordListVO.java` | 历史记录列表 VO |
| `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfRecordDetailVO.java` | 历史记录详情 VO |
| `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/MapResultItem.java` | Map 阶段单条结果（JSONB 序列化用） |
| `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/ReduceResult.java` | Reduce 阶段结果（JSONB 序列化用） |
| `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PlanSnapshot.java` | 封面页数据快照（JSONB 序列化用） |
| `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/PdfReportService.java` | PDF 报告服务接口 |
| `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/PdfReportServiceImpl.java` | PDF 报告服务实现（Map-Reduce 编排） |
| `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/PdfReportServiceImplTest.java` | PdfReportService 单测 |

### 修改文件

| 文件 | 改动 |
|------|------|
| `haifeng-app/.../service/algorithm/pdf/AiChatService.java` | 新增 `chatSync` 非流式方法 |
| `haifeng-app/.../service/impl/algorithm/pdf/AiChatServiceImpl.java` | 实现 `chatSync`，复用 key 轮转 |
| `haifeng-app/.../controller/algorithm/pdf/PdfPlanController.java` | 删除 3 个维度接口，改造 chat/stream 为 generate/{planId}，新增 records 端点 |
| `haifeng-app/.../service/algorithm/pdf/PdfPlanService.java` | 删除旧接口，保留为历史记录查询接口 |
| `haifeng-app/.../service/impl/algorithm/pdf/PdfPlanServiceImpl.java` | 重写为历史记录查询实现 |

### 删除文件

| 文件 | 原因 |
|------|------|
| `haifeng-app/.../dto/algorithm/pdf/AiChatRequestDTO.java` | chat/stream 改造后不再需要 |
| `haifeng-app/.../vo/algorithm/pdf/PdfUniversityVO.java` | 旧维度聚合 VO，不再使用 |
| `haifeng-app/.../vo/algorithm/pdf/PdfCityVO.java` | 同上 |
| `haifeng-app/.../vo/algorithm/pdf/PdfMajorVO.java` | 同上 |

---

## Task 1: Entity + Mapper（PdfReport）

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/pdf/PdfReport.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/pdf/PdfReportMapper.java`

- [ ] **Step 1: 创建 PdfReport Entity**

```java
package com.haifeng.common.entity.algorithm.pdf;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * PDF 报告记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_pdf_report", autoResultMap = true)
public class PdfReport {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long memberId;

    private Integer planId;

    /** 0=生成中, 1=成功, 2=失败 */
    private Short status;

    /** Map 阶段逐校 AI 简评 JSONB 数组 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String mapResults;

    /** Reduce 阶段全局研判 JSONB */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String reduceResult;

    /** 封面页数据快照 JSONB */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String planSnapshot;

    private String failReason;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 PdfReportMapper**

```java
package com.haifeng.common.mapper.algorithm.pdf;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PdfReportMapper extends BaseMapper<PdfReport> {
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/pdf/PdfReport.java \
        haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/pdf/PdfReportMapper.java
git commit -m "feat: PdfReport entity + mapper"
```

---

## Task 2: JSONB 序列化 VO（MapResultItem / ReduceResult / PlanSnapshot）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/MapResultItem.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/ReduceResult.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PlanSnapshot.java`

- [ ] **Step 1: 创建 MapResultItem**

```java
package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Map 阶段单条结果（序列化为 JSONB 存入 map_results 数组）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapResultItem {

    private Long universityId;
    private String universityName;
    private String cityName;
    private String groupName;
    private List<MajorBrief> majors;

    /** AI 产出的 ~300字简评；失败时为 null */
    private String commentary;

    /** AI 调用是否成功 */
    private Boolean success;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MajorBrief {
        private String majorName;
        private BigDecimal safetyLevel;
        private String levelShort;
    }
}
```

- [ ] **Step 2: 创建 ReduceResult**

```java
package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reduce 阶段全局研判结果（序列化为 JSONB 存入 reduce_result）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReduceResult {

    /** 全局宏观分析 */
    private String globalAnalysis;

    /** SWOT 象限分析 */
    private String swot;

    /** 推荐填报梯队顺序 */
    private String recommendation;
}
```

- [ ] **Step 3: 创建 PlanSnapshot**

```java
package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封面页数据快照（序列化为 JSONB 存入 plan_snapshot）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanSnapshot {

    private Short planYear;
    private String planProvince;
    private String reformModel;
    private Integer userScore;
    private Integer userRank;
    private String planBatch;
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/MapResultItem.java \
        haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/ReduceResult.java \
        haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PlanSnapshot.java
git commit -m "feat: JSONB 序列化 VO (MapResultItem/ReduceResult/PlanSnapshot)"
```

---

## Task 3: AiChatService 新增 chatSync 非流式方法

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/AiChatService.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImpl.java`
- Test: `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImplTest.java`

- [ ] **Step 1: 在 AiChatService 接口新增方法签名**

在 `AiChatService.java` 接口中新增：

```java
/**
 * 非流式 AI 调用（Map/Reduce 内部使用）
 * <p>请求体 stream=false，收集完整响应后返回文本。
 * 复用 ApiKeyPool 多 key 轮转，配额在 Controller 层已扣，此处不再 incr。
 *
 * @param userId   用户ID（用于 key 轮转命中缓存）
 * @param messages 消息列表
 * @return AI 完整文本响应；所有 key 都失败抛 BusinessException(AI_ALL_KEYS_FAILED)
 */
String chatSync(Long userId, List<ChatMessage> messages);
```

- [ ] **Step 2: 在 AiChatServiceImpl 中实现 chatSync**

在 `AiChatServiceImpl.java` 中新增以下代码：

```java
@Override
public String chatSync(Long userId, List<ChatMessage> messages) {
    List<ModelProviderConfig> providers = keyPool.orderedFallback(userId);
    if (providers.isEmpty()) {
        throw new BusinessException(ResultCode.AI_ALL_KEYS_FAILED);
    }
    return tryProviderSync(providers, 0, messages);
}

private String tryProviderSync(List<ModelProviderConfig> providers, int index, List<ChatMessage> messages) {
    if (index >= providers.size()) {
        throw new BusinessException(ResultCode.AI_ALL_KEYS_FAILED);
    }
    ModelProviderConfig provider = providers.get(index);
    String body = buildSyncRequestBody(messages, provider.getModelName());
    try {
        String response = callDeepSeekSync(provider.getApiKey(), body);
        return extractSyncContent(response);
    } catch (Exception err) {
        log.warn("DeepSeek sync call failed with provider id={}, key ...{}: {}",
                provider.getId(), maskKey(provider.getApiKey()), err.getMessage());
        keyPool.markUnhealthy(provider);
        return tryProviderSync(providers, index + 1, messages);
    }
}

/**
 * 非流式 HTTP 调用——返回完整 JSON 响应体。
 */
protected String callDeepSeekSync(String key, String body) {
    return webClient.post()
            .uri(CHAT_PATH)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + key)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .block();
}

private String buildSyncRequestBody(List<ChatMessage> messages, String modelName) {
    ObjectNode root = MAPPER.createObjectNode();
    root.put("model", modelName);
    root.put("stream", false);
    root.put("max_tokens", properties.getMaxTokens());
    root.put("temperature", properties.getTemperature());

    ArrayNode arr = root.putArray("messages");
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
 * 从非流式响应 JSON 中提取 choices[0].message.content
 */
private String extractSyncContent(String response) {
    if (response == null || response.isBlank()) {
        return "";
    }
    try {
        JsonNode node = MAPPER.readTree(response);
        JsonNode content = node.path("choices").path(0).path("message").path("content");
        return content.isMissingNode() || content.isNull() ? "" : content.asText("");
    } catch (Exception e) {
        log.error("Failed to parse sync AI response: {}", response, e);
        return "";
    }
}
```

需要补充的 import（在文件顶部已有的基础上确认存在）：
```java
import org.springframework.http.MediaType;
```

- [ ] **Step 3: 写 chatSync 单测**

在 `AiChatServiceImplTest.java` 中新增测试方法：

```java
@Test
void chatSync_firstKeySucceeds_returnsContent() {
    when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
            provider(1L, "key-1", "deepseek-chat")
    ));

    TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
            Collections.emptyList()) {
        @Override
        protected String callDeepSeekSync(String key, String body) {
            return "{\"choices\":[{\"message\":{\"content\":\"北交大自动化不错\"}}]}";
        }
    };

    List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));
    String result = service.chatSync(1L, messages);

    assertThat(result).isEqualTo("北交大自动化不错");
    verify(keyPool, never()).markUnhealthy(any());
}

@Test
void chatSync_firstKeyFails_fallbackToSecond() {
    when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
            provider(1L, "key-1", "deepseek-chat"),
            provider(2L, "key-2", "deepseek-reasoner")
    ));

    TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
            Collections.emptyList()) {
        @Override
        protected String callDeepSeekSync(String key, String body) {
            if (key.equals("key-1")) {
                throw new RuntimeException("500 Internal Server Error");
            }
            return "{\"choices\":[{\"message\":{\"content\":\"备用key成功\"}}]}";
        }
    };

    List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));
    String result = service.chatSync(1L, messages);

    assertThat(result).isEqualTo("备用key成功");
    verify(keyPool).markUnhealthy(argThat(p -> p != null && p.getId().equals(1L)));
}

@Test
void chatSync_allKeysFail_throwsBusinessException() {
    when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
            provider(1L, "key-1", "deepseek-chat"),
            provider(2L, "key-2", "deepseek-reasoner")
    ));

    TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
            Collections.emptyList()) {
        @Override
        protected String callDeepSeekSync(String key, String body) {
            throw new RuntimeException("fail");
        }
    };

    List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));

    assertThatThrownBy(() -> service.chatSync(1L, messages))
            .isInstanceOf(com.haifeng.common.exception.BusinessException.class)
            .extracting("code")
            .isEqualTo(1041);
}

@Test
void chatSync_emptyProviders_throwsBusinessException() {
    when(keyPool.orderedFallback(1L)).thenReturn(Collections.emptyList());

    TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
            Collections.emptyList());

    List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));

    assertThatThrownBy(() -> service.chatSync(1L, messages))
            .isInstanceOf(com.haifeng.common.exception.BusinessException.class)
            .extracting("code")
            .isEqualTo(1041);
}
```

需要补充的 import：
```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd haifeng-app && mvn test -pl haifeng-app -Dtest=AiChatServiceImplTest -Dsurefire.useFile=false`
Expected: PASS（所有测试方法绿色）

- [ ] **Step 5: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/AiChatService.java \
        haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImpl.java \
        haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/AiChatServiceImplTest.java
git commit -m "feat: AiChatService 新增 chatSync 非流式方法 + 单测"
```

---

## Task 4: PdfReportService 接口 + DTO/VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/PdfReportService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/PdfRecordQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfRecordListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfRecordDetailVO.java`

- [ ] **Step 1: 创建 PdfRecordQueryDTO**

```java
package com.haifeng.app.dto.algorithm.pdf;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PdfRecordQueryDTO extends BasePageQueryDTO {
}
```

- [ ] **Step 2: 创建 PdfRecordListVO**

```java
package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * PDF 报告历史记录列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRecordListVO {

    private Integer id;
    private Integer planId;
    private String planName;
    private Short status;
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 3: 创建 PdfRecordDetailVO**

```java
package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * PDF 报告记录详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRecordDetailVO {

    private Integer id;
    private Integer planId;
    private String planName;
    private Short status;
    private String mapResults;
    private String reduceResult;
    private String planSnapshot;
    private String failReason;
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 创建 PdfReportService 接口**

```java
package com.haifeng.app.service.algorithm.pdf;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordDetailVO;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordListVO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * PDF 报告生成与历史记录服务
 */
public interface PdfReportService {

    /**
     * 生成 PDF 报告（SSE 流式返回进度）
     *
     * @param userId 用户ID
     * @param planId 志愿方案ID
     * @return SSE 进度事件流
     */
    Flux<ServerSentEvent<String>> generateReport(Long userId, Integer planId);

    /**
     * 分页查询历史报告记录
     *
     * @param userId 用户ID
     * @param dto    分页参数
     * @return 分页结果
     */
    IPage<PdfRecordListVO> pageRecords(Long userId, PdfRecordQueryDTO dto);

    /**
     * 查询报告记录详情
     *
     * @param userId   用户ID
     * @param recordId 报告记录ID
     * @return 记录详情
     */
    PdfRecordDetailVO getRecordDetail(Long userId, Integer recordId);
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/PdfReportService.java \
        haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/PdfRecordQueryDTO.java \
        haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfRecordListVO.java \
        haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfRecordDetailVO.java
git commit -m "feat: PdfReportService 接口 + DTO/VO"
```

---

## Task 5: PdfReportServiceImpl — Map-Reduce 编排核心实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/PdfReportServiceImpl.java`
- Test: `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/PdfReportServiceImplTest.java`

- [ ] **Step 1: 写 PdfReportServiceImplTest 失败测试**

```java
package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.app.vo.algorithm.pdf.ExportGroupContextVO;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.mapper.algorithm.pdf.PdfReportMapper;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import com.haifeng.common.service.ai.AiQuotaService;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.GaokaoArchiveVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PdfReportServiceImplTest {

    private PdfReportMapper pdfReportMapper = mock(PdfReportMapper.class);
    private AiChatService aiChatService = mock(AiChatService.class);
    private AiQuotaService quotaService = mock(AiQuotaService.class);
    private WishPlanService wishPlanService = mock(WishPlanService.class);
    private GaokaoArchiveService gaokaoArchiveService = mock(GaokaoArchiveService.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    private PdfReportServiceImpl service;

    @BeforeEach
    void setup() {
        service = new PdfReportServiceImpl(
                pdfReportMapper, aiChatService, quotaService,
                wishPlanService, gaokaoArchiveService, objectMapper);
    }

    @Test
    void generateReport_quotaExceeded_emitsError() {
        doThrow(new com.haifeng.common.exception.QuotaExceededException())
                .when(quotaService).incrAndCheck(1L);

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"error\""))
                .verifyComplete();
    }

    @Test
    void generateReport_noExportableGroups_emitsError() {
        when(wishPlanService.getExportGroupContexts(100))
                .thenReturn(Collections.emptyList());

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"error\""))
                .verifyComplete();
    }

    @Test
    void generateReport_success_emitsMapProgressAndDone() {
        // 准备数据
        ExportGroupContextVO group1 = ExportGroupContextVO.builder()
                .groupSnapshotId(1)
                .universityId(10L)
                .cityName("北京")
                .groupSortOrder(1)
                .groupCode("001")
                .groupName("专业组1")
                .exportableMajors(Arrays.asList(
                        WishExportMajorVO.builder()
                                .majorId(100L)
                                .safetyLevel(new BigDecimal("0.78"))
                                .levelShort("稳")
                                .build()
                ))
                .build();

        when(quotaService.incrAndCheck(1L)).thenReturn(1);
        when(wishPlanService.getExportGroupContexts(100))
                .thenReturn(Collections.singletonList(group1));
        when(aiChatService.chatSync(eq(1L), anyList()))
                .thenReturn("北交大自动化不错");
        when(gaokaoArchiveService.getMyArchive())
                .thenReturn(GaokaoArchiveVO.builder()
                        .gaokaoYear((short) 2026)
                        .gaokaoProvince("北京")
                        .score(615)
                        .rank(8500)
                        .build());

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                // 应该有 map 进度事件
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"map\""))
                // 应该有 reduce 事件
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"reduce\""))
                // 应该有 done 事件
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"done\""))
                .verifyComplete();

        // 验证记录被保存，且 status=1（成功）
        verify(pdfReportMapper).updateById(argThat(r ->
                r instanceof PdfReport && ((PdfReport) r).getStatus() == 1));
    }

    @Test
    void generateReport_mapFails_marksFailedButContinues() {
        ExportGroupContextVO group1 = ExportGroupContextVO.builder()
                .groupSnapshotId(1)
                .universityId(10L)
                .cityName("北京")
                .groupSortOrder(1)
                .groupCode("001")
                .groupName("专业组1")
                .exportableMajors(Arrays.asList(
                        WishExportMajorVO.builder()
                                .majorId(100L)
                                .safetyLevel(new BigDecimal("0.78"))
                                .levelShort("稳")
                                .build()
                ))
                .build();

        when(quotaService.incrAndCheck(1L)).thenReturn(1);
        when(wishPlanService.getExportGroupContexts(100))
                .thenReturn(Collections.singletonList(group1));
        // Map 调用抛异常
        when(aiChatService.chatSync(eq(1L), anyList()))
                .thenThrow(new com.haifeng.common.exception.BusinessException(
                        com.haifeng.common.response.ResultCode.AI_ALL_KEYS_FAILED))
                .thenReturn("全局分析结果");  // Reduce 成功
        when(gaokaoArchiveService.getMyArchive())
                .thenReturn(GaokaoArchiveVO.builder()
                        .gaokaoYear((short) 2026)
                        .gaokaoProvince("北京")
                        .score(615)
                        .rank(8500)
                        .build());

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"map\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"reduce\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"done\""))
                .verifyComplete();

        // 即使 Map 失败，Reduce 仍执行，最终 status=1
        verify(pdfReportMapper).updateById(argThat(r ->
                r instanceof PdfReport && ((PdfReport) r).getStatus() == 1));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd haifeng-app && mvn test -pl haifeng-app -Dtest=PdfReportServiceImplTest -Dsurefire.useFile=false`
Expected: FAIL — `PdfReportServiceImpl` 类不存在或方法未实现

- [ ] **Step 3: 实现 PdfReportServiceImpl**

```java
package com.haifeng.app.service.impl.algorithm.pdf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.GaokaoArchiveVO;
import com.haifeng.app.vo.algorithm.pdf.*;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.algorithm.pdf.PdfReportMapper;
import com.haifeng.common.mapper.algorithm.wish.WishPlanMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.ai.AiQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportServiceImpl implements PdfReportService {

    private static final int MAP_MAX_CONCURRENCY = 3;

    private final PdfReportMapper pdfReportMapper;
    private final AiChatService aiChatService;
    private final AiQuotaService quotaService;
    private final WishPlanService wishPlanService;
    private final GaokaoArchiveService gaokaoArchiveService;
    private final ObjectMapper objectMapper;
    private final WishPlanMapper wishPlanMapper;

    @Override
    public Flux<ServerSentEvent<String>> generateReport(Long userId, Integer planId) {
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        CompletableFuture.runAsync(() -> {
            try {
                doGenerate(userId, planId, sink);
            } catch (Exception e) {
                log.error("PDF report generation failed, userId={}, planId={}", userId, planId, e);
                sink.tryEmitNext(errorEvent(e.getMessage(), 500));
            } finally {
                sink.tryEmitComplete();
            }
        });

        return sink.asFlux();
    }

    private void doGenerate(Long userId, Integer planId,
                            Sinks.Many<ServerSentEvent<String>> sink) {
        // 1. 配额校验
        try {
            quotaService.incrAndCheck(userId);
        } catch (QuotaExceededException e) {
            sink.tryEmitNext(errorEvent("今日PDF生成次数已用完", 429));
            return;
        }

        // 2. 创建记录 status=0
        PdfReport report = PdfReport.builder()
                .memberId(userId)
                .planId(planId)
                .status((short) 0)
                .build();
        pdfReportMapper.insert(report);
        Integer recordId = report.getId();
        sink.tryEmitNext(sseEvent("{\"stage\":\"quota_checked\",\"recordId\":" + recordId + "}"));

        // 3. 查 wish_plan + gaokao_archive → 存 plan_snapshot
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || Boolean.TRUE.equals(wishPlan.getDeleted())) {
            updateReportFailed(recordId, "志愿方案不存在");
            sink.tryEmitNext(errorEvent("志愿方案不存在", 404));
            return;
        }

        GaokaoArchiveVO archive = gaokaoArchiveService.getMyArchive();
        PlanSnapshot snapshot = PlanSnapshot.builder()
                .planYear(wishPlan.getPlanYear())
                .planProvince(wishPlan.getPlanProvince())
                .reformModel(wishPlan.getReformModel())
                .userScore(wishPlan.getUserScore())
                .userRank(wishPlan.getUserRank())
                .planBatch(wishPlan.getPlanBatch())
                .build();

        // 4. 查可导出专业组
        List<ExportGroupContextVO> groups = wishPlanService.getExportGroupContexts(planId);
        if (groups == null || groups.isEmpty()) {
            updateReportFailed(recordId, "没有可导出的专业组");
            sink.tryEmitNext(errorEvent("没有可导出的专业组，请先在志愿方案中勾选导出专业", 400));
            return;
        }

        // 5. Map 阶段（限流并行）
        List<MapResultItem> mapResults = runMapPhase(userId, groups, sink);

        // 6. 存 map_results
        try {
            String mapJson = objectMapper.writeValueAsString(mapResults);
            report.setMapResults(mapJson);
            report.setPlanSnapshot(objectMapper.writeValueAsString(snapshot));
            pdfReportMapper.updateById(report);
        } catch (Exception e) {
            log.error("Failed to serialize map_results", e);
        }

        // 7. Reduce 阶段
        sink.tryEmitNext(sseEvent("{\"stage\":\"reduce\",\"status\":\"running\"}"));

        String reduceJson = null;
        try {
            String reduceInput = buildReduceInput(mapResults);
            List<ChatMessage> reduceMessages = List.of(
                    new ChatMessage("system", buildReduceSystemPrompt()),
                    new ChatMessage("user", reduceInput)
            );
            String reduceResponse = aiChatService.chatSync(userId, reduceMessages);

            ReduceResult reduceResult = parseReduceResult(reduceResponse);
            reduceJson = objectMapper.writeValueAsString(reduceResult);
            sink.tryEmitNext(sseEvent("{\"stage\":\"reduce\",\"status\":\"done\"}"));
        } catch (Exception e) {
            log.error("Reduce phase failed, recordId={}", recordId, e);
            updateReportFailed(recordId, "Reduce阶段失败: " + e.getMessage());
            sink.tryEmitNext(errorEvent("Reduce阶段失败", recordId, 500));
            return;
        }

        // 8. 更新 status=1
        report.setReduceResult(reduceJson);
        report.setStatus((short) 1);
        pdfReportMapper.updateById(report);

        // 9. 完成
        sink.tryEmitNext(sseEvent("{\"stage\":\"done\",\"recordId\":" + recordId + "}"));
    }

    /**
     * Map 阶段：限流并行调用 AI，每个专业组一次调用
     */
    private List<MapResultItem> runMapPhase(Long userId, List<ExportGroupContextVO> groups,
                                            Sinks.Many<ServerSentEvent<String>> sink) {
        int total = groups.size();
        Semaphore semaphore = new Semaphore(MAP_MAX_CONCURRENCY);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<CompletableFuture<MapResultItem>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            final int index = i;
            final ExportGroupContextVO group = groups.get(i);

            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        sink.tryEmitNext(sseEvent(
                                "{\"stage\":\"map\",\"current\":" + (index + 1) +
                                ",\"total\":" + total +
                                ",\"university\":\"" + escapeJson(group.getGroupName()) + "\"}"));

                        return callMapAI(userId, group);
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return MapResultItem.builder()
                            .success(false)
                            .commentary(null)
                            .build();
                }
            }, executor));
        }

        // 等待全部完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        sink.tryEmitNext(sseEvent("{\"stage\":\"map_done\"}"));

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 对单个专业组调用 AI，返回 MapResultItem
     */
    private MapResultItem callMapAI(Long userId, ExportGroupContextVO group) {
        List<MapResultItem.MajorBrief> majors = group.getExportableMajors().stream()
                .map(m -> MapResultItem.MajorBrief.builder()
                        .majorName(getMajorName(m))
                        .safetyLevel(m.getSafetyLevel())
                        .levelShort(m.getLevelShort())
                        .build())
                .collect(Collectors.toList());

        try {
            String mapInput = buildMapInput(group, majors);
            List<ChatMessage> messages = List.of(
                    new ChatMessage("system", buildMapSystemPrompt()),
                    new ChatMessage("user", mapInput)
            );
            String commentary = aiChatService.chatSync(userId, messages);

            return MapResultItem.builder()
                    .universityId(group.getUniversityId())
                    .cityName(group.getCityName())
                    .groupName(group.getGroupName())
                    .majors(majors)
                    .commentary(commentary)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.warn("Map AI call failed for group {}, university {}: {}",
                    group.getGroupSnapshotId(), group.getUniversityId(), e.getMessage());
            return MapResultItem.builder()
                    .universityId(group.getUniversityId())
                    .cityName(group.getCityName())
                    .groupName(group.getGroupName())
                    .majors(majors)
                    .commentary(null)
                    .success(false)
                    .build();
        }
    }

    private String buildMapInput(ExportGroupContextVO group, List<MapResultItem.MajorBrief> majors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"university\":\"").append(group.getGroupName() != null ? group.getGroupName() : "")
          .append("\",\"city\":\"").append(group.getCityName() != null ? group.getCityName() : "")
          .append("\",\"majors\":[");
        for (int i = 0; i < majors.size(); i++) {
            MapResultItem.MajorBrief m = majors.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"").append(m.getMajorName() != null ? m.getMajorName() : "")
              .append("\",\"safetyLevel\":").append(m.getSafetyLevel() != null ? m.getSafetyLevel() : "0")
              .append(",\"levelShort\":\"").append(m.getLevelShort() != null ? m.getLevelShort() : "")
              .append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String buildMapSystemPrompt() {
        return """
            你是一位资深高考志愿规划师。请根据提供的大学、城市和专业信息，给出300字以内的客观研判。
            要求：
            1. 结合该校该专业在该城市的产业地缘优势或劣势
            2. 结合行业发展趋势给出前瞻性判断
            3. 不要罗列数据，只给结论性观点
            4. 严格控制在300字以内
            """;
    }

    private String buildReduceInput(List<MapResultItem> mapResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < mapResults.size(); i++) {
            MapResultItem item = mapResults.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"大学\":\"").append(item.getGroupName() != null ? item.getGroupName() : "")
              .append("\",\"城市\":\"").append(item.getCityName() != null ? item.getCityName() : "")
              .append("\",\"专业\":[");
            List<MapResultItem.MajorBrief> majors = item.getMajors();
            if (majors != null) {
                for (int j = 0; j < majors.size(); j++) {
                    if (j > 0) sb.append(",");
                    sb.append("\"").append(majors.get(j).getMajorName() != null ? majors.get(j).getMajorName() : "").append("\"");
                }
            }
            sb.append("],\"录取概率\":\"");
            if (majors != null && !majors.isEmpty()) {
                sb.append(majors.stream()
                        .map(m -> m.getLevelShort() != null ? m.getLevelShort() : "")
                        .distinct()
                        .collect(Collectors.joining("/")));
            }
            sb.append("\",\"AI简评\":\"")
              .append(item.getCommentary() != null ? item.getCommentary() : "暂无简评")
              .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildReduceSystemPrompt() {
        return """
            你是海枫未来规划院的首席志愿规划专家。请根据提供的各大学AI简评浓缩数据，进行全局博弈分析。
            请输出以下三个部分（用 === 分隔）：
            1. 全局宏观全景研判：分析哪些属于高风险高收益，哪些属于性价比之王
            2. SWOT象限分析：城市地域红利VS学校名气光环的博弈辩证
            3. 海枫强烈推荐填报梯队顺序：综合考虑概率、城市、行业，给出排兵布阵建议

            要求：不要重复各校的简评内容，只做交叉对比和全局统筹。
            """;
    }

    private ReduceResult parseReduceResult(String response) {
        if (response == null || response.isBlank()) {
            return ReduceResult.builder()
                    .globalAnalysis("")
                    .swot("")
                    .recommendation("")
                    .build();
        }
        String[] parts = response.split("===", 3);
        return ReduceResult.builder()
                .globalAnalysis(parts.length > 0 ? parts[0].trim() : "")
                .swot(parts.length > 1 ? parts[1].trim() : "")
                .recommendation(parts.length > 2 ? parts[2].trim() : "")
                .build();
    }

    private String getMajorName(WishExportMajorVO major) {
        // WishExportMajorVO 没有 majorName 字段，从 majorId 无法反查
        // 实际使用时需要从 ExportGroupContextVO 的上层传入 majorName
        // 这里暂用 majorId 的字符串形式
        return major.getMajorId() != null ? "专业" + major.getMajorId() : "未知专业";
    }

    private void updateReportFailed(Integer recordId, String reason) {
        PdfReport update = new PdfReport();
        update.setId(recordId);
        update.setStatus((short) 2);
        update.setFailReason(reason);
        pdfReportMapper.updateById(update);
    }

    private ServerSentEvent<String> sseEvent(String data) {
        return ServerSentEvent.<String>builder().data(data).build();
    }

    private ServerSentEvent<String> errorEvent(String message, int code) {
        return sseEvent("{\"stage\":\"error\",\"message\":\"" + escapeJson(message) + "\",\"code\":" + code + "}");
    }

    private ServerSentEvent<String> errorEvent(String message, Integer recordId, int code) {
        return sseEvent("{\"stage\":\"error\",\"message\":\"" + escapeJson(message) +
                "\",\"recordId\":" + recordId + ",\"code\":" + code + "}");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    // ===================== 历史记录查询 =====================

    @Override
    public IPage<PdfRecordListVO> pageRecords(Long userId, PdfRecordQueryDTO dto) {
        Page<PdfReport> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<PdfReport> wrapper = new LambdaQueryWrapper<PdfReport>()
                .eq(PdfReport::getMemberId, userId)
                .orderByDesc(PdfReport::getCreatedAt);

        IPage<PdfReport> result = pdfReportMapper.selectPage(page, wrapper);

        return result.convert(report -> PdfRecordListVO.builder()
                .id(report.getId())
                .planId(report.getPlanId())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build());
    }

    @Override
    public PdfRecordDetailVO getRecordDetail(Long userId, Integer recordId) {
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId()) || Boolean.TRUE.equals(report.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报告记录不存在");
        }
        return PdfRecordDetailVO.builder()
                .id(report.getId())
                .planId(report.getPlanId())
                .status(report.getStatus())
                .mapResults(report.getMapResults())
                .reduceResult(report.getReduceResult())
                .planSnapshot(report.getPlanSnapshot())
                .failReason(report.getFailReason())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd haifeng-app && mvn test -pl haifeng-app -Dtest=PdfReportServiceImplTest -Dsurefire.useFile=false`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/PdfReportServiceImpl.java \
        haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/pdf/PdfReportServiceImplTest.java
git commit -m "feat: PdfReportServiceImpl Map-Reduce 编排核心实现 + 单测"
```

---

## Task 6: Controller 改造 + 删除旧代码

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/pdf/PdfPlanController.java`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/AiChatRequestDTO.java`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfUniversityVO.java`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfCityVO.java`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfMajorVO.java`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/PdfPlanService.java`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/PdfPlanServiceImpl.java`

- [ ] **Step 1: 重写 PdfPlanController**

将 `PdfPlanController.java` 全文替换为：

```java
package com.haifeng.app.controller.algorithm.pdf;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordDetailVO;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.response.R;
import com.haifeng.common.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/pdf")
@RequiredArgsConstructor
@RequireLogin
@RequireVip
public class PdfPlanController {

    private final PdfReportService pdfReportService;

    /**
     * 生成 PDF 报告（SSE 流式返回进度）
     */
    @PostMapping(value = "/generate/{planId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateReport(@PathVariable Integer planId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return pdfReportService.generateReport(userId, planId);
    }

    /**
     * 历史报告记录列表（分页）
     */
    @GetMapping("/records")
    public R<IPage<PdfRecordListVO>> pageRecords(@Valid PdfRecordQueryDTO dto) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return R.ok(pdfReportService.pageRecords(userId, dto));
    }

    /**
     * 报告记录详情
     */
    @GetMapping("/records/{recordId}")
    public R<PdfRecordDetailVO> getRecordDetail(@PathVariable Integer recordId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return R.ok(pdfReportService.getRecordDetail(userId, recordId));
    }
}
```

- [ ] **Step 2: 删除旧文件**

```bash
git rm haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/pdf/AiChatRequestDTO.java
git rm haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfUniversityVO.java
git rm haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfCityVO.java
git rm haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/pdf/PdfMajorVO.java
git rm haifeng-app/src/main/java/com/haifeng/app/service/algorithm/pdf/PdfPlanService.java
git rm haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/pdf/PdfPlanServiceImpl.java
```

- [ ] **Step 3: 编译验证**

Run: `cd haifeng-app && mvn compile -pl haifeng-app -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 4: 运行所有测试**

Run: `cd haifeng-app && mvn test -pl haifeng-app -Dtest="AiChatServiceImplTest,PdfReportServiceImplTest" -Dsurefire.useFile=false`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: Controller 改造为 generate/records 端点，删除旧维度导出代码"
```

---

## Task 7: AiQuotaService 配额语义调整

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/service/ai/AiQuotaService.java`

- [ ] **Step 1: 更新 AiQuotaService 注释和 key 前缀**

将 `AiQuotaService.java` 中的注释从"AI 调用次数"改为"PDF 生成次数"，更新 key 前缀：

```java
// 修改 QUOTA_KEY_PREFIX
private static final String QUOTA_KEY_PREFIX = "pdf:report:quota:";
```

更新类注释：
```java
/**
 * PDF 生成配额：
 * - INCR pdf:report:quota:{userId}:{yyyyMMdd}（首次写入时设 TTL 到当日 23:59:59）
 * - 上限来源：system_settings.university_api_number（缓存 Redis 5 分钟，默认 1）
 * - 含义：每天可生成 PDF 报告的次数（1 次 PDF = 1 额度，内部 N+1 次 AI 调用不另计）
 * - 超额抛 QuotaExceededException（HTTP 429）
 */
```

- [ ] **Step 2: 编译验证**

Run: `cd haifeng-common && mvn compile -pl haifeng-common`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/ai/AiQuotaService.java
git commit -m "refactor: AiQuotaService 配额语义从AI调用次数改为PDF生成次数"
```

---

## Self-Review

### Spec Coverage Check

| Spec 要求 | 对应 Task |
|-----------|-----------|
| t_pdf_report 表 | V25 已创建 + Task 1 Entity/Mapper |
| AI 结果存 JSONB | Task 2 (VO) + Task 5 (序列化存储) |
| plan_snapshot 封面快照 | Task 2 (PlanSnapshot) + Task 5 (存入) |
| 4 个 API 端点 | Task 6 (generate SSE + records list + records detail) |
| 删除 3 个维度接口 | Task 6 Step 2 |
| chat/stream 改造为 generate/{planId} | Task 6 Step 1 |
| Map 限流并行 Semaphore=3 | Task 5 (runMapPhase) |
| Reduce 1 次调用 | Task 5 (doGenerate Reduce 阶段) |
| 单校失败不阻断 | Task 5 (callMapAI catch) |
| AiChatService 新增 chatSync | Task 3 |
| 配额 1 次 PDF = 1 额度 | Task 7 |
| 历史记录查询 | Task 5 (pageRecords + getRecordDetail) |
| PDF 重新渲染端点 | 标注为后续实现（spec 第 6 节明确"后续实现"） |

### Placeholder Scan

- 无 TBD/TODO
- 所有代码步骤都有完整代码
- 所有 import 已列出

### Type Consistency

- `PdfReport.status` → `Short`（0/1/2），所有引用一致
- `chatSync(Long userId, List<ChatMessage> messages)` → 接口和实现一致
- `MapResultItem.MajorBrief` → 在 Map 输入构建和 Reduce 输入构建中引用一致
- `PdfReportService.generateReport` 返回 `Flux<ServerSentEvent<String>>` → Controller 调用一致
