# 算法约束模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现算法约束模块的三个子模块（约束字典、专业约束关联、安全系数）的CRUD功能

**Architecture:** 遵循项目现有分层架构（Controller-Service-Mapper），复用ScoreRank模块的Excel导入模式

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL

---

## 文件结构

### haifeng-common（Entity + Mapper）

```
src/main/java/com/haifeng/common/
├── entity/algorithm/
│   ├── ConstraintDict.java          # 约束字典实体
│   ├── MajorConstraint.java         # 专业约束关联实体
│   └── SafetyLevelDict.java         # 安全系数实体
└── mapper/algorithm/
    ├── ConstraintDictMapper.java
    ├── MajorConstraintMapper.java
    └── SafetyLevelDictMapper.java
```

### haifeng-admin（Controller/Service/DTO/VO）

```
src/main/java/com/haifeng/admin/
├── controller/algorithm/constraint/
│   ├── ConstraintDictController.java
│   ├── MajorConstraintController.java
│   └── SafetyLevelController.java
├── service/algorithm/constraint/
│   ├── ConstraintDictService.java
│   ├── MajorConstraintService.java
│   └── SafetyLevelService.java
├── service/impl/algorithm/constraint/
│   ├── ConstraintDictServiceImpl.java
│   ├── MajorConstraintServiceImpl.java
│   └── SafetyLevelServiceImpl.java
├── dto/algorithm/constraint/
│   ├── ConstraintDictAddDTO.java
│   ├── ConstraintDictQueryDTO.java
│   ├── MajorConstraintAddDTO.java
│   ├── MajorConstraintQueryDTO.java
│   ├── SafetyLevelAddDTO.java
│   └── SafetyLevelBatchDeleteDTO.java
├── vo/algorithm/constraint/
│   ├── ConstraintDictListVO.java
│   ├── ConstraintDictDetailVO.java
│   ├── MajorConstraintListVO.java
│   ├── MajorConstraintDetailVO.java
│   ├── SafetyLevelListVO.java
│   └── SafetyLevelDetailVO.java
└── excel/algorithm/constraint/
    └── MajorConstraintImportDTO.java
```

### Flyway迁移

```
src/main/resources/db/migration/
└── V13__algorithm_constraint.sql
```

---

## Task 1: Flyway数据库迁移

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V13__algorithm_constraint.sql`

- [ ] **Step 1: 创建Flyway迁移文件**

```sql
-- ============================================================
-- V13: 算法约束模块
-- ============================================================

-- ============================================================
-- 1. 约束条件字典表 (t_constraint_dict)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_constraint_dict (
    code                VARCHAR(50)     PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL UNIQUE,
    category            VARCHAR(30)     NOT NULL,
    description         TEXT,
    severity            VARCHAR(10)     NOT NULL DEFAULT 'HARD',
    check_field         VARCHAR(50),
    check_operator      VARCHAR(20),
    check_value         VARCHAR(100),
    extra_field         VARCHAR(50),
    extra_operator      VARCHAR(20),
    extra_value         VARCHAR(100),
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_severity CHECK (severity IN ('HARD', 'SOFT'))
);

COMMENT ON TABLE t_constraint_dict IS '约束条件字典表';
COMMENT ON COLUMN t_constraint_dict.code IS '约束代码（主键）';
COMMENT ON COLUMN t_constraint_dict.name IS '约束名称';
COMMENT ON COLUMN t_constraint_dict.category IS '约束大类';
COMMENT ON COLUMN t_constraint_dict.severity IS 'HARD=硬限制/SOFT=软提示';
COMMENT ON COLUMN t_constraint_dict.check_field IS '对应t_member_gaokao表字段';
COMMENT ON COLUMN t_constraint_dict.check_operator IS '判断运算符';
COMMENT ON COLUMN t_constraint_dict.check_value IS '判断值';

CREATE INDEX idx_cd_category ON t_constraint_dict (category);
CREATE INDEX idx_cd_is_active ON t_constraint_dict (is_active);

CREATE TRIGGER trg_constraint_dict_updated_at
    BEFORE UPDATE ON t_constraint_dict
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ============================================================
-- 2. 专业约束关联表 (t_major_constraint)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_major_constraint (
    id                  BIGINT          PRIMARY KEY,
    major_code          VARCHAR(20)     NOT NULL,
    major_name          VARCHAR(100)    NOT NULL,
    constraint_code     VARCHAR(50)     NOT NULL,
    constraint_name     VARCHAR(100)    NOT NULL,
    remark              VARCHAR(200),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_major_constraint UNIQUE (major_code, constraint_code)
);

COMMENT ON TABLE t_major_constraint IS '专业约束关联表';
COMMENT ON COLUMN t_major_constraint.major_code IS '专业代码';
COMMENT ON COLUMN t_major_constraint.major_name IS '专业名称';
COMMENT ON COLUMN t_major_constraint.constraint_code IS '约束代码';
COMMENT ON COLUMN t_major_constraint.constraint_name IS '约束名称';

