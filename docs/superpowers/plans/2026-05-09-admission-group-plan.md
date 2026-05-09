# 专业组管理模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现专业组管理模块，包含三个子模块的完整CRUD，以及Excel一键导入两表的核心功能。

**Architecture:** 使用数据库触发器自动计算专业组聚合数据，Excel导入采用两次遍历模式（先校验后插入），通过分组Key自动创建/复用专业组记录。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL (触发器/函数)

**设计文档:** `docs/superpowers/specs/2026-05-09-admission-group-design.md`

---

## 文件结构

### Flyway迁移
- `haifeng-admin/src/main/resources/db/migration/V11__create_admission_group.sql`

### Entity (haifeng-common)
- `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/AdmissionGroup.java`
- `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/AdmissionMajorScore.java`
- `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/SubjectReqDict.java`

### Mapper (haifeng-common)
- `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionGroupMapper.java`
- `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionMajorScoreMapper.java`
- `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/SubjectReqDictMapper.java`

### DTO (haifeng-admin)
- `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionGroupQueryDTO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionGroupAddDTO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionMajorScoreQueryDTO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionMajorScoreAddDTO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/SubjectReqDictQueryDTO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/SubjectReqDictAddDTO.java`

### VO (haifeng-admin)
- `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionGroupListVO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionGroupDetailVO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionMajorScoreListVO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionMajorScoreDetailVO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/SubjectReqDictListVO.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/SubjectReqDictDetailVO.java`

### Excel DTO (haifeng-admin)
- `haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/admission/AdmissionImportDTO.java`

### Service (haifeng-admin)
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/SubjectReqDictService.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/AdmissionMajorScoreService.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/AdmissionGroupService.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/SubjectReqDictServiceImpl.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionMajorScoreServiceImpl.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionGroupServiceImpl.java`

### Controller (haifeng-admin)
- `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/SubjectReqDictController.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/AdmissionMajorScoreController.java`
- `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/AdmissionGroupController.java`

---

## Task 1: 创建Flyway迁移V11

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V11__create_admission_group.sql`

- [ ] **Step 1: 创建V11迁移文件**

