# App端录取专业组查询 实现计划（任务6）

> **For agentic workers:** 任务独立，按顺序执行。

**Goal:** 为App端实现录取专业组（t_admission_group）及专业录取明细（t_admission_major_score）的3个查询接口，要求VIP权限。

**架构:** 复用common已有entity/mapper（algorithm包），在haifeng-app/university包下新增DTO/VO/Service/Controller。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, LambdaQueryWrapper

---

### Task 1: DTO 和 VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/university/AdmissionGroupQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/AdmissionGroupListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/AdmissionGroupDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/AdmissionMajorScoreListVO.java`

- [ ] **Step 1: Create AdmissionGroupQueryDTO**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {
    private String province;
    private String batch;
    private String cityName;
}
```

- [ ] **Step 2: Create AdmissionGroupListVO**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionGroupListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String groupCode;
    private String groupName;
    private Short year;
    private String province;
    private String batch;
    private String cityName;
    private List<String> subjects;
    private String requirementType;
    private Integer majorCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private Integer maxScore;
    private Integer maxRank;
    private BigDecimal avgScore;
    private Integer avgRank;
}
```

- [ ] **Step 3: Create AdmissionGroupDetailVO**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionGroupDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Long universityId;
    private String universityName;
    private String cityName;
    private Short year;
    private String province;
    private String batch;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private List<String> subjects;
    private String requirementType;
    private String description;
    private List<String> constraints;
    private Integer majorCount;
    private Integer categoryCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private Integer maxScore;
    private Integer maxRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: Create AdmissionMajorScoreListVO**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionMajorScoreListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer groupId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private Integer maxScore;
    private Integer maxRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private List<String> constraints;
}
```

### Task 2: Service 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/university/AdmissionGroupService.java`

- [ ] **Step 1: Create AdmissionGroupService interface**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.AdmissionGroupQueryDTO;
import com.haifeng.app.vo.university.AdmissionGroupDetailVO;
import com.haifeng.app.vo.university.AdmissionGroupListVO;
import com.haifeng.app.vo.university.AdmissionMajorScoreListVO;

import java.util.List;

public interface AdmissionGroupService {

    IPage<AdmissionGroupListVO> pageByUniversity(Long universityId, AdmissionGroupQueryDTO dto);

    AdmissionGroupDetailVO getDetail(Integer groupId);

    List<AdmissionMajorScoreListVO> listScores(Integer groupId);
}
```

### Task 3: Service 实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/AdmissionGroupServiceImpl.java`

