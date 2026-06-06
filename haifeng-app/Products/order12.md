# C 端专业-考研方向关联查询 API 文档（专业↔考研方向双向）

## 功能概述

本模块实现 C 端本科专业与考研方向的**双向嵌套关联查询**：按本科专业 id 查可考的考研方向，按考研方向 id 查可考的本科专业。前端拿到任一专业列表中的 `id` 后，调用对应嵌套接口即可渲染「可关联的对端专业」清单。两个接口均需 Pro 及以上会员权限，不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 本科专业 → 考研方向 | 按本科专业 id 分页查关联的考研方向 | Pro 及以上 |
| 考研方向 → 本科专业 | 按考研方向 id 分页查关联的本科专业 | Pro 及以上 |

### 与已有接口的协同

| 已有接口（order7） | 拿到 id 后调用本模块 |
|---|---|
| `GET /api/v1/app/major/list`（公开）| `GET /api/v1/app/major/{majorId}/postgrad-directions`（Pro）|
| `GET /api/v1/app/postgrad-major/list`（登录）| `GET /api/v1/app/postgrad-major/{postgradMajorId}/undergraduate-majors`（Pro）|

前端流程示例：
1. 用户在「专业列表」点击某个本科专业 → 拿到 `majorId`
2. 调用本模块接口1 → 拿到该专业可考的考研方向列表
3. 用户点击某个考研方向 → 跳转到「考研专业详情」（order7 接口 6）

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| Pro 及以上 | 需 `member_type ∈ {pro, vip}`；由 `@RequirePro` 切面校验（已隐含登录） |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1717392000000
}
```

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | 字段级校验失败（`page < 1` / `size < 10` / `size > 1000`） |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 |
| 403 | 无权限 | 普通用户访问本模块接口 |
| 500 | 服务器内部错误 | 未预期异常 |

### 分页参数（BasePageQueryDTO）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–1000 |

### 数据可见性规则

- 关联表 `t_major_postgrad_direction` 无 `status` 字段，无需过滤
- 两端主表（`t_major` / `t_postgrad_major`）按 `status = 1` 过滤
- **路径 id 对端主表不存在时返回空分页**（不返回 404），与 `/{id}/universities` / `/{universityId}/postgrad-majors` 一致
- 无任何关联记录时同样返回空分页

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `postgrad_major_name` → `postgradMajorName`）
- 雪花算法生成的 ID 在 JSON 中以数字返回（注意前端 long 精度）

---

## 1. 本科专业 → 考研方向

**功能描述**：按本科专业 ID 分页查询与其关联的考研方向列表（id + 名称）。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/major/{majorId}/postgrad-directions` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| majorId | Path | Long | 是 | **精准（=）** | 本科专业 ID |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |

> 本接口无 DTO 筛选参数；按 `BasePageQueryDTO` 默认分页即可。

### 排序规则

`mpd.sort_order ASC, pm.id DESC`

- 优先按关联表的推荐排序（`sort_order`）升序
- 平局时按考研专业 id 降序（较新记录在前）

### 请求示例

```http
GET /api/v1/app/major/1001/postgrad-directions?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 2001, "postgradMajorName": "计算机科学与技术" },
      { "id": 2002, "postgradMajorName": "软件工程" },
      { "id": 2003, "postgradMajorName": "人工智能" }
    ],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**PostgradMajorDirectionBriefVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 考研专业 ID（前端据此跳转考研专业详情） |
| postgradMajorName | String | 考研专业名称 |

### 行为说明

1. 根据路径 `majorId` 联表 `t_major_postgrad_direction`（按 `major_id` 过滤）和 `t_postgrad_major`，只返回 `pm.status = 1` 的记录
2. 走 `idx_mpd_major` 索引 → 主键回表 `t_postgrad_major`
3. 关联结果按 `sort_order` 升序、`pm.id` 降序返回
4. **不存在任何关联时**返回空分页（`records: []`），不抛 404

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户（非 Pro/Vip） | 403 | 权限不足（需要专业版及以上） |
| `page < 1` 或 `size` 越界 | 400 | 字段级校验信息 |
| `majorId` 在 t_major 不存在 | 200 | 返回空分页（不抛 404） |
| `majorId` 存在但无任何关联 | 200 | 返回空分页 |

---

## 2. 考研方向 → 本科专业

**功能描述**：按考研方向 ID 分页查询与其关联的本科专业列表（id + 名称）。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/postgrad-major/{postgradMajorId}/undergraduate-majors` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| postgradMajorId | Path | Long | 是 | **精准（=）** | 考研专业 ID |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |

