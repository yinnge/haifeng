# 体制内招录模块 API 文档

## 概述

在就业管理模块下新增 `civilService` 子包，管理四种体制内招录职位类型：公务员（国考/省考）、事业编、部队文职、选调生。

**基础路径：** `/api/v1/admin/employment/civil-service/`

**认证要求：** 所有接口需要 `@RequireLogin`（JWT Token）

---

## 基础数据

### 考试类型 (exam_type)

| 值 | 说明 |
|----|------|
| 国考 | 国家公务员考试 |
| 省考 | 省级公务员考试 |

### 报名状态 (reg_status - CivilPosition)

| 值 | 说明 |
|----|------|
| 报名中 | 正在报名 |
| 已结束 | 报名已截止 |
| 即将开始 | 尚未开始 |

### 职位的状态 (position_status)

| 表 | 可选值 |
|----|--------|
| t_institution_position | 招聘中, 已结束 |
| t_military_position | 进行中, 已结束 |
| t_selection_position | 报名中, 笔试阶段, 面试阶段, 已结束, 即将开始 |

### 选调类型 (selection_type)

| 值 | 说明 |
|----|------|
| 定向选调 | 面向特定高校 |
| 非定向选调 | 面向所有符合条件高校 |
| 急需紧缺专业选调 | 面向特定专业 |

### 学历要求枚举

| 表 | 可选值 |
|----|--------|
| t_civil_position | 不限, 大专, 本科, 硕士, 博士 |
| t_institution_position | 无要求, 大专, 本科, 硕士, 博士 |
| t_military_position | 本科及以上, 硕士及以上, 博士 |
| t_selection_position | 本科, 硕士, 博士, 本科及以上, 硕士及以上 |

### 政治面貌

| 表 | 可选值 |
|----|--------|
| t_civil_position | 不限, 中共党员, 共青团员, 群众 |
| t_selection_position | 中共党员, 中共预备党员, 共青团员, 不限 |

---

## 1. 公务员职位 (Civil Position)

**基础路径：** `/api/v1/admin/employment/civil-service/civil-position`

### 1.1 分页查询

```
GET /api/v1/admin/employment/civil-service/civil-position/list
```

**请求参数 (Query String)：**

| 参数 | 类型 | 必填 | 说明 | 查询方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认1 | - |
| size | Integer | 否 | 每页条数，默认10 | - |
| positionName | String | 否 | 职位名称 | 模糊查询 |
| recruitingDept | String | 否 | 招录部门 | 模糊查询 |
| workLocation | String | 否 | 工作地点 | 模糊查询 |
| examType | String | 否 | 考试类型(国考/省考) | 精确查询 |
| regStatus | String | 否 | 报名状态(报名中/已结束/即将开始) | 精确查询 |
| minEducation | String | 否 | 最低学历(不限/大专/本科/硕士/博士) | 精确查询 |

**响应字段（分页列表）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1812345678901234561,
        "positionName": "一级主任科员",
        "examType": "国考",
        "recruitingDept": "国家税务总局",
        "minEducation": "本科",
        "workLocation": "北京",
        "regStartDate": "2026-10-15T08:00:00+08:00",
        "regEndDate": "2026-10-24T18:00:00+08:00",
        "regStatus": "报名中"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

**排序规则：** `sort_order ASC, updated_at DESC`

### 1.2 详情

```
GET /api/v1/admin/employment/civil-service/civil-position/{id}/detail
```

**响应：** 返回 CivilPosition 全部字段

### 1.3 修改

```
PUT /api/v1/admin/employment/civil-service/civil-position/{id}/update
```

**请求体 (JSON)：** CivilPositionUpdateDTO（所有可修改字段）

### 1.4 删除（物理删除）

```
DELETE /api/v1/admin/employment/civil-service/civil-position/{id}/delete
```

### 1.5 启用/禁用（软删除）

```
PATCH /api/v1/admin/employment/civil-service/civil-position/{id}/status
```

**请求体 (JSON)：**

```json
{
  "status": 0
}
```

| status | 含义 |
|--------|------|
| 0 | 启用（is_deleted = false） |
| 1 | 禁用（is_deleted = true） |

### 1.6 批量删除

```
DELETE /api/v1/admin/employment/civil-service/civil-position/batch-delete
```

**请求体 (JSON)：**

```json
[1812345678901234561, 1812345678901234562]
```

### 1.7 Excel预校验

```
POST /api/v1/admin/employment/civil-service/civil-position/pre-validate
```

**请求参数：** `file` (MultipartFile)

**响应：** 校验通过返回 `"校验通过"`，有错误返回具体行号和错误信息

### 1.8 导入Excel

```
POST /api/v1/admin/employment/civil-service/civil-position/import
```

**请求参数：** `file` (MultipartFile)

**校验规则：**
- 职位名称不能为空
- 考试类型不能为空

---

## 2. 事业编职位 (Institution Position)

