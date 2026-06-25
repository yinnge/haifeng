# 基层服务岗位模块 C端 API 文档

## 概述

基层服务岗位模块包含三个子模块，提供岗位查询能力。

| 子模块 | 说明 | 表名 |
|-------|------|------|
| 基层服务项目岗位 | 三支一扶 + 西部计划 | t_grassroots_project_position |
| 社区工作者岗位 | 社区党务/服务/网格员等 | t_community_position |
| 公益性岗位 | 公共管理/服务/环境等 | t_public_welfare_position |

---

## 一、基层服务项目岗位 (Grassroots Project Position)

### 1.1 分页列表查询

> 无需登录

```
GET /api/v1/app/employment/grassroots/project/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 | 匹配方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认 1 | - |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 | - |
| positionName | String | 否 | 岗位名称 | LIKE |
| organizingDept | String | 否 | 组织单位 | LIKE |
| serviceUnit | String | 否 | 服务单位 | LIKE |
| projectType | String | 否 | 项目类型（三支一扶/西部计划） | = |
| year | String | 否 | 招募年份 | = |
| serviceType | String | 否 | 服务类型 | = |
| province | String | 否 | 省份 | = |
| city | String | 否 | 城市 | = |
| county | String | 否 | 区/县 | = |
| educationRequirement | String | 否 | 学历要求 | = |
| majorRequirement | String | 否 | 专业要求 | = |
| gradYearRequirement | String | 否 | 毕业年份要求 | = |
| politicalStatus | String | 否 | 政治面貌 | = |
| positionStatus | String | 否 | 状态（招募中/已结束/即将开始） | = |
| ageLimitMin | Integer | 否 | 年龄下限（>=） | 范围 |
| ageLimitMax | Integer | 否 | 年龄上限（<=） | 范围 |

> 模糊查询 + 精准查询 为 AND 关系。
> **排序规则**：按 `sort_order` 降序（值越大优先级越高），相同 `sort_order` 的按 `created_at` 降序。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "projectType": "三支一扶",
        "year": "2026",
        "positionName": "乡镇支教教师",
        "serviceType": "支教",
        "organizingDept": "省人社厅",
        "serviceUnit": "某乡镇中心小学",
        "province": "广东省",
        "city": "广州市",
        "county": "从化区",
        "township": "良口镇",
        "servicePeriod": "2年",
        "educationRequirement": "本科及以上",
        "majorRequirement": "教育学类",
        "ageLimit": 30,
        "recruitmentCount": 5,
        "politicalStatus": "不限"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 列表返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |
| projectType | String | 项目类型 |
| year | String | 招募年份 |
| positionName | String | 岗位名称 |
| serviceType | String | 服务类型 |
| organizingDept | String | 组织单位 |
| serviceUnit | String | 服务单位 |
| province | String | 省份 |
| city | String | 城市 |
| county | String | 区/县 |
| township | String | 乡镇/街道 |
| servicePeriod | String | 服务期限 |
| educationRequirement | String | 学历要求 |
| majorRequirement | String | 专业要求 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招募人数 |
| politicalStatus | String | 政治面貌 |

---

### 1.2 详情查询

> 需要登录 (@RequireLogin)

```
GET /api/v1/app/employment/grassroots/project/{id}/detail
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 岗位ID |

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "雪花ID",
    "projectType": "三支一扶",
    "year": "2026",
    "positionName": "乡镇支教教师",
    "serviceType": "支教",
    "organizingDept": "省人社厅",
    "serviceUnit": "某乡镇中心小学",
    "province": "广东省",
    "city": "广州市",
    "county": "从化区",
    "township": "良口镇",
    "servicePeriod": "2年",
    "serviceStartDate": "2026-09-01",
    "serviceEndDate": "2028-08-31",
    "educationRequirement": "本科及以上",
    "majorRequirement": "教育学类",
    "ageLimit": 30,
    "recruitmentCount": 5,
    "gradYearRequirement": "2024-2026届",
    "householdRequirement": "不限",
    "otherRequirement": "具有教师资格证优先",
    "politicalStatus": "不限",
    "examContent": "公共基础知识+教育基础知识",
    "examTime": "2026-07-15T09:00:00+08:00",
    "interviewForm": "结构化面试",
    "monthlySubsidy": "3000元/月",
    "socialInsurance": "五险一金",
    "housingInfo": "提供住宿",
    "otherBenefits": "交通补贴200元/月",
    "afterServicePolicy": "服务期满考核合格后，可享受公务员定向招录",
    "canTransferToCivil": true,
    "canTransferToInstitution": true,
    "examBonusPoints": "笔试加5分",
    "tuitionCompensation": "最高8000元/年",
    "postgradBonus": "初试总分加10分",
    "regStartDate": "2026-06-01T00:00:00+08:00",
    "regEndDate": "2026-06-20T23:59:59+08:00",
    "applyLink": "https://example.com/apply",
    "positionStatus": "招募中",
    "contactPhone": "020-12345678",
    "remark": "备注信息",
    "content": "<p>详细说明HTML内容</p>"
  },
  "timestamp": 1718000000000
}
```

#### 详情返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |
| projectType | String | 项目类型 |
| year | String | 招募年份 |
| positionName | String | 岗位名称 |
| serviceType | String | 服务类型 |
| organizingDept | String | 组织单位 |
| serviceUnit | String | 服务单位 |
| province | String | 省份 |
| city | String | 城市 |
| county | String | 区/县 |
| township | String | 乡镇/街道 |
| servicePeriod | String | 服务期限 |
| serviceStartDate | String | 服务开始日期 |
| serviceEndDate | String | 服务结束日期 |
| educationRequirement | String | 学历要求 |
| majorRequirement | String | 专业要求 |
| ageLimit | Integer | 年龄上限(周岁) |
| recruitmentCount | Integer | 招募人数 |
| gradYearRequirement | String | 毕业年份要求 |
| householdRequirement | String | 户籍要求 |
| otherRequirement | String | 其他要求 |
| politicalStatus | String | 政治面貌 |
| examContent | String | 笔试内容 |
| examTime | String (TIMESTAMPTZ) | 考试时间 |
| interviewForm | String | 面试形式 |
| monthlySubsidy | String | 月补贴标准 |
| socialInsurance | String | 社保缴纳说明 |
| housingInfo | String | 住房安排 |
| otherBenefits | String | 其他待遇 |
| afterServicePolicy | String | 期满政策综述 |
| canTransferToCivil | Boolean | 期满可否定向考公 |
| canTransferToInstitution | Boolean | 期满可否转事业编 |
| examBonusPoints | String | 考试加分政策 |
| tuitionCompensation | String | 学费补偿/贷款代偿 |
| postgradBonus | String | 考研加分 |
| regStartDate | String (TIMESTAMPTZ) | 报名开始 |
| regEndDate | String (TIMESTAMPTZ) | 报名截止 |
| applyLink | String | 报名链接 |
| positionStatus | String | 状态 |
| contactPhone | String | 联系电话 |
| remark | String | 备注 |
| content | String | 详细说明(支持HTML) |

---

### 1.3 备考指南查询

> 无需登录，根据 `guide_category = 'grassroots'` 过滤

```
GET /api/v1/app/employment/grassroots/project/exam-guide/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 |
| title | String | 否 | 标题 (LIKE) |
| subtitle | String | 否 | 副标题 (LIKE) |
| guideType | String | 否 | 指南类型 (=) |
| difficultyLevel | String | 否 | 难度等级 (=) |
| authorTitle | String | 否 | 作者职称/头衔 (LIKE) |
| authorName | String | 否 | 作者姓名 (LIKE) |

