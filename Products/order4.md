# 首页管理模块实施报告

## 模块概述

首页管理模块为管理员提供公告、规划师、培训机构的增删改查功能，用于管理首页展示内容。

### 功能清单
| 子模块 | 功能 |
|--------|------|
| 公告管理 | 公告列表、详情、新增、修改、禁用/启用（状态切换）、硬删除 |
| 规划师管理 | 规划师列表、详情、新增、修改、禁用/启用（状态切换）、硬删除 |
| 培训机构管理 | 机构列表、详情、新增、修改、禁用/启用（状态切换）、硬删除 |

### 删除机制说明
| 操作 | HTTP方法 | 路径 | 说明 |
|------|----------|------|------|
| 硬删除 | DELETE | `/{id}` | 物理删除记录，数据不可恢复 |
| 禁用/启用 | PUT | `/{id}/status` | status=0禁用，status=1启用（软删除可恢复） |
| 详情 | GET | `/{id}` | 查看详情 |

前端列表每行应显示三个按钮：删除（硬删除）、禁用/启用、详情

---

## API 接口文档

### 管理端接口 (端口: 8081)

路由前缀：`/api/v1/admin/home/`

---

### 一、公告管理接口

#### 1.1 分页查询公告列表
```
GET /api/v1/admin/home/announcement/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 否 | 标题模糊查询 |
| status | Short | 否 | 状态: 0-下架 1-展示 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1234567890123456789",
        "title": "2026年高考志愿填报指南发布",
        "tag": "重要",
        "status": 1,
        "updatedAt": "2026-05-06T10:30:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714300000000
}
```

---

#### 1.2 获取公告详情
```
GET /api/v1/admin/home/announcement/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1234567890123456789",
    "title": "2026年高考志愿填报指南发布",
    "content": "<p>详细内容...</p>",
    "tag": "重要",
    "status": 1,
    "createdAt": "2026-05-01T09:00:00+08:00",
    "updatedAt": "2026-05-06T10:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 1.3 新增公告
```
POST /api/v1/admin/home/announcement
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 标题（最长100字符） |
| content | String | 是 | 内容（富文本） |
| tag | String | 否 | 标签（最长20字符） |

**请求示例：**
```json
{
  "title": "2026年高考志愿填报指南发布",
  "content": "<p>详细内容...</p>",
  "tag": "重要"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1234567890123456789",
  "timestamp": 1714300000000
}
```

---

