# 院校次表模块设计文档

## 概述

本文档描述院校管理模块的次表功能设计，包含院系详情、实验室列表、学科评估三个子模块的完整设计方案。

**创建日期**：2026-05-07
**模块归属**：院校管理 > 院系详情/实验室列表/学科评估

---

## 一、需求澄清总结

| 决策项 | 确认结果 |
|--------|----------|
| ID生成策略 | 雪花算法（BIGINT） |
| 软删除字段 | 统一使用status字段（0-删除/禁用，1-正常） |
| 专业字段组织 | major_compose存专业组成占比，subjects_detail存专业详情（删除冗余的majors_detail） |
| Excel导入方式 | 多Sheet方案，与院校适应指南模式一致 |
| 架构方案 | 独立Controller方案 |

---

## 二、数据库设计

### V6迁移文件：V6__create_universities_details.sql

#### 2.1 实验室表 (laboratories)

与院校表 1:N 关系

```sql
CREATE TABLE laboratories (
    id                    BIGINT        PRIMARY KEY,           -- 雪花算法ID
    university_id         BIGINT        NOT NULL,              -- 外键：院校ID
    university_name       VARCHAR(50)   NOT NULL,              -- 冗余：院校名称
    name                  VARCHAR(200)  NOT NULL,              -- 实验室名称
    lab_type              VARCHAR(100)  NOT NULL,              -- 实验室类型
    established_year      VARCHAR(20),                         -- 成立时间
    region                VARCHAR(100),                        -- 所在地区
    department            VARCHAR(100),                        -- 主管部门
    director              VARCHAR(50),                         -- 实验室主任
    staff_count           VARCHAR(50),                         -- 人员规模
    student_count         VARCHAR(50),                         -- 学生规模
    email                 VARCHAR(200),                        -- 联系邮箱
    phone                 VARCHAR(50),                         -- 联系电话
    introduction          TEXT,                                -- 实验室简介
    research_description  TEXT,                                -- 研究方向描述
    lab_space             TEXT,                                -- 实验室空间
    open_topics           TEXT,                                -- 开放课题
    cooperation           TEXT,                                -- 合作交流
    visiting_scholars     TEXT,                                -- 访问学者
    research_fields       TEXT[],                              -- 研究领域数组
    statistics            JSONB         DEFAULT '[]'::JSONB,   -- 统计数据
    major_equipment       TEXT[],                              -- 主要设备数组
    core_team             JSONB         DEFAULT '[]'::JSONB,   -- 核心团队
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_labs_university_id ON laboratories(university_id);
CREATE INDEX idx_labs_lab_type ON laboratories(lab_type);
CREATE INDEX idx_labs_region ON laboratories(region);
CREATE INDEX idx_labs_status ON laboratories(status);
CREATE INDEX idx_labs_name ON laboratories(name);
```

**JSONB字段结构**：

- `statistics`：`[{"label": "发表论文", "count": 500}, {"label": "授权专利", "count": 120}]`
- `core_team`：`[{"name": "张三", "position": "教授", "title": "主任"}]`

---

#### 2.2 院系主表 (t_department)

与院校表 1:N 关系

```sql
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

-- 索引
CREATE INDEX idx_dept_university_id ON t_department(university_id);
CREATE INDEX idx_dept_status ON t_department(status);
CREATE INDEX idx_dept_name ON t_department(department_name);
CREATE INDEX idx_dept_type ON t_department(department_type);
```

---

#### 2.3 院系分析报告表 (department_reports)

与院系表 1:1 关系

