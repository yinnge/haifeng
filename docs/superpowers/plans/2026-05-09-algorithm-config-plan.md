# 算法配置管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现算法配置管理模块的三个子模块（省份改革配置、一分一段位次、批次分数线），支持CRUD和Excel导入

**Architecture:** 采用标准的Controller-Service-Mapper三层架构，Entity和Mapper放在haifeng-common，业务层放在haifeng-admin。使用雪花算法生成ID，硬删除策略，EasyExcel处理导入。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL, Flyway

---

## 文件结构

### 新建文件（25个）

```
haifeng-admin/src/main/resources/db/migration/
└── V12__algorithm_config.sql                          # Flyway迁移

haifeng-common/src/main/java/com/haifeng/common/
├── entity/algorithm/
│   ├── ProvinceReform.java                            # 省份改革配置实体
│   ├── ScoreRank.java                                 # 一分一段实体
│   └── BatchScoreLine.java                            # 批次分数线实体
└── mapper/algorithm/
    ├── ProvinceReformMapper.java                      # 省份改革Mapper
    ├── ScoreRankMapper.java                           # 一分一段Mapper
    └── BatchScoreLineMapper.java                      # 批次分数线Mapper

haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/algorithm/config/
│   ├── ProvinceReformController.java
│   ├── ScoreRankController.java
│   └── BatchScoreLineController.java
├── service/algorithm/config/
│   ├── ProvinceReformService.java
│   ├── ScoreRankService.java
│   └── BatchScoreLineService.java
├── service/impl/algorithm/config/
│   ├── ProvinceReformServiceImpl.java
│   ├── ScoreRankServiceImpl.java
│   └── BatchScoreLineServiceImpl.java
├── dto/algorithm/config/
│   ├── ProvinceReformAddDTO.java
│   ├── ProvinceReformQueryDTO.java
│   ├── ScoreRankAddDTO.java
│   ├── ScoreRankQueryDTO.java
│   ├── BatchScoreLineAddDTO.java
│   └── BatchScoreLineQueryDTO.java
├── vo/algorithm/config/
│   ├── ProvinceReformListVO.java
│   ├── ProvinceReformDetailVO.java
│   ├── ScoreRankListVO.java
│   ├── ScoreRankDetailVO.java
│   ├── BatchScoreLineListVO.java
│   └── BatchScoreLineDetailVO.java
└── excel/algorithm/config/
    ├── ScoreRankImportDTO.java
    └── BatchScoreLineImportDTO.java
```

---

## Task 1: 数据库迁移

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V12__algorithm_config.sql`

- [ ] **Step 1: 创建Flyway迁移文件**

```sql
-- ============================================================
-- V12__algorithm_config.sql
-- 算法配置管理模块：省份改革配置、一分一段位次、批次分数线
-- ============================================================

-- ============================================================
-- 1. 省份高考改革配置表（t_province_reform）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_province_reform (
    id              BIGINT          PRIMARY KEY,
    province        VARCHAR(20)     NOT NULL UNIQUE,
    reform_year     SMALLINT,
    reform_model    VARCHAR(20),
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);

COMMENT ON TABLE t_province_reform IS '省份高考改革配置表';
COMMENT ON COLUMN t_province_reform.id IS '主键（雪花算法）';
COMMENT ON COLUMN t_province_reform.province IS '省份';
COMMENT ON COLUMN t_province_reform.reform_year IS '新高考首届高考年份（NULL=尚未改革）';
COMMENT ON COLUMN t_province_reform.reform_model IS '改革模式（3+1+2 / 3+3 / 传统文理）';
COMMENT ON COLUMN t_province_reform.created_at IS '创建时间';

-- ============================================================
-- 2. 一分一段位次表（t_score_rank）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_score_rank (
    id                  BIGINT          PRIMARY KEY,
    province            VARCHAR(20)     NOT NULL,
    year                SMALLINT        NOT NULL,
    subject_type        VARCHAR(20)     NOT NULL,
    score               SMALLINT        NOT NULL,
    rank                INTEGER         NOT NULL,
    same_score_count    INTEGER,
    cumulative_count    INTEGER,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_score_rank UNIQUE (province, year, subject_type, score)
);