#### 1.4 修改公告
```
PUT /api/v1/admin/home/announcement/{id}
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 标题（最长100字符） |
| content | String | 是 | 内容（富文本） |
| tag | String | 否 | 标签（最长20字符） |

**请求示例：**
```json
{
  "title": "2026年高考志愿填报指南发布（更新版）",
  "content": "<p>更新后的内容...</p>",
  "tag": "重要"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

#### 1.5 修改公告状态
```
PUT /api/v1/admin/home/announcement/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 状态: 0-下架 1-展示 |

**请求示例：**
```json
{
  "status": 0
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

#### 1.6 硬删除公告
```
DELETE /api/v1/admin/home/announcement/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复。如需保留数据，请使用状态切换接口（1.5）将status设为0（下架/禁用）。

**操作日志：** 此接口自动记录操作日志

---

### 二、规划师管理接口

#### 2.1 分页查询规划师列表
```
GET /api/v1/admin/home/planner/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 姓名模糊查询 |
| status | Short | 否 | 状态: 0-下架 1-展示 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1234567890123456789",
        "name": "张老师",
        "position": "高级规划师",
        "region": "北京",
        "avatar": "https://example.com/avatar.jpg",
        "specialty": "高考志愿规划",
        "douyinName": "张老师说志愿",
        "douyinUrl": "https://www.douyin.com/user/xxx",
        "sortOrder": 1,
        "status": 1
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714300000000
}
```

---

#### 2.2 获取规划师详情
```
GET /api/v1/admin/home/planner/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规划师ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1234567890123456789",
    "name": "张老师",
    "position": "高级规划师",
    "region": "北京",
    "avatar": "https://example.com/avatar.jpg",
    "specialty": "高考志愿规划",
    "douyinName": "张老师说志愿",
    "douyinUrl": "https://www.douyin.com/user/xxx",
    "personalDescription": "从事高考志愿规划10年...",
    "experienceJob": "曾任某高中升学指导主任...",
    "achievements": ["帮助1000+学生成功录取", "高考志愿规划师认证"],
    "expertiseAreas": ["提前批规划", "强基计划", "综合评价"],
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-01-01T09:00:00+08:00",
    "updatedAt": "2026-05-06T10:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 2.3 新增规划师
```
POST /api/v1/admin/home/planner
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名（最长50字符） |
| position | String | 否 | 职位（最长50字符） |
| region | String | 否 | 地区（最长20字符） |
| avatar | String | 否 | 头像URL（最长100字符） |
| specialty | String | 否 | 专长（最长100字符） |
| douyinName | String | 否 | 抖音名称（最长100字符） |
| douyinUrl | String | 否 | 抖音链接（最长100字符） |
| personalDescription | String | 否 | 个人简介 |
| experienceJob | String | 否 | 工作经历 |
| achievements | String[] | 否 | 成就列表 |
| expertiseAreas | String[] | 否 | 擅长领域 |
| sortOrder | Integer | 否 | 排序值（越小越靠前，默认0） |

**请求示例：**
```json
{
  "name": "张老师",
  "position": "高级规划师",
  "region": "北京",
  "avatar": "https://example.com/avatar.jpg",
  "specialty": "高考志愿规划",
  "douyinName": "张老师说志愿",
  "douyinUrl": "https://www.douyin.com/user/xxx",
  "personalDescription": "从事高考志愿规划10年...",
  "experienceJob": "曾任某高中升学指导主任...",
  "achievements": ["帮助1000+学生成功录取", "高考志愿规划师认证"],
  "expertiseAreas": ["提前批规划", "强基计划", "综合评价"],
  "sortOrder": 1
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1234567890123456789",
  "timestamp": 1714300000000
}
```

---

#### 2.4 修改规划师
```
PUT /api/v1/admin/home/planner/{id}
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规划师ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名（最长50字符） |
| position | String | 否 | 职位（最长50字符） |
| region | String | 否 | 地区（最长20字符） |
| avatar | String | 否 | 头像URL（最长100字符） |
| specialty | String | 否 | 专长（最长100字符） |
| douyinName | String | 否 | 抖音名称（最长100字符） |
| douyinUrl | String | 否 | 抖音链接（最长100字符） |
| personalDescription | String | 否 | 个人简介 |
| experienceJob | String | 否 | 工作经历 |
| achievements | String[] | 否 | 成就列表 |
| expertiseAreas | String[] | 否 | 擅长领域 |
| sortOrder | Integer | 否 | 排序值（越小越靠前） |

**请求示例：**
```json
{
  "name": "张老师",
  "position": "首席规划师",
  "region": "北京",
  "specialty": "高考志愿规划、强基计划",
  "sortOrder": 0
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

#### 2.5 修改规划师状态
```
PUT /api/v1/admin/home/planner/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规划师ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 状态: 0-下架 1-展示 |

**请求示例：**
```json
{
  "status": 0
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

#### 2.6 硬删除规划师
```
DELETE /api/v1/admin/home/planner/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规划师ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复。如需保留数据，请使用状态切换接口（2.5）将status设为0（下架/禁用）。

**操作日志：** 此接口自动记录操作日志

---

### 三、培训机构管理接口

#### 3.1 分页查询培训机构列表
```
GET /api/v1/admin/home/institution/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 名称模糊查询 |
| type | String | 否 | 类型筛选 |
| status | Short | 否 | 状态: 0-下架 1-展示 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1234567890123456789",
        "name": "海峰教育",
        "type": "高考志愿规划",
        "phone": "010-12345678",
        "address": "北京市海淀区xxx",
        "logo": "https://example.com/logo.png",
        "sortOrder": 1,
        "status": 1
      }
    ],
    "total": 30,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714300000000
}
```

---

#### 3.2 获取培训机构详情
```
GET /api/v1/admin/home/institution/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 机构ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1234567890123456789",
    "name": "海峰教育",
    "type": "高考志愿规划",
    "phone": "010-12345678",
    "address": "北京市海淀区xxx",
    "description": "专注高考志愿规划服务10年...",
    "courses": ["一对一志愿规划", "志愿填报指导课", "强基计划辅导"],
    "images": ["https://example.com/img1.jpg", "https://example.com/img2.jpg"],
    "logo": "https://example.com/logo.png",
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-01-01T09:00:00+08:00",
    "updatedAt": "2026-05-06T10:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 3.3 新增培训机构
```
POST /api/v1/admin/home/institution
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 机构名称（最长100字符） |
| type | String | 是 | 机构类型（最长100字符） |
| phone | String | 否 | 联系电话（最长20字符） |
| address | String | 否 | 地址（最长100字符） |
| description | String | 否 | 机构简介 |
| courses | String[] | 否 | 课程列表 |
| images | String[] | 否 | 机构图片列表 |
| logo | String | 否 | Logo URL（最长200字符） |
| sortOrder | Integer | 否 | 排序值（越小越靠前，默认0） |

**请求示例：**
```json
{
  "name": "海峰教育",
  "type": "高考志愿规划",
  "phone": "010-12345678",
  "address": "北京市海淀区xxx",
  "description": "专注高考志愿规划服务10年...",
  "courses": ["一对一志愿规划", "志愿填报指导课", "强基计划辅导"],
  "images": ["https://example.com/img1.jpg", "https://example.com/img2.jpg"],
  "logo": "https://example.com/logo.png",
  "sortOrder": 1
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1234567890123456789",
  "timestamp": 1714300000000
}
```

---

#### 3.4 修改培训机构
```
PUT /api/v1/admin/home/institution/{id}
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 机构ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 机构名称（最长100字符） |
| type | String | 是 | 机构类型（最长100字符） |
| phone | String | 否 | 联系电话（最长20字符） |
| address | String | 否 | 地址（最长100字符） |
| description | String | 否 | 机构简介 |
| courses | String[] | 否 | 课程列表 |
| images | String[] | 否 | 机构图片列表 |
| logo | String | 否 | Logo URL（最长200字符） |
| sortOrder | Integer | 否 | 排序值（越小越靠前） |

**请求示例：**
```json
{
  "name": "海峰教育（升级版）",
  "type": "高考志愿规划",
  "phone": "010-87654321",
  "address": "北京市朝阳区xxx",
  "sortOrder": 0
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

#### 3.5 修改培训机构状态
```
PUT /api/v1/admin/home/institution/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 机构ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Short | 是 | 状态: 0-下架 1-展示 |

**请求示例：**
```json
{
  "status": 0
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

#### 3.6 硬删除培训机构
```
DELETE /api/v1/admin/home/institution/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 机构ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复。如需保留数据，请使用状态切换接口（3.5）将status设为0（下架/禁用）。

**操作日志：** 此接口自动记录操作日志

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据库表结构

### t_announcements (公告表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 公告ID（雪花算法） |
| title | VARCHAR(100) | 公告标题 |
| content | TEXT | 公告内容 |
| tag | VARCHAR(20) | 公告标签 |
| status | SMALLINT | 状态: 0-下架, 1-展示 |
| is_deleted | BOOLEAN | 是否删除（软删除） |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_planners (规划师表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 规划师ID（雪花算法） |
| name | VARCHAR(50) | 规划师姓名 |
| position | VARCHAR(50) | 职位 |
| region | VARCHAR(20) | 所在地区 |
| avatar | VARCHAR(100) | 头像URL |
| specialty | VARCHAR(100) | 专业特长 |
| douyin_name | VARCHAR(100) | 抖音名称 |
| douyin_url | VARCHAR(100) | 抖音链接 |
| personal_description | TEXT | 个人简介 |
| experience_job | TEXT | 工作经历 |
| achievements | TEXT[] | 成就列表（数组） |
| expertise_areas | TEXT[] | 擅长领域（数组） |
| sort_order | INTEGER | 排序值（越小越靠前） |
| status | SMALLINT | 状态: 0-下架, 1-展示 |
| is_deleted | BOOLEAN | 是否删除（软删除） |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_institutions (培训机构表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 机构ID（雪花算法） |
| name | VARCHAR(100) | 机构名称 |
| type | VARCHAR(100) | 机构类型 |
| phone | VARCHAR(20) | 联系电话 |
| address | VARCHAR(100) | 机构地址 |
| description | TEXT | 机构简介 |
| courses | TEXT[] | 课程列表（数组） |
| images | TEXT[] | 机构图片（数组） |
| logo | VARCHAR(200) | 机构Logo URL |
| sort_order | INTEGER | 排序值（越小越靠前） |
| status | SMALLINT | 状态: 0-下架, 1-展示 |
| is_deleted | BOOLEAN | 是否删除（软删除） |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

---

## 文件清单

### haifeng-common (公共模块)
- `entity/home/Announcement.java` - 公告实体
- `entity/home/Planner.java` - 规划师实体
- `entity/home/Institution.java` - 培训机构实体
- `mapper/home/AnnouncementMapper.java` - 公告 Mapper
- `mapper/home/PlannerMapper.java` - 规划师 Mapper
- `mapper/home/InstitutionMapper.java` - 培训机构 Mapper

### haifeng-admin (管理端)
- `db/migration/V4__create_content_tables.sql` - 数据库迁移脚本
- `controller/home/AnnouncementController.java` - 公告控制器
- `controller/home/PlannerController.java` - 规划师控制器
- `controller/home/InstitutionController.java` - 培训机构控制器
- `service/home/AnnouncementService.java` - 公告服务接口
- `service/home/PlannerService.java` - 规划师服务接口
- `service/home/InstitutionService.java` - 培训机构服务接口
- `service/impl/home/AnnouncementServiceImpl.java` - 公告服务实现
- `service/impl/home/PlannerServiceImpl.java` - 规划师服务实现
- `service/impl/home/InstitutionServiceImpl.java` - 培训机构服务实现
- `dto/home/AnnouncementQueryDTO.java` - 公告查询 DTO
- `dto/home/AnnouncementAddDTO.java` - 公告新增 DTO
- `dto/home/AnnouncementUpdateDTO.java` - 公告修改 DTO
- `dto/home/PlannerQueryDTO.java` - 规划师查询 DTO
- `dto/home/PlannerAddDTO.java` - 规划师新增 DTO
- `dto/home/PlannerUpdateDTO.java` - 规划师修改 DTO
- `dto/home/InstitutionQueryDTO.java` - 培训机构查询 DTO
- `dto/home/InstitutionAddDTO.java` - 培训机构新增 DTO
- `dto/home/InstitutionUpdateDTO.java` - 培训机构修改 DTO
- `dto/home/StatusDTO.java` - 状态修改 DTO（公用）
- `vo/home/AnnouncementListVO.java` - 公告列表 VO
- `vo/home/AnnouncementDetailVO.java` - 公告详情 VO
- `vo/home/PlannerListVO.java` - 规划师列表 VO
- `vo/home/PlannerDetailVO.java` - 规划师详情 VO
- `vo/home/InstitutionListVO.java` - 培训机构列表 VO
- `vo/home/InstitutionDetailVO.java` - 培训机构详情 VO