```sql
-- ===========================================================
-- V11: 专业组管理模块
-- 包含: t_admission_group, t_admission_major_score, t_subject_req_dict
-- 以及相关函数和触发器
-- ===========================================================

BEGIN;

-- ===========================================================
-- 1. 选科要求字典表 (t_subject_req_dict)
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_subject_req_dict (
    id                  SERIAL          PRIMARY KEY,
    code                VARCHAR(50)     NOT NULL UNIQUE,
    display_name        VARCHAR(100)    NOT NULL,
    requirement_level   SMALLINT        NOT NULL DEFAULT 0,
    subjects            TEXT[]          NOT NULL DEFAULT '{}',
    requirement_type    VARCHAR(10)     NOT NULL DEFAULT 'NONE',
    sort_order          INTEGER         DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_req_type CHECK (requirement_type IN ('NONE', 'ANY', 'ALL'))
);

COMMENT ON TABLE t_subject_req_dict IS '选科要求字典表';
COMMENT ON COLUMN t_subject_req_dict.code IS '标准代码';
COMMENT ON COLUMN t_subject_req_dict.display_name IS '前端展示名称';
COMMENT ON COLUMN t_subject_req_dict.requirement_level IS '严格等级（0=不限，1=2选1，2=必选，3=均须）';
COMMENT ON COLUMN t_subject_req_dict.subjects IS '涉及的科目';
COMMENT ON COLUMN t_subject_req_dict.requirement_type IS '类型（NONE/ANY/ALL）';

-- 字典表触发器
CREATE TRIGGER trg_subject_req_dict_updated_at
    BEFORE UPDATE ON t_subject_req_dict
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ===========================================================
-- 2. 专业组录取表 (t_admission_group)
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_group (
    id                      SERIAL          PRIMARY KEY,
    university_id           BIGINT          NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    subject_type            VARCHAR(20)     NOT NULL,
    batch                   VARCHAR(50)     NOT NULL,
    enrollment_code         VARCHAR(30),
    group_code              VARCHAR(30)     NOT NULL,
    group_name              VARCHAR(100),
    description             TEXT,
    subject_requirements    VARCHAR(50),
    requirement_level       SMALLINT        DEFAULT 0,
    constraints             TEXT[]          DEFAULT '{}',
    major_count             INTEGER         DEFAULT 0,
    category_count          INTEGER         DEFAULT 0,
    admission_count         INTEGER,
    min_score               INTEGER,
    min_rank                INTEGER,
    avg_score               NUMERIC(6, 2),
    avg_rank                INTEGER,
    max_score               INTEGER,
    max_rank                INTEGER,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_admission_group
        UNIQUE (university_id, year, province, subject_type, batch, group_code)
);

-- 索引
CREATE INDEX idx_ag_university_year ON t_admission_group (university_id, year DESC, province) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_province ON t_admission_group (province, year DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_subject ON t_admission_group (subject_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_batch ON t_admission_group (batch) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_min_score ON t_admission_group (min_score DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_year ON t_admission_group (year DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_constraints_gin ON t_admission_group USING GIN (constraints) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_admission_group IS '专业组录取汇总表';
COMMENT ON COLUMN t_admission_group.university_id IS '大学ID';
COMMENT ON COLUMN t_admission_group.year IS '录取年份';
COMMENT ON COLUMN t_admission_group.province IS '省份';
COMMENT ON COLUMN t_admission_group.subject_type IS '科类（物理类/历史类/文科/理科/不分文理）';
COMMENT ON COLUMN t_admission_group.batch IS '批次（本科批/提前批/专科批）';
COMMENT ON COLUMN t_admission_group.enrollment_code IS '省招代码';
COMMENT ON COLUMN t_admission_group.group_code IS '专业组代码';
COMMENT ON COLUMN t_admission_group.group_name IS '专业组名称';
COMMENT ON COLUMN t_admission_group.subject_requirements IS '选科要求';
COMMENT ON COLUMN t_admission_group.major_count IS '专业数量';
COMMENT ON COLUMN t_admission_group.admission_count IS '录取总人数';

-- 触发器
CREATE TRIGGER trg_admission_group_updated_at
    BEFORE UPDATE ON t_admission_group
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ===========================================================
-- 3. 专业录取明细表 (t_admission_major_score)
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_major_score (
    id                      SERIAL          PRIMARY KEY,
    group_id                INTEGER         NOT NULL REFERENCES t_admission_group(id) ON DELETE CASCADE,
    major_id                BIGINT,
    major_code              VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    subject_requirements    VARCHAR(200),
    requirement_level       SMALLINT        DEFAULT 0,
    education_level         VARCHAR(20),
    duration                VARCHAR(20),
    tuition                 VARCHAR(50),
    description             TEXT,
    admission_count         INTEGER,
    min_score               INTEGER,
    min_rank                INTEGER,
    avg_score               NUMERIC(6, 2),
    avg_rank                INTEGER,
    max_score               INTEGER,
    max_rank                INTEGER,
    constraints             TEXT[]          DEFAULT '{}',
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_ams_group_major UNIQUE (group_id, major_code)
);

-- 索引
CREATE INDEX idx_ams_group_id ON t_admission_major_score (group_id);
CREATE INDEX idx_ams_major_id ON t_admission_major_score (major_id) WHERE major_id IS NOT NULL;
CREATE INDEX idx_ams_major_code ON t_admission_major_score (major_code);
CREATE INDEX idx_ams_min_score ON t_admission_major_score (min_score DESC NULLS LAST);
CREATE INDEX idx_ams_constraints_gin ON t_admission_major_score USING GIN (constraints);

COMMENT ON TABLE t_admission_major_score IS '专业录取明细表';
COMMENT ON COLUMN t_admission_major_score.group_id IS '所属专业组ID';
COMMENT ON COLUMN t_admission_major_score.major_id IS '关联专业表ID';
COMMENT ON COLUMN t_admission_major_score.major_code IS '专业代码';
COMMENT ON COLUMN t_admission_major_score.major_name IS '专业名称';
COMMENT ON COLUMN t_admission_major_score.subject_requirements IS '选科要求';
COMMENT ON COLUMN t_admission_major_score.admission_count IS '录取人数';

-- 触发器
CREATE TRIGGER trg_ams_updated_at
    BEFORE UPDATE ON t_admission_major_score
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ===========================================================
-- 4. 函数：根据选科要求code查等级
-- ===========================================================
CREATE OR REPLACE FUNCTION fn_get_requirement_level(p_req VARCHAR)
RETURNS SMALLINT AS $$
DECLARE
    v_level SMALLINT;
BEGIN
    IF p_req IS NULL OR p_req = '' THEN
        RETURN 0;
    END IF;

    SELECT requirement_level INTO v_level
    FROM t_subject_req_dict
    WHERE code = p_req;

    RETURN COALESCE(v_level, 0);
END;
$$ LANGUAGE plpgsql STABLE;

-- ===========================================================
-- 5. 函数：明细表INSERT/UPDATE时自动设置requirement_level
-- ===========================================================
CREATE OR REPLACE FUNCTION fn_auto_set_req_level()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.subject_requirements IS NULL OR NEW.subject_requirements = '' THEN
        NEW.requirement_level := 0;
        RETURN NEW;
    END IF;

    SELECT COALESCE(requirement_level, 0)
    INTO   NEW.requirement_level
    FROM   t_subject_req_dict
    WHERE  code = NEW.subject_requirements;

    IF NOT FOUND THEN
        NEW.requirement_level := 0;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ams_req_level
    BEFORE INSERT OR UPDATE OF subject_requirements
    ON t_admission_major_score
    FOR EACH ROW
    EXECUTE FUNCTION fn_auto_set_req_level();

-- ===========================================================
-- 6. 函数：重算单个专业组的聚合数据
-- ===========================================================
CREATE OR REPLACE FUNCTION fn_recalc_group(p_group_id INTEGER)
RETURNS VOID AS $$
DECLARE
    v_least_req   VARCHAR(200);
    v_least_level SMALLINT;
BEGIN
    IF p_group_id IS NULL THEN RETURN; END IF;

    -- 聚合分数 + 数量
    UPDATE t_admission_group g
    SET
        admission_count = (
            SELECT COALESCE(SUM(ams.admission_count), 0)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.major_id IS NOT NULL
        ),
        min_score = (
            SELECT MIN(ams.min_score)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.min_score IS NOT NULL
        ),
        min_rank = (
            SELECT MAX(ams.min_rank)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.min_rank IS NOT NULL
        ),
        avg_score = (
            SELECT ROUND(
                SUM(ams.avg_score * ams.admission_count)
                / NULLIF(SUM(ams.admission_count), 0)
            , 2)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.major_id IS NOT NULL
            AND    ams.avg_score IS NOT NULL
            AND    ams.admission_count IS NOT NULL
        ),
        avg_rank = (
            SELECT ROUND(
                SUM(ams.avg_rank::NUMERIC * ams.admission_count)
                / NULLIF(SUM(ams.admission_count), 0)
            )::INTEGER
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.major_id IS NOT NULL
            AND    ams.avg_rank IS NOT NULL
            AND    ams.admission_count IS NOT NULL
        ),
        max_score = (
            SELECT MAX(ams.max_score)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.max_score IS NOT NULL
        ),
        max_rank = (
            SELECT MIN(ams.max_rank)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.max_rank IS NOT NULL
        ),
        major_count = (
            SELECT COUNT(*)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id  = p_group_id
            AND    ams.major_id  IS NOT NULL
        ),
        category_count = (
            SELECT COUNT(*)
            FROM   t_admission_major_score ams
            WHERE  ams.group_id = p_group_id
            AND    ams.major_id IS NULL
        ),
        updated_at = NOW()
    WHERE g.id = p_group_id;

    -- 推导选科要求（取最宽松）
    SELECT ams.subject_requirements, ams.requirement_level
    INTO   v_least_req, v_least_level
    FROM   t_admission_major_score ams
    WHERE  ams.group_id = p_group_id
    AND    ams.subject_requirements IS NOT NULL
    AND    ams.subject_requirements <> ''
    ORDER BY ams.requirement_level ASC, ams.subject_requirements ASC
    LIMIT 1;

    IF FOUND THEN
        UPDATE t_admission_group
        SET    subject_requirements = v_least_req,
               requirement_level   = v_least_level
        WHERE  id = p_group_id;
    ELSE
        UPDATE t_admission_group
        SET    subject_requirements = '不限',
               requirement_level   = 0
        WHERE  id = p_group_id
        AND    (subject_requirements IS NULL OR subject_requirements = '');
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================
-- 7. 函数：全量重算所有专业组
-- ===========================================================
CREATE OR REPLACE FUNCTION fn_recalc_all_groups()
RETURNS TABLE(recalced_count INTEGER) AS $$
DECLARE
    v_count INTEGER := 0;
    v_group_id INTEGER;
BEGIN
    FOR v_group_id IN
        SELECT id FROM t_admission_group WHERE is_deleted = FALSE
    LOOP
        PERFORM fn_recalc_group(v_group_id);
        v_count := v_count + 1;
    END LOOP;

    RETURN QUERY SELECT v_count;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================
-- 8. 触发器函数：明细表变更时自动重算专业组
-- ===========================================================
CREATE OR REPLACE FUNCTION fn_on_major_score_changed()
RETURNS TRIGGER AS $$
DECLARE
    v_old_group_id INTEGER;
    v_new_group_id INTEGER;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_old_group_id := OLD.group_id;
        v_new_group_id := NULL;
    ELSIF TG_OP = 'INSERT' THEN
        v_old_group_id := NULL;
        v_new_group_id := NEW.group_id;
    ELSE
        v_old_group_id := OLD.group_id;
        v_new_group_id := NEW.group_id;
    END IF;

    IF v_new_group_id IS NOT NULL THEN
        PERFORM fn_recalc_group(v_new_group_id);
    END IF;

    IF v_old_group_id IS NOT NULL
       AND v_old_group_id IS DISTINCT FROM v_new_group_id THEN
        PERFORM fn_recalc_group(v_old_group_id);
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_major_score_changed
    AFTER INSERT OR UPDATE OR DELETE
    ON t_admission_major_score
    FOR EACH ROW
    EXECUTE FUNCTION fn_on_major_score_changed();

-- ===========================================================
-- 9. 初始化选科要求字典数据
-- ===========================================================

-- 不限
INSERT INTO t_subject_req_dict (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES ('不限', '不限', 0, '{}', 'NONE', 0)
ON CONFLICT (code) DO NOTHING;

-- 单科必选
INSERT INTO t_subject_req_dict (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    ('物理', '物理(必选)', 2, '{物理}', 'ALL', 40),
    ('化学', '化学(必选)', 2, '{化学}', 'ALL', 41),
    ('生物', '生物(必选)', 2, '{生物}', 'ALL', 42),
    ('历史', '历史(必选)', 2, '{历史}', 'ALL', 43),
    ('政治', '思想政治(必选)', 2, '{思想政治}', 'ALL', 44),
    ('地理', '地理(必选)', 2, '{地理}', 'ALL', 45)
ON CONFLICT (code) DO NOTHING;

-- 2选1
INSERT INTO t_subject_req_dict (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    ('物理或化学', '物理、化学(2选1)', 1, '{物理,化学}', 'ANY', 50),
    ('物理或生物', '物理、生物(2选1)', 1, '{物理,生物}', 'ANY', 51),
    ('物理或历史', '物理、历史(2选1)', 1, '{物理,历史}', 'ANY', 52),
    ('化学或生物', '化学、生物(2选1)', 1, '{化学,生物}', 'ANY', 55),
    ('化学或历史', '化学、历史(2选1)', 1, '{化学,历史}', 'ANY', 56),
    ('生物或历史', '生物、历史(2选1)', 1, '{生物,历史}', 'ANY', 59)
ON CONFLICT (code) DO NOTHING;

-- 2科均须
INSERT INTO t_subject_req_dict (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    ('物理+化学', '物理+化学(均须选考)', 3, '{物理,化学}', 'ALL', 70),
    ('物理+生物', '物理+生物(均须选考)', 3, '{物理,生物}', 'ALL', 71),
    ('化学+生物', '化学+生物(均须选考)', 3, '{化学,生物}', 'ALL', 75),
    ('历史+政治', '历史+思想政治(均须选考)', 3, '{历史,思想政治}', 'ALL', 82),
    ('历史+地理', '历史+地理(均须选考)', 3, '{历史,地理}', 'ALL', 83)
ON CONFLICT (code) DO NOTHING;

COMMIT;
```

