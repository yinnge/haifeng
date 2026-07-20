# 基层服务岗位管理 API 文档

## 基础信息

- **模块归属**: admin 端 - 就业管理 - 基层服务管理
- **包名**: `com.haifeng.admin.controller/employment/grassrootsPosition`
- **基础路径**: `/api/v1/admin/employment/grassroots-position`
- **认证方式**: JWT Token (所有接口均需登录，通过 @RequireAdminModule 控制模块权限)
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
| page | Integer | 否 | 页码，默认1，最小1 |
| size | Integer | 否 | 每页条数，默认10，范围10-100 |
| positionName | String | 否 | 模糊查询：岗位名称（最大50字符） |
| organizingDept | String | 否 | 模糊查询：组织单位（最大50字符） |
| serviceUnit | String | 否 | 模糊查询：服务单位（最大50字符） |
| projectType | String | 否 | 精确：项目类型（最大30字符） |
| year | String | 否 | 精确：招募年份（最大10字符） |
| serviceType | String | 否 | 精确：服务类型（最大50字符） |
| province | String | 否 | 精确：省份（最大30字符） |
| city | String | 否 | 精确：城市（最大50字符） |
| county | String | 否 | 精确：区/县（最大50字符） |
| positionStatus | String | 否 | 精确：状态（最大20字符） |

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

**字段长度限制** (UpdateDTO):

| 字段 | 最大长度 | 字段 | 最大长度 |
|------|----------|------|----------|
| projectType | 30 | year | 10 |
| positionName | 200 | serviceType | 50 |
| organizingDept | 200 | serviceUnit | 200 |
| province | 30 | city | 50 |
| county | 50 | township | 100 |
| servicePeriod | 30 | serviceStartDate | 30 |
| serviceEndDate | 30 | educationRequirement | 30 |
| majorRequirement | 500 | gradYearRequirement | 50 |
| householdRequirement | 100 | politicalStatus | 30 |
| examContent | 500 | interviewForm | 100 |
| monthlySubsidy | 50 | socialInsurance | 200 |
| housingInfo | 200 | examBonusPoints | 50 |
| tuitionCompensation | 100 | postgradBonus | 100 |
| applyLink | 500 | positionStatus | 20 |
| contactPhone | 50 | | |

所有 String 字段传 null 表示不修改该字段（部分更新）。

**枚举字段校验**（更新时传入非法值返回 400）:

| 字段 | 允许值 |
|------|--------|
| projectType | 三支一扶、西部计划 |
| serviceType | 支教、支农、支医、帮扶乡村振兴、基础教育、服务三农、医疗卫生、基层青年工作、基层社会管理、服务新疆、服务西藏 |
| educationRequirement | 大专、本科、硕士、大专及以上、本科及以上 |
| positionStatus | 招募中、已结束、即将开始 |

---

### 1.4 软删除（单条）

```
DELETE /api/v1/admin/employment/grassroots-position/project/{id}/delete
```

**路径参数**: id (Long)

软删除：将 `is_deleted` 设为 `true`。

---

### 1.5 更新岗位状态

```
PATCH /api/v1/admin/employment/grassroots-position/project/{id}/status
```

**路径参数**: id (Long)

**请求体** (JSON):

