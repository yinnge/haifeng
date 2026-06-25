# 体制内招录 API 文档

## 概述

实现 App 端"体制内招录"模块的查询功能，包含 4 类职位：

| 模块 | 表 | 说明 |
|------|-----|------|
| 公务员 | t_civil_position | 国考/省考招录职位 |
| 事业编 | t_institution_position | 事业单位招聘职位 |
| 部队文职 | t_military_position | 军队文职人员招聘岗位 |
| 选调生 | t_selection_position | 定向/非定向选调生招录岗位 |

## 基本信息

- **Base Path**: `/api/v1/app/employment/civil-service`
- **统一响应格式**: `R<T>` 包裹
- **认证**: 详情接口需 `@RequireLogin`（需在请求头携带 Access Token）
- **分页参数**: `page`（从1开始）、`size`（10/20/30/50/100）

### 响应结构

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1234567890
}
```

### 分页响应结构

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [],
    "total": 0,
    "size": 10,
    "current": 1,
    "pages": 0
  },
  "timestamp": 1234567890
}
```

---

## 一、公务员考试职位

### 1.1 分页列表

获取公务员考试职位分页列表，支持模糊搜索和精确筛选。

- **URL**: `/api/v1/app/employment/civil-service/position/list`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| keyword | String | 否 | 模糊 LIKE | 全局模糊搜索（positionName OR recruitingDept OR workLocation） |
| examType | String | 否 | 精确 EQ | 考试类型（国考/省考） |
| positionCode | String | 否 | 精确 EQ | 职位代码 |
| deptCode | String | 否 | 精确 EQ | 部门代码 |
| minEducation | String | 否 | 精确 EQ | 最低学历（不限/大专/本科/硕士/博士） |
| majorRequirement | String | 否 | 精确 EQ | 专业要求 |
| degreeRequirement | String | 否 | 精确 EQ | 学位要求（不限/学士/硕士/博士） |
| politicalStatus | String | 否 | 精确 EQ | 政治面貌（不限/中共党员/共青团员/群众） |
| examCategory | String | 否 | 精确 EQ | 考试类别 |

> **查询逻辑**:
> - 基础条件：`is_deleted = false`
> - 模糊字段（positionName, recruitingDept, workLocation）：通过 keyword 传入，多个模糊字段之间用 **OR** 连接
> - 精确字段（examType, positionCode, deptCode, minEducation, majorRequirement, degreeRequirement, politicalStatus, examCategory）：多个精确字段之间用 **AND** 连接
> - 模糊与精确之间用 **AND** 连接
> - 排序：`created_at DESC`

#### 响应数据（CivilPositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 职位名称 |
| examType | String | 考试类型（国考/省考） |
| recruitingDept | String | 招录部门 |
| minEducation | String | 最低学历 |
| majorRequirement | String | 专业要求 |
| degreeRequirement | String | 学位要求 |
| politicalStatus | String | 政治面貌 |
| examCategory | String | 考试类别 |
| workLocation | String | 工作地点 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| regStatus | String | 报名状态（报名中/已结束/即将开始） |
| applicantCount | Integer | 报名人数 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/position/list?page=1&size=20&keyword=财务&examType=国考
```

---

### 1.2 查询详情

根据 ID 获取公务员考试职位详情。

- **URL**: `/api/v1/app/employment/civil-service/position/{id}/detail`
- **Method**: `GET`
- **Auth**: 需要登录（`@RequireLogin`）

#### 路径参数

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| id | Long | 是 | 职位ID |

#### 响应数据（CivilPositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 职位名称 |
| examType | String | 考试类型 |
| recruitingDept | String | 招录部门 |
| deptCode | String | 部门代码 |
| positionCode | String | 职位代码 |
| affiliatedBureau | String | 隶属局/分局 |
| majorRequirement | String | 专业要求 |
| minEducation | String | 最低学历 |
| degreeRequirement | String | 学位要求 |
| politicalStatus | String | 政治面貌 |
| workExperience | String | 工作年限要求 |
| grassrootsExperience | String | 基层工作经历要求 |
| examCategory | String | 考试类别 |
| interviewRatio | String | 面试比例 |
| recruitmentCount | Integer | 招录人数 |
| hasProfessionalTest | Boolean | 是否有专业科目考试 |
| workLocation | String | 工作地点 |
| workLocationDetail | String | 工作地点详细地址 |
| householdRequirement | String | 户籍要求 |
| householdLocation | String | 户籍所在地 |
| positionIntro | String | 职位简介 |
| remark | String | 备注 |
| officialWebsite | String | 官方网站 |
| contactPhone | String | 咨询电话 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| regStatus | String | 报名状态 |
| applicantCount | Integer | 报名人数 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/position/1891234567890123456/detail
Authorization: Bearer <access_token>
```

