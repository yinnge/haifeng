# 系统设置与用户管理模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现系统设置模块（单例配置）和用户管理模块（列表/详情/状态修改），包含微信号 AES 加密存储和 SHA-256 盲索引查询。

**Architecture:** 使用 MyBatis-Plus TypeHandler 实现透明加解密，Service 层计算盲索引，JSONB 字段通过 JacksonTypeHandler 映射为 Java 对象。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Hutool (AES/SHA256), PostgreSQL JSONB

---

## 文件结构

### haifeng-common 新增/修改

| 文件 | 职责 |
|------|------|
| config/SecurityProperties.java | 读取 AES 密钥和哈希盐值配置 |
| util/SpringContextHolder.java | 静态获取 Spring Bean |
| util/CryptoUtil.java | AES 加解密 + SHA-256 盲索引 |
| util/DesensitizeUtil.java | 数据脱敏工具 |
| handler/AESEncryptTypeHandler.java | MyBatis-Plus 自动加解密 |
| annotation/OperationLog.java | 操作日志注解 |
| aspect/OperationLogAspect.java | 操作日志切面 |
| entity/user/Member.java | 新增 wechatId, wechatIdIndex 字段 |
| entity/system/SystemSettings.java | 系统设置实体 |
| entity/system/ContactUrl.java | JSONB 映射类 |
| entity/system/BasicMessage.java | JSONB 映射类 |
| mapper/system/SystemSettingsMapper.java | 系统设置 Mapper |

### haifeng-admin 新增/修改

| 文件 | 职责 |
|------|------|
| resources/db/migration/V1__create_admin_tables.sql | 修改 t_member 表 |
| resources/db/migration/V2__create_system_settings.sql | 系统设置表 |
| dto/user/MemberQueryDTO.java | 用户查询参数 |
| dto/user/MemberStatusDTO.java | 状态修改参数 |
| vo/user/MemberListVO.java | 用户列表 VO |
| vo/user/MemberDetailVO.java | 用户详情 VO |
| service/user/MemberService.java | 用户管理接口 |
| service/impl/user/MemberServiceImpl.java | 用户管理实现 |
| controller/user/MemberController.java | 用户管理 Controller |
| dto/system/SystemSettingsUpdateDTO.java | 设置更新参数 |
| vo/system/SystemSettingsVO.java | 设置 VO |
| service/system/SystemSettingsService.java | 系统设置接口 |
| service/impl/system/SystemSettingsServiceImpl.java | 系统设置实现 |
| controller/system/SystemSettingsController.java | 系统设置 Controller |

---

## Task 1: 数据库迁移脚本

**Files:**
- Modify: `haifeng-admin/src/main/resources/db/migration/V1__create_admin_tables.sql:113-148`
- Create: `haifeng-admin/src/main/resources/db/migration/V2__create_system_settings.sql`

- [ ] **Step 1: 修改 V1 脚本，在 t_member 表中添加微信字段**

在 `V1__create_admin_tables.sql` 的 t_member 表定义中（第 130 行 `is_deleted` 之前）添加：

```sql
    wechat_id                   VARCHAR(255),                       -- 微信号(AES加密存储)
    wechat_id_index             VARCHAR(64),                        -- 微信号盲索引(SHA-256)
```

在索引部分（第 140 行之后）添加：

```sql
CREATE INDEX idx_member_wechat_index ON t_member(wechat_id_index) WHERE is_deleted = FALSE;
```

在注释部分（第 147 行之后）添加：

```sql
COMMENT ON COLUMN t_member.wechat_id IS '微信号(AES加密存储)';
COMMENT ON COLUMN t_member.wechat_id_index IS '微信号盲索引(SHA-256哈希，用于等值查询)';
```

- [ ] **Step 2: 创建 V2 系统设置表迁移脚本**

创建文件 `haifeng-admin/src/main/resources/db/migration/V2__create_system_settings.sql`：

