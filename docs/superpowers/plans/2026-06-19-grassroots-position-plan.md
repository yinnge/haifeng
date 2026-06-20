# 基层服务岗位模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement app-side query APIs for 3 grassroots position tables (基层服务项目/社区工作者/公益性岗位)

**Architecture:** 3 entities + 3 mappers in haifeng-common; DTOs/VOs/Services/Controllers in haifeng-app, following existing `employment.jobIndex` pattern.

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL

---

### Task 1: Entity - GrassrootsProjectPosition (haifeng-common)

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/grassrootsPosition/GrassrootsProjectPosition.java`

- [ ] **Create GrassrootsProjectPosition entity**

```java
package com.haifeng.common.entity.employment.grassrootsPosition;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName(value = "t_grassroots_project_position", autoResultMap = true)
public class GrassrootsProjectPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String serviceStartDate;
    private String serviceEndDate;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String householdRequirement;
    private String otherRequirement;
    private String politicalStatus;
    private String examContent;
    private OffsetDateTime examTime;
    private String interviewForm;
    private String monthlySubsidy;
    private String socialInsurance;
    private String housingInfo;
    private String otherBenefits;
    private String afterServicePolicy;
    private Boolean canTransferToCivil;
    private Boolean canTransferToInstitution;
    private String examBonusPoints;
    private String tuitionCompensation;
    private String postgradBonus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyLink;
    private String positionStatus;
    private String contactPhone;
    private String remark;
    private String content;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

---

### Task 2: Entity - CommunityPosition (haifeng-common)

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/grassrootsPosition/CommunityPosition.java`

- [ ] **Create CommunityPosition entity**

```java
package com.haifeng.common.entity.employment.grassrootsPosition;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName(value = "t_community_position", autoResultMap = true)
public class CommunityPosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String streetOffice;
    private String communityName;
    private String supervisingDept;
    private String district;
    private String positionName;
    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String workExperience;
    private String socialWorkCert;
    private String communityExperience;
    private String residenceRequirement;
    private String salaryRange;
    private String salaryComposition;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String applyMethod;
    private String contactPhone;
    private String contactAddress;
    private String remark;
    private String content;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

---

### Task 3: Entity - PublicWelfarePosition (haifeng-common)

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/grassrootsPosition/PublicWelfarePosition.java`

- [ ] **Create PublicWelfarePosition entity**

```java
package com.haifeng.common.entity.employment.grassrootsPosition;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
@TableName(value = "t_public_welfare_position", autoResultMap = true)
public class PublicWelfarePosition implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String workContent;
    private String province;
    private String city;
    private String district;
    private String workLocation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] targetGroup;

    private String educationRequirement;
    private String ageRange;
    private String healthRequirement;
    private Integer recruitmentCount;
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    private String monthlySalary;
    private String salarySource;
    private String subsidyStandard;
    private String socialInsuranceInfo;
    private String otherBenefits;
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    private String applyAddress;
    private String requiredDocuments;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

---

### Task 4: Mapper - All 3 (haifeng-common)

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/grassrootsPosition/GrassrootsProjectPositionMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/grassrootsPosition/CommunityPositionMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/grassrootsPosition/PublicWelfarePositionMapper.java`

- [ ] **Create GrassrootsProjectPositionMapper**

```java
package com.haifeng.common.mapper.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.grassrootsPosition.GrassrootsProjectPosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GrassrootsProjectPositionMapper extends BaseMapper<GrassrootsProjectPosition> {
}
```

- [ ] **Create CommunityPositionMapper**

```java
package com.haifeng.common.mapper.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.grassrootsPosition.CommunityPosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommunityPositionMapper extends BaseMapper<CommunityPosition> {
}
```

- [ ] **Create PublicWelfarePositionMapper**

```java
package com.haifeng.common.mapper.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.grassrootsPosition.PublicWelfarePosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PublicWelfarePositionMapper extends BaseMapper<PublicWelfarePosition> {
}
```

---

### Task 5: DTOs and VOs - GrassrootsProjectPosition (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/grassrootsPosition/GrassrootsProjectPositionSearchDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/grassrootsPosition/GrassrootsProjectPositionListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/grassrootsPosition/GrassrootsProjectPositionDetailVO.java`

- [ ] **Create SearchDTO**

```java
package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GrassrootsProjectPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String projectType;
    private String year;
    private String serviceType;
    private String province;
    private String city;
    private String county;
    private String township;
    private String educationRequirement;
    private String majorRequirement;
    private String gradYearRequirement;
    private String politicalStatus;
    private String positionStatus;
}
```

