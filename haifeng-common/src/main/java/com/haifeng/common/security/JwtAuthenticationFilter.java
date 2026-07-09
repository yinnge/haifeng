package com.haifeng.common.security;

import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                if (claims == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwtUtil.getUserIdFromToken(token);
                String userType = jwtUtil.getUserTypeFromToken(token);
                String memberType = jwtUtil.getMemberTypeFromToken(token);

                if (userId != null && isTokenVersionValid(claims)) {
                    // 构建权限列表
                    List<SimpleGrantedAuthority> authorities = buildAuthorities(userType, memberType);

                    // 创建 AuthUser
                    AuthUser authUser = AuthUser.builder()
                            .userId(userId)
                            .userType(userType)
                            .memberType(memberType)
                            .authorities(authorities)
                            .build();

                    // 设置认证信息到 SecurityContext
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(authUser, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT认证成功: userId={}, userType={}", userId, userType);
                }
            }
        } catch (Exception e) {
            log.warn("JWT认证失败: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 校验 Token 版本号（仅 admin 用户）
     */
    private boolean isTokenVersionValid(Claims claims) {
        Integer tokenVersion = jwtUtil.getTokenVersionFromClaims(claims);
        if (tokenVersion == null) {
            return true;
        }

        Long userId = claims.get("userId", Long.class);
        String userType = claims.get("userType", String.class);
        if (userId == null || !"admin".equals(userType)) {
            return true;
        }

        String versionKey = RedisKeyConstant.getTokenVersionKey(userId, JwtUtil.USER_TYPE_ADMIN);
        String currentVersion = redisTemplate.opsForValue().get(versionKey);
        if (currentVersion == null) {
            return true;
        }

        boolean valid = Integer.valueOf(currentVersion).equals(tokenVersion);
        if (!valid) {
            log.warn("Token版本号不匹配，userId={}，tokenVersion={}，currentVersion={}", userId, tokenVersion, currentVersion);
        }
        return valid;
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 构建权限列表
     */
    private List<SimpleGrantedAuthority> buildAuthorities(String userType, String memberType) {
        if ("admin".equals(userType)) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if ("member".equals(userType)) {
            if ("vip".equals(memberType)) {
                return List.of(
                        new SimpleGrantedAuthority("ROLE_MEMBER"),
                        new SimpleGrantedAuthority("ROLE_VIP")
                );
            }
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
