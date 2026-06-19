# 统一岗位搜索 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement unified job search for the App end — paginated query and detail APIs based on `t_job_index` table.

**Architecture:** Create entity + mapper in `haifeng-common`, then DTO/VO/Service/Controller in `haifeng-app`, following the existing City module pattern. Use MyBatis-Plus LambdaQueryWrapper for dynamic conditions.

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, JUnit 5 + Mockito, Maven

---

### Task 1: Entity + Mapper (haifeng-common)

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/jobIndex/JobIndex.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/jobIndex/JobIndexMapper.java`

- [ ] **Step 1: Create JobIndex entity**

Create file: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/jobIndex/JobIndex.java`

```java
package com.haifeng.common.entity.employment.jobIndex;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_job_index", autoResultMap = true)
public class JobIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceType;

    private Long sourceId;

    private String categoryLabel;

    private String positionName;

    private String organizationName;

    private String organizationLogo;

    private String province;

    private String city;

    private String educationRequirement;

    private Integer recruitmentCount;

    private String recruitmentType;

    private Integer salaryMin;

    private Integer salaryMax;

    private String salaryText;

    private OffsetDateTime publishDate;

    private OffsetDateTime regDeadline;

    private Boolean isHot;

    private Integer viewCount;

    private Integer applyCount;

    private String positionStatus;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: Create JobIndexMapper**

Create file: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/jobIndex/JobIndexMapper.java`

```java
package com.haifeng.common.mapper.employment.jobIndex;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.jobIndex.JobIndex;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobIndexMapper extends BaseMapper<JobIndex> {

}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl haifeng-common -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/employment/jobIndex/JobIndex.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/employment/jobIndex/JobIndexMapper.java
git commit -m "feat(job-index): add entity and mapper"
```

---

### Task 2: DTO + VOs (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/jobIndex/JobSearchDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/jobIndex/JobIndexListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/jobIndex/JobIndexDetailVO.java`

- [ ] **Step 1: Create JobSearchDTO**

Create file: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/jobIndex/JobSearchDTO.java`

```java
package com.haifeng.app.dto.employment.jobIndex;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class JobSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String province;

    private String city;

    private String educationRequirement;

    private String recruitmentType;

    private Integer salaryMin;

    private Integer salaryMax;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDateStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDateEnd;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate regDeadlineStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate regDeadlineEnd;

    private String positionStatus;
}
```

- [ ] **Step 2: Create JobIndexListVO**

Create file: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/jobIndex/JobIndexListVO.java`

```java
package com.haifeng.app.vo.employment.jobIndex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobIndexListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String categoryLabel;

    private String positionName;

    private String organizationName;

    private String city;

    private String educationRequirement;

    private String recruitmentType;

    private String salaryText;

    private String positionStatus;
}
```

- [ ] **Step 3: Create JobIndexDetailVO**

Create file: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/jobIndex/JobIndexDetailVO.java`

```java
package com.haifeng.app.vo.employment.jobIndex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobIndexDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String sourceType;

    private Long sourceId;

    private String categoryLabel;

    private String positionName;

    private String organizationName;

    private String organizationLogo;

    private String province;

    private String city;

    private String educationRequirement;

    private Integer recruitmentCount;

    private String recruitmentType;

    private Integer salaryMin;

    private Integer salaryMax;

    private String salaryText;

    private String positionStatus;

    private OffsetDateTime publishDate;

    private OffsetDateTime regDeadline;

    private Boolean isHot;

    private Integer viewCount;

    private Integer applyCount;
}
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/employment/jobIndex/JobSearchDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/employment/jobIndex/JobIndexListVO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/employment/jobIndex/JobIndexDetailVO.java
git commit -m "feat(job-index): add DTO and VOs"
```

---

### Task 3: Service Interface + Implementation + Tests

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/employment/jobIndex/JobIndexService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/jobIndex/JobIndexServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/employment/jobIndex/JobIndexServiceImplTest.java`

- [ ] **Step 1: Create JobIndexService interface**

Create file: `haifeng-app/src/main/java/com/haifeng/app/service/employment/jobIndex/JobIndexService.java`

```java
package com.haifeng.app.service.employment.jobIndex;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;

public interface JobIndexService {

    IPage<JobIndexListVO> page(JobSearchDTO dto);

    JobIndexDetailVO detail(Long id);
}
```

- [ ] **Step 2: Write the failing test**

Create file: `haifeng-app/src/test/java/com/haifeng/app/service/employment/jobIndex/JobIndexServiceImplTest.java`