**基础路径：** `/api/v1/admin/employment/civil-service/institution-position`

### 2.1 分页查询

```
GET /api/v1/admin/employment/civil-service/institution-position/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 | 查询方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认1 | - |
| size | Integer | 否 | 每页条数，默认10 | - |
| positionName | String | 否 | 职位名称 | 模糊查询 |
| supervisingDept | String | 否 | 主管部门 | 模糊查询 |
| institution | String | 否 | 事业单位 | 模糊查询 |
| province | String | 否 | 省份 | 精确查询 |
| examCategory | String | 否 | 考试类别 | 精确查询 |
| positionType | String | 否 | 岗位类型 | 精确查询 |
| positionStatus | String | 否 | 状态 | 精确查询 |

**响应字段（分页列表）：**
id, positionName, supervisingDept, institution, province, examCategory, positionType, subCategory, salaryRange, positionStatus

### 2.2 ~ 2.8

同公务员接口模式，URL 路径前缀替换为 `/institution-position`

**校验规则（预校验和导入）：**
- 职位名称不能为空
- 省份如果填写，需要是合法的省份（通过 `ProvinceEnum.isValid()` 校验）

---

## 3. 部队文职岗位 (Military Position)

**基础路径：** `/api/v1/admin/employment/civil-service/military-position`

### 3.1 分页查询

```
GET /api/v1/admin/employment/civil-service/military-position/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 | 查询方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认1 | - |
| size | Integer | 否 | 每页条数，默认10 | - |
| positionName | String | 否 | 岗位名称 | 模糊查询 |
| employerUnit | String | 否 | 用人单位 | 模糊查询 |
| department | String | 否 | 科室 | 模糊查询 |
| positionType | String | 否 | 岗位类型 | 精确查询 |
| positionStatus | String | 否 | 状态 | 精确查询 |

**响应字段（分页列表）：**
id, positionName, employerUnit, department, positionType, workLocation, salaryRange, regDeadline, positionStatus

### 3.2 ~ 3.8

同公务员接口模式，URL 路径前缀替换为 `/military-position`

**校验规则（预校验和导入）：**
- 岗位名称不能为空

---

## 4. 选调生岗位 (Selection Position)

**基础路径：** `/api/v1/admin/employment/civil-service/selection-position`

### 4.1 分页查询

```
GET /api/v1/admin/employment/civil-service/selection-position/list
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 | 查询方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认1 | - |
| size | Integer | 否 | 每页条数，默认10 | - |
| positionName | String | 否 | 岗位名称 | 模糊查询 |
| targetUnit | String | 否 | 目标单位 | 模糊查询 |
| organizingDept | String | 否 | 组织部门 | 模糊查询 |
| selectionType | String | 否 | 选调类型 | 精确查询 |
| year | String | 否 | 年份 | 精确查询 |
| province | String | 否 | 省份 | 精确查询 |
| politicalStatus | String | 否 | 政治面貌 | 精确查询 |
| positionStatus | String | 否 | 状态 | 精确查询 |

**响应字段（分页列表）：**
id, positionName, selectionType, year, province, organizingDept, targetUnit, workLocation, politicalStatus, regStartDate, regEndDate, positionStatus

### 4.2 ~ 4.8

同公务员接口模式，URL 路径前缀替换为 `/selection-position`

**校验规则（预校验和导入）：**
- 岗位名称不能为空
- 省份如果填写，需要是合法的省份

---

## Excel 导入说明

### 文件格式
- 使用 EasyExcel 读取 `.xlsx` 文件
- 表头通过 `@ExcelProperty` 注解映射
- 支持标准日期格式自动转换（OffsetDateTime）
- 支持数字类型自动转换（Integer, Boolean）

### 数组字段处理（String[]）
使用 `StringToArrayConverter`，Excel 中以逗号（支持中英文逗号）分隔的值会被解析为数组：

| 表 | ExcelDTO数组字段 |
|----|-----------------|
| InstitutionPositionExcelDTO | majorRequirements（专业要求） |
| MilitaryPositionExcelDTO | responsibilities（工作职责）, qualifications（任职资格） |
| SelectionPositionExcelDTO | majorCategories（专业类别）, targetUniversities（目标院校） |

### 事务一致性
- import 接口使用 `@Transactional(rollbackFor = Exception.class)`
- 导入过程中遇到错误全部回滚

---

## 操作日志

所有修改操作会自动记录操作日志：

| Controller | module | action |
|-----------|--------|--------|
| CivilPositionController | 体制内招录 | 修改/删除/启用禁用/批量删除/导入公务员职位 |
| InstitutionPositionController | 体制内招录 | 修改/删除/启用禁用/批量删除/导入事业编职位 |
| MilitaryPositionController | 体制内招录 | 修改/删除/启用禁用/批量删除/导入部队文职岗位 |
| SelectionPositionController | 体制内招录 | 修改/删除/启用禁用/批量删除/导入选调生岗位 |

---

## 文件结构

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
