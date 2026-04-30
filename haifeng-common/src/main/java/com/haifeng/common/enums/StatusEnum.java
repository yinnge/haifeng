package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum StatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    @EnumValue
    private final Integer value;
    private final String desc;

    StatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