```java
package com.haifeng.app.service.employment.jobIndex;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.service.impl.employment.jobIndex.JobIndexServiceImpl;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;
import com.haifeng.common.entity.employment.jobIndex.JobIndex;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.jobIndex.JobIndexMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobIndexServiceImplTest {

    @Mock
    private JobIndexMapper jobIndexMapper;

    @InjectMocks
    private JobIndexServiceImpl service;

    @Captor
    private ArgumentCaptor<LambdaQueryWrapper<JobIndex>> wrapperCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, JobIndex.class);
    }

    @Test
    void page_withKeyword_buildsFuzzyCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setKeyword("工程师");
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        String sql = wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).containsIgnoringCase("position_name");
        assertThat(sql).containsIgnoringCase("organization_name");
        assertThat(sql).containsIgnoringCase("LIKE");
        assertThat(sql).contains("is_deleted");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void page_withProvince_buildsExactCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setProvince("广东省");
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        assertThat(wrapperCaptor.getValue().getCustomSqlSegment()).contains("province");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void page_withSalaryRange_buildsOverlapCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setSalaryMin(10);
        dto.setSalaryMax(30);
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        String sql = wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).contains("salary_max");
        assertThat(sql).contains("salary_min");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void detail_notFound_throws404() {
        when(jobIndexMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = catchThrowableOfType(
                () -> service.detail(999L), BusinessException.class);

        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        verify(jobIndexMapper).selectById(999L);
    }

    @Test
    void detail_found_returnsVO() {
        JobIndex entity = JobIndex.builder()
                .id(1L)
                .sourceType("civil")
                .sourceId(100L)
                .categoryLabel("公务员")
                .positionName("一级科员")
                .organizationName("某单位")
                .province("广东省")
                .city("广州市")
                .educationRequirement("本科")
                .recruitmentCount(2)
                .recruitmentType("国考")
                .salaryMin(8)
                .salaryMax(15)
                .salaryText("8k-15k")
                .positionStatus("招聘中")
                .publishDate(OffsetDateTime.now())
                .isHot(true)
                .viewCount(1000)
                .applyCount(50)
                .isDeleted(false)
                .build();
        when(jobIndexMapper.selectById(1L)).thenReturn(entity);

        JobIndexDetailVO vo = service.detail(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getSourceType()).isEqualTo("civil");
        assertThat(vo.getCategoryLabel()).isEqualTo("公务员");
        assertThat(vo.getPositionStatus()).isEqualTo("招聘中");
        verify(jobIndexMapper).selectById(1L);
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn test -pl haifeng-app -am -Dtest=JobIndexServiceImplTest -q`
Expected: Compilation error or test failure (service not yet implemented)

- [ ] **Step 4: Create JobIndexServiceImpl**

