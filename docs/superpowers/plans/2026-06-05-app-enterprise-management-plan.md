# C端企业管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build C端 enterprise list, enterprise positions, enterprise→industry jump data, and industry→enterprise jump data APIs.

**Architecture:** Add a focused app-side `company` module for enterprise queries, reusing the existing common `Enterprise`, `EnterprisePosition`, `EnterpriseIndustry` entities and mappers. Extend the existing app-side industry module only for the industry→enterprise jump-data endpoint. Keep the Pro-only middle-table endpoints separate from the public enterprise list so public responses do not leak Pro-gated relationship data.

**Tech Stack:** Spring Boot, MyBatis-Plus, Jakarta Validation, Lombok, JUnit 5, Mockito, AssertJ.

---

## Constraints

- Do not run `git commit` or any git submit step. The requester will commit all changes later.
- Do not modify database schema or Flyway migrations.
- Do not implement后台 CRUD/import.
- Middle-table endpoints use only `@RequirePro`; do not also add `@RequireLogin`.
- Enterprise positions endpoint uses `@RequireLogin`.

## File Structure

### Create

- `haifeng-app/src/main/java/com/haifeng/app/dto/company/EnterpriseQueryDTO.java` — request DTO for public enterprise paging filters.
- `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterpriseListVO.java` — enterprise list response item.
- `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterprisePositionVO.java` — enterprise position response item.
- `haifeng-app/src/main/java/com/haifeng/app/vo/company/IndustryJumpVO.java` — compact industry jump target.
- `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterpriseIndustryGroupVO.java` — one enterprise id plus its industries.
- `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterpriseJumpVO.java` — compact enterprise jump target.
- `haifeng-app/src/main/java/com/haifeng/app/vo/company/IndustryEnterpriseGroupVO.java` — one industry id plus its enterprises.
- `haifeng-app/src/main/java/com/haifeng/app/service/company/EnterpriseService.java` — service interface for enterprise C端 queries.
- `haifeng-app/src/main/java/com/haifeng/app/service/impl/company/EnterpriseServiceImpl.java` — service implementation using common mappers.
- `haifeng-app/src/main/java/com/haifeng/app/controller/company/EnterpriseController.java` — app enterprise endpoints.
- `haifeng-app/src/test/java/com/haifeng/app/service/company/EnterpriseServiceImplTest.java` — unit tests for enterprise service behavior.
- `haifeng-app/src/test/java/com/haifeng/app/service/industry/IndustryServiceImplTest.java` — unit tests for the new industry→enterprise grouping behavior.
- `haifeng-app/src/test/java/com/haifeng/app/controller/company/EnterpriseControllerAnnotationTest.java` — reflection tests for enterprise endpoint permissions.
- `haifeng-app/src/test/java/com/haifeng/app/controller/industry/IndustryControllerAnnotationTest.java` — reflection test for industry→enterprise Pro permission.

### Modify

- `haifeng-app/src/main/java/com/haifeng/app/controller/industry/IndustryController.java` — add `GET /api/v1/app/industry/enterprises` with `@RequirePro`.
- `haifeng-app/src/main/java/com/haifeng/app/service/industry/IndustryService.java` — add `enterprisesByIndustryIds` method.
- `haifeng-app/src/main/java/com/haifeng/app/service/impl/industry/IndustryServiceImpl.java` — inject `EnterpriseIndustryMapper` and implement grouping.

---