---

### 1.3 备考指南

获取公务员考试备考指南列表。

- **URL**: `/api/v1/app/employment/civil-service/position/exam-guide`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| subtitle | String | 否 | 模糊 LIKE | 副标题 |
| guideType | String | 否 | 精确 EQ | 指南类型 |
| difficultyLevel | String | 否 | 精确 EQ | 难度等级 |
| authorTitle | String | 否 | 精确 EQ | 作者头衔 |
| authorName | String | 否 | 精确 EQ | 作者名称 |

> **查询逻辑**:
> - 固定条件：`guide_category = 'civil'` AND `is_deleted = false`
> - 标题和副标题模糊搜索之间用 **OR** 连接
> - 精确字段之间用 **AND** 连接
> - 排序：`sort_order DESC NULLS LAST`, `created_at DESC`

#### 响应数据（ExamGuideDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| guideCategory | String | 指南分类（固定为 civil） |
| guideType | String | 指南类型 |
| title | String | 标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图片 |
| iconClass | String | 图标样式类 |
| summary | String | 摘要 |
| content | String | 内容 |
| tags | String[] | 标签列表（TEXT[]） |
| difficultyLevel | String | 难度等级 |
| targetAudience | String | 目标受众 |
| authorName | String | 作者名称 |
| authorTitle | String | 作者头衔 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否推荐 |
| sortOrder | Integer | 排序序号 |
| viewCount | Integer | 浏览量 |
| likeCount | Integer | 点赞量 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/position/exam-guide?page=1&size=10&guideType=笔试
```

---

### 1.4 公告

获取公务员考试公告列表。

- **URL**: `/api/v1/app/employment/civil-service/position/notice`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| summary | String | 否 | 模糊 LIKE | 摘要 |
| source | String | 否 | 模糊 LIKE | 来源 |
| noticeType | String | 否 | 精确 EQ | 公告类型 |
| province | String | 否 | 精确 EQ | 省份 |
| city | String | 否 | 精确 EQ | 城市 |
| year | String | 否 | 精确 EQ | 年份 |

> **查询逻辑**:
> - 固定条件：`notice_category = 'civil'` AND `is_deleted = false`
> - 标题、摘要、来源模糊搜索之间用 **OR** 连接
> - 精确字段之间用 **AND** 连接
> - 排序：`is_top DESC`, `publish_date DESC NULLS LAST`

#### 响应数据（NoticeDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| noticeCategory | String | 公告分类（固定为 civil） |
| noticeType | String | 公告类型 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容 |
| province | String | 省份 |
| city | String | 城市 |
| tags | String[] | 标签列表（TEXT[]） |
| year | String | 年份 |
| source | String | 来源 |
| sourceUrl | String | 来源链接 |
| publishDate | OffsetDateTime | 发布日期 |
| publishUnit | String | 发布单位 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| examTime | OffsetDateTime | 考试时间 |
| recruitmentCount | Integer | 招录人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| viewCount | Integer | 浏览量 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/position/notice?page=1&size=10&year=2026
```

---

### 公务员枚举值

#### examType（考试类型）

| 值 | 说明 |
|----|------|
| 国考 | 国家公务员考试 |
| 省考 | 省级公务员考试 |

