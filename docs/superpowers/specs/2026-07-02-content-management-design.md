# 招聘内容管理模块 - 设计文档

## 概述

招聘内容管理模块包含两个核心实体：
1. **统一备考指南 (t_exam_guide)** — 全平台备考文章/经验/技巧
2. **统一公告 (t_notice)** — 全平台招考/招聘公告

两者均为纯内容型（文章+公告），不涉及岗位业务逻辑，独立管理后台。

## 技术方案

复用现有 `TeacherPosition` 的成熟模式，为 ExamGuide 和 Notice 各创建一套完整的 Controller/Service/DTO/VO/Excel。

### 包结构

```
com.haifeng.admin/
├── controller/employment/contentManagement/
│   ├── ExamGuideController.java
│   └── NoticeController.java
├── service/employment/contentManagement/
│   ├── ExamGuideService.java
│   └── NoticeService.java
├── service/impl/employment/contentManagement/
│   ├── ExamGuideServiceImpl.java
│   └── NoticeServiceImpl.java
├── dto/employment/contentManagement/
│   ├── guide/
│   │   ├── ExamGuideQueryDTO.java
│   │   ├── ExamGuideUpdateDTO.java
│   │   └── ExamGuideStatusDTO.java
│   └── notice/
│       ├── NoticeQueryDTO.java
│       ├── NoticeUpdateDTO.java
│       └── NoticeStatusDTO.java
├── vo/employment/contentManagement/
│   ├── guide/
│   │   ├── ExamGuideListVO.java
│   │   └── ExamGuideDetailVO.java
│   └── notice/
│       ├── NoticeListVO.java
│       └── NoticeDetailVO.java
└── excel/employment/contentManagement/
    ├── ExamGuideExcelDTO.java
    └── NoticeExcelDTO.java
```

## 接口设计（各8个，共16个）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页查询 |
| GET | /{id}/detail | 详情 |
| PUT | /{id}/update | 修改 |
| DELETE | /{id}/delete | 物理删除 |
| PATCH | /{id}/status | 启用/禁用(is_deleted) |
| DELETE | /batch-delete | 批量物理删除 |
| POST | /pre-validate | Excel预校验 |
| POST | /import | 导入xlsx |

### URL前缀
- ExamGuide: `/api/v1/admin/employment/content-management/exam-guide`
- Notice: `/api/v1/admin/employment/content-management/notice`

## 查询逻辑

### ExamGuide分页
- 排序：`sort_order DESC, created_at DESC`
- 过滤：`is_deleted = false`
- 精确：guide_category, guide_type, is_top
- 模糊：title, subtitle
- 精确与模糊之间为 **AND** 关系
- ListVO字段：id, guideCategory, guideType, title, subtitle, isTop, isRecommended, viewCount, likeCount, sortOrder

### Notice分页
- 排序：`sort_order DESC, created_at DESC`
- 过滤：`is_deleted = false`
- 精确：notice_category, notice_type, province, city, year, is_top, is_important
- 模糊：title
- 精确与模糊之间为 **AND** 关系
- ListVO字段：id, title, noticeCategory, noticeType, province, city, year, isTop, isImportant, viewCount, sortOrder

## 状态控制

status 参数：0=禁用(is_deleted=true)，1=启用(is_deleted=false)

## Excel导入

- tags字段使用逗号分隔的字符串导入，程序解析为 `String[]`
- 其余字段直接映射
- 使用雪花算法生成ID
- 必填校验：guideCategory + title (ExamGuide), noticeCategory + title (Notice)

## 安全与日志

- 所有接口需 `@RequireLogin`
- 写操作接口加 `@OperationLog(module="招聘内容管理")`
- Service层使用 `@Slf4j` 记录关键操作日志