## Task 1: Add Enterprise DTO and VO Types

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/company/EnterpriseQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterpriseListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterprisePositionVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/company/IndustryJumpVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterpriseIndustryGroupVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/company/EnterpriseJumpVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/company/IndustryEnterpriseGroupVO.java`

- [ ] **Step 1: Create `EnterpriseQueryDTO`**

```java
package com.haifeng.app.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端企业列表查询 DTO
 * enterpriseName 走 LIKE；enterpriseNature / enterpriseType / cityName / recruitmentStatus 精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseQueryDTO extends BasePageQueryDTO {

    /** 企业名称模糊（LIKE %enterpriseName%） */
    private String enterpriseName;

    /** 企业性质精准匹配 */
    private String enterpriseNature;

    /** 企业类型精准匹配 */
    private String enterpriseType;

    /** 城市名称精准匹配 */
    private String cityName;

    /** 招聘状态精准匹配 */
    private String recruitmentStatus;
}
```

- [ ] **Step 2: Create `EnterpriseListVO`**

```java
package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端企业列表 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String logoUrl;
    private String officialWebsite;
    private String region;
    private String enterpriseScale;
    private String mainBusiness;
    private String enterpriseIntro;
}
```

- [ ] **Step 3: Create `EnterprisePositionVO`**

```java
package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * C 端企业岗位 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterprisePositionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String positionName;
    private String recruitmentType;
    private String positionRequirement;
    private List<String> positionTags;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private String majorRequirement;
    private String workExperience;
    private Integer salaryMin;
    private Integer salaryMax;
    private String applyLink;
    private OffsetDateTime deadline;
    private String positionStatus;
}
```

- [ ] **Step 4: Create middle-table jump VOs**

```java
package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 行业跳转信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryJumpVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long industryId;
    private String industryName;
}
```

```java
package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 企业关联行业分组 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseIndustryGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long enterpriseId;
    private List<IndustryJumpVO> industries;
}
```

```java
package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 企业跳转信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseJumpVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long enterpriseId;
    private String enterpriseName;
}
```

```java
package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 行业关联企业分组 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryEnterpriseGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long industryId;
    private List<EnterpriseJumpVO> enterprises;
}
```

- [ ] **Step 5: Compile only the app module to catch type errors**

Run:

```bash
mvn -pl haifeng-app -am compile -DskipTests
```

Expected: compile reaches `BUILD SUCCESS`. If unrelated existing source errors appear, record them and continue only if they are not caused by these new files.

---

## Task 2: Add Enterprise Service Tests First

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/company/EnterpriseServiceImplTest.java`

- [ ] **Step 1: Write failing service tests**

Create the test file with this content. It references `EnterpriseServiceImpl`, which is implemented in Task 3, so it should fail before implementation.

