package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 规划师服务对象枚举。
 *
 * <p>仅两个固定值：高考 / 考研。实体字段按中文字符串存储（与 DB 对齐、不加 CHECK），
 * 本枚举提供合法性校验，避免各处硬编码中文。
 */
@Getter
@AllArgsConstructor
public enum ServiceTargetEnum {

    GAOKAO("高考"),
    KAOYAN("考研");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(e -> e.value.equals(value));
    }
}
