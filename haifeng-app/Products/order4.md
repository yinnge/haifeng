# C 端首页展示 API 文档（公告 / 规划师 / 培训机构）

## 功能概述

本模块实现 C 端首页三类运营内容的只读展示接口。所有接口均为**公开访问**（无需登录、不区分会员等级），结果通过 Redis 缓存 30 分钟以降低数据库压力。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 公告管理 | 公告分页列表、详情 | 公开访问 |
| 规划师管理 | 规划师分页列表（支持地区筛选）、详情 | 公开访问 |
| 培训机构管理 | 培训机构分页列表、详情 | 公开访问 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，无需 Token，任何人可访问 |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1717392000000
}
```

### 公共参数（所有列表接口）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–1000（可选档位：10/20/30/50/100） |

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | `page < 1`、`size < 10`、`size > 1000`、`region` 不在省份枚举中 |
| 404 | 资源不存在 | 详情接口的 id 不存在 / 已下架（status=0） |
| 500 | 服务器内部错误 | 未预期异常（如 Redis 连接失败） |

### 数据可见性规则

- **C 端只展示 `status = 1`（展示中）的数据**。Admin 端将一条数据下架（`status = 0`）后，C 端列表和详情都查不到该条数据（详情返回 404）。
- 缓存 TTL 30 分钟，Admin 改动后 C 端最长延迟 30 分钟生效。

### Redis 缓存说明

| Key 前缀 | 示例 |
|----------|------|
| `haifeng:app:home:announcement:list:` | `haifeng:app:home:announcement:list:p=1:s=10:tag=政策` |
| `haifeng:app:home:announcement:detail:` | `haifeng:app:home:announcement:detail:123456` |
| `haifeng:app:home:planner:list:` | `haifeng:app:home:planner:list:p=1:s=10:region=北京` |
| `haifeng:app:home:planner:detail:` | `haifeng:app:home:planner:detail:123456` |
| `haifeng:app:home:institution:list:` | `haifeng:app:home:institution:list:p=1:s=10` |
| `haifeng:app:home:institution:detail:` | `haifeng:app:home:institution:detail:123456` |

---

## 一、公告管理

### 1.1 分页查询公告列表

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/announcements` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |

**请求参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |
| tag | String | 否 | - | 公告标签精准匹配（非模糊） |

**请求示例**

```http
GET /api/v1/app/home/announcements?page=1&size=10&tag=政策 HTTP/1.1
Host: api.haifeng.com
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| records | Array | 公告列表 |
| records[].id | Long | 公告 ID（雪花算法） |
| records[].title | String | 公告标题 |
| records[].tag | String | 公告标签 |
| records[].updatedAt | DateTime | 更新时间（ISO-8601 带时区） |
| total | Long | 总条数 |
| current | Long | 当前页码 |
| size | Long | 每页条数 |
| pages | Long | 总页数 |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1798765432109876543,
        "title": "2026 年高考志愿填报指南正式发布",
        "tag": "政策",
        "updatedAt": "2026-06-01T10:30:00+08:00"
      },
      {
        "id": 1798765432109876544,
        "title": "新增双一流高校选科要求查询功能",
        "tag": "产品更新",
        "updatedAt": "2026-05-28T14:20:00+08:00"
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10,
    "pages": 3
  },
  "timestamp": 1717392000000
}
```

**排序规则**：按 `updatedAt DESC`。

---

### 1.2 查询公告详情

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/announcements/{id}` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 公告 ID |

**请求示例**

```http
GET /api/v1/app/home/announcements/1798765432109876543 HTTP/1.1
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 公告 ID |
| title | String | 公告标题 |
| content | String | 公告正文（富文本） |
| tag | String | 公告标签 |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1798765432109876543,
    "title": "2026 年高考志愿填报指南正式发布",
    "content": "为帮助 2026 届考生顺利完成志愿填报，本平台正式发布《2026 年高考志愿填报指南》……",
    "tag": "政策"
  },
  "timestamp": 1717392000000
}
```

**异常返回**

```json
{
  "code": 404,
  "msg": "公告不存在",
  "data": null,
  "timestamp": 1717392000000
}
```

---

## 二、规划师管理

### 2.1 分页查询规划师列表

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/planners` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |

**请求参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |
| region | String | 否 | - | 所在地区精准匹配，必须是省份枚举的中文名（见下表） |

**region 合法值**（必须严格匹配，否则返回 400）

```
北京、天津、河北、山西、内蒙古、辽宁、吉林、黑龙江、上海、江苏、
浙江、安徽、福建、江西、山东、河南、湖北、湖南、广东、广西、海南、
重庆、四川、贵州、云南、西藏、陕西、甘肃、青海、宁夏、新疆、
香港、澳门、台湾
```

**请求示例**

```http
GET /api/v1/app/home/planners?page=1&size=10&region=北京 HTTP/1.1
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| records | Array | 规划师列表 |
| records[].id | Long | 规划师 ID |
| records[].name | String | 规划师姓名 |
| records[].region | String | 所在地区（省份） |
| records[].position | String | 职位 |
| records[].avatar | String | 头像 URL |
| records[].specialty | String | 专业特长（一句话） |
| records[].personalDescription | String | 个人简介（前端 CSS 控制截断） |
| total | Long | 总条数 |
| current | Long | 当前页码 |
| size | Long | 每页条数 |
| pages | Long | 总页数 |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1798765432109800001,
        "name": "张老师",
        "region": "北京",
        "position": "高级规划师",
        "avatar": "https://cdn.haifeng.com/avatar/zhang.jpg",
        "specialty": "理科志愿填报",
        "personalDescription": "10 年高考志愿规划经验，累计服务考生 5000+……"
      }
    ],
    "total": 12,
    "current": 1,
    "size": 10,
    "pages": 2
  },
  "timestamp": 1717392000000
}
```

**排序规则**：按 `sortOrder ASC, id DESC`（运营自定义置顶顺序优先）。

**region 非法时的响应**

```json
{
  "code": 400,
  "msg": "无效的省份",
  "data": null,
  "timestamp": 1717392000000
}
```

---

### 2.2 查询规划师详情

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/planners/{id}` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 规划师 ID |

**请求示例**

```http
GET /api/v1/app/home/planners/1798765432109800001 HTTP/1.1
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 规划师 ID |
| name | String | 姓名 |
| position | String | 职位 |
| region | String | 所在地区 |
| avatar | String | 头像 URL |
| specialty | String | 专业特长 |
| douyinName | String | 抖音名称 |
| douyinUrl | String | 抖音链接 |
| personalDescription | String | 个人简介 |
| experienceJob | String | 工作经历 |
| achievements | Array&lt;String&gt; | 成就列表 |
| expertiseAreas | Array&lt;String&gt; | 擅长领域 |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1798765432109800001,
    "name": "张老师",
    "position": "高级规划师",
    "region": "北京",
    "avatar": "https://cdn.haifeng.com/avatar/zhang.jpg",
    "specialty": "理科志愿填报",
    "douyinName": "海峰张老师",
    "douyinUrl": "https://v.douyin.com/abc123",
    "personalDescription": "10 年高考志愿规划经验，累计服务考生 5000+，擅长理科及强基计划方向……",
    "experienceJob": "2015-2018 北京 XX 教育志愿规划主管\n2018-至今 海峰未来规划院",
    "achievements": [
      "2024 年全国优秀志愿规划师",
      "累计被清北录取学员 38 名"
    ],
    "expertiseAreas": [
      "强基计划",
      "综合评价",
      "国家专项"
    ]
  },
  "timestamp": 1717392000000
}
```

**异常返回**

```json
{
  "code": 404,
  "msg": "规划师不存在",
  "data": null,
  "timestamp": 1717392000000
}
```

---

## 三、培训机构管理

### 3.1 分页查询培训机构列表

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/institutions` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |

**请求参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数 |

> 当前版本无业务筛选字段，仅支持分页。

**请求示例**

