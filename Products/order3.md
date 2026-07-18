# 用户管理模块扩展实施报告

## 概述

本次实现了管理端用户管理模块的扩展功能：

1. **会员开通/升级/续费** - 支持 normal→pro→vip 升级路径，包含推荐佣金计算
2. **订单管理** - 订单分页查询、详情查看、删除
3. **佣金管理** - 佣金记录分页查询、删除
4. **通知管理** - 通知分页查询、群发系统公告、删除
5. **提现管理** - 提现记录分页查询、处理（支付/拒绝）、删除

---

## 技术亮点

### 1. 会员升级路径控制
- 升级路径：normal→pro→vip
- Pro 会员可续费 Pro 或升级 VIP
- VIP 会员可续费 VIP；过期后可降级为 Pro
- 未过期续费从原到期时间叠加，已过期或类型变更从当前时间计算
- VIP→PRO 降级时保留剩余时长（从原到期时间叠加）

### 2. 推荐佣金自动计算
- 订单创建时自动检查推荐人关系
- 根据最终会员类型计算佣金（Pro/VIP 不同比率）
- 佣金自动累加到推荐人余额
- 同时发送佣金到账通知

### 3. 提现流程闭环
- 待处理 → 已支付/已拒绝 状态流转
- 拒绝时自动退回佣金到用户余额
- 支持查看用户微信明文（强制记录操作日志）

### 4. 系统公告群发
- 支持按会员类型筛选目标用户
- 批量插入通知记录
- 返回群发用户数量

### 5. 数据脱敏
- 用户名脱敏：保留首字，其余用 `*` 替代
- 微信号脱敏：保留前4位和后2位
- 列表默认脱敏，明文需单独接口获取

### 6. 硬删除与恢复机制
- **禁用（软删除）**：设置 `is_deleted = true`，记录保留在数据库
- **硬删除（物理删除）**：永久删除记录，避免数据库数据越来越多
- **恢复**：将已禁用的记录恢复为正常状态（`is_deleted = false`）
- 前端列表可显示三个按钮：删除（硬删除）、禁用/恢复、详情

---

## 接口文档

### 会员管理接口 (端口: 8081)

> **乐观锁：** 所有写操作（updateStatus、upgrade）基于 `version` 字段实现乐观锁，数据被并发修改时返回 `400: 数据已被其他人修改，请刷新后重试`。

#### 1. 分页查询用户列表
```
GET /api/v1/admin/user/list
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数：10/20/30/50/100，默认10 |
| phone | String | 否 | 手机号（模糊查询，最长50字符） |
| memberType | String | 否 | 会员类型：normal/pro/vip |
| status | String | 否 | 账号状态：active/disabled |
| wechatId | String | 否 | 微信号（精确查询，后端转盲索引） |
| inviteCode | String | 否 | 邀请码（模糊查询，最长50字符） |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "username": "张三",
        "phone": "138****5678",
        "memberType": "pro",
        "wechatId": "wxid***23",
        "status": "active",
        "lastLoginAt": "2026-05-06T10:00:00",
        "lastLoginIp": "192.168.1.1",
        "createdAt": "2026-05-06T10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 获取用户详情
```
GET /api/v1/admin/user/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "username": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "138****5678",
    "inviteCode": "ABC12345",
    "memberType": "pro",
    "expireAt": "2027-05-06T10:00:00",
    "wechatId": "wxid***23",
    "referrerId": 1111111111111111111,
    "referrerUsername": "推荐人",
    "commissionBalance": 50.00,
    "commissionTotalEarned": 200.00,
    "commissionTotalPaid": 150.00,
    "status": "active",
    "lastLoginAt": "2026-05-06T10:00:00",
    "lastLoginIp": "192.168.1.1",
    "createdAt": "2026-05-06T10:00:00",
    "updatedAt": "2026-05-06T10:00:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 3. 修改用户状态
```
PUT /api/v1/admin/user/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 是 | 状态：active（启用）或 disabled（禁用） |

**请求示例：**
```json
{
  "status": "disabled"
}
```

**操作日志：** 此接口自动记录操作日志

---

#### 4. 查看用户微信明文
```
GET /api/v1/admin/user/{id}/wechat
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "wxid_abc123456",
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口强制记录操作日志（敏感操作）
**频率限制：** 每IP每分钟最多10次

---

