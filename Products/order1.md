# 业务认证基础层实施报告

## 快速入门

### 1. Token 使用方式
所有需要认证的接口，请在请求头中添加：
```
Authorization: Bearer {accessToken}
```

### 2. 双 Token 刷新流程

```
┌──────────┐                              ┌──────────┐
│  客户端   │                              │  服务端   │
└────┬─────┘                              └────┬─────┘
     │                                         │
     │  1. 登录请求 (username + password)      │
     │ ─────────────────────────────────────> │
     │                                         │
     │  2. 返回 AccessToken + RefreshToken     │
     │ <───────────────────────────────────── │
     │                                         │
     │  3. 携带 AccessToken 访问业务接口        │
     │ ─────────────────────────────────────> │
     │                                         │
     │  4. AccessToken 过期 (2小时后)           │
     │      返回 401 Unauthorized              │
     │ <───────────────────────────────────── │
     │                                         │
     │  5. 使用 RefreshToken 刷新              │
     │ ─────────────────────────────────────> │
     │                                         │
     │  6. 返回新的 AccessToken + RefreshToken │
     │ <───────────────────────────────────── │
     │                                         │
     │  7. RefreshToken 也过期 (7天后)          │
     │      返回 401，需要重新登录              │
     │ <───────────────────────────────────── │
     │                                         │
```

**刷新策略：**
- AccessToken 过期时，使用 RefreshToken 调用 `/refresh` 接口获取新 Token
- 每次刷新会同时返回新的 AccessToken 和 RefreshToken
- RefreshToken 过期后需要重新登录

### 3. 用户身份说明

| 身份 | member_type | 访问范围 |
|------|-------------|---------|
| 未登录 | - | 公开接口（Security 白名单配置） |
| 普通用户 | `normal` | 基础功能（需登录） |
| Pro用户 | `pro` | 中级功能（专业版） |
| VIP用户 | `vip` | 全部功能（旗舰版） |
| 管理员 | userType=admin | 后台管理 |

**权限注解：**
- 无注解 + Security 白名单 → 公开接口，无需登录
- `@RequireLogin` → 需要登录（任意 member_type）
- `@RequireLogin(userType = "admin")` → 仅管理员
- `@RequirePro` → 需要 Pro 或 VIP
- `@RequireVip` → 仅 VIP

---

## 完成功能概述

### 1. 公共模块扩展 (haifeng-common)
- JWT 工具类扩展：支持双端认证（admin/member）和会员类型（normal/pro/vip）
- Redis Key 常量类：统一管理 Token 存储键、验证码键、登录失败计数键、预认证键
- 安全工具类 SecurityUtil：获取当前登录用户信息
- 权限注解：`@RequireLogin`、`@RequirePro`、`@RequireVip`
- AOP 切面：自动校验登录状态和 VIP 权限
- JWT 过滤器：自动解析请求头中的 Token 并设置认证信息
- **图形验证码服务**：基于 easy-captcha，Redis 存储（2分钟过期），防暴力破解
- **TOTP 双因素认证服务**：基于 GoogleAuth + ZXing，支持动态二维码生成

### 2. 管理端认证 (haifeng-admin)
- 数据库表：sys_role、sys_module、sys_role_module、sys_admin（含 TOTP 字段）、admin_logs
- 完整认证流程：登录（含账号锁定、TOTP二次验证）、刷新 Token、登出
- **账号锁定机制**：连续5次密码错误锁定30分钟
- **TOTP 双因素认证**：可选开启，兼容 Google Authenticator
- **管理员个人中心**：查看/修改个人信息、修改密码、TOTP 管理
- **角色保护**：超级管理员角色（id=1）不可删除
- **管理员保护**：默认管理员（id=1）不可删除且角色不可变更

### 3. 用户端认证 (haifeng-app)
- 数据库表：t_member、t_member_profile
- 完整认证流程：注册、登录、刷新 Token、登出
- **邀请码自动生成**（数据库触发器）
- **推荐关系绑定**（注册时通过邀请码绑定推荐人）
- **佣金字段**（commission_balance 等）
- VIP 过期自动检测

---

## API 接口文档

### 管理端接口 (端口: 8081)

