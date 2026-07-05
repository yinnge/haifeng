# 基层服务岗位管理 API 文档

## 基础信息

- **模块归属**: admin 端 - 就业管理 - 基层服务管理
- **包名**: `com.haifeng.admin.controller/employment/grassrootsPosition`
- **基础路径**: `/api/v1/admin/employment/grassroots-position`
- **认证方式**: JWT Token (除 welfare/list 外所有接口均需登录)
- **统一响应格式**:
  ```json
  { "code": 200, "msg": "success", "data": {}, "timestamp": 1234567890 }
  ```

---

## 一、基层服务项目岗位（三支一扶+西部计划）

**Controller**: `GrassrootsProjectPositionController`
**表**: `t_grassroots_project_position`
**路径前缀**: `/api/v1/admin/employment/grassroots-position/project`

### 1.1 分页查询

```
GET /api/v1/admin/employment/grassroots-position/project/list
```

**请求参数** (Query Params):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| positionName | String | 否 | 模糊查询：岗位名称 |
| organizingDept | String | 否 | 模糊查询：组织单位 |
| serviceUnit | String | 否 | 模糊查询：服务单位 |
| projectType | String | 否 | 精确：项目类型（三支一扶/西部计划） |
| year | String | 否 | 精确：招募年份 |
| serviceType | String | 否 | 精确：服务类型 |
| province | String | 否 | 精确：省份 |
| city | String | 否 | 精确：城市 |
| county | String | 否 | 精确：区/县 |
| positionStatus | String | 否 | 精确：状态（招募中/已结束/即将开始） |

**响应字段** (分页列表):

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectType | String | 项目类型 |
| year | String | 招募年份 |
| positionName | String | 岗位名称 |
| serviceType | String | 服务类型 |
| organizingDept | String | 组织单位 |
| serviceUnit | String | 服务单位 |
| province | String | 省份 |
| city | String | 城市 |
| county | String | 区/县 |
| positionStatus | String | 状态 |

**排序**: `sort_order ASC, updated_at DESC`

---

### 1.2 查看详情

```
GET /api/v1/admin/employment/grassroots-position/project/{id}/detail
```

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |

**响应字段**: 全部表字段 + isDeleted, createdAt, updatedAt

---

### 1.3 修改

```
PUT /api/v1/admin/employment/grassroots-position/project/{id}/update
```

**路径参数**: id (Long)

**请求体** (JSON):

```json
{
  "projectType": "三支一扶",
  "year": "2026",
  "positionName": "乡镇支教岗",
  "serviceType": "支教",
  "organizingDept": "省人社厅",
  "serviceUnit": "XX县教育局",
  "province": "广东省",
  "city": "梅州市",
  "county": "五华县",
  "township": "华城镇",
  "servicePeriod": "2年",
  "serviceStartDate": "2026-09-01",
  "serviceEndDate": "2028-08-31",
  "educationRequirement": "本科及以上",
  "majorRequirement": "师范类",
  "ageLimit": 30,
  "recruitmentCount": 10,
  "gradYearRequirement": "2024-2026届",
  "householdRequirement": "不限",
  "otherRequirement": "具有教师资格证",
  "politicalStatus": "不限",
  "examContent": "公共基础知识+教育基础知识",
  "examTime": "2026-06-15T09:00:00Z",
  "interviewForm": "结构化面试",
  "monthlySubsidy": "3000元/月",
  "socialInsurance": "五险",
  "housingInfo": "提供宿舍",
  "otherBenefits": "交通补贴200元/月",
  "afterServicePolicy": "服务期满考核合格可转为事业编",
  "canTransferToCivil": false,
  "canTransferToInstitution": true,
  "examBonusPoints": "笔试加10分",
  "tuitionCompensation": "学费代偿最高12000元/年",
  "postgradBonus": "考研初试加10分",
  "regStartDate": "2026-04-01T00:00:00Z",
  "regEndDate": "2026-04-30T23:59:59Z",
  "applyLink": "http://example.com/apply",
  "positionStatus": "招募中",
  "contactPhone": "0753-1234567",
  "remark": "备注信息",
  "content": "<p>详细说明</p>",
  "sortOrder": 1
}
```

---

### 1.4 物理删除（单条）

```
DELETE /api/v1/admin/employment/grassroots-position/project/{id}/delete
```

---

### 1.5 禁用/启用

```
PATCH /api/v1/admin/employment/grassroots-position/project/{id}/status
```

**请求体** (JSON):

```json
{
  "status": 1
}
```

| 值 | 说明 |
|----|------|
| 0 | 禁用 (is_deleted = true) |
| 1 | 启用 (is_deleted = false) |

