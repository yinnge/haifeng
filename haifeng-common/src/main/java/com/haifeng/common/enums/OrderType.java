package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderType {

    NEW("new", "新开通"),
    RENEWAL("renewal", "续费");

    @EnumValue
    private final String value;
    private final String desc;

    OrderType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