```sql
CREATE TABLE department_reports (
    id                    BIGINT        PRIMARY KEY,
    department_id         BIGINT        NOT NULL UNIQUE,       -- 1:1约束
    subtitle              VARCHAR(200),
    overview              JSONB         DEFAULT '{}'::JSONB,   -- 学院定位
    subjects_detail       JSONB         DEFAULT '[]'::JSONB,   -- 专业详情列表
    postgraduate          JSONB         DEFAULT '{}'::JSONB,   -- 考研分析
    city_salary           JSONB         DEFAULT '[]'::JSONB,   -- 城市薪资对比
    salary                JSONB         DEFAULT '[]'::JSONB,   -- 专业薪资对比
    career                JSONB         DEFAULT '[]'::JSONB,   -- 职业发展路径
    trends                JSONB         DEFAULT '{}'::JSONB,   -- 行业趋势
    prospects             JSONB         DEFAULT '{}'::JSONB,   -- 数据概览
    disclaimer            JSONB         DEFAULT '{}'::JSONB,   -- 免责声明
    major_compose         JSONB         DEFAULT '[]'::JSONB,   -- 专业组成
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_dept_reports_department_id ON department_reports(department_id);
CREATE INDEX idx_dept_reports_status ON department_reports(status);
```

**JSONB字段结构详解**：

| 字段 | 结构 |
|------|------|
| overview | `{"title": "学院简介", "content": ["内容1", "内容2"]}` |
| subjects_detail | `[{"subject_name": "...", "tags": [], "core": "...", "support": "...", "positioning": "...", "courses": [], "abilities": [], "certificates": []}]` |
| postgraduate | `{"title": "考研方向", "content": ["方向1", "方向2"]}` |
| city_salary | `[{"city": "北京", "min_salary": 15, "max_salary": 30}]` |
| salary | `[{"subject": "计算机", "min_salary": 15, "max_salary": 30}]` |
| career | `[{"title": "技术路线", "description": "...", "subtitle": "初级", "years": "0-3年", "position": "工程师", "goal": "...", "salary_range": "10-15万"}]` |
| trends | `{"growth_tracks": [], "policy_guidance": [], "employment_analysis": []}` |
| prospects | `{"employment_rate": "95.6%", "master_salary": "18万", "further_study_rate": "35.2%", "fortune500_rate": "22.8%", "salary_growth_rate": "12.5%", "overseas_rate": "15.3%"}` |
| disclaimer | `{"text": "免责声明内容", "update_time": "2025年10月", "version": "V3.0", "compiler": "海峰规划院"}` |
| major_compose | `[{"subject_name": "计算机科学", "percent": 45.5}]` |

---

#### 2.4 学科评估表 (t_subject_evaluation)

```sql
CREATE TABLE t_subject_evaluation (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL,
    university_name       VARCHAR(100)  NOT NULL,
    discipline_code       VARCHAR(20)   NOT NULL,              -- 学科代码
    discipline_name       VARCHAR(100)  NOT NULL,              -- 学科名称
    evaluation_round      VARCHAR(20)   DEFAULT '第四轮',      -- 评估轮次
    evaluation_grade      VARCHAR(5)    NOT NULL,              -- 评估等级
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_eval_grade CHECK (
        evaluation_grade IN ('A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C', 'C-')
    )
);

-- 索引
CREATE INDEX idx_se_university ON t_subject_evaluation(university_id);
CREATE INDEX idx_se_discipline ON t_subject_evaluation(discipline_name, evaluation_grade);
CREATE INDEX idx_se_grade ON t_subject_evaluation(evaluation_grade);
CREATE INDEX idx_se_status ON t_subject_evaluation(status);
```

---

## 三、API接口设计

### 3.1 院系管理接口 (DepartmentController)