```json
{
  "positionStatus": "招募中"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| positionStatus | String | 是 | 岗位状态，最大20字符 |

**positionStatus 可选值**:

| 值 | 说明 |
|----|------|
| 招募中 | 正在招募（默认值） |
| 已结束 | 招募已结束 |
| 即将开始 | 即将开始招募 |

> **注意**: 基层服务项目使用"招募**中**"，社区工作者和公益性岗位使用"招聘**中**"。

---

### 1.6 批量删除

```
POST /api/v1/admin/employment/grassroots-position/project/batch-delete
```

**请求体** (JSON):

```json
[1001, 1002, 1003]
```

| 限制 | 值 |
|------|-----|
| 方法 | POST（非 DELETE，兼容性考虑） |
| 最大数量 | 100 条 |

**行为说明**:
- 批量软删除：将 `is_deleted` 设为 `true`
- 仅处理未删除的记录（`is_deleted = false`），已删除的记录会被跳过
- 日志输出：`requested=X, actual=Y`（Y 为实际更新行数）

---

### 1.7 校验 Excel

```
POST /api/v1/admin/employment/grassroots-position/project/pre-validate
```

**请求参数**: file (MultipartFile, .xlsx 或 .xls)

**成功响应**: `{ "code": 200, "msg": "校验通过" }`

**失败响应**: `{ "code": 400, "msg": "第3行: 学历要求不能为空; 省份不合法: 北京\n第5行: 项目类型不能为空" }`

---

### 1.8 导入 Excel

```
POST /api/v1/admin/employment/grassroots-position/project/import
```

**请求参数**: file (MultipartFile, .xlsx 或 .xls)

导入所有表字段，失败时全部回滚。**已存在的重复记录自动跳过，仅插入新记录**（去重维度：岗位名称 + 年份 + 项目类型）。

**Excel 表头对照**:

| Excel表头 | 对应字段 | 说明 |
|-----------|----------|------|
| 项目类型 | projectType | 必填，枚举：三支一扶/西部计划 |
| 年份 | year | 必填，最大10字符 |
| 岗位名称 | positionName | 必填，最大200字符 |
| 服务类型 | serviceType | 必填，枚举见下方 |
| 组织单位 | organizingDept | 最大200字符 |
| 服务单位 | serviceUnit | 最大200字符 |
| 省份 | province | 必填，合法省份 |
| 城市 | city | 最大50字符 |
| 区/县 | county | 最大50字符 |
| 乡镇/街道 | township | 最大100字符 |
| 服务期限 | servicePeriod | 必填，最大30字符 |
| 服务开始日期 | serviceStartDate | 最大30字符 |
| 服务结束日期 | serviceEndDate | 最大30字符 |
| 学历要求 | educationRequirement | 必填，枚举：大专/本科/硕士/大专及以上/本科及以上 |
| 专业要求 | majorRequirement | 最大500字符 |
| 年龄上限 | ageLimit | 整数，18-35 |
| 招募人数 | recruitmentCount | 整数，>0 |
| 毕业年份要求 | gradYearRequirement | 最大50字符 |
| 户籍要求 | householdRequirement | 最大100字符 |
| 政治面貌 | politicalStatus | 最大30字符 |
| 其他要求 | otherRequirement | TEXT，无长度限制 |
| 笔试内容 | examContent | 最大500字符 |
| 考试时间 | examTime | 时间格式 |
| 面试形式 | interviewForm | 最大100字符 |
| 月补贴标准 | monthlySubsidy | 最大50字符 |
| 社保缴纳 | socialInsurance | 最大200字符 |
| 住房安排 | housingInfo | 最大200字符 |
| 其他待遇 | otherBenefits | TEXT，无长度限制 |
| 期满政策 | afterServicePolicy | TEXT，无长度限制 |
| 可定向考公 | canTransferToCivil | true/false |
| 可转事业编 | canTransferToInstitution | true/false |
| 考试加分 | examBonusPoints | 最大50字符 |
| 学费补偿 | tuitionCompensation | 最大100字符 |
| 考研加分 | postgradBonus | 最大100字符 |
| 报名开始 | regStartDate | 时间格式 |
| 报名截止 | regEndDate | 时间格式 |
| 报名链接 | applyLink | 最大500字符 |
| 状态 | positionStatus | 枚举：招募中/已结束/即将开始 |
| 联系电话 | contactPhone | 最大50字符 |
| 备注 | remark | TEXT，无长度限制 |
| 详细说明 | content | TEXT，支持HTML |
| 排序 | sortOrder | 整数，默认0 |

**服务类型枚举**: 支教、支农、支医、帮扶乡村振兴、基础教育、服务三农、医疗卫生、基层青年工作、基层社会管理、服务新疆、服务西藏

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
| page | Integer | 否 | 页码，默认1，最小1 |
| size | Integer | 否 | 每页条数，默认10，范围10-100 |
| positionName | String | 否 | 模糊：岗位名称（最大50字符） |
| communityName | String | 否 | 模糊：社区名称（最大50字符） |
| supervisingDept | String | 否 | 模糊：主管部门（最大50字符） |
| positionType | String | 否 | 精确：岗位类型（最大50字符） |
| province | String | 否 | 精确：省份（最大30字符） |
| city | String | 否 | 精确：城市（最大50字符） |
| positionStatus | String | 否 | 精确：状态（最大20字符） |

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

**排序**: `sort_order ASC, updated_at DESC`

---

### 2.2 查看详情

```
GET /api/v1/admin/employment/grassroots-position/community/{id}/detail
```

**路径参数**: id (Long)

**响应字段**: 全部表字段 + isDeleted, createdAt, updatedAt

---

### 2.3 修改

```
PUT /api/v1/admin/employment/grassroots-position/community/{id}/update
```

**路径参数**: id (Long)

**请求体** (JSON):

```json
{
  "streetOffice": "XX街道办事处",
  "communityName": "XX社区",
  "supervisingDept": "民政局",
  "district": "XX区",
  "positionName": "社区网格员",
  "positionType": "社区网格员",
  "employmentType": "合同制",
  "province": "广东省",
  "city": "梅州市",
  "workLocation": "XX社区居委会",
  "educationRequirement": "大专",
  "ageLimit": 35,
  "recruitmentCount": 5,
  "majorRequirement": "不限",
  "householdRequirement": "本市户籍",
  "politicalStatus": "不限",
  "workExperience": "不限",
  "socialWorkCert": "不要求",
  "communityExperience": "有社区工作经验优先",
  "residenceRequirement": "本区居住",
  "salaryRange": "3000-4000元/月",
  "salaryComposition": "基本工资+绩效",
  "benefits": "五险一金",
  "examContent": "公共基础知识",
  "interviewForm": "结构化面试",
  "regStartDate": "2026-04-01T00:00:00Z",
  "regEndDate": "2026-04-30T23:59:59Z",
  "examTime": "2026-05-15T09:00:00Z",
  "positionStatus": "招聘中",
  "applyLink": "http://example.com/apply",
  "applyMethod": "现场报名",
  "contactPhone": "0753-1234567",
  "contactAddress": "XX区民政局",
  "remark": "备注",
  "content": "<p>详细说明</p>",
  "sortOrder": 1
}
```

**字段长度限制** (UpdateDTO):

| 字段 | 最大长度 | 字段 | 最大长度 |
|------|----------|------|----------|
| streetOffice | 200 | communityName | 200 |
| supervisingDept | 200 | district | 100 |
| positionName | 200 | positionType | 50 |
| employmentType | 30 | province | 30 |
| city | 50 | workLocation | 200 |
| educationRequirement | 30 | majorRequirement | 500 |
| householdRequirement | 100 | politicalStatus | 30 |
| workExperience | 50 | socialWorkCert | 50 |
| communityExperience | 100 | residenceRequirement | 200 |
| salaryRange | 50 | salaryComposition | 200 |
| examContent | 500 | interviewForm | 100 |
| positionStatus | 20 | applyLink | 500 |
| contactPhone | 50 | contactAddress | 200 |

所有 String 字段传 null 表示不修改该字段（部分更新）。

**枚举字段校验**（更新时传入非法值返回 400）:

| 字段 | 允许值 |
|------|--------|
| positionType | 社区党务工作者、社区服务工作者、社区网格员、社区调解员、社区安全员、社区文化专干、社会工作师、综合岗、其他 |
| employmentType | 事业编制、合同制、政府购买服务、公益性岗位 |
| educationRequirement | 不限、高中、大专、本科、硕士 |
| socialWorkCert | 不要求、初级社工师、中级社工师、高级社工师、优先 |
| positionStatus | 招聘中、已结束、即将开始 |

---

### 2.4 软删除（单条）

```
DELETE /api/v1/admin/employment/grassroots-position/community/{id}/delete
```

---

### 2.5 更新岗位状态

```
PATCH /api/v1/admin/employment/grassroots-position/community/{id}/status
```

**请求体** (JSON):

```json
{
  "positionStatus": "招聘中"
}
```

**positionStatus 可选值**:

| 值 | 说明 |
|----|------|
| 招聘中 | 正在招聘（默认值） |
| 已结束 | 招聘已结束 |
| 即将开始 | 即将开始招聘 |

---

### 2.6 批量删除

```
POST /api/v1/admin/employment/grassroots-position/community/batch-delete
```

**请求体** (JSON):

```json
[1001, 1002, 1003]
```

方法为 POST，最大100条。行为与 1.6 一致。

---

### 2.7 校验 Excel

```
POST /api/v1/admin/employment/grassroots-position/community/pre-validate
```

**请求参数**: file (MultipartFile, .xlsx 或 .xls)

---

### 2.8 导入 Excel

```
POST /api/v1/admin/employment/grassroots-position/community/import
```

**请求参数**: file (MultipartFile, .xlsx 或 .xls)

**已存在的重复记录自动跳过，仅插入新记录**（去重维度：岗位名称 + 省份 + 城市）。

**Excel 表头对照**:

| Excel表头 | 对应字段 | 说明 |
|-----------|----------|------|
| 街道办事处/乡镇 | streetOffice | 必填，最大200字符 |
| 社区名称 | communityName | 最大200字符 |
| 主管部门 | supervisingDept | 最大200字符 |
| 区/县 | district | 最大100字符 |
| 岗位名称 | positionName | 必填，最大200字符 |
| 岗位类型 | positionType | 必填，枚举见下方 |
| 用工形式 | employmentType | 必填，枚举见下方 |
| 省份 | province | 必填，合法省份 |
| 城市 | city | 必填，最大50字符 |
| 工作地点 | workLocation | 最大200字符 |
| 学历要求 | educationRequirement | 枚举：不限/高中/大专/本科/硕士 |
| 年龄上限 | ageLimit | 整数，18-55 |
| 招聘人数 | recruitmentCount | 整数，>0 |
| 专业要求 | majorRequirement | 最大500字符 |
| 户籍要求 | householdRequirement | 最大100字符 |
| 政治面貌 | politicalStatus | 最大30字符 |
| 工作经验 | workExperience | 最大50字符 |
| 社工证要求 | socialWorkCert | 枚举见下方 |
| 社区经验要求 | communityExperience | 最大100字符 |
| 居住地要求 | residenceRequirement | 最大200字符 |
| 薪资待遇 | salaryRange | 最大50字符 |
| 薪资构成 | salaryComposition | 最大200字符 |
| 福利待遇 | benefits | TEXT，无长度限制 |
| 笔试内容 | examContent | 最大500字符 |
| 面试形式 | interviewForm | 最大100字符 |
| 报名开始 | regStartDate | 时间格式 |
| 报名截止 | regEndDate | 时间格式 |
| 考试时间 | examTime | 时间格式 |
| 状态 | positionStatus | 枚举：招聘中/已结束/即将开始 |
| 报名链接 | applyLink | 最大500字符 |
| 报名方式 | applyMethod | TEXT，无长度限制 |
| 联系电话 | contactPhone | 最大50字符 |
| 报名地址 | contactAddress | 最大200字符 |
| 备注 | remark | TEXT，无长度限制 |
| 详细说明 | content | TEXT，支持HTML |
| 排序 | sortOrder | 整数，默认0 |

**岗位类型枚举**: 社区党务工作者、社区服务工作者、社区网格员、社区调解员、社区安全员、社区文化专干、社会工作师、综合岗、其他

**用工形式枚举**: 事业编制、合同制、政府购买服务、公益性岗位

**社工证要求枚举**: 不要求、初级社工师、中级社工师、高级社工师、优先

---

## 三、公益性岗位

**Controller**: `PublicWelfarePositionController`
**表**: `t_public_welfare_position`
**路径前缀**: `/api/v1/admin/employment/grassroots-position/welfare`

### 3.1 分页查询

```
GET /api/v1/admin/employment/grassroots-position/welfare/list
```

**请求参数** (Query Params):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1，最小1 |
| size | Integer | 否 | 每页条数，默认10，范围10-100 |
| positionName | String | 否 | 模糊：岗位名称（最大50字符） |
| developingUnit | String | 否 | 模糊：开发单位（最大50字符） |
| employingUnit | String | 否 | 模糊：用工单位（最大50字符） |
| positionCategory | String | 否 | 精确：岗位类别（最大50字符） |
| province | String | 否 | 精确：省份（最大30字符） |
| city | String | 否 | 精确：城市（最大50字符） |
| district | String | 否 | 精确：区/县（最大50字符） |
| maxServiceYears | Integer | 否 | 精确：最长服务年限 |
| positionStatus | String | 否 | 精确：状态（最大20字符） |

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

**排序**: `sort_order ASC, updated_at DESC`

---

### 3.2 查看详情

```
GET /api/v1/admin/employment/grassroots-position/welfare/{id}/detail
```

**响应字段**: 全部表字段 + isDeleted, createdAt, updatedAt

---

### 3.3 修改

```
PUT /api/v1/admin/employment/grassroots-position/welfare/{id}/update
```

**请求体** (JSON):

```json
{
  "developingUnit": "市人社局",
  "employingUnit": "XX街道办",
  "positionName": "公共管理岗",
  "positionCategory": "公共管理类",
  "workContent": "协助社区管理工作",
  "province": "广东省",
  "city": "梅州市",
  "district": "梅江区",
  "workLocation": "XX街道办",
  "targetGroup": ["低保户", "残疾人"],
  "educationRequirement": "高中",
  "ageRange": "18-45周岁",
  "healthRequirement": "身体健康",
  "recruitmentCount": 10,
  "householdRequirement": "本市户籍",
  "employmentDifficultyCert": true,
  "otherRequirement": "无犯罪记录",
  "contractPeriod": "1年",
  "isRenewable": true,
  "maxServiceYears": 3,
  "monthlySalary": "2500元/月",
  "salarySource": "就业专项资金",
  "subsidyStandard": "岗位补贴1500元/月",
  "socialInsuranceInfo": "缴纳五险",
  "otherBenefits": "餐补300元/月",
  "workSchedule": "周一至周五 8:30-17:30",
  "isShiftWork": false,
  "regStartDate": "2026-04-01T00:00:00Z",
  "regEndDate": "2026-04-30T23:59:59Z",
  "applyMethod": "现场报名",
  "applyAddress": "XX区人社局",
  "requiredDocuments": "身份证、户口本、就业困难证明",
  "positionStatus": "招聘中",
  "contactPhone": "0753-1234567",
  "contactPerson": "张老师",
  "remark": "备注",
  "content": "<p>详细说明</p>",
  "sortOrder": 1
}
```

**字段长度限制** (UpdateDTO):

| 字段 | 最大长度 | 字段 | 最大长度 |
|------|----------|------|----------|
| developingUnit | 200 | employingUnit | 200 |
| positionName | 200 | positionCategory | 50 |
| province | 30 | city | 50 |
| district | 50 | workLocation | 200 |
| educationRequirement | 30 | ageRange | 50 |
| healthRequirement | 200 | householdRequirement | 100 |
| contractPeriod | 30 | monthlySalary | 50 |
| salarySource | 100 | subsidyStandard | 200 |
| socialInsuranceInfo | 200 | workSchedule | 100 |
| applyAddress | 200 | positionStatus | 20 |
| contactPhone | 50 | contactPerson | 50 |

所有 String 字段传 null 表示不修改该字段（部分更新）。

**枚举字段校验**（更新时传入非法值返回 400）:

| 字段 | 允许值 |
|------|--------|
| positionCategory | 公共管理类、公共服务类、公共环境类、公共安全类、设施维护类、其他 |
| educationRequirement | 不限、初中、高中、大专、本科 |
| positionStatus | 招聘中、已结束、即将开始 |

---

### 3.4 软删除（单条）

```
DELETE /api/v1/admin/employment/grassroots-position/welfare/{id}/delete
```

---

### 3.5 更新岗位状态

```
PATCH /api/v1/admin/employment/grassroots-position/welfare/{id}/status
```

**请求体** (JSON):

```json
{
  "positionStatus": "招聘中"
}
```

**positionStatus 可选值**:

| 值 | 说明 |
|----|------|
| 招聘中 | 正在招聘（默认值） |
| 已结束 | 招聘已结束 |
| 即将开始 | 即将开始招聘 |

---

### 3.6 批量删除

```
POST /api/v1/admin/employment/grassroots-position/welfare/batch-delete
```

**请求体** (JSON):

```json
[1001, 1002, 1003]
```

方法为 POST，最大100条。行为与 1.6 一致。

---

### 3.7 校验 Excel

```
POST /api/v1/admin/employment/grassroots-position/welfare/pre-validate
```

**请求参数**: file (MultipartFile, .xlsx 或 .xls)

---

### 3.8 导入 Excel

```
POST /api/v1/admin/employment/grassroots-position/welfare/import
```

**请求参数**: file (MultipartFile, .xlsx 或 .xls)

**已存在的重复记录自动跳过，仅插入新记录**（去重维度：岗位名称 + 省份 + 城市）。

**Excel 表头对照**:

| Excel表头 | 对应字段 | 说明 |
|-----------|----------|------|
| 开发单位 | developingUnit | 必填，最大200字符 |
| 用工单位 | employingUnit | 最大200字符 |
| 岗位名称 | positionName | 必填，最大200字符 |
| 岗位类别 | positionCategory | 必填，枚举见下方 |
| 工作内容 | workContent | TEXT，无长度限制 |
| 省份 | province | 必填，合法省份 |
| 城市 | city | 必填，最大50字符 |
| 区/县 | district | 最大50字符 |
| 工作地点 | workLocation | 最大200字符 |
| 面向人群 | targetGroup | 逗号分隔，如"低保户,残疾人" |
| 学历要求 | educationRequirement | 枚举：不限/初中/高中/大专/本科 |
| 年龄范围 | ageRange | 最大50字符 |
| 身体条件 | healthRequirement | 最大200字符 |
| 招聘人数 | recruitmentCount | 整数，>0 |
| 户籍要求 | householdRequirement | 最大100字符 |
| 困难认定 | employmentDifficultyCert | true/false |
| 其他要求 | otherRequirement | TEXT，无长度限制 |
| 合同期限 | contractPeriod | 必填，最大30字符 |
| 可续签 | isRenewable | true/false |
| 最长服务年限 | maxServiceYears | 整数，>0 |
| 月工资 | monthlySalary | 最大50字符 |
| 工资来源 | salarySource | 最大100字符 |
| 补贴标准 | subsidyStandard | 最大200字符 |
| 社保缴纳 | socialInsuranceInfo | 最大200字符 |
| 其他福利 | otherBenefits | TEXT，无长度限制 |
| 工作时间 | workSchedule | 最大100字符 |
| 是否倒班 | isShiftWork | true/false |
| 报名开始 | regStartDate | 时间格式 |
| 报名截止 | regEndDate | 时间格式 |
| 报名方式 | applyMethod | TEXT，无长度限制 |
| 报名地址 | applyAddress | 最大200字符 |
| 所需材料 | requiredDocuments | TEXT，无长度限制 |
| 状态 | positionStatus | 枚举：招聘中/已结束/即将开始 |
| 联系电话 | contactPhone | 最大50字符 |
| 联系人 | contactPerson | 最大50字符 |
| 备注 | remark | TEXT，无长度限制 |
| 详细说明 | content | TEXT，支持HTML |
| 排序 | sortOrder | 整数，默认0 |

**岗位类别枚举**: 公共管理类、公共服务类、公共环境类、公共安全类、设施维护类、其他

---

## 四、公共 DTO

### PositionStatusUpdateDTO

用于更新岗位招募状态（PATCH /{id}/status）。

```json
{
  "positionStatus": "招募中"
}
```

| 字段 | 类型 | 必填 | 最大长度 | 说明 |
|------|------|------|----------|------|
| positionStatus | String | 是 | 20 | 岗位状态 |

**各模块可选值**:

| 模块 | 可选值 |
|------|--------|
| 基层服务项目 | 招募中、已结束、即将开始 |
| 社区工作者 | 招聘中、已结束、即将开始 |
| 公益性岗位 | 招聘中、已结束、即将开始 |

---

## 五、Excel 导入详细说明

### 5.1 文件要求

| 要求 | 说明 |
|------|------|
| 格式 | `.xlsx` 或 `.xls`（不接受 CSV） |
| 大小 | 最大 30MB |
| 编码 | UTF-8（中文表头） |
| 表头 | 第一行为表头，必须与文档中的表头文字完全一致 |
| 数据 | 第二行起为数据行 |

### 5.2 导入流程

```
1. 准备 Excel 文件
       ↓
