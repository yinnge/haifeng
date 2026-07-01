# App端：特殊通道 + 院校通道关联 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现app端特殊通道模块4个任务的查询接口 + 院校管理新增的通道关联查询

**Architecture:** 在haifeng-app新建`special`模块（4个Controller+Service+DTO+VO），在已有`university`模块追加2个端点。所有Entity/Mapper已在haifeng-common中存在，app端仅引用。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Spring Security (@RequireLogin), ProvinceEnum, SnowflakeIdGenerator

---

### 任务1：创建所有DTO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/special/SpecialChannelQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/special/SpecialChannelUnivQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/special/StrongBaseScoreQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/university/UniversityChannelQueryDTO.java`

- [ ] **Step 1: 创建 SpecialChannelQueryDTO**

```java
package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelQueryDTO extends BasePageQueryDTO {
    private String displayType;
    private String channelName;
}
```

- [ ] **Step 2: 创建 SpecialChannelUnivQueryDTO**

```java
package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelUnivQueryDTO extends BasePageQueryDTO {
    @NotBlank(message = "通道代码不能为空")
    private String channelCode;

    private String channelName;

    private String regionTag;

    private OffsetDateTime signupStart;

    private OffsetDateTime signupEnd;
}
```

- [ ] **Step 3: 创建 StrongBaseScoreQueryDTO**

```java
package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseScoreQueryDTO extends BasePageQueryDTO {
    private Short year;
    private String province;
    private String subjectType;
    private String entryScoreType;
    private String universityName;
    private String majorName;
    private String majorCode;
}
```

- [ ] **Step 4: 创建 UniversityChannelQueryDTO**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityChannelQueryDTO extends BasePageQueryDTO {
    private String channelName;
    private String regionTag;
}
```

### 任务2：创建所有VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/SpecialChannelListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/SpecialChannelDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/SpecialChannelUnivListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/SpecialChannelUnivDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/StrongBaseScoreListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/StrongBaseScoreDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/special/StrongBaseUniversityDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/UniversityChannelListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/university/ChannelOptionVO.java`

- [ ] **Step 1: 创建 SpecialChannelListVO**

```java
package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String channelCode;
    private String channelName;
    private String subtitle;
    private String filterLabel;
    private String displayType;
}
```

- [ ] **Step 2: 创建 SpecialChannelDetailVO**

```java
package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialChannelDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String channelCode;
    private String channelName;
    private String subtitle;
    private String filterLabel;
    private String displayType;
    private String content;
}
```

- [ ] **Step 3: 创建 SpecialChannelUnivListVO**

```java
package com.haifeng.app.vo.special;

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
public class SpecialChannelUnivListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long universityId;
    private String universityName;
    private Short year;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
```

- [ ] **Step 4: 创建 SpecialChannelUnivDetailVO**

```java
package com.haifeng.app.vo.special;

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
public class SpecialChannelUnivDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String channelCode;
    private String channelName;
    private Long universityId;
    private String universityName;
    private Short year;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
    private String officialUrl;
    private String brochureTitle;
    private String brochureContent;
}
```

- [ ] **Step 5: 创建 StrongBaseScoreListVO**

```java
package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseScoreListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long universityId;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String majorName;
    private String majorCode;
    private BigDecimal entryScore;
    private String entryScoreType;
    private String entryRatio;
    private BigDecimal admissionScore;
    private Integer planCount;
    private Integer admissionCount;
}
```

- [ ] **Step 6: 创建 StrongBaseScoreDetailVO**

```java
package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseScoreDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long universityId;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String majorName;
    private String majorCode;
    private BigDecimal entryScore;
    private String entryScoreType;
    private String entryFormula;
    private String entryRatio;
    private BigDecimal admissionScore;
    private String admissionFormula;
    private Integer planCount;
    private Integer admissionCount;
    private String remark;
}
```

- [ ] **Step 7: 创建 StrongBaseUniversityDetailVO**

```java
package com.haifeng.app.vo.special;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrongBaseUniversityDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long universityId;
    private String universityName;
    private Boolean isPilot;
    private Short pilotYear;
    private String officialUrl;
    private String signupUrl;
    private Boolean testBeforeScore;
    private String defaultEntryRatio;
    private String defaultAdmissionFormula;
    private String[] availableMajors;
    private String specialNotes;
}
```

- [ ] **Step 8: 创建 UniversityChannelListVO**