> 注意：`guideCategory` 由服务端硬编码为 `grassroots`，前端无需传入。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "guideCategory": "grassroots",
        "guideType": "备考指南",
        "title": "三支一扶备考攻略",
        "subtitle": "2026年最新政策解读",
        "coverImage": "https://example.com/cover.jpg",
        "iconClass": "icon-grassroots",
        "summary": "本文详细介绍三支一扶考试内容及备考策略",
        "content": "<p>详细内容HTML</p>",
        "tags": ["三支一扶", "支教", "备考"],
        "difficultyLevel": "中级",
        "targetAudience": "应届毕业生",
        "authorName": "张老师",
        "authorTitle": "资深公考讲师",
        "isTop": false,
        "isRecommended": true,
        "sortOrder": 1,
        "viewCount": 1200,
        "likeCount": 86
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 指南ID |
| guideCategory | String | 指南分类（固定为 grassroots） |
| guideType | String | 指南类型 |
| title | String | 标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图 |
| iconClass | String | 图标样式类 |
| summary | String | 摘要 |
| content | String | 详细内容（支持HTML） |
| tags | String[] | 标签列表 |
| difficultyLevel | String | 难度等级 |
| targetAudience | String | 目标受众 |
| authorName | String | 作者名称 |
| authorTitle | String | 作者头衔 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否推荐 |
| sortOrder | Integer | 排序号 |
| viewCount | Integer | 浏览量 |
| likeCount | Integer | 点赞数 |

