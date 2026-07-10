# 招聘内容管理模块 C端 API 文档

## 功能概述

招聘内容管理模块提供两个统一内容实体，供所有就业子模块（教师招聘、医疗卫生、银行/金融、基层服务、公考等）调用：

| 实体 | 说明 | 表名 |
|------|------|------|
| 统一备考指南 | 全平台备考文章/经验/技巧 | t_exam_guide |
| 统一公告 | 全平台招考/招聘公告 | t_notice |

其他子模块通过 `guide_category` / `notice_category` 预设过滤，跳转至本模块获取数据。

## 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，任何人可访问 |

---

# 一、统一备考指南（ExamGuide）

## 1.1 按类型列表查询

公开接口，无需登录。按 `guide_category` 和 `guide_type` 查询备考指南列表，全量返回。

### 1.1.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/content/exam-guide/list-by-type` |
| 权限 | 公开 |
| 分页 | 否，全量返回 |

### 1.1.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| guideCategory | String | 是 | 指南分类（由各子模块传入，如 teacher / healthcare / finance / grassroots / community / public_welfare / civil / institution / military / selection） |
| guideType | String | 否 | 指南类型，默认 `备考攻略`，可选值见「备考指南类型枚举」 |

### 1.1.3 查询逻辑说明

1. **预设过滤**：`guide_category = ?`（按传入 guideCategory）
2. **精确查询**：`guide_type = ?`（按传入 guideType，默认 `'备考攻略'`）
3. **软删除过滤**：`is_deleted = false`
4. **排序规则**：`sort_order DESC NULLS LAST, created_at DESC`
5. **不分页**：全量返回匹配记录

### 1.1.4 返回字段（ExamGuideDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| guideCategory | String | 指南分类 |
| guideType | String | 指南类型 |
| title | String | 标题 |
| subtitle | String | 副标题 |
| coverImage | String | 封面图 |
| iconClass | String | 图标样式 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| tags | String[] | 标签列表（TEXT[]） |
| difficultyLevel | String | 难度等级 |
| targetAudience | String | 目标受众 |
| authorName | String | 作者姓名 |
| authorTitle | String | 作者头衔 |
| isTop | Boolean | 是否置顶 |
| isRecommended | Boolean | 是否推荐 |
| sortOrder | Integer | 排序序号 |
| createdAt | OffsetDateTime | 创建时间 |
| updatedAt | OffsetDateTime | 更新时间 |

### 1.1.5 请求示例

```http
GET /api/v1/app/employment/content/exam-guide/list-by-type?guideCategory=teacher&guideType=备考攻略 HTTP/1.1
Host: api.haifeng.com
```

### 1.1.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 400001,
      "guideCategory": "teacher",
      "guideType": "备考攻略",
      "title": "2026年教师招聘备考攻略",
      "subtitle": "教综+学科专业知识全面解析",
      "coverImage": "https://example.com/cover/teacher-guide.png",
      "iconClass": "icon-teacher",
      "summary": "涵盖教育综合知识和学科专业知识的备考策略",
      "content": "<p>2026年教师招聘备考攻略...</p>",
      "tags": ["教师招聘", "备考", "教综"],
      "difficultyLevel": "中级",
      "targetAudience": "应届毕业生",
      "authorName": "王老师",
      "authorTitle": "高级教师",
      "isTop": true,
      "isRecommended": true,
      "sortOrder": 1,
      "createdAt": "2026-06-01T10:00:00+08:00",
      "updatedAt": "2026-06-01T10:00:00+08:00"
    }
  ],
  "timestamp": 1715580000000
}
```

---

## 1.2 详情查询

公开接口，无需登录。获取单篇备考指南的完整内容，并增加浏览量计数。

### 1.2.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/content/exam-guide/{id}/detail` |
| 权限 | 公开 |

### 1.2.2 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 备考指南 ID |

### 1.2.3 返回字段（ExamGuideDetailVO）

同 1.1.4 返回字段。