#### 5. 会员开通/升级/续费
```
POST /api/v1/admin/user/{id}/upgrade
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | String | 是 | 目标会员类型：pro/vip |
| durationMonths | Integer | 是 | 购买时长（月），范围1-120 |
| amount | BigDecimal | 否 | 支付金额（>0），不传则根据系统设置自动计算 |
| remark | String | 否 | 备注 |

**请求示例：**
```json
{
  "targetType": "pro",
  "durationMonths": 12,
  "remark": "后台手动开通"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1714300000000
}
```

**返回值说明：** data 为生成的订单ID

**业务规则：**
- normal 用户可开通 pro 或 vip
- pro 用户可续费 pro 或升级 vip
- vip 用户可续费 vip；过期后可降级为 pro
- 未过期续费：从原到期时间叠加
- 已过期或类型变更：从当前时间开始计算
- **幂等防重复**：同一用户不可创建同类型未删除订单，已存在则返回 `400: 该用户已有同类未完成订单，请勿重复操作`
- VIP→PRO 降级时保留剩余时长（从原到期时间叠加）

**操作日志：** 此接口自动记录操作日志

---

### 订单管理接口 (端口: 8081)

> **乐观锁：** 所有写操作（delete、restore）基于 `version` 字段实现乐观锁，数据被并发修改时返回 `400: 数据已被其他人修改，请刷新后重试`。

#### 1. 分页查询订单列表
```
GET /api/v1/admin/user/order/list
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数：10/20/30/50/100，默认10 |
| phone | String | 否 | 手机号（模糊查询，最长50字符） |
| wechatId | String | 否 | 微信号（精确查询，后端转盲索引） |
| operatorName | String | 否 | 操作人名称（模糊查询，最长50字符） |
| orderType | String | 否 | 订单类型：new/renewal |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "orderNo": "ORD202605061234567890",
        "memberName": "张三",
        "phone": "138****5678",
        "wechatId": "wxid***23",
        "orderType": "new",
        "beforeType": "normal",
        "afterType": "pro",
        "durationMonths": 12,
        "amount": 199.00,
        "createdAt": "2026-05-06T10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 获取订单详情
```
GET /api/v1/admin/user/order/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "orderNo": "ORD202605061234567890",
    "memberName": "张三",
    "phone": "138****5678",
    "wechatId": "wxid***23",
    "orderType": "new",
    "beforeType": "normal",
    "afterType": "pro",
    "durationMonths": 12,
    "amount": 199.00,
    "memberId": 9876543210987654321,
    "beforeExpireAt": null,
    "afterExpireAt": "2027-05-06T10:00:00",
    "operatorId": 1111111111111111111,
    "operatorName": "admin",
    "remark": "后台手动开通",
    "createdAt": "2026-05-06T10:00:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 3. 查看订单微信明文
```
GET /api/v1/admin/user/order/{id}/wechat
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "wxid_abc123456",
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口强制记录操作日志（敏感操作）
**频率限制：** 每IP每分钟最多10次

---

#### 4. 禁用订单（软删除）
```
DELETE /api/v1/admin/user/order/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

#### 5. 硬删除订单（物理删除）
```
DELETE /api/v1/admin/user/order/{id}/hard
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：**
- 物理删除记录，数据不可恢复
- 若该订单存在关联的佣金记录，拒绝硬删除并返回 `400: 该订单存在关联的佣金记录，无法硬删除`

**操作日志：** 此接口自动记录操作日志

---

#### 6. 恢复订单
```
PUT /api/v1/admin/user/order/{id}/restore
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：**
- 将已禁用的订单恢复为正常状态
- 恢复前校验订单所属会员是否仍存在且未删除，否则返回 `400: 该订单所属会员已不存在，无法恢复`

**操作日志：** 此接口自动记录操作日志

---

### 佣金管理接口 (端口: 8081)

#### 1. 分页查询佣金列表
```
GET /api/v1/admin/user/commission/list
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数 |
| referrerPhone | String | 否 | 推荐人手机号（前缀匹配，最长20字符） |
| referrerName | String | 否 | 推荐人名称（模糊查询，最长50字符） |
| refereePhone | String | 否 | 被推荐人手机号（前缀匹配，最长20字符） |
| refereeName | String | 否 | 被推荐人名称（模糊查询，最长50字符） |
| orderNo | String | 否 | 订单号（模糊查询，最长50字符） |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "referrerName": "推荐人",
        "referrerPhone": "138****5678",
        "refereeName": "被推荐人",
        "refereePhone": "139****1234",
        "orderId": 3333333333333333333,
        "orderAmount": 199.00,
        "commissionRate": 10,
        "commissionAmount": 19.90,
        "createdAt": "2026-05-06T10:00:00+08:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 禁用佣金记录（软删除）
