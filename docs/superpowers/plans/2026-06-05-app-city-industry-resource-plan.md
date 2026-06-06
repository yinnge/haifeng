# App端城市/行业/资源管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 App 端城市、行业、资源三个模块的只读展示接口（共7个API）

**Architecture:** 每个模块独立 Controller + Service + VO/DTO，遵循项目既有模式（如 UniversityController）。城市和行业采用主表+详情表的一对一关联查询，资源模块在获取URL时同步原子更新浏览计数。

**Tech Stack:** Spring Boot, MyBatis-Plus (LambdaQueryWrapper / IPage), Lombok, Jakarta Validation

---

## File Structure

### 新建文件

| 文件 | 职责 |
|------|------|
| `haifeng-app/.../controller/city/CityController.java` | 城市列表（公开）+ 详情（@RequireLogin） |
| `haifeng-app/.../service/city/CityService.java` | 城市模块 Service 接口 |
| `haifeng-app/.../service/impl/city/CityServiceImpl.java` | 城市模块 Service 实现 |
| `haifeng-app/.../dto/city/CityQueryDTO.java` | 城市列表查询参数 |
| `haifeng-app/.../vo/city/CityListVO.java` | 城市列表返回字段 |
| `haifeng-app/.../vo/city/CityDetailVO.java` | 城市详情返回字段 |
| `haifeng-app/.../controller/industry/IndustryController.java` | 行业列表（公开）+ 详情（@RequireLogin） |
| `haifeng-app/.../service/industry/IndustryService.java` | 行业模块 Service 接口 |
| `haifeng-app/.../service/impl/industry/IndustryServiceImpl.java` | 行业模块 Service 实现 |
| `haifeng-app/.../dto/industry/IndustryQueryDTO.java` | 行业列表查询参数 |
| `haifeng-app/.../vo/industry/IndustryListVO.java` | 行业列表返回字段 |
| `haifeng-app/.../vo/industry/IndustryDetailVO.java` | 行业详情返回字段 |
| `haifeng-app/.../controller/resource/ResourceController.java` | 资源列表（公开）+ URL（@RequireLogin） |
| `haifeng-app/.../service/resource/ResourceService.java` | 资源模块 Service 接口 |
| `haifeng-app/.../service/impl/resource/ResourceServiceImpl.java` | 资源模块 Service 实现 |
| `haifeng-app/.../dto/resource/ResourceQueryDTO.java` | 资源列表查询参数 |
| `haifeng-app/.../vo/resource/ResourceListVO.java` | 资源列表返回字段 |
| `haifeng-app/.../vo/resource/ResourceUrlVO.java` | 资源URL返回字段 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `haifeng-common/.../mapper/resource/ResourceMapper.java` | 添加 `incrementViewCount` 方法 |

---

## Task 1: 城市模块 — DTO 与 VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/city/CityQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/city/CityListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/city/CityDetailVO.java`

- [ ] **Step 1: 创建 CityQueryDTO**

```java
package com.haifeng.app.dto.city;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端城市列表查询 DTO
 * cityName 走 LIKE，province / region 精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CityQueryDTO extends BasePageQueryDTO {

    /** 城市名称模糊（LIKE %cityName%） */
    private String cityName;

    /** 省份精准匹配 */
    private String province;

    /** 地区精准匹配 */
    private String region;
}
```

- [ ] **Step 2: 创建 CityListVO**

```java
package com.haifeng.app.vo.city;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * C 端城市列表 VO（任务 1 接口 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
    private Integer keyCollegeCount;
    private BigDecimal residentPopulation;
    private BigDecimal gdp;
}
```

- [ ] **Step 3: 创建 CityDetailVO**

