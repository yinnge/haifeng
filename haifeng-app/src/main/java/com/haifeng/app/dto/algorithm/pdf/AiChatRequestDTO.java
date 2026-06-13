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
