# 管理员登录安全加固设计文档

## 概述

为管理员登录增加多层安全防护，包括账号锁定机制、TOTP双因素认证、管理员个人中心，以及角色保护机制。

## 功能模块

| 模块 | 说明 |
|------|------|
| 账号锁定 | 连续5次密码错误锁定30分钟 |
| TOTP双因素 | 可选开启，基于Google Authenticator标准 |
| 个人中心 | 管理员自助管理个人信息和TOTP |
| 角色保护 | 超级管理员角色和默认管理员不可删除 |

---

## 1. 数据库变更

### sys_admin 表新增字段

```sql
-- 直接修改 V1__create_admin_tables.sql（未启用 Flyway）
ALTER TABLE sys_admin ADD COLUMN totp_secret VARCHAR(64);
ALTER TABLE sys_admin ADD COLUMN is_totp_enabled BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN sys_admin.totp_secret IS 'TOTP动态口令密钥(Base32编码)';
COMMENT ON COLUMN sys_admin.is_totp_enabled IS '是否已开启双因素认证';
```

### SysAdmin 实体新增字段

```java
private String totpSecret;
private Boolean totpEnabled;
```

---

## 2. Redis Key 设计

| Key | 说明 | TTL |
|-----|------|-----|
| `haifeng:admin:login:fail:{phone}` | 登录失败计数 | 30分钟 |
| `haifeng:admin:pre-auth:{token}` | TOTP预认证临时凭证，存储adminId | 2分钟 |

### RedisKeyConstant 新增

```java
public static final String ADMIN_LOGIN_FAIL_PREFIX = "haifeng:admin:login:fail:";
public static final String ADMIN_PRE_AUTH_PREFIX = "haifeng:admin:pre-auth:";

public static String getAdminLoginFailKey(String phone) {
    return ADMIN_LOGIN_FAIL_PREFIX + phone;
}

public static String getAdminPreAuthKey(String token) {
    return ADMIN_PRE_AUTH_PREFIX + token;
}
```

---

## 3. 账号锁定机制

### 逻辑流程

```
密码校验前：
1. 检查 Redis Key `haifeng:admin:login:fail:{phone}` 的值
2. 若值 >= 5，直接拒绝登录，返回 "账号已锁定，请30分钟后重试"

密码校验后：
- 密码错误 → INCR 计数器，若不存在则创建并设置 TTL 30分钟
- 密码正确 → DEL 清除计数器
```

### 错误码新增

| code | msg |
|------|-----|
| 1006 | 账号已锁定，请30分钟后重试 |
| 20001 | 需进行二次验证 |

---

## 4. TOTP 双因素认证

### 依赖库

```xml
<!-- GoogleAuth TOTP 库 -->
<dependency>
    <groupId>com.warrenstrange</groupId>
    <artifactId>googleauth</artifactId>
    <version>1.5.0</version>
</dependency>

<!-- ZXing 二维码生成 -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

### 登录流程（两步认证）

**步骤1：POST /api/v1/admin/auth/login**

```
请求: { phone, password, captchaCode, uuid }

响应A（未开启TOTP）:
{ code: 200, data: { accessToken, refreshToken, ... } }

响应B（已开启TOTP）:
{ code: 20001, msg: "需进行二次验证", data: { preAuthToken: "xxx" } }
```

**步骤2：POST /api/v1/admin/auth/login/totp**

```
请求: { preAuthToken, totpCode }

响应: { code: 200, data: { accessToken, refreshToken, ... } }
```

### TotpService 接口

```java
public interface TotpService {
    // 生成密钥（首次开启时调用）
    String generateSecret();

    // 生成二维码 Base64 图片
    String generateQrCodeBase64(String secret, String username);

    // 验证动态码
    boolean verifyCode(String secret, String code);
}
```

### 二维码生成

- 数据库只存 `totp_secret`（Base32编码，约32字符）
- 二维码不存储，每次请求时动态生成
- 格式：`otpauth://totp/海峰后台:{username}?secret={secret}&issuer=海峰后台`

---

## 5. 管理员个人中心

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/profile` | 获取当前管理员信息 |
| PUT | `/api/v1/admin/profile` | 修改个人信息 |
| PUT | `/api/v1/admin/profile/password` | 修改密码（需验证旧密码） |
| POST | `/api/v1/admin/profile/totp/enable` | 开启TOTP（生成密钥+二维码） |
| POST | `/api/v1/admin/profile/totp/verify` | 验证并确认绑定TOTP |
| POST | `/api/v1/admin/profile/totp/disable` | 关闭TOTP（需验证密码） |
| GET | `/api/v1/admin/profile/totp/qrcode` | 获取当前TOTP二维码 |

### DTO 设计

```java
// 修改个人信息
public class ProfileUpdateDTO {
    private String username;
    private String phone;
    private String email;
    private String avatar;
}

// 修改密码
public class PasswordUpdateDTO {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}

// TOTP 验证
public class TotpVerifyDTO {
    @NotBlank
    private String code;  // 6位动态码
}