CREATE INDEX idx_mc_major ON t_major_constraint (major_code);
CREATE INDEX idx_mc_constraint ON t_major_constraint (constraint_code);

-- ============================================================
-- 3. 安全系数等级字典 (t_safety_level_dict)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_safety_level_dict (
    level               SMALLINT        PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL UNIQUE,
    name                VARCHAR(30)     NOT NULL,
    name_short          VARCHAR(10)     NOT NULL,
    min_coefficient     NUMERIC(3,2)    NOT NULL,
    max_coefficient     NUMERIC(3,2)    NOT NULL,
    color               VARCHAR(20),
    confidence          VARCHAR(20),
    confidence_reason   VARCHAR(150),
    description         TEXT,
    CONSTRAINT chk_coeff_range CHECK (min_coefficient < max_coefficient)
);

COMMENT ON TABLE t_safety_level_dict IS '安全系数等级字典';
COMMENT ON COLUMN t_safety_level_dict.level IS '等级编号1-5';
COMMENT ON COLUMN t_safety_level_dict.code IS '代码';
COMMENT ON COLUMN t_safety_level_dict.name IS '中文名称';
COMMENT ON COLUMN t_safety_level_dict.name_short IS '简称';
COMMENT ON COLUMN t_safety_level_dict.min_coefficient IS '系数下界';
COMMENT ON COLUMN t_safety_level_dict.max_coefficient IS '系数上界';
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/V13__algorithm_constraint.sql
git commit -m "feat(algorithm-constraint): add V13 flyway migration"
```

---

## Task 2: 约束字典 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ConstraintDict.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java`

- [ ] **Step 1: 创建ConstraintDict实体**

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
@TableName("t_constraint_dict")
public class ConstraintDict {

    @TableId(type = IdType.INPUT)
    private String code;

    private String name;

    private String category;

    private String description;

    private String severity;

    private String checkField;

    private String checkOperator;

    private String checkValue;

    private String extraField;

    private String extraOperator;

    private String extraValue;

    private Integer sortOrder;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建ConstraintDictMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ConstraintDictMapper extends BaseMapper<ConstraintDict> {

    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name}")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name} AND code != #{excludeCode}")
    int countByNameExclude(@Param("name") String name, @Param("excludeCode") String excludeCode);

    @Select("SELECT code FROM t_constraint_dict WHERE name = #{name}")
    String selectCodeByName(@Param("name") String name);
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/ConstraintDict.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/ConstraintDictMapper.java
git commit -m "feat(algorithm-constraint): add ConstraintDict entity and mapper"
```

---

## Task 3: 约束字典 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/ConstraintDictAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/ConstraintDictQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/ConstraintDictListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/ConstraintDictDetailVO.java`

- [ ] **Step 1: 创建ConstraintDictAddDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConstraintDictAddDTO {

    @NotBlank(message = "约束代码不能为空")
    @Size(max = 50, message = "约束代码最长50字符")
    private String code;

    @NotBlank(message = "约束名称不能为空")
    @Size(max = 100, message = "约束名称最长100字符")
    private String name;

    @NotBlank(message = "约束大类不能为空")
    @Size(max = 30, message = "约束大类最长30字符")
    private String category;

    private String description;

    @Pattern(regexp = "^(HARD|SOFT)$", message = "severity只能是HARD或SOFT")
    private String severity = "HARD";

    @Size(max = 50, message = "check_field最长50字符")
    private String checkField;

    @Size(max = 20, message = "check_operator最长20字符")
    private String checkOperator;

    @Size(max = 100, message = "check_value最长100字符")
    private String checkValue;

    @Size(max = 50, message = "extra_field最长50字符")
    private String extraField;

    @Size(max = 20, message = "extra_operator最长20字符")
    private String extraOperator;

    @Size(max = 100, message = "extra_value最长100字符")
    private String extraValue;

    private Integer sortOrder = 0;

    private Boolean isActive = true;
}
```

- [ ] **Step 2: 创建ConstraintDictQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConstraintDictQueryDTO extends BasePageQueryDTO {
    // 无模糊查询，只有分页
}
```

- [ ] **Step 3: 创建ConstraintDictListVO**

```java
package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

@Data
public class ConstraintDictListVO {
    private String code;
    private String category;
    private String severity;
    private String checkField;
    private Boolean isActive;
}
```

- [ ] **Step 4: 创建ConstraintDictDetailVO**

```java
package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ConstraintDictDetailVO {
    private String code;
    private String name;
    private String category;
    private String description;
    private String severity;
    private String checkField;
    private String checkOperator;
    private String checkValue;
    private String extraField;
    private String extraOperator;
    private String extraValue;
    private Integer sortOrder;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/
git commit -m "feat(algorithm-constraint): add ConstraintDict DTO and VO"
```

---

## Task 4: 约束字典 - Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/constraint/ConstraintDictService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/constraint/ConstraintDictServiceImpl.java`

