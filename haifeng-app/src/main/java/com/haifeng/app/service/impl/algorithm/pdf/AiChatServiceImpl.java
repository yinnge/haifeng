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
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
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
        return Mono.<Void>fromRunnable(() -> quotaService.incrAndCheck(userId))
                .thenMany(Flux.defer(() -> doStream(userId, request)));
    }

    private Flux<ServerSentEvent<String>> doStream(Long userId, AiChatRequestDTO request) {
        List<ModelProviderConfig> providers = keyPool.orderedFallback(userId);

        return tryProvider(providers, 0, request)
                .map(this::extractDeltaContent)
                .map(content -> ServerSentEvent.<String>builder().data(content).build());
    }

    /**
     * 顺序尝试 providers[index]；该 provider 失败则标记并递归到下一个；
     * 全部失败则发出 AI_ALL_KEYS_FAILED 错误。
     */
    private Flux<String> tryProvider(List<ModelProviderConfig> providers, int index, AiChatRequestDTO request) {
        if (index >= providers.size()) {
            return Flux.error(new BusinessException(ResultCode.AI_ALL_KEYS_FAILED));
        }
        ModelProviderConfig provider = providers.get(index);
        String body = buildRequestBody(request.getMessages(), provider.getModelName());
        return callDeepSeekRaw(provider.getApiKey(), body)
                .onErrorResume(err -> {
                    log.warn("DeepSeek call failed with provider id={}, key ...{}: {}",
                            provider.getId(), maskKey(provider.getApiKey()), err.getMessage());
                    keyPool.markUnhealthy(provider);
                    return tryProvider(providers, index + 1, request);
                });
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

    private String buildRequestBody(List<ChatMessage> messages, String modelName) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", modelName);
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
                .block(java.time.Duration.ofSeconds(properties.getTimeoutSeconds()));
    }

    private String buildSyncRequestBody(List<ChatMessage> messages, String modelName) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", modelName);
        root.put("stream", false);
        root.put("max_tokens", properties.getMaxTokens());
        root.put("temperature", properties.getTemperature());

        ArrayNode arr = root.putArray("messages");
        // 不预置空 system 消息（与 buildRequestBody 不同），因为 Map/Reduce 调用方会在 messages 中自带 system prompt
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

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return key.substring(key.length() - 4);
    }
}
