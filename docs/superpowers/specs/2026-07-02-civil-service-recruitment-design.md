# 体制内招录模块设计文档

## 概述

在现有就业管理模块下新增 `civilService` 子模块，包含公务员、事业编、部队文职、选调生四个职位类型的后台管理功能。

数据层（Entity/Mapper/Flyway）已在 `haifeng-common` 中完成，本次实现 admin 端的 Controller/Service/DTO/VO/Excel 导入层。

## 包结构

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/employment/civilService/
│   ├── CivilPositionController.java
│   ├── InstitutionPositionController.java
│   ├── MilitaryPositionController.java
│   └── SelectionPositionController.java
├── service/employment/civilService/
│   ├── CivilPositionService.java
│   ├── InstitutionPositionService.java
│   ├── MilitaryPositionService.java
│   └── SelectionPositionService.java
├── service/impl/employment/civilService/
│   ├── CivilPositionServiceImpl.java
│   ├── InstitutionPositionServiceImpl.java
│   ├── MilitaryPositionServiceImpl.java
│   └── SelectionPositionServiceImpl.java
├── dto/employment/civilService/
│   ├── CivilPositionQueryDTO.java
│   ├── CivilPositionUpdateDTO.java
│   ├── InstitutionPositionQueryDTO.java
│   ├── InstitutionPositionUpdateDTO.java
│   ├── MilitaryPositionQueryDTO.java
│   ├── MilitaryPositionUpdateDTO.java
│   ├── SelectionPositionQueryDTO.java
│   └── SelectionPositionUpdateDTO.java
├── vo/employment/civilService/
│   ├── CivilPositionListVO.java
│   ├── CivilPositionDetailVO.java
│   ├── InstitutionPositionListVO.java
│   ├── InstitutionPositionDetailVO.java
│   ├── MilitaryPositionListVO.java
│   ├── MilitaryPositionDetailVO.java
│   ├── SelectionPositionListVO.java
│   └── SelectionPositionDetailVO.java
└── excel/employment/civilService/
    ├── CivilPositionExcelDTO.java
    ├── InstitutionPositionExcelDTO.java
    ├── MilitaryPositionExcelDTO.java
    └── SelectionPositionExcelDTO.java
```

StatusDTO 复用已有 `com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO`。

## API 接口

### 通用接口（每张表 8 个）

| 方法 | 路径 | 功能 | 参数 |
|------|------|------|------|
| GET | `/list` | 分页查询 | QueryDTO |
| GET | `/{id}/detail` | 详情 | path id |
| PUT | `/{id}/update` | 修改 | path id + UpdateDTO |
| DELETE | `/{id}/delete` | 物理删除 | path id |
| PATCH | `/{id}/status` | 启用/禁用 | path id + StatusDTO |
| DELETE | `/batch-delete` | 批量物理删除 | List<Long> ids |
| POST | `/pre-validate` | Excel 预校验 | MultipartFile |
| POST | `/import` | 导入 Excel | MultipartFile |

### URL 映射

| Controller | RequestMapping |
|-----------|---------------|
| CivilPositionController | `/api/v1/admin/employment/civil-service/civil-position` |
| InstitutionPositionController | `/api/v1/admin/employment/civil-service/institution-position` |
| MilitaryPositionController | `/api/v1/admin/employment/civil-service/military-position` |
| SelectionPositionController | `/api/v1/admin/employment/civil-service/selection-position` |

## 分页列表字段

### CivilPosition（公务员）

**ListVO 字段：** id, positionName, examType, recruitingDept, minEducation, workLocation, regStartDate, regEndDate, regStatus

**查询条件：**
- 模糊：positionName, recruitingDept, workLocation
- 精确：examType, regStatus, minEducation

### InstitutionPosition（事业编）

**ListVO 字段：** id, positionName, supervisingDept, institution, province, examCategory, positionType, subCategory, salaryRange, positionStatus

**查询条件：**
- 模糊：positionName, supervisingDept, institution
- 精确：province, examCategory, positionType, positionStatus

### MilitaryPosition（部队文职）

**ListVO 字段：** id, positionName, employerUnit, department, positionType, workLocation, salaryRange, regDeadline, positionStatus

**查询条件：**
- 模糊：positionName, employerUnit, department
- 精确：positionType, positionStatus

### SelectionPosition（选调生）

**ListVO 字段：** id, positionName, selectionType, year, province, organizingDept, targetUnit, workLocation, politicalStatus, regStartDate, regEndDate, positionStatus

**查询条件：**
- 模糊：positionName, targetUnit, organizingDept
- 精确：selectionType, year, province, politicalStatus, positionStatus

## 排序规则

统一：`sort_order ASC, updated_at DESC`

## Excel 导入

### 特殊字段处理（String[] 数组字段）

复用 `com.haifeng.admin.converter.StringArrayConverter`：

| ExcelDTO | 数组字段 |
|----------|---------|
| InstitutionPositionExcelDTO | majorRequirements |
| MilitaryPositionExcelDTO | responsibilities, qualifications |
| SelectionPositionExcelDTO | majorCategories, targetUniversities |

### 校验规则

- positionName 不能为空
- province 字段（如存在）使用 `ProvinceEnum.isValid()` 校验
- 其他必填字段非空检查

### 事务

import 方法统一加 `@Transactional(rollbackFor = Exception.class)`，出错全部回滚。

## 注解

- Controller 类加 `@RequireLogin`（需要登录）
- update/delete/status/batch-delete/import 方法加 `@OperationLog(module = "体制内招录", action = "...")`
- update/import 方法走 `@Transactional(rollbackFor = Exception.class)`

## 操作日志 action 命名

| 表 | module | action |
|----|--------|--------|
| CivilPosition | 体制内招录 | 修改公务员职位 / 删除公务员职位 / 启用禁用公务员职位 / 批量删除公务员职位 / 导入公务员职位 |
| InstitutionPosition | 体制内招录 | 修改事业编职位 / 删除事业编职位 / 启用禁用事业编职位 / 批量删除事业编职位 / 导入事业编职位 |
| MilitaryPosition | 体制内招录 | 修改部队文职岗位 / 删除部队文职岗位 / 启用禁用部队文职岗位 / 批量删除部队文职岗位 / 导入部队文职岗位 |
| SelectionPosition | 体制内招录 | 修改选调生岗位 / 删除选调生岗位 / 启用禁用选调生岗位 / 批量删除选调生岗位 / 导入选调生岗位 |