- [ ] **Step 2: 提交迁移文件**

```bash
git add haifeng-admin/src/main/resources/db/migration/V11__create_admission_group.sql
git commit -m "feat(db): add V11 migration for admission group module

- Create t_subject_req_dict table
- Create t_admission_group table
- Create t_admission_major_score table
- Add aggregate calculation functions and triggers
- Initialize subject requirement dictionary data"
```

---

## Task 2: 创建Entity类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/SubjectReqDict.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/AdmissionGroup.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/AdmissionMajorScore.java`

- [ ] **Step 1: 创建SubjectReqDict实体**

```java
package com.haifeng.common.entity.algorithm;

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
@TableName(value = "t_subject_req_dict", autoResultMap = true)
public class SubjectReqDict {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String displayName;

    private Short requirementLevel;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> subjects;

    private String requirementType;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建AdmissionGroup实体**

```java
package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_admission_group", autoResultMap = true)
public class AdmissionGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long universityId;

    private Short year;

    private String province;

    private String subjectType;

    private String batch;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    private String description;

    private String subjectRequirements;

    private Short requirementLevel;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> constraints;

    private Integer majorCount;

    private Integer categoryCount;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Integer avgRank;

    private Integer maxScore;

    private Integer maxRank;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建AdmissionMajorScore实体**

```java
package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_admission_major_score", autoResultMap = true)
public class AdmissionMajorScore {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer groupId;

    private Long majorId;

    private String majorCode;

    private String majorName;

    private String subjectRequirements;

    private Short requirementLevel;

    private String educationLevel;

    private String duration;

    private String tuition;

    private String description;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Integer avgRank;

    private Integer maxScore;

    private Integer maxRank;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> constraints;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: 提交Entity类**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/
git commit -m "feat(entity): add admission group module entities

- SubjectReqDict: subject requirement dictionary
- AdmissionGroup: admission group summary
- AdmissionMajorScore: major score details"
```

---

## Task 3: 创建Mapper接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/SubjectReqDictMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionGroupMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionMajorScoreMapper.java`

- [ ] **Step 1: 创建SubjectReqDictMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SubjectReqDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectReqDictMapper extends BaseMapper<SubjectReqDict> {

    @Select("SELECT requirement_level FROM t_subject_req_dict WHERE code = #{code} LIMIT 1")
    Short selectLevelByCode(@Param("code") String code);
}
```

- [ ] **Step 2: 创建AdmissionGroupMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdmissionGroupMapper extends BaseMapper<AdmissionGroup> {

    @Select("SELECT id FROM t_admission_group " +
            "WHERE university_id = #{universityId} " +
            "AND year = #{year} " +
            "AND province = #{province} " +
            "AND subject_type = #{subjectType} " +
            "AND batch = #{batch} " +
            "AND group_code = #{groupCode} " +
            "AND is_deleted = FALSE " +
            "LIMIT 1")
    Integer selectIdByBusinessKey(
            @Param("universityId") Long universityId,
            @Param("year") Short year,
            @Param("province") String province,
            @Param("subjectType") String subjectType,
            @Param("batch") String batch,
            @Param("groupCode") String groupCode);

    @Select("SELECT * FROM fn_recalc_all_groups()")
    Integer recalcAllGroups();
}
```

- [ ] **Step 3: 创建AdmissionMajorScoreMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdmissionMajorScoreMapper extends BaseMapper<AdmissionMajorScore> {

    @Select("SELECT COUNT(*) FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND major_code = #{majorCode}")
    int countByGroupIdAndMajorCode(
            @Param("groupId") Integer groupId,
            @Param("majorCode") String majorCode);
}
```

- [ ] **Step 4: 提交Mapper接口**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/
git commit -m "feat(mapper): add admission group module mappers

- SubjectReqDictMapper with level lookup
- AdmissionGroupMapper with business key lookup and recalc
- AdmissionMajorScoreMapper with duplicate check"
```

---

## Task 4: 创建DTO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/SubjectReqDictQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/SubjectReqDictAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionGroupQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionGroupAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionMajorScoreQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/AdmissionMajorScoreAddDTO.java`

