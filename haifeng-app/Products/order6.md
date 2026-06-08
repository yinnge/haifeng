# C 端首页展示 API 文档（公告 / 规划师 / 培训机构）

## 功能概述

本模块实现 C 端首页三类运营内容的只读展示接口。**所有接口公开访问**（无需登录、不区分会员等级），数据由 admin 端运营人员维护。C 端只读展示 `status = 1` 的数据，并通过 Redis 缓存（TTL = 30 分钟）降低数据库压力。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 公告（announcement） | 公告分页列表（带 tag 精准筛选）、详情 | 公开访问 |
| 规划师（planner） | 规划师分页列表（带 region 精准筛选）、详情 | 公开访问 |
| 培训机构（institution） | 培训机构分页列表、详情 | 公开访问 |

> 本模块仅**培训机构 `name`** 使用模糊查询（LIKE / 部分匹配），其余筛选参数（`tag`、`region`）以及路径参数 `id` 均为**精准查询**（`=` 精确匹配）。详见文末「模糊查询 vs 精准查询字段总览」。

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

### 公共分页参数（所有列表接口，`BasePageQueryDTO`）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1（`@Min(1)`） |
| size | Integer | 否 | 10 | 每页条数，10–1000（`@Min(10) @Max(1000)`，推荐档位：10/20/30/50/100） |

> 分页参数校验由 `BasePageQueryDTO` 上的 `@Min/@Max` 注解完成，校验失败时由 `GlobalExceptionHandler` 统一返回 400。

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | `page < 1` / `size < 10` / `size > 1000` / `region` 不在 `ProvinceEnum` 中文 desc 中 |
| 404 | 资源不存在 | 详情接口的 `id` 不存在 / 已下架（`status = 0`） |
| 500 | 服务器内部错误 | 未预期异常（如 Redis 连接失败时冒泡） |

### 数据可见性规则

- C 端列表/详情**强制 `status = 1`**（只看展示中），Admin 端将一条数据下架（`status = 0`）后，C 端列表和详情都查不到该条数据，详情返回 404。
- 软删除字段 `is_deleted = true` 由 MyBatis-Plus 的 `@TableLogic` 自动过滤，业务代码无需手动加条件。
- 详情不存在 / `status = 0` → 抛 `BusinessException(ResultCode.NOT_FOUND, "<资源>不存在")`。
- `region` 不在 `ProvinceEnum` 中 → 抛 `BusinessException(ResultCode.BAD_REQUEST, "无效的省份")`，**校验顺序必须在缓存读取之前**（非法参数不应消耗缓存槽位）。
- Admin 端写操作零改动；C 端缓存采用被动 TTL 过期策略，Admin 改动后 C 端最长延迟 30 分钟生效。

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `personal_description` → `personalDescription`）
- `TIMESTAMPTZ` 序列化为 ISO-8601 带时区字符串
- `TEXT[]` 字段（`achievements` / `expertiseAreas` / `courses` / `images`）通过 `JacksonTypeHandler` 双向转换
- 文本字段（`personalDescription` / `description`）**后端原样返回**，不做后端截断，由前端 CSS（`-webkit-line-clamp` 等）控制省略号

### Redis 缓存设计

| 项目 | 决定 |
|------|------|
| 序列化器 | `RedisTemplate<String, Object>` + JSON 序列化（含 `JavaTime` 模块） |
| TTL | **30 分钟**（`HOME_CACHE_TTL_MINUTES = 30L`），被动过期，不主动失效 |
| 空列表 | 同样缓存（防穿透） |
| 详情不存在 | **不缓存 `null`**，直接抛 404（流量小，可接受） |
| Redis 故障 | 不吞异常，让 `RedisConnectionFailureException` 冒泡到全局处理器（本期不引入降级） |
| 列表分页中转 | `Page<T>` → `PageCacheDTO<T>`（`records` / `total` / `current` / `size`），解决 `MyBatis-Plus Page` 序列化不稳定问题 |

