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
| DELETE | `/{id}/delete` | 软删除 |
| PATCH | `/{id}/status` | 更新岗位状态 |
| POST | `/batch-delete` | 批量软删除 |
| POST | `/pre-validate` | Excel 预校验 |
| POST | `/import` | 导入 Excel |

---

## 一、教师招聘接口

### 基础路径
`/api/v1/admin/employment/industry-position/teacher`

**权限：** 需要管理员模块权限（类级 @RequireAdminModule("emp_industry_teacher")）

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
| schoolNature | String | 否 | 学校性质（精准匹配：公办/民办） |
| recruitmentType | String | 否 | 招聘类型（精准匹配：编制/合同制/特岗教师/人事代理/编外聘用） |
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

**请求体：** 所有业务字段（可选传，不传的字段不修改）

**String 字段长度限制：**

| 字段 | 最大长度 | 字段 | 最大长度 |
|------|----------|------|----------|
| schoolName | 200 | schoolType | 30 |
| schoolNature | 20 | supervisingDept | 200 |
| positionName | 200 | subject | 50 |
| recruitmentType | 30 | province | 30 |
| city | 50 | district | 50 |
| educationRequirement | 30 | degreeRequirement | 30 |
| majorRequirement | 500 | teacherCertRequirement | 100 |
| teacherCertSubject | 50 | putonghuaLevel | 30 |
| otherCertRequirement | 200 | workExperience | 50 |
| isNormalMajor | 20 | salaryRange | 50 |
| examContent | 500 | interviewForm | 100 |
| positionStatus | 20 | applyLink | 500 |
| contactPhone | 50 | — | — |

**不受限字段（TEXT类型）：** benefits, remark, content

**日志：** `@OperationLog(module = "行业专项招聘", action = "修改教师招聘岗位")`

---

### 1.4 删除教师招聘岗位（软删除）

**DELETE** `/api/v1/admin/employment/industry-position/teacher/{id}/delete`

**日志：** `@OperationLog(module = "行业专项招聘", action = "删除教师招聘岗位")`

---

### 1.5 更新教师招聘岗位状态

**PATCH** `/api/v1/admin/employment/industry-position/teacher/{id}/status`

**请求体：**
```json
{
  "positionStatus": "招聘中"   // 招聘中 | 已结束 | 即将开始
}
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "更新教师招聘岗位状态")`

---

### 1.6 批量软删除

**POST** `/api/v1/admin/employment/industry-position/teacher/batch-delete`

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
  "code": 400,
  "msg": "第2行: 学校名称不能为空\n第3行: 省份不合法",
  "data": null
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
| 年龄上限 | ageLimit | 可空，18-60 |
| 招聘人数 | recruitmentCount | 可空，>0 |
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

### 2.2 查看医疗卫生招聘详情

**GET** `/api/v1/admin/employment/industry-position/healthcare/{id}/detail`

**响应：** 所有业务字段 + `sortOrder`, `isDeleted`, `createdAt`, `updatedAt`

---

### 2.3 修改医疗卫生招聘岗位

**PUT** `/api/v1/admin/employment/industry-position/healthcare/{id}/update`

**请求体：** 所有业务字段（可选传，不传的字段不修改）

**String 字段长度限制：**

| 字段 | 最大长度 | 字段 | 最大长度 |
|------|----------|------|----------|
| institutionName | 200 | institutionType | 50 |
| institutionLevel | 30 | institutionNature | 20 |
| positionName | 200 | department | 100 |
| positionCategory | 30 | recruitmentType | 30 |
| province | 30 | city | 50 |
| district | 50 | educationRequirement | 30 |
| degreeRequirement | 30 | majorRequirement | 500 |
| workExperience | 50 | licenseRequirement | 100 |
| titleRequirement | 30 | internshipRequirement | 50 |
| salaryRange | 50 | housingSubsidy | 100 |
| examContent | 500 | applyLink | 500 |
| positionStatus | 20 | contactPhone | 50 |
| contactPerson | 50 | — | — |

**不受限字段（TEXT类型）：** researchRequirement, benefits, remark, content

**日志：** `@OperationLog(module = "行业专项招聘", action = "修改医疗卫生岗位")`

---

### 2.4 删除医疗卫生招聘岗位（软删除）

**DELETE** `/api/v1/admin/employment/industry-position/healthcare/{id}/delete`

**日志：** `@OperationLog(module = "行业专项招聘", action = "删除医疗卫生岗位")`

---

### 2.5 更新医疗卫生招聘岗位状态

**PATCH** `/api/v1/admin/employment/industry-position/healthcare/{id}/status`