### 1.2.4 请求示例

```http
GET /api/v1/app/employment/content/exam-guide/400001/detail HTTP/1.1
Host: api.haifeng.com
```

### 1.2.5 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 400001,
    "guideCategory": "teacher",
    "guideType": "备考攻略",
    "title": "2026年教师招聘备考攻略",
    "subtitle": "教综+学科专业知识全面解析",
    "coverImage": "https://example.com/cover/teacher-guide.png",
    "iconClass": "icon-teacher",
    "summary": "涵盖教育综合知识和学科专业知识的备考策略",
    "content": "<p>2026年教师招聘备考攻略...</p>",
    "tags": ["教师招聘", "备考", "教综"],
    "difficultyLevel": "中级",
    "targetAudience": "应届毕业生",
    "authorName": "王老师",
    "authorTitle": "高级教师",
    "isTop": true,
    "isRecommended": true,
    "sortOrder": 1,
    "createdAt": "2026-06-01T10:00:00+08:00",
    "updatedAt": "2026-06-01T10:00:00+08:00"
  },
  "timestamp": 1715580000000
}
```

### 1.2.6 错误码

| code | msg | 说明 |
|------|-----|------|
| 404 | 指南不存在 | 备考指南不存在 |

---

# 二、统一公告（Notice）

## 2.1 按类型列表查询

公开接口，无需登录。按 `notice_category` 和 `notice_type` 查询公告列表，全量返回。

### 2.1.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/content/notice/list-by-type` |
| 权限 | 公开 |
| 分页 | 否，全量返回 |

### 2.1.2 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| noticeCategory | String | 是 | 公告分类（由各子模块传入，如 teacher / healthcare / finance / grassroots / community / public_welfare / civil / institution / military / selection） |
| noticeType | String | 否 | 公告类型，默认 `招聘公告`，可选值见「公告类型枚举」 |

### 2.1.3 查询逻辑说明

1. **预设过滤**：`notice_category = ?`（按传入 noticeCategory）
2. **精确查询**：`notice_type = ?`（按传入 noticeType，默认 `'招聘公告'`）
3. **软删除过滤**：`is_deleted = false`
4. **排序规则**：`sort_order DESC NULLS LAST, created_at DESC`
5. **不分页**：全量返回匹配记录

### 2.1.4 返回字段（NoticeDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| noticeCategory | String | 公告分类 |
| noticeType | String | 公告类型 |
| title | String | 标题 |
| summary | String | 摘要 |
| content | String | 内容（支持HTML） |
| province | String | 省份 |
| city | String | 城市 |
| tags | String[] | 标签列表（TEXT[]） |
| year | Integer | 年份 |
| source | String | 来源 |
| sourceUrl | String | 来源链接 |
| publishDate | OffsetDateTime | 发布日期 |
| publishUnit | String | 发布单位 |
| regStartDate | OffsetDateTime | 报名开始日期 |
| regEndDate | OffsetDateTime | 报名截止日期 |
| examTime | OffsetDateTime | 考试时间 |
| recruitmentCount | Integer | 招录人数 |
| isTop | Boolean | 是否置顶 |
| isImportant | Boolean | 是否重要 |
| sortOrder | Integer | 排序权重 |
| createdAt | OffsetDateTime | 创建时间 |
| updatedAt | OffsetDateTime | 更新时间 |

### 2.1.5 请求示例

```http
GET /api/v1/app/employment/content/notice/list-by-type?noticeCategory=teacher&noticeType=招聘公告 HTTP/1.1
Host: api.haifeng.com
```

