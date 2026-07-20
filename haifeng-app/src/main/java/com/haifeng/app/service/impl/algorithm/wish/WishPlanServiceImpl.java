package com.haifeng.app.service.impl.algorithm.wish;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.algorithm.wish.WishGroupExportAllDTO;
import com.haifeng.app.dto.algorithm.wish.WishGroupSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorExportDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.util.algorithm.pdf.EnrichmentLoader;
import com.haifeng.app.util.algorithm.wish.WishPlanExcelUtil;
import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
import com.haifeng.app.vo.algorithm.pdf.CityEnrichmentVO;
import com.haifeng.app.vo.algorithm.pdf.ExportGroupContextVO;
import com.haifeng.app.vo.algorithm.pdf.MajorEnrichmentVO;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportFileVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportProgressVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.algorithm.*;
import com.haifeng.common.entity.algorithm.wish.WishGroupSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.mapper.algorithm.*;
import com.haifeng.common.mapper.algorithm.wish.WishGroupSnapshotMapper;
import com.haifeng.common.mapper.algorithm.wish.WishMajorSnapshotMapper;
import com.haifeng.common.mapper.algorithm.wish.WishPlanMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.service.algorithm.ProvinceReformService;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.service.algorithm.safety.SafetyLevelService;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcContext;
import com.haifeng.common.service.algorithm.safety.dto.SafetyCalcResult;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishPlanServiceImpl implements WishPlanService {

    private static final long CACHE_TTL_HOURS = 24;

    private final SystemSettingsMapper settingsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private final WishPlanMapper wishPlanMapper;
    private final WishGroupSnapshotMapper wishGroupSnapshotMapper;
    private final WishMajorSnapshotMapper wishMajorSnapshotMapper;
    private final MemberGaokaoMapper memberGaokaoMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final UniversityMapper universityMapper;
    private final SafetyLevelService safetyLevelService;
    private final ConstraintMatcherService constraintMatcherService;
    private final ConstraintDictMapper constraintDictMapper;
    private final ScoreRankMapper scoreRankMapper;
    private final ProvinceConfigMapper provinceConfigMapper;
    private final ProvinceReformService provinceReformService;
    private final WishPlanExcelUtil wishPlanExcelUtil;
    private final EnrichmentLoader enrichmentLoader;
    private final TransactionTemplate transactionTemplate;

    @Override
    public WishPlanLimitVO getDefaultLimits() {
        String cacheKey = RedisKeyConstant.WISH_PLAN_DEFAULT_LIMITS_KEY;
        WishPlanLimitVO cached = safeGetFromCache(cacheKey);
        if (cached != null) {
            log.debug("志愿方案默认数量限制缓存命中");
            return cached;
        }
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));
        WishPlanLimitVO vo;
        if (settings == null) {
            log.warn("system_settings 表为空，返回零值默认数量限制");
            vo = WishPlanLimitVO.builder()
                    .reachHighCount(0).reachCount(0).matchCount(0).safeCount(0).floorCount(0).build();
        } else {
            vo = WishPlanLimitVO.builder()
                    .reachHighCount(settings.getReachHighCount())
                    .reachCount(settings.getReachCount())
                    .matchCount(settings.getMatchCount())
                    .safeCount(settings.getSafeCount())
                    .floorCount(settings.getFloorCount())
                    .build();
        }
        safeSetCache(cacheKey, vo);
        return vo;
    }

    @Override
    public WishPlanListVO addMajors(WishPlanAddMajorsDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 1. 获取高考档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(ResultCode.GAOKAO_ARCHIVE_NOT_FOUND);
        }

        // 2. 查询专业明细
        List<AdmissionMajorScore> majorScores = admissionMajorScoreMapper.selectBatchIds(dto.getMajorIds());
        if (majorScores.size() != dto.getMajorIds().size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部分专业明细不存在");
        }

        // 3. 查询专业组
        AdmissionGroup group = admissionGroupMapper.selectById(dto.getGroupId());
        if (group == null || Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new BusinessException(ResultCode.ADMISSION_GROUP_NOT_FOUND);
        }

        // C7. 校验专业是否属于指定的专业组
        for (AdmissionMajorScore ms : majorScores) {
            if (!ms.getGroupId().equals(dto.getGroupId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "专业「" + ms.getMajorName() + "」不属于指定的专业组");
            }
        }

        // 4. 预查询安全系数计算所需上下文
        List<String> constraintCodes = constraintMatcherService.matchConstraints(gaokao);
        if (constraintCodes == null) constraintCodes = Collections.emptyList();

        Set<String> allConstraintCodes = new HashSet<>(constraintCodes);
        if (group.getConstraints() != null) allConstraintCodes.addAll(group.getConstraints());
        for (AdmissionMajorScore m : majorScores) {
            if (m.getConstraints() != null) allConstraintCodes.addAll(m.getConstraints());
        }
        Map<String, String> severityMap = allConstraintCodes.isEmpty()
                ? Collections.emptyMap()
                : constraintDictMapper.selectSeverityByCodes(new ArrayList<>(allConstraintCodes)).stream()
                        .collect(Collectors.toMap(ConstraintDict::getCode, ConstraintDict::getSeverity, (a, b) -> a));

        Short majorMinYear = (short) (gaokao.getGaokaoYear() - 5);
        List<String> majorCodes = majorScores.stream().map(AdmissionMajorScore::getMajorCode).collect(Collectors.toList());
        List<MajorHistoryItem> majorHistoryList = admissionMajorScoreMapper.selectMajorHistoryItems(
                group.getUniversityId(), group.getId(), majorCodes, majorMinYear);
        Map<String, List<MajorHistoryItem>> majorHistoryMap = majorHistoryList.stream()
                .filter(m -> m.getMajorCode() != null)
                .collect(Collectors.groupingBy(MajorHistoryItem::getMajorCode));

        BigDecimal density = queryDensity(gaokao);
        ProvinceConfig provinceConfig = queryProvinceConfig(gaokao);
        Short reformYear = queryReformYear(gaokao);

        // 5. 计算每个专业的安全系数，检查"禁"级别，收集数据
        List<MajorSafetyInfo> majorInfos = new ArrayList<>();
        for (AdmissionMajorScore major : majorScores) {
            List<MajorHistoryItem> history = majorHistoryMap.getOrDefault(major.getMajorCode(), Collections.emptyList());
            SafetyCalcContext ctx = SafetyCalcContext.builder()
                    .density(density)
                    .provinceConfig(provinceConfig)
                    .reformYear(reformYear)
                    .severityMap(severityMap)
                    .majorHistory(history)
                    .build();
            SafetyCalcResult result = safetyLevelService.calculateMajorSafety(
                    gaokao, major, group, constraintCodes, ctx);

            if ("禁".equals(result.getLevelShort())) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "专业「" + major.getMajorName() + "」为'禁'级别，不允许添加到志愿表");
            }

            // 构建 HistoryScore 用于存储到 snapshot
            List<WishMajorSnapshot.HistoryScore> historyScores = history.stream()
                    .limit(5)
                    .map(h -> WishMajorSnapshot.HistoryScore.builder()
                            .year((int) h.getYear())
                            .minScore(h.getMinScore())
                            .minRank(h.getMinRank())
                            .avgScore(h.getAvgScore())
                            .avgRank(h.getAvgRank())
                            .maxScore(h.getMaxScore())
                            .maxRank(h.getMaxRank())
                            .admissionCount(h.getAdmissionCount())
                            .build())
                    .collect(Collectors.toList());

            majorInfos.add(new MajorSafetyInfo(major, result, historyScores));
        }

        // L3. 仅包裹 DB 写操作，CPU 密集型计算已在事务外完成
        return transactionTemplate.execute(status -> {
            // 6. 获取或创建 WishPlan（plan 数量限制检查在 getOrCreatePlan 内部，仅新建时检查）
            WishPlan plan;
            if (dto.getPlanId() != null) {
                plan = wishPlanMapper.selectById(dto.getPlanId());
                if (plan == null || plan.getDeleted() || !memberId.equals(plan.getMemberId())) {
                    throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
                }
            } else {
                plan = getOrCreatePlan(memberId, gaokao);
            }

            // 7. 校验档位数量限制
            WishPlanLimitVO limits = getDefaultLimits();
            Map<String, Integer> planCounts = new HashMap<>();
            planCounts.put("搏", plan.getBoLimit() != null ? plan.getBoLimit() : 0);
            planCounts.put("冲", plan.getChongLimit() != null ? plan.getChongLimit() : 0);
            planCounts.put("稳", plan.getWenLimit() != null ? plan.getWenLimit() : 0);
            planCounts.put("保", plan.getBaoLimit() != null ? plan.getBaoLimit() : 0);
            planCounts.put("垫", plan.getDieLimit() != null ? plan.getDieLimit() : 0);

            Map<String, Integer> maxLimits = new HashMap<>();
            maxLimits.put("搏", limits.getReachHighCount());
            maxLimits.put("冲", limits.getReachCount());
            maxLimits.put("稳", limits.getMatchCount());
            maxLimits.put("保", limits.getSafeCount());
            maxLimits.put("垫", limits.getFloorCount());

            Map<String, Integer> newCounts = new HashMap<>();
            for (MajorSafetyInfo info : majorInfos) {
                String ls = info.result.getLevelShort();
                newCounts.merge(ls, 1, Integer::sum);
            }

            for (Map.Entry<String, Integer> entry : newCounts.entrySet()) {
                String level = entry.getKey();
                int newCnt = entry.getValue();
                int current = planCounts.getOrDefault(level, 0);
                int maxAllowed = maxLimits.getOrDefault(level, Integer.MAX_VALUE);
                if (current + newCnt > maxAllowed) {
                    throw new BusinessException(ResultCode.BAD_REQUEST,
                            level + "档专业已选" + current + "个，最多" + maxAllowed + "个，本次添加" + newCnt + "个超出限制");
                }
            }

            // 8. 创建/获取 WishGroupSnapshot (去重: 一个 plan 一个 groupId 只存一条)
            WishGroupSnapshot groupSnap = wishGroupSnapshotMapper.selectOne(
                    new LambdaQueryWrapper<WishGroupSnapshot>()
                            .eq(WishGroupSnapshot::getPlanId, plan.getId())
                            .eq(WishGroupSnapshot::getGroupId, dto.getGroupId())
                            .last("LIMIT 1"));

            if (groupSnap == null) {
                University univ = universityMapper.selectById(group.getUniversityId());

                groupSnap = WishGroupSnapshot.builder()
                        .planId(plan.getId())
                        .groupId(dto.getGroupId())
                        .groupSortOrder(0)
                        .universityId(group.getUniversityId())
                        .universityName(group.getUniversityName())
                        .cityName(group.getCityName())
                        .year(group.getYear())
                        .province(group.getProvince())
                        .batch(group.getBatch())
                        .enrollmentCode(group.getEnrollmentCode())
                        .groupCode(group.getGroupCode())
                        .groupName(group.getGroupName())
                        .subjects(group.getSubjects())
                        .description(group.getDescription())
                        .constraintsDescription(group.getConstraints())
                        .category(univ != null ? univ.getCategory() : null)
                        .majorCount(group.getMajorCount())
                        .nature(univ != null ? univ.getNature() : null)
                        .tags(univ != null ? univ.getTags() : null)
                        .recommendationYear(univ != null ? univ.getRecommendationYear() : null)
                        .recommendationRate(univ != null ? univ.getRecommendationRate() : null)
                        .build();
                wishGroupSnapshotMapper.insert(groupSnap);
            }

            // M3. 校验重复添加专业
            List<Long> newMajorIds = majorInfos.stream()
                    .map(m -> m.major.getId().longValue())
                    .collect(Collectors.toList());
            List<Long> existingMajorIds = wishMajorSnapshotMapper.selectList(
                    new LambdaQueryWrapper<WishMajorSnapshot>()
                            .eq(WishMajorSnapshot::getPlanId, plan.getId())
                            .in(WishMajorSnapshot::getMajorId, newMajorIds)
                            .select(WishMajorSnapshot::getMajorId))
                    .stream()
                    .map(WishMajorSnapshot::getMajorId)
                    .collect(Collectors.toList());
            if (!existingMajorIds.isEmpty()) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "以下专业已在志愿表中: " + existingMajorIds);
            }

            // 9. 批量创建 WishMajorSnapshot
            Integer finalGroupSnapId = groupSnap.getId();
            List<WishMajorSnapshot> majorSnapshots = majorInfos.stream()
                    .map(info -> WishMajorSnapshot.builder()
                            .planId(plan.getId())
                            .groupSnapshotId(finalGroupSnapId)
                            .majorId(info.major.getId().longValue())
                            .majorSortOrder(0)
                            .isExported(true)
                            .majorCode(info.major.getMajorCode())
                            .majorName(info.major.getMajorName())
                            .duration(info.major.getDuration())
                            .tuition(info.major.getTuition())
                            .description(info.major.getDescription())
                            .admissionCount(info.major.getAdmissionCount())
                            .safetyLevel(info.result.getSafetyLevel())
                            .levelShort(info.result.getLevelShort())
                            .historyScores(info.historyScores)
                            .build())
                    .collect(Collectors.toList());
            for (WishMajorSnapshot ms : majorSnapshots) {
                wishMajorSnapshotMapper.insert(ms);
            }

            // 10. 更新 plan 的 bo~die_limit
            int newBo = planCounts.get("搏") + newCounts.getOrDefault("搏", 0);
            int newChong = planCounts.get("冲") + newCounts.getOrDefault("冲", 0);
            int newWen = planCounts.get("稳") + newCounts.getOrDefault("稳", 0);
            int newBao = planCounts.get("保") + newCounts.getOrDefault("保", 0);
            int newDie = planCounts.get("垫") + newCounts.getOrDefault("垫", 0);

            plan.setBoLimit(newBo);
            plan.setChongLimit(newChong);
            plan.setWenLimit(newWen);
            plan.setBaoLimit(newBao);
            plan.setDieLimit(newDie);
            wishPlanMapper.updateById(plan);

            log.info("会员 {} 添加专业到志愿表 planId={}, 新增专业数={}, 各档 搏={} 冲={} 稳={} 保={} 垫={}",
                    memberId, plan.getId(), majorInfos.size(), newBo, newChong, newWen, newBao, newDie);

            return toPlanListVO(plan);
        });
    }

    @Override
    public List<WishPlanListVO> myPlans() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        List<WishPlan> plans = wishPlanMapper.selectList(
                new LambdaQueryWrapper<WishPlan>()
                        .eq(WishPlan::getMemberId, memberId)
                        .orderByDesc(WishPlan::getCreatedAt));
        return plans.stream().map(this::toPlanListVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Integer planId) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        WishPlan plan = wishPlanMapper.selectById(planId);
        if (plan == null || !memberId.equals(plan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }
        // 先删子表再删父表，避免孤儿引用
        List<WishGroupSnapshot> groups = wishGroupSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishGroupSnapshot>().eq(WishGroupSnapshot::getPlanId, planId));
        if (!CollectionUtils.isEmpty(groups)) {
            List<Integer> groupSnapIds = groups.stream().map(WishGroupSnapshot::getId).collect(Collectors.toList());
            wishMajorSnapshotMapper.delete(
                    new LambdaQueryWrapper<WishMajorSnapshot>().in(WishMajorSnapshot::getGroupSnapshotId, groupSnapIds));
            wishGroupSnapshotMapper.delete(
                    new LambdaQueryWrapper<WishGroupSnapshot>().eq(WishGroupSnapshot::getPlanId, planId));
        }
        wishPlanMapper.deleteById(planId);
        log.info("会员 {} 删除志愿表 planId={}", memberId, planId);
    }

    @Override
    public IPage<WishPlanGroupVO> pageGroups(Integer planId, Integer page, Integer size) {
        // C3. 验证 plan 所有权
        validatePlanOwnership(planId);

        Page<WishGroupSnapshot> p = new Page<>(page, size);
        IPage<WishGroupSnapshot> snapPage = wishGroupSnapshotMapper.selectPage(p,
                new LambdaQueryWrapper<WishGroupSnapshot>()
                        .eq(WishGroupSnapshot::getPlanId, planId)
                        .orderByAsc(WishGroupSnapshot::getGroupSortOrder));

        Page<WishPlanGroupVO> result = new Page<>(snapPage.getCurrent(), snapPage.getSize(), snapPage.getTotal());
        result.setRecords(snapPage.getRecords().stream()
                .map(this::toGroupVO)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public IPage<WishPlanMajorVO> pageMajors(Integer planId, Integer groupSnapshotId, Integer page, Integer size) {
        // C3. 验证 plan 所有权
        validatePlanOwnership(planId);

        Page<WishMajorSnapshot> p = new Page<>(page, size);
        IPage<WishMajorSnapshot> snapPage = wishMajorSnapshotMapper.selectPage(p,
                new LambdaQueryWrapper<WishMajorSnapshot>()
                        .eq(WishMajorSnapshot::getPlanId, planId)
                        .eq(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotId)
                        .orderByAsc(WishMajorSnapshot::getMajorSortOrder));

        Page<WishPlanMajorVO> result = new Page<>(snapPage.getCurrent(), snapPage.getSize(), snapPage.getTotal());
        result.setRecords(snapPage.getRecords().stream()
                .map(this::toMajorVO)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupSortOrder(Integer planId, WishGroupSortDTO dto) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // M5. 批量验证
        List<Integer> groupIds = dto.getItems().stream()
                .map(WishGroupSortDTO.GroupSortItem::getGroupId)
                .collect(Collectors.toList());
        List<WishGroupSnapshot> existing = wishGroupSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishGroupSnapshot>()
                        .eq(WishGroupSnapshot::getPlanId, planId)
                        .in(WishGroupSnapshot::getId, groupIds));
        Set<Integer> foundIds = existing.stream()
                .map(WishGroupSnapshot::getId)
                .collect(Collectors.toSet());
        for (Integer id : groupIds) {
            if (!foundIds.contains(id)) {
                throw new BusinessException(ResultCode.WISH_GROUP_NOT_FOUND);
            }
        }

        // 批量更新专业组排序
        for (WishGroupSortDTO.GroupSortItem item : dto.getItems()) {
            LambdaUpdateWrapper<WishGroupSnapshot> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WishGroupSnapshot::getPlanId, planId)
                    .eq(WishGroupSnapshot::getId, item.getGroupId())
                    .set(WishGroupSnapshot::getGroupSortOrder, item.getSortOrder());
            wishGroupSnapshotMapper.update(null, updateWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMajorSortOrder(Integer planId, Integer groupSnapshotId, WishMajorSortDTO dto) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 3. 验证专业组存在
        WishGroupSnapshot groupSnapshot = wishGroupSnapshotMapper.selectById(groupSnapshotId);
        if (groupSnapshot == null || !groupSnapshot.getPlanId().equals(planId)) {
            throw new BusinessException(ResultCode.WISH_GROUP_NOT_FOUND);
        }

        // M5. 批量验证
        List<Long> majorIds = dto.getItems().stream()
                .map(WishMajorSortDTO.MajorSortItem::getMajorId)
                .collect(Collectors.toList());
        List<WishMajorSnapshot> existing = wishMajorSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishMajorSnapshot>()
                        .eq(WishMajorSnapshot::getPlanId, planId)
                        .eq(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotId)
                        .in(WishMajorSnapshot::getId, majorIds));
        Set<Long> foundIds = existing.stream()
                .map(WishMajorSnapshot::getId)
                .map(Integer::longValue)
                .collect(Collectors.toSet());
        for (Long id : majorIds) {
            if (!foundIds.contains(id)) {
                throw new BusinessException(ResultCode.WISH_MAJOR_NOT_FOUND);
            }
        }

        // 批量更新专业排序
        for (WishMajorSortDTO.MajorSortItem item : dto.getItems()) {
            LambdaUpdateWrapper<WishMajorSnapshot> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WishMajorSnapshot::getPlanId, planId)
                    .eq(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotId)
                    .eq(WishMajorSnapshot::getId, item.getMajorId())
                    .set(WishMajorSnapshot::getMajorSortOrder, item.getSortOrder());
            wishMajorSnapshotMapper.update(null, updateWrapper);
        }
    }

    // ===================== 导出状态 =====================

    private static final long EXPORT_KEY_EXPIRE_DAYS = 7;

    @Override
    public void updateMajorExportStatus(Integer planId, Integer majorId, WishMajorExportDTO dto) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 3. 验证专业存在
        WishMajorSnapshot majorSnapshot = wishMajorSnapshotMapper.selectById(majorId);
        if (majorSnapshot == null || !majorSnapshot.getPlanId().equals(planId)) {
            throw new BusinessException(ResultCode.WISH_MAJOR_NOT_FOUND);
        }

        // 4. 存入Redis（key 含 memberId 做用户隔离）
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + currentMemberId + ":" + planId;
        String field = "major:" + majorId + ":isExported";
        try {
            redisTemplate.opsForHash().put(key, field, dto.getIsExported().toString());
            redisTemplate.expire(key, EXPORT_KEY_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 写入导出状态失败: key={}, field={}", key, field, e);
        }
    }

    @Override
    public void batchUpdateMajorExportStatus(Integer planId, Integer groupSnapshotId, WishGroupExportAllDTO dto) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 3. 验证专业组存在
        WishGroupSnapshot groupSnapshot = wishGroupSnapshotMapper.selectById(groupSnapshotId);
        if (groupSnapshot == null || !groupSnapshot.getPlanId().equals(planId)) {
            throw new BusinessException(ResultCode.WISH_GROUP_NOT_FOUND);
        }

        // 4. 查询该专业组下所有专业
        LambdaQueryWrapper<WishMajorSnapshot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WishMajorSnapshot::getPlanId, planId)
                .eq(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotId);
        List<WishMajorSnapshot> majors = wishMajorSnapshotMapper.selectList(queryWrapper);

        // 5. 批量存入Redis（key 含 memberId 做用户隔离）
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + currentMemberId + ":" + planId;
        Map<String, String> fieldMap = new HashMap<>();
        for (WishMajorSnapshot major : majors) {
            String field = "major:" + major.getId() + ":isExported";
            fieldMap.put(field, dto.getIsExported().toString());
        }
        try {
            redisTemplate.opsForHash().putAll(key, fieldMap);
            redisTemplate.expire(key, EXPORT_KEY_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis 批量写入导出状态失败: key={}", key, e);
        }
    }

    @Override
    public WishPlanExportProgressVO getExportProgress(Integer planId) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // M17. 进度改为基于导出专业数/总专业数
        long totalMajors = wishMajorSnapshotMapper.selectCount(
                new LambdaQueryWrapper<WishMajorSnapshot>()
                        .eq(WishMajorSnapshot::getPlanId, planId));

        Set<Integer> exportMajors = getExportMajors(planId, currentMemberId);
        int exportedCount = exportMajors.size();

        int percentage = totalMajors > 0 ? (int) (exportedCount * 100 / totalMajors) : 0;

        return WishPlanExportProgressVO.builder()
                .totalMajors((int) totalMajors)
                .exportedMajors(exportedCount)
                .percentage(percentage)
                .status(percentage >= 100 ? "completed" : "processing")
                .message(percentage >= 100 ? "导出完成" : "正在准备导出...")
                .build();
    }

    @Override
    public WishPlanExportFileVO generateExportFile(Integer planId) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 3. 查询专业组和专业数据 (M4: 一次性查询再分组)
        LambdaQueryWrapper<WishGroupSnapshot> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(WishGroupSnapshot::getPlanId, planId)
                .orderByAsc(WishGroupSnapshot::getGroupSortOrder);
        List<WishGroupSnapshot> groups = wishGroupSnapshotMapper.selectList(groupQuery);

        List<WishMajorSnapshot> allMajors = wishMajorSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishMajorSnapshot>()
                        .eq(WishMajorSnapshot::getPlanId, planId)
                        .orderByAsc(WishMajorSnapshot::getMajorSortOrder));
        Map<Integer, List<WishMajorSnapshot>> majorsMap = allMajors.stream()
                .collect(Collectors.groupingBy(WishMajorSnapshot::getGroupSnapshotId));

        // 4. 获取导出的专业
        Set<Integer> exportMajors = getExportMajors(planId, currentMemberId);

        // 5. 生成Excel文件到临时目录 (C2: 净化文件名 + 用户隔离)
        String rawName = wishPlan.getPlanName();
        String sanitized = rawName == null ? "wish-plan" : rawName.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\-_]", "_");
        if (sanitized.isBlank()) {
            sanitized = "wish-plan";
        }
        String fileName = sanitized + "_" + currentMemberId + ".xlsx";
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName;

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            wishPlanExcelUtil.exportToExcel(outputStream, wishPlan, groups, majorsMap, exportMajors);
        } catch (IOException e) {
            log.error("生成Excel文件失败", e);
            throw new BusinessException(ResultCode.EXPORT_FAILED);
        }

        // M1. downloadUrl 指向 GET /download 端点（非自身 POST /generate）
        String downloadUrl = "/api/v1/app/algorithm/wish-plan/" + planId + "/export/download?file=" + fileName;

        log.info("会员 {} 生成导出文件 planId={}, fileName={}", currentMemberId, planId, fileName);

        return WishPlanExportFileVO.builder()
                .downloadUrl(downloadUrl)
                .fileName(fileName)
                .build();
    }

    @Override
    public byte[] readExportFile(Integer planId, String fileName) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // C2. 净化文件名，防止路径穿越
        String sanitized = fileName == null ? "" : fileName.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\-_]", "_");
        if (sanitized.isBlank() || !sanitized.endsWith(".xlsx")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法文件名");
        }

        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, sanitized);

        // 确认解析后的规范路径仍在临时目录内
        try {
            String canonicalTemp = new File(tempDir).getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalTemp + File.separator)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "非法文件路径");
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件路径解析失败");
        }

        if (!file.exists()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "导出文件不存在，请先调用生成接口");
        }

        try {
            byte[] content = Files.readAllBytes(file.toPath());
            // M15. 读取后删除临时文件，防止磁盘占满
            if (!file.delete()) {
                log.warn("临时文件删除失败: {}", file.getAbsolutePath());
            }
            return content;
        } catch (IOException e) {
            log.error("读取导出文件失败: {}", file.getAbsolutePath(), e);
            throw new BusinessException(ResultCode.EXPORT_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveExportStatusToDatabase(Integer planId) {
        // 1. 验证志愿方案存在
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || wishPlan.getDeleted()) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // 2. 验证当前用户是志愿方案的所有者
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!currentMemberId.equals(wishPlan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }

        // M11. Redis 异常降级处理（key 含 memberId 做用户隔离）
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + currentMemberId + ":" + planId;
        Map<Object, Object> entries;
        try {
            entries = redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.warn("Redis 读取导出状态失败: key={}", key, e);
            return;
        }

        if (entries.isEmpty()) {
            return;
        }

        // 4. 按导出状态分组，批量更新数据库
        List<Integer> exportTrueIds = new ArrayList<>();
        List<Integer> exportFalseIds = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (field.startsWith("major:") && field.endsWith(":isExported")) {
                Integer majorId = Integer.parseInt(field.replace("major:", "").replace(":isExported", ""));
                if (Boolean.parseBoolean(value)) {
                    exportTrueIds.add(majorId);
                } else {
                    exportFalseIds.add(majorId);
                }
            }
        }
        if (!exportTrueIds.isEmpty()) {
            wishMajorSnapshotMapper.update(null,
                    new LambdaUpdateWrapper<WishMajorSnapshot>()
                            .in(WishMajorSnapshot::getId, exportTrueIds)
                            .eq(WishMajorSnapshot::getPlanId, planId)
                            .set(WishMajorSnapshot::getIsExported, true));
        }
        if (!exportFalseIds.isEmpty()) {
            wishMajorSnapshotMapper.update(null,
                    new LambdaUpdateWrapper<WishMajorSnapshot>()
                            .in(WishMajorSnapshot::getId, exportFalseIds)
                            .eq(WishMajorSnapshot::getPlanId, planId)
                            .set(WishMajorSnapshot::getIsExported, false));
        }

        // 5. 删除Redis缓存
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 删除导出状态失败: key={}", key, e);
        }

        log.info("会员 {} 保存导出状态到数据库 planId={}", currentMemberId, planId);
    }

    @Override
    public WishGroupSnapshot getExportGroupSnapshot(Integer groupSnapshotId) {
        WishGroupSnapshot groupSnapshot = wishGroupSnapshotMapper.selectById(groupSnapshotId);
        if (groupSnapshot == null) {
            throw new BusinessException(ResultCode.WISH_GROUP_NOT_FOUND);
        }
        return groupSnapshot;
    }

    @Override
    public List<WishExportMajorVO> getExportableMajors(Integer groupSnapshotId) {
        List<WishMajorSnapshot> majors = wishMajorSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishMajorSnapshot>()
                        .eq(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotId)
                        .eq(WishMajorSnapshot::getIsExported, true)
                        .orderByAsc(WishMajorSnapshot::getMajorSortOrder));
        return majors.stream()
                .filter(m -> m.getMajorId() != null)
                .map(m -> WishExportMajorVO.builder()
                        .majorId(m.getMajorId())
                        .majorName(m.getMajorName())
                        .safetyLevel(m.getSafetyLevel())
                        .levelShort(m.getLevelShort())
                        .historyScores(m.getHistoryScores())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ExportGroupContextVO> getExportGroupContexts(Integer planId) {
        List<WishGroupSnapshot> groups = wishGroupSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishGroupSnapshot>()
                        .eq(WishGroupSnapshot::getPlanId, planId)
                        .orderByAsc(WishGroupSnapshot::getGroupSortOrder));

        // 第一轮：收集每个组的可导出专业，并汇总所有 majorId 用于批量加载
        List<ExportGroupContextVO> result = new ArrayList<>();
        Map<String, CityEnrichmentVO> cityEnrichmentCache = new HashMap<>();
        List<Long> allMajorIds = new ArrayList<>();

        // 临时保存每组的专业列表，避免二次查询
        List<List<WishExportMajorVO>> perGroupMajors = new ArrayList<>();

        for (WishGroupSnapshot g : groups) {
            List<WishExportMajorVO> exportableMajors = getExportableMajors(g.getId());
            if (CollectionUtils.isEmpty(exportableMajors)) {
                // 该专业组下所有专业 is_exported=false，跳过（不进入 AI 分析）
                perGroupMajors.add(Collections.emptyList());
                continue;
            }
            for (WishExportMajorVO m : exportableMajors) {
                if (m.getMajorId() != null) {
                    allMajorIds.add(m.getMajorId());
                }
            }
            perGroupMajors.add(exportableMajors);
        }

        // 批量加载专业增强数据（避免 N+1 查询）
        Map<Long, MajorEnrichmentVO> majorEnrichmentMap = enrichmentLoader.loadMajorsBatch(allMajorIds);

        // 第二轮：组装结果
        for (int i = 0; i < groups.size(); i++) {
            WishGroupSnapshot g = groups.get(i);
            List<WishExportMajorVO> exportableMajors = perGroupMajors.get(i);
            if (CollectionUtils.isEmpty(exportableMajors)) {
                continue;
            }

            // 填充专业增强数据
            for (WishExportMajorVO m : exportableMajors) {
                if (m.getMajorId() != null) {
                    m.setMajorEnrichment(majorEnrichmentMap.get(m.getMajorId()));
                }
            }

            // 加载城市增强数据（同城缓存）
            CityEnrichmentVO cityEnrichment = g.getCityName() != null
                    ? cityEnrichmentCache.computeIfAbsent(g.getCityName(), enrichmentLoader::loadCity)
                    : null;

            result.add(ExportGroupContextVO.builder()
                    .groupSnapshotId(g.getId())
                    .universityId(g.getUniversityId())
                    .universityName(g.getUniversityName())
                    .cityName(g.getCityName())
                    .groupSortOrder(g.getGroupSortOrder())
                    .groupCode(g.getGroupCode())
                    .groupName(g.getGroupName())
                    .exportableMajors(exportableMajors)
                    .cityEnrichment(cityEnrichment)
                    .build());
        }
        return result;
    }

    private Set<Integer> getExportMajors(Integer planId, Long memberId) {
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + memberId + ":" + planId;
        Map<Object, Object> entries;
        try {
            entries = redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.warn("Redis 读取导出专业列表失败，降级返回空集合: key={}", key, e);
            return Collections.emptySet();
        }

        Set<Integer> exportMajors = new HashSet<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (field.startsWith("major:") && field.endsWith(":isExported") && "true".equals(value)) {
                Integer majorId = Integer.parseInt(field.replace("major:", "").replace(":isExported", ""));
                exportMajors.add(majorId);
            }
        }

        return exportMajors;
    }

    // ===================== 私有方法 =====================

    /**
     * C3. 验证 plan 所有权
     */
    private void validatePlanOwnership(Integer planId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        WishPlan plan = wishPlanMapper.selectById(planId);
        if (plan == null || plan.getDeleted() || !currentMemberId.equals(plan.getMemberId())) {
            throw new BusinessException(ResultCode.WISH_PLAN_NOT_FOUND);
        }
    }

    private WishPlan getOrCreatePlan(Long memberId, MemberGaokao gaokao) {
        WishPlan existing = wishPlanMapper.selectOne(
                new LambdaQueryWrapper<WishPlan>()
                        .eq(WishPlan::getMemberId, memberId)
                        .orderByDesc(WishPlan::getCreatedAt)
                        .last("LIMIT 1 FOR UPDATE"));
        if (existing != null) {
            return existing;
        }
        // C4. plan 数量限制检查仅在需要新建时执行
        long count = wishPlanMapper.selectCount(
                new LambdaQueryWrapper<WishPlan>().eq(WishPlan::getMemberId, memberId));
        String memberType = SecurityUtil.getCurrentMemberType();
        int maxPlans = getMaxPlans(memberType);
        if (count >= maxPlans) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "当前会员类型最多允许 " + maxPlans + " 个志愿表");
        }

        String planName = "我的志愿方案" + (count + 1);

        WishPlan plan = WishPlan.builder()
                .memberId(memberId)
                .planName(planName)
                .planYear(gaokao.getGaokaoYear())
                .planProvince(gaokao.getGaokaoProvince())
                .reformModel(gaokao.getReformModel())
                .planBatch(gaokao.getBatch())
                .userScore(gaokao.getScore())
                .userRank(gaokao.getRank())
                .boLimit(0).chongLimit(0).wenLimit(0).baoLimit(0).dieLimit(0)
                .build();
        wishPlanMapper.insert(plan);
        log.info("创建新志愿表 memberId={}, planId={}, name={}", memberId, plan.getId(), planName);
        return plan;
    }

    private int getMaxPlans(String memberType) {
        if ("vip".equals(memberType)) return 10;
        if ("pro".equals(memberType)) return 5;
        return 1;
    }

    private WishPlanListVO toPlanListVO(WishPlan plan) {
        return WishPlanListVO.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .planYear(plan.getPlanYear())
                .planProvince(plan.getPlanProvince())
                .reformModel(plan.getReformModel())
                .planBatch(plan.getPlanBatch())
                .userScore(plan.getUserScore())
                .userRank(plan.getUserRank())
                .boLimit(plan.getBoLimit())
                .chongLimit(plan.getChongLimit())
                .wenLimit(plan.getWenLimit())
                .baoLimit(plan.getBaoLimit())
                .dieLimit(plan.getDieLimit())
                .createdAt(plan.getCreatedAt())
                .build();
    }

    private WishPlanGroupVO toGroupVO(WishGroupSnapshot snap) {
        return WishPlanGroupVO.builder()
                .id(snap.getId())
                .groupId(snap.getGroupId())
                .planId(snap.getPlanId())
                .groupSortOrder(snap.getGroupSortOrder())
                .universityId(snap.getUniversityId())
                .universityName(snap.getUniversityName())
                .cityName(snap.getCityName())
                .category(snap.getCategory())
                .nature(snap.getNature())
                .groupCode(snap.getGroupCode())
                .groupName(snap.getGroupName())
                .enrollmentCode(snap.getEnrollmentCode())
                .year(snap.getYear())
                .province(snap.getProvince())
                .batch(snap.getBatch())
                .subjects(snap.getSubjects())
                .constraintsDescription(snap.getConstraintsDescription())
                .description(snap.getDescription())
                .majorCount(snap.getMajorCount())
                .tags(snap.getTags())
                .recommendationYear(snap.getRecommendationYear())
                .recommendationRate(snap.getRecommendationRate())
                .build();
    }

    private WishPlanMajorVO toMajorVO(WishMajorSnapshot snap) {
        List<YearScoreVO> historyScores = snap.getHistoryScores() == null ? Collections.emptyList()
                : snap.getHistoryScores().stream()
                        .map(h -> YearScoreVO.builder()
                                .year(h.getYear() != null ? h.getYear().shortValue() : null)
                                .minScore(h.getMinScore())
                                .minRank(h.getMinRank())
                                .avgScore(h.getAvgScore())
                                .avgRank(h.getAvgRank())
                                .maxScore(h.getMaxScore())
                                .maxRank(h.getMaxRank())
                                .admissionCount(h.getAdmissionCount())
                                .build())
                        .collect(Collectors.toList());
        return WishPlanMajorVO.builder()
                .id(snap.getId())
                .groupSnapshotId(snap.getGroupSnapshotId())
                .majorId(snap.getMajorId())
                .majorSortOrder(snap.getMajorSortOrder())
                .majorCode(snap.getMajorCode())
                .majorName(snap.getMajorName())
                .duration(snap.getDuration())
                .tuition(snap.getTuition())
                .description(snap.getDescription())
                .admissionCount(snap.getAdmissionCount())
                .safetyLevel(snap.getSafetyLevel())
                .levelShort(snap.getLevelShort())
                .historyScores(historyScores)
                .build();
    }

    private BigDecimal queryDensity(MemberGaokao gaokao) {
        if (gaokao == null || gaokao.getScore() == null || gaokao.getGaokaoProvince() == null
                || gaokao.getSubjectType() == null || gaokao.getGaokaoYear() == null) {
            return null;
        }
        return scoreRankMapper.selectDensity(
                gaokao.getGaokaoProvince(), gaokao.getGaokaoYear(),
                gaokao.getSubjectType(), gaokao.getScore());
    }

    private ProvinceConfig queryProvinceConfig(MemberGaokao gaokao) {
        if (gaokao == null || gaokao.getGaokaoProvince() == null) return null;
        return provinceConfigMapper.selectByProvince(gaokao.getGaokaoProvince());
    }

    private Short queryReformYear(MemberGaokao gaokao) {
        if (gaokao == null || gaokao.getGaokaoProvince() == null) return null;
        return provinceReformService.getEarliestReformYear(gaokao.getGaokaoProvince());
    }

    // ===================== 缓存工具 =====================

    private WishPlanLimitVO safeGetFromCache(String key) {
        try {
            return (WishPlanLimitVO) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级走 DB: key={}", key, e);
            return null;
        }
    }

    private void safeSetCache(String key, WishPlanLimitVO vo) {
        try {
            redisTemplate.opsForValue().set(key, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 写入失败: key={}", key, e);
        }
    }

    @lombok.AllArgsConstructor
    private static class MajorSafetyInfo {
        final AdmissionMajorScore major;
        final SafetyCalcResult result;
        final List<WishMajorSnapshot.HistoryScore> historyScores;
    }
}
