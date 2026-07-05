# 体制内招录模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为公务员、事业编、部队文职、选调生4张表实现admin端分页/详情/修改/删除/禁用/批量删除/Excel导入功能

**Architecture:** 在employment模块下新增civilService子包，每张表独立Controller+Service+DTO+VO+ExcelDTO，完全复用现有CommunityPosition模式

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, 雪花ID

**参考实现:** `CommunityPositionController.java` 及其对应Service/Impl/DTO/VO/ExcelDTO

---

### 文件总览

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── dto/employment/civilService/   (8 files)
├── vo/employment/civilService/    (8 files)
├── excel/employment/civilService/ (4 files)
├── service/employment/civilService/ (4 files)
├── service/impl/employment/civilService/ (4 files)
└── controller/employment/civilService/ (4 files)
```

详细字段定义见设计文档 `docs/superpowers/specs/2026-07-02-civil-service-recruitment-design.md`

### Task 1: DTO/VO 层 (16个文件)
- CivilPositionQueryDTO / UpdateDTO
- InstitutionPositionQueryDTO / UpdateDTO
- MilitaryPositionQueryDTO / UpdateDTO
- SelectionPositionQueryDTO / UpdateDTO
- CivilPosition ListVO + DetailVO
- InstitutionPosition ListVO + DetailVO
- MilitaryPosition ListVO + DetailVO
- SelectionPosition ListVO + DetailVO

### Task 2: ExcelDTO 层 (4个文件)
- CivilPositionExcelDTO
- InstitutionPositionExcelDTO
- MilitaryPositionExcelDTO
- SelectionPositionExcelDTO

### Task 3: Service 接口层 (4个文件)
- CivilPositionService
- InstitutionPositionService
- MilitaryPositionService
- SelectionPositionService

### Task 4: ServiceImpl 层 (4个文件)
- CivilPositionServiceImpl
- InstitutionPositionServiceImpl
- MilitaryPositionServiceImpl
- SelectionPositionServiceImpl

### Task 5: Controller 层 (4个文件)
- CivilPositionController
- InstitutionPositionController
- MilitaryPositionController
- SelectionPositionController

### Task 6: 编译验证
- `mvn compile -pl haifeng-admin -am`
