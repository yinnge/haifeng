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

**请求体 (JSON)：** CivilPositionUpdateDTO（所有可修改字段，均为选填，只传需要修改的字段）

**校验规则：**

| 字段 | 校验规则 | DB CHECK 约束 |
|------|---------|--------------|
| positionName | `@Size(max=200)` | - |
| examType | `@Pattern(国考\|省考)` | `IN ('国考', '省考')` |
| recruitingDept | `@Size(max=200)` | - |
| deptCode | `@Size(max=30)` | - |
| positionCode | `@Size(max=30)` | - |
| affiliatedBureau | `@Size(max=200)` | - |
| majorRequirement | `@Size(max=500)` | - |
| minEducation | `@Pattern(不限\|大专\|本科\|硕士\|博士)` | `IN ('不限', '大专', '本科', '硕士', '博士')` |
| degreeRequirement | `@Pattern(不限\|学士\|硕士\|博士)` | `IN ('不限', '学士', '硕士', '博士')` |
| politicalStatus | `@Pattern(不限\|中共党员\|共青团员\|群众)` | `IN ('不限', '中共党员', '共青团员', '群众')` |
| workExperience | `@Size(max=50)` | - |
| grassrootsExperience | `@Size(max=50)` | - |
| examCategory | `@Size(max=50)` | - |
| interviewRatio | `@Size(max=20)` | - |
| recruitmentCount | `@Min(1)` | `> 0` |
| hasProfessionalTest | - | - |
| workLocation | `@Size(max=100)` | - |
| workLocationDetail | `@Size(max=200)` | - |
| householdRequirement | `@Size(max=100)` | - |
| householdLocation | `@Size(max=100)` | - |
| positionIntro | - | - |
| remark | - | - |
| officialWebsite | `@Size(max=500)` | - |
| contactPhone | `@Size(max=50)` | - |
| regStartDate | - | - |
| regEndDate | - | - |
| regStatus | `@Pattern(报名中\|已结束\|即将开始)` | `IN ('报名中', '已结束', '即将开始')` |
| applicantCount | `@Min(0)` | `>= 0` |
| sortOrder | `@Min(0)` | - |

### 1.4 删除（软删除）

```
DELETE /api/v1/admin/employment/civil-service/civil-position/{id}/delete
```

### 1.5 更新报名状态

```
PATCH /api/v1/admin/employment/civil-service/civil-position/{id}/status
```

**请求体 (JSON)：**

```json
{
  "status": 0
}
```

| status | 含义 | 对应 regStatus |
|--------|------|---------------|
| 0 | 报名中 | 报名中 |
| 1 | 已结束 | 已结束 |

> **注意：** 此接口操作 `reg_status` 字段，不再操作 `is_deleted`。`is_deleted` 仅供软删除使用。

### 1.6 批量删除

```
POST /api/v1/admin/employment/civil-service/civil-position/batch-delete
```

**请求体 (JSON)：**

```json
[1812345678901234561, 1812345678901234562]
```

**校验规则：**
- `@Valid`：触发参数校验
- `@NotEmpty(message = "ids不能为空")`：列表不能为空
- `@Size(max = 100, message = "单次最多删除100条")`：单次最多删除 100 条

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

**业务去重规则：**
- 按 `(exam_type, dept_code, position_code)` 三元组判断是否已存在
- 已存在的记录**自动跳过**，不插入也不报错
- 日志输出 `total=Excel总行数, imported=实际导入数`

**行校验规则（全部通过才插入，任一失败整体回滚）：**

| 字段 | 必填 | 校验规则 |
|------|:----:|---------|
| 职位名称 | ✓ | 非空 |
| 考试类型 | ✓ | 非空，值域：国考/省考 |
| 最低学历 | 否 | 值域：不限/大专/本科/硕士/博士 |
| 学位要求 | 否 | 值域：不限/学士/硕士/博士 |
| 政治面貌 | 否 | 值域：不限/中共党员/共青团员/群众 |
| 报名状态 | 否 | 值域：报名中/已结束/即将开始 |
| 招录人数 | 否 | 须 ≥ 1 |
| 报名人数 | 否 | 须 ≥ 0 |
| 排序 | 否 | 须 ≥ 0 |

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

