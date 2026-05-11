# 特殊通道模块

## 模块概述

本模块实现特殊招生通道管理功能，属于「特殊通道模块」父模块，包含四个子模块：

| 子模块 | 说明 |
|--------|------|
| 特殊招生通道列表 | 管理各类特殊招生通道（强基计划、综合评价、专项计划、港澳院校等） |
| 通道-大学关联 | 管理通道与大学的关联关系，包含招生简章、报名时间等信息 |
| 强基计划数据 | 管理强基计划各校的入围分数线、录取分数线等数据 |
| 强基院校配置 | 管理39所强基计划试点院校的配置信息 |

## 数据表

### 1. t_special_channel（特殊招生通道内容表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| channel_code | VARCHAR(30) | 是 | 通道代码（唯一） |
| channel_name | VARCHAR(50) | 是 | 通道名称 |
| subtitle | VARCHAR(200) | 否 | 副标题 |
| parent_code | VARCHAR(30) | 否 | 父级通道代码（用于分组） |
| filter_label | VARCHAR(30) | 否 | 筛选按钮文字 |
| display_type | VARCHAR(20) | 是 | 展示类型：UNIVERSITY_LIST/ARTICLE_ONLY/MAJOR_DATA/GROUP |
| content | TEXT | 否 | 富文本内容（HTML） |
| sort_order | INTEGER | 否 | 排序值，默认0 |
| is_active | BOOLEAN | 是 | 是否启用，默认true |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

**display_type枚举值**：
- `UNIVERSITY_LIST` - 展示大学列表（专项/综评/港澳）
- `ARTICLE_ONLY` - 只展示文章（民族班）
- `MAJOR_DATA` - 展示专业级数据（强基计划）
- `GROUP` - 分组父节点

### 2. t_special_channel_university（通道-大学关联表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| channel_code | VARCHAR(30) | 是 | 通道代码 |
| channel_name | VARCHAR(50) | 是 | 通道名称（冗余） |
| university_id | BIGINT | 是 | 大学ID |
| university_name | VARCHAR(50) | 是 | 大学名称（冗余） |
| year | SMALLINT | 否 | 招生年份 |
| region_tag | VARCHAR(20) | 否 | 地区标签（香港/澳门/NULL） |
| signup_start | TIMESTAMPTZ | 否 | 报名开始时间 |
| signup_end | TIMESTAMPTZ | 否 | 报名截止时间 |
| official_url | VARCHAR(500) | 否 | 报名官网URL |
| brochure_title | VARCHAR(200) | 否 | 招生简章标题 |
| brochure_content | TEXT | 否 | 招生简章正文（HTML） |
| sort_order | INTEGER | 否 | 排序值，默认0 |
| is_active | BOOLEAN | 是 | 是否启用，默认true |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

**唯一约束**：channel_code + university_id + year

### 3. t_strong_base_score（强基计划入围/录取数据表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| university_id | BIGINT | 是 | 大学ID |
| university_name | VARCHAR(50) | 是 | 大学名称（冗余） |
| year | SMALLINT | 是 | 年份 |
| province | VARCHAR(20) | 是 | 省份 |
| subject_type | VARCHAR(20) | 是 | 科类（物理类/历史类/理科/文科/综合改革） |
| major_name | VARCHAR(100) | 是 | 专业名称 |
| major_code | VARCHAR(20) | 否 | 专业代码 |
| entry_score | NUMERIC(7,2) | 否 | 入围分数线 |
| entry_score_type | VARCHAR(30) | 否 | 入围分数类型，默认"高考成绩" |
| entry_formula | VARCHAR(500) | 否 | 入围计算公式 |
| entry_ratio | VARCHAR(20) | 否 | 入围比例（如1:3、1:5） |
| admission_score | NUMERIC(7,2) | 否 | 录取综合分 |
| admission_formula | VARCHAR(500) | 否 | 录取公式，默认"高考成绩×85%+校测成绩×15%" |
| plan_count | INTEGER | 否 | 招生计划数 |
| admission_count | INTEGER | 否 | 实际录取人数 |
| remark | VARCHAR(500) | 否 | 备注 |
| is_active | BOOLEAN | 是 | 是否启用，默认true |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

**唯一约束**：university_id + year + province + subject_type + major_name

