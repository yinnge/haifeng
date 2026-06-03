package com.haifeng.app.service.algorithm;

import com.haifeng.app.vo.algorithm.CheckGroupResultVO;
import com.haifeng.app.vo.algorithm.ConstraintDetailsVO;
import com.haifeng.app.vo.algorithm.ConstraintMatchVO;

import java.util.List;

/**
 * 约束服务接口
 */
public interface ConstraintService {

    /**
     * 获取当前用户触发的约束列表
     *
     * @return 约束匹配结果
     */
    ConstraintMatchVO matchConstraints();

    /**
     * 根据约束代码列表获取约束详情
     *
     * @param codes 约束代码列表
     * @return 约束详情列表
     */
    ConstraintDetailsVO getConstraintDetails(List<String> codes);

    /**
     * 校验当前用户是否满足专业组约束
     *
     * @param groupId 专业组ID
     * @return 校验结果
     */
    CheckGroupResultVO checkGroupConstraints(Integer groupId);
}