- [ ] **Step 1: 创建ConstraintDictService接口**

```java
package com.haifeng.admin.service.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictQueryDTO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictListVO;

import java.util.List;

public interface ConstraintDictService {

    IPage<ConstraintDictListVO> page(ConstraintDictQueryDTO dto);

    ConstraintDictDetailVO detail(String code);

    void add(ConstraintDictAddDTO dto);

    void update(String code, ConstraintDictAddDTO dto);

    void toggleActive(String code);

    void delete(String code);

    void batchDelete(List<String> codes);
}
```

- [ ] **Step 2: 创建ConstraintDictServiceImpl**

```java
package com.haifeng.admin.service.impl.algorithm.constraint;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.ConstraintDictService;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictListVO;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConstraintDictServiceImpl implements ConstraintDictService {

    private final ConstraintDictMapper constraintDictMapper;

    private static final Set<String> VALID_CHECK_FIELDS = Set.of(
            "subject_type", "second_subject_type", "third_subject_type",
            "score_chinese", "score_math", "score_english",
            "score_subject_1", "score_subject_2", "score_subject_3",
            "is_color_blind", "is_color_weak", "vision_left", "vision_right", "has_smell_disorder",
            "height_cm", "weight_kg", "is_left_handed", "has_tattoo", "has_scar", "has_stutter",
            "is_fresh_graduate", "political_status", "household_type", "is_poverty_county",
            "foreign_language"
    );

    @Override
    public IPage<ConstraintDictListVO> page(ConstraintDictQueryDTO dto) {
        Page<ConstraintDict> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ConstraintDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ConstraintDict::getSortOrder)
               .orderByAsc(ConstraintDict::getCode);

        IPage<ConstraintDict> resultPage = constraintDictMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ConstraintDictDetailVO detail(String code) {
        ConstraintDict entity = constraintDictMapper.selectById(code);
        if (entity == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public void add(ConstraintDictAddDTO dto) {
        if (constraintDictMapper.selectById(dto.getCode()) != null) {
            throw new BusinessException(400, "约束代码已存在");
        }
        if (constraintDictMapper.countByName(dto.getName()) > 0) {
            throw new BusinessException(400, "约束名称已存在");
        }
        validateCheckField(dto.getCheckField());

        ConstraintDict entity = ConstraintDict.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .severity(dto.getSeverity())
                .checkField(dto.getCheckField())
                .checkOperator(dto.getCheckOperator())
                .checkValue(dto.getCheckValue())
                .extraField(dto.getExtraField())
                .extraOperator(dto.getExtraOperator())
                .extraValue(dto.getExtraValue())
                .sortOrder(dto.getSortOrder())
                .isActive(dto.getIsActive())
                .build();

        constraintDictMapper.insert(entity);
        log.info("新增约束字典，code={}", dto.getCode());
    }

    @Override
    public void update(String code, ConstraintDictAddDTO dto) {
        ConstraintDict existing = constraintDictMapper.selectById(code);
        if (existing == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        if (constraintDictMapper.countByNameExclude(dto.getName(), code) > 0) {
            throw new BusinessException(400, "约束名称已存在");
        }
        validateCheckField(dto.getCheckField());

        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());
        existing.setSeverity(dto.getSeverity());
        existing.setCheckField(dto.getCheckField());
        existing.setCheckOperator(dto.getCheckOperator());
        existing.setCheckValue(dto.getCheckValue());
        existing.setExtraField(dto.getExtraField());
        existing.setExtraOperator(dto.getExtraOperator());
        existing.setExtraValue(dto.getExtraValue());
        existing.setSortOrder(dto.getSortOrder());
        existing.setIsActive(dto.getIsActive());

        constraintDictMapper.updateById(existing);
        log.info("修改约束字典，code={}", code);
    }

    @Override
    public void toggleActive(String code) {
        ConstraintDict existing = constraintDictMapper.selectById(code);
        if (existing == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        existing.setIsActive(!existing.getIsActive());
        constraintDictMapper.updateById(existing);
        log.info("切换约束字典状态，code={}, isActive={}", code, existing.getIsActive());
    }

    @Override
    public void delete(String code) {
        int rows = constraintDictMapper.deleteById(code);
        if (rows == 0) {
            throw new BusinessException(404, "约束字典不存在");
        }
        log.info("删除约束字典，code={}", code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        constraintDictMapper.deleteBatchIds(codes);
        log.info("批量删除约束字典，codes={}", codes);
    }

    private void validateCheckField(String checkField) {
        if (checkField != null && !checkField.isEmpty() && !VALID_CHECK_FIELDS.contains(checkField)) {
            throw new BusinessException(400, "check_field不合法，只允许高考档案表字段");
        }
    }

    private ConstraintDictListVO convertToListVO(ConstraintDict entity) {
        ConstraintDictListVO vo = new ConstraintDictListVO();
        vo.setCode(entity.getCode());
        vo.setCategory(entity.getCategory());
        vo.setSeverity(entity.getSeverity());
        vo.setCheckField(entity.getCheckField());
        vo.setIsActive(entity.getIsActive());
        return vo;
    }

    private ConstraintDictDetailVO convertToDetailVO(ConstraintDict entity) {
        ConstraintDictDetailVO vo = new ConstraintDictDetailVO();
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setCategory(entity.getCategory());
        vo.setDescription(entity.getDescription());
        vo.setSeverity(entity.getSeverity());
        vo.setCheckField(entity.getCheckField());
        vo.setCheckOperator(entity.getCheckOperator());
        vo.setCheckValue(entity.getCheckValue());
        vo.setExtraField(entity.getExtraField());
        vo.setExtraOperator(entity.getExtraOperator());
        vo.setExtraValue(entity.getExtraValue());
        vo.setSortOrder(entity.getSortOrder());
        vo.setIsActive(entity.getIsActive());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/constraint/ConstraintDictService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/constraint/ConstraintDictServiceImpl.java
git commit -m "feat(algorithm-constraint): add ConstraintDict service"
```