- [ ] **Create ListVO**

```java
package com.haifeng.app.vo.employment.grassrootsPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrassrootsProjectPositionListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String positionStatus;
    private String politicalStatus;
}
```

- [ ] **Create DetailVO**

```java
package com.haifeng.app.vo.employment.grassrootsPosition;

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
public class GrassrootsProjectPositionDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String serviceStartDate;
    private String serviceEndDate;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String householdRequirement;
    private String otherRequirement;
    private String politicalStatus;
    private String examContent;
    private OffsetDateTime examTime;
    private String interviewForm;
    private String monthlySubsidy;
    private String socialInsurance;
    private String housingInfo;
    private String otherBenefits;
    private String afterServicePolicy;
    private Boolean canTransferToCivil;
    private Boolean canTransferToInstitution;
    private String examBonusPoints;
    private String tuitionCompensation;
    private String postgradBonus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyLink;
    private String positionStatus;
    private String contactPhone;
    private String remark;
    private String content;
}
```

---

### Task 6: DTOs and VOs - CommunityPosition (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/grassrootsPosition/CommunityPositionSearchDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/grassrootsPosition/CommunityPositionListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/grassrootsPosition/CommunityPositionDetailVO.java`

- [ ] **Create SearchDTO**

```java
package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String educationRequirement;
    private String politicalStatus;
    private String workExperience;
    private String positionStatus;
}
```

- [ ] **Create ListVO**

```java
package com.haifeng.app.vo.employment.grassrootsPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPositionListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String communityName;
    private String district;
    private String positionName;
    private String positionType;
    private String province;
    private String city;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String workExperience;
    private String positionStatus;
}
```

- [ ] **Create DetailVO**

```java
package com.haifeng.app.vo.employment.grassrootsPosition;

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
public class CommunityPositionDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String streetOffice;
    private String communityName;
    private String supervisingDept;
    private String district;
    private String positionName;
    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String workExperience;
    private String socialWorkCert;
    private String communityExperience;
    private String residenceRequirement;
    private String salaryRange;
    private String salaryComposition;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String applyMethod;
    private String contactPhone;
    private String contactAddress;
    private String remark;
    private String content;
}
```

---

### Task 7: DTOs and VOs - PublicWelfarePosition (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/grassrootsPosition/PublicWelfarePositionSearchDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/grassrootsPosition/PublicWelfarePositionListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/grassrootsPosition/PublicWelfarePositionDetailVO.java`

- [ ] **Create SearchDTO**

```java
package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicWelfarePositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private String positionStatus;
    private String targetGroup;
}
```

- [ ] **Create ListVO**

```java
package com.haifeng.app.vo.employment.grassrootsPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicWelfarePositionListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private Integer recruitmentCount;
    private String monthlySalary;
    private String householdRequirement;
    private String contractPeriod;
}
```

- [ ] **Create DetailVO**

```java
package com.haifeng.app.vo.employment.grassrootsPosition;

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
public class PublicWelfarePositionDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String workContent;
    private String province;
    private String city;
    private String district;
    private String workLocation;
    private String[] targetGroup;
    private String educationRequirement;
    private String ageRange;
    private String healthRequirement;
    private Integer recruitmentCount;
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    private String monthlySalary;
    private String salarySource;
    private String subsidyStandard;
    private String socialInsuranceInfo;
    private String otherBenefits;
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    private String applyAddress;
    private String requiredDocuments;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;
}
```

---

### Task 8: Service + Controller - GrassrootsProjectPosition (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/employment/grassrootsPosition/GrassrootsProjectPositionService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/grassrootsPosition/GrassrootsProjectPositionServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/grassrootsPosition/GrassrootsProjectPositionController.java`

- [ ] **Create Service interface**

```java
package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;

public interface GrassrootsProjectPositionService {
    IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionSearchDTO dto);
    GrassrootsProjectPositionDetailVO detail(Long id);
}
```

- [ ] **Create ServiceImpl**

