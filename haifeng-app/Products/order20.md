# 行业专项招聘 API 文档

## 功能概述

行业专项招聘模块包含三个独立子模块：**教师招聘**、**医疗卫生**、**银行/金融**。各自独立表结构，提供列表查询（公开）和详情查看（需登录）接口。

## 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，任何人可访问 |
| @RequireLogin | 需要携带有效的 Access Token |

**Token 传递方式：**
```
Authorization: Bearer <access_token>
```

## 模块结构

```
模块：employment
子模块：industryPosition
```

---

# 一、教师招聘（TeacherPosition）

## 1.1 教师招聘列表

公开接口，无需登录，支持模糊搜索 + 多维度精确筛选。

### 1.1.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/teacher/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 1.1.2 请求参数
``
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认 1 |
| size | Integer | 否 | - | 每页条数，默认 10，可选值：10/20/30/50/100 |
| keyword | String | 否 | 模糊 LIKE | 同时匹配 `school_name` 和 `position_name` |
| schoolType | String | 否 | 精确 = | 学校类型：幼儿园/小学/初中/高中/中职/高职/大学/特殊教育学校 |
| schoolNature | String | 否 | 精确 = | 学校性质：公办/民办 |
| subject | String | 否 | 精确 = | 学科：语文/数学/英语/物理/化学/生物/历史/地理/政治/音乐/美术/体育/信息技术/心理健康/通用技术/科学/道德与法治/综合实践/学前教育/特殊教育/其他 |
| province | String | 否 | 精确 = | 省份 |
| city | String | 否 | 精确 = | 城市 |
| district | String | 否 | 精确 = | 区/县 |
| recruitmentCount | Integer | 否 | 精确 = | 招聘人数 |
| ageLimit | Integer | 否 | 判断 >= | 年龄上限（查询条件：`age_limit >= ageLimit`） |
| positionStatus | String | 否 | 精确 = | 状态：招聘中/已结束/即将开始 |
| educationRequirement | String | 否 | 精确 = | 学历要求：不限/大专/本科/硕士/博士 |
| degreeRequirement | String | 否 | 精确 = | 学位要求 |
| majorRequirement | String | 否 | 精确 = | 专业要求 |

### 1.1.3 查询逻辑说明

1. **模糊查询**：`keyword` 同时匹配 `school_name` 或 `position_name`（OR 关系，LIKE %v%）
2. **精确查询**：`schoolType`、`schoolNature`、`subject`、`province`、`city`、`district`、`recruitmentCount`、`positionStatus`、`educationRequirement`、`degreeRequirement`、`majorRequirement` 各自精确匹配
3. **判断查询**：`ageLimit` 使用 `>=` 条件
4. **所有条件之间为 AND 关系**
5. **软删除过滤**：`is_deleted = false`

### 1.1.4 返回字段（TeacherPositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| schoolName | String | 学校名称 |
| schoolType | String | 学校类型 |
| schoolNature | String | 学校性质 |
| positionName | String | 岗位名称 |
| subject | String | 学科 |
| recruitmentType | String | 招聘类型（编制/合同制/特岗教师/人事代理/编外聘用） |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| workExperience | String | 教学经验要求 |
| recruitmentCount | Integer | 招聘人数 |
| ageLimit | Integer | 年龄上限 |
| salaryRange | String | 薪资待遇 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| positionStatus | String | 岗位状态 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |

### 1.1.5 请求示例

```http
GET /api/v1/app/employment/teacher/list?page=1&size=10&keyword=数学&schoolType=高中&province=广东&positionStatus=招聘中 HTTP/1.1
Host: api.haifeng.com
```

### 1.1.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 100001,
        "schoolName": "广州市第一中学",
        "schoolType": "高中",
        "schoolNature": "公办",
        "positionName": "高中数学教师",
        "subject": "数学",
        "recruitmentType": "编制",
        "province": "广东",
        "city": "广州",
        "district": "越秀区",
        "workExperience": "不限",
        "recruitmentCount": 3,
        "ageLimit": 35,
        "salaryRange": "8k-12k",
        "regStartDate": "2026-07-01T00:00:00+08:00",
        "regEndDate": "2026-07-20T23:59:59+08:00",
        "positionStatus": "招聘中",
        "educationRequirement": "本科",
        "degreeRequirement": "学士",
        "majorRequirement": "数学与应用数学、学科教学（数学）"
      }
    ],
    "total": 42,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 1.2 教师招聘详情

