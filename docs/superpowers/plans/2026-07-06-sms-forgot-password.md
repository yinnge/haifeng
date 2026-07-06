# 短信验证码 - 忘记密码 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** C端用户通过短信验证码重置密码（忘记密码）

**Architecture:** `SmsService` 作为基础设施层放在 haifeng-common（类似 CaptchaService），业务逻辑在 haifeng-app 的 `AppAuthService` 中实现。两个免登录端点，支持图形验证码前置校验、发送冷却、日限、IP限流、暴力破解防御。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Spring Security, Redis (StringRedisTemplate), WebClient (Spring WebFlux), Hutool, BCrypt

---

### Task 1: Common 基础设施 — SmsProperties + RedisKey + ResultCode

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/config/SmsProperties.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/response/ResultCode.java`

- [ ] **Step 1: Create SmsProperties.java**

```java
package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "submail")
public class SmsProperties {
    private String appid;
    private String appkey;
    private String sign;
}
```

- [ ] **Step 2: Add SMS Redis keys to RedisKeyConstant.java**

Append after the `WISH_EXPORT_PREFIX` field (before the `HOME_ANNOUNCEMENT_...` section):

```java
    /**
     * 短信验证码
     */
    public static final String SMS_CODE = "haifeng:sms:code:";
    public static final String SMS_SEND_COOL = "haifeng:sms:send:cool:";
    public static final String SMS_SEND_LIMIT = "haifeng:sms:send:limit:";
    public static final String SMS_VERIFY_FAIL = "haifeng:sms:verify:fail:";
```

- [ ] **Step 3: Add SMS error codes to ResultCode.java**

Append after `TOTP_REQUIRED(20001, ...)`:

```java
    SMS_SEND_FAILED(1400, "短信发送失败"),
    SMS_CODE_EXPIRED(1401, "验证码已过期或不存在"),
    SMS_CODE_LOCKED(1402, "验证码错误次数过多，已锁定30分钟"),
```

---

### Task 2: SmsService 接口 + 实现（Submail 集成）

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/SmsService.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/service/impl/SmsServiceImpl.java`

- [ ] **Step 1: Create SmsService interface**

```java
package com.haifeng.common.service;

public interface SmsService {
    String sendSmsCode(String phone, String code);
}
```

- [ ] **Step 2: Create SmsServiceImpl implementation**

```java
package com.haifeng.common.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.common.config.SmsProperties;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.service.SmsService;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final WebClient webClient;
    private final SmsProperties smsProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String sendSmsCode(String phone, String code) {
        String content = smsProperties.getSign() + "您的验证码是：" + code + "，5分钟内有效，请勿泄露他人。";

        try {
            String response = webClient.post()
                    .uri("https://api-v4.mysubmail.com/sms/send.json")
                    .bodyValue(Map.of(
                            "appid", smsProperties.getAppid(),
                            "to", phone,
                            "content", content,
                            "signature", smsProperties.getAppkey()
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            JsonNode json = objectMapper.readTree(response);
            String status = json.has("status") ? json.get("status").asText() : null;
            String sendId = json.has("send_id") ? json.get("send_id").asText() : null;

            if (!"success".equals(status)) {
                log.error("发送短信验证码失败，phone={}, response={}",
                        DesensitizeUtil.desensitizePhone(phone), response);
                throw new BusinessException(1400, "短信发送失败");
            }

            log.info("发送短信验证码成功，phone={}, sendId={}",
                    DesensitizeUtil.desensitizePhone(phone), sendId);
            return sendId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送短信验证码失败，phone={}", DesensitizeUtil.desensitizePhone(phone), e);
            throw new BusinessException(1400, "短信发送失败，请稍后重试");
        }
    }
}
```

---

### Task 3: DTOs

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/auth/ForgotPasswordSendCodeDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/auth/ForgotPasswordResetDTO.java`

- [ ] **Step 1: Create ForgotPasswordSendCodeDTO.java**

```java
package com.haifeng.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordSendCodeDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "图形验证码不能为空")
    private String captchaCode;

    @NotBlank(message = "图形验证码UUID不能为空")
    private String uuid;
}
```

- [ ] **Step 2: Create ForgotPasswordResetDTO.java**

```java
package com.haifeng.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ForgotPasswordResetDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$",
             message = "密码必须是数字+字母，长度6-16位")
    private String password;
}
```

---

### Task 4: AppAuthService 追加 forgot-password 方法

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/auth/AppAuthService.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/auth/AppAuthServiceImpl.java`