### 2.2 详情

```
GET /api/v1/admin/employment/civil-service/institution-position/{id}/detail
```

**响应：** 返回 InstitutionPosition 全部字段

### 2.3 修改

```
PUT /api/v1/admin/employment/civil-service/institution-position/{id}/update
```

**请求体 (JSON)：** InstitutionPositionUpdateDTO（所有可修改字段，均为选填，只传需要修改的字段）

**校验规则：**

| 字段 | 校验规则 | DB CHECK 约束 |
|------|---------|--------------|
| positionName | `@Size(max=200)` | - |
| supervisingDept | `@Size(max=200)` | - |
| institution | `@Size(max=200)` | - |
| workLocation | `@Size(max=100)` | - |
| province | `@Size(max=30)` | - |
| examCategory | `@Size(max=50)` | - |
| positionType | `@Size(max=50)` | - |
| subCategory | `@Size(max=50)` | - |
| educationRequirement | `@Pattern(无要求\|大专\|本科\|硕士\|博士)` | `IN ('无要求', '大专', '本科', '硕士', '博士')` |
| degreeRequirement | `@Pattern(无要求\|学士\|硕士\|博士)` | `IN ('无要求', '学士', '硕士', '博士')` |
| ageLimit | `@Min(18) @Max(65)` | `>= 18 AND <= 65` |
| recruitmentCount | `@Min(1)` | `> 0` |
| salaryRange | `@Size(max=50)` | - |
| regDeadline | `@Size(max=30)` | - |
| majorRequirements | - | - |
| specialPosition | `@Size(max=100)` | - |
| otherRequirement | `@Size(max=500)` | - |
| otherRequirementDesc | - | - |
| remarkType | `@Size(max=50)` | - |
| remarkDesc | - | - |
| consultationPhone | `@Size(max=50)` | - |
| supervisionPhone | `@Size(max=50)` | - |
| positionStatus | `@Pattern(招聘中\|已结束)` | `IN ('招聘中', '已结束')` |
| positionTag | `@Pattern(热门\|无\|急招)` | `IN ('热门', '无', '急招')` |
| tagText | `@Size(max=50)` | - |
| sortOrder | `@Min(0)` | - |

### 2.4 删除（软删除）

```
DELETE /api/v1/admin/employment/civil-service/institution-position/{id}/delete
```

### 2.5 更新职位状态

```
PATCH /api/v1/admin/employment/civil-service/institution-position/{id}/status
```

**请求体 (JSON)：**

```json
{
  "status": 0
}
```

| status | 含义 | 对应 positionStatus |
|--------|------|---------------------|
| 0 | 招聘中 | 招聘中 |
| 1 | 已结束 | 已结束 |

> **注意：** 此接口操作 `position_status` 字段，不再操作 `is_deleted`。`is_deleted` 仅供软删除使用。

### 2.6 批量删除

```
POST /api/v1/admin/employment/civil-service/institution-position/batch-delete
```

**请求体 (JSON)：**

```json
[1812345678901234561, 1812345678901234562]
```

**校验规则：**
- `@Valid`：触发参数校验
- `@NotEmpty(message = "ids不能为空")`：列表不能为空
- `@Size(max = 100, message = "单次最多删除100条")`：单次最多删除 100 条

### 2.7 Excel预校验

```
POST /api/v1/admin/employment/civil-service/institution-position/pre-validate
```

**请求参数：** `file` (MultipartFile)

**响应：** 校验通过返回 `R.ok()`，有错误返回 400 + 具体行号和错误信息

### 2.8 导入Excel

