# 系统设置与用户管理模块设计文档

## 概述

本文档描述系统设置模块和用户管理模块的实现设计，包括微信号加密存储方案。

## 需求确认

| 项目 | 确认结果 |
|------|----------|
| 系统设置表 | 单例模式（id=1），只支持查询/更新 |
| 用户管理操作 | 分页列表 + 详情查询 + 修改状态 |
| 用户可修改字段 | 仅 status（active/disabled） |
| 密钥存储 | 环境变量（.env + application.yml） |
| JSONB 更新策略 | 整体替换 |
| 加密方案 | MyBatis-Plus TypeHandler（方案A） |

---

## 1. 数据库设计

### 1.1 修改 V1__create_admin_tables.sql

在 t_member 表中新增两个字段：

```sql
-- 在 t_member 表定义中添加
wechat_id           VARCHAR(255),                   -- 微信号(AES加密存储)
wechat_id_index     VARCHAR(64),                    -- 微信号盲索引(SHA-256)

-- 新增索引
CREATE INDEX idx_member_wechat_index ON t_member(wechat_id_index) WHERE is_deleted = FALSE;

-- 注释
COMMENT ON COLUMN t_member.wechat_id IS '微信号(AES加密存储)';
COMMENT ON COLUMN t_member.wechat_id_index IS '微信号盲索引(SHA-256哈希，用于等值查询)';
```

### 1.2 新建 V2__create_system_settings.sql

```sql
CREATE TABLE system_settings (
    id                BIGSERIAL PRIMARY KEY,
    site_name         VARCHAR(50),
    site_url          VARCHAR(100),
    site_icp          VARCHAR(100),
    site_description  TEXT,
    api_number        INTEGER DEFAULT 3,
    pro_price         INTEGER DEFAULT 199,
    vip_price         INTEGER DEFAULT 599,
    pro_commission_rate   SMALLINT DEFAULT 10 CHECK (pro_commission_rate >= 0 AND pro_commission_rate <= 100),
    vip_commission_rate   SMALLINT DEFAULT 15 CHECK (vip_commission_rate >= 0 AND vip_commission_rate <= 100),
    seo_title         VARCHAR(200),
    seo_keywords      VARCHAR(100),
    seo_description   TEXT,
    contact_url       JSONB DEFAULT '{}',
    basic_message     JSONB DEFAULT '{}',
    created_at        TIMESTAMPTZ DEFAULT NOW(),
    updated_at        TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE system_settings IS '系统设置表（单例）';
COMMENT ON COLUMN system_settings.contact_url IS 'JSON: {wechat, weibo, zhihu, douyin, bilibili}';
COMMENT ON COLUMN system_settings.basic_message IS 'JSON: {address, phone, email, consultationTime}';

-- 单例初始数据
INSERT INTO system_settings (id, site_name, site_description)
VALUES (1, '海峰未来规划院', '专业的高考志愿填报平台');
```

### 1.3 JSONB 字段结构

**contact_url:**
```json
{
  "wechat": "https://xxx.com/wechat_qr.png",
  "weibo": "https://weibo.com/haifeng",
  "zhihu": "https://zhihu.com/haifeng",
  "douyin": "https://douyin.com/haifeng",
  "bilibili": "https://space.bilibili.com/123"
}
```

**basic_message:**
```json
{
  "address": "北京市海淀区xxx",
  "phone": "400-123-4567",
  "email": "contact@haifeng.com",
  "consultationTime": "周一至周五 9:00-18:00"
}
```

---

## 2. 加密方案设计

### 2.1 配置

**application.yml:**
```yaml
haifeng:
  security:
    aes-key: ${AES_SECRET_KEY:haifeng_default_aes_key_16}
    hash-salt: ${HASH_SALT:haifeng_blind_index_salt_2024}
```

**.env 新增:**
```
AES_SECRET_KEY=your_16_or_32_char_key
HASH_SALT=your_random_salt_string
```

