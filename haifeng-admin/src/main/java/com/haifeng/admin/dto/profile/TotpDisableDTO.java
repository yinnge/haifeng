package com.haifeng.admin.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpDisableDTO {

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "TOTP 验证码不能为空")
    private String code;
}