```java
package com.haifeng.app.vo.city;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * C 端城市详情 VO（任务 1 接口 2，来自 t_city_detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 来自 t_city_detail =====
    private String cityName;
    private BigDecimal area;
    private String subtitle;
    private String cityLevel;
    private String adminCode;
    private BigDecimal perCapitaGdp;
    private BigDecimal urbanizationRate;
    private BigDecimal ruralPopRatio;
    private BigDecimal agingRate;
    private BigDecimal migrantPopRatio;
    private BigDecimal gdpGrowthRate;
    private Integer fortune500Count;
    private Map<String, Object> industryStructure;
    private String industryDescription;
    private List<String> mainIndustries;
    private List<String> emergingIndustries;
    private Map<String, Object> futurePlan;
    private Map<String, Object> highEducation;
    private Map<String, Object> basicEducation;
    private Map<String, Object> enterpriseStats;
    private Map<String, Object> housingPriceLevel;
    private Map<String, Object> rentalCost;
    private Map<String, Object> housingPolicy;
    private Map<String, Object> consumption;
    private Map<String, Object> employment;
    private Map<String, Object> transportation;
    private Map<String, Object> medical;
    private Map<String, Object> culture;
}
```

---

## Task 2: 城市模块 — Service 与 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/city/CityService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/city/CityServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/city/CityController.java`

- [ ] **Step 1: 创建 CityService 接口**

```java
package com.haifeng.app.service.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;

public interface CityService {

    /**
     * 分页查询城市列表（isDeleted=false）；cityName LIKE，province/region EQ；排序 id ASC
     */
    IPage<CityListVO> page(CityQueryDTO dto);

    /**
     * 城市详情：通过 cityId 关联 t_city_detail 查询
     * 不存在 → BusinessException(NOT_FOUND)
     */
    CityDetailVO detail(Long cityId);
}
```

- [ ] **Step 2: 创建 CityServiceImpl**

```java
package com.haifeng.app.service.impl.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.city.CityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityDetailMapper;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityMapper cityMapper;
    private final CityDetailMapper cityDetailMapper;

    @Override
    public IPage<CityListVO> page(CityQueryDTO dto) {
        Page<City> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<City>()
                .eq(City::getIsDeleted, false)
                .like(StringUtils.hasText(dto.getCityName()), City::getCityName, dto.getCityName())
                .eq(StringUtils.hasText(dto.getProvince()), City::getProvince, dto.getProvince())
                .eq(StringUtils.hasText(dto.getRegion()), City::getRegion, dto.getRegion())
                .orderByAsc(City::getId);

        IPage<City> entityPage = cityMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public CityDetailVO detail(Long cityId) {
        CityDetail detail = cityDetailMapper.findByCityId(cityId);
        if (detail == null) {
            log.debug("城市详情不存在, cityId={}", cityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "城市详情不存在");
        }

        return CityDetailVO.builder()
                .cityName(detail.getCityName())
                .area(detail.getArea())
                .subtitle(detail.getSubtitle())
                .cityLevel(detail.getCityLevel())
                .adminCode(detail.getAdminCode())
                .perCapitaGdp(detail.getPerCapitaGdp())
                .urbanizationRate(detail.getUrbanizationRate())
                .ruralPopRatio(detail.getRuralPopRatio())
                .agingRate(detail.getAgingRate())
                .migrantPopRatio(detail.getMigrantPopRatio())
                .gdpGrowthRate(detail.getGdpGrowthRate())
                .fortune500Count(detail.getFortune500Count())
                .industryStructure(detail.getIndustryStructure())
                .industryDescription(detail.getIndustryDescription())
                .mainIndustries(detail.getMainIndustries())
                .emergingIndustries(detail.getEmergingIndustries())
                .futurePlan(detail.getFuturePlan())
                .highEducation(detail.getHighEducation())
                .basicEducation(detail.getBasicEducation())
                .enterpriseStats(detail.getEnterpriseStats())
                .housingPriceLevel(detail.getHousingPriceLevel())
                .rentalCost(detail.getRentalCost())
                .housingPolicy(detail.getHousingPolicy())
                .consumption(detail.getConsumption())
                .employment(detail.getEmployment())
                .transportation(detail.getTransportation())
                .medical(detail.getMedical())
                .culture(detail.getCulture())
                .build();
    }

    private CityListVO toListVO(City e) {
        return CityListVO.builder()
                .id(e.getId())
                .cityName(e.getCityName())
                .province(e.getProvince())
                .region(e.getRegion())
                .cityIntro(e.getCityIntro())
                .collegeCount(e.getCollegeCount())
                .keyCollegeCount(e.getKeyCollegeCount())
                .residentPopulation(e.getResidentPopulation())
                .gdp(e.getGdp())
                .build();
    }
}
```