```sql
-- V2__create_system_settings.sql
-- 系统设置表（单例模式）

CREATE TABLE system_settings (
    id                BIGSERIAL PRIMARY KEY,
    site_name         VARCHAR(50),
    site_url          VARCHAR(100),
    site_icp          VARCHAR(100),
    site_description  TEXT,
    api_number        INTEGER DEFAULT 3,
    pro_price         INTEGER DEFAULT 199,
    vip_price         INTEGER DEFAULT 599,
    seo_title         VARCHAR(200),
    seo_keywords      VARCHAR(100),
    seo_description   TEXT,
    contact_url       JSONB DEFAULT '{}',
    basic_message     JSONB DEFAULT '{}',
    created_at        TIMESTAMPTZ DEFAULT NOW(),
    updated_at        TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE system_settings IS '系统设置表（单例）';
COMMENT ON COLUMN system_settings.contact_url IS 'JSON格式：{wechat, weibo, zhihu, douyin, bilibili}';
COMMENT ON COLUMN system_settings.basic_message IS 'JSON格式：{address, phone, email, consultationTime}';

-- 插入默认配置（单例模式，只有一行）
INSERT INTO system_settings (id, site_name, site_description, contact_url, basic_message)
VALUES (
    1,
    '海峰未来规划院',
    '专业的高考志愿填报平台',
    '{"wechat": "", "weibo": "", "zhihu": "", "douyin": "", "bilibili": ""}',
    '{"address": "", "phone": "", "email": "", "consultationTime": ""}'
);
```

- [ ] **Step 3: 提交数据库迁移**

```bash
git add haifeng-admin/src/main/resources/db/migration/V1__create_admin_tables.sql
git add haifeng-admin/src/main/resources/db/migration/V2__create_system_settings.sql
git commit -m "feat(db): t_member添加微信加密字段，新增系统设置表"
```

---

## Task 2: 安全配置与工具类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/config/SecurityProperties.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/util/SpringContextHolder.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/util/CryptoUtil.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/util/DesensitizeUtil.java`

- [ ] **Step 1: 创建 SecurityProperties 配置类**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/config/SecurityProperties.java`：

```java
package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "haifeng.security")
public class SecurityProperties {

    /**
     * AES 加密密钥（必须是 16/24/32 位）
     */
    private String aesKey = "haifeng_aes_key_16";

    /**
     * 盲索引哈希盐值
     */
    private String hashSalt = "haifeng_blind_index_salt_2024";
}
```

- [ ] **Step 2: 创建 SpringContextHolder 工具类**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/util/SpringContextHolder.java`：

```java
package com.haifeng.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext 尚未初始化");
        }
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext 尚未初始化");
        }
        return applicationContext.getBean(name, clazz);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
```

- [ ] **Step 3: 创建 CryptoUtil 加解密工具类**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/util/CryptoUtil.java`：

```java
package com.haifeng.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CryptoUtil {

    private CryptoUtil() {
    }

    /**
     * AES 加密
     *
     * @param plainText 明文
     * @param key       密钥（16/24/32位）
     * @return 加密后的 Base64 字符串，输入为空返回 null
     */
    public static String encrypt(String plainText, String key) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try {
            AES aes = SecureUtil.aes(padKey(key));
            return aes.encryptBase64(plainText);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES 解密
     *
     * @param cipherText 密文（Base64）
     * @param key        密钥
     * @return 解密后的明文，输入为空返回 null
     */
    public static String decrypt(String cipherText, String key) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        try {
            AES aes = SecureUtil.aes(padKey(key));
            return aes.decryptStr(cipherText);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成盲索引（SHA-256）
     *
     * @param plainText 原文
     * @param salt      盐值
     * @return 64位十六进制哈希值，输入为空返回 null
     */
    public static String blindIndex(String plainText, String salt) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        String normalized = normalize(plainText);
        String salted = salt + normalized;
        return SecureUtil.sha256(salted);
    }

    /**
     * 规范化处理：转小写、去除首尾空格
     */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase();
    }

    /**
     * 补齐密钥到 16 位
     */
    private static byte[] padKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[16];
        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 16));
        return paddedKey;
    }
}
```

- [ ] **Step 4: 创建 DesensitizeUtil 脱敏工具类**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/util/DesensitizeUtil.java`：

