package com.haifeng.common.service.algorithm.matcher.operator;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 运算符策略工厂
 */
@Component
public class OperatorStrategyFactory {

    private final Map<String, OperatorStrategy> strategies = new HashMap<>();

    @PostConstruct
    public void init() {
        strategies.put("EQ", new EqStrategy());
        strategies.put("NE", new NeStrategy());
        strategies.put("GT", new GtStrategy());
        strategies.put("GE", new GeStrategy());
        strategies.put("LT", new LtStrategy());
        strategies.put("LE", new LeStrategy());
        strategies.put("IN", new InStrategy());
        strategies.put("NOT_IN", new NotInStrategy());
        strategies.put("IS_NULL", new IsNullStrategy());
        strategies.put("IS_NOT_NULL", new IsNotNullStrategy());
    }

    /**
     * 根据运算符获取策略
     *
     * @param operator 运算符名称
     * @return 策略实例，不存在返回 null
     */
    public OperatorStrategy getStrategy(String operator) {
        return strategies.get(operator);
    }
}
