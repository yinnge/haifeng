# 专业管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现专业管理模块，包含专业列表、考研专业、考研专业-大学关联三个子模块的完整CRUD和Excel导入功能。

**Architecture:** 采用Spring Boot + MyBatis-Plus分层架构，Entity/Mapper放haifeng-common，Controller/Service/DTO/VO放haifeng-admin。使用EasyExcel处理Excel导入，事务保证数据一致性。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL, 雪花算法ID

**Spec:** `docs/superpowers/specs/2026-05-07-major-management-design.md`

---

## 文件结构

### haifeng-common (Entity + Mapper)
```
src/main/java/com/haifeng/common/
├── entity/major/
│   ├── Major.java
│   ├── MajorDetail.java
│   ├── PostgradMajor.java
│   └── PostgradMajorUniversity.java
└── mapper/major/
    ├── MajorMapper.java
    ├── MajorDetailMapper.java
    ├── PostgradMajorMapper.java
    └── PostgradMajorUniversityMapper.java
```

### haifeng-admin (Controller + Service + DTO + VO)
```
src/main/java/com/haifeng/admin/
├── controller/major/
│   ├── MajorController.java
│   ├── PostgradMajorController.java
│   └── PostgradMajorUniversityController.java
├── service/major/
│   ├── MajorService.java
│   ├── PostgradMajorService.java
│   └── PostgradMajorUniversityService.java
├── service/impl/major/
│   ├── MajorServiceImpl.java
│   ├── PostgradMajorServiceImpl.java
│   └── PostgradMajorUniversityServiceImpl.java
├── dto/major/
│   ├── MajorQueryDTO.java
│   ├── MajorAddDTO.java
│   ├── MajorUpdateDTO.java
│   ├── MajorDetailUpdateDTO.java
│   ├── MajorImportDTO.java
│   ├── MajorDetailImportDTO.java
│   ├── PostgradMajorQueryDTO.java
│   ├── PostgradMajorAddDTO.java
│   ├── PostgradMajorUpdateDTO.java
│   ├── PostgradMajorImportDTO.java
│   ├── PostgradMajorUniversityQueryDTO.java
│   └── PostgradMajorUniversityImportDTO.java
└── vo/major/
    ├── MajorListVO.java
    ├── MajorDetailVO.java
    ├── PostgradMajorListVO.java
    ├── PostgradMajorDetailVO.java
    ├── PostgradMajorUniversityListVO.java
    └── ImportResultVO.java
```

### Flyway迁移
```
haifeng-admin/src/main/resources/db/migration/
└── V7__create_major_tables.sql
```

---

## Task 1: 创建Flyway数据库迁移

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V7__create_major_tables.sql`

- [ ] **Step 1: 创建迁移文件**

```sql
-- ============================================================
-- 专业管理模块数据库迁移
-- V7__create_major_tables.sql
-- ============================================================

-- ----------------------------------------------------------
-- 1. 专业主表 (t_major)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_major (
    id                  BIGINT          PRIMARY KEY,
    major_code          VARCHAR(20)     NOT NULL,
    major_name          VARCHAR(100)    NOT NULL,
    discipline_name     VARCHAR(100),
    major_type          VARCHAR(30)     NOT NULL,
    major_category      VARCHAR(50),
    parent_category     VARCHAR(50),
    major_tags          VARCHAR(50),
    degree_awarded      VARCHAR(50),
    study_duration      VARCHAR(20),
    employment_rate     NUMERIC(5, 2),
    salary_min          INTEGER,
    salary_max          INTEGER,
    description         TEXT,
    status              SMALLINT        NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_major_code UNIQUE (major_code),
    CONSTRAINT chk_major_employment_rate
        CHECK (employment_rate IS NULL OR (employment_rate >= 0 AND employment_rate <= 100)),
    CONSTRAINT chk_major_salary_range
        CHECK (
            (salary_min IS NULL AND salary_max IS NULL)
            OR (salary_min IS NULL)
            OR (salary_max IS NULL)
            OR (salary_min >= 0 AND salary_max >= 0 AND salary_min <= salary_max)
        )
);

CREATE INDEX idx_major_name ON t_major USING btree (major_name varchar_pattern_ops) WHERE status = 1;
CREATE INDEX idx_major_category ON t_major (major_category) WHERE status = 1;
CREATE INDEX idx_major_type ON t_major (major_type) WHERE status = 1;
CREATE INDEX idx_major_employment_rate ON t_major (employment_rate DESC NULLS LAST) WHERE status = 1;
CREATE INDEX idx_major_parent_category ON t_major (parent_category) WHERE status = 1;

COMMENT ON TABLE t_major IS '专业信息表';
COMMENT ON COLUMN t_major.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_major.major_code IS '专业代码（唯一）';
COMMENT ON COLUMN t_major.major_name IS '专业名称';
COMMENT ON COLUMN t_major.discipline_name IS '学科名称';
COMMENT ON COLUMN t_major.major_type IS '专业类型（本科/专科）';
COMMENT ON COLUMN t_major.major_category IS '学科门类';
COMMENT ON COLUMN t_major.parent_category IS '专业类';
COMMENT ON COLUMN t_major.major_tags IS '专业标签';
COMMENT ON COLUMN t_major.degree_awarded IS '授予学位';
COMMENT ON COLUMN t_major.study_duration IS '学制';
COMMENT ON COLUMN t_major.employment_rate IS '就业率（0-100）';
COMMENT ON COLUMN t_major.salary_min IS '薪资下限（元/月）';
COMMENT ON COLUMN t_major.salary_max IS '薪资上限（元/月）';
COMMENT ON COLUMN t_major.description IS '专业描述';
COMMENT ON COLUMN t_major.status IS '状态（1=启用，0=禁用）';