```java
package com.haifeng.common.util;

public class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 微信号脱敏：保留前4位和后2位
     * 例如：wxid_abc123 -> wxid_***23
     *
     * @param wechatId 微信号
     * @return 脱敏后的微信号
     */
    public static String desensitizeWechat(String wechatId) {
        if (wechatId == null || wechatId.length() <= 6) {
            return wechatId;
        }
        int length = wechatId.length();
        String prefix = wechatId.substring(0, 4);
        String suffix = wechatId.substring(length - 2);
        return prefix + "***" + suffix;
    }

    /**
     * 手机号脱敏：保留前3位和后4位
     * 例如：13812345678 -> 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
```

- [ ] **Step 5: 提交工具类**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/config/SecurityProperties.java
git add haifeng-common/src/main/java/com/haifeng/common/util/SpringContextHolder.java
git add haifeng-common/src/main/java/com/haifeng/common/util/CryptoUtil.java
git add haifeng-common/src/main/java/com/haifeng/common/util/DesensitizeUtil.java
git commit -m "feat(common): 添加安全配置和加解密工具类"
```

---

## Task 3: TypeHandler 与操作日志

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/handler/AESEncryptTypeHandler.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/annotation/OperationLog.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/aspect/OperationLogAspect.java`

- [ ] **Step 1: 创建 AESEncryptTypeHandler**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/handler/AESEncryptTypeHandler.java`：

```java
package com.haifeng.common.handler;

import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.SpringContextHolder;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(String.class)
public class AESEncryptTypeHandler extends BaseTypeHandler<String> {

    private String getKey() {
        SecurityProperties properties = SpringContextHolder.getBean(SecurityProperties.class);
        return properties.getAesKey();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, CryptoUtil.encrypt(parameter, getKey()));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return CryptoUtil.decrypt(value, getKey());
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return CryptoUtil.decrypt(value, getKey());
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return CryptoUtil.decrypt(value, getKey());
    }
}
```

- [ ] **Step 2: 创建 OperationLog 注解**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/annotation/OperationLog.java`：

```java
package com.haifeng.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标注需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作描述
     */
    String action() default "";
}
```

- [ ] **Step 3: 创建 OperationLogAspect 切面**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/aspect/OperationLogAspect.java`：

```java
package com.haifeng.common.aspect;

import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("@annotation(com.haifeng.common.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        String module = operationLog.module();
        String action = operationLog.action();

        Long adminId = null;
        try {
            adminId = SecurityUtil.getCurrentAdminId();
        } catch (Exception e) {
            // 忽略获取管理员ID失败
        }

        String ip = getClientIp();
        String requestPath = getRequestPath();

        Object result = null;
        String status = "SUCCESS";
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = "FAIL";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[操作日志] 模块={}, 操作={}, 管理员ID={}, IP={}, 路径={}, 状态={}, 耗时={}ms, 错误={}",
                    module, action, adminId, ip, requestPath, status, costTime, errorMsg);
        }

        return result;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // 忽略
        }
        return "unknown";
    }

    private String getRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getRequestURI();
            }
        } catch (Exception e) {
            // 忽略
        }
        return "unknown";
    }
}
```

- [ ] **Step 4: 提交 TypeHandler 和日志切面**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/handler/AESEncryptTypeHandler.java
git add haifeng-common/src/main/java/com/haifeng/common/annotation/OperationLog.java
git add haifeng-common/src/main/java/com/haifeng/common/aspect/OperationLogAspect.java
git commit -m "feat(common): 添加AES加密TypeHandler和操作日志切面"
```

---

## Task 4: Entity 层修改

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/entity/user/Member.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/system/SystemSettings.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/system/ContactUrl.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/system/BasicMessage.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/system/SystemSettingsMapper.java`

- [ ] **Step 1: 修改 Member 实体，添加微信字段**

在 `Member.java` 中添加以下导入和字段：

添加导入：
```java
import com.baomidou.mybatisplus.annotation.TableField;
import com.haifeng.common.handler.AESEncryptTypeHandler;
```

在 `lastLoginIp` 字段之后添加：
```java
    @TableField(typeHandler = AESEncryptTypeHandler.class)
    private String wechatId;

    private String wechatIdIndex;
```

- [ ] **Step 2: 创建 ContactUrl JSONB 映射类**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/entity/system/ContactUrl.java`：

```java
package com.haifeng.common.entity.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactUrl implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信二维码URL
     */
    private String wechat;

    /**
     * 微博主页URL
     */
    private String weibo;

    /**
     * 知乎主页URL
     */
    private String zhihu;

    /**
     * 抖音主页URL
     */
    private String douyin;

    /**
     * B站主页URL
     */
    private String bilibili;
}
```

