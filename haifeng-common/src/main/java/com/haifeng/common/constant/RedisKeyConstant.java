package com.haifeng.common.constant;

/**
 * Redis Key 常量
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
    }

    /**
     * Token 相关
     */
    public static final String TOKEN_REFRESH_PREFIX = "haifeng:token:refresh:";

    /**
     * 用户信息缓存
     */
    public static final String MEMBER_INFO_PREFIX = "haifeng:member:info:";

    /**
     * 接口限流
     */
    public static final String LIMIT_API_PREFIX = "haifeng:limit:api:";

    /**
     * 验证码
     */
    public static final String CAPTCHA_PREFIX = "haifeng:captcha:";

    /**
     * 管理员登录失败计数
     */
    public static final String ADMIN_LOGIN_FAIL_PREFIX = "haifeng:admin:login:fail:";

    /**
     * 管理员 TOTP 预认证
     */
    public static final String ADMIN_PRE_AUTH_PREFIX = "haifeng:admin:pre-auth:";

    /**
     * 获取 RefreshToken 的 Redis Key
     *
     * @param userId   用户ID
     * @param userType 用户类型（admin/member）
     * @return Redis Key
     */
    public static String getRefreshTokenKey(Long userId, String userType) {
        return TOKEN_REFRESH_PREFIX + userType + ":" + userId;
    }

    /**
     * 获取用户信息缓存 Key
     *
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String getMemberInfoKey(Long userId) {
        return MEMBER_INFO_PREFIX + userId;
    }

    /**
     * 获取接口限流 Key
     *
     * @param ip   IP地址
     * @param path 接口路径
     * @return Redis Key
     */
    public static String getLimitApiKey(String ip, String path) {
        return LIMIT_API_PREFIX + ip + ":" + path;
    }

    /**
     * 获取验证码 Redis Key
     *
     * @param uuid 验证码唯一标识
     * @return Redis Key
     */
    public static String getCaptchaKey(String uuid) {
        return CAPTCHA_PREFIX + uuid;
    }

    /**
     * 获取管理员登录失败计数 Key
     *
     * @param phone 手机号
     * @return Redis Key
     */
    public static String getAdminLoginFailKey(String phone) {
        return ADMIN_LOGIN_FAIL_PREFIX + phone;
    }

    /**
     * 获取管理员预认证 Key
     *
     * @param token 预认证令牌
     * @return Redis Key
     */
    public static String getAdminPreAuthKey(String token) {
        return ADMIN_PRE_AUTH_PREFIX + token;
    }
}
