package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum WithdrawStatus {

    PENDING("pending", "待处理"),
    PAID("paid", "已打款"),
    REJECTED("rejected", "已拒绝");

    @EnumValue
    private final String value;
    private final String desc;

    WithdrawStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
