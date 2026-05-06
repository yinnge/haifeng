package com.haifeng.common.aspect;

import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("@annotation(com.haifeng.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        String module = operationLog.module();
        String action = operationLog.action();

        Long adminId = null;
        try {
            adminId = SecurityUtil.getCurrentAdminId();
        } catch (Exception e) {
            // 忽略获取管理员ID失败
        }

        String ip = getClientIp();
        String requestPath = getRequestPath();

        Object result = null;
        String status = "SUCCESS";
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = "FAIL";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[操作日志] 模块={}, 操作={}, 管理员ID={}, IP={}, 路径={}, 状态={}, 耗时={}ms, 错误={}",
                    module, action, adminId, ip, requestPath, status, costTime, errorMsg);
        }

        return result;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // 忽略
        }
        return "unknown";
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
}
