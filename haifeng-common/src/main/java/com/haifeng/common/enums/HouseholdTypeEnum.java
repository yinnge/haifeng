package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 户籍类型枚举
 */
@Getter
@AllArgsConstructor
public enum HouseholdTypeEnum {

    URBAN("城镇"),
    RURAL("农村");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (HouseholdTypeEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
