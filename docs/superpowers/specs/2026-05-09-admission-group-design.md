# 专业组管理模块设计文档

> 创建日期: 2026-05-09
> 模块: 专业组管理（algorithm/admission）
> 状态: 已确认

## 1. 概述

### 1.1 模块定位

专业组管理是高考算法模块的核心子模块，负责管理高考录取的专业组数据和专业明细数据。

### 1.2 子模块列表

| 子模块 | 说明 |
|--------|------|
| 专业组录取列表 | 按大学+年份+省份+科类+批次维度的录取汇总 |
| 专业组明细列表 | 专业组内每个专业的录取分数明细 |
| 选科要求列表 | 选科要求字典（3+1+2/3+3改革省份的选科约束） |

### 1.3 核心需求

1. 三个表的完整CRUD功能
2. **Excel一键导入**：上传一个xlsx文件，自动创建/更新专业组和专业明细两个表
3. **自动聚合计算**：专业明细变更时，数据库触发器自动重算专业组的聚合数据
4. **手动全量重算**：提供接口触发所有专业组的聚合数据重算

## 2. 数据库设计

### 2.1 表结构

#### t_admission_group（专业组录取表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| university_id | INTEGER | 大学ID（外键） |
| year | SMALLINT | 年份 |
| province | VARCHAR(20) | 省份 |
| subject_type | VARCHAR(20) | 科类（理科/物理类/文科/历史类/不分文理） |
| batch | VARCHAR(50) | 批次（本科批/提前批/专科批） |
| enrollment_code | VARCHAR(30) | 省招代码 |
| group_code | VARCHAR(30) | 专业组代码 |
| group_name | VARCHAR(100) | 专业组名称 |
| description | TEXT | 专业组简介 |
| subject_requirements | VARCHAR(50) | 选科要求（聚合推导） |
| requirement_level | SMALLINT | 选科等级（聚合推导，最宽松） |
| constraints | TEXT[] | 静态限制 |
| major_count | INTEGER | 专业数量（聚合计算） |
| admission_count | INTEGER | 录取总人数（聚合计算） |
| min_score | INTEGER | 最低分（聚合计算） |
| min_rank | INTEGER | 最低位次（聚合计算） |
| avg_score | NUMERIC(6,2) | 平均分（加权聚合计算） |
| avg_rank | INTEGER | 平均位次（加权聚合计算） |
| max_score | INTEGER | 最高分（聚合计算） |
| max_rank | INTEGER | 最高位次（聚合计算） |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**唯一约束**: `(university_id, year, province, subject_type, batch, group_code)`

#### t_admission_major_score（专业录取明细表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| group_id | INTEGER | 所属专业组ID（外键） |
| major_id | INTEGER | 关联专业表ID（可选） |
| major_code | VARCHAR(20) | 专业代码 |
| major_name | VARCHAR(100) | 专业名称 |
| subject_requirements | VARCHAR(200) | 选科要求 |
| requirement_level | SMALLINT | 选科等级（自动设置） |
| education_level | VARCHAR(20) | 层次（本科/专科） |
| duration | VARCHAR(20) | 学制 |
| tuition | VARCHAR(50) | 学费 |
| description | TEXT | 专业简介 |
| admission_count | INTEGER | 录取人数 |
| min_score | INTEGER | 最低分 |
| min_rank | INTEGER | 最低位次 |
| avg_score | NUMERIC(6,2) | 平均分 |
| avg_rank | INTEGER | 平均位次 |
| max_score | INTEGER | 最高分 |
| max_rank | INTEGER | 最高位次 |
| constraints | TEXT[] | 动态限制 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**唯一约束**: `(group_id, major_code)` — 同一专业组内专业代码唯一

**删除策略**: 硬删除（无软删除）