**entry_score_type枚举值**：
- `高考成绩` - 直接用高考分
- `加权成绩` - 高考重点科目×1.2+其他
- `校测初试` - 高考前校测（复旦等）

### 4. t_strong_base_university（强基计划院校配置表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| university_id | BIGINT | 是 | 大学ID（唯一） |
| university_name | VARCHAR(50) | 是 | 大学名称（冗余） |
| is_pilot | BOOLEAN | 否 | 是否强基试点校，默认true |
| pilot_year | SMALLINT | 否 | 首次试点年份 |
| official_url | VARCHAR(500) | 否 | 强基计划官方页面URL |
| signup_url | VARCHAR(500) | 否 | 报名入口URL |
| test_before_score | BOOLEAN | 否 | 是否高考出分前校测，默认false |
| default_entry_ratio | VARCHAR(20) | 否 | 默认入围比例，默认"1:5" |
| default_admission_formula | VARCHAR(500) | 否 | 默认录取公式 |
| available_majors | TEXT[] | 否 | 可选专业列表（PostgreSQL数组） |
| special_notes | TEXT | 否 | 特殊说明 |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

**注意**：此表**无is_active字段**，不支持禁用功能

---

## 一、特殊招生通道接口

基础路径：`/api/v1/admin/special/channel`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增通道 |
| PUT | `/{id}` | 修改通道 |
| PUT | `/{id}/toggle` | 切换启用状态 |
| DELETE | `/{id}` | 删除通道（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

### 1.1 分页查询列表

**请求**
```
GET /api/v1/admin/special/channel/page?page=1&size=10&displayType=UNIVERSITY_LIST&channelName=专项
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |
| displayType | String | 否 | **精确查询** | 展示类型（UNIVERSITY_LIST/ARTICLE_ONLY/MAJOR_DATA/GROUP） |
| channelName | String | 否 | **模糊查询** | 通道名称 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000001",
        "channelCode": "STRONG_BASE",
        "channelName": "强基计划",
        "displayType": "MAJOR_DATA",
        "isActive": true
      },
      {
        "id": "1893000000000002",
        "channelCode": "COMPREHENSIVE_EVAL",
        "channelName": "综合评价",
        "displayType": "UNIVERSITY_LIST",
        "isActive": true
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID（雪花算法） |
| channelCode | String | 通道代码 |
| channelName | String | 通道名称 |
| displayType | String | 展示类型 |
| isActive | Boolean | 是否启用 |

### 1.2 获取详情

**请求**
```
GET /api/v1/admin/special/channel/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000001",
    "channelCode": "STRONG_BASE",
    "channelName": "强基计划",
    "subtitle": "聚焦基础学科，培养拔尖创新人才",
    "parentCode": null,
    "filterLabel": null,
    "displayType": "MAJOR_DATA",
    "content": "<p>强基计划主要选拔培养有志于服务国家重大战略需求且综合素质优秀或基础学科拔尖的学生。</p>",
    "sortOrder": 1,
    "isActive": true,
    "createdAt": "2026-05-11T10:00:00+08:00",
    "updatedAt": "2026-05-11T10:00:00+08:00"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| channelCode | String | 通道代码 |
| channelName | String | 通道名称 |
| subtitle | String \| null | 副标题 |
| parentCode | String \| null | 父级通道代码 |
| filterLabel | String \| null | 筛选按钮文字 |
| displayType | String | 展示类型 |
| content | String \| null | 富文本内容（HTML） |
| sortOrder | Integer | 排序值 |
| isActive | Boolean | 是否启用 |
| createdAt | String | 创建时间（ISO 8601格式） |
| updatedAt | String | 更新时间（ISO 8601格式） |

### 1.3 新增通道

