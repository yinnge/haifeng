package com.haifeng.admin.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpVerifyDTO {

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "TOTP 密钥不能为空")
    private String secret;
}