**请求体：**
```json
{
  "positionStatus": "招聘中"   // 招聘中 | 已结束 | 即将开始
}
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "更新医疗卫生岗位状态")`

---

### 2.6 批量软删除

**POST** `/api/v1/admin/employment/industry-position/healthcare/batch-delete`

**请求体：**
```json
[1001, 1002, 1003]
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "批量删除医疗卫生岗位")`

---

### 2.7 导入医疗卫生招聘岗位 Excel

**POST** `/api/v1/admin/employment/industry-position/healthcare/import`

**请求参数：** `file` (MultipartFile)

**日志：** `@OperationLog(module = "行业专项招聘", action = "导入医疗卫生岗位")`

**Excel 字段映射：**

| Excel 表头 | 字段名 | 必填 |
|------------|--------|:----:|
| 医疗机构名称 | institutionName | ✓ |
| 机构类型 | institutionType | ✓ |
| 机构等级 | institutionLevel | |
| 机构性质 | institutionNature | |
| 岗位名称 | positionName | ✓ |
| 科室 | department | |
| 岗位类别 | positionCategory | ✓ |
| 招聘类型 | recruitmentType | |
| 省份 | province | ✓ |
| 城市 | city | |
| 区/县 | district | |
| 学历要求 | educationRequirement | |
| 学位要求 | degreeRequirement | |
| 专业要求 | majorRequirement | |
| 年龄上限 | ageLimit | 可空，18-65 |
| 招聘人数 | recruitmentCount | 可空，>0 |
| 工作经验要求 | workExperience | |
| 执业资格要求 | licenseRequirement | |
| 职称要求 | titleRequirement | |
| 规培要求 | internshipRequirement | |
| 科研要求 | researchRequirement | |
| 薪资待遇 | salaryRange | |
| 福利待遇 | benefits | |
| 住房补贴 | housingSubsidy | |
| 报名开始日期 | regStartDate | |
| 报名截止日期 | regEndDate | |
| 考试时间 | examTime | |
| 考试内容 | examContent | |
| 报名链接 | applyLink | |
| 状态 | positionStatus | |
| 联系电话 | contactPhone | |
| 联系人 | contactPerson | |
| 备注 | remark | |
| 详细说明 | content | |
| 排序 | sortOrder | |

---

## 三、银行/金融接口

### 基础路径
`/api/v1/admin/employment/industry-position/finance`

**权限区别：** 所有接口均需要管理员模块权限（类级 @RequireAdminModule("emp_industry_bank")）。

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

### 3.2 查看银行/金融招聘详情

**GET** `/api/v1/admin/employment/industry-position/finance/{id}/detail`

**响应：** 所有业务字段 + `sortOrder`, `isDeleted`, `createdAt`, `updatedAt`

**特殊字段说明：**
- `majorPreference`：优先专业，JSON 数组存储（如 `["金融学","会计学"]`）
- `certRequirements`：证书要求，JSON 数组存储（如 `["CFA","CPA"]`）
- `salaryMin` / `salaryMax`：最低/最高月薪（Integer 类型）
- `isRemote`：是否支持远程（Boolean 类型）

---

### 3.3 修改银行/金融招聘岗位

**PUT** `/api/v1/admin/employment/industry-position/finance/{id}/update`

**请求体：** 所有业务字段（可选传，不传的字段不修改）

**String 字段长度限制：**

| 字段 | 最大长度 | 字段 | 最大长度 |
|------|----------|------|----------|
| institutionName | 200 | institutionCategory | 30 |
| institutionType | 50 | institutionLogo | 500 |
| branchName | 200 | positionName | 200 |
| positionCategory | 50 | recruitmentType | 30 |
| province | 30 | city | 50 |
| workLocation | 200 | educationRequirement | 30 |
| degreeRequirement | 30 | majorRequirement | 500 |
| workExperience | 50 | languageRequirement | 100 |
| computerRequirement | 100 | salaryText | 100 |
| examContent | 500 | interviewRounds | 100 |
| applyLink | 500 | positionStatus | 20 |
| contactInfo | 200 | — | — |

**不受限字段（TEXT类型）：** otherRequirement, benefits, remark, content

**不受限字段（数组类型，JSON存储）：** majorPreference, certRequirements

**日志：** `@OperationLog(module = "行业专项招聘", action = "修改银行/金融招聘岗位")`

---

### 3.4 删除银行/金融招聘岗位（软删除）

**DELETE** `/api/v1/admin/employment/industry-position/finance/{id}/delete`

**日志：** `@OperationLog(module = "行业专项招聘", action = "删除银行/金融招聘岗位")`

---

### 3.5 更新银行/金融招聘岗位状态

