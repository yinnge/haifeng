package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum NotificationType {

    MEMBER_EXPIRE_SOON("member_expire_soon", "会员即将到期"),
    MEMBER_EXPIRED("member_expired", "会员已过期"),
    COMMISSION_EARNED("commission_earned", "佣金到账"),
    COMMISSION_PAID("commission_paid", "佣金已发放"),
    COMMISSION_REJECTED("commission_rejected", "提现被拒绝"),
    SYSTEM_NOTICE("system_notice", "系统公告"),
    MEMBER_RENEWED("member_renewed", "会员续费成功"),
    MEMBER_ACTIVATION_SUCCESS("member_activation_success", "会员开通成功"),
    MEMBER_REVOKED("member_revoked", "会员已撤销"),
    COMMISSION_REVERSED("commission_reversed", "佣金已撤回");

    @EnumValue
    private final String value;
    private final String desc;

    NotificationType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static NotificationType fromValue(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