```
DELETE /api/v1/admin/user/commission/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 佣金记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

#### 3. 硬删除佣金记录（物理删除）
```
DELETE /api/v1/admin/user/commission/{id}/hard
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 佣金记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复。**必须先禁用（软删除）才能硬删除**，未禁用的记录执行硬删除会返回 `400: 请先禁用该佣金记录，再执行硬删除`

**操作日志：** 此接口自动记录操作日志

---

#### 4. 恢复佣金记录
```
PUT /api/v1/admin/user/commission/{id}/restore
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 佣金记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 将已禁用的佣金记录恢复为正常状态

**操作日志：** 此接口自动记录操作日志

---

### 通知管理接口 (端口: 8081)

#### 1. 分页查询通知列表
```
GET /api/v1/admin/user/notification/list
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数 |
| memberId | Long | 否 | 用户ID |
| notificationType | String | 否 | 通知类型（见类型说明），最长50字符 |
| isRead | Boolean | 否 | 是否已读 |

**通知类型说明：**
| 类型 | 说明 |
|------|------|
| member_expire_soon | 会员即将过期 |
| member_expired | 会员已过期 |
| commission_earned | 佣金到账 |
| commission_paid | 佣金已发放 |
| commission_rejected | 提现被拒绝 |
| system_notice | 系统公告 |
| member_renewed | 会员续费成功 |
| member_activation_success | 会员开通成功 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "memberId": 9876543210987654321,
        "memberName": "张*",
        "notificationType": "system_notice",
        "title": "系统升级公告",
        "content": "系统将于今晚22:00进行升级维护...",
        "isRead": false,
        "createdAt": "2026-05-06T10:00:00+08:00",
        "readAt": null
      }
    ],
    "total": 200,
    "size": 10,
    "current": 1,
    "pages": 20
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 群发系统公告
```
POST /api/v1/admin/user/notification/broadcast
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 公告标题（最多200字符） |
| content | String | 是 | 公告内容（最多5000字符） |

**请求示例：**
```json
{
  "title": "系统升级公告",
  "content": "系统将于今晚22:00进行升级维护，届时服务暂停约30分钟。"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "群发任务已提交",
  "timestamp": 1714300000000
}
```

**返回值说明：** data 为提交确认信息，实际群发为异步执行（发送给所有 active 状态用户）

**业务说明：**
- 异步执行，后台线程池批量插入通知记录
- 发送范围：所有状态为 active 且未删除的用户
- 使用 BATCH 模式批量插入，保证性能

**操作日志：** 此接口自动记录操作日志

---

#### 3. 禁用通知（软删除）
```
DELETE /api/v1/admin/user/notification/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 通知ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

#### 4. 硬删除通知（物理删除）
```
DELETE /api/v1/admin/user/notification/{id}/hard
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 通知ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复

**操作日志：** 此接口自动记录操作日志

---

#### 5. 恢复通知
```
PUT /api/v1/admin/user/notification/{id}/restore
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 通知ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 将已禁用的通知恢复为正常状态

**操作日志：** 此接口自动记录操作日志

---

### 提现管理接口 (端口: 8081)

#### 1. 分页查询提现列表
```
GET /api/v1/admin/user/withdraw/list
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数 |
| memberName | String | 否 | 用户名（模糊查询，最长50字符） |
| phone | String | 否 | 手机号（模糊查询，最长50字符） |
| wechatId | String | 否 | 微信号（精确查询，后端转盲索引，最长100字符） |
| status | String | 否 | 状态：pending/paid/rejected（最长20字符） |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "memberId": 9876543210987654321,
        "memberName": "张*",
        "phone": "138****5678",
        "wechatId": "wxid***23",
        "amount": 100.00,
        "status": "pending",
        "operatorName": null,
        "remark": null,
        "createdAt": "2026-05-06T10:00:00+08:00",
        "updatedAt": null
      }
    ],
    "total": 30,
    "size": 10,
    "current": 1,
    "pages": 3
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 查看提现微信明文
```
GET /api/v1/admin/user/withdraw/{id}/wechat
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 提现记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": "wxid_abc123456",
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口强制记录操作日志（敏感操作）

---

