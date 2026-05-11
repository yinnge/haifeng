package com.haifeng.common.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO {

    /**
     * 验证码唯一标识
     */
    private String uuid;

    /**
     * Base64 编码的验证码图片
     * 前端使用: <img src="data:image/png;base64,{image}">
     */
    private String image;
}
