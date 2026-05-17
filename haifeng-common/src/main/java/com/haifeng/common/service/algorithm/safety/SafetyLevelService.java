package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;

import java.math.BigDecimal;
import java.util.List;

public interface SafetyLevelService {

    /**
     * 计算专业明细的安全系数
     *
     * @param gaokao          用户档案
     * @param major           专业明细
     * @param group           所属专业组
     * @param historyGroups   历史专业组数据（近5年）
     * @param userConstraints 用户触发的约束 codes
     * @return 计算结果
     */
    SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                          AdmissionMajorScore major,
                                          AdmissionGroup group,
                                          List<AdmissionGroup> historyGroups,
                                          List<String> userConstraints);

    /**
     * 根据系数获取等级信息
     *
     * @param coefficient 安全系数
     * @return 等级字典
     */
    SafetyLevelDict getLevelByCoefficient(BigDecimal coefficient);
}
