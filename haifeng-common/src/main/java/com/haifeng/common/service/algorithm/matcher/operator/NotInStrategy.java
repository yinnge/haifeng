package com.haifeng.common.service.algorithm.matcher.operator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 不在列表中策略 (NOT_IN)
 */
public class NotInStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return true;
        }
        if (checkValue == null) {
            return true;
        }
        Set<String> values = Arrays.stream(checkValue.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return !values.contains(fieldValue.toString());
    }
}