#### Key 规范（追加到 `RedisKeyConstant`）

```java
public static final String HOME_ANNOUNCEMENT_LIST_PREFIX   = "haifeng:app:home:announcement:list:";
public static final String HOME_ANNOUNCEMENT_DETAIL_PREFIX = "haifeng:app:home:announcement:detail:";
public static final String HOME_PLANNER_LIST_PREFIX        = "haifeng:app:home:planner:list:";
public static final String HOME_PLANNER_DETAIL_PREFIX      = "haifeng:app:home:planner:detail:";
public static final String HOME_INSTITUTION_LIST_PREFIX    = "haifeng:app:home:institution:list:";
public static final String HOME_INSTITUTION_DETAIL_PREFIX  = "haifeng:app:home:institution:detail:";
public static final long   HOME_CACHE_TTL_MINUTES          = 30L;
```

#### Key 构造示例

| 接口 | Key 模式 | 示例 |
|------|----------|------|
| 公告列表 | `haifeng:app:home:announcement:list:p={page}:s={size}:tag={tag}` | `haifeng:app:home:announcement:list:p=1:s=10:tag=政策` |
| 公告详情 | `haifeng:app:home:announcement:detail:{id}` | `haifeng:app:home:announcement:detail:1798765432109876543` |
| 规划师列表 | `haifeng:app:home:planner:list:p={page}:s={size}:region={region}` | `haifeng:app:home:planner:list:p=1:s=10:region=北京` |
| 规划师详情 | `haifeng:app:home:planner:detail:{id}` | `haifeng:app:home:planner:detail:1798765432109800001` |
| 培训机构列表 | `haifeng:app:home:institution:list:p={page}:s={size}` | `haifeng:app:home:institution:list:p=1:s=10` |
| 培训机构详情 | `haifeng:app:home:institution:detail:{id}` | `haifeng:app:home:institution:detail:1798765432109900001` |

> `tag`/`region` 为空时拼为空串（如 `:tag=`），保证不同参数组合产生不同 Key。

#### 读写流程（每个查询方法统一套路）

```
1. region 校验（如有）—— 非法直接抛 400，不读缓存
2. 构造 cacheKey
3. cached = redisTemplate.opsForValue().get(cacheKey)
4. 命中 → 转回 PageCacheDTO → 重新组装为 Page<T> / IPage<T> → 直接返回
5. 未命中 → 查 DB（强制 status=1 + @TableLogic 过滤） → 转 PageCacheDTO → 写 redis（30 min TTL） → 返回
```

---

## 附录：枚举类说明

### `ProvinceEnum`（`com.haifeng.common.enums.ProvinceEnum`）

- **包路径**：`com.haifeng.common.enums.ProvinceEnum`
- **存储方式**：Java 枚举 + 中文 `desc` 字段（`@Getter` 暴露）
- **校验方法**：`ProvinceEnum.isValid(String province)`
  - 传入 `null` 返回 `true`（视为「不过滤」）
  - 严格按 `desc` 字符串匹配（`equals`），区分大小写
- **使用位置**：`PlannerServiceImpl` 在分页查询起始处调用，`PlannerController` 透传 `region` 字符串
- **非法响应**：HTTP 400 `{"code":400,"msg":"无效的省份"}`

> 本枚举**不含**县级市、特别行政区下的区/县；如传入「海淀区」「浦东新区」等市级以下名称，视为非法省份。

#### `ProvinceEnum` 全部合法值（中文 desc，传错即返回 400）