```
POST /api/v1/admin/employment/civil-service/institution-position/import
```

**请求参数：** `file` (MultipartFile)

**业务去重规则：**
- 按 `(position_name, province)` 二元组判断是否已存在
- 已存在的记录**自动跳过**，不插入也不报错
- 日志输出 `total=Excel总行数, imported=实际导入数`

**行校验规则（全部通过才插入，任一失败整体回滚）：**

| 字段 | 必填 | 校验规则 |
|------|:----:|---------|
| 职位名称 | ✓ | 非空 |
| 省份 | 否 | 合法省份（`ProvinceEnum.isValid()`） |
| 学历要求 | 否 | 值域：无要求/大专/本科/硕士/博士 |
| 学位要求 | 否 | 值域：无要求/学士/硕士/博士 |
| 职位状态 | 否 | 值域：招聘中/已结束 |
| 标签 | 否 | 值域：热门/无/急招 |
| 年龄上限 | 否 | 范围：18-65 |
| 招聘人数 | 否 | 须 > 0 |

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

### 3.2 详情

```
GET /api/v1/admin/employment/civil-service/military-position/{id}/detail
```

**响应：** 返回 MilitaryPosition 全部字段

### 3.3 修改

```
PUT /api/v1/admin/employment/civil-service/military-position/{id}/update
```

**请求体 (JSON)：** MilitaryPositionUpdateDTO（所有可修改字段）

### 3.4 删除（软删除）

```
DELETE /api/v1/admin/employment/civil-service/military-position/{id}/delete
```

### 3.5 更新状态

```
PATCH /api/v1/admin/employment/civil-service/military-position/{id}/status
```

**请求体 (JSON)：** `PositionStatusUpdateDTO`

```json
{
  "positionStatus": "进行中"
}
```

**校验规则：**
- `@NotBlank(message = "岗位状态不能为空")`
- `@Size(max = 20, message = "岗位状态长度不能超过20")`
- Service 层校验值域：只允许 `进行中`、`已结束`，不合法时返回 400

| positionStatus | 说明 |
|----------------|------|
| 进行中 | 岗位招聘进行中 |
| 已结束 | 岗位招聘已结束 |

> **注意：** 此接口操作 `position_status` 字段，不再操作 `is_deleted`。`is_deleted` 仅供软删除使用。

### 3.6 批量删除

```
POST /api/v1/admin/employment/civil-service/military-position/batch-delete
```

**请求体 (JSON)：**

```json
[1812345678901234561, 1812345678901234562]
```

**校验规则：**
- `@Valid`：触发参数校验
- `@NotEmpty(message = "ids不能为空")`：列表不能为空
- `@Size(max = 100, message = "单次最多删除100条")`：单次最多删除 100 条

### 3.7 Excel预校验

```
POST /api/v1/admin/employment/civil-service/military-position/pre-validate
```

**请求参数：** `file` (MultipartFile)

**文件校验：**
- 文件不能为空（`file.isEmpty()` 检查）
- 文件类型只能是 `.xlsx` 或 `.xls`
- 不满足返回 400

**响应：** 校验通过返回 `R.ok()`，有错误返回 400 + 具体行号和错误信息

### 3.8 导入Excel

```
POST /api/v1/admin/employment/civil-service/military-position/import
```

**请求参数：** `file` (MultipartFile)

**文件校验：**
- 文件不能为空（`file.isEmpty()` 检查）
- 文件类型只能是 `.xlsx` 或 `.xls`
- 不满足返回 400

**行校验规则：**

| 字段 | 校验规则 |
|------|---------|
| 岗位名称 | 必填，非空 |
| 学历要求 | 选填，值域：本科及以上/硕士及以上/博士 |
| 状态 | 选填，值域：进行中/已结束 |

**校验流程：**
1. 文件类型校验 → 2. 读取 Excel → 3. 逐行校验 → 4. 全部通过才插入（校验失败 = 全部不插入）→ 5. `Db.saveBatch()` 批量插入

