package com.haifeng.common.util;

import com.haifeng.common.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 获取当前登录用户
     */
    public static AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }
        return null;
    }

    /**
     * 获取当前用户ID（通用）
     */
    public static Long getCurrentUserId() {
        AuthUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前管理员ID
     *
     * @return 管理员ID，非管理员返回null
     */
    public static Long getCurrentAdminId() {
        AuthUser user = getCurrentUser();
        if (user != null && user.isAdmin()) {
            return user.getUserId();
        }
        return null;
    }

    /**
     * 获取当前会员ID
     *
     * @return 会员ID，非会员返回null
     */
    public static Long getCurrentMemberId() {
        AuthUser user = getCurrentUser();
        if (user != null && user.isMember()) {
            return user.getUserId();
        }
        return null;
    }

    /**
     * 获取当前会员类型
     *
     * @return normal / vip，非会员返回null
     */
    public static String getCurrentMemberType() {
        AuthUser user = getCurrentUser();
        if (user != null && user.isMember()) {
            return user.getMemberType();
        }
        return null;
    }

    /**
     * 判断当前用户是否为VIP
     */
    public static boolean isVip() {
        AuthUser user = getCurrentUser();
        return user != null && user.isVip();
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isLogin() {
        return getCurrentUser() != null;
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        AuthUser user = getCurrentUser();
        return user != null && user.isAdmin();
    }

    /**
     * 判断当前用户是否为会员
     */
    public static boolean isMember() {
        AuthUser user = getCurrentUser();
        return user != null && user.isMember();
    }
}
