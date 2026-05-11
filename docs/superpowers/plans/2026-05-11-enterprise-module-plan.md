# 企业模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现企业管理模块，包含企业CRUD、企业-行业关联管理、xlsx批量导入功能

**Architecture:** 双Controller架构（EnterpriseController + EnterpriseIndustryController），复用现有EasyExcel导入模式，企业+岗位单文件多Sheet导入

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL, Flyway

---

## 文件结构

### 新建文件

**haifeng-admin/src/main/resources/db/migration/**
- `V15__enterprise__tables.sql` - Flyway迁移脚本

**haifeng-common/src/main/java/com/haifeng/common/entity/company/**
- `Enterprise.java` - 企业实体
- `EnterprisePosition.java` - 企业岗位实体
- `EnterpriseIndustry.java` - 企业-行业关联实体

**haifeng-common/src/main/java/com/haifeng/common/mapper/company/**
- `EnterpriseMapper.java`
- `EnterprisePositionMapper.java`
- `EnterpriseIndustryMapper.java`

**haifeng-admin/src/main/java/com/haifeng/admin/excel/company/**
- `EnterpriseExcelDTO.java` - 企业导入DTO
- `EnterprisePositionExcelDTO.java` - 岗位导入DTO
- `EnterpriseIndustryExcelDTO.java` - 关联表导入DTO
- `StringArrayConverter.java` - TEXT[]转换器（复用university包的）

**haifeng-admin/src/main/java/com/haifeng/admin/dto/company/**
- `EnterpriseQueryDTO.java`
- `EnterpriseAddDTO.java`
- `EnterpriseUpdateDTO.java`
- `EnterpriseStatusDTO.java`
- `EnterpriseBatchDeleteDTO.java`
- `EnterpriseIndustryQueryDTO.java`
- `EnterpriseIndustryBatchDeleteDTO.java`

**haifeng-admin/src/main/java/com/haifeng/admin/vo/company/**
- `EnterpriseListVO.java`
- `EnterpriseDetailVO.java`
- `EnterprisePositionVO.java`
- `EnterpriseIndustryListVO.java`
- `EnterpriseIndustryDetailVO.java`

**haifeng-admin/src/main/java/com/haifeng/admin/service/company/**
- `EnterpriseService.java`
- `EnterpriseIndustryService.java`

**haifeng-admin/src/main/java/com/haifeng/admin/service/impl/company/**
- `EnterpriseServiceImpl.java`
- `EnterpriseIndustryServiceImpl.java`

**haifeng-admin/src/main/java/com/haifeng/admin/controller/company/**
- `EnterpriseController.java`
- `EnterpriseIndustryController.java`

---

## Task 1: Flyway迁移脚本

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V15__enterprise__tables.sql`

- [ ] **Step 1: 创建V16迁移脚本**

```sql
-- ============================================================
-- V16: 企业模块表
-- ============================================================

BEGIN;

-- ============================================================
-- 企业表 (t_enterprise)
-- ============================================================

CREATE TABLE IF NOT EXISTS t_enterprise (

    id                      BIGINT          PRIMARY KEY,

    -- ==================== 地区信息 ====================
    city_name               VARCHAR(50),                            -- 城市名称

    -- ==================== 基本信息 ====================
    enterprise_name         VARCHAR(200)    NOT NULL,                -- 企业名称
    enterprise_nature       VARCHAR(30)     NOT NULL,                -- 企业性质
    enterprise_type         VARCHAR(50),                             -- 企业类型
    logo_url                VARCHAR(500),                            -- Logo地址
    official_website        VARCHAR(500),                            -- 官网

    -- ==================== 总部信息 ====================
    region                  VARCHAR(100),                            -- 总部地区

    -- ==================== 描述信息 ====================
    enterprise_scale        VARCHAR(50),                             -- 企业规模
    main_business           VARCHAR(500),                            -- 主营业务
    enterprise_intro        TEXT,                                    -- 企业简介

    -- ==================== 状态 ====================
    recruitment_status      VARCHAR(20)     DEFAULT '招聘中',         -- 招聘状态

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_enterprise_name UNIQUE (enterprise_name),

    CONSTRAINT chk_enterprise_nature CHECK (
        enterprise_nature IN ('央企', '国企', '民企', '外企', '合资')
    )
);

-- 索引
CREATE INDEX idx_ent_city_nature_type
    ON t_enterprise (city_name, enterprise_nature, enterprise_type)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ent_nature
    ON t_enterprise (enterprise_nature)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ent_name_pattern
    ON t_enterprise USING btree (enterprise_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ent_region
    ON t_enterprise (region)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_enterprise_updated_at
    BEFORE UPDATE ON t_enterprise
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_enterprise                          IS '企业主体表';
COMMENT ON COLUMN t_enterprise.city_name                IS '城市名称';
COMMENT ON COLUMN t_enterprise.enterprise_name          IS '企业名称';
COMMENT ON COLUMN t_enterprise.enterprise_nature        IS '企业性质：央企、国企、民企、外企、合资';
COMMENT ON COLUMN t_enterprise.enterprise_type          IS '企业类型';
COMMENT ON COLUMN t_enterprise.logo_url                 IS 'Logo图片地址';
COMMENT ON COLUMN t_enterprise.official_website         IS '企业官网';
COMMENT ON COLUMN t_enterprise.region                   IS '总部所在地区';
COMMENT ON COLUMN t_enterprise.enterprise_scale         IS '企业规模';
COMMENT ON COLUMN t_enterprise.main_business            IS '主营业务';
COMMENT ON COLUMN t_enterprise.enterprise_intro         IS '企业简介';
COMMENT ON COLUMN t_enterprise.recruitment_status       IS '招聘状态';


-- ============================================================
-- 企业岗位表 (t_enterprise_position)
-- ============================================================

CREATE TABLE IF NOT EXISTS t_enterprise_position (

    id                          BIGINT          PRIMARY KEY,

    -- ==================== 关联企业 ====================
    enterprise_id               BIGINT          NOT NULL,

    -- ==================== 岗位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    recruitment_type            VARCHAR(30),
    position_requirement        TEXT,
    position_tags               TEXT[]          DEFAULT '{}',

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30),
    city                        VARCHAR(50),
    work_location               VARCHAR(200),

    -- ==================== 招聘要求 ====================
    education_requirement       VARCHAR(30),
    major_requirement           VARCHAR(500),
    work_experience             VARCHAR(50),

    -- ==================== 薪资信息 ====================
    salary_min                  INTEGER,
    salary_max                  INTEGER,

    -- ==================== 报名信息 ====================
    apply_link                  VARCHAR(500),
    deadline                    TIMESTAMPTZ,

    -- ==================== 状态 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_ep_recruitment_type CHECK (
        recruitment_type IS NULL
        OR recruitment_type IN ('校招', '社招', '实习')
    ),
    CONSTRAINT chk_ep_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_ep_position_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束')
    ),
    CONSTRAINT chk_ep_salary CHECK (
        salary_min IS NULL
        OR salary_max IS NULL
        OR salary_min <= salary_max
    )
);

-- 索引
CREATE INDEX idx_ep_enterprise
    ON t_enterprise_position (enterprise_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_position_name
    ON t_enterprise_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_recruitment_type
    ON t_enterprise_position (recruitment_type)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_location
    ON t_enterprise_position (province, city)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_education
    ON t_enterprise_position (education_requirement)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_deadline
    ON t_enterprise_position (deadline ASC NULLS LAST)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_tags
    ON t_enterprise_position USING gin (position_tags)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_ep_salary
    ON t_enterprise_position (salary_min, salary_max)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_enterprise_position_updated_at
    BEFORE UPDATE ON t_enterprise_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_enterprise_position                             IS '企业岗位表';
COMMENT ON COLUMN t_enterprise_position.enterprise_id               IS '关联企业ID';
COMMENT ON COLUMN t_enterprise_position.position_name               IS '岗位名称';
COMMENT ON COLUMN t_enterprise_position.recruitment_type            IS '招聘类型：校招/社招/实习';
COMMENT ON COLUMN t_enterprise_position.position_requirement        IS '岗位要求详细描述';
COMMENT ON COLUMN t_enterprise_position.position_tags               IS '岗位标签';
COMMENT ON COLUMN t_enterprise_position.province                    IS '省份';
COMMENT ON COLUMN t_enterprise_position.city                        IS '城市';
COMMENT ON COLUMN t_enterprise_position.work_location               IS '详细工作地点';
COMMENT ON COLUMN t_enterprise_position.education_requirement       IS '学历要求';
COMMENT ON COLUMN t_enterprise_position.major_requirement           IS '专业要求';
COMMENT ON COLUMN t_enterprise_position.work_experience             IS '工作经验要求';
COMMENT ON COLUMN t_enterprise_position.salary_min                  IS '最低月薪（单位：k）';
COMMENT ON COLUMN t_enterprise_position.salary_max                  IS '最高月薪（单位：k）';
COMMENT ON COLUMN t_enterprise_position.apply_link                  IS '申请链接';
COMMENT ON COLUMN t_enterprise_position.deadline                    IS '报名截止日期';
COMMENT ON COLUMN t_enterprise_position.position_status             IS '岗位状态';


-- ============================================================
-- 企业-行业关联表 (t_enterprise_industry)
-- ============================================================

CREATE TABLE IF NOT EXISTS t_enterprise_industry (

    id              BIGINT      PRIMARY KEY,
    enterprise_id   BIGINT      NOT NULL,
    enterprise_name VARCHAR(200) NOT NULL,
    industry_id     BIGINT      NOT NULL,
    industry_name   VARCHAR(100) NOT NULL,
    is_primary      BOOLEAN     NOT NULL DEFAULT FALSE,
    sort_order      SMALLINT    NOT NULL DEFAULT 0,

    -- 审计
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 唯一约束
    CONSTRAINT uk_ent_ind UNIQUE (enterprise_id, industry_id)
);

-- 索引
CREATE INDEX idx_ei_industry
    ON t_enterprise_industry (industry_id);

CREATE INDEX idx_ei_enterprise
    ON t_enterprise_industry (enterprise_id);

-- 注释
COMMENT ON TABLE  t_enterprise_industry              IS '企业-行业多对多关联表';
COMMENT ON COLUMN t_enterprise_industry.enterprise_id IS '企业ID';
COMMENT ON COLUMN t_enterprise_industry.enterprise_name IS '企业名称（冗余）';
COMMENT ON COLUMN t_enterprise_industry.industry_id  IS '行业ID';
COMMENT ON COLUMN t_enterprise_industry.industry_name IS '行业名称（冗余）';
COMMENT ON COLUMN t_enterprise_industry.is_primary   IS '是否为主行业';
COMMENT ON COLUMN t_enterprise_industry.sort_order   IS '排序权重';

COMMIT;
```

- [ ] **Step 2: 验证SQL语法**

运行: `cd haifeng-admin && mvn flyway:info -Dflyway.configFiles=src/main/resources/flyway.conf`

预期: 显示V16为Pending状态

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/V15__enterprise__tables.sql
git commit -m "feat(db): add V16 enterprise module tables"
```

---

## Task 2: Entity类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/company/Enterprise.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/company/EnterprisePosition.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/company/EnterpriseIndustry.java`

- [ ] **Step 1: 创建Enterprise实体**

```java
package com.haifeng.common.entity.company;

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
@TableName(value = "t_enterprise", autoResultMap = true)
public class Enterprise {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String cityName;

    private String enterpriseName;

    private String enterpriseNature;

    private String enterpriseType;

    private String logoUrl;

    private String officialWebsite;

    private String region;

    private String enterpriseScale;

    private String mainBusiness;

    private String enterpriseIntro;

    private String recruitmentStatus;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建EnterprisePosition实体**

```java
package com.haifeng.common.entity.company;

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
@TableName(value = "t_enterprise_position", autoResultMap = true)
public class EnterprisePosition {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long enterpriseId;

    private String positionName;

    private String recruitmentType;

    private String positionRequirement;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> positionTags;

    private String province;

    private String city;

    private String workLocation;

    private String educationRequirement;

    private String majorRequirement;

    private String workExperience;

    private Integer salaryMin;

    private Integer salaryMax;

    private String applyLink;

    private OffsetDateTime deadline;

    private String positionStatus;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建EnterpriseIndustry实体**

```java
package com.haifeng.common.entity.company;

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
@TableName(value = "t_enterprise_industry", autoResultMap = true)
public class EnterpriseIndustry {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long enterpriseId;

    private String enterpriseName;

    private Long industryId;

    private String industryName;

    private Boolean isPrimary;

    private Short sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/company/
git commit -m "feat(entity): add Enterprise, EnterprisePosition, EnterpriseIndustry entities"
```

---

## Task 3: Mapper接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/company/EnterpriseMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/company/EnterprisePositionMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/company/EnterpriseIndustryMapper.java`

- [ ] **Step 1: 创建EnterpriseMapper**

```java
package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise WHERE enterprise_name = #{enterpriseName})")
    boolean existsByEnterpriseName(@Param("enterpriseName") String enterpriseName);

    @Select("SELECT id FROM t_enterprise WHERE enterprise_name = #{enterpriseName}")
    Long findIdByEnterpriseName(@Param("enterpriseName") String enterpriseName);
}
```

- [ ] **Step 2: 创建EnterprisePositionMapper**

```java
package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.EnterprisePosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface EnterprisePositionMapper extends BaseMapper<EnterprisePosition> {

    @Delete("DELETE FROM t_enterprise_position WHERE enterprise_id = #{enterpriseId}")
    int deleteByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
}
```

- [ ] **Step 3: 创建EnterpriseIndustryMapper**

```java
package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnterpriseIndustryMapper extends BaseMapper<EnterpriseIndustry> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise_industry WHERE enterprise_id = #{enterpriseId} AND industry_id = #{industryId})")
    boolean existsByEnterpriseIdAndIndustryId(@Param("enterpriseId") Long enterpriseId, @Param("industryId") Long industryId);
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/company/
git commit -m "feat(mapper): add Enterprise, EnterprisePosition, EnterpriseIndustry mappers"
```

---

## Task 4: Excel DTO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/company/EnterpriseExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/company/EnterprisePositionExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/company/EnterpriseIndustryExcelDTO.java`

- [ ] **Step 1: 创建EnterpriseExcelDTO**

```java
package com.haifeng.admin.excel.company;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EnterpriseExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("企业名称")
    private String enterpriseName;

    @ExcelProperty("企业性质")
    private String enterpriseNature;

    @ExcelProperty("企业类型")
    private String enterpriseType;

    @ExcelProperty("Logo地址")
    private String logoUrl;

    @ExcelProperty("官网")
    private String officialWebsite;

    @ExcelProperty("总部地区")
    private String region;

    @ExcelProperty("企业规模")
    private String enterpriseScale;

    @ExcelProperty("主营业务")
    private String mainBusiness;

    @ExcelProperty("企业简介")
    private String enterpriseIntro;

    @ExcelProperty("招聘状态")
    private String recruitmentStatus;
}
```

- [ ] **Step 2: 创建EnterprisePositionExcelDTO**

```java
package com.haifeng.admin.excel.company;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnterprisePositionExcelDTO {

    @ExcelProperty("企业名称")
    private String enterpriseName;

    @ExcelProperty("岗位名称")
    private String positionName;

    @ExcelProperty("招聘类型")
    private String recruitmentType;

    @ExcelProperty("岗位要求")
    private String positionRequirement;

    @ExcelProperty(value = "岗位标签", converter = StringArrayConverter.class)
    private List<String> positionTags;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("城市")
    private String city;

    @ExcelProperty("工作地点")
    private String workLocation;

    @ExcelProperty("学历要求")
    private String educationRequirement;

    @ExcelProperty("专业要求")
    private String majorRequirement;

    @ExcelProperty("工作经验")
    private String workExperience;

    @ExcelProperty("最低薪资")
    private Integer salaryMin;

    @ExcelProperty("最高薪资")
    private Integer salaryMax;

    @ExcelProperty("申请链接")
    private String applyLink;

    @ExcelProperty("截止日期")
    private LocalDateTime deadline;

    @ExcelProperty("岗位状态")
    private String positionStatus;
}
```

- [ ] **Step 3: 创建EnterpriseIndustryExcelDTO**

```java
package com.haifeng.admin.excel.company;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EnterpriseIndustryExcelDTO {

    @ExcelProperty("企业名称")
    private String enterpriseName;

    @ExcelProperty("行业名称")
    private String industryName;
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/company/
git commit -m "feat(excel): add Enterprise module Excel DTOs"
```

---

## Task 5: DTO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseStatusDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseBatchDeleteDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseIndustryQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/company/EnterpriseIndustryBatchDeleteDTO.java`

- [ ] **Step 1: 创建EnterpriseQueryDTO**

```java
package com.haifeng.admin.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseQueryDTO extends BasePageQueryDTO {

    private String cityName;

    private String enterpriseName;

    private String enterpriseNature;

    private String enterpriseType;

    private String recruitmentStatus;

    private Boolean isDeleted;
}
```

- [ ] **Step 2: 创建EnterpriseAddDTO**

```java
package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnterpriseAddDTO {

    private String cityName;

    @NotBlank(message = "企业名称不能为空")
    private String enterpriseName;

    @NotBlank(message = "企业性质不能为空")
    @Pattern(regexp = "央企|国企|民企|外企|合资", message = "企业性质必须是：央企、国企、民企、外企、合资")
    private String enterpriseNature;

    private String enterpriseType;

    private String logoUrl;

    private String officialWebsite;

    private String region;

    private String enterpriseScale;

    private String mainBusiness;

    private String enterpriseIntro;

    private String recruitmentStatus;
}
```

- [ ] **Step 3: 创建EnterpriseUpdateDTO**

```java
package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnterpriseUpdateDTO {

    private String cityName;

    @NotBlank(message = "企业名称不能为空")
    private String enterpriseName;

    @NotBlank(message = "企业性质不能为空")
    @Pattern(regexp = "央企|国企|民企|外企|合资", message = "企业性质必须是：央企、国企、民企、外企、合资")
    private String enterpriseNature;

    private String enterpriseType;

    private String logoUrl;

    private String officialWebsite;

    private String region;

    private String enterpriseScale;

    private String mainBusiness;

    private String enterpriseIntro;

    private String recruitmentStatus;

    private Boolean isDeleted;
}
```

- [ ] **Step 4: 创建EnterpriseStatusDTO**

```java
package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnterpriseStatusDTO {

    @NotNull(message = "状态不能为空")
    private Boolean isDeleted;
}
```

- [ ] **Step 5: 创建EnterpriseBatchDeleteDTO**

```java
package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnterpriseBatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的企业")
    private List<Long> ids;
}
```

- [ ] **Step 6: 创建EnterpriseIndustryQueryDTO**

```java
package com.haifeng.admin.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseIndustryQueryDTO extends BasePageQueryDTO {

    private String enterpriseName;

    private String industryName;
}
```

- [ ] **Step 7: 创建EnterpriseIndustryBatchDeleteDTO**

```java
package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class EnterpriseIndustryBatchDeleteDTO {

    @NotEmpty(message = "请选择要删除的关联记录")
    private List<Long> ids;
}
```

- [ ] **Step 8: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/company/
git commit -m "feat(dto): add Enterprise module DTOs"
```

---

## Task 6: VO类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/company/EnterpriseListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/company/EnterpriseDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/company/EnterprisePositionVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/company/EnterpriseIndustryListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/company/EnterpriseIndustryDetailVO.java`

- [ ] **Step 1: 创建EnterpriseListVO**

```java
package com.haifeng.admin.vo.company;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseListVO {

    private Long id;

    private String cityName;

    private String enterpriseName;

    private String enterpriseNature;

    private String enterpriseType;

    private String recruitmentStatus;

    private Boolean isDeleted;

    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 创建EnterpriseDetailVO**

```java
package com.haifeng.admin.vo.company;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnterpriseDetailVO {

    private Long id;

    private String cityName;

    private String enterpriseName;

    private String enterpriseNature;

    private String enterpriseType;

    private String logoUrl;

    private String officialWebsite;

    private String region;

    private String enterpriseScale;

    private String mainBusiness;

    private String enterpriseIntro;

    private String recruitmentStatus;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Tab2: 岗位列表
    private List<EnterprisePositionVO> positions;
}
```

- [ ] **Step 3: 创建EnterprisePositionVO**

```java
package com.haifeng.admin.vo.company;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnterprisePositionVO {

    private Long id;

    private Long enterpriseId;

    private String positionName;

    private String recruitmentType;

    private String positionRequirement;

    private List<String> positionTags;

    private String province;

    private String city;

    private String workLocation;

    private String educationRequirement;

    private String majorRequirement;

    private String workExperience;

    private Integer salaryMin;

    private Integer salaryMax;

    private String applyLink;

    private LocalDateTime deadline;

    private String positionStatus;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: 创建EnterpriseIndustryListVO**

```java
package com.haifeng.admin.vo.company;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseIndustryListVO {

    private Long id;

    private Long enterpriseId;

    private String enterpriseName;

    private Long industryId;

    private String industryName;

    private Boolean isPrimary;

    private Short sortOrder;

    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 创建EnterpriseIndustryDetailVO**

```java
package com.haifeng.admin.vo.company;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnterpriseIndustryDetailVO {

    private Long id;

    private Long enterpriseId;

    private String enterpriseName;

    private Long industryId;

    private String industryName;

    private Boolean isPrimary;

    private Short sortOrder;

    private LocalDateTime createdAt;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/company/
git commit -m "feat(vo): add Enterprise module VOs"
```

---

## Task 7: EnterpriseService接口和实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/company/EnterpriseService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/company/EnterpriseServiceImpl.java`

- [ ] **Step 1: 创建EnterpriseService接口**

```java
package com.haifeng.admin.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.*;
import com.haifeng.admin.vo.company.EnterpriseDetailVO;
import com.haifeng.admin.vo.company.EnterpriseListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EnterpriseService {

    IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto);

    EnterpriseDetailVO detail(Long id);

    Long add(EnterpriseAddDTO dto);

    void update(Long id, EnterpriseUpdateDTO dto);

    void updateStatus(Long id, EnterpriseStatusDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importEnterprises(MultipartFile file);
}
```

- [ ] **Step 2: 创建EnterpriseServiceImpl**

```java
package com.haifeng.admin.service.impl.company;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.company.*;
import com.haifeng.admin.excel.company.EnterpriseExcelDTO;
import com.haifeng.admin.excel.company.EnterprisePositionExcelDTO;
import com.haifeng.admin.service.company.EnterpriseService;
import com.haifeng.admin.vo.company.EnterpriseDetailVO;
import com.haifeng.admin.vo.company.EnterpriseListVO;
import com.haifeng.admin.vo.company.EnterprisePositionVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
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
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final EnterprisePositionMapper enterprisePositionMapper;

    @Override
    public IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto) {
        Page<Enterprise> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getCityName())) {
            wrapper.like(Enterprise::getCityName, dto.getCityName());
        }
        if (StringUtils.hasText(dto.getEnterpriseName())) {
            wrapper.like(Enterprise::getEnterpriseName, dto.getEnterpriseName());
        }
        if (StringUtils.hasText(dto.getEnterpriseNature())) {
            wrapper.eq(Enterprise::getEnterpriseNature, dto.getEnterpriseNature());
        }
        if (StringUtils.hasText(dto.getEnterpriseType())) {
            wrapper.like(Enterprise::getEnterpriseType, dto.getEnterpriseType());
        }
        if (StringUtils.hasText(dto.getRecruitmentStatus())) {
            wrapper.eq(Enterprise::getRecruitmentStatus, dto.getRecruitmentStatus());
        }
        if (dto.getIsDeleted() != null) {
            wrapper.eq(Enterprise::getIsDeleted, dto.getIsDeleted());
        }

        wrapper.orderByDesc(Enterprise::getCreatedAt);

        IPage<Enterprise> enterprisePage = enterpriseMapper.selectPage(page, wrapper);

        return enterprisePage.convert(enterprise -> {
            EnterpriseListVO vo = new EnterpriseListVO();
            BeanUtils.copyProperties(enterprise, vo);
            if (enterprise.getCreatedAt() != null) {
                vo.setCreatedAt(enterprise.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public EnterpriseDetailVO detail(Long id) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        EnterpriseDetailVO vo = new EnterpriseDetailVO();
        BeanUtils.copyProperties(enterprise, vo);

        if (enterprise.getCreatedAt() != null) {
            vo.setCreatedAt(enterprise.getCreatedAt().toLocalDateTime());
        }
        if (enterprise.getUpdatedAt() != null) {
            vo.setUpdatedAt(enterprise.getUpdatedAt().toLocalDateTime());
        }

        // 查询岗位列表
        LambdaQueryWrapper<EnterprisePosition> positionWrapper = new LambdaQueryWrapper<>();
        positionWrapper.eq(EnterprisePosition::getEnterpriseId, id)
                       .eq(EnterprisePosition::getIsDeleted, false)
                       .orderByDesc(EnterprisePosition::getCreatedAt);
        List<EnterprisePosition> positions = enterprisePositionMapper.selectList(positionWrapper);

        List<EnterprisePositionVO> positionVOs = positions.stream().map(position -> {
            EnterprisePositionVO pvo = new EnterprisePositionVO();
            BeanUtils.copyProperties(position, pvo);
            if (position.getCreatedAt() != null) {
                pvo.setCreatedAt(position.getCreatedAt().toLocalDateTime());
            }
            if (position.getUpdatedAt() != null) {
                pvo.setUpdatedAt(position.getUpdatedAt().toLocalDateTime());
            }
            if (position.getDeadline() != null) {
                pvo.setDeadline(position.getDeadline().toLocalDateTime());
            }
            return pvo;
        }).toList();

        vo.setPositions(positionVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(EnterpriseAddDTO dto) {
        if (enterpriseMapper.existsByEnterpriseName(dto.getEnterpriseName())) {
            throw new BusinessException(400, "企业名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long enterpriseId = SnowflakeIdGenerator.nextId();

        Enterprise enterprise = Enterprise.builder()
                .id(enterpriseId)
                .cityName(dto.getCityName())
                .enterpriseName(dto.getEnterpriseName())
                .enterpriseNature(dto.getEnterpriseNature())
                .enterpriseType(dto.getEnterpriseType())
                .logoUrl(dto.getLogoUrl())
                .officialWebsite(dto.getOfficialWebsite())
                .region(dto.getRegion())
                .enterpriseScale(dto.getEnterpriseScale())
                .mainBusiness(dto.getMainBusiness())
                .enterpriseIntro(dto.getEnterpriseIntro())
                .recruitmentStatus(dto.getRecruitmentStatus() != null ? dto.getRecruitmentStatus() : "招聘中")
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        enterpriseMapper.insert(enterprise);
        log.info("新增企业成功: id={}, enterpriseName={}", enterpriseId, dto.getEnterpriseName());
        return enterpriseId;
    }

    @Override
    public void update(Long id, EnterpriseUpdateDTO dto) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        if (!enterprise.getEnterpriseName().equals(dto.getEnterpriseName())
                && enterpriseMapper.existsByEnterpriseName(dto.getEnterpriseName())) {
            throw new BusinessException(400, "企业名称已存在");
        }

        enterprise.setCityName(dto.getCityName());
        enterprise.setEnterpriseName(dto.getEnterpriseName());
        enterprise.setEnterpriseNature(dto.getEnterpriseNature());
        enterprise.setEnterpriseType(dto.getEnterpriseType());
        enterprise.setLogoUrl(dto.getLogoUrl());
        enterprise.setOfficialWebsite(dto.getOfficialWebsite());
        enterprise.setRegion(dto.getRegion());
        enterprise.setEnterpriseScale(dto.getEnterpriseScale());
        enterprise.setMainBusiness(dto.getMainBusiness());
        enterprise.setEnterpriseIntro(dto.getEnterpriseIntro());
        enterprise.setRecruitmentStatus(dto.getRecruitmentStatus());
        if (dto.getIsDeleted() != null) {
            enterprise.setIsDeleted(dto.getIsDeleted());
        }
        enterprise.setUpdatedAt(OffsetDateTime.now());

        enterpriseMapper.updateById(enterprise);
        log.info("更新企业成功: id={}, enterpriseName={}", id, dto.getEnterpriseName());
    }

    @Override
    public void updateStatus(Long id, EnterpriseStatusDTO dto) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        enterprise.setIsDeleted(dto.getIsDeleted());
        enterprise.setUpdatedAt(OffsetDateTime.now());

        enterpriseMapper.updateById(enterprise);
        log.info("更新企业状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 删除关联岗位
        enterprisePositionMapper.deleteByEnterpriseId(id);

        // 删除企业
        enterpriseMapper.deleteById(id);

        log.info("硬删除企业成功: id={}, enterpriseName={}", id, enterprise.getEnterpriseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的企业");
        }

        // 删除所有关联岗位
        for (Long enterpriseId : ids) {
            enterprisePositionMapper.deleteByEnterpriseId(enterpriseId);
        }

        // 批量删除企业
        int deleted = enterpriseMapper.deleteBatchIds(ids);
        log.info("批量硬删除企业成功: 删除数量={}, ids={}", deleted, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importEnterprises(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet0: 企业主表
            List<EnterpriseExcelDTO> enterpriseData = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            if (enterpriseData == null || enterpriseData.isEmpty()) {
                throw new BusinessException(400, "导入失败：Sheet0（企业主表）为空");
            }

            // Sheet1: 企业岗位
            List<EnterprisePositionExcelDTO> positionData = EasyExcel.read(file.getInputStream())
                    .head(EnterprisePositionExcelDTO.class)
                    .sheet(1)
                    .doReadSync();

            Set<String> enterpriseNamesInFile = new HashSet<>();
            Map<String, Long> enterpriseNameToIdMap = new HashMap<>();
            List<Enterprise> enterprises = new ArrayList<>();
            List<EnterprisePosition> positions = new ArrayList<>();

            // 校验企业数据
            for (int i = 0; i < enterpriseData.size(); i++) {
                int rowNum = i + 2;
                EnterpriseExcelDTO data = enterpriseData.get(i);

                if (!StringUtils.hasText(data.getEnterpriseName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业名称不能为空");
                    continue;
                }

                if (!StringUtils.hasText(data.getEnterpriseNature())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业性质不能为空");
                    continue;
                }

                if (!isValidEnterpriseNature(data.getEnterpriseNature())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业性质必须是：央企、国企、民企、外企、合资");
                    continue;
                }

                if (enterpriseNamesInFile.contains(data.getEnterpriseName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业名称'" + data.getEnterpriseName() + "'在文件中重复");
                    continue;
                }
                enterpriseNamesInFile.add(data.getEnterpriseName());

                if (enterpriseMapper.existsByEnterpriseName(data.getEnterpriseName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业名称'" + data.getEnterpriseName() + "'已存在于数据库");
                    continue;
                }

                OffsetDateTime now = OffsetDateTime.now();
                Long enterpriseId = SnowflakeIdGenerator.nextId();
                enterpriseNameToIdMap.put(data.getEnterpriseName(), enterpriseId);

                Enterprise enterprise = Enterprise.builder()
                        .id(enterpriseId)
                        .cityName(data.getCityName())
                        .enterpriseName(data.getEnterpriseName())
                        .enterpriseNature(data.getEnterpriseNature())
                        .enterpriseType(data.getEnterpriseType())
                        .logoUrl(data.getLogoUrl())
                        .officialWebsite(data.getOfficialWebsite())
                        .region(data.getRegion())
                        .enterpriseScale(data.getEnterpriseScale())
                        .mainBusiness(data.getMainBusiness())
                        .enterpriseIntro(data.getEnterpriseIntro())
                        .recruitmentStatus(StringUtils.hasText(data.getRecruitmentStatus()) ? data.getRecruitmentStatus() : "招聘中")
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                enterprises.add(enterprise);
            }

            // 校验岗位数据
            if (positionData != null && !positionData.isEmpty()) {
                for (int i = 0; i < positionData.size(); i++) {
                    int rowNum = i + 2;
                    EnterprisePositionExcelDTO data = positionData.get(i);

                    if (!StringUtils.hasText(data.getEnterpriseName())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：企业名称不能为空");
                        continue;
                    }

                    if (!StringUtils.hasText(data.getPositionName())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：岗位名称不能为空");
                        continue;
                    }

                    if (!enterpriseNamesInFile.contains(data.getEnterpriseName())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：企业名称'" + data.getEnterpriseName() + "'在Sheet0中不存在");
                        continue;
                    }

                    if (StringUtils.hasText(data.getRecruitmentType()) && !isValidRecruitmentType(data.getRecruitmentType())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：招聘类型必须是：校招、社招、实习");
                        continue;
                    }

                    if (StringUtils.hasText(data.getEducationRequirement()) && !isValidEducation(data.getEducationRequirement())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：学历要求必须是：不限、大专、本科、硕士、博士");
                        continue;
                    }

                    Long enterpriseId = enterpriseNameToIdMap.get(data.getEnterpriseName());
                    OffsetDateTime now = OffsetDateTime.now();
                    Long positionId = SnowflakeIdGenerator.nextId();

                    EnterprisePosition position = EnterprisePosition.builder()
                            .id(positionId)
                            .enterpriseId(enterpriseId)
                            .positionName(data.getPositionName())
                            .recruitmentType(data.getRecruitmentType())
                            .positionRequirement(data.getPositionRequirement())
                            .positionTags(data.getPositionTags())
                            .province(data.getProvince())
                            .city(data.getCity())
                            .workLocation(data.getWorkLocation())
                            .educationRequirement(data.getEducationRequirement())
                            .majorRequirement(data.getMajorRequirement())
                            .workExperience(data.getWorkExperience())
                            .salaryMin(data.getSalaryMin())
                            .salaryMax(data.getSalaryMax())
                            .applyLink(data.getApplyLink())
                            .deadline(data.getDeadline() != null ? data.getDeadline().atOffset(ZoneOffset.ofHours(8)) : null)
                            .positionStatus(StringUtils.hasText(data.getPositionStatus()) ? data.getPositionStatus() : "招聘中")
                            .isDeleted(false)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    positions.add(position);
                }
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            // 批量插入
            for (Enterprise enterprise : enterprises) {
                enterpriseMapper.insert(enterprise);
            }
            for (EnterprisePosition position : positions) {
                enterprisePositionMapper.insert(position);
            }

            log.info("导入企业成功，企业数量={}，岗位数量={}", enterprises.size(), positions.size());

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }

    private boolean isValidEnterpriseNature(String nature) {
        return "央企".equals(nature) || "国企".equals(nature) || "民企".equals(nature)
                || "外企".equals(nature) || "合资".equals(nature);
    }

    private boolean isValidRecruitmentType(String type) {
        return "校招".equals(type) || "社招".equals(type) || "实习".equals(type);
    }

    private boolean isValidEducation(String education) {
        return "不限".equals(education) || "大专".equals(education) || "本科".equals(education)
                || "硕士".equals(education) || "博士".equals(education);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/company/EnterpriseService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/company/EnterpriseServiceImpl.java
git commit -m "feat(service): add EnterpriseService with xlsx import"
```

---

## Task 8: EnterpriseIndustryService接口和实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/company/EnterpriseIndustryService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/company/EnterpriseIndustryServiceImpl.java`

- [ ] **Step 1: 创建EnterpriseIndustryService接口**

```java
package com.haifeng.admin.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterpriseIndustryQueryDTO;
import com.haifeng.admin.vo.company.EnterpriseIndustryDetailVO;
import com.haifeng.admin.vo.company.EnterpriseIndustryListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EnterpriseIndustryService {

    IPage<EnterpriseIndustryListVO> page(EnterpriseIndustryQueryDTO dto);

    EnterpriseIndustryDetailVO detail(Long id);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void importEnterpriseIndustries(MultipartFile file);
}
```

- [ ] **Step 2: 创建EnterpriseIndustryServiceImpl**

```java
package com.haifeng.admin.service.impl.company;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.company.EnterpriseIndustryQueryDTO;
import com.haifeng.admin.excel.company.EnterpriseIndustryExcelDTO;
import com.haifeng.admin.service.company.EnterpriseIndustryService;
import com.haifeng.admin.vo.company.EnterpriseIndustryDetailVO;
import com.haifeng.admin.vo.company.EnterpriseIndustryListVO;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
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
public class EnterpriseIndustryServiceImpl implements EnterpriseIndustryService {

    private final EnterpriseIndustryMapper enterpriseIndustryMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final IndustryMapper industryMapper;

    @Override
    public IPage<EnterpriseIndustryListVO> page(EnterpriseIndustryQueryDTO dto) {
        Page<EnterpriseIndustry> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<EnterpriseIndustry> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getEnterpriseName())) {
            wrapper.like(EnterpriseIndustry::getEnterpriseName, dto.getEnterpriseName());
        }
        if (StringUtils.hasText(dto.getIndustryName())) {
            wrapper.like(EnterpriseIndustry::getIndustryName, dto.getIndustryName());
        }

        wrapper.orderByDesc(EnterpriseIndustry::getCreatedAt);

        IPage<EnterpriseIndustry> resultPage = enterpriseIndustryMapper.selectPage(page, wrapper);

        return resultPage.convert(entity -> {
            EnterpriseIndustryListVO vo = new EnterpriseIndustryListVO();
            BeanUtils.copyProperties(entity, vo);
            if (entity.getCreatedAt() != null) {
                vo.setCreatedAt(entity.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public EnterpriseIndustryDetailVO detail(Long id) {
        EnterpriseIndustry entity = enterpriseIndustryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        EnterpriseIndustryDetailVO vo = new EnterpriseIndustryDetailVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getCreatedAt() != null) {
            vo.setCreatedAt(entity.getCreatedAt().toLocalDateTime());
        }
        return vo;
    }

    @Override
    public void delete(Long id) {
        EnterpriseIndustry entity = enterpriseIndustryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        enterpriseIndustryMapper.deleteById(id);
        log.info("硬删除企业-行业关联成功: id={}, enterpriseName={}, industryName={}",
                id, entity.getEnterpriseName(), entity.getIndustryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的关联记录");
        }

        int deleted = enterpriseIndustryMapper.deleteBatchIds(ids);
        log.info("批量硬删除企业-行业关联成功: 删除数量={}, ids={}", deleted, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importEnterpriseIndustries(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            List<EnterpriseIndustryExcelDTO> data = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseIndustryExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            if (data == null || data.isEmpty()) {
                throw new BusinessException(400, "导入失败：Excel文件为空");
            }

            // 缓存查询结果
            Map<String, Long> enterpriseIdCache = new HashMap<>();
            Map<String, Long> industryIdCache = new HashMap<>();
            Set<String> relationKeysInFile = new HashSet<>();
            List<EnterpriseIndustry> toInsert = new ArrayList<>();

            for (int i = 0; i < data.size(); i++) {
                int rowNum = i + 2;
                EnterpriseIndustryExcelDTO dto = data.get(i);

                if (!StringUtils.hasText(dto.getEnterpriseName())) {
                    errorMsgs.add("第" + rowNum + "行：企业名称不能为空");
                    continue;
                }

                if (!StringUtils.hasText(dto.getIndustryName())) {
                    errorMsgs.add("第" + rowNum + "行：行业名称不能为空");
                    continue;
                }

                // 查找企业ID
                Long enterpriseId = enterpriseIdCache.get(dto.getEnterpriseName());
                if (enterpriseId == null) {
                    enterpriseId = enterpriseMapper.findIdByEnterpriseName(dto.getEnterpriseName());
                    if (enterpriseId == null) {
                        errorMsgs.add("第" + rowNum + "行：企业名称'" + dto.getEnterpriseName() + "'不存在");
                        continue;
                    }
                    enterpriseIdCache.put(dto.getEnterpriseName(), enterpriseId);
                }

                // 查找行业ID
                Long industryId = industryIdCache.get(dto.getIndustryName());
                if (industryId == null) {
                    LambdaQueryWrapper<com.haifeng.common.entity.industry.Industry> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(com.haifeng.common.entity.industry.Industry::getIndustryName, dto.getIndustryName())
                           .eq(com.haifeng.common.entity.industry.Industry::getIsDeleted, false);
                    com.haifeng.common.entity.industry.Industry industry = industryMapper.selectOne(wrapper);
                    if (industry == null) {
                        errorMsgs.add("第" + rowNum + "行：行业名称'" + dto.getIndustryName() + "'不存在");
                        continue;
                    }
                    industryId = industry.getId();
                    industryIdCache.put(dto.getIndustryName(), industryId);
                }

                // 检查文件内重复
                String relationKey = enterpriseId + "_" + industryId;
                if (relationKeysInFile.contains(relationKey)) {
                    errorMsgs.add("第" + rowNum + "行：企业'" + dto.getEnterpriseName() + "'-行业'" + dto.getIndustryName() + "'关联在文件中重复");
                    continue;
                }
                relationKeysInFile.add(relationKey);

                // 检查数据库中是否已存在
                if (enterpriseIndustryMapper.existsByEnterpriseIdAndIndustryId(enterpriseId, industryId)) {
                    errorMsgs.add("第" + rowNum + "行：企业'" + dto.getEnterpriseName() + "'-行业'" + dto.getIndustryName() + "'关联已存在");
                    continue;
                }

                EnterpriseIndustry entity = EnterpriseIndustry.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .enterpriseId(enterpriseId)
                        .enterpriseName(dto.getEnterpriseName())
                        .industryId(industryId)
                        .industryName(dto.getIndustryName())
                        .isPrimary(false)
                        .sortOrder((short) 0)
                        .createdAt(OffsetDateTime.now())
                        .build();

                toInsert.add(entity);
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            // 批量插入
            for (EnterpriseIndustry entity : toInsert) {
                enterpriseIndustryMapper.insert(entity);
            }

            log.info("导入企业-行业关联成功，数量={}", toInsert.size());

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/company/EnterpriseIndustryService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/company/EnterpriseIndustryServiceImpl.java
git commit -m "feat(service): add EnterpriseIndustryService with xlsx import"
```

---

## Task 9: Controller类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/company/EnterpriseController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/company/EnterpriseIndustryController.java`

- [ ] **Step 1: 创建EnterpriseController**

```java
package com.haifeng.admin.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.*;
import com.haifeng.admin.service.company.EnterpriseService;
import com.haifeng.admin.vo.company.EnterpriseDetailVO;
import com.haifeng.admin.vo.company.EnterpriseListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/company/enterprise")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    /**
     * 分页查询企业列表
     */
    @GetMapping("/list")
    public R<IPage<EnterpriseListVO>> list(@Valid EnterpriseQueryDTO dto) {
        return R.ok(enterpriseService.page(dto));
    }

    /**
     * 获取企业详情（含岗位列表）
     */
    @GetMapping("/{id}")
    public R<EnterpriseDetailVO> detail(@PathVariable Long id) {
        return R.ok(enterpriseService.detail(id));
    }

    /**
     * 新增企业
     */
    @PostMapping
    @OperationLog(module = "企业管理", action = "新增企业")
    public R<Long> add(@Valid @RequestBody EnterpriseAddDTO dto) {
        return R.ok(enterpriseService.add(dto));
    }

    /**
     * 修改企业
     */
    @PutMapping("/{id}")
    @OperationLog(module = "企业管理", action = "修改企业")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody EnterpriseUpdateDTO dto) {
        enterpriseService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改企业状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "企业管理", action = "修改企业状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody EnterpriseStatusDTO dto) {
        enterpriseService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 硬删除企业
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "企业管理", action = "硬删除企业")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除企业
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "企业管理", action = "批量硬删除企业")
    public R<Void> batchDelete(@Valid @RequestBody EnterpriseBatchDeleteDTO dto) {
        enterpriseService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入企业xlsx（Sheet0=企业, Sheet1=岗位）
     */
    @PostMapping("/import")
    @OperationLog(module = "企业管理", action = "导入企业")
    public R<Void> importEnterprises(@RequestParam("file") MultipartFile file) {
        enterpriseService.importEnterprises(file);
        return R.ok();
    }
}
```

- [ ] **Step 2: 创建EnterpriseIndustryController**

```java
package com.haifeng.admin.controller.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.company.EnterpriseIndustryBatchDeleteDTO;
import com.haifeng.admin.dto.company.EnterpriseIndustryQueryDTO;
import com.haifeng.admin.service.company.EnterpriseIndustryService;
import com.haifeng.admin.vo.company.EnterpriseIndustryDetailVO;
import com.haifeng.admin.vo.company.EnterpriseIndustryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/company/enterprise-industry")
@RequiredArgsConstructor
public class EnterpriseIndustryController {