#### 1. 获取验证码
```
GET /api/v1/admin/auth/captcha
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAI..."
  },
  "timestamp": 1714300000000
}
```

**说明：**
- 验证码图片为 Base64 编码，前端直接使用 `<img src="{image}">`
- uuid 用于登录时关联验证码
- 验证码有效期 2 分钟，一次性使用

---

#### 2. 管理员登录
```
POST /api/v1/admin/auth/login
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码（数字+字母，6-16位） |
| captchaCode | String | 是 | 用户输入的验证码 |
| uuid | String | 是 | 验证码标识（从 captcha 接口获取） |

**请求示例：**
```json
{
  "phone": "13800138000",
  "password": "abc123456",
  "captchaCode": "A4B9",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应示例（未开启TOTP）：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpire": 7200,
    "refreshTokenExpire": 604800,
    "tokenType": "Bearer"
  },
  "timestamp": 1714300000000
}
```

**响应示例（已开启TOTP，需二次验证）：**
```json
{
  "code": 20001,
  "msg": "需进行二次验证",
  "data": {
    "preAuthToken": "550e8400-e29b-41d4-a716-446655440000"
  },
  "timestamp": 1714300000000
}
```

**说明：**
- 连续5次密码错误将锁定账号30分钟
- 如果管理员开启了 TOTP，返回 code=20001，需调用 TOTP 登录接口

---

#### 3. TOTP 二次验证登录
```
POST /api/v1/admin/auth/login/totp
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| preAuthToken | String | 是 | 预认证令牌（从登录接口获取） |
| totpCode | String | 是 | 6位动态验证码（从手机 App 获取） |

**请求示例：**
```json
{
  "preAuthToken": "550e8400-e29b-41d4-a716-446655440000",
  "totpCode": "123456"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpire": 7200,
    "refreshTokenExpire": 604800,
    "tokenType": "Bearer"
  },
  "timestamp": 1714300000000
}
```

**说明：**
- preAuthToken 有效期 2 分钟，过期需重新登录
- totpCode 为 Google Authenticator 等 App 显示的 6 位数字

---

#### 5. 刷新 Token
```
POST /api/v1/admin/auth/refresh
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

**请求示例：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpire": 7200,
    "refreshTokenExpire": 604800,
    "tokenType": "Bearer"
  },
  "timestamp": 1714300000000
}
```

---

#### 6. 管理员登出
```
POST /api/v1/admin/auth/logout
Authorization: Bearer {accessToken}
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

### 管理员个人中心接口 (端口: 8081)

#### 1. 获取当前管理员信息
```
GET /api/v1/admin/profile
Authorization: Bearer {accessToken}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "超级管理员",
    "phone": "13800000000",
    "email": null,
    "avatar": null,
    "roleName": "超级管理员",
    "isTotpEnabled": false,
    "lastLoginAt": "2026-05-08T10:00:00+08:00",
    "createdAt": "2026-01-01T00:00:00+08:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 修改个人信息
```
PUT /api/v1/admin/profile
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 否 | 用户名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| avatar | String | 否 | 头像URL |

**请求示例：**
```json
{
  "username": "newadmin",
  "email": "admin@example.com"
}
```

---

#### 3. 修改密码
```
PUT /api/v1/admin/profile/password
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**请求示例：**
```json
{
  "oldPassword": "123456",
  "newPassword": "newPassword123"
}
```

---

#### 4. 开启 TOTP（生成密钥和二维码）
```
POST /api/v1/admin/profile/totp/enable
Authorization: Bearer {accessToken}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAM..."
  },
  "timestamp": 1714300000000
}
```

**说明：**
- `secret` 用于手动输入到认证 App
- `qrCodeImage` 为 Base64 编码的二维码图片，前端直接展示供用户扫描
- 此时 TOTP 尚未生效，需调用验证接口确认绑定

---

#### 5. 验证并确认绑定 TOTP
```
POST /api/v1/admin/profile/totp/verify
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 6位动态验证码 |

**请求示例：**
```json
{
  "code": "123456"
}
```

**说明：**
- 验证成功后 TOTP 正式启用
- 下次登录将需要进行二次验证

---

#### 6. 关闭 TOTP
```
POST /api/v1/admin/profile/totp/disable
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| password | String | 是 | 当前密码（安全验证） |