需登录后访问，根据列表返回的 ID 获取岗位完整信息。

### 1.2.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/teacher/{id}/detail` |
| 权限 | @RequireLogin |

### 1.2.2 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 教师招聘岗位 ID（t_teacher_position 主键） |

### 1.2.3 返回字段（TeacherPositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| schoolName | String | 学校名称 |
| schoolType | String | 学校类型 |
| schoolNature | String | 学校性质 |
| supervisingDept | String | 主管教育部门 |
| positionName | String | 岗位名称 |
| subject | String | 学科 |
| recruitmentType | String | 招聘类型 |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招聘人数 |
| teacherCertRequirement | String | 教师资格证要求 |
| teacherCertSubject | String | 资格证学科要求 |
| putonghuaLevel | String | 普通话等级要求 |
| otherCertRequirement | String | 其他证书要求 |
| workExperience | String | 教学经验要求 |
| isNormalMajor | String | 是否要求师范专业（要求/优先/不限） |
| salaryRange | String | 薪资待遇 |
| benefits | String | 福利待遇 |
| examContent | String | 笔试内容 |
| interviewForm | String | 面试形式（试讲/说课/结构化/答辩） |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| examTime | OffsetDateTime | 考试时间 |
| positionStatus | String | 岗位状态 |
| applyLink | String | 报名链接 |
| contactPhone | String | 联系电话 |
| remark | String | 备注 |
| content | String | 详细说明（支持HTML） |

### 1.2.4 请求示例

```http
GET /api/v1/app/employment/teacher/100001/detail HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 1.2.5 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 100001,
    "schoolName": "广州市第一中学",
    "schoolType": "高中",
    "schoolNature": "公办",
    "supervisingDept": "广州市教育局",
    "positionName": "高中数学教师",
    "subject": "数学",
    "recruitmentType": "编制",
    "province": "广东",
    "city": "广州",
    "district": "越秀区",
    "educationRequirement": "本科",
    "degreeRequirement": "学士",
    "majorRequirement": "数学与应用数学、学科教学（数学）",
    "ageLimit": 35,
    "recruitmentCount": 3,
    "teacherCertRequirement": "具有相应学科高级中学教师资格证",
    "teacherCertSubject": "数学",
    "putonghuaLevel": "二级乙等",
    "otherCertRequirement": null,
    "workExperience": "不限",
    "isNormalMajor": "优先",
    "salaryRange": "8k-12k",
    "benefits": "五险一金、寒暑假、绩效奖金",
    "examContent": "教育综合知识+学科专业知识",
    "interviewForm": "试讲+结构化面试",
    "regStartDate": "2026-07-01T00:00:00+08:00",
    "regEndDate": "2026-07-20T23:59:59+08:00",
    "examTime": "2026-08-01T09:00:00+08:00",
    "positionStatus": "招聘中",
    "applyLink": "https://example.com/apply/123",
    "contactPhone": "020-12345678",
    "remark": "需在报名截止前完成网上确认",
    "content": "<p>广州市第一中学2026年公开招聘教师公告...</p>"
  },
  "timestamp": 1715580000000
}
```

### 1.2.6 错误码

| code | msg |
|------|-----|
| 404 | 岗位不存在 |
| 401 | 未登录或Token已过期 |

---

## 1.3 教师招聘备考指南

公开接口，无需登录，根据 `guide_category=teacher` 预设条件查询备考指南。

### 1.3.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/teacher/exam-guide/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 1.3.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选值：10/20/30/50/100 |

### 1.3.3 查询逻辑说明

1. **预设过滤**：`guide_category = 'teacher'`
2. **软删除过滤**：`is_deleted = false`

### 1.3.4 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| guideCategory | String | 指南分类（teacher） |
| guideType | String | 指南类型 |
| title | String | 标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图 |
| iconClass | String | 图标样式 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| tags | String | 标签 |
| difficultyLevel | String | 难度等级 |
| targetAudience | String | 目标受众 |
| authorName | String | 作者姓名 |
| authorTitle | String | 作者职称 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否推荐 |
| sortOrder | Integer | 排序号 |
| viewCount | Integer | 浏览量 |
| likeCount | Integer | 点赞量 |

### 1.3.5 请求示例

```http
GET /api/v1/app/employment/teacher/exam-guide/list?page=1&size=10 HTTP/1.1
Host: api.haifeng.com
```