---

### 1.4 公告表查询

> 无需登录，根据 `notice_category = 'grassroots'` 过滤

```
GET /api/v1/app/employment/grassroots/project/notice/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 |
| title | String | 否 | 标题 (LIKE) |
| summary | String | 否 | 摘要 (LIKE) |
| source | String | 否 | 来源 (LIKE) |
| noticeType | String | 否 | 公告类型 (=) |
| province | String | 否 | 省份 (=) |
| city | String | 否 | 城市 (=) |
| year | String | 否 | 年份 (=) |

> 注意：`noticeCategory` 由服务端硬编码为 `grassroots`，前端无需传入。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "noticeCategory": "grassroots",
        "noticeType": "招募公告",
        "title": "2026年广东省三支一扶招募公告",
        "summary": "广东省2026年三支一扶计划招募公告",
        "content": "<p>公告详细内容HTML</p>",
        "province": "广东省",
        "city": "广州市",
        "tags": ["三支一扶", "广东"],
        "year": "2026",
        "source": "广东省人社厅",
        "sourceUrl": "https://example.com/notice",
        "publishDate": "2026-05-01T00:00:00+08:00",
        "publishUnit": "广东省人力资源和社会保障厅",
        "regStartDate": "2026-06-01T00:00:00+08:00",
        "regEndDate": "2026-06-20T23:59:59+08:00",
        "examTime": "2026-07-15T09:00:00+08:00",
        "recruitmentCount": 3000,
        "isTop": true,
        "isImportant": true,
        "viewCount": 5000
      }
    ],
    "total": 30,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 公告ID |
| noticeCategory | String | 公告分类（固定为 grassroots） |
| noticeType | String | 公告类型 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 详细内容（支持HTML） |
| province | String | 省份 |
| city | String | 城市 |
| tags | String[] | 标签列表 |
| year | String | 年份 |
| source | String | 来源 |
| sourceUrl | String | 来源链接 |
| publishDate | String (TIMESTAMPTZ) | 发布时间 |
| publishUnit | String | 发布单位 |
| regStartDate | String (TIMESTAMPTZ) | 报名开始 |
| regEndDate | String (TIMESTAMPTZ) | 报名截止 |
| examTime | String (TIMESTAMPTZ) | 考试时间 |
| recruitmentCount | Integer | 招募人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| viewCount | Integer | 浏览量 |

---

## 二、社区工作者岗位 (Community Position)

### 2.1 分页列表查询

> 无需登录

```
GET /api/v1/app/employment/grassroots/community/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 | 匹配方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认 1 | - |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 | - |
| positionName | String | 否 | 岗位名称 | LIKE |
| streetOffice | String | 否 | 街道办事处/乡镇 | LIKE |
| communityName | String | 否 | 社区名称 | LIKE |
| supervisingDept | String | 否 | 主管部门 | LIKE |
| positionType | String | 否 | 岗位类型 | = |
| employmentType | String | 否 | 用工形式 | = |
| province | String | 否 | 省份 | = |
| city | String | 否 | 城市 | = |
| educationRequirement | String | 否 | 学历要求 | = |
| majorRequirement | String | 否 | 专业要求 | = |
| politicalStatus | String | 否 | 政治面貌 | = |
| workExperience | String | 否 | 工作经验 | = |
| positionStatus | String | 否 | 状态（招聘中/已结束/即将开始） | = |
| ageLimitMin | Integer | 否 | 年龄下限（>=） | 范围 |
| ageLimitMax | Integer | 否 | 年龄上限（<=） | 范围 |