- [ ] **Step 1: 创建SubjectReqDictQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectReqDictQueryDTO extends BasePageQueryDTO {

    private String code;

    private String displayName;

    private String requirementType;
}
```

- [ ] **Step 2: 创建SubjectReqDictAddDTO**

```java
package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubjectReqDictAddDTO {

    @NotBlank(message = "标准代码不能为空")
    private String code;

    @NotBlank(message = "展示名称不能为空")
    private String displayName;

    @NotNull(message = "严格等级不能为空")
    private Short requirementLevel;

    private List<String> subjects;

    @NotBlank(message = "类型不能为空")
    private String requirementType;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建AdmissionGroupQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {

    private String universityName;

    private Short year;

    private String province;

    private String subjectType;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    private Boolean isDeleted;
}
```

- [ ] **Step 4: 创建AdmissionGroupAddDTO**

```java
package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdmissionGroupAddDTO {

    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "科类不能为空")
    private String subjectType;

    @NotBlank(message = "批次不能为空")
    private String batch;

    private String enrollmentCode;

    @NotBlank(message = "专业组代码不能为空")
    private String groupCode;

    private String groupName;

    private String description;

    private List<String> constraints;
}
```

- [ ] **Step 5: 创建AdmissionMajorScoreQueryDTO**

```java
package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionMajorScoreQueryDTO extends BasePageQueryDTO {

    private Integer groupId;

    private String majorCode;

    private String majorName;

    private String educationLevel;
}
```

- [ ] **Step 6: 创建AdmissionMajorScoreAddDTO**

```java
package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdmissionMajorScoreAddDTO {

    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    private Long majorId;

    @NotBlank(message = "专业代码不能为空")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    private String subjectRequirements;

    private String educationLevel;

    private String duration;

    private String tuition;

    private String description;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Integer avgRank;

    private Integer maxScore;

    private Integer maxRank;

    private List<String> constraints;
}
```

- [ ] **Step 7: 提交DTO类**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/
git commit -m "feat(dto): add admission group module DTOs

- SubjectReqDictQueryDTO, SubjectReqDictAddDTO
- AdmissionGroupQueryDTO, AdmissionGroupAddDTO
- AdmissionMajorScoreQueryDTO, AdmissionMajorScoreAddDTO"
```

---

## Task 5: 创建VO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/SubjectReqDictListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/SubjectReqDictDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionGroupListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionGroupDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionMajorScoreListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/AdmissionMajorScoreDetailVO.java`

- [ ] **Step 1: 创建SubjectReqDictListVO和DetailVO**

```java
package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;

import java.util.List;

@Data
public class SubjectReqDictListVO {

    private Integer id;

    private String code;

    private String displayName;

    private Short requirementLevel;

    private List<String> subjects;

    private String requirementType;

    private Integer sortOrder;
}
```

