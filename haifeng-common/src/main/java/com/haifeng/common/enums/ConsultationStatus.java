package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 咨询留言处理状态。
 */
@Getter
public enum ConsultationStatus {

    PENDING(0, "待处理"),
    HANDLED(1, "已处理");

    @EnumValue
    private final Integer value;
    private final String desc;

    ConsultationStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ConsultationStatus fromValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
