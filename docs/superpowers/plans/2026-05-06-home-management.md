# 首页管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现首页管理模块的三个子模块（公告列表、规划师列表、培训机构列表）的完整CRUD功能

**Architecture:** 采用标准三层架构，Entity和Mapper放在haifeng-common共用，Controller/Service/DTO/VO放在haifeng-admin。使用MyBatis-Plus简化数据库操作，雪花算法生成ID。

**删除策略：**
- **硬删除（DELETE接口）**：物理删除记录，数据不可恢复
- **软删除（状态切换）**：通过PUT /{id}/status接口设置status=0禁用，status=1启用

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL (TEXT[]数组类型), Flyway

---

## 文件结构

### haifeng-common (新增)
```
src/main/java/com/haifeng/common/
├── entity/home/
│   ├── Announcement.java      # 公告实体
│   ├── Planner.java           # 规划师实体
│   └── Institution.java       # 培训机构实体
└── mapper/home/
    ├── AnnouncementMapper.java
    ├── PlannerMapper.java
    └── InstitutionMapper.java
```

### haifeng-admin (新增)
```
src/main/java/com/haifeng/admin/
├── controller/home/
│   ├── AnnouncementController.java
│   ├── PlannerController.java
│   └── InstitutionController.java
├── service/home/
│   ├── AnnouncementService.java
│   ├── PlannerService.java
│   └── InstitutionService.java
├── service/impl/home/
│   ├── AnnouncementServiceImpl.java
│   ├── PlannerServiceImpl.java
│   └── InstitutionServiceImpl.java
├── dto/home/
│   ├── AnnouncementQueryDTO.java
│   ├── AnnouncementAddDTO.java
│   ├── AnnouncementUpdateDTO.java
│   ├── PlannerQueryDTO.java
│   ├── PlannerAddDTO.java
│   ├── PlannerUpdateDTO.java
│   ├── InstitutionQueryDTO.java
│   ├── InstitutionAddDTO.java
│   ├── InstitutionUpdateDTO.java
│   └── StatusDTO.java
└── vo/home/
    ├── AnnouncementListVO.java
    ├── AnnouncementDetailVO.java
    ├── PlannerListVO.java
    ├── PlannerDetailVO.java
    ├── InstitutionListVO.java
    └── InstitutionDetailVO.java
```

### 数据库迁移
```
haifeng-admin/src/main/resources/db/migration/
└── V4__create_content_tables.sql
```

### 文档
```
Products/
└── order4.md
```

---

## Task 1: 创建数据库迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V4__create_content_tables.sql`

- [ ] **Step 1: 创建V4迁移文件**

```sql
-- V4__create_content_tables.sql
-- 首页管理模块数据库表

-- ============================================
-- 1. 公告表 (t_announcements)
-- ============================================
CREATE TABLE t_announcements (
    id              BIGINT PRIMARY KEY,
    title           VARCHAR(100) NOT NULL,
    content         TEXT NOT NULL,
    tag             VARCHAR(20),
    status          SMALLINT DEFAULT 1 NOT NULL,
    is_deleted      BOOLEAN DEFAULT FALSE NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_announcements_title ON t_announcements(title) WHERE is_deleted = FALSE;
CREATE INDEX idx_announcements_status ON t_announcements(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_announcements IS '公告表';
COMMENT ON COLUMN t_announcements.id IS '公告ID(雪花算法)';
COMMENT ON COLUMN t_announcements.title IS '公告标题';
COMMENT ON COLUMN t_announcements.content IS '公告内容';
COMMENT ON COLUMN t_announcements.tag IS '公告类型标签';
COMMENT ON COLUMN t_announcements.status IS '状态: 0-下架 1-展示';
COMMENT ON COLUMN t_announcements.is_deleted IS '软删除标记';

-- ============================================
-- 2. 规划师表 (t_planners)
-- ============================================
CREATE TABLE t_planners (
    id                     BIGINT PRIMARY KEY,
    name                   VARCHAR(50) NOT NULL,
    position               VARCHAR(50),
    region                 VARCHAR(20),
    avatar                 VARCHAR(100),
    specialty              VARCHAR(100),
    douyin_name            VARCHAR(100),
    douyin_url             VARCHAR(100),
    personal_description   TEXT,
    experience_job         TEXT,
    achievements           TEXT[],
    expertise_areas        TEXT[],
    sort_order             INTEGER DEFAULT 0,
    status                 SMALLINT DEFAULT 1 NOT NULL,
    is_deleted             BOOLEAN DEFAULT FALSE NOT NULL,
    created_at             TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at             TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_planners_name ON t_planners(name) WHERE is_deleted = FALSE;
CREATE INDEX idx_planners_status ON t_planners(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_planners_sort ON t_planners(sort_order) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_planners IS '规划师表';
COMMENT ON COLUMN t_planners.id IS '规划师ID(雪花算法)';
COMMENT ON COLUMN t_planners.name IS '姓名';
COMMENT ON COLUMN t_planners.position IS '职位(如:高级规划师)';
COMMENT ON COLUMN t_planners.region IS '所在省份';
COMMENT ON COLUMN t_planners.avatar IS '头像URL';
COMMENT ON COLUMN t_planners.specialty IS '专长领域简述';
COMMENT ON COLUMN t_planners.douyin_name IS '抖音号名称';
COMMENT ON COLUMN t_planners.douyin_url IS '抖音主页链接';
COMMENT ON COLUMN t_planners.personal_description IS '个人简介';
COMMENT ON COLUMN t_planners.experience_job IS '从业经验';
COMMENT ON COLUMN t_planners.achievements IS '主要成就(数组)';
COMMENT ON COLUMN t_planners.expertise_areas IS '擅长领域(数组)';
COMMENT ON COLUMN t_planners.sort_order IS '排序(越小越靠前)';
COMMENT ON COLUMN t_planners.status IS '状态: 0-下架 1-展示';

-- ============================================
-- 3. 培训机构表 (t_institutions)
-- ============================================
CREATE TABLE t_institutions (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    address         VARCHAR(100),
    description     TEXT,
    courses         TEXT[],
    images          TEXT[],
    logo            VARCHAR(200),
    sort_order      INTEGER DEFAULT 0,
    status          SMALLINT DEFAULT 1 NOT NULL,
    is_deleted      BOOLEAN DEFAULT FALSE NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_institutions_name ON t_institutions(name) WHERE is_deleted = FALSE;
CREATE INDEX idx_institutions_type ON t_institutions(type) WHERE is_deleted = FALSE;
CREATE INDEX idx_institutions_status ON t_institutions(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_institutions_sort ON t_institutions(sort_order) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_institutions IS '培训机构表';
COMMENT ON COLUMN t_institutions.id IS '机构ID(雪花算法)';
COMMENT ON COLUMN t_institutions.name IS '机构名称';
COMMENT ON COLUMN t_institutions.type IS '机构类型';
COMMENT ON COLUMN t_institutions.phone IS '联系电话';
COMMENT ON COLUMN t_institutions.address IS '机构地址';
COMMENT ON COLUMN t_institutions.description IS '机构简介';
COMMENT ON COLUMN t_institutions.courses IS '课程列表(数组)';
COMMENT ON COLUMN t_institutions.images IS '机构图片(数组)';
COMMENT ON COLUMN t_institutions.logo IS 'Logo URL';
COMMENT ON COLUMN t_institutions.sort_order IS '排序(越小越靠前)';
COMMENT ON COLUMN t_institutions.status IS '状态: 0-下架 1-展示';
```