#### minEducation（最低学历）

| 值 | 说明 |
|----|------|
| 不限 | 不限 |
| 大专 | 大专 |
| 本科 | 本科 |
| 硕士 | 硕士 |
| 博士 | 博士 |

#### degreeRequirement（学位要求）

| 值 | 说明 |
|----|------|
| 不限 | 不限 |
| 学士 | 学士 |
| 硕士 | 硕士 |
| 博士 | 博士 |

#### politicalStatus（政治面貌）

| 值 | 说明 |
|----|------|
| 不限 | 不限 |
| 中共党员 | 中共党员 |
| 共青团员 | 共青团员 |
| 群众 | 群众 |

#### regStatus（报名状态）

| 值 | 说明 |
|----|------|
| 报名中 | 报名中 |
| 已结束 | 已结束 |
| 即将开始 | 即将开始 |

---

## 二、事业编职位

### 2.1 分页列表

获取事业编职位分页列表，支持模糊搜索和精确筛选。

- **URL**: `/api/v1/app/employment/civil-service/institution/list`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| keyword | String | 否 | 模糊 LIKE | 全局模糊搜索（positionName OR supervisingDept OR institution OR workLocation） |
| province | String | 否 | 精确 EQ | 省份 |
| examCategory | String | 否 | 精确 EQ | 考试类别 |
| positionType | String | 否 | 精确 EQ | 职位类型 |
| educationRequirement | String | 否 | 精确 EQ | 学历要求（无要求/大专/本科/硕士/博士） |
| degreeRequirement | String | 否 | 精确 EQ | 学位要求（无要求/学士/硕士/博士） |
| positionStatus | String | 否 | 精确 EQ | 职位状态（招聘中/已结束） |
| specialPosition | String | 否 | 精确 EQ | 特殊岗位标识 |
| ageLimit | Integer | 否 | 范围 GE | 年龄上限（>= 查询值） |

> **查询逻辑**:
> - 基础条件：`is_deleted = false`
> - 模糊字段（positionName, supervisingDept, institution, workLocation）：通过 keyword 传入，多个模糊字段之间用 **OR** 连接
> - 精确字段（province, examCategory, positionType, educationRequirement, degreeRequirement, positionStatus, specialPosition）：多个精确字段之间用 **AND** 连接
> - ageLimit：使用 `>=` 范围查询
> - 模糊与精确之间用 **AND** 连接
> - 排序：`created_at DESC`

#### 响应数据（InstitutionPositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 职位名称 |
| supervisingDept | String | 主管部门 |
| institution | String | 招聘单位 |
| workLocation | String | 工作地点 |
| province | String | 省份 |
| examCategory | String | 考试类别 |
| positionType | String | 职位类型 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招聘人数 |
| salaryRange | String | 薪资范围 |
| regDeadline | String | 报名截止时间 |
| specialPosition | String | 特殊岗位标识 |
| positionStatus | String | 职位状态 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/institution/list?page=1&size=20&province=广东省&positionType=专业技术岗
```

---

### 2.2 查询详情

根据 ID 获取事业编职位详情。

- **URL**: `/api/v1/app/employment/civil-service/institution/{id}/detail`
- **Method**: `GET`
- **Auth**: 需要登录（`@RequireLogin`）

#### 路径参数

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| id | Long | 是 | 职位ID |

#### 响应数据（InstitutionPositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 职位名称 |
| supervisingDept | String | 主管部门 |
| institution | String | 招聘单位 |
| workLocation | String | 工作地点 |
| province | String | 省份 |
| examCategory | String | 考试类别 |
| positionType | String | 职位类型 |
| subCategory | String | 职位子类 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招聘人数 |
| salaryRange | String | 薪资范围 |
| regDeadline | String | 报名截止时间 |
| majorRequirements | String[] | 专业要求列表（TEXT[]） |
| specialPosition | String | 特殊岗位标识 |
| otherRequirement | String | 其他要求 |
| otherRequirementDesc | String | 其他要求说明 |
| remarkType | String | 备注类型 |
| remarkDesc | String | 备注说明 |
| consultationPhone | String | 咨询电话 |
| supervisionPhone | String | 监督电话 |
| positionStatus | String | 职位状态 |
| positionTag | String | 职位标签（热门/无/急招） |
| tagText | String | 标签自定义文本 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/institution/1891234567890654321/detail
Authorization: Bearer <access_token>
```

