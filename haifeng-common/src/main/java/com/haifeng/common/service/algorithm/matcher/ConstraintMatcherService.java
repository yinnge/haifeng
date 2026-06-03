package com.haifeng.common.service.algorithm.matcher;

import com.haifeng.common.entity.algorithm.MemberGaokao;

import java.util.List;

/**
 * 约束匹配服务
 */
public interface ConstraintMatcherService {

    /**
     * 根据用户档案匹配触发的约束列表
     *
     * @param memberId 会员ID
     * @return 触发的约束 code 列表
     */
    List<String> matchConstraints(Long memberId);

    /**
     * 根据用户档案对象匹配触发的约束列表
     *
     * @param gaokao 用户高考档案
     * @return 触发的约束 code 列表
     */
    List<String> matchConstraints(MemberGaokao gaokao);
}