2. 调用 pre-validate 接口预校验
       ↓
   ┌─ 校验通过 → 调用 import 接口导入
   │
   └─ 校验失败 → 根据错误信息修正 Excel → 重新预校验
       ↓
3. 导入完成（全量事务：成功全部插入，失败全部回滚）
```

**推荐流程**: 先调用 `pre-validate` 预校验，确认通过后再调用 `import` 执行导入。也可直接调用 `import`，但失败时会返回错误信息并回滚。

### 5.3 校验规则

#### 必填字段

每个模块的必填字段在上方 Excel 表头对照表中已标注。必填字段为空时校验失败。

#### 枚举值校验

以下字段只能填写指定的枚举值，其他值校验失败：

**基层服务项目**:

| 字段 | 允许值 |
|------|--------|
| 项目类型 | 三支一扶、西部计划 |
| 服务类型 | 支教、支农、支医、帮扶乡村振兴、基础教育、服务三农、医疗卫生、基层青年工作、基层社会管理、服务新疆、服务西藏 |
| 学历要求 | 大专、本科、硕士、大专及以上、本科及以上 |
| 状态 | 招募中、已结束、即将开始 |

**社区工作者**:

| 字段 | 允许值 |
|------|--------|
| 岗位类型 | 社区党务工作者、社区服务工作者、社区网格员、社区调解员、社区安全员、社区文化专干、社会工作师、综合岗、其他 |
| 用工形式 | 事业编制、合同制、政府购买服务、公益性岗位 |
| 学历要求 | 不限、高中、大专、本科、硕士 |
| 社工证要求 | 不要求、初级社工师、中级社工师、高级社工师、优先 |
| 状态 | 招聘中、已结束、即将开始 |

**公益性岗位**:

| 字段 | 允许值 |
|------|--------|
| 岗位类别 | 公共管理类、公共服务类、公共环境类、公共安全类、设施维护类、其他 |
| 学历要求 | 不限、初中、高中、大专、本科 |
| 状态 | 招聘中、已结束、即将开始 |

#### 省份校验

所有模块的"省份"字段必须为合法的中国省份名称（通过 `ProvinceEnum` 校验）。

#### 长度校验

各字段最大长度已在上方 Excel 表头对照表中标注。超过限制校验失败。

#### 数值校验

| 字段 | 规则 |
|------|------|
| 年龄上限 | 基层项目 18-35，社区 18-55 |
| 招募/招聘人数 | > 0 |
| 最长服务年限 | > 0 |

### 5.4 Boolean 字段说明

Excel 中 Boolean 类型字段的填写规则：

| 填写值 | 解析结果 |
|--------|----------|
| true | true |
| false | false |
| 留空 | null（数据库使用默认值） |

涉及字段：困难认定、可续签、是否倒班、可定向考公、可转事业编。

### 5.5 时间字段说明

时间字段支持以下格式：

| 格式 | 示例 |
|------|------|
| yyyy-MM-dd HH:mm:ss | 2026-04-01 00:00:00 |
| yyyy-MM-dd | 2026-04-01（时分秒默认为00:00:00） |
| ISO 8601 | 2026-04-01T00:00:00Z |

涉及字段：报名开始、报名截止、考试时间。

### 5.6 错误展示规则

- 最多显示前 **20** 条错误
- 超过 20 条时末尾追加 `"...共X条错误，仅显示前20条"`
- 错误格式：`第N行: 字段名错误描述; 字段名错误描述`
- 多个错误用分号 `;` 分隔，多行用换行 `\n` 分隔

**示例**:
```
第3行: 岗位名称不能为空; 省份不合法: 北京
第5行: 项目类型不能为空; 学历要求不能为空
...共35条错误，仅显示前20条
```

### 5.7 导入后行为

| 行为 | 说明 |
|------|------|
| ID 生成 | 每条记录自动生成雪花算法 ID |
| is_deleted | 默认 false（可见状态） |
| created_at / updated_at | 由系统自动填充为导入时间 |
| 排序 | sortOrder 默认 0 |
| 重复记录 | 按业务维度（各模块不同）匹配已存在的未删除记录，自动跳过不插入 |
| 重复日志 | 跳过的重复记录会在服务端日志中记录，包含重复记录的名称和维度信息 |
| 后续操作 | 导入后可正常使用"显示/隐藏"和"更新状态"功能 |

### 5.8 事务说明

- 导入操作在单个事务中执行
- 格式/枚举/必填校验失败：全部回滚，无部分成功，导入失败不会产生任何数据
- 校验通过后，已存在的重复记录自动跳过，仅插入新记录（跳过不触发回滚）
- 日志记录跳过的重复记录详情

---

## 六、通用说明

### 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 / Excel校验不通过 / 部分记录不存在 |
| 401 | 未登录 / Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### @OperationLog 注解使用

修改、删除、更新状态、批量删除、导入操作均添加 `@OperationLog` 注解，自动记录操作日志到 `sys_operation_log` 表。

### @RequireAdminModule 权限控制

| Controller | 模块标识 |
|------------|----------|
| GrassrootsProjectPositionController | emp_grassroots_3s |
| CommunityPositionController | emp_grassroots_comm |
| PublicWelfarePositionController | emp_grassroots_welfare |

### 更新状态 vs 软删除 的区别

| 操作 | 接口 | 影响字段 | 说明 |
|------|------|----------|------|
| 更新状态 | PATCH /{id}/status | positionStatus | 修改招募状态（招募中/已结束/即将开始），不影响列表可见性 |
| 显示/隐藏 | 无独立接口 | is_deleted | 控制是否在列表中显示 |
| 软删除 | DELETE /{id}/delete | is_deleted | 隐藏记录（等同于"隐藏"） |
| 批量删除 | POST /batch-delete | is_deleted | 批量隐藏记录 |
