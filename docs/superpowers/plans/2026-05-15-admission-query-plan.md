# App 端专业组查询接口实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 app 端专业组/专业明细分页查询及三个详情接口

**Architecture:** 2条SQL策略避免N+1问题；common层提供选科匹配服务；app层处理权限模糊化

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL text[] 数组运算符

---

## 文件结构

### haifeng-common（新增/修改）

| 文件 | 操作 | 说明 |
|------|------|------|
| `service/algorithm/matcher/SubjectMatchResult.java` | 新建 | 选科匹配结果 |
| `service/algorithm/matcher/SubjectMatcher.java` | 新建 | 选科匹配服务 |
| `mapper/algorithm/AdmissionGroupMapper.java` | 修改 | 新增批量查询方法 |
| `mapper/algorithm/AdmissionMajorScoreMapper.java` | 修改 | 新增历史数据查询 |

### haifeng-app（新增）

| 文件 | 操作 | 说明 |
|------|------|------|
| `vo/algorithm/admission/YearScoreVO.java` | 新建 | 年份分数VO |
| `vo/algorithm/admission/AdmissionGroupPageVO.java` | 新建 | 专业组分页VO |
| `vo/algorithm/admission/AdmissionMajorPageVO.java` | 新建 | 专业明细分页VO |
| `vo/university/UniversityBriefVO.java` | 新建 | 院校简要VO |
| `vo/major/MajorBriefVO.java` | 新建 | 专业简要VO |
| `vo/city/CityBriefVO.java` | 新建 | 城市简要VO |
| `dto/algorithm/admission/AdmissionGroupQueryDTO.java` | 新建 | 专业组查询DTO |
| `dto/algorithm/admission/AdmissionMajorQueryDTO.java` | 新建 | 专业明细查询DTO |
| `service/algorithm/admission/AdmissionQueryService.java` | 新建 | 查询服务接口 |
| `service/impl/algorithm/admission/AdmissionQueryServiceImpl.java` | 新建 | 查询服务实现 |
| `controller/algorithm/admission/AdmissionQueryController.java` | 新建 | 查询控制器 |
| `controller/university/UniversityBriefController.java` | 新建 | 院校详情控制器 |
| `controller/major/MajorBriefController.java` | 新建 | 专业详情控制器 |
| `controller/city/CityBriefController.java` | 新建 | 城市详情控制器 |

---

## Task 1: SubjectMatchResult 和 SubjectMatcher

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/matcher/SubjectMatchResult.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/algorithm/matcher/SubjectMatcher.java`

- [ ] **Step 1: 创建 SubjectMatchResult**

```java
package com.haifeng.common.service.algorithm.matcher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectMatchResult {
    private boolean match;
    private String reason;

    public static SubjectMatchResult ok() {
        return SubjectMatchResult.builder().match(true).build();
    }

    public static SubjectMatchResult fail(String reason) {
        return SubjectMatchResult.builder().match(false).reason(reason).build();
    }
}
```

- [ ] **Step 2: 创建 SubjectMatcher**

```java
package com.haifeng.common.service.algorithm.matcher;

