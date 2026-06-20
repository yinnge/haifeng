# 招聘内容管理 App 端查询 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** App 端实现统一备考指南（t_exam_guide）和统一公告（t_notice）的列表和详情查询接口

**Architecture:** 在 haifeng-common 添加 Entity + Mapper，在 haifeng-app 添加 Controller + Service + Impl + DTO + VO，遵循现有 `jobIndex` 子包模式

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + PostgreSQL (TEXT[] 数组用 String[] 映射)

---

### Task 1: 创建 Entity（haifeng-common）

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/contentManagement/ExamGuide.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/employment/contentManagement/Notice.java`

- [ ] **Step 1: 创建 ExamGuide.java**

```java
package com.haifeng.common.entity.employment.contentManagement;

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
@TableName(value = "t_exam_guide", autoResultMap = true)
public class ExamGuide implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private String guideCategory;

    private String guideType;

    private String title;

    private String subtitle;

    private String coverImage;

    private String iconClass;

    private String summary;

    private String content;

    private String[] tags;

    private String difficultyLevel;

    private String targetAudience;

    private String authorName;

    private String authorTitle;

    private Boolean isTop;

    private Boolean isRecommended;

    private Integer sortOrder;

    private Integer viewCount;

    private Integer likeCount;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 Notice.java**

```java
package com.haifeng.common.entity.employment.contentManagement;

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
@TableName(value = "t_notice", autoResultMap = true)
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private String noticeCategory;

    private String noticeType;

    private String title;

    private String summary;

    private String content;

    private String province;

    private String city;

    private String[] tags;

    private String year;

    private String source;

    private String sourceUrl;

    private OffsetDateTime publishDate;

    private String publishUnit;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private OffsetDateTime examTime;

    private Integer recruitmentCount;

    private Boolean isTop;

    private Boolean isImportant;

    private Integer viewCount;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

---

### Task 2: 创建 Mapper（haifeng-common）

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/contentManagement/ExamGuideMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/employment/contentManagement/NoticeMapper.java`

- [ ] **Step 1: 创建 ExamGuideMapper.java**

```java
package com.haifeng.common.mapper.employment.contentManagement;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamGuideMapper extends BaseMapper<ExamGuide> {
}
```

- [ ] **Step 2: 创建 NoticeMapper.java**

```java
package com.haifeng.common.mapper.employment.contentManagement;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
```

---

### Task 3: 创建查询 DTO（haifeng-app）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/contentManagement/examGuide/ExamGuideQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/employment/contentManagement/notice/NoticeQueryDTO.java`

- [ ] **Step 1: 创建 ExamGuideQueryDTO.java**

```java
package com.haifeng.app.dto.employment.contentManagement.examGuide;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamGuideQueryDTO extends BasePageQueryDTO {

    private String title;

    private String subtitle;

    private String guideCategory;

    private String guideType;

    private String difficultyLevel;

    private String authorTitle;

    private String authorName;
}
```

- [ ] **Step 2: 创建 NoticeQueryDTO.java**

```java
package com.haifeng.app.dto.employment.contentManagement.notice;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeQueryDTO extends BasePageQueryDTO {

    private String title;

    private String summary;

    private String source;

    private String noticeCategory;

    private String noticeType;

    private String province;

    private String city;

    private String year;
}
```

---

### Task 4: 创建 VO（haifeng-app）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/contentManagement/examGuide/ExamGuideListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/contentManagement/examGuide/ExamGuideDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/contentManagement/notice/NoticeListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/employment/contentManagement/notice/NoticeDetailVO.java`

- [ ] **Step 1: 创建 ExamGuideListVO.java**

```java
package com.haifeng.app.vo.employment.contentManagement.examGuide;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamGuideListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String guideCategory;

    private String guideType;

    private String title;

    private String subtitle;

    private String[] tags;

    private String authorName;

    private String authorTitle;
}
```

- [ ] **Step 2: 创建 ExamGuideDetailVO.java**

