package com.haifeng.common.service.algorithm.matcher.operator;

/**
 * 为空策略 (IS_NULL)
 */
public class IsNullStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return fieldValue == null;
    }
}
