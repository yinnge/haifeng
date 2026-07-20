# 志愿方案模块接口文档

## 概述

本模块实现用户端志愿方案的完整生命周期管理，包含方案的创建、专业添加、排序调整、导出状态管理及 Excel 导出功能。系统根据用户高考档案自动计算安全系数，按档位（搏/冲/稳/保/垫）分类管理专业，并通过 Redis 缓存导出状态以支持高频交互。

**端口：** 8080（用户端）

**基础路径：** `/api/v1/app/algorithm/wish-plan`

**认证要求：** `@RequireLogin`（需登录，JWT Bearer Token）

**特殊权限：** 导出相关接口（progress/generate/download）需要 `@RequirePro`（Pro 及以上会员）

---

## 接口清单

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/default-limits` | 获取各档默认数量上限 | 登录 |
| POST | `/add-majors` | 添加专业到志愿方案 | 登录 |
| GET | `/my-plans` | 获取我的志愿方案列表 | 登录 |
| DELETE | `/{planId}` | 删除志愿方案 | 登录 |
| GET | `/{planId}/groups` | 分页查询方案内专业组 | 登录 |
| GET | `/{planId}/groups/{groupSnapshotId}/majors` | 分页查询专业组内专业 | 登录 |
| PUT | `/{planId}/groups/sort` | 修改专业组排序 | 登录 |
| PUT | `/{planId}/groups/{groupSnapshotId}/majors/sort` | 修改专业排序 | 登录 |
| PUT | `/{planId}/majors/{majorId}/export` | 修改单个专业导出状态 | 登录 |
| PUT | `/{planId}/groups/{groupSnapshotId}/export-all` | 批量修改专业组导出状态 | 登录 |
| GET | `/{planId}/export/progress` | 获取导出进度 | Pro+ |
| POST | `/{planId}/export/generate` | 生成导出文件 | Pro+ |
| GET | `/{planId}/export/download` | 下载导出文件 | Pro+ |
| POST | `/{planId}/export/save` | 保存导出状态到数据库 | 登录 |

---

## 1. 获取各档默认数量上限

### 1.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/wish-plan/default-limits` |
| 方法 | GET |
| 权限 | 登录用户（`@RequireLogin`） |

### 1.2 请求头

```
Authorization: Bearer {accessToken}
```

### 1.3 请求参数

无

### 1.4 查询逻辑说明

- 从 `system_settings` 表读取 5 个档位的默认推荐志愿数
- 通过 Redis 缓存 24 小时（key: `haifeng:wish-plan:default-limits`）
- 若 `system_settings` 表为空，返回零值（所有档位均为 0）
- Redis 异常时降级走 DB，不影响接口可用性