```java
package com.haifeng.app.vo.university;

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
public class UniversityChannelListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String channelCode;
    private String channelName;
    private Short year;
    private String regionTag;
    private OffsetDateTime signupStart;
    private OffsetDateTime signupEnd;
}
```

- [ ] **Step 9: 创建 ChannelOptionVO**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelOptionVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String channelCode;
    private String channelName;
}
```

### 任务3：在SpecialChannelUniversityMapper添加自定义查询

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/special/SpecialChannelUniversityMapper.java`

- [ ] **Step 1: 添加selectDistinctActiveChannels方法**

```java
@Select("SELECT DISTINCT channel_code, channel_name FROM t_special_channel_university WHERE is_active = TRUE ORDER BY channel_name")
List<SpecialChannelUniversity> selectDistinctActiveChannels();
```

### 任务4：创建Service接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/special/SpecialChannelService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/special/SpecialChannelUniversityService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/special/StrongBaseScoreService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/special/StrongBaseUniversityService.java`

- [ ] **Step 1: 创建 SpecialChannelService**

```java
package com.haifeng.app.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelQueryDTO;
import com.haifeng.app.vo.special.SpecialChannelDetailVO;
import com.haifeng.app.vo.special.SpecialChannelListVO;

public interface SpecialChannelService {
    IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto);
    SpecialChannelDetailVO detail(Long id);
}
```

- [ ] **Step 2: 创建 SpecialChannelUniversityService**

```java
package com.haifeng.app.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.app.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.app.vo.special.SpecialChannelUnivListVO;

public interface SpecialChannelUniversityService {
    IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto);
    SpecialChannelUnivDetailVO detail(Long id);
}
```

- [ ] **Step 3: 创建 StrongBaseScoreService**

```java
package com.haifeng.app.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.app.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.app.vo.special.StrongBaseScoreListVO;

public interface StrongBaseScoreService {
    IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto);
    StrongBaseScoreDetailVO detail(Long id);
}
```

- [ ] **Step 4: 创建 StrongBaseUniversityService**

```java
package com.haifeng.app.service.special;

import com.haifeng.app.vo.special.StrongBaseUniversityDetailVO;

public interface StrongBaseUniversityService {
    StrongBaseUniversityDetailVO detailByUniversityId(Long universityId);
}
```

### 任务5：创建Service实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/special/SpecialChannelServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/special/SpecialChannelUniversityServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/special/StrongBaseScoreServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/special/StrongBaseUniversityServiceImpl.java`

- [ ] **Step 1: 创建 SpecialChannelServiceImpl**

```java
package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.special.SpecialChannelQueryDTO;
import com.haifeng.app.service.special.SpecialChannelService;
import com.haifeng.app.vo.special.SpecialChannelDetailVO;
import com.haifeng.app.vo.special.SpecialChannelListVO;
import com.haifeng.common.entity.special.SpecialChannel;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelServiceImpl implements SpecialChannelService {

    private final SpecialChannelMapper specialChannelMapper;

    @Override
    public IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto) {
        Page<SpecialChannel> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannel> wrapper = new LambdaQueryWrapper<SpecialChannel>()
                .eq(SpecialChannel::getIsActive, true)
                .eq(StringUtils.hasText(dto.getDisplayType()), SpecialChannel::getDisplayType, dto.getDisplayType())
                .like(StringUtils.hasText(dto.getChannelName()), SpecialChannel::getChannelName, dto.getChannelName())
                .orderByAsc(SpecialChannel::getSortOrder)
                .orderByDesc(SpecialChannel::getId);
        return specialChannelMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public SpecialChannelDetailVO detail(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsActive())) {
            log.debug("特殊通道不存在或已禁用, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "特殊通道不存在");
        }
        return SpecialChannelDetailVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .subtitle(entity.getSubtitle())
                .filterLabel(entity.getFilterLabel())
                .displayType(entity.getDisplayType())
                .content(entity.getContent())
                .build();
    }

    private SpecialChannelListVO toListVO(SpecialChannel e) {
        return SpecialChannelListVO.builder()
                .id(e.getId())
                .channelCode(e.getChannelCode())
                .channelName(e.getChannelName())
                .subtitle(e.getSubtitle())
                .filterLabel(e.getFilterLabel())
                .displayType(e.getDisplayType())
                .build();
    }
}
```