- [ ] **Step 2: 提交迁移文件**

```bash
git add haifeng-admin/src/main/resources/db/migration/V4__create_content_tables.sql
git commit -m "feat(db): 添加首页管理模块数据库表V4"
```

---

## Task 2: 创建Entity实体类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/home/Announcement.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/home/Planner.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/home/Institution.java`

- [ ] **Step 1: 创建Announcement实体**

```java
package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_announcements")
public class Announcement {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String content;

    private String tag;

    private Short status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建Planner实体**

```java
package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_planners", autoResultMap = true)
public class Planner {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String position;

    private String region;

    private String avatar;

    private String specialty;

    private String douyinName;

    private String douyinUrl;

    private String personalDescription;

    private String experienceJob;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> achievements;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> expertiseAreas;

    private Integer sortOrder;

    private Short status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建Institution实体**

```java
package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_institutions", autoResultMap = true)
public class Institution {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String type;

    private String phone;

    private String address;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> courses;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private String logo;

    private Integer sortOrder;

    private Short status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: 提交Entity**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/home/
git commit -m "feat(common): 添加首页管理模块Entity实体类"
```

---

## Task 3: 创建Mapper接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/home/AnnouncementMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/home/PlannerMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/home/InstitutionMapper.java`

- [ ] **Step 1: 创建AnnouncementMapper**

```java
package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
```

- [ ] **Step 2: 创建PlannerMapper**

```java
package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Planner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlannerMapper extends BaseMapper<Planner> {
}
```

- [ ] **Step 3: 创建InstitutionMapper**

```java
package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Institution;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstitutionMapper extends BaseMapper<Institution> {
}
```

- [ ] **Step 4: 提交Mapper**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/home/
git commit -m "feat(common): 添加首页管理模块Mapper接口"
```

---

## Task 4: 创建公告模块DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/AnnouncementQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/AnnouncementAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/AnnouncementUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/StatusDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/home/AnnouncementListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/home/AnnouncementDetailVO.java`

- [ ] **Step 1: 创建AnnouncementQueryDTO**

```java
package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryDTO extends BasePageQueryDTO {

    /**
     * 标题模糊查询
     */
    private String title;

    /**
     * 状态筛选: 0-下架 1-展示
     */
    private Short status;
}
```

- [ ] **Step 2: 创建AnnouncementAddDTO**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementAddDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 20, message = "标签最长20字符")
    private String tag;
}
```

- [ ] **Step 3: 创建AnnouncementUpdateDTO**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementUpdateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 20, message = "标签最长20字符")
    private String tag;
}
```