### 1.3.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 400001,
        "guideCategory": "teacher",
        "guideType": "笔试指南",
        "title": "2026年教师招聘备考攻略",
        "subtitle": "教综+学科专业知识全面解析",
        "coverImage": "https://example.com/cover/teacher-guide.png",
        "iconClass": "icon-teacher",
        "summary": "涵盖教育综合知识和学科专业知识的备考策略",
        "content": "<p>2026年教师招聘备考攻略...</p>",
        "tags": "教师招聘,备考,教综",
        "difficultyLevel": "中级",
        "targetAudience": "应届毕业生",
        "authorName": "王老师",
        "authorTitle": "高级教师",
        "isTop": true,
        "isRecommended": true,
        "sortOrder": 1,
        "viewCount": 12580,
        "likeCount": 836
      }
    ],
    "total": 5,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 1.4 教师招聘公告

公开接口，无需登录，根据 `notice_category=teacher` 预设条件查询公告。

### 1.4.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/teacher/notice/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 1.4.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选值：10/20/30/50/100 |

### 1.4.3 查询逻辑说明

1. **预设过滤**：`notice_category = 'teacher'`
2. **软删除过滤**：`is_deleted = false`

### 1.4.4 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| noticeCategory | String | 公告分类（teacher） |
| noticeType | String | 公告类型 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| province | String | 省份 |
| city | String | 城市 |
| tags | String | 标签 |
| year | Integer | 年份 |
| source | String | 来源 |
| sourceUrl | String | 来源链接 |
| publishDate | OffsetDateTime | 发布日期 |
| publishUnit | String | 发布单位 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| examTime | OffsetDateTime | 考试时间 |
| recruitmentCount | Integer | 招聘人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| viewCount | Integer | 浏览量 |

### 1.4.5 请求示例

```http
GET /api/v1/app/employment/teacher/notice/list?page=1&size=10 HTTP/1.1
Host: api.haifeng.com
```

### 1.4.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 500001,
        "noticeCategory": "teacher",
        "noticeType": "招聘公告",
        "title": "广州市教育局2026年公开招聘教师公告",
        "summary": "广州市教育局直属学校公开招聘教师500名",
        "content": "<p>根据《广东省事业单位公开招聘人员办法》...</p>",
        "province": "广东",
        "city": "广州",
        "tags": "教师招聘,编制,广州",
        "year": 2026,
        "source": "广州市教育局",
        "sourceUrl": "https://example.com/notice/001",
        "publishDate": "2026-06-01T10:00:00+08:00",
        "publishUnit": "广州市教育局",
        "regStartDate": "2026-07-01T00:00:00+08:00",
        "regEndDate": "2026-07-20T23:59:59+08:00",
        "examTime": "2026-08-01T09:00:00+08:00",
        "recruitmentCount": 500,
        "isTop": true,
        "isImportant": true,
        "viewCount": 25680
      }
    ],
    "total": 3,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

# 二、医疗卫生（HealthcarePosition）

## 2.1 医疗卫生列表

公开接口，无需登录，支持模糊搜索 + 多维度精确筛选。

### 2.1.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/healthcare/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 2.1.2 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认 1 |
| size | Integer | 否 | - | 每页条数，默认 10，可选值：10/20/30/50/100 |
| keyword | String | 否 | 模糊 LIKE | 同时匹配 `institution_name` 和 `position_name` |
| institutionType | String | 否 | 精确 = | 机构类型：综合医院/专科医院/中医医院/社区卫生服务中心/疾控中心/妇幼保健院/卫生监督所/急救中心/血站/精神卫生中心/康复中心/其他 |
| institutionLevel | String | 否 | 精确 = | 机构等级：三级甲等/三级乙等/二级甲等/二级乙等/一级/未定级/社区 |
| institutionNature | String | 否 | 精确 = | 机构性质：公立/民营 |
| department | String | 否 | 精确 = | 科室 |
| positionCategory | String | 否 | 精确 = | 岗位类别：临床医师/护理/药学/医技/公共卫生/行政后勤/科研 |
| province | String | 否 | 精确 = | 省份 |
| city | String | 否 | 精确 = | 城市 |
| district | String | 否 | 精确 = | 区/县 |
| ageLimit | Integer | 否 | 判断 >= | 年龄上限（查询条件：`age_limit >= ageLimit`） |
| positionStatus | String | 否 | 精确 = | 状态：招聘中/已结束/即将开始 |
| educationRequirement | String | 否 | 精确 = | 学历要求：不限/大专/本科/硕士/博士 |
| degreeRequirement | String | 否 | 精确 = | 学位要求 |
| majorRequirement | String | 否 | 精确 = | 专业要求 |