- [ ] **Step 1: Add 2 methods to AppAuthService interface**

```java
    void forgotPasswordSendCode(ForgotPasswordSendCodeDTO dto);
    void forgotPasswordReset(ForgotPasswordResetDTO dto);
```

Add the new import at the top:
```java
import com.haifeng.app.dto.auth.ForgotPasswordSendCodeDTO;
import com.haifeng.app.dto.auth.ForgotPasswordResetDTO;
```

- [ ] **Step 2: Implement forgotPasswordSendCode in AppAuthServiceImpl**

Add these new fields to `AppAuthServiceImpl` (before constructor):
```java
    private final SmsService smsService;
```

Add these imports at the top:
```java
import cn.hutool.core.util.RandomUtil;
import com.haifeng.app.dto.auth.ForgotPasswordSendCodeDTO;
import com.haifeng.app.dto.auth.ForgotPasswordResetDTO;
import com.haifeng.common.config.SmsProperties;
import com.haifeng.common.service.SmsService;
import com.haifeng.common.util.DesensitizeUtil;
```

Add the implementation methods before the closing brace of the class:

```java
    @Override
    public void forgotPasswordSendCode(ForgotPasswordSendCodeDTO dto) {
        // 1. 校验图形验证码
        if (!captchaService.validateCaptcha(dto.getUuid(), dto.getCaptchaCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        String phone = dto.getPhone();

        // 2. 检查手机号是否存在（不存在则假装成功，避免枚举攻击）
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, phone)
                        .eq(Member::getDeleted, false)
        );
        if (member == null) {
            log.info("发送短信验证码-手机号未注册（假装成功），phone={}",
                    DesensitizeUtil.desensitizePhone(phone));
            return;
        }

        // 3. 60秒发送冷却
        String coolKey = RedisKeyConstant.SMS_SEND_COOL + phone;
        Boolean isCooled = redisTemplate.opsForValue().setIfAbsent(coolKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isCooled)) {
            log.warn("短信发送被限流，phone={}, 原因=冷却中",
                    DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "发送过于频繁，请60秒后重试");
        }

        // 4. 每日发送次数限制（5次）
        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        String limitKey = RedisKeyConstant.SMS_SEND_LIMIT + dateStr + ":" + phone;
        Long sendCount = redisTemplate.opsForValue().increment(limitKey);
        if (sendCount != null && sendCount == 1) {
            // 首次设置，TTL 到次日零点
            long secondsUntilEndOfDay = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    java.time.LocalDate.now().plusDays(1).atStartOfDay()
            ).getSeconds();
            redisTemplate.expire(limitKey, secondsUntilEndOfDay, TimeUnit.SECONDS);
        }
        if (sendCount != null && sendCount > 5) {
            log.warn("短信发送被限流，phone={}, 原因=日限达上限",
                    DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日短信发送次数已达上限");
        }

        // 5. IP 频率限制
        String clientIp = IpUtil.getClientIp();
        String ipLimitKey = RedisKeyConstant.getLimitApiKey(clientIp,
                "/api/v1/app/auth/forgot-password/send-code");
        Long ipCount = redisTemplate.opsForValue().increment(ipLimitKey);
        if (ipCount != null && ipCount == 1) {
            redisTemplate.expire(ipLimitKey, 60, TimeUnit.SECONDS); // 60秒内限制
        }
        if (ipCount != null && ipCount > 10) {
            log.warn("短信发送被限流，IP={}, 原因=IP请求过于频繁", IpUtil.getClientIp());
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后重试");
        }

        // 6. 生成6位验证码并发送短信
        String code = RandomUtil.randomNumbers(6);
        try {
            smsService.sendSmsCode(phone, code);
        } catch (BusinessException e) {
            // 发送失败时，删除冷却key，让用户可以重试
            redisTemplate.delete(coolKey);
            throw e;
        }

        // 7. 验证码存入 Redis，5分钟有效
        String codeKey = RedisKeyConstant.SMS_CODE + phone;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
    }

    @Override
    public void forgotPasswordReset(ForgotPasswordResetDTO dto) {
        String phone = dto.getPhone();

        // 1. 检查手机号是否存在
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, phone)
                        .eq(Member::getDeleted, false)
        );
        if (member == null) {
            log.warn("密码重置失败，手机号不存在: {}", DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 检查验证码是否存在/已过期
        String codeKey = RedisKeyConstant.SMS_CODE + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            log.warn("密码重置失败，验证码已过期，phone={}",
                    DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.SMS_CODE_EXPIRED);
        }

        // 3. 匹配验证码
        if (!storedCode.equals(dto.getCode())) {
            String failKey = RedisKeyConstant.SMS_VERIFY_FAIL + phone;
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1) {
                redisTemplate.expire(failKey, 30, TimeUnit.MINUTES);
            }

            log.warn("短信验证码校验失败，phone={}, 已失败次数={}",
                    DesensitizeUtil.desensitizePhone(phone), failCount);

            if (failCount != null && failCount >= 5) {
                // 已锁定，extend TTL to 30min from now
                redisTemplate.expire(failKey, 30, TimeUnit.MINUTES);
                redisTemplate.delete(codeKey);
                log.warn("短信验证码锁定，phone={}", DesensitizeUtil.desensitizePhone(phone));
                throw new BusinessException(ResultCode.SMS_CODE_LOCKED);
            }

            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误");
        }

        // 验证码匹配成功，删除 Redis key（一次性使用）
        redisTemplate.delete(codeKey);
        redisTemplate.delete(RedisKeyConstant.SMS_VERIFY_FAIL + phone);

        // 4. 更新密码
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setUpdatedAt(java.time.OffsetDateTime.now());
        memberMapper.updateById(member);

        // 5. 清除该用户所有登录态
        String refreshKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), "member");
        redisTemplate.delete(refreshKey);

        log.info("密码重置成功，memberId={}, phone={}",
                member.getId(), DesensitizeUtil.desensitizePhone(phone));
    }
```