---

## Task 5: 约束字典 - Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/constraint/ConstraintDictController.java`

- [ ] **Step 1: 创建ConstraintDictController**

```java
package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.ConstraintDictService;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/dict")
@RequiredArgsConstructor
public class ConstraintDictController {

    private final ConstraintDictService constraintDictService;

    @GetMapping("/page")
    public R<IPage<ConstraintDictListVO>> page(@Valid ConstraintDictQueryDTO dto) {
        return R.ok(constraintDictService.page(dto));
    }

    @GetMapping("/{code}")
    public R<ConstraintDictDetailVO> detail(@PathVariable String code) {
        return R.ok(constraintDictService.detail(code));
    }

    @PostMapping
    @OperationLog(module = "约束字典管理", action = "新增约束字典")
    public R<Void> add(@Valid @RequestBody ConstraintDictAddDTO dto) {
        constraintDictService.add(dto);
        return R.ok();
    }

    @PutMapping("/{code}")
    @OperationLog(module = "约束字典管理", action = "修改约束字典")
    public R<Void> update(@PathVariable String code, @Valid @RequestBody ConstraintDictAddDTO dto) {
        constraintDictService.update(code, dto);
        return R.ok();
    }

    @PutMapping("/{code}/toggle")
    @OperationLog(module = "约束字典管理", action = "切换约束字典状态")
    public R<Void> toggleActive(@PathVariable String code) {
        constraintDictService.toggleActive(code);
        return R.ok();
    }

    @DeleteMapping("/{code}")
    @OperationLog(module = "约束字典管理", action = "删除约束字典")
    public R<Void> delete(@PathVariable String code) {
        constraintDictService.delete(code);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "约束字典管理", action = "批量删除约束字典")
    public R<Void> batchDelete(@RequestBody List<String> codes) {
        constraintDictService.batchDelete(codes);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/constraint/ConstraintDictController.java
git commit -m "feat(algorithm-constraint): add ConstraintDict controller"
```

---

## Task 6: 专业约束关联 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/MajorConstraint.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MajorConstraintMapper.java`

- [ ] **Step 1: 创建MajorConstraint实体**

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
@TableName("t_major_constraint")
public class MajorConstraint {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorCode;

    private String majorName;

    private String constraintCode;

