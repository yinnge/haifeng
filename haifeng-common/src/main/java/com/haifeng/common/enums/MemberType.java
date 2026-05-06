package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MemberType {

    NORMAL("normal", "普通用户"),
    PRO("pro", "Pro会员"),
    VIP("vip", "VIP会员");

    @EnumValue
    private final String value;
    private final String desc;

    MemberType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MemberType fromValue(String value) {
        for (MemberType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return NORMAL;
    }
}
