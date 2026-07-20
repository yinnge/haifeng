package com.haifeng.common.service.algorithm.matcher;

import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.service.algorithm.matcher.operator.OperatorStrategy;
import com.haifeng.common.service.algorithm.matcher.operator.OperatorStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 约束匹配服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstraintMatcherServiceImpl implements ConstraintMatcherService {

    private static final long CACHE_TTL_MINUTES = 5;

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final OperatorStrategyFactory strategyFactory;
    private final FieldValueExtractor fieldExtractor;
    private final RedisTemplate<String, Object> redisTemplate;

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

        // 查询所有启用的约束（优先走缓存）
        List<ConstraintDict> constraints = getActiveConstraints();
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
     * 获取活跃约束列表（优先缓存）
     */
    @SuppressWarnings("unchecked")
    private List<ConstraintDict> getActiveConstraints() {
        String cacheKey = RedisKeyConstant.CONSTRAINT_ACTIVE_LIST_KEY;
        try {
            List<ConstraintDict> cached = (List<ConstraintDict>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("约束缓存命中");
                return cached;
            }
        } catch (Exception e) {
            log.warn("读取约束缓存失败，降级查库", e);
        }

        List<ConstraintDict> list = constraintDictMapper.selectActiveList();

        try {
            if (list != null && !list.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, list, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("写入约束缓存失败", e);
        }

        return list;
    }

    /**
     * 主动清除约束缓存（管理员修改约束后调用）
     */
    public void evictConstraintCache() {
        try {
            redisTemplate.delete(RedisKeyConstant.CONSTRAINT_ACTIVE_LIST_KEY);
            log.info("约束缓存已清除");
        } catch (Exception e) {
            log.warn("清除约束缓存失败", e);
        }
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

        // 有额外条件但缺少运算符，只返回主条件结果
        if (!StringUtils.hasText(constraint.getExtraOperator())) {
            log.warn("约束配置异常：extraField 存在但 extraOperator 为空，code={}", constraint.getCode());
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
