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
     * 安全系数 ZSet 缓存
     * member: groupId, score: safetyLevel (0.00~1.00)
     */
    public static final String ALGO_SAFETY_ZSET_PREFIX = "haifeng:algo:safety:zset:";

    /**
     * 安全系数全量计算分布式锁
     */
    public static final String ALGO_SAFETY_LOCK_PREFIX = "haifeng:algo:safety:lock:";

    /**
     * 志愿方案默认数量限制缓存
     * value: WishPlanLimitVO (5 个字段: 搏/冲/稳/保/垫)
     */
    public static final String WISH_PLAN_DEFAULT_LIMITS_KEY = "haifeng:wish-plan:default-limits";

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

    /**
     * 获取安全系数 ZSet 缓存 Key
     *
     * @param memberId      用户ID
     * @param year          高考年份(按用户档案 gaokaoYear 强制带入,避免不同年份缓存串)
     * @param batch         批次
     * @param subjectFilter 是否启用选科筛选
     * @return Redis Key
     */
    public static String getAlgoSafetyZSetKey(Long memberId, Short year, String batch, boolean subjectFilter) {
        return ALGO_SAFETY_ZSET_PREFIX + memberId + ":" + year + ":" + batch + ":" + subjectFilter;
    }

    /**
     * 获取安全系数全量计算分布式锁 Key
     *
     * @param year 高考年份
     */
    public static String getAlgoSafetyLockKey(Long memberId, Short year, String batch, boolean subjectFilter) {
        return ALGO_SAFETY_LOCK_PREFIX + memberId + ":" + year + ":" + batch + ":" + subjectFilter;
    }

    /**
     * 大学 tags 字典缓存
     * value: List<String>(去重后的全部 tags)
     */
    public static final String UNIVERSITY_TAGS_DISTINCT_KEY = "haifeng:university:tags:distinct";

    /**
     * 志愿方案导出状态缓存
     * hash key: planId, field: major:{majorId}:isExported
     */
    public static final String WISH_EXPORT_PREFIX = "haifeng:wish:export:";

    /**
     * 首页 - 公告缓存
     */
    public static final String HOME_ANNOUNCEMENT_LIST_PREFIX   = "haifeng:app:home:announcement:list:";
    public static final String HOME_ANNOUNCEMENT_DETAIL_PREFIX = "haifeng:app:home:announcement:detail:";

    /**
     * 首页 - 规划师缓存
     */
    public static final String HOME_PLANNER_LIST_PREFIX   = "haifeng:app:home:planner:list:";
    public static final String HOME_PLANNER_DETAIL_PREFIX = "haifeng:app:home:planner:detail:";

    /**
     * 首页 - 培训机构缓存
     */
    public static final String HOME_INSTITUTION_LIST_PREFIX   = "haifeng:app:home:institution:list:";
    public static final String HOME_INSTITUTION_DETAIL_PREFIX = "haifeng:app:home:institution:detail:";

    /**
     * 首页模块缓存 TTL（分钟）
     */
    public static final long HOME_CACHE_TTL_MINUTES = 30L;

    /**
     * 公告列表缓存 Key
     */
    public static String getAnnouncementListKey(int page, int size, String tag) {
        return HOME_ANNOUNCEMENT_LIST_PREFIX + "p=" + page + ":s=" + size + ":tag=" + (tag == null ? "" : tag);
    }

    /**
     * 公告详情缓存 Key
     */
    public static String getAnnouncementDetailKey(Long id) {
        return HOME_ANNOUNCEMENT_DETAIL_PREFIX + id;
    }

    /**
     * 规划师列表缓存 Key
     */
    public static String getPlannerListKey(int page, int size, String region) {
        return HOME_PLANNER_LIST_PREFIX + "p=" + page + ":s=" + size + ":region=" + (region == null ? "" : region);
    }

    /**
     * 规划师详情缓存 Key
     */
    public static String getPlannerDetailKey(Long id) {
        return HOME_PLANNER_DETAIL_PREFIX + id;
    }

    /**
     * 培训机构列表缓存 Key
     */
    public static String getInstitutionListKey(int page, int size, String name) {
        return HOME_INSTITUTION_LIST_PREFIX + "p=" + page + ":s=" + size + ":name=" + (name == null ? "" : name);
    }

    /**
     * 培训机构详情缓存 Key
     */
    public static String getInstitutionDetailKey(Long id) {
        return HOME_INSTITUTION_DETAIL_PREFIX + id;
    }
}
