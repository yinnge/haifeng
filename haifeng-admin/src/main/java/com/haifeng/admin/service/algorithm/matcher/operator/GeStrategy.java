package com.haifeng.admin.service.algorithm.matcher.operator;

import java.math.BigDecimal;

/**
 * 大于等于策略 (GE)
 */
public class GeStrategy implements OperatorStrategy {

    @Override
    public boolean evaluate(Object fieldValue, String checkValue) {
        if (fieldValue == null) {
            return false;
        }
        try {
            BigDecimal field = new BigDecimal(fieldValue.toString());
            BigDecimal check = new BigDecimal(checkValue);
            return field.compareTo(check) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