**请求示例：**
```json
{
  "password": "123456"
}
```

---

#### 7. 获取当前 TOTP 二维码
```
GET /api/v1/admin/profile/totp/qrcode
Authorization: Bearer {accessToken}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAM..."
  },
  "timestamp": 1714300000000
}
```

**说明：**
- 仅在 TOTP 已开启时可用
- 用于更换手机时重新绑定

---

### 用户端接口 (端口: 8080)

#### 1. 获取验证码
```
GET /api/v1/app/auth/captcha
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAI..."
  },
  "timestamp": 1714300000000
}
```

**说明：**
- 验证码图片为 Base64 编码，前端直接使用 `<img src="{image}">`
- uuid 用于登录时关联验证码
- 验证码有效期 2 分钟，一次性使用

---

#### 2. 用户注册
```
POST /api/v1/app/auth/register
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 (2-50字符) |
| password | String | 是 | 密码 (6-100字符) |
| phone | String | 否 | 手机号 (最长20字符) |
| referrerCode | String | 否 | 推荐人邀请码 (最长20字符) |

**请求示例：**
```json
{
  "username": "testuser",
  "password": "123456",
  "phone": "13800138000",
  "referrerCode": "ABC12345"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpire": 7200,
    "refreshTokenExpire": 604800,
    "tokenType": "Bearer"
  },
  "timestamp": 1714300000000
}
```

**说明：**
- 注册成功后，系统自动为用户生成唯一邀请码（8位字母数字）
- 如果提供了有效的 referrerCode，会自动绑定推荐关系

---

#### 3. 用户登录
```
POST /api/v1/app/auth/login
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码（数字+字母，6-16位） |
| captchaCode | String | 是 | 用户输入的验证码 |
| uuid | String | 是 | 验证码标识（从 captcha 接口获取） |

**请求示例：**
```json
{
  "phone": "13800138000",
  "password": "abc123456",
  "captchaCode": "A4B9",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpire": 7200,
    "refreshTokenExpire": 604800,
    "tokenType": "Bearer"
  },
  "timestamp": 1714300000000
}
```

---

#### 4. 刷新 Token
```
POST /api/v1/app/auth/refresh
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | String | 是 | 刷新令牌 |

**请求示例：**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpire": 7200,
    "refreshTokenExpire": 604800,
    "tokenType": "Bearer"
  },
  "timestamp": 1714300000000
}
```

---

#### 5. 用户登出

```
POST /api/v1/app/auth/logout
Authorization: Bearer {accessToken}
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

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 用户名已存在 / 手机号已存在 / 邀请码无效 / 验证码错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 / 账号已禁用 |
| 1001 | 用户不存在 |
| 1002 | 密码错误 |
| 1003 | 会员已过期 |
| 1004 | 权限不足（需要专业版及以上） |
| 1005 | 权限不足（需要旗舰版） |
| 1006 | 账号已锁定，请30分钟后重试 |
| 20001 | 需进行二次验证（TOTP） |

---

## 默认账号

### 管理员账号
| 字段 | 值 |
|------|------|
| 用户名 | admin |
| 密码 | 123456 |
| 角色 | 超级管理员 |

---

## Token 说明

### Token 结构
```json
{
  "userId": 123456789,
  "userType": "admin",       // admin 或 member
  "memberType": "vip",       // normal/pro/vip（仅 member 有）
  "tokenType": "access",     // access 或 refresh
  "iat": 1234567890,
  "exp": 1234567890
}
```

### Token 有效期
| 类型 | 有效期 |
|------|--------|
| AccessToken | 2 小时 (7200秒) |
| RefreshToken | 7 天 (604800秒) |

---

## 邀请码系统

### 邀请码规则
- 长度：8位
- 字符集：`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`（排除易混淆的 0/O/I/1）
- 生成时机：用户注册时由数据库触发器自动生成
- 唯一性：数据库层面保证唯一

### 推荐关系
- 注册时通过 `referrerCode` 参数绑定推荐人
- 绑定后记录：`referrer_id`（推荐人ID）、`referrer_username`（推荐人用户名）
- 佣金字段：`commission_balance`（可提现）、`commission_total_earned`（累计获得）、`commission_total_paid`（累计已发放）

---

## TOTP 双因素认证

### 功能说明
TOTP（Time-based One-Time Password）是基于时间的一次性密码，兼容 Google Authenticator、Microsoft Authenticator 等主流认证 App。

### 管理员开启流程
1. 登录后进入个人中心
2. 调用 `POST /api/v1/admin/profile/totp/enable` 获取密钥和二维码
3. 使用手机 App 扫描二维码
4. 输入 App 显示的 6 位数字，调用 `POST /api/v1/admin/profile/totp/verify` 确认绑定
5. 绑定成功后，下次登录需进行二次验证

### 登录流程（已开启 TOTP）
```
┌──────────┐                              ┌──────────┐
│  客户端   │                              │  服务端   │
└────┬─────┘                              └────┬─────┘
     │                                         │
     │  1. 登录请求 (phone + password + captcha)│
     │ ─────────────────────────────────────> │
     │                                         │
     │  2. 返回 code=20001 + preAuthToken      │
     │ <───────────────────────────────────── │
     │                                         │
     │  3. 弹出 TOTP 输入框，用户输入 6 位数字   │
     │                                         │
     │  4. 调用 /login/totp (preAuthToken + code)│
     │ ─────────────────────────────────────> │
     │                                         │
     │  5. 返回 AccessToken + RefreshToken     │
     │ <───────────────────────────────────── │