---

### 2.3 备考指南

获取事业编考试备考指南列表。

- **URL**: `/api/v1/app/employment/civil-service/institution/exam-guide`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| subtitle | String | 否 | 模糊 LIKE | 副标题 |
| guideType | String | 否 | 精确 EQ | 指南类型 |
| difficultyLevel | String | 否 | 精确 EQ | 难度等级 |
| authorTitle | String | 否 | 精确 EQ | 作者头衔 |
| authorName | String | 否 | 精确 EQ | 作者名称 |

> **查询逻辑**: 同 1.3，固定条件 `guide_category = 'institution'`

#### 响应数据（ExamGuideDetailVO）

同 1.3，字段结构一致，`guideCategory` 固定为 `institution`。

#### 请求示例

```
GET /api/v1/app/employment/civil-service/institution/exam-guide?page=1&size=10
```

---

### 2.4 公告

获取事业编考试公告列表。

- **URL**: `/api/v1/app/employment/civil-service/institution/notice`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| summary | String | 否 | 模糊 LIKE | 摘要 |
| source | String | 否 | 模糊 LIKE | 来源 |
| noticeType | String | 否 | 精确 EQ | 公告类型 |
| province | String | 否 | 精确 EQ | 省份 |
| city | String | 否 | 精确 EQ | 城市 |
| year | String | 否 | 精确 EQ | 年份 |

> **查询逻辑**: 同 1.4，固定条件 `notice_category = 'institution'`

#### 响应数据（NoticeDetailVO）

同 1.4，字段结构一致，`noticeCategory` 固定为 `institution`。

#### 请求示例

```
GET /api/v1/app/employment/civil-service/institution/notice?page=1&size=10
```

---

### 事业编枚举值

#### educationRequirement（学历要求）

| 值 | 说明 |
|----|------|
| 无要求 | 无要求 |
| 大专 | 大专 |
| 本科 | 本科 |
| 硕士 | 硕士 |
| 博士 | 博士 |

#### degreeRequirement（学位要求）

| 值 | 说明 |
|----|------|
| 无要求 | 无要求 |
| 学士 | 学士 |
| 硕士 | 硕士 |
| 博士 | 博士 |

#### positionStatus（职位状态）

| 值 | 说明 |
|----|------|
| 招聘中 | 招聘中 |
| 已结束 | 已结束 |

#### positionTag（职位标签）

| 值 | 说明 |
|----|------|
| 热门 | 热门 |
| 无 | 无 |
| 急招 | 急招 |

---

## 三、部队文职岗位

### 3.1 分页列表

获取部队文职岗位分页列表，支持模糊搜索和精确筛选。

- **URL**: `/api/v1/app/employment/civil-service/military/list`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| keyword | String | 否 | 模糊 LIKE | 全局模糊搜索（positionName OR employerUnit OR department） |
| positionType | String | 否 | 精确 EQ | 岗位类型 |
| workLocation | String | 否 | 精确 EQ | 工作地点 |
| majorRequirement | String | 否 | 精确 EQ | 专业要求 |
| educationRequirement | String | 否 | 精确 EQ | 学历要求（本科及以上/硕士及以上/博士） |
| positionStatus | String | 否 | 精确 EQ | 岗位状态（进行中/已结束） |

> **查询逻辑**:
> - 基础条件：`is_deleted = false`
> - 模糊字段（positionName, employerUnit, department）：通过 keyword 传入，多个模糊字段之间用 **OR** 连接
> - 精确字段（positionType, workLocation, majorRequirement, educationRequirement, positionStatus）：多个精确字段之间用 **AND** 连接
> - 模糊与精确之间用 **AND** 连接
> - 排序：`created_at DESC`

