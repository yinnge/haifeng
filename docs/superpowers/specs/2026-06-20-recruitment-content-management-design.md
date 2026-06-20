# 招聘内容管理 - 设计文档

## 概述

实现 App 端"招聘内容管理"模块的查询功能，包含两张内容型表：统一备考指南（t_exam_guide）和统一公告（t_notice）。仅涉及 App 端只读查询接口，管理端 CRUD 后续实现。

## 包结构

```
haifeng-common/
└── entity/employment/contentManagement/
    ├── ExamGuide.java
    └── Notice.java
└── mapper/employment/contentManagement/
    ├── ExamGuideMapper.java
    └── NoticeMapper.java

haifeng-app/
└── controller/employment/contentManagement/
    ├── examGuide/ExamGuideController.java
    └── notice/NoticeController.java
└── service/employment/contentManagement/
    ├── examGuide/ExamGuideService.java
    └── notice/NoticeService.java
└── service/impl/employment/contentManagement/
    ├── examGuide/ExamGuideServiceImpl.java
    └── notice/NoticeServiceImpl.java
└── dto/employment/contentManagement/
    ├── examGuide/ExamGuideQueryDTO.java
    └── notice/NoticeQueryDTO.java
└── vo/employment/contentManagement/
    ├── examGuide/ExamGuideListVO.java
    ├── examGuide/ExamGuideDetailVO.java
    ├── notice/NoticeListVO.java
    └── notice/NoticeDetailVO.java
```

## Entity 设计

### ExamGuide.java
- 映射 `t_exam_guide`，`@TableName(value = "t_exam_guide", autoResultMap = true)`
- `tags` 字段类型：`String[]`（MyBatis-Plus 自动映射 PG `TEXT[]`）
- 使用 `SnowflakeIdGenerator` 生成 ID（`@TableId(type = IdType.INPUT)`）
- 审计字段：`isDeleted`, `createdAt`, `updatedAt`

### Notice.java
- 映射 `t_notice`，`@TableName(value = "t_notice", autoResultMap = true)`
- `tags` 字段类型：`String[]`
- ID 生成与审计字段同上

## API 接口设计

Base path: `/api/v1/app/employment/content`

### 统一备考指南

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/exam-guide/list` | ❌ | 分页列表 |
| GET | `/exam-guide/{id}` | ✅ @RequireLogin | 详情 |

### 统一公告

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/notice/list` | ❌ | 分页列表 |
| GET | `/notice/{id}` | ✅ @RequireLogin | 详情 |

## DTO 设计

### ExamGuideQueryDTO（继承 BasePageQueryDTO）
| 字段 | 类型 | 查询方式 | 说明 |
|------|------|----------|------|
| title | String | 模糊 LIKE | 文章标题 |
| subtitle | String | 模糊 LIKE | 副标题 |
| guideCategory | String | 精确 EQ | 指南类别 |
| guideType | String | 精确 EQ | 指南类型 |
| difficultyLevel | String | 精确 EQ | 难度 |
| authorTitle | String | 精确 EQ | 作者头衔 |
| authorName | String | 精确 EQ | 作者名 |

模糊字段（title, subtitle）之间用 OR 连接，精确字段用 AND 连接。

### NoticeQueryDTO（继承 BasePageQueryDTO）
| 字段 | 类型 | 查询方式 | 说明 |
|------|------|----------|------|
| title | String | 模糊 LIKE | 公告标题 |
| summary | String | 模糊 LIKE | 摘要 |
| source | String | 模糊 LIKE | 来源 |
| noticeCategory | String | 精确 EQ | 公告类别 |
| noticeType | String | 精确 EQ | 公告类型 |
| province | String | 精确 EQ | 省份 |
| city | String | 精确 EQ | 城市 |
| year | String | 精确 EQ | 年份 |

模糊字段（title, summary, source）之间用 OR 连接，精确字段用 AND 连接。

## VO 设计

### ExamGuideListVO
`id, guideCategory, guideType, title, subtitle, tags, authorName, authorTitle`

### ExamGuideDetailVO
`id, guideCategory, guideType, title, subtitle, coverImage, iconClass, summary, content, tags, difficultyLevel, targetAudience, authorName, authorTitle, isTop, isRecommended, sortOrder, viewCount, likeCount, createdAt, updatedAt`

### NoticeListVO
`id, title, summary, publishDate, viewCount, noticeCategory, province, city, year, regStartDate, regEndDate, recruitmentCount`

### NoticeDetailVO
`id, noticeCategory, noticeType, title, summary, content, province, city, tags, year, source, sourceUrl, publishDate, publishUnit, regStartDate, regEndDate, examTime, recruitmentCount, isTop, isImportant, viewCount, createdAt, updatedAt`

## 查询逻辑

### 列表查询
1. 基础条件：`is_deleted = false`
2. 模糊查询：使用 `like` 配合 `StrUtil.isNotBlank()` 判断，多个模糊字段用 `and(w -> w.like(...).or().like(...))`
3. 精确查询：使用 `eq` 配合 `StrUtil.isNotBlank()` 判断
4. 分页：使用 `Page<>(dto.getPage(), dto.getSize())`
5. 排序：
   - ExamGuide：`sortOrder DESC NULLS LAST, createdAt DESC`
   - Notice：`isTop DESC, publishDate DESC NULLS LAST`

### 详情查询
1. 根据 ID 查询
2. 检查是否存在且未软删除
3. 不存在则抛出 `BusinessException(NOT_FOUND)`

## 错误处理
- 资源不存在：`BusinessException(ResultCode.NOT_FOUND)`
- 参数校验异常：`MethodArgumentNotValidException` -> `GlobalExceptionHandler` 统一处理

## 注意事项
- `tags`（TEXT[]）直接用 `String[]` 在 VO 中返回，前端自行处理渲染
- 所有 Logger 使用 `@Slf4j` 注解
- 不使用 `System.out.println()`
- 敏感信息不打印日志