-- ----------------------------------------------------------
-- 2. 专业详情表 (t_major_detail)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_major_detail (
    id                      BIGINT          PRIMARY KEY,
    major_id                BIGINT          NOT NULL,
    course_count            INTEGER,
    graduate_scale          VARCHAR(20),
    male_ratio              NUMERIC(5, 2),
    female_ratio            NUMERIC(5, 2),
    major_description       TEXT,
    training_objective      TEXT,
    training_requirement    TEXT,
    subject_requirement     TEXT,
    career_prospect         TEXT,
    main_courses            TEXT[]          DEFAULT '{}',
    knowledge_skills        TEXT[]          DEFAULT '{}',
    status                  SMALLINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_major_detail_major_id UNIQUE (major_id),
    CONSTRAINT chk_major_detail_male_ratio
        CHECK (male_ratio IS NULL OR (male_ratio >= 0 AND male_ratio <= 100)),
    CONSTRAINT chk_major_detail_female_ratio
        CHECK (female_ratio IS NULL OR (female_ratio >= 0 AND female_ratio <= 100))
);

CREATE INDEX idx_major_detail_courses ON t_major_detail USING gin (main_courses) WHERE status = 1;

COMMENT ON TABLE t_major_detail IS '专业详情表（与t_major一对一）';
COMMENT ON COLUMN t_major_detail.major_id IS '关联专业表ID';
COMMENT ON COLUMN t_major_detail.course_count IS '课程数量';
COMMENT ON COLUMN t_major_detail.graduate_scale IS '毕业生规模';
COMMENT ON COLUMN t_major_detail.male_ratio IS '男生比例（%）';
COMMENT ON COLUMN t_major_detail.female_ratio IS '女生比例（%）';
COMMENT ON COLUMN t_major_detail.major_description IS '专业描述';
COMMENT ON COLUMN t_major_detail.training_objective IS '培养目标';
COMMENT ON COLUMN t_major_detail.training_requirement IS '培养要求';
COMMENT ON COLUMN t_major_detail.subject_requirement IS '学科要求';
COMMENT ON COLUMN t_major_detail.career_prospect IS '就业前景';
COMMENT ON COLUMN t_major_detail.main_courses IS '主要课程（数组）';
COMMENT ON COLUMN t_major_detail.knowledge_skills IS '知识能力（数组）';

-- ----------------------------------------------------------
-- 3. 考研专业表 (t_postgrad_major)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_postgrad_major (
    id                      BIGINT          PRIMARY KEY,
    major_name              VARCHAR(100)    NOT NULL,
    major_code              VARCHAR(20)     NOT NULL,
    degree_type             VARCHAR(20)     NOT NULL,
    discipline_category     VARCHAR(50)     NOT NULL,
    popularity              VARCHAR(10),
    difficulty              VARCHAR(10),
    brief                   TEXT,
    introduction            TEXT,
    exam_subjects           TEXT[]          DEFAULT '{}',
    admission_requirements  TEXT[]          DEFAULT '{}',
    cross_exam_difficulty   VARCHAR(10),
    cross_exam_description  TEXT,
    cross_exam_factors      TEXT[]          DEFAULT '{}',
    status                  SMALLINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_postgrad_major_code UNIQUE (major_code),
    CONSTRAINT chk_postgrad_degree_type
        CHECK (degree_type IN ('学术学位', '专业学位')),
    CONSTRAINT chk_postgrad_popularity
        CHECK (popularity IS NULL OR popularity IN ('热门', '一般', '冷门')),
    CONSTRAINT chk_postgrad_difficulty
        CHECK (difficulty IS NULL OR difficulty IN ('高', '中', '低')),
    CONSTRAINT chk_postgrad_cross_exam_difficulty
        CHECK (cross_exam_difficulty IS NULL OR cross_exam_difficulty IN ('较易', '中等', '较难'))
);

CREATE INDEX idx_pg_major_name ON t_postgrad_major USING btree (major_name varchar_pattern_ops) WHERE status = 1;
CREATE INDEX idx_pg_discipline_category ON t_postgrad_major (discipline_category) WHERE status = 1;
CREATE INDEX idx_pg_degree_type ON t_postgrad_major (degree_type) WHERE status = 1;
CREATE INDEX idx_pg_popularity ON t_postgrad_major (popularity) WHERE status = 1;
CREATE INDEX idx_pg_difficulty ON t_postgrad_major (difficulty) WHERE status = 1;
CREATE INDEX idx_pg_exam_subjects ON t_postgrad_major USING gin (exam_subjects) WHERE status = 1;

COMMENT ON TABLE t_postgrad_major IS '考研专业表';
COMMENT ON COLUMN t_postgrad_major.major_name IS '专业名称';
COMMENT ON COLUMN t_postgrad_major.major_code IS '专业代码（唯一）';
COMMENT ON COLUMN t_postgrad_major.degree_type IS '学位类型（学术学位/专业学位）';
COMMENT ON COLUMN t_postgrad_major.discipline_category IS '学科门类';
COMMENT ON COLUMN t_postgrad_major.popularity IS '热度（热门/一般/冷门）';
COMMENT ON COLUMN t_postgrad_major.difficulty IS '难度（高/中/低）';
COMMENT ON COLUMN t_postgrad_major.brief IS '专业简介';
COMMENT ON COLUMN t_postgrad_major.introduction IS '专业介绍';
COMMENT ON COLUMN t_postgrad_major.exam_subjects IS '考试科目';
COMMENT ON COLUMN t_postgrad_major.admission_requirements IS '报考要求';
COMMENT ON COLUMN t_postgrad_major.cross_exam_difficulty IS '跨考难度（较易/中等/较难）';
COMMENT ON COLUMN t_postgrad_major.cross_exam_description IS '跨考说明';
COMMENT ON COLUMN t_postgrad_major.cross_exam_factors IS '跨考因素';