- [ ] **Step 3: 创建 BasicMessage JSONB 映射类**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/entity/system/BasicMessage.java`：

```java
package com.haifeng.common.entity.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 咨询时间
     */
    private String consultationTime;
}
```

- [ ] **Step 4: 创建 SystemSettings 实体**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/entity/system/SystemSettings.java`：

```java
package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "system_settings", autoResultMap = true)
public class SystemSettings {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String siteName;

    private String siteUrl;

    private String siteIcp;

    private String siteDescription;

    private Integer apiNumber;

    private Integer proPrice;

    private Integer vipPrice;

    private String seoTitle;

    private String seoKeywords;

    private String seoDescription;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private ContactUrl contactUrl;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private BasicMessage basicMessage;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: 创建 SystemSettingsMapper**

创建文件 `haifeng-common/src/main/java/com/haifeng/common/mapper/system/SystemSettingsMapper.java`：

```java
package com.haifeng.common.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.system.SystemSettings;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemSettingsMapper extends BaseMapper<SystemSettings> {
}
```

- [ ] **Step 6: 提交 Entity 层修改**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/Member.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/system/
git add haifeng-common/src/main/java/com/haifeng/common/mapper/system/
git commit -m "feat(entity): Member添加微信字段，新增SystemSettings实体"
```

---

## Task 5: 用户管理 DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/MemberQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/MemberStatusDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/MemberListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/MemberDetailVO.java`

- [ ] **Step 1: 创建 MemberQueryDTO**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/MemberQueryDTO.java`：

```java
package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQueryDTO extends BasePageQueryDTO {

    /**
     * 手机号（模糊查询）
     */
    private String phone;

    /**
     * 会员类型：normal/pro/vip
     */
    private String memberType;

    /**
     * 微信号（等值查询，后端转换为盲索引）
     */
    private String wechatId;

    /**
     * 账号状态：active/disabled
     */
    private String status;
}
```

- [ ] **Step 2: 创建 MemberStatusDTO**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/MemberStatusDTO.java`：

```java
package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberStatusDTO {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|disabled)$", message = "状态只能是 active 或 disabled")
    private String status;
}
```

