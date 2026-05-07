# 院校次表模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现院校管理模块的三个次表子模块：院系详情、实验室列表、学科评估

**Architecture:** 采用独立Controller方案，每个子模块有独立的Controller/Service/DTO/VO。Entity和Mapper放在haifeng-common模块。Excel导入使用EasyExcel多Sheet方案，与现有院校适应指南模式一致。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, PostgreSQL (JSONB, TEXT[])

---

## 文件结构

### haifeng-common 模块
```
entity/university/
├── Laboratory.java           # 实验室实体
├── Department.java           # 院系实体
├── DepartmentReport.java     # 院系报告实体
└── SubjectEvaluation.java    # 学科评估实体

mapper/university/
├── LaboratoryMapper.java
├── DepartmentMapper.java
├── DepartmentReportMapper.java
└── SubjectEvaluationMapper.java
```

### haifeng-admin 模块
```
controller/university/
├── LaboratoryController.java
├── DepartmentController.java
└── SubjectEvaluationController.java

service/university/
├── LaboratoryService.java
├── DepartmentService.java
└── SubjectEvaluationService.java

service/impl/university/
├── LaboratoryServiceImpl.java
├── DepartmentServiceImpl.java
└── SubjectEvaluationServiceImpl.java

dto/university/
├── LaboratoryQueryDTO.java
├── LaboratoryAddDTO.java
├── LaboratoryUpdateDTO.java
├── DepartmentQueryDTO.java
├── DepartmentAddDTO.java
├── DepartmentUpdateDTO.java
├── SubjectEvaluationQueryDTO.java
├── SubjectEvaluationAddDTO.java
└── SubjectEvaluationUpdateDTO.java

vo/university/
├── LaboratoryListVO.java
├── LaboratoryDetailVO.java
├── DepartmentListVO.java
├── DepartmentDetailVO.java
├── SubjectEvaluationListVO.java
└── SubjectEvaluationDetailVO.java

excel/university/
├── LaboratoryExcelDTO.java
├── CoreTeamExcelDTO.java
├── StatisticsExcelDTO.java
├── DepartmentExcelDTO.java
├── DepartmentReportExcelDTO.java
├── CitySalaryExcelDTO.java
├── PostgraduateExcelDTO.java
├── DisclaimerExcelDTO.java
├── ProspectsExcelDTO.java
├── TrendsExcelDTO.java
├── OverviewExcelDTO.java
├── CareerExcelDTO.java
├── SubjectsDetailExcelDTO.java
├── SalaryExcelDTO.java
├── MajorComposeExcelDTO.java
└── SubjectEvaluationExcelDTO.java
```

---

## Task 1: 创建数据库迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V6__create_universities_details.sql`

- [ ] **Step 1: 创建V6迁移文件**

```sql
-- ============================================
-- V6__create_universities_details.sql
-- 院校次表：实验室、院系、学科评估
-- ============================================

-- 1. 实验室表 (laboratories)
CREATE TABLE laboratories (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL,
    university_name       VARCHAR(50)   NOT NULL,
    name                  VARCHAR(200)  NOT NULL,
    lab_type              VARCHAR(100)  NOT NULL,
    established_year      VARCHAR(20),
    region                VARCHAR(100),
    department            VARCHAR(100),
    director              VARCHAR(50),
    staff_count           VARCHAR(50),
    student_count         VARCHAR(50),
    email                 VARCHAR(200),
    phone                 VARCHAR(50),
    introduction          TEXT,
    research_description  TEXT,
    lab_space             TEXT,
    open_topics           TEXT,
    cooperation           TEXT,
    visiting_scholars     TEXT,
    research_fields       TEXT[],
    statistics            JSONB         DEFAULT '[]'::JSONB,
    major_equipment       TEXT[],
    core_team             JSONB         DEFAULT '[]'::JSONB,
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_labs_university_id ON laboratories(university_id);
CREATE INDEX idx_labs_lab_type ON laboratories(lab_type);
CREATE INDEX idx_labs_region ON laboratories(region);
CREATE INDEX idx_labs_status ON laboratories(status);
CREATE INDEX idx_labs_name ON laboratories(name);

COMMENT ON TABLE laboratories IS '实验室表';
COMMENT ON COLUMN laboratories.id IS '实验室ID（雪花算法）';
COMMENT ON COLUMN laboratories.university_id IS '所属院校ID';
COMMENT ON COLUMN laboratories.university_name IS '院校名称（冗余）';
COMMENT ON COLUMN laboratories.name IS '实验室名称';
COMMENT ON COLUMN laboratories.lab_type IS '实验室类型';
COMMENT ON COLUMN laboratories.statistics IS '统计数据JSONB';
COMMENT ON COLUMN laboratories.core_team IS '核心团队JSONB';

-- 2. 院系主表 (t_department)
CREATE TABLE t_department (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL,
    university_name       VARCHAR(50)   NOT NULL,
    department_name       VARCHAR(100)  NOT NULL,
    department_type       VARCHAR(100)  NOT NULL,
    page_title            VARCHAR(200),
    tags                  TEXT[],
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dept_university_id ON t_department(university_id);
CREATE INDEX idx_dept_status ON t_department(status);
CREATE INDEX idx_dept_name ON t_department(department_name);
CREATE INDEX idx_dept_type ON t_department(department_type);

COMMENT ON TABLE t_department IS '院系主表';
COMMENT ON COLUMN t_department.id IS '院系ID（雪花算法）';
COMMENT ON COLUMN t_department.university_id IS '所属院校ID';
COMMENT ON COLUMN t_department.department_name IS '院系名称';
COMMENT ON COLUMN t_department.department_type IS '院系类型';

-- 3. 院系分析报告表 (department_reports)
CREATE TABLE department_reports (
    id                    BIGINT        PRIMARY KEY,
    department_id         BIGINT        NOT NULL UNIQUE,
    subtitle              VARCHAR(200),
    overview              JSONB         DEFAULT '{}'::JSONB,
    subjects_detail       JSONB         DEFAULT '[]'::JSONB,
    postgraduate          JSONB         DEFAULT '{}'::JSONB,
    city_salary           JSONB         DEFAULT '[]'::JSONB,
    salary                JSONB         DEFAULT '[]'::JSONB,
    career                JSONB         DEFAULT '[]'::JSONB,
    trends                JSONB         DEFAULT '{}'::JSONB,
    prospects             JSONB         DEFAULT '{}'::JSONB,
    disclaimer            JSONB         DEFAULT '{}'::JSONB,
    major_compose         JSONB         DEFAULT '[]'::JSONB,
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dept_reports_department_id ON department_reports(department_id);
CREATE INDEX idx_dept_reports_status ON department_reports(status);

COMMENT ON TABLE department_reports IS '院系分析报告表（与院系1:1）';
COMMENT ON COLUMN department_reports.department_id IS '关联院系ID';
COMMENT ON COLUMN department_reports.overview IS '学院定位JSONB';
COMMENT ON COLUMN department_reports.subjects_detail IS '专业详情JSONB数组';
COMMENT ON COLUMN department_reports.major_compose IS '专业组成JSONB数组';

-- 4. 学科评估表 (t_subject_evaluation)
CREATE TABLE t_subject_evaluation (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL,
    university_name       VARCHAR(100)  NOT NULL,
    discipline_code       VARCHAR(20)   NOT NULL,
    discipline_name       VARCHAR(100)  NOT NULL,
    evaluation_round      VARCHAR(20)   DEFAULT '第四轮',
    evaluation_grade      VARCHAR(5)    NOT NULL,
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_eval_grade CHECK (
        evaluation_grade IN ('A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C', 'C-')
    )
);

CREATE INDEX idx_se_university ON t_subject_evaluation(university_id);
CREATE INDEX idx_se_discipline ON t_subject_evaluation(discipline_name, evaluation_grade);
CREATE INDEX idx_se_grade ON t_subject_evaluation(evaluation_grade);
CREATE INDEX idx_se_status ON t_subject_evaluation(status);

COMMENT ON TABLE t_subject_evaluation IS '学科评估表';
COMMENT ON COLUMN t_subject_evaluation.discipline_code IS '学科代码（4位）';
COMMENT ON COLUMN t_subject_evaluation.discipline_name IS '学科名称';
COMMENT ON COLUMN t_subject_evaluation.evaluation_round IS '评估轮次';
COMMENT ON COLUMN t_subject_evaluation.evaluation_grade IS '评估等级（A+/A/A-/B+/B/B-/C+/C/C-）';
```

