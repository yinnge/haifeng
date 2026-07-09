package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.service.ai.AiQuotaService;
import com.haifeng.common.service.ai.ApiKeyPool;
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    /** 子类化以替换 callDeepSeekSync */
    static class SyncTestableImpl extends AiChatServiceImpl {
        private String syncResponse;
        private RuntimeException syncError;

        SyncTestableImpl(ApiKeyPool keyPool, AiQuotaService quotaService,
                         WebClient webClient, DeepSeekProperties properties) {
            super(keyPool, quotaService, webClient, properties);
        }

        void setSyncResponse(String response) {
            this.syncResponse = response;
            this.syncError = null;
        }

        void setSyncError(RuntimeException error) {
            this.syncError = error;
            this.syncResponse = null;
        }

        @Override
        protected String callDeepSeekSync(String baseUrl, String key, String body) {
            if (syncError != null) {
                throw syncError;
            }
            return syncResponse;
        }
    }

    private ModelProviderConfig provider(Long id, String apiKey, String modelName) {
        return ModelProviderConfig.builder()
                .id(id)
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com")
                .modelName(modelName)
                .providerName("deepseek")
                .build();
    }

    @Test
    void chatSync_firstKeySucceeds_returnsContent() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat")
        ));

        SyncTestableImpl service = new SyncTestableImpl(keyPool, quotaService, webClient, properties);
        service.setSyncResponse("{\"choices\":[{\"message\":{\"content\":\"北交大自动化不错\"}}]}");

        List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));
        String result = service.chatSync(1L, messages);

        assertThat(result).isEqualTo("北交大自动化不错");
        verify(keyPool, never()).markUnhealthy(any(ModelProviderConfig.class));
    }

    @Test
    void chatSync_firstKeyFails_fallbackToSecond() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, "key-2", "deepseek-reasoner")
        ));

        // First call fails, second succeeds
        AtomicInteger callCount = new AtomicInteger(0);
        AiChatServiceImpl spy = new AiChatServiceImpl(keyPool, quotaService, webClient, properties) {
            @Override
            protected String callDeepSeekSync(String baseUrl, String key, String body) {
                if (callCount.getAndIncrement() == 0) {
                    throw new RuntimeException("500 Internal Server Error");
                }
                return "{\"choices\":[{\"message\":{\"content\":\"备用key成功\"}}]}";
            }
        };

        List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));
        String result = spy.chatSync(1L, messages);

        assertThat(result).isEqualTo("备用key成功");
        verify(keyPool).markUnhealthy(argThat(p -> p != null && p.getId().equals(1L)));
    }

    @Test
    void chatSync_allKeysFail_throwsBusinessException() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, "key-2", "deepseek-reasoner")
        ));

        SyncTestableImpl service = new SyncTestableImpl(keyPool, quotaService, webClient, properties);
        service.setSyncError(new RuntimeException("fail"));

        List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));

        assertThatThrownBy(() -> service.chatSync(1L, messages))
                .isInstanceOf(com.haifeng.common.exception.BusinessException.class)
                .extracting("code")
                .isEqualTo(1041);
    }

    @Test
    void chatSync_emptyProviders_throwsBusinessException() {
        when(keyPool.orderedFallback(1L)).thenReturn(Collections.emptyList());

        SyncTestableImpl service = new SyncTestableImpl(keyPool, quotaService, webClient, properties);

        List<ChatMessage> messages = Collections.singletonList(new ChatMessage("user", "分析大学"));

        assertThatThrownBy(() -> service.chatSync(1L, messages))
                .isInstanceOf(com.haifeng.common.exception.BusinessException.class)
                .extracting("code")
                .isEqualTo(1041);
    }
}