- [ ] **Step 4: 创建StatusDTO (通用状态DTO)**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusDTO {

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Short status;
}
```

- [ ] **Step 5: 创建AnnouncementListVO**

```java
package com.haifeng.admin.vo.home;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AnnouncementListVO {

    private Long id;

    private String title;

    private String tag;

    private Short status;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: 创建AnnouncementDetailVO**

```java
package com.haifeng.admin.vo.home;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AnnouncementDetailVO {

    private Long id;

    private String title;

    private String content;

    private String tag;

    private Short status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 7: 提交公告模块DTO和VO**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/AnnouncementQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/AnnouncementAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/AnnouncementUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/StatusDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/home/AnnouncementListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/home/AnnouncementDetailVO.java
git commit -m "feat(admin): 添加公告模块DTO和VO"
```

---

## Task 5: 创建公告模块Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/home/AnnouncementService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/home/AnnouncementServiceImpl.java`

- [ ] **Step 1: 创建AnnouncementService接口**

```java
package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.AnnouncementAddDTO;
import com.haifeng.admin.dto.home.AnnouncementQueryDTO;
import com.haifeng.admin.dto.home.AnnouncementUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.vo.home.AnnouncementDetailVO;
import com.haifeng.admin.vo.home.AnnouncementListVO;

public interface AnnouncementService {

    /**
     * 分页查询公告列表
     */
    IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto);

    /**
     * 获取公告详情
     */
    AnnouncementDetailVO detail(Long id);

    /**
     * 新增公告
     */
    Long add(AnnouncementAddDTO dto);

    /**
     * 修改公告
     */
    void update(Long id, AnnouncementUpdateDTO dto);

    /**
     * 修改公告状态
     */
    void updateStatus(Long id, StatusDTO dto);

    /**
     * 删除公告(软删除)
     */
    void delete(Long id);
}
```

- [ ] **Step 2: 创建AnnouncementServiceImpl实现**

```java
package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.AnnouncementAddDTO;
import com.haifeng.admin.dto.home.AnnouncementQueryDTO;
import com.haifeng.admin.dto.home.AnnouncementUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.AnnouncementService;
import com.haifeng.admin.vo.home.AnnouncementDetailVO;
import com.haifeng.admin.vo.home.AnnouncementListVO;
import com.haifeng.common.entity.home.Announcement;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.AnnouncementMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto) {
        Page<Announcement> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getDeleted, false);

        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.like(Announcement::getTitle, dto.getTitle());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Announcement::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(Announcement::getUpdatedAt);

        IPage<Announcement> announcementPage = announcementMapper.selectPage(page, wrapper);

        return announcementPage.convert(announcement -> {
            AnnouncementListVO vo = new AnnouncementListVO();
            BeanUtils.copyProperties(announcement, vo);
            return vo;
        });
    }

    @Override
    public AnnouncementDetailVO detail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        AnnouncementDetailVO vo = new AnnouncementDetailVO();
        BeanUtils.copyProperties(announcement, vo);
        return vo;
    }

    @Override
    public Long add(AnnouncementAddDTO dto) {
        Long id = SnowflakeIdGenerator.nextId();
        OffsetDateTime now = OffsetDateTime.now();

        Announcement announcement = Announcement.builder()
                .id(id)
                .title(dto.getTitle())
                .content(dto.getContent())
                .tag(dto.getTag())
                .status((short) 1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        announcementMapper.insert(announcement);
        log.info("新增公告成功: id={}, title={}", id, dto.getTitle());
        return id;
    }

    @Override
    public void update(Long id, AnnouncementUpdateDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setTag(dto.getTag());
        announcement.setUpdatedAt(OffsetDateTime.now());

        announcementMapper.updateById(announcement);
        log.info("修改公告成功: id={}", id);
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        announcement.setStatus(dto.getStatus());
        announcement.setUpdatedAt(OffsetDateTime.now());

        announcementMapper.updateById(announcement);
        log.info("修改公告状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(404, "公告不存在");
        }

        announcement.setDeleted(true);
        announcement.setUpdatedAt(OffsetDateTime.now());

        announcementMapper.updateById(announcement);
        log.info("删除公告成功: id={}", id);
    }
}
```

- [ ] **Step 3: 提交公告Service**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/home/AnnouncementService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/home/AnnouncementServiceImpl.java
git commit -m "feat(admin): 添加公告模块Service"
```

---

## Task 6: 创建公告模块Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/home/AnnouncementController.java`

- [ ] **Step 1: 创建AnnouncementController**

```java
package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.AnnouncementAddDTO;
import com.haifeng.admin.dto.home.AnnouncementQueryDTO;
import com.haifeng.admin.dto.home.AnnouncementUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.AnnouncementService;
import com.haifeng.admin.vo.home.AnnouncementDetailVO;
import com.haifeng.admin.vo.home.AnnouncementListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/home/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 分页查询公告列表
     */
    @GetMapping("/list")
    public R<IPage<AnnouncementListVO>> list(@Valid AnnouncementQueryDTO dto) {
        return R.ok(announcementService.page(dto));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public R<AnnouncementDetailVO> detail(@PathVariable Long id) {
        return R.ok(announcementService.detail(id));
    }

    /**
     * 新增公告
     */
    @PostMapping
    @OperationLog(module = "首页管理", action = "新增公告")
    public R<Long> add(@Valid @RequestBody AnnouncementAddDTO dto) {
        return R.ok(announcementService.add(dto));
    }

    /**
     * 修改公告
     */
    @PutMapping("/{id}")
    @OperationLog(module = "首页管理", action = "修改公告")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementUpdateDTO dto) {
        announcementService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改公告状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "首页管理", action = "修改公告状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        announcementService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "首页管理", action = "删除公告")
    public R<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交公告Controller**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/home/AnnouncementController.java
git commit -m "feat(admin): 添加公告模块Controller"
```

---

## Task 7: 创建规划师模块DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/PlannerQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/PlannerAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/PlannerUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/home/PlannerListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/home/PlannerDetailVO.java`

- [ ] **Step 1: 创建PlannerQueryDTO**

```java
package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /**
     * 姓名模糊查询
     */
    private String name;

    /**
     * 状态筛选: 0-下架 1-展示
     */
    private Short status;
}
```

- [ ] **Step 2: 创建PlannerAddDTO**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PlannerAddDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50字符")
    private String name;

    @Size(max = 50, message = "职位最长50字符")
    private String position;

    @Size(max = 20, message = "地区最长20字符")
    private String region;

    @Size(max = 100, message = "头像URL最长100字符")
    private String avatar;

    @Size(max = 100, message = "专长最长100字符")
    private String specialty;

    @Size(max = 100, message = "抖音名称最长100字符")
    private String douyinName;

    @Size(max = 100, message = "抖音链接最长100字符")
    private String douyinUrl;

    private String personalDescription;

    private String experienceJob;

    private List<String> achievements;

    private List<String> expertiseAreas;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建PlannerUpdateDTO**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PlannerUpdateDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50字符")
    private String name;

    @Size(max = 50, message = "职位最长50字符")
    private String position;

    @Size(max = 20, message = "地区最长20字符")
    private String region;

    @Size(max = 100, message = "头像URL最长100字符")
    private String avatar;

    @Size(max = 100, message = "专长最长100字符")
    private String specialty;

    @Size(max = 100, message = "抖音名称最长100字符")
    private String douyinName;

    @Size(max = 100, message = "抖音链接最长100字符")
    private String douyinUrl;

    private String personalDescription;

    private String experienceJob;

    private List<String> achievements;

    private List<String> expertiseAreas;

    private Integer sortOrder;
}
```

- [ ] **Step 4: 创建PlannerListVO**

```java
package com.haifeng.admin.vo.home;

