package com.haifeng.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.entity.system.AdminLog;
import com.haifeng.common.mapper.system.AdminLogMapper;
import com.haifeng.common.util.IpUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final String MASKED_VALUE = "***";
    private static final Pattern API_KEY_PARAM_PATTERN = Pattern.compile("(?i)(apiKey|api_key)(\\s*[=:]\\s*)([^,}\\s]+)");

    private final AdminLogMapper adminLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.haifeng.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        String module = operationLog.module();
        String action = operationLog.action();
        String operation = module + " - " + action;

        Long adminId = null;
        String adminName = null;
        try {
            adminId = SecurityUtil.getCurrentAdminId();
            if (SecurityUtil.getCurrentUser() != null) {
                adminName = SecurityUtil.getCurrentUser().getUsername();
            }
        } catch (Exception e) {
            // 忽略获取管理员信息失败
        }

        String ip = IpUtil.getClientIp();
        String requestPath = getRequestPath();
        String requestMethod = getRequestMethod();
        String requestParams = getRequestParams(joinPoint);

        Object result = null;
        String status = "SUCCESS";
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = "FAIL";
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 持久化到数据库
            try {
                AdminLog adminLogEntity = AdminLog.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .adminId(adminId)
                        .adminName(adminName)
                        .operation(operation)
                        .requestPath(requestPath)
                        .requestMethod(requestMethod)
                        .requestParams(requestParams)
                        .result(status)
                        .errorMsg(errorMsg)
                        .ip(ip)
                        .createdAt(OffsetDateTime.now())
                        .build();
                adminLogMapper.insert(adminLogEntity);
            } catch (Exception e) {
                log.error("保存操作日志到数据库失败", e);
            }

            log.info("[操作日志] 操作={}, 管理员={}, IP={}, 路径={}, 方法={}, 状态={}, 耗时={}ms",
                    operation, adminName, ip, requestPath, requestMethod, status, costTime);
        }

        return result;
    }

    private String getRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getRequestURI();
            }
        } catch (Exception e) {
            // 忽略
        }
        return "unknown";
    }

    private String getRequestMethod() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getMethod();
            }
        } catch (Exception e) {
            // 忽略
        }
        return "unknown";
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }

            // 过滤掉HttpServletRequest等特殊类型，并脱敏敏感字段
            String params = Arrays.stream(args)
                    .filter(arg -> arg != null)
                    .filter(arg -> !(arg instanceof HttpServletRequest))
                    .map(arg -> {
                        try {
                            String json = objectMapper.writeValueAsString(arg);
                            json = redactSensitiveFields(json);
                            return json;
                        } catch (Exception e) {
                            return redactSensitiveFields(arg.toString());
                        }
                    })
                    .collect(Collectors.joining(", "));

            // 限制长度
            if (params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
            return params;
        } catch (Exception e) {
            return null;
        }
    }

    private String redactSensitiveFields(String value) {
        if (value == null) {
            return null;
        }
        String redacted = value;
        // 脱敏处理：隐藏密码、token、API Key等敏感字段
        redacted = maskSensitiveJsonField(redacted, "password");
        redacted = maskSensitiveJsonField(redacted, "token");
        redacted = maskSensitiveJsonField(redacted, "wechatId");
        redacted = maskSensitiveJsonField(redacted, "apiKey");
        redacted = maskSensitiveJsonField(redacted, "api_key");
        return maskSensitiveRequestParam(redacted);
    }

    private String maskSensitiveJsonField(String json, String fieldName) {
        return json.replaceAll("\"" + fieldName + "\"\\s*:\\s*\"[^\"]*\"", "\"" + fieldName + "\":\"" + MASKED_VALUE + "\"");
    }

    private String maskSensitiveRequestParam(String value) {
        return API_KEY_PARAM_PATTERN.matcher(value).replaceAll("$1$2" + MASKED_VALUE);
    }
}