- [ ] **Step 3: Fix the class annotation — update constructor after adding SmsService**

The `@RequiredArgsConstructor` will auto-inject `SmsService` since it's `final`. Ensure the field declaration is correct:

```java
    private final SmsService smsService;
```

---

### Task 5: Controller + Security Config + application.yml

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/auth/AppAuthController.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/config/SecurityConfig.java`
- Modify: `haifeng-app/src/main/resources/application.yml`

- [ ] **Step 1: Add 2 endpoints to AppAuthController.java**

Add new imports:
```java
import com.haifeng.app.dto.auth.ForgotPasswordSendCodeDTO;
import com.haifeng.app.dto.auth.ForgotPasswordResetDTO;
```

Add before the closing brace of the class:

```java
    @PostMapping("/forgot-password/send-code")
    public R<Void> forgotPasswordSendCode(@Valid @RequestBody ForgotPasswordSendCodeDTO dto) {
        appAuthService.forgotPasswordSendCode(dto);
        return R.ok();
    }

    @PostMapping("/forgot-password/reset")
    public R<Void> forgotPasswordReset(@Valid @RequestBody ForgotPasswordResetDTO dto) {
        appAuthService.forgotPasswordReset(dto);
        return R.ok();
    }
```

- [ ] **Step 2: Add whitelist paths to SecurityConfig.java**

In the `WHITE_LIST` array, add after the `/api/v1/*/auth/login/totp` line:

```java
            // 忘记密码
            "/api/v1/app/auth/forgot-password/send-code",
            "/api/v1/app/auth/forgot-password/reset",
```

- [ ] **Step 3: Add Submail config to application.yml**

Add at the end of `haifeng-app/src/main/resources/application.yml`:

```yaml
submail:
  appid: ${SUBMAIL_APPID}
  appkey: ${SUBMAIL_APPKEY}
  sign: 【海峰规划】
```

---

### Task 6: Verify build

- [ ] **Step 1: Build the project**

```bash
mvn compile -pl haifeng-common,haifeng-app -am
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Check for any compilation errors and fix**

```bash
mvn compile -pl haifeng-common,haifeng-app -am 2>&1
```