**错误信息限制：**
- 最多显示前 20 条错误
- 超出后追加 `...共N条错误，仅显示前20条`

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

### 4.2 详情

```
GET /api/v1/admin/employment/civil-service/selection-position/{id}/detail
```

**响应：** 返回 SelectionPosition 全部字段

### 4.3 修改

```
PUT /api/v1/admin/employment/civil-service/selection-position/{id}/update
```

**请求体 (JSON)：** SelectionPositionUpdateDTO（所有可修改字段）

### 4.4 删除（软删除）

```
DELETE /api/v1/admin/employment/civil-service/selection-position/{id}/delete
```

### 4.5 更新状态

```
PATCH /api/v1/admin/employment/civil-service/selection-position/{id}/status
```

**请求体 (JSON)：** `PositionStatusUpdateDTO`

```json
{
  "positionStatus": "报名中"
}
```

**校验规则：**
- `@NotBlank(message = "岗位状态不能为空")`
- `@Size(max = 20, message = "岗位状态长度不能超过20")`
- Service 层校验值域：只允许 `报名中`、`笔试阶段`、`面试阶段`、`已结束`、`即将开始`，不合法时返回 400

| positionStatus | 说明 |
|----------------|------|
| 报名中 | 正在报名 |
| 笔试阶段 | 笔试进行中 |
| 面试阶段 | 面试进行中 |
| 已结束 | 招录已结束 |
| 即将开始 | 尚未开始 |

> **注意：** 此接口操作 `position_status` 字段，不再操作 `is_deleted`。`is_deleted` 仅供软删除使用。

### 4.6 批量删除

```
POST /api/v1/admin/employment/civil-service/selection-position/batch-delete
```

**请求体 (JSON)：**

```json
[1812345678901234561, 1812345678901234562]
```

**校验规则：**
- `@Valid`：触发参数校验
- `@NotEmpty(message = "ids不能为空")`：列表不能为空
- `@Size(max = 100, message = "单次最多删除100条")`：单次最多删除 100 条

### 4.7 Excel预校验

```
POST /api/v1/admin/employment/civil-service/selection-position/pre-validate
```

**请求参数：** `file` (MultipartFile)

**文件校验：**
- 文件不能为空（`file.isEmpty()` 检查）
- 文件类型只能是 `.xlsx` 或 `.xls`
- 不满足返回 400

**响应：** 校验通过返回 `R.ok()`，有错误返回 400 + 具体行号和错误信息

### 4.8 导入Excel

```
POST /api/v1/admin/employment/civil-service/selection-position/import
```

**请求参数：** `file` (MultipartFile)

**文件校验：**
- 文件不能为空（`file.isEmpty()` 检查）
- 文件类型只能是 `.xlsx` 或 `.xls`
- 不满足返回 400

**行校验规则：**

| 字段 | 校验规则 |
|------|---------|
| 岗位名称 | 必填，非空 |
| 选调类型 | 必填，值域：定向选调/非定向选调/急需紧缺专业选调 |
| 年份 | 必填，非空 |
| 省份 | 必填，合法省份（`ProvinceEnum.isValid()`） |
| 学历要求 | 必填，值域：本科/硕士/博士/本科及以上/硕士及以上 |
| 政治面貌 | 选填，值域：中共党员/中共预备党员/共青团员/不限 |
| 年龄上限 | 选填，范围：18-40 |
| 状态 | 选填，值域：报名中/笔试阶段/面试阶段/已结束/即将开始 |

**校验流程：**
1. 文件类型校验 → 2. 读取 Excel → 3. 逐行校验 → 4. 全部通过才插入（校验失败 = 全部不插入）→ 5. `Db.saveBatch()` 批量插入

**错误信息限制：**
- 最多显示前 20 条错误
- 超出后追加 `...共N条错误，仅显示前20条`

