# 招聘内容管理 API 文档

## 概述

招聘内容管理模块提供统一备考指南和统一公告的CRUD功能。

### 模块分类
| 子模块 | 说明 |
|--------|------|
| 统一备考指南(ExamGuide) | 全平台备考文章/经验/技巧 |
| 统一公告(Notice) | 全平台招考/招聘公告 |

### Base URL
```
/api/v1/admin/employment/content-management
```

### 统一响应格式
```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1234567890
}
```

### 分页请求参数(BasePageQueryDTO)
| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| page | Integer | 否 | 1 | 页码，最小1 |
| size | Integer | 否 | 10 | 每页条数，10-1000 |

---

## 一、统一备考指南 (ExamGuide)

### 1.1 分页查询列表

**GET** `/exam-guide/list`

#### 请求参数(Query)
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |
| title | String | 否 | 标题(模糊) |
| subtitle | String | 否 | 副标题(模糊) |
| guideCategory | String | 否 | 指南类别(精确) |
| guideType | String | 否 | 指南类型(精确) |
| isTop | Boolean | 否 | 是否置顶(精确) |

#### 返回 data
```json
{
  "records": [
    {
      "id": 123456789,
      "guideCategory": "civil",
      "guideType": "备考攻略",
      "title": "标题",
      "subtitle": "副标题",
      "isTop": false,
      "isRecommended": true,
      "viewCount": 100,
      "likeCount": 20,
      "sortOrder": 0
    }
  ],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10
}
```

#### 排序规则
`sort_order DESC, created_at DESC`

---

### 1.2 查看详情

**GET** `/exam-guide/{id}/detail`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 指南ID |

#### 返回 data
```json
{
  "id": 123456789,
  "guideCategory": "civil",
  "guideType": "备考攻略",
  "title": "标题",
  "subtitle": "副标题",
  "coverImage": "https://...",
  "iconClass": "fa-book",
  "summary": "摘要",
  "content": "详细内容(HTML)",
  "tags": ["标签1", "标签2"],
  "difficultyLevel": "入门",
  "targetAudience": "目标读者",
  "authorName": "作者名",
  "authorTitle": "作者头衔",
  "isTop": false,
  "isRecommended": true,
  "sortOrder": 0,
  "viewCount": 100,
  "likeCount": 20,
  "isDeleted": false,
  "createdAt": "2026-01-01T00:00:00+08:00",
  "updatedAt": "2026-01-01T00:00:00+08:00"
}
```

---

### 1.3 修改

**PUT** `/exam-guide/{id}/update`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 指南ID |

#### 请求体
```json
{
  "guideCategory": "civil",
  "guideType": "备考攻略",
  "title": "标题",
  "subtitle": "副标题",
  "coverImage": "https://...",
  "iconClass": "fa-book",
  "summary": "摘要",
  "content": "详细内容(HTML)",
  "tags": ["标签1", "标签2"],
  "difficultyLevel": "入门",
  "targetAudience": "目标读者",
  "authorName": "作者名",
  "authorTitle": "作者头衔",
  "isTop": false,
  "isRecommended": true,
  "sortOrder": 0
}
```

---

### 1.4 删除(物理)

**DELETE** `/exam-guide/{id}/delete`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 指南ID |

---

### 1.5 启用/禁用

**PATCH** `/exam-guide/{id}/status`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 指南ID |

#### 请求体
```json
{
  "status": 1
}
```
status: 0=禁用(isDeleted=true), 1=启用(isDeleted=false)

---

### 1.6 批量删除

**POST** `/exam-guide/batch-delete`

#### 请求体
```json
[1001, 1002, 1003]
```

#### 校验规则
- `@NotEmpty`：列表不能为空
- `@Size(max = 100)`：单次最多删除 100 条

---

## 二、统一公告 (Notice)

### 2.1 分页查询列表

**GET** `/notice/list`

