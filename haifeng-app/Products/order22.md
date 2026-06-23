# 招聘内容管理 API 文档

## 概述

实现 App 端"招聘内容管理"模块的查询功能，包含两个子模块：

| 模块 | 表 | 说明 |
|------|-----|------|
| 统一备考指南 | t_exam_guide | 全平台备考文章/经验/技巧 |
| 统一公告 | t_notice | 全平台招考/招聘公告 |

## 基本信息

- **Base Path**: `/api/v1/app/employment/content`
- **统一响应格式**: `R<T>` 包裹
- **认证**: 部分接口需 `@RequireLogin`（需在请求头携带 Access Token）
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

## 一、统一备考指南

### 1.1 分页列表

获取备考指南分页列表，支持模糊搜索和精确筛选。

- **URL**: `/api/v1/app/employment/content/exam-guide/list`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 文章标题 |
| subtitle | String | 否 | 模糊 LIKE | 副标题 |
| guideCategory | String | 否 | 精确 EQ | 指南类别 |
| guideType | String | 否 | 精确 EQ | 指南类型 |
| difficultyLevel | String | 否 | 精确 EQ | 难度 |
| authorTitle | String | 否 | 精确 EQ | 作者头衔 |
| authorName | String | 否 | 精确 EQ | 作者名 |

> **查询逻辑**:
> - 基础条件：`is_deleted = false`
> - 模糊字段（title, subtitle）：多个模糊字段之间用 **OR** 连接
> - 精确字段（guideCategory, guideType, difficultyLevel, authorTitle, authorName）：多个精确字段之间用 **AND** 连接
> - 模糊与精确之间用 **AND** 连接
> - 排序：`sort_order DESC NULLS LAST, created_at DESC`

#### 响应数据（ExamGuideListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| guideCategory | String | 指南类别 |
| guideType | String | 指南类型 |
| title | String | 文章标题 |
| subtitle | String | 副标题 |
| tags | String[] | 标签列表（TEXT[]） |
| authorName | String | 作者名 |
| authorTitle | String | 作者头衔 |

#### 请求示例

```
GET /api/v1/app/employment/content/exam-guide/list?page=1&size=20&guideCategory=civil&title=公务员
```

---

### 1.2 查询详情

根据 ID 获取备考指南详情。

- **URL**: `/api/v1/app/employment/content/exam-guide/{id}`
- **Method**: `GET`
- **Auth**: 需要登录（`@RequireLogin`）

#### 路径参数

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| id | Long | 是 | 备考指南ID |

#### 响应数据（ExamGuideDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| guideCategory | String | 指南类别 |
| guideType | String | 指南类型 |
| title | String | 文章标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图片URL |
| iconClass | String | 图标CSS类名 |
| summary | String | 摘要 |
| content | String | 详细内容（HTML） |
| tags | String[] | 标签列表（TEXT[]） |
| difficultyLevel | String | 难度（入门/进阶/高阶） |
| targetAudience | String | 目标读者 |
| authorName | String | 作者名 |
| authorTitle | String | 作者头衔 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否编辑推荐 |
| sortOrder | Integer | 排序权重 |
| viewCount | Integer | 阅读量 |
| likeCount | Integer | 点赞数 |
| createdAt | OffsetDateTime | 创建时间 |
| updatedAt | OffsetDateTime | 更新时间 |

#### 请求示例

```
GET /api/v1/app/employment/content/exam-guide/1891234567890123456
Authorization: Bearer <access_token>
```

---

### 备考指南枚举值

#### guideCategory（指南类别）

| 值 | 说明 |
|----|------|
| civil | 公务员 |
| institution | 事业单位 |
| military | 军队文职 |
| selection | 选调生 |
| teacher | 教师招聘 |
| healthcare | 医疗卫生 |
| finance | 金融银行 |
| grassroots | 基层服务 |
| community | 社区工作者 |
| general | 通用/其他 |

#### guideType（指南类型）

| 值 | 说明 |
|----|------|
| 备考攻略 | 备考攻略 |
| 科目指导 | 科目指导 |
| 真题解析 | 真题解析 |
| 面试技巧 | 面试技巧 |
| 时事热点 | 时事热点 |
| 经验分享 | 经验分享 |
| 政策解读 | 政策解读 |
| 学习计划 | 学习计划 |

#### difficultyLevel（难度）

| 值 | 说明 |
|----|------|
| 入门 | 入门 |
| 进阶 | 进阶 |
| 高阶 | 高阶 |

---

## 二、统一公告

### 2.1 分页列表

获取统一公告分页列表，支持模糊搜索和精确筛选。

- **URL**: `/api/v1/app/employment/content/notice/list`
- **Method**: `GET`
- **Auth**: 无需登录

#### 请求参数（Query String）

| 参数 | 类型 | 必须 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，默认10 |
| title | String | 否 | 模糊 LIKE | 公告标题 |
| summary | String | 否 | 模糊 LIKE | 摘要 |
| source | String | 否 | 模糊 LIKE | 发布来源 |
| noticeCategory | String | 否 | 精确 EQ | 公告类别 |
| noticeType | String | 否 | 精确 EQ | 公告类型 |
| province | String | 否 | 精确 EQ | 省份 |
| city | String | 否 | 精确 EQ | 城市 |
| year | String | 否 | 精确 EQ | 年份 |

