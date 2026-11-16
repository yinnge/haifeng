package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 咨询留言的业务类型。
 */
@Getter
public enum ConsultationBizType {

    PLANNER("planner", "规划师"),
    INSTITUTION("institution", "培训机构");

    @EnumValue
    private final String value;
    private final String desc;

    ConsultationBizType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ConsultationBizType fromValue(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