#### 响应数据（MilitaryPositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 岗位名称 |
| employerUnit | String | 用人单位 |
| department | String | 所属部门 |
| positionType | String | 岗位类型 |
| majorRequirement | String | 专业要求 |
| educationRequirement | String | 学历要求 |
| workLocation | String | 工作地点 |
| salaryRange | String | 薪资范围 |
| regDeadline | String | 报名截止时间 |
| positionStatus | String | 岗位状态 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/military/list?page=1&size=20&keyword=参谋&positionType=行政管理
```

---

### 3.2 查询详情

根据 ID 获取部队文职岗位详情。

- **URL**: `/api/v1/app/employment/civil-service/military/{id}/detail`
- **Method**: `GET`
- **Auth**: 需要登录（`@RequireLogin`）

#### 路径参数

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| id | Long | 是 | 岗位ID |

#### 响应数据（MilitaryPositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 岗位名称 |
| employerUnit | String | 用人单位 |
| department | String | 所属部门 |
| positionType | String | 岗位类型 |
| workLocation | String | 工作地点 |
| salaryRange | String | 薪资范围 |
| majorRequirement | String | 专业要求 |
| educationRequirement | String | 学历要求 |
| regDeadline | String | 报名截止时间 |
| positionStatus | String | 岗位状态 |
| positionDescription | String | 岗位描述 |
| responsibilities | String[] | 岗位职责列表（TEXT[]） |
| qualifications | String[] | 任职资格列表（TEXT[]） |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/military/1891234567890654321/detail
Authorization: Bearer <access_token>
```

---

### 3.3 备考指南

获取部队文职考试备考指南列表。

- **URL**: `/api/v1/app/employment/civil-service/military/exam-guide`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| subtitle | String | 否 | 模糊 LIKE | 副标题 |
| guideType | String | 否 | 精确 EQ | 指南类型 |
| difficultyLevel | String | 否 | 精确 EQ | 难度等级 |
| authorTitle | String | 否 | 精确 EQ | 作者头衔 |
| authorName | String | 否 | 精确 EQ | 作者名称 |

> **查询逻辑**: 同 1.3，固定条件 `guide_category = 'military'`

#### 响应数据（ExamGuideDetailVO）

同 1.3，字段结构一致，`guideCategory` 固定为 `military`。

#### 请求示例

```
GET /api/v1/app/employment/civil-service/military/exam-guide?page=1&size=10
```

---

### 3.4 公告

获取部队文职考试公告列表。

- **URL**: `/api/v1/app/employment/civil-service/military/notice`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| summary | String | 否 | 模糊 LIKE | 摘要 |
| source | String | 否 | 模糊 LIKE | 来源 |
| noticeType | String | 否 | 精确 EQ | 公告类型 |
| province | String | 否 | 精确 EQ | 省份 |
| city | String | 否 | 精确 EQ | 城市 |
| year | String | 否 | 精确 EQ | 年份 |

> **查询逻辑**: 同 1.4，固定条件 `notice_category = 'military'`

#### 响应数据（NoticeDetailVO）

同 1.4，字段结构一致，`noticeCategory` 固定为 `military`。

#### 请求示例

```
GET /api/v1/app/employment/civil-service/military/notice?page=1&size=10
```

---

### 部队文职枚举值

#### educationRequirement（学历要求）

| 值 | 说明 |
|----|------|
| 本科及以上 | 本科及以上 |
| 硕士及以上 | 硕士及以上 |
| 博士 | 博士 |

#### positionStatus（岗位状态）

| 值 | 说明 |
|----|------|
| 进行中 | 进行中 |
| 已结束 | 已结束 |

---

## 四、选调生岗位

### 4.1 分页列表

获取选调生岗位分页列表，支持模糊搜索和精确筛选。