| # | 枚举名 | 合法 `desc`（必须严格匹配） |
|---|--------|------------------------------|
| 1 | `BEIJING` | 北京 |
| 2 | `TIANJIN` | 天津 |
| 3 | `HEBEI` | 河北 |
| 4 | `SHANXI` | 山西 |
| 5 | `NEIMENGGU` | 内蒙古 |
| 6 | `LIAONING` | 辽宁 |
| 7 | `JILIN` | 吉林 |
| 8 | `HEILONGJIANG` | 黑龙江 |
| 9 | `SHANGHAI` | 上海 |
| 10 | `JIANGSU` | 江苏 |
| 11 | `ZHEJIANG` | 浙江 |
| 12 | `ANHUI` | 安徽 |
| 13 | `FUJIAN` | 福建 |
| 14 | `JIANGXI` | 江西 |
| 15 | `SHANDONG` | 山东 |
| 16 | `HENAN` | 河南 |
| 17 | `HUBEI` | 湖北 |
| 18 | `HUNAN` | 湖南 |
| 19 | `GUANGDONG` | 广东 |
| 20 | `GUANGXI` | 广西 |
| 21 | `HAINAN` | 海南 |
| 22 | `CHONGQING` | 重庆 |
| 23 | `SICHUAN` | 四川 |
| 24 | `GUIZHOU` | 贵州 |
| 25 | `YUNNAN` | 云南 |
| 26 | `XIZANG` | 西藏 |
| 27 | `SHAANXI` | 陕西 |
| 28 | `GANSU` | 甘肃 |
| 29 | `QINGHAI` | 青海 |
| 30 | `NINGXIA` | 宁夏 |
| 31 | `XINJIANG` | 新疆 |
| 32 | `HONGKONG` | 香港 |
| 33 | `MACAO` | 澳门 |
| 34 | `TAIWAN` | 台湾 |

合计 **34 个**合法值（含 31 个省级行政区 + 香港 + 澳门 + 台湾）。

> 数据库 `t_planners.region` 列（`VARCHAR(20)`）存储的也是上述中文 desc，Service 层使用 `ProvinceEnum.isValid` 在内存中校验，**不走数据库 JOIN**。

---

## 一、公告管理（Announcement）

### 1.1 分页查询公告列表

**功能描述**：分页查询公告（`t_announcements`），按 `updatedAt DESC` 排序；支持可选的 `tag` **精准**查询。公开访问，30 分钟 Redis 缓存。

#### 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/announcements` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |
| Service | `AnnouncementServiceImpl.pageList(AnnouncementQueryDTO)` |

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 查询方式 | 说明 |
|------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | — | 页码（`@Min(1)`） |
| size | Integer | 否 | 10 | — | 每页条数（`@Min(10) @Max(1000)`） |
| tag | String | 否 | — | **精准（`=` 精确匹配）** | 公告标签，等值匹配 `t_announcements.tag`；不传则不过滤 |

> **注意**：`tag` 是**精准匹配**，不是 `LIKE '%tag%'`。例如传 `tag=政策` 只会命中 `tag` 列值**完全等于** `"政策"` 的记录，不会命中 `"政策解读"`、`"新政策"` 之类的部分匹配。

#### 排序规则

`updated_at DESC`（按更新时间倒序，最新优先）

#### 强制过滤

- `status = 1`（只看展示中）
- `is_deleted = false`（由 `@TableLogic` 自动追加）

#### 请求示例

```http
GET /api/v1/app/home/announcements?page=1&size=10&tag=政策 HTTP/1.1
Host: api.haifeng.com
```

#### 响应字段（`AnnouncementListVO`）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| id | Long | t_announcements.id | 公告 ID（雪花算法） |
| title | String | t_announcements.title | 公告标题 |
| tag | String | t_announcements.tag | 公告标签 |
| updatedAt | DateTime | t_announcements.updated_at | 更新时间（ISO-8601 带时区） |

#### 响应示例

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

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `page < 1` / `size` 越界 | 400 | 字段级校验信息 |
| Redis 异常 | 500 | 未预期异常（异常冒泡） |

---

### 1.2 查询公告详情

**功能描述**：根据 `id` 查询公告详情（`title` / `content` / `tag`），不存在或已下架返回 404。公开访问，30 分钟 Redis 缓存。

#### 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/announcements/{id}` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟（**详情不存在时不缓存**，直接抛 404） |
| Service | `AnnouncementServiceImpl.detail(Long id)` |