```java
package com.haifeng.app.service.company;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.service.impl.company.EnterpriseServiceImpl;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceImplTest {

    @Mock private EnterpriseMapper enterpriseMapper;
    @Mock private EnterprisePositionMapper enterprisePositionMapper;
    @Mock private EnterpriseIndustryMapper enterpriseIndustryMapper;

    @InjectMocks private EnterpriseServiceImpl service;

    @Test
    void page_returnsConvertedVOs() {
        Enterprise entity = Enterprise.builder()
                .id(1L)
                .cityName("深圳")
                .enterpriseName("华为技术有限公司")
                .enterpriseNature("民企")
                .enterpriseType("科技")
                .logoUrl("https://example.com/logo.png")
                .officialWebsite("https://www.huawei.com")
                .region("华南")
                .enterpriseScale("10万人以上")
                .mainBusiness("通信设备")
                .enterpriseIntro("企业介绍")
                .isDeleted(false)
                .build();
        Page<Enterprise> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(enterpriseMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        IPage<EnterpriseListVO> result = service.page(new EnterpriseQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        EnterpriseListVO vo = result.getRecords().get(0);
        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getCityName()).isEqualTo("深圳");
        assertThat(vo.getEnterpriseName()).isEqualTo("华为技术有限公司");
        assertThat(vo.getEnterpriseNature()).isEqualTo("民企");
        assertThat(vo.getEnterpriseType()).isEqualTo("科技");
        assertThat(vo.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(vo.getOfficialWebsite()).isEqualTo("https://www.huawei.com");
        assertThat(vo.getRegion()).isEqualTo("华南");
        assertThat(vo.getEnterpriseScale()).isEqualTo("10万人以上");
        assertThat(vo.getMainBusiness()).isEqualTo("通信设备");
        assertThat(vo.getEnterpriseIntro()).isEqualTo("企业介绍");
    }

    @Test
    void page_passesPageNumberAndSizeFromDto() {
        EnterpriseQueryDTO dto = new EnterpriseQueryDTO();
        dto.setPage(2);
        dto.setSize(20);
        Page<Enterprise> page = new Page<>(2, 20);
        page.setRecords(List.of());
        page.setTotal(0);

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        when(enterpriseMapper.selectPage(pageCaptor.capture(), any(Wrapper.class))).thenReturn(page);

        service.page(dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20L);
    }

    @Test
    void positions_enterpriseMissing_throws404() {
        when(enterpriseMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.positions(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("企业不存在");
        verify(enterprisePositionMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void positions_enterpriseDeleted_throws404() {
        Enterprise enterprise = Enterprise.builder().id(1L).isDeleted(true).build();
        when(enterpriseMapper.selectById(1L)).thenReturn(enterprise);

        assertThatThrownBy(() -> service.positions(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("企业不存在");
        verify(enterprisePositionMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void positions_returnsConvertedVOs() {
        Enterprise enterprise = Enterprise.builder().id(1L).isDeleted(false).build();
        OffsetDateTime deadline = OffsetDateTime.parse("2026-07-01T00:00:00+08:00");
        EnterprisePosition position = EnterprisePosition.builder()
                .id(11L)
                .enterpriseId(1L)
                .positionName("后端开发")
                .recruitmentType("校招")
                .positionRequirement("熟悉 Java")
                .positionTags(List.of("Java", "Spring"))
                .province("广东")
                .city("深圳")
                .workLocation("南山区")
                .educationRequirement("本科")
                .majorRequirement("计算机类")
                .workExperience("不限")
                .salaryMin(15)
                .salaryMax(25)
                .applyLink("https://example.com/apply")
                .deadline(deadline)
                .positionStatus("招聘中")
                .isDeleted(false)
                .build();
        when(enterpriseMapper.selectById(1L)).thenReturn(enterprise);
        when(enterprisePositionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(position));

        List<EnterprisePositionVO> result = service.positions(1L);

        assertThat(result).hasSize(1);
        EnterprisePositionVO vo = result.get(0);
        assertThat(vo.getPositionName()).isEqualTo("后端开发");
        assertThat(vo.getRecruitmentType()).isEqualTo("校招");
        assertThat(vo.getPositionRequirement()).isEqualTo("熟悉 Java");
        assertThat(vo.getPositionTags()).containsExactly("Java", "Spring");
        assertThat(vo.getProvince()).isEqualTo("广东");
        assertThat(vo.getCity()).isEqualTo("深圳");
        assertThat(vo.getWorkLocation()).isEqualTo("南山区");
        assertThat(vo.getEducationRequirement()).isEqualTo("本科");
        assertThat(vo.getMajorRequirement()).isEqualTo("计算机类");
        assertThat(vo.getWorkExperience()).isEqualTo("不限");
        assertThat(vo.getSalaryMin()).isEqualTo(15);
        assertThat(vo.getSalaryMax()).isEqualTo(25);
        assertThat(vo.getApplyLink()).isEqualTo("https://example.com/apply");
        assertThat(vo.getDeadline()).isEqualTo(deadline);
        assertThat(vo.getPositionStatus()).isEqualTo("招聘中");
    }

    @Test
    void industriesByEnterpriseIds_emptyIds_throwsBadRequest() {
        assertThatThrownBy(() -> service.industriesByEnterpriseIds(List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("企业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void industriesByEnterpriseIds_groupsByRequestedEnterpriseIdsAndReturnsEmptyGroups() {
        EnterpriseIndustry relation1 = EnterpriseIndustry.builder()
                .id(101L)
                .enterpriseId(1L)
                .industryId(10L)
                .industryName("人工智能")
                .sortOrder((short) 1)
                .build();
        EnterpriseIndustry relation2 = EnterpriseIndustry.builder()
                .id(102L)
                .enterpriseId(1L)
                .industryId(11L)
                .industryName("智能制造")
                .sortOrder((short) 2)
                .build();
        when(enterpriseIndustryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(relation1, relation2));

        List<EnterpriseIndustryGroupVO> result = service.industriesByEnterpriseIds(List.of(1L, 2L, 1L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEnterpriseId()).isEqualTo(1L);
        assertThat(result.get(0).getIndustries()).hasSize(2);
        assertThat(result.get(0).getIndustries().get(0).getIndustryId()).isEqualTo(10L);
        assertThat(result.get(0).getIndustries().get(0).getIndustryName()).isEqualTo("人工智能");
        assertThat(result.get(0).getIndustries().get(1).getIndustryId()).isEqualTo(11L);
        assertThat(result.get(0).getIndustries().get(1).getIndustryName()).isEqualTo("智能制造");
        assertThat(result.get(1).getEnterpriseId()).isEqualTo(2L);
        assertThat(result.get(1).getIndustries()).isEmpty();
    }
}
```