COMMENT ON TABLE t_score_rank IS '一分一段位次表';
COMMENT ON COLUMN t_score_rank.id IS '主键（雪花算法）';
COMMENT ON COLUMN t_score_rank.province IS '省份';
COMMENT ON COLUMN t_score_rank.year IS '年份';
COMMENT ON COLUMN t_score_rank.subject_type IS '科类（物理类/历史类/文科/理科/不分文理）';
COMMENT ON COLUMN t_score_rank.score IS '分数';
COMMENT ON COLUMN t_score_rank.rank IS '位次';
COMMENT ON COLUMN t_score_rank.same_score_count IS '同分人数';
COMMENT ON COLUMN t_score_rank.cumulative_count IS '累计人数';
COMMENT ON COLUMN t_score_rank.created_at IS '创建时间';

CREATE INDEX idx_sr_lookup ON t_score_rank (province, year, subject_type, score);
CREATE INDEX idx_sr_rank_lookup ON t_score_rank (province, year, subject_type, rank);

-- ============================================================
-- 3. 批次分数线表（t_batch_score_line）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_batch_score_line (
    id              BIGINT          PRIMARY KEY,
    province        VARCHAR(20)     NOT NULL,
    year            SMALLINT        NOT NULL,
    subject_type    VARCHAR(20)     NOT NULL,
    batch           VARCHAR(50)     NOT NULL,
    score_line      INTEGER         NOT NULL,
    rank_line       INTEGER,
    remark          VARCHAR(200),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_batch_score_line UNIQUE (province, year, subject_type, batch)
);

COMMENT ON TABLE t_batch_score_line IS '批次分数线表';
COMMENT ON COLUMN t_batch_score_line.id IS '主键（雪花算法）';
COMMENT ON COLUMN t_batch_score_line.province IS '省份';
COMMENT ON COLUMN t_batch_score_line.year IS '年份';
COMMENT ON COLUMN t_batch_score_line.subject_type IS '科类';
COMMENT ON COLUMN t_batch_score_line.batch IS '批次名称';
COMMENT ON COLUMN t_batch_score_line.score_line IS '省控分数线';
COMMENT ON COLUMN t_batch_score_line.rank_line IS '省控线对应位次';
COMMENT ON COLUMN t_batch_score_line.remark IS '备注';
COMMENT ON COLUMN t_batch_score_line.created_at IS '创建时间';

CREATE INDEX idx_bsl_lookup ON t_batch_score_line (province, year, subject_type);
CREATE INDEX idx_bsl_year ON t_batch_score_line (year DESC);

-- ============================================================
-- 4. 预置数据：省份高考改革配置
-- ============================================================
INSERT INTO t_province_reform (id, province, reform_year, reform_model) VALUES
-- 第一批 3+3
(1, '上海', 2017, '3+3'),
(2, '浙江', 2017, '3+3'),
-- 第二批 3+3
(3, '北京', 2020, '3+3'),
(4, '天津', 2020, '3+3'),
(5, '山东', 2020, '3+3'),
(6, '海南', 2020, '3+3'),
-- 第三批 3+1+2
(7, '广东', 2021, '3+1+2'),
(8, '福建', 2021, '3+1+2'),
(9, '河北', 2021, '3+1+2'),
(10, '辽宁', 2021, '3+1+2'),
(11, '湖北', 2021, '3+1+2'),
(12, '湖南', 2021, '3+1+2'),
(13, '重庆', 2021, '3+1+2'),
(14, '江苏', 2021, '3+1+2'),
-- 第四批 3+1+2
(15, '吉林', 2024, '3+1+2'),
(16, '黑龙江', 2024, '3+1+2'),
(17, '安徽', 2024, '3+1+2'),
(18, '江西', 2024, '3+1+2'),
(19, '广西', 2024, '3+1+2'),
(20, '贵州', 2024, '3+1+2'),
(21, '甘肃', 2024, '3+1+2'),
-- 第五批 3+1+2
(22, '山西', 2025, '3+1+2'),
(23, '河南', 2025, '3+1+2'),
(24, '四川', 2025, '3+1+2'),
(25, '云南', 2025, '3+1+2'),
(26, '内蒙古', 2025, '3+1+2'),
(27, '陕西', 2025, '3+1+2'),
(28, '青海', 2025, '3+1+2'),
(29, '宁夏', 2025, '3+1+2'),
-- 尚未改革
(30, '西藏', NULL, '传统文理'),
(31, '新疆', NULL, '传统文理');
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/V12__algorithm_config.sql
git commit -m "feat(algorithm-config): add flyway migration V12"
```

---

## Task 2: Entity层

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ProvinceReform.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ScoreRank.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/BatchScoreLine.java`

- [ ] **Step 1: 创建ProvinceReform实体**

