package com.haifeng.admin.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpDisableDTO {

    @NotBlank(message = "密码不能为空")
    private String password;
}
