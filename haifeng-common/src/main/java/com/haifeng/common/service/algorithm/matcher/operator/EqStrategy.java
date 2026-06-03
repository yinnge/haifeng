package com.haifeng.common.service.algorithm.matcher.operator;

/**
 * 等于策略 (EQ)
 */
public class EqStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        return fieldValue.toString().equals(checkValue);
    }
}
