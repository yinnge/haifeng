package com.haifeng.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] WHITE_LIST = {
            // 认证相关
            "/api/v1/*/auth/login",
            "/api/v1/*/auth/register",
            "/api/v1/*/auth/refresh",
            "/api/v1/*/auth/captcha",
            "/api/v1/*/auth/login/totp",
            "/api/v1/*/auth/logout",
            "/api/v1/app/auth/forgot-password/send-code",
            "/api/v1/app/auth/forgot-password/reset",
            // 首页
            "/api/v1/app/home/**",
            // 院校（公开部分）
            "/api/v1/app/university/list",
            "/api/v1/app/university/channel-options",
            "/api/v1/app/university/*/channels",
            // 专业（公开部分）
            "/api/v1/app/major/list",
            "/api/v1/app/major/category-stats",
            // 城市
            "/api/v1/app/city/list",
            // 企业
            "/api/v1/app/enterprise/list",
            // 行业
            "/api/v1/app/industry/categories",
            "/api/v1/app/industry/list",
            // 资源
            "/api/v1/app/resource/list",
            "/api/v1/app/resource/categories",
            // 证书
            "/api/v1/app/certificate/list",
            "/api/v1/app/certificate/categories",
            // 竞赛
            "/api/v1/app/competition/list",
            // 特殊通道
            "/api/v1/app/special/channel/list",
            "/api/v1/app/special/strong-base-score/list",
            "/api/v1/app/special/channel-univ/list",
            // 就业（列表公开）
            "/api/v1/app/employment/*/list",
            "/api/v1/app/employment/*/*/list",
            "/api/v1/app/employment/content/*/list-by-type",
            // Actuator
            "/actuator/**",
            // Swagger
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（使用 JWT，无需 CSRF）
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用 HTTP Basic
                .httpBasic(AbstractHttpConfigurer::disable)
                // 无状态会话
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 授权配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                // 异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(R.fail(ResultCode.UNAUTHORIZED)));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(R.fail(ResultCode.FORBIDDEN)));
                        })
                )
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
