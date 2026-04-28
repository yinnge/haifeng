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

### 3. 权限等级说明

| 等级 | 身份 | 说明 | 可访问资源 |
|------|------|------|-----------|
| 0 | 游客 | 未登录用户 | 公开接口（登录、注册、刷新Token） |
| 1 | 普通会员 | member_type = normal | 基础功能、个人中心 |
| 2 | VIP会员 | member_type = vip 且未过期 | 全部功能、高级分析、AI咨询 |
| 3 | 管理员 | userType = admin | 管理后台、用户管理、数据管理 |

**权限校验：**
- `@RequireLogin` - 需要登录（等级 >= 1）
- `@RequireLogin(userType = "admin")` - 需要管理员身份（等级 = 3）
- `@RequireVip` - 需要VIP会员（等级 = 2）

---

## 完成功能概述

### 1. 公共模块扩展 (haifeng-common)
- JWT 工具类扩展：支持双端认证（admin/member）和会员类型（normal/vip）
- Redis Key 常量类：统一管理 Token 存储键
- 安全工具类 SecurityUtil：获取当前登录用户信息
- 权限注解：`@RequireLogin`、`@RequireVip`
- AOP 切面：自动校验登录状态和 VIP 权限
- JWT 过滤器：自动解析请求头中的 Token 并设置认证信息

### 2. 管理端认证 (haifeng-admin)
- 数据库表：sys_role、sys_module、sys_role_module、sys_admin、admin_logs
- 完整认证流程：登录、刷新 Token、登出

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

#### 1. 管理员登录
```
POST /api/v1/admin/auth/login
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 (2-50字符) |
| password | String | 是 | 密码 (6-100字符) |

**请求示例：**
```json
{
  "username": "admin",
  "password": "123456"
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

#### 2. 刷新 Token
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

#### 3. 管理员登出
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

### 用户端接口 (端口: 8080)

#### 1. 用户注册
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

#### 2. 用户登录
```
POST /api/v1/app/auth/login
Content-Type: application/json
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 (2-50字符) |
| password | String | 是 | 密码 (6-100字符) |

**请求示例：**
```json
{
  "username": "testuser",
  "password": "123456"
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

#### 3. 刷新 Token
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

#### 4. 用户登出
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
| 400 | 参数错误 / 用户名已存在 / 手机号已存在 / 邀请码无效 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 / 账号已禁用 |
| 1001 | 用户不存在 |
| 1002 | 密码错误 |
| 1004 | 权限不足（非VIP） |

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
  "memberType": "vip",       // normal 或 vip（仅 member 有）
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

### @RequireVip - 需要 VIP
```java
@RequireVip
@GetMapping("/vip-feature")
public R<Void> vipFeature() {
    // 仅 VIP 会员可访问
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
| member_type | VARCHAR(20) | 会员类型（normal/vip） |
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
- `pom.xml` - 添加 AOP 依赖
- `util/JwtUtil.java` - JWT 工具类（扩展）
- `constant/RedisKeyConstant.java` - Redis Key 常量
- `dto/LoginDTO.java` - 登录请求 DTO
- `dto/RefreshTokenDTO.java` - 刷新 Token 请求 DTO
- `vo/TokenVO.java` - Token 响应 VO
- `security/AuthUser.java` - 自定义 UserDetails
- `util/SecurityUtil.java` - 安全工具类
- `annotation/RequireLogin.java` - 登录注解
- `annotation/RequireVip.java` - VIP 注解
- `aspect/AuthAspect.java` - 权限切面
- `security/JwtAuthenticationFilter.java` - JWT 过滤器
- `config/SecurityConfig.java` - 安全配置（修改）

### haifeng-admin (管理端)
- `pom.xml` - 添加 Lombok 依赖
- `db/migration/V1__create_admin_tables.sql` - 数据库迁移
- `entity/SysRole.java` - 角色实体
- `entity/SysModule.java` - 模块实体
- `entity/SysRoleModule.java` - 角色模块关联实体
- `entity/SysAdmin.java` - 管理员实体（含 role_name）
- `entity/AdminLog.java` - 操作日志实体
- `mapper/SysAdminMapper.java` - 管理员 Mapper
- `service/AdminAuthService.java` - 认证服务接口
- `service/impl/AdminAuthServiceImpl.java` - 认证服务实现
- `controller/AdminAuthController.java` - 认证控制器

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
