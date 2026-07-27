package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderStatus {

    PENDING("pending", "待处理"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    @EnumValue
    private final String value;
    private final String desc;

    OrderStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