### 2.1.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 500001,
      "noticeCategory": "teacher",
      "noticeType": "招聘公告",
      "title": "广州市教育局2026年公开招聘教师公告",
      "summary": "广州市教育局直属学校公开招聘教师500名",
      "content": "<p>根据《广东省事业单位公开招聘人员办法》...</p>",
      "province": "广东",
      "city": "广州",
      "tags": ["教师招聘", "编制", "广州"],
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
      "sortOrder": 100,
      "createdAt": "2026-06-01T10:00:00+08:00",
      "updatedAt": "2026-06-01T10:00:00+08:00"
    }
  ],
  "timestamp": 1715580000000
}
```

---

## 2.2 详情查询

公开接口，无需登录。获取单条公告的完整内容，并增加浏览量计数。

### 2.2.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/content/notice/{id}/detail` |
| 权限 | 公开 |

### 2.2.2 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告 ID |

### 2.2.3 返回字段（NoticeDetailVO）

同 2.1.4 返回字段。

### 2.2.4 请求示例

```http
GET /api/v1/app/employment/content/notice/500001/detail HTTP/1.1
Host: api.haifeng.com
```

### 2.2.5 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 500001,
    "noticeCategory": "teacher",
    "noticeType": "招聘公告",
    "title": "广州市教育局2026年公开招聘教师公告",
    "summary": "广州市教育局直属学校公开招聘教师500名",
    "content": "<p>根据《广东省事业单位公开招聘人员办法》...</p>",
    "province": "广东",
    "city": "广州",
    "tags": ["教师招聘", "编制", "广州"],
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
    "sortOrder": 100,
    "createdAt": "2026-06-01T10:00:00+08:00",
    "updatedAt": "2026-06-01T10:00:00+08:00"
  },
  "timestamp": 1715580000000
}
```

### 2.2.6 错误码

| code | msg | 说明 |
|------|-----|------|
| 404 | 公告不存在 | 公告不存在 |

---

# 三、备考指南类型枚举

#### guideType（指南类型）

| 值 | 说明 |
|----|------|
| 备考攻略 | 备考攻略 |
| 科目指导 | 科目指导 |
| 真题解析 | 真题解析 |
| 面试技巧 | 面试技巧 |
| 时事热点 | 时事热点 |
| 经验分享 | 经验分享 |
| 政策解读 | 政策解读 |
| 学习计划 | 学习计划 |

---

# 四、公告类型枚举

#### noticeType（公告类型）

| 值 | 说明 |
|----|------|
| 招聘公告 | 招聘公告 |
| 招录公告 | 招录公告 |
| 补录公告 | 补录公告 |
| 调剂公告 | 调剂公告 |
| 成绩公示 | 成绩公示 |
| 面试通知 | 面试通知 |
| 体检通知 | 体检通知 |
| 录用公示 | 录用公示 |
| 报名指南 | 报名指南 |
| 考试大纲 | 考试大纲 |
| 政策解读 | 政策解读 |

---

# 五、通用错误码

| code | msg | 说明 |
|------|-----|------|
| 200 | success | 成功 |
| 400 | * | 参数错误/业务校验失败 |
| 401 | 未登录或Token已过期 | 需重新登录 |
| 404 | 资源不存在 | 资源不存在 |
| 500 | 服务器内部错误 | 系统异常 |

---

# 六、接口路径汇总

| 方法 | URL | 需登录 | 说明 |
|------|-----|--------|------|
| GET | `/api/v1/app/employment/content/exam-guide/list-by-type` | ❌ | 备考指南列表 |
| GET | `/api/v1/app/employment/content/exam-guide/{id}/detail` | ❌ | 备考指南详情 |
| GET | `/api/v1/app/employment/content/notice/list-by-type` | ❌ | 公告列表 |
| GET | `/api/v1/app/employment/content/notice/{id}/detail` | ❌ | 公告详情 |

---

# 七、公共约束

1. **软删除过滤**：所有列表查询均过滤 `is_deleted = false`
2. **排序规则**：`sort_order DESC NULLS LAST, created_at DESC`
3. **内容格式**：`content` 字段支持 HTML 格式
4. **标签格式**：`tags` 字段为 TEXT[] 数组类型，JSON 序列化为字符串数组
5. **时间格式**：所有时间字段使用 ISO-8601 格式，带时区偏移（如 `2026-06-01T10:00:00+08:00`）