### 2.1.3 查询逻辑说明

1. **模糊查询**：`keyword` 同时匹配 `institution_name` 或 `position_name`（OR 关系，LIKE %v%）
2. **精确查询**：各精确字段各自 `=` 匹配
3. **判断查询**：`ageLimit` 使用 `>=` 条件
4. **所有条件之间为 AND 关系**
5. **软删除过滤**：`is_deleted = false`

### 2.1.4 返回字段（HealthcarePositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| institutionName | String | 医疗机构名称 |
| institutionLevel | String | 机构等级 |
| positionName | String | 岗位名称 |
| department | String | 科室 |
| positionCategory | String | 岗位类别 |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招聘人数 |
| salaryRange | String | 薪资待遇 |
| workExperience | String | 工作经验 |
| positionStatus | String | 岗位状态 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |

### 2.1.5 请求示例

```http
GET /api/v1/app/employment/healthcare/list?page=1&size=10&keyword=护理&institutionLevel=三级甲等&province=广东&positionStatus=招聘中 HTTP/1.1
Host: api.haifeng.com
```

### 2.1.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 200001,
        "institutionName": "广东省人民医院",
        "institutionLevel": "三级甲等",
        "positionName": "临床护理",
        "department": "护理部",
        "positionCategory": "护理",
        "province": "广东",
        "city": "广州",
        "district": "越秀区",
        "ageLimit": 35,
        "recruitmentCount": 10,
        "salaryRange": "8k-15k",
        "workExperience": "不限",
        "positionStatus": "招聘中",
        "educationRequirement": "本科",
        "degreeRequirement": "学士",
        "majorRequirement": "护理学"
      }
    ],
    "total": 28,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 2.2 医疗卫生详情

需登录后访问，根据列表返回的 ID 获取岗位完整信息。

### 2.2.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/healthcare/{id}/detail` |
| 权限 | @RequireLogin |

### 2.2.2 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 医疗卫生岗位 ID（t_healthcare_position 主键） |

### 2.2.3 返回字段（HealthcarePositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| institutionName | String | 医疗机构名称 |
| institutionType | String | 机构类型 |
| institutionLevel | String | 机构等级 |
| institutionNature | String | 机构性质（公立/民营） |
| positionName | String | 岗位名称 |
| department | String | 科室 |
| positionCategory | String | 岗位类别 |
| recruitmentType | String | 招聘类型（编制/合同制/人事代理/规培/进修） |
| province | String | 省份 |
| city | String | 城市 |
| district | String | 区/县 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |
| ageLimit | Integer | 年龄上限 |
| recruitmentCount | Integer | 招聘人数 |
| workExperience | String | 工作经验 |
| licenseRequirement | String | 执业资格证要求 |
| titleRequirement | String | 职称要求（不限/初级/中级/副高级/正高级） |
| internshipRequirement | String | 规培要求 |
| researchRequirement | String | 科研要求 |
| salaryRange | String | 薪资待遇 |
| benefits | String | 福利待遇 |
| housingSubsidy | String | 住房补贴/安家费 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| examTime | OffsetDateTime | 考试时间 |
| examContent | String | 考试内容 |
| applyLink | String | 报名链接 |
| positionStatus | String | 岗位状态 |
| contactPhone | String | 联系电话 |
| contactPerson | String | 联系人 |
| remark | String | 备注 |
| content | String | 详细说明（支持HTML） |

### 2.2.4 请求示例