---

## Excel 导入说明

### 文件要求

- 格式：`.xlsx` 或 `.xls`
- 部队文职、选调生模块校验文件非空（`file.isEmpty()`）和文件后缀，不满足返回 400
- 公务员、事业编模块暂无文件类型校验
- 表头通过 `@ExcelProperty` 注解映射
- 支持 `OffsetDateTime` 日期格式自动转换（`OffsetDateTimeConverter`），支持格式：`yyyy-MM-dd HH:mm:ssXXX`、`yyyy-MM-dd HH:mm:ss.SSSXXX`、`yyyy-MM-dd HH:mm:ss`、`yyyy-MM-dd HH:mm`、`yyyy-MM-dd`
- 支持 `Integer`、`Boolean` 类型自动转换

### 各模块 Excel 列定义

#### 公务员职位 Excel 列（29列）

| Excel列名 | Java字段 | 类型 | 必填 | 校验规则 |
|-----------|---------|------|------|---------|
| 职位名称 | positionName | String | 是 | 非空 |
| 考试类型 | examType | String | 是 | 非空，值域：国考/省考 |
| 招录部门 | recruitingDept | String | 否 | - |
| 部门代码 | deptCode | String | 否 | 与职位代码组合做去重键 |
| 职位代码 | positionCode | String | 否 | 与部门代码组合做去重键 |
| 隶属局 | affiliatedBureau | String | 否 | - |
| 专业要求 | majorRequirement | String | 否 | - |
| 最低学历 | minEducation | String | 否 | 值域：不限/大专/本科/硕士/博士 |
| 学位要求 | degreeRequirement | String | 否 | 值域：不限/学士/硕士/博士 |
| 政治面貌 | politicalStatus | String | 否 | 值域：不限/中共党员/共青团员/群众 |
| 工作经验 | workExperience | String | 否 | - |
| 基层经验 | grassrootsExperience | String | 否 | - |
| 考试类别 | examCategory | String | 否 | - |
| 面试比例 | interviewRatio | String | 否 | - |
| 招录人数 | recruitmentCount | Integer | 否 | 须 ≥ 1 |
| 专业考试 | hasProfessionalTest | Boolean | 否 | - |
| 工作地点 | workLocation | String | 否 | - |
| 工作地点详情 | workLocationDetail | String | 否 | - |
| 户籍要求 | householdRequirement | String | 否 | - |
| 户籍所在地 | householdLocation | String | 否 | - |
| 职位简介 | positionIntro | String | 否 | - |
| 备注 | remark | String | 否 | - |
| 官网 | officialWebsite | String | 否 | - |
| 联系电话 | contactPhone | String | 否 | - |
| 报名开始 | regStartDate | OffsetDateTime | 否 | - |
| 报名截止 | regEndDate | OffsetDateTime | 否 | - |
| 报名状态 | regStatus | String | 否 | 值域：报名中/已结束/即将开始 |
| 报名人数 | applicantCount | Integer | 否 | 须 ≥ 0 |
| 排序 | sortOrder | Integer | 否 | 须 ≥ 0 |

**去重键：** `(exam_type, dept_code, position_code)` — 三元组均非空时启用去重，已存在则跳过。

#### 事业编职位 Excel 列（26列）

