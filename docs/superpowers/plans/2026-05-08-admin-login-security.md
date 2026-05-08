# 管理员登录安全加固实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为管理员登录增加账号锁定、TOTP双因素认证、个人中心和角色保护功能

**Architecture:** 基于现有 JWT 认证架构，新增 Redis 存储登录失败计数和预认证凭证，使用 GoogleAuth 库实现 TOTP，ZXing 生成二维码。登录流程分两步：密码验证 → TOTP验证（如已开启）。

**Tech Stack:** Spring Boot 3.x, GoogleAuth 1.5.0, ZXing 3.5.2, Redis, PostgreSQL

---

## 文件结构

### haifeng-common 新增/修改

| 操作 | 文件 | 职责 |
|------|------|------|
| 修改 | pom.xml | 添加 googleauth、zxing 依赖 |
| 修改 | constant/RedisKeyConstant.java | 新增登录失败、预认证 Key |
| 修改 | response/ResultCode.java | 新增 ACCOUNT_LOCKED、TOTP_REQUIRED 错误码 |
| 修改 | entity/permission/SysAdmin.java | 新增 totpSecret、totpEnabled 字段 |
| 新增 | service/TotpService.java | TOTP 服务接口 |
| 新增 | service/impl/TotpServiceImpl.java | TOTP 服务实现 |

### haifeng-admin 新增/修改

| 操作 | 文件 | 职责 |
|------|------|------|
| 修改 | resources/db/migration/V1__create_admin_tables.sql | 新增 TOTP 字段 |
| 修改 | controller/auth/AdminAuthController.java | 新增 TOTP 登录接口 |
| 修改 | service/auth/AdminAuthService.java | 新增 TOTP 登录方法签名 |
| 修改 | service/impl/auth/AdminAuthServiceImpl.java | 账号锁定 + TOTP 流程 |
| 修改 | service/impl/permission/RoleServiceImpl.java | 角色保护 |
| 修改 | service/impl/permission/AdminServiceImpl.java | 管理员保护 |
| 新增 | dto/auth/TotpLoginDTO.java | TOTP 登录请求 |
| 新增 | vo/auth/PreAuthVO.java | 预认证响应 |
| 新增 | controller/profile/ProfileController.java | 个人中心控制器 |
| 新增 | service/profile/ProfileService.java | 个人中心服务接口 |
| 新增 | service/impl/profile/ProfileServiceImpl.java | 个人中心服务实现 |
| 新增 | dto/profile/ProfileUpdateDTO.java | 修改个人信息请求 |
| 新增 | dto/profile/PasswordUpdateDTO.java | 修改密码请求 |
| 新增 | dto/profile/TotpVerifyDTO.java | TOTP 验证请求 |
| 新增 | dto/profile/TotpDisableDTO.java | 关闭 TOTP 请求 |
| 新增 | vo/profile/ProfileVO.java | 个人信息响应 |
| 新增 | vo/profile/TotpEnableVO.java | TOTP 开启响应 |

---

## Task 1: 添加依赖

**Files:**
- Modify: `haifeng-common/pom.xml:103`

- [ ] **Step 1: 添加 GoogleAuth 和 ZXing 依赖**

在 `</dependencies>` 之前添加：

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

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/pom.xml
git commit -m "build: 添加 GoogleAuth 和 ZXing 依赖"
```

---

## Task 2: 修改数据库表结构

**Files:**
- Modify: `haifeng-admin/src/main/resources/db/migration/V1__create_admin_tables.sql:81-82`

- [ ] **Step 1: 在 sys_admin 表定义中新增 TOTP 字段**

在 `updated_at` 字段之后、表定义结束前添加：

```sql
    totp_secret     VARCHAR(64),
    is_totp_enabled BOOLEAN DEFAULT FALSE,