import lombok.Data;

@Data
public class PlannerListVO {

    private Long id;

    private String name;

    private String position;

    private String region;

    private String avatar;

    private String specialty;

    private String douyinName;

    private String douyinUrl;

    private Integer sortOrder;

    private Short status;
}
```

- [ ] **Step 5: 创建PlannerDetailVO**

```java
package com.haifeng.admin.vo.home;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PlannerDetailVO {

    private Long id;

    private String name;

    private String position;

    private String region;

    private String avatar;

    private String specialty;

    private String douyinName;

    private String douyinUrl;

    private String personalDescription;

    private String experienceJob;

    private List<String> achievements;

    private List<String> expertiseAreas;

    private Integer sortOrder;

    private Short status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: 提交规划师DTO和VO**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/PlannerQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/PlannerAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/PlannerUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/home/PlannerListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/home/PlannerDetailVO.java
git commit -m "feat(admin): 添加规划师模块DTO和VO"
```

---

## Task 8: 创建规划师模块Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/home/PlannerService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/home/PlannerServiceImpl.java`

- [ ] **Step 1: 创建PlannerService接口**

```java
package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;

public interface PlannerService {

    /**
     * 分页查询规划师列表
     */
    IPage<PlannerListVO> page(PlannerQueryDTO dto);

    /**
     * 获取规划师详情
     */
    PlannerDetailVO detail(Long id);

    /**
     * 新增规划师
     */
    Long add(PlannerAddDTO dto);

    /**
     * 修改规划师
     */
    void update(Long id, PlannerUpdateDTO dto);

    /**
     * 修改规划师状态
     */
    void updateStatus(Long id, StatusDTO dto);

    /**
     * 删除规划师(软删除)
     */
    void delete(Long id);
}
```

- [ ] **Step 2: 创建PlannerServiceImpl实现**

```java
package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.PlannerService;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private final PlannerMapper plannerMapper;

    @Override
    public IPage<PlannerListVO> page(PlannerQueryDTO dto) {
        Page<Planner> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Planner::getDeleted, false);

        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Planner::getName, dto.getName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Planner::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(Planner::getSortOrder)
               .orderByDesc(Planner::getCreatedAt);

        IPage<Planner> plannerPage = plannerMapper.selectPage(page, wrapper);

        return plannerPage.convert(planner -> {
            PlannerListVO vo = new PlannerListVO();
            BeanUtils.copyProperties(planner, vo);
            return vo;
        });
    }

    @Override
    public PlannerDetailVO detail(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(planner, vo);
        return vo;
    }

    @Override
    public Long add(PlannerAddDTO dto) {
        Long id = SnowflakeIdGenerator.nextId();
        OffsetDateTime now = OffsetDateTime.now();

        Planner planner = Planner.builder()
                .id(id)
                .name(dto.getName())
                .position(dto.getPosition())
                .region(dto.getRegion())
                .avatar(dto.getAvatar())
                .specialty(dto.getSpecialty())
                .douyinName(dto.getDouyinName())
                .douyinUrl(dto.getDouyinUrl())
                .personalDescription(dto.getPersonalDescription())
                .experienceJob(dto.getExperienceJob())
                .achievements(dto.getAchievements())
                .expertiseAreas(dto.getExpertiseAreas())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        plannerMapper.insert(planner);
        log.info("新增规划师成功: id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    public void update(Long id, PlannerUpdateDTO dto) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        planner.setName(dto.getName());
        planner.setPosition(dto.getPosition());
        planner.setRegion(dto.getRegion());
        planner.setAvatar(dto.getAvatar());
        planner.setSpecialty(dto.getSpecialty());
        planner.setDouyinName(dto.getDouyinName());
        planner.setDouyinUrl(dto.getDouyinUrl());
        planner.setPersonalDescription(dto.getPersonalDescription());
        planner.setExperienceJob(dto.getExperienceJob());
        planner.setAchievements(dto.getAchievements());
        planner.setExpertiseAreas(dto.getExpertiseAreas());
        if (dto.getSortOrder() != null) {
            planner.setSortOrder(dto.getSortOrder());
        }
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);
        log.info("修改规划师成功: id={}", id);
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        planner.setStatus(dto.getStatus());
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);
        log.info("修改规划师状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        planner.setDeleted(true);
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);
        log.info("删除规划师成功: id={}", id);
    }
}
```

- [ ] **Step 3: 提交规划师Service**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/home/PlannerService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/home/PlannerServiceImpl.java
git commit -m "feat(admin): 添加规划师模块Service"
```

---

## Task 9: 创建规划师模块Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/home/PlannerController.java`

- [ ] **Step 1: 创建PlannerController**

```java
package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.PlannerService;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/home/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    /**
     * 分页查询规划师列表
     */
    @GetMapping("/list")
    public R<IPage<PlannerListVO>> list(@Valid PlannerQueryDTO dto) {
        return R.ok(plannerService.page(dto));
    }

    /**
     * 获取规划师详情
     */
    @GetMapping("/{id}")
    public R<PlannerDetailVO> detail(@PathVariable Long id) {
        return R.ok(plannerService.detail(id));
    }

    /**
     * 新增规划师
     */
    @PostMapping
    @OperationLog(module = "首页管理", action = "新增规划师")
    public R<Long> add(@Valid @RequestBody PlannerAddDTO dto) {
        return R.ok(plannerService.add(dto));
    }

    /**
     * 修改规划师
     */
    @PutMapping("/{id}")
    @OperationLog(module = "首页管理", action = "修改规划师")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PlannerUpdateDTO dto) {
        plannerService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改规划师状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "首页管理", action = "修改规划师状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        plannerService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 删除规划师
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "首页管理", action = "删除规划师")
    public R<Void> delete(@PathVariable Long id) {
        plannerService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交规划师Controller**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/home/PlannerController.java
git commit -m "feat(admin): 添加规划师模块Controller"
```

---

## Task 10: 创建培训机构模块DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/InstitutionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/InstitutionAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/home/InstitutionUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/home/InstitutionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/home/InstitutionDetailVO.java`

- [ ] **Step 1: 创建InstitutionQueryDTO**

```java
package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionQueryDTO extends BasePageQueryDTO {

    /**
     * 名称模糊查询
     */
    private String name;

    /**
     * 类型筛选
     */
    private String type;

    /**
     * 状态筛选: 0-下架 1-展示
     */
    private Short status;
}
```

- [ ] **Step 2: 创建InstitutionAddDTO**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionAddDTO {

    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称最长100字符")
    private String name;

    @NotBlank(message = "机构类型不能为空")
    @Size(max = 100, message = "机构类型最长100字符")
    private String type;

    @Size(max = 20, message = "联系电话最长20字符")
    private String phone;

    @Size(max = 100, message = "地址最长100字符")
    private String address;

    private String description;

    private List<String> courses;

    private List<String> images;

    @Size(max = 200, message = "Logo URL最长200字符")
    private String logo;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建InstitutionUpdateDTO**

```java
package com.haifeng.admin.dto.home;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionUpdateDTO {

    @NotBlank(message = "机构名称不能为空")
    @Size(max = 100, message = "机构名称最长100字符")
    private String name;

    @NotBlank(message = "机构类型不能为空")
    @Size(max = 100, message = "机构类型最长100字符")
    private String type;

    @Size(max = 20, message = "联系电话最长20字符")
    private String phone;

    @Size(max = 100, message = "地址最长100字符")
    private String address;

    private String description;

    private List<String> courses;

    private List<String> images;

    @Size(max = 200, message = "Logo URL最长200字符")
    private String logo;

    private Integer sortOrder;
}
```

- [ ] **Step 4: 创建InstitutionListVO**

```java
package com.haifeng.admin.vo.home;

import lombok.Data;

@Data
public class InstitutionListVO {

    private Long id;

    private String name;

    private String type;

    private String phone;

    private String address;

    private String logo;

    private Integer sortOrder;

    private Short status;
}
```

- [ ] **Step 5: 创建InstitutionDetailVO**

```java
package com.haifeng.admin.vo.home;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class InstitutionDetailVO {

    private Long id;

    private String name;

    private String type;

    private String phone;

    private String address;

    private String description;

    private List<String> courses;

    private List<String> images;

    private String logo;

    private Integer sortOrder;

    private Short status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: 提交培训机构DTO和VO**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/InstitutionQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/InstitutionAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/home/InstitutionUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/home/InstitutionListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/home/InstitutionDetailVO.java
git commit -m "feat(admin): 添加培训机构模块DTO和VO"
```

---

## Task 11: 创建培训机构模块Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/home/InstitutionService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/home/InstitutionServiceImpl.java`

- [ ] **Step 1: 创建InstitutionService接口**

```java
package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.InstitutionAddDTO;
import com.haifeng.admin.dto.home.InstitutionQueryDTO;
import com.haifeng.admin.dto.home.InstitutionUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.vo.home.InstitutionDetailVO;
import com.haifeng.admin.vo.home.InstitutionListVO;

public interface InstitutionService {

    /**
     * 分页查询培训机构列表
     */
    IPage<InstitutionListVO> page(InstitutionQueryDTO dto);

    /**
     * 获取培训机构详情
     */
    InstitutionDetailVO detail(Long id);

    /**
     * 新增培训机构
     */
    Long add(InstitutionAddDTO dto);

    /**
     * 修改培训机构
     */
    void update(Long id, InstitutionUpdateDTO dto);

    /**
     * 修改培训机构状态
     */
    void updateStatus(Long id, StatusDTO dto);

    /**
     * 删除培训机构(软删除)
     */
    void delete(Long id);
}
```

- [ ] **Step 2: 创建InstitutionServiceImpl实现**

```java
package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.InstitutionAddDTO;
import com.haifeng.admin.dto.home.InstitutionQueryDTO;
import com.haifeng.admin.dto.home.InstitutionUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.InstitutionService;
import com.haifeng.admin.vo.home.InstitutionDetailVO;
import com.haifeng.admin.vo.home.InstitutionListVO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionMapper institutionMapper;

    @Override
    public IPage<InstitutionListVO> page(InstitutionQueryDTO dto) {
        Page<Institution> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Institution::getDeleted, false);

        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Institution::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(Institution::getType, dto.getType());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Institution::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(Institution::getSortOrder)
               .orderByDesc(Institution::getCreatedAt);

        IPage<Institution> institutionPage = institutionMapper.selectPage(page, wrapper);

        return institutionPage.convert(institution -> {
            InstitutionListVO vo = new InstitutionListVO();
            BeanUtils.copyProperties(institution, vo);
            return vo;
        });
    }

    @Override
    public InstitutionDetailVO detail(Long id) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        InstitutionDetailVO vo = new InstitutionDetailVO();
        BeanUtils.copyProperties(institution, vo);
        return vo;
    }

    @Override
    public Long add(InstitutionAddDTO dto) {
        Long id = SnowflakeIdGenerator.nextId();
        OffsetDateTime now = OffsetDateTime.now();

        Institution institution = Institution.builder()
                .id(id)
                .name(dto.getName())
                .type(dto.getType())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .courses(dto.getCourses())
                .images(dto.getImages())
                .logo(dto.getLogo())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        institutionMapper.insert(institution);
        log.info("新增培训机构成功: id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    public void update(Long id, InstitutionUpdateDTO dto) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        institution.setName(dto.getName());
        institution.setType(dto.getType());
        institution.setPhone(dto.getPhone());
        institution.setAddress(dto.getAddress());
        institution.setDescription(dto.getDescription());
        institution.setCourses(dto.getCourses());
        institution.setImages(dto.getImages());
        institution.setLogo(dto.getLogo());
        if (dto.getSortOrder() != null) {
            institution.setSortOrder(dto.getSortOrder());
        }
        institution.setUpdatedAt(OffsetDateTime.now());

        institutionMapper.updateById(institution);
        log.info("修改培训机构成功: id={}", id);
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        institution.setStatus(dto.getStatus());
        institution.setUpdatedAt(OffsetDateTime.now());

        institutionMapper.updateById(institution);
        log.info("修改培训机构状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        institution.setDeleted(true);
        institution.setUpdatedAt(OffsetDateTime.now());

        institutionMapper.updateById(institution);
        log.info("删除培训机构成功: id={}", id);
    }
}
```

- [ ] **Step 3: 提交培训机构Service**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/home/InstitutionService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/home/InstitutionServiceImpl.java
git commit -m "feat(admin): 添加培训机构模块Service"
```

---

## Task 12: 创建培训机构模块Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/home/InstitutionController.java`

- [ ] **Step 1: 创建InstitutionController**

```java
package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.InstitutionAddDTO;
import com.haifeng.admin.dto.home.InstitutionQueryDTO;
import com.haifeng.admin.dto.home.InstitutionUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.InstitutionService;
import com.haifeng.admin.vo.home.InstitutionDetailVO;
import com.haifeng.admin.vo.home.InstitutionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/home/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    /**
     * 分页查询培训机构列表
     */
    @GetMapping("/list")
    public R<IPage<InstitutionListVO>> list(@Valid InstitutionQueryDTO dto) {
        return R.ok(institutionService.page(dto));
    }

    /**
     * 获取培训机构详情
     */
    @GetMapping("/{id}")
    public R<InstitutionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionService.detail(id));
    }

    /**
     * 新增培训机构
     */
    @PostMapping
    @OperationLog(module = "首页管理", action = "新增培训机构")
    public R<Long> add(@Valid @RequestBody InstitutionAddDTO dto) {
        return R.ok(institutionService.add(dto));
    }

    /**
     * 修改培训机构
     */
    @PutMapping("/{id}")
    @OperationLog(module = "首页管理", action = "修改培训机构")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody InstitutionUpdateDTO dto) {
        institutionService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改培训机构状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "首页管理", action = "修改培训机构状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        institutionService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 删除培训机构
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "首页管理", action = "删除培训机构")
    public R<Void> delete(@PathVariable Long id) {
        institutionService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交培训机构Controller**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/home/InstitutionController.java
git commit -m "feat(admin): 添加培训机构模块Controller"
```

---

## Task 13: 创建order4.md接口文档

**Files:**
- Create: `Products/order4.md`

- [ ] **Step 1: 创建order4.md**

```markdown
# 首页管理模块实施报告

## 模块概述

首页管理模块包含三个子模块：
- 公告列表 - 管理系统公告
- 规划师列表 - 管理高考志愿规划师信息
- 培训机构列表 - 管理合作培训机构信息

---

## API 接口文档

### 路由前缀
`/api/v1/admin/home/`

---

### 1. 公告管理接口

#### 1.1 分页查询公告列表
```
GET /api/v1/admin/home/announcement/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 否 | 标题模糊查询 |
| status | Short | 否 | 状态: 0-下架 1-展示 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "title": "系统升级公告",
        "tag": "系统",
        "status": 1,
        "updatedAt": "2026-05-06T10:00:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

---

#### 1.2 获取公告详情
```
GET /api/v1/admin/home/announcement/{id}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890,
    "title": "系统升级公告",
    "content": "系统将于今晚进行升级...",
    "tag": "系统",
    "status": 1,
    "createdAt": "2026-05-06T10:00:00+08:00",
    "updatedAt": "2026-05-06T10:00:00+08:00"
  }
}
```

---

#### 1.3 新增公告
```
POST /api/v1/admin/home/announcement
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 标题(最长100字符) |
| content | String | 是 | 内容 |
| tag | String | 否 | 标签(最长20字符) |

**请求示例：**
```json
{
  "title": "系统升级公告",
  "content": "系统将于今晚进行升级...",
  "tag": "系统"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890
}
```

---

#### 1.4 修改公告
```
PUT /api/v1/admin/home/announcement/{id}
Content-Type: application/json
```

**请求参数：** 同新增公告

---

#### 1.5 修改公告状态
```
PUT /api/v1/admin/home/announcement/{id}/status
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 状态: 0-下架 1-展示 |

---

#### 1.6 删除公告
```
DELETE /api/v1/admin/home/announcement/{id}
```

---

### 2. 规划师管理接口

#### 2.1 分页查询规划师列表
```
GET /api/v1/admin/home/planner/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 姓名模糊查询 |
| status | Short | 否 | 状态: 0-下架 1-展示 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "name": "张老师",
        "position": "高级规划师",
        "region": "广东",
        "avatar": "/uploads/avatar/1.jpg",
        "specialty": "新高考选科",
        "douyinName": "未来规划院",
        "douyinUrl": "https://douyin.com/xxx",
        "sortOrder": 1,
        "status": 1
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

---

#### 2.2 获取规划师详情
```
GET /api/v1/admin/home/planner/{id}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890,
    "name": "张老师",
    "position": "高级规划师",
    "region": "广东",
    "avatar": "/uploads/avatar/1.jpg",
    "specialty": "新高考选科",
    "douyinName": "未来规划院",
    "douyinUrl": "https://douyin.com/xxx",
    "personalDescription": "从事高考志愿规划10年...",
    "experienceJob": "10年高考志愿规划经验",
    "achievements": ["帮助500+学生录取985", "省级优秀规划师"],
    "expertiseAreas": ["新高考选科", "艺术类志愿", "强基计划"],
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-05-06T10:00:00+08:00",
    "updatedAt": "2026-05-06T10:00:00+08:00"
  }
}
```

---

#### 2.3 新增规划师
```
POST /api/v1/admin/home/planner
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名(最长50字符) |
| position | String | 否 | 职位 |
| region | String | 否 | 省份 |
| avatar | String | 否 | 头像URL |
| specialty | String | 否 | 专长领域 |
| douyinName | String | 否 | 抖音号名称 |
| douyinUrl | String | 否 | 抖音主页链接 |
| personalDescription | String | 否 | 个人简介 |
| experienceJob | String | 否 | 从业经验 |
| achievements | String[] | 否 | 主要成就 |
| expertiseAreas | String[] | 否 | 擅长领域 |
| sortOrder | Integer | 否 | 排序(越小越靠前) |

