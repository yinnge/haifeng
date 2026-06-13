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