```http
GET /api/v1/app/employment/healthcare/200001/detail HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 2.2.5 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 200001,
    "institutionName": "广东省人民医院",
    "institutionType": "综合医院",
    "institutionLevel": "三级甲等",
    "institutionNature": "公立",
    "positionName": "临床护理",
    "department": "护理部",
    "positionCategory": "护理",
    "recruitmentType": "编制",
    "province": "广东",
    "city": "广州",
    "district": "越秀区",
    "educationRequirement": "本科",
    "degreeRequirement": "学士",
    "majorRequirement": "护理学",
    "ageLimit": 35,
    "recruitmentCount": 10,
    "workExperience": "不限",
    "licenseRequirement": "护士执业资格证",
    "titleRequirement": "不限",
    "internshipRequirement": "完成规培",
    "researchRequirement": null,
    "salaryRange": "8k-15k",
    "benefits": "五险一金、带薪年假、房补",
    "housingSubsidy": "住房补贴1000元/月",
    "regStartDate": "2026-07-01T00:00:00+08:00",
    "regEndDate": "2026-07-20T23:59:59+08:00",
    "examTime": "2026-08-05T09:00:00+08:00",
    "examContent": "医学基础知识+护理专业知识",
    "applyLink": "https://example.com/apply/456",
    "positionStatus": "招聘中",
    "contactPhone": "020-87654321",
    "contactPerson": "李老师",
    "remark": "需持有护士执业资格证",
    "content": "<p>广东省人民医院2026年公开招聘护理人员公告...</p>"
  },
  "timestamp": 1715580000000
}
```

### 2.2.6 错误码

| code | msg |
|------|-----|
| 404 | 岗位不存在 |
| 401 | 未登录或Token已过期 |

---

## 2.3 医疗卫生备考指南

公开接口，无需登录，根据 `guide_category=healthcare` 预设条件查询备考指南。

### 2.3.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/healthcare/exam-guide/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 2.3.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选值：10/20/30/50/100 |

### 2.3.3 查询逻辑说明

1. **预设过滤**：`guide_category = 'healthcare'`
2. **软删除过滤**：`is_deleted = false`

### 2.3.4 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| guideCategory | String | 指南分类（healthcare） |
| guideType | String | 指南类型 |
| title | String | 标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图 |
| iconClass | String | 图标样式 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| tags | String | 标签 |
| difficultyLevel | String | 难度等级 |
| targetAudience | String | 目标受众 |
| authorName | String | 作者姓名 |
| authorTitle | String | 作者职称 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否推荐 |
| sortOrder | Integer | 排序号 |
| viewCount | Integer | 浏览量 |
| likeCount | Integer | 点赞量 |

### 2.3.5 请求示例

```http
GET /api/v1/app/employment/healthcare/exam-guide/list?page=1&size=10 HTTP/1.1
Host: api.haifeng.com
```

### 2.3.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 410001,
        "guideCategory": "healthcare",
        "guideType": "笔试指南",
        "title": "2026年医疗卫生招聘备考攻略",
        "subtitle": "医学基础知识+专业知识全面解析",
        "coverImage": "https://example.com/cover/healthcare-guide.png",
        "iconClass": "icon-healthcare",
        "summary": "涵盖医学基础知识和各专业知识的备考策略",
        "content": "<p>2026年医疗卫生招聘备考攻略...</p>",
        "tags": "医疗卫生,备考,医学基础",
        "difficultyLevel": "中级",
        "targetAudience": "医学类应届毕业生",
        "authorName": "张教授",
        "authorTitle": "主任医师",
        "isTop": true,
        "isRecommended": true,
        "sortOrder": 1,
        "viewCount": 9880,
        "likeCount": 652
      }
    ],
    "total": 4,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 2.4 医疗卫生公告

公开接口，无需登录，根据 `notice_category=healthcare` 预设条件查询公告。

### 2.4.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/healthcare/notice/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 2.4.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选值：10/20/30/50/100 |

### 2.4.3 查询逻辑说明

1. **预设过滤**：`notice_category = 'healthcare'`
2. **软删除过滤**：`is_deleted = false`

### 2.4.4 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| noticeCategory | String | 公告分类（healthcare） |
| noticeType | String | 公告类型 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| province | String | 省份 |
| city | String | 城市 |
| tags | String | 标签 |
| year | Integer | 年份 |
| source | String | 来源 |
| sourceUrl | String | 来源链接 |
| publishDate | OffsetDateTime | 发布日期 |
| publishUnit | String | 发布单位 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| examTime | OffsetDateTime | 考试时间 |
| recruitmentCount | Integer | 招聘人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| viewCount | Integer | 浏览量 |

### 2.4.5 请求示例

```http
GET /api/v1/app/employment/healthcare/notice/list?page=1&size=10 HTTP/1.1
Host: api.haifeng.com
```