```

### 重置 TOTP
- 管理员手机丢失或更换时，需由超级管理员在管理员列表中关闭其 TOTP
- 关闭后该管理员可重新登录并再次开启 TOTP

### 技术实现
- 密钥生成：GoogleAuth 库（Base32 编码）
- 二维码生成：ZXing 库（200x200 PNG）
- 存储：`sys_admin.totp_secret`（密钥）、`sys_admin.is_totp_enabled`（是否启用）
- 预认证：Redis 存储（2分钟过期），Key 格式 `haifeng:admin:pre-auth:{token}`

---

## 账号锁定机制

### 功能说明
防止暴力破解攻击，连续输错密码 5 次后锁定账号 30 分钟。

### 实现规则
- 每次密码错误：Redis 计数器 +1
- 首次错误时设置 30 分钟过期时间
- 计数器达到 5 次：拒绝登录，返回错误码 1006
- 密码正确：清除计数器
- 30 分钟后计数器自动过期，账号解锁

### Redis Key
- 格式：`haifeng:admin:login:fail:{phone}`
- 值：失败次数（Integer）
- TTL：30 分钟

---

## 权限注解使用

### @RequireLogin - 需要登录
```java
@RequireLogin
@GetMapping("/profile")
public R<UserVO> getProfile() {
    // 需要登录才能访问
}