```java
package com.haifeng.app.service.impl.employment.grassrootsPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.GrassrootsProjectPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.GrassrootsProjectPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrassrootsProjectPositionServiceImpl implements GrassrootsProjectPositionService {

    private final GrassrootsProjectPositionMapper grassrootsProjectPositionMapper;

    @Override
    public IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionSearchDTO dto) {
        LambdaQueryWrapper<GrassrootsProjectPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrassrootsProjectPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(GrassrootsProjectPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(GrassrootsProjectPosition::getOrganizingDept, dto.getKeyword())
                    .or()
                    .like(GrassrootsProjectPosition::getServiceUnit, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getProjectType()), GrassrootsProjectPosition::getProjectType, dto.getProjectType());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), GrassrootsProjectPosition::getYear, dto.getYear());
        wrapper.eq(StrUtil.isNotBlank(dto.getServiceType()), GrassrootsProjectPosition::getServiceType, dto.getServiceType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), GrassrootsProjectPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), GrassrootsProjectPosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getCounty()), GrassrootsProjectPosition::getCounty, dto.getCounty());
        wrapper.eq(StrUtil.isNotBlank(dto.getTownship()), GrassrootsProjectPosition::getTownship, dto.getTownship());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), GrassrootsProjectPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), GrassrootsProjectPosition::getMajorRequirement, dto.getMajorRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getGradYearRequirement()), GrassrootsProjectPosition::getGradYearRequirement, dto.getGradYearRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), GrassrootsProjectPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), GrassrootsProjectPosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(GrassrootsProjectPosition::getCreatedAt);

        Page<GrassrootsProjectPosition> page = new Page<>(dto.getPage(), dto.getSize());
        grassrootsProjectPositionMapper.selectPage(page, wrapper);

        return page.convert(p -> GrassrootsProjectPositionListVO.builder()
                .id(p.getId())
                .projectType(p.getProjectType())
                .year(p.getYear())
                .positionName(p.getPositionName())
                .serviceType(p.getServiceType())
                .organizingDept(p.getOrganizingDept())
                .serviceUnit(p.getServiceUnit())
                .province(p.getProvince())
                .city(p.getCity())
                .county(p.getCounty())
                .township(p.getTownship())
                .servicePeriod(p.getServicePeriod())
                .educationRequirement(p.getEducationRequirement())
                .majorRequirement(p.getMajorRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .gradYearRequirement(p.getGradYearRequirement())
                .positionStatus(p.getPositionStatus())
                .politicalStatus(p.getPoliticalStatus())
                .build());
    }

    @Override
    public GrassrootsProjectPositionDetailVO detail(Long id) {
        GrassrootsProjectPosition p = grassrootsProjectPositionMapper.selectById(id);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
            log.warn("基层服务项目岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return GrassrootsProjectPositionDetailVO.builder()
                .id(p.getId())
                .projectType(p.getProjectType())
                .year(p.getYear())
                .positionName(p.getPositionName())
                .serviceType(p.getServiceType())
                .organizingDept(p.getOrganizingDept())
                .serviceUnit(p.getServiceUnit())
                .province(p.getProvince())
                .city(p.getCity())
                .county(p.getCounty())
                .township(p.getTownship())
                .servicePeriod(p.getServicePeriod())
                .serviceStartDate(p.getServiceStartDate())
                .serviceEndDate(p.getServiceEndDate())
                .educationRequirement(p.getEducationRequirement())
                .majorRequirement(p.getMajorRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .gradYearRequirement(p.getGradYearRequirement())
                .householdRequirement(p.getHouseholdRequirement())
                .otherRequirement(p.getOtherRequirement())
                .politicalStatus(p.getPoliticalStatus())
                .examContent(p.getExamContent())
                .examTime(p.getExamTime())
                .interviewForm(p.getInterviewForm())
                .monthlySubsidy(p.getMonthlySubsidy())
                .socialInsurance(p.getSocialInsurance())
                .housingInfo(p.getHousingInfo())
                .otherBenefits(p.getOtherBenefits())
                .afterServicePolicy(p.getAfterServicePolicy())
                .canTransferToCivil(p.getCanTransferToCivil())
                .canTransferToInstitution(p.getCanTransferToInstitution())
                .examBonusPoints(p.getExamBonusPoints())
                .tuitionCompensation(p.getTuitionCompensation())
                .postgradBonus(p.getPostgradBonus())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .applyLink(p.getApplyLink())
                .positionStatus(p.getPositionStatus())
                .contactPhone(p.getContactPhone())
                .remark(p.getRemark())
                .content(p.getContent())
                .build();
    }
}
```

- [ ] **Create Controller**

```java
package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/project")
@RequiredArgsConstructor
public class GrassrootsProjectPositionController {

    private final GrassrootsProjectPositionService grassrootsProjectPositionService;

    @GetMapping("/list")
    public R<IPage<GrassrootsProjectPositionListVO>> list(@Valid GrassrootsProjectPositionSearchDTO dto) {
        return R.ok(grassrootsProjectPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<GrassrootsProjectPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(grassrootsProjectPositionService.detail(id));
    }
}
```

---