| Excel列名 | Java字段 | 类型 | 必填 | 校验规则 |
|-----------|---------|------|------|---------|
| 职位名称 | positionName | String | 是 | 非空 |
| 主管部门 | supervisingDept | String | 否 | - |
| 事业单位 | institution | String | 否 | - |
| 工作地点 | workLocation | String | 否 | - |
| 省份 | province | String | 否 | 合法省份（`ProvinceEnum.isValid()`） |
| 考试类别 | examCategory | String | 否 | - |
| 岗位类型 | positionType | String | 否 | - |
| 子分类 | subCategory | String | 否 | - |
| 学历要求 | educationRequirement | String | 否 | 值域：无要求/大专/本科/硕士/博士 |
| 学位要求 | degreeRequirement | String | 否 | 值域：无要求/学士/硕士/博士 |
| 年龄上限 | ageLimit | Integer | 否 | 范围：18-65 |
| 招聘人数 | recruitmentCount | Integer | 否 | 须 > 0 |
| 薪资范围 | salaryRange | String | 否 | - |
| 报名截止 | regDeadline | String | 否 | - |
| 专业要求 | majorRequirements | String[] | 否 | 逗号分隔，`StringToArrayConverter` |
| 特殊岗位 | specialPosition | String | 否 | - |
| 其他要求 | otherRequirement | String | 否 | - |
| 其他要求说明 | otherRequirementDesc | String | 否 | - |
| 备注类型 | remarkType | String | 否 | - |
| 备注说明 | remarkDesc | String | 否 | - |
| 咨询电话 | consultationPhone | String | 否 | - |
| 监督电话 | supervisionPhone | String | 否 | - |
| 状态 | positionStatus | String | 否 | 值域：招聘中/已结束 |
| 标签 | positionTag | String | 否 | 值域：热门/无/急招 |
| 标签文字 | tagText | String | 否 | - |
| 排序 | sortOrder | Integer | 否 | 须 ≥ 0 |

**去重键：** `(position_name, province)` — 二元组均非空时启用去重，已存在则跳过。

#### 部队文职岗位 Excel 列（14列）

| Excel列名 | Java字段 | 类型 | 必填 | 校验规则 |
|-----------|---------|------|------|---------|
| 岗位名称 | positionName | String | 是 | 非空 |
| 用人单位 | employerUnit | String | 否 | - |
| 科室 | department | String | 否 | - |
| 岗位类型 | positionType | String | 否 | - |
| 工作地点 | workLocation | String | 否 | - |
| 薪资范围 | salaryRange | String | 否 | - |
| 专业要求 | majorRequirement | String | 否 | - |
| 学历要求 | educationRequirement | String | 否 | 值域：本科及以上/硕士及以上/博士 |
| 报名截止 | regDeadline | String | 否 | - |
| 状态 | positionStatus | String | 否 | 值域：进行中/已结束 |
| 岗位描述 | positionDescription | String | 否 | - |
| 工作职责 | responsibilities | String[] | 否 | 逗号分隔，`StringToArrayConverter` |
| 任职资格 | qualifications | String[] | 否 | 逗号分隔，`StringToArrayConverter` |
| 排序 | sortOrder | Integer | 否 | - |

#### 选调生岗位 Excel 列（33列）