### 2.4.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 510001,
        "noticeCategory": "healthcare",
        "noticeType": "招聘公告",
        "title": "广东省卫生健康委2026年公开招聘公告",
        "summary": "广东省卫生健康委直属单位公开招聘医护人才2000名",
        "content": "<p>根据《广东省事业单位公开招聘人员办法》...</p>",
        "province": "广东",
        "city": "广州",
        "tags": "医疗卫生,编制,广东",
        "year": 2026,
        "source": "广东省卫生健康委",
        "sourceUrl": "https://example.com/notice/002",
        "publishDate": "2026-06-15T10:00:00+08:00",
        "publishUnit": "广东省卫生健康委",
        "regStartDate": "2026-07-01T00:00:00+08:00",
        "regEndDate": "2026-07-25T23:59:59+08:00",
        "examTime": "2026-08-15T09:00:00+08:00",
        "recruitmentCount": 2000,
        "isTop": true,
        "isImportant": true,
        "viewCount": 32150
      }
    ],
    "total": 6,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

# 三、银行/金融（FinancePosition）

## 3.1 银行/金融列表

公开接口，无需登录，支持模糊搜索 + 多维度精确筛选。

### 3.1.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/finance/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 3.1.2 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认 1 |
| size | Integer | 否 | - | 每页条数，默认 10，可选值：10/20/30/50/100 |
| keyword | String | 否 | 模糊 LIKE | 同时匹配 `institution_name` 和 `position_name` |
| institutionCategory | String | 否 | 精确 = | 机构大类：银行/证券/保险/基金/信托/期货/监管机构/金融科技 |
| institutionType | String | 否 | 精确 = | 机构细分类型 |
| branchName | String | 否 | 精确 = | 分支机构名称 |
| positionCategory | String | 否 | 精确 = | 岗位类别 |
| recruitmentType | String | 否 | 精确 = | 招聘类型：秋招/春招/社招/实习/定向 |
| province | String | 否 | 精确 = | 省份 |
| city | String | 否 | 精确 = | 城市 |
| ageLimit | Integer | 否 | 判断 >= | 年龄上限（查询条件：`age_limit >= ageLimit`） |
| salaryMin | Integer | 否 | 判断 >= | 最低月薪（查询条件：`salary_min >= salaryMin`） |
| positionStatus | String | 否 | 精确 = | 状态：招聘中/已结束/即将开始 |
| educationRequirement | String | 否 | 精确 = | 学历要求：不限/大专/本科/硕士/博士 |
| degreeRequirement | String | 否 | 精确 = | 学位要求 |
| majorRequirement | String | 否 | 精确 = | 专业要求 |

### 3.1.3 查询逻辑说明

1. **模糊查询**：`keyword` 同时匹配 `institution_name` 或 `position_name`（OR 关系，LIKE %v%）
2. **精确查询**：各精确字段各自 `=` 匹配
3. **判断查询**：`ageLimit` 使用 `>=` 条件，`salaryMin` 使用 `>=` 条件
4. **所有条件之间为 AND 关系**
5. **软删除过滤**：`is_deleted = false`

### 3.1.4 返回字段（FinancePositionListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| institutionName | String | 金融机构名称 |
| institutionCategory | String | 机构大类 |
| positionName | String | 岗位名称 |
| positionCategory | String | 岗位类别 |
| recruitmentType | String | 招聘类型 |
| province | String | 省份 |
| city | String | 城市 |
| ageLimit | Integer | 年龄上限 |
| workExperience | String | 工作经验要求 |
| salaryMin | Integer | 最低月薪（单位：k） |
| salaryMax | Integer | 最高月薪（单位：k） |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| isRemote | Boolean | 是否支持远程 |
| workLocation | String | 详细工作地点 |
| recruitmentCount | Integer | 招聘人数 |
| positionStatus | String | 岗位状态 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |

### 3.1.5 请求示例

```http
GET /api/v1/app/employment/finance/list?page=1&size=10&keyword=Java&institutionCategory=银行&recruitmentType=秋招&province=广东&positionStatus=招聘中 HTTP/1.1
Host: api.haifeng.com
```

### 3.1.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 300001,
        "institutionName": "中国工商银行",
        "institutionCategory": "银行",
        "positionName": "科技菁英（Java开发方向）",
        "positionCategory": "金融科技",
        "recruitmentType": "秋招",
        "province": "广东",
        "city": "广州",
        "ageLimit": 30,
        "workExperience": "不限",
        "salaryMin": 15,
        "salaryMax": 25,
        "regStartDate": "2026-09-01T00:00:00+08:00",
        "regEndDate": "2026-10-10T23:59:59+08:00",
        "isRemote": false,
        "workLocation": "广州市天河区",
        "recruitmentCount": 5,
        "positionStatus": "招聘中",
        "educationRequirement": "本科",
        "degreeRequirement": "学士",
        "majorRequirement": "计算机科学与技术、软件工程"
      }
    ],
    "total": 35,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 3.2 银行/金融详情