- [ ] **Step 3: 创建 MemberListVO**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/MemberListVO.java`：

```java
package com.haifeng.admin.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MemberListVO {

    private Long id;

    private String username;

    private String phone;

    private String memberType;

    /**
     * 微信号（脱敏后）
     */
    private String wechatId;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 创建 MemberDetailVO**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/MemberDetailVO.java`：

```java
package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class MemberDetailVO {

    private Long id;

    private String username;

    private String avatar;

    private String phone;

    private String inviteCode;

    private String memberType;

    private OffsetDateTime expireAt;

    /**
     * 微信号（脱敏后）
     */
    private String wechatId;

    private Long referrerId;

    private String referrerUsername;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: 提交用户管理 DTO/VO**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/user/
git commit -m "feat(admin): 添加用户管理DTO和VO"
```

---

## Task 6: 用户管理 Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/user/MemberService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/MemberServiceImpl.java`

- [ ] **Step 1: 创建 MemberService 接口**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/service/user/MemberService.java`：

```java
package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;

public interface MemberService {

    /**
     * 分页查询用户列表
     */
    IPage<MemberListVO> page(MemberQueryDTO dto);

    /**
     * 获取用户详情
     */
    MemberDetailVO detail(Long id);

    /**
     * 修改用户状态
     */
    void updateStatus(Long id, MemberStatusDTO dto);

    /**
     * 获取用户微信明文（需记录操作日志）
     */
    String getWechatPlaintext(Long id);
}
```

- [ ] **Step 2: 创建 MemberServiceImpl 实现**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/MemberServiceImpl.java`：

```java
package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.service.user.MemberService;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<MemberListVO> page(MemberQueryDTO dto) {
        Page<Member> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, false);

        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(Member::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getMemberType())) {
            wrapper.eq(Member::getMemberType, dto.getMemberType());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Member::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            // 将微信号转换为盲索引进行等值查询
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(Member::getWechatIdIndex, blindIndex);
        }

        wrapper.orderByDesc(Member::getCreatedAt);

        IPage<Member> memberPage = memberMapper.selectPage(page, wrapper);

        return memberPage.convert(member -> {
            MemberListVO vo = new MemberListVO();
            BeanUtils.copyProperties(member, vo);
            // 微信号脱敏
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(member.getWechatId()));
            return vo;
        });
    }

    @Override
    public MemberDetailVO detail(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        MemberDetailVO vo = new MemberDetailVO();
        BeanUtils.copyProperties(member, vo);
        // 微信号脱敏
        vo.setWechatId(DesensitizeUtil.desensitizeWechat(member.getWechatId()));
        return vo;
    }

    @Override
    public void updateStatus(Long id, MemberStatusDTO dto) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        member.setStatus(dto.getStatus());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("修改用户状态成功: userId={}, status={}", id, dto.getStatus());
    }

    @Override
    public String getWechatPlaintext(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 微信号已通过 TypeHandler 自动解密
        return member.getWechatId();
    }
}
```

- [ ] **Step 3: 提交用户管理 Service**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/user/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/
git commit -m "feat(admin): 添加用户管理Service实现"
```

---

## Task 7: 用户管理 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberController.java`

- [ ] **Step 1: 创建 MemberController**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberController.java`：

```java
package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.service.user.MemberService;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public R<IPage<MemberListVO>> list(@Valid MemberQueryDTO dto) {
        return R.ok(memberService.page(dto));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<MemberDetailVO> detail(@PathVariable Long id) {
        return R.ok(memberService.detail(id));
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "用户管理", action = "修改用户状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody MemberStatusDTO dto) {
        memberService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 查看用户微信明文（强制记录操作日志）
     */
    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看用户微信明文")
    public R<String> getWechat(@PathVariable Long id) {
        return R.ok(memberService.getWechatPlaintext(id));
    }
}
```

- [ ] **Step 2: 提交用户管理 Controller**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberController.java
git commit -m "feat(admin): 添加用户管理Controller"
```

---

## Task 8: 系统设置 DTO/VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/SystemSettingsUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/system/SystemSettingsVO.java`

- [ ] **Step 1: 创建 SystemSettingsUpdateDTO**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/dto/system/SystemSettingsUpdateDTO.java`：

```java
package com.haifeng.admin.dto.system;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SystemSettingsUpdateDTO {

    @Size(max = 50, message = "网站名称最多50字符")
    private String siteName;

    @Size(max = 100, message = "网站Logo URL最多100字符")
    private String siteUrl;

    @Size(max = 100, message = "ICP备案号最多100字符")
    private String siteIcp;

    private String siteDescription;

    @Min(value = 1, message = "API调用次数最小为1")
    private Integer apiNumber;

    @Min(value = 0, message = "Pro会员价格不能为负")
    private Integer proPrice;

    @Min(value = 0, message = "VIP会员价格不能为负")
    private Integer vipPrice;

    @Size(max = 200, message = "SEO标题最多200字符")
    private String seoTitle;

    @Size(max = 100, message = "SEO关键词最多100字符")
    private String seoKeywords;

    private String seoDescription;

    /**
     * 社交媒体链接（整体替换）
     */
    private ContactUrl contactUrl;

    /**
     * 基本联系信息（整体替换）
     */
    private BasicMessage basicMessage;
}
```

- [ ] **Step 2: 创建 SystemSettingsVO**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/vo/system/SystemSettingsVO.java`：

```java
package com.haifeng.admin.vo.system;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SystemSettingsVO {

    private Long id;

    private String siteName;

    private String siteUrl;

    private String siteIcp;

    private String siteDescription;

    private Integer apiNumber;

    private Integer proPrice;

    private Integer vipPrice;

    private String seoTitle;

    private String seoKeywords;

    private String seoDescription;

    private ContactUrl contactUrl;

    private BasicMessage basicMessage;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 提交系统设置 DTO/VO**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/system/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/system/
git commit -m "feat(admin): 添加系统设置DTO和VO"
```

---

## Task 9: 系统设置 Service

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/system/SystemSettingsService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/system/SystemSettingsServiceImpl.java`

- [ ] **Step 1: 创建 SystemSettingsService 接口**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/service/system/SystemSettingsService.java`：

```java
package com.haifeng.admin.service.system;

import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.vo.system.SystemSettingsVO;

public interface SystemSettingsService {

    /**
     * 获取系统设置
     */
    SystemSettingsVO get();

    /**
     * 更新系统设置
     */
    void update(SystemSettingsUpdateDTO dto);
}
```

- [ ] **Step 2: 创建 SystemSettingsServiceImpl 实现**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/system/SystemSettingsServiceImpl.java`：

```java
package com.haifeng.admin.service.impl.system;

import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.service.system.SystemSettingsService;
import com.haifeng.admin.vo.system.SystemSettingsVO;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private static final Long SINGLETON_ID = 1L;

    private final SystemSettingsMapper settingsMapper;

    @Override
    public SystemSettingsVO get() {
        SystemSettings settings = settingsMapper.selectById(SINGLETON_ID);
        if (settings == null) {
            throw new BusinessException(404, "系统设置不存在");
        }

        SystemSettingsVO vo = new SystemSettingsVO();
        BeanUtils.copyProperties(settings, vo);
        return vo;
    }

    @Override
    public void update(SystemSettingsUpdateDTO dto) {
        SystemSettings settings = settingsMapper.selectById(SINGLETON_ID);
        if (settings == null) {
            throw new BusinessException(404, "系统设置不存在");
        }

        // 只更新非空字段
        if (dto.getSiteName() != null) {
            settings.setSiteName(dto.getSiteName());
        }
        if (dto.getSiteUrl() != null) {
            settings.setSiteUrl(dto.getSiteUrl());
        }
        if (dto.getSiteIcp() != null) {
            settings.setSiteIcp(dto.getSiteIcp());
        }
        if (dto.getSiteDescription() != null) {
            settings.setSiteDescription(dto.getSiteDescription());
        }
        if (dto.getApiNumber() != null) {
            settings.setApiNumber(dto.getApiNumber());
        }
        if (dto.getProPrice() != null) {
            settings.setProPrice(dto.getProPrice());
        }
        if (dto.getVipPrice() != null) {
            settings.setVipPrice(dto.getVipPrice());
        }
        if (dto.getSeoTitle() != null) {
            settings.setSeoTitle(dto.getSeoTitle());
        }
        if (dto.getSeoKeywords() != null) {
            settings.setSeoKeywords(dto.getSeoKeywords());
        }
        if (dto.getSeoDescription() != null) {
            settings.setSeoDescription(dto.getSeoDescription());
        }
        if (dto.getContactUrl() != null) {
            settings.setContactUrl(dto.getContactUrl());
        }
        if (dto.getBasicMessage() != null) {
            settings.setBasicMessage(dto.getBasicMessage());
        }

        settings.setUpdatedAt(OffsetDateTime.now());
        settingsMapper.updateById(settings);

        log.info("系统设置更新成功");
    }
}
```

- [ ] **Step 3: 提交系统设置 Service**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/system/
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/system/
git commit -m "feat(admin): 添加系统设置Service实现"
```

---

## Task 10: 系统设置 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/system/SystemSettingsController.java`

- [ ] **Step 1: 创建 SystemSettingsController**

创建文件 `haifeng-admin/src/main/java/com/haifeng/admin/controller/system/SystemSettingsController.java`：

```java
package com.haifeng.admin.controller.system;

import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.service.system.SystemSettingsService;
import com.haifeng.admin.vo.system.SystemSettingsVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/system/settings")
@RequiredArgsConstructor
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    /**
     * 获取系统设置
     */
    @GetMapping
    public R<SystemSettingsVO> get() {
        return R.ok(settingsService.get());
    }

    /**
     * 更新系统设置
     */
    @PutMapping
    @OperationLog(module = "系统管理", action = "更新系统设置")
    public R<Void> update(@Valid @RequestBody SystemSettingsUpdateDTO dto) {
        settingsService.update(dto);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交系统设置 Controller**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/system/SystemSettingsController.java
git commit -m "feat(admin): 添加系统设置Controller"
```

---

## Task 11: 配置文件更新

**Files:**
- Modify: `haifeng-admin/src/main/resources/application-dev.yml`
- Modify: `.env`

- [ ] **Step 1: 更新 application-dev.yml 添加安全配置**

在 `haifeng-admin/src/main/resources/application-dev.yml` 文件末尾添加：

```yaml

# 安全配置
haifeng:
  security:
    aes-key: ${AES_SECRET_KEY:haifeng_default_aes_key_16}
    hash-salt: ${HASH_SALT:haifeng_blind_index_salt_2024}
```

- [ ] **Step 2: 更新 .env 添加密钥配置**

在 `.env` 文件中添加（如果不存在则创建）：

```
# 微信号加密密钥（生产环境必须修改）
AES_SECRET_KEY=haifeng_prod_aes_key_16
HASH_SALT=haifeng_prod_blind_index_salt_2024
```

- [ ] **Step 3: 提交配置更新**

```bash
git add haifeng-admin/src/main/resources/application-dev.yml
git commit -m "feat(config): 添加AES加密和盲索引配置"
```

---

## Task 12: 文档与收尾

**Files:**
- Create: `Products/order2.md`

- [ ] **Step 1: 创建 order2.md 功能概述与接口文档**

创建文件 `Products/order2.md`：

```markdown
# Order 2: 系统设置与用户管理模块

## 概述

本次迭代实现了两个核心模块：

1. **系统设置模块** - 管理网站基本配置、SEO信息、社交媒体链接等
2. **用户管理模块** - 查看用户列表、详情，修改用户状态，查看用户微信明文

### 技术亮点

- **微信号加密存储** - 使用 AES-256 对称加密，防止数据库泄露
- **盲索引查询** - 使用 SHA-256 哈希实现加密字段的等值查询
- **操作日志审计** - 查看敏感信息（如微信明文）强制记录日志
- **JSONB 灵活存储** - 社交媒体链接和联系信息使用 PostgreSQL JSONB

---

## 接口文档

### 用户管理接口

#### 1. 分页查询用户列表

**请求**
```
GET /api/v1/admin/user/list
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |
| phone | string | 否 | 手机号（模糊查询） |
| memberType | string | 否 | 会员类型：normal/pro/vip |
| wechatId | string | 否 | 微信号（等值查询） |
| status | string | 否 | 状态：active/disabled |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 123456789,
        "username": "张三",
        "phone": "13812345678",
        "memberType": "vip",
        "wechatId": "wxid_***23",
        "status": "active",
        "lastLoginAt": "2026-05-06T10:00:00+08:00",
        "lastLoginIp": "192.168.1.1",
        "createdAt": "2026-01-01T00:00:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

#### 2. 获取用户详情

**请求**
```
GET /api/v1/admin/user/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123456789,
    "username": "张三",
    "avatar": "https://xxx.com/avatar.jpg",
    "phone": "13812345678",
    "inviteCode": "ABC12345",
    "memberType": "vip",
    "expireAt": "2027-05-06T00:00:00+08:00",
    "wechatId": "wxid_***23",
    "referrerId": 987654321,
    "referrerUsername": "李四",
    "commissionBalance": 100.00,
    "commissionTotalEarned": 500.00,
    "commissionTotalPaid": 400.00,
    "status": "active",
    "lastLoginAt": "2026-05-06T10:00:00+08:00",
    "lastLoginIp": "192.168.1.1",
    "createdAt": "2026-01-01T00:00:00+08:00",
    "updatedAt": "2026-05-06T10:00:00+08:00"
  }
}
```

#### 3. 修改用户状态

**请求**
```
PUT /api/v1/admin/user/{id}/status
Content-Type: application/json

{
  "status": "disabled"
}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

#### 4. 查看用户微信明文

**请求**
```
GET /api/v1/admin/user/{id}/wechat
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": "wxid_abc123456789"
}
```

> 注意：此接口会强制记录操作日志，包括操作人、时间、IP等信息。

---

### 系统设置接口

#### 1. 获取系统设置

**请求**
```
GET /api/v1/admin/system/settings
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "siteName": "海峰未来规划院",
    "siteUrl": "https://xxx.com/logo.png",
    "siteIcp": "京ICP备xxxxx号",
    "siteDescription": "专业的高考志愿填报平台",
    "apiNumber": 3,
    "proPrice": 199,
    "vipPrice": 599,
    "seoTitle": "海峰未来规划院 - 高考志愿填报",
    "seoKeywords": "高考,志愿填报,规划",
    "seoDescription": "专业的高考志愿填报平台...",
    "contactUrl": {
      "wechat": "https://xxx.com/wechat_qr.png",
      "weibo": "https://weibo.com/haifeng",
      "zhihu": "https://zhihu.com/haifeng",
      "douyin": "https://douyin.com/haifeng",
      "bilibili": "https://space.bilibili.com/123"
    },
    "basicMessage": {
      "address": "北京市海淀区xxx",
      "phone": "400-123-4567",
      "email": "contact@haifeng.com",
      "consultationTime": "周一至周五 9:00-18:00"
    },
    "updatedAt": "2026-05-06T10:00:00+08:00"
  }
}
```

#### 2. 更新系统设置

**请求**
```
PUT /api/v1/admin/system/settings
Content-Type: application/json