---

### 1.6 批量物理删除

```
DELETE /api/v1/admin/employment/grassroots-position/project/batch-delete
```

**请求体** (JSON):

```json
[1001, 1002, 1003]
```

---

### 1.7 校验 Excel

```
POST /api/v1/admin/employment/grassroots-position/project/pre-validate
```

**请求参数**: file (MultipartFile, .xlsx)

**成功响应**: `{ "code": 200, "msg": "校验通过" }`

**失败响应**: `{ "code": 400, "msg": "第3行【学历要求】值非法，第5行...", ... }`

---

### 1.8 导入 Excel

```
POST /api/v1/admin/employment/grassroots-position/project/import
```

**请求参数**: file (MultipartFile, .xlsx)

导入所有表字段，失败时全部回滚。

**Excel 表头对照**:

| Excel表头 | 对应字段 | 说明 |
|-----------|----------|------|
| 项目类型 | projectType | 三支一扶/西部计划 |
| 年份 | year | |
| 岗位名称 | positionName | |
| 服务类型 | serviceType | |
| 组织单位 | organizingDept | |
| 服务单位 | serviceUnit | |
| 省份 | province | |
| 城市 | city | |
| 区/县 | county | |
| 乡镇/街道 | township | |
| 服务期限 | servicePeriod | |
| 服务开始日期 | serviceStartDate | |
| 服务结束日期 | serviceEndDate | |
| 学历要求 | educationRequirement | |
| 专业要求 | majorRequirement | |
| 年龄上限 | ageLimit | |
| 招募人数 | recruitmentCount | |
| 毕业年份要求 | gradYearRequirement | |
| 户籍要求 | householdRequirement | |
| 政治面貌 | politicalStatus | |
| 其他要求 | otherRequirement | |
| 笔试内容 | examContent | |
| 考试时间 | examTime | |
| 面试形式 | interviewForm | |
| 月补贴标准 | monthlySubsidy | |
| 社保缴纳 | socialInsurance | |
| 住房安排 | housingInfo | |
| 其他待遇 | otherBenefits | |
| 期满政策 | afterServicePolicy | |
| 可定向考公 | canTransferToCivil | true/false |
| 可转事业编 | canTransferToInstitution | true/false |
| 考试加分 | examBonusPoints | |
| 学费补偿 | tuitionCompensation | |
| 考研加分 | postgradBonus | |
| 报名开始 | regStartDate | |
| 报名截止 | regEndDate | |
| 报名链接 | applyLink | |
| 状态 | positionStatus | 招募中/已结束/即将开始 |
| 联系电话 | contactPhone | |
| 备注 | remark | |
| 详细说明 | content | 支持HTML |
| 排序 | sortOrder | |

---

## 二、社区工作者岗位

**Controller**: `CommunityPositionController`
**表**: `t_community_position`
**路径前缀**: `/api/v1/admin/employment/grassroots-position/community`

### 2.1 分页查询

```
GET /api/v1/admin/employment/grassroots-position/community/list
```

**请求参数** (Query Params):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| positionName | String | 否 | 模糊：岗位名称 |
| communityName | String | 否 | 模糊：社区名称 |
| supervisingDept | String | 否 | 模糊：主管部门 |
| positionType | String | 否 | 精确：岗位类型 |
| province | String | 否 | 精确：省份 |
| city | String | 否 | 精确：城市 |
| positionStatus | String | 否 | 精确：状态 |

**响应字段** (分页列表):

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| communityName | String | 社区名称 |
| positionName | String | 岗位名称 |
| supervisingDept | String | 主管部门 |
| positionType | String | 岗位类型 |
| province | String | 省份 |
| city | String | 城市 |
| positionStatus | String | 状态 |

### 2.2 查看详情 ~ 2.8 导入Excel

接口模式与基层服务项目完全一致，路径前缀替换为 `/community/`。

**Excel 表头对照**:

