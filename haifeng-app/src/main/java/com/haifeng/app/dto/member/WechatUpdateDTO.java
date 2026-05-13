package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WechatUpdateDTO {

    @NotBlank(message = "微信号不能为空")
    @Size(max = 50, message = "微信号最多50个字符")
    private String wechatId;
}