### 1.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "reachHighCount": 2,
    "reachCount": 3,
    "matchCount": 5,
    "safeCount": 3,
    "floorCount": 2
  },
  "timestamp": 1714300000000
}
```

### 1.6 响应字段说明（WishPlanLimitVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| reachHighCount | Integer | 搏(大胆冲刺)档默认推荐志愿数 |
| reachCount | Integer | 冲(可以冲击)档默认推荐志愿数 |
| matchCount | Integer | 稳(较为稳妥)档默认推荐志愿数 |
| safeCount | Integer | 保(比较安全)档默认推荐志愿数 |
| floorCount | Integer | 垫(高度保底)档默认推荐志愿数 |

---

## 2. 添加专业到志愿方案

### 2.1 接口信息

| 项 | 值 |
|----|----|
| URL | `POST /api/v1/app/algorithm/wish-plan/add-majors` |
| 方法 | POST |
| 权限 | 登录用户（`@RequireLogin`） |

### 2.2 请求头

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### 2.3 请求体（JSON）

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 否 | @Min(1) | 指定志愿方案ID。为空时自动获取或创建最近的方案 |
| groupId | Integer | 是 | @NotNull, @Min(1) | 专业组ID（来源：录取查询模块的 AdmissionGroup.id） |
| majorIds | List\<Long\> | 是 | @NotEmpty, @Size(max=100) | 专业ID列表（来源：录取查询模块的 AdmissionMajorScore.id） |

**请求示例：**

```json
{
  "planId": 1001,
  "groupId": 501,
  "majorIds": [2001, 2002, 2003]
}
```

### 2.4 业务逻辑说明

1. **高考档案校验**：用户必须有高考档案（`MemberGaokao`），否则返回 1010
2. **专业组校验**：`groupId` 必须存在且未删除，否则返回 1011
3. **专业归属校验**：每个 `majorId` 必须属于指定的 `groupId`，否则报错
4. **安全系数计算**：对每个专业计算安全等级，**"禁"级别专业不允许添加**
5. **重复添加校验**：同一志愿方案内不能重复添加相同专业（跨组也不行）
6. **档位数量限制**：添加后各档总数不能超过 `system_settings` 中的上限
7. **专业组快照去重**：同一 plan + groupId 只存一条 `WishGroupSnapshot`
8. **方案自动创建**：`planId` 为空时，自动创建新方案（受会员类型最大方案数限制）
9. **事务保护**：DB 写操作在 `TransactionTemplate` 中执行，CPU 计算在事务外完成

### 2.5 会员方案数量限制

| 会员类型 | 最大方案数 |
|----------|-----------|
| normal | 1 |
| pro | 5 |
| vip | 10 |

### 2.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1001,
    "planName": "我的志愿方案1",
    "planYear": 2025,
    "planProvince": "广东",
    "reformModel": "3+1+2",
    "planBatch": "本科批",
    "userScore": 620,
    "userRank": 15000,
    "boLimit": 2,
    "chongLimit": 3,
    "wenLimit": 5,
    "baoLimit": 3,
    "dieLimit": 2,
    "createdAt": "2025-06-25T10:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

### 2.7 响应字段说明（WishPlanListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 志愿方案ID |
| planName | String | 方案名称 |
| planYear | Short | 高考年份 |
| planProvince | String | 高考省份 |
| reformModel | String | 改革模式（如：3+1+2、3+3） |
| planBatch | String | 录取批次 |
| userScore | Integer | 用户分数 |
| userRank | Integer | 用户位次 |
| boLimit | Integer | 搏档已选专业数 |
| chongLimit | Integer | 冲档已选专业数 |
| wenLimit | Integer | 稳档已选专业数 |
| baoLimit | Integer | 保档已选专业数 |
| dieLimit | Integer | 垫档已选专业数 |
| createdAt | OffsetDateTime | 创建时间 |

### 2.8 响应示例（失败 - 高考档案不存在）

```json
{
  "code": 1010,
  "msg": "用户高考档案不存在，请先填写档案",
  "data": null,
  "timestamp": 1714300000000
}
```

### 2.9 响应示例（失败 - 专业为禁级别）

```json
{
  "code": 400,
  "msg": "专业「XXX」为'禁'级别，不允许添加到志愿表",
  "data": null,
  "timestamp": 1714300000000
}
```

### 2.10 响应示例（失败 - 档位超限）

```json
{
  "code": 400,
  "msg": "稳档专业已选5个，最多5个，本次添加2个超出限制",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 3. 获取我的志愿方案列表

### 3.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/wish-plan/my-plans` |
| 方法 | GET |
| 权限 | 登录用户（`@RequireLogin`） |

### 3.2 请求头

```
Authorization: Bearer {accessToken}
```

### 3.3 请求参数

无

### 3.4 查询逻辑说明

- 查询当前用户所有未删除的志愿方案
- 按 `created_at` 降序排列（最新创建的在前）
- 无分页，一次返回全部方案（受会员类型最大方案数限制，最多 10 个）

### 3.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1002,
      "planName": "我的志愿方案2",
      "planYear": 2025,
      "planProvince": "广东",
      "reformModel": "3+1+2",
      "planBatch": "本科批",
      "userScore": 620,
      "userRank": 15000,
      "boLimit": 1,
      "chongLimit": 2,
      "wenLimit": 3,
      "baoLimit": 2,
      "dieLimit": 1,
      "createdAt": "2025-06-25T11:00:00+08:00"
    },
    {
      "id": 1001,
      "planName": "我的志愿方案1",
      "planYear": 2025,
      "planProvince": "广东",
      "reformModel": "3+1+2",
      "planBatch": "本科批",
      "userScore": 620,
      "userRank": 15000,
      "boLimit": 2,
      "chongLimit": 3,
      "wenLimit": 5,
      "baoLimit": 3,
      "dieLimit": 2,
      "createdAt": "2025-06-25T10:30:00+08:00"
    }
  ],
  "timestamp": 1714300000000
}
```

---

## 4. 删除志愿方案

### 4.1 接口信息

| 项 | 值 |
|----|----|
| URL | `DELETE /api/v1/app/algorithm/wish-plan/{planId}` |
| 方法 | DELETE |
| 权限 | 登录用户（`@RequireLogin`） |

### 4.2 请求头

```
Authorization: Bearer {accessToken}
```

### 4.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 4.4 业务逻辑说明

- 校验方案存在且属于当前用户
- **先删子表再删父表**：先删除 `t_wish_major_snapshot`，再删除 `t_wish_group_snapshot`，最后软删 `t_wish_plan`
- `t_wish_plan` 使用软删除（`is_deleted = true`），快照表使用硬删除
- 事务保护，任一步骤失败整体回滚

### 4.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

### 4.6 响应示例（失败 - 方案不存在或无权操作）

```json
{
  "code": 1020,
  "msg": "志愿方案不存在",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 5. 分页查询方案内专业组

### 5.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/wish-plan/{planId}/groups` |
| 方法 | GET |
| 权限 | 登录用户（`@RequireLogin`） |

### 5.2 请求头

```
Authorization: Bearer {accessToken}
```

### 5.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 5.4 请求参数（Query String）

| 参数名 | 类型 | 必填 | 默认值 | 校验规则 | 说明 |
|--------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | @Min(1) | 页码，从1开始 |
| size | Integer | 否 | 10 | @Min(1), @Max(100) | 每页条数 |

### 5.5 查询逻辑说明

- 校验方案存在、属于当前用户
- 按 `group_sort_order` 升序排列
- 返回快照数据（`WishGroupSnapshot`），非实时查询录取表

### 5.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "groupId": 501,
        "planId": 1001,
        "groupSortOrder": 1,
        "universityId": 101,
        "universityName": "北京大学",
        "cityName": "北京",
        "category": "综合",
        "nature": "公办",
        "groupCode": "01",
        "groupName": "不限选考科目组",
        "enrollmentCode": "10001",
        "year": 2024,
        "province": "广东",
        "batch": "本科批",
        "subjects": ["物理", "化学"],
        "constraintsDescription": ["色盲不可报考"],
        "description": "包含计算机类、电子信息类等专业",
        "majorCount": 5,
        "tags": ["985", "211", "双一流"],
        "recommendationYear": 2024,
        "recommendationRate": 0.85
      }
    ],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1714300000000
}
```

### 5.7 响应字段说明（WishPlanGroupVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 快照ID（用于后续查询专业） |
| groupId | Integer | 原始专业组ID（来自 t_admission_group） |
| planId | Integer | 志愿方案ID |
| groupSortOrder | Integer | 排序号（越小越靠前） |
| universityId | Integer | 院校ID |
| universityName | String | 院校名称 |
| cityName | String | 城市名称 |
| category | String | 院校类别（如：综合、理工） |
| nature | String | 办学性质（如：公办、民办） |
| groupCode | String | 专业组代码 |
| groupName | String | 专业组名称 |
| enrollmentCode | String | 招生代码 |
| year | Short | 招生年份 |
| province | String | 招生省份 |
| batch | String | 录取批次 |
| subjects | List\<String\> | 选科要求科目 |
| constraintsDescription | List\<String\> | 约束条件描述 |
| description | String | 专业组说明 |
| majorCount | Integer | 包含专业数量 |
| tags | List\<String\> | 院校标签（如：985、211） |
| recommendationYear | Integer | 推荐数据年份 |
| recommendationRate | BigDecimal | 推荐指数 |

---

## 6. 分页查询专业组内专业

### 6.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/wish-plan/{planId}/groups/{groupSnapshotId}/majors` |
| 方法 | GET |
| 权限 | 登录用户（`@RequireLogin`） |

