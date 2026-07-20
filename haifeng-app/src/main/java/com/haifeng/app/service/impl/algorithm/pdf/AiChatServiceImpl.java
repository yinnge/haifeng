package com.haifeng.app.service.impl.algorithm.pdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.ai.ApiKeyPool;
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String CHAT_PATH = "/v1/chat/completions";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ApiKeyPool keyPool;
    private final WebClient webClient;
    private final DeepSeekProperties properties;

    public AiChatServiceImpl(ApiKeyPool keyPool,
                             @Qualifier("deepSeekWebClient") WebClient webClient,
                             DeepSeekProperties properties) {
        this.keyPool = keyPool;
        this.webClient = webClient;
        this.properties = properties;
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
            String response = callDeepSeekSync(provider.getBaseUrl(), provider.getApiKey(), body);
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
    protected String callDeepSeekSync(String baseUrl, String key, String body) {
        return webClient.post()
                .uri(baseUrl + CHAT_PATH)
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

        // 禁用思考模式，避免返回思考过程内容
        ObjectNode thinking = root.putObject("thinking");
        thinking.put("type", "disabled");

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
     *
     * @throws BusinessException 当响应为空或 JSON 解析失败时抛出，由上层 callMapAI/Reduce 的 catch 统一降级处理
     */
    private String extractSyncContent(String response) {
        if (response == null || response.isBlank()) {
            throw new BusinessException(ResultCode.AI_ALL_KEYS_FAILED, "AI 响应为空");
        }
        try {
            JsonNode node = MAPPER.readTree(response);
            JsonNode content = node.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new BusinessException(ResultCode.AI_ALL_KEYS_FAILED, "AI 响应缺少 choices[0].message.content");
            }
            return content.asText("");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse sync AI response: {}", response, e);
            throw new BusinessException(ResultCode.AI_ALL_KEYS_FAILED, "AI 响应 JSON 解析失败: " + e.getMessage());
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return key.substring(key.length() - 4);
    }
}