**PATCH** `/api/v1/admin/employment/industry-position/finance/{id}/status`

**请求体：**
```json
{
  "positionStatus": "招聘中"   // 招聘中 | 已结束 | 即将开始
}
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "更新银行/金融招聘岗位状态")`

---

### 3.6 批量软删除

**POST** `/api/v1/admin/employment/industry-position/finance/batch-delete`

**请求体：**
```json
[1001, 1002, 1003]
```

**日志：** `@OperationLog(module = "行业专项招聘", action = "批量删除银行/金融招聘岗位")`

---

### 3.7 导入银行/金融招聘岗位 Excel

**POST** `/api/v1/admin/employment/industry-position/finance/import`

**请求参数：** `file` (MultipartFile)

**日志：** `@OperationLog(module = "行业专项招聘", action = "导入银行/金融招聘岗位")`

**Excel 字段映射：**

| Excel 表头 | 字段名 | 必填 | 特殊处理 |
|------------|--------|:----:|----------|
| 机构名称 | institutionName | ✓ | |
| 机构大类 | institutionCategory | ✓ | |
| 机构细分类型 | institutionType | | |
| 机构Logo | institutionLogo | | |
| 分支机构名称 | branchName | | |
| 岗位名称 | positionName | ✓ | |
| 岗位类别 | positionCategory | | |
| 招聘类型 | recruitmentType | ✓ | |
| 省份 | province | | 非空时 ProvinceEnum.isValid() 校验 |
| 城市 | city | | |
| 详细工作地点 | workLocation | | |
| 是否支持远程 | isRemote | | Boolean |
| 学历要求 | educationRequirement | | |
| 学位要求 | degreeRequirement | | |
| 专业要求 | majorRequirement | | |
| 优先专业 | majorPreference | | 逗号分隔 → List\<String\>，支持中英文逗号 |
| 年龄上限 | ageLimit | 可空 | 18-45 |
| 工作经验要求 | workExperience | | |
| 招聘人数 | recruitmentCount | 可空 | >0 |
| 证书要求 | certRequirements | | 逗号分隔 → List\<String\>，支持中英文逗号 |
| 语言要求 | languageRequirement | | |
| 计算机要求 | computerRequirement | | |
| 其他要求 | otherRequirement | | |
| 最低月薪 | salaryMin | | Integer，≤ salaryMax |
| 最高月薪 | salaryMax | | Integer，≥ salaryMin |
| 薪资文本说明 | salaryText | | |
| 福利待遇 | benefits | | |
| 考试内容 | examContent | | |
| 考试时间 | examTime | | OffsetDateTime |
| 面试轮次说明 | interviewRounds | | |
| 报名开始 | regStartDate | | OffsetDateTime |
| 报名截止 | regEndDate | | OffsetDateTime |
| 网申链接 | applyLink | | |
| 状态 | positionStatus | | |
| 联系方式 | contactInfo | | |
| 备注 | remark | | |
| 详细说明 | content | | |
| 排序 | sortOrder | | Integer |

---

## 四、通用规则

### 分页参数
- page: 默认1，最小1
- size: 默认10，可选值 10/20/30/50/100

### 排序
所有列表查询按 `sort_order` 降序 + `created_at` 降序排列

### 岗位状态（positionStatus）
- `招聘中` - 正在招聘
- `已结束` - 招聘已结束
- `即将开始` - 招聘即将开始
- 已软删除的记录不在列表中显示，无法通过 status 接口恢复

### 数据校验
- 省份通过 `ProvinceEnum.isValid()` 校验
- Excel 导入支持预校验（`/pre-validate`）后再正式导入（`/import`）
- 导入出错全部回滚（`@Transactional`）
- Excel 数值字段校验规则：
  - 年龄上限 `ageLimit`：教师 18-60，医疗 18-65，金融 18-45（可空）
  - 招聘人数 `recruitmentCount`：>0（可空）
  - 最低月薪 `salaryMin` ≤ 最高月薪 `salaryMax`（金融模块，可空）
- 更新接口所有 String 字段均有 `@Size` 长度限制，超出返回 400 校验错误

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
│   ├── PositionStatusUpdateDTO.java   # 共用状态更新DTO
│   ├── teacher/
│   │   ├── TeacherPositionQueryDTO.java
│   │   └── TeacherPositionUpdateDTO.java
│   ├── healthcare/
│   │   ├── HealthcarePositionQueryDTO.java
│   │   └── HealthcarePositionUpdateDTO.java
│   └── finance/
│       ├── FinancePositionQueryDTO.java
│       └── FinancePositionUpdateDTO.java
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