### 6.2 请求头

```
Authorization: Bearer {accessToken}
```

### 6.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |
| groupSnapshotId | Integer | 是 | @Min(1) | 专业组快照ID（来自 pageGroups 返回的 id） |

### 6.4 请求参数（Query String）

| 参数名 | 类型 | 必填 | 默认值 | 校验规则 | 说明 |
|--------|------|------|--------|----------|------|
| page | Integer | 否 | 1 | @Min(1) | 页码，从1开始 |
| size | Integer | 否 | 10 | @Min(1), @Max(100) | 每页条数 |

### 6.5 查询逻辑说明

- 校验方案存在、属于当前用户
- 校验 `groupSnapshotId` 属于该 `planId`
- 按 `major_sort_order` 升序排列
- 返回快照数据（`WishMajorSnapshot`），包含历史录取分

### 6.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "groupSnapshotId": 1,
        "majorId": 2001,
        "majorSortOrder": 1,
        "majorCode": "080901",
        "majorName": "计算机科学与技术",
        "duration": "4年",
        "tuition": "5000元/年",
        "description": "培养计算机领域高级人才",
        "admissionCount": 30,
        "safetyLevel": 0.72,
        "levelShort": "保",
        "historyScores": [
          {
            "year": 2024,
            "minScore": 675,
            "minRank": 1500,
            "avgScore": 680.00,
            "avgRank": 1200,
            "maxScore": 685,
            "maxRank": 1000,
            "admissionCount": 15
          },
          {
            "year": 2023,
            "minScore": 670,
            "minRank": 1800,
            "avgScore": 675.00,
            "avgRank": 1500,
            "maxScore": 680,
            "maxRank": 1200,
            "admissionCount": 18
          }
        ]
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1714300000000
}
```

### 6.7 响应字段说明（WishPlanMajorVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 快照ID |
| groupSnapshotId | Integer | 所属专业组快照ID |
| majorId | Long | 原始专业ID（来自 t_admission_major_score） |
| majorSortOrder | Integer | 排序号 |
| majorCode | String | 专业代码 |
| majorName | String | 专业名称 |
| duration | String | 学制 |
| tuition | String | 学费信息 |
| description | String | 专业说明 |
| admissionCount | Integer | 录取人数 |
| safetyLevel | BigDecimal | 安全系数 0.00~1.00 |
| levelShort | String | 安全等级简称：搏/冲/稳/保/垫 |
| historyScores | List\<YearScoreVO\> | 历史录取分快照（最多5年） |

### 6.8 YearScoreVO 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| year | Integer | 年份 |
| minScore | Integer | 最低分 |
| minRank | Integer | 最低位次 |
| avgScore | BigDecimal | 平均分 |
| avgRank | Integer | 平均位次 |
| maxScore | Integer | 最高分 |
| maxRank | Integer | 最高位次 |
| admissionCount | Integer | 录取人数 |

---

## 7. 修改专业组排序

### 7.1 接口信息

| 项 | 值 |
|----|----|
| URL | `PUT /api/v1/app/algorithm/wish-plan/{planId}/groups/sort` |
| 方法 | PUT |
| 权限 | 登录用户（`@RequireLogin`） |

### 7.2 请求头

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### 7.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 7.4 请求体（JSON）

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| items | List\<GroupSortItem\> | 是 | @NotEmpty, @Size(max=100), @Valid | 排序列表 |

**GroupSortItem 字段：**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| groupId | Integer | 是 | @NotNull | 专业组快照ID |
| sortOrder | Integer | 是 | @NotNull | 新的排序号（越小越靠前） |

**请求示例：**

```json
{
  "items": [
    { "groupId": 3, "sortOrder": 1 },
    { "groupId": 1, "sortOrder": 2 },
    { "groupId": 2, "sortOrder": 3 }
  ]
}
```

### 7.5 业务逻辑说明

- 校验方案存在、属于当前用户
- **批量验证**：所有 `groupId` 必须属于该 `planId`，否则返回 1021
- 排序号可以不连续，系统按 `sortOrder` 升序展示
- 支持拖拽排序：前端将整个列表按新顺序重新编号后一次性提交

### 7.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 8. 修改专业排序

### 8.1 接口信息

| 项 | 值 |
|----|----|
| URL | `PUT /api/v1/app/algorithm/wish-plan/{planId}/groups/{groupSnapshotId}/majors/sort` |
| 方法 | PUT |
| 权限 | 登录用户（`@RequireLogin`） |

### 8.2 请求头

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### 8.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |
| groupSnapshotId | Integer | 是 | @Min(1) | 专业组快照ID |

### 8.4 请求体（JSON）

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| items | List\<MajorSortItem\> | 是 | @NotEmpty, @Size(max=100), @Valid | 排序列表 |

**MajorSortItem 字段：**

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| majorId | Long | 是 | @NotNull | 专业快照ID（来自 pageMajors 返回的 id） |
| sortOrder | Integer | 是 | @NotNull | 新的排序号 |

**请求示例：**

```json
{
  "items": [
    { "majorId": 2, "sortOrder": 1 },
    { "majorId": 1, "sortOrder": 2 },
    { "majorId": 3, "sortOrder": 3 }
  ]
}
```

### 8.5 业务逻辑说明

- 校验方案存在、属于当前用户
- 校验 `groupSnapshotId` 属于该 `planId`
- **批量验证**：所有 `majorId` 必须属于该 `groupSnapshotId`，否则返回 1022

### 8.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 9. 修改单个专业导出状态

### 9.1 接口信息

| 项 | 值 |
|----|----|
| URL | `PUT /api/v1/app/algorithm/wish-plan/{planId}/majors/{majorId}/export` |
| 方法 | PUT |
| 权限 | 登录用户（`@RequireLogin`） |

### 9.2 请求头

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### 9.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |
| majorId | Integer | 是 | @Min(1) | 专业快照ID |

### 9.4 请求体（JSON）

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| isExported | Boolean | 是 | @NotNull | true=导出，false=不导出 |

**请求示例：**

```json
{
  "isExported": false
}
```

### 9.5 业务逻辑说明

- 校验方案存在、属于当前用户
- 校验专业存在且属于该方案
- **导出状态存 Redis**，不立即写 DB（key: `haifeng:wish:export:{memberId}:{planId}`）
- Redis 过期时间 7 天
- 前端需在完成所有导出状态调整后调用「保存导出状态到数据库」接口持久化

### 9.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 10. 批量修改专业组导出状态

### 10.1 接口信息

| 项 | 值 |
|----|----|
| URL | `PUT /api/v1/app/algorithm/wish-plan/{planId}/groups/{groupSnapshotId}/export-all` |
| 方法 | PUT |
| 权限 | 登录用户（`@RequireLogin`） |

### 10.2 请求头

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### 10.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |
| groupSnapshotId | Integer | 是 | @Min(1) | 专业组快照ID |

### 10.4 请求体（JSON）

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| isExported | Boolean | 是 | @NotNull | true=全部导出，false=全部不导出 |

**请求示例：**

```json
{
  "isExported": true
}
```

### 10.5 业务逻辑说明

- 校验方案存在、属于当前用户
- 校验专业组存在且属于该方案
- 批量设置该专业组下**所有专业**的导出状态
- 同样存 Redis，需配合「保存导出状态到数据库」接口持久化

### 10.6 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 11. 获取导出进度

### 11.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/wish-plan/{planId}/export/progress` |
| 方法 | GET |
| 权限 | Pro 及以上会员（`@RequirePro`） |