**基础路径**：`/api/v1/admin/university/department`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/list` | 支持模糊查询 |
| 获取详情 | GET | `/{id}` | 返回院系+报告（Tabs数据） |
| 新增 | POST | `/` | 同时创建院系+报告（事务） |
| 修改 | PUT | `/{id}` | 同时更新院系+报告 |
| 切换状态 | PUT | `/{id}/status` | 禁用/启用 |
| 软删除 | DELETE | `/{id}` | status=0 |
| 硬删除 | DELETE | `/{id}/hard` | 物理删除 |
| 批量软删除 | DELETE | `/batch` | 批量status=0 |
| 批量硬删除 | DELETE | `/batch/hard` | 批量物理删除 |
| 导入院系主表 | POST | `/import` | xlsx多Sheet |
| 导入报告JSONB | POST | `/import-report` | xlsx多Sheet |

**列表查询参数 (DepartmentQueryDTO)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| universityName | String | 院校名称（模糊） |
| departmentName | String | 院系名称（模糊） |
| departmentType | String | 院系类型 |
| status | Integer | 状态 |
| page | Integer | 页码 |
| size | Integer | 每页条数 |

---

### 3.2 实验室管理接口 (LaboratoryController)

**基础路径**：`/api/v1/admin/university/laboratory`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/list` | 支持模糊查询 |
| 获取详情 | GET | `/{id}` | 返回完整字段 |
| 新增 | POST | `/` | |
| 修改 | PUT | `/{id}` | |
| 切换状态 | PUT | `/{id}/status` | |
| 软删除 | DELETE | `/{id}` | |
| 硬删除 | DELETE | `/{id}/hard` | |
| 批量软删除 | DELETE | `/batch` | |
| 批量硬删除 | DELETE | `/batch/hard` | |
| 导入 | POST | `/import` | xlsx多Sheet |

**列表查询参数 (LaboratoryQueryDTO)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| universityName | String | 院校名称（模糊） |
| name | String | 实验室名称（模糊） |
| labType | String | 实验室类型 |
| region | String | 所在地区 |
| department | String | 主管部门 |
| status | Integer | 状态 |
| page | Integer | 页码 |
| size | Integer | 每页条数 |

---

### 3.3 学科评估接口 (SubjectEvaluationController)

