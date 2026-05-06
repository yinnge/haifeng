package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NotificationType {

    MEMBER_EXPIRE_SOON("member_expire_soon", "会员即将到期"),
    MEMBER_EXPIRED("member_expired", "会员已过期"),
    COMMISSION_EARNED("commission_earned", "佣金到账"),
    COMMISSION_PAID("commission_paid", "佣金已发放"),
    SYSTEM_NOTICE("system_notice", "系统公告"),
    MEMBER_RENEWED("member_renewed", "会员续费成功"),
    MEMBER_ACTIVATION_SUCCESS("member_activation_success", "会员开通成功");

    @EnumValue
    private final String value;
    private final String desc;

    NotificationType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
