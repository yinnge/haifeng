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