#### 路径参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| id | Long | 是 | **精准（`=`）** | 公告 ID |

#### 请求示例

```http
GET /api/v1/app/home/announcements/1798765432109876543 HTTP/1.1
```

#### 响应字段（`AnnouncementDetailVO`）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| id | Long | t_announcements.id | 公告 ID |
| title | String | t_announcements.title | 公告标题 |
| content | String | t_announcements.content | 公告正文（富文本，原样返回） |
| tag | String | t_announcements.tag | 公告标签 |

#### 响应示例

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

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `id` 不存在 | 404 | 公告不存在 |
| `id` 存在但 `status = 0`（下架） | 404 | 公告不存在 |

---

## 二、规划师管理（Planner）

### 2.1 分页查询规划师列表

**功能描述**：分页查询规划师（`t_planners`），按 `sort_order ASC, id DESC` 排序（运营自定义置顶顺序优先）；支持可选的 `region` **精准**查询，**`region` 值必须严格匹配 `ProvinceEnum.desc`**，否则返回 400。公开访问，30 分钟 Redis 缓存。

#### 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/planners` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |
| Service | `PlannerServiceImpl.pageList(PlannerQueryDTO)` |

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 查询方式 | 说明 |
|------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | — | 页码（`@Min(1)`） |
| size | Integer | 否 | 10 | — | 每页条数（`@Min(10) @Max(1000)`） |
| region | String | 否 | — | **精准（`=` 精确匹配 `ProvinceEnum.desc`）** | 所在地区，**必须是「附录：枚举类说明」中列出的 34 个中文省份名之一**；不传或传 `null` 则不过滤 |

> **注意**：`region` 是**精准匹配**，非模糊；同时**必须**通过 `ProvinceEnum.isValid()` 校验，传错（如 `火星`、`北京市`、`北京 `）一律返回 400「无效的省份」。

#### 排序规则

`sort_order ASC, id DESC`（运营自定义置顶顺序优先，平局时按 id 倒序）

#### 强制过滤

- `status = 1`
- `is_deleted = false`（由 `@TableLogic` 自动追加）
- 若 `region != null` 且 `!ProvinceEnum.isValid(region)` → 抛 `BusinessException(BAD_REQUEST, "无效的省份")`
- 校验顺序：region 校验**必须**在缓存读取之前，非法参数不消耗缓存槽位

#### 请求示例

```http
GET /api/v1/app/home/planners?page=1&size=10&region=北京 HTTP/1.1
```

#### 响应字段（`PlannerListVO`）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| id | Long | t_planners.id | 规划师 ID |
| name | String | t_planners.name | 规划师姓名 |
| region | String | t_planners.region | 所在地区（省份） |
| position | String | t_planners.position | 职位 |
| avatar | String | t_planners.avatar | 头像 URL |
| specialty | String | t_planners.specialty | 专业特长（一句话） |
| personalDescription | String | t_planners.personal_description | 个人简介（原样返回，前端 CSS 控制截断） |

#### 响应示例

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

#### `region` 非法时的响应

```http
GET /api/v1/app/home/planners?region=火星
```

```json
{
  "code": 400,
  "msg": "无效的省份",
  "data": null,
  "timestamp": 1717392000000
}
```

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `page < 1` / `size` 越界 | 400 | 字段级校验信息 |
| `region` 不在 `ProvinceEnum` 中（含 `null` 以外的非法值） | 400 | 无效的省份 |

---

### 2.2 查询规划师详情

**功能描述**：根据 `id` 查询规划师详情（11 个字段，含抖音信息和数组字段），不存在或已下架返回 404。公开访问，30 分钟 Redis 缓存。

#### 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/planners/{id}` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟（**详情不存在时不缓存**） |
| Service | `PlannerServiceImpl.detail(Long id)` |

#### 路径参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| id | Long | 是 | **精准（`=`）** | 规划师 ID |

#### 请求示例

```http
GET /api/v1/app/home/planners/1798765432109800001 HTTP/1.1
```