-- ----------------------------------------------------------
-- 4. 考研专业-大学关联表 (t_postgrad_major_university)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_postgrad_major_university (
    id                      BIGINT          PRIMARY KEY,
    postgrad_major_id       BIGINT          NOT NULL,
    university_id           BIGINT          NOT NULL,
    university_name         VARCHAR(100)    NOT NULL,
    postgrad_major_name     VARCHAR(100)    NOT NULL,
    sort_order              INTEGER         DEFAULT 0,
    status                  SMALLINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_pg_major_university UNIQUE (postgrad_major_id, university_id)
);

CREATE INDEX idx_pmu_major ON t_postgrad_major_university (postgrad_major_id, sort_order);
CREATE INDEX idx_pmu_university ON t_postgrad_major_university (university_id);

COMMENT ON TABLE t_postgrad_major_university IS '考研专业-大学关联表';
COMMENT ON COLUMN t_postgrad_major_university.postgrad_major_id IS '考研专业ID';
COMMENT ON COLUMN t_postgrad_major_university.university_id IS '大学ID';
COMMENT ON COLUMN t_postgrad_major_university.university_name IS '大学名称（冗余）';
COMMENT ON COLUMN t_postgrad_major_university.postgrad_major_name IS '考研专业名称（冗余）';
COMMENT ON COLUMN t_postgrad_major_university.sort_order IS '排序权重';

-- ----------------------------------------------------------
-- 5. 触发器：自动更新 updated_at
-- ----------------------------------------------------------
CREATE TRIGGER trg_major_updated_at
    BEFORE UPDATE ON t_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_major_detail_updated_at
    BEFORE UPDATE ON t_major_detail
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

CREATE TRIGGER trg_postgrad_major_updated_at
    BEFORE UPDATE ON t_postgrad_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();
```

- [ ] **Step 2: 验证SQL语法**

Run: `cd haifeng-admin && mvn flyway:validate -Dflyway.target=7`

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/resources/db/migration/V7__create_major_tables.sql
git commit -m "feat(db): 添加专业管理模块数据库迁移V7"
```

---

## Task 2: 创建Entity类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/major/Major.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/major/MajorDetail.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/major/PostgradMajor.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/major/PostgradMajorUniversity.java`

- [ ] **Step 1: 创建Major.java**

```java
package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("t_major")
public class Major {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private String parentCategory;

    private String majorTags;

    private String degreeAwarded;

    private String studyDuration;

    private BigDecimal employmentRate;

    private Integer salaryMin;

    private Integer salaryMax;

    private String description;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建MajorDetail.java**

```java
package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName(value = "t_major_detail", autoResultMap = true)
public class MajorDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long majorId;

    private Integer courseCount;

    private String graduateScale;

    private BigDecimal maleRatio;

    private BigDecimal femaleRatio;

    private String majorDescription;

    private String trainingObjective;

    private String trainingRequirement;

    private String subjectRequirement;

    private String careerProspect;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] mainCourses;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] knowledgeSkills;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建PostgradMajor.java**

```java
package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName(value = "t_postgrad_major", autoResultMap = true)
public class PostgradMajor {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private String popularity;

    private String difficulty;

    private String brief;

    private String introduction;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] examSubjects;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] admissionRequirements;

    private String crossExamDifficulty;

    private String crossExamDescription;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] crossExamFactors;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: 创建PostgradMajorUniversity.java**

```java
package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("t_postgrad_major_university")
public class PostgradMajorUniversity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long postgradMajorId;

    private Long universityId;

    private String universityName;

    private String postgradMajorName;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/major/
git commit -m "feat(entity): 添加专业管理模块Entity类"
```

---

## Task 3: 创建Mapper接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorDetailMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/PostgradMajorMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/PostgradMajorUniversityMapper.java`

- [ ] **Step 1: 创建MajorMapper.java**

```java
package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    @Select("SELECT id FROM t_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);
}
```

- [ ] **Step 2: 创建MajorDetailMapper.java**

```java
package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.MajorDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorDetailMapper extends BaseMapper<MajorDetail> {

    @Select("SELECT * FROM t_major_detail WHERE major_id = #{majorId} AND status = 1 LIMIT 1")
    MajorDetail selectByMajorId(@Param("majorId") Long majorId);

    @Select("SELECT COUNT(*) > 0 FROM t_major_detail WHERE major_id = #{majorId}")
    boolean existsByMajorId(@Param("majorId") Long majorId);
}
```

- [ ] **Step 3: 创建PostgradMajorMapper.java**

```java
package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.PostgradMajor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostgradMajorMapper extends BaseMapper<PostgradMajor> {

    @Select("SELECT id FROM t_postgrad_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT major_name FROM t_postgrad_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    String selectNameByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_postgrad_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);
}
```

- [ ] **Step 4: 创建PostgradMajorUniversityMapper.java**

