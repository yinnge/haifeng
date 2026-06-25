# 志愿方案模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** 实现用户勾选专业明细添加到志愿表、查看志愿表详情（组快照/专业快照）功能

**Architecture:** 在现有 `wish` 子包下扩展，新增 5 个 API 端点，核心逻辑在 `WishPlanServiceImpl` 中实现，复用 AdmissionQueryServiceImpl 的 SafetyLevelService 计算安全系数。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL, Redis

---

### Pre-check: 确认已完成实体补充

`WishGroupSnapshot.java` 已补充 `universityId`, `category`, `majorCount`, `nature`, `tags` 字段。确认一下：

- [ ] **Step 1: 确认实体完整性**

Run: 无，目测 `haifeng-common/.../entity/algorithm/wish/WishGroupSnapshot.java` 包含：
- `universityId` (Long)
- `category` (String)
- `majorCount` (Integer)
- `nature` (String)
- `tags` (List\<String\>, JacksonTypeHandler)

---

### Task 1: DTO/VO 基础类创建

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/wish/WishPlanAddMajorsDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanGroupVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanMajorVO.java`

- [ ] **Step 1: 创建 WishPlanAddMajorsDTO**

```java
package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class WishPlanAddMajorsDTO {

    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    @NotEmpty(message = "请选择至少一个专业")
    private List<Integer> majorIds;
}
```

- [ ] **Step 2: 创建 WishPlanListVO**

```java
package com.haifeng.app.vo.algorithm.wish;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class WishPlanListVO {
    private Integer id;
    private String planName;
    private Short planYear;
    private String planProvince;
    private String reformModel;
    private String planBatch;
    private Integer userScore;
    private Integer userRank;
    private Integer boLimit;
    private Integer chongLimit;
    private Integer wenLimit;
    private Integer baoLimit;
    private Integer dieLimit;
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 3: 创建 WishPlanGroupVO**

```java
package com.haifeng.app.vo.algorithm.wish;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WishPlanGroupVO {
    private Integer id;
    private Integer groupId;
    private Integer planId;
    private Integer groupSortOrder;
    private Long universityId;
    private String universityName;
    private String cityName;
    private String category;
    private String nature;
    private String groupCode;
    private String groupName;
    private String enrollmentCode;
    private Short year;
    private String province;
    private String batch;
    private List<String> subjects;
    private List<String> constraints;
    private List<String> constraintsDescription;
    private String description;
    private Integer majorCount;
    private List<String> tags;
    private Integer recommendationYear;
    private BigDecimal recommendationRate;
}
```

- [ ] **Step 4: 创建 WishPlanMajorVO**

```java
package com.haifeng.app.vo.algorithm.wish;

import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WishPlanMajorVO {
    private Integer id;
    private Integer groupSnapshotId;
    private Long majorId;
    private Integer majorSortOrder;
    private String majorCode;
    private String majorName;
    private String duration;
    private BigDecimal tuition;
    private Integer admissionCount;
    private BigDecimal safetyLevel;
    private String levelShort;
    private List<YearScoreVO> historyScores;
}
```

---

### Task 2: Service 接口扩展

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/wish/WishPlanService.java`

- [ ] **Step 1: 追加方法定义**

```java
package com.haifeng.app.service.algorithm.wish;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;

import java.util.List;

public interface WishPlanService {

    WishPlanLimitVO getDefaultLimits();

    /** 添加专业明细到志愿表 */
    WishPlanListVO addMajors(WishPlanAddMajorsDTO dto);

    /** 查看当前用户的志愿表列表 */
    List<WishPlanListVO> myPlans();

    /** 删除志愿表 */
    void deletePlan(Integer planId);

    /** 分页查志愿组快照 */
    IPage<WishPlanGroupVO> pageGroups(Integer planId, Integer page, Integer size);

    /** 分页查专业快照 */
    IPage<WishPlanMajorVO> pageMajors(Integer planId, Integer groupSnapshotId, Integer page, Integer size);
}
```

---

### Task 3: Controller 端点追加

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/WishPlanController.java`

- [ ] **Step 1: 追加断言导入和端点**

```java
package com.haifeng.app.controller.algorithm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/wish-plan")
@RequiredArgsConstructor
@RequireLogin
public class WishPlanController {

    private final WishPlanService wishPlanService;

    @GetMapping("/default-limits")
    public R<WishPlanLimitVO> getDefaultLimits() {
        return R.ok(wishPlanService.getDefaultLimits());
    }

    @PostMapping("/add-majors")
    public R<WishPlanListVO> addMajors(@Valid @RequestBody WishPlanAddMajorsDTO dto) {
        return R.ok(wishPlanService.addMajors(dto));
    }

    @GetMapping("/my-plans")
    public R<List<WishPlanListVO>> myPlans() {
        return R.ok(wishPlanService.myPlans());
    }

    @DeleteMapping("/{planId}")
    public R<Void> deletePlan(@PathVariable Integer planId) {
        wishPlanService.deletePlan(planId);
        return R.ok();
    }

    @GetMapping("/{planId}/groups")
    public R<IPage<WishPlanGroupVO>> pageGroups(
            @PathVariable Integer planId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(wishPlanService.pageGroups(planId, page, size));
    }

    @GetMapping("/{planId}/groups/{groupSnapshotId}/majors")
    public R<IPage<WishPlanMajorVO>> pageMajors(
            @PathVariable Integer planId,
            @PathVariable Integer groupSnapshotId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(wishPlanService.pageMajors(planId, groupSnapshotId, page, size));
    }
}
```

---

### Task 4: Service 实现 - 核心业务逻辑

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/wish/WishPlanServiceImpl.java`

This is the bulk of the work. The impl needs:

1. `addMajors()` — the most complex method
2. `myPlans()` — simple query
3. `deletePlan()` — soft delete plan + hard delete snapshots
4. `pageGroups()` — paginate WishGroupSnapshot by planId
5. `pageMajors()` — paginate WishMajorSnapshot by groupSnapshotId

Plus the existing `getDefaultLimits()` method.

- [ ] **Step 1: Implement addMajors() — 获取请求中的专业数据**

Flow:
```
1. memberId = SecurityUtil.getCurrentMemberId()
2. Validate gaokao exists → throw if null
3. Query AdmissionMajorScore records by majorIds
4. Check none has levelShort == "禁" (compute safety for each)
5. Get/validate member type plan limit
6. Get or create WishPlan
7. Compute safety for each major
8. Check level limits
9. Create/update WishGroupSnapshot
10. Batch insert WishMajorSnapshot
11. Update plan counts
12. Return WishPlanListVO
```

Dependencies needed from AdmissionQueryServiceImpl's pattern:
- `MemberGaokaoMapper` — already used
- `AdmissionMajorScoreMapper` — query by IDs
- `AdmissionGroupMapper` — get group by ID for the selected group
- `UniversityMapper` — get university details for group snapshot
- `SafetyLevelService` — compute major safety level
- `SafetyLevelDictCache` — map safety level → levelShort
- `SafetyCalcContext` — build context for safety calc
- `ConstraintMatcherService` — match user constraints
- `ConstraintDictMapper` — get severity map
- `ScoreRankMapper` — query density
- `ProvinceConfigMapper` — get province config
- `ProvinceReformMapper` — get reform year
- `SystemSettingsMapper` — get default limits (already used)
- `WishPlanMapper`, `WishGroupSnapshotMapper`, `WishMajorSnapshotMapper`
- `RedisTemplate` (already used)

Full implementation:

```java
package com.haifeng.app.service.impl.algorithm.wish;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
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
import com.haifeng.common.mapper.algorithm.*;
import com.haifeng.common.mapper.algorithm.wish.WishGroupSnapshotMapper;
import com.haifeng.common.mapper.algorithm.wish.WishMajorSnapshotMapper;
import com.haifeng.common.mapper.algorithm.wish.WishPlanMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.service.algorithm.safety.SafetyLevelDictCache;
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
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishPlanServiceImpl implements WishPlanService {

    private static final long CACHE_TTL_HOURS = 24;
    private static final int PAGE_SIZE = 10;

    // === 已有依赖 ===
    private final SystemSettingsMapper settingsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // === 新注入依赖 ===
    private final WishPlanMapper wishPlanMapper;
    private final WishGroupSnapshotMapper wishGroupSnapshotMapper;
    private final WishMajorSnapshotMapper wishMajorSnapshotMapper;
    private final MemberGaokaoMapper memberGaokaoMapper;
    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final UniversityMapper universityMapper;
    private final SafetyLevelService safetyLevelService;
    private final SafetyLevelDictCache safetyLevelDictCache;
    private final ConstraintMatcherService constraintMatcherService;
    private final ConstraintDictMapper constraintDictMapper;
    private final ScoreRankMapper scoreRankMapper;
    private final ProvinceConfigMapper provinceConfigMapper;
    private final ProvinceReformMapper provinceReformMapper;

    @Override
    public WishPlanLimitVO getDefaultLimits() {
        String cacheKey = RedisKeyConstant.WISH_PLAN_DEFAULT_LIMITS_KEY;
        WishPlanLimitVO cached = safeGetFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));
        WishPlanLimitVO vo;
        if (settings == null) {
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

        // 4. 计算各专业的安全系数，同时检查"禁"级别
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
            SafetyCalcResult result = safetyLevelService.calculateMajorSafety(gaokao, major, group, constraintCodes, ctx);

            String levelShort = result.getLevelShort();
            if ("禁".equals(levelShort)) {
                throw new BusinessException(400, "专业「" + major.getMajorName() + "」为'禁'级别，不允许添加到志愿表");
            }

            List<YearScoreVO> historyScores = history.stream()
                    .limit(5)
                    .map(h -> YearScoreVO.builder()
                            .year(h.getYear()).minScore(h.getMinScore()).minRank(h.getMinRank())
                            .avgScore(h.getAvgScore()).avgRank(h.getAvgRank())
                            .maxScore(h.getMaxScore()).maxRank(h.getMaxRank())
                            .admissionCount(h.getAdmissionCount())
                            .build())
                    .collect(Collectors.toList());

            majorInfos.add(new MajorSafetyInfo(major, result, historyScores));
        }

        // 5. 校验用户 plan 数量限制
        long planCount = wishPlanMapper.selectCount(
                new LambdaQueryWrapper<WishPlan>().eq(WishPlan::getMemberId, memberId));
        String memberType = SecurityUtil.getCurrentMemberType();
        int maxPlans = getMaxPlans(memberType);
        if (planCount >= maxPlans) {
            throw new BusinessException(400, "当前会员类型最多允许 " + maxPlans + " 个志愿表");
        }

        // 6. 获取或创建 WishPlan
        WishPlan plan = getOrCreatePlan(memberId, gaokao);

        // 7. 校验档位数量限制
        WishPlanLimitVO limits = getDefaultLimits();
        Map<String, Integer> levelCounts = new HashMap<>();
        levelCounts.put("搏", plan.getBoLimit());
        levelCounts.put("冲", plan.getChongLimit());
        levelCounts.put("稳", plan.getWenLimit());
        levelCounts.put("保", plan.getBaoLimit());
        levelCounts.put("垫", plan.getDieLimit());
        // "禁" already checked above

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
            int current = levelCounts.getOrDefault(level, 0);
            int maxAllowed = maxLimits.getOrDefault(level, Integer.MAX_VALUE);
            if (current + newCnt > maxAllowed) {
                throw new BusinessException(400,
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
                    .constraints(group.getConstraints())
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
                        .tuition(parseTuition(info.major.getTuition()))
                        .admissionCount(info.major.getAdmissionCount())
                        .safetyLevel(info.result.getSafetyLevel())
                        .levelShort(info.result.getLevelShort())
                        .historyScores(info.historyScores)
                        .build())
                .collect(Collectors.toList());
        for (WishMajorSnapshot ms : majorSnapshots) {
            wishMajorSnapshotMapper.insert(ms);
        }

        // 10. 更新 plan 的 bo~die_limit (当前已选数量)
        int newBo = levelCounts.getOrDefault("搏", 0) + newCounts.getOrDefault("搏", 0);
        int newChong = levelCounts.getOrDefault("冲", 0) + newCounts.getOrDefault("冲", 0);
        int newWen = levelCounts.getOrDefault("稳", 0) + newCounts.getOrDefault("稳", 0);
        int newBao = levelCounts.getOrDefault("保", 0) + newCounts.getOrDefault("保", 0);
        int newDie = levelCounts.getOrDefault("垫", 0) + newCounts.getOrDefault("垫", 0);

        plan.setBoLimit(newBo);
        plan.setChongLimit(newChong);
        plan.setWenLimit(newWen);
        plan.setBaoLimit(newBao);
        plan.setDieLimit(newDie);
        wishPlanMapper.updateById(plan);

        log.info("会员 {} 添加专业到志愿表 planId={}, 新增专业数={}, 当前各档 搏={} 冲={} 稳={} 保={} 垫={}",
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
        // 软删除 plan
        wishPlanMapper.deleteById(planId);
        // 硬删除关联快照
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
                        .orderByAsc(WishGroupSnapshot::getId));

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
                        .orderByAsc(WishMajorSnapshot::getId));

        Page<WishPlanMajorVO> result = new Page<>(snapPage.getCurrent(), snapPage.getSize(), snapPage.getTotal());
        result.setRecords(snapPage.getRecords().stream()
                .map(this::toMajorVO)
                .collect(Collectors.toList()));
        return result;
    }

    // ===================== 私有方法 =====================

    private WishPlan getOrCreatePlan(Long memberId, MemberGaokao gaokao) {
        // 查找已有的 plan（取最新一个）
        WishPlan existing = wishPlanMapper.selectOne(
                new LambdaQueryWrapper<WishPlan>()
                        .eq(WishPlan::getMemberId, memberId)
                        .orderByDesc(WishPlan::getCreatedAt)
                        .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        // 创建新 plan
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
                .boLimit(0).chongLimit(0).wenLimit(0).baoLimit(0).dieLimit(0).jinLimit(0)
                .build();
        wishPlanMapper.insert(plan);
        log.info("创建新志愿表 memberId={}, planId={}, name={}", memberId, plan.getId(), planName);
        return plan;
    }

    private int getMaxPlans(String memberType) {
        if ("vip".equals(memberType)) return 10;
        if ("pro".equals(memberType)) return 5;
        return 1; // normal
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
                .constraints(snap.getConstraints())
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
                                .year(h.getYear()).minScore(h.getMinScore()).minRank(h.getMinRank())
                                .avgScore(h.getAvgScore()).avgRank(h.getAvgRank())
                                .maxScore(h.getMaxScore()).maxRank(h.getMaxRank())
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
        return provinceReformMapper.selectReformYearByProvince(gaokao.getGaokaoProvince());
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

    // ===================== 内部类 =====================

    @lombok.AllArgsConstructor
    private static class MajorSafetyInfo {
        final AdmissionMajorScore major;
        final SafetyCalcResult result;
        final List<YearScoreVO> historyScores;
    }
}
```

Note: The `WishMajorSnapshot.historyScores` is `List<HistoryScore>` (inner class) but `WishPlanMajorVO` uses `List<YearScoreVO>`. The mapping handles this conversion in `toMajorVO`.

- [ ] **Step 2: 编译验证**

Run: `cd D:\exeProject\ideaProjects\Project-HaiFeng && .\mvnw.cmd compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 5: 集成验证

- [ ] **Step 1: 检查关键依赖是否可注入**

确认 `SafetyLevelService`, `SafetyLevelDictCache`, `ConstraintMatcherService`, `ConstraintDictMapper`, `ScoreRankMapper`, `ProvinceConfigMapper`, `ProvinceReformMapper` 都已经在 Spring 容器中（被 `AdmissionQueryServiceImpl` 使用）。

可通过检查 `AdmissionQueryServiceImpl` 的导入确认它们已被其他 Service 注入使用。

- [ ] **Step 2: 完整编译**

Run: `cd D:\exeProject\ideaProjects\Project-HaiFeng && .\mvnw.cmd compile -pl haifeng-app -am`

Expected: BUILD SUCCESS
