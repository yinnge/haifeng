# 短信验证码 - 忘记密码功能 · 设计文档

> 版本：v1.0
> 日期：2026-07-06
> 状态：待评审
> 基于：需求文档 `haifeng-app/Need/短信验证码-忘记密码-需求文档.md`

---

## 1. 功能概述

C端会员（用户）通过手机号接收短信验证码，验证身份后重置密码。仅面向 C端会员，B端管理员使用 TOTP 不需要此功能。

### 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/app/auth/forgot-password/send-code` | 发送短信验证码 |
| POST | `/api/v1/app/auth/forgot-password/reset` | 校验验证码并重置密码 |

### 安全白名单

以上两个接口加入 `SecurityConfig.WHITE_LIST`，免登录访问。

---

## 2. 模块结构

### 新增文件清单

```
haifeng-common/
├── service/
│   └── SmsService.java                          ← 接口
├── service/impl/
│   └── SmsServiceImpl.java                      ← 实现（WebClient 调 Submail）
├── config/
│   └── SmsProperties.java                       ← @ConfigurationProperties
└── constant/
    └── RedisKeyConstant.java                     ← 追加 4 个 SMS key 常量

haifeng-app/
├── controller/auth/
│   └── AppAuthController.java                   ← 追加 2 个端点
├── service/auth/
│   └── AppAuthService.java                      ← 追加 2 个方法
├── service/impl/auth/
│   └── AppAuthServiceImpl.java                  ← 追加实现
└── dto/auth/
    ├── ForgotPasswordSendCodeDTO.java           ← 发送验证码请求体
    └── ForgotPasswordResetDTO.java              ← 重置密码请求体
```

### 为什么 SmsService 放 common？

短信发送是基础设施能力（类似 CaptchaService），后续 admin 端如需短信通知可直接复用。

---

## 3. DTO 设计

### 3.1 ForgotPasswordSendCodeDTO

```java
@Data
public class ForgotPasswordSendCodeDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "图形验证码不能为空")
    private String captchaCode;  // 4 位图片验证码

    @NotBlank(message = "图形验证码UUID不能为空")
    private String uuid;         // 图片验证码的 UUID
}
```

### 3.2 ForgotPasswordResetDTO

```java
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

## 4. SmsService 设计

### 4.1 接口

```java
public interface SmsService {
    /**
     * 发送短信验证码
     * @param phone 目标手机号
     * @param code  6位验证码
     * @return Submail 返回的 send_id
     */
    String sendSmsCode(String phone, String code);
}
```

### 4.2 实现 - SmsServiceImpl

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final WebClient webClient;
    private final SmsProperties smsProperties;

    @Override
    public String sendSmsCode(String phone, String code) {
        String content = smsProperties.getSign() + "您的验证码是：" + code + "，5分钟内有效，请勿泄露他人。";

        try {
            String response = webClient.post()
                    .uri("https://api-v4.mysubmail.com/sms/send.json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(buildFormBody(phone, content))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            // 解析 Submail 响应
            // 成功响应示例：{"status": "success", "send_id": "..."}
            // null 响应或非 success → 视为失败
            ...
        } catch (Exception e) {
            log.error("发送短信验证码失败，phone={}", DesensitizeUtil.desensitizePhone(phone), e);
            throw new BusinessException(1400, "短信发送失败");
        }
    }
}
```

### 4.3 SmsProperties

```java
@ConfigurationProperties(prefix = "submail")
@Data
public class SmsProperties {
    private String appid;
    private String appkey;
    private String sign;  // 短信签名，如 【海峰规划】
}
```

### 4.4 application-dev.yml 追加

```yaml
submail:
  appid: ${SUBMAIL_APPID}
  appkey: ${SUBMAIL_APPKEY}
  sign: 【海峰规划】
```

---

## 5. Redis Key 设计

追加到 `RedisKeyConstant.java`：

| 常量名 | Redis Key 模式 | TTL | 用途 |
|--------|---------------|-----|------|
| SMS_CODE | `haifeng:sms:code:{phone}` | 5min | 短信验证码 |
| SMS_SEND_COOL | `haifeng:sms:send:cool:{phone}` | 60s | 发送冷却 |
| SMS_SEND_LIMIT | `haifeng:sms:send:limit:{yyyyMMdd}:{phone}` | 当日剩余 | 每日发送次数 |
| SMS_VERIFY_FAIL | `haifeng:sms:verify:fail:{phone}` | 30min(锁定后) | 验证码失败计数 / 锁定 |

```java
public static final String SMS_CODE = "haifeng:sms:code:";
public static final String SMS_SEND_COOL = "haifeng:sms:send:cool:";
public static final String SMS_SEND_LIMIT = "haifeng:sms:send:limit:";
public static final String SMS_VERIFY_FAIL = "haifeng:sms:verify:fail:";
```

---

## 6. 核心业务逻辑

### 6.1 发送验证码

```
POST /api/v1/app/auth/forgot-password/send-code
Request: {phone, captchaCode, uuid}
```

**校验流程（顺序执行，任一失败即返回）：**

1. **图形验证码校验** — `CaptchaService.validateCaptcha(uuid, captchaCode)`
   - 一次性，校验后删除 Redis key
   - 失败 → `400 "验证码错误或已过期"`