    private String constraintName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: 创建MajorConstraintMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.MajorConstraint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorConstraintMapper extends BaseMapper<MajorConstraint> {

    @Select("SELECT COUNT(*) FROM t_major_constraint WHERE major_code = #{majorCode} AND constraint_code = #{constraintCode}")
    int countByBusinessKey(@Param("majorCode") String majorCode, @Param("constraintCode") String constraintCode);
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/MajorConstraint.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MajorConstraintMapper.java
git commit -m "feat(algorithm-constraint): add MajorConstraint entity and mapper"
```

---

## Task 7: 专业约束关联 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/MajorConstraintAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/MajorConstraintQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/MajorConstraintListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/MajorConstraintDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/constraint/MajorConstraintImportDTO.java`

- [ ] **Step 1: 创建MajorConstraintAddDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MajorConstraintAddDTO {

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @NotBlank(message = "约束名称不能为空")
    @Size(max = 100, message = "约束名称最长100字符")
    private String constraintName;

    @Size(max = 200, message = "备注最长200字符")
    private String remark;
}
```

- [ ] **Step 2: 创建MajorConstraintQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorConstraintQueryDTO extends BasePageQueryDTO {

    private String majorCode;

    private String majorName;

    private String constraintCode;

    private String constraintName;
}
```

- [ ] **Step 3: 创建MajorConstraintListVO**

```java
package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

@Data
public class MajorConstraintListVO {
    private Long id;
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
}
```

- [ ] **Step 4: 创建MajorConstraintDetailVO**

```java
package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MajorConstraintDetailVO {
    private Long id;
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
    private String remark;
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建MajorConstraintImportDTO**

```java
package com.haifeng.admin.excel.algorithm.constraint;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MajorConstraintImportDTO {

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("约束名称")
    private String constraintName;

    @ExcelProperty("备注")
    private String remark;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/MajorConstraintAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/MajorConstraintQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/MajorConstraintListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/MajorConstraintDetailVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/constraint/MajorConstraintImportDTO.java
git commit -m "feat(algorithm-constraint): add MajorConstraint DTO, VO and ImportDTO"
```

---

## Task 8: 专业约束关联 - Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/constraint/MajorConstraintService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/constraint/MajorConstraintServiceImpl.java`

- [ ] **Step 1: 创建MajorConstraintService接口**

```java
package com.haifeng.admin.service.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MajorConstraintService {

    IPage<MajorConstraintListVO> page(MajorConstraintQueryDTO dto);

    MajorConstraintDetailVO detail(Long id);

    Long add(MajorConstraintAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importData(MultipartFile file);
}
```

- [ ] **Step 2: 创建MajorConstraintServiceImpl**

```java
package com.haifeng.admin.service.impl.algorithm.constraint;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.excel.algorithm.constraint.MajorConstraintImportDTO;
import com.haifeng.admin.service.algorithm.constraint.MajorConstraintService;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import com.haifeng.common.entity.algorithm.MajorConstraint;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MajorConstraintMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorConstraintServiceImpl implements MajorConstraintService {

    private final MajorConstraintMapper majorConstraintMapper;
    private final MajorMapper majorMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<MajorConstraintListVO> page(MajorConstraintQueryDTO dto) {
        Page<MajorConstraint> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MajorConstraint> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getMajorCode())) {
            wrapper.eq(MajorConstraint::getMajorCode, dto.getMajorCode());
        }
        if (StringUtils.hasText(dto.getMajorName())) {
            wrapper.eq(MajorConstraint::getMajorName, dto.getMajorName());
        }
        if (StringUtils.hasText(dto.getConstraintCode())) {
            wrapper.eq(MajorConstraint::getConstraintCode, dto.getConstraintCode());
        }
        if (StringUtils.hasText(dto.getConstraintName())) {
            wrapper.eq(MajorConstraint::getConstraintName, dto.getConstraintName());
        }

        wrapper.orderByAsc(MajorConstraint::getMajorCode)
               .orderByAsc(MajorConstraint::getConstraintCode);

        IPage<MajorConstraint> resultPage = majorConstraintMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public MajorConstraintDetailVO detail(Long id) {
        MajorConstraint entity = majorConstraintMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业约束关联不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Long add(MajorConstraintAddDTO dto) {
        String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
        if (majorCode == null) {
            throw new BusinessException(400, "专业名称[" + dto.getMajorName() + "]不存在");
        }

        String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());
        if (constraintCode == null) {
            throw new BusinessException(400, "约束名称[" + dto.getConstraintName() + "]不存在");
        }

        if (majorConstraintMapper.countByBusinessKey(majorCode, constraintCode) > 0) {
            throw new BusinessException(400, "该专业约束关联已存在");
        }

        MajorConstraint entity = MajorConstraint.builder()
                .id(snowflakeIdGenerator.nextId())
                .majorCode(majorCode)
                .majorName(dto.getMajorName())
                .constraintCode(constraintCode)
                .constraintName(dto.getConstraintName())
                .remark(dto.getRemark())
                .build();

        majorConstraintMapper.insert(entity);
        log.info("新增专业约束关联，majorCode={}, constraintCode={}", majorCode, constraintCode);
        return entity.getId();
    }

    @Override
    public void delete(Long id) {
        int rows = majorConstraintMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(404, "专业约束关联不存在");
        }
        log.info("删除专业约束关联，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        majorConstraintMapper.deleteBatchIds(ids);
        log.info("批量删除专业约束关联，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<MajorConstraintImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(MajorConstraintImportDTO.class)
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
            MajorConstraintImportDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getMajorName())) {
                errors.add("第" + rowNum + "行: 专业名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getConstraintName())) {
                errors.add("第" + rowNum + "行: 约束名称不能为空");
                continue;
            }

            String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
            if (majorCode == null) {
                errors.add("第" + rowNum + "行: 专业名称[" + dto.getMajorName() + "]不存在");
                continue;
            }

            String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());
            if (constraintCode == null) {
                errors.add("第" + rowNum + "行: 约束名称[" + dto.getConstraintName() + "]不存在");
                continue;
            }

            String businessKey = majorCode + "_" + constraintCode;
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录");
                continue;
            }
            excelKeys.add(businessKey);

            if (majorConstraintMapper.countByBusinessKey(majorCode, constraintCode) > 0) {
                errors.add("第" + rowNum + "行: 数据库已存在该关联（专业=" + dto.getMajorName() + ", 约束=" + dto.getConstraintName() + "）");
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        int insertCount = 0;
        for (MajorConstraintImportDTO dto : dataList) {
            String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
            String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());

            MajorConstraint entity = MajorConstraint.builder()
                    .id(snowflakeIdGenerator.nextId())
                    .majorCode(majorCode)
                    .majorName(dto.getMajorName())
                    .constraintCode(constraintCode)
                    .constraintName(dto.getConstraintName())
                    .remark(dto.getRemark())
                    .build();
            majorConstraintMapper.insert(entity);
            insertCount++;
        }

        log.info("导入专业约束关联成功: 新增记录={}条", insertCount);
    }

    private MajorConstraintListVO convertToListVO(MajorConstraint entity) {
        MajorConstraintListVO vo = new MajorConstraintListVO();
        vo.setId(entity.getId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setConstraintCode(entity.getConstraintCode());
        vo.setConstraintName(entity.getConstraintName());
        return vo;
    }

    private MajorConstraintDetailVO convertToDetailVO(MajorConstraint entity) {
        MajorConstraintDetailVO vo = new MajorConstraintDetailVO();
        vo.setId(entity.getId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setConstraintCode(entity.getConstraintCode());
        vo.setConstraintName(entity.getConstraintName());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/constraint/MajorConstraintService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/constraint/MajorConstraintServiceImpl.java
git commit -m "feat(algorithm-constraint): add MajorConstraint service with Excel import"
```

---

## Task 9: 专业约束关联 - Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/constraint/MajorConstraintController.java`

- [ ] **Step 1: 创建MajorConstraintController**

```java
package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.MajorConstraintService;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/major")
@RequiredArgsConstructor
public class MajorConstraintController {

    private final MajorConstraintService majorConstraintService;

    @GetMapping("/page")
    public R<IPage<MajorConstraintListVO>> page(@Valid MajorConstraintQueryDTO dto) {
        return R.ok(majorConstraintService.page(dto));
    }

    @GetMapping("/{id}")
    public R<MajorConstraintDetailVO> detail(@PathVariable Long id) {
        return R.ok(majorConstraintService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "专业约束管理", action = "新增专业约束关联")
    public R<Long> add(@Valid @RequestBody MajorConstraintAddDTO dto) {
        return R.ok(majorConstraintService.add(dto));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专业约束管理", action = "删除专业约束关联")
    public R<Void> delete(@PathVariable Long id) {
        majorConstraintService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "专业约束管理", action = "批量删除专业约束关联")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        majorConstraintService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "专业约束管理", action = "导入专业约束关联")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        majorConstraintService.importData(file);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/constraint/MajorConstraintController.java
git commit -m "feat(algorithm-constraint): add MajorConstraint controller"
```

---

## Task 10: 安全系数 - Entity和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/SafetyLevelDict.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/SafetyLevelDictMapper.java`

- [ ] **Step 1: 创建SafetyLevelDict实体**

```java
package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_safety_level_dict")
public class SafetyLevelDict {

    @TableId(type = IdType.INPUT)
    private Short level;

    private String code;

    private String name;

    private String nameShort;

    private BigDecimal minCoefficient;

    private BigDecimal maxCoefficient;

    private String color;

    private String confidence;

    private String confidenceReason;

    private String description;
}
```

- [ ] **Step 2: 创建SafetyLevelDictMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SafetyLevelDictMapper extends BaseMapper<SafetyLevelDict> {

    @Select("SELECT COUNT(*) FROM t_safety_level_dict WHERE code = #{code}")
    int countByCode(@Param("code") String code);

    @Select("SELECT COUNT(*) FROM t_safety_level_dict WHERE code = #{code} AND level != #{excludeLevel}")
    int countByCodeExclude(@Param("code") String code, @Param("excludeLevel") Short excludeLevel);
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/SafetyLevelDict.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/SafetyLevelDictMapper.java
git commit -m "feat(algorithm-constraint): add SafetyLevelDict entity and mapper"
```

---

## Task 11: 安全系数 - DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/SafetyLevelAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/SafetyLevelQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/SafetyLevelBatchDeleteDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/SafetyLevelListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/SafetyLevelDetailVO.java`

- [ ] **Step 1: 创建SafetyLevelAddDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SafetyLevelAddDTO {

    @NotNull(message = "等级不能为空")
    @Min(value = 1, message = "等级最小为1")
    @Max(value = 10, message = "等级最大为10")
    private Short level;

    @NotBlank(message = "代码不能为空")
    @Size(max = 20, message = "代码最长20字符")
    private String code;

    @NotBlank(message = "名称不能为空")
    @Size(max = 30, message = "名称最长30字符")
    private String name;

    @NotBlank(message = "简称不能为空")
    @Size(max = 10, message = "简称最长10字符")
    private String nameShort;

    @NotNull(message = "系数下界不能为空")
    @DecimalMin(value = "0.00", message = "系数下界最小为0")
    @DecimalMax(value = "1.00", message = "系数下界最大为1")
    private BigDecimal minCoefficient;

    @NotNull(message = "系数上界不能为空")
    @DecimalMin(value = "0.00", message = "系数上界最小为0")
    @DecimalMax(value = "1.00", message = "系数上界最大为1")
    private BigDecimal maxCoefficient;

    @Size(max = 20, message = "颜色最长20字符")
    private String color;

    @Size(max = 20, message = "置信度最长20字符")
    private String confidence;

    @Size(max = 150, message = "置信度说明最长150字符")
    private String confidenceReason;

    private String description;
}
```

- [ ] **Step 2: 创建SafetyLevelQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SafetyLevelQueryDTO extends BasePageQueryDTO {
    // 无查询条件，只有分页
}
```

- [ ] **Step 3: 创建SafetyLevelBatchDeleteDTO**

```java
package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SafetyLevelBatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的记录")
    private List<Short> levels;
}
```

- [ ] **Step 4: 创建SafetyLevelListVO**

```java
package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SafetyLevelListVO {
    private Short level;
    private String code;
    private String name;
    private String nameShort;
    private BigDecimal minCoefficient;
    private BigDecimal maxCoefficient;
    private String confidence;
}
```

- [ ] **Step 5: 创建SafetyLevelDetailVO**

```java
package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SafetyLevelDetailVO {
    private Short level;
    private String code;
    private String name;
    private String nameShort;
    private BigDecimal minCoefficient;
    private BigDecimal maxCoefficient;
    private String color;
    private String confidence;
    private String confidenceReason;
    private String description;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/SafetyLevelAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/SafetyLevelQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/constraint/SafetyLevelBatchDeleteDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/SafetyLevelListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/constraint/SafetyLevelDetailVO.java
git commit -m "feat(algorithm-constraint): add SafetyLevel DTO and VO"
```

---

## Task 12: 安全系数 - Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/constraint/SafetyLevelService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/constraint/SafetyLevelServiceImpl.java`

- [ ] **Step 1: 创建SafetyLevelService接口**

```java
package com.haifeng.admin.service.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelQueryDTO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelListVO;

import java.util.List;

public interface SafetyLevelService {

    IPage<SafetyLevelListVO> page(SafetyLevelQueryDTO dto);

    SafetyLevelDetailVO detail(Short level);

    void add(SafetyLevelAddDTO dto);

    void update(Short level, SafetyLevelAddDTO dto);

    void delete(Short level);

    void batchDelete(List<Short> levels);
}
```

- [ ] **Step 2: 创建SafetyLevelServiceImpl**

```java
package com.haifeng.admin.service.impl.algorithm.constraint;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.SafetyLevelService;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelListVO;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.SafetyLevelDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyLevelServiceImpl implements SafetyLevelService {

    private final SafetyLevelDictMapper safetyLevelDictMapper;

    @Override
    public IPage<SafetyLevelListVO> page(SafetyLevelQueryDTO dto) {
        Page<SafetyLevelDict> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SafetyLevelDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SafetyLevelDict::getLevel);

        IPage<SafetyLevelDict> resultPage = safetyLevelDictMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public SafetyLevelDetailVO detail(Short level) {
        SafetyLevelDict entity = safetyLevelDictMapper.selectById(level);
        if (entity == null) {
            throw new BusinessException(404, "安全系数等级不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public void add(SafetyLevelAddDTO dto) {
        if (dto.getMinCoefficient().compareTo(dto.getMaxCoefficient()) >= 0) {
            throw new BusinessException(400, "系数下界必须小于系数上界");
        }
        if (safetyLevelDictMapper.selectById(dto.getLevel()) != null) {
            throw new BusinessException(400, "等级已存在");
        }
        if (safetyLevelDictMapper.countByCode(dto.getCode()) > 0) {
            throw new BusinessException(400, "代码已存在");
        }

        SafetyLevelDict entity = SafetyLevelDict.builder()
                .level(dto.getLevel())
                .code(dto.getCode())
                .name(dto.getName())
                .nameShort(dto.getNameShort())
                .minCoefficient(dto.getMinCoefficient())
                .maxCoefficient(dto.getMaxCoefficient())
                .color(dto.getColor())
                .confidence(dto.getConfidence())
                .confidenceReason(dto.getConfidenceReason())
                .description(dto.getDescription())
                .build();

        safetyLevelDictMapper.insert(entity);
        log.info("新增安全系数等级，level={}", dto.getLevel());
    }

    @Override
    public void update(Short level, SafetyLevelAddDTO dto) {
        SafetyLevelDict existing = safetyLevelDictMapper.selectById(level);
        if (existing == null) {
            throw new BusinessException(404, "安全系数等级不存在");
        }
        if (dto.getMinCoefficient().compareTo(dto.getMaxCoefficient()) >= 0) {
            throw new BusinessException(400, "系数下界必须小于系数上界");
        }
        if (safetyLevelDictMapper.countByCodeExclude(dto.getCode(), level) > 0) {
            throw new BusinessException(400, "代码已存在");
        }

        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setNameShort(dto.getNameShort());
        existing.setMinCoefficient(dto.getMinCoefficient());
        existing.setMaxCoefficient(dto.getMaxCoefficient());
        existing.setColor(dto.getColor());
        existing.setConfidence(dto.getConfidence());
        existing.setConfidenceReason(dto.getConfidenceReason());
        existing.setDescription(dto.getDescription());

        safetyLevelDictMapper.updateById(existing);
        log.info("修改安全系数等级，level={}", level);
    }

    @Override
    public void delete(Short level) {
        int rows = safetyLevelDictMapper.deleteById(level);
        if (rows == 0) {
            throw new BusinessException(404, "安全系数等级不存在");
        }
        log.info("删除安全系数等级，level={}", level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Short> levels) {
        if (levels == null || levels.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        safetyLevelDictMapper.deleteBatchIds(levels);
        log.info("批量删除安全系数等级，levels={}", levels);
    }

    private SafetyLevelListVO convertToListVO(SafetyLevelDict entity) {
        SafetyLevelListVO vo = new SafetyLevelListVO();
        vo.setLevel(entity.getLevel());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setNameShort(entity.getNameShort());
        vo.setMinCoefficient(entity.getMinCoefficient());
        vo.setMaxCoefficient(entity.getMaxCoefficient());
        vo.setConfidence(entity.getConfidence());
        return vo;
    }

    private SafetyLevelDetailVO convertToDetailVO(SafetyLevelDict entity) {
        SafetyLevelDetailVO vo = new SafetyLevelDetailVO();
        vo.setLevel(entity.getLevel());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setNameShort(entity.getNameShort());
        vo.setMinCoefficient(entity.getMinCoefficient());
        vo.setMaxCoefficient(entity.getMaxCoefficient());
        vo.setColor(entity.getColor());
        vo.setConfidence(entity.getConfidence());
        vo.setConfidenceReason(entity.getConfidenceReason());
        vo.setDescription(entity.getDescription());
        return vo;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/constraint/SafetyLevelService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/constraint/SafetyLevelServiceImpl.java
git commit -m "feat(algorithm-constraint): add SafetyLevel service"
```

---

## Task 13: 安全系数 - Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/constraint/SafetyLevelController.java`

- [ ] **Step 1: 创建SafetyLevelController**

```java
package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelBatchDeleteDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.SafetyLevelService;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/safety-level")
@RequiredArgsConstructor
public class SafetyLevelController {

    private final SafetyLevelService safetyLevelService;

    @GetMapping("/page")
    public R<IPage<SafetyLevelListVO>> page(@Valid SafetyLevelQueryDTO dto) {
        return R.ok(safetyLevelService.page(dto));
    }

    @GetMapping("/{level}")
    public R<SafetyLevelDetailVO> detail(@PathVariable Short level) {
        return R.ok(safetyLevelService.detail(level));
    }

    @PostMapping
    @OperationLog(module = "安全系数管理", action = "新增安全系数等级")
    public R<Void> add(@Valid @RequestBody SafetyLevelAddDTO dto) {
        safetyLevelService.add(dto);
        return R.ok();
    }

    @PutMapping("/{level}")
    @OperationLog(module = "安全系数管理", action = "修改安全系数等级")
    public R<Void> update(@PathVariable Short level, @Valid @RequestBody SafetyLevelAddDTO dto) {
        safetyLevelService.update(level, dto);
        return R.ok();
    }

    @DeleteMapping("/{level}")
    @OperationLog(module = "安全系数管理", action = "删除安全系数等级")
    public R<Void> delete(@PathVariable Short level) {
        safetyLevelService.delete(level);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "安全系数管理", action = "批量删除安全系数等级")
    public R<Void> batchDelete(@Valid @RequestBody SafetyLevelBatchDeleteDTO dto) {
        safetyLevelService.batchDelete(dto.getLevels());
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/constraint/SafetyLevelController.java
git commit -m "feat(algorithm-constraint): add SafetyLevel controller"
```

---

## Task 14: 添加MajorMapper方法

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorMapper.java`

- [ ] **Step 1: 添加selectCodeByName方法**

在MajorMapper中添加：

```java
@Select("SELECT major_code FROM t_major WHERE major_name = #{majorName} AND is_deleted = false LIMIT 1")
String selectCodeByName(@Param("majorName") String majorName);
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorMapper.java
git commit -m "feat(algorithm-constraint): add selectCodeByName to MajorMapper"
```

---

## Task 15: 验证编译

- [ ] **Step 1: Maven编译验证**

```bash
cd /d/exeProject/ideaProjects/Project-HaiFeng && ./mvnw compile -DskipTests
```

预期输出：BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

根据编译输出修复问题。

- [ ] **Step 3: 最终提交（如有修复）**

```bash
git add -A && git commit -m "fix(algorithm-constraint): fix compilation issues"
```
