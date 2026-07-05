# Order 16：行业专项招聘 — API 接口文档

## 概述

实现 admin 端对教师招聘、医疗卫生、银行/金融三张行业招聘表的 CRUD + Excel 导入功能。

### 基础路径
- 教师招聘：`/api/v1/admin/employment/industry-position/teacher`
- 医疗卫生：`/api/v1/admin/employment/industry-position/healthcare`
- 银行/金融：`/api/v1/admin/employment/industry-position/finance`

### 统一路径后缀
所有接口（除分页）使用动词式后缀：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页查询 |
| GET | `/{id}/detail` | 详情 |
| PUT | `/{id}/update` | 修改 |
| DELETE | `/{id}/delete` | 物理删除 |
| PATCH | `/{id}/status` | 启用/禁用 |
| DELETE | `/batch-delete` | 批量物理删除 |
| POST | `/pre-validate` | Excel 预校验 |
| POST | `/import` | 导入 Excel |

---

## 一、教师招聘接口

### 基础路径
`/api/v1/admin/employment/industry-position/teacher`

**权限：** 需要登录（类级 @RequireLogin）

---

### 1.1 分页查询教师招聘岗位

**GET** `/api/v1/admin/employment/industry-position/teacher/list`

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码（默认1） |
| size | Integer | 否 | 每页条数（默认10，可传10/20/30/50/100） |
| schoolName | String | 否 | 学校名称（模糊查询） |
| positionName | String | 否 | 岗位名称（模糊查询） |
| schoolType | String | 否 | 学校类型（精准匹配） |
| province | String | 否 | 省份（精准匹配） |
| city | String | 否 | 城市（精准匹配） |
| district | String | 否 | 区/县（精准匹配） |
| positionStatus | String | 否 | 状态（精准匹配） |

