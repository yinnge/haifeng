package com.haifeng.common.service.algorithm.matcher.operator;

/**
 * 运算符策略接口
 */
public interface OperatorStrategy {

    /**
     * 判断字段值是否满足条件
     *
     * @param fieldValue 用户档案中的字段值（可能为 null）
     * @param checkValue 约束字典中配置的值
     * @return true=条件满足（触发约束）
     */
    boolean evaluate(Object fieldValue, String checkValue);
}
