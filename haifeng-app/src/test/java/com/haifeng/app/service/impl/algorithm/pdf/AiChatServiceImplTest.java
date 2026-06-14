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