```java
package com.haifeng.common.entity.algorithm;

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
@TableName("t_province_reform")
public class ProvinceReform {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String province;

    private Short reformYear;

    private String reformModel;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: 创建ScoreRank实体**

```java
package com.haifeng.common.entity.algorithm;

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
@TableName("t_score_rank")
public class ScoreRank {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String province;

    private Short year;

    private String subjectType;

    private Short score;

    private Integer rank;

    private Integer sameScoreCount;

    private Integer cumulativeCount;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 3: 创建BatchScoreLine实体**

```java
package com.haifeng.common.entity.algorithm;

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
@TableName("t_batch_score_line")
public class BatchScoreLine {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String province;

    private Short year;

    private String subjectType;

    private String batch;

    private Integer scoreLine;

    private Integer rankLine;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ProvinceReform.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ScoreRank.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/BatchScoreLine.java
git commit -m "feat(algorithm-config): add entity classes"
```

---

## Task 3: Mapper层

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ProvinceReformMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ScoreRankMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/BatchScoreLineMapper.java`

- [ ] **Step 1: 创建ProvinceReformMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProvinceReformMapper extends BaseMapper<ProvinceReform> {

    @Select("SELECT id FROM t_province_reform WHERE province = #{province} LIMIT 1")
    Long selectIdByProvince(@Param("province") String province);
}
```

- [ ] **Step 2: 创建ScoreRankMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ScoreRank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScoreRankMapper extends BaseMapper<ScoreRank> {

    @Select("SELECT id FROM t_score_rank " +
            "WHERE province = #{province} " +
            "AND year = #{year} " +
            "AND subject_type = #{subjectType} " +
            "AND score = #{score} LIMIT 1")
    Long selectIdByBusinessKey(
            @Param("province") String province,
            @Param("year") Short year,
            @Param("subjectType") String subjectType,
            @Param("score") Short score);

    @Select("SELECT COUNT(*) FROM t_score_rank " +
            "WHERE province = #{province} " +
            "AND year = #{year} " +
            "AND subject_type = #{subjectType} " +
            "AND score = #{score}")
    int countByBusinessKey(
            @Param("province") String province,
            @Param("year") Short year,
            @Param("subjectType") String subjectType,
            @Param("score") Short score);
}
```

- [ ] **Step 3: 创建BatchScoreLineMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BatchScoreLineMapper extends BaseMapper<BatchScoreLine> {

    @Select("SELECT id FROM t_batch_score_line " +
            "WHERE province = #{province} " +
            "AND year = #{year} " +
            "AND subject_type = #{subjectType} " +
            "AND batch = #{batch} LIMIT 1")
    Long selectIdByBusinessKey(
            @Param("province") String province,
            @Param("year") Short year,
            @Param("subjectType") String subjectType,
            @Param("batch") String batch);

    @Select("SELECT COUNT(*) FROM t_batch_score_line " +
            "WHERE province = #{province} " +
            "AND year = #{year} " +
            "AND subject_type = #{subjectType} " +
            "AND batch = #{batch}")
    int countByBusinessKey(
            @Param("province") String province,
            @Param("year") Short year,
            @Param("subjectType") String subjectType,
            @Param("batch") String batch);
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ProvinceReformMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ScoreRankMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/BatchScoreLineMapper.java
git commit -m "feat(algorithm-config): add mapper interfaces"
```

---

## Task 4: 省份改革配置模块 - DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/ProvinceReformAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/ProvinceReformQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/ProvinceReformListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/ProvinceReformDetailVO.java`

- [ ] **Step 1: 创建ProvinceReformAddDTO**

```java
package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProvinceReformAddDTO {

    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    private Short reformYear;

    @Size(max = 20, message = "改革模式长度不能超过20")
    private String reformModel;
}
```

- [ ] **Step 2: 创建ProvinceReformQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.config;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProvinceReformQueryDTO extends BasePageQueryDTO {
    // 无查询条件
}
```

- [ ] **Step 3: 创建ProvinceReformListVO**

```java
package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

@Data
public class ProvinceReformListVO {

    private Long id;

    private String province;

    private Short reformYear;