> 模糊查询 + 精准查询 为 AND 关系。
> **排序规则**：按 `sort_order` 降序（值越大优先级越高），相同 `sort_order` 的按 `created_at` 降序。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "communityName": "幸福社区",
        "district": "天河区",
        "positionName": "社区网格员",
        "educationRequirement": "大专",
        "majorRequirement": "不限",
        "positionType": "社区网格员",
        "province": "广东省",
        "city": "广州市",
        "ageLimit": 40,
        "recruitmentCount": 3,
        "workExperience": "不限"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 列表返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |
| communityName | String | 社区名称 |
| district | String | 区/县 |
| positionName | String | 岗位名称 |
| educationRequirement | String | 学历要求 |
| majorRequirement | String | 专业要求 |
| positionType | String | 岗位类型 |
| province | String | 省份 |
| city | String | 城市 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招聘人数 |
| workExperience | String | 工作经验 |

---

### 2.2 详情查询

> 需要登录 (@RequireLogin)

```
GET /api/v1/app/employment/grassroots/community/{id}/detail
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 岗位ID |

#### 详情返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |
| streetOffice | String | 街道办事处/乡镇 |
| communityName | String | 社区名称 |
| supervisingDept | String | 主管部门 |
| district | String | 区/县 |
| positionName | String | 岗位名称 |
| positionType | String | 岗位类型 |
| employmentType | String | 用工形式 |
| province | String | 省份 |
| city | String | 城市 |
| workLocation | String | 详细工作地点 |
| educationRequirement | String | 学历要求 |
| ageLimit | Integer | 年龄上限(周岁) |
| recruitmentCount | Integer | 招聘人数 |
| majorRequirement | String | 专业要求 |
| householdRequirement | String | 户籍要求 |
| politicalStatus | String | 政治面貌 |
| workExperience | String | 工作经验 |
| socialWorkCert | String | 社工证要求 |
| communityExperience | String | 社区工作经验 |
| residenceRequirement | String | 居住地要求 |
| salaryRange | String | 薪资待遇 |
| salaryComposition | String | 薪资构成说明 |
| benefits | String | 福利待遇 |
| examContent | String | 笔试内容 |
| interviewForm | String | 面试形式 |
| regStartDate | String (TIMESTAMPTZ) | 报名开始 |
| regEndDate | String (TIMESTAMPTZ) | 报名截止 |
| examTime | String (TIMESTAMPTZ) | 考试时间 |
| positionStatus | String | 状态 |
| applyLink | String | 报名链接 |
| applyMethod | String | 报名方式 |
| contactPhone | String | 联系电话 |
| contactAddress | String | 现场报名地址 |
| remark | String | 备注 |
| content | String | 详细说明(支持HTML) |

---

### 2.3 备考指南查询

> 无需登录，根据 `guide_category = 'community'` 过滤

```
GET /api/v1/app/employment/grassroots/community/exam-guide/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 |
| title | String | 否 | 标题 (LIKE) |
| subtitle | String | 否 | 副标题 (LIKE) |
| guideType | String | 否 | 指南类型 (=) |
| difficultyLevel | String | 否 | 难度等级 (=) |
| authorTitle | String | 否 | 作者职称/头衔 (LIKE) |
| authorName | String | 否 | 作者姓名 (LIKE) |

> 注意：`guideCategory` 由服务端硬编码为 `community`，前端无需传入。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "guideCategory": "community",
        "guideType": "备考指南",
        "title": "社区工作者考试备考指南",
        "subtitle": "2026年最新考情分析",
        "coverImage": "https://example.com/cover.jpg",
        "iconClass": "icon-community",
        "summary": "社区工作者考试内容及备考策略详解",
        "content": "<p>详细内容HTML</p>",
        "tags": ["社区工作者", "备考"],
        "difficultyLevel": "初级",
        "targetAudience": "求职者",
        "authorName": "李老师",
        "authorTitle": "社区工作培训师",
        "isTop": false,
        "isRecommended": true,
        "sortOrder": 1,
        "viewCount": 800,
        "likeCount": 56
      }
    ],
    "total": 30,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 返回字段

同 1.3 备考指南返回字段。

---

### 2.4 公告表查询

> 无需登录，根据 `notice_category = 'community'` 过滤