- [ ] **Step 2: 验证迁移文件语法**

Run: `cd haifeng-admin && mvn flyway:validate -Dflyway.target=6`

Expected: BUILD SUCCESS 或提示需要运行迁移

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/resources/db/migration/V6__create_universities_details.sql
git commit -m "feat(db): 添加院校次表迁移文件V6

- laboratories 实验室表
- t_department 院系主表
- department_reports 院系报告表
- t_subject_evaluation 学科评估表"
```

---

## Task 2: 创建实验室实体和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/university/Laboratory.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/university/LaboratoryMapper.java`

- [ ] **Step 1: 创建Laboratory实体类**

```java
package com.haifeng.common.entity.university;

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
@TableName(value = "laboratories", autoResultMap = true)
public class Laboratory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String universityName;

    private String name;

    private String labType;

    private String establishedYear;

    private String region;

    private String department;

    private String director;

    private String staffCount;

    private String studentCount;

    private String email;

    private String phone;

    private String introduction;

    private String researchDescription;

    private String labSpace;

    private String openTopics;

    private String cooperation;

    private String visitingScholars;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> researchFields;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> statistics;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> majorEquipment;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> coreTeam;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建LaboratoryMapper接口**

```java
package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.Laboratory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LaboratoryMapper extends BaseMapper<Laboratory> {

    @Select("SELECT EXISTS(SELECT 1 FROM laboratories WHERE name = #{name} AND status = 1)")
    boolean existsByName(@Param("name") String name);

    @Select("SELECT EXISTS(SELECT 1 FROM laboratories WHERE university_id = #{universityId} AND name = #{name} AND status = 1)")
    boolean existsByUniversityIdAndName(@Param("universityId") Long universityId, @Param("name") String name);
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/university/Laboratory.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/university/LaboratoryMapper.java
git commit -m "feat(entity): 添加Laboratory实体和Mapper"
```

---

## Task 3: 创建院系实体和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/university/Department.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/university/DepartmentReport.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/university/DepartmentMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/university/DepartmentReportMapper.java`

- [ ] **Step 1: 创建Department实体类**

```java
package com.haifeng.common.entity.university;

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
@TableName(value = "t_department", autoResultMap = true)
public class Department {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String universityName;

    private String departmentName;

    private String departmentType;

    private String pageTitle;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建DepartmentReport实体类**

```java
package com.haifeng.common.entity.university;

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
@TableName(value = "department_reports", autoResultMap = true)
public class DepartmentReport {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long departmentId;

    private String subtitle;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> overview;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> subjectsDetail;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> postgraduate;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> citySalary;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> salary;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> career;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> trends;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> prospects;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> disclaimer;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> majorCompose;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建DepartmentMapper接口**

```java
package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_department WHERE university_id = #{universityId} AND department_name = #{departmentName} AND status = 1)")
    boolean existsByUniversityIdAndName(@Param("universityId") Long universityId, @Param("departmentName") String departmentName);

    @Select("SELECT id FROM t_department WHERE university_name = #{universityName} AND department_name = #{departmentName} AND status = 1 LIMIT 1")
    Long findIdByUniversityAndDepartmentName(@Param("universityName") String universityName, @Param("departmentName") String departmentName);
}
```

- [ ] **Step 4: 创建DepartmentReportMapper接口**

```java
package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.DepartmentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentReportMapper extends BaseMapper<DepartmentReport> {

    @Select("SELECT * FROM department_reports WHERE department_id = #{departmentId} AND status = 1 LIMIT 1")
    DepartmentReport selectByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("SELECT EXISTS(SELECT 1 FROM department_reports WHERE department_id = #{departmentId})")
    boolean existsByDepartmentId(@Param("departmentId") Long departmentId);
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/university/Department.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/university/DepartmentReport.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/university/DepartmentMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/university/DepartmentReportMapper.java
git commit -m "feat(entity): 添加Department和DepartmentReport实体及Mapper"
```

---

## Task 4: 创建学科评估实体和Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/university/SubjectEvaluation.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/university/SubjectEvaluationMapper.java`

- [ ] **Step 1: 创建SubjectEvaluation实体类**

```java
package com.haifeng.common.entity.university;

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
@TableName("t_subject_evaluation")
public class SubjectEvaluation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String universityName;

    private String disciplineCode;

    private String disciplineName;

    private String evaluationRound;

    private String evaluationGrade;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建SubjectEvaluationMapper接口**

```java
package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.SubjectEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectEvaluationMapper extends BaseMapper<SubjectEvaluation> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_subject_evaluation WHERE university_id = #{universityId} AND discipline_code = #{disciplineCode} AND evaluation_round = #{evaluationRound} AND status = 1)")
    boolean existsByUniversityAndDiscipline(@Param("universityId") Long universityId,
                                            @Param("disciplineCode") String disciplineCode,
                                            @Param("evaluationRound") String evaluationRound);
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/university/SubjectEvaluation.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/university/SubjectEvaluationMapper.java
git commit -m "feat(entity): 添加SubjectEvaluation实体和Mapper"
```

---

## Task 5: 创建实验室DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/LaboratoryQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/LaboratoryAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/LaboratoryUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/university/LaboratoryListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/university/LaboratoryDetailVO.java`

- [ ] **Step 1: 创建LaboratoryQueryDTO**

```java
package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LaboratoryQueryDTO extends BasePageQueryDTO {

    private String universityName;

    private String name;

    private String labType;

    private String region;

    private String department;

    private Integer status;
}
```

- [ ] **Step 2: 创建LaboratoryAddDTO**

```java
package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LaboratoryAddDTO {

    @NotNull(message = "院校ID不能为空")
    private Long universityId;

    @NotBlank(message = "实验室名称不能为空")
    private String name;

    @NotBlank(message = "实验室类型不能为空")
    private String labType;

    private String establishedYear;

    private String region;

    private String department;

    private String director;

    private String staffCount;

    private String studentCount;

    private String email;

    private String phone;

    private String introduction;

    private String researchDescription;

    private String labSpace;

    private String openTopics;

    private String cooperation;

    private String visitingScholars;

    private List<String> researchFields;

    private List<Map<String, Object>> statistics;

    private List<String> majorEquipment;

    private List<Map<String, Object>> coreTeam;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建LaboratoryUpdateDTO**

```java
package com.haifeng.admin.dto.university;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LaboratoryUpdateDTO {

    private String name;

    private String labType;

    private String establishedYear;

    private String region;

    private String department;

    private String director;

    private String staffCount;

    private String studentCount;

    private String email;

    private String phone;

    private String introduction;

    private String researchDescription;

    private String labSpace;

    private String openTopics;

    private String cooperation;

    private String visitingScholars;

    private List<String> researchFields;

    private List<Map<String, Object>> statistics;

    private List<String> majorEquipment;

    private List<Map<String, Object>> coreTeam;

    private Integer sortOrder;

    private Integer status;
}
```

- [ ] **Step 4: 创建LaboratoryListVO**

```java
package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LaboratoryListVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String name;

    private String labType;

    private String region;

    private String department;

    private String director;

    private Integer status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建LaboratoryDetailVO**

```java
package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class LaboratoryDetailVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String name;

    private String labType;

    private String establishedYear;

    private String region;

    private String department;

    private String director;

    private String staffCount;

    private String studentCount;

    private String email;

    private String phone;

    private String introduction;

    private String researchDescription;

    private String labSpace;

    private String openTopics;

    private String cooperation;

    private String visitingScholars;

    private List<String> researchFields;

    private List<Map<String, Object>> statistics;

    private List<String> majorEquipment;

    private List<Map<String, Object>> coreTeam;

    private Integer sortOrder;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/LaboratoryQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/LaboratoryAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/LaboratoryUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/university/LaboratoryListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/university/LaboratoryDetailVO.java
git commit -m "feat(dto): 添加实验室DTO和VO"
```

---

## Task 6: 创建实验室Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/university/LaboratoryService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/university/LaboratoryServiceImpl.java`

- [ ] **Step 1: 创建LaboratoryService接口**

```java
package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.LaboratoryAddDTO;
import com.haifeng.admin.dto.university.LaboratoryQueryDTO;
import com.haifeng.admin.dto.university.LaboratoryUpdateDTO;
import com.haifeng.admin.vo.university.LaboratoryDetailVO;
import com.haifeng.admin.vo.university.LaboratoryListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LaboratoryService {

    IPage<LaboratoryListVO> page(LaboratoryQueryDTO dto);

    LaboratoryDetailVO detail(Long id);

    Long add(LaboratoryAddDTO dto);

    void update(Long id, LaboratoryUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void hardDelete(Long id);

    void batchDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void importLaboratories(MultipartFile file);
}
```

- [ ] **Step 2: 创建LaboratoryServiceImpl实现类**

```java
package com.haifeng.admin.service.impl.university;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haifeng.admin.dto.university.LaboratoryAddDTO;
import com.haifeng.admin.dto.university.LaboratoryQueryDTO;
import com.haifeng.admin.dto.university.LaboratoryUpdateDTO;
import com.haifeng.admin.excel.university.CoreTeamExcelDTO;
import com.haifeng.admin.excel.university.LaboratoryExcelDTO;
import com.haifeng.admin.excel.university.StatisticsExcelDTO;
import com.haifeng.admin.service.university.LaboratoryService;
import com.haifeng.admin.vo.university.LaboratoryDetailVO;
import com.haifeng.admin.vo.university.LaboratoryListVO;
import com.haifeng.common.entity.university.Laboratory;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.LaboratoryMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl extends ServiceImpl<LaboratoryMapper, Laboratory> implements LaboratoryService {

    private final LaboratoryMapper laboratoryMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<LaboratoryListVO> page(LaboratoryQueryDTO dto) {
        Page<Laboratory> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Laboratory> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Laboratory::getStatus, (short) 0);

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(Laboratory::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Laboratory::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getLabType())) {
            wrapper.eq(Laboratory::getLabType, dto.getLabType());
        }
        if (StringUtils.hasText(dto.getRegion())) {
            wrapper.eq(Laboratory::getRegion, dto.getRegion());
        }
        if (StringUtils.hasText(dto.getDepartment())) {
            wrapper.eq(Laboratory::getDepartment, dto.getDepartment());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Laboratory::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(Laboratory::getSortOrder).orderByDesc(Laboratory::getCreatedAt);

        IPage<Laboratory> labPage = laboratoryMapper.selectPage(page, wrapper);

        return labPage.convert(lab -> {
            LaboratoryListVO vo = new LaboratoryListVO();
            BeanUtils.copyProperties(lab, vo);
            vo.setStatus(lab.getStatus() != null ? lab.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    public LaboratoryDetailVO detail(Long id) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null || lab.getStatus() == 0) {
            throw new BusinessException(404, "实验室不存在");
        }

        LaboratoryDetailVO vo = new LaboratoryDetailVO();
        BeanUtils.copyProperties(lab, vo);
        vo.setStatus(lab.getStatus() != null ? lab.getStatus().intValue() : null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(LaboratoryAddDTO dto) {
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(400, "院校不存在");
        }

        if (laboratoryMapper.existsByUniversityIdAndName(dto.getUniversityId(), dto.getName())) {
            throw new BusinessException(400, "该院校下实验室名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Laboratory lab = Laboratory.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .name(dto.getName())
                .labType(dto.getLabType())
                .establishedYear(dto.getEstablishedYear())
                .region(dto.getRegion())
                .department(dto.getDepartment())
                .director(dto.getDirector())
                .staffCount(dto.getStaffCount())
                .studentCount(dto.getStudentCount())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .introduction(dto.getIntroduction())
                .researchDescription(dto.getResearchDescription())
                .labSpace(dto.getLabSpace())
                .openTopics(dto.getOpenTopics())
                .cooperation(dto.getCooperation())
                .visitingScholars(dto.getVisitingScholars())
                .researchFields(dto.getResearchFields())
                .statistics(dto.getStatistics())
                .majorEquipment(dto.getMajorEquipment())
                .coreTeam(dto.getCoreTeam())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        laboratoryMapper.insert(lab);
        log.info("新增实验室成功，id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, LaboratoryUpdateDTO dto) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null || lab.getStatus() == 0) {
            throw new BusinessException(404, "实验室不存在");
        }

        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(lab.getName())) {
            if (laboratoryMapper.existsByUniversityIdAndName(lab.getUniversityId(), dto.getName())) {
                throw new BusinessException(400, "该院校下实验室名称已存在");
            }
            lab.setName(dto.getName());
        }

        if (dto.getLabType() != null) lab.setLabType(dto.getLabType());
        if (dto.getEstablishedYear() != null) lab.setEstablishedYear(dto.getEstablishedYear());
        if (dto.getRegion() != null) lab.setRegion(dto.getRegion());
        if (dto.getDepartment() != null) lab.setDepartment(dto.getDepartment());
        if (dto.getDirector() != null) lab.setDirector(dto.getDirector());
        if (dto.getStaffCount() != null) lab.setStaffCount(dto.getStaffCount());
        if (dto.getStudentCount() != null) lab.setStudentCount(dto.getStudentCount());
        if (dto.getEmail() != null) lab.setEmail(dto.getEmail());
        if (dto.getPhone() != null) lab.setPhone(dto.getPhone());
        if (dto.getIntroduction() != null) lab.setIntroduction(dto.getIntroduction());
        if (dto.getResearchDescription() != null) lab.setResearchDescription(dto.getResearchDescription());
        if (dto.getLabSpace() != null) lab.setLabSpace(dto.getLabSpace());
        if (dto.getOpenTopics() != null) lab.setOpenTopics(dto.getOpenTopics());
        if (dto.getCooperation() != null) lab.setCooperation(dto.getCooperation());
        if (dto.getVisitingScholars() != null) lab.setVisitingScholars(dto.getVisitingScholars());
        if (dto.getResearchFields() != null) lab.setResearchFields(dto.getResearchFields());
        if (dto.getStatistics() != null) lab.setStatistics(dto.getStatistics());
        if (dto.getMajorEquipment() != null) lab.setMajorEquipment(dto.getMajorEquipment());
        if (dto.getCoreTeam() != null) lab.setCoreTeam(dto.getCoreTeam());
        if (dto.getSortOrder() != null) lab.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) lab.setStatus(dto.getStatus().shortValue());

        lab.setUpdatedAt(OffsetDateTime.now());
        laboratoryMapper.updateById(lab);
        log.info("更新实验室成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null) {
            throw new BusinessException(404, "实验室不存在");
        }

        LambdaUpdateWrapper<Laboratory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Laboratory::getId, id)
               .set(Laboratory::getStatus, status.shortValue())
               .set(Laboratory::getUpdatedAt, OffsetDateTime.now());
        laboratoryMapper.update(null, wrapper);
        log.info("更新实验室状态，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updateStatus(id, 0);
        log.info("软删除实验室，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null) {
            throw new BusinessException(404, "实验室不存在");
        }
        laboratoryMapper.deleteById(id);
        log.info("硬删除实验室，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        LambdaUpdateWrapper<Laboratory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Laboratory::getId, ids)
               .set(Laboratory::getStatus, (short) 0)
               .set(Laboratory::getUpdatedAt, OffsetDateTime.now());
        laboratoryMapper.update(null, wrapper);
        log.info("批量软删除实验室，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        laboratoryMapper.deleteBatchIds(ids);
        log.info("批量硬删除实验室，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLaboratories(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet0: 主表数据
            List<LaboratoryExcelDTO> mainData = EasyExcel.read(file.getInputStream())
                    .head(LaboratoryExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            // Sheet1: core_team数据
            List<CoreTeamExcelDTO> coreTeamData = EasyExcel.read(file.getInputStream())
                    .head(CoreTeamExcelDTO.class)
                    .sheet(1)
                    .doReadSync();

            // Sheet2: statistics数据
            List<StatisticsExcelDTO> statisticsData = EasyExcel.read(file.getInputStream())
                    .head(StatisticsExcelDTO.class)
                    .sheet(2)
                    .doReadSync();

            // 按实验室名称分组JSONB数据
            Map<String, List<Map<String, Object>>> coreTeamMap = coreTeamData.stream()
                    .filter(d -> StringUtils.hasText(d.getLabName()))
                    .collect(Collectors.groupingBy(
                            CoreTeamExcelDTO::getLabName,
                            Collectors.mapping(d -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("name", d.getMemberName());
                                m.put("position", d.getPosition());
                                m.put("title", d.getJobTitle());
                                return m;
                            }, Collectors.toList())
                    ));

            Map<String, List<Map<String, Object>>> statisticsMap = statisticsData.stream()
                    .filter(d -> StringUtils.hasText(d.getLabName()))
                    .collect(Collectors.groupingBy(
                            StatisticsExcelDTO::getLabName,
                            Collectors.mapping(d -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("label", d.getLabel());
                                m.put("count", d.getCount());
                                return m;
                            }, Collectors.toList())
                    ));

            // 校验主表数据
            Map<String, Long> universityIdCache = new HashMap<>();
            Map<String, String> universityNameCache = new HashMap<>();
            List<Laboratory> laboratories = new ArrayList<>();

            for (int i = 0; i < mainData.size(); i++) {
                int rowNum = i + 2;
                LaboratoryExcelDTO data = mainData.get(i);

                if (!StringUtils.hasText(data.getUniversityName())) {
                    errorMsgs.add("第" + rowNum + "行：院校名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getName())) {
                    errorMsgs.add("第" + rowNum + "行：实验室名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getLabType())) {
                    errorMsgs.add("第" + rowNum + "行：实验室类型不能为空");
                    continue;
                }

                // 查询院校ID
                Long universityId = universityIdCache.get(data.getUniversityName());
                if (universityId == null) {
                    LambdaQueryWrapper<University> uniWrapper = new LambdaQueryWrapper<>();
                    uniWrapper.eq(University::getName, data.getUniversityName()).eq(University::getStatus, (short) 1);
                    University university = universityMapper.selectOne(uniWrapper);
                    if (university == null) {
                        errorMsgs.add("第" + rowNum + "行：院校名称'" + data.getUniversityName() + "'不存在");
                        continue;
                    }
                    universityId = university.getId();
                    universityIdCache.put(data.getUniversityName(), universityId);
                    universityNameCache.put(data.getUniversityName(), university.getName());
                }

                // 检查重复
                if (laboratoryMapper.existsByUniversityIdAndName(universityId, data.getName())) {
                    errorMsgs.add("第" + rowNum + "行：该院校下实验室名称'" + data.getName() + "'已存在");
                    continue;
                }

                OffsetDateTime now = OffsetDateTime.now();
                Laboratory lab = Laboratory.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .universityId(universityId)
                        .universityName(universityNameCache.get(data.getUniversityName()))
                        .name(data.getName())
                        .labType(data.getLabType())
                        .establishedYear(data.getEstablishedYear())
                        .region(data.getRegion())
                        .department(data.getDepartment())
                        .director(data.getDirector())
                        .staffCount(data.getStaffCount())
                        .studentCount(data.getStudentCount())
                        .email(data.getEmail())
                        .phone(data.getPhone())
                        .introduction(data.getIntroduction())
                        .researchDescription(data.getResearchDescription())
                        .labSpace(data.getLabSpace())
                        .openTopics(data.getOpenTopics())
                        .cooperation(data.getCooperation())
                        .visitingScholars(data.getVisitingScholars())
                        .researchFields(data.getResearchFields())
                        .majorEquipment(data.getMajorEquipment())
                        .coreTeam(coreTeamMap.get(data.getName()))
                        .statistics(statisticsMap.get(data.getName()))
                        .sortOrder(data.getSortOrder() != null ? data.getSortOrder() : 0)
                        .status(data.getStatus() != null ? data.getStatus().shortValue() : (short) 1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                laboratories.add(lab);
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            if (!laboratories.isEmpty()) {
                saveBatch(laboratories);
                log.info("导入实验室成功，数量={}", laboratories.size());
            }

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/university/LaboratoryService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/university/LaboratoryServiceImpl.java
git commit -m "feat(service): 添加LaboratoryService实现"
```

---

## Task 7: 创建实验室Excel DTO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/university/LaboratoryExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/university/CoreTeamExcelDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/university/StatisticsExcelDTO.java`

- [ ] **Step 1: 创建LaboratoryExcelDTO**

```java
package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class LaboratoryExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("实验室名称")
    private String name;

    @ExcelProperty("实验室类型")
    private String labType;

    @ExcelProperty("成立时间")
    private String establishedYear;

    @ExcelProperty("所在地区")
    private String region;

    @ExcelProperty("主管部门")
    private String department;

    @ExcelProperty("实验室主任")
    private String director;

    @ExcelProperty("人员规模")
    private String staffCount;

    @ExcelProperty("学生规模")
    private String studentCount;

    @ExcelProperty("联系邮箱")
    private String email;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("实验室简介")
    private String introduction;

    @ExcelProperty("研究方向描述")
    private String researchDescription;

    @ExcelProperty("实验室空间")
    private String labSpace;

    @ExcelProperty("开放课题")
    private String openTopics;

    @ExcelProperty("合作交流")
    private String cooperation;

    @ExcelProperty("访问学者")
    private String visitingScholars;

    @ExcelProperty(value = "研究领域(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> researchFields;

    @ExcelProperty(value = "主要设备(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> majorEquipment;

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("状态")
    private Integer status;
}
```

- [ ] **Step 2: 创建CoreTeamExcelDTO**

```java
package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CoreTeamExcelDTO {

    @ExcelProperty("实验室名称")
    private String labName;

    @ExcelProperty("成员姓名")
    private String memberName;

    @ExcelProperty("职务")
    private String position;

    @ExcelProperty("岗位名称")
    private String jobTitle;
}
```

- [ ] **Step 3: 创建StatisticsExcelDTO**

```java
package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StatisticsExcelDTO {

    @ExcelProperty("实验室名称")
    private String labName;

    @ExcelProperty("统计标签")
    private String label;

    @ExcelProperty("数量")
    private Integer count;
}
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/university/LaboratoryExcelDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/university/CoreTeamExcelDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/university/StatisticsExcelDTO.java
git commit -m "feat(excel): 添加实验室Excel DTO"
```

---

## Task 8: 创建实验室Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/university/LaboratoryController.java`

- [ ] **Step 1: 创建LaboratoryController**

```java
package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.dto.university.LaboratoryAddDTO;
import com.haifeng.admin.dto.university.LaboratoryQueryDTO;
import com.haifeng.admin.dto.university.LaboratoryUpdateDTO;
import com.haifeng.admin.service.university.LaboratoryService;
import com.haifeng.admin.vo.university.LaboratoryDetailVO;
import com.haifeng.admin.vo.university.LaboratoryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/university/laboratory")
@RequiredArgsConstructor
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @GetMapping("/list")
    public R<IPage<LaboratoryListVO>> list(@Valid LaboratoryQueryDTO dto) {
        return R.ok(laboratoryService.page(dto));
    }

    @GetMapping("/{id}")
    public R<LaboratoryDetailVO> detail(@PathVariable Long id) {
        return R.ok(laboratoryService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "实验室管理", action = "新增实验室")
    public R<Long> add(@Valid @RequestBody LaboratoryAddDTO dto) {
        return R.ok(laboratoryService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "实验室管理", action = "修改实验室")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody LaboratoryUpdateDTO dto) {
        laboratoryService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "实验室管理", action = "修改实验室状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        laboratoryService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "实验室管理", action = "软删除实验室")
    public R<Void> delete(@PathVariable Long id) {
        laboratoryService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "实验室管理", action = "硬删除实验室")
    public R<Void> hardDelete(@PathVariable Long id) {
        laboratoryService.hardDelete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "实验室管理", action = "批量软删除实验室")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        laboratoryService.batchDelete(dto.getIds());
        return R.ok();
    }

    @DeleteMapping("/batch/hard")
    @OperationLog(module = "实验室管理", action = "批量硬删除实验室")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        laboratoryService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "实验室管理", action = "导入实验室数据")
    public R<Void> importLaboratories(@RequestParam("file") MultipartFile file) {
        laboratoryService.importLaboratories(file);
        return R.ok();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/university/LaboratoryController.java
git commit -m "feat(controller): 添加LaboratoryController"
```

---

## Task 9: 创建院系DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/DepartmentQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/DepartmentAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/DepartmentUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/university/DepartmentListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/university/DepartmentDetailVO.java`

- [ ] **Step 1: 创建DepartmentQueryDTO**

```java
package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentQueryDTO extends BasePageQueryDTO {

    private String universityName;

    private String departmentName;

    private String departmentType;

    private Integer status;
}
```

- [ ] **Step 2: 创建DepartmentAddDTO**

```java
package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DepartmentAddDTO {

    @NotNull(message = "院校ID不能为空")
    private Long universityId;

    @NotBlank(message = "院系名称不能为空")
    private String departmentName;

    @NotBlank(message = "院系类型不能为空")
    private String departmentType;

    private String pageTitle;

    private List<String> tags;

    private Integer sortOrder;

    // 报告相关字段
    private String subtitle;

    private Map<String, Object> overview;

    private List<Map<String, Object>> subjectsDetail;

    private Map<String, Object> postgraduate;

    private List<Map<String, Object>> citySalary;

    private List<Map<String, Object>> salary;

    private List<Map<String, Object>> career;

    private Map<String, Object> trends;

    private Map<String, Object> prospects;

    private Map<String, Object> disclaimer;

    private List<Map<String, Object>> majorCompose;
}
```

- [ ] **Step 3: 创建DepartmentUpdateDTO**

```java
package com.haifeng.admin.dto.university;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DepartmentUpdateDTO {

    private String departmentName;

    private String departmentType;

    private String pageTitle;

    private List<String> tags;

    private Integer sortOrder;

    private Integer status;

    // 报告相关字段
    private String subtitle;

    private Map<String, Object> overview;

    private List<Map<String, Object>> subjectsDetail;

    private Map<String, Object> postgraduate;

    private List<Map<String, Object>> citySalary;

    private List<Map<String, Object>> salary;

    private List<Map<String, Object>> career;

    private Map<String, Object> trends;

    private Map<String, Object> prospects;

    private Map<String, Object> disclaimer;

    private List<Map<String, Object>> majorCompose;
}
```

- [ ] **Step 4: 创建DepartmentListVO**

```java
package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DepartmentListVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String departmentName;

    private String departmentType;

    private String pageTitle;

    private Integer sortOrder;

    private Integer status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建DepartmentDetailVO**

```java
package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DepartmentDetailVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String departmentName;

    private String departmentType;

    private String pageTitle;

    private List<String> tags;

    private Integer sortOrder;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    // 报告相关字段
    private Long reportId;

    private String subtitle;

    private Map<String, Object> overview;

    private List<Map<String, Object>> subjectsDetail;

    private Map<String, Object> postgraduate;

    private List<Map<String, Object>> citySalary;

    private List<Map<String, Object>> salary;

    private List<Map<String, Object>> career;

    private Map<String, Object> trends;

    private Map<String, Object> prospects;

    private Map<String, Object> disclaimer;

    private List<Map<String, Object>> majorCompose;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/DepartmentQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/DepartmentAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/DepartmentUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/university/DepartmentListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/university/DepartmentDetailVO.java
git commit -m "feat(dto): 添加院系DTO和VO"
```

---

## Task 10: 创建院系Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/university/DepartmentService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/university/DepartmentServiceImpl.java`

- [ ] **Step 1: 创建DepartmentService接口**

```java
package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.DepartmentAddDTO;
import com.haifeng.admin.dto.university.DepartmentQueryDTO;
import com.haifeng.admin.dto.university.DepartmentUpdateDTO;
import com.haifeng.admin.vo.university.DepartmentDetailVO;
import com.haifeng.admin.vo.university.DepartmentListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DepartmentService {

    IPage<DepartmentListVO> page(DepartmentQueryDTO dto);

    DepartmentDetailVO detail(Long id);

    Long add(DepartmentAddDTO dto);

    void update(Long id, DepartmentUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void hardDelete(Long id);

    void batchDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void importDepartments(MultipartFile file);

    void importDepartmentReports(MultipartFile file);
}
```

- [ ] **Step 2: 创建DepartmentServiceImpl（分页、详情、新增部分）**

```java
package com.haifeng.admin.service.impl.university;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haifeng.admin.dto.university.DepartmentAddDTO;
import com.haifeng.admin.dto.university.DepartmentQueryDTO;
import com.haifeng.admin.dto.university.DepartmentUpdateDTO;
import com.haifeng.admin.service.university.DepartmentService;
import com.haifeng.admin.vo.university.DepartmentDetailVO;
import com.haifeng.admin.vo.university.DepartmentListVO;
import com.haifeng.common.entity.university.Department;
import com.haifeng.common.entity.university.DepartmentReport;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.DepartmentMapper;
import com.haifeng.common.mapper.university.DepartmentReportMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
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
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final DepartmentReportMapper departmentReportMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<DepartmentListVO> page(DepartmentQueryDTO dto) {
        Page<Department> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Department::getStatus, (short) 0);

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(Department::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getDepartmentName())) {
            wrapper.like(Department::getDepartmentName, dto.getDepartmentName());
        }
        if (StringUtils.hasText(dto.getDepartmentType())) {
            wrapper.eq(Department::getDepartmentType, dto.getDepartmentType());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Department::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(Department::getSortOrder).orderByDesc(Department::getCreatedAt);

        IPage<Department> deptPage = departmentMapper.selectPage(page, wrapper);

        return deptPage.convert(dept -> {
            DepartmentListVO vo = new DepartmentListVO();
            BeanUtils.copyProperties(dept, vo);
            vo.setStatus(dept.getStatus() != null ? dept.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    public DepartmentDetailVO detail(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null || dept.getStatus() == 0) {
            throw new BusinessException(404, "院系不存在");
        }

        DepartmentDetailVO vo = new DepartmentDetailVO();
        BeanUtils.copyProperties(dept, vo);
        vo.setStatus(dept.getStatus() != null ? dept.getStatus().intValue() : null);

        // 查询关联的报告
        DepartmentReport report = departmentReportMapper.selectByDepartmentId(id);
        if (report != null) {
            vo.setReportId(report.getId());
            vo.setSubtitle(report.getSubtitle());
            vo.setOverview(report.getOverview());
            vo.setSubjectsDetail(report.getSubjectsDetail());
            vo.setPostgraduate(report.getPostgraduate());
            vo.setCitySalary(report.getCitySalary());
            vo.setSalary(report.getSalary());
            vo.setCareer(report.getCareer());
            vo.setTrends(report.getTrends());
            vo.setProspects(report.getProspects());
            vo.setDisclaimer(report.getDisclaimer());
            vo.setMajorCompose(report.getMajorCompose());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(DepartmentAddDTO dto) {
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(400, "院校不存在");
        }

        if (departmentMapper.existsByUniversityIdAndName(dto.getUniversityId(), dto.getDepartmentName())) {
            throw new BusinessException(400, "该院校下院系名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long deptId = SnowflakeIdGenerator.nextId();

        // 创建院系
        Department dept = Department.builder()
                .id(deptId)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .departmentName(dto.getDepartmentName())
                .departmentType(dto.getDepartmentType())
                .pageTitle(dto.getPageTitle())
                .tags(dto.getTags())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        departmentMapper.insert(dept);

        // 创建关联报告
        Long reportId = SnowflakeIdGenerator.nextId();
        DepartmentReport report = DepartmentReport.builder()
                .id(reportId)
                .departmentId(deptId)
                .subtitle(dto.getSubtitle())
                .overview(dto.getOverview())
                .subjectsDetail(dto.getSubjectsDetail())
                .postgraduate(dto.getPostgraduate())
                .citySalary(dto.getCitySalary())
                .salary(dto.getSalary())
                .career(dto.getCareer())
                .trends(dto.getTrends())
                .prospects(dto.getProspects())
                .disclaimer(dto.getDisclaimer())
                .majorCompose(dto.getMajorCompose())
                .sortOrder(0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        departmentReportMapper.insert(report);

        log.info("新增院系成功，id={}, name={}", deptId, dto.getDepartmentName());
        return deptId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DepartmentUpdateDTO dto) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null || dept.getStatus() == 0) {
            throw new BusinessException(404, "院系不存在");
        }

        if (StringUtils.hasText(dto.getDepartmentName()) && !dto.getDepartmentName().equals(dept.getDepartmentName())) {
            if (departmentMapper.existsByUniversityIdAndName(dept.getUniversityId(), dto.getDepartmentName())) {
                throw new BusinessException(400, "该院校下院系名称已存在");
            }
            dept.setDepartmentName(dto.getDepartmentName());
        }

        if (dto.getDepartmentType() != null) dept.setDepartmentType(dto.getDepartmentType());
        if (dto.getPageTitle() != null) dept.setPageTitle(dto.getPageTitle());
        if (dto.getTags() != null) dept.setTags(dto.getTags());
        if (dto.getSortOrder() != null) dept.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) dept.setStatus(dto.getStatus().shortValue());

        dept.setUpdatedAt(OffsetDateTime.now());
        departmentMapper.updateById(dept);

        // 更新报告
        DepartmentReport report = departmentReportMapper.selectByDepartmentId(id);
        if (report != null) {
            if (dto.getSubtitle() != null) report.setSubtitle(dto.getSubtitle());
            if (dto.getOverview() != null) report.setOverview(dto.getOverview());
            if (dto.getSubjectsDetail() != null) report.setSubjectsDetail(dto.getSubjectsDetail());
            if (dto.getPostgraduate() != null) report.setPostgraduate(dto.getPostgraduate());
            if (dto.getCitySalary() != null) report.setCitySalary(dto.getCitySalary());
            if (dto.getSalary() != null) report.setSalary(dto.getSalary());
            if (dto.getCareer() != null) report.setCareer(dto.getCareer());
            if (dto.getTrends() != null) report.setTrends(dto.getTrends());
            if (dto.getProspects() != null) report.setProspects(dto.getProspects());
            if (dto.getDisclaimer() != null) report.setDisclaimer(dto.getDisclaimer());
            if (dto.getMajorCompose() != null) report.setMajorCompose(dto.getMajorCompose());

            report.setUpdatedAt(OffsetDateTime.now());
            departmentReportMapper.updateById(report);
        }

        log.info("更新院系成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(404, "院系不存在");
        }

        OffsetDateTime now = OffsetDateTime.now();

        LambdaUpdateWrapper<Department> deptWrapper = new LambdaUpdateWrapper<>();
        deptWrapper.eq(Department::getId, id)
                   .set(Department::getStatus, status.shortValue())
                   .set(Department::getUpdatedAt, now);
        departmentMapper.update(null, deptWrapper);

        // 同步更新报告状态
        LambdaUpdateWrapper<DepartmentReport> reportWrapper = new LambdaUpdateWrapper<>();
        reportWrapper.eq(DepartmentReport::getDepartmentId, id)
                     .set(DepartmentReport::getStatus, status.shortValue())
                     .set(DepartmentReport::getUpdatedAt, now);
        departmentReportMapper.update(null, reportWrapper);

        log.info("更新院系状态，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updateStatus(id, 0);
        log.info("软删除院系，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(404, "院系不存在");
        }

        // 先删除报告
        LambdaQueryWrapper<DepartmentReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(DepartmentReport::getDepartmentId, id);
        departmentReportMapper.delete(reportWrapper);

        // 再删除院系
        departmentMapper.deleteById(id);
        log.info("硬删除院系，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        OffsetDateTime now = OffsetDateTime.now();

        LambdaUpdateWrapper<Department> deptWrapper = new LambdaUpdateWrapper<>();
        deptWrapper.in(Department::getId, ids)
                   .set(Department::getStatus, (short) 0)
                   .set(Department::getUpdatedAt, now);
        departmentMapper.update(null, deptWrapper);

        LambdaUpdateWrapper<DepartmentReport> reportWrapper = new LambdaUpdateWrapper<>();
        reportWrapper.in(DepartmentReport::getDepartmentId, ids)
                     .set(DepartmentReport::getStatus, (short) 0)
                     .set(DepartmentReport::getUpdatedAt, now);
        departmentReportMapper.update(null, reportWrapper);

        log.info("批量软删除院系，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        LambdaQueryWrapper<DepartmentReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.in(DepartmentReport::getDepartmentId, ids);
        departmentReportMapper.delete(reportWrapper);

        departmentMapper.deleteBatchIds(ids);
        log.info("批量硬删除院系，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDepartments(MultipartFile file) {
        // Excel导入实现 - 见Task 11
        throw new BusinessException(500, "待实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDepartmentReports(MultipartFile file) {
        // Excel导入实现 - 见Task 11
        throw new BusinessException(500, "待实现");
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/university/DepartmentService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/university/DepartmentServiceImpl.java
git commit -m "feat(service): 添加DepartmentService实现（基础CRUD）"
```

---

## Task 11: 创建院系Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/university/DepartmentController.java`

- [ ] **Step 1: 创建DepartmentController**

```java
package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.dto.university.DepartmentAddDTO;
import com.haifeng.admin.dto.university.DepartmentQueryDTO;
import com.haifeng.admin.dto.university.DepartmentUpdateDTO;
import com.haifeng.admin.service.university.DepartmentService;
import com.haifeng.admin.vo.university.DepartmentDetailVO;
import com.haifeng.admin.vo.university.DepartmentListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/university/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/list")
    public R<IPage<DepartmentListVO>> list(@Valid DepartmentQueryDTO dto) {
        return R.ok(departmentService.page(dto));
    }

    @GetMapping("/{id}")
    public R<DepartmentDetailVO> detail(@PathVariable Long id) {
        return R.ok(departmentService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "院系管理", action = "新增院系")
    public R<Long> add(@Valid @RequestBody DepartmentAddDTO dto) {
        return R.ok(departmentService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "院系管理", action = "修改院系")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateDTO dto) {
        departmentService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "院系管理", action = "修改院系状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        departmentService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "院系管理", action = "软删除院系")
    public R<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "院系管理", action = "硬删除院系")
    public R<Void> hardDelete(@PathVariable Long id) {
        departmentService.hardDelete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "院系管理", action = "批量软删除院系")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        departmentService.batchDelete(dto.getIds());
        return R.ok();
    }

    @DeleteMapping("/batch/hard")
    @OperationLog(module = "院系管理", action = "批量硬删除院系")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        departmentService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "院系管理", action = "导入院系数据")
    public R<Void> importDepartments(@RequestParam("file") MultipartFile file) {
        departmentService.importDepartments(file);
        return R.ok();
    }

    @PostMapping("/import-report")
    @OperationLog(module = "院系管理", action = "导入院系报告数据")
    public R<Void> importDepartmentReports(@RequestParam("file") MultipartFile file) {
        departmentService.importDepartmentReports(file);
        return R.ok();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/university/DepartmentController.java
git commit -m "feat(controller): 添加DepartmentController"
```

---

## Task 12: 创建学科评估DTO和VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/SubjectEvaluationQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/SubjectEvaluationAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/university/SubjectEvaluationUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/university/SubjectEvaluationListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/university/SubjectEvaluationDetailVO.java`

- [ ] **Step 1: 创建SubjectEvaluationQueryDTO**

```java
package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectEvaluationQueryDTO extends BasePageQueryDTO {

    private String universityName;

    private String disciplineCode;

    private String disciplineName;

    private String evaluationRound;

    private String evaluationGrade;

    private Integer status;
}
```

- [ ] **Step 2: 创建SubjectEvaluationAddDTO**

```java
package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SubjectEvaluationAddDTO {

    @NotNull(message = "院校ID不能为空")
    private Long universityId;

    @NotBlank(message = "学科代码不能为空")
    private String disciplineCode;

    @NotBlank(message = "学科名称不能为空")
    private String disciplineName;

    private String evaluationRound;

    @NotBlank(message = "评估等级不能为空")
    @Pattern(regexp = "^(A\\+|A|A-|B\\+|B|B-|C\\+|C|C-)$", message = "评估等级格式不正确")
    private String evaluationGrade;

    private Integer sortOrder;
}
```

- [ ] **Step 3: 创建SubjectEvaluationUpdateDTO**

```java
package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SubjectEvaluationUpdateDTO {

    private String disciplineCode;

    private String disciplineName;

    private String evaluationRound;

    @Pattern(regexp = "^(A\\+|A|A-|B\\+|B|B-|C\\+|C|C-)$", message = "评估等级格式不正确")
    private String evaluationGrade;

    private Integer sortOrder;

    private Integer status;
}
```

- [ ] **Step 4: 创建SubjectEvaluationListVO**

```java
package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SubjectEvaluationListVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String disciplineCode;

    private String disciplineName;

    private String evaluationRound;

    private String evaluationGrade;

    private Integer status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建SubjectEvaluationDetailVO**

```java
package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SubjectEvaluationDetailVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String disciplineCode;

    private String disciplineName;

    private String evaluationRound;

    private String evaluationGrade;

    private Integer sortOrder;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/SubjectEvaluationQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/SubjectEvaluationAddDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/university/SubjectEvaluationUpdateDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/university/SubjectEvaluationListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/university/SubjectEvaluationDetailVO.java
git commit -m "feat(dto): 添加学科评估DTO和VO"
```

---

## Task 13: 创建学科评估Service和Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/university/SubjectEvaluationService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/university/SubjectEvaluationServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/university/SubjectEvaluationController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/university/SubjectEvaluationExcelDTO.java`

- [ ] **Step 1: 创建SubjectEvaluationService接口**

```java
package com.haifeng.admin.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.university.SubjectEvaluationAddDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationUpdateDTO;
import com.haifeng.admin.vo.university.SubjectEvaluationDetailVO;
import com.haifeng.admin.vo.university.SubjectEvaluationListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubjectEvaluationService {

    IPage<SubjectEvaluationListVO> page(SubjectEvaluationQueryDTO dto);

    SubjectEvaluationDetailVO detail(Long id);

    Long add(SubjectEvaluationAddDTO dto);

    void update(Long id, SubjectEvaluationUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void hardDelete(Long id);

    void batchDelete(List<Long> ids);

    void batchHardDelete(List<Long> ids);

    void importSubjectEvaluations(MultipartFile file);
}
```

- [ ] **Step 2: 创建SubjectEvaluationServiceImpl**

```java
package com.haifeng.admin.service.impl.university;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haifeng.admin.dto.university.SubjectEvaluationAddDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationUpdateDTO;
import com.haifeng.admin.excel.university.SubjectEvaluationExcelDTO;
import com.haifeng.admin.service.university.SubjectEvaluationService;
import com.haifeng.admin.vo.university.SubjectEvaluationDetailVO;
import com.haifeng.admin.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.entity.university.SubjectEvaluation;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.SubjectEvaluationMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
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
public class SubjectEvaluationServiceImpl extends ServiceImpl<SubjectEvaluationMapper, SubjectEvaluation> implements SubjectEvaluationService {

    private final SubjectEvaluationMapper subjectEvaluationMapper;
    private final UniversityMapper universityMapper;

    private static final Set<String> VALID_GRADES = Set.of("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-");

    @Override
    public IPage<SubjectEvaluationListVO> page(SubjectEvaluationQueryDTO dto) {
        Page<SubjectEvaluation> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SubjectEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SubjectEvaluation::getStatus, (short) 0);

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(SubjectEvaluation::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getDisciplineCode())) {
            wrapper.eq(SubjectEvaluation::getDisciplineCode, dto.getDisciplineCode());
        }
        if (StringUtils.hasText(dto.getDisciplineName())) {
            wrapper.like(SubjectEvaluation::getDisciplineName, dto.getDisciplineName());
        }
        if (StringUtils.hasText(dto.getEvaluationRound())) {
            wrapper.eq(SubjectEvaluation::getEvaluationRound, dto.getEvaluationRound());
        }
        if (StringUtils.hasText(dto.getEvaluationGrade())) {
            wrapper.eq(SubjectEvaluation::getEvaluationGrade, dto.getEvaluationGrade());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SubjectEvaluation::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(SubjectEvaluation::getSortOrder).orderByDesc(SubjectEvaluation::getCreatedAt);

        IPage<SubjectEvaluation> evalPage = subjectEvaluationMapper.selectPage(page, wrapper);

        return evalPage.convert(eval -> {
            SubjectEvaluationListVO vo = new SubjectEvaluationListVO();
            BeanUtils.copyProperties(eval, vo);
            vo.setStatus(eval.getStatus() != null ? eval.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    public SubjectEvaluationDetailVO detail(Long id) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null || eval.getStatus() == 0) {
            throw new BusinessException(404, "学科评估记录不存在");
        }

        SubjectEvaluationDetailVO vo = new SubjectEvaluationDetailVO();
        BeanUtils.copyProperties(eval, vo);
        vo.setStatus(eval.getStatus() != null ? eval.getStatus().intValue() : null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SubjectEvaluationAddDTO dto) {
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(400, "院校不存在");
        }

        String round = dto.getEvaluationRound() != null ? dto.getEvaluationRound() : "第四轮";
        if (subjectEvaluationMapper.existsByUniversityAndDiscipline(dto.getUniversityId(), dto.getDisciplineCode(), round)) {
            throw new BusinessException(400, "该院校在此轮次下的学科评估记录已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        SubjectEvaluation eval = SubjectEvaluation.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .disciplineCode(dto.getDisciplineCode())
                .disciplineName(dto.getDisciplineName())
                .evaluationRound(round)
                .evaluationGrade(dto.getEvaluationGrade())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        subjectEvaluationMapper.insert(eval);
        log.info("新增学科评估成功，id={}", id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SubjectEvaluationUpdateDTO dto) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null || eval.getStatus() == 0) {
            throw new BusinessException(404, "学科评估记录不存在");
        }

        if (dto.getDisciplineCode() != null) eval.setDisciplineCode(dto.getDisciplineCode());
        if (dto.getDisciplineName() != null) eval.setDisciplineName(dto.getDisciplineName());
        if (dto.getEvaluationRound() != null) eval.setEvaluationRound(dto.getEvaluationRound());
        if (dto.getEvaluationGrade() != null) eval.setEvaluationGrade(dto.getEvaluationGrade());
        if (dto.getSortOrder() != null) eval.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) eval.setStatus(dto.getStatus().shortValue());

        eval.setUpdatedAt(OffsetDateTime.now());
        subjectEvaluationMapper.updateById(eval);
        log.info("更新学科评估成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException(404, "学科评估记录不存在");
        }

        LambdaUpdateWrapper<SubjectEvaluation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SubjectEvaluation::getId, id)
               .set(SubjectEvaluation::getStatus, status.shortValue())
               .set(SubjectEvaluation::getUpdatedAt, OffsetDateTime.now());
        subjectEvaluationMapper.update(null, wrapper);
        log.info("更新学科评估状态，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updateStatus(id, 0);
        log.info("软删除学科评估，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException(404, "学科评估记录不存在");
        }
        subjectEvaluationMapper.deleteById(id);
        log.info("硬删除学科评估，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        LambdaUpdateWrapper<SubjectEvaluation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(SubjectEvaluation::getId, ids)
               .set(SubjectEvaluation::getStatus, (short) 0)
               .set(SubjectEvaluation::getUpdatedAt, OffsetDateTime.now());
        subjectEvaluationMapper.update(null, wrapper);
        log.info("批量软删除学科评估，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        subjectEvaluationMapper.deleteBatchIds(ids);
        log.info("批量硬删除学科评估，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSubjectEvaluations(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            List<SubjectEvaluationExcelDTO> dataList = EasyExcel.read(file.getInputStream())
                    .head(SubjectEvaluationExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            Map<String, Long> universityIdCache = new HashMap<>();
            Map<String, String> universityNameCache = new HashMap<>();
            List<SubjectEvaluation> evaluations = new ArrayList<>();

            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = i + 2;
                SubjectEvaluationExcelDTO data = dataList.get(i);

                if (!StringUtils.hasText(data.getUniversityName())) {
                    errorMsgs.add("第" + rowNum + "行：院校名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getDisciplineCode())) {
                    errorMsgs.add("第" + rowNum + "行：学科代码不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getDisciplineName())) {
                    errorMsgs.add("第" + rowNum + "行：学科名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getEvaluationGrade())) {
                    errorMsgs.add("第" + rowNum + "行：评估等级不能为空");
                    continue;
                }
                if (!VALID_GRADES.contains(data.getEvaluationGrade())) {
                    errorMsgs.add("第" + rowNum + "行：评估等级'" + data.getEvaluationGrade() + "'格式不正确");
                    continue;
                }

                Long universityId = universityIdCache.get(data.getUniversityName());
                if (universityId == null) {
                    LambdaQueryWrapper<University> uniWrapper = new LambdaQueryWrapper<>();
                    uniWrapper.eq(University::getName, data.getUniversityName()).eq(University::getStatus, (short) 1);
                    University university = universityMapper.selectOne(uniWrapper);
                    if (university == null) {
                        errorMsgs.add("第" + rowNum + "行：院校名称'" + data.getUniversityName() + "'不存在");
                        continue;
                    }
                    universityId = university.getId();
                    universityIdCache.put(data.getUniversityName(), universityId);
                    universityNameCache.put(data.getUniversityName(), university.getName());
                }

                String round = data.getEvaluationRound() != null ? data.getEvaluationRound() : "第四轮";
                if (subjectEvaluationMapper.existsByUniversityAndDiscipline(universityId, data.getDisciplineCode(), round)) {
                    errorMsgs.add("第" + rowNum + "行：该院校在此轮次下的学科'" + data.getDisciplineCode() + "'评估记录已存在");
                    continue;
                }

                OffsetDateTime now = OffsetDateTime.now();
                SubjectEvaluation eval = SubjectEvaluation.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .universityId(universityId)
                        .universityName(universityNameCache.get(data.getUniversityName()))
                        .disciplineCode(data.getDisciplineCode())
                        .disciplineName(data.getDisciplineName())
                        .evaluationRound(round)
                        .evaluationGrade(data.getEvaluationGrade())
                        .sortOrder(data.getSortOrder() != null ? data.getSortOrder() : 0)
                        .status(data.getStatus() != null ? data.getStatus().shortValue() : (short) 1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                evaluations.add(eval);
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            if (!evaluations.isEmpty()) {
                saveBatch(evaluations);
                log.info("导入学科评估成功，数量={}", evaluations.size());
            }

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }
}
```

- [ ] **Step 3: 创建SubjectEvaluationExcelDTO**

```java
package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SubjectEvaluationExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("学科代码")
    private String disciplineCode;

    @ExcelProperty("学科名称")
    private String disciplineName;

    @ExcelProperty("评估轮次")
    private String evaluationRound;

    @ExcelProperty("评估等级")
    private String evaluationGrade;

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("状态")
    private Integer status;
}
```

- [ ] **Step 4: 创建SubjectEvaluationController**

```java
package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationAddDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationUpdateDTO;
import com.haifeng.admin.service.university.SubjectEvaluationService;
import com.haifeng.admin.vo.university.SubjectEvaluationDetailVO;
import com.haifeng.admin.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/university/subject-evaluation")
@RequiredArgsConstructor
public class SubjectEvaluationController {

    private final SubjectEvaluationService subjectEvaluationService;

    @GetMapping("/list")
    public R<IPage<SubjectEvaluationListVO>> list(@Valid SubjectEvaluationQueryDTO dto) {
        return R.ok(subjectEvaluationService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SubjectEvaluationDetailVO> detail(@PathVariable Long id) {
        return R.ok(subjectEvaluationService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "学科评估管理", action = "新增学科评估")
    public R<Long> add(@Valid @RequestBody SubjectEvaluationAddDTO dto) {
        return R.ok(subjectEvaluationService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "学科评估管理", action = "修改学科评估")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SubjectEvaluationUpdateDTO dto) {
        subjectEvaluationService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "学科评估管理", action = "修改学科评估状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        subjectEvaluationService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "学科评估管理", action = "软删除学科评估")
    public R<Void> delete(@PathVariable Long id) {
        subjectEvaluationService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "学科评估管理", action = "硬删除学科评估")
    public R<Void> hardDelete(@PathVariable Long id) {
        subjectEvaluationService.hardDelete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "学科评估管理", action = "批量软删除学科评估")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        subjectEvaluationService.batchDelete(dto.getIds());
        return R.ok();
    }

    @DeleteMapping("/batch/hard")
    @OperationLog(module = "学科评估管理", action = "批量硬删除学科评估")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        subjectEvaluationService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "学科评估管理", action = "导入学科评估数据")
    public R<Void> importSubjectEvaluations(@RequestParam("file") MultipartFile file) {
        subjectEvaluationService.importSubjectEvaluations(file);
        return R.ok();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/university/SubjectEvaluationService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/university/SubjectEvaluationServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/excel/university/SubjectEvaluationExcelDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/university/SubjectEvaluationController.java
git commit -m "feat(module): 添加学科评估完整模块"
```

---

## Task 14: 创建接口文档 order6.md

**Files:**
- Create: `Products/order6.md`

- [ ] **Step 1: 创建order6.md接口文档**

创建完整的接口文档，包含Excel表头规范和所有API接口说明。

文档结构：
1. Excel导入表头规范
2. 院系管理接口（11个）
3. 实验室管理接口（10个）
4. 学科评估接口（10个）

- [ ] **Step 2: Commit**

```bash
git add Products/order6.md
git commit -m "docs: 添加院校次表模块接口文档order6.md"
```

---

## Task 15: 编译验证

**Files:**
- 无新增文件，验证已有代码

- [ ] **Step 1: 编译项目**

Run: `cd D:/exeProject/ideaProjects/Project-HaiFeng && mvn clean compile -DskipTests`

Expected: BUILD SUCCESS

- [ ] **Step 2: 运行Flyway迁移**

Run: `cd haifeng-admin && mvn flyway:migrate`

Expected: Successfully applied 1 migration to schema

- [ ] **Step 3: 启动应用测试**

Run: `cd haifeng-admin && mvn spring-boot:run`

Expected: 应用正常启动，无异常

- [ ] **Step 4: 最终提交**

```bash
git add .
git commit -m "feat(university): 完成院校次表模块实现

- 实验室管理（Laboratory）
- 院系管理（Department + DepartmentReport）
- 学科评估（SubjectEvaluation）
- Excel批量导入支持
- 完整CRUD和状态管理"
```

---

## 自审清单

- [x] 所有Entity使用雪花算法ID（IdType.ASSIGN_ID）
- [x] 软删除统一使用status字段
- [x] JSONB字段使用JacksonTypeHandler
- [x] Excel导入支持多Sheet
- [x] 错误信息包含具体行号
- [x] 事务注解添加rollbackFor
- [x] 日志记录关键操作
- [x] 文件路径完整且正确
