# C 端院校管理模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `haifeng-app` 模块按 `7院校管理.md` 任务 1–4 落地 9 个只读接口：院校列表（公开）、院校详情（登录）、适应指南 6 子接口（其中 academic 需 Pro）、校园图册（登录）。

**Architecture:** 严格遵循 `InstitutionServiceImpl` 已确立的 Controller → Service 接口 → ServiceImpl → BaseMapper(LambdaQueryWrapper) 分层；entity/mapper 复用 `haifeng-common` 已有的 4 个类（`University / UniversityDetail / UniversityGuide / CampusGallery`）；JSONB 字段透传为 `Map<String, Object>`，本期不加 Redis 缓存。

**Tech Stack:** Spring Boot 3.x / MyBatis-Plus / PostgreSQL / Mockito + JUnit 5 + AssertJ；权限注解 `@RequireLogin` / `@RequirePro`；分页基类 `BasePageQueryDTO`；JSONB 用 `JacksonTypeHandler`（entity 已配）。

**参考实现模板：**
- Service 套路看 `haifeng-app/src/main/java/com/haifeng/app/service/impl/home/InstitutionServiceImpl.java`（去掉其中 Redis 部分）
- Controller 套路看 `haifeng-app/src/main/java/com/haifeng/app/controller/home/InstitutionController.java`
- 测试套路看 `haifeng-app/src/test/java/com/haifeng/app/service/home/InstitutionServiceImplTest.java`

---

## 文件总览（一次性建好骨架）

```
haifeng-app/src/main/java/com/haifeng/app/
├── controller/university/
│   ├── UniversityController.java          // 任务 1 + 2
│   ├── UniversityGuideController.java     // 任务 3（6 个子路径）
│   └── CampusGalleryController.java       // 任务 4
├── service/university/
│   ├── UniversityService.java
│   ├── UniversityGuideService.java
│   └── CampusGalleryService.java
├── service/impl/university/
│   ├── UniversityServiceImpl.java
│   ├── UniversityGuideServiceImpl.java
│   └── CampusGalleryServiceImpl.java
├── dto/university/
│   ├── UniversityQueryDTO.java
│   └── CampusGalleryQueryDTO.java
└── vo/university/
    ├── UniversityListVO.java
    ├── UniversityDetailVO.java
    ├── UniversityGuideOverviewVO.java
    ├── UniversityGuideSurvivalVO.java
    ├── UniversityGuideAcademicVO.java
    ├── UniversityGuideSocialVO.java
    ├── UniversityGuideSafetyVO.java
    ├── UniversityGuideLifeVO.java
    └── CampusGalleryListVO.java
```

> 已有的 `UniversityBriefController.java` / `UniversityBriefVO.java` 不动，与本期接口路径不冲突。

---

## 任务索引

- **Task 1** — DTO + VO 骨架（任务 1 列表）
- **Task 2** — `UniversityService.page(...)` TDD
- **Task 3** — `UniversityController` 列表接口
- **Task 4** — `UniversityDetailVO` + `UniversityService.detail(...)` TDD
- **Task 5** — `UniversityController` 详情接口（@RequireLogin）
- **Task 6** — 6 个 Guide VO 骨架
- **Task 7** — `UniversityGuideService` 6 方法 TDD
- **Task 8** — `UniversityGuideController` 6 子路径
- **Task 9** — Gallery DTO + VO + Service TDD
- **Task 10** — `CampusGalleryController`
- **Task 11** — 全模块编译 + 测试一次性验收

---

### Task 1：列表 DTO 与 VO 骨架

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/university/UniversityQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityListVO.java`

- [ ] **Step 1.1：创建 `UniversityQueryDTO`**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端院校列表查询 DTO
 * 全部筛选字段 optional；name 走 LIKE，其余精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityQueryDTO extends BasePageQueryDTO {

    /** 院校名称模糊（LIKE %name%） */
    private String name;

    private String provinceName;
    private String nature;
    private String category;
    private String department;
    private String educationLevel;
    private Boolean hasDoctorate;
    private Boolean hasMaster;
}
```

- [ ] **Step 1.2：创建 `UniversityListVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * C 端院校列表 VO（任务 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<String> tags;
    private String cityName;
    private String educationLevel;
    private String provinceName;
    private String introduction;
    private String imageUrl;
    private String nature;
    private String category;
    private Integer majorCount;
    private Boolean hasDoctorate;
    private Boolean hasMaster;
    private String department;
}
```

- [ ] **Step 1.3：编译并提交**

Run:
```bash
cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q
```
Expected: `BUILD SUCCESS`

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/university/UniversityQueryDTO.java \
        haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityListVO.java
git commit -m "feat(app/university): add UniversityQueryDTO and UniversityListVO"
```

---

### Task 2：列表 Service 接口 + TDD 实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/university/UniversityServiceImplTest.java`