2. **手机号是否存在** — 查 `t_member`
   - 不存在 → 统一返回 `200`，不调 Submail、不存验证码
   - 避免枚举攻击

3. **60秒冷却** — Redis `SET NX EX 60`
   - key 存在 → `429 "发送过于频繁，请60秒后重试"`

4. **每日上限 5 次** — Redis INCR，首次调用设 TTL 到次日零点
   - 超过 5 次 → `429 "今日短信发送次数已达上限"`

5. **IP 限流** — 复用 `RedisKeyConstant.getLimitApiKey()`
   - 频率过高 → `429`

**通过校验后：**
- 生成 6 位随机数字验证码（`RandomStringUtils.randomNumeric(6)`）
- 调用 `SmsService.sendSmsCode(phone, code)` 发短信
- `haifeng:sms:code:{phone}` = code，TTL=5min
- `haifeng:sms:send:cool:{phone}` = 1，TTL=60s
- `haifeng:sms:send:limit:{yyyyMMdd}:{phone}` INCR 1

**Response：** `{code:200, msg:"success", data:null}`

### 6.2 重置密码

```
POST /api/v1/app/auth/forgot-password/reset
Request: {phone, code, password}
```

**校验流程（顺序执行）：**

1. **手机号是否存在** — 查 `t_member`
   - 不存在 → `1001 "用户不存在"`

2. **验证码是否存在** — 查 `haifeng:sms:code:{phone}`
   - 不存在或已过期 → `400 "验证码已过期"`

3. **验证码匹配**
   - 匹配失败：
     - 递增 `haifeng:sms:verify:fail:{phone}` 计数器
     - 连续 5 次失败 → 设 key TTL=1800s（30分钟锁定）
     - 返回 `429 "验证码错误次数过多，请30分钟后再试"`
   - 匹配成功：
     - 删除 `haifeng:sms:code:{phone}`（一次性）
     - 删除 `haifeng:sms:verify:fail:{phone}`（清除失败计数）

4. **更新密码** — BCrypt 加密后更新 `t_member.password`

5. **清除登录态** — 删除 Redis 中 `haifeng:token:refresh:member:{userId}`
   - 遍历删除该会员的所有 refresh token
   - 强制用户重新登录

**Response：** `{code:200, msg:"success", data:null}`

---

## 7. ResultCode 新增

```java
SMS_SEND_FAILED(1400, "短信发送失败"),
SMS_CODE_EXPIRED(1401, "验证码已过期或不存在"),
SMS_CODE_LOCKED(1402, "验证码错误次数过多，已锁定30分钟"),
```

---

## 8. SecurityConfig 白名单追加

```java
private static final String[] WHITE_LIST = {
    // ... 现有
    "/api/v1/app/auth/forgot-password/send-code",
    "/api/v1/app/auth/forgot-password/reset"
};
```

---

## 9. 安全策略汇总

| 防护维度 | 规则 | 实现方式 |
|---------|------|---------|
| 图形验证码前置 | send-code 需要先通过图片验证码 | CaptchaService（现有） |
| 发送冷却 | 同一手机号间隔 ≥ 60秒 | Redis SET NX EX |
| 每日上限 | 同一手机号每天最多 5 次 | Redis INCR + 次日过期 |
| IP 限流 | 同一 IP 对 send-code 接口限流 | LIMIT_API（现有） |
| 验证码暴力破解 | 连续 5 次错误锁定 30 分钟 | Redis 递增计数器 + TTL |
| 手机号枚举防御 | 发送接口手机号不存在也返回 200 | 逻辑判断 |
| 一次性验证码 | 验证成功后立即删除 Redis key | Redis del |
| 日志脱敏 | 所有日志手机号脱敏 | DesensitizeUtil（现有） |
| Submail 超时 | 5 秒超时保护 | WebClient timeout |

---

## 10. 日志规范

| 场景 | 级别 | 日志内容 |
|------|------|----------|
| 发送验证码成功 | info | `发送短信验证码成功，phone={脱敏}, sendId={send_id}` |
| 发送验证码失败 | error | `发送短信验证码失败，phone={脱敏}, error={msg}` |
| 重置密码成功 | info | `密码重置成功，memberId={id}, phone={脱敏}` |
| 验证码校验失败 | warn | `短信验证码校验失败，phone={脱敏}, 已失败次数={n}` |
| 达到限流 | warn | `短信发送被限流，phone={脱敏}, 原因={冷却/日限}` |
| Submail 不可用 | error | `Submail API 调用失败，phone={脱敏}`, e |

所有日志中的手机号使用 `DesensitizeUtil.desensitizePhone()` 脱敏。

---

## 11. 边界 & 异常处理

| 场景 | 处理方式 |
|------|----------|
| Submail 服务不可用 | 捕获超时/异常 → 返回 500，日志记录 error |
| 同一手机号同时请求 2 次 | 60 秒冷却 key 阻止第二次 |
| 用户 5 分钟内未收到短信 | 前端引导重新发送（需等待 60 秒冷却） |
| 用户更换设备 | 无影响，验证码绑定手机号而非设备 |
| 密码重置后旧 Token 仍有效 | 主动清除 Redis refresh token；access token 2h 自然过期 |
