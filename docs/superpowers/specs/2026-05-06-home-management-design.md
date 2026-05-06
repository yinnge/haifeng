# 首页管理模块设计文档

**日期**: 2026-05-06
**模块**: 首页管理 (home)
**子模块**: 公告列表、规划师列表、培训机构列表

---

## 1. 概述

实现后台管理系统的首页管理模块，包含三个子模块的CRUD功能：
- 公告列表 - 管理系统公告
- 规划师列表 - 管理高考志愿规划师信息
- 培训机构列表 - 管理合作培训机构信息

## 2. 数据库设计

### 2.1 公告表 (t_announcements)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花ID |
| title | VARCHAR(100) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 内容 |
| tag | VARCHAR(20) | - | 类型标签 |
| status | SMALLINT | DEFAULT 1 NOT NULL | 0-下架 1-展示 |
| is_deleted | BOOLEAN | DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() | 更新时间 |

**索引**:
- idx_announcements_title (title模糊查询)
- idx_announcements_status

### 2.2 规划师表 (t_planners)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花ID |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| position | VARCHAR(50) | - | 职位 |
| region | VARCHAR(20) | - | 省份 |
| avatar | VARCHAR(100) | - | 头像URL |
| specialty | VARCHAR(100) | - | 专长领域 |
| douyin_name | VARCHAR(100) | - | 抖音号名称 |
| douyin_url | VARCHAR(100) | - | 抖音主页链接 |
| personal_description | TEXT | - | 个人简介 |
| experience_job | TEXT | - | 从业经验 |
| achievements | TEXT[] | - | 主要成就(数组) |
| expertise_areas | TEXT[] | - | 擅长领域(数组) |
| sort_order | INTEGER | DEFAULT 0 | 排序(越小越靠前) |
| status | SMALLINT | DEFAULT 1 NOT NULL | 0-下架 1-展示 |
| is_deleted | BOOLEAN | DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() | 更新时间 |

**索引**:
- idx_planners_name (name模糊查询)
- idx_planners_status

### 2.3 培训机构表 (t_institutions)

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 雪花ID |
| name | VARCHAR(100) | NOT NULL | 机构名称 |
| type | VARCHAR(100) | NOT NULL | 机构类型 |
| phone | VARCHAR(20) | - | 联系电话 |
| address | VARCHAR(100) | - | 机构地址 |
| description | TEXT | - | 机构简介 |
| courses | TEXT[] | - | 课程列表(数组) |
| images | TEXT[] | - | 机构图片(数组) |
| logo | VARCHAR(200) | - | Logo URL |
| sort_order | INTEGER | DEFAULT 0 | 排序(越小越靠前) |
| status | SMALLINT | DEFAULT 1 NOT NULL | 0-下架 1-展示 |
| is_deleted | BOOLEAN | DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMPTZ | DEFAULT NOW() | 更新时间 |

**索引**:
- idx_institutions_name (name模糊查询)
- idx_institutions_type
- idx_institutions_status

## 3. API接口设计

### 3.1 路由前缀
`/api/v1/admin/home/`

### 3.2 公告管理接口

| 方法 | 路径 | 功能 | 操作日志 |
|------|------|------|----------|
| GET | /announcement/list | 分页列表 | - |
| GET | /announcement/{id} | 详情 | - |
| POST | /announcement | 新增 | @OperationLog |
| PUT | /announcement/{id} | 修改 | @OperationLog |
| PUT | /announcement/{id}/status | 上架/下架 | @OperationLog |
| DELETE | /announcement/{id} | 删除(软删除) | @OperationLog |

**查询参数** (AnnouncementQueryDTO):
- title: 标题模糊查询
- status: 状态筛选
- page, size: 分页

**列表返回** (AnnouncementListVO): id, title, tag, status, updatedAt
**详情返回** (AnnouncementDetailVO): 列表字段 + content

### 3.3 规划师管理接口

| 方法 | 路径 | 功能 | 操作日志 |
|------|------|------|----------|
| GET | /planner/list | 分页列表(按sort_order排序) | - |
| GET | /planner/{id} | 详情 | - |
| POST | /planner | 新增 | @OperationLog |
| PUT | /planner/{id} | 修改 | @OperationLog |
| PUT | /planner/{id}/status | 上架/下架 | @OperationLog |
| DELETE | /planner/{id} | 删除(软删除) | @OperationLog |

**查询参数** (PlannerQueryDTO):
- name: 姓名模糊查询
- status: 状态筛选
- page, size: 分页

**列表返回** (PlannerListVO): id, name, position, region, avatar, specialty, douyinName, douyinUrl, sortOrder, status
**详情返回** (PlannerDetailVO): 列表字段 + personalDescription, experienceJob, achievements[], expertiseAreas[]

### 3.4 培训机构管理接口

| 方法 | 路径 | 功能 | 操作日志 |
|------|------|------|----------|
| GET | /institution/list | 分页列表(按sort_order排序) | - |
| GET | /institution/{id} | 详情 | - |
| POST | /institution | 新增 | @OperationLog |
| PUT | /institution/{id} | 修改 | @OperationLog |
| PUT | /institution/{id}/status | 上架/下架 | @OperationLog |
| DELETE | /institution/{id} | 删除(软删除) | @OperationLog |

**查询参数** (InstitutionQueryDTO):
- name: 名称模糊查询
- type: 类型筛选
- status: 状态筛选
- page, size: 分页

**列表返回** (InstitutionListVO): id, name, type, phone, address, logo, sortOrder, status
**详情返回** (InstitutionDetailVO): 列表字段 + description, courses[], images[]

## 4. 文件清单

### 4.1 haifeng-common

```
entity/home/
├── Announcement.java
├── Planner.java
└── Institution.java

mapper/home/
├── AnnouncementMapper.java
├── PlannerMapper.java
└── InstitutionMapper.java
```

### 4.2 haifeng-admin

```
controller/home/
├── AnnouncementController.java
├── PlannerController.java
└── InstitutionController.java

service/home/
├── AnnouncementService.java
├── PlannerService.java
└── InstitutionService.java

service/impl/home/
├── AnnouncementServiceImpl.java
├── PlannerServiceImpl.java
└── InstitutionServiceImpl.java

dto/home/
├── AnnouncementQueryDTO.java
├── AnnouncementAddDTO.java
├── AnnouncementUpdateDTO.java
├── PlannerQueryDTO.java
├── PlannerAddDTO.java
├── PlannerUpdateDTO.java
├── InstitutionQueryDTO.java
├── InstitutionAddDTO.java
├── InstitutionUpdateDTO.java
└── StatusDTO.java

vo/home/
├── AnnouncementListVO.java
├── AnnouncementDetailVO.java
├── PlannerListVO.java
├── PlannerDetailVO.java
├── InstitutionListVO.java
└── InstitutionDetailVO.java
```

### 4.3 数据库迁移

```
db/migration/
└── V4__create_content_tables.sql
```

### 4.4 文档

```
Products/
└── order4.md
```

## 5. 实现要点

1. **ID生成**: 使用雪花算法生成BIGINT主键
2. **软删除**: is_deleted字段，查询时默认过滤已删除数据
3. **排序**: 规划师和培训机构按sort_order升序排列
4. **数组字段**: achievements/expertise_areas/courses/images使用PostgreSQL TEXT[]类型
5. **操作日志**: 所有写操作(POST/PUT/DELETE)使用@OperationLog注解
6. **分页**: 继承BasePageQueryDTO，支持10/20/30/50/100/200/500/1000条/页
