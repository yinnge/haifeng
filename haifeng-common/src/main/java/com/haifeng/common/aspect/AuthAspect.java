package com.haifeng.common.aspect;

import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.security.AuthUser;
import com.haifeng.common.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 认证权限切面
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class AuthAspect {

    /**
     * 检查登录状态
     */
    @Before("@annotation(requireLogin) || @within(requireLogin)")
    public void checkLogin(JoinPoint joinPoint, RequireLogin requireLogin) {
        // 方法级注解优先
        RequireLogin annotation = getMethodAnnotation(joinPoint, RequireLogin.class);
        if (annotation == null) {
            annotation = requireLogin;
        }

        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            log.warn("用户未登录，拒绝访问: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 检查用户类型限制
        String requiredUserType = annotation.userType();
        if (!requiredUserType.isEmpty()) {
            if (!requiredUserType.equals(currentUser.getUserType())) {
                log.warn("用户类型不匹配，拒绝访问: 需要={}, 实际={}",
                        requiredUserType, currentUser.getUserType());
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }

    /**
     * 检查VIP权限
     */
    @Before("@annotation(requireVip) || @within(requireVip)")
    public void checkVip(JoinPoint joinPoint, RequireVip requireVip) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            log.warn("用户未登录，拒绝VIP访问: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (!currentUser.isVip()) {
            log.warn("用户非VIP，拒绝访问: userId={}", currentUser.getUserId());
            throw new BusinessException(ResultCode.VIP_REQUIRED);
        }
    }

    /**
     * 获取方法上的注解
     */
    private <T extends java.lang.annotation.Annotation> T getMethodAnnotation(
            JoinPoint joinPoint, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(annotationClass);
    }
}