**请求**
```
POST /api/v1/admin/special/channel
Content-Type: application/json

{
  "channelCode": "NEW_CHANNEL",
  "channelName": "新通道",
  "subtitle": "新通道副标题",
  "parentCode": null,
  "filterLabel": null,
  "displayType": "UNIVERSITY_LIST",
  "content": "<p>通道介绍内容</p>",
  "sortOrder": 10
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| channelCode | String | 是 | 最大30字符，唯一 | 通道代码 |
| channelName | String | 是 | 最大50字符 | 通道名称 |
| subtitle | String | 否 | 最大200字符 | 副标题 |
| parentCode | String | 否 | 最大30字符 | 父级通道代码 |
| filterLabel | String | 否 | 最大30字符 | 筛选按钮文字 |
| displayType | String | 是 | 只能是UNIVERSITY_LIST/ARTICLE_ONLY/MAJOR_DATA/GROUP | 展示类型 |
| content | String | 否 | - | 富文本内容 |
| sortOrder | Integer | 否 | - | 排序值，默认0 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该通道代码已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.4 修改通道

**请求**
```
PUT /api/v1/admin/special/channel/{id}
Content-Type: application/json

{
  "channelCode": "NEW_CHANNEL",
  "channelName": "新通道（已更新）",
  "subtitle": "更新后的副标题",
  "parentCode": null,
  "filterLabel": null,
  "displayType": "UNIVERSITY_LIST",
  "content": "<p>更新后的内容</p>",
  "sortOrder": 15
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.5 切换启用状态

**请求**
```
PUT /api/v1/admin/special/channel/{id}/toggle
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.6 删除通道

**请求**
```
DELETE /api/v1/admin/special/channel/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.7 批量删除

**请求**
```
DELETE /api/v1/admin/special/channel/batch
Content-Type: application/json

{
  "ids": [1893000000000001, 1893000000000002, 1893000000000003]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | Array\<Long\> | 是 | ID数组，不能为空 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

---

## 二、通道-大学关联接口

基础路径：`/api/v1/admin/special/channel-univ`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增关联 |
| PUT | `/{id}` | 修改关联 |
| PUT | `/{id}/toggle` | 切换启用状态 |
| DELETE | `/{id}` | 删除关联（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

### 2.1 分页查询列表

**请求**
```
GET /api/v1/admin/special/channel-univ/page?page=1&size=10
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |

> **注意**：此接口**无查询条件**，返回全部通道-大学关联记录

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000001",
        "channelName": "强基计划",
        "universityName": "清华大学",
        "year": 2026,
        "regionTag": null,
        "isActive": true
      },
      {
        "id": "1893000000000002",
        "channelName": "港澳院校招生",
        "universityName": "香港大学",
        "year": 2026,
        "regionTag": "香港",
        "isActive": true
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID（雪花算法） |
| channelName | String | 通道名称 |
| universityName | String | 大学名称 |
| year | Integer \| null | 招生年份 |
| regionTag | String \| null | 地区标签（香港/澳门） |
| isActive | Boolean | 是否启用 |

### 2.2 获取详情

**请求**
```
GET /api/v1/admin/special/channel-univ/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000001",
    "channelCode": "STRONG_BASE",
    "channelName": "强基计划",
    "universityId": "10001",
    "universityName": "清华大学",
    "year": 2026,
    "regionTag": null,
    "signupStart": "2026-04-01T00:00:00+08:00",
    "signupEnd": "2026-04-30T23:59:59+08:00",
    "officialUrl": "https://admission.tsinghua.edu.cn",
    "brochureTitle": "清华大学2026年强基计划招生简章",
    "brochureContent": "<p>招生简章详细内容...</p>",
    "sortOrder": 1,
    "isActive": true,
    "createdAt": "2026-05-11T10:00:00+08:00",
    "updatedAt": "2026-05-11T10:00:00+08:00"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| channelCode | String | 通道代码 |
| channelName | String | 通道名称 |
| universityId | String | 大学ID |
| universityName | String | 大学名称 |
| year | Integer \| null | 招生年份 |
| regionTag | String \| null | 地区标签 |
| signupStart | String \| null | 报名开始时间（ISO 8601格式） |
| signupEnd | String \| null | 报名截止时间（ISO 8601格式） |
| officialUrl | String \| null | 报名官网URL |
| brochureTitle | String \| null | 招生简章标题 |
| brochureContent | String \| null | 招生简章正文（HTML） |
| sortOrder | Integer | 排序值 |
| isActive | Boolean | 是否启用 |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |

### 2.3 新增关联

**请求**
```
POST /api/v1/admin/special/channel-univ
Content-Type: application/json

{
  "channelCode": "STRONG_BASE",
  "channelName": "强基计划",
  "universityId": 10001,
  "universityName": "清华大学",
  "year": 2026,
  "regionTag": null,
  "signupStart": "2026-04-01T00:00:00+08:00",
  "signupEnd": "2026-04-30T23:59:59+08:00",
  "officialUrl": "https://admission.tsinghua.edu.cn",
  "brochureTitle": "清华大学2026年强基计划招生简章",
  "brochureContent": "<p>招生简章详细内容...</p>",
  "sortOrder": 1
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| channelCode | String | 是 | 最大30字符 | 通道代码 |
| channelName | String | 是 | 最大50字符 | 通道名称 |
| universityId | Long | 是 | - | 大学ID |
| universityName | String | 是 | 最大50字符 | 大学名称 |
| year | Short | 否 | - | 招生年份 |
| regionTag | String | 否 | 最大20字符 | 地区标签 |
| signupStart | String | 否 | ISO 8601格式 | 报名开始时间 |
| signupEnd | String | 否 | ISO 8601格式 | 报名截止时间 |
| officialUrl | String | 否 | 最大500字符 | 报名官网URL |
| brochureTitle | String | 否 | 最大200字符 | 招生简章标题 |
| brochureContent | String | 否 | - | 招生简章正文 |
| sortOrder | Integer | 否 | - | 排序值，默认0 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该通道下该大学该年份的记录已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.4 修改关联

**请求**
```
PUT /api/v1/admin/special/channel-univ/{id}
Content-Type: application/json

{
  "channelCode": "STRONG_BASE",
  "channelName": "强基计划",
  "universityId": 10001,
  "universityName": "清华大学",
  "year": 2026,
  "regionTag": null,
  "signupStart": "2026-04-01T00:00:00+08:00",
  "signupEnd": "2026-05-15T23:59:59+08:00",
  "officialUrl": "https://admission.tsinghua.edu.cn",
  "brochureTitle": "清华大学2026年强基计划招生简章（更新版）",
  "brochureContent": "<p>更新后的招生简章内容...</p>",
  "sortOrder": 1
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.5 切换启用状态

**请求**
```
PUT /api/v1/admin/special/channel-univ/{id}/toggle
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.6 删除关联

**请求**
```
DELETE /api/v1/admin/special/channel-univ/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.7 批量删除

**请求**
```
DELETE /api/v1/admin/special/channel-univ/batch
Content-Type: application/json

{
  "ids": [1893000000000001, 1893000000000002]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | Array\<Long\> | 是 | ID数组，不能为空 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

---

## 三、强基计划数据接口

基础路径：`/api/v1/admin/special/strong-base-score`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增数据 |
| PUT | `/{id}` | 修改数据 |
| PUT | `/{id}/toggle` | 切换启用状态 |
| DELETE | `/{id}` | 删除数据（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

### 3.1 分页查询列表

**请求**
```
GET /api/v1/admin/special/strong-base-score/page?page=1&size=10&universityName=清华大学&year=2025&province=北京&subjectType=物理类
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |
| universityName | String | 否 | **精确查询** | 大学名称 |
| year | Short | 否 | **精确查询** | 年份 |
| province | String | 否 | **精确查询** | 省份 |
| subjectType | String | 否 | **精确查询** | 科类（物理类/历史类/理科/文科/综合改革） |

> **注意**：所有查询条件均为**精确匹配**，不支持模糊查询

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000001",
        "universityName": "清华大学",
        "year": 2025,
        "province": "北京",
        "subjectType": "物理类",
        "majorName": "数学与应用数学",
        "isActive": true
      },
      {
        "id": "1893000000000002",
        "universityName": "清华大学",
        "year": 2025,
        "province": "北京",
        "subjectType": "物理类",
        "majorName": "物理学",
        "isActive": true
      }
    ],
    "total": 500,
    "size": 10,
    "current": 1,
    "pages": 50
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID（雪花算法） |
| universityName | String | 大学名称 |
| year | Integer | 年份 |
| province | String | 省份 |
| subjectType | String | 科类 |
| majorName | String | 专业名称 |
| isActive | Boolean | 是否启用 |

### 3.2 获取详情

**请求**
```
GET /api/v1/admin/special/strong-base-score/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000001",
    "universityId": "10001",
    "universityName": "清华大学",
    "year": 2025,
    "province": "北京",
    "subjectType": "物理类",
    "majorName": "数学与应用数学",
    "majorCode": "070101",
    "entryScore": 680.00,
    "entryScoreType": "高考成绩",
    "entryFormula": null,
    "entryRatio": "1:5",
    "admissionScore": 92.50,
    "admissionFormula": "高考成绩×85%+校测成绩×15%",
    "planCount": 30,
    "admissionCount": 28,
    "remark": "破格入围不计入此线",
    "isActive": true,
    "createdAt": "2026-05-11T10:00:00+08:00",
    "updatedAt": "2026-05-11T10:00:00+08:00"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| universityId | String | 大学ID |
| universityName | String | 大学名称 |
| year | Integer | 年份 |
| province | String | 省份 |
| subjectType | String | 科类 |
| majorName | String | 专业名称 |
| majorCode | String \| null | 专业代码 |
| entryScore | Number \| null | 入围分数线 |
| entryScoreType | String | 入围分数类型 |
| entryFormula | String \| null | 入围计算公式 |
| entryRatio | String \| null | 入围比例 |
| admissionScore | Number \| null | 录取综合分 |
| admissionFormula | String \| null | 录取公式 |
| planCount | Integer \| null | 招生计划数 |
| admissionCount | Integer \| null | 实际录取人数 |
| remark | String \| null | 备注 |
| isActive | Boolean | 是否启用 |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |

### 3.3 新增数据

**请求**
```
POST /api/v1/admin/special/strong-base-score
Content-Type: application/json

{
  "universityId": 10001,
  "universityName": "清华大学",
  "year": 2026,
  "province": "北京",
  "subjectType": "物理类",
  "majorName": "数学与应用数学",
  "majorCode": "070101",
  "entryScore": 685.00,
  "entryScoreType": "高考成绩",
  "entryFormula": null,
  "entryRatio": "1:5",
  "admissionScore": 93.00,
  "admissionFormula": "高考成绩×85%+校测成绩×15%",
  "planCount": 35,
  "admissionCount": null,
  "remark": null
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| universityId | Long | 是 | - | 大学ID |
| universityName | String | 是 | 最大50字符 | 大学名称 |
| year | Short | 是 | - | 年份 |
| province | String | 是 | 最大20字符 | 省份 |
| subjectType | String | 是 | 最大20字符 | 科类 |
| majorName | String | 是 | 最大100字符 | 专业名称 |
| majorCode | String | 否 | 最大20字符 | 专业代码 |
| entryScore | BigDecimal | 否 | - | 入围分数线 |
| entryScoreType | String | 否 | 最大30字符 | 入围分数类型，默认"高考成绩" |
| entryFormula | String | 否 | 最大500字符 | 入围计算公式 |
| entryRatio | String | 否 | 最大20字符 | 入围比例 |
| admissionScore | BigDecimal | 否 | - | 录取综合分 |
| admissionFormula | String | 否 | 最大500字符 | 录取公式 |
| planCount | Integer | 否 | - | 招生计划数 |
| admissionCount | Integer | 否 | - | 实际录取人数 |
| remark | String | 否 | 最大500字符 | 备注 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该大学该年份该省份该科类该专业的记录已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.4 修改数据

**请求**
```
PUT /api/v1/admin/special/strong-base-score/{id}
Content-Type: application/json

{
  "universityId": 10001,
  "universityName": "清华大学",
  "year": 2026,
  "province": "北京",
  "subjectType": "物理类",
  "majorName": "数学与应用数学",
  "majorCode": "070101",
  "entryScore": 688.00,
  "entryScoreType": "高考成绩",
  "entryFormula": null,
  "entryRatio": "1:5",
  "admissionScore": 93.50,
  "admissionFormula": "高考成绩×85%+校测成绩×15%",
  "planCount": 35,
  "admissionCount": 33,
  "remark": "更新后的备注"
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.5 切换启用状态

**请求**
```
PUT /api/v1/admin/special/strong-base-score/{id}/toggle
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.6 删除数据

**请求**
```
DELETE /api/v1/admin/special/strong-base-score/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.7 批量删除

**请求**
```
DELETE /api/v1/admin/special/strong-base-score/batch
Content-Type: application/json

{
  "ids": [1893000000000001, 1893000000000002]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | Array\<Long\> | 是 | ID数组，不能为空 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

---

## 四、强基院校配置接口

基础路径：`/api/v1/admin/special/strong-base-univ`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增配置 |
| PUT | `/{id}` | 修改配置 |
| DELETE | `/{id}` | 删除配置（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

> **注意**：此模块**无切换启用状态接口**，不支持禁用功能

### 4.1 分页查询列表

**请求**
```
GET /api/v1/admin/special/strong-base-univ/page?page=1&size=10&universityName=清华大学&isPilot=true&pilotYear=2020&testBeforeScore=false
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |
| universityName | String | 否 | **精确查询** | 大学名称 |
| isPilot | Boolean | 否 | **精确查询** | 是否强基试点校 |
| pilotYear | Short | 否 | **精确查询** | 首次试点年份 |
| testBeforeScore | Boolean | 否 | **精确查询** | 是否高考出分前校测 |

> **注意**：所有查询条件均为**精确匹配**，不支持模糊查询

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000001",
        "universityName": "清华大学",
        "isPilot": true,
        "pilotYear": 2020,
        "testBeforeScore": false
      },
      {
        "id": "1893000000000002",
        "universityName": "复旦大学",
        "isPilot": true,
        "pilotYear": 2020,
        "testBeforeScore": true
      }
    ],
    "total": 39,
    "size": 10,
    "current": 1,
    "pages": 4
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID（雪花算法） |
| universityName | String | 大学名称 |
| isPilot | Boolean | 是否强基试点校 |
| pilotYear | Integer \| null | 首次试点年份 |
| testBeforeScore | Boolean | 是否高考出分前校测 |

### 4.2 获取详情

**请求**
```
GET /api/v1/admin/special/strong-base-univ/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000001",
    "universityId": "10001",
    "universityName": "清华大学",
    "isPilot": true,
    "pilotYear": 2020,
    "officialUrl": "https://admission.tsinghua.edu.cn/qjjh",
    "signupUrl": "https://bm.chsi.com.cn/jcxkzs/sch/10003",
    "testBeforeScore": false,
    "defaultEntryRatio": "1:5",
    "defaultAdmissionFormula": "高考成绩×85%+校测成绩×15%",
    "availableMajors": ["数学与应用数学", "物理学", "化学", "生物科学", "信息与计算科学"],
    "specialNotes": "清华大学强基计划招收理科基础学科拔尖学生",
    "createdAt": "2026-05-11T10:00:00+08:00",
    "updatedAt": "2026-05-11T10:00:00+08:00"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| universityId | String | 大学ID |
| universityName | String | 大学名称 |
| isPilot | Boolean | 是否强基试点校 |
| pilotYear | Integer \| null | 首次试点年份 |
| officialUrl | String \| null | 强基计划官方页面URL |
| signupUrl | String \| null | 报名入口URL |
| testBeforeScore | Boolean | 是否高考出分前校测 |
| defaultEntryRatio | String \| null | 默认入围比例 |
| defaultAdmissionFormula | String \| null | 默认录取公式 |
| availableMajors | Array\<String\> \| null | 可选专业列表 |
| specialNotes | String \| null | 特殊说明 |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |

### 4.3 新增配置

**请求**
```
POST /api/v1/admin/special/strong-base-univ
Content-Type: application/json

{
  "universityId": 10001,
  "universityName": "清华大学",
  "isPilot": true,
  "pilotYear": 2020,
  "officialUrl": "https://admission.tsinghua.edu.cn/qjjh",
  "signupUrl": "https://bm.chsi.com.cn/jcxkzs/sch/10003",
  "testBeforeScore": false,
  "defaultEntryRatio": "1:5",
  "defaultAdmissionFormula": "高考成绩×85%+校测成绩×15%",
  "availableMajors": ["数学与应用数学", "物理学", "化学", "生物科学"],
  "specialNotes": "特殊说明内容"
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| universityId | Long | 是 | 唯一 | 大学ID |
| universityName | String | 是 | 最大50字符 | 大学名称 |
| isPilot | Boolean | 否 | - | 是否强基试点校，默认true |
| pilotYear | Short | 否 | - | 首次试点年份 |
| officialUrl | String | 否 | 最大500字符 | 强基计划官方页面URL |
| signupUrl | String | 否 | 最大500字符 | 报名入口URL |
| testBeforeScore | Boolean | 否 | - | 是否高考出分前校测，默认false |
| defaultEntryRatio | String | 否 | 最大20字符 | 默认入围比例 |
| defaultAdmissionFormula | String | 否 | 最大500字符 | 默认录取公式 |
| availableMajors | Array\<String\> | 否 | - | 可选专业列表 |
| specialNotes | String | 否 | - | 特殊说明 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该大学的强基配置已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

### 4.4 修改配置

**请求**
```
PUT /api/v1/admin/special/strong-base-univ/{id}
Content-Type: application/json

{
  "universityId": 10001,
  "universityName": "清华大学",
  "isPilot": true,
  "pilotYear": 2020,
  "officialUrl": "https://admission.tsinghua.edu.cn/qjjh",
  "signupUrl": "https://bm.chsi.com.cn/jcxkzs/sch/10003",
  "testBeforeScore": false,
  "defaultEntryRatio": "1:6",
  "defaultAdmissionFormula": "高考成绩×85%+校测成绩×15%",
  "availableMajors": ["数学与应用数学", "物理学", "化学", "生物科学", "信息与计算科学"],
  "specialNotes": "更新后的特殊说明"
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 4.5 删除配置

**请求**
```
DELETE /api/v1/admin/special/strong-base-univ/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 4.6 批量删除

**请求**
```
DELETE /api/v1/admin/special/strong-base-univ/batch
Content-Type: application/json

{
  "ids": [1893000000000001, 1893000000000002]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | Array\<Long\> | 是 | ID数组，不能为空 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

---

## 五、查询条件汇总

| 子模块 | 查询字段 | 查询方式 |
|--------|----------|----------|
| 特殊招生通道列表 | displayType | **精确查询** |
| 特殊招生通道列表 | channelName | **模糊查询** |
| 通道-大学关联 | 无 | 无查询条件 |
| 强基计划数据 | universityName | **精确查询** |
| 强基计划数据 | year | **精确查询** |
| 强基计划数据 | province | **精确查询** |
| 强基计划数据 | subjectType | **精确查询** |
| 强基院校配置 | universityName | **精确查询** |
| 强基院校配置 | isPilot | **精确查询** |
| 强基院校配置 | pilotYear | **精确查询** |
| 强基院校配置 | testBeforeScore | **精确查询** |

---

## 六、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 数据校验失败 / 数据已存在 |
| 404 | 记录不存在 |
| 401 | 未登录 / Token过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 七、字段类型规范

为确保前后端数据一致性，所有接口遵循以下类型规范：

| 后端类型 | 前端类型 | 说明 |
|----------|----------|------|
| Long（ID） | String | 避免JavaScript精度丢失 |
| Short/Integer | Number | 数值类型 |
| BigDecimal | Number | 小数类型，如680.00 |
| String | String | 字符串 |
| Boolean | Boolean | 布尔值 |
| String[] | Array\<String\> | 字符串数组（如availableMajors） |
| OffsetDateTime | String | ISO 8601格式，如 `2026-05-11T10:00:00+08:00` |
| null | null | 空值保持null，不转为空字符串 |

> **重要**：所有模块的 `id` 字段返回为**字符串类型**，前端请求路径参数和请求体中也应传数字或字符串均可，后端自动转换。

---

## 八、功能对比

| 功能 | 特殊招生通道 | 通道-大学关联 | 强基计划数据 | 强基院校配置 |
|------|-------------|--------------|-------------|-------------|
| 分页查询 | ✓ | ✓ | ✓ | ✓ |
| 获取详情 | ✓ | ✓ | ✓ | ✓ |
| 新增 | ✓ | ✓ | ✓ | ✓ |
| 修改 | ✓ | ✓ | ✓ | ✓ |
| 切换状态 | ✓ | ✓ | ✓ | **✗** |
| 删除 | ✓（硬） | ✓（硬） | ✓（硬） | ✓（硬） |
| 批量删除 | ✓（硬） | ✓（硬） | ✓（硬） | ✓（硬） |
| 查询条件 | 2个 | 无 | 4个 | 4个 |