@RequireLogin(userType = "admin")
@GetMapping("/admin-only")
public R<Void> adminOnly() {
    // 仅管理员可访问
}
```

### @RequirePro - 需要 Pro 及以上
```java
@RequirePro
@GetMapping("/pro-feature")
public R<Void> proFeature() {
    // Pro会员 或 VIP会员 可访问
}
```

### @RequireVip - 需要 VIP
```java
@RequireVip
@GetMapping("/vip-feature")
public R<Void> vipFeature() {
    // 仅 VIP 会员可访问（旗舰版专属）
}
```

---

## 数据库表结构

### t_member (会员表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 会员ID（雪花算法） |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(100) | 密码（BCrypt） |
| phone | VARCHAR(20) | 手机号（唯一） |
| avatar | VARCHAR(500) | 头像URL |
| invite_code | VARCHAR(20) | 邀请码（唯一，自动生成） |
| member_type | VARCHAR(20) | 会员类型（normal/pro/vip） |
| expire_at | TIMESTAMPTZ | 会员到期时间 |
| referrer_id | BIGINT | 推荐人ID |
| referrer_username | VARCHAR(50) | 推荐人用户名 |
| commission_balance | DECIMAL(10,2) | 可提现余额 |
| commission_total_earned | DECIMAL(10,2) | 累计获得佣金 |
| commission_total_paid | DECIMAL(10,2) | 累计已发放佣金 |
| status | VARCHAR(20) | 状态（active/disabled） |

### t_member_profile (用户资料表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 资料ID |
| member_id | BIGINT | 会员ID（唯一） |
| real_name | VARCHAR(50) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| identity | VARCHAR(100) | 身份（如：北京大学） |
| province | VARCHAR(30) | 省份 |
| city | VARCHAR(50) | 城市 |
| major | VARCHAR(100) | 专业 |
| grade | VARCHAR(20) | 年级 |
| education_level | VARCHAR(20) | 学历层次 |

---

## 文件清单

### haifeng-common (公共模块)
- `pom.xml` - 添加 AOP、easy-captcha、googleauth、zxing 依赖
- `util/JwtUtil.java` - JWT 工具类（扩展）
- `constant/RedisKeyConstant.java` - Redis Key 常量（含验证码Key、登录失败Key、预认证Key）
- `response/ResultCode.java` - 错误码枚举（含 ACCOUNT_LOCKED、TOTP_REQUIRED）
- `response/R.java` - 统一响应类（含带数据的 fail 方法）
- `dto/auth/LoginDTO.java` - 登录请求 DTO（含 captchaCode、uuid）
- `dto/auth/RefreshTokenDTO.java` - 刷新 Token 请求 DTO
- `vo/auth/TokenVO.java` - Token 响应 VO
- `vo/auth/CaptchaVO.java` - 验证码响应 VO（uuid + Base64图片）
- `service/CaptchaService.java` - 验证码服务接口
- `service/impl/CaptchaServiceImpl.java` - 验证码服务实现
- `service/TotpService.java` - TOTP 双因素认证服务接口
- `service/impl/TotpServiceImpl.java` - TOTP 服务实现（GoogleAuth + ZXing）
- `security/AuthUser.java` - 自定义 UserDetails
- `util/SecurityUtil.java` - 安全工具类
- `annotation/RequireLogin.java` - 登录注解
- `annotation/RequirePro.java` - Pro会员注解（专业版及以上）
- `annotation/RequireVip.java` - VIP 注解（旗舰版）
- `aspect/AuthAspect.java` - 权限切面
- `security/JwtAuthenticationFilter.java` - JWT 过滤器
- `config/SecurityConfig.java` - 安全配置（含 captcha、totp 白名单）
- `entity/permission/SysAdmin.java` - 管理员实体（含 totpSecret、totpEnabled）

### haifeng-admin (管理端)
- `pom.xml` - 添加 Lombok 依赖
- `db/migration/V1__create_admin_tables.sql` - 数据库迁移（含 TOTP 字段）
- `entity/SysRole.java` - 角色实体
- `entity/SysModule.java` - 模块实体
- `entity/SysRoleModule.java` - 角色模块关联实体
- `entity/SysAdmin.java` - 管理员实体（含 role_name、totp_secret、is_totp_enabled）
- `entity/AdminLog.java` - 操作日志实体
- `mapper/SysAdminMapper.java` - 管理员 Mapper
- `service/auth/AdminAuthService.java` - 认证服务接口（含 loginWithTotp）
- `service/impl/auth/AdminAuthServiceImpl.java` - 认证服务实现（含账号锁定、TOTP）
- `controller/auth/AdminAuthController.java` - 认证控制器（含 TOTP 登录接口）
- `dto/auth/TotpLoginDTO.java` - TOTP 登录请求 DTO
- `vo/auth/PreAuthVO.java` - 预认证响应 VO
- `service/profile/ProfileService.java` - 个人中心服务接口
- `service/impl/profile/ProfileServiceImpl.java` - 个人中心服务实现
- `controller/profile/ProfileController.java` - 个人中心控制器
- `dto/profile/ProfileUpdateDTO.java` - 修改个人信息 DTO
- `dto/profile/PasswordUpdateDTO.java` - 修改密码 DTO
- `dto/profile/TotpVerifyDTO.java` - TOTP 验证 DTO
- `dto/profile/TotpDisableDTO.java` - 关闭 TOTP DTO
- `vo/profile/ProfileVO.java` - 个人信息 VO
- `vo/profile/TotpEnableVO.java` - TOTP 开启响应 VO
- `service/impl/permission/RoleServiceImpl.java` - 角色服务（含角色保护）
- `service/impl/permission/AdminServiceImpl.java` - 管理员服务（含管理员保护）

### haifeng-app (用户端)
- `pom.xml` - 添加 Lombok 依赖
- `db/migration/V1__create_member_tables.sql` - 数据库迁移（含邀请码触发器）
- `entity/Member.java` - 会员实体（含邀请码、推荐关系、佣金字段）
- `entity/MemberProfile.java` - 会员资料实体
- `mapper/MemberMapper.java` - 会员 Mapper
- `dto/RegisterDTO.java` - 注册请求 DTO（含 referrerCode）
- `service/AppAuthService.java` - 认证服务接口
- `service/impl/AppAuthServiceImpl.java` - 认证服务实现（含推荐关系绑定）
- `controller/AppAuthController.java` - 认证控制器

---

## 权限管理模块接口

### 角色管理

#### 1. 角色列表（分页）
```
GET /api/v1/admin/permission/roles
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| roleName | String | 否 | 角色名称（模糊搜索） |
| status | Integer | 否 | 状态（0=禁用，1=启用） |