**基础路径**：`/api/v1/admin/university/subject-evaluation`

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/list` | |
| 获取详情 | GET | `/{id}` | |
| 新增 | POST | `/` | |
| 修改 | PUT | `/{id}` | |
| 切换状态 | PUT | `/{id}/status` | |
| 软删除 | DELETE | `/{id}` | |
| 硬删除 | DELETE | `/{id}/hard` | |
| 批量软删除 | DELETE | `/batch` | |
| 批量硬删除 | DELETE | `/batch/hard` | |
| 导入 | POST | `/import` | xlsx |

**列表查询参数 (SubjectEvaluationQueryDTO)**：

| 参数 | 类型 | 说明 |
|------|------|------|
| universityName | String | 院校名称（模糊） |
| disciplineCode | String | 学科代码 |
| disciplineName | String | 学科名称（模糊） |
| evaluationRound | String | 评估轮次 |
| evaluationGrade | String | 评估等级 |
| status | Integer | 状态 |
| page | Integer | 页码 |
| size | Integer | 每页条数 |

---

## 四、Excel导入Sheet规范

### 4.1 xlsx1：院系管理（t_department + department_reports）

| Sheet | 表/字段 | 表头列 |
|-------|---------|--------|
| Sheet0 | t_department主表 | 院校名称, 院系名称, 院系类型, 页面主标题, 院系标签, 排序, 状态 |
| Sheet1 | department_reports基础 | 院系名称, 副标题 |
| Sheet2 | city_salary | 院系名称, 城市名称, 最低薪资(万元/年), 最高薪资(万元/年) |
| Sheet3 | postgraduate | 院系名称, 标题, 考研方向内容(逗号分隔) |
| Sheet4 | disclaimer | 院系名称, 免责声明文本, 更新时间, 报告版本, 编制单位 |
| Sheet5 | prospects | 院系名称, 综合就业率, 硕士平均起薪, 继续深造率, 进入世界500强, 年薪增长率, 海外深造占比 |
| Sheet6 | trends | 院系名称, 高速增长赛道(逗号分隔), 核心政策导向(逗号分隔), 就业环境分析(逗号分隔) |
| Sheet7 | overview | 院系名称, 标题, 内容描述(逗号分隔) |
| Sheet8 | career | 院系名称, 路径标题, 路径描述, 阶段小标题, 工作年限, 职位名称, 核心目标, 薪资范围(万元/年) |
| Sheet9 | subjects_detail | 院系名称, 专业名称, 专业标签(逗号分隔), 核心学科, 支撑学科, 专业定位, 核心课程(逗号分隔), 培养能力(逗号分隔), 推荐证书(逗号分隔) |
| Sheet10 | salary | 院系名称, 专业名称, 最低薪资(万元/年), 最高薪资(万元/年) |
| Sheet11 | major_compose | 院系名称, 学科名称, 占比(%) |

---

### 4.2 xlsx2：实验室表 (laboratories)

| Sheet | 表/字段 | 表头列 |
|-------|---------|--------|
| Sheet0 | laboratories主表 | 院校名称, 实验室名称, 实验室类型, 成立时间, 所在地区, 主管部门, 实验室主任, 人员规模, 学生规模, 联系邮箱, 联系电话, 实验室简介, 研究方向描述, 实验室空间, 开放课题, 合作交流, 访问学者, 研究领域(逗号分隔), 主要设备(逗号分隔), 排序, 状态 |
| Sheet1 | core_team | 实验室名称, 成员姓名, 职务, 岗位名称 |
| Sheet2 | statistics | 实验室名称, 统计标签, 数量 |

---

### 4.3 xlsx3：学科评估表 (t_subject_evaluation)

| Sheet | 表/字段 | 表头列 |
|-------|---------|--------|
| Sheet0 | t_subject_evaluation | 院校名称, 学科代码, 学科名称, 评估轮次, 评估等级, 排序, 状态 |

---

## 五、代码结构

### 5.1 文件清单

```
haifeng-common/
├── entity/university/
│   ├── Laboratory.java              ← 新增
│   ├── Department.java              ← 新增
│   ├── DepartmentReport.java        ← 新增
│   └── SubjectEvaluation.java       ← 新增
├── mapper/university/
│   ├── LaboratoryMapper.java        ← 新增
│   ├── DepartmentMapper.java        ← 新增
│   ├── DepartmentReportMapper.java  ← 新增
│   └── SubjectEvaluationMapper.java ← 新增

