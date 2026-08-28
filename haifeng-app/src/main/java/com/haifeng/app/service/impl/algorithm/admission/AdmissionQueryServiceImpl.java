package com.haifeng.app.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.app.converter.SubjectsArrayConverter;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.app.service.algorithm.admission.AdmissionQueryService;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;
import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.algorithm.MajorHistoryItem;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.algorithm.matcher.SubjectMatchResult;
import com.haifeng.common.service.algorithm.matcher.SubjectMatcher;
import com.haifeng.common.service.algorithm.safety.SafetyLevelService;
import com.haifeng.common.service.algorithm.safety.dto.SafetyBatchContext;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
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
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<Map<String, Object>>> HISTORY_TYPE_REF =
            new TypeReference<List<Map<String, Object>>>() {};

    /** 批量加载专业明细时每组 IN 查询的 id 上限 */
    private static final int MAJOR_LOAD_BATCH_SIZE = 500;

    @Override
    public IPage<AdmissionGroupPageVO> pageGroups(AdmissionGroupQueryDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(ResultCode.GAOKAO_ARCHIVE_NOT_FOUND);
        }

        // 安全系数范围校验（左闭右开 [min, max)）
        BigDecimal minSafetyLevel = dto.getMinSafetyLevel();
        BigDecimal maxSafetyLevel = dto.getMaxSafetyLevel();
        if (minSafetyLevel != null && maxSafetyLevel != null
                && minSafetyLevel.compareTo(maxSafetyLevel) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        boolean hasSafetyFilter = minSafetyLevel != null || maxSafetyLevel != null;

        String province = gaokao.getGaokaoProvince();
        String batch = dto.getBatch();
        String batchPattern = "%" + batch.replace("%", "\\%").replace("_", "\\_") + "%";
        boolean subjectFilter = Boolean.TRUE.equals(dto.getSubjectFilter());

        String userSubjects = buildUserSubjectsArray(gaokao);

        int page = dto.getPage();
        int size = dto.getSize();
        int offset = (page - 1) * size;
        Short targetYear = gaokao.getGaokaoYear();

        String universityName = dto.getUniversityName();
        String cityName = dto.getCityName();
        String groupName = dto.getGroupName();
        String enrollmentCode = dto.getEnrollmentCode();

        List<AdmissionGroup> groups;
        long total = 0;
        Short actualYear;

        if (hasSafetyFilter) {
            // ===== 有安全系数筛选：全量加载 + 内存计算 + 内存分页 =====
            actualYear = targetYear;
            groups = admissionGroupMapper.selectAllByCondition(
                    province, batch, actualYear, subjectFilter, userSubjects,
                    universityName, cityName, groupName, enrollmentCode, batchPattern);
            if (groups.isEmpty()) {
                // 年份回退（与无筛选路径一致）
                actualYear = (short) (targetYear - 1);
                groups = admissionGroupMapper.selectAllByCondition(
                        province, batch, actualYear, subjectFilter, userSubjects,
                        universityName, cityName, groupName, enrollmentCode, batchPattern);
            }
            if (groups.isEmpty()) {
                return new Page<AdmissionGroupPageVO>(page, size).setTotal(0);
            }
        } else {
            // ===== 无筛选：原有 SQL 分页路径 =====
            groups = admissionGroupMapper.selectPageByCondition(
                    province, batch, targetYear, subjectFilter, userSubjects,
                    universityName, cityName, groupName, enrollmentCode, batchPattern, size, offset);
            total = admissionGroupMapper.countByCondition(
                    province, batch, targetYear, subjectFilter, userSubjects,
                    universityName, cityName, groupName, enrollmentCode, batchPattern);

            Short fallbackYear = null;
            if (groups.isEmpty() && total == 0) {
                fallbackYear = (short) (targetYear - 1);
                groups = admissionGroupMapper.selectPageByCondition(
                        province, batch, fallbackYear, subjectFilter, userSubjects,
                        universityName, cityName, groupName, enrollmentCode, batchPattern, size, offset);
                total = admissionGroupMapper.countByCondition(
                        province, batch, fallbackYear, subjectFilter, userSubjects,
                        universityName, cityName, groupName, enrollmentCode, batchPattern);
            }

            if (groups.isEmpty()) {
                return new Page<AdmissionGroupPageVO>(page, size).setTotal(total);
            }
            actualYear = fallbackYear != null ? fallbackYear : targetYear;
        }

        Short minYear = (short) (actualYear - 4);

        List<String> userConstraints = constraintMatcherService.matchConstraints(gaokao);
        // 批量预取计算上下文（密度/省配置/权重配置/severityMap，每次请求一次）
        SafetyBatchContext ctx = safetyLevelService.buildBatchContext(gaokao, userConstraints);

        Map<Integer, SafetyCalcResult> precomputedMaxSafety = null;
        Map<Integer, List<AdmissionMajorScore>> majorsByGroupId;

        if (hasSafetyFilter) {
            // 一次性加载全部组的专业明细（按 500 个 id 分批）
            List<Integer> allGroupIds = groups.stream()
                    .map(AdmissionGroup::getId)
                    .collect(Collectors.toList());
            majorsByGroupId = loadMajorsByGroupIds(allGroupIds);

            // 逐组计算组内最大安全系数，保留 safetyLevel ∈ [min, max) 的组（保持 SQL 排序顺序）
            List<AdmissionGroup> filtered = new ArrayList<>();
            Map<Integer, SafetyCalcResult> maxSafetyByGroupId = new HashMap<>();
            for (AdmissionGroup group : groups) {
                List<AdmissionMajorScore> majors =
                        majorsByGroupId.getOrDefault(group.getId(), Collections.emptyList());
                SafetyCalcResult maxSafety =
                        calculateGroupMaxSafety(group, gaokao, userConstraints, majors, minYear, ctx);
                BigDecimal level = maxSafety.getSafetyLevel();
                if (minSafetyLevel != null && level.compareTo(minSafetyLevel) < 0) {
                    continue;
                }
                if (maxSafetyLevel != null && level.compareTo(maxSafetyLevel) >= 0) {
                    continue;
                }
                filtered.add(group);
                maxSafetyByGroupId.put(group.getId(), maxSafety);
            }

            total = filtered.size();
            int fromIndex = Math.min(offset, filtered.size());
            int toIndex = Math.min(fromIndex + size, filtered.size());
            groups = new ArrayList<>(filtered.subList(fromIndex, toIndex));
            precomputedMaxSafety = maxSafetyByGroupId;
        } else {
            // 普通用户与 Pro 及以上均展示全部专业组明细（移除原先前 10 条限制）
            List<Integer> nonMaskedGroupIds = new ArrayList<>();
            for (AdmissionGroup group : groups) {
                nonMaskedGroupIds.add(group.getId());
            }
            majorsByGroupId = nonMaskedGroupIds.isEmpty()
                    ? Collections.emptyMap()
                    : loadMajorsByGroupIds(nonMaskedGroupIds);
        }

        List<AdmissionGroupPageVO> voList = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            AdmissionGroup group = groups.get(i);
            boolean shouldMask = false;

            if (shouldMask) {
                voList.add(AdmissionGroupPageVO.builder()
                        .id(group.getId())
                        .masked(true)
                        .build());
            } else {
                List<AdmissionMajorScore> majors =
                        majorsByGroupId.getOrDefault(group.getId(), Collections.emptyList());
                voList.add(buildGroupVO(group, gaokao, userConstraints, majors, minYear, ctx, precomputedMaxSafety));
            }
        }

        Page<AdmissionGroupPageVO> result = new Page<>(page, size);
        result.setRecords(voList);
        result.setTotal(total);
        return result;
    }

    /**
     * 批量加载专业明细（按 500 个 groupId 分批 IN 查询）
     */
    private Map<Integer, List<AdmissionMajorScore>> loadMajorsByGroupIds(List<Integer> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<AdmissionMajorScore>> result = new HashMap<>();
        for (int i = 0; i < groupIds.size(); i += MAJOR_LOAD_BATCH_SIZE) {
            List<Integer> batch = groupIds.subList(i, Math.min(i + MAJOR_LOAD_BATCH_SIZE, groupIds.size()));
            List<AdmissionMajorScore> majors = admissionMajorScoreMapper.selectList(
                    new LambdaQueryWrapper<AdmissionMajorScore>()
                            .in(AdmissionMajorScore::getGroupId, batch)
                            .eq(AdmissionMajorScore::getIsDeleted, false)
            );
            for (AdmissionMajorScore major : majors) {
                result.computeIfAbsent(major.getGroupId(), k -> new ArrayList<>()).add(major);
            }
        }
        return result;
    }

    /**
     * 计算专业组的最大安全系数（组级：使用各专业自身历史，与专业明细行口径一致，逐专业计算取最大值）
     */
    private SafetyCalcResult calculateGroupMaxSafety(AdmissionGroup group,
                                                     MemberGaokao gaokao,
                                                     List<String> userConstraints,
                                                     List<AdmissionMajorScore> majors,
                                                     Short minYear,
                                                     SafetyBatchContext ctx) {
        if (majors.isEmpty()) {
            return SafetyCalcResult.noData();
        }

        BigDecimal maxSafetyLevel = BigDecimal.ZERO;
        String levelShort = "禁";
        String safetyDescription = "";

        for (AdmissionMajorScore major : majors) {
            List<AdmissionGroup> historyGroups = majorHistoryToAdmissionGroups(major, minYear);
            SafetyCalcResult result = safetyLevelService.calculateMajorSafety(
                    gaokao, major, group, historyGroups, userConstraints, ctx
            );
            if (result.getSafetyLevel().compareTo(maxSafetyLevel) > 0) {
                maxSafetyLevel = result.getSafetyLevel();
                levelShort = result.getLevelShort();
                safetyDescription = result.getSafetyDescription();
            }
        }

        return SafetyCalcResult.builder()
                .safetyLevel(maxSafetyLevel)
                .levelShort(levelShort)
                .safetyDescription(safetyDescription)
                .build();
    }

    @Override
    public List<Integer> listExistingGroupIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        // selectList 受全局逻辑删除拦截器影响，会自动附加 is_deleted=false，
        // 因此已被物理删除或禁用的组都不会出现在结果中，返回即"当前有效的组"。
        List<AdmissionGroup> found = admissionGroupMapper.selectList(
                new LambdaQueryWrapper<AdmissionGroup>()
                        .select(AdmissionGroup::getId)
                        .in(AdmissionGroup::getId, ids)
        );
        return found.stream().map(AdmissionGroup::getId).collect(Collectors.toList());
    }

    @Override
    public IPage<AdmissionMajorPageVO> pageMajors(AdmissionMajorQueryDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(ResultCode.GAOKAO_ARCHIVE_NOT_FOUND);
        }

        AdmissionGroup group = admissionGroupMapper.selectById(dto.getGroupId());
        if (group == null || Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new BusinessException(ResultCode.ADMISSION_GROUP_NOT_FOUND);
        }
        if (!gaokao.getGaokaoProvince().equals(group.getProvince())) {
            throw new BusinessException(ResultCode.ADMISSION_GROUP_NOT_FOUND);
        }

        Page<AdmissionMajorScore> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionMajorScore::getGroupId, dto.getGroupId())
               .eq(AdmissionMajorScore::getIsDeleted, false);

        String majorName = dto.getMajorName();
        String majorCode = dto.getMajorCode();
        if (majorName != null && !majorName.isBlank()) {
            wrapper.like(AdmissionMajorScore::getMajorName, majorName);
        }
        if (majorCode != null && !majorCode.isBlank()) {
            wrapper.like(AdmissionMajorScore::getMajorCode, majorCode);
        }

        wrapper.orderByAsc(AdmissionMajorScore::getMajorCode);

        IPage<AdmissionMajorScore> majorPage = admissionMajorScoreMapper.selectPage(page, wrapper);

        if (majorPage.getRecords().isEmpty()) {
            return new Page<AdmissionMajorPageVO>(dto.getPage(), dto.getSize()).setTotal(0);
        }

        Short minYear = (short) (group.getYear() - 4);
        List<String> userConstraints = constraintMatcherService.matchConstraints(gaokao);

        List<AdmissionMajorPageVO> voList = majorPage.getRecords().stream()
                .map(major -> buildMajorVO(major, gaokao, group, userConstraints, minYear))
                .collect(Collectors.toList());

        Page<AdmissionMajorPageVO> result = new Page<>(dto.getPage(), dto.getSize());
        result.setRecords(voList);
        result.setTotal(majorPage.getTotal());
        return result;
    }

    // ==================== history jsonb 提取方法 ====================

    /**
     * 从 major.history jsonb 提取指定年份范围的历史分数
     */
    private List<YearScoreVO> extractMajorHistory(AdmissionMajorScore major, Short minYear) {
        List<Map<String, Object>> history = parseHistoryJson(major.getHistory());
        return history.stream()
                .filter(h -> intVal(h, "year") != null && intVal(h, "year") >= minYear)
                .sorted((a, b) -> intVal(b, "year") - intVal(a, "year"))
                .limit(5)
                .map(this::mapToYearScoreVO)
                .collect(Collectors.toList());
    }

    /**
     * 从 group.history jsonb 提取指定年份范围的历史分数
     */
    private List<YearScoreVO> extractGroupHistory(AdmissionGroup group, Short minYear) {
        List<Map<String, Object>> history = parseHistoryJson(group.getHistory());
        return history.stream()
                .filter(h -> intVal(h, "year") != null && intVal(h, "year") >= minYear)
                .sorted((a, b) -> intVal(b, "year") - intVal(a, "year"))
                .limit(5)
                .map(this::mapToYearScoreVO)
                .collect(Collectors.toList());
    }

    /**
     * 将 history jsonb 转换为 MajorHistoryItem 列表（供安全系数计算使用）
     */
    private List<MajorHistoryItem> historyToMajorHistoryItems(AdmissionMajorScore major, Short minYear) {
        List<Map<String, Object>> history = parseHistoryJson(major.getHistory());
        return history.stream()
                .filter(h -> intVal(h, "year") != null && intVal(h, "year") >= minYear)
                .sorted((a, b) -> intVal(b, "year") - intVal(a, "year"))
                .limit(5)
                .map(h -> MajorHistoryItem.builder()
                        .majorCode(major.getMajorCode())
                        .year(shortVal(h, "year"))
                        .minScore(intVal(h, "minScore"))
                        .minRank(intVal(h, "minRank"))
                        .avgScore(bdVal(h, "avgScore"))
                        .avgRank(intVal(h, "avgRank"))
                        .maxScore(intVal(h, "maxScore"))
                        .maxRank(intVal(h, "maxRank"))
                        .admissionCount(intVal(h, "admissionCount"))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 将 major.history jsonb 转换为 AdmissionGroup 列表（供专业级安全系数计算使用）
     * 用 major 自身的历史数据代替 group 聚合数据，精度更高
     */
    private List<AdmissionGroup> majorHistoryToAdmissionGroups(AdmissionMajorScore major, Short minYear) {
        List<Map<String, Object>> history = parseHistoryJson(major.getHistory());
        return history.stream()
                .filter(h -> intVal(h, "year") != null && intVal(h, "year") >= minYear)
                .sorted((a, b) -> intVal(b, "year") - intVal(a, "year"))
                .limit(5)
                .map(h -> {
                    AdmissionGroup g = new AdmissionGroup();
                    g.setYear(shortVal(h, "year"));
                    g.setMinScore(intVal(h, "minScore"));
                    g.setMinRank(intVal(h, "minRank"));
                    g.setAvgScore(bdVal(h, "avgScore"));
                    g.setAvgRank(intVal(h, "avgRank"));
                    g.setMaxScore(intVal(h, "maxScore"));
                    g.setMaxRank(intVal(h, "maxRank"));
                    g.setAdmissionCount(intVal(h, "admissionCount"));
                    return g;
                })
                .collect(Collectors.toList());
    }

    /**
     * 解析 history jsonb 为 List<Map>
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseHistoryJson(Object history) {
        if (history == null) return Collections.emptyList();
        if (history instanceof List) {
            return (List<Map<String, Object>>) history;
        }
        try {
            return objectMapper.readValue(history.toString(), HISTORY_TYPE_REF);
        } catch (Exception e) {
            log.warn("解析 history jsonb 失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== VO 构建 ====================

    private AdmissionMajorPageVO buildMajorVO(AdmissionMajorScore major,
                                              MemberGaokao gaokao,
                                              AdmissionGroup group,
                                              List<String> userConstraints,
                                              Short minYear) {
        List<YearScoreVO> historyScores = extractMajorHistory(major, minYear);

        List<AdmissionGroup> historyGroups = majorHistoryToAdmissionGroups(major, minYear);

        SafetyCalcResult safetyResult = safetyLevelService.calculateMajorSafety(
                gaokao, major, group, historyGroups, userConstraints
        );

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
                .constraints(major.getConstraints() == null ? Collections.emptyList() : major.getConstraints())
                .historyScores(historyScores)
                .build();
    }

    private AdmissionGroupPageVO buildGroupVO(AdmissionGroup group,
                                               MemberGaokao gaokao,
                                               List<String> userConstraints,
                                               List<AdmissionMajorScore> majors,
                                               Short minYear,
                                               SafetyBatchContext ctx,
                                               Map<Integer, SafetyCalcResult> precomputedMaxSafety) {
        SubjectMatchResult matchResult = subjectMatcher.match(gaokao, group);

        List<YearScoreVO> historyScores = extractGroupHistory(group, minYear);

        // 优先复用安全系数筛选阶段已计算的结果，避免重复计算
        SafetyCalcResult maxSafety = precomputedMaxSafety != null
                ? precomputedMaxSafety.get(group.getId())
                : null;
        if (maxSafety == null) {
            maxSafety = calculateGroupMaxSafety(group, gaokao, userConstraints, majors, minYear, ctx);
        }

        return AdmissionGroupPageVO.builder()
                .id(group.getId())
                .masked(false)
                .safetyLevel(maxSafety.getSafetyLevel())
                .levelShort(maxSafety.getLevelShort())
                .safetyDescription(maxSafety.getSafetyDescription())
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
                .subjectMatch(matchResult.isMatch())
                .subjectMatchReason(matchResult.getReason())
                .historyScores(historyScores)
                .build();
    }

    // ==================== 工具方法 ====================

    private YearScoreVO mapToYearScoreVO(Map<String, Object> map) {
        return YearScoreVO.builder()
                .year(shortVal(map, "year"))
                .minScore(intVal(map, "minScore"))
                .minRank(intVal(map, "minRank"))
                .avgScore(bdVal(map, "avgScore"))
                .avgRank(intVal(map, "avgRank"))
                .maxScore(intVal(map, "maxScore"))
                .maxRank(intVal(map, "maxRank"))
                .admissionCount(intVal(map, "admissionCount"))
                .build();
    }

    private Integer intVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private Short shortVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).shortValue();
        try { return Short.parseShort(v.toString()); } catch (Exception e) { return null; }
    }

    private BigDecimal bdVal(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? new BigDecimal(v.toString()) : null;
    }

    private String buildUserSubjectsArray(MemberGaokao gaokao) {
        List<String> subjects = gaokao.resolveAllSubjects();
        if (subjects.isEmpty()) {
            return null;
        }
        return SubjectsArrayConverter.toPgArrayLiteral(subjects);
    }
}