Create file: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/jobIndex/JobIndexServiceImpl.java`

```java
package com.haifeng.app.service.impl.employment.jobIndex;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.service.employment.jobIndex.JobIndexService;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;
import com.haifeng.common.entity.employment.jobIndex.JobIndex;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.jobIndex.JobIndexMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobIndexServiceImpl implements JobIndexService {

    private final JobIndexMapper jobIndexMapper;

    @Override
    public IPage<JobIndexListVO> page(JobSearchDTO dto) {
        LambdaQueryWrapper<JobIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobIndex::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(JobIndex::getPositionName, dto.getKeyword())
                    .or()
                    .like(JobIndex::getOrganizationName, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), JobIndex::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), JobIndex::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), JobIndex::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getRecruitmentType()), JobIndex::getRecruitmentType, dto.getRecruitmentType());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), JobIndex::getPositionStatus, dto.getPositionStatus());

        if (dto.getSalaryMin() != null) {
            wrapper.ge(JobIndex::getSalaryMax, dto.getSalaryMin());
        }
        if (dto.getSalaryMax() != null) {
            wrapper.le(JobIndex::getSalaryMin, dto.getSalaryMax());
        }

        if (dto.getPublishDateStart() != null) {
            wrapper.ge(JobIndex::getPublishDate, toStartOfDay(dto.getPublishDateStart()));
        }
        if (dto.getPublishDateEnd() != null) {
            wrapper.le(JobIndex::getPublishDate, toEndOfDay(dto.getPublishDateEnd()));
        }
        if (dto.getRegDeadlineStart() != null) {
            wrapper.ge(JobIndex::getRegDeadline, toStartOfDay(dto.getRegDeadlineStart()));
        }
        if (dto.getRegDeadlineEnd() != null) {
            wrapper.le(JobIndex::getRegDeadline, toEndOfDay(dto.getRegDeadlineEnd()));
        }

        wrapper.orderByDesc(JobIndex::getPublishDate);

        Page<JobIndex> page = new Page<>(dto.getPage(), dto.getSize());
        jobIndexMapper.selectPage(page, wrapper);

        return page.convert(job -> JobIndexListVO.builder()
                .id(job.getId())
                .categoryLabel(job.getCategoryLabel())
                .positionName(job.getPositionName())
                .organizationName(job.getOrganizationName())
                .city(job.getCity())
                .educationRequirement(job.getEducationRequirement())
                .recruitmentType(job.getRecruitmentType())
                .salaryText(job.getSalaryText())
                .positionStatus(job.getPositionStatus())
                .build());
    }

    @Override
    public JobIndexDetailVO detail(Long id) {
        JobIndex job = jobIndexMapper.selectById(id);
        if (job == null || Boolean.TRUE.equals(job.getIsDeleted())) {
            log.warn("岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return JobIndexDetailVO.builder()
                .id(job.getId())
                .sourceType(job.getSourceType())
                .sourceId(job.getSourceId())
                .categoryLabel(job.getCategoryLabel())
                .positionName(job.getPositionName())
                .organizationName(job.getOrganizationName())
                .organizationLogo(job.getOrganizationLogo())
                .province(job.getProvince())
                .city(job.getCity())
                .educationRequirement(job.getEducationRequirement())
                .recruitmentCount(job.getRecruitmentCount())
                .recruitmentType(job.getRecruitmentType())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryText(job.getSalaryText())
                .positionStatus(job.getPositionStatus())
                .publishDate(job.getPublishDate())
                .regDeadline(job.getRegDeadline())
                .isHot(job.getIsHot())
                .viewCount(job.getViewCount())
                .applyCount(job.getApplyCount())
                .build();
    }

    private static OffsetDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    }

    private static OffsetDateTime toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `mvn test -pl haifeng-app -am -Dtest=JobIndexServiceImplTest -q`
Expected: BUILD SUCCESS, all 5 tests pass

- [ ] **Step 6: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/employment/jobIndex/JobIndexService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/jobIndex/JobIndexServiceImpl.java
git add haifeng-app/src/test/java/com/haifeng/app/service/employment/jobIndex/JobIndexServiceImplTest.java
git commit -m "feat(job-index): add service with search and detail logic"
```

---

### Task 4: Controller + Annotation Test

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/jobIndex/JobIndexController.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/controller/employment/jobIndex/JobIndexControllerAnnotationTest.java`

- [ ] **Step 1: Create JobIndexController**

Create file: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/jobIndex/JobIndexController.java`

```java
package com.haifeng.app.controller.employment.jobIndex;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.service.employment.jobIndex.JobIndexService;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/employment/job")
@RequiredArgsConstructor
public class JobIndexController {

    private final JobIndexService jobIndexService;

    @GetMapping("/list")
    public R<IPage<JobIndexListVO>> list(@Valid JobSearchDTO dto) {
        return R.ok(jobIndexService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<JobIndexDetailVO> detail(@PathVariable Long id) {
        return R.ok(jobIndexService.detail(id));
    }
}
```

- [ ] **Step 2: Write the annotation test**

Create file: `haifeng-app/src/test/java/com/haifeng/app/controller/employment/jobIndex/JobIndexControllerAnnotationTest.java`

```java
package com.haifeng.app.controller.employment.jobIndex;

import com.haifeng.common.annotation.RequireLogin;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class JobIndexControllerAnnotationTest {

    @Test
    void listMethod_shouldBePublic() throws Exception {
        Method method = JobIndexController.class.getMethod("list", com.haifeng.app.dto.employment.jobIndex.JobSearchDTO.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/list");
        assertThat(method.getAnnotation(RequireLogin.class)).isNull();
    }

    @Test
    void detailMethod_shouldRequireLogin() throws Exception {
        Method method = JobIndexController.class.getMethod("detail", Long.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/{id}/detail");
        assertThat(method.getAnnotation(RequireLogin.class)).isNotNull();
    }

    @Test
    void controller_shouldHaveRestControllerAndRequestMapping() {
        RestController restCtrl = JobIndexController.class.getAnnotation(RestController.class);
        assertThat(restCtrl).isNotNull();
        RequestMapping mapping = JobIndexController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).contains("/api/v1/app/employment/job");
    }
}
```

- [ ] **Step 3: Run tests to verify they pass**

Run: `mvn test -pl haifeng-app -am -Dtest=JobIndexControllerAnnotationTest,JobIndexServiceImplTest -q`
Expected: BUILD SUCCESS, all 8 tests pass

- [ ] **Step 4: Verify full compilation**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/employment/jobIndex/JobIndexController.java
git add haifeng-app/src/test/java/com/haifeng/app/controller/employment/jobIndex/JobIndexControllerAnnotationTest.java
git commit -m "feat(job-index): add controller with list and detail endpoints"
```
