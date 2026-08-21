package com.haifeng.common.service.algorithm.matcher.operator;

/**
 * 为真策略 (IS_TRUE)：字段值为 true 时触发
 * 用于布尔字段（如 is_color_blind），无需填写 checkValue
 */
public class IsTrueStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return Boolean.TRUE.equals(fieldValue);
    }
}