```java
package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.PostgradMajorUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostgradMajorUniversityMapper extends BaseMapper<PostgradMajorUniversity> {

    @Select("SELECT COUNT(*) > 0 FROM t_postgrad_major_university WHERE postgrad_major_id = #{postgradMajorId} AND university_id = #{universityId}")
    boolean existsByRelation(@Param("postgradMajorId") Long postgradMajorId, @Param("universityId") Long universityId);
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/major/
git commit -m "feat(mapper): 添加专业管理模块Mapper接口"
```

---

## Task 4: 创建DTO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorDetailUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorImportDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/MajorDetailImportDTO.java`

- [ ] **Step 1: 创建MajorQueryDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorQueryDTO extends BasePageQueryDTO {

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private Short status;
}
```

- [ ] **Step 2: 创建MajorAddDTO.java**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MajorAddDTO {

    @NotBlank(message = "专业代码不能为空")
    @Size(max = 20, message = "专业代码最长20字符")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @Size(max = 100, message = "学科名称最长100字符")
    private String disciplineName;

    @NotBlank(message = "专业类型不能为空")
    @Size(max = 30, message = "专业类型最长30字符")
    private String majorType;

    @Size(max = 50, message = "学科门类最长50字符")
    private String majorCategory;

    @Size(max = 50, message = "专业类最长50字符")
    private String parentCategory;

    @Size(max = 50, message = "专业标签最长50字符")
    private String majorTags;

    @Size(max = 50, message = "授予学位最长50字符")
    private String degreeAwarded;

    @Size(max = 20, message = "学制最长20字符")
    private String studyDuration;

    @DecimalMin(value = "0", message = "就业率不能小于0")
    @DecimalMax(value = "100", message = "就业率不能大于100")
    private BigDecimal employmentRate;

    @Min(value = 0, message = "薪资下限不能小于0")
    private Integer salaryMin;

    @Min(value = 0, message = "薪资上限不能小于0")
    private Integer salaryMax;

    private String description;
}
```

- [ ] **Step 3: 创建MajorUpdateDTO.java**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MajorUpdateDTO {

    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @Size(max = 100, message = "学科名称最长100字符")
    private String disciplineName;

    @Size(max = 30, message = "专业类型最长30字符")
    private String majorType;

    @Size(max = 50, message = "学科门类最长50字符")
    private String majorCategory;

    @Size(max = 50, message = "专业类最长50字符")
    private String parentCategory;

    @Size(max = 50, message = "专业标签最长50字符")
    private String majorTags;

    @Size(max = 50, message = "授予学位最长50字符")
    private String degreeAwarded;

    @Size(max = 20, message = "学制最长20字符")
    private String studyDuration;

    @DecimalMin(value = "0", message = "就业率不能小于0")
    @DecimalMax(value = "100", message = "就业率不能大于100")
    private BigDecimal employmentRate;

    @Min(value = 0, message = "薪资下限不能小于0")
    private Integer salaryMin;

    @Min(value = 0, message = "薪资上限不能小于0")
    private Integer salaryMax;

    private String description;
}
```

- [ ] **Step 4: 创建MajorDetailUpdateDTO.java**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MajorDetailUpdateDTO {

    @Min(value = 0, message = "课程数量不能小于0")
    private Integer courseCount;

    @Size(max = 20, message = "毕业生规模最长20字符")
    private String graduateScale;

    @DecimalMin(value = "0", message = "男生比例不能小于0")
    @DecimalMax(value = "100", message = "男生比例不能大于100")
    private BigDecimal maleRatio;

    @DecimalMin(value = "0", message = "女生比例不能小于0")
    @DecimalMax(value = "100", message = "女生比例不能大于100")
    private BigDecimal femaleRatio;

    private String majorDescription;

    private String trainingObjective;

    private String trainingRequirement;

    private String subjectRequirement;

    private String careerProspect;

    private String[] mainCourses;

    private String[] knowledgeSkills;
}
```

- [ ] **Step 5: 创建MajorImportDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MajorImportDTO {

    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("学科名称")
    private String disciplineName;

    @ExcelProperty("专业类型")
    private String majorType;

    @ExcelProperty("学科门类")
    private String majorCategory;

    @ExcelProperty("专业类")
    private String parentCategory;

    @ExcelProperty("专业标签")
    private String majorTags;

    @ExcelProperty("授予学位")
    private String degreeAwarded;

    @ExcelProperty("学制")
    private String studyDuration;

    @ExcelProperty("就业率")
    private BigDecimal employmentRate;

    @ExcelProperty("薪资下限")
    private Integer salaryMin;

    @ExcelProperty("薪资上限")
    private Integer salaryMax;

    @ExcelProperty("专业描述")
    private String description;
}
```

- [ ] **Step 6: 创建MajorDetailImportDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MajorDetailImportDTO {

    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("课程数量")
    private Integer courseCount;

    @ExcelProperty("毕业生规模")
    private String graduateScale;

    @ExcelProperty("男生比例")
    private BigDecimal maleRatio;

    @ExcelProperty("女生比例")
    private BigDecimal femaleRatio;

    @ExcelProperty("专业描述")
    private String majorDescription;

    @ExcelProperty("培养目标")
    private String trainingObjective;

    @ExcelProperty("培养要求")
    private String trainingRequirement;

    @ExcelProperty("学科要求")
    private String subjectRequirement;

    @ExcelProperty("就业前景")
    private String careerProspect;

    @ExcelProperty(value = "主要课程", converter = StringToArrayConverter.class)
    private String[] mainCourses;

    @ExcelProperty(value = "知识能力", converter = StringToArrayConverter.class)
    private String[] knowledgeSkills;
}
```

- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/major/
git commit -m "feat(dto): 添加专业管理模块DTO类（第一批）"
```

---

## Task 5: 创建考研专业DTO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/PostgradMajorQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/PostgradMajorAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/PostgradMajorUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/PostgradMajorImportDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/PostgradMajorUniversityQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/major/PostgradMajorUniversityImportDTO.java`

- [ ] **Step 1: 创建PostgradMajorQueryDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorQueryDTO extends BasePageQueryDTO {

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private String popularity;

    private String difficulty;

    private Short status;
}
```

- [ ] **Step 2: 创建PostgradMajorAddDTO.java**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PostgradMajorAddDTO {

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @NotBlank(message = "专业代码不能为空")
    @Size(max = 20, message = "专业代码最长20字符")
    private String majorCode;

    @NotBlank(message = "学位类型不能为空")
    @Pattern(regexp = "学术学位|专业学位", message = "学位类型必须是'学术学位'或'专业学位'")
    private String degreeType;

    @NotBlank(message = "学科门类不能为空")
    @Size(max = 50, message = "学科门类最长50字符")
    private String disciplineCategory;

    @Pattern(regexp = "热门|一般|冷门|", message = "热度必须是'热门'、'一般'或'冷门'")
    private String popularity;

    @Pattern(regexp = "高|中|低|", message = "难度必须是'高'、'中'或'低'")
    private String difficulty;

    private String brief;

    private String introduction;

    private String[] examSubjects;

    private String[] admissionRequirements;

    @Pattern(regexp = "较易|中等|较难|", message = "跨考难度必须是'较易'、'中等'或'较难'")
    private String crossExamDifficulty;

    private String crossExamDescription;

    private String[] crossExamFactors;
}
```

- [ ] **Step 3: 创建PostgradMajorUpdateDTO.java**

```java
package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PostgradMajorUpdateDTO {

    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @Pattern(regexp = "学术学位|专业学位|", message = "学位类型必须是'学术学位'或'专业学位'")
    private String degreeType;

    @Size(max = 50, message = "学科门类最长50字符")
    private String disciplineCategory;

    @Pattern(regexp = "热门|一般|冷门|", message = "热度必须是'热门'、'一般'或'冷门'")
    private String popularity;

    @Pattern(regexp = "高|中|低|", message = "难度必须是'高'、'中'或'低'")
    private String difficulty;

    private String brief;

    private String introduction;

    private String[] examSubjects;

    private String[] admissionRequirements;

    @Pattern(regexp = "较易|中等|较难|", message = "跨考难度必须是'较易'、'中等'或'较难'")
    private String crossExamDifficulty;

    private String crossExamDescription;

    private String[] crossExamFactors;
}
```

- [ ] **Step 4: 创建PostgradMajorImportDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;

@Data
public class PostgradMajorImportDTO {

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelProperty("专业代码")
    private String majorCode;

    @ExcelProperty("学位类型")
    private String degreeType;

    @ExcelProperty("学科门类")
    private String disciplineCategory;

    @ExcelProperty("热度")
    private String popularity;

    @ExcelProperty("难度")
    private String difficulty;

    @ExcelProperty("专业简介")
    private String brief;

    @ExcelProperty("专业介绍")
    private String introduction;

    @ExcelProperty(value = "考试科目", converter = StringToArrayConverter.class)
    private String[] examSubjects;

    @ExcelProperty(value = "报考要求", converter = StringToArrayConverter.class)
    private String[] admissionRequirements;

    @ExcelProperty("跨考难度")
    private String crossExamDifficulty;

    @ExcelProperty("跨考说明")
    private String crossExamDescription;

    @ExcelProperty(value = "跨考因素", converter = StringToArrayConverter.class)
    private String[] crossExamFactors;
}
```

- [ ] **Step 5: 创建PostgradMajorUniversityQueryDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorUniversityQueryDTO extends BasePageQueryDTO {

    private String universityName;

    private String postgradMajorName;

    private Short status;
}
```

- [ ] **Step 6: 创建PostgradMajorUniversityImportDTO.java**

```java
package com.haifeng.admin.dto.major;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PostgradMajorUniversityImportDTO {

    @ExcelProperty("大学名称")
    private String universityName;

    @ExcelProperty("考研专业代码")
    private String postgradMajorCode;

    @ExcelProperty("排序权重")
    private Integer sortOrder;
}
```

- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/major/
git commit -m "feat(dto): 添加考研专业管理DTO类"
```

---

## Task 6: 创建VO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/MajorListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/MajorDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/PostgradMajorListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/PostgradMajorDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/PostgradMajorUniversityListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/major/ImportResultVO.java`