{
  "siteName": "海峰未来规划院",
  "siteUrl": "https://xxx.com/logo.png",
  "siteIcp": "京ICP备xxxxx号",
  "siteDescription": "专业的高考志愿填报平台",
  "apiNumber": 5,
  "proPrice": 199,
  "vipPrice": 599,
  "seoTitle": "海峰未来规划院 - 高考志愿填报",
  "seoKeywords": "高考,志愿填报,规划",
  "seoDescription": "专业的高考志愿填报平台...",
  "contactUrl": {
    "wechat": "https://xxx.com/wechat_qr.png",
    "weibo": "https://weibo.com/haifeng",
    "zhihu": "https://zhihu.com/haifeng",
    "douyin": "https://douyin.com/haifeng",
    "bilibili": "https://space.bilibili.com/123"
  },
  "basicMessage": {
    "address": "北京市海淀区xxx",
    "phone": "400-123-4567",
    "email": "contact@haifeng.com",
    "consultationTime": "周一至周五 9:00-18:00"
  }
}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

---

## 数据库变更

### t_member 表新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| wechat_id | VARCHAR(255) | 微信号（AES加密存储） |
| wechat_id_index | VARCHAR(64) | 微信号盲索引（SHA-256） |

### 新增 system_settings 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键（固定为1） |
| site_name | VARCHAR(50) | 网站名称 |
| site_url | VARCHAR(100) | 网站Logo URL |
| site_icp | VARCHAR(100) | ICP备案号 |
| site_description | TEXT | 网站描述 |
| api_number | INTEGER | API调用限制次数 |
| pro_price | INTEGER | Pro会员价格 |
| vip_price | INTEGER | VIP会员价格 |
| seo_title | VARCHAR(200) | SEO标题 |
| seo_keywords | VARCHAR(100) | SEO关键词 |
| seo_description | TEXT | SEO描述 |
| contact_url | JSONB | 社交媒体链接 |
| basic_message | JSONB | 基本联系信息 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