### Task 9: Service + Controller - CommunityPosition (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/employment/grassrootsPosition/CommunityPositionService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/grassrootsPosition/CommunityPositionServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/grassrootsPosition/CommunityPositionController.java`

- [ ] **Create Service interface**

```java
package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;

public interface CommunityPositionService {
    IPage<CommunityPositionListVO> page(CommunityPositionSearchDTO dto);
    CommunityPositionDetailVO detail(Long id);
}
```

- [ ] **Create ServiceImpl**

```java
package com.haifeng.app.service.impl.employment.grassrootsPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.CommunityPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.CommunityPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPositionServiceImpl implements CommunityPositionService {

    private final CommunityPositionMapper communityPositionMapper;

    @Override
    public IPage<CommunityPositionListVO> page(CommunityPositionSearchDTO dto) {
        LambdaQueryWrapper<CommunityPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(CommunityPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getStreetOffice, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getCommunityName, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getSupervisingDept, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getDistrict, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getWorkLocation, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getMajorRequirement, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), CommunityPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getEmploymentType()), CommunityPosition::getEmploymentType, dto.getEmploymentType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), CommunityPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), CommunityPosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), CommunityPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), CommunityPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getWorkExperience()), CommunityPosition::getWorkExperience, dto.getWorkExperience());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), CommunityPosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(CommunityPosition::getCreatedAt);

        Page<CommunityPosition> page = new Page<>(dto.getPage(), dto.getSize());
        communityPositionMapper.selectPage(page, wrapper);

        return page.convert(p -> CommunityPositionListVO.builder()
                .id(p.getId())
                .communityName(p.getCommunityName())
                .district(p.getDistrict())
                .positionName(p.getPositionName())
                .positionType(p.getPositionType())
                .province(p.getProvince())
                .city(p.getCity())
                .educationRequirement(p.getEducationRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .majorRequirement(p.getMajorRequirement())
                .workExperience(p.getWorkExperience())
                .positionStatus(p.getPositionStatus())
                .build());
    }

    @Override
    public CommunityPositionDetailVO detail(Long id) {
        CommunityPosition p = communityPositionMapper.selectById(id);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
            log.warn("社区工作者岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return CommunityPositionDetailVO.builder()
                .id(p.getId())
                .streetOffice(p.getStreetOffice())
                .communityName(p.getCommunityName())
                .supervisingDept(p.getSupervisingDept())
                .district(p.getDistrict())
                .positionName(p.getPositionName())
                .positionType(p.getPositionType())
                .employmentType(p.getEmploymentType())
                .province(p.getProvince())
                .city(p.getCity())
                .workLocation(p.getWorkLocation())
                .educationRequirement(p.getEducationRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .majorRequirement(p.getMajorRequirement())
                .householdRequirement(p.getHouseholdRequirement())
                .politicalStatus(p.getPoliticalStatus())
                .workExperience(p.getWorkExperience())
                .socialWorkCert(p.getSocialWorkCert())
                .communityExperience(p.getCommunityExperience())
                .residenceRequirement(p.getResidenceRequirement())
                .salaryRange(p.getSalaryRange())
                .salaryComposition(p.getSalaryComposition())
                .benefits(p.getBenefits())
                .examContent(p.getExamContent())
                .interviewForm(p.getInterviewForm())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .examTime(p.getExamTime())
                .positionStatus(p.getPositionStatus())
                .applyLink(p.getApplyLink())
                .applyMethod(p.getApplyMethod())
                .contactPhone(p.getContactPhone())
                .contactAddress(p.getContactAddress())
                .remark(p.getRemark())
                .content(p.getContent())
                .build();
    }
}
```

- [ ] **Create Controller**

```java
package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/community")
@RequiredArgsConstructor
public class CommunityPositionController {

    private final CommunityPositionService communityPositionService;

    @GetMapping("/list")
    public R<IPage<CommunityPositionListVO>> list(@Valid CommunityPositionSearchDTO dto) {
        return R.ok(communityPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<CommunityPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(communityPositionService.detail(id));
    }
}
```

---

### Task 10: Service + Controller - PublicWelfarePosition (haifeng-app)

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/employment/grassrootsPosition/PublicWelfarePositionService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/grassrootsPosition/PublicWelfarePositionServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/grassrootsPosition/PublicWelfarePositionController.java`

- [ ] **Create Service interface**

```java
package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;

