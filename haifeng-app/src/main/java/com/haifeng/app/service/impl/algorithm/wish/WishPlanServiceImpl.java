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
import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
import com.haifeng.app.vo.algorithm.pdf.ExportGroupContextVO;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportFileVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportProgressVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.app.util.algorithm.wish.WishPlanExcelUtil;
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
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    @Transactional(rollbackFor = Exception.class)
    public WishPlanListVO addMajors(WishPlanAddMajorsDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (memberId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 1. 获取高考档案
        MemberGaokao gaokao = memberGaokaoMapper.selectByMemberId(memberId);
        if (gaokao == null) {
            throw new BusinessException(400, "请先填写高考档案");
        }

        // 2. 查询专业明细
        List<AdmissionMajorScore> majorScores = admissionMajorScoreMapper.selectBatchIds(dto.getMajorIds());
        if (majorScores.size() != dto.getMajorIds().size()) {
            throw new BusinessException(400, "部分专业明细不存在");
        }

        // 3. 查询专业组
        AdmissionGroup group = admissionGroupMapper.selectById(dto.getGroupId());
        if (group == null || Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new BusinessException(400, "专业组不存在");
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
                throw new BusinessException(400, "专业「" + major.getMajorName() + "」为'禁'级别，不允许添加到志愿表");
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

        // 6. 校验用户 plan 数量限制
        long planCount = wishPlanMapper.selectCount(
                new LambdaQueryWrapper<WishPlan>().eq(WishPlan::getMemberId, memberId));
        String memberType = SecurityUtil.getCurrentMemberType();
        int maxPlans = getMaxPlans(memberType);
        if (planCount >= maxPlans) {
            throw new BusinessException(400, "当前会员类型最多允许 " + maxPlans + " 个志愿表");
        }

        // 7. 获取或创建 WishPlan
        WishPlan plan = getOrCreatePlan(memberId, gaokao);

        // 8. 校验档位数量限制
        WishPlanLimitVO limits = getDefaultLimits();
        Map<String, Integer> planCounts = new HashMap<>();
        planCounts.put("搏", plan.getBoLimit());
        planCounts.put("冲", plan.getChongLimit());
        planCounts.put("稳", plan.getWenLimit());
        planCounts.put("保", plan.getBaoLimit());
        planCounts.put("垫", plan.getDieLimit());

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
                throw new BusinessException(400,
                        level + "档专业已选" + current + "个，最多" + maxAllowed + "个，本次添加" + newCnt + "个超出限制");
            }
        }

        // 9. 创建/获取 WishGroupSnapshot (去重: 一个 plan 一个 groupId 只存一条)
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
                    .constraintsDescription(group.getConstraintsDescription())
                    .category(univ != null ? univ.getCategory() : null)
                    .majorCount(univ != null ? univ.getMajorCount() : 0)
                    .nature(univ != null ? univ.getNature() : null)
                    .tags(univ != null ? univ.getTags() : null)
                    .recommendationYear(univ != null ? univ.getRecommendationYear() : null)
                    .recommendationRate(univ != null ? univ.getRecommendationRate() : null)
                    .build();
            wishGroupSnapshotMapper.insert(groupSnap);
        }

        // 10. 批量创建 WishMajorSnapshot
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
                        .tuition(parseTuition(info.major.getTuition()))
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

        // 11. 更新 plan 的 bo~die_limit
        int newBo = planCounts.getOrDefault("搏", 0) + newCounts.getOrDefault("搏", 0);
        int newChong = planCounts.getOrDefault("冲", 0) + newCounts.getOrDefault("冲", 0);
        int newWen = planCounts.getOrDefault("稳", 0) + newCounts.getOrDefault("稳", 0);
        int newBao = planCounts.getOrDefault("保", 0) + newCounts.getOrDefault("保", 0);
        int newDie = planCounts.getOrDefault("垫", 0) + newCounts.getOrDefault("垫", 0);

        plan.setBoLimit(newBo);
        plan.setChongLimit(newChong);
        plan.setWenLimit(newWen);
        plan.setBaoLimit(newBao);
        plan.setDieLimit(newDie);
        wishPlanMapper.updateById(plan);

        log.info("会员 {} 添加专业到志愿表 planId={}, 新增专业数={}, 各档 搏={} 冲={} 稳={} 保={} 垫={}",
                memberId, plan.getId(), majorInfos.size(), newBo, newChong, newWen, newBao, newDie);

        return toPlanListVO(plan);
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
            throw new BusinessException(400, "志愿表不存在或无权限");
        }
        wishPlanMapper.deleteById(planId);
        List<WishGroupSnapshot> groups = wishGroupSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishGroupSnapshot>().eq(WishGroupSnapshot::getPlanId, planId));
        if (!CollectionUtils.isEmpty(groups)) {
            List<Integer> groupSnapIds = groups.stream().map(WishGroupSnapshot::getId).collect(Collectors.toList());
            wishMajorSnapshotMapper.delete(
                    new LambdaQueryWrapper<WishMajorSnapshot>().in(WishMajorSnapshot::getGroupSnapshotId, groupSnapIds));
            wishGroupSnapshotMapper.delete(
                    new LambdaQueryWrapper<WishGroupSnapshot>().eq(WishGroupSnapshot::getPlanId, planId));
        }
        log.info("会员 {} 删除志愿表 planId={}", memberId, planId);
    }

    @Override
    public IPage<WishPlanGroupVO> pageGroups(Integer planId, Integer page, Integer size) {
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

        // 3. 批量更新专业组排序
        for (WishGroupSortDTO.GroupSortItem item : dto.getItems()) {
            // 验证专业组属于该志愿方案
            WishGroupSnapshot groupSnapshot = wishGroupSnapshotMapper.selectById(item.getGroupId());
            if (groupSnapshot == null || !groupSnapshot.getPlanId().equals(planId)) {
                throw new BusinessException(ResultCode.WISH_GROUP_NOT_FOUND);
            }

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

        // 4. 批量更新专业排序
        for (WishMajorSortDTO.MajorSortItem item : dto.getItems()) {
            // 验证专业属于该志愿方案和专业组
            WishMajorSnapshot majorSnapshot = wishMajorSnapshotMapper.selectById(item.getMajorId());
            if (majorSnapshot == null || !majorSnapshot.getPlanId().equals(planId) ||
                !majorSnapshot.getGroupSnapshotId().equals(groupSnapshotId)) {
                throw new BusinessException(ResultCode.WISH_MAJOR_NOT_FOUND);
            }

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

        // 4. 存入Redis
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + planId;
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

        // 5. 批量存入Redis
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + planId;
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

        // 3. 查询专业组数量
        LambdaQueryWrapper<WishGroupSnapshot> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(WishGroupSnapshot::getPlanId, planId);
        long totalGroups = wishGroupSnapshotMapper.selectCount(groupQuery);

        // 4. 查询已导出的专业
        Set<Integer> exportMajors = getExportMajors(planId);

        // 5. 计算已完成的专业组数量（包含至少一个导出专业的专业组）
        int completedGroups = 0;
        if (!exportMajors.isEmpty()) {
            LambdaQueryWrapper<WishMajorSnapshot> majorQuery = new LambdaQueryWrapper<>();
            majorQuery.eq(WishMajorSnapshot::getPlanId, planId)
                    .in(WishMajorSnapshot::getId, exportMajors)
                    .select(WishMajorSnapshot::getGroupSnapshotId)
                    .groupBy(WishMajorSnapshot::getGroupSnapshotId);
            List<WishMajorSnapshot> completedMajors = wishMajorSnapshotMapper.selectList(majorQuery);
            completedGroups = completedMajors.size();
        }

        // 6. 计算进度
        int percentage = totalGroups > 0 ? (int) (completedGroups * 100 / totalGroups) : 0;

        return WishPlanExportProgressVO.builder()
                .totalGroups((int) totalGroups)
                .completedGroups(completedGroups)
                .percentage(percentage)
                .status("processing")
                .message("正在准备导出...")
                .build();
    }

    @Override
    public WishPlanExportFileVO downloadExportFile(Integer planId) {
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

        // 3. 查询专业组和专业数据
        LambdaQueryWrapper<WishGroupSnapshot> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(WishGroupSnapshot::getPlanId, planId)
                .orderByAsc(WishGroupSnapshot::getGroupSortOrder);
        List<WishGroupSnapshot> groups = wishGroupSnapshotMapper.selectList(groupQuery);

        Map<Integer, List<WishMajorSnapshot>> majorsMap = new HashMap<>();
        for (WishGroupSnapshot group : groups) {
            LambdaQueryWrapper<WishMajorSnapshot> majorQuery = new LambdaQueryWrapper<>();
            majorQuery.eq(WishMajorSnapshot::getPlanId, planId)
                    .eq(WishMajorSnapshot::getGroupSnapshotId, group.getId())
                    .orderByAsc(WishMajorSnapshot::getMajorSortOrder);
            List<WishMajorSnapshot> majors = wishMajorSnapshotMapper.selectList(majorQuery);
            majorsMap.put(group.getId(), majors);
        }

        // 4. 获取导出的专业
        Set<Integer> exportMajors = getExportMajors(planId);

        // 5. 生成Excel文件到临时目录
        String fileName = wishPlan.getPlanName() + ".xlsx";
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = tempDir + File.separator + fileName;

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            wishPlanExcelUtil.exportToExcel(outputStream, wishPlan, groups, majorsMap, exportMajors);
        } catch (IOException e) {
            log.error("生成Excel文件失败", e);
            throw new BusinessException(ResultCode.EXPORT_FAILED);
        }

        // 6. 返回文件信息（实际项目中应该返回临时文件的访问URL）
        String downloadUrl = "/api/v1/app/algorithm/wish-plan/" + planId + "/export/download?file=" + fileName;

        return WishPlanExportFileVO.builder()
                .downloadUrl(downloadUrl)
                .fileName(fileName)
                .build();
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

        // 3. 从Redis获取所有is_exported状态
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + planId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return;
        }

        // 4. 批量更新数据库
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String field = entry.getKey().toString();
            String value = entry.getValue().toString();

            if (field.startsWith("major:") && field.endsWith(":isExported")) {
                Integer majorId = Integer.parseInt(field.replace("major:", "").replace(":isExported", ""));
                Boolean isExported = Boolean.parseBoolean(value);

                LambdaUpdateWrapper<WishMajorSnapshot> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(WishMajorSnapshot::getId, majorId)
                        .eq(WishMajorSnapshot::getPlanId, planId)
                        .set(WishMajorSnapshot::getIsExported, isExported);
                wishMajorSnapshotMapper.update(null, updateWrapper);
            }
        }

        // 5. 删除Redis缓存
        redisTemplate.delete(key);

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
    public List<WishExportMajorVO> getExportableMajorIds(Integer groupSnapshotId) {
        List<WishMajorSnapshot> majors = wishMajorSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishMajorSnapshot>()
                        .eq(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotId)
                        .eq(WishMajorSnapshot::getIsExported, true)
                        .orderByAsc(WishMajorSnapshot::getMajorSortOrder));
        return majors.stream()
                .filter(m -> m.getMajorId() != null)
                .map(m -> WishExportMajorVO.builder()
                        .majorId(m.getMajorId())
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

        List<ExportGroupContextVO> result = new ArrayList<>();
        for (WishGroupSnapshot g : groups) {
            List<WishExportMajorVO> exportableMajors = getExportableMajorIds(g.getId());
            if (CollectionUtils.isEmpty(exportableMajors)) {
                // 该专业组下所有专业 is_exported=false，跳过（不进入 AI 分析）
                continue;
            }
            result.add(ExportGroupContextVO.builder()
                    .groupSnapshotId(g.getId())
                    .universityId(g.getUniversityId())
                    .cityName(g.getCityName())
                    .groupSortOrder(g.getGroupSortOrder())
                    .groupCode(g.getGroupCode())
                    .groupName(g.getGroupName())
                    .exportableMajors(exportableMajors)
                    .build());
        }
        return result;
    }

    private Set<Integer> getExportMajors(Integer planId) {
        String key = RedisKeyConstant.WISH_EXPORT_PREFIX + planId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

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

    private WishPlan getOrCreatePlan(Long memberId, MemberGaokao gaokao) {
        WishPlan existing = wishPlanMapper.selectOne(
                new LambdaQueryWrapper<WishPlan>()
                        .eq(WishPlan::getMemberId, memberId)
                        .orderByDesc(WishPlan::getCreatedAt)
                        .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        long count = wishPlanMapper.selectCount(
                new LambdaQueryWrapper<WishPlan>().eq(WishPlan::getMemberId, memberId));
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

    private BigDecimal parseTuition(String tuition) {
        if (tuition == null || tuition.isBlank()) return null;
        try {
            return new BigDecimal(tuition.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