#### 响应字段（`PlannerDetailVO`）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| id | Long | t_planners.id | 规划师 ID |
| name | String | t_planners.name | 姓名 |
| position | String | t_planners.position | 职位 |
| region | String | t_planners.region | 所在地区 |
| avatar | String | t_planners.avatar | 头像 URL |
| specialty | String | t_planners.specialty | 专业特长 |
| douyinName | String | t_planners.douyin_name | 抖音名称 |
| douyinUrl | String | t_planners.douyin_url | 抖音链接 |
| personalDescription | String | t_planners.personal_description | 个人简介 |
| experienceJob | String | t_planners.experience_job | 工作经历 |
| achievements | Array\<String\> | t_planners.achievements（`TEXT[]` + `JacksonTypeHandler`） | 成就列表 |
| expertiseAreas | Array\<String\> | t_planners.expertise_areas（`TEXT[]` + `JacksonTypeHandler`） | 擅长领域 |

#### 响应示例

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

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `id` 不存在 | 404 | 规划师不存在 |
| `id` 存在但 `status = 0`（下架） | 404 | 规划师不存在 |

---

## 三、培训机构管理（Institution）

### 3.1 分页查询培训机构列表

**功能描述**：分页查询培训机构（`t_institutions`），按 `sort_order ASC, id DESC` 排序；支持可选的 `name` **模糊**查询。公开访问，30 分钟 Redis 缓存。

#### 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/institutions` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟 |
| Service | `InstitutionServiceImpl.pageList(InstitutionQueryDTO)` |

#### 请求参数

| 参数 | 类型 | 必填 | 默认值 | 查询方式 | 说明 |
|------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | — | 页码（`@Min(1)`） |
| size | Integer | 否 | 10 | — | 每页条数（`@Min(10) @Max(1000)`） |
| name | String | 否 | — | **模糊（`LIKE '%name%'`）** | 机构名称，部分匹配；不传则不过滤 |

#### 排序规则

`sort_order ASC, id DESC`（运营自定义置顶顺序优先，平局时按 id 倒序）

#### 强制过滤

- `status = 1`
- `is_deleted = false`（由 `@TableLogic` 自动追加）

#### 请求示例

```http
GET /api/v1/app/home/institutions?page=1&size=10 HTTP/1.1
```

#### 响应字段（`InstitutionListVO`）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| id | Long | t_institutions.id | 机构 ID |
| name | String | t_institutions.name | 机构名称 |
| type | String | t_institutions.type | 机构类型 |
| description | String | t_institutions.description | 机构简介（原样返回，前端 CSS 控制截断） |
| images | Array\<String\> | t_institutions.images（`TEXT[]` + `JacksonTypeHandler`） | 机构图片 URL 列表 |

#### 响应示例

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

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `page < 1` / `size` 越界 | 400 | 字段级校验信息 |

---

### 3.2 查询培训机构详情

**功能描述**：根据 `id` 查询培训机构详情（9 个字段，含电话、地址、课程、图片、Logo），不存在或已下架返回 404。公开访问，30 分钟 Redis 缓存。

#### 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/institutions/{id}` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 30 分钟（**详情不存在时不缓存**） |
| Service | `InstitutionServiceImpl.detail(Long id)` |

#### 路径参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| id | Long | 是 | **精准（`=`）** | 培训机构 ID |

#### 请求示例

```http
GET /api/v1/app/home/institutions/1798765432109900001 HTTP/1.1
```

#### 响应字段（`InstitutionDetailVO`）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| id | Long | t_institutions.id | 机构 ID |
| name | String | t_institutions.name | 机构名称 |
| type | String | t_institutions.type | 机构类型 |
| phone | String | t_institutions.phone | 联系电话 |
| address | String | t_institutions.address | 机构地址 |
| description | String | t_institutions.description | 机构简介 |
| courses | Array\<String\> | t_institutions.courses（`TEXT[]` + `JacksonTypeHandler`） | 课程列表 |
| images | Array\<String\> | t_institutions.images（`TEXT[]` + `JacksonTypeHandler`） | 机构图片 URL 列表 |
| logo | String | t_institutions.logo | 机构 Logo URL |