    private String reformModel;
}
```

- [ ] **Step 4: 创建ProvinceReformDetailVO**

```java
package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProvinceReformDetailVO {

    private Long id;

    private String province;

    private Short reformYear;

    private String reformModel;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/
git commit -m "feat(province-reform): add DTO and VO classes"
```

---

## Task 5: 省份改革配置模块 - Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/config/ProvinceReformService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/config/ProvinceReformServiceImpl.java`

- [ ] **Step 1: 创建ProvinceReformService接口**

```java
package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformAddDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformQueryDTO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformListVO;

import java.util.List;

public interface ProvinceReformService {

    IPage<ProvinceReformListVO> page(ProvinceReformQueryDTO dto);

    ProvinceReformDetailVO detail(Long id);

    Long add(ProvinceReformAddDTO dto);

    void update(Long id, ProvinceReformAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建ProvinceReformServiceImpl实现**

```java
package com.haifeng.admin.service.impl.algorithm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformAddDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformQueryDTO;
import com.haifeng.admin.service.algorithm.config.ProvinceReformService;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformListVO;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ProvinceReformMapper;
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
public class ProvinceReformServiceImpl implements ProvinceReformService {

    private final ProvinceReformMapper provinceReformMapper;

    @Override
    public IPage<ProvinceReformListVO> page(ProvinceReformQueryDTO dto) {
        Page<ProvinceReform> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<ProvinceReform> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProvinceReform::getProvince);

        IPage<ProvinceReform> result = provinceReformMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public ProvinceReformDetailVO detail(Long id) {
        ProvinceReform entity = provinceReformMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "省份配置不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Long add(ProvinceReformAddDTO dto) {
        Long existingId = provinceReformMapper.selectIdByProvince(dto.getProvince());
        if (existingId != null) {
            throw new BusinessException(400, "该省份配置已存在");
        }

        ProvinceReform entity = new ProvinceReform();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(SnowflakeIdGenerator.nextId());

        provinceReformMapper.insert(entity);
        log.info("新增省份改革配置成功，id={}, province={}", entity.getId(), entity.getProvince());
        return entity.getId();
    }

    @Override
    public void update(Long id, ProvinceReformAddDTO dto) {
        ProvinceReform existing = provinceReformMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "省份配置不存在");
        }

        Long existingId = provinceReformMapper.selectIdByProvince(dto.getProvince());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该省份配置已存在");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setId(id);
        provinceReformMapper.updateById(existing);
        log.info("更新省份改革配置成功，id={}", id);
    }