- [ ] **Step 2: Run the test and verify it fails before implementation**

Run:

```bash
mvn -pl haifeng-app -Dtest=EnterpriseServiceImplTest test
```

Expected: compilation fails because `EnterpriseServiceImpl` and enterprise app VO/service types are not fully implemented yet. If Task 1 has already created the VO/DTO types, the remaining expected failure is `cannot find symbol: class EnterpriseServiceImpl`.

---

## Task 3: Implement Enterprise Service

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/company/EnterpriseService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/company/EnterpriseServiceImpl.java`

- [ ] **Step 1: Create `EnterpriseService` interface**

```java
package com.haifeng.app.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;

import java.util.List;

public interface EnterpriseService {

    /** 企业分页列表（公开） */
    IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto);

    /** 企业岗位列表（登录） */
    List<EnterprisePositionVO> positions(Long enterpriseId);

    /** 企业 → 行业跳转信息（Pro） */
    List<EnterpriseIndustryGroupVO> industriesByEnterpriseIds(List<Long> enterpriseIds);
}
```

- [ ] **Step 2: Create `EnterpriseServiceImpl`**

```java
package com.haifeng.app.service.impl.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.service.company.EnterpriseService;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;
import com.haifeng.app.vo.company.IndustryJumpVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final EnterprisePositionMapper enterprisePositionMapper;
    private final EnterpriseIndustryMapper enterpriseIndustryMapper;

    @Override
    public IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto) {
        Page<Enterprise> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<Enterprise>()
                .eq(Enterprise::getIsDeleted, false)
                .like(StringUtils.hasText(dto.getEnterpriseName()), Enterprise::getEnterpriseName, dto.getEnterpriseName())
                .eq(StringUtils.hasText(dto.getEnterpriseNature()), Enterprise::getEnterpriseNature, dto.getEnterpriseNature())
                .eq(StringUtils.hasText(dto.getEnterpriseType()), Enterprise::getEnterpriseType, dto.getEnterpriseType())
                .eq(StringUtils.hasText(dto.getCityName()), Enterprise::getCityName, dto.getCityName())
                .eq(StringUtils.hasText(dto.getRecruitmentStatus()), Enterprise::getRecruitmentStatus, dto.getRecruitmentStatus())
                .orderByAsc(Enterprise::getId);

        IPage<Enterprise> entityPage = enterpriseMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public List<EnterprisePositionVO> positions(Long enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null || Boolean.TRUE.equals(enterprise.getIsDeleted())) {
            log.debug("企业不存在或已删除, enterpriseId={}", enterpriseId);
            throw new BusinessException(ResultCode.NOT_FOUND, "企业不存在");
        }

        LambdaQueryWrapper<EnterprisePosition> wrapper = new LambdaQueryWrapper<EnterprisePosition>()
                .eq(EnterprisePosition::getEnterpriseId, enterpriseId)
                .eq(EnterprisePosition::getIsDeleted, false)
                .orderByAsc(EnterprisePosition::getId);

        return enterprisePositionMapper.selectList(wrapper).stream()
                .map(this::toPositionVO)
                .toList();
    }

    @Override
    public List<EnterpriseIndustryGroupVO> industriesByEnterpriseIds(List<Long> enterpriseIds) {
        List<Long> ids = normalizeIds(enterpriseIds, "企业ID列表不能为空");

        LambdaQueryWrapper<EnterpriseIndustry> wrapper = new LambdaQueryWrapper<EnterpriseIndustry>()
                .in(EnterpriseIndustry::getEnterpriseId, ids)
                .orderByAsc(EnterpriseIndustry::getEnterpriseId)
                .orderByAsc(EnterpriseIndustry::getSortOrder)
                .orderByAsc(EnterpriseIndustry::getId);

        List<EnterpriseIndustry> relations = enterpriseIndustryMapper.selectList(wrapper);

        Map<Long, List<IndustryJumpVO>> grouped = new LinkedHashMap<>();
        ids.forEach(id -> grouped.put(id, new ArrayList<>()));

        for (EnterpriseIndustry relation : relations) {
            List<IndustryJumpVO> industries = grouped.get(relation.getEnterpriseId());
            if (industries != null) {
                industries.add(IndustryJumpVO.builder()
                        .industryId(relation.getIndustryId())
                        .industryName(relation.getIndustryName())
                        .build());
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> EnterpriseIndustryGroupVO.builder()
                        .enterpriseId(entry.getKey())
                        .industries(entry.getValue())
                        .build())
                .toList();
    }

    private EnterpriseListVO toListVO(Enterprise e) {
        return EnterpriseListVO.builder()
                .id(e.getId())
                .cityName(e.getCityName())
                .enterpriseName(e.getEnterpriseName())
                .enterpriseNature(e.getEnterpriseNature())
                .enterpriseType(e.getEnterpriseType())
                .logoUrl(e.getLogoUrl())
                .officialWebsite(e.getOfficialWebsite())
                .region(e.getRegion())
                .enterpriseScale(e.getEnterpriseScale())
                .mainBusiness(e.getMainBusiness())
                .enterpriseIntro(e.getEnterpriseIntro())
                .build();
    }

    private EnterprisePositionVO toPositionVO(EnterprisePosition e) {
        return EnterprisePositionVO.builder()
                .positionName(e.getPositionName())
                .recruitmentType(e.getRecruitmentType())
                .positionRequirement(e.getPositionRequirement())
                .positionTags(e.getPositionTags())
                .province(e.getProvince())
                .city(e.getCity())
                .workLocation(e.getWorkLocation())
                .educationRequirement(e.getEducationRequirement())
                .majorRequirement(e.getMajorRequirement())
                .workExperience(e.getWorkExperience())
                .salaryMin(e.getSalaryMin())
                .salaryMax(e.getSalaryMax())
                .applyLink(e.getApplyLink())
                .deadline(e.getDeadline())
                .positionStatus(e.getPositionStatus())
                .build();
    }

    private List<Long> normalizeIds(List<Long> ids, String emptyMessage) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, emptyMessage);
        }

        List<Long> normalized = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, emptyMessage);
        }
        return normalized;
    }
}
```

- [ ] **Step 3: Run enterprise service tests**

Run:

```bash
mvn -pl haifeng-app -Dtest=EnterpriseServiceImplTest test
```

Expected: `BUILD SUCCESS`, with all tests in `EnterpriseServiceImplTest` passing.

---

## Task 4: Add Enterprise Controller and Permission Tests

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/company/EnterpriseController.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/controller/company/EnterpriseControllerAnnotationTest.java`