- **URL**: `/api/v1/app/employment/civil-service/selection/list`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| keyword | String | 否 | 模糊 LIKE | 全局模糊搜索（positionName OR targetUnit OR workLocation） |
| selectionType | String | 否 | 精确 EQ | 选调类型（定向选调/非定向选调/急需紧缺专业选调） |
| year | String | 否 | 精确 EQ | 年份 |
| province | String | 否 | 精确 EQ | 省份 |
| majorRequirement | String | 否 | 精确 EQ | 专业要求 |
| universityRequirement | String | 否 | 精确 EQ | 院校要求 |
| educationRequirement | String | 否 | 精确 EQ | 学历要求（本科/硕士/博士/本科及以上/硕士及以上） |
| degreeRequirement | String | 否 | 精确 EQ | 学位要求 |
| politicalStatus | String | 否 | 精确 EQ | 政治面貌 |
| positionStatus | String | 否 | 精确 EQ | 岗位状态（报名中/笔试阶段/面试阶段/已结束/即将开始） |
| ageLimit | Integer | 否 | 范围 GE | 年龄上限（>= 查询值） |

> **查询逻辑**:
> - 基础条件：`is_deleted = false`
> - 模糊字段（positionName, targetUnit, workLocation）：通过 keyword 传入，多个模糊字段之间用 **OR** 连接
> - 精确字段（selectionType, year, province, majorRequirement, universityRequirement, educationRequirement, degreeRequirement, politicalStatus, positionStatus）：多个精确字段之间用 **AND** 连接
> - ageLimit：使用 `>=` 范围查询
> - 模糊与精确之间用 **AND** 连接
> - 排序：`created_at DESC`