// TOTP 登录
public class TotpLoginDTO {
    @NotBlank
    private String preAuthToken;
    @NotBlank
    private String totpCode;
}
```

### VO 设计

```java
// 个人信息
public class ProfileVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String roleName;
    private Boolean isTotpEnabled;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
}

// TOTP 开启响应
public class TotpEnableVO {
    private String secret;      // 用于手动输入
    private String qrCodeImage; // Base64 二维码
}

// 预认证响应
public class PreAuthVO {
    private String preAuthToken;
}
```

### 文件归属

| 类型 | 位置 |
|------|------|
| ProfileController | haifeng-admin/controller/profile/ |
| ProfileService | haifeng-admin/service/profile/ |
| ProfileServiceImpl | haifeng-admin/service/impl/profile/ |
| DTO | haifeng-admin/dto/profile/ |
| VO | haifeng-admin/vo/profile/ |
| TotpService | haifeng-common/service/ |
| TotpServiceImpl | haifeng-common/service/impl/ |

---

## 6. 角色与管理员保护

### 保护规则

| 保护对象 | 规则 |
|---------|------|
| 超级管理员角色 (id=1) | 不可删除，可修改名称/描述/权限 |
| 默认管理员 (id=1) | 不可删除，可修改信息（但不能改role_id） |

### 实现位置

**RoleServiceImpl** - 删除角色时校验：
```java
public void deleteRole(Long id) {
    if (id == 1L) {
        throw new BusinessException(400, "超级管理员角色不可删除");
    }
    // ...
}
```

**AdminServiceImpl** - 删除/修改管理员时校验：
```java
public void deleteAdmin(Long id) {
    if (id == 1L) {
        throw new BusinessException(400, "默认管理员不可删除");
    }
    // ...
}

public void updateAdmin(Long id, AdminUpdateDTO dto) {
    if (id == 1L && dto.getRoleId() != null && dto.getRoleId() != 1L) {
        throw new BusinessException(400, "默认管理员角色不可变更");
    }
    // ...
}
```

---

## 7. 安全白名单更新

SecurityConfig 中需要放行的路径：

```java
"/api/v1/admin/auth/login/totp"  // TOTP 二次验证接口
```

---

## 8. 完整登录流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                      管理员登录流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. POST /login (phone + password + captcha)                    │
│     │                                                           │
│     ├─ 验证码校验 ──────────────────────────────→ 失败返回错误    │
│     │                                                           │
│     ├─ 检查账号锁定 ─────────────────────────────→ 锁定返回1006   │
│     │                                                           │
│     ├─ 密码校验 ─────────────────────────────────→ 错误计数+1    │
│     │                                            (满5次锁定)     │
│     │                                                           │
│     ├─ 清除失败计数                                              │
│     │                                                           │
│     └─ 检查 is_totp_enabled                                     │
│         │                                                       │
│         ├─ false ──→ 返回 JWT Token (200)                       │
│         │                                                       │
│         └─ true ───→ 生成 preAuthToken 存 Redis                 │
│                      返回 code=20001 + preAuthToken              │
│                                                                 │
│  2. POST /login/totp (preAuthToken + totpCode)                  │
│     │                                                           │
│     ├─ 验证 preAuthToken ────────────────────────→ 无效返回错误  │
│     │                                                           │
│     ├─ 验证 TOTP 动态码 ─────────────────────────→ 错误返回错误  │
│     │                                                           │
│     └─ 返回 JWT Token (200)                                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 9. 文件变更清单

### haifeng-common（新增/修改）

| 操作 | 文件 |
|------|------|
| 修改 | pom.xml - 添加 googleauth、zxing 依赖 |
| 修改 | constant/RedisKeyConstant.java - 新增 Key |
| 修改 | response/ResultCode.java - 新增错误码 |
| 修改 | entity/permission/SysAdmin.java - 新增 TOTP 字段 |
| 新增 | service/TotpService.java |
| 新增 | service/impl/TotpServiceImpl.java |

### haifeng-admin（新增/修改）

| 操作 | 文件 |
|------|------|
| 修改 | V1__create_admin_tables.sql - 新增字段 |
| 修改 | controller/auth/AdminAuthController.java - 新增 TOTP 登录接口 |
| 修改 | service/impl/auth/AdminAuthServiceImpl.java - 账号锁定+TOTP流程 |
| 修改 | service/impl/permission/RoleServiceImpl.java - 角色保护 |
| 修改 | service/impl/permission/AdminServiceImpl.java - 管理员保护 |
| 新增 | controller/profile/ProfileController.java |
| 新增 | service/profile/ProfileService.java |
| 新增 | service/impl/profile/ProfileServiceImpl.java |
| 新增 | dto/profile/ProfileUpdateDTO.java |
| 新增 | dto/profile/PasswordUpdateDTO.java |
| 新增 | dto/profile/TotpVerifyDTO.java |
| 新增 | dto/auth/TotpLoginDTO.java |
| 新增 | vo/profile/ProfileVO.java |
| 新增 | vo/profile/TotpEnableVO.java |
| 新增 | vo/auth/PreAuthVO.java |
