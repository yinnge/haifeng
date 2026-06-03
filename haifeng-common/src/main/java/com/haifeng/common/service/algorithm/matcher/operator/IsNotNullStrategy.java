package com.haifeng.common.service.algorithm.matcher.operator;

/**
 * 不为空策略 (IS_NOT_NULL)
 */
public class IsNotNullStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        return fieldValue != null;
    }
}
