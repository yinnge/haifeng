# 竞赛证书管理模块设计文档

## 概述

本模块实现竞赛证书管理功能，包括：
- 职业技能证书管理
- 科研竞赛管理（含详情）
- 竞赛-专业关联管理

**适用角色：** 仅限管理员

## 数据库设计

### 1. 证书表 (t_certificate)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花算法生成 |
| cert_name | VARCHAR(150) | NOT NULL, UNIQUE | 证书名称 |
| category | VARCHAR(50) | | 分类（IT类、财会类、语言类、工程类） |
| cert_level | VARCHAR(50) | | 等级（初级、中级、高级） |
| applicable_major | VARCHAR(200) | | 适用专业 |
| registration_time | VARCHAR(100) | | 报名时间（如：每年3月/9月） |
| exam_time | VARCHAR(100) | | 考试时间（如：5月中旬） |
| exam_fee | INTEGER | >= 0 | 考试费用（元） |
| cert_intro | TEXT | | 证书简介 |
| exam_requirements | TEXT[] | DEFAULT '{}' | 报考条件列表 |
| exam_arrangement | TEXT | | 考试安排详情 |
| official_website | VARCHAR(500) | | 官方网站链接 |
| is_deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 软删除标记 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 更新时间 |

**索引：**
- `idx_cert_category` - 分类索引（排除已删除）
- `idx_cert_level` - 等级索引（排除已删除）
- `idx_cert_name_search` - 名称模糊搜索索引
- `idx_cert_major` - 适用专业搜索索引

---

### 2. 竞赛表 (t_competition)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花算法生成 |
| comp_name | VARCHAR(200) | NOT NULL, UNIQUE | 竞赛名称 |
| comp_level | VARCHAR(50) | | 级别（国家级、省级、校级） |
| registration_time | VARCHAR(100) | | 报名时间 |
| is_deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 软删除标记 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 更新时间 |

**索引：**
- `idx_comp_level` - 级别索引（排除已删除）
- `idx_comp_name_search` - 名称模糊搜索索引

---

### 3. 竞赛详情表 (t_competition_detail) - 与竞赛表1:1

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花算法生成 |
| competition_id | BIGINT | NOT NULL, UNIQUE | 关联竞赛ID |
| basic_info | JSONB | DEFAULT '{}' | 基本信息 |
| awards | TEXT[] | DEFAULT '{}' | 奖项列表 |
| background | TEXT | | 竞赛背景与意义 |
| purposes | TEXT[] | DEFAULT '{}' | 竞赛目的 |
| competition_rules | JSONB | DEFAULT '[]' | 竞赛规则 |
| scoring_criteria | TEXT[] | DEFAULT '{}' | 评分标准 |
| notices | TEXT[] | DEFAULT '{}' | 注意事项 |
| process_guide | JSONB | DEFAULT '[]' | 参赛流程指南 |
| awards_display | JSONB | DEFAULT '[]' | 奖项设置展示 |
| is_deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 软删除标记 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 更新时间 |

**JSONB字段结构：**

```json
// basic_info
{
  "organizer": "教育部高等教育司",
  "hold_time": "每年9月-11月",
  "target": "全日制在校本科生及研究生",
  "participation_form": "团队",
  "level": "国家级",
  "reg_fee": "免费",
  "official_website": "https://xxx.edu.cn",
  "contact_email": "xxx@edu.cn",
  "contact_phone": "010-12345678"
}

// competition_rules / process_guide / awards_display
[
  { "title": "参赛资格", "content": "全日制在校生..." },
  { "title": "组队要求", "content": "每队3-5人..." }
]
```

---

### 4. 竞赛-专业关联表 (t_competition_major) - 多对多

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花算法生成 |
| competition_id | BIGINT | NOT NULL | 竞赛ID |
| major_id | BIGINT | NOT NULL | 专业ID |
| major_name | VARCHAR(100) | NOT NULL | 专业名称（冗余） |
| competition_name | VARCHAR(200) | NOT NULL | 竞赛名称（冗余） |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |

**约束：**
- `uk_comp_major` - UNIQUE(competition_id, major_id)

**索引：**
- `idx_cm_competition` - 竞赛ID索引
- `idx_cm_major` - 专业ID索引

**注意：** 此表无软删除，只有硬删除

---

## API接口设计

### 路由前缀

`/api/v1/admin/certificate/`

### 一、证书管理 (CertificateController)

