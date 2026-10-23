package com.haifeng.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

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
            // 全站模糊搜索（游客可用：院校/城市/专业联想）
            "/api/v1/app/search/**",
            // 热点资讯（列表+详情均免登录公开；按模块分子路径，后续考公/考研/中考/英语四六级同型追加）
            "/api/v1/app/algorithm/article/gaokao/*",
            // 高考模拟填报（游客可查：年份/改革模式/位次/批次线 + 专业组/专业分页；档案读写 /archive 与组存活校验 /group/exists 仍需登录）
            "/api/v1/app/gaokao/years",
            "/api/v1/app/gaokao/reform-model",
            "/api/v1/app/gaokao/rank",
            "/api/v1/app/gaokao/batch-lines",
            "/api/v1/app/admission/group/page",
            "/api/v1/app/admission/major/page",
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
            "/api/v1/app/enterprise/types",
            // 企业详情按 id 查询为公开接口（/industries 仍由 @RequireVip AOP 守卫，不会越权）
            "/api/v1/app/enterprise/*",
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
            // 就业（列表/筛选/年份公开，详情需登录）
            "/api/v1/app/employment/*/list",
            "/api/v1/app/employment/*/*/list",
            "/api/v1/app/employment/*/filters",
            "/api/v1/app/employment/*/*/filters",
            "/api/v1/app/employment/*/*/years",
            "/api/v1/app/employment/*/*/grad-years",
            "/api/v1/app/employment/content/*/list-by-type",
            "/api/v1/app/employment/content/*/*/view",
            // 专栏文件（列表 + 动态筛选公开，详情/预览/下载仍需 VIP 鉴权）
            "/api/v1/app/fileload/middle/list",
            "/api/v1/app/fileload/middle/stages",
            "/api/v1/app/fileload/middle/subjects",
            "/api/v1/app/fileload/middle/tags",
            "/api/v1/app/fileload/high/list",
            "/api/v1/app/fileload/high/stages",
            "/api/v1/app/fileload/high/subjects",
            "/api/v1/app/fileload/high/tags",
            // Actuator
            "/actuator/**",
            // PDF 公开预览端点（HMAC 签名 token + 有效期自校验，供 pdf.js viewer 拉取报告 PDF）
            "/api/v1/public/pdf/**",
            // 自托管 pdf.js 查看器静态资源：PDF 直链与 viewer 同源（127.0.0.1:18080），
            // 规避 pdf.js validateFileURL 的「file origin does not match viewer's」跨源拦截；
            // 静态资源无敏感数据，viewer.html 自身不做任何后端数据请求
            "/pdfjs/**",
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
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 无状态会话
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 授权配置
                .authorizeHttpRequests(auth -> auth
                        // ERROR/ASYNC dispatch（如 SSE 流异常后的 /error 转发）不经过 JWT 过滤器，
                        // 放行以免对已提交响应再次鉴权失败
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
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