---

#### 2. 角色详情
```
GET /api/v1/admin/permission/roles/{id}
```

---

#### 3. 新增角色
```
POST /api/v1/admin/permission/roles
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 是 | 角色名称（最长50字符） |
| roleCode | String | 是 | 角色编码（最长50字符，唯一） |
| description | String | 否 | 描述（最长100字符） |

**请求示例：**
```json
{
  "roleName": "内容编辑",
  "roleCode": "content_editor",
  "description": "负责内容审核和编辑"
}
```

---

#### 4. 更新角色
```
PUT /api/v1/admin/permission/roles/{id}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 是 | 角色名称（最长50字符） |
| roleCode | String | 是 | 角色编码（最长50字符，唯一） |
| description | String | 否 | 描述（最长100字符） |

**请求示例：**
```json
{
  "roleName": "高级编辑",
  "roleCode": "senior_editor",
  "description": "高级内容编辑，拥有更多权限"
}
```

---

#### 5. 删除角色（硬删除）
```
DELETE /api/v1/admin/permission/roles/{id}
```
**说明：** 从数据库彻底删除角色记录，不可恢复

---

#### 6. 切换角色状态（禁用/启用）
```
PUT /api/v1/admin/permission/roles/{id}/toggle-status
```
**说明：** 禁用时启用，启用时禁用（status: 0↔1）

---

#### 7. 角色绑定模块
```
POST /api/v1/admin/permission/roles/{id}/modules
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| moduleIds | Long[] | 是 | 模块ID列表 |

**请求示例：**
```json
{
  "moduleIds": [1, 2, 3, 5, 6]
}
```

**说明：**
- 如果传入的是父模块ID（level=1），会自动关联其所有子模块
- 每次调用会先清除该角色的所有旧关联，再建立新关联

---

### 管理员管理

#### 1. 管理员列表（分页）
```
GET /api/v1/admin/permission/admins
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |
| username | String | 否 | 用户名（模糊搜索） |
| phone | String | 否 | 手机号（模糊搜索） |
| realName | String | 否 | 真实姓名（模糊搜索） |
| status | Integer | 否 | 状态（0=禁用，1=启用） |

---

#### 2. 管理员详情
```
GET /api/v1/admin/permission/admins/{id}
```

---

#### 3. 新增管理员
```
POST /api/v1/admin/permission/admins
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（2-50字符） |
| password | String | 是 | 密码（数字+字母，6-16位） |
| realName | String | 否 | 真实姓名（最长50字符） |
| phone | String | 是 | 手机号（11位） |
| email | String | 否 | 邮箱（最长100字符） |
| avatar | String | 否 | 头像URL（最长500字符） |
| roleId | Long | 是 | 角色ID |

**请求示例：**
```json
{
  "username": "zhangsan",
  "password": "Admin123",
  "realName": "张三",
  "phone": "13812345678",
  "email": "zhangsan@example.com",
  "roleId": 2
}
```

---

#### 4. 更新管理员
```
PUT /api/v1/admin/permission/admins/{id}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（2-50字符） |
| password | String | 否 | 密码（数字+字母，6-16位，不传则不修改） |
| realName | String | 否 | 真实姓名（最长50字符） |
| phone | String | 是 | 手机号（11位） |
| email | String | 否 | 邮箱（最长100字符） |
| avatar | String | 否 | 头像URL（最长500字符） |
| roleId | Long | 是 | 角色ID |
| status | Integer | 否 | 状态（0=禁用，1=启用） |

