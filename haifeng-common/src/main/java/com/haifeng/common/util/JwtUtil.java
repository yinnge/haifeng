package com.haifeng.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.access-secret}")
    private String secret;

    @Value("${jwt.access-token-expire:7200}")
    private Long accessTokenExpire;

    @Value("${jwt.refresh-token-expire:604800}")
    private Long refreshTokenExpire;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_USER_TYPE = "userType";
    private static final String CLAIM_MEMBER_TYPE = "memberType";
    private static final String CLAIM_TOKEN_VERSION = "tokenVersion";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String USER_TYPE_ADMIN = "admin";
    public static final String USER_TYPE_MEMBER = "member";
    public static final String MEMBER_TYPE_NORMAL = "normal";  // 普通版
    public static final String MEMBER_TYPE_PRO = "pro";        // 专业版
    public static final String MEMBER_TYPE_VIP = "vip";        // 旗舰版

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 AccessToken（2小时）- 简化版本，向后兼容
     */
    public String generateAccessToken(Long userId) {
        return generateAccessToken(userId, USER_TYPE_MEMBER, null);
    }

    /**
     * 生成 AccessToken（2小时）- 完整版本
     */
    public String generateAccessToken(Long userId, String userType, String memberType) {
        return generateAccessToken(userId, userType, memberType, 0);
    }

    /**
     * 生成 AccessToken（2小时）- 含 tokenVersion
     */
    public String generateAccessToken(Long userId, String userType, String memberType, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        claims.put(CLAIM_USER_TYPE, userType);
        claims.put(CLAIM_TOKEN_VERSION, tokenVersion);
        if (memberType != null) {
            claims.put(CLAIM_MEMBER_TYPE, memberType);
        }
        return createToken(claims, accessTokenExpire);
    }

    /**
     * 生成 RefreshToken（7天）- 简化版本，向后兼容
     */
    public String generateRefreshToken(Long userId) {
        return generateRefreshToken(userId, USER_TYPE_MEMBER);
    }

    /**
     * 生成 RefreshToken（7天）- 完整版本
     */
    public String generateRefreshToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        claims.put(CLAIM_USER_TYPE, userType);
        return createToken(claims, refreshTokenExpire);
    }

    private String createToken(Map<String, Object> claims, Long expireSeconds) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireSeconds * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    /**
     * 判断Token是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 判断是否为AccessToken
     */
    public boolean isAccessToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * 判断是否为RefreshToken
     */
    public boolean isRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    /**
     * 验证Token有效性
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null && !isTokenExpired(token);
    }

    /**
     * 从Token中获取用户类型
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get(CLAIM_USER_TYPE, String.class);
    }

    /**
     * 从Token中获取会员类型
     */
    public String getMemberTypeFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get(CLAIM_MEMBER_TYPE, String.class);
    }

    /**
     * 从 Claims 中获取 tokenVersion
     */
    public Integer getTokenVersionFromClaims(Claims claims) {
        if (claims == null) return null;
        return claims.get(CLAIM_TOKEN_VERSION, Integer.class);
    }

    /**
     * 获取 AccessToken 过期时间（秒）
     */
    public Long getAccessTokenExpire() {
        return accessTokenExpire;
    }

    /**
     * 获取 RefreshToken 过期时间（秒）
     */
    public Long getRefreshTokenExpire() {
        return refreshTokenExpire;
    }
}