```http
GET /api/v1/app/home/institutions?page=1&size=10 HTTP/1.1
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| records | Array | 培训机构列表 |
| records[].id | Long | 机构 ID |
| records[].name | String | 机构名称 |
| records[].type | String | 机构类型 |
| records[].description | String | 机构简介 |
| records[].images | Array&lt;String&gt; | 机构图片 URL 列表 |
| total | Long | 总条数 |
| current | Long | 当前页码 |
| size | Long | 每页条数 |
| pages | Long | 总页数 |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1798765432109900001,
        "name": "海峰志愿规划培训中心",
        "type": "志愿规划培训",
        "description": "专注高考志愿规划师培训，签约导师全程指导……",
        "images": [
          "https://cdn.haifeng.com/inst/haifeng-1.jpg",
          "https://cdn.haifeng.com/inst/haifeng-2.jpg"
        ]
      }
    ],
    "total": 8,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

**排序规则**：按 `sortOrder ASC, id DESC`（运营自定义置顶顺序优先）。

---

### 3.2 查询培训机构详情

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/institutions/{id}` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 培训机构 ID |

**请求示例**

```http
GET /api/v1/app/home/institutions/1798765432109900001 HTTP/1.1
```

**响应参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 机构 ID |
| name | String | 机构名称 |
| type | String | 机构类型 |
| phone | String | 联系电话 |
| address | String | 机构地址 |
| description | String | 机构简介 |
| courses | Array&lt;String&gt; | 课程列表 |
| images | Array&lt;String&gt; | 机构图片 URL 列表 |
| logo | String | 机构 Logo URL |

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1798765432109900001,
    "name": "海峰志愿规划培训中心",
    "type": "志愿规划培训",
    "phone": "400-888-8888",
    "address": "北京市海淀区中关村大街 1 号",
    "description": "专注高考志愿规划师培训，签约导师全程指导……",
    "courses": [
      "志愿规划师初级认证",
      "志愿规划师高级认证",
      "强基与综评专项研修"
    ],
    "images": [
      "https://cdn.haifeng.com/inst/haifeng-1.jpg",
      "https://cdn.haifeng.com/inst/haifeng-2.jpg"
    ],
    "logo": "https://cdn.haifeng.com/inst/haifeng-logo.png"
  },
  "timestamp": 1717392000000
}
```

**异常返回**

```json
{
  "code": 404,
  "msg": "培训机构不存在",
  "data": null,
  "timestamp": 1717392000000
}
```

---

## 附录：接口清单

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/api/v1/app/home/announcements` | 公告列表 |
| 2 | GET | `/api/v1/app/home/announcements/{id}` | 公告详情 |
| 3 | GET | `/api/v1/app/home/planners` | 规划师列表 |
| 4 | GET | `/api/v1/app/home/planners/{id}` | 规划师详情 |
| 5 | GET | `/api/v1/app/home/institutions` | 培训机构列表 |
| 6 | GET | `/api/v1/app/home/institutions/{id}` | 培训机构详情 |

---

## 附录：常见问答

**Q：Admin 修改了一条公告后，为什么 C 端没立刻看到？**
A：本模块采用 TTL 被动过期策略，缓存默认 30 分钟。最长延迟 30 分钟后 C 端会看到新内容；如需立即生效，可由运维清理对应 Redis Key。

**Q：列表的 `personalDescription` / `description` 字段很长，前端要不要截断？**
A：后端原样返回完整文本，由前端 CSS 控制截断（`-webkit-line-clamp` 或类似方案）。

**Q：详情接口拿不到刚下架的内容怎么办？**
A：下架后立即清缓存或等待 TTL 过期，C 端将返回 404「资源不存在」。这是预期行为：被下架的内容对 C 端不可见。

**Q：`region` 参数是中文吗？**
A：是。必须严格匹配 `ProvinceEnum` 中的中文 desc（如「北京」），传错会返回 400「无效的省份」。

**Q：列表接口是否有最大 size 限制？**
A：有。`size` 取值范围 10–1000，超出会被 `BasePageQueryDTO` 校验拦截返回 400。