```
GET /api/v1/app/employment/grassroots/community/notice/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 |
| title | String | 否 | 标题 (LIKE) |
| summary | String | 否 | 摘要 (LIKE) |
| source | String | 否 | 来源 (LIKE) |
| noticeType | String | 否 | 公告类型 (=) |
| province | String | 否 | 省份 (=) |
| city | String | 否 | 城市 (=) |
| year | String | 否 | 年份 (=) |

> 注意：`noticeCategory` 由服务端硬编码为 `community`，前端无需传入。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "noticeCategory": "community",
        "noticeType": "招聘公告",
        "title": "2026年广州市社区专职工作者招聘公告",
        "summary": "广州市2026年社区专职工作者招聘",
        "content": "<p>公告详细内容HTML</p>",
        "province": "广东省",
        "city": "广州市",
        "tags": ["社区工作者", "广州"],
        "year": "2026",
        "source": "广州市民政局",
        "sourceUrl": "https://example.com/notice",
        "publishDate": "2026-05-15T00:00:00+08:00",
        "publishUnit": "广州市民政局",
        "regStartDate": "2026-06-01T00:00:00+08:00",
        "regEndDate": "2026-06-15T23:59:59+08:00",
        "examTime": "2026-07-01T09:00:00+08:00",
        "recruitmentCount": 500,
        "isTop": true,
        "isImportant": true,
        "viewCount": 3000
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 返回字段

同 1.4 公告表返回字段。

---

## 三、公益性岗位 (Public Welfare Position)

### 3.1 分页列表查询

> 无需登录

```
GET /api/v1/app/employment/grassroots/welfare/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 | 匹配方式 |
|------|------|------|------|---------|
| page | Integer | 否 | 页码，默认 1 | - |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 | - |
| positionName | String | 否 | 岗位名称 | LIKE |
| developingUnit | String | 否 | 开发单位 | LIKE |
| employingUnit | String | 否 | 用工单位 | LIKE |
| positionCategory | String | 否 | 岗位类别 | = |
| province | String | 否 | 省份 | = |
| city | String | 否 | 城市 | = |
| district | String | 否 | 区/县 | = |
| educationRequirement | String | 否 | 学历要求 | = |
| householdRequirement | String | 否 | 户籍要求 | = |
| maxServiceYears | Integer | 否 | 最长服务年限 | = |
| positionStatus | String | 否 | 状态（招聘中/已结束/即将开始） | = |
| targetGroup | String | 否 | 面向人群（传数组中的某个值） | = |
| ageRangeMin | Integer | 否 | 年龄下限（>=） | 范围 |
| ageRangeMax | Integer | 否 | 年龄上限（<=） | 范围 |

> 注意：targetGroup 字段为 TEXT[] 数组类型，精确匹配时需要传入数组中的元素值。
> **排序规则**：按 `sort_order` 降序（值越大优先级越高），相同 `sort_order` 的按 `created_at` 降序。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "developingUnit": "某区人社局",
        "employingUnit": "某街道办事处",
        "positionName": "社区保洁员",
        "positionCategory": "公共环境类",
        "province": "广东省",
        "city": "广州市",
        "district": "越秀区",
        "educationRequirement": "不限",
        "recruitmentCount": 10,
        "monthlySalary": "2800元/月",
        "contractPeriod": "1年",
        "maxServiceYears": 3,
        "regStartDate": "2026-06-01T00:00:00+08:00",
        "regEndDate": "2026-06-15T23:59:59+08:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 列表返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |
| developingUnit | String | 开发单位 |
| employingUnit | String | 用工单位 |
| positionName | String | 岗位名称 |
| positionCategory | String | 岗位类别 |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| educationRequirement | String | 学历要求 |
| recruitmentCount | Integer | 招聘人数 |
| monthlySalary | String | 月工资标准 |
| contractPeriod | String | 合同期限 |
| maxServiceYears | Integer | 最长服务年限 |
| regStartDate | String (TIMESTAMPTZ) | 报名开始 |
| regEndDate | String (TIMESTAMPTZ) | 报名截止 |

---

### 3.2 详情查询

> 需要登录 (@RequireLogin)

```
GET /api/v1/app/employment/grassroots/welfare/{id}/detail
```

#### 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 岗位ID |

