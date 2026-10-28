package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 收藏目标类型
 */
@Getter
public enum FavoriteTargetType {

    UNIVERSITY("university", "院校"),
    MAJOR("major", "专业"),
    CITY("city", "城市"),
    COMPETITION("competition", "竞赛"),
    CERTIFICATE("certificate", "证书"),
    ENTERPRISE("enterprise", "企业"),
    INDUSTRY("industry", "行业");

    @EnumValue
    private final String value;
    private final String desc;

    FavoriteTargetType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FavoriteTargetType fromValue(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
