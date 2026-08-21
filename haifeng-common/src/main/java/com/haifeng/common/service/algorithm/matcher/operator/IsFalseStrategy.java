package com.haifeng.common.service.algorithm.matcher.operator;

/**
 * 为假策略 (IS_FALSE)：字段值为 false 时触发
 * 用于布尔字段（如 is_color_blind），无需填写 checkValue
 */
public class IsFalseStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return Boolean.FALSE.equals(fieldValue);
    }
}