- [ ] **Step 1: Create AdmissionGroupServiceImpl**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.AdmissionGroupQueryDTO;
import com.haifeng.app.service.university.AdmissionGroupService;
import com.haifeng.app.vo.university.AdmissionGroupDetailVO;
import com.haifeng.app.vo.university.AdmissionGroupListVO;
import com.haifeng.app.vo.university.AdmissionMajorScoreListVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionGroupServiceImpl implements AdmissionGroupService {

    private final AdmissionGroupMapper groupMapper;
    private final AdmissionMajorScoreMapper majorScoreMapper;

    @Override
    public IPage<AdmissionGroupListVO> pageByUniversity(Long universityId, AdmissionGroupQueryDTO dto) {
        if (!ProvinceEnum.isValid(dto.getProvince())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "省份参数不合法");
        }

        int currentYear = OffsetDateTime.now().getYear();
        short minYear = (short) (currentYear - 5);

        Page<AdmissionGroup> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdmissionGroup> wrapper = new LambdaQueryWrapper<AdmissionGroup>()
                .eq(AdmissionGroup::getUniversityId, universityId)
                .eq(AdmissionGroup::getIsDeleted, false)
                .ge(AdmissionGroup::getYear, minYear)
                .eq(StringUtils.hasText(dto.getProvince()),
                        AdmissionGroup::getProvince, dto.getProvince())
                .eq(StringUtils.hasText(dto.getBatch()),
                        AdmissionGroup::getBatch, dto.getBatch())
                .like(StringUtils.hasText(dto.getCityName()),
                        AdmissionGroup::getCityName, dto.getCityName())
                .orderByDesc(AdmissionGroup::getYear)
                .orderByAsc(AdmissionGroup::getId);

        IPage<AdmissionGroup> entityPage = groupMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public AdmissionGroupDetailVO getDetail(Integer groupId) {
        AdmissionGroup entity = groupMapper.selectById(groupId);
        if (entity == null || entity.getIsDeleted()) {
            log.debug("录取专业组不存在或已删除, groupId={}", groupId);
            throw new BusinessException(ResultCode.NOT_FOUND, "录取专业组不存在");
        }
        return toDetailVO(entity);
    }

    @Override
    public List<AdmissionMajorScoreListVO> listScores(Integer groupId) {
        List<AdmissionMajorScore> list = majorScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionMajorScore>()
                        .eq(AdmissionMajorScore::getGroupId, groupId)
                        .eq(AdmissionMajorScore::getIsDeleted, false));
        return list.stream().map(this::toScoreVO).collect(Collectors.toList());
    }

    private AdmissionGroupListVO toListVO(AdmissionGroup e) {
        return AdmissionGroupListVO.builder()
                .id(e.getId())
                .groupCode(e.getGroupCode())
                .groupName(e.getGroupName())
                .year(e.getYear())
                .province(e.getProvince())
                .batch(e.getBatch())
                .cityName(e.getCityName())
                .subjects(e.getSubjects())
                .requirementType(e.getRequirementType())
                .majorCount(e.getMajorCount())
                .admissionCount(e.getAdmissionCount())
                .minScore(e.getMinScore())
                .minRank(e.getMinRank())
                .maxScore(e.getMaxScore())
                .maxRank(e.getMaxRank())
                .avgScore(e.getAvgScore())
                .avgRank(e.getAvgRank())
                .build();
    }

    private AdmissionGroupDetailVO toDetailVO(AdmissionGroup e) {
        return AdmissionGroupDetailVO.builder()
                .id(e.getId())
                .universityId(e.getUniversityId())
                .universityName(e.getUniversityName())
                .cityName(e.getCityName())
                .year(e.getYear())
                .province(e.getProvince())
                .batch(e.getBatch())
                .enrollmentCode(e.getEnrollmentCode())
                .groupCode(e.getGroupCode())
                .groupName(e.getGroupName())
                .subjects(e.getSubjects())
                .requirementType(e.getRequirementType())
                .description(e.getDescription())
                .constraints(e.getConstraints())
                .majorCount(e.getMajorCount())
                .categoryCount(e.getCategoryCount())
                .admissionCount(e.getAdmissionCount())
                .minScore(e.getMinScore())
                .minRank(e.getMinRank())
                .maxScore(e.getMaxScore())
                .maxRank(e.getMaxRank())
                .avgScore(e.getAvgScore())
                .avgRank(e.getAvgRank())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private AdmissionMajorScoreListVO toScoreVO(AdmissionMajorScore e) {
        return AdmissionMajorScoreListVO.builder()
                .id(e.getId())
                .groupId(e.getGroupId())
                .majorCode(e.getMajorCode())
                .majorName(e.getMajorName())
                .educationLevel(e.getEducationLevel())
                .duration(e.getDuration())
                .tuition(e.getTuition())
                .description(e.getDescription())
                .admissionCount(e.getAdmissionCount())
                .minScore(e.getMinScore())
                .minRank(e.getMinRank())
                .maxScore(e.getMaxScore())
                .maxRank(e.getMaxRank())
                .avgScore(e.getAvgScore())
                .avgRank(e.getAvgRank())
                .constraints(e.getConstraints())
                .build();
    }
}
```

### Task 4: Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/university/AdmissionGroupController.java`

- [ ] **Step 1: Create AdmissionGroupController**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.AdmissionGroupQueryDTO;
import com.haifeng.app.service.university.AdmissionGroupService;
import com.haifeng.app.vo.university.AdmissionGroupDetailVO;
import com.haifeng.app.vo.university.AdmissionGroupListVO;
import com.haifeng.app.vo.university.AdmissionMajorScoreListVO;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/university/admission-group")
@RequiredArgsConstructor
public class AdmissionGroupController {

    private final AdmissionGroupService admissionGroupService;

    @RequireVip
    @GetMapping("/{universityId}")
    public R<IPage<AdmissionGroupListVO>> page(
            @PathVariable Long universityId,
            @Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionGroupService.pageByUniversity(universityId, dto));
    }

    @RequireVip
    @GetMapping("/{groupId}/scores")
    public R<List<AdmissionMajorScoreListVO>> scores(@PathVariable Integer groupId) {
        return R.ok(admissionGroupService.listScores(groupId));
    }

    @RequireVip
    @GetMapping("/{groupId}/detail")
    public R<AdmissionGroupDetailVO> detail(@PathVariable Integer groupId) {
        return R.ok(admissionGroupService.getDetail(groupId));
    }
}
```

### Task 5: 更新API文档

检查现有API文档格式并更新（具体操作待确认）。