需登录后访问，根据列表返回的 ID 获取岗位完整信息。

### 3.2.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/finance/{id}/detail` |
| 权限 | @RequireLogin |

### 3.2.2 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 金融岗位 ID（t_finance_position 主键） |

### 3.2.3 返回字段（FinancePositionDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| institutionName | String | 金融机构名称 |
| institutionCategory | String | 机构大类（银行/证券/保险/基金/信托/期货/监管机构/金融科技） |
| institutionType | String | 机构细分类型 |
| institutionLogo | String | 机构 Logo |
| branchName | String | 分支机构名称 |
| positionName | String | 岗位名称 |
| positionCategory | String | 岗位类别 |
| recruitmentType | String | 招聘类型（秋招/春招/社招/实习/定向） |
| province | String | 省份 |
| city | String | 城市 |
| workLocation | String | 详细工作地点 |
| isRemote | Boolean | 是否支持远程 |
| educationRequirement | String | 学历要求 |
| degreeRequirement | String | 学位要求 |
| majorRequirement | String | 专业要求 |
| majorPreference | List\<String\> | 优先专业列表（JSONB） |
| ageLimit | Integer | 年龄上限 |
| workExperience | String | 工作经验要求 |
| recruitmentCount | Integer | 招聘人数 |
| certRequirements | List\<String\> | 证书要求列表（JSONB） |
| languageRequirement | String | 语言要求 |
| computerRequirement | String | 计算机要求 |
| otherRequirement | String | 其他要求 |
| salaryMin | Integer | 最低月薪（单位：k） |
| salaryMax | Integer | 最高月薪（单位：k） |
| salaryText | String | 薪资文本说明 |
| benefits | String | 福利待遇 |
| examContent | String | 考试内容 |
| examTime | OffsetDateTime | 考试时间 |
| interviewRounds | String | 面试轮次说明 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| applyLink | String | 网申/报名链接 |
| positionStatus | String | 岗位状态 |
| contactInfo | String | 联系方式 |
| remark | String | 备注 |
| content | String | 详细说明（支持HTML） |

### 3.2.4 请求示例

```http
GET /api/v1/app/employment/finance/300001/detail HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 3.2.5 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 300001,
    "institutionName": "中国工商银行",
    "institutionCategory": "银行",
    "institutionType": "国有商业银行",
    "institutionLogo": "https://example.com/logo/icbc.png",
    "branchName": "广东省分行",
    "positionName": "科技菁英（Java开发方向）",
    "positionCategory": "金融科技",
    "recruitmentType": "秋招",
    "province": "广东",
    "city": "广州",
    "workLocation": "广州市天河区",
    "isRemote": false,
    "educationRequirement": "本科",
    "degreeRequirement": "学士",
    "majorRequirement": "计算机科学与技术、软件工程",
    "majorPreference": ["人工智能", "大数据"],
    "ageLimit": 30,
    "workExperience": "不限",
    "recruitmentCount": 5,
    "certRequirements": ["CET-6", "计算机二级"],
    "languageRequirement": "大学英语六级（CET-6）",
    "computerRequirement": "熟悉Java、Spring Boot",
    "otherRequirement": "有金融科技项目经验者优先",
    "salaryMin": 15,
    "salaryMax": 25,
    "salaryText": "15k-25k·15薪",
    "benefits": "五险一金、补充医疗保险、企业年金、带薪年假",
    "examContent": "EPI+综合知识+英语+性格测试",
    "examTime": "2026-10-25T14:00:00+08:00",
    "interviewRounds": "初面-复面-终面",
    "regStartDate": "2026-09-01T00:00:00+08:00",
    "regEndDate": "2026-10-10T23:59:59+08:00",
    "applyLink": "https://job.icbc.com.cn/apply/789",
    "positionStatus": "招聘中",
    "contactInfo": "020-88886666",
    "remark": "需同时通过网申和笔试",
    "content": "<p>中国工商银行2027年度校园招聘公告...</p>"
  },
  "timestamp": 1715580000000
}
```

### 3.2.6 错误码

| code | msg |
|------|-----|
| 404 | 岗位不存在 |
| 401 | 未登录或Token已过期 |