#### 详情返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 岗位ID |
| developingUnit | String | 开发单位 |
| employingUnit | String | 用工单位 |
| positionName | String | 岗位名称 |
| positionCategory | String | 岗位类别 |
| workContent | String | 工作内容描述 |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| workLocation | String | 详细工作地点 |
| targetGroup | String[] | 面向人群列表 |
| educationRequirement | String | 学历要求 |
| ageRange | String | 年龄范围 |
| healthRequirement | String | 身体条件要求 |
| recruitmentCount | Integer | 招聘人数 |
| householdRequirement | String | 户籍要求 |
| employmentDifficultyCert | Boolean | 是否需要就业困难认定证明 |
| otherRequirement | String | 其他要求 |
| contractPeriod | String | 合同期限 |
| isRenewable | Boolean | 是否可续签 |
| maxServiceYears | Integer | 最长服务年限 |
| monthlySalary | String | 月工资标准 |
| salarySource | String | 工资来源 |
| subsidyStandard | String | 岗位补贴标准 |
| socialInsuranceInfo | String | 社保缴纳说明 |
| otherBenefits | String | 其他待遇 |
| workSchedule | String | 工作时间安排 |
| isShiftWork | Boolean | 是否倒班 |
| regStartDate | String (TIMESTAMPTZ) | 报名开始 |
| regEndDate | String (TIMESTAMPTZ) | 报名截止 |
| applyMethod | String | 报名方式 |
| applyAddress | String | 报名地点 |
| requiredDocuments | String | 报名需携带材料 |
| positionStatus | String | 状态 |
| contactPhone | String | 联系电话 |
| contactPerson | String | 联系人 |
| remark | String | 备注 |
| content | String | 详细说明(支持HTML) |

---

### 3.3 备考指南查询

> 无需登录，根据 `guide_category = 'public_welfare'` 过滤

```
GET /api/v1/app/employment/grassroots/welfare/exam-guide/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 |
| title | String | 否 | 标题 (LIKE) |
| subtitle | String | 否 | 副标题 (LIKE) |
| guideType | String | 否 | 指南类型 (=) |
| difficultyLevel | String | 否 | 难度等级 (=) |
| authorTitle | String | 否 | 作者职称/头衔 (LIKE) |
| authorName | String | 否 | 作者姓名 (LIKE) |

> 注意：`guideCategory` 由服务端硬编码为 `public_welfare`，前端无需传入。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "guideCategory": "public_welfare",
        "guideType": "备考指南",
        "title": "公益性岗位报考指南",
        "subtitle": "2026年最新政策解读",
        "coverImage": "https://example.com/cover.jpg",
        "iconClass": "icon-welfare",
        "summary": "公益性岗位报考条件及待遇详解",
        "content": "<p>详细内容HTML</p>",
        "tags": ["公益性岗位", "报考"],
        "difficultyLevel": "初级",
        "targetAudience": "就业困难人员",
        "authorName": "王老师",
        "authorTitle": "就业指导师",
        "isTop": false,
        "isRecommended": true,
        "sortOrder": 1,
        "viewCount": 600,
        "likeCount": 42
      }
    ],
    "total": 20,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 返回字段

同 1.3 备考指南返回字段。

---

### 3.4 公告表查询

> 无需登录，根据 `notice_category = 'public_welfare'` 过滤

```
GET /api/v1/app/employment/grassroots/welfare/notice/list
```

#### 请求参数 (Query)

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选 10/20/30/50/100 |
| title | String | 否 | 标题 (LIKE) |
| summary | String | 否 | 摘要 (LIKE) |
| source | String | 否 | 来源 (LIKE) |
| noticeType | String | 否 | 公告类型 (=) |
| province | String | 否 | 省份 (=) |
| city | String | 否 | 城市 (=) |
| year | String | 否 | 年份 (=) |

> 注意：`noticeCategory` 由服务端硬编码为 `public_welfare`，前端无需传入。

#### 响应参数

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "雪花ID",
        "noticeCategory": "public_welfare",
        "noticeType": "招聘公告",
        "title": "2026年某区公益性岗位招聘公告",
        "summary": "某区2026年公益性岗位招聘",
        "content": "<p>公告详细内容HTML</p>",
        "province": "广东省",
        "city": "广州市",
        "tags": ["公益性岗位", "广州"],
        "year": "2026",
        "source": "某区人社局",
        "sourceUrl": "https://example.com/notice",
        "publishDate": "2026-05-20T00:00:00+08:00",
        "publishUnit": "某区人力资源和社会保障局",
        "regStartDate": "2026-06-01T00:00:00+08:00",
        "regEndDate": "2026-06-10T23:59:59+08:00",
        "examTime": null,
        "recruitmentCount": 200,
        "isTop": false,
        "isImportant": true,
        "viewCount": 1500
      }
    ],
    "total": 15,
    "page": 1,
    "size": 10
  },
  "timestamp": 1718000000000
}
```