haifeng-admin/
├── controller/university/
│   ├── DepartmentController.java    ← 新增
│   ├── LaboratoryController.java    ← 新增
│   └── SubjectEvaluationController.java ← 新增
├── service/university/
│   ├── DepartmentService.java       ← 新增
│   ├── LaboratoryService.java       ← 新增
│   └── SubjectEvaluationService.java ← 新增
├── service/impl/university/
│   ├── DepartmentServiceImpl.java   ← 新增
│   ├── LaboratoryServiceImpl.java   ← 新增
│   └── SubjectEvaluationServiceImpl.java ← 新增
├── dto/university/
│   ├── DepartmentQueryDTO.java      ← 新增
│   ├── DepartmentAddDTO.java        ← 新增
│   ├── DepartmentUpdateDTO.java     ← 新增
│   ├── LaboratoryQueryDTO.java      ← 新增
│   ├── LaboratoryAddDTO.java        ← 新增
│   ├── LaboratoryUpdateDTO.java     ← 新增
│   ├── SubjectEvaluationQueryDTO.java ← 新增
│   ├── SubjectEvaluationAddDTO.java ← 新增
│   └── SubjectEvaluationUpdateDTO.java ← 新增
├── vo/university/
│   ├── DepartmentListVO.java        ← 新增
│   ├── DepartmentDetailVO.java      ← 新增
│   ├── LaboratoryListVO.java        ← 新增
│   ├── LaboratoryDetailVO.java      ← 新增
│   ├── SubjectEvaluationListVO.java ← 新增
│   └── SubjectEvaluationDetailVO.java ← 新增
├── excel/university/
│   ├── DepartmentExcelDTO.java      ← 新增
│   ├── DepartmentReportExcelDTO.java ← 新增
│   ├── CitySalaryExcelDTO.java      ← 新增
│   ├── PostgraduateExcelDTO.java    ← 新增
│   ├── DisclaimerExcelDTO.java      ← 新增
│   ├── ProspectsExcelDTO.java       ← 新增
│   ├── TrendsExcelDTO.java          ← 新增
│   ├── OverviewExcelDTO.java        ← 新增
│   ├── CareerExcelDTO.java          ← 新增
│   ├── SubjectsDetailExcelDTO.java  ← 新增
│   ├── SalaryExcelDTO.java          ← 新增
│   ├── MajorComposeExcelDTO.java    ← 新增
│   ├── LaboratoryExcelDTO.java      ← 新增
│   ├── CoreTeamExcelDTO.java        ← 新增
│   ├── StatisticsExcelDTO.java      ← 新增
│   └── SubjectEvaluationExcelDTO.java ← 新增
```

---

## 六、错误处理设计

### 6.1 Excel导入错误收集模式

```java
@Transactional(rollbackFor = Exception.class)
public void importDepartments(MultipartFile file) {
    List<String> errorMsgs = new ArrayList<>();

    // 1. 读取Sheet0主表数据
    List<DepartmentExcelDTO> dataList = EasyExcel.read(file.getInputStream())
            .head(DepartmentExcelDTO.class)
            .sheet(0)
            .doReadSync();

    // 2. 行级校验
    for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
        int rowNum = rowIndex + 2; // Excel行号从1开始，跳过表头
        DepartmentExcelDTO data = dataList.get(rowIndex);

        // 必填校验
        if (StringUtils.isBlank(data.getUniversityName())) {
            errorMsgs.add("第" + rowNum + "行：院校名称不能为空");
            continue;
        }

        // 外键校验
        University univ = universityMapper.selectByName(data.getUniversityName());
        if (univ == null) {
            errorMsgs.add("第" + rowNum + "行：院校名称'" + data.getUniversityName() + "'不存在");
            continue;
        }

        // 唯一性校验
        if (departmentMapper.existsByUniversityIdAndName(univ.getId(), data.getDepartmentName())) {
            errorMsgs.add("第" + rowNum + "行：该院校下院系名称'" + data.getDepartmentName() + "'已存在");
        }
    }

    // 3. 统一抛出
    if (!errorMsgs.isEmpty()) {
        throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
    }

    // 4. 批量插入
    List<Department> departments = convertToDepartments(dataList);
    saveBatch(departments);
}
```

### 6.2 事务一致性

- 所有导入方法添加 `@Transactional(rollbackFor = Exception.class)`
- 错误时自动回滚，保证数据一致性
- 使用 MyBatis-Plus 的 `saveBatch` 批量插入

### 6.3 错误信息格式

```json
{
  "code": 400,
  "msg": "导入失败：第3行：院校名称不能为空；第5行：院校名称'未知大学'不存在；第7行：该院校下院系名称'计算机学院'已存在",
  "data": null,
  "timestamp": 1234567890
}
```

---

## 七、接口文档输出

完成实现后，需创建 `Products/order6.md` 接口文档，开头包含：

1. xlsx1：院系管理表头规范（12个Sheet）
2. xlsx2：实验室表头规范（3个Sheet）
3. xlsx3：学科评估表头规范（1个Sheet）

---

## 八、实施任务清单

1. **任务1**：创建 V6__create_universities_details.sql 迁移文件
2. **任务2**：实现院系管理模块（Entity/Mapper/DTO/VO/Service/Controller）
3. **任务3**：实现实验室管理模块（Entity/Mapper/DTO/VO/Service/Controller）
4. **任务4**：实现学科评估模块（Entity/Mapper/DTO/VO/Service/Controller）
5. **任务5**：创建 Products/order6.md 接口文档