### 11.2 请求头

```
Authorization: Bearer {accessToken}
```

### 11.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 11.4 查询逻辑说明

- 校验方案存在、属于当前用户
- `totalMajors`：方案内所有专业总数（查 DB）
- `exportedMajors`：已勾选导出的专业数（查 Redis）
- `percentage` = exportedMajors * 100 / totalMajors
- `status`：`completed`（percentage >= 100）或 `processing`

### 11.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "totalMajors": 20,
    "exportedMajors": 15,
    "percentage": 75,
    "status": "processing",
    "message": "正在准备导出..."
  },
  "timestamp": 1714300000000
}
```

### 11.6 响应字段说明（WishPlanExportProgressVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| totalMajors | Integer | 方案内专业总数 |
| exportedMajors | Integer | 已勾选导出的专业数 |
| percentage | Integer | 进度百分比 0~100 |
| status | String | 状态：`processing` / `completed` |
| message | String | 状态消息 |

---

## 12. 生成导出文件

### 12.1 接口信息

| 项 | 值 |
|----|----|
| URL | `POST /api/v1/app/algorithm/wish-plan/{planId}/export/generate` |
| 方法 | POST |
| 权限 | Pro 及以上会员（`@RequirePro`） |

### 12.2 请求头

```
Authorization: Bearer {accessToken}
```

### 12.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 12.4 业务逻辑说明

- 校验方案存在、属于当前用户
- 从 Redis 读取导出状态，**仅导出 isExported=true 的专业**
- 生成 `.xlsx` 文件到服务器临时目录
- 文件名格式：`{方案名}_{会员ID}.xlsx`（已做用户隔离）
- 文件名安全处理：仅保留中文、字母、数字、下划线、连字符
- **非幂等**：每次调用都会生成新文件，旧文件在下载后自动删除
- 下载链接有效期：文件生成后直到被下载（下载后即删）

### 12.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "downloadUrl": "/api/v1/app/algorithm/wish-plan/1001/export/download?file=我的志愿方案1_1001.xlsx",
    "fileName": "我的志愿方案1_1001.xlsx"
  },
  "timestamp": 1714300000000
}
```