---

#### 2.4 修改规划师
```
PUT /api/v1/admin/home/planner/{id}
Content-Type: application/json
```

**请求参数：** 同新增规划师

---

#### 2.5 修改规划师状态
```
PUT /api/v1/admin/home/planner/{id}/status
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 状态: 0-下架 1-展示 |

---

#### 2.6 删除规划师
```
DELETE /api/v1/admin/home/planner/{id}
```

---

### 3. 培训机构管理接口

#### 3.1 分页查询培训机构列表
```
GET /api/v1/admin/home/institution/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 名称模糊查询 |
| type | String | 否 | 类型筛选 |
| status | Short | 否 | 状态: 0-下架 1-展示 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "name": "新东方",
        "type": "语言培训",
        "phone": "400-123-4567",
        "address": "北京市海淀区",
        "logo": "/uploads/logo/1.jpg",
        "sortOrder": 1,
        "status": 1
      }
    ],
    "total": 30,
    "size": 10,
    "current": 1,
    "pages": 3
  }
}
```

---

#### 3.2 获取培训机构详情
```
GET /api/v1/admin/home/institution/{id}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890,
    "name": "新东方",
    "type": "语言培训",
    "phone": "400-123-4567",
    "address": "北京市海淀区",
    "description": "新东方教育科技集团...",
    "courses": ["雅思冲刺班", "托福强化班", "日语N1考前班"],
    "images": ["/uploads/inst/1.jpg", "/uploads/inst/2.jpg"],
    "logo": "/uploads/logo/1.jpg",
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-05-06T10:00:00+08:00",
    "updatedAt": "2026-05-06T10:00:00+08:00"
  }
}
```

---

#### 3.3 新增培训机构
```
POST /api/v1/admin/home/institution
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 机构名称(最长100字符) |
| type | String | 是 | 机构类型 |
| phone | String | 否 | 联系电话 |
| address | String | 否 | 机构地址 |
| description | String | 否 | 机构简介 |
| courses | String[] | 否 | 课程列表 |
| images | String[] | 否 | 机构图片 |
| logo | String | 否 | Logo URL |
| sortOrder | Integer | 否 | 排序(越小越靠前) |