> **查询逻辑**:
> - 基础条件：`is_deleted = false`
> - 模糊字段（title, summary, source）：多个模糊字段之间用 **OR** 连接
> - 精确字段（noticeCategory, noticeType, province, city, year）：多个精确字段之间用 **AND** 连接
> - 模糊与精确之间用 **AND** 连接
> - 排序：`is_top DESC, publish_date DESC NULLS LAST`

#### 响应数据（NoticeListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| title | String | 公告标题 |
| summary | String | 摘要 |
| publishDate | OffsetDateTime | 发布日期 |
| viewCount | Integer | 阅读量 |
| noticeCategory | String | 公告类别 |
| province | String | 省份 |
| city | String | 城市 |
| year | String | 年份 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| recruitmentCount | Integer | 招录总人数 |

#### 请求示例

```
GET /api/v1/app/employment/content/notice/list?page=1&size=20&noticeCategory=civil&year=2026
```

---

### 2.2 查询详情

根据 ID 获取统一公告详情。

- **URL**: `/api/v1/app/employment/content/notice/{id}`
- **Method**: `GET`
- **Auth**: 需要登录（`@RequireLogin`）

#### 路径参数

| 参数 | 类型 | 必须 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

#### 响应数据（NoticeDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| noticeCategory | String | 公告类别 |
| noticeType | String | 公告类型 |
| title | String | 公告标题 |
| summary | String | 摘要 |
| content | String | 公告内容（HTML） |
| province | String | 省份 |
| city | String | 城市 |
| tags | String[] | 标签列表（TEXT[]） |
| year | String | 年份 |
| source | String | 发布来源 |
| sourceUrl | String | 原文链接 |
| publishDate | OffsetDateTime | 发布日期 |
| publishUnit | String | 发布单位 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名结束日期 |
| examTime | OffsetDateTime | 考试时间 |
| recruitmentCount | Integer | 招录总人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| viewCount | Integer | 阅读量 |
| createdAt | OffsetDateTime | 创建时间 |
| updatedAt | OffsetDateTime | 更新时间 |

#### 请求示例

```
GET /api/v1/app/employment/content/notice/1891234567890654321
Authorization: Bearer <access_token>
```

---

### 公告枚举值

#### noticeCategory（公告类别）

| 值 | 说明 |
|----|------|
| civil | 公务员 |
| institution | 事业单位 |
| military | 军队文职 |
| selection | 选调生 |
| teacher | 教师招聘 |
| healthcare | 医疗卫生 |
| finance | 金融银行 |
| grassroots | 基层服务 |
| community | 社区工作者 |
| public_welfare | 公益岗位 |
| enterprise | 国企/名企 |
| general | 通用/其他 |

#### noticeType（公告类型）

| 值 | 说明 |
|----|------|
| 招聘公告 | 招聘公告 |
| 招录公告 | 招录公告 |
| 补录公告 | 补录公告 |
| 调剂公告 | 调剂公告 |
| 成绩公示 | 成绩公示 |
| 面试通知 | 面试通知 |
| 体检通知 | 体检通知 |
| 录用公示 | 录用公示 |
| 报名指南 | 报名指南 |
| 考试大纲 | 考试大纲 |
| 政策解读 | 政策解读 |

---

## 错误码说明

| code | 说明 | 场景 |
|------|------|------|
| 200 | 成功 | - |
| 400 | 参数错误 | 分页参数不合法、ID格式错误 |
| 401 | 未登录/Token过期 | 访问需登录接口未传Token |
| 404 | 资源不存在 | 查询详情时ID不存在或已软删除 |

---

## Java 包路径参考

### haifeng-common

| 类 | 路径 |
|----|------|
| ExamGuide Entity | `com.haifeng.common.entity.employment.contentManagement.ExamGuide` |
| Notice Entity | `com.haifeng.common.entity.employment.contentManagement.Notice` |
| ExamGuideMapper | `com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper` |
| NoticeMapper | `com.haifeng.common.mapper.employment.contentManagement.NoticeMapper` |

### haifeng-app

| 类 | 路径 |
|----|------|
| ExamGuideController | `com.haifeng.app.controller.employment.contentManagement.examGuide.ExamGuideController` |
| NoticeController | `com.haifeng.app.controller.employment.contentManagement.notice.NoticeController` |
| ExamGuideService | `com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService` |
| NoticeService | `com.haifeng.app.service.employment.contentManagement.notice.NoticeService` |
| ExamGuideServiceImpl | `com.haifeng.app.service.impl.employment.contentManagement.examGuide.ExamGuideServiceImpl` |
| NoticeServiceImpl | `com.haifeng.app.service.impl.employment.contentManagement.notice.NoticeServiceImpl` |
| ExamGuideQueryDTO | `com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO` |
| NoticeQueryDTO | `com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO` |
| ExamGuideListVO | `com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO` |
| ExamGuideDetailVO | `com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO` |
| NoticeListVO | `com.haifeng.app.vo.employment.contentManagement.notice.NoticeListVO` |
| NoticeDetailVO | `com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO` |