---

## 安全说明

1. **AES 加密密钥** - 通过环境变量 `AES_SECRET_KEY` 配置，生产环境必须修改
2. **盲索引盐值** - 通过环境变量 `HASH_SALT` 配置，生产环境必须修改
3. **操作日志** - 查看微信明文接口强制记录操作日志，便于审计追踪
4. **数据脱敏** - 列表和详情接口返回脱敏后的微信号
```

- [ ] **Step 2: 提交文档**

```bash
git add Products/order2.md
git commit -m "docs: 添加order2功能概述与接口文档"
```

- [ ] **Step 3: 最终验证**

启动项目验证：
```bash
cd haifeng-admin
mvn spring-boot:run
```

测试接口：
```bash
# 获取系统设置
curl http://localhost:8081/api/v1/admin/system/settings

# 获取用户列表
curl http://localhost:8081/api/v1/admin/user/list
```

---

## 完成检查清单

- [ ] V1 迁移脚本已修改（t_member 新增字段）
- [ ] V2 迁移脚本已创建（system_settings 表）
- [ ] 加密工具类已创建并测试
- [ ] TypeHandler 已配置并生效
- [ ] 用户管理接口可正常访问
- [ ] 系统设置接口可正常访问
- [ ] 微信号查询使用盲索引
- [ ] 查看微信明文有操作日志
- [ ] Products/order2.md 文档已完成