- [ ] **Step 1: Write controller annotation tests**

```java
package com.haifeng.app.controller.company;

import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseControllerAnnotationTest {

    @Test
    void positions_requiresLogin() throws Exception {
        Method method = EnterpriseController.class.getMethod("positions", Long.class);

        assertThat(method.getAnnotation(RequireLogin.class)).isNotNull();
        assertThat(method.getAnnotation(RequirePro.class)).isNull();
    }

    @Test
    void industries_requiresOnlyPro() throws Exception {
        Method method = EnterpriseController.class.getMethod("industries", List.class);

        assertThat(method.getAnnotation(RequirePro.class)).isNotNull();
        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails before controller exists**

Run:

```bash
mvn -pl haifeng-app -Dtest=EnterpriseControllerAnnotationTest test
```

Expected: compilation fails because `EnterpriseController` does not exist.

- [ ] **Step 3: Create `EnterpriseController`**

```java
package com.haifeng.app.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.service.company.EnterpriseService;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端企业管理 - 列表（公开）+ 岗位（登录）+ 关联行业（Pro）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    /** 企业分页列表（公开） */
    @GetMapping("/list")
    public R<IPage<EnterpriseListVO>> list(@Valid EnterpriseQueryDTO dto) {
        return R.ok(enterpriseService.page(dto));
    }

    /** 企业岗位列表（登录） */
    @RequireLogin
    @GetMapping("/{enterpriseId}/positions")
    public R<List<EnterprisePositionVO>> positions(@PathVariable Long enterpriseId) {
        return R.ok(enterpriseService.positions(enterpriseId));
    }

    /** 企业 → 行业跳转信息（Pro 及以上） */
    @RequirePro
    @GetMapping("/industries")
    public R<List<EnterpriseIndustryGroupVO>> industries(@RequestParam List<Long> enterpriseIds) {
        return R.ok(enterpriseService.industriesByEnterpriseIds(enterpriseIds));
    }
}
```

- [ ] **Step 4: Run controller annotation tests**

Run:

```bash
mvn -pl haifeng-app -Dtest=EnterpriseControllerAnnotationTest test
```

Expected: `BUILD SUCCESS`.

---

## Task 5: Extend Industry Service with Industry→Enterprise Jump Data

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/industry/IndustryService.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/industry/IndustryServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/industry/IndustryServiceImplTest.java`