### 12.6 响应字段说明（WishPlanExportFileVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| downloadUrl | String | 下载链接（相对路径，前端拼接 base URL） |
| fileName | String | 文件名（可用于展示） |

### 12.7 注意事项

- 前端拿到 `downloadUrl` 后应立即发起下载请求
- 文件为一次性下载，下载后服务器自动删除
- 若用户未勾选任何专业（Redis 中无导出数据），生成的 Excel 为空表

---

## 13. 下载导出文件

### 13.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/algorithm/wish-plan/{planId}/export/download` |
| 方法 | GET |
| 权限 | Pro 及以上会员（`@RequirePro`） |

### 13.2 请求头

```
Authorization: Bearer {accessToken}
```

### 13.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 13.4 请求参数（Query String）

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| file | String | 是 | @NotBlank | 文件名（来自 generate 接口返回的 fileName） |

### 13.5 查询逻辑说明

- 校验方案存在、属于当前用户
- **文件名安全处理**：净化文件名（仅保留中文、字母、数字、下划线、连字符），防止路径穿越
- 校验解析后的规范路径仍在临时目录内
- 读取文件后**自动删除**临时文件
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename*=UTF-8''{encodedFileName}`

### 13.6 请求示例

```
GET /api/v1/app/algorithm/wish-plan/1001/export/download?file=%E6%88%91%E7%9A%84%E5%BF%97%E6%84%BF%E6%96%B9%E6%A1%881_1001.xlsx
Authorization: Bearer {accessToken}
```

### 13.7 响应

- 成功：返回文件二进制流（200 OK）
- 文件不存在：返回 404（`导出文件不存在，请先调用生成接口`）
- 非法文件名：返回 400（`非法文件名` 或 `非法文件路径`）

---

## 14. 保存导出状态到数据库

### 14.1 接口信息

| 项 | 值 |
|----|----|
| URL | `POST /api/v1/app/algorithm/wish-plan/{planId}/export/save` |
| 方法 | POST |
| 权限 | 登录用户（`@RequireLogin`） |

### 14.2 请求头

```
Authorization: Bearer {accessToken}
```

### 14.3 路径参数

| 参数名 | 类型 | 必填 | 校验规则 | 说明 |
|--------|------|------|----------|------|
| planId | Integer | 是 | @Min(1) | 志愿方案ID |

### 14.4 业务逻辑说明

- 校验方案存在、属于当前用户
- 从 Redis 读取所有导出状态（`haifeng:wish:export:{memberId}:{planId}`）
- **按状态分组批量更新** DB：isExported=true 的一批，false 的一批（最多 2 条 SQL）
- 更新后**删除 Redis 缓存**
- Redis 异常时降级跳过（不阻断流程）
- 用途：前端调整完导出勾选后，调用此接口将状态持久化到 DB

### 14.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

### 14.6 注意事项

- 此接口是可选的，Redis 中的导出状态在 7 天内有效
- 建议在用户完成所有导出勾选后、生成文件前调用
- 若 Redis 中无数据，接口直接返回成功（幂等）

---

## 15. 枚举值说明

### 15.1 安全等级（levelShort）

| 简称 | 名称 | 安全系数范围 | 颜色 | 说明 |
|------|------|-------------|------|------|
| 搏 | 大胆冲刺 | 0.00~0.30 | #FF4D4F | 录取概率极低，"彩票"志愿 |
| 冲 | 可以冲击 | 0.30~0.50 | #FFA940 | 有一定可能，但风险较大 |
| 稳 | 较为稳妥 | 0.50~0.70 | #FADB14 | 录取概率中等偏上 |
| 保 | 比较安全 | 0.70~0.85 | #52C41A | 录取概率较高 |
| 垫 | 高度保底 | 0.85~1.00 | #1890FF | 录取概率极高 |
| 禁 | 不可报考 | 0.00（固定） | #999999 | 存在硬性报考限制，不允许添加 |

### 15.2 导出状态（isExported）

| 值 | 说明 |
|----|------|
| true | 该专业纳入导出 |
| false | 该专业不导出 |

### 15.3 导出进度状态（status）

| 值 | 说明 |
|----|------|
| processing | 正在准备导出（percentage < 100） |
| completed | 导出完成（percentage >= 100） |

### 15.4 会员类型与权限

| 会员类型 | 志愿方案上限 | 导出功能 |
|----------|-------------|----------|
| normal | 1 | 不可使用（需 Pro+） |
| pro | 5 | 可使用 |
| vip | 10 | 可使用 |

### 15.5 改革模式（reformModel）常见值

| 值 | 说明 |
|----|------|
| 3+3 | 浙江、上海等 |
| 3+1+2 | 广东、江苏等 |
| 传统文理 | 云南、贵州等 |

---

## 16. 错误码说明

| code | 说明 | 触发场景 |
|------|------|---------|
| 200 | 成功 | 正常请求 |
| 400 | 参数错误 | 参数校验失败、专业不属于指定组、档位超限、重复添加专业 |
| 401 | 未登录或Token过期 | 未携带/无效JWT |
| 403 | 无权限 | Pro 以下用户访问导出接口 |
| 404 | 资源不存在 | 文件不存在（下载时） |
| 1010 | 高考档案不存在 | addMajors 时用户未填写 MemberGaokao |
| 1011 | 专业组不存在 | addMajors 时 groupId 不存在或已删除 |
| 1020 | 志愿方案不存在 | planId 不存在、已删除、或不属于当前用户 |
| 1021 | 方案专业组不存在 | groupSnapshotId 不存在或不属于该 planId |
| 1022 | 方案专业不存在 | majorId 不存在或不属于该 groupSnapshotId |
| 1030 | 导出失败 | Excel 文件生成/读取失败 |
| 500 | 服务器内部错误 | 系统异常 |

---

## 17. 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1714300000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 成功，其他见错误码说明 |
| msg | String | 状态描述 |
| data | Object | 响应数据，可为 null |
| timestamp | Long | 服务器时间戳（毫秒） |

---

## 18. 分页响应结构（MyBatis-Plus Page）

| 字段 | 类型 | 说明 |
|------|------|------|
| records | List | 数据列表 |
| total | Long | 总记录数 |
| size | Long | 每页条数 |
| current | Long | 当前页码 |
| pages | Long | 总页数 |

---

## 19. 数据库表结构（简表）

### 19.1 t_wish_plan（志愿方案主表）

来源：`V29__create_wish_plan_tables.sql`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL (PK) | 主键（自增） |
| member_id | BIGINT | 用户ID |
| plan_name | VARCHAR(100) | 方案名称，默认"我的志愿方案1" |
| plan_year | SMALLINT | 高考年份 |
| plan_province | VARCHAR(30) | 高考省份 |
| reform_model | VARCHAR(20) | 改革模式 |
| plan_batch | VARCHAR(50) | 录取批次 |
| user_score | INTEGER | 用户分数 |
| user_rank | INTEGER | 用户位次 |
| bo_limit | INTEGER | 搏档已选数量，默认0 |
| chong_limit | INTEGER | 冲档已选数量，默认0 |
| wen_limit | INTEGER | 稳档已选数量，默认0 |
| bao_limit | INTEGER | 保档已选数量，默认0 |
| die_limit | INTEGER | 垫档已选数量，默认0 |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### 19.2 t_wish_group_snapshot（专业组快照表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL (PK) | 主键（自增） |
| plan_id | INTEGER (FK) | 关联 t_wish_plan.id |
| group_id | INTEGER | 原始专业组ID |
| group_sort_order | INTEGER | 排序号，默认0 |
| university_id | BIGINT | 院校ID |
| university_name | VARCHAR(50) | 院校名称 |
| city_name | VARCHAR(50) | 城市名称 |
| year | SMALLINT | 招生年份 |
| province | VARCHAR(20) | 招生省份 |
| batch | VARCHAR(50) | 录取批次 |
| enrollment_code | VARCHAR(30) | 招生代码 |
| group_code | VARCHAR(30) | 专业组代码 |
| group_name | VARCHAR(100) | 专业组名称 |
| subjects | TEXT[] | 选科科目数组 |
| description | TEXT | 专业组说明 |
| constraints_description | TEXT[] | 约束条件数组 |
| category | VARCHAR(50) | 院校类别 |
| major_count | INTEGER | 包含专业数量 |
| nature | VARCHAR(50) | 办学性质 |
| recommendation_rate | DECIMAL(5,2) | 推荐指数 |
| recommendation_year | INTEGER | 推荐数据年份 |
| tags | TEXT[] | 院校标签数组 |
| created_at | TIMESTAMPTZ | 创建时间 |

### 19.3 t_wish_major_snapshot（专业明细快照表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL (PK) | 主键（自增） |
| plan_id | INTEGER (FK) | 关联 t_wish_plan.id |
| group_snapshot_id | INTEGER (FK) | 关联 t_wish_group_snapshot.id |
| major_id | BIGINT | 原始专业ID |
| major_sort_order | INTEGER | 排序号，默认0 |
| is_exported | BOOLEAN | 是否导出，默认true |
| major_code | VARCHAR(30) | 专业代码 |
| major_name | TEXT | 专业名称 |
| duration | VARCHAR(20) | 学制 |
| tuition | VARCHAR(50) | 学费 |
| description | TEXT | 专业说明 |
| admission_count | INTEGER | 录取人数 |
| safety_level | NUMERIC(3,2) | 安全系数 |
| level_short | VARCHAR(10) | 安全等级简称 |
| history_scores | JSONB | 历史录取分快照 |
| created_at | TIMESTAMPTZ | 创建时间 |

---

## 20. 文件清单

| 类型 | 文件路径 |
|------|---------|
| Controller | `haifeng-app/.../controller/algorithm/WishPlanController.java` |
| Service | `haifeng-app/.../service/algorithm/wish/WishPlanService.java` |
| ServiceImpl | `haifeng-app/.../service/impl/algorithm/wish/WishPlanServiceImpl.java` |
| DTO | `haifeng-app/.../dto/algorithm/wish/WishPlanAddMajorsDTO.java` |
| DTO | `haifeng-app/.../dto/algorithm/wish/WishGroupSortDTO.java` |
| DTO | `haifeng-app/.../dto/algorithm/wish/WishMajorSortDTO.java` |
| DTO | `haifeng-app/.../dto/algorithm/wish/WishMajorExportDTO.java` |
| DTO | `haifeng-app/.../dto/algorithm/wish/WishGroupExportAllDTO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishPlanListVO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishPlanGroupVO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishPlanMajorVO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishPlanLimitVO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishPlanExportFileVO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishPlanExportProgressVO.java` |
| VO | `haifeng-app/.../vo/algorithm/wish/WishExportMajorVO.java` |
| Entity | `haifeng-common/.../entity/algorithm/wish/WishPlan.java` |
| Entity | `haifeng-common/.../entity/algorithm/wish/WishGroupSnapshot.java` |
| Entity | `haifeng-common/.../entity/algorithm/wish/WishMajorSnapshot.java` |
| Mapper | `haifeng-common/.../mapper/algorithm/wish/WishPlanMapper.java` |
| Mapper | `haifeng-common/.../mapper/algorithm/wish/WishGroupSnapshotMapper.java` |
| Mapper | `haifeng-common/.../mapper/algorithm/wish/WishMajorSnapshotMapper.java` |
| Excel工具 | `haifeng-app/.../util/algorithm/wish/WishPlanExcelUtil.java` |
| Flyway | `haifeng-admin/.../db/migration/V29__create_wish_plan_tables.sql` |

---

## 21. 前端对接注意事项

### 21.1 接口调用顺序建议

```
1. GET /default-limits          → 获取各档上限，用于前端校验提示
2. GET /my-plans                → 获取方案列表
3. POST /add-majors             → 添加专业（可多次调用，逐步构建方案）
4. GET /{planId}/groups         → 查看方案内专业组
5. GET /{planId}/groups/{id}/majors → 查看专业组内专业
6. PUT /{planId}/groups/sort          → 调整专业组排序（拖拽后调用）
7. PUT /{planId}/groups/{id}/majors/sort → 调整专业排序（拖拽后调用）
8. PUT /{planId}/majors/{id}/export      → 勾选/取消勾选导出
9. PUT /{planId}/groups/{id}/export-all  → 批量勾选/取消
10. GET /{planId}/export/progress        → 查看导出进度（可选轮询）
11. POST /{planId}/export/save           → 持久化导出状态（可选）
12. POST /{planId}/export/generate       → 生成 Excel
13. GET /{planId}/export/download?file=  → 下载文件
```

### 21.2 排序交互

- 排序接口接收**完整列表**（不是单个移动），前端拖拽结束后将整个列表按新顺序重新编号
- `sortOrder` 从 1 开始，可以不连续（系统按数值升序排列）
- 示例：3 个专业组，用户将第 1 个拖到最后，前端提交 `[{id:2,sort:1}, {id:3,sort:2}, {id:1,sort:3}]`

### 21.3 导出状态管理

- 导出状态**先存 Redis**，高频交互不写 DB
- 用户完成所有勾选后，调用 `POST /export/save` 持久化
- 也可不调用 save，直接 generate（generate 从 Redis 读取）
- Redis 状态 7 天过期，过期后需重新勾选

### 21.4 文件下载

- `generate` 返回的 `downloadUrl` 是相对路径，前端需拼接 base URL
- 下载接口返回二进制流，前端用 `<a>` 标签 + `download` 属性或 `blob` 下载
- 文件为一次性下载，下载后服务器自动删除，刷新页面需重新 generate

### 21.5 专业添加的 planId 策略

- 首次添加时不传 `planId`，系统自动创建方案
- 后续添加时传入 `planId`，追加到已有方案
- 前端可通过 `GET /my-plans` 获取方案列表，让用户选择追加到哪个方案

### 21.6 错误处理

- 所有接口返回统一 `R<T>` 格式，`code=200` 表示成功
- 参数校验错误返回 `code=400`，`msg` 中包含具体字段错误信息
- 业务异常返回对应错误码（1010/1011/1020/1021/1022/1030）
- 401 表示 Token 过期，需跳转登录页
- 403 表示权限不足（Pro 以下用户访问导出功能）

### 21.7 快照机制

- 方案内的专业组和专业都是**快照数据**，添加时从录取表复制
- 快照保存了添加时的安全系数、历史录取分等信息
- 录取表数据更新不会影响已有方案
- 前端展示的是快照数据，非实时查询

### 21.8 拉动刷新建议

- `my-plans` 接口无缓存，每次进入方案列表页调用
- `groups` 和 `majors` 接口可缓存，但在添加/排序/删除后需刷新
- 导出状态在 `export` 和 `save` 接口调用后需刷新进度