public interface PublicWelfarePositionService {
    IPage<PublicWelfarePositionListVO> page(PublicWelfarePositionSearchDTO dto);
    PublicWelfarePositionDetailVO detail(Long id);
}
```

- [ ] **Create ServiceImpl**

```java
package com.haifeng.app.service.impl.employment.grassrootsPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.PublicWelfarePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.PublicWelfarePositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicWelfarePositionServiceImpl implements PublicWelfarePositionService {

    private final PublicWelfarePositionMapper publicWelfarePositionMapper;

    @Override
    public IPage<PublicWelfarePositionListVO> page(PublicWelfarePositionSearchDTO dto) {
        LambdaQueryWrapper<PublicWelfarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublicWelfarePosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(PublicWelfarePosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(PublicWelfarePosition::getDevelopingUnit, dto.getKeyword())
                    .or()
                    .like(PublicWelfarePosition::getEmployingUnit, dto.getKeyword())
                    .or()
                    .like(PublicWelfarePosition::getProvince, dto.getKeyword())
                    .or()
                    .like(PublicWelfarePosition::getCity, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionCategory()), PublicWelfarePosition::getPositionCategory, dto.getPositionCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), PublicWelfarePosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), PublicWelfarePosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getDistrict()), PublicWelfarePosition::getDistrict, dto.getDistrict());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), PublicWelfarePosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), PublicWelfarePosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(PublicWelfarePosition::getCreatedAt);

        Page<PublicWelfarePosition> page = new Page<>(dto.getPage(), dto.getSize());
        publicWelfarePositionMapper.selectPage(page, wrapper);

        return page.convert(p -> PublicWelfarePositionListVO.builder()
                .id(p.getId())
                .developingUnit(p.getDevelopingUnit())
                .employingUnit(p.getEmployingUnit())
                .positionName(p.getPositionName())
                .positionCategory(p.getPositionCategory())
                .province(p.getProvince())
                .city(p.getCity())
                .district(p.getDistrict())
                .educationRequirement(p.getEducationRequirement())
                .recruitmentCount(p.getRecruitmentCount())
                .monthlySalary(p.getMonthlySalary())
                .householdRequirement(p.getHouseholdRequirement())
                .contractPeriod(p.getContractPeriod())
                .build());
    }

    @Override
    public PublicWelfarePositionDetailVO detail(Long id) {
        PublicWelfarePosition p = publicWelfarePositionMapper.selectById(id);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
            log.warn("公益性岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return PublicWelfarePositionDetailVO.builder()
                .id(p.getId())
                .developingUnit(p.getDevelopingUnit())
                .employingUnit(p.getEmployingUnit())
                .positionName(p.getPositionName())
                .positionCategory(p.getPositionCategory())
                .workContent(p.getWorkContent())
                .province(p.getProvince())
                .city(p.getCity())
                .district(p.getDistrict())
                .workLocation(p.getWorkLocation())
                .targetGroup(p.getTargetGroup())
                .educationRequirement(p.getEducationRequirement())
                .ageRange(p.getAgeRange())
                .healthRequirement(p.getHealthRequirement())
                .recruitmentCount(p.getRecruitmentCount())
                .householdRequirement(p.getHouseholdRequirement())
                .employmentDifficultyCert(p.getEmploymentDifficultyCert())
                .otherRequirement(p.getOtherRequirement())
                .contractPeriod(p.getContractPeriod())
                .isRenewable(p.getIsRenewable())
                .maxServiceYears(p.getMaxServiceYears())
                .monthlySalary(p.getMonthlySalary())
                .salarySource(p.getSalarySource())
                .subsidyStandard(p.getSubsidyStandard())
                .socialInsuranceInfo(p.getSocialInsuranceInfo())
                .otherBenefits(p.getOtherBenefits())
                .workSchedule(p.getWorkSchedule())
                .isShiftWork(p.getIsShiftWork())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .applyMethod(p.getApplyMethod())
                .applyAddress(p.getApplyAddress())
                .requiredDocuments(p.getRequiredDocuments())
                .positionStatus(p.getPositionStatus())
                .contactPhone(p.getContactPhone())
                .contactPerson(p.getContactPerson())
                .remark(p.getRemark())
                .content(p.getContent())
                .build();
    }
}
```

- [ ] **Create Controller**

```java
package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/welfare")
@RequiredArgsConstructor
public class PublicWelfarePositionController {

    private final PublicWelfarePositionService publicWelfarePositionService;

    @GetMapping("/list")
    public R<IPage<PublicWelfarePositionListVO>> list(@Valid PublicWelfarePositionSearchDTO dto) {
        return R.ok(publicWelfarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<PublicWelfarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(publicWelfarePositionService.detail(id));
    }
}
```