- [ ] **Step 1: Write failing industry service tests**

```java
package com.haifeng.app.service.industry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.impl.industry.IndustryServiceImpl;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.industry.IndustryDetailMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndustryServiceImplTest {

    @Mock private IndustryMapper industryMapper;
    @Mock private IndustryDetailMapper industryDetailMapper;
    @Mock private EnterpriseIndustryMapper enterpriseIndustryMapper;

    @InjectMocks private IndustryServiceImpl service;

    @Test
    void enterprisesByIndustryIds_emptyIds_throwsBadRequest() {
        assertThatThrownBy(() -> service.enterprisesByIndustryIds(List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("行业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void enterprisesByIndustryIds_groupsByRequestedIndustryIdsAndReturnsEmptyGroups() {
        EnterpriseIndustry relation1 = EnterpriseIndustry.builder()
                .id(101L)
                .industryId(10L)
                .enterpriseId(1L)
                .enterpriseName("华为技术有限公司")
                .sortOrder((short) 1)
                .build();
        EnterpriseIndustry relation2 = EnterpriseIndustry.builder()
                .id(102L)
                .industryId(10L)
                .enterpriseId(2L)
                .enterpriseName("腾讯科技")
                .sortOrder((short) 2)
                .build();
        when(enterpriseIndustryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(relation1, relation2));

        List<IndustryEnterpriseGroupVO> result = service.enterprisesByIndustryIds(List.of(10L, 11L, 10L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIndustryId()).isEqualTo(10L);
        assertThat(result.get(0).getEnterprises()).hasSize(2);
        assertThat(result.get(0).getEnterprises().get(0).getEnterpriseId()).isEqualTo(1L);
        assertThat(result.get(0).getEnterprises().get(0).getEnterpriseName()).isEqualTo("华为技术有限公司");
        assertThat(result.get(0).getEnterprises().get(1).getEnterpriseId()).isEqualTo(2L);
        assertThat(result.get(0).getEnterprises().get(1).getEnterpriseName()).isEqualTo("腾讯科技");
        assertThat(result.get(1).getIndustryId()).isEqualTo(11L);
        assertThat(result.get(1).getEnterprises()).isEmpty();
    }
}
```

- [ ] **Step 2: Run test to verify it fails before method exists**

Run:

```bash
mvn -pl haifeng-app -Dtest=IndustryServiceImplTest test
```

Expected: compilation fails because `enterprisesByIndustryIds` is not defined.

- [ ] **Step 3: Modify `IndustryService`**

Add the import and method:

```java
package com.haifeng.app.service.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;

import java.util.List;

public interface IndustryService {

    /**
     * 分页查询行业列表（isDeleted=false）；category EQ；排序 id ASC
     */
    IPage<IndustryListVO> page(IndustryQueryDTO dto);

    /**
     * 行业详情：通过 industryId 关联 t_industry_detail 查询
     * 不存在 → BusinessException(NOT_FOUND)
     */
    IndustryDetailVO detail(Long industryId);

    /** 行业 → 企业跳转信息（Pro） */
    List<IndustryEnterpriseGroupVO> enterprisesByIndustryIds(List<Long> industryIds);
}
```

- [ ] **Step 4: Modify `IndustryServiceImpl`**

Replace the file content with the following complete version, preserving existing page/detail behavior and adding the new method:

