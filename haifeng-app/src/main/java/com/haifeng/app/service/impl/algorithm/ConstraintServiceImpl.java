package com.haifeng.app.service.impl.algorithm;

import com.haifeng.app.service.algorithm.ConstraintService;
import com.haifeng.app.vo.algorithm.*;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 约束服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstraintServiceImpl implements ConstraintService {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final ConstraintMatcherService constraintMatcherService;

    @Override
    public ConstraintMatchVO matchConstraints() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 查询用户档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(ResultCode.GAOKAO_ARCHIVE_NOT_FOUND);
        }

        // 调用 common 的 matcher 匹配约束
        List<String> triggeredCodes = constraintMatcherService.matchConstraints(gaokao);

        return ConstraintMatchVO.builder()
                .constraintCodes(triggeredCodes)
                .totalCount(triggeredCodes.size())
                .build();
    }

    @Override
    public ConstraintDetailsVO getConstraintDetails(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return ConstraintDetailsVO.builder()
                    .constraints(Collections.emptyList())
                    .build();
        }

        // 批量查询约束详情（仅未删除的）
        List<ConstraintDict> constraints = constraintDictMapper.selectActiveBatchIds(codes);

        List<ConstraintDetailVO> details = constraints.stream()
                .map(this::toDetailVO)
                .collect(Collectors.toList());

        return ConstraintDetailsVO.builder()
                .constraints(details)
                .build();
    }

    @Override
    public CheckGroupResultVO checkGroupConstraints(Integer groupId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 查询用户档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(ResultCode.GAOKAO_ARCHIVE_NOT_FOUND);
        }

        // 查询专业组
        AdmissionGroup group = admissionGroupMapper.selectById(groupId);
        if (group == null || Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new BusinessException(ResultCode.ADMISSION_GROUP_NOT_FOUND);
        }

        // 调用 common 的 matcher 获取用户触发的约束
        List<String> userConstraints = constraintMatcherService.matchConstraints(gaokao);
        Set<String> userConstraintSet = new HashSet<>(userConstraints);

        // 获取专业组的约束
        List<String> groupConstraints = group.getConstraints();
        if (groupConstraints == null || groupConstraints.isEmpty()) {
            // 专业组无约束限制，直接通过
            return CheckGroupResultVO.builder()
                    .isPass(true)
                    .hardConflicts(Collections.emptyList())
                    .softConflicts(Collections.emptyList())
                    .build();
        }

        // 查找冲突的约束
        List<String> conflictCodes = groupConstraints.stream()
                .filter(userConstraintSet::contains)
                .collect(Collectors.toList());

        if (conflictCodes.isEmpty()) {
            return CheckGroupResultVO.builder()
                    .isPass(true)
                    .hardConflicts(Collections.emptyList())
                    .softConflicts(Collections.emptyList())
                    .build();
        }

        // 查询冲突约束的详情（仅未删除的）
        List<ConstraintDict> conflictConstraints = constraintDictMapper.selectActiveBatchIds(conflictCodes);

        // 按严重程度分类
        List<ConstraintConflictVO> hardConflicts = new ArrayList<>();
        List<ConstraintConflictVO> softConflicts = new ArrayList<>();

        for (ConstraintDict constraint : conflictConstraints) {
            ConstraintConflictVO conflict = ConstraintConflictVO.builder()
                    .code(constraint.getCode())
                    .name(constraint.getName())
                    .description(constraint.getDescription())
                    .build();

            if ("HARD".equals(constraint.getSeverity())) {
                hardConflicts.add(conflict);
            } else {
                softConflicts.add(conflict);
            }
        }

        return CheckGroupResultVO.builder()
                .isPass(hardConflicts.isEmpty())
                .hardConflicts(hardConflicts)
                .softConflicts(softConflicts)
                .build();
    }

    /**
     * 转换为详情VO
     */
    private ConstraintDetailVO toDetailVO(ConstraintDict dict) {
        return ConstraintDetailVO.builder()
                .code(dict.getCode())
                .name(dict.getName())
                .category(dict.getCategory())
                .description(dict.getDescription())
                .severity(dict.getSeverity())
                .build();
    }
}
