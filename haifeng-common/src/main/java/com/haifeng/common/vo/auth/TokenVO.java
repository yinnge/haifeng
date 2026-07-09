package com.haifeng.common.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Token 响应 VO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVO {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 访问令牌过期时间（秒）
     */
    private Long accessTokenExpire;

    /**
     * 刷新令牌过期时间（秒）
     */
    private Long refreshTokenExpire;

    /**
     * Token 类型
     */
    @Builder.Default
    private String tokenType = "Bearer";
}