### 2.2 工具类

| 类名 | 位置 | 职责 |
|------|------|------|
| SecurityProperties | config/ | 读取配置属性 |
| SpringContextHolder | util/ | 获取Spring Bean |
| CryptoUtil | util/ | AES加解密 + SHA-256盲索引 |
| AESEncryptTypeHandler | handler/ | MyBatis-Plus自动加解密 |

### 2.3 加密流程

```
存储微信号:
  原文 "wxid_abc123"
    ↓ AES加密
  密文存入 wechat_id
    ↓ SHA-256(salt + normalize(原文))
  哈希存入 wechat_id_index

查询微信号:
  搜索词 "wxid_abc123"
    ↓ SHA-256(salt + normalize(搜索词))
  用哈希值匹配 wechat_id_index (=, 非LIKE)
```

### 2.4 脱敏规则

列表/详情返回时：`wxid_abc123` → `wxid_***23`（保留前4位和后2位）

---

## 3. 接口设计

### 3.1 用户管理接口

**路径前缀:** `/api/v1/admin/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页查询（phone, memberType, wechatId, inviteCode） |
| GET | `/{id}` | 获取详情（微信脱敏） |
| PUT | `/{id}/status` | 修改状态 |
| GET | `/{id}/wechat` | 查看微信明文（强制记录日志） |

### 3.2 系统设置接口

**路径前缀:** `/api/v1/admin/system/settings`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 获取设置 |
| PUT | `/` | 更新设置 |

---

## 4. 文件清单

### 4.1 haifeng-common

| 操作 | 路径 |
|------|------|
| 新增 | config/SecurityProperties.java |
| 新增 | util/SpringContextHolder.java |
| 新增 | util/CryptoUtil.java |
| 新增 | handler/AESEncryptTypeHandler.java |
| 修改 | entity/user/Member.java |
| 新增 | entity/system/SystemSettings.java |
| 新增 | entity/system/ContactUrl.java |
| 新增 | entity/system/BasicMessage.java |
| 新增 | mapper/system/SystemSettingsMapper.java |

### 4.2 haifeng-admin

| 操作 | 路径 |
|------|------|
| 修改 | resources/db/migration/V1__create_admin_tables.sql |
| 新增 | resources/db/migration/V2__create_system_settings.sql |
| 新增 | dto/user/MemberQueryDTO.java |
| 新增 | dto/user/MemberStatusDTO.java |
| 新增 | vo/user/MemberListVO.java |
| 新增 | vo/user/MemberDetailVO.java |
| 新增 | service/user/MemberService.java |
| 新增 | service/impl/user/MemberServiceImpl.java |
| 新增 | controller/user/MemberController.java |
| 新增 | dto/system/SystemSettingsUpdateDTO.java |
| 新增 | vo/system/SystemSettingsVO.java |
| 新增 | service/system/SystemSettingsService.java |
| 新增 | service/impl/system/SystemSettingsServiceImpl.java |
| 新增 | controller/system/SystemSettingsController.java |

### 4.3 其他

| 操作 | 路径 |
|------|------|
| 修改 | .env |
| 新增 | Products/order2.md |

---

## 5. 实现顺序

1. **基础设施层** - 数据库迁移、配置类、加密工具
2. **实体层** - Entity、JSONB映射类、Mapper
3. **用户管理模块** - DTO/VO、Service、Controller
4. **系统设置模块** - DTO/VO、Service、Controller
5. **配置与文档** - 环境变量、order2.md

---

## 6. 注意事项

1. **TypeHandler 获取配置** - 通过 SpringContextHolder 获取 SecurityProperties
2. **盲索引计算** - 在 Service 层手动调用 CryptoUtil.blindIndex()
3. **查看微信明文** - 必须使用 @OperationLog 记录审计日志
4. **系统设置单例** - 只有 id=1 一行数据，禁止新增删除
5. **JSONB 更新** - 前端传完整对象，后端整体替换