- [ ] **Step 2: 创建 SpecialChannelUniversityServiceImpl**

```java
package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.app.service.special.SpecialChannelUniversityService;
import com.haifeng.app.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.app.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelUniversityServiceImpl implements SpecialChannelUniversityService {

    private final SpecialChannelUniversityMapper specialChannelUniversityMapper;

    @Override
    public IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto) {
        if (!ProvinceEnum.isValid(dto.getRegionTag())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "省份参数不合法");
        }
        Page<SpecialChannelUniversity> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannelUniversity> wrapper = new LambdaQueryWrapper<SpecialChannelUniversity>()
                .eq(SpecialChannelUniversity::getIsActive, true)
                .eq(SpecialChannelUniversity::getChannelCode, dto.getChannelCode())
                .like(StringUtils.hasText(dto.getChannelName()), SpecialChannelUniversity::getChannelName, dto.getChannelName())
                .eq(StringUtils.hasText(dto.getRegionTag()), SpecialChannelUniversity::getRegionTag, dto.getRegionTag())
                .ge(dto.getSignupStart() != null, SpecialChannelUniversity::getSignupStart, dto.getSignupStart())
                .le(dto.getSignupEnd() != null, SpecialChannelUniversity::getSignupEnd, dto.getSignupEnd())
                .orderByAsc(SpecialChannelUniversity::getSortOrder)
                .orderByDesc(SpecialChannelUniversity::getId);
        return specialChannelUniversityMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public SpecialChannelUnivDetailVO detail(Long id) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsActive())) {
            log.debug("通道-大学关联不存在或已禁用, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        return SpecialChannelUnivDetailVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .year(entity.getYear())
                .regionTag(entity.getRegionTag())
                .signupStart(entity.getSignupStart())
                .signupEnd(entity.getSignupEnd())
                .officialUrl(entity.getOfficialUrl())
                .brochureTitle(entity.getBrochureTitle())
                .brochureContent(entity.getBrochureContent())
                .build();
    }

    private SpecialChannelUnivListVO toListVO(SpecialChannelUniversity e) {
        return SpecialChannelUnivListVO.builder()
                .universityId(e.getUniversityId())
                .universityName(e.getUniversityName())
                .year(e.getYear())
                .regionTag(e.getRegionTag())
                .signupStart(e.getSignupStart())
                .signupEnd(e.getSignupEnd())
                .build();
    }
}
```

- [ ] **Step 3: 创建 StrongBaseScoreServiceImpl**

```java
package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.app.service.special.StrongBaseScoreService;
import com.haifeng.app.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.app.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.entity.special.StrongBaseScore;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseScoreMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseScoreServiceImpl implements StrongBaseScoreService {

    private final StrongBaseScoreMapper strongBaseScoreMapper;

    @Override
    public IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto) {
        if (!ProvinceEnum.isValid(dto.getProvince())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "省份参数不合法");
        }
        Page<StrongBaseScore> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<StrongBaseScore> wrapper = new LambdaQueryWrapper<StrongBaseScore>()
                .eq(StrongBaseScore::getIsActive, true)
                .eq(dto.getYear() != null, StrongBaseScore::getYear, dto.getYear())
                .eq(StringUtils.hasText(dto.getProvince()), StrongBaseScore::getProvince, dto.getProvince())
                .eq(StringUtils.hasText(dto.getSubjectType()), StrongBaseScore::getSubjectType, dto.getSubjectType())
                .eq(StringUtils.hasText(dto.getEntryScoreType()), StrongBaseScore::getEntryScoreType, dto.getEntryScoreType())
                .like(StringUtils.hasText(dto.getUniversityName()), StrongBaseScore::getUniversityName, dto.getUniversityName())
                .like(StringUtils.hasText(dto.getMajorName()), StrongBaseScore::getMajorName, dto.getMajorName())
                .like(StringUtils.hasText(dto.getMajorCode()), StrongBaseScore::getMajorCode, dto.getMajorCode())
                .orderByDesc(StrongBaseScore::getYear)
                .orderByDesc(StrongBaseScore::getId);
        return strongBaseScoreMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public StrongBaseScoreDetailVO detail(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsActive())) {
            log.debug("强基数据不存在或已禁用, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "强基数据不存在");
        }
        return StrongBaseScoreDetailVO.builder()
                .id(entity.getId())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .year(entity.getYear())
                .province(entity.getProvince())
                .subjectType(entity.getSubjectType())
                .majorName(entity.getMajorName())
                .majorCode(entity.getMajorCode())
                .entryScore(entity.getEntryScore())
                .entryScoreType(entity.getEntryScoreType())
                .entryFormula(entity.getEntryFormula())
                .entryRatio(entity.getEntryRatio())
                .admissionScore(entity.getAdmissionScore())
                .admissionFormula(entity.getAdmissionFormula())
                .planCount(entity.getPlanCount())
                .admissionCount(entity.getAdmissionCount())
                .remark(entity.getRemark())
                .build();
    }

    private StrongBaseScoreListVO toListVO(StrongBaseScore e) {
        return StrongBaseScoreListVO.builder()
                .id(e.getId())
                .universityId(e.getUniversityId())
                .universityName(e.getUniversityName())
                .year(e.getYear())
                .province(e.getProvince())
                .subjectType(e.getSubjectType())
                .majorName(e.getMajorName())
                .majorCode(e.getMajorCode())
                .entryScore(e.getEntryScore())
                .entryScoreType(e.getEntryScoreType())
                .entryRatio(e.getEntryRatio())
                .admissionScore(e.getAdmissionScore())
                .planCount(e.getPlanCount())
                .admissionCount(e.getAdmissionCount())
                .build();
    }
}
```

