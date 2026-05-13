package com.haifeng.admin.service.algorithm.matcher.operator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 在列表中策略 (IN)
 */
public class InStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null || checkValue == null) {
            return false;
        }
        Set<String> values = Arrays.stream(checkValue.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return values.contains(fieldValue.toString());
    }
}