```

- [ ] **Step 2: 添加字段注释**

在 `COMMENT ON COLUMN sys_admin.status` 之后添加：

```sql
COMMENT ON COLUMN sys_admin.totp_secret IS 'TOTP动态口令密钥(Base32编码)';
COMMENT ON COLUMN sys_admin.is_totp_enabled IS '是否已开启双因素认证';
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/resources/db/migration/V1__create_admin_tables.sql
git commit -m "feat(db): sys_admin 表新增 TOTP 相关字段"
```

---

## Task 3: 更新 SysAdmin 实体

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/entity/permission/SysAdmin.java:47-49`

- [ ] **Step 1: 新增 TOTP 字段**

在 `deleted` 字段之后添加：

```java
    private String totpSecret;

    @TableField("is_totp_enabled")
    private Boolean totpEnabled;
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/permission/SysAdmin.java
git commit -m "feat(entity): SysAdmin 新增 TOTP 字段"
```

---

## Task 4: 扩展 RedisKeyConstant

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java:28-71`

- [ ] **Step 1: 新增常量和方法**

在 `CAPTCHA_PREFIX` 之后添加：

```java
    /**
     * 管理员登录失败计数
     */
    public static final String ADMIN_LOGIN_FAIL_PREFIX = "haifeng:admin:login:fail:";

    /**
     * 管理员 TOTP 预认证
     */
    public static final String ADMIN_PRE_AUTH_PREFIX = "haifeng:admin:pre-auth:";
```

在 `getCaptchaKey` 方法之后添加：

```java
    /**
     * 获取管理员登录失败计数 Key
     *
     * @param phone 手机号
     * @return Redis Key
     */
    public static String getAdminLoginFailKey(String phone) {
        return ADMIN_LOGIN_FAIL_PREFIX + phone;
    }

    /**
     * 获取管理员预认证 Key
     *
     * @param token 预认证令牌
     * @return Redis Key
     */
    public static String getAdminPreAuthKey(String token) {
        return ADMIN_PRE_AUTH_PREFIX + token;
    }
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java
git commit -m "feat(constant): 新增登录失败和预认证 Redis Key"
```

---

## Task 5: 新增错误码

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/response/ResultCode.java:24`

- [ ] **Step 1: 新增 ACCOUNT_LOCKED 和 TOTP_REQUIRED 错误码**

在 `VIP_REQUIRED` 之后添加：

```java
    ACCOUNT_LOCKED(1006, "账号已锁定，请30分钟后重试"),
    TOTP_REQUIRED(20001, "需进行二次验证");
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/response/ResultCode.java
git commit -m "feat(response): 新增账号锁定和 TOTP 错误码"
```

---

## Task 6: 创建 TotpService 接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/TotpService.java`

- [ ] **Step 1: 创建接口文件**

```java
package com.haifeng.common.service;

/**
 * TOTP 双因素认证服务
 */
public interface TotpService {

    /**
     * 生成 TOTP 密钥
     *
     * @return Base32 编码的密钥
     */
    String generateSecret();

    /**
     * 生成二维码 Base64 图片
     *
     * @param secret   TOTP 密钥
     * @param username 用户名（显示在 App 中）
     * @return Base64 编码的 PNG 图片
     */
    String generateQrCodeBase64(String secret, String username);

    /**
     * 验证 TOTP 动态码
     *
     * @param secret TOTP 密钥
     * @param code   6位动态码
     * @return 是否验证通过
     */
    boolean verifyCode(String secret, String code);
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/TotpService.java
git commit -m "feat(service): 创建 TotpService 接口"
```

---

