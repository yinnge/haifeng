package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 高考改革模式枚举
 */
@Getter
@AllArgsConstructor
public enum ReformModelEnum {

    TRADITIONAL("传统文理"),
    THREE_PLUS_THREE("3+3"),
    THREE_PLUS_ONE_PLUS_TWO("3+1+2");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (ReformModelEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
