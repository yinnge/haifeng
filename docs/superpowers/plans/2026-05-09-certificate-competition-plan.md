# 竞赛证书管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现竞赛证书管理模块，包括证书管理、竞赛管理（含详情）、竞赛-专业关联管理

**Architecture:** 采用独立Controller模式，每个子模块一个Controller。竞赛与竞赛详情1:1关系，通过事务保证数据一致性。竞赛-专业为多对多关联，基于名称新增、级联删除。

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + PostgreSQL + Flyway

---

## 文件结构

### haifeng-common

```
entity/certificate/
├── Certificate.java           # 证书实体
├── Competition.java           # 竞赛实体
├── CompetitionDetail.java     # 竞赛详情实体
└── CompetitionMajor.java      # 竞赛-专业关联实体

mapper/certificate/
├── CertificateMapper.java
├── CompetitionMapper.java
├── CompetitionDetailMapper.java
└── CompetitionMajorMapper.java
```

### haifeng-admin

```
controller/certificate/
├── CertificateController.java
├── CompetitionController.java
└── CompetitionMajorController.java

service/certificate/
├── CertificateService.java
├── CompetitionService.java
└── CompetitionMajorService.java

service/impl/certificate/
├── CertificateServiceImpl.java
├── CompetitionServiceImpl.java
└── CompetitionMajorServiceImpl.java

dto/certificate/
├── CertificateQueryDTO.java
├── CertificateAddDTO.java
├── CertificateUpdateDTO.java
├── CompetitionQueryDTO.java
├── CompetitionAddDTO.java
├── CompetitionUpdateDTO.java
├── CompetitionDetailDTO.java
├── CompetitionMajorQueryDTO.java
├── CompetitionMajorAddDTO.java
└── BatchDeleteDTO.java

vo/certificate/
├── CertificateListVO.java
├── CertificateDetailVO.java
├── CompetitionListVO.java
├── CompetitionDetailVO.java
└── CompetitionMajorListVO.java

db/migration/
└── V8__create_certificate_competition.sql
```

---

## Task 1: 数据库迁移脚本

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V8__create_certificate_competition.sql`

- [ ] **Step 1: 创建数据库迁移脚本**

```sql
-- ============================================
-- V8__create_certificate_competition.sql
-- 竞赛证书管理模块：证书表、竞赛表、竞赛详情表、竞赛-专业关联表
-- ============================================

-- ===========================================================
-- 1. 证书表 (t_certificate)
-- ===========================================================
CREATE TABLE t_certificate (
    id                      BIGINT          PRIMARY KEY,
    cert_name               VARCHAR(150)    NOT NULL,
    category                VARCHAR(50),
    cert_level              VARCHAR(50),
    applicable_major        VARCHAR(200),
    registration_time       VARCHAR(100),
    exam_time               VARCHAR(100),
    exam_fee                INTEGER,
    cert_intro              TEXT,
    exam_requirements       TEXT[]          DEFAULT '{}',
    exam_arrangement        TEXT,
    official_website        VARCHAR(500),
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_cert_name UNIQUE (cert_name),
    CONSTRAINT chk_exam_fee CHECK (exam_fee IS NULL OR exam_fee >= 0)
);