```java
package com.haifeng.app.service.impl.industry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.service.industry.IndustryService;
import com.haifeng.app.vo.company.EnterpriseJumpVO;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.entity.industry.IndustryDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.industry.IndustryDetailMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryServiceImpl implements IndustryService {

    private final IndustryMapper industryMapper;
    private final IndustryDetailMapper industryDetailMapper;
    private final EnterpriseIndustryMapper enterpriseIndustryMapper;

    @Override
    public IPage<IndustryListVO> page(IndustryQueryDTO dto) {
        Page<Industry> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Industry> wrapper = new LambdaQueryWrapper<Industry>()
                .eq(Industry::getIsDeleted, false)
                .eq(StringUtils.hasText(dto.getCategory()), Industry::getCategory, dto.getCategory())
                .orderByAsc(Industry::getId);

        IPage<Industry> entityPage = industryMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public IndustryDetailVO detail(Long industryId) {
        IndustryDetail detail = industryDetailMapper.findByIndustryId(industryId);
        if (detail == null) {
            log.debug("行业详情不存在, industryId={}", industryId);
            throw new BusinessException(ResultCode.NOT_FOUND, "行业详情不存在");
        }

        return IndustryDetailVO.builder()
                .industryName(detail.getIndustryName())
                .shortDescription(detail.getShortDescription())
                .detailedDescription(detail.getDetailedDescription())
                .industryScale(detail.getIndustryScale())
                .industryTalentDemand(detail.getIndustryTalentDemand())
                .industrySalary(detail.getIndustrySalary())
                .policyInfo(detail.getPolicyInfo())
                .developmentSupportInfo(detail.getDevelopmentSupportInfo())
                .talentAnalysis(detail.getTalentAnalysis())
                .talentPolicy(detail.getTalentPolicy())
                .salaryData(detail.getSalaryData())
                .build();
    }

    @Override
    public List<IndustryEnterpriseGroupVO> enterprisesByIndustryIds(List<Long> industryIds) {
        List<Long> ids = normalizeIds(industryIds, "行业ID列表不能为空");

        LambdaQueryWrapper<EnterpriseIndustry> wrapper = new LambdaQueryWrapper<EnterpriseIndustry>()
                .in(EnterpriseIndustry::getIndustryId, ids)
                .orderByAsc(EnterpriseIndustry::getIndustryId)
                .orderByAsc(EnterpriseIndustry::getSortOrder)
                .orderByAsc(EnterpriseIndustry::getId);

        List<EnterpriseIndustry> relations = enterpriseIndustryMapper.selectList(wrapper);

        Map<Long, List<EnterpriseJumpVO>> grouped = new LinkedHashMap<>();
        ids.forEach(id -> grouped.put(id, new ArrayList<>()));

        for (EnterpriseIndustry relation : relations) {
            List<EnterpriseJumpVO> enterprises = grouped.get(relation.getIndustryId());
            if (enterprises != null) {
                enterprises.add(EnterpriseJumpVO.builder()
                        .enterpriseId(relation.getEnterpriseId())
                        .enterpriseName(relation.getEnterpriseName())
                        .build());
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> IndustryEnterpriseGroupVO.builder()
                        .industryId(entry.getKey())
                        .enterprises(entry.getValue())
                        .build())
                .toList();
    }

    private IndustryListVO toListVO(Industry e) {
        return IndustryListVO.builder()
                .id(e.getId())
                .industryName(e.getIndustryName())
                .category(e.getCategory())
                .description(e.getDescription())
                .annualGrowthRate(e.getAnnualGrowthRate())
                .marketScale(e.getMarketScale())
                .talentGap(e.getTalentGap())
                .investmentHeat(e.getInvestmentHeat())
                .build();
    }

    private List<Long> normalizeIds(List<Long> ids, String emptyMessage) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, emptyMessage);
        }

        List<Long> normalized = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, emptyMessage);
        }
        return normalized;
    }
}
```

- [ ] **Step 5: Run industry service tests**

Run:

```bash
mvn -pl haifeng-app -Dtest=IndustryServiceImplTest test
```

Expected: `BUILD SUCCESS`.

---

## Task 6: Add Industry Controller Endpoint and Permission Test

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/industry/IndustryController.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/controller/industry/IndustryControllerAnnotationTest.java`

- [ ] **Step 1: Write industry controller annotation test**

```java
package com.haifeng.app.controller.industry;

