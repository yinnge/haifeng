package com.haifeng.common.service.algorithm.matcher;

import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.service.algorithm.matcher.operator.OperatorStrategy;
import com.haifeng.common.service.algorithm.matcher.operator.OperatorStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 约束匹配服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstraintMatcherServiceImpl implements ConstraintMatcherService {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final OperatorStrategyFactory strategyFactory;
    private final FieldValueExtractor fieldExtractor;

    @Override
    public List<String> matchConstraints(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }

        // 查询用户档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            log.debug("用户档案不存在，memberId={}", memberId);
            return Collections.emptyList();
        }

        return matchConstraints(gaokao);
    }

    @Override
    public List<String> matchConstraints(MemberGaokao gaokao) {
        if (gaokao == null) {
            return Collections.emptyList();
        }

        // 查询所有启用的约束
        List<ConstraintDict> constraints = constraintDictMapper.selectActiveList();
        if (constraints == null || constraints.isEmpty()) {
            return Collections.emptyList();
        }

        // 遍历匹配
        List<String> triggeredCodes = new ArrayList<>();
        for (ConstraintDict constraint : constraints) {
            if (isTriggered(gaokao, constraint)) {
                triggeredCodes.add(constraint.getCode());
            }
        }

        log.debug("约束匹配完成，memberId={}，触发约束数={}", gaokao.getMemberId(), triggeredCodes.size());
        return triggeredCodes;
    }

    /**
     * 判断约束是否被触发
     */
    private boolean isTriggered(MemberGaokao gaokao, ConstraintDict constraint) {
        // 主条件判断
        boolean mainResult = evaluateCondition(
                gaokao,
                constraint.getCheckField(),
                constraint.getCheckOperator(),
                constraint.getCheckValue()
        );

        // 无额外条件，直接返回主条件结果
        if (!StringUtils.hasText(constraint.getExtraField())) {
            return mainResult;
        }

        // 有额外条件，AND 关系
        boolean extraResult = evaluateCondition(
                gaokao,
                constraint.getExtraField(),
                constraint.getExtraOperator(),
                constraint.getExtraValue()
        );

        return mainResult && extraResult;
    }

    /**
     * 执行单个条件判断
     */
    private boolean evaluateCondition(MemberGaokao gaokao,
                                       String field,
                                       String operator,
                                       String value) {
        if (!StringUtils.hasText(field) || !StringUtils.hasText(operator)) {
            return false;
        }

        // 获取字段值
        Object fieldValue = fieldExtractor.extract(gaokao, field);

        // 获取策略并执行
        OperatorStrategy strategy = strategyFactory.getStrategy(operator);
        if (strategy == null) {
            log.warn("未知运算符: {}", operator);
            return false;
        }

        return strategy.evaluate(fieldValue, value);
    }
}