#### 响应数据（SelectionPositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 岗位名称 |
| selectionType | String | 选调类型 |
| year | String | 年份 |
| province | String | 省份 |
| organizingDept | String | 组织部门 |
| targetUnit | String | 录用单位 |
| workLocation | String | 工作地点 |
| majorRequirement | String | 专业要求 |
| universityRequirement | String | 院校要求 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| trainingDirection | String | 培养方向 |
| politicalStatus | String | 政治面貌 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招录人数 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| positionStatus | String | 岗位状态 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/selection/list?page=1&size=20&year=2026&province=广东省&selectionType=定向选调
```

---

### 4.2 查询详情

根据 ID 获取选调生岗位详情。

- **URL**: `/api/v1/app/employment/civil-service/selection/{id}/detail`
- **Method**: `GET`
- **Auth**: 需要登录（`@RequireLogin`）

#### 路径参数

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| id | Long | 是 | 岗位ID |

#### 响应数据（SelectionPositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| positionName | String | 岗位名称 |
| selectionType | String | 选调类型 |
| year | String | 年份 |
| province | String | 省份 |
| organizingDept | String | 组织部门 |
| targetUnit | String | 录用单位 |
| workLocation | String | 工作地点 |
| trainingDirection | String | 培养方向 |
| grassrootsServiceYears | String | 基层服务年限 |
| trainingPlan | String | 培养计划 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |
| majorCategories | String[] | 专业类别列表（TEXT[]） |
| universityRequirement | String | 院校要求 |
| targetUniversities | String[] | 目标院校列表（TEXT[]） |
| politicalStatus | String | 政治面貌 |
| studentCadreRequirement | String | 学生干部要求 |
| awardsRequirement | String | 奖励荣誉要求 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招录人数 |
| examSubjects | String | 考试科目 |
| interviewForm | String | 面试形式 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| examTime | OffsetDateTime | 考试时间 |
| applyLink | String | 报名链接 |
| positionStatus | String | 岗位状态 |
| remark | String | 备注 |
| contactPhone | String | 联系电话 |
| officialLink | String | 官方公告链接 |
| content | String | 详细内容 |

#### 请求示例

```
GET /api/v1/app/employment/civil-service/selection/1891234567890654321/detail
Authorization: Bearer <access_token>
```

---

### 4.3 备考指南

获取选调生考试备考指南列表。

- **URL**: `/api/v1/app/employment/civil-service/selection/exam-guide`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| subtitle | String | 否 | 模糊 LIKE | 副标题 |
| guideType | String | 否 | 精确 EQ | 指南类型 |
| difficultyLevel | String | 否 | 精确 EQ | 难度等级 |
| authorTitle | String | 否 | 精确 EQ | 作者头衔 |
| authorName | String | 否 | 精确 EQ | 作者名称 |

> **查询逻辑**: 同 1.3，固定条件 `guide_category = 'selection'`

#### 响应数据（ExamGuideDetailVO）

同 1.3，字段结构一致，`guideCategory` 固定为 `selection`。

#### 请求示例

```
GET /api/v1/app/employment/civil-service/selection/exam-guide?page=1&size=10
```

---

### 4.4 公告

获取选调生考试公告列表。

- **URL**: `/api/v1/app/employment/civil-service/selection/notice`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 标题 |
| summary | String | 否 | 模糊 LIKE | 摘要 |
| source | String | 否 | 模糊 LIKE | 来源 |
| noticeType | String | 否 | 精确 EQ | 公告类型 |
| province | String | 否 | 精确 EQ | 省份 |
| city | String | 否 | 精确 EQ | 城市 |
| year | String | 否 | 精确 EQ | 年份 |

> **查询逻辑**: 同 1.4，固定条件 `notice_category = 'selection'`

#### 响应数据（NoticeDetailVO）

同 1.4，字段结构一致，`noticeCategory` 固定为 `selection`。

#### 请求示例

```
GET /api/v1/app/employment/civil-service/selection/notice?page=1&size=10
```

---

### 选调生枚举值

#### selectionType（选调类型）

| 值 | 说明 |
|----|------|
| 定向选调 | 定向选调 |
| 非定向选调 | 非定向选调 |
| 急需紧缺专业选调 | 急需紧缺专业选调 |

#### educationRequirement（学历要求）

| 值 | 说明 |
|----|------|
| 本科 | 本科 |
| 硕士 | 硕士 |
| 博士 | 博士 |
| 本科及以上 | 本科及以上 |
| 硕士及以上 | 硕士及以上 |

#### politicalStatus（政治面貌）

| 值 | 说明 |
|----|------|
| 中共党员 | 中共党员 |
| 中共预备党员 | 中共预备党员 |
| 共青团员 | 共青团员 |
| 不限 | 不限 |

#### positionStatus（岗位状态）

| 值 | 说明 |
|----|------|
| 报名中 | 报名中 |
| 笔试阶段 | 笔试阶段 |
| 面试阶段 | 面试阶段 |
| 已结束 | 已结束 |
| 即将开始 | 即将开始 |

---

## 错误码说明

| code | 说明 | 场景 |
|------|------|------|
| 200 | 成功 | - |
| 400 | 参数错误 | 分页参数不合法、ID格式错误 |
| 401 | 未登录/Token过期 | 访问需登录接口未传Token |
| 404 | 资源不存在 | 查询详情时ID不存在或已软删除 |

---

## API 端点汇总

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/civil-service/position/list` | 否 | 公务员分页列表 |
| GET | `/api/v1/app/employment/civil-service/position/{id}/detail` | 是 | 公务员详情 |
| GET | `/api/v1/app/employment/civil-service/position/exam-guide` | 否 | 公务员备考指南 |
| GET | `/api/v1/app/employment/civil-service/position/notice` | 否 | 公务员考试公告 |
| GET | `/api/v1/app/employment/civil-service/institution/list` | 否 | 事业编分页列表 |
| GET | `/api/v1/app/employment/civil-service/institution/{id}/detail` | 是 | 事业编详情 |
| GET | `/api/v1/app/employment/civil-service/institution/exam-guide` | 否 | 事业编备考指南 |
| GET | `/api/v1/app/employment/civil-service/institution/notice` | 否 | 事业编公告 |
| GET | `/api/v1/app/employment/civil-service/military/list` | 否 | 部队文职分页列表 |
| GET | `/api/v1/app/employment/civil-service/military/{id}/detail` | 是 | 部队文职详情 |
| GET | `/api/v1/app/employment/civil-service/military/exam-guide` | 否 | 部队文职备考指南 |
| GET | `/api/v1/app/employment/civil-service/military/notice` | 否 | 部队文职公告 |
| GET | `/api/v1/app/employment/civil-service/selection/list` | 否 | 选调生分页列表 |
| GET | `/api/v1/app/employment/civil-service/selection/{id}/detail` | 是 | 选调生详情 |
| GET | `/api/v1/app/employment/civil-service/selection/exam-guide` | 否 | 选调生备考指南 |
| GET | `/api/v1/app/employment/civil-service/selection/notice` | 否 | 选调生公告 |

