package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.AiChatRequestDTO;
import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.service.ai.AiQuotaService;
import com.haifeng.common.service.ai.ApiKeyPool;
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
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

    /** 子类化以替换 callDeepSeekRaw */
    static class TestableImpl extends AiChatServiceImpl {
        private final List<Flux<String>> queue;
        private final AtomicInteger calls = new AtomicInteger(0);
        private final java.util.List<String> keys = new java.util.ArrayList<>();
        private final java.util.List<String> bodies = new java.util.ArrayList<>();

        TestableImpl(ApiKeyPool keyPool, AiQuotaService quotaService,
                     WebClient webClient, DeepSeekProperties properties,
                     List<Flux<String>> queue) {
            super(keyPool, quotaService, webClient, properties);
            this.queue = queue;
        }

        @Override
        protected Flux<String> callDeepSeekRaw(String key, String body) {
            keys.add(key);
            bodies.add(body);
            int idx = calls.getAndIncrement();
            return queue.get(Math.min(idx, queue.size() - 1));
        }
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
        protected String callDeepSeekSync(String key, String body) {
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
                .modelName(modelName)
                .providerName("deepseek")
                .build();
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
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, "key-2", "deepseek-reasoner")
        ));

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

        assertThat(service.keys).containsExactly("key-1");
        assertThat(service.bodies.get(0)).contains("\"model\":\"deepseek-chat\"");
        verify(keyPool, never()).markUnhealthy(any(ModelProviderConfig.class));
    }

    @Test
    void firstKeyFails_fallbackToSecond() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, "key-2", "deepseek-reasoner")
        ));

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

        assertThat(service.keys).containsExactly("key-1", "key-2");
        assertThat(service.bodies.get(0)).contains("\"model\":\"deepseek-chat\"");
        assertThat(service.bodies.get(1)).contains("\"model\":\"deepseek-reasoner\"");
        verify(keyPool).markUnhealthy(argThat(provider -> provider != null && provider.getId().equals(1L)));
    }

    @Test
    void emptyProviders_throwsBusinessException() {
        when(keyPool.orderedFallback(1L)).thenReturn(Collections.emptyList());

        TestableImpl service = new TestableImpl(keyPool, quotaService, webClient, properties,
                Collections.singletonList(Flux.empty()));

        AiChatRequestDTO dto = new AiChatRequestDTO();
        dto.setMessages(Collections.singletonList(new ChatMessage("user", "hi")));

        StepVerifier.create(service.streamChat(1L, dto))
                .expectErrorMatches(t -> t instanceof com.haifeng.common.exception.BusinessException
                        && ((com.haifeng.common.exception.BusinessException) t).getCode() == 1041)
                .verify();

        assertThat(service.keys).isEmpty();
        verify(keyPool, never()).markUnhealthy(any(ModelProviderConfig.class));
    }

    @Test
    void allKeysFail_throwsBusinessException() {
        when(keyPool.orderedFallback(1L)).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, "key-2", "deepseek-reasoner")
        ));

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

        assertThat(service.keys).containsExactly("key-1", "key-2");
        verify(keyPool).markUnhealthy(argThat(provider -> provider != null && provider.getId().equals(1L)));
        verify(keyPool).markUnhealthy(argThat(provider -> provider != null && provider.getId().equals(2L)));
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
            protected String callDeepSeekSync(String key, String body) {
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
