package com.haifeng.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求 DTO
 */
@Data
public class RefreshTokenDTO {

    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