```java
package com.haifeng.app.vo.employment.contentManagement.examGuide;

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
public class ExamGuideDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String guideCategory;

    private String guideType;

    private String title;

    private String subtitle;

    private String coverImage;

    private String iconClass;

    private String summary;

    private String content;

    private String[] tags;

    private String difficultyLevel;

    private String targetAudience;

    private String authorName;

    private String authorTitle;

    private Boolean isTop;

    private Boolean isRecommended;

    private Integer sortOrder;

    private Integer viewCount;

    private Integer likeCount;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 NoticeListVO.java**

```java
package com.haifeng.app.vo.employment.contentManagement.notice;

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
public class NoticeListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String summary;

    private OffsetDateTime publishDate;

    private Integer viewCount;

    private String noticeCategory;

    private String province;

    private String city;

    private String year;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private Integer recruitmentCount;
}
```

- [ ] **Step 4: 创建 NoticeDetailVO.java**

```java
package com.haifeng.app.vo.employment.contentManagement.notice;

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
public class NoticeDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String noticeCategory;

    private String noticeType;

    private String title;

    private String summary;

    private String content;

    private String province;

    private String city;

    private String[] tags;

    private String year;

    private String source;

    private String sourceUrl;

    private OffsetDateTime publishDate;

    private String publishUnit;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private OffsetDateTime examTime;

    private Integer recruitmentCount;

    private Boolean isTop;

    private Boolean isImportant;

    private Integer viewCount;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

---

### Task 5: 创建 Service 接口和实现（haifeng-app）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/employment/contentManagement/examGuide/ExamGuideService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/employment/contentManagement/notice/NoticeService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/contentManagement/examGuide/ExamGuideServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/employment/contentManagement/notice/NoticeServiceImpl.java`

- [ ] **Step 1: 创建 ExamGuideService.java**

```java
package com.haifeng.app.service.employment.contentManagement.examGuide;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO;

public interface ExamGuideService {

    IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto);

    ExamGuideDetailVO detail(Long id);
}
```

- [ ] **Step 2: 创建 NoticeService.java**

```java
package com.haifeng.app.service.employment.contentManagement.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeListVO;

public interface NoticeService {

    IPage<NoticeListVO> page(NoticeQueryDTO dto);

    NoticeDetailVO detail(Long id);
}
```

- [ ] **Step 3: 创建 ExamGuideServiceImpl.java**

```java
package com.haifeng.app.service.impl.employment.contentManagement.examGuide;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGuideServiceImpl implements ExamGuideService {

    private final ExamGuideMapper examGuideMapper;

    @Override
    public IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto) {
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSubtitle())) {
            wrapper.and(w -> {
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(ExamGuide::getTitle, dto.getTitle());
                }
                if (StrUtil.isNotBlank(dto.getSubtitle())) {
                    if (StrUtil.isNotBlank(dto.getTitle())) {
                        w.or();
                    }
                    w.like(ExamGuide::getSubtitle, dto.getSubtitle());
                }
            });
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getGuideCategory()), ExamGuide::getGuideCategory, dto.getGuideCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getGuideType()), ExamGuide::getGuideType, dto.getGuideType());
        wrapper.eq(StrUtil.isNotBlank(dto.getDifficultyLevel()), ExamGuide::getDifficultyLevel, dto.getDifficultyLevel());
        wrapper.eq(StrUtil.isNotBlank(dto.getAuthorTitle()), ExamGuide::getAuthorTitle, dto.getAuthorTitle());
        wrapper.eq(StrUtil.isNotBlank(dto.getAuthorName()), ExamGuide::getAuthorName, dto.getAuthorName());

        wrapper.orderByDesc(ExamGuide::getSortOrder).last("NULLS LAST");
        wrapper.orderByDesc(ExamGuide::getCreatedAt);

        Page<ExamGuide> page = new Page<>(dto.getPage(), dto.getSize());
        examGuideMapper.selectPage(page, wrapper);

        return page.convert(guide -> ExamGuideListVO.builder()
                .id(guide.getId())
                .guideCategory(guide.getGuideCategory())
                .guideType(guide.getGuideType())
                .title(guide.getTitle())
                .subtitle(guide.getSubtitle())
                .tags(guide.getTags())
                .authorName(guide.getAuthorName())
                .authorTitle(guide.getAuthorTitle())
                .build());
    }

    @Override
    public ExamGuideDetailVO detail(Long id) {
        ExamGuide guide = examGuideMapper.selectById(id);
        if (guide == null || Boolean.TRUE.equals(guide.getIsDeleted())) {
            log.warn("备考指南不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return ExamGuideDetailVO.builder()
                .id(guide.getId())
                .guideCategory(guide.getGuideCategory())
                .guideType(guide.getGuideType())
                .title(guide.getTitle())
                .subtitle(guide.getSubtitle())
                .coverImage(guide.getCoverImage())
                .iconClass(guide.getIconClass())
                .summary(guide.getSummary())
                .content(guide.getContent())
                .tags(guide.getTags())
                .difficultyLevel(guide.getDifficultyLevel())
                .targetAudience(guide.getTargetAudience())
                .authorName(guide.getAuthorName())
                .authorTitle(guide.getAuthorTitle())
                .isTop(guide.getIsTop())
                .isRecommended(guide.getIsRecommended())
                .sortOrder(guide.getSortOrder())
                .viewCount(guide.getViewCount())
                .likeCount(guide.getLikeCount())
                .createdAt(guide.getCreatedAt())
                .updatedAt(guide.getUpdatedAt())
                .build();
    }
}
```