- [ ] **Step 2.1：写失败测试 `UniversityServiceImplTest`**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.impl.university.UniversityServiceImpl;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityServiceImplTest {

    @Mock private UniversityMapper universityMapper;

    @InjectMocks private UniversityServiceImpl service;

    @Test
    void page_returnsConvertedVOs() {
        University entity = University.builder()
                .id(1L).name("清华大学").cityName("北京")
                .educationLevel("本科").provinceName("北京")
                .nature("公办").category("综合").majorCount(120)
                .hasDoctorate(true).hasMaster(true).department("教育部")
                .status((short) 1).build();
        Page<University> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(universityMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        UniversityQueryDTO dto = new UniversityQueryDTO();
        IPage<UniversityListVO> result = service.page(dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("清华大学");
        assertThat(result.getRecords().get(0).getMajorCount()).isEqualTo(120);
    }

    @Test
    void page_passesPageNumberAndSizeFromDto() {
        UniversityQueryDTO dto = new UniversityQueryDTO();
        dto.setPage(3);
        dto.setSize(20);

        Page<University> page = new Page<>(3, 20);
        page.setRecords(List.of());
        page.setTotal(0);

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        when(universityMapper.selectPage(pageCaptor.capture(), any(Wrapper.class))).thenReturn(page);

        service.page(dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(3L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20L);
    }
}
```

- [ ] **Step 2.2：创建 `UniversityService` 接口**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.vo.university.UniversityListVO;

public interface UniversityService {

    /**
     * 分页查询院校列表（仅 status=1）
     * 多筛选条件 AND 组合；name 走 LIKE %name%
     * 排序：sort_order ASC, id DESC
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);
}
```

- [ ] **Step 2.3：跑测试确认失败**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=UniversityServiceImplTest -q
```
Expected: 编译错误 `UniversityServiceImpl` 类不存在。

- [ ] **Step 2.4：实现 `UniversityServiceImpl`**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private static final short STATUS_PUBLISHED = 1;

    private final UniversityMapper universityMapper;

    @Override
    public IPage<UniversityListVO> page(UniversityQueryDTO dto) {
        Page<University> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<University>()
                .eq(University::getStatus, STATUS_PUBLISHED)
                .like(StringUtils.hasText(dto.getName()), University::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getProvinceName()), University::getProvinceName, dto.getProvinceName())
                .eq(StringUtils.hasText(dto.getNature()), University::getNature, dto.getNature())
                .eq(StringUtils.hasText(dto.getCategory()), University::getCategory, dto.getCategory())
                .eq(StringUtils.hasText(dto.getDepartment()), University::getDepartment, dto.getDepartment())
                .eq(StringUtils.hasText(dto.getEducationLevel()), University::getEducationLevel, dto.getEducationLevel())
                .eq(dto.getHasDoctorate() != null, University::getHasDoctorate, dto.getHasDoctorate())
                .eq(dto.getHasMaster() != null, University::getHasMaster, dto.getHasMaster())
                .orderByAsc(University::getSortOrder)
                .orderByDesc(University::getId);

        IPage<University> entityPage = universityMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    private UniversityListVO toListVO(University e) {
        return UniversityListVO.builder()
                .name(e.getName())
                .tags(e.getTags())
                .cityName(e.getCityName())
                .educationLevel(e.getEducationLevel())
                .provinceName(e.getProvinceName())
                .introduction(e.getIntroduction())
                .imageUrl(e.getImageUrl())
                .nature(e.getNature())
                .category(e.getCategory())
                .majorCount(e.getMajorCount())
                .hasDoctorate(e.getHasDoctorate())
                .hasMaster(e.getHasMaster())
                .department(e.getDepartment())
                .build();
    }
}
```

- [ ] **Step 2.5：跑测试确认通过**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=UniversityServiceImplTest -q
```
Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 2.6：提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityService.java \
        haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityServiceImpl.java \
        haifeng-app/src/test/java/com/haifeng/app/service/university/UniversityServiceImplTest.java
git commit -m "feat(app/university): implement paginated university list service"
```

---

### Task 3：列表 Controller（公开）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityController.java`

- [ ] **Step 3.1：创建 Controller（先只放 list 接口）**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端院校管理 - 列表（公开）+ 详情（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    /** 任务 1：分页查询院校列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<UniversityListVO>> list(@Valid UniversityQueryDTO dto) {
        return R.ok(universityService.page(dto));
    }
}
```

- [ ] **Step 3.2：编译 + 提交**

Run:
```bash
mvn -pl haifeng-app -am compile -q
```
Expected: `BUILD SUCCESS`

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityController.java
git commit -m "feat(app/university): expose public GET /university/list endpoint"
```

---

### Task 4：详情 VO + Service 方法 TDD

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityDetailVO.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityService.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityServiceImpl.java`
- Modify: `haifeng-app/src/test/java/com/haifeng/app/service/university/UniversityServiceImplTest.java`

- [ ] **Step 4.1：创建 `UniversityDetailVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * C 端院校详情 VO（任务 2，联表 t_universities + t_universities_detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 来自 t_universities_detail =====
    private String address;
    private String admissionPhone;
    private String website;
    private Integer historyGroupScore;
    private Integer scienceGroupScore;
    private List<String> carouselImages;
    /** 详情表的 introduction（更完整） */
    private String introduction;
    private Map<String, Integer> rankings;
    private String abroadRate;
    private String genderRatio;

    // ===== 来自 t_universities =====
    private String name;
    private String nameEn;
    private String provinceName;
    private String cityName;
    private String region;
    private String category;
    private Integer majorCount;
    private String educationLevel;
    private String nature;
    private BigDecimal recommendationRate;
    private Integer recommendationYear;
    private Boolean hasDoctorate;
    private Boolean hasMaster;
    private String department;
    private List<String> tags;
    private String famousUnion;
}
```

- [ ] **Step 4.2：扩展 Service 接口加 `detail(...)`**

把 `UniversityService.java` 改为：

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;

public interface UniversityService {

    /**
     * 分页查询院校列表（仅 status=1）；多筛选 AND；name LIKE；排序 sort_order ASC, id DESC
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);

    /**
     * 院校详情：联表查询 t_universities + t_universities_detail
     * 任一不存在或 status != 1 → BusinessException(NOT_FOUND)
     */
    UniversityDetailVO detail(Long universityId);
}
```

- [ ] **Step 4.3：往测试类追加 3 个 detail 测试用例**

在 `UniversityServiceImplTest` 现有内容基础上，添加 import 并追加用例：

新增 import（合并到既有 import 块）：
```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityDetailMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

类字段补充：
```java
@Mock private UniversityDetailMapper universityDetailMapper;
```

追加用例：
```java
@Test
void detail_returnsMergedVO() {
    University univ = University.builder()
            .id(1L).name("清华大学").nameEn("Tsinghua").provinceName("北京")
            .cityName("北京").region("华北").category("综合").majorCount(120)
            .educationLevel("本科").nature("公办").department("教育部")
            .hasDoctorate(true).hasMaster(true).status((short) 1).build();
    UniversityDetail detail = UniversityDetail.builder()
            .id(11L).universityId(1L).address("北京市海淀区")
            .admissionPhone("010-62770334").website("https://www.tsinghua.edu.cn")
            .introduction("详细介绍").status((short) 1).build();

    when(universityMapper.selectById(1L)).thenReturn(univ);
    when(universityDetailMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(detail);

    UniversityDetailVO vo = service.detail(1L);

    assertThat(vo.getName()).isEqualTo("清华大学");
    assertThat(vo.getAddress()).isEqualTo("北京市海淀区");
    assertThat(vo.getIntroduction()).isEqualTo("详细介绍");
}

@Test
void detail_universityNotFound_throws404() {
    when(universityMapper.selectById(99L)).thenReturn(null);

    assertThatThrownBy(() -> service.detail(99L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("院校不存在");
}

@Test
void detail_universityStatusZero_throws404() {
    University univ = University.builder().id(1L).status((short) 0).build();
    when(universityMapper.selectById(1L)).thenReturn(univ);

    assertThatThrownBy(() -> service.detail(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("院校不存在");
}

@Test
void detail_detailRecordMissing_throws404() {
    University univ = University.builder().id(1L).status((short) 1).build();
    when(universityMapper.selectById(1L)).thenReturn(univ);
    when(universityDetailMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

    assertThatThrownBy(() -> service.detail(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("院校详情不存在");
}
```

- [ ] **Step 4.4：跑测试确认失败**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=UniversityServiceImplTest -q
```
Expected: 编译失败或 `detail` 方法未实现。

- [ ] **Step 4.5：实现 `detail(...)`**

在 `UniversityServiceImpl` 中：

1. 字段注入新增：
```java
private final com.haifeng.common.mapper.university.UniversityDetailMapper universityDetailMapper;
```

2. 顶部 import 加：
```java
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityDetailMapper;
import com.haifeng.common.response.ResultCode;
```
> 上面 `universityDetailMapper` 字段直接写完整类名，也可与 import 选其一，但推荐用 import 形式保持风格统一。整理为：

```java
private final UniversityDetailMapper universityDetailMapper;
```

3. 追加方法：

```java
@Override
public UniversityDetailVO detail(Long universityId) {
    University univ = universityMapper.selectById(universityId);
    if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
        log.debug("院校不存在或已下架, universityId={}", universityId);
        throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
    }

    UniversityDetail detail = universityDetailMapper.selectOne(
            new LambdaQueryWrapper<UniversityDetail>()
                    .eq(UniversityDetail::getUniversityId, universityId)
                    .eq(UniversityDetail::getStatus, STATUS_PUBLISHED));
    if (detail == null) {
        log.debug("院校详情未配置, universityId={}", universityId);
        throw new BusinessException(ResultCode.NOT_FOUND, "院校详情不存在");
    }

    return UniversityDetailVO.builder()
            // detail
            .address(detail.getAddress())
            .admissionPhone(detail.getAdmissionPhone())
            .website(detail.getWebsite())
            .historyGroupScore(detail.getHistoryGroupScore())
            .scienceGroupScore(detail.getScienceGroupScore())
            .carouselImages(detail.getCarouselImages())
            .introduction(detail.getIntroduction())
            .rankings(detail.getRankings())
            .abroadRate(detail.getAbroadRate())
            .genderRatio(detail.getGenderRatio())
            // university
            .name(univ.getName())
            .nameEn(univ.getNameEn())
            .provinceName(univ.getProvinceName())
            .cityName(univ.getCityName())
            .region(univ.getRegion())
            .category(univ.getCategory())
            .majorCount(univ.getMajorCount())
            .educationLevel(univ.getEducationLevel())
            .nature(univ.getNature())
            .recommendationRate(univ.getRecommendationRate())
            .recommendationYear(univ.getRecommendationYear())
            .hasDoctorate(univ.getHasDoctorate())
            .hasMaster(univ.getHasMaster())
            .department(univ.getDepartment())
            .tags(univ.getTags())
            .famousUnion(univ.getFamousUnion())
            .build();
}
```

- [ ] **Step 4.6：跑测试通过**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=UniversityServiceImplTest -q
```
Expected: `Tests run: 6, Failures: 0`

- [ ] **Step 4.7：提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityDetailVO.java \
        haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityService.java \
        haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityServiceImpl.java \
        haifeng-app/src/test/java/com/haifeng/app/service/university/UniversityServiceImplTest.java
git commit -m "feat(app/university): implement university detail with t_universities_detail join"
```

---

### Task 5：详情 Controller 接口（@RequireLogin）

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityController.java`

- [ ] **Step 5.1：在 `UniversityController` 类内追加 detail 方法**

在 list 方法下方插入：

```java
/** 任务 2：院校详情，需登录 */
@com.haifeng.common.annotation.RequireLogin
@GetMapping("/{universityId}/detail")
public R<com.haifeng.app.vo.university.UniversityDetailVO> detail(@PathVariable Long universityId) {
    return R.ok(universityService.detail(universityId));
}
```

并把上述完整类名提到 import 区域（推荐风格）：

```java
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.common.annotation.RequireLogin;
import org.springframework.web.bind.annotation.PathVariable;
```

方法签名简化为：
```java
@RequireLogin
@GetMapping("/{universityId}/detail")
public R<UniversityDetailVO> detail(@PathVariable Long universityId) {
    return R.ok(universityService.detail(universityId));
}
```

- [ ] **Step 5.2：编译并提交**

Run:
```bash
mvn -pl haifeng-app -am compile -q
```
Expected: `BUILD SUCCESS`

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityController.java
git commit -m "feat(app/university): expose GET /university/{id}/detail (RequireLogin)"
```

---

### Task 6：6 个 Guide VO 骨架

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuideOverviewVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuideSurvivalVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuideAcademicVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuideSocialVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuideSafetyVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuideLifeVO.java`

- [ ] **Step 6.1：`UniversityGuideOverviewVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 任务 3.1 概览：指南自定义标签 + 院校简要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> customTags;

    // 来自 t_universities
    private String name;
    private List<String> tags;
    private String region;
    private String category;
    private String nature;
    private String imageUrl;
}
```

- [ ] **Step 6.2：`UniversityGuideSurvivalVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.2 基础生存类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideSurvivalVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> campusFacilities;
    private Map<String, Object> dormitoryServices;
    private Map<String, Object> campusTransportation;
}
```

- [ ] **Step 6.3：`UniversityGuideAcademicVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.3 学业规划类（@RequirePro） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideAcademicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> academicGuidance;
    private Map<String, Object> majorTransferGuidelines;
    private Map<String, Object> majorTransferConstriction;
    private Map<String, Object> academicSupportResources;
}
```

- [ ] **Step 6.4：`UniversityGuideSocialVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.4 社交融入类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideSocialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> studentOrganizations;
    private Map<String, Object> campusEvents;
    private Map<String, Object> classDormSocial;
}
```

- [ ] **Step 6.5：`UniversityGuideSafetyVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.5 权益与安全类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideSafetyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> financialAid;
    private Map<String, Object> campusSecurity;
    private Map<String, Object> healthServices;
}
```

- [ ] **Step 6.6：`UniversityGuideLifeVO`**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.6 周边生活类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideLifeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> lifeServices;
}
```

- [ ] **Step 6.7：编译并提交**

Run:
```bash
mvn -pl haifeng-app -am compile -q
```
Expected: `BUILD SUCCESS`

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityGuide*VO.java
git commit -m "feat(app/university): add 6 university guide section VOs"
```

---

### Task 7：Guide Service 6 方法 TDD

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityGuideService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityGuideServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/university/UniversityGuideServiceImplTest.java`

- [ ] **Step 7.1：写失败测试**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.impl.university.UniversityGuideServiceImpl;
import com.haifeng.app.vo.university.*;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityGuideServiceImplTest {

    @Mock private UniversityGuideMapper guideMapper;
    @Mock private UniversityMapper universityMapper;

    @InjectMocks private UniversityGuideServiceImpl service;

    private UniversityGuide sampleGuide() {
        return UniversityGuide.builder()
                .id(1L).universityId(100L).status((short) 1)
                .customTags(List.of("好食堂", "图书馆爆款"))
                .campusFacilities(Map.of("canteen", "5 个食堂"))
                .dormitoryServices(Map.of("ac", true))
                .campusTransportation(Map.of("shuttle", "校车"))
                .academicGuidance(Map.of("tutor", "导师制"))
                .majorTransferGuidelines(Map.of("rules", "..."))
                .majorTransferConstriction(Map.of("limit", "..."))
                .academicSupportResources(Map.of("library", "..."))
                .studentOrganizations(Map.of("clubs", 100))
                .campusEvents(Map.of("sports", "运动会"))
                .classDormSocial(Map.of("class", "..."))
                .financialAid(Map.of("scholarship", "..."))
                .campusSecurity(Map.of("guard", "24h"))
                .healthServices(Map.of("hospital", "校医院"))
                .lifeServices(Map.of("shop", "便利店"))
                .build();
    }

    @Test
    void overview_returnsTagsAndUniversityFields() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());
        University u = University.builder()
                .id(100L).name("清华").region("华北").category("综合")
                .nature("公办").imageUrl("img.png").tags(List.of("985"))
                .status((short) 1).build();
        when(universityMapper.selectById(100L)).thenReturn(u);

        UniversityGuideOverviewVO vo = service.overview(100L);

        assertThat(vo.getCustomTags()).contains("好食堂");
        assertThat(vo.getName()).isEqualTo("清华");
        assertThat(vo.getTags()).contains("985");
    }

    @Test
    void survival_returnsThreeSurvivalMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideSurvivalVO vo = service.survival(100L);

        assertThat(vo.getCampusFacilities()).containsEntry("canteen", "5 个食堂");
        assertThat(vo.getDormitoryServices()).containsEntry("ac", true);
        assertThat(vo.getCampusTransportation()).isNotNull();
    }

    @Test
    void academic_returnsFourAcademicMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideAcademicVO vo = service.academic(100L);

        assertThat(vo.getAcademicGuidance()).isNotNull();
        assertThat(vo.getMajorTransferGuidelines()).isNotNull();
        assertThat(vo.getMajorTransferConstriction()).isNotNull();
        assertThat(vo.getAcademicSupportResources()).isNotNull();
    }

    @Test
    void social_returnsThreeSocialMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideSocialVO vo = service.social(100L);

        assertThat(vo.getStudentOrganizations()).containsEntry("clubs", 100);
        assertThat(vo.getCampusEvents()).isNotNull();
        assertThat(vo.getClassDormSocial()).isNotNull();
    }

    @Test
    void safety_returnsThreeSafetyMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideSafetyVO vo = service.safety(100L);

        assertThat(vo.getFinancialAid()).isNotNull();
        assertThat(vo.getCampusSecurity()).containsEntry("guard", "24h");
        assertThat(vo.getHealthServices()).isNotNull();
    }

    @Test
    void life_returnsLifeServices() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideLifeVO vo = service.life(100L);

        assertThat(vo.getLifeServices()).containsEntry("shop", "便利店");
    }

    @Test
    void survival_guideMissing_throws404() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.survival(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校适应指南不存在");
    }

    @Test
    void overview_universityMissing_throws404() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());
        when(universityMapper.selectById(100L)).thenReturn(null);

        assertThatThrownBy(() -> service.overview(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校不存在");
    }
}
```

- [ ] **Step 7.2：Service 接口**

```java
package com.haifeng.app.service.university;