- [ ] **Step 3: 创建 CityController**

```java
package com.haifeng.app.controller.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
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
 * C 端城市管理 - 列表（公开）+ 详情（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    /** 任务 1 接口 1：分页查询城市列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<CityListVO>> list(@Valid CityQueryDTO dto) {
        return R.ok(cityService.page(dto));
    }

    /** 任务 1 接口 2：城市详情，需登录 */
    @RequireLogin
    @GetMapping("/{cityId}/detail")
    public R<CityDetailVO> detail(@PathVariable Long cityId) {
        return R.ok(cityService.detail(cityId));
    }
}
```

---

## Task 3: 行业模块 — DTO 与 VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/industry/IndustryQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/industry/IndustryListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/industry/IndustryDetailVO.java`

- [ ] **Step 1: 创建 IndustryQueryDTO**

```java
package com.haifeng.app.dto.industry;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端行业列表查询 DTO
 * category 精准匹配
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IndustryQueryDTO extends BasePageQueryDTO {

    /** 行业分类精准匹配 */
    private String category;
}
```

- [ ] **Step 2: 创建 IndustryListVO**

```java
package com.haifeng.app.vo.industry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * C 端行业列表 VO（任务 2 接口 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String industryName;
    private String category;
    private String description;
    private BigDecimal annualGrowthRate;
    private String marketScale;
    private String talentGap;
    private BigDecimal investmentHeat;
}
```

- [ ] **Step 3: 创建 IndustryDetailVO**

```java
package com.haifeng.app.vo.industry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * C 端行业详情 VO（任务 2 接口 2，来自 t_industry_detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 来自 t_industry_detail =====
    private String industryName;
    private String shortDescription;
    private String detailedDescription;
    private Map<String, Object> industryScale;
    private Map<String, Object> industryTalentDemand;
    private Map<String, Object> industrySalary;
    private Map<String, Object> policyInfo;
    private Map<String, Object> developmentSupportInfo;
    private Map<String, Object> talentAnalysis;
    private Map<String, Object> talentPolicy;
    private Map<String, Object> salaryData;
}
```

---

## Task 4: 行业模块 — Service 与 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/industry/IndustryService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/industry/IndustryServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/industry/IndustryController.java`

- [ ] **Step 1: 创建 IndustryService 接口**

```java
package com.haifeng.app.service.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;

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
}
```

- [ ] **Step 2: 创建 IndustryServiceImpl**

```java
package com.haifeng.app.service.impl.industry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.service.industry.IndustryService;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.entity.industry.IndustryDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.industry.IndustryDetailMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryServiceImpl implements IndustryService {

    private final IndustryMapper industryMapper;
    private final IndustryDetailMapper industryDetailMapper;

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
}
```

- [ ] **Step 3: 创建 IndustryController**

```java
package com.haifeng.app.controller.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.service.industry.IndustryService;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;
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
 * C 端行业管理 - 列表（公开）+ 详情（登录）
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

    /** 任务 2 接口 2：行业详情，需登录 */
    @RequireLogin
    @GetMapping("/{industryId}/detail")
    public R<IndustryDetailVO> detail(@PathVariable Long industryId) {
        return R.ok(industryService.detail(industryId));
    }
}
```

---

## Task 5: 资源模块 — DTO、VO 与 Mapper 修改

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/resource/ResourceQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/resource/ResourceListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/resource/ResourceUrlVO.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/resource/ResourceMapper.java`

- [ ] **Step 1: 创建 ResourceQueryDTO**

```java
package com.haifeng.app.dto.resource;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端资源列表查询 DTO
 * category 精准匹配
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceQueryDTO extends BasePageQueryDTO {

    /** 资源分类精准匹配 */
    private String category;
}
```

- [ ] **Step 2: 创建 ResourceListVO**

```java
package com.haifeng.app.vo.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端资源列表 VO（任务 3 接口 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String resourceName;
    private String coverUrl;
    private String description;
    private String category;
    private String fileType;
    private Integer viewCount;
}
```

- [ ] **Step 3: 创建 ResourceUrlVO**

```java
package com.haifeng.app.vo.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端资源 URL VO（任务 3 接口 2，登录后查看）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUrlVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String resourceUrl;
    private String accessCode;
}
```

- [ ] **Step 4: 修改 ResourceMapper 添加 incrementViewCount**

在 `haifeng-common/src/main/java/com/haifeng/common/mapper/resource/ResourceMapper.java` 中添加方法：

```java
package com.haifeng.common.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.resource.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false")
    int incrementViewCount(@Param("id") Long id);
}
```

---

## Task 6: 资源模块 — Service 与 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/resource/ResourceService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/resource/ResourceServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/resource/ResourceController.java`

