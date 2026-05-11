# 特殊通道模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现特殊招生通道管理模块的后台CRUD功能，包含4个子模块。

**Architecture:** 采用标准的Controller-Service-Mapper三层架构，Entity和Mapper放在haifeng-common共用，Controller/Service/DTO/VO放在haifeng-admin。每个子模块独立Controller，使用雪花算法生成ID，硬删除策略。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL, Flyway

**Design Spec:** `docs/superpowers/specs/2026-05-11-special-channel-design.md`

---

## Task 1: Flyway数据库迁移

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V14__t_special_channel__tables.sql`

- [ ] **Step 1: 创建V14迁移文件**

```sql
-- V14__t_special_channel__tables.sql
-- 特殊通道模块数据库表

-- ============================================================
-- 1. 特殊招生通道内容表 (t_special_channel)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_special_channel (
    id                  BIGINT          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL UNIQUE,
    channel_name        VARCHAR(50)     NOT NULL,
    subtitle            VARCHAR(200),
    parent_code         VARCHAR(30),
    filter_label        VARCHAR(30),
    display_type        VARCHAR(20)     NOT NULL,
    content             TEXT,
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_special_channel IS '特殊招生通道内容表';
COMMENT ON COLUMN t_special_channel.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_special_channel.channel_code IS '通道代码(唯一)';
COMMENT ON COLUMN t_special_channel.channel_name IS '通道名称';
COMMENT ON COLUMN t_special_channel.subtitle IS '副标题';
COMMENT ON COLUMN t_special_channel.parent_code IS '父级通道代码';
COMMENT ON COLUMN t_special_channel.filter_label IS '筛选按钮文字';
COMMENT ON COLUMN t_special_channel.display_type IS '展示类型: UNIVERSITY_LIST/ARTICLE_ONLY/MAJOR_DATA/GROUP';
COMMENT ON COLUMN t_special_channel.content IS '富文本内容(HTML)';
COMMENT ON COLUMN t_special_channel.sort_order IS '排序权重';
COMMENT ON COLUMN t_special_channel.is_active IS '是否启用';

CREATE INDEX idx_sc_display_type ON t_special_channel(display_type) WHERE is_active = TRUE;
CREATE INDEX idx_sc_parent ON t_special_channel(parent_code) WHERE is_active = TRUE;
CREATE INDEX idx_sc_name ON t_special_channel(channel_name);

-- ============================================================
-- 2. 通道-大学关联表 (t_special_channel_university)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_special_channel_university (
    id                  BIGINT          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL,
    channel_name        VARCHAR(50)     NOT NULL,
    university_id       BIGINT          NOT NULL,
    university_name     VARCHAR(50)     NOT NULL,
    year                SMALLINT,
    region_tag          VARCHAR(20),
    signup_start        TIMESTAMPTZ,
    signup_end          TIMESTAMPTZ,
    official_url        VARCHAR(500),
    brochure_title      VARCHAR(200),
    brochure_content    TEXT,
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_channel_univ UNIQUE (channel_code, university_id, year)
);

COMMENT ON TABLE t_special_channel_university IS '通道-大学关联表';
COMMENT ON COLUMN t_special_channel_university.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_special_channel_university.channel_code IS '通道代码';
COMMENT ON COLUMN t_special_channel_university.channel_name IS '通道名称(冗余)';
COMMENT ON COLUMN t_special_channel_university.university_id IS '大学ID';
COMMENT ON COLUMN t_special_channel_university.university_name IS '大学名称(冗余)';
COMMENT ON COLUMN t_special_channel_university.year IS '招生年份';
COMMENT ON COLUMN t_special_channel_university.region_tag IS '地区标签: 香港/澳门/NULL';
COMMENT ON COLUMN t_special_channel_university.signup_start IS '报名开始时间';
COMMENT ON COLUMN t_special_channel_university.signup_end IS '报名截止时间';
COMMENT ON COLUMN t_special_channel_university.official_url IS '报名官网URL';
COMMENT ON COLUMN t_special_channel_university.brochure_title IS '简章标题';
COMMENT ON COLUMN t_special_channel_university.brochure_content IS '简章正文(HTML)';
COMMENT ON COLUMN t_special_channel_university.sort_order IS '排序权重';
COMMENT ON COLUMN t_special_channel_university.is_active IS '是否启用';

CREATE INDEX idx_scu_channel ON t_special_channel_university(channel_code) WHERE is_active = TRUE;
CREATE INDEX idx_scu_region ON t_special_channel_university(channel_code, region_tag) WHERE is_active = TRUE;

-- ============================================================
-- 3. 强基计划入围/录取数据表 (t_strong_base_score)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_strong_base_score (
    id                      BIGINT          PRIMARY KEY,
    university_id           BIGINT          NOT NULL,
    university_name         VARCHAR(50)     NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    subject_type            VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    major_code              VARCHAR(20),
    entry_score             NUMERIC(7,2),
    entry_score_type        VARCHAR(30)     DEFAULT '高考成绩',
    entry_formula           VARCHAR(500),
    entry_ratio             VARCHAR(20),
    admission_score         NUMERIC(7,2),
    admission_formula       VARCHAR(500)    DEFAULT '高考成绩×85%+校测成绩×15%',
    plan_count              INTEGER,
    admission_count         INTEGER,
    remark                  VARCHAR(500),
    is_active               BOOLEAN         DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_strong_base UNIQUE (university_id, year, province, subject_type, major_name)
);

COMMENT ON TABLE t_strong_base_score IS '强基计划入围/录取数据表';
COMMENT ON COLUMN t_strong_base_score.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_strong_base_score.university_id IS '大学ID';
COMMENT ON COLUMN t_strong_base_score.university_name IS '大学名称(冗余)';
COMMENT ON COLUMN t_strong_base_score.year IS '年份';
COMMENT ON COLUMN t_strong_base_score.province IS '省份';
COMMENT ON COLUMN t_strong_base_score.subject_type IS '科类: 物理类/历史类/理科/文科/综合改革';
COMMENT ON COLUMN t_strong_base_score.major_name IS '专业名称';
COMMENT ON COLUMN t_strong_base_score.major_code IS '专业代码';
COMMENT ON COLUMN t_strong_base_score.entry_score IS '入围分数线';
COMMENT ON COLUMN t_strong_base_score.entry_score_type IS '入围分数类型: 高考成绩/加权成绩/校测初试';
COMMENT ON COLUMN t_strong_base_score.entry_formula IS '入围计算公式';
COMMENT ON COLUMN t_strong_base_score.entry_ratio IS '入围比例';
COMMENT ON COLUMN t_strong_base_score.admission_score IS '录取综合分';
COMMENT ON COLUMN t_strong_base_score.admission_formula IS '录取综合分计算公式';
COMMENT ON COLUMN t_strong_base_score.plan_count IS '招生计划数';
COMMENT ON COLUMN t_strong_base_score.admission_count IS '实际录取人数';
COMMENT ON COLUMN t_strong_base_score.remark IS '备注';
COMMENT ON COLUMN t_strong_base_score.is_active IS '是否启用';

CREATE INDEX idx_sbs_univ_year ON t_strong_base_score(university_id, year DESC);
CREATE INDEX idx_sbs_province ON t_strong_base_score(province, year DESC, subject_type);

-- ============================================================
-- 4. 强基计划院校配置表 (t_strong_base_university)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_strong_base_university (
    id                          BIGINT          PRIMARY KEY,
    university_id               BIGINT          NOT NULL UNIQUE,
    university_name             VARCHAR(50)     NOT NULL,
    is_pilot                    BOOLEAN         DEFAULT TRUE,
    pilot_year                  SMALLINT,
    official_url                VARCHAR(500),
    signup_url                  VARCHAR(500),
    test_before_score           BOOLEAN         DEFAULT FALSE,
    default_entry_ratio         VARCHAR(20)     DEFAULT '1:5',
    default_admission_formula   VARCHAR(500)    DEFAULT '高考成绩×85%+校测成绩×15%',
    available_majors            TEXT[],
    special_notes               TEXT,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_strong_base_university IS '强基计划院校配置表';
COMMENT ON COLUMN t_strong_base_university.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_strong_base_university.university_id IS '大学ID(唯一)';
COMMENT ON COLUMN t_strong_base_university.university_name IS '大学名称(冗余)';
COMMENT ON COLUMN t_strong_base_university.is_pilot IS '是否强基试点校';
COMMENT ON COLUMN t_strong_base_university.pilot_year IS '首次试点年份';
COMMENT ON COLUMN t_strong_base_university.official_url IS '强基计划官方页面URL';
COMMENT ON COLUMN t_strong_base_university.signup_url IS '报名入口URL';
COMMENT ON COLUMN t_strong_base_university.test_before_score IS '是否高考出分前校测';
COMMENT ON COLUMN t_strong_base_university.default_entry_ratio IS '默认入围比例';
COMMENT ON COLUMN t_strong_base_university.default_admission_formula IS '默认录取综合分公式';
COMMENT ON COLUMN t_strong_base_university.available_majors IS '可选专业列表';
COMMENT ON COLUMN t_strong_base_university.special_notes IS '特殊说明';

CREATE INDEX idx_sbu_pilot ON t_strong_base_university(is_pilot);
CREATE INDEX idx_sbu_name ON t_strong_base_university(university_name);
```

- [ ] **Step 2: 验证SQL语法**

Run: `cd haifeng-admin && cat src/main/resources/db/migration/V14__t_special_channel__tables.sql | head -50`

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/resources/db/migration/V14__t_special_channel__tables.sql
git commit -m "feat(special): add V14 flyway migration for special channel tables"
```

---

## Task 2: SpecialChannel模块 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/special/SpecialChannel.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/special/SpecialChannelMapper.java`

- [ ] **Step 1: 创建SpecialChannel Entity**

```java
package com.haifeng.common.entity.special;

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
@TableName("t_special_channel")
public class SpecialChannel {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String channelCode;

    private String channelName;

    private String subtitle;

    private String parentCode;

    private String filterLabel;

    private String displayType;

    private String content;

    private Integer sortOrder;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建SpecialChannelMapper**

```java
package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.SpecialChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpecialChannelMapper extends BaseMapper<SpecialChannel> {

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_code = #{code}")
    int countByCode(@Param("code") String code);

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_name = #{name}")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_name = #{name} AND id != #{excludeId}")
    int countByNameExclude(@Param("name") String name, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM t_special_channel WHERE channel_code = #{code} AND id != #{excludeId}")
    int countByCodeExclude(@Param("code") String code, @Param("excludeId") Long excludeId);
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/special/SpecialChannel.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/special/SpecialChannelMapper.java
git commit -m "feat(special): add SpecialChannel entity and mapper"
```

---

## Task 3: SpecialChannel模块 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelBatchDeleteDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/SpecialChannelListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/SpecialChannelDetailVO.java`

- [ ] **Step 1: 创建SpecialChannelQueryDTO**

```java
package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelQueryDTO extends BasePageQueryDTO {

    /**
     * 展示类型(精准查询)
     */
    private String displayType;

    /**
     * 通道名称(模糊查询)
     */
    private String channelName;
}
```

- [ ] **Step 2: 创建SpecialChannelAddDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SpecialChannelAddDTO {

    @NotBlank(message = "通道代码不能为空")
    @Size(max = 30, message = "通道代码长度不能超过30")
    private String channelCode;

    @NotBlank(message = "通道名称不能为空")
    @Size(max = 50, message = "通道名称长度不能超过50")
    private String channelName;

    @Size(max = 200, message = "副标题长度不能超过200")
    private String subtitle;

    @Size(max = 30, message = "父级代码长度不能超过30")
    private String parentCode;

    @Size(max = 30, message = "筛选标签长度不能超过30")
    private String filterLabel;

    @NotBlank(message = "展示类型不能为空")
    @Size(max = 20, message = "展示类型长度不能超过20")
    private String displayType;

    private String content;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建SpecialChannelBatchDeleteDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SpecialChannelBatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
```

- [ ] **Step 4: 创建SpecialChannelListVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class SpecialChannelListVO {

    private Long id;

    private String channelCode;

    private String channelName;

    private String displayType;

    private Boolean isActive;
}
```

- [ ] **Step 5: 创建SpecialChannelDetailVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SpecialChannelDetailVO {

    private Long id;

    private String channelCode;

    private String channelName;

    private String subtitle;

    private String parentCode;

    private String filterLabel;

    private String displayType;

    private String content;

    private Integer sortOrder;

    private Boolean isActive;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/
git commit -m "feat(special): add SpecialChannel DTOs and VOs"
```

---

## Task 4: SpecialChannel模块 - Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/special/SpecialChannelService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/SpecialChannelServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/special/SpecialChannelController.java`

- [ ] **Step 1: 创建SpecialChannelService接口**

```java
package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelQueryDTO;
import com.haifeng.admin.vo.special.SpecialChannelDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelListVO;

import java.util.List;

public interface SpecialChannelService {

    IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto);

    SpecialChannelDetailVO detail(Long id);

    void add(SpecialChannelAddDTO dto);

    void update(Long id, SpecialChannelAddDTO dto);

    void toggleActive(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建SpecialChannelServiceImpl**

```java
package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.SpecialChannelAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelService;
import com.haifeng.admin.vo.special.SpecialChannelDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelListVO;
import com.haifeng.common.entity.special.SpecialChannel;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelServiceImpl implements SpecialChannelService {

    private final SpecialChannelMapper specialChannelMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto) {
        Page<SpecialChannel> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SpecialChannel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getDisplayType())) {
            wrapper.eq(SpecialChannel::getDisplayType, dto.getDisplayType());
        }
        if (StringUtils.hasText(dto.getChannelName())) {
            wrapper.like(SpecialChannel::getChannelName, dto.getChannelName());
        }
        wrapper.orderByAsc(SpecialChannel::getSortOrder)
               .orderByDesc(SpecialChannel::getCreatedAt);

        IPage<SpecialChannel> result = specialChannelMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            SpecialChannelListVO vo = new SpecialChannelListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public SpecialChannelDetailVO detail(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        SpecialChannelDetailVO vo = new SpecialChannelDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SpecialChannelAddDTO dto) {
        if (specialChannelMapper.countByCode(dto.getChannelCode()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "通道代码已存在");
        }

        SpecialChannel entity = new SpecialChannel();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        entity.setIsActive(true);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        specialChannelMapper.insert(entity);
        log.info("新增招生通道: code={}, name={}", dto.getChannelCode(), dto.getChannelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SpecialChannelAddDTO dto) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }

        if (!entity.getChannelCode().equals(dto.getChannelCode())
                && specialChannelMapper.countByCodeExclude(dto.getChannelCode(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "通道代码已存在");
        }

        BeanUtils.copyProperties(dto, entity);
        specialChannelMapper.updateById(entity);
        log.info("修改招生通道: id={}, code={}", id, dto.getChannelCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }

        entity.setIsActive(!entity.getIsActive());
        specialChannelMapper.updateById(entity);
        log.info("切换招生通道状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (specialChannelMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        specialChannelMapper.deleteById(id);
        log.info("删除招生通道: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        specialChannelMapper.deleteBatchIds(ids);
        log.info("批量删除招生通道: ids={}", ids);
    }
}
```

- [ ] **Step 3: 创建SpecialChannelController**

```java
package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelBatchDeleteDTO;
import com.haifeng.admin.dto.special.SpecialChannelQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelService;
import com.haifeng.admin.vo.special.SpecialChannelDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/special/channel")
@RequiredArgsConstructor
public class SpecialChannelController {

    private final SpecialChannelService specialChannelService;

    @GetMapping("/page")
    public R<IPage<SpecialChannelListVO>> page(@Valid SpecialChannelQueryDTO dto) {
        return R.ok(specialChannelService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SpecialChannelDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "特殊通道管理", action = "新增招生通道")
    public R<Void> add(@Valid @RequestBody SpecialChannelAddDTO dto) {
        specialChannelService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "修改招生通道")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SpecialChannelAddDTO dto) {
        specialChannelService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "特殊通道管理", action = "切换通道状态")
    public R<Void> toggleActive(@PathVariable Long id) {
        specialChannelService.toggleActive(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "删除招生通道")
    public R<Void> delete(@PathVariable Long id) {
        specialChannelService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "特殊通道管理", action = "批量删除招生通道")
    public R<Void> batchDelete(@Valid @RequestBody SpecialChannelBatchDeleteDTO dto) {
        specialChannelService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/special/SpecialChannelService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/SpecialChannelServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/special/SpecialChannelController.java
git commit -m "feat(special): add SpecialChannel service and controller"
```

---

## Task 5: SpecialChannelUniversity模块 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/special/SpecialChannelUniversity.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/special/SpecialChannelUniversityMapper.java`

- [ ] **Step 1: 创建SpecialChannelUniversity Entity**

```java
package com.haifeng.common.entity.special;

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
@TableName("t_special_channel_university")
public class SpecialChannelUniversity {

    @TableId(type = IdType.INPUT)
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

    private Integer sortOrder;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建SpecialChannelUniversityMapper**

```java
package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpecialChannelUniversityMapper extends BaseMapper<SpecialChannelUniversity> {

    @Select("SELECT COUNT(*) FROM t_special_channel_university WHERE channel_code = #{channelCode} AND university_id = #{universityId} AND year = #{year}")
    int countByUnique(@Param("channelCode") String channelCode, @Param("universityId") Long universityId, @Param("year") Short year);

    @Select("SELECT COUNT(*) FROM t_special_channel_university WHERE channel_code = #{channelCode} AND university_id = #{universityId} AND year = #{year} AND id != #{excludeId}")
    int countByUniqueExclude(@Param("channelCode") String channelCode, @Param("universityId") Long universityId, @Param("year") Short year, @Param("excludeId") Long excludeId);
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/special/SpecialChannelUniversity.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/special/SpecialChannelUniversityMapper.java
git commit -m "feat(special): add SpecialChannelUniversity entity and mapper"
```

---

## Task 6: SpecialChannelUniversity模块 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelUnivQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelUnivAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelUnivBatchDeleteDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/SpecialChannelUnivListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/SpecialChannelUnivDetailVO.java`

- [ ] **Step 1: 创建SpecialChannelUnivQueryDTO**

```java
package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialChannelUnivQueryDTO extends BasePageQueryDTO {
    // 无精准查询条件
}
```

- [ ] **Step 2: 创建SpecialChannelUnivAddDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SpecialChannelUnivAddDTO {

    @NotBlank(message = "通道代码不能为空")
    @Size(max = 30, message = "通道代码长度不能超过30")
    private String channelCode;

    @NotBlank(message = "通道名称不能为空")
    @Size(max = 50, message = "通道名称长度不能超过50")
    private String channelName;

    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    private Short year;

    @Size(max = 20, message = "地区标签长度不能超过20")
    private String regionTag;

    private OffsetDateTime signupStart;

    private OffsetDateTime signupEnd;

    @Size(max = 500, message = "官网URL长度不能超过500")
    private String officialUrl;

    @Size(max = 200, message = "简章标题长度不能超过200")
    private String brochureTitle;

    private String brochureContent;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建SpecialChannelUnivBatchDeleteDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SpecialChannelUnivBatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
```

- [ ] **Step 4: 创建SpecialChannelUnivListVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class SpecialChannelUnivListVO {

    private Long id;

    private String channelName;

    private String universityName;

    private Short year;

    private String regionTag;

    private Boolean isActive;
}
```

- [ ] **Step 5: 创建SpecialChannelUnivDetailVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SpecialChannelUnivDetailVO {

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

    private Integer sortOrder;

    private Boolean isActive;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelUnivQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelUnivAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/SpecialChannelUnivBatchDeleteDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/SpecialChannelUnivListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/SpecialChannelUnivDetailVO.java
git commit -m "feat(special): add SpecialChannelUniv DTOs and VOs"
```

---

## Task 7: SpecialChannelUniversity模块 - Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/special/SpecialChannelUnivService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/SpecialChannelUnivServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/special/SpecialChannelUnivController.java`

- [ ] **Step 1: 创建SpecialChannelUnivService接口**

```java
package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelUnivAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.admin.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelUnivListVO;

import java.util.List;

public interface SpecialChannelUnivService {

    IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto);

    SpecialChannelUnivDetailVO detail(Long id);

    void add(SpecialChannelUnivAddDTO dto);

    void update(Long id, SpecialChannelUnivAddDTO dto);

    void toggleActive(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建SpecialChannelUnivServiceImpl**

```java
package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.SpecialChannelUnivAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelUnivService;
import com.haifeng.admin.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelUnivServiceImpl implements SpecialChannelUnivService {

    private final SpecialChannelUniversityMapper specialChannelUniversityMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto) {
        Page<SpecialChannelUniversity> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SpecialChannelUniversity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SpecialChannelUniversity::getSortOrder)
               .orderByDesc(SpecialChannelUniversity::getCreatedAt);

        IPage<SpecialChannelUniversity> result = specialChannelUniversityMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            SpecialChannelUnivListVO vo = new SpecialChannelUnivListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public SpecialChannelUnivDetailVO detail(Long id) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        SpecialChannelUnivDetailVO vo = new SpecialChannelUnivDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SpecialChannelUnivAddDTO dto) {
        if (specialChannelUniversityMapper.countByUnique(dto.getChannelCode(), dto.getUniversityId(), dto.getYear()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通道下该大学该年份的记录已存在");
        }

        SpecialChannelUniversity entity = new SpecialChannelUniversity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        entity.setIsActive(true);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        specialChannelUniversityMapper.insert(entity);
        log.info("新增通道-大学关联: channelCode={}, universityId={}, year={}", dto.getChannelCode(), dto.getUniversityId(), dto.getYear());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SpecialChannelUnivAddDTO dto) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }

        boolean keyChanged = !entity.getChannelCode().equals(dto.getChannelCode())
                || !entity.getUniversityId().equals(dto.getUniversityId())
                || (entity.getYear() == null ? dto.getYear() != null : !entity.getYear().equals(dto.getYear()));

        if (keyChanged && specialChannelUniversityMapper.countByUniqueExclude(dto.getChannelCode(), dto.getUniversityId(), dto.getYear(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通道下该大学该年份的记录已存在");
        }

        BeanUtils.copyProperties(dto, entity);
        specialChannelUniversityMapper.updateById(entity);
        log.info("修改通道-大学关联: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }

        entity.setIsActive(!entity.getIsActive());
        specialChannelUniversityMapper.updateById(entity);
        log.info("切换通道-大学关联状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (specialChannelUniversityMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        specialChannelUniversityMapper.deleteById(id);
        log.info("删除通道-大学关联: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        specialChannelUniversityMapper.deleteBatchIds(ids);
        log.info("批量删除通道-大学关联: ids={}", ids);
    }
}
```

- [ ] **Step 3: 创建SpecialChannelUnivController**

```java
package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelUnivAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivBatchDeleteDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelUnivService;
import com.haifeng.admin.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/special/channel-univ")
@RequiredArgsConstructor
public class SpecialChannelUnivController {

    private final SpecialChannelUnivService specialChannelUnivService;

    @GetMapping("/page")
    public R<IPage<SpecialChannelUnivListVO>> page(@Valid SpecialChannelUnivQueryDTO dto) {
        return R.ok(specialChannelUnivService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SpecialChannelUnivDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelUnivService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "特殊通道管理", action = "新增通道-大学关联")
    public R<Void> add(@Valid @RequestBody SpecialChannelUnivAddDTO dto) {
        specialChannelUnivService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "修改通道-大学关联")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SpecialChannelUnivAddDTO dto) {
        specialChannelUnivService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "特殊通道管理", action = "切换关联状态")
    public R<Void> toggleActive(@PathVariable Long id) {
        specialChannelUnivService.toggleActive(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "删除通道-大学关联")
    public R<Void> delete(@PathVariable Long id) {
        specialChannelUnivService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "特殊通道管理", action = "批量删除通道-大学关联")
    public R<Void> batchDelete(@Valid @RequestBody SpecialChannelUnivBatchDeleteDTO dto) {
        specialChannelUnivService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/special/SpecialChannelUnivService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/SpecialChannelUnivServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/special/SpecialChannelUnivController.java
git commit -m "feat(special): add SpecialChannelUniv service and controller"
```

---

## Task 8: StrongBaseScore模块 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/special/StrongBaseScore.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/special/StrongBaseScoreMapper.java`

- [ ] **Step 1: 创建StrongBaseScore Entity**

```java
package com.haifeng.common.entity.special;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_strong_base_score")
public class StrongBaseScore {

    @TableId(type = IdType.INPUT)
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

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建StrongBaseScoreMapper**

```java
package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.StrongBaseScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StrongBaseScoreMapper extends BaseMapper<StrongBaseScore> {

    @Select("SELECT COUNT(*) FROM t_strong_base_score WHERE university_id = #{universityId} AND year = #{year} AND province = #{province} AND subject_type = #{subjectType} AND major_name = #{majorName}")
    int countByUnique(@Param("universityId") Long universityId, @Param("year") Short year, @Param("province") String province, @Param("subjectType") String subjectType, @Param("majorName") String majorName);

    @Select("SELECT COUNT(*) FROM t_strong_base_score WHERE university_id = #{universityId} AND year = #{year} AND province = #{province} AND subject_type = #{subjectType} AND major_name = #{majorName} AND id != #{excludeId}")
    int countByUniqueExclude(@Param("universityId") Long universityId, @Param("year") Short year, @Param("province") String province, @Param("subjectType") String subjectType, @Param("majorName") String majorName, @Param("excludeId") Long excludeId);
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/special/StrongBaseScore.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/special/StrongBaseScoreMapper.java
git commit -m "feat(special): add StrongBaseScore entity and mapper"
```

---

## Task 9: StrongBaseScore模块 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseScoreQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseScoreAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseScoreBatchDeleteDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseScoreListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseScoreDetailVO.java`

- [ ] **Step 1: 创建StrongBaseScoreQueryDTO**

```java
package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseScoreQueryDTO extends BasePageQueryDTO {

    /**
     * 大学名称(精准查询)
     */
    private String universityName;

    /**
     * 年份(精准查询)
     */
    private Short year;

    /**
     * 省份(精准查询)
     */
    private String province;

    /**
     * 科类(精准查询)
     */
    private String subjectType;
}
```

- [ ] **Step 2: 创建StrongBaseScoreAddDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrongBaseScoreAddDTO {

    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @NotBlank(message = "科类不能为空")
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String majorName;

    @Size(max = 20, message = "专业代码长度不能超过20")
    private String majorCode;

    private BigDecimal entryScore;

    @Size(max = 30, message = "入围分数类型长度不能超过30")
    private String entryScoreType;

    @Size(max = 500, message = "入围计算公式长度不能超过500")
    private String entryFormula;

    @Size(max = 20, message = "入围比例长度不能超过20")
    private String entryRatio;

    private BigDecimal admissionScore;

    @Size(max = 500, message = "录取公式长度不能超过500")
    private String admissionFormula;

    private Integer planCount;

    private Integer admissionCount;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
```

- [ ] **Step 3: 创建StrongBaseScoreBatchDeleteDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StrongBaseScoreBatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
```

- [ ] **Step 4: 创建StrongBaseScoreListVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class StrongBaseScoreListVO {

    private Long id;

    private String universityName;

    private Short year;

    private String province;

    private String subjectType;

    private Boolean isActive;
}
```

- [ ] **Step 5: 创建StrongBaseScoreDetailVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class StrongBaseScoreDetailVO {

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

    private Boolean isActive;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseScoreQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseScoreAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseScoreBatchDeleteDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseScoreListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseScoreDetailVO.java
git commit -m "feat(special): add StrongBaseScore DTOs and VOs"
```

---

## Task 10: StrongBaseScore模块 - Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/special/StrongBaseScoreService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/StrongBaseScoreServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/special/StrongBaseScoreController.java`

- [ ] **Step 1: 创建StrongBaseScoreService接口**

```java
package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;

import java.util.List;

public interface StrongBaseScoreService {

    IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto);

    StrongBaseScoreDetailVO detail(Long id);

    void add(StrongBaseScoreAddDTO dto);

    void update(Long id, StrongBaseScoreAddDTO dto);

    void toggleActive(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建StrongBaseScoreServiceImpl**

```java
package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.service.special.StrongBaseScoreService;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.entity.special.StrongBaseScore;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseScoreMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseScoreServiceImpl implements StrongBaseScoreService {

    private final StrongBaseScoreMapper strongBaseScoreMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto) {
        Page<StrongBaseScore> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<StrongBaseScore> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.eq(StrongBaseScore::getUniversityName, dto.getUniversityName());
        }
        if (dto.getYear() != null) {
            wrapper.eq(StrongBaseScore::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(StrongBaseScore::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(StrongBaseScore::getSubjectType, dto.getSubjectType());
        }
        wrapper.orderByDesc(StrongBaseScore::getYear)
               .orderByDesc(StrongBaseScore::getCreatedAt);

        IPage<StrongBaseScore> result = strongBaseScoreMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            StrongBaseScoreListVO vo = new StrongBaseScoreListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public StrongBaseScoreDetailVO detail(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        StrongBaseScoreDetailVO vo = new StrongBaseScoreDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StrongBaseScoreAddDTO dto) {
        if (strongBaseScoreMapper.countByUnique(dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getSubjectType(), dto.getMajorName()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该强基计划数据已存在");
        }

        StrongBaseScore entity = new StrongBaseScore();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        entity.setIsActive(true);
        if (entity.getEntryScoreType() == null) {
            entity.setEntryScoreType("高考成绩");
        }
        if (entity.getAdmissionFormula() == null) {
            entity.setAdmissionFormula("高考成绩×85%+校测成绩×15%");
        }

        strongBaseScoreMapper.insert(entity);
        log.info("新增强基计划数据: universityId={}, year={}, province={}, majorName={}", dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getMajorName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StrongBaseScoreAddDTO dto) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }

        boolean keyChanged = !entity.getUniversityId().equals(dto.getUniversityId())
                || !entity.getYear().equals(dto.getYear())
                || !entity.getProvince().equals(dto.getProvince())
                || !entity.getSubjectType().equals(dto.getSubjectType())
                || !entity.getMajorName().equals(dto.getMajorName());

        if (keyChanged && strongBaseScoreMapper.countByUniqueExclude(dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getSubjectType(), dto.getMajorName(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该强基计划数据已存在");
        }

        BeanUtils.copyProperties(dto, entity);
        strongBaseScoreMapper.updateById(entity);
        log.info("修改强基计划数据: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }

        entity.setIsActive(!entity.getIsActive());
        strongBaseScoreMapper.updateById(entity);
        log.info("切换强基计划数据状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (strongBaseScoreMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        strongBaseScoreMapper.deleteById(id);
        log.info("删除强基计划数据: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        strongBaseScoreMapper.deleteBatchIds(ids);
        log.info("批量删除强基计划数据: ids={}", ids);
    }
}
```

- [ ] **Step 3: 创建StrongBaseScoreController**

```java
package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreBatchDeleteDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.service.special.StrongBaseScoreService;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/special/strong-base-score")
@RequiredArgsConstructor
public class StrongBaseScoreController {

    private final StrongBaseScoreService strongBaseScoreService;

    @GetMapping("/page")
    public R<IPage<StrongBaseScoreListVO>> page(@Valid StrongBaseScoreQueryDTO dto) {
        return R.ok(strongBaseScoreService.page(dto));
    }

    @GetMapping("/{id}")
    public R<StrongBaseScoreDetailVO> detail(@PathVariable Long id) {
        return R.ok(strongBaseScoreService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "强基计划管理", action = "新增强基数据")
    public R<Void> add(@Valid @RequestBody StrongBaseScoreAddDTO dto) {
        strongBaseScoreService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "修改强基数据")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StrongBaseScoreAddDTO dto) {
        strongBaseScoreService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "强基计划管理", action = "切换强基数据状态")
    public R<Void> toggleActive(@PathVariable Long id) {
        strongBaseScoreService.toggleActive(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "删除强基数据")
    public R<Void> delete(@PathVariable Long id) {
        strongBaseScoreService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "强基计划管理", action = "批量删除强基数据")
    public R<Void> batchDelete(@Valid @RequestBody StrongBaseScoreBatchDeleteDTO dto) {
        strongBaseScoreService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/special/StrongBaseScoreService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/StrongBaseScoreServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/special/StrongBaseScoreController.java
git commit -m "feat(special): add StrongBaseScore service and controller"
```

---

## Task 11: StrongBaseUniversity模块 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/special/StrongBaseUniversity.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/special/StrongBaseUniversityMapper.java`

- [ ] **Step 1: 创建StrongBaseUniversity Entity**

```java
package com.haifeng.common.entity.special;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_strong_base_university", autoResultMap = true)
public class StrongBaseUniversity {

    @TableId(type = IdType.INPUT)
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] availableMajors;

    private String specialNotes;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建StrongBaseUniversityMapper**

```java
package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StrongBaseUniversityMapper extends BaseMapper<StrongBaseUniversity> {

    @Select("SELECT COUNT(*) FROM t_strong_base_university WHERE university_id = #{universityId}")
    int countByUniversityId(@Param("universityId") Long universityId);

    @Select("SELECT COUNT(*) FROM t_strong_base_university WHERE university_id = #{universityId} AND id != #{excludeId}")
    int countByUniversityIdExclude(@Param("universityId") Long universityId, @Param("excludeId") Long excludeId);
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/special/StrongBaseUniversity.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/special/StrongBaseUniversityMapper.java
git commit -m "feat(special): add StrongBaseUniversity entity and mapper"
```

---

## Task 12: StrongBaseUniversity模块 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseUnivQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseUnivAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseUnivBatchDeleteDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseUnivListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseUnivDetailVO.java`

- [ ] **Step 1: 创建StrongBaseUnivQueryDTO**

```java
package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseUnivQueryDTO extends BasePageQueryDTO {

    /**
     * 大学名称(精准查询)
     */
    private String universityName;

    /**
     * 是否试点校(精准查询)
     */
    private Boolean isPilot;

    /**
     * 试点年份(精准查询)
     */
    private Short pilotYear;

    /**
     * 是否高考出分前校测(精准查询)
     */
    private Boolean testBeforeScore;
}
```

- [ ] **Step 2: 创建StrongBaseUnivAddDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StrongBaseUnivAddDTO {

    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    private Boolean isPilot;

    private Short pilotYear;

    @Size(max = 500, message = "官方页面URL长度不能超过500")
    private String officialUrl;

    @Size(max = 500, message = "报名入口URL长度不能超过500")
    private String signupUrl;

    private Boolean testBeforeScore;

    @Size(max = 20, message = "默认入围比例长度不能超过20")
    private String defaultEntryRatio;

    @Size(max = 500, message = "默认录取公式长度不能超过500")
    private String defaultAdmissionFormula;

    private String[] availableMajors;

    private String specialNotes;
}
```

- [ ] **Step 3: 创建StrongBaseUnivBatchDeleteDTO**

```java
package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StrongBaseUnivBatchDeleteDTO {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
```

- [ ] **Step 4: 创建StrongBaseUnivListVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

@Data
public class StrongBaseUnivListVO {

    private Long id;

    private String universityName;

    private Boolean isPilot;

    private Short pilotYear;

    private Boolean testBeforeScore;
}
```

- [ ] **Step 5: 创建StrongBaseUnivDetailVO**

```java
package com.haifeng.admin.vo.special;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class StrongBaseUnivDetailVO {

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

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseUnivQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseUnivAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/special/StrongBaseUnivBatchDeleteDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseUnivListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/special/StrongBaseUnivDetailVO.java
git commit -m "feat(special): add StrongBaseUniv DTOs and VOs"
```

---

## Task 13: StrongBaseUniversity模块 - Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/special/StrongBaseUnivService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/StrongBaseUnivServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/special/StrongBaseUnivController.java`

- [ ] **Step 1: 创建StrongBaseUnivService接口**

```java
package com.haifeng.admin.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;

import java.util.List;

public interface StrongBaseUnivService {

    IPage<StrongBaseUnivListVO> page(StrongBaseUnivQueryDTO dto);

    StrongBaseUnivDetailVO detail(Long id);

    void add(StrongBaseUnivAddDTO dto);

    void update(Long id, StrongBaseUnivAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建StrongBaseUnivServiceImpl**

```java
package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.service.special.StrongBaseUnivService;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseUniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseUnivServiceImpl implements StrongBaseUnivService {

    private final StrongBaseUniversityMapper strongBaseUniversityMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<StrongBaseUnivListVO> page(StrongBaseUnivQueryDTO dto) {
        Page<StrongBaseUniversity> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<StrongBaseUniversity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.eq(StrongBaseUniversity::getUniversityName, dto.getUniversityName());
        }
        if (dto.getIsPilot() != null) {
            wrapper.eq(StrongBaseUniversity::getIsPilot, dto.getIsPilot());
        }
        if (dto.getPilotYear() != null) {
            wrapper.eq(StrongBaseUniversity::getPilotYear, dto.getPilotYear());
        }
        if (dto.getTestBeforeScore() != null) {
            wrapper.eq(StrongBaseUniversity::getTestBeforeScore, dto.getTestBeforeScore());
        }
        wrapper.orderByDesc(StrongBaseUniversity::getCreatedAt);

        IPage<StrongBaseUniversity> result = strongBaseUniversityMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            StrongBaseUnivListVO vo = new StrongBaseUnivListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public StrongBaseUnivDetailVO detail(Long id) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        StrongBaseUnivDetailVO vo = new StrongBaseUnivDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StrongBaseUnivAddDTO dto) {
        if (strongBaseUniversityMapper.countByUniversityId(dto.getUniversityId()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该大学的强基配置已存在");
        }

        StrongBaseUniversity entity = new StrongBaseUniversity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        if (entity.getIsPilot() == null) {
            entity.setIsPilot(true);
        }
        if (entity.getTestBeforeScore() == null) {
            entity.setTestBeforeScore(false);
        }
        if (entity.getDefaultEntryRatio() == null) {
            entity.setDefaultEntryRatio("1:5");
        }
        if (entity.getDefaultAdmissionFormula() == null) {
            entity.setDefaultAdmissionFormula("高考成绩×85%+校测成绩×15%");
        }

        strongBaseUniversityMapper.insert(entity);
        log.info("新增强基院校配置: universityId={}, universityName={}", dto.getUniversityId(), dto.getUniversityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StrongBaseUnivAddDTO dto) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }

        if (!entity.getUniversityId().equals(dto.getUniversityId())
                && strongBaseUniversityMapper.countByUniversityIdExclude(dto.getUniversityId(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该大学的强基配置已存在");
        }

        BeanUtils.copyProperties(dto, entity);
        strongBaseUniversityMapper.updateById(entity);
        log.info("修改强基院校配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (strongBaseUniversityMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        strongBaseUniversityMapper.deleteById(id);
        log.info("删除强基院校配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        strongBaseUniversityMapper.deleteBatchIds(ids);
        log.info("批量删除强基院校配置: ids={}", ids);
    }
}
```

- [ ] **Step 3: 创建StrongBaseUnivController**

```java
package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivBatchDeleteDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.service.special.StrongBaseUnivService;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/special/strong-base-univ")
@RequiredArgsConstructor
public class StrongBaseUnivController {

    private final StrongBaseUnivService strongBaseUnivService;

    @GetMapping("/page")
    public R<IPage<StrongBaseUnivListVO>> page(@Valid StrongBaseUnivQueryDTO dto) {
        return R.ok(strongBaseUnivService.page(dto));
    }

    @GetMapping("/{id}")
    public R<StrongBaseUnivDetailVO> detail(@PathVariable Long id) {
        return R.ok(strongBaseUnivService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "强基计划管理", action = "新增强基院校配置")
    public R<Void> add(@Valid @RequestBody StrongBaseUnivAddDTO dto) {
        strongBaseUnivService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "修改强基院校配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StrongBaseUnivAddDTO dto) {
        strongBaseUnivService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "删除强基院校配置")
    public R<Void> delete(@PathVariable Long id) {
        strongBaseUnivService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "强基计划管理", action = "批量删除强基院校配置")
    public R<Void> batchDelete(@Valid @RequestBody StrongBaseUnivBatchDeleteDTO dto) {
        strongBaseUnivService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/special/StrongBaseUnivService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/special/StrongBaseUnivServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/special/StrongBaseUnivController.java
git commit -m "feat(special): add StrongBaseUniv service and controller (no toggle)"
```

---

## Task 14: 编译验证

**Files:** None (verification only)

- [ ] **Step 1: 编译项目验证代码正确性**

Run: `cd D:\exeProject\ideaProjects\Project-HaiFeng && mvn compile -DskipTests`

Expected: BUILD SUCCESS

- [ ] **Step 2: 如有编译错误，修复后重新编译**

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "feat(special): complete special channel module implementation

- Add V14 flyway migration for 4 tables
- Add Entity and Mapper for all 4 modules
- Add DTO/VO for all 4 modules
- Add Service and Controller for all 4 modules
- Implement CRUD with hard delete
- Use snowflake ID generation

Closes: special channel module"
```

---

## Summary

| Task | Description | Files |
|------|-------------|-------|
| 1 | Flyway迁移 | 1 SQL |
| 2-4 | SpecialChannel模块 | Entity, Mapper, DTO×3, VO×2, Service, ServiceImpl, Controller |
| 5-7 | SpecialChannelUniversity模块 | Entity, Mapper, DTO×3, VO×2, Service, ServiceImpl, Controller |
| 8-10 | StrongBaseScore模块 | Entity, Mapper, DTO×3, VO×2, Service, ServiceImpl, Controller |
| 11-13 | StrongBaseUniversity模块 | Entity, Mapper, DTO×3, VO×2, Service, ServiceImpl, Controller |
| 14 | 编译验证 | - |

**Total Files:** 1 SQL + 4 Entity + 4 Mapper + 12 DTO + 8 VO + 4 Service + 4 ServiceImpl + 4 Controller = **41 files**
