package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 政治面貌枚举
 */
@Getter
@AllArgsConstructor
public enum PoliticalStatusEnum {

    MASSES("群众"),
    LEAGUE_MEMBER("共青团员"),
    PARTY_MEMBER("中共党员"),
    PROBATIONARY_PARTY_MEMBER("中共预备党员");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (PoliticalStatusEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
