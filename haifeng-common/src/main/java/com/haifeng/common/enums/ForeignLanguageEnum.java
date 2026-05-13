package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 外语语种枚举
 */
@Getter
@AllArgsConstructor
public enum ForeignLanguageEnum {

    ENGLISH("英语"),
    JAPANESE("日语"),
    RUSSIAN("俄语"),
    GERMAN("德语"),
    FRENCH("法语"),
    SPANISH("西班牙语");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (ForeignLanguageEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
