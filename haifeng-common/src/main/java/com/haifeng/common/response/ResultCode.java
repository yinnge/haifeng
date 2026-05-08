package com.haifeng.common.response;

import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码从 1000 开始
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    MEMBER_EXPIRED(1003, "会员已过期"),
    PRO_REQUIRED(1004, "权限不足（需要专业版及以上）"),
    VIP_REQUIRED(1005, "权限不足（需要旗舰版）"),
    ACCOUNT_LOCKED(1006, "账号已锁定，请30分钟后重试"),
    TOTP_REQUIRED(20001, "需进行二次验证");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