import com.haifeng.app.vo.university.*;

public interface UniversityGuideService {

    /** 概览：自定义标签 + 联院校简要字段 */
    UniversityGuideOverviewVO overview(Long universityId);

    /** 基础生存类 */
    UniversityGuideSurvivalVO survival(Long universityId);

    /** 学业规划类（需 Pro） */
    UniversityGuideAcademicVO academic(Long universityId);

    /** 社交融入类 */
    UniversityGuideSocialVO social(Long universityId);

    /** 权益与安全类 */
    UniversityGuideSafetyVO safety(Long universityId);

    /** 周边生活类 */
    UniversityGuideLifeVO life(Long universityId);
}
```

- [ ] **Step 7.3：跑测试确认失败**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=UniversityGuideServiceImplTest -q
```
Expected: 编译失败（Impl 未实现）。

- [ ] **Step 7.4：实现 `UniversityGuideServiceImpl`**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.university.UniversityGuideService;
import com.haifeng.app.vo.university.*;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityGuideServiceImpl implements UniversityGuideService {

    private static final short STATUS_PUBLISHED = 1;

    private final UniversityGuideMapper guideMapper;
    private final UniversityMapper universityMapper;

    @Override
    public UniversityGuideOverviewVO overview(Long universityId) {
        UniversityGuide guide = loadGuide(universityId);

        University univ = universityMapper.selectById(universityId);
        if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
            log.debug("院校不存在或已下架, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        return UniversityGuideOverviewVO.builder()
                .customTags(guide.getCustomTags())
                .name(univ.getName())
                .tags(univ.getTags())
                .region(univ.getRegion())
                .category(univ.getCategory())
                .nature(univ.getNature())
                .imageUrl(univ.getImageUrl())
                .build();
    }

    @Override
    public UniversityGuideSurvivalVO survival(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideSurvivalVO.builder()
                .campusFacilities(g.getCampusFacilities())
                .dormitoryServices(g.getDormitoryServices())
                .campusTransportation(g.getCampusTransportation())
                .build();
    }

    @Override
    public UniversityGuideAcademicVO academic(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideAcademicVO.builder()
                .academicGuidance(g.getAcademicGuidance())
                .majorTransferGuidelines(g.getMajorTransferGuidelines())
                .majorTransferConstriction(g.getMajorTransferConstriction())
                .academicSupportResources(g.getAcademicSupportResources())
                .build();
    }

    @Override
    public UniversityGuideSocialVO social(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideSocialVO.builder()
                .studentOrganizations(g.getStudentOrganizations())
                .campusEvents(g.getCampusEvents())
                .classDormSocial(g.getClassDormSocial())
                .build();
    }

    @Override
    public UniversityGuideSafetyVO safety(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideSafetyVO.builder()
                .financialAid(g.getFinancialAid())
                .campusSecurity(g.getCampusSecurity())
                .healthServices(g.getHealthServices())
                .build();
    }

    @Override
    public UniversityGuideLifeVO life(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideLifeVO.builder()
                .lifeServices(g.getLifeServices())
                .build();
    }

    /** 统一指南加载 + 校验，不存在抛 404 */
    private UniversityGuide loadGuide(Long universityId) {
        UniversityGuide g = guideMapper.selectOne(
                new LambdaQueryWrapper<UniversityGuide>()
                        .eq(UniversityGuide::getUniversityId, universityId)
                        .eq(UniversityGuide::getStatus, STATUS_PUBLISHED));
        if (g == null) {
            log.debug("院校适应指南不存在, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }
        return g;
    }
}
```

- [ ] **Step 7.5：跑测试通过**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=UniversityGuideServiceImplTest -q
```
Expected: `Tests run: 8, Failures: 0`

- [ ] **Step 7.6：提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityGuideService.java \
        haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityGuideServiceImpl.java \
        haifeng-app/src/test/java/com/haifeng/app/service/university/UniversityGuideServiceImplTest.java
git commit -m "feat(app/university): implement 6 university guide section services"
```

---

### Task 8：Guide Controller 6 子路径

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityGuideController.java`

- [ ] **Step 8.1：创建 Controller**

```java
package com.haifeng.app.controller.university;

import com.haifeng.app.service.university.UniversityGuideService;
import com.haifeng.app.vo.university.*;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端院校适应指南 - 6 个分类子路径
 * academic 需要 Pro，其余均需登录
 */
@RestController
@RequestMapping("/api/v1/app/university/guides")
@RequiredArgsConstructor
public class UniversityGuideController {

    private final UniversityGuideService guideService;

    @RequireLogin
    @GetMapping("/{universityId}/overview")
    public R<UniversityGuideOverviewVO> overview(@PathVariable Long universityId) {
        return R.ok(guideService.overview(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/survival")
    public R<UniversityGuideSurvivalVO> survival(@PathVariable Long universityId) {
        return R.ok(guideService.survival(universityId));
    }

    @RequirePro
    @GetMapping("/{universityId}/academic")
    public R<UniversityGuideAcademicVO> academic(@PathVariable Long universityId) {
        return R.ok(guideService.academic(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/social")
    public R<UniversityGuideSocialVO> social(@PathVariable Long universityId) {
        return R.ok(guideService.social(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/safety")
    public R<UniversityGuideSafetyVO> safety(@PathVariable Long universityId) {
        return R.ok(guideService.safety(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/life")
    public R<UniversityGuideLifeVO> life(@PathVariable Long universityId) {
        return R.ok(guideService.life(universityId));
    }
}
```

- [ ] **Step 8.2：编译并提交**

Run:
```bash
mvn -pl haifeng-app -am compile -q
```
Expected: `BUILD SUCCESS`

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityGuideController.java
git commit -m "feat(app/university): expose 6 guide endpoints, academic gated by RequirePro"
```

---

### Task 9：Gallery DTO + VO + Service TDD

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/university/CampusGalleryQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/CampusGalleryListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/university/CampusGalleryService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/CampusGalleryServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/university/CampusGalleryServiceImplTest.java`

- [ ] **Step 9.1：DTO**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端校园图册分页查询 DTO
 * universityId 在 path 上，imageType 可选精准匹配
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CampusGalleryQueryDTO extends BasePageQueryDTO {

    private String imageType;
}
```

- [ ] **Step 9.2：VO**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** 任务 4 校园图册列表 VO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusGalleryListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String imageType;
    private String imageUrl;
}
```

- [ ] **Step 9.3：失败测试**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.service.impl.university.CampusGalleryServiceImpl;
import com.haifeng.app.vo.university.CampusGalleryListVO;
import com.haifeng.common.entity.university.CampusGallery;
import com.haifeng.common.mapper.university.CampusGalleryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampusGalleryServiceImplTest {

    @Mock private CampusGalleryMapper galleryMapper;

    @InjectMocks private CampusGalleryServiceImpl service;

    @Test
    void page_returnsConvertedVOs() {
        CampusGallery e = CampusGallery.builder()
                .id(1L).universityId(100L).universityName("清华")
                .imageType("校门").imageUrl("https://x/y.jpg")
                .status((short) 1).build();
        Page<CampusGallery> p = new Page<>(1, 10);
        p.setRecords(List.of(e));
        p.setTotal(1);
        when(galleryMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(p);

        IPage<CampusGalleryListVO> result = service.page(100L, new CampusGalleryQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getImageType()).isEqualTo("校门");
        assertThat(result.getRecords().get(0).getImageUrl()).isEqualTo("https://x/y.jpg");
    }

    @Test
    void page_passesPageAndSizeFromDto() {
        CampusGalleryQueryDTO dto = new CampusGalleryQueryDTO();
        dto.setPage(2);
        dto.setSize(30);

        Page<CampusGallery> p = new Page<>(2, 30);
        p.setRecords(List.of());
        p.setTotal(0);

        ArgumentCaptor<Page> cap = ArgumentCaptor.forClass(Page.class);
        when(galleryMapper.selectPage(cap.capture(), any(Wrapper.class))).thenReturn(p);

        service.page(100L, dto);

        assertThat(cap.getValue().getCurrent()).isEqualTo(2L);
        assertThat(cap.getValue().getSize()).isEqualTo(30L);
    }
}
```

- [ ] **Step 9.4：Service 接口**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.vo.university.CampusGalleryListVO;

public interface CampusGalleryService {

    /**
     * 按 universityId 分页查询校园图册（仅 status=1）
     * imageType 可选精准匹配，排序 sort_order ASC, id DESC
     */
    IPage<CampusGalleryListVO> page(Long universityId, CampusGalleryQueryDTO dto);
}
```

- [ ] **Step 9.5：跑测试确认失败**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=CampusGalleryServiceImplTest -q
```
Expected: 编译失败。

- [ ] **Step 9.6：实现**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.service.university.CampusGalleryService;
import com.haifeng.app.vo.university.CampusGalleryListVO;
import com.haifeng.common.entity.university.CampusGallery;
import com.haifeng.common.mapper.university.CampusGalleryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusGalleryServiceImpl implements CampusGalleryService {

    private static final short STATUS_PUBLISHED = 1;

    private final CampusGalleryMapper galleryMapper;

    @Override
    public IPage<CampusGalleryListVO> page(Long universityId, CampusGalleryQueryDTO dto) {
        Page<CampusGallery> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<CampusGallery> wrapper = new LambdaQueryWrapper<CampusGallery>()
                .eq(CampusGallery::getUniversityId, universityId)
                .eq(CampusGallery::getStatus, STATUS_PUBLISHED)
                .eq(StringUtils.hasText(dto.getImageType()),
                        CampusGallery::getImageType, dto.getImageType())
                .orderByAsc(CampusGallery::getSortOrder)
                .orderByDesc(CampusGallery::getId);

        IPage<CampusGallery> entityPage = galleryMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    private CampusGalleryListVO toListVO(CampusGallery e) {
        return CampusGalleryListVO.builder()
                .imageType(e.getImageType())
                .imageUrl(e.getImageUrl())
                .build();
    }
}
```

- [ ] **Step 9.7：跑测试通过**

Run:
```bash
mvn -pl haifeng-app -am test -Dtest=CampusGalleryServiceImplTest -q
```
Expected: `Tests run: 2, Failures: 0`

- [ ] **Step 9.8：提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/university/CampusGalleryQueryDTO.java \
        haifeng-app/src/main/java/com/haifeng/app/vo/university/CampusGalleryListVO.java \
        haifeng-app/src/main/java/com/haifeng/app/service/university/CampusGalleryService.java \
        haifeng-app/src/main/java/com/haifeng/app/service/impl/university/CampusGalleryServiceImpl.java \
        haifeng-app/src/test/java/com/haifeng/app/service/university/CampusGalleryServiceImplTest.java
git commit -m "feat(app/university): implement campus gallery paginated query by university"
```

---

### Task 10：Gallery Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/university/CampusGalleryController.java`

- [ ] **Step 10.1：创建 Controller**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.service.university.CampusGalleryService;
import com.haifeng.app.vo.university.CampusGalleryListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端校园图册（任务 4），按院校分页查询，需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class CampusGalleryController {

    private final CampusGalleryService galleryService;

    @RequireLogin
    @GetMapping("/{universityId}/gallery")
    public R<IPage<CampusGalleryListVO>> gallery(
            @PathVariable Long universityId,
            @Valid CampusGalleryQueryDTO dto) {
        return R.ok(galleryService.page(universityId, dto));
    }
}
```

- [ ] **Step 10.2：编译并提交**

Run:
```bash
mvn -pl haifeng-app -am compile -q
```
Expected: `BUILD SUCCESS`

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/university/CampusGalleryController.java
git commit -m "feat(app/university): expose GET /university/{id}/gallery (RequireLogin)"
```

---

### Task 11：全模块编译 + 测试一次性验收

- [ ] **Step 11.1：编译整个 app 模块**

Run:
```bash
mvn -pl haifeng-app -am clean compile -q
```
Expected: `BUILD SUCCESS`，无任何 warning/error

- [ ] **Step 11.2：跑 university 包下全部测试**

Run:
```bash
mvn -pl haifeng-app test -Dtest='com.haifeng.app.service.university.*Test' -q
```
Expected: 输出含 `Tests run: 16, Failures: 0, Errors: 0`（2 + 4 + 8 + 2）

- [ ] **Step 11.3：跑 home 包旧测试，确认未破坏**

Run:
```bash
mvn -pl haifeng-app test -Dtest='com.haifeng.app.service.home.*Test' -q
```
Expected: 全绿，未引入回归

- [ ] **Step 11.4：路径自检（无独立测试，由代码 review 完成）**

人工对照下表，确保所有路径与 spec 一致：

| 端点 | 权限 |
|---|---|
| `GET /api/v1/app/university/list` | 公开 |
| `GET /api/v1/app/university/{id}/detail` | RequireLogin |
| `GET /api/v1/app/university/guides/{id}/overview` | RequireLogin |
| `GET /api/v1/app/university/guides/{id}/survival` | RequireLogin |
| `GET /api/v1/app/university/guides/{id}/academic` | RequirePro |
| `GET /api/v1/app/university/guides/{id}/social` | RequireLogin |
| `GET /api/v1/app/university/guides/{id}/safety` | RequireLogin |
| `GET /api/v1/app/university/guides/{id}/life` | RequireLogin |
| `GET /api/v1/app/university/{id}/gallery` | RequireLogin |

- [ ] **Step 11.5：可选 — 整库 verify**

Run:
```bash
mvn -pl haifeng-app -am verify -DskipITs -q
```
Expected: `BUILD SUCCESS`

---

## Self-Review

**1. Spec 覆盖检查**
- 任务 1 列表（公开 + 名称模糊 + 7 精准筛选） → Task 1–3 ✅
- 任务 2 详情（@RequireLogin，联表 university + detail） → Task 4–5 ✅
- 任务 3 指南 6 子接口（academic = @RequirePro） → Task 6–8 ✅
- 任务 4 图册（@RequireLogin，按院校 + imageType） → Task 9–10 ✅

**2. 占位符扫描**
- 无 TBD/TODO；所有 step 都给了完整代码或确切命令；测试用例为可运行真代码。

**3. 类型/签名一致性**
- `UniversityService.page` / `.detail` → Controller 调用一致
- `UniversityGuideService` 6 个方法签名 → Controller 6 个调用一致
- `CampusGalleryService.page(Long, CampusGalleryQueryDTO)` → Controller 调用一致
- VO Builder 字段名与 Entity getter 名一一对应（已对照 entity 文件）

**4. 修正项**
- Task 4 Step 4.5 中 mapper 字段命名 review 后确认与 import 简化方案选了 `UniversityDetailMapper`，与全 service 风格一致。

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-06-03-app-university-management.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