| Excel表头 | 对应字段 | 说明 |
|-----------|----------|------|
| 街道办事处/乡镇 | streetOffice | |
| 社区名称 | communityName | |
| 主管部门 | supervisingDept | |
| 区/县 | district | |
| 岗位名称 | positionName | |
| 岗位类型 | positionType | |
| 用工形式 | employmentType | |
| 省份 | province | |
| 城市 | city | |
| 工作地点 | workLocation | |
| 学历要求 | educationRequirement | |
| 年龄上限 | ageLimit | |
| 招聘人数 | recruitmentCount | |
| 专业要求 | majorRequirement | |
| 户籍要求 | householdRequirement | |
| 政治面貌 | politicalStatus | |
| 工作经验 | workExperience | |
| 社工证要求 | socialWorkCert | |
| 社区经验要求 | communityExperience | |
| 居住地要求 | residenceRequirement | |
| 薪资待遇 | salaryRange | |
| 薪资构成 | salaryComposition | |
| 福利待遇 | benefits | |
| 笔试内容 | examContent | |
| 面试形式 | interviewForm | |
| 报名开始 | regStartDate | |
| 报名截止 | regEndDate | |
| 考试时间 | examTime | |
| 状态 | positionStatus | 招聘中/已结束/即将开始 |
| 报名链接 | applyLink | |
| 报名方式 | applyMethod | |
| 联系电话 | contactPhone | |
| 报名地址 | contactAddress | |
| 备注 | remark | |
| 详细说明 | content | 支持HTML |
| 排序 | sortOrder | |

---

## 三、公益性岗位

**Controller**: `PublicWelfarePositionController`
**表**: `t_public_welfare_position`
**路径前缀**: `/api/v1/admin/employment/grassroots-position/welfare`

### 3.1 分页查询（公开接口，无需登录）

```
GET /api/v1/admin/employment/grassroots-position/welfare/list
```

**请求参数** (Query Params):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| positionName | String | 否 | 模糊：岗位名称 |
| developingUnit | String | 否 | 模糊：开发单位 |
| employingUnit | String | 否 | 模糊：用工单位 |
| positionCategory | String | 否 | 精确：岗位类别 |
| province | String | 否 | 精确：省份 |
| city | String | 否 | 精确：城市 |
| district | String | 否 | 精确：区/县 |
| maxServiceYears | Integer | 否 | 精确：最长服务年限 |
| positionStatus | String | 否 | 精确：状态 |

**响应字段** (分页列表):

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| developingUnit | String | 开发单位 |
| employingUnit | String | 用工单位 |
| positionName | String | 岗位名称 |
| positionCategory | String | 岗位类别 |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| monthlySalary | String | 月工资 |
| regStartDate | Timestamptz | 报名开始 |
| regEndDate | Timestamptz | 报名截止 |
| positionStatus | String | 状态 |

### 3.2 ~ 3.8 接口（均需登录）

接口模式与基层服务项目一致，路径前缀替换为 `/welfare/`。

**Excel 表头对照**:

| Excel表头 | 对应字段 | 说明 |
|-----------|----------|------|
| 开发单位 | developingUnit | |
| 用工单位 | employingUnit | |
| 岗位名称 | positionName | |
| 岗位类别 | positionCategory | |
| 工作内容 | workContent | |
| 省份 | province | |
| 城市 | city | |
| 区/县 | district | |
| 工作地点 | workLocation | |
| 面向人群 | targetGroup | 逗号分隔，如"低保户,残疾人" |
| 学历要求 | educationRequirement | |
| 年龄范围 | ageRange | |
| 身体条件 | healthRequirement | |
| 招聘人数 | recruitmentCount | |
| 户籍要求 | householdRequirement | |
| 困难认定 | employmentDifficultyCert | true/false |
| 其他要求 | otherRequirement | |
| 合同期限 | contractPeriod | |
| 可续签 | isRenewable | true/false |
| 最长服务年限 | maxServiceYears | |
| 月工资 | monthlySalary | |
| 工资来源 | salarySource | |
| 补贴标准 | subsidyStandard | |
| 社保缴纳 | socialInsuranceInfo | |
| 其他福利 | otherBenefits | |
| 工作时间 | workSchedule | |
| 是否倒班 | isShiftWork | true/false |
| 报名开始 | regStartDate | |
| 报名截止 | regEndDate | |
| 报名方式 | applyMethod | |
| 报名地址 | applyAddress | |
| 所需材料 | requiredDocuments | |
| 状态 | positionStatus | 招聘中/已结束/即将开始 |
| 联系电话 | contactPhone | |
| 联系人 | contactPerson | |
| 备注 | remark | |
| 详细说明 | content | 支持HTML |
| 排序 | sortOrder | |

---

## 四、公共 DTO

### StatusDTO

```json
{
  "status": 0
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 0=禁用, 1=启用 |

---

## 五、通用说明

### 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 / Excel校验不通过 |
| 401 | 未登录 / Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### @OperationLog 注解使用

修改、删除、禁用/启用、批量删除、导入操作均添加 `@OperationLog` 注解，自动记录操作日志到 `sys_operation_log` 表。

### Excel校验

- 先调用 `pre-validate` 接口校验文件格式和字段合法性
- 校验通过后再调用 `import` 接口执行导入
- 导入过程事务一致性，出错全部回滚
- 错误信息明确到行号和字段名