> 本接口无 DTO 筛选参数；按 `BasePageQueryDTO` 默认分页即可。

### 排序规则

`mpd.sort_order ASC, m.id DESC`

- 优先按关联表的推荐排序（`sort_order`）升序
- 平局时按本科专业 id 降序（较新记录在前）

### 请求示例

```http
GET /api/v1/app/postgrad-major/2001/undergraduate-majors?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 1001, "majorName": "计算机科学与技术" },
      { "id": 1002, "majorName": "软件工程" },
      { "id": 1003, "majorName": "数据科学与大数据技术" }
    ],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**UndergraduateMajorDirectionBriefVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 本科专业 ID（前端据此跳转本科专业详情） |
| majorName | String | 本科专业名称 |

### 行为说明

1. 根据路径 `postgradMajorId` 联表 `t_major_postgrad_direction`（按 `postgrad_major_id` 过滤）和 `t_major`，只返回 `m.status = 1` 的记录
2. 走 `idx_mpd_postgrad` 索引 → 主键回表 `t_major`
3. 关联结果按 `sort_order` 升序、`m.id` 降序返回
4. **不存在任何关联时**返回空分页（`records: []`），不抛 404

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户（非 Pro/Vip） | 403 | 权限不足（需要专业版及以上） |
| `page < 1` 或 `size` 越界 | 400 | 字段级校验信息 |
| `postgradMajorId` 在 t_postgrad_major 不存在 | 200 | 返回空分页（不抛 404） |
| `postgradMajorId` 存在但无任何关联 | 200 | 返回空分页 |

---

## 关联表与索引说明

| 表 | 关键字段 | 索引 |
|---|---|---|
| `t_major_postgrad_direction` | `major_id` | `idx_mpd_major` |
| `t_major_postgrad_direction` | `postgrad_major_id` | `idx_mpd_postgrad` |
| `t_major_postgrad_direction` | `major_name` | `idx_mpd_major_name`（btree pattern_ops）|
| `t_major_postgrad_direction` | `postgrad_major_name` | `idx_mpd_postgrad_name`（btree pattern_ops）|
| `t_major_postgrad_direction` | `(major_id, postgrad_major_id)` | `uk_major_postgrad` UNIQUE |

> 接口 1 走 `idx_mpd_major`；接口 2 走 `idx_mpd_postgrad`。两个索引都已存在（V10 迁移文件），无需新建。

---

## 筛选条件总览

| 接口 | 路径参数 | 模糊查询字段 | 精准查询字段 |
|------|----------|---------------|---------------|
| 1. 本科专业 → 考研方向 | `majorId`（path）| — | `page`、`size` |
| 2. 考研方向 → 本科专业 | `postgradMajorId`（path）| — | `page`、`size` |

> 本模块**无任何业务筛选字段**（如 `degreeType` / `majorCategory`），仅按 `BasePageQueryDTO` 默认分页。如需筛选，调用对应主表列表接口（order7 接口 1 / 5）即可。

---

## 接口路径速查

```
GET  /api/v1/app/major/{majorId}/postgrad-directions           [Pro]    本科专业 → 考研方向列表
GET  /api/v1/app/postgrad-major/{postgradMajorId}/undergraduate-majors  [Pro]    考研方向 → 本科专业列表
```

## 与 order7 模块的对照

| 已有（order7） | 新增（本模块 order12） |
|---|---|
| 专业列表 / 详情 / 统计 / 排行（4 个）| — |
| 考研专业列表 / 详情（2 个）| — |
| 大学 → 考研专业（1 个，Pro）| **本科专业 → 考研方向（1 个，Pro）** |
| 考研专业 → 大学（1 个，Pro）| **考研方向 → 本科专业（1 个，Pro）** |