- [ ] **Step 4: 创建 StrongBaseUniversityServiceImpl**

```java
package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.special.StrongBaseUniversityService;
import com.haifeng.app.vo.special.StrongBaseUniversityDetailVO;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseUniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseUniversityServiceImpl implements StrongBaseUniversityService {

    private final StrongBaseUniversityMapper strongBaseUniversityMapper;

    @Override
    public StrongBaseUniversityDetailVO detailByUniversityId(Long universityId) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectOne(
                new LambdaQueryWrapper<StrongBaseUniversity>()
                        .eq(StrongBaseUniversity::getUniversityId, universityId));
        if (entity == null) {
            log.debug("强基院校配置不存在, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        return StrongBaseUniversityDetailVO.builder()
                .id(entity.getId())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .isPilot(entity.getIsPilot())
                .pilotYear(entity.getPilotYear())
                .officialUrl(entity.getOfficialUrl())
                .signupUrl(entity.getSignupUrl())
                .testBeforeScore(entity.getTestBeforeScore())
                .defaultEntryRatio(entity.getDefaultEntryRatio())
                .defaultAdmissionFormula(entity.getDefaultAdmissionFormula())
                .availableMajors(entity.getAvailableMajors())
                .specialNotes(entity.getSpecialNotes())
                .build();
    }
}
```

### 任务6：创建Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/special/SpecialChannelController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/special/SpecialChannelUniversityController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/special/StrongBaseScoreController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/special/StrongBaseUniversityController.java`

- [ ] **Step 1: 创建 SpecialChannelController**

```java
package com.haifeng.app.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelQueryDTO;
import com.haifeng.app.service.special.SpecialChannelService;
import com.haifeng.app.vo.special.SpecialChannelDetailVO;
import com.haifeng.app.vo.special.SpecialChannelListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/special/channel")
@RequiredArgsConstructor
public class SpecialChannelController {

    private final SpecialChannelService specialChannelService;

    @GetMapping("/list")
    public R<IPage<SpecialChannelListVO>> list(@Valid SpecialChannelQueryDTO dto) {
        return R.ok(specialChannelService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<SpecialChannelDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelService.detail(id));
    }
}
```

- [ ] **Step 2: 创建 SpecialChannelUniversityController**

```java
package com.haifeng.app.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.app.service.special.SpecialChannelUniversityService;
import com.haifeng.app.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.app.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/special/channel-univ")
@RequiredArgsConstructor
public class SpecialChannelUniversityController {

    private final SpecialChannelUniversityService specialChannelUniversityService;

    @GetMapping("/list")
    public R<IPage<SpecialChannelUnivListVO>> list(@Valid SpecialChannelUnivQueryDTO dto) {
        return R.ok(specialChannelUniversityService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<SpecialChannelUnivDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelUniversityService.detail(id));
    }
}
```

- [ ] **Step 3: 创建 StrongBaseScoreController**