```java
package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class SubjectReqDictDetailVO {

    private Integer id;

    private String code;

    private String displayName;

    private Short requirementLevel;

    private List<String> subjects;

    private String requirementType;

    private Integer sortOrder;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建AdmissionGroupListVO和DetailVO**

```java
package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdmissionGroupListVO {

    private Integer id;

    private Long universityId;

    private String universityName;

    private Short year;

    private String province;

    private String subjectType;

    private String batch;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    private String subjectRequirements;

    private Integer majorCount;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Boolean isDeleted;
}
```

```java
package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdmissionGroupDetailVO {

    private Integer id;

    private Long universityId;

    private String universityName;

    private Short year;

    private String province;

    private String subjectType;

    private String batch;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    private String description;

    private String subjectRequirements;

    private Short requirementLevel;

    private List<String> constraints;

    private Integer majorCount;

    private Integer categoryCount;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Integer avgRank;

    private Integer maxScore;

    private Integer maxRank;

    private Boolean isDeleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建AdmissionMajorScoreListVO和DetailVO**

```java
package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdmissionMajorScoreListVO {

    private Integer id;

    private Integer groupId;

    private String majorCode;

    private String majorName;

    private String educationLevel;

    private String subjectRequirements;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;
}
```

```java
package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdmissionMajorScoreDetailVO {

    private Integer id;

    private Integer groupId;

    private Long majorId;

    private String majorCode;

    private String majorName;

    private String subjectRequirements;

    private Short requirementLevel;

    private String educationLevel;

    private String duration;

    private String tuition;

    private String description;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Integer avgRank;

    private Integer maxScore;

    private Integer maxRank;

    private List<String> constraints;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: 提交VO类**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/
git commit -m "feat(vo): add admission group module VOs

- SubjectReqDictListVO, SubjectReqDictDetailVO
- AdmissionGroupListVO, AdmissionGroupDetailVO
- AdmissionMajorScoreListVO, AdmissionMajorScoreDetailVO"
```

---

## Task 6: 实现SubjectReqDict Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/SubjectReqDictService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/SubjectReqDictServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/SubjectReqDictController.java`

- [ ] **Step 1: 创建SubjectReqDictService接口**

```java
package com.haifeng.admin.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictAddDTO;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictQueryDTO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictDetailVO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictListVO;

import java.util.List;

public interface SubjectReqDictService {

    IPage<SubjectReqDictListVO> page(SubjectReqDictQueryDTO dto);

    SubjectReqDictDetailVO detail(Integer id);

    Integer add(SubjectReqDictAddDTO dto);

    void update(Integer id, SubjectReqDictAddDTO dto);

    void delete(Integer id);

    void batchDelete(List<Integer> ids);
}
```

- [ ] **Step 2: 创建SubjectReqDictServiceImpl**

```java
package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictAddDTO;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictQueryDTO;
import com.haifeng.admin.service.algorithm.admission.SubjectReqDictService;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictDetailVO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictListVO;
import com.haifeng.common.entity.algorithm.SubjectReqDict;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.SubjectReqDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectReqDictServiceImpl implements SubjectReqDictService {

    private final SubjectReqDictMapper subjectReqDictMapper;

    @Override
    public IPage<SubjectReqDictListVO> page(SubjectReqDictQueryDTO dto) {
        Page<SubjectReqDict> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SubjectReqDict> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getCode())) {
            wrapper.like(SubjectReqDict::getCode, dto.getCode());
        }
        if (StringUtils.hasText(dto.getDisplayName())) {
            wrapper.like(SubjectReqDict::getDisplayName, dto.getDisplayName());
        }
        if (StringUtils.hasText(dto.getRequirementType())) {
            wrapper.eq(SubjectReqDict::getRequirementType, dto.getRequirementType());
        }

        wrapper.orderByAsc(SubjectReqDict::getSortOrder)
               .orderByAsc(SubjectReqDict::getId);

        IPage<SubjectReqDict> result = subjectReqDictMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            SubjectReqDictListVO vo = new SubjectReqDictListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public SubjectReqDictDetailVO detail(Integer id) {
        SubjectReqDict entity = subjectReqDictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选科要求不存在");
        }

        SubjectReqDictDetailVO vo = new SubjectReqDictDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(SubjectReqDictAddDTO dto) {
        // 检查code是否重复
        LambdaQueryWrapper<SubjectReqDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectReqDict::getCode, dto.getCode());
        if (subjectReqDictMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "标准代码已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        SubjectReqDict entity = SubjectReqDict.builder()
                .code(dto.getCode())
                .displayName(dto.getDisplayName())
                .requirementLevel(dto.getRequirementLevel())
                .subjects(dto.getSubjects())
                .requirementType(dto.getRequirementType())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        subjectReqDictMapper.insert(entity);
        log.info("新增选科要求成功: id={}, code={}", entity.getId(), entity.getCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, SubjectReqDictAddDTO dto) {
        SubjectReqDict entity = subjectReqDictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选科要求不存在");
        }

        // 检查code是否与其他记录重复
        LambdaQueryWrapper<SubjectReqDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectReqDict::getCode, dto.getCode())
               .ne(SubjectReqDict::getId, id);
        if (subjectReqDictMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "标准代码已存在");
        }

        entity.setCode(dto.getCode());
        entity.setDisplayName(dto.getDisplayName());
        entity.setRequirementLevel(dto.getRequirementLevel());
        entity.setSubjects(dto.getSubjects());
        entity.setRequirementType(dto.getRequirementType());
        entity.setSortOrder(dto.getSortOrder());
        entity.setUpdatedAt(OffsetDateTime.now());

        subjectReqDictMapper.updateById(entity);
        log.info("修改选科要求成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        SubjectReqDict entity = subjectReqDictMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选科要求不存在");
        }

        subjectReqDictMapper.deleteById(id);
        log.info("删除选科要求成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        subjectReqDictMapper.deleteBatchIds(ids);
        log.info("批量删除选科要求成功: 数量={}", ids.size());
    }
}
```

- [ ] **Step 3: 创建SubjectReqDictController**

```java
package com.haifeng.admin.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictAddDTO;
import com.haifeng.admin.dto.algorithm.admission.SubjectReqDictQueryDTO;
import com.haifeng.admin.service.algorithm.admission.SubjectReqDictService;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictDetailVO;
import com.haifeng.admin.vo.algorithm.admission.SubjectReqDictListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/admission/subject-req")
@RequiredArgsConstructor
public class SubjectReqDictController {

    private final SubjectReqDictService subjectReqDictService;

    @GetMapping("/page")
    public R<IPage<SubjectReqDictListVO>> page(@Valid SubjectReqDictQueryDTO dto) {
        return R.ok(subjectReqDictService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SubjectReqDictDetailVO> detail(@PathVariable Integer id) {
        return R.ok(subjectReqDictService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "选科要求管理", action = "新增选科要求")
    public R<Integer> add(@Valid @RequestBody SubjectReqDictAddDTO dto) {
        return R.ok(subjectReqDictService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "选科要求管理", action = "修改选科要求")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody SubjectReqDictAddDTO dto) {
        subjectReqDictService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "选科要求管理", action = "删除选科要求")
    public R<Void> delete(@PathVariable Integer id) {
        subjectReqDictService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "选科要求管理", action = "批量删除选科要求")
    public R<Void> batchDelete(@RequestBody List<Integer> ids) {
        subjectReqDictService.batchDelete(ids);
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交SubjectReqDict模块**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/SubjectReqDictService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/SubjectReqDictServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/SubjectReqDictController.java
git commit -m "feat(subject-req): implement SubjectReqDict CRUD

- Service interface and implementation
- Controller with full CRUD endpoints
- Batch delete support"
```

---

## Task 7: 实现AdmissionMajorScore Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/AdmissionMajorScoreService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionMajorScoreServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/AdmissionMajorScoreController.java`

- [ ] **Step 1: 创建AdmissionMajorScoreService接口**

```java
package com.haifeng.admin.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreQueryDTO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreListVO;

import java.util.List;

public interface AdmissionMajorScoreService {

    IPage<AdmissionMajorScoreListVO> page(AdmissionMajorScoreQueryDTO dto);

    AdmissionMajorScoreDetailVO detail(Integer id);

    Integer add(AdmissionMajorScoreAddDTO dto);

    void update(Integer id, AdmissionMajorScoreAddDTO dto);

    void delete(Integer id);

    void batchDelete(List<Integer> ids);
}
```

- [ ] **Step 2: 创建AdmissionMajorScoreServiceImpl**

```java
package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionMajorScoreService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreListVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionMajorScoreServiceImpl implements AdmissionMajorScoreService {

    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final AdmissionGroupMapper admissionGroupMapper;

    @Override
    public IPage<AdmissionMajorScoreListVO> page(AdmissionMajorScoreQueryDTO dto) {
        Page<AdmissionMajorScore> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();

        if (dto.getGroupId() != null) {
            wrapper.eq(AdmissionMajorScore::getGroupId, dto.getGroupId());
        }
        if (StringUtils.hasText(dto.getMajorCode())) {
            wrapper.like(AdmissionMajorScore::getMajorCode, dto.getMajorCode());
        }
        if (StringUtils.hasText(dto.getMajorName())) {
            wrapper.like(AdmissionMajorScore::getMajorName, dto.getMajorName());
        }
        if (StringUtils.hasText(dto.getEducationLevel())) {
            wrapper.eq(AdmissionMajorScore::getEducationLevel, dto.getEducationLevel());
        }

        wrapper.orderByDesc(AdmissionMajorScore::getId);

        IPage<AdmissionMajorScore> result = admissionMajorScoreMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            AdmissionMajorScoreListVO vo = new AdmissionMajorScoreListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public AdmissionMajorScoreDetailVO detail(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        AdmissionMajorScoreDetailVO vo = new AdmissionMajorScoreDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(AdmissionMajorScoreAddDTO dto) {
        // 检查专业组是否存在
        AdmissionGroup group = admissionGroupMapper.selectById(dto.getGroupId());
        if (group == null || group.getIsDeleted()) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 检查同一专业组内专业代码是否重复
        int count = admissionMajorScoreMapper.countByGroupIdAndMajorCode(dto.getGroupId(), dto.getMajorCode());
        if (count > 0) {
            throw new BusinessException(400, "该专业组内专业代码已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        AdmissionMajorScore entity = AdmissionMajorScore.builder()
                .groupId(dto.getGroupId())
                .majorId(dto.getMajorId())
                .majorCode(dto.getMajorCode())
                .majorName(dto.getMajorName())
                .subjectRequirements(dto.getSubjectRequirements())
                .educationLevel(dto.getEducationLevel())
                .duration(dto.getDuration())
                .tuition(dto.getTuition())
                .description(dto.getDescription())
                .admissionCount(dto.getAdmissionCount())
                .minScore(dto.getMinScore())
                .minRank(dto.getMinRank())
                .avgScore(dto.getAvgScore())
                .avgRank(dto.getAvgRank())
                .maxScore(dto.getMaxScore())
                .maxRank(dto.getMaxRank())
                .constraints(dto.getConstraints())
                .createdAt(now)
                .updatedAt(now)
                .build();

        admissionMajorScoreMapper.insert(entity);
        log.info("新增专业录取明细成功: id={}, groupId={}, majorCode={}",
                entity.getId(), dto.getGroupId(), dto.getMajorCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, AdmissionMajorScoreAddDTO dto) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        // 如果修改了专业代码，检查是否与同组内其他记录重复
        if (!entity.getMajorCode().equals(dto.getMajorCode())) {
            int count = admissionMajorScoreMapper.countByGroupIdAndMajorCode(dto.getGroupId(), dto.getMajorCode());
            if (count > 0) {
                throw new BusinessException(400, "该专业组内专业代码已存在");
            }
        }

        entity.setGroupId(dto.getGroupId());
        entity.setMajorId(dto.getMajorId());
        entity.setMajorCode(dto.getMajorCode());
        entity.setMajorName(dto.getMajorName());
        entity.setSubjectRequirements(dto.getSubjectRequirements());
        entity.setEducationLevel(dto.getEducationLevel());
        entity.setDuration(dto.getDuration());
        entity.setTuition(dto.getTuition());
        entity.setDescription(dto.getDescription());
        entity.setAdmissionCount(dto.getAdmissionCount());
        entity.setMinScore(dto.getMinScore());
        entity.setMinRank(dto.getMinRank());
        entity.setAvgScore(dto.getAvgScore());
        entity.setAvgRank(dto.getAvgRank());
        entity.setMaxScore(dto.getMaxScore());
        entity.setMaxRank(dto.getMaxRank());
        entity.setConstraints(dto.getConstraints());
        entity.setUpdatedAt(OffsetDateTime.now());

        admissionMajorScoreMapper.updateById(entity);
        log.info("修改专业录取明细成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        admissionMajorScoreMapper.deleteById(id);
        log.info("删除专业录取明细成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        admissionMajorScoreMapper.deleteBatchIds(ids);
        log.info("批量删除专业录取明细成功: 数量={}", ids.size());
    }
}
```

- [ ] **Step 3: 创建AdmissionMajorScoreController**

```java
package com.haifeng.admin.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionMajorScoreService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/admission/major-score")
@RequiredArgsConstructor
public class AdmissionMajorScoreController {

    private final AdmissionMajorScoreService admissionMajorScoreService;

    @GetMapping("/page")
    public R<IPage<AdmissionMajorScoreListVO>> page(@Valid AdmissionMajorScoreQueryDTO dto) {
        return R.ok(admissionMajorScoreService.page(dto));
    }

    @GetMapping("/{id}")
    public R<AdmissionMajorScoreDetailVO> detail(@PathVariable Integer id) {
        return R.ok(admissionMajorScoreService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "专业录取明细管理", action = "新增专业明细")
    public R<Integer> add(@Valid @RequestBody AdmissionMajorScoreAddDTO dto) {
        return R.ok(admissionMajorScoreService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "专业录取明细管理", action = "修改专业明细")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody AdmissionMajorScoreAddDTO dto) {
        admissionMajorScoreService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专业录取明细管理", action = "删除专业明细")
    public R<Void> delete(@PathVariable Integer id) {
        admissionMajorScoreService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "专业录取明细管理", action = "批量删除专业明细")
    public R<Void> batchDelete(@RequestBody List<Integer> ids) {
        admissionMajorScoreService.batchDelete(ids);
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交AdmissionMajorScore模块**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/AdmissionMajorScoreService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionMajorScoreServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/AdmissionMajorScoreController.java
git commit -m "feat(major-score): implement AdmissionMajorScore CRUD

- Service with group existence check
- Unique constraint on (groupId, majorCode)
- DB trigger auto-recalculates group aggregates"
```

---

## Task 8: 实现AdmissionGroup Service和Controller（基础CRUD）

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/AdmissionGroupService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionGroupServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/AdmissionGroupController.java`

- [ ] **Step 1: 创建AdmissionGroupService接口**

```java
package com.haifeng.admin.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdmissionGroupService {

    IPage<AdmissionGroupListVO> page(AdmissionGroupQueryDTO dto);

    AdmissionGroupDetailVO detail(Integer id);

    Integer add(AdmissionGroupAddDTO dto);

    void update(Integer id, AdmissionGroupAddDTO dto);

    void updateStatus(Integer id, Boolean isDeleted);

    void delete(Integer id);

    void batchDelete(List<Integer> ids);

    void importData(MultipartFile file);

    Integer recalcAll();
}
```

- [ ] **Step 2: 创建AdmissionGroupServiceImpl（基础CRUD部分）**

```java
package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionGroupService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupListVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionGroupServiceImpl implements AdmissionGroupService {

    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<AdmissionGroupListVO> page(AdmissionGroupQueryDTO dto) {
        Page<AdmissionGroup> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdmissionGroup> wrapper = new LambdaQueryWrapper<>();

        if (dto.getYear() != null) {
            wrapper.eq(AdmissionGroup::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(AdmissionGroup::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(AdmissionGroup::getSubjectType, dto.getSubjectType());
        }
        if (StringUtils.hasText(dto.getEnrollmentCode())) {
            wrapper.like(AdmissionGroup::getEnrollmentCode, dto.getEnrollmentCode());
        }
        if (StringUtils.hasText(dto.getGroupCode())) {
            wrapper.like(AdmissionGroup::getGroupCode, dto.getGroupCode());
        }
        if (StringUtils.hasText(dto.getGroupName())) {
            wrapper.like(AdmissionGroup::getGroupName, dto.getGroupName());
        }
        if (dto.getIsDeleted() != null) {
            wrapper.eq(AdmissionGroup::getIsDeleted, dto.getIsDeleted());
        } else {
            wrapper.eq(AdmissionGroup::getIsDeleted, false);
        }

        // 按大学名模糊查询
        if (StringUtils.hasText(dto.getUniversityName())) {
            LambdaQueryWrapper<University> uniWrapper = new LambdaQueryWrapper<>();
            uniWrapper.like(University::getName, dto.getUniversityName())
                      .ne(University::getStatus, (short) 0)
                      .select(University::getId);
            List<University> universities = universityMapper.selectList(uniWrapper);
            if (universities.isEmpty()) {
                return new Page<>(dto.getPage(), dto.getSize());
            }
            List<Long> universityIds = universities.stream().map(University::getId).toList();
            wrapper.in(AdmissionGroup::getUniversityId, universityIds);
        }

        wrapper.orderByDesc(AdmissionGroup::getYear)
               .orderByDesc(AdmissionGroup::getId);

        IPage<AdmissionGroup> result = admissionGroupMapper.selectPage(page, wrapper);

        return result.convert(entity -> {
            AdmissionGroupListVO vo = new AdmissionGroupListVO();
            BeanUtils.copyProperties(entity, vo);
            // 查询大学名
            University university = universityMapper.selectById(entity.getUniversityId());
            if (university != null) {
                vo.setUniversityName(university.getName());
            }
            return vo;
        });
    }

    @Override
    public AdmissionGroupDetailVO detail(Integer id) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        AdmissionGroupDetailVO vo = new AdmissionGroupDetailVO();
        BeanUtils.copyProperties(entity, vo);

        University university = universityMapper.selectById(entity.getUniversityId());
        if (university != null) {
            vo.setUniversityName(university.getName());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(AdmissionGroupAddDTO dto) {
        // 检查大学是否存在
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "大学不存在");
        }

        // 检查唯一约束
        Integer existingId = admissionGroupMapper.selectIdByBusinessKey(
                dto.getUniversityId(), dto.getYear(), dto.getProvince(),
                dto.getSubjectType(), dto.getBatch(), dto.getGroupCode());
        if (existingId != null) {
            throw new BusinessException(400, "该专业组已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        AdmissionGroup entity = AdmissionGroup.builder()
                .universityId(dto.getUniversityId())
                .year(dto.getYear())
                .province(dto.getProvince())
                .subjectType(dto.getSubjectType())
                .batch(dto.getBatch())
                .enrollmentCode(dto.getEnrollmentCode())
                .groupCode(dto.getGroupCode())
                .groupName(dto.getGroupName())
                .description(dto.getDescription())
                .constraints(dto.getConstraints())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        admissionGroupMapper.insert(entity);
        log.info("新增专业组成功: id={}, universityId={}, groupCode={}",
                entity.getId(), dto.getUniversityId(), dto.getGroupCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, AdmissionGroupAddDTO dto) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 检查唯一约束（排除自己）
        Integer existingId = admissionGroupMapper.selectIdByBusinessKey(
                dto.getUniversityId(), dto.getYear(), dto.getProvince(),
                dto.getSubjectType(), dto.getBatch(), dto.getGroupCode());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该专业组已存在");
        }

        entity.setUniversityId(dto.getUniversityId());
        entity.setYear(dto.getYear());
        entity.setProvince(dto.getProvince());
        entity.setSubjectType(dto.getSubjectType());
        entity.setBatch(dto.getBatch());
        entity.setEnrollmentCode(dto.getEnrollmentCode());
        entity.setGroupCode(dto.getGroupCode());
        entity.setGroupName(dto.getGroupName());
        entity.setDescription(dto.getDescription());
        entity.setConstraints(dto.getConstraints());
        entity.setUpdatedAt(OffsetDateTime.now());

        admissionGroupMapper.updateById(entity);
        log.info("修改专业组成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer id, Boolean isDeleted) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        entity.setIsDeleted(isDeleted);
        entity.setUpdatedAt(OffsetDateTime.now());
        admissionGroupMapper.updateById(entity);

        log.info("修改专业组状态成功: id={}, isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 删除关联的专业明细（CASCADE已设置，但显式删除更清晰）
        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionMajorScore::getGroupId, id);
        admissionMajorScoreMapper.delete(wrapper);

        admissionGroupMapper.deleteById(id);
        log.info("硬删除专业组成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        for (Integer id : ids) {
            LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdmissionMajorScore::getGroupId, id);
            admissionMajorScoreMapper.delete(wrapper);
        }

        admissionGroupMapper.deleteBatchIds(ids);
        log.info("批量硬删除专业组成功: 数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        // 将在Task 9中实现
        throw new BusinessException(500, "导入功能待实现");
    }

    @Override
    public Integer recalcAll() {
        Integer count = admissionGroupMapper.recalcAllGroups();
        log.info("全量重算专业组聚合数据完成: 数量={}", count);
        return count;
    }
}
```

- [ ] **Step 3: 创建AdmissionGroupController**

```java
package com.haifeng.admin.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionGroupService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/admission/group")
@RequiredArgsConstructor
public class AdmissionGroupController {

    private final AdmissionGroupService admissionGroupService;

    @GetMapping("/page")
    public R<IPage<AdmissionGroupListVO>> page(@Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionGroupService.page(dto));
    }

    @GetMapping("/{id}")
    public R<AdmissionGroupDetailVO> detail(@PathVariable Integer id) {
        return R.ok(admissionGroupService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "专业组管理", action = "新增专业组")
    public R<Integer> add(@Valid @RequestBody AdmissionGroupAddDTO dto) {
        return R.ok(admissionGroupService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "专业组管理", action = "修改专业组")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody AdmissionGroupAddDTO dto) {
        admissionGroupService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "专业组管理", action = "修改专业组状态")
    public R<Void> updateStatus(@PathVariable Integer id, @RequestBody StatusDTO dto) {
        admissionGroupService.updateStatus(id, dto.getStatus() == 0);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专业组管理", action = "删除专业组")
    public R<Void> delete(@PathVariable Integer id) {
        admissionGroupService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "专业组管理", action = "批量删除专业组")
    public R<Void> batchDelete(@RequestBody List<Integer> ids) {
        admissionGroupService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "专业组管理", action = "导入专业组数据")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        admissionGroupService.importData(file);
        return R.ok();
    }

    @PostMapping("/recalc-all")
    @OperationLog(module = "专业组管理", action = "全量重算聚合数据")
    public R<Integer> recalcAll() {
        return R.ok(admissionGroupService.recalcAll());
    }
}
```

- [ ] **Step 4: 提交AdmissionGroup基础模块**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/AdmissionGroupService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionGroupServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/AdmissionGroupController.java
git commit -m "feat(admission-group): implement AdmissionGroup CRUD and recalc

- Full CRUD with soft delete toggle
- University name lookup in list/detail
- recalc-all endpoint for manual aggregation"
```

---

## Task 9: 实现Excel导入功能

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/admission/AdmissionImportDTO.java`
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionGroupServiceImpl.java`

- [ ] **Step 1: 创建AdmissionImportDTO**

```java
package com.haifeng.admin.excel.algorithm.admission;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdmissionImportDTO {

    // ==================== 专业组字段 ====================
    @ExcelProperty("大学名")
    private String universityName;

    @ExcelProperty("年份")
    private Short year;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("科类")
    private String subjectType;

    @ExcelProperty("批次")
    private String batch;

    @ExcelProperty("省招代码")
    private String enrollmentCode;

    @ExcelProperty("专业组代码")
    private String groupCode;

    @ExcelProperty("专业组简介")
    private String groupDescription;

    // ==================== 专业明细字段 ====================
    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("选科要求")
    private String subjectRequirements;

    @ExcelProperty("层次")
    private String educationLevel;

    @ExcelProperty("学费")
    private String tuition;

    @ExcelProperty("专业简介")
    private String majorDescription;

    @ExcelProperty("录取人数")
    private Integer admissionCount;

    @ExcelProperty("最低分")
    private Integer minScore;

    @ExcelProperty("中位分")
    private BigDecimal avgScore;

    @ExcelProperty("最高分")
    private Integer maxScore;

    @ExcelProperty("最低位次")
    private Integer minRank;

    @ExcelProperty("中位位次")
    private Integer avgRank;

    @ExcelProperty("最高位次")
    private Integer maxRank;
}
```

- [ ] **Step 2: 更新AdmissionGroupServiceImpl的importData方法**

在AdmissionGroupServiceImpl.java中，替换importData方法：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void importData(MultipartFile file) {
    if (file == null || file.isEmpty()) {
        throw new BusinessException(400, "请上传Excel文件");
    }

    List<AdmissionImportDTO> dataList;
    try {
        dataList = EasyExcel.read(file.getInputStream())
                .head(AdmissionImportDTO.class)
                .sheet()
                .doReadSync();
    } catch (IOException e) {
        log.error("读取Excel文件失败", e);
        throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
    }

    if (dataList == null || dataList.isEmpty()) {
        throw new BusinessException(400, "Excel文件中没有数据");
    }

    // ==================== 第一次遍历：校验 ====================
    List<String> errors = new ArrayList<>();
    Map<String, Long> universityIdCache = new HashMap<>();
    Set<String> validSubjectTypes = Set.of("理科", "物理类", "文科", "历史类", "不分文理");
    Set<String> validBatches = Set.of("本科批", "提前批", "专科批");
    // 用于检查同一专业组内专业代码是否重复
    Map<String, Set<String>> groupMajorCodes = new HashMap<>();

    for (int i = 0; i < dataList.size(); i++) {
        int rowNum = i + 2;
        AdmissionImportDTO dto = dataList.get(i);

        // 必填字段校验
        if (!StringUtils.hasText(dto.getUniversityName())) {
            errors.add("第" + rowNum + "行: 大学名不能为空");
            continue;
        }
        if (dto.getYear() == null) {
            errors.add("第" + rowNum + "行: 年份不能为空");
            continue;
        }
        if (!StringUtils.hasText(dto.getProvince())) {
            errors.add("第" + rowNum + "行: 省份不能为空");
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
        if (!StringUtils.hasText(dto.getGroupCode())) {
            errors.add("第" + rowNum + "行: 专业组代码不能为空");
            continue;
        }
        if (!StringUtils.hasText(dto.getMajorCode())) {
            errors.add("第" + rowNum + "行: 专业代码不能为空");
            continue;
        }
        if (!StringUtils.hasText(dto.getMajorName())) {
            errors.add("第" + rowNum + "行: 专业名称不能为空");
            continue;
        }

        // 枚举值校验
        if (!validSubjectTypes.contains(dto.getSubjectType())) {
            errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
            continue;
        }
        if (!validBatches.contains(dto.getBatch())) {
            errors.add("第" + rowNum + "行: 批次[" + dto.getBatch() + "]不合法，只允许：本科批/提前批/专科批");
            continue;
        }

        // 大学名校验
        String uniName = dto.getUniversityName().trim();
        if (!universityIdCache.containsKey(uniName)) {
            Long universityId = universityMapper.selectIdByName(uniName);
            if (universityId == null) {
                errors.add("第" + rowNum + "行: 大学[" + uniName + "]不存在");
                continue;
            }
            universityIdCache.put(uniName, universityId);
        }

        // 同组专业代码重复检查
        String groupKey = String.format("%s_%d_%s_%s_%s_%s",
                uniName, dto.getYear(), dto.getProvince(),
                dto.getSubjectType(), dto.getBatch(), dto.getGroupCode());
        groupMajorCodes.computeIfAbsent(groupKey, k -> new HashSet<>());
        if (groupMajorCodes.get(groupKey).contains(dto.getMajorCode())) {
            errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在同一专业组内重复");
            continue;
        }
        groupMajorCodes.get(groupKey).add(dto.getMajorCode());
    }

    if (!errors.isEmpty()) {
        throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
    }

    // ==================== 第二次遍历：按专业组分组插入 ====================
    Map<String, List<AdmissionImportDTO>> groupedData = new LinkedHashMap<>();
    for (AdmissionImportDTO dto : dataList) {
        String groupKey = String.format("%s_%d_%s_%s_%s_%s",
                dto.getUniversityName().trim(), dto.getYear(), dto.getProvince(),
                dto.getSubjectType(), dto.getBatch(), dto.getGroupCode());
        groupedData.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(dto);
    }

    OffsetDateTime now = OffsetDateTime.now();
    int groupCount = 0;
    int majorCount = 0;

    for (Map.Entry<String, List<AdmissionImportDTO>> entry : groupedData.entrySet()) {
        List<AdmissionImportDTO> rows = entry.getValue();
        AdmissionImportDTO firstRow = rows.get(0);

        Long universityId = universityIdCache.get(firstRow.getUniversityName().trim());

        // 查询或创建专业组
        Integer groupId = admissionGroupMapper.selectIdByBusinessKey(
                universityId, firstRow.getYear(), firstRow.getProvince(),
                firstRow.getSubjectType(), firstRow.getBatch(), firstRow.getGroupCode());

        if (groupId == null) {
            AdmissionGroup group = AdmissionGroup.builder()
                    .universityId(universityId)
                    .year(firstRow.getYear())
                    .province(firstRow.getProvince())
                    .subjectType(firstRow.getSubjectType())
                    .batch(firstRow.getBatch())
                    .enrollmentCode(firstRow.getEnrollmentCode())
                    .groupCode(firstRow.getGroupCode())
                    .groupName(firstRow.getGroupCode())
                    .description(firstRow.getGroupDescription())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            admissionGroupMapper.insert(group);
            groupId = group.getId();
            groupCount++;
        }

        // 插入专业明细
        for (AdmissionImportDTO row : rows) {
            AdmissionMajorScore majorScore = AdmissionMajorScore.builder()
                    .groupId(groupId)
                    .majorCode(row.getMajorCode())
                    .majorName(row.getMajorName())
                    .subjectRequirements(row.getSubjectRequirements())
                    .educationLevel(row.getEducationLevel())
                    .tuition(row.getTuition())
                    .description(row.getMajorDescription())
                    .admissionCount(row.getAdmissionCount())
                    .minScore(row.getMinScore())
                    .minRank(row.getMinRank())
                    .avgScore(row.getAvgScore())
                    .avgRank(row.getAvgRank())
                    .maxScore(row.getMaxScore())
                    .maxRank(row.getMaxRank())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            admissionMajorScoreMapper.insert(majorScore);
            majorCount++;
        }
    }

    log.info("导入专业组数据成功: 新增专业组={}个, 新增专业明细={}条", groupCount, majorCount);
}
```

- [ ] **Step 3: 添加必要的import**

在AdmissionGroupServiceImpl.java顶部添加：

```java
import com.alibaba.excel.EasyExcel;
import com.haifeng.admin.excel.algorithm.admission.AdmissionImportDTO;
import java.io.IOException;
import java.util.*;
```

- [ ] **Step 4: 提交Excel导入功能**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/admission/AdmissionImportDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/algorithm/admission/AdmissionGroupServiceImpl.java
git commit -m "feat(import): implement Excel import for admission data

- Two-pass validation (validate all then insert)
- Auto-create admission groups if not exists
- Check duplicate major codes within same group
- DB triggers auto-calculate aggregates"
```

---

## Task 10: 最终验证与提交

- [ ] **Step 1: 检查目录结构是否正确**

```bash
ls -la haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/
ls -la haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/
ls -la haifeng-admin/src/main/java/com/haifeng/admin/controller/algorithm/admission/
ls -la haifeng-admin/src/main/java/com/haifeng/admin/service/algorithm/admission/
ls -la haifeng-admin/src/main/java/com/haifeng/admin/dto/algorithm/admission/
ls -la haifeng-admin/src/main/java/com/haifeng/admin/vo/algorithm/admission/
ls -la haifeng-admin/src/main/java/com/haifeng/admin/excel/algorithm/admission/
```

- [ ] **Step 2: 编译检查**

```bash
cd D:/exeProject/ideaProjects/Project-HaiFeng
mvn compile -pl haifeng-common,haifeng-admin -am
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 最终提交**

```bash
git status
git log --oneline -10
```

确认所有文件已提交。

---

## 总结

| Task | 内容 | 文件数 |
|------|------|--------|
| 1 | Flyway V11迁移 | 1 |
| 2 | Entity类 | 3 |
| 3 | Mapper接口 | 3 |
| 4 | DTO类 | 6 |
| 5 | VO类 | 6 |
| 6 | SubjectReqDict模块 | 3 |
| 7 | AdmissionMajorScore模块 | 3 |
| 8 | AdmissionGroup基础模块 | 3 |
| 9 | Excel导入功能 | 2 |
| 10 | 验证 | 0 |

**总计: 30个文件**