---

## Java 包路径参考

### haifeng-common

| 类 | 路径 |
|----|------|
| CivilPosition Entity | `com.haifeng.common.entity.employment.civilService.CivilPosition` |
| InstitutionPosition Entity | `com.haifeng.common.entity.employment.civilService.InstitutionPosition` |
| MilitaryPosition Entity | `com.haifeng.common.entity.employment.civilService.MilitaryPosition` |
| SelectionPosition Entity | `com.haifeng.common.entity.employment.civilService.SelectionPosition` |
| CivilPositionMapper | `com.haifeng.common.mapper.employment.civilService.CivilPositionMapper` |
| InstitutionPositionMapper | `com.haifeng.common.mapper.employment.civilService.InstitutionPositionMapper` |
| MilitaryPositionMapper | `com.haifeng.common.mapper.employment.civilService.MilitaryPositionMapper` |
| SelectionPositionMapper | `com.haifeng.common.mapper.employment.civilService.SelectionPositionMapper` |

### haifeng-app

| 类 | 路径 |
|----|------|
| CivilPositionController | `com.haifeng.app.controller.employment.civilService.CivilPositionController` |
| InstitutionPositionController | `com.haifeng.app.controller.employment.civilService.InstitutionPositionController` |
| MilitaryPositionController | `com.haifeng.app.controller.employment.civilService.MilitaryPositionController` |
| SelectionPositionController | `com.haifeng.app.controller.employment.civilService.SelectionPositionController` |
| CivilPositionService | `com.haifeng.app.service.employment.civilService.CivilPositionService` |
| InstitutionPositionService | `com.haifeng.app.service.employment.civilService.InstitutionPositionService` |
| MilitaryPositionService | `com.haifeng.app.service.employment.civilService.MilitaryPositionService` |
| SelectionPositionService | `com.haifeng.app.service.employment.civilService.SelectionPositionService` |
| CivilPositionServiceImpl | `com.haifeng.app.service.impl.employment.civilService.CivilPositionServiceImpl` |
| InstitutionPositionServiceImpl | `com.haifeng.app.service.impl.employment.civilService.InstitutionPositionServiceImpl` |
| MilitaryPositionServiceImpl | `com.haifeng.app.service.impl.employment.civilService.MilitaryPositionServiceImpl` |
| SelectionPositionServiceImpl | `com.haifeng.app.service.impl.employment.civilService.SelectionPositionServiceImpl` |
| CivilPositionSearchDTO | `com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO` |
| InstitutionPositionSearchDTO | `com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO` |
| MilitaryPositionSearchDTO | `com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO` |
| SelectionPositionSearchDTO | `com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO` |
| CivilPositionListVO | `com.haifeng.app.vo.employment.civilService.CivilPositionListVO` |
| CivilPositionDetailVO | `com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO` |
| InstitutionPositionListVO | `com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO` |
| InstitutionPositionDetailVO | `com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO` |
| MilitaryPositionListVO | `com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO` |
| MilitaryPositionDetailVO | `com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO` |
| SelectionPositionListVO | `com.haifeng.app.vo.employment.civilService.SelectionPositionListVO` |
| SelectionPositionDetailVO | `com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO` |