**请求示例：**
```json
{
  "username": "zhangsan",
  "realName": "张三丰",
  "phone": "13812345678",
  "email": "zhangsan@example.com",
  "roleId": 2,
  "status": 1
}
```

**注意：** 默认管理员（id=1）的角色不可变更

---

#### 5. 删除管理员（硬删除）
```
DELETE /api/v1/admin/permission/admins/{id}
```
**说明：** 从数据库彻底删除管理员记录，不可恢复

---

#### 6. 切换管理员状态（禁用/启用）
```
PUT /api/v1/admin/permission/admins/{id}/toggle-status
```
**说明：** 禁用时启用，启用时禁用（status: 0↔1）

---

### 模块管理

#### 1. 模块列表（树形）
```
GET /api/v1/admin/permission/modules
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| moduleCode | String | 否 | 模块编码（精确匹配） |

**说明：** 返回树形结构，无分页

---

#### 2. 新增模块
```
POST /api/v1/admin/permission/modules
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| moduleName | String | 是 | 模块名称（最长50字符） |
| moduleCode | String | 是 | 模块编码（最长50字符，唯一） |
| parentId | Long | 否 | 父模块ID（子模块必填） |
| path | String | 否 | 路由路径（最长200字符） |
| icon | String | 否 | 图标（最长50字符） |
| sortOrder | Integer | 否 | 排序（默认0，数值越小越靠前） |
| level | Integer | 是 | 层级（1=父模块，2=子模块） |
| description | String | 否 | 描述（最长255字符） |

**请求示例：**
```json
{
  "moduleName": "用户管理",
  "moduleCode": "user_management",
  "parentId": null,
  "path": "/admin/user",
  "icon": "user",
  "sortOrder": 4,
  "level": 1,
  "description": "用户管理模块"
}
```

---

#### 3. 更新模块
```
PUT /api/v1/admin/permission/modules/{id}
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| moduleName | String | 是 | 模块名称（最长50字符） |
| moduleCode | String | 是 | 模块编码（最长50字符，唯一） |
| parentId | Long | 否 | 父模块ID |
| path | String | 否 | 路由路径（最长200字符） |
| icon | String | 否 | 图标（最长50字符） |
| sortOrder | Integer | 否 | 排序 |
| level | Integer | 是 | 层级（1=父模块，2=子模块） |
| description | String | 否 | 描述（最长255字符） |

**请求示例：**
```json
{
  "moduleName": "用户管理",
  "moduleCode": "user_mgmt",
  "path": "/admin/users",
  "icon": "users",
  "sortOrder": 3,
  "level": 1,
  "description": "用户管理相关功能"
}
```

---

#### 4. 删除模块（硬删除）
```
DELETE /api/v1/admin/permission/modules/{id}
```
**说明：** 从数据库彻底删除模块记录，不可恢复

---

#### 5. 切换模块状态（禁用/启用）
```
PUT /api/v1/admin/permission/modules/{id}/toggle-status
```
**说明：** 禁用时启用，启用时禁用（status: 0↔1）

---

## 列表操作按钮说明

所有列表右侧都有三个操作按钮：

| 按钮 | 接口 | 说明 |
|------|------|------|
| 删除 | `DELETE /{id}` | 硬删除，从数据库彻底删除，不可恢复 |
| 禁用/启用 | `PUT /{id}/toggle-status` | 切换状态（status: 0↔1），可恢复 |
| 详情 | `GET /{id}` | 查看详情并支持修改 |

---

## 模糊搜索说明

以下字段支持模糊搜索（LIKE %keyword%）：

| 模块 | 支持模糊搜索的字段 |
|------|------------------|
| 角色列表 | roleName（角色名称） |
| 管理员列表 | username（用户名）、phone（手机号）、realName（真实姓名） |
| 会员列表 | username（用户名）、phone（手机号） |

**精确匹配字段：**
- 模块列表：moduleCode（模块编码）
- 所有 status 字段：0 或 1