```java
package com.haifeng.app.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.app.service.special.StrongBaseScoreService;
import com.haifeng.app.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.app.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/special/strong-base-score")
@RequiredArgsConstructor
public class StrongBaseScoreController {

    private final StrongBaseScoreService strongBaseScoreService;

    @GetMapping("/list")
    public R<IPage<StrongBaseScoreListVO>> list(@Valid StrongBaseScoreQueryDTO dto) {
        return R.ok(strongBaseScoreService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<StrongBaseScoreDetailVO> detail(@PathVariable Long id) {
        return R.ok(strongBaseScoreService.detail(id));
    }
}
```

- [ ] **Step 4: 创建 StrongBaseUniversityController**

```java
package com.haifeng.app.controller.special;

import com.haifeng.app.service.special.StrongBaseUniversityService;
import com.haifeng.app.vo.special.StrongBaseUniversityDetailVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/special/strong-base-univ")
@RequiredArgsConstructor
public class StrongBaseUniversityController {

    private final StrongBaseUniversityService strongBaseUniversityService;

    @RequireLogin
    @GetMapping("/{universityId}")
    public R<StrongBaseUniversityDetailVO> detailByUniversityId(@PathVariable Long universityId) {
        return R.ok(strongBaseUniversityService.detailByUniversityId(universityId));
    }
}
```

### 任务7：修改University模块，追加任务5

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityService.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityServiceImpl.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityController.java`

- [ ] **Step 1: 修改 UniversityService 接口，追加两个方法**

```java
// 追加到现有接口中
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityChannelQueryDTO;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityChannelListVO;

IPage<UniversityChannelListVO> pageChannels(Long universityId, UniversityChannelQueryDTO dto);
List<ChannelOptionVO> listChannelOptions();
```

- [ ] **Step 2: 修改 UniversityServiceImpl，追加实现**

```java
// 追加到现有实现类
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;

// 追加字段注入
private final SpecialChannelUniversityMapper specialChannelUniversityMapper;

// 追加方法
@Override
public IPage<UniversityChannelListVO> pageChannels(Long universityId, UniversityChannelQueryDTO dto) {
    Page<SpecialChannelUniversity> page = new Page<>(dto.getPage(), dto.getSize());
    LambdaQueryWrapper<SpecialChannelUniversity> wrapper = new LambdaQueryWrapper<SpecialChannelUniversity>()
            .eq(SpecialChannelUniversity::getIsActive, true)
            .eq(SpecialChannelUniversity::getUniversityId, universityId)
            .like(StringUtils.hasText(dto.getChannelName()), SpecialChannelUniversity::getChannelName, dto.getChannelName())
            .eq(StringUtils.hasText(dto.getRegionTag()), SpecialChannelUniversity::getRegionTag, dto.getRegionTag())
            .orderByAsc(SpecialChannelUniversity::getSortOrder)
            .orderByDesc(SpecialChannelUniversity::getId);
    return specialChannelUniversityMapper.selectPage(page, wrapper).convert(e ->
            UniversityChannelListVO.builder()
                    .channelCode(e.getChannelCode())
                    .channelName(e.getChannelName())
                    .year(e.getYear())
                    .regionTag(e.getRegionTag())
                    .signupStart(e.getSignupStart())
                    .signupEnd(e.getSignupEnd())
                    .build());
}

@Override
public List<ChannelOptionVO> listChannelOptions() {
    List<SpecialChannelUniversity> list = specialChannelUniversityMapper.selectDistinctActiveChannels();
    return list.stream()
            .map(e -> ChannelOptionVO.builder()
                    .channelCode(e.getChannelCode())
                    .channelName(e.getChannelName())
                    .build())
            .toList();
}
```

- [ ] **Step 3: 修改 UniversityController，追加两个端点**

```java
// 追加到现有Controller
import com.haifeng.app.dto.university.UniversityChannelQueryDTO;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityChannelListVO;

@GetMapping("/{universityId}/channels")
public R<IPage<UniversityChannelListVO>> channels(
        @PathVariable Long universityId,
        @Valid UniversityChannelQueryDTO dto) {
    return R.ok(universityService.pageChannels(universityId, dto));
}

@GetMapping("/channel-options")
public R<List<ChannelOptionVO>> channelOptions() {
    return R.ok(universityService.listChannelOptions());
}
```

### 任务8：编译验证

- [ ] **Step 1: Maven编译**

Run: `mvn compile -pl haifeng-app -am -q` (from D:\0code\haifeng\backend\haifeng)
Expected: BUILD SUCCESS