---

## 3.3 银行/金融备考指南

公开接口，无需登录，根据 `guide_category=finance` 预设条件查询备考指南。

### 3.3.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/finance/exam-guide/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 3.3.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选值：10/20/30/50/100 |

### 3.3.3 查询逻辑说明

1. **预设过滤**：`guide_category = 'finance'`
2. **软删除过滤**：`is_deleted = false`

### 3.3.4 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| guideCategory | String | 指南分类（finance） |
| guideType | String | 指南类型 |
| title | String | 标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图 |
| iconClass | String | 图标样式 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| tags | String | 标签 |
| difficultyLevel | String | 难度等级 |
| targetAudience | String | 目标受众 |
| authorName | String | 作者姓名 |
| authorTitle | String | 作者职称 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否推荐 |
| sortOrder | Integer | 排序号 |
| viewCount | Integer | 浏览量 |
| likeCount | Integer | 点赞量 |

### 3.3.5 请求示例

```http
GET /api/v1/app/employment/finance/exam-guide/list?page=1&size=10 HTTP/1.1
Host: api.haifeng.com
```

### 3.3.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 420001,
        "guideCategory": "finance",
        "guideType": "笔试指南",
        "title": "2026年银行系统招聘备考攻略",
        "subtitle": "EPI+综合知识+英语全面解析",
        "coverImage": "https://example.com/cover/finance-guide.png",
        "iconClass": "icon-finance",
        "summary": "涵盖EPI、综合知识、英语和性格测试的备考策略",
        "content": "<p>2026年银行系统招聘备考攻略...</p>",
        "tags": "银行招聘,备考,EPI",
        "difficultyLevel": "中级",
        "targetAudience": "经管类应届毕业生",
        "authorName": "李老师",
        "authorTitle": "金融培训专家",
        "isTop": true,
        "isRecommended": true,
        "sortOrder": 1,
        "viewCount": 15680,
        "likeCount": 1023
      }
    ],
    "total": 6,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 3.4 银行/金融公告

公开接口，无需登录，根据 `notice_category=finance` 预设条件查询公告。

### 3.4.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/finance/notice/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |

### 3.4.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10，可选值：10/20/30/50/100 |

### 3.4.3 查询逻辑说明

1. **预设过滤**：`notice_category = 'finance'`
2. **软删除过滤**：`is_deleted = false`

### 3.4.4 返回字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| noticeCategory | String | 公告分类（finance） |
| noticeType | String | 公告类型 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| province | String | 省份 |
| city | String | 城市 |
| tags | String | 标签 |
| year | Integer | 年份 |
| source | String | 来源 |
| sourceUrl | String | 来源链接 |
| publishDate | OffsetDateTime | 发布日期 |
| publishUnit | String | 发布单位 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| examTime | OffsetDateTime | 考试时间 |
| recruitmentCount | Integer | 招聘人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| viewCount | Integer | 浏览量 |

### 3.4.5 请求示例

```http
GET /api/v1/app/employment/finance/notice/list?page=1&size=10 HTTP/1.1
Host: api.haifeng.com
```

### 3.4.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 520001,
        "noticeCategory": "finance",
        "noticeType": "招聘公告",
        "title": "中国工商银行2027年度校园招聘公告",
        "summary": "中国工商银行2027年度校园招聘正式启动",
        "content": "<p>中国工商银行2027年度校园招聘公告...</p>",
        "province": "广东",
        "city": "广州",
        "tags": "银行招聘,校招,工商银行",
        "year": 2026,
        "source": "中国工商银行",
        "sourceUrl": "https://job.icbc.com.cn/notice/003",
        "publishDate": "2026-08-20T10:00:00+08:00",
        "publishUnit": "中国工商银行总行",
        "regStartDate": "2026-09-01T00:00:00+08:00",
        "regEndDate": "2026-10-10T23:59:59+08:00",
        "examTime": "2026-10-25T14:00:00+08:00",
        "recruitmentCount": 20000,
        "isTop": true,
        "isImportant": true,
        "viewCount": 58600
      }
    ],
    "total": 8,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

# 四、通用错误码

| code | msg | 说明 |
|------|-----|------|
| 200 | success | 成功 |
| 400 | * | 参数错误/业务校验失败 |
| 401 | 未登录或Token已过期 | 需重新登录 |
| 404 | 岗位不存在 | 资源不存在 |
| 500 | 服务器内部错误 | 系统异常 |