- [ ] **Step 1: 创建 ResourceService 接口**

```java
package com.haifeng.app.service.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.resource.ResourceQueryDTO;
import com.haifeng.app.vo.resource.ResourceListVO;
import com.haifeng.app.vo.resource.ResourceUrlVO;

public interface ResourceService {

    /**
     * 分页查询资源列表（isDeleted=false）；category EQ；排序 sort_order ASC, created_at DESC
     */
    IPage<ResourceListVO> page(ResourceQueryDTO dto);

    /**
     * 查看资源 URL：根据 id 获取 resourceUrl + accessCode，同步 view_count + 1
     * 不存在或已删除 → BusinessException(NOT_FOUND)
     */
    ResourceUrlVO getUrl(Long id);
}
```

- [ ] **Step 2: 创建 ResourceServiceImpl**

```java
package com.haifeng.app.service.impl.resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.resource.ResourceQueryDTO;
import com.haifeng.app.service.resource.ResourceService;
import com.haifeng.app.vo.resource.ResourceListVO;
import com.haifeng.app.vo.resource.ResourceUrlVO;
import com.haifeng.common.entity.resource.Resource;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.resource.ResourceMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;

    @Override
    public IPage<ResourceListVO> page(ResourceQueryDTO dto) {
        Page<Resource> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<Resource>()
                .eq(Resource::getIsDeleted, false)
                .eq(StringUtils.hasText(dto.getCategory()), Resource::getCategory, dto.getCategory())
                .orderByAsc(Resource::getSortOrder)
                .orderByDesc(Resource::getCreatedAt);

        IPage<Resource> entityPage = resourceMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public ResourceUrlVO getUrl(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null || Boolean.TRUE.equals(resource.getIsDeleted())) {
            log.debug("资源不存在或已删除, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }

        // 原子更新浏览计数
        resourceMapper.incrementViewCount(id);

        return ResourceUrlVO.builder()
                .resourceUrl(resource.getResourceUrl())
                .accessCode(resource.getAccessCode())
                .build();
    }

    private ResourceListVO toListVO(Resource e) {
        return ResourceListVO.builder()
                .id(e.getId())
                .resourceName(e.getResourceName())
                .coverUrl(e.getCoverUrl())
                .description(e.getDescription())
                .category(e.getCategory())
                .fileType(e.getFileType())
                .viewCount(e.getViewCount())
                .build();
    }
}
```

- [ ] **Step 3: 创建 ResourceController**

```java
package com.haifeng.app.controller.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.resource.ResourceQueryDTO;
import com.haifeng.app.service.resource.ResourceService;
import com.haifeng.app.vo.resource.ResourceListVO;
import com.haifeng.app.vo.resource.ResourceUrlVO;
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
 * C 端资源管理 - 列表（公开）+ URL（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /** 任务 3 接口 1：分页查询资源列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<ResourceListVO>> list(@Valid ResourceQueryDTO dto) {
        return R.ok(resourceService.page(dto));
    }

    /** 任务 3 接口 2 & 3：查看资源 URL 并同步 +1 浏览计数，需登录 */
    @RequireLogin
    @GetMapping("/{id}/url")
    public R<ResourceUrlVO> getUrl(@PathVariable Long id) {
        return R.ok(resourceService.getUrl(id));
    }
}
```

---

## Task 7: 编译验证

**Files:** 无新文件，验证编译通过

- [ ] **Step 1: 执行 Maven 编译**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn compile -pl haifeng-app -am -q`

Expected: BUILD SUCCESS

- [ ] **Step 2: 如有编译错误，逐一修复**

常见问题：import 路径不对、字段名与 entity 不匹配。根据编译输出逐个修正。
