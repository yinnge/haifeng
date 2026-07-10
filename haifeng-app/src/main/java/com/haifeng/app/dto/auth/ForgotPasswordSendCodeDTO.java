package com.haifeng.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordSendCodeDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "图形验证码不能为空")
    @Size(min = 4, max = 4, message = "图形验证码必须是4位")
    private String captchaCode;

    @NotBlank(message = "图形验证码UUID不能为空")
    @Size(max = 100, message = "图形验证码标识长度不能超过100")
    private String uuid;
}