**排序规则：** `sort_order` 降序 → `created_at` 降序

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 123456789,
        "schoolName": "北京第一中学",
        "schoolType": "高中",
        "schoolNature": "公办",
        "positionName": "高中数学教师",
        "recruitmentType": "编制",
        "province": "北京市",
        "city": "北京",
        "district": "海淀区",
        "positionStatus": "招聘中",
        "updatedAt": "2026-07-01T10:00:00+08:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1234567890
}
```

---

### 1.2 查看教师招聘详情

**GET** `/api/v1/admin/employment/industry-position/teacher/{id}/detail`

**响应：** 所有业务字段 + `sortOrder`, `isDeleted`, `createdAt`, `updatedAt`

---

### 1.3 修改教师招聘岗位

**PUT** `/api/v1/admin/employment/industry-position/teacher/{id}/update`

**请求体：** 所有业务字段

**日志：** `@OperationLog(module = "行业专项招聘", action = "修改教师招聘岗位")`

---

### 1.4 删除教师招聘岗位（物理删除）

**DELETE** `/api/v1/admin/employment/industry-position/teacher/{id}/delete`

**日志：** `@OperationLog(module = "行业专项招聘", action = "删除教师招聘岗位")`

---

### 1.5 启用/禁用教师招聘岗位

**PATCH** `/api/v1/admin/employment/industry-position/teacher/{id}/status`

**请求体：**
```json
{
  "status": 1   // 1=启用(isDeleted=false), 0=禁用(isDeleted=true)
}
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "启用/禁用教师招聘岗位")`

---

### 1.6 批量物理删除

**DELETE** `/api/v1/admin/employment/industry-position/teacher/batch-delete`

**请求体：**
```json
[1001, 1002, 1003]
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "批量删除教师招聘岗位")`

---

### 1.7 Excel 预校验

**POST** `/api/v1/admin/employment/industry-position/teacher/pre-validate`

**请求参数：** `file` (MultipartFile)

**响应：**
```json
{
  "code": 200,
  "msg": "校验通过",
  "data": "校验通过"
}
```
或
```json
{
  "code": 200,
  "msg": "success",
  "data": "第2行: 学校名称不能为空; 第3行: 省份不合法"
}
```

---

### 1.8 导入教师招聘岗位 Excel

**POST** `/api/v1/admin/employment/industry-position/teacher/import`

**请求参数：** `file` (MultipartFile)

**日志：** `@OperationLog(module = "行业专项招聘", action = "导入教师招聘岗位")`

**Excel 字段映射：**

| Excel 表头 | 字段名 | 必填 |
|------------|--------|:----:|
| 学校名称 | schoolName | ✓ |
| 学校类型 | schoolType | ✓ |
| 学校性质 | schoolNature | |
| 主管教育部门 | supervisingDept | |
| 岗位名称 | positionName | ✓ |
| 学科 | subject | ✓ |
| 招聘类型 | recruitmentType | ✓ |
| 省份 | province | ✓ |
| 城市 | city | |
| 区/县 | district | |
| 学历要求 | educationRequirement | |
| 学位要求 | degreeRequirement | |
| 专业要求 | majorRequirement | |
| 年龄上限 | ageLimit | |
| 招聘人数 | recruitmentCount | |
| 教师资格证要求 | teacherCertRequirement | |
| 资格证学科要求 | teacherCertSubject | |
| 普通话等级要求 | putonghuaLevel | |
| 其他证书要求 | otherCertRequirement | |
| 教学经验要求 | workExperience | |
| 是否要求师范专业 | isNormalMajor | |
| 薪资待遇 | salaryRange | |
| 福利待遇 | benefits | |
| 笔试内容 | examContent | |
| 面试形式 | interviewForm | |
| 报名开始日期 | regStartDate | |
| 报名截止日期 | regEndDate | |
| 考试时间 | examTime | |
| 状态 | positionStatus | |
| 报名链接 | applyLink | |
| 联系电话 | contactPhone | |
| 备注 | remark | |
| 详细说明 | content | |
| 排序 | sortOrder | |

---

## 二、医疗卫生接口

### 基础路径
`/api/v1/admin/employment/industry-position/healthcare`

与教师招聘接口结构完全一致，仅请求/响应字段不同。

### 2.1 分页查询

**GET** `/api/v1/admin/employment/industry-position/healthcare/list`

**查询参数：**
| 参数 | 类型 | 查询方式 |
|------|------|---------|
| institutionName | String | LIKE 模糊 |
| positionName | String | LIKE 模糊 |
| institutionNature | String | EQ 精准 |
| department | String | EQ 精准 |
| province | String | EQ 精准 |
| city | String | EQ 精准 |
| district | String | EQ 精准 |
| positionStatus | String | EQ 精准 |

### 2.2 详情

**GET** `/api/v1/admin/employment/industry-position/healthcare/{id}/detail`

**ListVO 字段：** id, institutionName, positionName, department, positionCategory, province, city, district, positionStatus, updatedAt

### 2.3-2.8
与教师招聘结构一致，仅操作日志 action 文案不同（"医疗卫生岗位"）。

---

## 三、银行/金融接口

### 基础路径
`/api/v1/admin/employment/industry-position/finance`

**权限区别：** 分页列表**不需要登录**，其余接口需要登录（方法级 @RequireLogin）。

### 3.1 分页查询

**GET** `/api/v1/admin/employment/industry-position/finance/list`

**查询参数：**
| 参数 | 类型 | 查询方式 |
|------|------|---------|
| institutionName | String | LIKE 模糊 |
| positionName | String | LIKE 模糊 |
| institutionCategory | String | EQ 精准 |
| institutionType | String | EQ 精准 |
| province | String | EQ 精准 |
| city | String | EQ 精准 |
| positionStatus | String | EQ 精准 |

**ListVO 字段：** id, institutionName, institutionCategory, positionName, positionCategory, recruitmentType, province, city, positionStatus, updatedAt

### 3.2-3.8
与教师招聘结构一致，仅操作日志 action 文案不同（"银行/金融招聘岗位"）。

### Excel 特殊字段
| Excel 表头 | 字段名 | 类型 | 说明 |
|------------|--------|------|------|
| 优先专业 | majorPreference | List\<String\> | 逗号分隔（支持中英文逗号） |
| 证书要求 | certRequirements | List\<String\> | 逗号分隔（支持中英文逗号） |

---

## 四、通用规则

### 分页参数
- page: 默认1，最小1
- size: 默认10，可选值 10/20/30/50/100

### 排序
所有列表查询按 `sort_order` 降序 + `created_at` 降序排列

### 状态（启用/禁用）
- `status=1` → `is_deleted=false`（启用，出现在列表）
- `status=0` → `is_deleted=true`（禁用，不出现在列表）
- 已禁用的记录可以通过同样的接口重新启用

### 数据校验
- 省份通过 `ProvinceEnum.isValid()` 校验
- Excel 导入支持预校验（`/pre-validate`）后再正式导入（`/import`）
- 导入出错全部回滚（`@Transactional`）

### 操作日志
所有写操作（修改/删除/启用禁用/批量删除/导入）均记录操作日志，module 统一为 `"行业专项招聘"`。

---

## 五、文件结构

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/employment/industryPosition/
│   ├── TeacherPositionController.java
│   ├── HealthcarePositionController.java
│   └── FinancePositionController.java
├── service/employment/industryPosition/
│   ├── TeacherPositionService.java
│   ├── HealthcarePositionService.java
│   └── FinancePositionService.java
├── service/impl/employment/industryPosition/
│   ├── TeacherPositionServiceImpl.java
│   ├── HealthcarePositionServiceImpl.java
│   └── FinancePositionServiceImpl.java
├── dto/employment/industryPosition/
│   ├── teacher/
│   │   ├── TeacherPositionQueryDTO.java
│   │   ├── TeacherPositionUpdateDTO.java
│   │   └── TeacherPositionStatusDTO.java
│   ├── healthcare/
│   │   ├── HealthcarePositionQueryDTO.java
│   │   ├── HealthcarePositionUpdateDTO.java
│   │   └── HealthcarePositionStatusDTO.java
│   └── finance/
│       ├── FinancePositionQueryDTO.java
│       ├── FinancePositionUpdateDTO.java
│       └── FinancePositionStatusDTO.java
├── vo/employment/industryPosition/
│   ├── teacher/
│   │   ├── TeacherPositionListVO.java
│   │   └── TeacherPositionDetailVO.java
│   ├── healthcare/
│   │   ├── HealthcarePositionListVO.java
│   │   └── HealthcarePositionDetailVO.java
│   └── finance/
│       ├── FinancePositionListVO.java
│       └── FinancePositionDetailVO.java
└── excel/employment/industryPosition/
    ├── TeacherPositionExcelDTO.java
    ├── HealthcarePositionExcelDTO.java
    └── FinancePositionExcelDTO.java
```
