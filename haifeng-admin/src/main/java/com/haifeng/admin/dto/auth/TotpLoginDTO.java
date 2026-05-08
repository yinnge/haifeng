package com.haifeng.admin.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpLoginDTO {

    @NotBlank(message = "预认证令牌不能为空")
    private String preAuthToken;

    @NotBlank(message = "动态验证码不能为空")
    private String totpCode;
}