#### 1.1 分页查询证书列表
```
GET /certificate/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| certName | String | 否 | 证书名称（模糊） |
| category | String | 否 | 分类 |
| certLevel | String | 否 | 等级 |
| applicableMajor | String | 否 | 适用专业（模糊） |
| isDeleted | Boolean | 否 | 是否已删除 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应字段（列表）：**
- id, certName, category, certLevel, applicableMajor, isDeleted

#### 1.2 获取证书详情
```
GET /certificate/{id}
```

**响应：** 证书全部字段

#### 1.3 新增证书
```
POST /certificate
```

**请求参数：** 除id、created_at、updated_at外的所有字段

#### 1.4 修改证书
```
PUT /certificate/{id}
```

**请求参数：** 同新增

#### 1.5 切换状态（禁用/启用）
```
PUT /certificate/{id}/toggle-status
```

**说明：** 切换 is_deleted 值

#### 1.6 硬删除单条
```
DELETE /certificate/{id}
```

#### 1.7 批量硬删除
```
DELETE /certificate/batch
```

**请求参数：**
```json
{ "ids": [1, 2, 3] }
```

---

### 二、竞赛管理 (CompetitionController)

#### 2.1 分页查询竞赛列表
```
GET /competition/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compName | String | 否 | 竞赛名称（模糊） |
| compLevel | String | 否 | 级别 |
| isDeleted | Boolean | 否 | 是否已删除 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应字段（列表）：**
- id, compName, compLevel, registrationTime, isDeleted

#### 2.2 获取竞赛+详情（tabs数据）
```
GET /competition/{id}
```

**响应结构：**
```json
{
  "competition": { /* 竞赛基本信息 */ },
  "detail": { /* 竞赛详情信息 */ }
}
```

#### 2.3 新增竞赛+详情（事务）
```
POST /competition
```

**请求结构：**
```json
{
  "competition": { /* 竞赛基本信息 */ },
  "detail": { /* 竞赛详情信息 */ }
}
```

**事务保证：** 竞赛和详情同时成功或同时失败

#### 2.4 修改竞赛+详情（事务）
```
PUT /competition/{id}
```

**请求结构：** 同新增

#### 2.5 切换状态（禁用/启用）
```
PUT /competition/{id}/toggle-status
```

**说明：** 同时切换竞赛表和详情表的 is_deleted 值

#### 2.6 硬删除竞赛+详情（事务）
```
DELETE /competition/{id}
```

**说明：** 同时删除竞赛和对应详情

#### 2.7 批量硬删除
```
DELETE /competition/batch
```

---

### 三、竞赛-专业关联 (CompetitionMajorController)

#### 3.1 分页查询关联列表
```
GET /competition-major/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| majorName | String | 否 | 专业名称（模糊） |
| competitionName | String | 否 | 竞赛名称（模糊） |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应字段（列表）：**
- id, majorName, competitionName, createdAt

#### 3.2 新增关联
```
POST /competition-major
```

**请求参数：**
```json
{
  "competitionName": "全国大学生数学建模竞赛",
  "majorName": "计算机科学与技术"
}
```

**业务逻辑：**
1. 根据 competitionName 查询 t_competition 获取 competition_id
2. 根据 majorName 查询 t_major 获取 major_id
3. 若任一不存在，返回错误
4. 检查是否已存在关联，存在则返回错误
5. 保存关联记录

#### 3.3 硬删除单条
```
DELETE /competition-major/{id}
```

#### 3.4 批量硬删除
```
DELETE /competition-major/batch
```

---

## 代码结构

### haifeng-common

```
entity/certificate/
├── Certificate.java
├── Competition.java
├── CompetitionDetail.java
└── CompetitionMajor.java

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
├── CompetitionMajorQueryDTO.java
└── CompetitionMajorAddDTO.java

vo/certificate/
├── CertificateListVO.java
├── CertificateDetailVO.java
├── CompetitionListVO.java
├── CompetitionDetailVO.java
├── CompetitionMajorListVO.java
└── CompetitionMajorDetailVO.java
```

### 数据库迁移

```
db/migration/V8__create_certificate_competition.sql
```

---

## 操作按钮说明

每条列表记录右侧显示三个按钮：

| 按钮 | 操作类型 | 说明 |
|------|----------|------|
| 详情 | 查看/修改 | 点击进入详情页，可查看和修改 |
| 禁用/启用 | 软删除切换 | 切换 is_deleted 状态，可恢复 |
| 删除 | 硬删除 | 物理删除，不可恢复 |

**竞赛-专业关联表例外：** 只有 详情 和 删除 按钮，无禁用功能

---

## 事务要求

竞赛模块需保证事务一致性：

1. **新增竞赛：** 同时创建 t_competition 和 t_competition_detail
2. **修改竞赛：** 同时更新两表
3. **删除竞赛：** 同时删除两表记录
4. **切换状态：** 同时更新两表的 is_deleted

使用 `@Transactional` 注解保证原子性

---

## 删除竞赛时的关联数据处理

当删除竞赛时，需要检查 `t_competition_major` 中是否存在关联记录：

- **硬删除竞赛：** 同时级联删除 `t_competition_major` 中该竞赛的所有关联记录
- **软删除（禁用）竞赛：** 不影响关联表，但关联表查询时应排除已禁用的竞赛

---

## 注意事项

1. 所有ID使用雪花算法生成
2. 时间字段使用 TIMESTAMPTZ（带时区）
3. 软删除使用 is_deleted 字段
4. 关联表无软删除，直接硬删除
5. 管理员操作自动记录操作日志（@OperationLog）
6. 删除竞赛时级联删除关联表中的记录