import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndustryControllerAnnotationTest {

    @Test
    void enterprises_requiresOnlyPro() throws Exception {
        Method method = IndustryController.class.getMethod("enterprises", List.class);

        assertThat(method.getAnnotation(RequirePro.class)).isNotNull();
        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails before endpoint exists**

Run:

```bash
mvn -pl haifeng-app -Dtest=IndustryControllerAnnotationTest test
```

Expected: test fails with `NoSuchMethodException` for `enterprises`.

- [ ] **Step 3: Modify `IndustryController`**

Replace the file content with the following complete version, preserving existing list/detail endpoints and adding `GET /enterprises`:

```java
package com.haifeng.app.controller.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.service.industry.IndustryService;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端行业管理 - 列表（公开）+ 详情（登录）+ 关联企业（Pro）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/industry")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryService industryService;

    /** 任务 2 接口 1：分页查询行业列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<IndustryListVO>> list(@Valid IndustryQueryDTO dto) {
        return R.ok(industryService.page(dto));
    }

    /** 行业 → 企业跳转信息（Pro 及以上） */
    @RequirePro
    @GetMapping("/enterprises")
    public R<List<IndustryEnterpriseGroupVO>> enterprises(@RequestParam List<Long> industryIds) {
        return R.ok(industryService.enterprisesByIndustryIds(industryIds));
    }

    /** 任务 2 接口 2：行业详情，需登录 */
    @RequireLogin
    @GetMapping("/{industryId}/detail")
    public R<IndustryDetailVO> detail(@PathVariable Long industryId) {
        return R.ok(industryService.detail(industryId));
    }
}
```

- [ ] **Step 4: Run industry controller annotation test**

Run:

```bash
mvn -pl haifeng-app -Dtest=IndustryControllerAnnotationTest test
```

Expected: `BUILD SUCCESS`.

---

## Task 7: Full Verification

**Files:**
- Verify all files touched by Tasks 1-6.

- [ ] **Step 1: Run targeted tests**

Run:

```bash
mvn -pl haifeng-app -Dtest=EnterpriseServiceImplTest,EnterpriseControllerAnnotationTest,IndustryServiceImplTest,IndustryControllerAnnotationTest test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run app compile with dependencies**

Run:

```bash
mvn -pl haifeng-app -am compile -DskipTests
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Manual endpoint checklist**

If the app can be started locally, verify these with Swagger/Postman/curl:

```http
GET /api/v1/app/enterprise/list?page=1&size=10
GET /api/v1/app/enterprise/list?enterpriseName=科技&enterpriseNature=民企&cityName=深圳
GET /api/v1/app/enterprise/{enterpriseId}/positions
GET /api/v1/app/enterprise/industries?enterpriseIds=1,2,3
GET /api/v1/app/industry/enterprises?industryIds=10,11
```

Expected behavior:

- Enterprise list is public and returns `IPage<EnterpriseListVO>`.
- Enterprise positions requires login and returns `List<EnterprisePositionVO>`.
- Enterprise→industry requires Pro and returns one group per deduped requested enterprise ID.
- Industry→enterprise requires Pro and returns one group per deduped requested industry ID.
- Empty `enterpriseIds` returns `BusinessException(ResultCode.BAD_REQUEST, "企业ID列表不能为空")`.
- Empty `industryIds` returns `BusinessException(ResultCode.BAD_REQUEST, "行业ID列表不能为空")`.

- [ ] **Step 4: Do not commit**

Do not run `git commit`. Report changed files and verification output to the requester.

---

## Self-Review

### Spec coverage

- Enterprise paging endpoint: Task 1 DTO/VO, Task 2 tests, Task 3 service, Task 4 controller.
- Enterprise positions endpoint with `@RequireLogin`: Task 2 tests, Task 3 service, Task 4 controller and annotation test.
- Enterprise→industry batch query with `@RequirePro`: Task 2 tests, Task 3 service, Task 4 controller and annotation test.
- Industry→enterprise batch query with `@RequirePro`: Task 5 service/test, Task 6 controller/annotation test.
- No DB/backend changes: documented in constraints and no tasks modify DB/admin files.
- No git commits: documented in constraints and Task 7 Step 4.

### Placeholder scan

No `TBD`, `TODO`, or unspecified implementation steps remain. All code-bearing steps include complete code blocks or explicit file replacement content.

### Type consistency

- `EnterpriseService.industriesByEnterpriseIds(List<Long>)` matches controller and tests.
- `IndustryService.enterprisesByIndustryIds(List<Long>)` matches controller and tests.
- VO property names match the approved design: `industryId`, `industryName`, `enterpriseId`, `enterpriseName`, `industries`, `enterprises`.
