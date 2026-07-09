package com.haifeng.common.aspect;

import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.security.AuthUser;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Order(1)
public class AuthAspect {

    private final SysAdminMapper adminMapper;

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
     * 检查Pro权限（专业版及以上：pro、vip）
     */
    @Before("@annotation(requirePro) || @within(requirePro)")
    public void checkPro(JoinPoint joinPoint, RequirePro requirePro) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            log.warn("用户未登录，拒绝Pro访问: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (!currentUser.isProOrAbove()) {
            log.warn("用户非Pro及以上，拒绝访问: userId={}, memberType={}",
                    currentUser.getUserId(), currentUser.getMemberType());
            throw new BusinessException(ResultCode.PRO_REQUIRED);
        }
    }

    /**
     * 检查VIP权限（仅旗舰版）
     */
    @Before("@annotation(requireVip) || @within(requireVip)")
    public void checkVip(JoinPoint joinPoint, RequireVip requireVip) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            log.warn("用户未登录，拒绝VIP访问: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (!currentUser.isVip()) {
            log.warn("用户非VIP，拒绝访问: userId={}, memberType={}",
                    currentUser.getUserId(), currentUser.getMemberType());
            throw new BusinessException(ResultCode.VIP_REQUIRED);
        }
    }

    /**
     * 检查管理员模块权限
     */
    @Before("@annotation(requireAdminModule) || @within(requireAdminModule)")
    public void checkAdminModule(JoinPoint joinPoint, RequireAdminModule requireAdminModule) {
        RequireAdminModule annotation = getMethodAnnotation(joinPoint, RequireAdminModule.class);
        if (annotation == null) {
            annotation = requireAdminModule;
        }

        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            log.warn("非管理员用户，拒绝访问模块: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        String moduleCode = annotation.value();
        int count = adminMapper.countModulePermission(currentUser.getUserId(), moduleCode);
        if (count == 0) {
            log.warn("管理员无权访问模块，adminId={}, moduleCode={}", currentUser.getUserId(), moduleCode);
            throw new BusinessException(ResultCode.MODULE_FORBIDDEN);
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