#### 响应示例

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

#### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `id` 不存在 | 404 | 培训机构不存在 |
| `id` 存在但 `status = 0`（下架） | 404 | 培训机构不存在 |

---

## 模糊查询 vs 精准查询字段总览

> **本模块全部为精准查询，没有任何模糊查询（LIKE / 部分匹配）。**

| # | 接口 | 模糊查询字段 | 精准查询字段 | 备注 |
|---|------|---------------|---------------|------|
| 1.1 | 公告列表 | — | `tag`（`=`） | 传 `tag=政策` 只命中 `tag` 列**完全等于** `"政策"` 的记录 |
| 1.2 | 公告详情 | — | `id`（path，`=`） | 主键查询 |
| 2.1 | 规划师列表 | — | `region`（`=` + `ProvinceEnum.isValid` 校验） | `region` 必须严格匹配 `ProvinceEnum.desc` 之一；非法返回 400 |
| 2.2 | 规划师详情 | — | `id`（path，`=`） | 主键查询 |
| 3.1 | 培训机构列表 | `name`（`LIKE`） | — | 名称模糊匹配，传 `name=海峰` 命中 `name` 列包含 `"海峰"` 的记录 |
| 3.2 | 培训机构详情 | — | `id`（path，`=`） | 主键查询 |

### 精准查询字段详细说明

| 字段 | 接口 | 校验位置 | 校验方式 | 非法处理 |
|------|------|----------|----------|----------|
| `tag` | 1.1 公告列表 | SQL 层（MyBatis-Plus `eq`） | 字符串等值匹配 | 不抛错，返回空分页 |
| `region` | 2.1 规划师列表 | Service 入口 | `ProvinceEnum.isValid(region)`，匹配 `desc` | `BusinessException(BAD_REQUEST, "无效的省份")` → HTTP 400 |
| `id`（path） | 1.2 / 2.2 / 3.2 详情 | SQL 层（MyBatis-Plus `eq`） | 主键等值匹配 | `BusinessException(NOT_FOUND, "<资源>不存在")` → HTTP 404 |
| `page` / `size` | 全部列表 | Controller 层（`@Min/@Max`） | 注解校验 | 字段级错误 → `GlobalExceptionHandler` → HTTP 400 |

### 关键约定

- **仅培训机构 `name` 字段使用 `LIKE` 模糊查询**：其余筛选条件（`tag`、`region`、`id`）都是 `=` 精确匹配。
- **区分大小写**：`tag`、`region` 匹配都区分大小写，传 `beijing` 不会命中 `北京`。
- **空值处理**：`tag`、`region` 不传（`null`）视为"不过滤"，不抛错。
- **`region` 校验顺序**：必须放在缓存读取之前，非法参数不消耗缓存槽位、不写任何 Key。

---

## 接口路径速查

```
GET  /api/v1/app/home/announcements              [公开]   公告列表（tag 精准）
GET  /api/v1/app/home/announcements/{id}         [公开]   公告详情
GET  /api/v1/app/home/planners                   [公开]   规划师列表（region 精准 + ProvinceEnum 校验）
GET  /api/v1/app/home/planners/{id}              [公开]   规划师详情
GET  /api/v1/app/home/institutions               [公开]   培训机构列表
GET  /api/v1/app/home/institutions/{id}          [公开]   培训机构详情
```

---

## 文件清单（实现参考）

### 修改（1 个文件）

| 文件 | 变更 |
|------|------|
| `haifeng-common/constant/RedisKeyConstant.java` | 追加 6 个 Key 前缀、TTL 常量 `HOME_CACHE_TTL_MINUTES = 30L`、6 个 Key 构造方法 |

### 新增（19 个文件）

