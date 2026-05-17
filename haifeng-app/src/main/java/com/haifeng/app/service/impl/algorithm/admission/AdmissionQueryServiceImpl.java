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
import com.haifeng.common.service.algorithm.safety.SafetyLevelService;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;

import java.math.BigDecimal;
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
    private final SafetyLevelService safetyLevelService;
    private final ConstraintMatcherService constraintMatcherService;

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

        // 获取用户约束
        List<String> userConstraints = constraintMatcherService.matchConstraints(gaokao);

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
                voList.add(buildGroupVO(group, historyMap, gaokao, userConstraints));
            }
        }

        Page<AdmissionGroupPageVO> result = new Page<>(dto.getPage(), size);
        result.setRecords(voList);
        result.setTotal(total);
        return result;
    }

    @Override
    public IPage<AdmissionMajorPageVO> pageMajors(AdmissionMajorQueryDTO dto) {
        // 1. 查询当前专业组
        AdmissionGroup group = admissionGroupMapper.selectById(dto.getGroupId());
        if (group == null || Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new BusinessException(ResultCode.ADMISSION_GROUP_NOT_FOUND);
        }

        // 2. SQL1: 分页查询专业明细
        Page<AdmissionMajorScore> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionMajorScore::getGroupId, dto.getGroupId())
               .orderByAsc(AdmissionMajorScore::getMajorCode);

        IPage<AdmissionMajorScore> majorPage = admissionMajorScoreMapper.selectPage(page, wrapper);

        if (majorPage.getRecords().isEmpty()) {
            return new Page<AdmissionMajorPageVO>(dto.getPage(), dto.getSize()).setTotal(0);
        }

        // 3. SQL2: IN批量查历史数据
        List<String> majorCodes = majorPage.getRecords().stream()
                .map(AdmissionMajorScore::getMajorCode)
                .collect(Collectors.toList());

        Short minYear = (short) (Year.now().getValue() - 4);
        List<Map<String, Object>> historyList = admissionMajorScoreMapper.selectHistoryByMajorCodes(
                group.getUniversityId(), majorCodes, minYear);

        // 按 major_code 分组
        Map<String, List<Map<String, Object>>> historyMap = historyList.stream()
                .collect(Collectors.groupingBy(m -> (String) m.get("major_code")));

        // 获取用户档案和约束
        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        List<String> userConstraints = gaokao != null
                ? constraintMatcherService.matchConstraints(gaokao)
                : Collections.emptyList();

        // 查询历史专业组数据
        Short historyMinYear = (short) (Year.now().getValue() - 4);
        List<GroupKey> keys = Collections.singletonList(
                new GroupKey(group.getUniversityId(), group.getGroupCode())
        );
        List<AdmissionGroup> historyGroups = admissionGroupMapper.selectHistoryByKeys(keys, historyMinYear);

        // 4. 组装 VO
        final MemberGaokao finalGaokao = gaokao;
        final AdmissionGroup finalGroup = group;
        final List<AdmissionGroup> finalHistoryGroups = historyGroups;
        final List<String> finalUserConstraints = userConstraints;

        List<AdmissionMajorPageVO> voList = majorPage.getRecords().stream()
                .map(major -> buildMajorVO(major, historyMap, finalGaokao, finalGroup,
                        finalHistoryGroups, finalUserConstraints))
                .collect(Collectors.toList());

        Page<AdmissionMajorPageVO> result = new Page<>(dto.getPage(), dto.getSize());
        result.setRecords(voList);
        result.setTotal(majorPage.getTotal());
        return result;
    }

    private AdmissionMajorPageVO buildMajorVO(AdmissionMajorScore major,
                                              Map<String, List<Map<String, Object>>> historyMap,
                                              MemberGaokao gaokao,
                                              AdmissionGroup group,
                                              List<AdmissionGroup> historyGroups,
                                              List<String> userConstraints) {
        List<Map<String, Object>> history = historyMap.getOrDefault(major.getMajorCode(), Collections.emptyList());
        List<YearScoreVO> historyScores = history.stream()
                .limit(5)
                .map(this::mapToYearScoreVO)
                .collect(Collectors.toList());

        // 计算安全系数
        SafetyCalcResult safetyResult;
        if (gaokao != null) {
            safetyResult = safetyLevelService.calculateMajorSafety(
                    gaokao, major, group, historyGroups, userConstraints
            );
        } else {
            safetyResult = SafetyCalcResult.noData();
        }

        return AdmissionMajorPageVO.builder()
                .id(major.getId())
                .safetyLevel(safetyResult.getSafetyLevel())
                .levelShort(safetyResult.getLevelShort())
                .safetyDescription(safetyResult.getSafetyDescription())
                .majorCode(major.getMajorCode())
                .majorName(major.getMajorName())
                .educationLevel(major.getEducationLevel())
                .duration(major.getDuration())
                .tuition(major.getTuition())
                .description(major.getDescription())
                .constraints(major.getConstraints())
                .historyScores(historyScores)
                .build();
    }

    private YearScoreVO mapToYearScoreVO(Map<String, Object> map) {
        return YearScoreVO.builder()
                .year(map.get("year") != null ? ((Number) map.get("year")).shortValue() : null)
                .minScore(map.get("min_score") != null ? ((Number) map.get("min_score")).intValue() : null)
                .minRank(map.get("min_rank") != null ? ((Number) map.get("min_rank")).intValue() : null)
                .avgScore(map.get("avg_score") != null ? new java.math.BigDecimal(map.get("avg_score").toString()) : null)
                .avgRank(map.get("avg_rank") != null ? ((Number) map.get("avg_rank")).intValue() : null)
                .maxScore(map.get("max_score") != null ? ((Number) map.get("max_score")).intValue() : null)
                .maxRank(map.get("max_rank") != null ? ((Number) map.get("max_rank")).intValue() : null)
                .admissionCount(map.get("admission_count") != null ? ((Number) map.get("admission_count")).intValue() : null)
                .build();
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
                                               MemberGaokao gaokao,
                                               List<String> userConstraints) {
        // 选科匹配
        SubjectMatchResult matchResult = subjectMatcher.match(gaokao, group);

        // 历史数据
        String key = group.getUniversityId() + "_" + group.getGroupCode();
        List<AdmissionGroup> history = historyMap.getOrDefault(key, Collections.emptyList());
        List<YearScoreVO> historyScores = history.stream()
                .limit(5)
                .map(this::toYearScoreVO)
                .collect(Collectors.toList());

        // 计算专业组安全系数 = max(所有专业明细的安全系数)
        BigDecimal maxSafetyLevel = BigDecimal.ZERO;
        String levelShort = "禁";
        String safetyDescription = "";

        // 查询该组下所有专业明细
        List<AdmissionMajorScore> majors = admissionMajorScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionMajorScore>()
                        .eq(AdmissionMajorScore::getGroupId, group.getId())
        );

        for (AdmissionMajorScore major : majors) {
            SafetyCalcResult result = safetyLevelService.calculateMajorSafety(
                    gaokao, major, group, history, userConstraints
            );
            if (result.getSafetyLevel().compareTo(maxSafetyLevel) > 0) {
                maxSafetyLevel = result.getSafetyLevel();
                levelShort = result.getLevelShort();
                safetyDescription = result.getSafetyDescription();
            }
        }

        // 如果没有专业明细，使用默认值
        if (majors.isEmpty()) {
            maxSafetyLevel = new BigDecimal("0.50");
            levelShort = "稳";
            safetyDescription = "暂无专业明细数据";
        }

        return AdmissionGroupPageVO.builder()
                .id(group.getId())
                .masked(false)
                .safetyLevel(maxSafetyLevel)
                .levelShort(levelShort)
                .safetyDescription(safetyDescription)
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
