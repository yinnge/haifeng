package com.haifeng.app.service.algorithm.pdf;

import com.haifeng.app.vo.algorithm.pdf.ChatMessage;

import java.util.List;

public interface AiChatService {

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
}