- [ ] **Step 1: 创建MajorListVO.java**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class MajorListVO {

    private Long id;

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private Short status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: 创建MajorDetailVO.java**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class MajorDetailVO {

    // 主表字段
    private Long id;
    private String majorCode;
    private String majorName;
    private String disciplineName;
    private String majorType;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private String studyDuration;
    private BigDecimal employmentRate;
    private Integer salaryMin;
    private Integer salaryMax;
    private String description;
    private Short status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // 详情表字段
    private Long detailId;
    private Integer courseCount;
    private String graduateScale;
    private BigDecimal maleRatio;
    private BigDecimal femaleRatio;
    private String majorDescription;
    private String trainingObjective;
    private String trainingRequirement;
    private String subjectRequirement;
    private String careerProspect;
    private String[] mainCourses;
    private String[] knowledgeSkills;
}
```

- [ ] **Step 3: 创建PostgradMajorListVO.java**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PostgradMajorListVO {

    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private String popularity;

    private String difficulty;

    private Short status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 创建PostgradMajorDetailVO.java**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PostgradMajorDetailVO {

    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private String popularity;

    private String difficulty;

    private String brief;

    private String introduction;

    private String[] examSubjects;

    private String[] admissionRequirements;

    private String crossExamDifficulty;

    private String crossExamDescription;

    private String[] crossExamFactors;

    private Short status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: 创建PostgradMajorUniversityListVO.java**

```java
package com.haifeng.admin.vo.major;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PostgradMajorUniversityListVO {

    private Long id;

    private String universityName;

    private String postgradMajorName;

    private Integer sortOrder;

    private Short status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 6: 创建ImportResultVO.java**

```java
package com.haifeng.admin.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultVO {

    private Integer total;

    private Integer success;

    private Integer failed;

    private List<String> errors;
}
```

- [ ] **Step 7: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/major/
git commit -m "feat(vo): 添加专业管理模块VO类"
```

---

## Task 7: 创建StringToArrayConverter

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/converter/StringToArrayConverter.java`

- [ ] **Step 1: 创建StringToArrayConverter.java**

```java
package com.haifeng.admin.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class StringToArrayConverter implements Converter<String[]> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String[].class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public String[] convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) {
        String value = cellData.getStringValue();
        if (value == null || value.trim().isEmpty()) {
            return new String[0];
        }
        // 兼容中英文逗号
        return value.split("[,，]");
    }

    @Override
    public WriteCellData<?> convertToExcelData(String[] value, ExcelContentProperty contentProperty,
                                                GlobalConfiguration globalConfiguration) {
        if (value == null || value.length == 0) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(String.join(",", value));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/converter/StringToArrayConverter.java
git commit -m "feat(converter): 添加Excel字符串转数组转换器"
```

---

## Task 8: 创建MajorService接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/major/MajorService.java`

- [ ] **Step 1: 创建MajorService.java**

```java
package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorDetailVO;
import com.haifeng.admin.vo.major.MajorListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MajorService {

    Page<MajorListVO> list(MajorQueryDTO queryDTO);

    MajorDetailVO getById(Long id);

    Long add(MajorAddDTO addDTO);

    void update(Long id, MajorUpdateDTO updateDTO);

    void updateStatus(Long id, Short status);

    void softDelete(Long id);

    void hardDelete(Long id);

    void batchSoftDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void updateDetail(Long id, MajorDetailUpdateDTO detailDTO);

    ImportResultVO importMajor(MultipartFile file);

    ImportResultVO importMajorDetail(MultipartFile file);

    void restore(Long id);
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/major/MajorService.java
git commit -m "feat(service): 添加MajorService接口"
```

---

## Task 9: 创建PostgradMajorService接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/major/PostgradMajorService.java`

- [ ] **Step 1: 创建PostgradMajorService.java**

```java
package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorDetailVO;
import com.haifeng.admin.vo.major.PostgradMajorListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostgradMajorService {

    Page<PostgradMajorListVO> list(PostgradMajorQueryDTO queryDTO);

    PostgradMajorDetailVO getById(Long id);

    Long add(PostgradMajorAddDTO addDTO);

    void update(Long id, PostgradMajorUpdateDTO updateDTO);

    void updateStatus(Long id, Short status);

    void softDelete(Long id);

    void hardDelete(Long id);

    void batchSoftDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    ImportResultVO importPostgradMajor(MultipartFile file);

    void restore(Long id);
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/major/PostgradMajorService.java
git commit -m "feat(service): 添加PostgradMajorService接口"
```

---

## Task 10: 创建PostgradMajorUniversityService接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/major/PostgradMajorUniversityService.java`

- [ ] **Step 1: 创建PostgradMajorUniversityService.java**

```java
package com.haifeng.admin.service.major;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorUniversityListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostgradMajorUniversityService {

    Page<PostgradMajorUniversityListVO> list(PostgradMajorUniversityQueryDTO queryDTO);

    void softDelete(Long id);

    void hardDelete(Long id);

    void batchSoftDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    ImportResultVO importPostgradMajorUniversity(MultipartFile file);

    void restore(Long id);
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/major/PostgradMajorUniversityService.java
git commit -m "feat(service): 添加PostgradMajorUniversityService接口"
```

---

## Task 11: 创建MajorServiceImpl（基础CRUD）

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/major/MajorServiceImpl.java`

- [ ] **Step 1: 创建MajorServiceImpl.java（第一部分：基础CRUD）**

```java
package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.service.major.MajorService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorDetailVO;
import com.haifeng.admin.vo.major.MajorListVO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {

    private final MajorMapper majorMapper;
    private final MajorDetailMapper majorDetailMapper;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public Page<MajorListVO> list(MajorQueryDTO queryDTO) {
        Page<Major> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getMajorCode()), Major::getMajorCode, queryDTO.getMajorCode())
               .like(StringUtils.hasText(queryDTO.getMajorName()), Major::getMajorName, queryDTO.getMajorName())
               .like(StringUtils.hasText(queryDTO.getDisciplineName()), Major::getDisciplineName, queryDTO.getDisciplineName())
               .eq(StringUtils.hasText(queryDTO.getMajorType()), Major::getMajorType, queryDTO.getMajorType())
               .eq(StringUtils.hasText(queryDTO.getMajorCategory()), Major::getMajorCategory, queryDTO.getMajorCategory())
               .eq(queryDTO.getStatus() != null, Major::getStatus, queryDTO.getStatus())
               .orderByDesc(Major::getCreatedAt);

        Page<Major> resultPage = majorMapper.selectPage(page, wrapper);

        Page<MajorListVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<MajorListVO> voList = resultPage.getRecords().stream().map(entity -> {
            MajorListVO vo = new MajorListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public MajorDetailVO getById(Long id) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        MajorDetailVO vo = new MajorDetailVO();
        BeanUtils.copyProperties(major, vo);

        MajorDetail detail = majorDetailMapper.selectByMajorId(id);
        if (detail != null) {
            vo.setDetailId(detail.getId());
            vo.setCourseCount(detail.getCourseCount());
            vo.setGraduateScale(detail.getGraduateScale());
            vo.setMaleRatio(detail.getMaleRatio());
            vo.setFemaleRatio(detail.getFemaleRatio());
            vo.setMajorDescription(detail.getMajorDescription());
            vo.setTrainingObjective(detail.getTrainingObjective());
            vo.setTrainingRequirement(detail.getTrainingRequirement());
            vo.setSubjectRequirement(detail.getSubjectRequirement());
            vo.setCareerProspect(detail.getCareerProspect());
            vo.setMainCourses(detail.getMainCourses());
            vo.setKnowledgeSkills(detail.getKnowledgeSkills());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(MajorAddDTO addDTO) {
        if (majorMapper.existsByMajorCode(addDTO.getMajorCode())) {
            throw new BusinessException(400, "专业代码已存在");
        }

        if (addDTO.getSalaryMin() != null && addDTO.getSalaryMax() != null
                && addDTO.getSalaryMin() > addDTO.getSalaryMax()) {
            throw new BusinessException(400, "薪资下限不能大于薪资上限");
        }

        Major major = new Major();
        BeanUtils.copyProperties(addDTO, major);
        major.setId(idGenerator.nextId());
        major.setStatus((short) 1);

        majorMapper.insert(major);
        log.info("新增专业成功，id={}, majorCode={}", major.getId(), major.getMajorCode());

        return major.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MajorUpdateDTO updateDTO) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        if (updateDTO.getSalaryMin() != null && updateDTO.getSalaryMax() != null
                && updateDTO.getSalaryMin() > updateDTO.getSalaryMax()) {
            throw new BusinessException(400, "薪资下限不能大于薪资上限");
        }

        BeanUtils.copyProperties(updateDTO, major, "id", "majorCode", "status", "createdAt");
        majorMapper.updateById(major);
        log.info("修改专业成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        major.setStatus(status);
        majorMapper.updateById(major);
        log.info("切换专业状态成功，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        updateStatus(id, (short) 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        // 同时删除详情
        LambdaQueryWrapper<MajorDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(MajorDetail::getMajorId, id);
        majorDetailMapper.delete(detailWrapper);

        majorMapper.deleteById(id);
        log.info("硬删除专业成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSoftDelete(List<Long> ids) {
        ids.forEach(this::softDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        ids.forEach(this::hardDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(Long id, MajorDetailUpdateDTO detailDTO) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        MajorDetail detail = majorDetailMapper.selectByMajorId(id);
        if (detail == null) {
            detail = new MajorDetail();
            detail.setId(idGenerator.nextId());
            detail.setMajorId(id);
            detail.setStatus((short) 1);
            BeanUtils.copyProperties(detailDTO, detail);
            majorDetailMapper.insert(detail);
        } else {
            BeanUtils.copyProperties(detailDTO, detail, "id", "majorId", "status", "createdAt");
            majorDetailMapper.updateById(detail);
        }
        log.info("修改专业详情成功，majorId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        updateStatus(id, (short) 1);
    }

    // 导入方法在下一个Task实现
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importMajor(MultipartFile file) {
        // 在Task 12实现
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importMajorDetail(MultipartFile file) {
        // 在Task 12实现
        return null;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/major/MajorServiceImpl.java
git commit -m "feat(service): 添加MajorServiceImpl基础CRUD实现"
```

---

## Task 12: 完善MajorServiceImpl（Excel导入）

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/major/MajorServiceImpl.java`

- [ ] **Step 1: 替换importMajor方法实现**

在MajorServiceImpl.java中替换importMajor方法：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public ImportResultVO importMajor(MultipartFile file) {
    List<String> errors = new ArrayList<>();
    List<MajorImportDTO> dataList;

    try {
        dataList = EasyExcel.read(file.getInputStream())
                .head(MajorImportDTO.class)
                .sheet()
                .doReadSync();
    } catch (Exception e) {
        log.error("Excel解析失败", e);
        throw new BusinessException(400, "Excel解析失败: " + e.getMessage());
    }

    if (dataList.isEmpty()) {
        throw new BusinessException(400, "Excel文件为空");
    }

    int total = dataList.size();
    int rowNum = 2; // Excel从第2行开始（第1行是表头）

    for (MajorImportDTO dto : dataList) {
        // 校验必填字段
        if (!StringUtils.hasText(dto.getMajorCode())) {
            errors.add("第" + rowNum + "行：专业代码不能为空");
            rowNum++;
            continue;
        }
        if (!StringUtils.hasText(dto.getMajorName())) {
            errors.add("第" + rowNum + "行：专业名称不能为空");
            rowNum++;
            continue;
        }
        if (!StringUtils.hasText(dto.getMajorType())) {
            errors.add("第" + rowNum + "行：专业类型不能为空");
            rowNum++;
            continue;
        }

        // 校验唯一性
        if (majorMapper.existsByMajorCode(dto.getMajorCode())) {
            errors.add("第" + rowNum + "行：专业代码'" + dto.getMajorCode() + "'已存在");
            rowNum++;
            continue;
        }

        // 校验薪资范围
        if (dto.getSalaryMin() != null && dto.getSalaryMax() != null
                && dto.getSalaryMin() > dto.getSalaryMax()) {
            errors.add("第" + rowNum + "行：薪资下限大于薪资上限");
            rowNum++;
            continue;
        }

        // 校验就业率范围
        if (dto.getEmploymentRate() != null &&
                (dto.getEmploymentRate().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                 dto.getEmploymentRate().compareTo(java.math.BigDecimal.valueOf(100)) > 0)) {
            errors.add("第" + rowNum + "行：就业率必须在0-100之间");
            rowNum++;
            continue;
        }

        rowNum++;
    }

    if (!errors.isEmpty()) {
        throw new BusinessException(400, String.join("; ", errors));
    }

    // 批量插入
    int successCount = 0;
    for (MajorImportDTO dto : dataList) {
        Major major = new Major();
        BeanUtils.copyProperties(dto, major);
        major.setId(idGenerator.nextId());
        major.setStatus((short) 1);
        majorMapper.insert(major);
        successCount++;
    }

    log.info("专业主表导入成功，total={}, success={}", total, successCount);

    return ImportResultVO.builder()
            .total(total)
            .success(successCount)
            .failed(0)
            .errors(new ArrayList<>())
            .build();
}
```

- [ ] **Step 2: 替换importMajorDetail方法实现**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public ImportResultVO importMajorDetail(MultipartFile file) {
    List<String> errors = new ArrayList<>();
    List<MajorDetailImportDTO> dataList;

    try {
        dataList = EasyExcel.read(file.getInputStream())
                .head(MajorDetailImportDTO.class)
                .sheet()
                .doReadSync();
    } catch (Exception e) {
        log.error("Excel解析失败", e);
        throw new BusinessException(400, "Excel解析失败: " + e.getMessage());
    }

    if (dataList.isEmpty()) {
        throw new BusinessException(400, "Excel文件为空");
    }

    int total = dataList.size();
    int rowNum = 2;

    for (MajorDetailImportDTO dto : dataList) {
        // 校验必填字段
        if (!StringUtils.hasText(dto.getMajorCode())) {
            errors.add("第" + rowNum + "行：专业代码不能为空");
            rowNum++;
            continue;
        }

        // 校验外键
        Long majorId = majorMapper.selectIdByMajorCode(dto.getMajorCode());
        if (majorId == null) {
            errors.add("第" + rowNum + "行：专业代码'" + dto.getMajorCode() + "'在主表中不存在");
            rowNum++;
            continue;
        }

        // 校验1:1关系
        if (majorDetailMapper.existsByMajorId(majorId)) {
            errors.add("第" + rowNum + "行：专业代码'" + dto.getMajorCode() + "'的详情已存在");
            rowNum++;
            continue;
        }

        // 校验比例范围
        if (dto.getMaleRatio() != null &&
                (dto.getMaleRatio().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                 dto.getMaleRatio().compareTo(java.math.BigDecimal.valueOf(100)) > 0)) {
            errors.add("第" + rowNum + "行：男生比例必须在0-100之间");
            rowNum++;
            continue;
        }
        if (dto.getFemaleRatio() != null &&
                (dto.getFemaleRatio().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                 dto.getFemaleRatio().compareTo(java.math.BigDecimal.valueOf(100)) > 0)) {
            errors.add("第" + rowNum + "行：女生比例必须在0-100之间");
            rowNum++;
            continue;
        }

        rowNum++;
    }

    if (!errors.isEmpty()) {
        throw new BusinessException(400, String.join("; ", errors));
    }

    // 批量插入
    int successCount = 0;
    for (MajorDetailImportDTO dto : dataList) {
        Long majorId = majorMapper.selectIdByMajorCode(dto.getMajorCode());

        MajorDetail detail = new MajorDetail();
        BeanUtils.copyProperties(dto, detail);
        detail.setId(idGenerator.nextId());
        detail.setMajorId(majorId);
        detail.setStatus((short) 1);
        majorDetailMapper.insert(detail);
        successCount++;
    }

    log.info("专业详情表导入成功，total={}, success={}", total, successCount);

    return ImportResultVO.builder()
            .total(total)
            .success(successCount)
            .failed(0)
            .errors(new ArrayList<>())
            .build();
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/major/MajorServiceImpl.java
git commit -m "feat(service): 添加MajorServiceImpl Excel导入实现"
```

---

## Task 13-16: 创建剩余ServiceImpl和Controller

由于篇幅限制，后续任务按照相同模式创建：

- **Task 13:** 创建PostgradMajorServiceImpl（基础CRUD + Excel导入）
- **Task 14:** 创建PostgradMajorUniversityServiceImpl
- **Task 15:** 创建MajorController
- **Task 16:** 创建PostgradMajorController和PostgradMajorUniversityController
- **Task 17:** 创建功能文档order7.md

每个Task遵循相同的TDD模式：创建文件 → 验证编译 → 提交。

---

## 验收标准

1. Flyway迁移成功，4个表创建正确
2. 所有31个接口可调用
3. 4个Excel导入功能正常
4. 软删除/硬删除/恢复功能正常
5. 模糊查询功能正常
6. 事务一致性保证