## Task 7: 实现 TotpServiceImpl

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/impl/TotpServiceImpl.java`

- [ ] **Step 1: 创建实现类**

```java
package com.haifeng.common.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.haifeng.common.service.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TotpServiceImpl implements TotpService {

    private static final String ISSUER = "海峰后台";
    private static final int QR_WIDTH = 200;
    private static final int QR_HEIGHT = 200;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Override
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    @Override
    public String generateQrCodeBase64(String secret, String username) {
        try {
            String otpAuthUrl = buildOtpAuthUrl(secret, username);

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("生成 TOTP 二维码失败", e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    @Override
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.length() != 6) {
            return false;
        }
        try {
            int codeInt = Integer.parseInt(code);
            return gAuth.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            log.warn("TOTP 验证码格式错误: {}", code);
            return false;
        }
    }

    private String buildOtpAuthUrl(String secret, String username) {
        String encodedIssuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encodedIssuer, encodedUsername, secret, encodedIssuer);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/service/impl/TotpServiceImpl.java
git commit -m "feat(service): 实现 TotpServiceImpl"
```

---

## Task 8: 创建 TotpLoginDTO 和 PreAuthVO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/auth/TotpLoginDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/auth/PreAuthVO.java`

- [ ] **Step 1: 创建 TotpLoginDTO**

```java
package com.haifeng.admin.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpLoginDTO {

    @NotBlank(message = "预认证令牌不能为空")
    private String preAuthToken;

    @NotBlank(message = "动态验证码不能为空")
    private String totpCode;
}
```

- [ ] **Step 2: 创建 PreAuthVO**

```java
package com.haifeng.admin.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreAuthVO {

    /**
     * 预认证令牌
     */
    private String preAuthToken;
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/auth/TotpLoginDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/auth/PreAuthVO.java
git commit -m "feat(dto/vo): 创建 TOTP 登录相关 DTO 和 VO"
```

---

## Task 9: 更新 AdminAuthService 接口

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/auth/AdminAuthService.java`

- [ ] **Step 1: 新增 TOTP 登录方法**

添加方法签名：

```java
    /**
     * TOTP 二次验证登录
     *
     * @param preAuthToken 预认证令牌
     * @param totpCode     6位动态验证码
     * @return Token信息
     */
    TokenVO loginWithTotp(String preAuthToken, String totpCode);
```

- [ ] **Step 2: 修改 login 方法返回类型**

将 `login` 方法返回类型改为 `Object`，因为可能返回 `TokenVO` 或 `PreAuthVO`：

```java
    /**
     * 管理员登录
     *
     * @param dto 登录信息
     * @return TokenVO（直接登录成功）或 PreAuthVO（需要 TOTP 二次验证）
     */
    Object login(LoginDTO dto);
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/auth/AdminAuthService.java
git commit -m "feat(service): AdminAuthService 新增 TOTP 登录方法"
```

---

## Task 10: 重构 AdminAuthServiceImpl 登录逻辑

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/auth/AdminAuthServiceImpl.java`

- [ ] **Step 1: 添加依赖注入**

在类顶部添加常量和注入：

```java
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final long LOGIN_FAIL_EXPIRE_MINUTES = 30;
    private static final long PRE_AUTH_EXPIRE_MINUTES = 2;

    private final TotpService totpService;
```

- [ ] **Step 2: 重写 login 方法**

替换整个 `login` 方法：

```java
    @Override
    public Object login(LoginDTO dto) {
        // 1. 验证码校验
        if (!captchaService.validateCaptcha(dto.getUuid(), dto.getCaptchaCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        // 2. 检查账号锁定
        String failKey = RedisKeyConstant.getAdminLoginFailKey(dto.getPhone());
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= MAX_LOGIN_FAIL_COUNT) {
            log.warn("管理员账号已锁定: {}", dto.getPhone());
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 3. 查询管理员
        SysAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .eq(SysAdmin::getDeleted, false)
        );

        if (admin == null) {
            log.warn("管理员登录失败，手机号不存在: {}", dto.getPhone());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (admin.getStatus() != 1) {
            log.warn("管理员账号已禁用: {}", dto.getPhone());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 4. 密码校验
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            // 记录失败次数
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            if (failCount == 1) {
                redisTemplate.expire(failKey, LOGIN_FAIL_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            log.warn("管理员登录失败，密码错误: {}，已失败 {} 次", dto.getPhone(), failCount);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 5. 密码正确，清除失败计数
        redisTemplate.delete(failKey);

        // 6. 检查是否开启 TOTP
        if (Boolean.TRUE.equals(admin.getTotpEnabled())) {
            // 生成预认证令牌
            String preAuthToken = java.util.UUID.randomUUID().toString();
            String preAuthKey = RedisKeyConstant.getAdminPreAuthKey(preAuthToken);
            redisTemplate.opsForValue().set(preAuthKey, admin.getId().toString(),
                    PRE_AUTH_EXPIRE_MINUTES, TimeUnit.MINUTES);

            log.info("管理员需要 TOTP 二次验证: {}", admin.getUsername());
            return PreAuthVO.builder().preAuthToken(preAuthToken).build();
        }

        // 7. 未开启 TOTP，直接发放 Token
        return issueToken(admin);
    }
```

- [ ] **Step 3: 添加 loginWithTotp 方法**

```java
    @Override
    public TokenVO loginWithTotp(String preAuthToken, String totpCode) {
        // 1. 验证预认证令牌
        String preAuthKey = RedisKeyConstant.getAdminPreAuthKey(preAuthToken);
        String adminIdStr = redisTemplate.opsForValue().get(preAuthKey);

        if (adminIdStr == null) {
            log.warn("预认证令牌无效或已过期");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "验证已过期，请重新登录");
        }

        // 2. 查询管理员
        Long adminId = Long.parseLong(adminIdStr);
        SysAdmin admin = adminMapper.selectById(adminId);

        if (admin == null || admin.getDeleted() || admin.getStatus() != 1) {
            log.warn("管理员不存在或已禁用: {}", adminId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 验证 TOTP 动态码
        if (!totpService.verifyCode(admin.getTotpSecret(), totpCode)) {
            log.warn("TOTP 验证码错误, adminId={}", adminId);
            throw new BusinessException(ResultCode.BAD_REQUEST, "动态验证码错误");
        }

        // 4. 删除预认证令牌（一次性使用）
        redisTemplate.delete(preAuthKey);

        log.info("管理员 TOTP 验证成功: {}", admin.getUsername());

        // 5. 发放 Token
        return issueToken(admin);
    }
```

- [ ] **Step 4: 抽取 issueToken 方法**

```java
    private TokenVO issueToken(SysAdmin admin) {
        String accessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        admin.setLastLoginAt(OffsetDateTime.now());
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员登录成功: {}", admin.getUsername());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }
```

- [ ] **Step 5: 添加 import**

```java
import com.haifeng.admin.vo.auth.PreAuthVO;
import com.haifeng.common.service.TotpService;
```

- [ ] **Step 6: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/auth/AdminAuthServiceImpl.java
git commit -m "feat(auth): 实现账号锁定和 TOTP 二次验证登录"
```

---

## Task 11: 更新 AdminAuthController

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/controller/auth/AdminAuthController.java`

- [ ] **Step 1: 修改 login 返回类型**

将 `login` 方法的返回类型从 `R<TokenVO>` 改为 `R<?>`:

```java
    @PostMapping("/login")
    public R<?> login(@Valid @RequestBody LoginDTO dto) {
        Object result = adminAuthService.login(dto);
        if (result instanceof PreAuthVO) {
            return R.fail(ResultCode.TOTP_REQUIRED, (PreAuthVO) result);
        }
        return R.ok((TokenVO) result);
    }
```

- [ ] **Step 2: 新增 TOTP 登录接口**

```java
    @PostMapping("/login/totp")
    public R<TokenVO> loginWithTotp(@Valid @RequestBody TotpLoginDTO dto) {
        TokenVO tokenVO = adminAuthService.loginWithTotp(dto.getPreAuthToken(), dto.getTotpCode());
        return R.ok(tokenVO);
    }
```

- [ ] **Step 3: 添加 import**

```java
import com.haifeng.admin.dto.auth.TotpLoginDTO;
import com.haifeng.admin.vo.auth.PreAuthVO;
import com.haifeng.common.response.ResultCode;
```

- [ ] **Step 4: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/auth/AdminAuthController.java
git commit -m "feat(controller): AdminAuthController 新增 TOTP 登录接口"
```

---

## Task 12: 更新 SecurityConfig 白名单

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/config/SecurityConfig.java:40`

- [ ] **Step 1: 添加 TOTP 登录接口到白名单**

在 `"/api/v1/*/auth/captcha"` 之后添加：

```java
            "/api/v1/*/auth/login/totp",
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/config/SecurityConfig.java
git commit -m "feat(security): 白名单新增 TOTP 登录接口"
```

---

## Task 13: 创建 Profile DTO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/profile/ProfileUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/profile/PasswordUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/profile/TotpVerifyDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/profile/TotpDisableDTO.java`

- [ ] **Step 1: 创建 ProfileUpdateDTO**

```java
package com.haifeng.admin.dto.profile;

import lombok.Data;

@Data
public class ProfileUpdateDTO {

    private String username;

    private String phone;

    private String email;

    private String avatar;
}
```

- [ ] **Step 2: 创建 PasswordUpdateDTO**

```java
package com.haifeng.admin.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdateDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
```

- [ ] **Step 3: 创建 TotpVerifyDTO**

```java
package com.haifeng.admin.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpVerifyDTO {

    @NotBlank(message = "验证码不能为空")
    private String code;
}
```

- [ ] **Step 4: 创建 TotpDisableDTO**

```java
package com.haifeng.admin.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpDisableDTO {

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 5: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/profile/
git commit -m "feat(dto): 创建个人中心相关 DTO"
```

---

## Task 14: 创建 Profile VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/profile/ProfileVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/profile/TotpEnableVO.java`

- [ ] **Step 1: 创建 ProfileVO**

```java
package com.haifeng.admin.vo.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
```

- [ ] **Step 2: 创建 TotpEnableVO**

```java
package com.haifeng.admin.vo.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpEnableVO {

    /**
     * TOTP 密钥（用于手动输入）
     */
    private String secret;

    /**
     * Base64 编码的二维码图片
     */
    private String qrCodeImage;
}
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/profile/
git commit -m "feat(vo): 创建个人中心相关 VO"
```

---

## Task 15: 创建 ProfileService 接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/profile/ProfileService.java`

- [ ] **Step 1: 创建接口**

```java
package com.haifeng.admin.service.profile;

import com.haifeng.admin.dto.profile.PasswordUpdateDTO;
import com.haifeng.admin.dto.profile.ProfileUpdateDTO;
import com.haifeng.admin.dto.profile.TotpDisableDTO;
import com.haifeng.admin.dto.profile.TotpVerifyDTO;
import com.haifeng.admin.vo.profile.ProfileVO;
import com.haifeng.admin.vo.profile.TotpEnableVO;

public interface ProfileService {

    /**
     * 获取当前管理员信息
     */
    ProfileVO getProfile();

    /**
     * 修改个人信息
     */
    void updateProfile(ProfileUpdateDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(PasswordUpdateDTO dto);

    /**
     * 开启 TOTP（生成密钥和二维码）
     */
    TotpEnableVO enableTotp();

    /**
     * 验证并确认绑定 TOTP
     */
    void verifyTotp(TotpVerifyDTO dto);

    /**
     * 关闭 TOTP
     */
    void disableTotp(TotpDisableDTO dto);

    /**
     * 获取当前 TOTP 二维码
     */
    TotpEnableVO getTotpQrCode();
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/profile/ProfileService.java
git commit -m "feat(service): 创建 ProfileService 接口"
```

---

## Task 16: 实现 ProfileServiceImpl

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/profile/ProfileServiceImpl.java`

- [ ] **Step 1: 创建实现类**

```java
package com.haifeng.admin.service.impl.profile;

import com.haifeng.admin.dto.profile.PasswordUpdateDTO;
import com.haifeng.admin.dto.profile.ProfileUpdateDTO;
import com.haifeng.admin.dto.profile.TotpDisableDTO;
import com.haifeng.admin.dto.profile.TotpVerifyDTO;
import com.haifeng.admin.service.profile.ProfileService;
import com.haifeng.admin.vo.profile.ProfileVO;
import com.haifeng.admin.vo.profile.TotpEnableVO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.TotpService;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    @Override
    public ProfileVO getProfile() {
        SysAdmin admin = getCurrentAdmin();
        return ProfileVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .phone(admin.getPhone())
                .email(admin.getEmail())
                .avatar(admin.getAvatar())
                .roleName(admin.getRoleName())
                .isTotpEnabled(Boolean.TRUE.equals(admin.getTotpEnabled()))
                .lastLoginAt(admin.getLastLoginAt())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(ProfileUpdateDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (StringUtils.hasText(dto.getUsername())) {
            admin.setUsername(dto.getUsername());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            admin.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            admin.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            admin.setAvatar(dto.getAvatar());
        }

        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员更新个人信息: {}", admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (!passwordEncoder.matches(dto.getOldPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }

        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员修改密码成功: {}", admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TotpEnableVO enableTotp() {
        SysAdmin admin = getCurrentAdmin();

        if (Boolean.TRUE.equals(admin.getTotpEnabled())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "TOTP 已开启");
        }

        // 生成密钥
        String secret = totpService.generateSecret();
        admin.setTotpSecret(secret);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        // 生成二维码
        String qrCode = totpService.generateQrCodeBase64(secret, admin.getUsername());

        log.info("管理员生成 TOTP 密钥: {}", admin.getUsername());

        return TotpEnableVO.builder()
                .secret(secret)
                .qrCodeImage(qrCode)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyTotp(TotpVerifyDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (admin.getTotpSecret() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先开启 TOTP");
        }

        if (!totpService.verifyCode(admin.getTotpSecret(), dto.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误");
        }

        admin.setTotpEnabled(true);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员绑定 TOTP 成功: {}", admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableTotp(TotpDisableDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "密码错误");
        }

        admin.setTotpEnabled(false);
        admin.setTotpSecret(null);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员关闭 TOTP: {}", admin.getUsername());
    }

    @Override
    public TotpEnableVO getTotpQrCode() {
        SysAdmin admin = getCurrentAdmin();

        if (!Boolean.TRUE.equals(admin.getTotpEnabled()) || admin.getTotpSecret() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "TOTP 未开启");
        }

        String qrCode = totpService.generateQrCodeBase64(admin.getTotpSecret(), admin.getUsername());

        return TotpEnableVO.builder()
                .secret(admin.getTotpSecret())
                .qrCodeImage(qrCode)
                .build();
    }

    private SysAdmin getCurrentAdmin() {
        Long adminId = SecurityUtil.getCurrentAdminId();
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return admin;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/profile/ProfileServiceImpl.java
git commit -m "feat(service): 实现 ProfileServiceImpl"
```

---

## Task 17: 创建 ProfileController

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/profile/ProfileController.java`

- [ ] **Step 1: 创建控制器**

```java
package com.haifeng.admin.controller.profile;

import com.haifeng.admin.dto.profile.PasswordUpdateDTO;
import com.haifeng.admin.dto.profile.ProfileUpdateDTO;
import com.haifeng.admin.dto.profile.TotpDisableDTO;
import com.haifeng.admin.dto.profile.TotpVerifyDTO;
import com.haifeng.admin.service.profile.ProfileService;
import com.haifeng.admin.vo.profile.ProfileVO;
import com.haifeng.admin.vo.profile.TotpEnableVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public R<ProfileVO> getProfile() {
        ProfileVO vo = profileService.getProfile();
        return R.ok(vo);
    }

    @PutMapping
    public R<Void> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        profileService.updatePassword(dto);
        return R.ok();
    }

    @PostMapping("/totp/enable")
    public R<TotpEnableVO> enableTotp() {
        TotpEnableVO vo = profileService.enableTotp();
        return R.ok(vo);
    }

    @PostMapping("/totp/verify")
    public R<Void> verifyTotp(@Valid @RequestBody TotpVerifyDTO dto) {
        profileService.verifyTotp(dto);
        return R.ok();
    }

    @PostMapping("/totp/disable")
    public R<Void> disableTotp(@Valid @RequestBody TotpDisableDTO dto) {
        profileService.disableTotp(dto);
        return R.ok();
    }

    @GetMapping("/totp/qrcode")
    public R<TotpEnableVO> getTotpQrCode() {
        TotpEnableVO vo = profileService.getTotpQrCode();
        return R.ok(vo);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/profile/ProfileController.java
git commit -m "feat(controller): 创建 ProfileController 个人中心接口"
```

---

## Task 18: 角色保护 - 修改 RoleServiceImpl

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/RoleServiceImpl.java`

- [ ] **Step 1: 在删除方法中添加保护逻辑**

在 `deleteRole` 或 `delete` 方法开头添加：

```java
        if (id == 1L) {
            throw new BusinessException(400, "超级管理员角色不可删除");
        }
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/RoleServiceImpl.java
git commit -m "feat(permission): 角色删除保护-超级管理员角色不可删除"
```

---

## Task 19: 管理员保护 - 修改 AdminServiceImpl

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/AdminServiceImpl.java`

- [ ] **Step 1: 在删除方法中添加保护逻辑**

在 `deleteAdmin` 或 `delete` 方法开头添加：

```java
        if (id == 1L) {
            throw new BusinessException(400, "默认管理员不可删除");
        }
```

- [ ] **Step 2: 在更新方法中添加角色保护**

在 `updateAdmin` 或 `update` 方法开头添加：

```java
        if (id == 1L && dto.getRoleId() != null && dto.getRoleId() != 1L) {
            throw new BusinessException(400, "默认管理员角色不可变更");
        }
```

- [ ] **Step 3: Commit**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/AdminServiceImpl.java
git commit -m "feat(permission): 管理员保护-默认管理员不可删除且角色不可变更"
```

---

## Task 20: 更新 R 类支持带数据的失败响应

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/response/R.java`

- [ ] **Step 1: 添加带数据的 fail 方法**

```java
    public static <T> R<T> fail(ResultCode resultCode, T data) {
        R<T> r = new R<>();
        r.setCode(resultCode.getCode());
        r.setMsg(resultCode.getMsg());
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        return r;
    }
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/response/R.java
git commit -m "feat(response): R 类新增带数据的 fail 方法"
```

---

## Task 21: 最终验证

- [ ] **Step 1: 检查所有文件已创建**

```bash
ls -la haifeng-common/src/main/java/com/haifeng/common/service/TotpService.java
ls -la haifeng-common/src/main/java/com/haifeng/common/service/impl/TotpServiceImpl.java
ls -la haifeng-admin/src/main/java/com/haifeng/admin/controller/profile/ProfileController.java
ls -la haifeng-admin/src/main/java/com/haifeng/admin/service/profile/ProfileService.java
ls -la haifeng-admin/src/main/java/com/haifeng/admin/service/impl/profile/ProfileServiceImpl.java
```

- [ ] **Step 2: 查看 git 状态**

```bash
git status
git log --oneline -15
```

- [ ] **Step 3: 完成**

所有功能已实现，包括：
- 账号锁定机制（5次错误锁定30分钟）
- TOTP 双因素认证（可选开启）
- 管理员个人中心（7个接口）
- 角色和管理员保护