| Excel列名 | Java字段 | 类型 | 必填 | 校验规则 |
|-----------|---------|------|------|---------|
| 岗位名称 | positionName | String | 是 | 非空 |
| 选调类型 | selectionType | String | 是 | 值域：定向选调/非定向选调/急需紧缺专业选调 |
| 年份 | year | String | 是 | 非空 |
| 省份 | province | String | 是 | 合法省份（`ProvinceEnum.isValid()`） |
| 组织部门 | organizingDept | String | 否 | - |
| 目标单位 | targetUnit | String | 否 | - |
| 工作地点 | workLocation | String | 否 | - |
| 培养方向 | trainingDirection | String | 否 | - |
| 基层服务年限 | grassrootsServiceYears | String | 否 | - |
| 培养计划 | trainingPlan | String | 否 | - |
| 学历要求 | educationRequirement | String | 是 | 值域：本科/硕士/博士/本科及以上/硕士及以上 |
| 学位要求 | degreeRequirement | String | 否 | - |
| 专业要求 | majorRequirement | String | 否 | - |
| 专业类别 | majorCategories | String[] | 否 | 逗号分隔，`StringToArrayConverter` |
| 院校要求 | universityRequirement | String | 否 | - |
| 目标院校 | targetUniversities | String[] | 否 | 逗号分隔，`StringToArrayConverter` |
| 政治面貌 | politicalStatus | String | 否 | 值域：中共党员/中共预备党员/共青团员/不限 |
| 学生干部要求 | studentCadreRequirement | String | 否 | - |
| 奖项要求 | awardsRequirement | String | 否 | - |
| 年龄上限 | ageLimit | Integer | 否 | 范围：18-40 |
| 招录人数 | recruitmentCount | Integer | 否 | - |
| 考试科目 | examSubjects | String | 否 | - |
| 面试形式 | interviewForm | String | 否 | - |
| 报名开始 | regStartDate | OffsetDateTime | 否 | - |
| 报名截止 | regEndDate | OffsetDateTime | 否 | - |
| 考试时间 | examTime | OffsetDateTime | 否 | - |
| 报名链接 | applyLink | String | 否 | - |
| 状态 | positionStatus | String | 否 | 值域：报名中/笔试阶段/面试阶段/已结束/即将开始 |
| 备注 | remark | String | 否 | - |
| 联系电话 | contactPhone | String | 否 | - |
| 官方链接 | officialLink | String | 否 | - |
| 详细说明 | content | String | 否 | - |
| 排序 | sortOrder | Integer | 否 | - |

### 数组字段处理（String[]）

使用 `StringToArrayConverter`，Excel 中以逗号（支持中英文逗号）分隔的值会被解析为数组：

| ExcelDTO | 数组字段 | Excel列名 |
|----------|---------|-----------|
| InstitutionPositionExcelDTO | majorRequirements | 专业要求 |
| MilitaryPositionExcelDTO | responsibilities | 工作职责 |
| MilitaryPositionExcelDTO | qualifications | 任职资格 |
| SelectionPositionExcelDTO | majorCategories | 专业类别 |
| SelectionPositionExcelDTO | targetUniversities | 目标院校 |

### 校验流程

1. **文件类型校验**（部队文职/选调生）：检查文件非空、后缀为 `.xlsx` 或 `.xls`
2. **读取 Excel**：EasyExcel 根据 `@ExcelProperty` 注解映射列
3. **逐行校验**：按各模块校验规则验证每一行
4. **全部通过才插入**：校验失败 = 全部不插入（事务一致性）
5. **业务去重**（公务员/事业编）：按唯一键查询，已存在则跳过
6. **批量插入**：部队文职/选调生使用 `Db.saveBatch()`；公务员/事业编使用逐条 `mapper.insert()`

> `pre-validate` 接口只执行步骤 1-4，不执行去重和插入。

### 错误信息限制

- 部队文职、选调生：最多显示前 20 条错误（`MAX_ERROR_DISPLAY = 20`），超出后追加 `...共N条错误，仅显示前20条`
- 公务员、事业编：显示全部错误，无条数限制

### 错误响应格式

```
第3行: 岗位名称不能为空
第5行: 学历要求只能是: 本科及以上、硕士及以上、博士; 状态只能是: 进行中、已结束
...共25条错误，仅显示前20条
```

### 事务一致性

- import 接口使用 `@Transactional(rollbackFor = Exception.class)`
- 导入过程中遇到错误全部回滚

---

## 操作日志

所有修改操作会自动记录操作日志：

| Controller | module | action |
|-----------|--------|--------|
| CivilPositionController | 体制内招录 | 修改/删除/更新报名状态/批量删除/导入公务员职位 |
| InstitutionPositionController | 体制内招录 | 修改/删除/更新职位状态/批量删除/导入事业编职位 |
| MilitaryPositionController | 体制内招录 | 修改/删除/更新部队文职岗位状态/批量删除/导入部队文职岗位 |
| SelectionPositionController | 体制内招录 | 修改/删除/更新选调生岗位状态/批量删除/导入选调生岗位 |

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
│   ├── PositionStatusUpdateDTO.java
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