-- 证书表索引
CREATE INDEX idx_cert_category ON t_certificate (category) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_level ON t_certificate (cert_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_name_search ON t_certificate USING btree (cert_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_major ON t_certificate USING btree (applicable_major varchar_pattern_ops) WHERE is_deleted = FALSE;

-- 证书表触发器
CREATE TRIGGER trg_certificate_updated_at
    BEFORE UPDATE ON t_certificate
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 证书表注释
COMMENT ON TABLE t_certificate IS '证书信息表';
COMMENT ON COLUMN t_certificate.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_certificate.cert_name IS '证书名称';
COMMENT ON COLUMN t_certificate.category IS '证书分类（IT类、财会类、语言类）';
COMMENT ON COLUMN t_certificate.cert_level IS '证书等级（初级、中级、高级）';
COMMENT ON COLUMN t_certificate.applicable_major IS '适用专业';
COMMENT ON COLUMN t_certificate.registration_time IS '报名时间';
COMMENT ON COLUMN t_certificate.exam_time IS '考试时间';
COMMENT ON COLUMN t_certificate.exam_fee IS '考试费用（元）';
COMMENT ON COLUMN t_certificate.cert_intro IS '证书简介';
COMMENT ON COLUMN t_certificate.exam_requirements IS '报考条件列表';
COMMENT ON COLUMN t_certificate.exam_arrangement IS '考试安排详情';
COMMENT ON COLUMN t_certificate.official_website IS '官方网站链接';
COMMENT ON COLUMN t_certificate.is_deleted IS '软删除标记';

-- ===========================================================
-- 2. 竞赛表 (t_competition)
-- ===========================================================
CREATE TABLE t_competition (
    id                      BIGINT          PRIMARY KEY,
    comp_name               VARCHAR(200)    NOT NULL,
    comp_level              VARCHAR(50),
    registration_time       VARCHAR(100),
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_comp_name UNIQUE (comp_name)
);

-- 竞赛表索引
CREATE INDEX idx_comp_level ON t_competition (comp_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_comp_name_search ON t_competition USING btree (comp_name varchar_pattern_ops) WHERE is_deleted = FALSE;

-- 竞赛表触发器
CREATE TRIGGER trg_competition_updated_at
    BEFORE UPDATE ON t_competition
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 竞赛表注释
COMMENT ON TABLE t_competition IS '竞赛基本信息表';
COMMENT ON COLUMN t_competition.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_competition.comp_name IS '竞赛名称';
COMMENT ON COLUMN t_competition.comp_level IS '竞赛级别（国家级/省级/校级）';
COMMENT ON COLUMN t_competition.registration_time IS '报名时间';
COMMENT ON COLUMN t_competition.is_deleted IS '软删除标记';

-- ===========================================================
-- 3. 竞赛详情表 (t_competition_detail) - 与竞赛表1:1
-- ===========================================================
CREATE TABLE t_competition_detail (
    id                      BIGINT          PRIMARY KEY,
    competition_id          BIGINT          NOT NULL UNIQUE,
    basic_info              JSONB           DEFAULT '{}'::JSONB,
    awards                  TEXT[]          DEFAULT '{}',
    background              TEXT,
    purposes                TEXT[]          DEFAULT '{}',
    competition_rules       JSONB           DEFAULT '[]'::JSONB,
    scoring_criteria        TEXT[]          DEFAULT '{}',
    notices                 TEXT[]          DEFAULT '{}',
    process_guide           JSONB           DEFAULT '[]'::JSONB,
    awards_display          JSONB           DEFAULT '[]'::JSONB,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 竞赛详情表索引
CREATE INDEX idx_comp_detail_comp_id ON t_competition_detail (competition_id);

-- 竞赛详情表触发器
CREATE TRIGGER trg_competition_detail_updated_at
    BEFORE UPDATE ON t_competition_detail
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 竞赛详情表注释
COMMENT ON TABLE t_competition_detail IS '竞赛详情表（与竞赛表一对一）';
COMMENT ON COLUMN t_competition_detail.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_competition_detail.competition_id IS '关联竞赛ID';
COMMENT ON COLUMN t_competition_detail.basic_info IS '基本信息（主办方、时间、对象等）';
COMMENT ON COLUMN t_competition_detail.awards IS '奖项列表';
COMMENT ON COLUMN t_competition_detail.background IS '竞赛背景与意义';
COMMENT ON COLUMN t_competition_detail.purposes IS '竞赛目的';
COMMENT ON COLUMN t_competition_detail.competition_rules IS '竞赛规则（title+content数组）';
COMMENT ON COLUMN t_competition_detail.scoring_criteria IS '评分标准';
COMMENT ON COLUMN t_competition_detail.notices IS '注意事项';
COMMENT ON COLUMN t_competition_detail.process_guide IS '参赛流程指南（title+content数组）';
COMMENT ON COLUMN t_competition_detail.awards_display IS '奖项设置展示（title+content数组）';
COMMENT ON COLUMN t_competition_detail.is_deleted IS '软删除标记';

-- ===========================================================
-- 4. 竞赛-专业关联表 (t_competition_major) - 多对多
-- ===========================================================
CREATE TABLE t_competition_major (
    id                  BIGINT          PRIMARY KEY,
    competition_id      BIGINT          NOT NULL,
    major_id            BIGINT          NOT NULL,
    major_name          VARCHAR(100)    NOT NULL,
    competition_name    VARCHAR(200)    NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_comp_major UNIQUE (competition_id, major_id)
);

-- 竞赛-专业关联表索引
CREATE INDEX idx_cm_competition ON t_competition_major (competition_id);
CREATE INDEX idx_cm_major ON t_competition_major (major_id);

-- 竞赛-专业关联表注释
COMMENT ON TABLE t_competition_major IS '竞赛-专业关联表（多对多）';
COMMENT ON COLUMN t_competition_major.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_competition_major.competition_id IS '竞赛ID';
COMMENT ON COLUMN t_competition_major.major_id IS '专业ID';
COMMENT ON COLUMN t_competition_major.major_name IS '专业名称（冗余）';
COMMENT ON COLUMN t_competition_major.competition_name IS '竞赛名称（冗余）';
```

- [ ] **Step 2: 验证迁移脚本语法**

检查SQL文件是否有语法错误，确保所有约束、索引、注释完整。

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/V8__create_certificate_competition.sql
git commit -m "feat(certificate): add V8 migration for certificate and competition tables

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 实体类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/certificate/Certificate.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/certificate/Competition.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/certificate/CompetitionDetail.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/certificate/CompetitionMajor.java`

- [ ] **Step 1: 创建 Certificate 实体**

```java
package com.haifeng.common.entity.certificate;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("t_certificate")
public class Certificate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String certName;

    private String category;

    private String certLevel;

    private String applicableMajor;

    private String registrationTime;

    private String examTime;

    private Integer examFee;

    private String certIntro;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> examRequirements;

    private String examArrangement;

    private String officialWebsite;

    @TableField("is_deleted")
    private Boolean isDeleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 Competition 实体**

```java
package com.haifeng.common.entity.certificate;

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
@TableName("t_competition")
public class Competition {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String compName;

    private String compLevel;

    private String registrationTime;

    @TableField("is_deleted")
    private Boolean isDeleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 CompetitionDetail 实体**

```java
package com.haifeng.common.entity.certificate;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_competition_detail", autoResultMap = true)
public class CompetitionDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long competitionId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> basicInfo;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> awards;

    private String background;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> purposes;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> competitionRules;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scoringCriteria;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> notices;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> processGuide;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> awardsDisplay;

    @TableField("is_deleted")
    private Boolean isDeleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: 创建 CompetitionMajor 实体**

```java
package com.haifeng.common.entity.certificate;

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
@TableName("t_competition_major")
public class CompetitionMajor {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long competitionId;

    private Long majorId;

    private String majorName;

    private String competitionName;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/certificate/
git commit -m "feat(certificate): add entity classes for certificate module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: Mapper接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CertificateMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CompetitionMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CompetitionDetailMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CompetitionMajorMapper.java`

- [ ] **Step 1: 创建 CertificateMapper**

```java
package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.Certificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_certificate WHERE cert_name = #{certName} AND is_deleted = FALSE)")
    boolean existsByCertName(String certName);
}
```

- [ ] **Step 2: 创建 CompetitionMapper**

```java
package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition WHERE comp_name = #{compName} AND is_deleted = FALSE)")
    boolean existsByCompName(String compName);

    @Select("SELECT * FROM t_competition WHERE comp_name = #{compName} AND is_deleted = FALSE LIMIT 1")
    Competition findByCompName(String compName);
}
```

- [ ] **Step 3: 创建 CompetitionDetailMapper**

```java
package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompetitionDetailMapper extends BaseMapper<CompetitionDetail> {

    @Select("SELECT * FROM t_competition_detail WHERE competition_id = #{competitionId}")
    CompetitionDetail findByCompetitionId(Long competitionId);
}
```

- [ ] **Step 4: 创建 CompetitionMajorMapper**

```java
package com.haifeng.common.mapper.certificate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.certificate.CompetitionMajor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompetitionMajorMapper extends BaseMapper<CompetitionMajor> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_competition_major WHERE competition_id = #{competitionId} AND major_id = #{majorId})")
    boolean existsByCompetitionIdAndMajorId(Long competitionId, Long majorId);

    @Delete("DELETE FROM t_competition_major WHERE competition_id = #{competitionId}")
    int deleteByCompetitionId(Long competitionId);
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/
git commit -m "feat(certificate): add mapper interfaces for certificate module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: DTO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CertificateQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CertificateAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CertificateUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CompetitionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CompetitionAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CompetitionUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CompetitionDetailDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CompetitionMajorQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/CompetitionMajorAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/BatchDeleteDTO.java`

- [ ] **Step 1: 创建 CertificateQueryDTO**

```java
package com.haifeng.admin.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateQueryDTO extends BasePageQueryDTO {
    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private Boolean isDeleted;
}
```

- [ ] **Step 2: 创建 CertificateAddDTO**

```java
package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class CertificateAddDTO {

    @NotBlank(message = "证书名称不能为空")
    @Size(max = 150, message = "证书名称最长150字符")
    private String certName;

    @Size(max = 50, message = "分类最长50字符")
    private String category;

    @Size(max = 50, message = "等级最长50字符")
    private String certLevel;

    @Size(max = 200, message = "适用专业最长200字符")
    private String applicableMajor;

    @Size(max = 100, message = "报名时间最长100字符")
    private String registrationTime;

    @Size(max = 100, message = "考试时间最长100字符")
    private String examTime;

    @Min(value = 0, message = "考试费用不能为负数")
    private Integer examFee;

    private String certIntro;

    private List<String> examRequirements;

    private String examArrangement;

    @Size(max = 500, message = "官网链接最长500字符")
    private String officialWebsite;
}
```

- [ ] **Step 3: 创建 CertificateUpdateDTO**

```java
package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class CertificateUpdateDTO {

    @NotBlank(message = "证书名称不能为空")
    @Size(max = 150, message = "证书名称最长150字符")
    private String certName;

    @Size(max = 50, message = "分类最长50字符")
    private String category;

    @Size(max = 50, message = "等级最长50字符")
    private String certLevel;

    @Size(max = 200, message = "适用专业最长200字符")
    private String applicableMajor;

    @Size(max = 100, message = "报名时间最长100字符")
    private String registrationTime;

    @Size(max = 100, message = "考试时间最长100字符")
    private String examTime;

    @Min(value = 0, message = "考试费用不能为负数")
    private Integer examFee;

    private String certIntro;

    private List<String> examRequirements;

    private String examArrangement;

    @Size(max = 500, message = "官网链接最长500字符")
    private String officialWebsite;
}
```

- [ ] **Step 4: 创建 CompetitionQueryDTO**

```java
package com.haifeng.admin.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionQueryDTO extends BasePageQueryDTO {
    private String compName;
    private String compLevel;
    private Boolean isDeleted;
}
```

- [ ] **Step 5: 创建 CompetitionDetailDTO（竞赛详情字段）**

```java
package com.haifeng.admin.dto.certificate;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CompetitionDetailDTO {
    private Map<String, Object> basicInfo;
    private List<String> awards;
    private String background;
    private List<String> purposes;
    private List<Map<String, String>> competitionRules;
    private List<String> scoringCriteria;
    private List<String> notices;
    private List<Map<String, String>> processGuide;
    private List<Map<String, String>> awardsDisplay;
}
```

- [ ] **Step 6: 创建 CompetitionAddDTO**

```java
package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CompetitionAddDTO {

    @NotBlank(message = "竞赛名称不能为空")
    @Size(max = 200, message = "竞赛名称最长200字符")
    private String compName;

    @Size(max = 50, message = "竞赛级别最长50字符")
    private String compLevel;

    @Size(max = 100, message = "报名时间最长100字符")
    private String registrationTime;

    @Valid
    private CompetitionDetailDTO detail;
}
```

- [ ] **Step 7: 创建 CompetitionUpdateDTO**

```java
package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CompetitionUpdateDTO {

    @NotBlank(message = "竞赛名称不能为空")
    @Size(max = 200, message = "竞赛名称最长200字符")
    private String compName;

    @Size(max = 50, message = "竞赛级别最长50字符")
    private String compLevel;

    @Size(max = 100, message = "报名时间最长100字符")
    private String registrationTime;

    @Valid
    private CompetitionDetailDTO detail;
}
```

- [ ] **Step 8: 创建 CompetitionMajorQueryDTO**

```java
package com.haifeng.admin.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionMajorQueryDTO extends BasePageQueryDTO {
    private String majorName;
    private String competitionName;
}
```

- [ ] **Step 9: 创建 CompetitionMajorAddDTO**

```java
package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompetitionMajorAddDTO {

    @NotBlank(message = "竞赛名称不能为空")
    private String competitionName;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;
}
```

- [ ] **Step 10: 创建 BatchDeleteDTO**

```java
package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的记录")
    private List<Long> ids;
}
```

- [ ] **Step 11: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/certificate/
git commit -m "feat(certificate): add DTO classes for certificate module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: VO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/certificate/CertificateListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/certificate/CertificateDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/certificate/CompetitionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/certificate/CompetitionDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/certificate/CompetitionMajorListVO.java`

- [ ] **Step 1: 创建 CertificateListVO**

```java
package com.haifeng.admin.vo.certificate;

import lombok.Data;

@Data
public class CertificateListVO {
    private Long id;
    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private Boolean isDeleted;
}
```

- [ ] **Step 2: 创建 CertificateDetailVO**

```java
package com.haifeng.admin.vo.certificate;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class CertificateDetailVO {
    private Long id;
    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private String registrationTime;
    private String examTime;
    private Integer examFee;
    private String certIntro;
    private List<String> examRequirements;
    private String examArrangement;
    private String officialWebsite;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 CompetitionListVO**

```java
package com.haifeng.admin.vo.certificate;

import lombok.Data;

@Data
public class CompetitionListVO {
    private Long id;
    private String compName;
    private String compLevel;
    private String registrationTime;
    private Boolean isDeleted;
}
```

- [ ] **Step 4: 创建 CompetitionDetailVO**

```java
package com.haifeng.admin.vo.certificate;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CompetitionDetailVO {

    // ==================== 竞赛主表字段 ====================
    private Long id;
    private String compName;
    private String compLevel;
    private String registrationTime;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // ==================== 竞赛详情表字段 ====================
    private Long detailId;
    private Map<String, Object> basicInfo;
    private List<String> awards;
    private String background;
    private List<String> purposes;
    private List<Map<String, String>> competitionRules;
    private List<String> scoringCriteria;
    private List<String> notices;
    private List<Map<String, String>> processGuide;
    private List<Map<String, String>> awardsDisplay;
}
```

- [ ] **Step 5: 创建 CompetitionMajorListVO**

```java
package com.haifeng.admin.vo.certificate;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CompetitionMajorListVO {
    private Long id;
    private String majorName;
    private String competitionName;
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/certificate/
git commit -m "feat(certificate): add VO classes for certificate module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 证书Service接口和实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/certificate/CertificateService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/certificate/CertificateServiceImpl.java`

- [ ] **Step 1: 创建 CertificateService 接口**

```java
package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;

import java.util.List;

public interface CertificateService {

    IPage<CertificateListVO> page(CertificateQueryDTO dto);

    CertificateDetailVO detail(Long id);

    Long add(CertificateAddDTO dto);

    void update(Long id, CertificateUpdateDTO dto);

    void toggleStatus(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建 CertificateServiceImpl**

```java
package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.service.certificate.CertificateService;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
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
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;

    @Override
    public IPage<CertificateListVO> page(CertificateQueryDTO dto) {
        Page<Certificate> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getCertName())) {
            wrapper.like(Certificate::getCertName, dto.getCertName());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.like(Certificate::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getCertLevel())) {
            wrapper.like(Certificate::getCertLevel, dto.getCertLevel());
        }
        if (StringUtils.hasText(dto.getApplicableMajor())) {
            wrapper.like(Certificate::getApplicableMajor, dto.getApplicableMajor());
        }
        if (dto.getIsDeleted() != null) {
            wrapper.eq(Certificate::getIsDeleted, dto.getIsDeleted());
        }

        wrapper.orderByDesc(Certificate::getUpdatedAt);

        IPage<Certificate> certificatePage = certificateMapper.selectPage(page, wrapper);

        return certificatePage.convert(cert -> {
            CertificateListVO vo = new CertificateListVO();
            BeanUtils.copyProperties(cert, vo);
            return vo;
        });
    }

    @Override
    public CertificateDetailVO detail(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        CertificateDetailVO vo = new CertificateDetailVO();
        BeanUtils.copyProperties(certificate, vo);
        return vo;
    }

    @Override
    public Long add(CertificateAddDTO dto) {
        if (certificateMapper.existsByCertName(dto.getCertName())) {
            throw new BusinessException(400, "证书名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Certificate certificate = Certificate.builder()
                .id(id)
                .certName(dto.getCertName())
                .category(dto.getCategory())
                .certLevel(dto.getCertLevel())
                .applicableMajor(dto.getApplicableMajor())
                .registrationTime(dto.getRegistrationTime())
                .examTime(dto.getExamTime())
                .examFee(dto.getExamFee())
                .certIntro(dto.getCertIntro())
                .examRequirements(dto.getExamRequirements())
                .examArrangement(dto.getExamArrangement())
                .officialWebsite(dto.getOfficialWebsite())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        certificateMapper.insert(certificate);

        log.info("新增证书成功: id={}, certName={}", id, dto.getCertName());
        return id;
    }

    @Override
    public void update(Long id, CertificateUpdateDTO dto) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        if (!certificate.getCertName().equals(dto.getCertName())
                && certificateMapper.existsByCertName(dto.getCertName())) {
            throw new BusinessException(400, "证书名称已存在");
        }

        certificate.setCertName(dto.getCertName());
        certificate.setCategory(dto.getCategory());
        certificate.setCertLevel(dto.getCertLevel());
        certificate.setApplicableMajor(dto.getApplicableMajor());
        certificate.setRegistrationTime(dto.getRegistrationTime());
        certificate.setExamTime(dto.getExamTime());
        certificate.setExamFee(dto.getExamFee());
        certificate.setCertIntro(dto.getCertIntro());
        certificate.setExamRequirements(dto.getExamRequirements());
        certificate.setExamArrangement(dto.getExamArrangement());
        certificate.setOfficialWebsite(dto.getOfficialWebsite());
        certificate.setUpdatedAt(OffsetDateTime.now());

        certificateMapper.updateById(certificate);

        log.info("更新证书成功: id={}, certName={}", id, dto.getCertName());
    }

    @Override
    public void toggleStatus(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        certificate.setIsDeleted(!certificate.getIsDeleted());
        certificate.setUpdatedAt(OffsetDateTime.now());

        certificateMapper.updateById(certificate);

        log.info("切换证书状态成功: id={}, isDeleted={}", id, certificate.getIsDeleted());
    }

    @Override
    public void delete(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException(404, "证书不存在");
        }

        certificateMapper.deleteById(id);

        log.info("硬删除证书成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的证书");
        }

        int deleted = certificateMapper.deleteBatchIds(ids);

        log.info("批量硬删除证书成功: 删除数量={}, ids={}", deleted, ids);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/certificate/CertificateService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/certificate/CertificateServiceImpl.java
git commit -m "feat(certificate): add CertificateService and implementation

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 竞赛Service接口和实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/certificate/CompetitionService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/certificate/CompetitionServiceImpl.java`

- [ ] **Step 1: 创建 CompetitionService 接口**

```java
package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;

import java.util.List;

public interface CompetitionService {

    IPage<CompetitionListVO> page(CompetitionQueryDTO dto);

    CompetitionDetailVO detail(Long id);

    Long add(CompetitionAddDTO dto);

    void update(Long id, CompetitionUpdateDTO dto);

    void toggleStatus(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建 CompetitionServiceImpl**

```java
package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionDetailDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.service.certificate.CompetitionService;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
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
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionMapper competitionMapper;
    private final CompetitionDetailMapper competitionDetailMapper;
    private final CompetitionMajorMapper competitionMajorMapper;

    @Override
    public IPage<CompetitionListVO> page(CompetitionQueryDTO dto) {
        Page<Competition> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getCompName())) {
            wrapper.like(Competition::getCompName, dto.getCompName());
        }
        if (StringUtils.hasText(dto.getCompLevel())) {
            wrapper.like(Competition::getCompLevel, dto.getCompLevel());
        }
        if (dto.getIsDeleted() != null) {
            wrapper.eq(Competition::getIsDeleted, dto.getIsDeleted());
        }

        wrapper.orderByDesc(Competition::getUpdatedAt);

        IPage<Competition> competitionPage = competitionMapper.selectPage(page, wrapper);

        return competitionPage.convert(comp -> {
            CompetitionListVO vo = new CompetitionListVO();
            BeanUtils.copyProperties(comp, vo);
            return vo;
        });
    }

    @Override
    public CompetitionDetailVO detail(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        CompetitionDetailVO vo = new CompetitionDetailVO();
        BeanUtils.copyProperties(competition, vo);

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            vo.setDetailId(detail.getId());
            vo.setBasicInfo(detail.getBasicInfo());
            vo.setAwards(detail.getAwards());
            vo.setBackground(detail.getBackground());
            vo.setPurposes(detail.getPurposes());
            vo.setCompetitionRules(detail.getCompetitionRules());
            vo.setScoringCriteria(detail.getScoringCriteria());
            vo.setNotices(detail.getNotices());
            vo.setProcessGuide(detail.getProcessGuide());
            vo.setAwardsDisplay(detail.getAwardsDisplay());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CompetitionAddDTO dto) {
        if (competitionMapper.existsByCompName(dto.getCompName())) {
            throw new BusinessException(400, "竞赛名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long competitionId = SnowflakeIdGenerator.nextId();
        Long detailId = SnowflakeIdGenerator.nextId();

        Competition competition = Competition.builder()
                .id(competitionId)
                .compName(dto.getCompName())
                .compLevel(dto.getCompLevel())
                .registrationTime(dto.getRegistrationTime())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        competitionMapper.insert(competition);

        CompetitionDetail detail = buildDetailFromDTO(detailId, competitionId, dto.getDetail(), now);
        competitionDetailMapper.insert(detail);

        log.info("新增竞赛成功: id={}, compName={}", competitionId, dto.getCompName());
        return competitionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CompetitionUpdateDTO dto) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        if (!competition.getCompName().equals(dto.getCompName())
                && competitionMapper.existsByCompName(dto.getCompName())) {
            throw new BusinessException(400, "竞赛名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();

        competition.setCompName(dto.getCompName());
        competition.setCompLevel(dto.getCompLevel());
        competition.setRegistrationTime(dto.getRegistrationTime());
        competition.setUpdatedAt(now);

        competitionMapper.updateById(competition);

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null && dto.getDetail() != null) {
            updateDetailFromDTO(detail, dto.getDetail(), now);
            competitionDetailMapper.updateById(detail);
        }

        log.info("更新竞赛成功: id={}, compName={}", id, dto.getCompName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        boolean newStatus = !competition.getIsDeleted();

        competition.setIsDeleted(newStatus);
        competition.setUpdatedAt(now);
        competitionMapper.updateById(competition);

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            detail.setIsDeleted(newStatus);
            detail.setUpdatedAt(now);
            competitionDetailMapper.updateById(detail);
        }

        log.info("切换竞赛状态成功: id={}, isDeleted={}", id, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        competitionMajorMapper.deleteByCompetitionId(id);

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            competitionDetailMapper.deleteById(detail.getId());
        }

        competitionMapper.deleteById(id);

        log.info("硬删除竞赛成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的竞赛");
        }

        for (Long competitionId : ids) {
            competitionMajorMapper.deleteByCompetitionId(competitionId);

            CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(competitionId);
            if (detail != null) {
                competitionDetailMapper.deleteById(detail.getId());
            }
        }

        int deleted = competitionMapper.deleteBatchIds(ids);

        log.info("批量硬删除竞赛成功: 删除数量={}, ids={}", deleted, ids);
    }

    private CompetitionDetail buildDetailFromDTO(Long detailId, Long competitionId,
            CompetitionDetailDTO dto, OffsetDateTime now) {
        CompetitionDetail.CompetitionDetailBuilder builder = CompetitionDetail.builder()
                .id(detailId)
                .competitionId(competitionId)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now);

        if (dto != null) {
            builder.basicInfo(dto.getBasicInfo())
                    .awards(dto.getAwards())
                    .background(dto.getBackground())
                    .purposes(dto.getPurposes())
                    .competitionRules(dto.getCompetitionRules())
                    .scoringCriteria(dto.getScoringCriteria())
                    .notices(dto.getNotices())
                    .processGuide(dto.getProcessGuide())
                    .awardsDisplay(dto.getAwardsDisplay());
        }

        return builder.build();
    }

    private void updateDetailFromDTO(CompetitionDetail detail, CompetitionDetailDTO dto,
            OffsetDateTime now) {
        detail.setBasicInfo(dto.getBasicInfo());
        detail.setAwards(dto.getAwards());
        detail.setBackground(dto.getBackground());
        detail.setPurposes(dto.getPurposes());
        detail.setCompetitionRules(dto.getCompetitionRules());
        detail.setScoringCriteria(dto.getScoringCriteria());
        detail.setNotices(dto.getNotices());
        detail.setProcessGuide(dto.getProcessGuide());
        detail.setAwardsDisplay(dto.getAwardsDisplay());
        detail.setUpdatedAt(now);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/certificate/CompetitionService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/certificate/CompetitionServiceImpl.java
git commit -m "feat(certificate): add CompetitionService with transaction support

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 竞赛-专业关联Service接口和实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/certificate/CompetitionMajorService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/certificate/CompetitionMajorServiceImpl.java`

- [ ] **Step 1: 创建 CompetitionMajorService 接口**

```java
package com.haifeng.admin.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.CompetitionMajorAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorQueryDTO;
import com.haifeng.admin.vo.certificate.CompetitionMajorListVO;

import java.util.List;

public interface CompetitionMajorService {

    IPage<CompetitionMajorListVO> page(CompetitionMajorQueryDTO dto);

    Long add(CompetitionMajorAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
```

- [ ] **Step 2: 创建 CompetitionMajorServiceImpl**

```java
package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CompetitionMajorAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorQueryDTO;
import com.haifeng.admin.service.certificate.CompetitionMajorService;
import com.haifeng.admin.vo.certificate.CompetitionMajorListVO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionMajor;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
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
public class CompetitionMajorServiceImpl implements CompetitionMajorService {

    private final CompetitionMajorMapper competitionMajorMapper;
    private final CompetitionMapper competitionMapper;
    private final MajorMapper majorMapper;

    @Override
    public IPage<CompetitionMajorListVO> page(CompetitionMajorQueryDTO dto) {
        Page<CompetitionMajor> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<CompetitionMajor> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getMajorName())) {
            wrapper.like(CompetitionMajor::getMajorName, dto.getMajorName());
        }
        if (StringUtils.hasText(dto.getCompetitionName())) {
            wrapper.like(CompetitionMajor::getCompetitionName, dto.getCompetitionName());
        }

        wrapper.orderByDesc(CompetitionMajor::getCreatedAt);

        IPage<CompetitionMajor> competitionMajorPage = competitionMajorMapper.selectPage(page, wrapper);

        return competitionMajorPage.convert(cm -> {
            CompetitionMajorListVO vo = new CompetitionMajorListVO();
            BeanUtils.copyProperties(cm, vo);
            return vo;
        });
    }

    @Override
    public Long add(CompetitionMajorAddDTO dto) {
        Competition competition = competitionMapper.findByCompName(dto.getCompetitionName());
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在: " + dto.getCompetitionName());
        }

        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        majorWrapper.eq(Major::getMajorName, dto.getMajorName())
                   .eq(Major::getStatus, 1);
        Major major = majorMapper.selectOne(majorWrapper);
        if (major == null) {
            throw new BusinessException(404, "专业不存在: " + dto.getMajorName());
        }

        if (competitionMajorMapper.existsByCompetitionIdAndMajorId(competition.getId(), major.getId())) {
            throw new BusinessException(400, "该竞赛与专业的关联已存在");
        }

        Long id = SnowflakeIdGenerator.nextId();

        CompetitionMajor competitionMajor = CompetitionMajor.builder()
                .id(id)
                .competitionId(competition.getId())
                .majorId(major.getId())
                .majorName(major.getMajorName())
                .competitionName(competition.getCompName())
                .createdAt(OffsetDateTime.now())
                .build();

        competitionMajorMapper.insert(competitionMajor);

        log.info("新增竞赛-专业关联成功: id={}, competition={}, major={}",
                id, dto.getCompetitionName(), dto.getMajorName());
        return id;
    }

    @Override
    public void delete(Long id) {
        CompetitionMajor competitionMajor = competitionMajorMapper.selectById(id);
        if (competitionMajor == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        competitionMajorMapper.deleteById(id);

        log.info("硬删除竞赛-专业关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的关联记录");
        }

        int deleted = competitionMajorMapper.deleteBatchIds(ids);

        log.info("批量硬删除竞赛-专业关联成功: 删除数量={}, ids={}", deleted, ids);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/certificate/CompetitionMajorService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/certificate/CompetitionMajorServiceImpl.java
git commit -m "feat(certificate): add CompetitionMajorService with name-based association

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: Controller类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/certificate/CertificateController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/certificate/CompetitionController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/certificate/CompetitionMajorController.java`

- [ ] **Step 1: 创建 CertificateController**

```java
package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CertificateAddDTO;
import com.haifeng.admin.dto.certificate.CertificateQueryDTO;
import com.haifeng.admin.dto.certificate.CertificateUpdateDTO;
import com.haifeng.admin.service.certificate.CertificateService;
import com.haifeng.admin.vo.certificate.CertificateDetailVO;
import com.haifeng.admin.vo.certificate.CertificateListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/certificate/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/list")
    public R<IPage<CertificateListVO>> list(@Valid CertificateQueryDTO dto) {
        return R.ok(certificateService.page(dto));
    }

    @GetMapping("/{id}")
    public R<CertificateDetailVO> detail(@PathVariable Long id) {
        return R.ok(certificateService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "证书管理", action = "新增证书")
    public R<Long> add(@Valid @RequestBody CertificateAddDTO dto) {
        return R.ok(certificateService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "证书管理", action = "修改证书")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CertificateUpdateDTO dto) {
        certificateService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle-status")
    @OperationLog(module = "证书管理", action = "切换证书状态")
    public R<Void> toggleStatus(@PathVariable Long id) {
        certificateService.toggleStatus(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "证书管理", action = "硬删除证书")
    public R<Void> delete(@PathVariable Long id) {
        certificateService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "证书管理", action = "批量硬删除证书")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        certificateService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 2: 创建 CompetitionController**

```java
package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.service.certificate.CompetitionService;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/certificate/competition")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping("/list")
    public R<IPage<CompetitionListVO>> list(@Valid CompetitionQueryDTO dto) {
        return R.ok(competitionService.page(dto));
    }

    @GetMapping("/{id}")
    public R<CompetitionDetailVO> detail(@PathVariable Long id) {
        return R.ok(competitionService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "竞赛管理", action = "新增竞赛")
    public R<Long> add(@Valid @RequestBody CompetitionAddDTO dto) {
        return R.ok(competitionService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "竞赛管理", action = "修改竞赛")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CompetitionUpdateDTO dto) {
        competitionService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle-status")
    @OperationLog(module = "竞赛管理", action = "切换竞赛状态")
    public R<Void> toggleStatus(@PathVariable Long id) {
        competitionService.toggleStatus(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "竞赛管理", action = "硬删除竞赛")
    public R<Void> delete(@PathVariable Long id) {
        competitionService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "竞赛管理", action = "批量硬删除竞赛")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        competitionService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 3: 创建 CompetitionMajorController**

```java
package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorQueryDTO;
import com.haifeng.admin.service.certificate.CompetitionMajorService;
import com.haifeng.admin.vo.certificate.CompetitionMajorListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/certificate/competition-major")
@RequiredArgsConstructor
public class CompetitionMajorController {

    private final CompetitionMajorService competitionMajorService;

    @GetMapping("/list")
    public R<IPage<CompetitionMajorListVO>> list(@Valid CompetitionMajorQueryDTO dto) {
        return R.ok(competitionMajorService.page(dto));
    }

    @PostMapping
    @OperationLog(module = "竞赛-专业关联", action = "新增关联")
    public R<Long> add(@Valid @RequestBody CompetitionMajorAddDTO dto) {
        return R.ok(competitionMajorService.add(dto));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "竞赛-专业关联", action = "硬删除关联")
    public R<Void> delete(@PathVariable Long id) {
        competitionMajorService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "竞赛-专业关联", action = "批量硬删除关联")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        competitionMajorService.batchDelete(dto.getIds());
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/certificate/
git commit -m "feat(certificate): add controllers for certificate module

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 10: 验证和最终提交

- [ ] **Step 1: 检查项目编译**

```bash
cd haifeng-admin && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 检查代码格式和导入**

确保所有文件的import语句正确，没有未使用的导入。

- [ ] **Step 3: 创建最终提交（如果有遗漏修复）**

```bash
git status
# 如有未提交的修复
git add -A
git commit -m "fix(certificate): fix compilation issues

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## 完成清单

| Task | 描述 | 状态 |
|------|------|------|
| Task 1 | 数据库迁移脚本 | [ ] |
| Task 2 | 实体类 | [ ] |
| Task 3 | Mapper接口 | [ ] |
| Task 4 | DTO类 | [ ] |
| Task 5 | VO类 | [ ] |
| Task 6 | 证书Service | [ ] |
| Task 7 | 竞赛Service | [ ] |
| Task 8 | 竞赛-专业关联Service | [ ] |
| Task 9 | Controller类 | [ ] |
| Task 10 | 验证和最终提交 | [ ] |