#### t_subject_req_dict（选科要求字典表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| code | VARCHAR(50) | 标准代码（唯一） |
| display_name | VARCHAR(100) | 前端展示名称 |
| requirement_level | SMALLINT | 严格等级（0=不限，1=2选1，2=必选，3=均须） |
| subjects | TEXT[] | 涉及的科目 |
| requirement_type | VARCHAR(10) | 类型（NONE/ANY/ALL） |
| sort_order | INTEGER | 排序 |

**删除策略**: 硬删除

### 2.2 数据库函数与触发器

| 函数/触发器 | 说明 |
|-------------|------|
| fn_get_requirement_level(code) | 根据选科要求code查字典返回等级 |
| fn_auto_set_req_level() | 明细表INSERT/UPDATE时自动设置requirement_level |
| trg_ams_req_level | 绑定到t_admission_major_score的触发器 |
| fn_recalc_group(group_id) | 重算单个专业组的聚合数据 |
| fn_on_major_score_changed() | 明细表变更后调用fn_recalc_group |
| trg_major_score_changed | 绑定到t_admission_major_score的AFTER触发器 |
| fn_recalc_all_groups() | 全量重算所有专业组（供手动接口调用） |

### 2.3 表关系

```
t_university (1) ──── (N) t_admission_group (1) ──── (N) t_admission_major_score
                                  │
                                  └── subject_requirements → t_subject_req_dict.code
```

## 3. 后端包结构

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/algorithm/admission/
│   ├── AdmissionGroupController.java
│   ├── AdmissionMajorScoreController.java
│   └── SubjectReqDictController.java
│
├── service/algorithm/admission/
│   ├── AdmissionGroupService.java
│   ├── AdmissionMajorScoreService.java
│   └── SubjectReqDictService.java
│
├── service/impl/algorithm/admission/
│   ├── AdmissionGroupServiceImpl.java
│   ├── AdmissionMajorScoreServiceImpl.java
│   └── SubjectReqDictServiceImpl.java
│
├── dto/algorithm/admission/
│   ├── AdmissionGroupQueryDTO.java
│   ├── AdmissionGroupAddDTO.java
│   ├── AdmissionMajorScoreQueryDTO.java
│   ├── AdmissionMajorScoreAddDTO.java
│   ├── SubjectReqDictQueryDTO.java
│   └── SubjectReqDictAddDTO.java
│
├── vo/algorithm/admission/
│   ├── AdmissionGroupListVO.java
│   ├── AdmissionGroupDetailVO.java
│   ├── AdmissionMajorScoreListVO.java
│   ├── AdmissionMajorScoreDetailVO.java
│   ├── SubjectReqDictListVO.java
│   └── SubjectReqDictDetailVO.java
│
└── excel/algorithm/admission/
    └── AdmissionImportDTO.java

haifeng-common/src/main/java/com/haifeng/common/
├── entity/algorithm/
│   ├── AdmissionGroup.java
│   ├── AdmissionMajorScore.java
│   └── SubjectReqDict.java
│
└── mapper/algorithm/
    ├── AdmissionGroupMapper.java
    ├── AdmissionMajorScoreMapper.java
    └── SubjectReqDictMapper.java
```

## 4. API接口设计

### 4.1 专业组录取列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/algorithm/admission/group/page` | 分页查询 |
| GET | `/api/v1/admin/algorithm/admission/group/{id}` | 详情 |
| POST | `/api/v1/admin/algorithm/admission/group` | 新增 |
| PUT | `/api/v1/admin/algorithm/admission/group/{id}` | 修改 |
| PUT | `/api/v1/admin/algorithm/admission/group/{id}/status` | 禁用/启用 |
| DELETE | `/api/v1/admin/algorithm/admission/group/{id}` | 硬删除 |
| DELETE | `/api/v1/admin/algorithm/admission/group/batch` | 批量硬删除 |
| POST | `/api/v1/admin/algorithm/admission/group/import` | Excel导入 |
| POST | `/api/v1/admin/algorithm/admission/group/recalc-all` | 全量重算 |

**查询条件**: 大学名、年份、省份、科类、省招代码、专业组代码、专业组名称、is_deleted

