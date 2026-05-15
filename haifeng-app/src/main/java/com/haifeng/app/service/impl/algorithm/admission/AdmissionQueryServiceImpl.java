package com.haifeng.app.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.app.service.algorithm.admission.AdmissionQueryService;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;
import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.algorithm.GroupKey;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.algorithm.matcher.SubjectMatchResult;
import com.haifeng.common.service.algorithm.matcher.SubjectMatcher;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionQueryServiceImpl implements AdmissionQueryService {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final SubjectMatcher subjectMatcher;

    @Override
    public IPage<AdmissionGroupPageVO> pageGroups(AdmissionGroupQueryDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 1. 获取用户档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(ResultCode.GAOKAO_ARCHIVE_NOT_FOUND);
        }

        String province = gaokao.getGaokaoProvince();
        String batch = dto.getBatch();
        boolean subjectFilter = Boolean.TRUE.equals(dto.getSubjectFilter());

        // 构建用户选科数组字符串（PostgreSQL格式）
        String userSubjects = buildUserSubjectsArray(gaokao);

        // 2. SQL1: 分页查询专业组
        int size = 20; // 固定20条
        int offset = (dto.getPage() - 1) * size;

        List<AdmissionGroup> groups = admissionGroupMapper.selectPageByCondition(
                province, batch, subjectFilter, userSubjects, size, offset);

        long total = admissionGroupMapper.countByCondition(province, batch, subjectFilter, userSubjects);

        if (groups.isEmpty()) {
            return new Page<AdmissionGroupPageVO>(dto.getPage(), size).setTotal(total);
        }

        // 3. SQL2: IN批量查历史数据
        List<GroupKey> keys = groups.stream()
                .map(g -> new GroupKey(g.getUniversityId(), g.getGroupCode()))
                .collect(Collectors.toList());

        Short minYear = (short) (Year.now().getValue() - 4);
        List<AdmissionGroup> historyList = admissionGroupMapper.selectHistoryByKeys(keys, minYear);

        // 按 university_id + group_code 分组
        Map<String, List<AdmissionGroup>> historyMap = historyList.stream()
                .collect(Collectors.groupingBy(g -> g.getUniversityId() + "_" + g.getGroupCode()));

        // 4. 判断会员类型
        String memberType = SecurityUtil.getCurrentMemberType();
        boolean isPremium = "pro".equals(memberType) || "vip".equals(memberType);

        // 5. 组装 VO
        List<AdmissionGroupPageVO> voList = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            AdmissionGroup group = groups.get(i);
            boolean shouldMask = !isPremium && i >= 10;

            if (shouldMask) {
                voList.add(AdmissionGroupPageVO.builder()
                        .id(group.getId())
                        .masked(true)
                        .build());
            } else {
                voList.add(buildGroupVO(group, historyMap, gaokao));
            }
        }

        Page<AdmissionGroupPageVO> result = new Page<>(dto.getPage(), size);
        result.setRecords(voList);
        result.setTotal(total);
        return result;
    }

    @Override
    public IPage<AdmissionMajorPageVO> pageMajors(AdmissionMajorQueryDTO dto) {
        // Task 9 实现
        return null;
    }

    private String buildUserSubjectsArray(MemberGaokao gaokao) {
        List<String> subjects = new ArrayList<>();
        if (gaokao.getSubjectType() != null) subjects.add(gaokao.getSubjectType());
        if (gaokao.getSecondSubjectType() != null) subjects.add(gaokao.getSecondSubjectType());
        if (gaokao.getThirdSubjectType() != null) subjects.add(gaokao.getThirdSubjectType());

        if (subjects.isEmpty()) return null;

        // PostgreSQL 数组格式: {物理,化学,生物}
        return "{" + String.join(",", subjects) + "}";
    }

    private AdmissionGroupPageVO buildGroupVO(AdmissionGroup group,
                                               Map<String, List<AdmissionGroup>> historyMap,
                                               MemberGaokao gaokao) {
        // 选科匹配
        SubjectMatchResult matchResult = subjectMatcher.match(gaokao, group);

        // 历史数据
        String key = group.getUniversityId() + "_" + group.getGroupCode();
        List<AdmissionGroup> history = historyMap.getOrDefault(key, Collections.emptyList());
        List<YearScoreVO> historyScores = history.stream()
                .limit(5)
                .map(this::toYearScoreVO)
                .collect(Collectors.toList());

        return AdmissionGroupPageVO.builder()
                .id(group.getId())
                .masked(false)
                .universityName(group.getUniversityName())
                .cityName(group.getCityName())
                .enrollmentCode(group.getEnrollmentCode())
                .groupCode(group.getGroupCode())
                .groupName(group.getGroupName())
                .subjects(group.getSubjects())
                .requirementType(group.getRequirementType())
                .description(group.getDescription())
                .majorCount(group.getMajorCount())
                .categoryCount(group.getCategoryCount())
                .constraints(group.getConstraints())
                .subjectMatch(matchResult.isMatch())
                .subjectMatchReason(matchResult.getReason())
                .historyScores(historyScores)
                .build();
    }

    private YearScoreVO toYearScoreVO(AdmissionGroup group) {
        return YearScoreVO.builder()
                .year(group.getYear())
                .minScore(group.getMinScore())
                .minRank(group.getMinRank())
                .avgScore(group.getAvgScore())
                .avgRank(group.getAvgRank())
                .maxScore(group.getMaxScore())
                .maxRank(group.getMaxRank())
                .admissionCount(group.getAdmissionCount())
                .build();
    }
}
