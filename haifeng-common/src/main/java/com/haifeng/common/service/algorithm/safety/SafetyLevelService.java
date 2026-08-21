package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.service.algorithm.safety.dto.SafetyBatchContext;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcContext;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;

import java.math.BigDecimal;
import java.util.List;

public interface SafetyLevelService {

    /**
     * 预取批量计算上下文（密度/省配置/GaokaoConfig/约束severityMap 各查一次）
     * 供同一用户的批量安全系数计算复用
     *
     * @param gaokao          用户档案
     * @param userConstraints 用户触发的约束 codes
     * @return 批量计算上下文
     */
    SafetyBatchContext buildBatchContext(MemberGaokao gaokao, List<String> userConstraints);

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
     * 计算专业明细的安全系数（基于批量预取上下文）
     * 与 calculateMajorSafety(gaokao, major, group, historyGroups, userConstraints) 语义完全一致，
     * 但使用预取的密度/省配置/约束权重配置/severityMap，无逐专业 DB 查询；
     * 等级反查使用 SafetyLevelDictCache
     *
     * @param gaokao          用户档案
     * @param major           专业明细
     * @param group           所属专业组
     * @param historyGroups   历史专业组数据（近5年）
     * @param userConstraints 用户触发的约束 codes
     * @param ctx             批量预取上下文
     * @return 计算结果
     */
    SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                          AdmissionMajorScore major,
                                          AdmissionGroup group,
                                          List<AdmissionGroup> historyGroups,
                                          List<String> userConstraints,
                                          SafetyBatchContext ctx);

    /**
     * 计算专业明细的安全系数（基于预聚合上下文）
     * 使用 SafetyCalcContext 中预查询的密度、省份配置、专业历史等数据，
     * 避免在循环中重复查询数据库
     *
     * @param gaokao          用户档案
     * @param major           专业明细
     * @param group           所属专业组
     * @param userConstraints 用户触发的约束 codes
     * @param context         预聚合上下文
     * @return 计算结果
     */
    SafetyCalcResult calculateMajorSafety(MemberGaokao gaokao,
                                          AdmissionMajorScore major,
                                          AdmissionGroup group,
                                          List<String> userConstraints,
                                          SafetyCalcContext context);

    /**
     * 根据系数获取等级信息
     *
     * @param coefficient 安全系数
     * @return 等级字典
     */
    SafetyLevelDict getLevelByCoefficient(BigDecimal coefficient);
}