### 4.2 专业组明细列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/algorithm/admission/major-score/page` | 分页查询 |
| GET | `/api/v1/admin/algorithm/admission/major-score/{id}` | 详情 |
| POST | `/api/v1/admin/algorithm/admission/major-score` | 新增 |
| PUT | `/api/v1/admin/algorithm/admission/major-score/{id}` | 修改 |
| DELETE | `/api/v1/admin/algorithm/admission/major-score/{id}` | 硬删除 |
| DELETE | `/api/v1/admin/algorithm/admission/major-score/batch` | 批量硬删除 |

**查询条件**: 专业组ID、专业代码、专业名称、层次

### 4.3 选科要求列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/algorithm/admission/subject-req/page` | 分页查询 |
| GET | `/api/v1/admin/algorithm/admission/subject-req/{id}` | 详情 |
| POST | `/api/v1/admin/algorithm/admission/subject-req` | 新增 |
| PUT | `/api/v1/admin/algorithm/admission/subject-req/{id}` | 修改 |
| DELETE | `/api/v1/admin/algorithm/admission/subject-req/{id}` | 硬删除 |
| DELETE | `/api/v1/admin/algorithm/admission/subject-req/batch` | 批量硬删除 |

**查询条件**: 标准代码、前端展示名称、涉及科目、requirement_type

## 5. Excel导入设计

### 5.1 导入流程

```
Excel文件上传
      ↓
第一次遍历：校验
├─ 检查表头是否符合规范
├─ 检查大学名是否都存在 → 缓存 Map<大学名, universityId>
├─ 检查科类枚举值（理科/物理类/文科/历史类/不分文理）
├─ 检查批次枚举值（本科批/提前批/专科批）
├─ 检查同一专业组内major_code是否重复
└─ 收集所有错误到 List<String> errors
      ↓
errors.isEmpty() ?
  ├─ 否 → 抛出异常，返回所有错误
  └─ 是 → 第二次遍历：插入数据
              ↓
按 (大学名+年份+省份+科类+批次+专业组代码) 分组
对每个分组：
├─ 查询专业组是否已存在
│   ├─ 不存在 → 创建新专业组记录
│   └─ 存在 → 使用已有group_id
└─ 插入该组所有专业明细记录
      ↓
数据库触发器自动计算聚合数据
      ↓
返回导入成功
```

### 5.2 Excel列映射

| Excel列名 | 字段 | 归属表 | 必填 |
|-----------|------|--------|------|
| 大学名 | universityName | Group | ✓ |
| 年份 | year | Group | ✓ |
| 省份 | province | Group | ✓ |
| 科类 | subjectType | Group | ✓ |
| 批次 | batch | Group | ✓ |
| 省招代码 | enrollmentCode | Group | |
| 专业组代码 | groupCode | Group | ✓ |
| 专业组简介 | groupDescription | Group | |
| 专业代码 | majorCode | MajorScore | ✓ |
| 专业名称 | majorName | MajorScore | ✓ |
| 选科要求 | subjectRequirements | MajorScore | |
| 层次 | educationLevel | MajorScore | |
| 学费 | tuition | MajorScore | |
| 专业简介 | majorDescription | MajorScore | |
| 录取人数 | admissionCount | MajorScore | |
| 最低分 | minScore | MajorScore | |
| 中位分 | avgScore | MajorScore | |
| 最高分 | maxScore | MajorScore | |
| 最低位次 | minRank | MajorScore | |
| 中位位次 | avgRank | MajorScore | |
| 最高位次 | maxRank | MajorScore | |

### 5.3 分组Key生成

```java
String groupKey = String.format("%s_%d_%s_%s_%s_%s",
    universityName, year, province, subjectType, batch, groupCode);
```

## 6. 错误处理

### 6.1 校验错误