---

#### 3.4 修改培训机构
```
PUT /api/v1/admin/home/institution/{id}
Content-Type: application/json
```

**请求参数：** 同新增培训机构

---

#### 3.5 修改培训机构状态
```
PUT /api/v1/admin/home/institution/{id}/status
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 状态: 0-下架 1-展示 |

---

#### 3.6 删除培训机构
```
DELETE /api/v1/admin/home/institution/{id}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据库表结构

### t_announcements (公告表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 公告ID(雪花算法) |
| title | VARCHAR(100) | 标题 |
| content | TEXT | 内容 |
| tag | VARCHAR(20) | 标签 |
| status | SMALLINT | 状态: 0-下架 1-展示 |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_planners (规划师表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 规划师ID(雪花算法) |
| name | VARCHAR(50) | 姓名 |
| position | VARCHAR(50) | 职位 |
| region | VARCHAR(20) | 省份 |
| avatar | VARCHAR(100) | 头像URL |
| specialty | VARCHAR(100) | 专长领域 |
| douyin_name | VARCHAR(100) | 抖音号名称 |
| douyin_url | VARCHAR(100) | 抖音链接 |
| personal_description | TEXT | 个人简介 |
| experience_job | TEXT | 从业经验 |
| achievements | TEXT[] | 主要成就 |
| expertise_areas | TEXT[] | 擅长领域 |
| sort_order | INTEGER | 排序 |
| status | SMALLINT | 状态 |
| is_deleted | BOOLEAN | 软删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_institutions (培训机构表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 机构ID(雪花算法) |
| name | VARCHAR(100) | 机构名称 |
| type | VARCHAR(100) | 机构类型 |
| phone | VARCHAR(20) | 联系电话 |
| address | VARCHAR(100) | 机构地址 |
| description | TEXT | 机构简介 |
| courses | TEXT[] | 课程列表 |
| images | TEXT[] | 机构图片 |
| logo | VARCHAR(200) | Logo URL |
| sort_order | INTEGER | 排序 |
| status | SMALLINT | 状态 |
| is_deleted | BOOLEAN | 软删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

---

## 文件清单

### haifeng-common
- `entity/home/Announcement.java`
- `entity/home/Planner.java`
- `entity/home/Institution.java`
- `mapper/home/AnnouncementMapper.java`
- `mapper/home/PlannerMapper.java`
- `mapper/home/InstitutionMapper.java`

### haifeng-admin
- `controller/home/AnnouncementController.java`
- `controller/home/PlannerController.java`
- `controller/home/InstitutionController.java`
- `service/home/AnnouncementService.java`
- `service/home/PlannerService.java`
- `service/home/InstitutionService.java`
- `service/impl/home/AnnouncementServiceImpl.java`
- `service/impl/home/PlannerServiceImpl.java`
- `service/impl/home/InstitutionServiceImpl.java`
- `dto/home/AnnouncementQueryDTO.java`
- `dto/home/AnnouncementAddDTO.java`
- `dto/home/AnnouncementUpdateDTO.java`
- `dto/home/PlannerQueryDTO.java`
- `dto/home/PlannerAddDTO.java`
- `dto/home/PlannerUpdateDTO.java`
- `dto/home/InstitutionQueryDTO.java`
- `dto/home/InstitutionAddDTO.java`
- `dto/home/InstitutionUpdateDTO.java`
- `dto/home/StatusDTO.java`
- `vo/home/AnnouncementListVO.java`
- `vo/home/AnnouncementDetailVO.java`
- `vo/home/PlannerListVO.java`
- `vo/home/PlannerDetailVO.java`
- `vo/home/InstitutionListVO.java`
- `vo/home/InstitutionDetailVO.java`

### 数据库迁移
- `db/migration/V4__create_content_tables.sql`
```

- [ ] **Step 2: 提交order4.md**

```bash
git add Products/order4.md
git commit -m "docs: 添加首页管理模块接口文档order4.md"
```

---

## Task 14: 编译验证

- [ ] **Step 1: 编译项目验证无错误**

```bash
cd D:/exeProject/ideaProjects/Project-HaiFeng
mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 最终提交**

```bash
git add -A
git commit -m "feat: 完成首页管理模块(公告/规划师/培训机构)"
```