import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SubjectMatcher {

    public SubjectMatchResult match(MemberGaokao gaokao, AdmissionGroup group) {
        List<String> userSubjects = Arrays.asList(
                gaokao.getSubjectType(),
                gaokao.getSecondSubjectType(),
                gaokao.getThirdSubjectType()
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());

        List<String> groupSubjects = group.getSubjects();
        String reqType = group.getRequirementType();

        // 不限 → 永远符合
        if ("不限".equals(reqType) || groupSubjects == null || groupSubjects.isEmpty()) {
            return SubjectMatchResult.ok();
        }

        // 计算交集数量
        long matchCount = groupSubjects.stream()
                .filter(userSubjects::contains)
                .count();

        switch (reqType) {
            case "2选1":
            case "3选1":
                if (matchCount >= 1) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("需从 " + String.join("/", groupSubjects) + " 中选考至少1门");

            case "必选1":
                if (matchCount >= 1) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须选考 " + groupSubjects.get(0));

            case "必选2":
                if (matchCount >= 2) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须同时选考 " + String.join(" 和 ", groupSubjects));

            case "必选3":
                if (matchCount >= 3) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须同时选考 " + String.join("、", groupSubjects));

            default:
                return SubjectMatchResult.ok();
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/matcher/SubjectMatchResult.java
git add haifeng-common/src/main/java/com/haifeng/common/service/algorithm/matcher/SubjectMatcher.java
git commit -m "feat(matcher): add SubjectMatcher for subject requirement matching"
```

---

## Task 2: YearScoreVO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/YearScoreVO.java`

- [ ] **Step 1: 创建 YearScoreVO**

```java
package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class YearScoreVO {
    private Short year;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private Integer maxScore;
    private Integer maxRank;
    private Integer admissionCount;
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/YearScoreVO.java
git commit -m "feat(vo): add YearScoreVO for historical score data"
```

---

## Task 3: AdmissionGroupMapper 新增方法

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionGroupMapper.java`

- [ ] **Step 1: 新增批量查询历史数据方法**

在 `AdmissionGroupMapper.java` 末尾添加：

```java
    /**
     * 批量查询历史数据
     * @param keys university_id + group_code 组合列表
     * @param minYear 最小年份
     * @return 历史数据列表
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_group " +
            "WHERE is_deleted = FALSE " +
            "AND year >= #{minYear} " +
            "AND (university_id, group_code) IN " +
            "<foreach collection='keys' item='key' open='(' separator=',' close=')'>" +
            "(#{key.universityId}, #{key.groupCode})" +
            "</foreach> " +
            "ORDER BY university_id, group_code, year DESC" +
            "</script>")
    List<AdmissionGroup> selectHistoryByKeys(@Param("keys") List<GroupKey> keys, @Param("minYear") Short minYear);

    /**
     * 分页查询专业组（带选科筛选）
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_group " +
            "WHERE province = #{province} " +
            "AND batch = #{batch} " +
            "AND is_deleted = FALSE " +
            "<if test='subjectFilter and userSubjects != null'>" +
            "AND (" +
            "  requirement_type = '不限' " +
            "  OR subjects = '{}' " +
            "  OR subjects IS NULL " +
            "  OR (requirement_type IN ('2选1', '3选1') AND subjects &amp;&amp; #{userSubjects}::text[]) " +
            "  OR (requirement_type IN ('必选1', '必选2', '必选3') AND #{userSubjects}::text[] @&gt; subjects)" +
            ")" +
            "</if>" +
            "ORDER BY min_rank ASC NULLS LAST " +
            "LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    List<AdmissionGroup> selectPageByCondition(
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("subjectFilter") boolean subjectFilter,
            @Param("userSubjects") String userSubjects,
            @Param("size") int size,
            @Param("offset") int offset);

    /**
     * 统计总数（带选科筛选）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_admission_group " +
            "WHERE province = #{province} " +
            "AND batch = #{batch} " +
            "AND is_deleted = FALSE " +
            "<if test='subjectFilter and userSubjects != null'>" +
            "AND (" +
            "  requirement_type = '不限' " +
            "  OR subjects = '{}' " +
            "  OR subjects IS NULL " +
            "  OR (requirement_type IN ('2选1', '3选1') AND subjects &amp;&amp; #{userSubjects}::text[]) " +
            "  OR (requirement_type IN ('必选1', '必选2', '必选3') AND #{userSubjects}::text[] @&gt; subjects)" +
            ")" +
            "</if>" +
            "</script>")
    long countByCondition(
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("subjectFilter") boolean subjectFilter,
            @Param("userSubjects") String userSubjects);
```

- [ ] **Step 2: 创建 GroupKey 内部类**

在 mapper 包下创建 `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/GroupKey.java`：

```java
package com.haifeng.common.mapper.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupKey {
    private Long universityId;
    private String groupCode;
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionGroupMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/GroupKey.java
git commit -m "feat(mapper): add batch query methods for AdmissionGroupMapper"
```

---

## Task 4: AdmissionGroupPageVO 和 DTO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionGroupPageVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/admission/AdmissionGroupQueryDTO.java`

- [ ] **Step 1: 创建 AdmissionGroupPageVO**

```java
package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdmissionGroupPageVO {
    private Integer id;
    private Boolean masked;

    private String universityName;
    private String cityName;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private List<String> subjects;
    private String requirementType;
    private String description;
    private Integer majorCount;
    private Integer categoryCount;
    private List<String> constraints;

    private Boolean subjectMatch;
    private String subjectMatchReason;

    private List<YearScoreVO> historyScores;
}
```

- [ ] **Step 2: 创建 AdmissionGroupQueryDTO**

```java
package com.haifeng.app.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdmissionGroupQueryDTO {
    @NotBlank(message = "批次不能为空")
    private String batch;

    private Boolean subjectFilter = false;

    private Integer page = 1;

    private Integer size = 20;
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionGroupPageVO.java
git add haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/admission/AdmissionGroupQueryDTO.java
git commit -m "feat(dto/vo): add AdmissionGroupPageVO and AdmissionGroupQueryDTO"
```

---

## Task 5: AdmissionQueryService 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/admission/AdmissionQueryService.java`

- [ ] **Step 1: 创建 Service 接口**

```java
package com.haifeng.app.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;

public interface AdmissionQueryService {

    /**
     * 分页查询专业组
     */
    IPage<AdmissionGroupPageVO> pageGroups(AdmissionGroupQueryDTO dto);

    /**
     * 分页查询专业明细
     */
    IPage<AdmissionMajorPageVO> pageMajors(AdmissionMajorQueryDTO dto);
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/algorithm/admission/AdmissionQueryService.java
git commit -m "feat(service): add AdmissionQueryService interface"
```

---

## Task 6: AdmissionQueryServiceImpl（专业组查询）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 创建 Service 实现类**

```java
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
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(service): implement pageGroups in AdmissionQueryServiceImpl"
```

---

## Task 7: AdmissionMajorScoreMapper 新增方法

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionMajorScoreMapper.java`

- [ ] **Step 1: 新增历史数据查询方法**

在 `AdmissionMajorScoreMapper.java` 末尾添加：

```java
    /**
     * 批量查询专业历史数据
     * @param universityId 大学ID
     * @param majorCodes 专业代码列表
     * @param minYear 最小年份
     * @return 历史数据列表
     */
    @Select("<script>" +
            "SELECT ams.*, ag.year " +
            "FROM t_admission_major_score ams " +
            "INNER JOIN t_admission_group ag ON ams.group_id = ag.id " +
            "WHERE ag.university_id = #{universityId} " +
            "AND ag.is_deleted = FALSE " +
            "AND ag.year >= #{minYear} " +
            "AND ams.major_code IN " +
            "<foreach collection='majorCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach> " +
            "ORDER BY ams.major_code, ag.year DESC" +
            "</script>")
    List<Map<String, Object>> selectHistoryByMajorCodes(
            @Param("universityId") Long universityId,
            @Param("majorCodes") List<String> majorCodes,
            @Param("minYear") Short minYear);
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionMajorScoreMapper.java
git commit -m "feat(mapper): add selectHistoryByMajorCodes method"
```

---

## Task 8: AdmissionMajorPageVO 和 DTO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionMajorPageVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/admission/AdmissionMajorQueryDTO.java`

- [ ] **Step 1: 创建 AdmissionMajorPageVO**

```java
package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdmissionMajorPageVO {
    private Integer id;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private List<String> constraints;

    private List<YearScoreVO> historyScores;
}
```

- [ ] **Step 2: 创建 AdmissionMajorQueryDTO**

```java
package com.haifeng.app.dto.algorithm.admission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdmissionMajorQueryDTO {
    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    private Integer page = 1;

    private Integer size = 20;
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/admission/AdmissionMajorPageVO.java
git add haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/admission/AdmissionMajorQueryDTO.java
git commit -m "feat(dto/vo): add AdmissionMajorPageVO and AdmissionMajorQueryDTO"
```

---

## Task 9: AdmissionQueryServiceImpl（专业明细查询）

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 实现 pageMajors 方法**

替换 `pageMajors` 方法：

```java
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

        // 4. 组装 VO
        List<AdmissionMajorPageVO> voList = majorPage.getRecords().stream()
                .map(major -> buildMajorVO(major, historyMap))
                .collect(Collectors.toList());

        Page<AdmissionMajorPageVO> result = new Page<>(dto.getPage(), dto.getSize());
        result.setRecords(voList);
        result.setTotal(majorPage.getTotal());
        return result;
    }

    private AdmissionMajorPageVO buildMajorVO(AdmissionMajorScore major,
                                              Map<String, List<Map<String, Object>>> historyMap) {
        List<Map<String, Object>> history = historyMap.getOrDefault(major.getMajorCode(), Collections.emptyList());
        List<YearScoreVO> historyScores = history.stream()
                .limit(5)
                .map(this::mapToYearScoreVO)
                .collect(Collectors.toList());

        return AdmissionMajorPageVO.builder()
                .id(major.getId())
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
```

- [ ] **Step 2: 添加必要的 import**

在文件顶部添加：

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(service): implement pageMajors in AdmissionQueryServiceImpl"
```

---

## Task 10: AdmissionQueryController

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/admission/AdmissionQueryController.java`

- [ ] **Step 1: 创建 Controller**

```java
package com.haifeng.app.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.app.service.algorithm.admission.AdmissionQueryService;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/admission")
@RequiredArgsConstructor
@RequireLogin
public class AdmissionQueryController {

    private final AdmissionQueryService admissionQueryService;

    /**
     * 分页查询专业组
     */
    @GetMapping("/group/page")
    public R<IPage<AdmissionGroupPageVO>> pageGroups(@Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionQueryService.pageGroups(dto));
    }

    /**
     * 分页查询专业明细
     */
    @GetMapping("/major/page")
    public R<IPage<AdmissionMajorPageVO>> pageMajors(@Valid AdmissionMajorQueryDTO dto) {
        return R.ok(admissionQueryService.pageMajors(dto));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/admission/AdmissionQueryController.java
git commit -m "feat(controller): add AdmissionQueryController"
```

---

## Task 11: 三个详情 VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityBriefVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/MajorBriefVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/city/CityBriefVO.java`

- [ ] **Step 1: 创建 UniversityBriefVO**

```java
package com.haifeng.app.vo.university;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UniversityBriefVO {
    private String name;
    private String provinceName;
    private String cityName;
    private String region;
    private String category;
    private String educationLevel;
    private String nature;
    private BigDecimal recommendationRate;
    private String department;
    private List<String> tags;
    private String imageUrl;
}
```

- [ ] **Step 2: 创建 MajorBriefVO**

```java
package com.haifeng.app.vo.major;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MajorBriefVO {
    private String majorCode;
    private String majorName;
    private String disciplineName;
    private String majorType;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private String description;
}
```

- [ ] **Step 3: 创建 CityBriefVO**

```java
package com.haifeng.app.vo.city;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityBriefVO {
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityBriefVO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/major/MajorBriefVO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/city/CityBriefVO.java
git commit -m "feat(vo): add UniversityBriefVO, MajorBriefVO, CityBriefVO"
```

---

## Task 12: 三个详情 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityBriefController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/major/MajorBriefController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/city/CityBriefController.java`

- [ ] **Step 1: 创建 UniversityBriefController**

```java
package com.haifeng.app.controller.university;

import com.haifeng.app.vo.university.UniversityBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
@RequireLogin
public class UniversityBriefController {

    private final UniversityMapper universityMapper;

    @GetMapping("/brief")
    public R<UniversityBriefVO> getByName(@RequestParam String name) {
        University university = universityMapper.selectByName(name);
        if (university == null) {
            throw new BusinessException(404, "院校不存在");
        }

        return R.ok(UniversityBriefVO.builder()
                .name(university.getName())
                .provinceName(university.getProvinceName())
                .cityName(university.getCityName())
                .region(university.getRegion())
                .category(university.getCategory())
                .educationLevel(university.getEducationLevel())
                .nature(university.getNature())
                .recommendationRate(university.getRecommendationRate())
                .department(university.getDepartment())
                .tags(university.getTags())
                .imageUrl(university.getImageUrl())
                .build());
    }
}
```

- [ ] **Step 2: 创建 MajorBriefController**

```java
package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.vo.major.MajorBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/app/major")
@RequiredArgsConstructor
@RequireLogin
public class MajorBriefController {

    private final MajorMapper majorMapper;

    @GetMapping("/brief")
    public R<List<MajorBriefVO>> getByName(@RequestParam String name) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getMajorName, name)
               .eq(Major::getStatus, 1);

        List<Major> majors = majorMapper.selectList(wrapper);

        List<MajorBriefVO> voList = majors.stream()
                .map(m -> MajorBriefVO.builder()
                        .majorCode(m.getMajorCode())
                        .majorName(m.getMajorName())
                        .disciplineName(m.getDisciplineName())
                        .majorType(m.getMajorType())
                        .majorCategory(m.getMajorCategory())
                        .parentCategory(m.getParentCategory())
                        .majorTags(m.getMajorTags())
                        .degreeAwarded(m.getDegreeAwarded())
                        .description(m.getDescription())
                        .build())
                .collect(Collectors.toList());

        return R.ok(voList);
    }
}
```

- [ ] **Step 3: 创建 CityBriefController**

```java
package com.haifeng.app.controller.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/city")
@RequiredArgsConstructor
@RequireLogin
public class CityBriefController {

    private final CityMapper cityMapper;

    @GetMapping("/brief")
    public R<CityBriefVO> getByName(@RequestParam String name) {
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(City::getCityName, name)
               .eq(City::getIsDeleted, false);

        City city = cityMapper.selectOne(wrapper);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        return R.ok(CityBriefVO.builder()
                .cityName(city.getCityName())
                .province(city.getProvince())
                .region(city.getRegion())
                .cityIntro(city.getCityIntro())
                .collegeCount(city.getCollegeCount())
                .build());
    }
}
```

- [ ] **Step 4: 在 UniversityMapper 新增 selectByName 方法**

在 `haifeng-common/src/main/java/com/haifeng/common/mapper/university/UniversityMapper.java` 添加：

```java
    @Select("SELECT * FROM t_universities WHERE name = #{name} AND status = 1 LIMIT 1")
    University selectByName(@Param("name") String name);
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityBriefController.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/major/MajorBriefController.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/city/CityBriefController.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/university/UniversityMapper.java
git commit -m "feat(controller): add UniversityBriefController, MajorBriefController, CityBriefController"
```

---

## 验证

- [ ] **Step 1: 启动 app 服务**

```bash
cd haifeng-app
mvn spring-boot:run
```

- [ ] **Step 2: 测试专业组查询**

```bash
curl -X GET "http://localhost:8081/api/v1/app/admission/group/page?batch=本科批" \
  -H "Authorization: Bearer <token>"
```

预期：返回20条数据，普通用户后10条 masked=true

- [ ] **Step 3: 测试专业明细查询**

```bash
curl -X GET "http://localhost:8081/api/v1/app/admission/major/page?groupId=1" \
  -H "Authorization: Bearer <token>"
```

- [ ] **Step 4: 测试三个详情接口**

```bash
curl -X GET "http://localhost:8081/api/v1/app/university/brief?name=清华大学" \
  -H "Authorization: Bearer <token>"

curl -X GET "http://localhost:8081/api/v1/app/major/brief?name=计算机科学与技术" \
  -H "Authorization: Bearer <token>"

curl -X GET "http://localhost:8081/api/v1/app/city/brief?name=北京" \
  -H "Authorization: Bearer <token>"
```
