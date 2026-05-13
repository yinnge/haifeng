package com.haifeng.admin.service.algorithm.matcher.operator;

/**
 * 不等于策略 (NE)
 */
public class NeStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return true;
        }
        return !fieldValue.toString().equals(checkValue);
    }
}