| 场景 | 处理方式 | 错误信息示例 |
|------|----------|--------------|
| 大学不存在 | 记录错误，校验阶段终止 | 第3行: 大学[XX大学]不存在 |
| 科类值非法 | 记录错误 | 第5行: 科类[文理科]不合法，只允许：理科/物理类/文科/历史类/不分文理 |
| 批次值非法 | 记录错误 | 第7行: 批次[第一批]不合法，只允许：本科批/提前批/专科批 |
| 同组专业代码重复 | 记录错误 | 第10行: 专业代码[080901]在同一专业组内重复（与第8行冲突） |
| 选科要求找不到 | 不报错，默认level=0 | - |
| 必填字段为空 | 记录错误 | 第12行: 专业代码不能为空 |

### 6.2 事务保证

- `importAdmissionData()` 方法加 `@Transactional(rollbackFor = Exception.class)`
- 校验阶段发现任何错误 → 抛出 `BusinessException`，不插入任何数据
- 插入阶段异常 → 自动回滚所有已插入数据

## 7. 关键决策记录

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 聚合计算位置 | 数据库触发器 | 保证数据一致性，任何途径修改明细都会自动重算 |
| 主表创建方式 | 自动创建 | 一个xlsx写两表是核心需求 |
| 大学不存在处理 | 两次验证 | 先全部校验通过再插入，更严谨 |
| 专业代码唯一性 | 同组内唯一 | 业务逻辑要求 |
| 选科要求找不到 | 默认level=0 | 不影响导入流程 |
| 字典表删除策略 | 硬删除 | 字典表数据量小，无需软删除 |

## 8. 文件清单

### 8.1 Flyway迁移

- `V11__create_admission_group.sql`

### 8.2 Entity（haifeng-common）

- `entity/algorithm/AdmissionGroup.java`
- `entity/algorithm/AdmissionMajorScore.java`
- `entity/algorithm/SubjectReqDict.java`

### 8.3 Mapper（haifeng-common）

- `mapper/algorithm/AdmissionGroupMapper.java`
- `mapper/algorithm/AdmissionMajorScoreMapper.java`
- `mapper/algorithm/SubjectReqDictMapper.java`

### 8.4 Controller（haifeng-admin）

- `controller/algorithm/admission/AdmissionGroupController.java`
- `controller/algorithm/admission/AdmissionMajorScoreController.java`
- `controller/algorithm/admission/SubjectReqDictController.java`

### 8.5 Service（haifeng-admin）

- `service/algorithm/admission/AdmissionGroupService.java`
- `service/algorithm/admission/AdmissionMajorScoreService.java`
- `service/algorithm/admission/SubjectReqDictService.java`
- `service/impl/algorithm/admission/AdmissionGroupServiceImpl.java`
- `service/impl/algorithm/admission/AdmissionMajorScoreServiceImpl.java`
- `service/impl/algorithm/admission/SubjectReqDictServiceImpl.java`

### 8.6 DTO（haifeng-admin）

- `dto/algorithm/admission/AdmissionGroupQueryDTO.java`
- `dto/algorithm/admission/AdmissionGroupAddDTO.java`
- `dto/algorithm/admission/AdmissionMajorScoreQueryDTO.java`
- `dto/algorithm/admission/AdmissionMajorScoreAddDTO.java`
- `dto/algorithm/admission/SubjectReqDictQueryDTO.java`
- `dto/algorithm/admission/SubjectReqDictAddDTO.java`

### 8.7 VO（haifeng-admin）

- `vo/algorithm/admission/AdmissionGroupListVO.java`
- `vo/algorithm/admission/AdmissionGroupDetailVO.java`
- `vo/algorithm/admission/AdmissionMajorScoreListVO.java`
- `vo/algorithm/admission/AdmissionMajorScoreDetailVO.java`
- `vo/algorithm/admission/SubjectReqDictListVO.java`
- `vo/algorithm/admission/SubjectReqDictDetailVO.java`

### 8.8 Excel DTO（haifeng-admin）

- `excel/algorithm/admission/AdmissionImportDTO.java`