#### 请求参数(Query)
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |
| title | String | 否 | 标题(模糊) |
| noticeCategory | String | 否 | 公告类别(精确) |
| noticeType | String | 否 | 公告类型(精确) |
| province | String | 否 | 省份(精确) |
| city | String | 否 | 城市(精确) |
| year | String | 否 | 年份(精确) |
| isTop | Boolean | 否 | 是否置顶(精确) |
| isImportant | Boolean | 否 | 是否重要(精确) |

#### 返回 data
```json
{
  "records": [
    {
      "id": 123456789,
      "title": "公告标题",
      "noticeCategory": "civil",
      "noticeType": "招聘公告",
      "province": "广东省",
      "city": "广州市",
      "year": "2026",
      "isTop": false,
      "isImportant": true,
      "viewCount": 500,
      "sortOrder": 0
    }
  ],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10
}
```

#### 排序规则
`sort_order DESC, created_at DESC`

---

### 2.2 查看详情

**GET** `/notice/{id}/detail`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

#### 返回 data
```json
{
  "id": 123456789,
  "noticeCategory": "civil",
  "noticeType": "招聘公告",
  "title": "公告标题",
  "summary": "摘要",
  "content": "公告内容(HTML)",
  "province": "广东省",
  "city": "广州市",
  "tags": ["标签1"],
  "year": "2026",
  "source": "来源",
  "sourceUrl": "https://...",
  "publishDate": "2026-01-01T00:00:00+08:00",
  "publishUnit": "发布单位",
  "regStartDate": "2026-01-10T00:00:00+08:00",
  "regEndDate": "2026-01-20T00:00:00+08:00",
  "examTime": "2026-02-01T00:00:00+08:00",
  "recruitmentCount": 100,
  "isTop": false,
  "isImportant": true,
  "sortOrder": 0,
  "viewCount": 500,
  "isDeleted": false,
  "createdAt": "2026-01-01T00:00:00+08:00",
  "updatedAt": "2026-01-01T00:00:00+08:00"
}
```

---

### 2.3 修改

**PUT** `/notice/{id}/update`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

#### 请求体
```json
{
  "noticeCategory": "civil",
  "noticeType": "招聘公告",
  "title": "公告标题",
  "summary": "摘要",
  "content": "公告内容(HTML)",
  "province": "广东省",
  "city": "广州市",
  "tags": ["标签1"],
  "year": "2026",
  "source": "来源",
  "sourceUrl": "https://...",
  "publishDate": "2026-01-01T00:00:00+08:00",
  "publishUnit": "发布单位",
  "regStartDate": "2026-01-10T00:00:00+08:00",
  "regEndDate": "2026-01-20T00:00:00+08:00",
  "examTime": "2026-02-01T00:00:00+08:00",
  "recruitmentCount": 100,
  "isTop": false,
  "isImportant": true,
  "sortOrder": 0
}
```

---

### 2.4 删除(物理)

**DELETE** `/notice/{id}/delete`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

---

### 2.5 启用/禁用

**PATCH** `/notice/{id}/status`

#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

#### 请求体
```json
{
  "status": 1
}
```
status: 0=禁用(isDeleted=true), 1=启用(isDeleted=false)

---

### 2.6 批量删除

**POST** `/notice/batch-delete`

#### 请求体
```json
[1001, 1002, 1003]
```

#### 校验规则
- `@NotEmpty`：列表不能为空
- `@Size(max = 100)`：单次最多删除 100 条

---

## 三、错误码

| Code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 四、OperationLog注解说明

以下接口记录操作日志：
- 修改 (module="招聘内容管理", action="修改备考指南"/"修改公告")
- 删除 (module="招聘内容管理", action="删除备考指南"/"删除公告")
- 启用/禁用 (module="招聘内容管理", action="启用/禁用备考指南"/"启用/禁用公告")
- 批量删除 (module="招聘内容管理", action="批量删除备考指南"/"批量删除公告")