#### 返回字段

同 1.4 公告表返回字段。

---

## 四、公共说明

### 4.1 接口路径汇总

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/grassroots/project/list` | ❌ | 基层服务项目-分页列表 |
| GET | `/api/v1/app/employment/grassroots/project/{id}/detail` | ✅ @RequireLogin | 基层服务项目-详情 |
| GET | `/api/v1/app/employment/grassroots/project/exam-guide/list` | ❌ | 基层服务项目-备考指南 |
| GET | `/api/v1/app/employment/grassroots/project/notice/list` | ❌ | 基层服务项目-公告表 |
| GET | `/api/v1/app/employment/grassroots/community/list` | ❌ | 社区工作者-分页列表 |
| GET | `/api/v1/app/employment/grassroots/community/{id}/detail` | ✅ @RequireLogin | 社区工作者-详情 |
| GET | `/api/v1/app/employment/grassroots/community/exam-guide/list` | ❌ | 社区工作者-备考指南 |
| GET | `/api/v1/app/employment/grassroots/community/notice/list` | ❌ | 社区工作者-公告表 |
| GET | `/api/v1/app/employment/grassroots/welfare/list` | ❌ | 公益性岗位-分页列表 |
| GET | `/api/v1/app/employment/grassroots/welfare/{id}/detail` | ✅ @RequireLogin | 公益性岗位-详情 |
| GET | `/api/v1/app/employment/grassroots/welfare/exam-guide/list` | ❌ | 公益性岗位-备考指南 |
| GET | `/api/v1/app/employment/grassroots/welfare/notice/list` | ❌ | 公益性岗位-公告表 |

### 4.2 公共约束

1. **软删除过滤**：所有查询均自动过滤 `is_deleted = false`
2. **模糊查询**：支持 `position_name`、`organizing_dept`、`service_unit` 等字段的 LIKE 匹配
3. **精准查询**：枚举值/状态字段使用精确等值匹配
4. **范围查询**：`age_limit`/`age_range` 字段支持上下界范围查询
5. **查询组合**：模糊条件与精准条件之间为 **AND** 关系，同类型条件之间也为 **AND** 关系
6. **分页参数**：`size` 可选值：10、20、30、50、100，未传则默认 10
7. **排序规则**：分页列表默认按 `sort_order` 降序（值越大优先级越高），相同 `sort_order` 的按 `created_at` 降序
8. **统一响应格式**：符合项目 `R<T>` 规范，code=200 表示成功
9. **ID 说明**：所有 ID 使用雪花算法生成

### 4.3 错误码

| 错误码 | 说明 | 场景 |
|--------|------|------|
| 200 | 成功 | - |
| 400 | 参数错误 | 分页参数非法、ID格式错误 |
| 401 | 未登录 | 详情接口未传Token或Token过期 |
| 404 | 资源不存在 | ID对应的岗位不存在(含已删除) |
| 500 | 服务器内部错误 | 系统异常 |

### 4.4 包结构参考

```
haifeng-app/
├── controller/employment/grassrootsPosition/
│   ├── GrassrootsProjectPositionController.java
│   ├── CommunityPositionController.java
│   └── PublicWelfarePositionController.java
├── service/employment/grassrootsPosition/
│   ├── GrassrootsProjectPositionService.java
│   ├── CommunityPositionService.java
│   └── PublicWelfarePositionService.java
└── service/impl/employment/grassrootsPosition/
    ├── GrassrootsProjectPositionServiceImpl.java
    ├── CommunityPositionServiceImpl.java
    └── PublicWelfarePositionServiceImpl.java

haifeng-app/dto/employment/grassrootsPosition/
├── GrassrootsProjectPositionSearchDTO.java
├── CommunityPositionSearchDTO.java
└── PublicWelfarePositionSearchDTO.java

haifeng-app/vo/employment/grassrootsPosition/
├── GrassrootsProjectPositionListVO.java
├── GrassrootsProjectPositionDetailVO.java
├── CommunityPositionListVO.java
├── CommunityPositionDetailVO.java
├── PublicWelfarePositionListVO.java
└── PublicWelfarePositionDetailVO.java
```