    private final EnterpriseIndustryService enterpriseIndustryService;

    /**
     * 分页查询企业-行业关联列表
     */
    @GetMapping("/list")
    public R<IPage<EnterpriseIndustryListVO>> list(@Valid EnterpriseIndustryQueryDTO dto) {
        return R.ok(enterpriseIndustryService.page(dto));
    }

    /**
     * 获取关联详情
     */
    @GetMapping("/{id}")
    public R<EnterpriseIndustryDetailVO> detail(@PathVariable Long id) {
        return R.ok(enterpriseIndustryService.detail(id));
    }

    /**
     * 硬删除关联记录
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "企业-行业关联", action = "硬删除关联")
    public R<Void> delete(@PathVariable Long id) {
        enterpriseIndustryService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除关联记录
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "企业-行业关联", action = "批量硬删除关联")
    public R<Void> batchDelete(@Valid @RequestBody EnterpriseIndustryBatchDeleteDTO dto) {
        enterpriseIndustryService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入企业-行业关联xlsx
     */
    @PostMapping("/import")
    @OperationLog(module = "企业-行业关联", action = "导入关联")
    public R<Void> importEnterpriseIndustries(@RequestParam("file") MultipartFile file) {
        enterpriseIndustryService.importEnterpriseIndustries(file);
        return R.ok();
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/company/
git commit -m "feat(controller): add EnterpriseController and EnterpriseIndustryController"
```

---

## Task 10: 编译验证

**Files:**
- Verify: all new files compile correctly

- [ ] **Step 1: 编译项目**

运行: `cd /d/exeProject/ideaProjects/Project-HaiFeng && mvn compile -DskipTests`

预期: BUILD SUCCESS

- [ ] **Step 2: 如果编译失败，修复错误**

检查编译输出，修复任何导入或语法错误

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "feat(company): complete enterprise module implementation"
```

---

## 总结

| Task | 说明 | 文件数 |
|------|------|--------|
| 1 | Flyway迁移脚本 | 1 |
| 2 | Entity类 | 3 |
| 3 | Mapper接口 | 3 |
| 4 | Excel DTO | 3 |
| 5 | DTO类 | 7 |
| 6 | VO类 | 5 |
| 7 | EnterpriseService | 2 |
| 8 | EnterpriseIndustryService | 2 |
| 9 | Controller | 2 |
| 10 | 编译验证 | - |

**总计：28个新文件**