    @Override
    public void delete(Long id) {
        ProvinceReform entity = provinceReformMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "省份配置不存在");
        }
        provinceReformMapper.deleteById(id);
        log.info("删除省份改革配置成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        provinceReformMapper.deleteBatchIds(ids);
        log.info("批量删除省份改革配置成功，ids={}", ids);
    }

    private ProvinceReformListVO convertToListVO(ProvinceReform entity) {
        ProvinceReformListVO vo = new ProvinceReformListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private ProvinceReformDetailVO convertToDetailVO(ProvinceReform entity) {
        ProvinceReformDetailVO vo = new ProvinceReformDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/config/ProvinceReformService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/config/ProvinceReformServiceImpl.java
git commit -m "feat(province-reform): add service layer"
```

---

## Task 6: 省份改革配置模块 - Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/config/ProvinceReformController.java`

- [ ] **Step 1: 创建ProvinceReformController**

```java
package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformAddDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformQueryDTO;
import com.haifeng.admin.service.algorithm.config.ProvinceReformService;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/config/province-reform")
@RequiredArgsConstructor
public class ProvinceReformController {

    private final ProvinceReformService provinceReformService;

    @GetMapping("/page")
    public R<IPage<ProvinceReformListVO>> page(@Valid ProvinceReformQueryDTO dto) {
        return R.ok(provinceReformService.page(dto));
    }

    @GetMapping("/{id}")
    public R<ProvinceReformDetailVO> detail(@PathVariable Long id) {
        return R.ok(provinceReformService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "省份改革配置", action = "新增省份配置")
    public R<Long> add(@Valid @RequestBody ProvinceReformAddDTO dto) {
        return R.ok(provinceReformService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "省份改革配置", action = "修改省份配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ProvinceReformAddDTO dto) {
        provinceReformService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "省份改革配置", action = "删除省份配置")
    public R<Void> delete(@PathVariable Long id) {
        provinceReformService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "省份改革配置", action = "批量删除省份配置")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        provinceReformService.batchDelete(ids);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/config/ProvinceReformController.java
git commit -m "feat(province-reform): add controller"
```

---

## Task 7: 一分一段模块 - DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/ScoreRankAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/ScoreRankQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/ScoreRankListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/ScoreRankDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/config/ScoreRankImportDTO.java`

- [ ] **Step 1: 创建ScoreRankAddDTO**

```java
package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScoreRankAddDTO {

    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "科类不能为空")
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @NotNull(message = "分数不能为空")
    private Short score;

    @NotNull(message = "位次不能为空")
    private Integer rank;

    private Integer sameScoreCount;

    private Integer cumulativeCount;
}
```

- [ ] **Step 2: 创建ScoreRankQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.config;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreRankQueryDTO extends BasePageQueryDTO {

    private String province;

    private Short year;

    private String subjectType;

    private Short score;

    private Integer rank;
}
```

- [ ] **Step 3: 创建ScoreRankListVO**

```java
package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

@Data
public class ScoreRankListVO {

    private Long id;

    private String province;

    private Short year;

    private String subjectType;

    private Short score;

    private Integer rank;
}
```

- [ ] **Step 4: 创建ScoreRankDetailVO**

```java
package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ScoreRankDetailVO {

    private Long id;

    private String province;

    private Short year;

    private String subjectType;

    private Short score;

    private Integer rank;

    private Integer sameScoreCount;

    private Integer cumulativeCount;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建ScoreRankImportDTO**

```java
package com.haifeng.admin.excel.algorithm.config;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreRankImportDTO {

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("年份")
    private Short year;

    @ExcelProperty("科类")
    private String subjectType;

    @ExcelProperty("分数")
    private Short score;

    @ExcelProperty("位次")
    private Integer rank;

    @ExcelProperty("同分人数")
    private Integer sameScoreCount;

    @ExcelProperty("累计人数")
    private Integer cumulativeCount;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/ScoreRankAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/ScoreRankQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/ScoreRankListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/ScoreRankDetailVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/config/ScoreRankImportDTO.java
git commit -m "feat(score-rank): add DTO, VO, and Excel import classes"
```

---

## Task 8: 一分一段模块 - Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/config/ScoreRankService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/config/ScoreRankServiceImpl.java`

- [ ] **Step 1: 创建ScoreRankService接口**

```java
package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ScoreRankAddDTO;
import com.haifeng.admin.dto.algorithm.config.ScoreRankQueryDTO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankDetailVO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ScoreRankService {

    IPage<ScoreRankListVO> page(ScoreRankQueryDTO dto);

    ScoreRankDetailVO detail(Long id);

    Long add(ScoreRankAddDTO dto);

    void update(Long id, ScoreRankAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importData(MultipartFile file);
}
```

- [ ] **Step 2: 创建ScoreRankServiceImpl实现**

```java
package com.haifeng.admin.service.impl.algorithm.config;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.ScoreRankAddDTO;
import com.haifeng.admin.dto.algorithm.config.ScoreRankQueryDTO;
import com.haifeng.admin.excel.algorithm.config.ScoreRankImportDTO;
import com.haifeng.admin.service.algorithm.config.ScoreRankService;
import com.haifeng.admin.vo.algorithm.config.ScoreRankDetailVO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankListVO;
import com.haifeng.common.entity.algorithm.ScoreRank;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreRankServiceImpl implements ScoreRankService {

    private final ScoreRankMapper scoreRankMapper;

    @Override
    public IPage<ScoreRankListVO> page(ScoreRankQueryDTO dto) {
        Page<ScoreRank> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<ScoreRank> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(ScoreRank::getProvince, dto.getProvince());
        }
        if (dto.getYear() != null) {
            wrapper.eq(ScoreRank::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(ScoreRank::getSubjectType, dto.getSubjectType());
        }
        if (dto.getScore() != null) {
            wrapper.eq(ScoreRank::getScore, dto.getScore());
        }
        if (dto.getRank() != null) {
            wrapper.eq(ScoreRank::getRank, dto.getRank());
        }

        wrapper.orderByDesc(ScoreRank::getYear)
               .orderByAsc(ScoreRank::getProvince)
               .orderByDesc(ScoreRank::getScore);

        IPage<ScoreRank> result = scoreRankMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public ScoreRankDetailVO detail(Long id) {
        ScoreRank entity = scoreRankMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Long add(ScoreRankAddDTO dto) {
        Long existingId = scoreRankMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        if (existingId != null) {
            throw new BusinessException(400, "该分数段记录已存在");
        }

        ScoreRank entity = new ScoreRank();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(SnowflakeIdGenerator.nextId());

        scoreRankMapper.insert(entity);
        log.info("新增一分一段记录成功，id={}", entity.getId());
        return entity.getId();
    }

    @Override
    public void update(Long id, ScoreRankAddDTO dto) {
        ScoreRank existing = scoreRankMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }

        Long existingId = scoreRankMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该分数段记录已存在");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setId(id);
        scoreRankMapper.updateById(existing);
        log.info("更新一分一段记录成功，id={}", id);
    }

    @Override
    public void delete(Long id) {
        ScoreRank entity = scoreRankMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        scoreRankMapper.deleteById(id);
        log.info("删除一分一段记录成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        scoreRankMapper.deleteBatchIds(ids);
        log.info("批量删除一分一段记录成功，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<ScoreRankImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(ScoreRankImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> excelKeys = new HashSet<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            ScoreRankImportDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份不能为空");
                continue;
            }
            if (dto.getYear() == null) {
                errors.add("第" + rowNum + "行: 年份不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类不能为空");
                continue;
            }
            if (dto.getScore() == null) {
                errors.add("第" + rowNum + "行: 分数不能为空");
                continue;
            }
            if (dto.getRank() == null) {
                errors.add("第" + rowNum + "行: 位次不能为空");
                continue;
            }

            String key = String.format("%s_%d_%s_%d",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
            if (excelKeys.contains(key)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（省份+年份+科类+分数）");
                continue;
            }
            excelKeys.add(key);

            int dbCount = scoreRankMapper.countByBusinessKey(
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
            if (dbCount > 0) {
                errors.add("第" + rowNum + "行: 数据库已存在该记录（省份=" + dto.getProvince() +
                        ", 年份=" + dto.getYear() + ", 科类=" + dto.getSubjectType() +
                        ", 分数=" + dto.getScore() + "）");
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        OffsetDateTime now = OffsetDateTime.now();
        int insertCount = 0;

        for (ScoreRankImportDTO dto : dataList) {
            ScoreRank entity = ScoreRank.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .province(dto.getProvince())
                    .year(dto.getYear())
                    .subjectType(dto.getSubjectType())
                    .score(dto.getScore())
                    .rank(dto.getRank())
                    .sameScoreCount(dto.getSameScoreCount())
                    .cumulativeCount(dto.getCumulativeCount())
                    .createdAt(now)
                    .build();
            scoreRankMapper.insert(entity);
            insertCount++;
        }

        log.info("导入一分一段数据成功: 新增记录={}条", insertCount);
    }

    private ScoreRankListVO convertToListVO(ScoreRank entity) {
        ScoreRankListVO vo = new ScoreRankListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private ScoreRankDetailVO convertToDetailVO(ScoreRank entity) {
        ScoreRankDetailVO vo = new ScoreRankDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/config/ScoreRankService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/config/ScoreRankServiceImpl.java
git commit -m "feat(score-rank): add service layer with import"
```

---

## Task 9: 一分一段模块 - Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/config/ScoreRankController.java`

- [ ] **Step 1: 创建ScoreRankController**

```java
package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ScoreRankAddDTO;
import com.haifeng.admin.dto.algorithm.config.ScoreRankQueryDTO;
import com.haifeng.admin.service.algorithm.config.ScoreRankService;
import com.haifeng.admin.vo.algorithm.config.ScoreRankDetailVO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/config/score-rank")
@RequiredArgsConstructor
public class ScoreRankController {

    private final ScoreRankService scoreRankService;

    @GetMapping("/page")
    public R<IPage<ScoreRankListVO>> page(@Valid ScoreRankQueryDTO dto) {
        return R.ok(scoreRankService.page(dto));
    }

    @GetMapping("/{id}")
    public R<ScoreRankDetailVO> detail(@PathVariable Long id) {
        return R.ok(scoreRankService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "一分一段管理", action = "新增一分一段记录")
    public R<Long> add(@Valid @RequestBody ScoreRankAddDTO dto) {
        return R.ok(scoreRankService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "一分一段管理", action = "修改一分一段记录")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ScoreRankAddDTO dto) {
        scoreRankService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "一分一段管理", action = "删除一分一段记录")
    public R<Void> delete(@PathVariable Long id) {
        scoreRankService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "一分一段管理", action = "批量删除一分一段记录")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        scoreRankService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "一分一段管理", action = "导入一分一段数据")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        scoreRankService.importData(file);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/config/ScoreRankController.java
git commit -m "feat(score-rank): add controller with import endpoint"
```

---

## Task 10: 批次分数线模块 - DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/BatchScoreLineAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/BatchScoreLineQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/BatchScoreLineListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/BatchScoreLineDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/config/BatchScoreLineImportDTO.java`

- [ ] **Step 1: 创建BatchScoreLineAddDTO**

```java
package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BatchScoreLineAddDTO {

    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "科类不能为空")
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @NotBlank(message = "批次不能为空")
    @Size(max = 50, message = "批次长度不能超过50")
    private String batch;

    @NotNull(message = "分数线不能为空")
    private Integer scoreLine;

    private Integer rankLine;

    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;
}
```

- [ ] **Step 2: 创建BatchScoreLineQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.config;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchScoreLineQueryDTO extends BasePageQueryDTO {

    private String province;

    private Short year;

    private String subjectType;

    private String batch;

    private Integer scoreLine;
}
```

- [ ] **Step 3: 创建BatchScoreLineListVO**

```java
package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

@Data
public class BatchScoreLineListVO {

    private Long id;

    private String province;

    private Short year;

    private String subjectType;

    private String batch;

    private Integer scoreLine;
}
```

- [ ] **Step 4: 创建BatchScoreLineDetailVO**

```java
package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BatchScoreLineDetailVO {

    private Long id;

    private String province;

    private Short year;

    private String subjectType;

    private String batch;

    private Integer scoreLine;

    private Integer rankLine;

    private String remark;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建BatchScoreLineImportDTO**

```java
package com.haifeng.admin.excel.algorithm.config;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BatchScoreLineImportDTO {

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("年份")
    private Short year;

    @ExcelProperty("科类")
    private String subjectType;

    @ExcelProperty("批次")
    private String batch;

    @ExcelProperty("分数线")
    private Integer scoreLine;

    @ExcelProperty("位次线")
    private Integer rankLine;

    @ExcelProperty("备注")
    private String remark;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/BatchScoreLineAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/config/BatchScoreLineQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/BatchScoreLineListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/config/BatchScoreLineDetailVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/config/BatchScoreLineImportDTO.java
git commit -m "feat(batch-score-line): add DTO, VO, and Excel import classes"
```

---

## Task 11: 批次分数线模块 - Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/config/BatchScoreLineService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/config/BatchScoreLineServiceImpl.java`

- [ ] **Step 1: 创建BatchScoreLineService接口**

```java
package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BatchScoreLineService {

    IPage<BatchScoreLineListVO> page(BatchScoreLineQueryDTO dto);

    BatchScoreLineDetailVO detail(Long id);

    Long add(BatchScoreLineAddDTO dto);

    void update(Long id, BatchScoreLineAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importData(MultipartFile file);
}
```

- [ ] **Step 2: 创建BatchScoreLineServiceImpl实现**

```java
package com.haifeng.admin.service.impl.algorithm.config;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.excel.algorithm.config.BatchScoreLineImportDTO;
import com.haifeng.admin.service.algorithm.config.BatchScoreLineService;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.BatchScoreLineMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchScoreLineServiceImpl implements BatchScoreLineService {

    private final BatchScoreLineMapper batchScoreLineMapper;

    @Override
    public IPage<BatchScoreLineListVO> page(BatchScoreLineQueryDTO dto) {
        Page<BatchScoreLine> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<BatchScoreLine> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(BatchScoreLine::getProvince, dto.getProvince());
        }
        if (dto.getYear() != null) {
            wrapper.eq(BatchScoreLine::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(BatchScoreLine::getSubjectType, dto.getSubjectType());
        }
        if (StringUtils.hasText(dto.getBatch())) {
            wrapper.eq(BatchScoreLine::getBatch, dto.getBatch());
        }
        if (dto.getScoreLine() != null) {
            wrapper.eq(BatchScoreLine::getScoreLine, dto.getScoreLine());
        }

        wrapper.orderByDesc(BatchScoreLine::getYear)
               .orderByAsc(BatchScoreLine::getProvince)
               .orderByAsc(BatchScoreLine::getBatch);

        IPage<BatchScoreLine> result = batchScoreLineMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public BatchScoreLineDetailVO detail(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "批次分数线不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Long add(BatchScoreLineAddDTO dto) {
        Long existingId = batchScoreLineMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        if (existingId != null) {
            throw new BusinessException(400, "该批次分数线已存在");
        }

        BatchScoreLine entity = new BatchScoreLine();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(SnowflakeIdGenerator.nextId());

        batchScoreLineMapper.insert(entity);
        log.info("新增批次分数线成功，id={}", entity.getId());
        return entity.getId();
    }

    @Override
    public void update(Long id, BatchScoreLineAddDTO dto) {
        BatchScoreLine existing = batchScoreLineMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "批次分数线不存在");
        }

        Long existingId = batchScoreLineMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该批次分数线已存在");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setId(id);
        batchScoreLineMapper.updateById(existing);
        log.info("更新批次分数线成功，id={}", id);
    }

    @Override
    public void delete(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "批次分数线不存在");
        }
        batchScoreLineMapper.deleteById(id);
        log.info("删除批次分数线成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        batchScoreLineMapper.deleteBatchIds(ids);
        log.info("批量删除批次分数线成功，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<BatchScoreLineImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(BatchScoreLineImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> excelKeys = new HashSet<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            BatchScoreLineImportDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份不能为空");
                continue;
            }
            if (dto.getYear() == null) {
                errors.add("第" + rowNum + "行: 年份不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次不能为空");
                continue;
            }
            if (dto.getScoreLine() == null) {
                errors.add("第" + rowNum + "行: 分数线不能为空");
                continue;
            }

            String key = String.format("%s_%d_%s_%s",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
            if (excelKeys.contains(key)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（省份+年份+科类+批次）");
                continue;
            }
            excelKeys.add(key);

            int dbCount = batchScoreLineMapper.countByBusinessKey(
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
            if (dbCount > 0) {
                errors.add("第" + rowNum + "行: 数据库已存在该记录（省份=" + dto.getProvince() +
                        ", 年份=" + dto.getYear() + ", 科类=" + dto.getSubjectType() +
                        ", 批次=" + dto.getBatch() + "）");
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        OffsetDateTime now = OffsetDateTime.now();
        int insertCount = 0;

        for (BatchScoreLineImportDTO dto : dataList) {
            BatchScoreLine entity = BatchScoreLine.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .province(dto.getProvince())
                    .year(dto.getYear())
                    .subjectType(dto.getSubjectType())
                    .batch(dto.getBatch())
                    .scoreLine(dto.getScoreLine())
                    .rankLine(dto.getRankLine())
                    .remark(dto.getRemark())
                    .createdAt(now)
                    .build();
            batchScoreLineMapper.insert(entity);
            insertCount++;
        }

        log.info("导入批次分数线数据成功: 新增记录={}条", insertCount);
    }

    private BatchScoreLineListVO convertToListVO(BatchScoreLine entity) {
        BatchScoreLineListVO vo = new BatchScoreLineListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private BatchScoreLineDetailVO convertToDetailVO(BatchScoreLine entity) {
        BatchScoreLineDetailVO vo = new BatchScoreLineDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/config/BatchScoreLineService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/config/BatchScoreLineServiceImpl.java
git commit -m "feat(batch-score-line): add service layer with import"
```

---

## Task 12: 批次分数线模块 - Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/config/BatchScoreLineController.java`

- [ ] **Step 1: 创建BatchScoreLineController**

```java
package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.service.algorithm.config.BatchScoreLineService;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/config/batch-score-line")
@RequiredArgsConstructor
public class BatchScoreLineController {

    private final BatchScoreLineService batchScoreLineService;

    @GetMapping("/page")
    public R<IPage<BatchScoreLineListVO>> page(@Valid BatchScoreLineQueryDTO dto) {
        return R.ok(batchScoreLineService.page(dto));
    }

    @GetMapping("/{id}")
    public R<BatchScoreLineDetailVO> detail(@PathVariable Long id) {
        return R.ok(batchScoreLineService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "批次分数线管理", action = "新增批次分数线")
    public R<Long> add(@Valid @RequestBody BatchScoreLineAddDTO dto) {
        return R.ok(batchScoreLineService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "批次分数线管理", action = "修改批次分数线")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BatchScoreLineAddDTO dto) {
        batchScoreLineService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "批次分数线管理", action = "删除批次分数线")
    public R<Void> delete(@PathVariable Long id) {
        batchScoreLineService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "批次分数线管理", action = "批量删除批次分数线")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        batchScoreLineService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "批次分数线管理", action = "导入批次分数线数据")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        batchScoreLineService.importData(file);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/config/BatchScoreLineController.java
git commit -m "feat(batch-score-line): add controller with import endpoint"
```

---

## Task 13: 最终验证与提交

- [ ] **Step 1: 编译验证**

```bash
cd haifeng-admin && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 最终提交**

```bash
git add .
git commit -m "feat(algorithm-config): complete algorithm config module

- Add province reform config CRUD
- Add score rank CRUD with Excel import
- Add batch score line CRUD with Excel import
- Add Flyway migration V12 with seed data"
```

---

## 验收标准

1. **数据库**: V12迁移执行成功，三个表创建完成，省份配置预置数据导入
2. **省份改革配置**: 分页、详情、新增、修改、删除、批量删除 6个接口可用
3. **一分一段位次**: 分页、详情、新增、修改、删除、批量删除、导入 7个接口可用
4. **批次分数线**: 分页、详情、新增、修改、删除、批量删除、导入 7个接口可用
5. **导入功能**: Excel校验完整，重复数据报错拒绝，事务回滚正常