- [ ] **Step 4: 创建 NoticeServiceImpl.java**

```java
package com.haifeng.app.service.impl.employment.contentManagement.notice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeListVO;
import com.haifeng.common.entity.employment.contentManagement.Notice;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.NoticeMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public IPage<NoticeListVO> page(NoticeQueryDTO dto) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSummary()) || StrUtil.isNotBlank(dto.getSource())) {
            wrapper.and(w -> {
                boolean hasPrev = false;
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(Notice::getTitle, dto.getTitle());
                    hasPrev = true;
                }
                if (StrUtil.isNotBlank(dto.getSummary())) {
                    if (hasPrev) { w.or(); }
                    w.like(Notice::getSummary, dto.getSummary());
                    hasPrev = true;
                }
                if (StrUtil.isNotBlank(dto.getSource())) {
                    if (hasPrev) { w.or(); }
                    w.like(Notice::getSource, dto.getSource());
                }
            });
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getNoticeCategory()), Notice::getNoticeCategory, dto.getNoticeCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getNoticeType()), Notice::getNoticeType, dto.getNoticeType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), Notice::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), Notice::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), Notice::getYear, dto.getYear());

        wrapper.orderByDesc(Notice::getIsTop);
        wrapper.orderByDesc(Notice::getPublishDate).last("NULLS LAST");

        Page<Notice> page = new Page<>(dto.getPage(), dto.getSize());
        noticeMapper.selectPage(page, wrapper);

        return page.convert(notice -> NoticeListVO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .summary(notice.getSummary())
                .publishDate(notice.getPublishDate())
                .viewCount(notice.getViewCount())
                .noticeCategory(notice.getNoticeCategory())
                .province(notice.getProvince())
                .city(notice.getCity())
                .year(notice.getYear())
                .regStartDate(notice.getRegStartDate())
                .regEndDate(notice.getRegEndDate())
                .recruitmentCount(notice.getRecruitmentCount())
                .build());
    }

    @Override
    public NoticeDetailVO detail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || Boolean.TRUE.equals(notice.getIsDeleted())) {
            log.warn("公告不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return NoticeDetailVO.builder()
                .id(notice.getId())
                .noticeCategory(notice.getNoticeCategory())
                .noticeType(notice.getNoticeType())
                .title(notice.getTitle())
                .summary(notice.getSummary())
                .content(notice.getContent())
                .province(notice.getProvince())
                .city(notice.getCity())
                .tags(notice.getTags())
                .year(notice.getYear())
                .source(notice.getSource())
                .sourceUrl(notice.getSourceUrl())
                .publishDate(notice.getPublishDate())
                .publishUnit(notice.getPublishUnit())
                .regStartDate(notice.getRegStartDate())
                .regEndDate(notice.getRegEndDate())
                .examTime(notice.getExamTime())
                .recruitmentCount(notice.getRecruitmentCount())
                .isTop(notice.getIsTop())
                .isImportant(notice.getIsImportant())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
```

---

### Task 6: 创建 Controller（haifeng-app）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/contentManagement/examGuide/ExamGuideController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/employment/contentManagement/notice/NoticeController.java`

- [ ] **Step 1: 创建 ExamGuideController.java**

```java
package com.haifeng.app.controller.employment.contentManagement.examGuide;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO;
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
@RequestMapping("/api/v1/app/employment/content/exam-guide")
@RequiredArgsConstructor
public class ExamGuideController {

    private final ExamGuideService examGuideService;

    @GetMapping("/list")
    public R<IPage<ExamGuideListVO>> list(@Valid ExamGuideQueryDTO dto) {
        return R.ok(examGuideService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<ExamGuideDetailVO> detail(@PathVariable Long id) {
        return R.ok(examGuideService.detail(id));
    }
}
```

- [ ] **Step 2: 创建 NoticeController.java**

```java
package com.haifeng.app.controller.employment.contentManagement.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeListVO;
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
@RequestMapping("/api/v1/app/employment/content/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<NoticeListVO>> list(@Valid NoticeQueryDTO dto) {
        return R.ok(noticeService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<NoticeDetailVO> detail(@PathVariable Long id) {
        return R.ok(noticeService.detail(id));
    }
}
```

---

### Task 7: 编译验证

- [ ] **Step 1: Maven 编译检查**

Run: `cd D:\0code\haifeng\backend\haifeng && mvn compile -q`
Expected: BUILD SUCCESS