#### 3. 处理提现
```
PUT /api/v1/admin/user/withdraw/{id}/process
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 提现记录ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| action | String | 是 | 处理动作：paid（已支付）或 rejected（已拒绝） |
| remark | String | 否 | 备注（最多500字符） |

**请求示例（支付）：**
```json
{
  "action": "paid",
  "remark": "已通过微信转账"
}
```

**请求示例（拒绝）：**
```json
{
  "action": "rejected",
  "remark": "微信号无效，请核实后重新申请"
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

**业务规则：**
- 只能处理状态为 pending 的提现申请
- 采用 SQL 层 CAS 更新（`WHERE status = 'pending'`），保证并发安全
- 并发处理同一记录时，后到的请求返回 `400: 处理失败，该提现记录状态已变更，请刷新后重试`
- 打款（paid）：更新状态为 PAID，累加用户 `commissionTotalPaid`
- 拒绝（rejected）：更新状态为 REJECTED，将提现金额退回用户 `commissionBalance`
- 处理后自动发送通知给用户：打款发送 `commission_paid` 类型，拒绝发送 `commission_rejected` 类型

**操作日志：** 此接口自动记录操作日志

---

#### 4. 禁用提现记录（软删除）
```
DELETE /api/v1/admin/user/withdraw/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 提现记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

#### 5. 硬删除提现记录（物理删除）
```
DELETE /api/v1/admin/user/withdraw/{id}/hard
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 提现记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复

**操作日志：** 此接口自动记录操作日志

---

#### 6. 恢复提现记录
```
PUT /api/v1/admin/user/withdraw/{id}/restore
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 提现记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 将已禁用的提现记录恢复为正常状态

**操作日志：** 此接口自动记录操作日志

---

## 数据库变更

### member_orders 表（新建）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| order_no | VARCHAR(50) | 订单号（唯一） |
| member_id | BIGINT | 用户ID |
| member_name | VARCHAR(50) | 用户名 |
| phone | VARCHAR(20) | 手机号 |
| wechat_id | VARCHAR(255) | 微信号（AES加密存储） |
| wechat_id_index | VARCHAR(64) | 微信号盲索引（SHA-256哈希） |
| order_type | VARCHAR(20) | 订单类型：new/renewal |
| before_type | VARCHAR(20) | 变更前会员类型：normal/pro/vip |
| after_type | VARCHAR(20) | 变更后会员类型：normal/pro/vip |
| duration_months | INTEGER | 购买时长（月） |
| amount | DECIMAL(10,2) | 支付金额 |
| before_expire_at | TIMESTAMPTZ | 变更前到期时间 |
| after_expire_at | TIMESTAMPTZ | 变更后到期时间 |
| operator_id | BIGINT | 操作人ID |
| operator_name | VARCHAR(50) | 操作人姓名 |
| remark | VARCHAR(500) | 备注 |
| is_deleted | BOOLEAN | 软删除标记 |
| version | INTEGER | 乐观锁版本号（默认0） |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**索引：**
```sql
CREATE INDEX idx_member_orders_member_id ON member_orders(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_orders_order_no ON member_orders(order_no) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_orders_created_at ON member_orders(created_at) WHERE is_deleted = FALSE;
```

---

### t_referral_commission 表（新建）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| referrer_id | BIGINT | 推荐人ID |
| referee_id | BIGINT | 被推荐人ID |
| order_id | BIGINT | 关联订单ID |
| member_type | VARCHAR(20) | 会员类型：pro/vip |
| order_amount | DECIMAL(10,2) | 订单金额 |
| commission_rate | SMALLINT | 佣金比例（0-100） |
| commission_amount | DECIMAL(10,2) | 佣金金额 |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**索引：**
```sql
CREATE INDEX idx_commission_referrer_id ON t_referral_commission(referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_commission_referee_id ON t_referral_commission(referee_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_commission_order_id ON t_referral_commission(order_id) WHERE is_deleted = FALSE;
```

---

### t_member_notification 表（新建）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| member_id | BIGINT | 用户ID |
| notification_type | VARCHAR(50) | 通知类型（见类型说明） |
| title | VARCHAR(100) | 标题 |
| content | VARCHAR(5000) | 内容 |
| related_id | BIGINT | 关联业务ID |
| is_read | BOOLEAN | 是否已读 |
| read_at | TIMESTAMPTZ | 用户阅读时间 |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**索引：**
```sql
CREATE INDEX idx_notification_member_id ON t_member_notification(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_notification_type ON t_member_notification(notification_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_notification_is_read ON t_member_notification(is_read) WHERE is_deleted = FALSE;
```

---

### t_withdraw_record 表（新建）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| member_id | BIGINT | 用户ID |
| member_name | VARCHAR(50) | 用户名 |
| phone | VARCHAR(20) | 手机号 |
| wechat_id | VARCHAR(255) | 微信号（AES加密） |
| wechat_id_index | VARCHAR(64) | 微信号盲索引 |
| amount | DECIMAL(10,2) | 提现金额 |
| status | VARCHAR(20) | 状态：pending/paid/rejected |
| operator_id | BIGINT | 处理人ID |
| operator_name | VARCHAR(50) | 处理人姓名 |
| remark | VARCHAR(500) | 备注 |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**索引：**
```sql
CREATE INDEX idx_withdraw_member_id ON t_withdraw_record(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_status ON t_withdraw_record(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_wechat_index ON t_withdraw_record(wechat_id_index) WHERE is_deleted = FALSE;
```

---

## 枚举定义

### OrderType（订单类型）
| 值 | 说明 |
|------|------|
| new | 新开通 |
| renewal | 续费/升级 |

### MemberType（会员类型）
| 值 | 说明 |
|------|------|
| normal | 普通用户 |
| pro | 专业版会员 |
| vip | 旗舰版会员 |

### NotificationType（通知类型）
| 值 | 说明 |
|------|------|
| member_expire_soon | 会员即将过期 |
| member_expired | 会员已过期 |
| commission_earned | 佣金到账 |
| commission_paid | 佣金已发放 |
| commission_rejected | 提现被拒绝 |
| system_notice | 系统公告 |
| member_renewed | 会员续费成功 |
| member_activation_success | 会员开通成功 |

### WithdrawStatus（提现状态）
| 值 | 说明 |
|------|------|
| pending | 待处理 |
| paid | 已支付 |
| rejected | 已拒绝 |

---

## 文件清单

### haifeng-common 新增

| 文件 | 职责 |
|------|------|
| enums/OrderType.java | 订单类型枚举 |
| enums/MemberType.java | 会员类型枚举（含fromValue方法） |
| enums/NotificationType.java | 通知类型枚举 |
| enums/WithdrawStatus.java | 提现状态枚举 |
| entity/user/MemberOrder.java | 订单实体 |
| entity/user/ReferralCommission.java | 佣金记录实体 |
| entity/user/MemberNotification.java | 通知实体 |
| entity/user/WithdrawRecord.java | 提现记录实体 |
| mapper/user/MemberOrderMapper.java | 订单Mapper |
| mapper/user/ReferralCommissionMapper.java | 佣金Mapper |
| mapper/user/MemberNotificationMapper.java | 通知Mapper |
| mapper/user/WithdrawRecordMapper.java | 提现Mapper |

### haifeng-common 修改

| 文件 | 修改内容 |
|------|------|
| entity/user/Member.java | 新增 @Version 乐观锁字段 |
| entity/user/MemberOrder.java | 新增 @Version 乐观锁字段 |
| entity/user/MemberNotification.java | 新增 updatedAt 字段 |
| mapper/user/WithdrawRecordMapper.java | 新增 updateStatusCas 方法（CAS 更新提现状态） |
| util/DesensitizeUtil.java | 新增 desensitizeName 方法 |

### haifeng-admin 新增

| 文件 | 职责 |
|------|------|
| dto/user/MemberUpgradeDTO.java | 会员升级参数 |
| dto/user/OrderQueryDTO.java | 订单查询参数 |
| dto/user/CommissionQueryDTO.java | 佣金查询参数（手机号前缀匹配、名称模糊、订单号模糊） |
| dto/user/NotificationQueryDTO.java | 通知查询参数 |
| dto/user/NotificationBroadcastDTO.java | 群发公告参数 |
| dto/user/WithdrawQueryDTO.java | 提现查询参数 |
| dto/user/WithdrawProcessDTO.java | 提现处理参数 |
| vo/user/OrderListVO.java | 订单列表VO |
| vo/user/OrderDetailVO.java | 订单详情VO |
| vo/user/CommissionListVO.java | 佣金列表VO（含推荐人/被推荐人名称和手机号） |
| vo/user/NotificationListVO.java | 通知列表VO |
| vo/user/WithdrawListVO.java | 提现列表VO |
| service/user/MemberOrderService.java | 订单服务接口 |
| service/user/CommissionService.java | 佣金服务接口 |
| service/user/NotificationService.java | 通知服务接口 |
| service/user/WithdrawService.java | 提现服务接口 |
| service/impl/user/MemberOrderServiceImpl.java | 订单服务实现 |
| service/impl/user/CommissionServiceImpl.java | 佣金服务实现 |
| service/impl/user/NotificationServiceImpl.java | 通知服务实现 |
| service/impl/user/WithdrawServiceImpl.java | 提现服务实现 |
| controller/user/MemberOrderController.java | 订单管理Controller |
| controller/user/CommissionController.java | 佣金管理Controller |
| controller/user/NotificationController.java | 通知管理Controller |
| controller/user/WithdrawController.java | 提现管理Controller |

### haifeng-admin 修改

| 文件 | 修改内容 |
|------|------|
| controller/user/MemberController.java | 新增 upgrade/list/detail/updateStatus/getWechat 接口，加 @Validated、@RateLimit |
| controller/user/MemberOrderController.java | 加 @Validated、@RateLimit |
| service/user/MemberService.java | 新增 upgradeMember 方法 |
| service/impl/user/MemberServiceImpl.java | 实现会员升级、订单创建、佣金计算逻辑；加乐观锁检查、幂等防重复、SystemSettings 去重查询、VIP→PRO 降级修复 |
| service/impl/user/MemberOrderServiceImpl.java | 加乐观锁检查、硬删除外键校验、恢复校验 |

### 数据库迁移

| 文件 | 说明 |
|------|------|
| V3__create_member_tables.sql | 新增：订单、佣金、通知、提现表 |
| V32__add_optimistic_lock_member_tables.sql | 新增：t_member 和 member_orders 的 version 字段 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 乐观锁冲突 / 幂等防重复 / 外键校验失败 |
| 401 | 未登录或Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在（用户/订单/佣金/通知/提现记录不存在） |
| 429 | 请求过于频繁（频率限制） |
| 500 | 服务器内部错误 |
| 1001 | 用户不存在 |
| 1006 | 提现记录已处理，不能重复操作 |

**常见 400 错误示例：**
```json
{
  "code": 400,
  "msg": "数据已被其他人修改，请刷新后重试"
}
```
```json
{
  "code": 400,
  "msg": "该用户已有同类未完成订单，请勿重复操作"
}
```
```json
{
  "code": 400,
  "msg": "该订单存在关联的佣金记录，无法硬删除"
}
```
```json
{
  "code": 400,
  "msg": "请先禁用该佣金记录，再执行硬删除"
}
```
```json
{
  "code": 400,
  "msg": "该订单所属会员已不存在，无法恢复"
}
```
```json
{
  "code": 400,
  "msg": "处理失败，该提现记录状态已变更，请刷新后重试"
}
```

---

## 安全说明

### 敏感操作审计

以下操作强制记录操作日志：
- 会员开通/升级/续费
- 修改用户状态
- 查看用户微信明文
- 查看订单微信明文
- 禁用订单（软删除）
- 硬删除订单（物理删除）
- 恢复订单
- 禁用佣金记录（软删除）
- 硬删除佣金记录（物理删除）
- 恢复佣金记录
- 群发系统公告
- 禁用通知（软删除）
- 硬删除通知（物理删除）
- 恢复通知
- 查看提现微信明文
- 处理提现
- 禁用提现记录（软删除）
- 硬删除提现记录（物理删除）
- 恢复提现记录

### 频率限制

以下敏感接口启用 IP 级频率限制（每IP每分钟最多10次）：
- 查看用户微信明文 `GET /api/v1/admin/user/{id}/wechat`
- 查看订单微信明文 `GET /api/v1/admin/user/order/{id}/wechat`
- 查看提现微信明文 `GET /api/v1/admin/user/withdraw/{id}/wechat`

### 乐观锁保护

所有写操作（更新、删除、恢复）基于 `version` 字段实现乐观锁，数据被并发修改时返回 `400: 数据已被其他人修改，请刷新后重试`。

### 幂等防重复

会员开通/升级接口在创建订单前检查同类型未删除订单是否已存在，防止重复开单。

### 外键校验

硬删除订单前检查是否存在关联的佣金记录，有则拒绝硬删除，避免孤儿引用。

### 微信号加密存储

- 提现记录中的微信号使用 AES 加密存储
- 通过 SHA-256 盲索引支持精确查询
- 列表默认脱敏显示，明文需单独接口获取