```
haifeng-common/dto/common/
  └── PageCacheDTO.java                                 ← 通用分页缓存中转结构

haifeng-app/controller/home/
  ├── AnnouncementController.java
  ├── PlannerController.java
  └── InstitutionController.java

haifeng-app/service/home/
  ├── AnnouncementService.java
  ├── PlannerService.java
  └── InstitutionService.java

haifeng-app/service/impl/home/
  ├── AnnouncementServiceImpl.java
  ├── PlannerServiceImpl.java
  └── InstitutionServiceImpl.java

haifeng-app/dto/home/
  ├── AnnouncementQueryDTO.java                         extends BasePageQueryDTO { tag }
  ├── PlannerQueryDTO.java                              extends BasePageQueryDTO { region }
  └── InstitutionQueryDTO.java                          extends BasePageQueryDTO { /* 无业务字段 */ }

haifeng-app/vo/home/
  ├── AnnouncementListVO.java                           { id, title, tag, updatedAt }
  ├── AnnouncementDetailVO.java                         { id, title, content, tag }
  ├── PlannerListVO.java                                { id, name, region, position, avatar, specialty, personalDescription }
  ├── PlannerDetailVO.java                              { id, name, position, region, avatar, specialty, douyinName, douyinUrl, personalDescription, experienceJob, achievements, expertiseAreas }
  ├── InstitutionListVO.java                            { id, name, type, description, images }
  └── InstitutionDetailVO.java                          { id, name, type, phone, address, description, courses, images, logo }
```

---

## 附录：常见问答

**Q：Admin 修改了一条公告后，为什么 C 端没立刻看到？**
A：本模块采用 TTL 被动过期策略，缓存默认 30 分钟。最长延迟 30 分钟后 C 端会看到新内容；如需立即生效，可由运维清理对应 Redis Key（`haifeng:app:home:announcement:*` / `planner:*` / `institution:*`）。

**Q：`tag` 支持模糊搜索吗？**
A：**不支持**。`tag` 是精准（`=`）匹配，传入 `tag=政策` 只会命中 `tag` 列**完全等于** `"政策"` 的记录，不会命中 `"政策解读"`、`"新政策"` 等部分匹配。如需模糊请走其他模块。

**Q：`region` 参数是中文吗？**
A：是。`region` 必须是 `ProvinceEnum.desc` 中的中文省份名（34 个值之一，含 31 个省级行政区 + 香港 + 澳门 + 台湾），传错（包括「北京 ` 」、「火星」、「北京市」等）一律返回 400「无效的省份」。

**Q：`region` 校验为什么不走数据库 JOIN 而是用内存枚举？**
A：`ProvinceEnum` 是固定的 34 个值，列表稳定且数量小，在内存中校验性能最高；同时校验失败时可在缓存读取之前直接拦截，不消耗缓存槽位。

**Q：列表的 `personalDescription` / `description` 字段很长，前端要不要截断？**
A：后端**原样返回**完整文本（不做后端截断），由前端 CSS 控制截断（`-webkit-line-clamp` 或类似方案）。

**Q：详情接口拿不到刚下架的内容怎么办？**
A：下架后 C 端立即从缓存和数据库都查不到该记录（列表过滤 + 详情 404）。但因有 30 分钟缓存，**最坏情况下需要 30 分钟后 C 端才看不到**。这是预期行为：被下架的内容对 C 端不可见。

**Q：列表接口是否有最大 size 限制？**
A：有。`size` 取值范围 10–1000（`@Min(10) @Max(1000)`），超出会被 `BasePageQueryDTO` 校验拦截返回 400。

**Q：Redis 挂了怎么办？**
A：本期不做降级。`RedisConnectionFailureException` 会冒泡到 `GlobalExceptionHandler`，对应接口返回 HTTP 500。后续可在 Service 加 `try/catch` 退化为直查 DB。

**Q：详情不存在时为什么不缓存 `null` 防穿透？**
A：C 端详情流量较小，TTL 30 分钟内同一 id 重复请求概率不高，不缓存 `null` 可简化实现。列表的空结果**会被缓存**以防穿透。
