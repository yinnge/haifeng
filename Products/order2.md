# 系统设置与用户管理模块实施报告

## 概述

本次实现了管理端的两大核心功能模块：

1. **系统设置模块** - 单例配置管理，包含网站基本信息、SEO配置、会员价格、社交媒体链接等
2. **用户管理模块** - 用户列表查询、详情查看、状态修改，支持微信号 AES 加密存储和盲索引查询

---

## 技术亮点

### 1. 微信号 AES 加密存储
- 使用 Hutool 的 AES 算法加密存储微信号，保护用户隐私
- 通过 MyBatis-Plus 的 `TypeHandler` 实现透明加解密，业务代码无感知
- 密钥从环境变量读取，不硬编码

### 2. SHA-256 盲索引查询
- 对微信号生成 SHA-256 哈希值作为盲索引
- 支持通过微信号精确查询用户，无需解密全表数据
- 哈希前做规范化处理（转小写、去空格），避免大小写不一致导致查询失败

### 3. 操作日志 AOP 切面
- 通过 `@OperationLog` 注解标记需要记录日志的接口
- 自动记录操作人、操作时间、请求路径、执行结果
- 敏感操作（如查看微信明文）强制记录日志

### 4. JSONB 字段映射
- 使用 PostgreSQL 的 JSONB 类型存储复杂结构（社交链接、联系信息）
- 通过 `JacksonTypeHandler` 自动映射为 Java 对象
- 支持整体替换更新

### 5. 数据脱敏
- 微信号脱敏：保留前4位和后2位，中间用 `***` 替代
- 手机号脱敏：保留前3位和后4位，中间用 `****` 替代
- 列表和详情默认返回脱敏数据，明文查看需单独接口

---

## 接口文档

### 用户管理接口 (端口: 8081)

#### 1. 分页查询用户列表
```
GET /api/v1/admin/user/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页条数，可选：10,20,30,50,100,200,500,1000 |
| phone | String | 否 | **模糊查询** | 手机号，支持部分匹配 |
| memberType | String | 否 | **精准查询** | 会员类型：normal/pro/vip |
| wechatId | String | 否 | **精准查询** | 微信号（输入明文，后端自动转盲索引匹配） |
| status | String | 否 | **精准查询** | 账号状态：active/disabled |
| inviteCode | String | 否 | **模糊查询** | 邀请码，支持部分匹配 |

**查询条件汇总：**
| 查询方式 | 字段 | 说明 |
|----------|------|------|
| 模糊查询 | phone | 如输入"138"可匹配"13812345678" |
| 模糊查询 | inviteCode | 如输入"ABC"可匹配"ABC12345" |
| 精准查询 | memberType | 必须完全匹配：normal/pro/vip |
| 精准查询 | status | 必须完全匹配：active/disabled |
| 精准查询 | wechatId | 输入微信号明文，后端先用SHA-256转盲索引再等值匹配 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "username": "testuser",
        "phone": "138****5678",
        "memberType": "normal",
        "wechatId": "wxid***23",
        "status": "active",
        "lastLoginAt": "2026-05-06T10:00:00+08:00",
        "lastLoginIp": "192.168.1.1",
        "createdAt": "2026-05-01T08:00:00+08:00"
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
    "id": 1234567890,
    "username": "testuser",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "138****5678",
    "inviteCode": "ABC12345",
    "memberType": "pro",
    "expireAt": "2027-05-06T00:00:00+08:00",
    "wechatId": "wxid***23",
    "referrerId": 9876543210,
    "referrerUsername": "referrer",
    "commissionBalance": 100.00,
    "commissionTotalEarned": 500.00,
    "commissionTotalPaid": 400.00,
    "status": "active",
    "lastLoginAt": "2026-05-06T10:00:00+08:00",
    "lastLoginIp": "192.168.1.1",
    "createdAt": "2026-05-01T08:00:00+08:00",
    "updatedAt": "2026-05-06T10:00:00+08:00"
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
| targetType | String | 是 | 目标会员类型：pro 或 vip |
| durationMonths | Integer | 是 | 时长（月），范围：1-120 |
| amount | BigDecimal | 否 | 金额，不传则自动计算（基于系统设置的年价格按月折算） |
| remark | String | 否 | 备注 |

**金额计算规则：**
- 自动计算公式：`(年价格 / 12) × 月数`
- 年价格来源：系统设置的 `pro_price`（Pro会员）或 `vip_price`（VIP会员）
- 若手动传入 `amount`，则使用手动值（可用于打折等场景）

**请求示例（自动计算金额）：**
```json
{
  "targetType": "pro",
  "durationMonths": 12,
  "remark": "后台手动开通"
}
```

**请求示例（手动指定金额，如打折）：**
```json
{
  "targetType": "pro",
  "durationMonths": 12,
  "amount": 149.00,
  "remark": "优惠活动减免50元"
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

**响应说明：** `data` 返回订单ID（Long类型）

**操作日志：** 此接口自动记录操作日志

---

### 系统设置接口 (端口: 8081)

#### 1. 获取系统设置
```
GET /api/v1/admin/system/settings
Authorization: Bearer {accessToken}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "siteName": "海峰未来规划院",
    "siteUrl": "https://haifeng.com",
    "siteIcp": "京ICP备12345678号",
    "siteDescription": "专业的高考志愿填报平台",
    "apiNumber": 3,
    "providerName": "openai",
    "modelName": "gpt-4",
    "proPrice": 199,
    "vipPrice": 599,
    "proCommissionRate": 10,
    "vipCommissionRate": 15,
    "seoTitle": "海峰未来规划院 - 高考志愿填报专家",
    "seoKeywords": "高考,志愿填报,大学,专业",
    "seoDescription": "海峰未来规划院为您提供专业的高考志愿填报服务",
    "contactUrl": {
      "wechat": "https://example.com/wechat-qr.jpg",
      "weibo": "https://weibo.com/haifeng",
      "zhihu": "https://zhihu.com/haifeng",
      "douyin": "https://douyin.com/haifeng",
      "bilibili": "https://space.bilibili.com/haifeng"
    },
    "basicMessage": {
      "address": "北京市海淀区xxx大厦",
      "phone": "400-123-4567",
      "email": "contact@haifeng.com",
      "consultationTime": "周一至周五 9:00-18:00"
    },
    "updatedAt": "2026-05-06T10:00:00+08:00"
  },
  "timestamp": 1714300000000
}
```

---

#### 2. 更新系统设置
```
PUT /api/v1/admin/system/settings
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数（所有字段可选，只更新非空字段）：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| siteName | String | 否 | 网站名称（最多50字符） |
| siteUrl | String | 否 | 网站Logo URL（最多100字符） |
| siteIcp | String | 否 | ICP备案号（最多100字符） |
| siteDescription | String | 否 | 网站描述 |
| apiNumber | Integer | 否 | 每日AI API调用次数上限（最小1），默认3 |
| proPrice | Integer | 否 | Pro会员价格（不能为负） |
| vipPrice | Integer | 否 | VIP会员价格（不能为负） |
| proCommissionRate | Integer | 否 | Pro会员提成比例（0-100） |
| vipCommissionRate | Integer | 否 | VIP会员提成比例（0-100） |
| seoTitle | String | 否 | SEO标题（最多200字符） |
| seoKeywords | String | 否 | SEO关键词（最多100字符） |
| seoDescription | String | 否 | SEO描述 |
| contactUrl | Object | 否 | 社交媒体链接（整体替换） |
| basicMessage | Object | 否 | 基本联系信息（整体替换） |

**contactUrl 结构：**
| 字段 | 类型 | 说明 |
|------|------|------|
| wechat | String | 微信二维码URL |
| weibo | String | 微博主页URL |
| zhihu | String | 知乎主页URL |
| douyin | String | 抖音主页URL |
| bilibili | String | B站主页URL |

**basicMessage 结构：**
| 字段 | 类型 | 说明 |
|------|------|------|
| address | String | 公司地址 |
| phone | String | 联系电话 |
| email | String | 联系邮箱 |
| consultationTime | String | 咨询时间 |

**请求示例：**
```json
{
  "siteName": "海峰未来规划院",
  "proPrice": 299,
  "contactUrl": {
    "wechat": "https://example.com/new-wechat-qr.jpg",
    "weibo": "https://weibo.com/haifeng",
    "zhihu": "",
    "douyin": "",
    "bilibili": ""
  }
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

**操作日志：** 此接口自动记录操作日志

---

#### 3. 获取所有启用的服务商列表
```
GET /api/v1/admin/system/settings/providers
Authorization: Bearer {accessToken}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": ["openai", "deepseek", "zhipu"],
  "timestamp": 1714300000000
}
```

**响应说明：** `data` 返回去重后的服务商名称列表，只包含状态为启用的服务商

**操作日志：** 此接口自动记录操作日志

---

#### 4. 更新系统设置中的服务商和模型
```
PUT /api/v1/admin/system/settings/provider-model
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| providerName | String | 是 | 服务商名称，必须是已启用的服务商 |
| modelName | String | 是 | 模型名称，必须是该服务商下的有效模型 |

**验证规则：**
1. `providerName` 不能为空，必须存在于 `t_model_provider` 表中且状态为启用
2. `modelName` 不能为空，必须是该服务商下的有效模型
3. 如果服务商不存在或未启用，返回错误："所选服务商不存在或未启用"
4. 如果模型不属于所选服务商，返回错误："该模型不属于所选服务商"

**请求示例：**
```json
{
  "providerName": "openai",
  "modelName": "gpt-4"
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

**错误响应示例：**
```json
{
  "code": 400,
  "msg": "所选服务商不存在或未启用",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志 (端口: 8081)

> 操作日志用于记录管理员的所有操作行为，通过 `@OperationLog` 注解自动采集并持久化到 `admin_logs` 表。

#### 1. 分页查询操作日志
```
GET /api/v1/admin/system/logs/list
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，可选：10,20,30,50,100,200,500,1000 |
| adminName | String | 否 | 管理员姓名（**模糊查询**） |
| result | String | 否 | 操作结果：SUCCESS/FAIL |
| requestMethod | String | 否 | 请求方法：GET/POST/PUT/DELETE |

**模糊查询支持：**
- `adminName` - 支持模糊匹配，如输入"张"可匹配"张三"、"张小明"等

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "adminName": "admin",
        "operation": "用户管理 - 修改用户状态",
        "requestMethod": "PUT",
        "result": "SUCCESS",
        "ip": "192.168.1.100",
        "createdAt": "2026-05-09T14:30:00+08:00"
      }
    ],
    "total": 500,
    "size": 10,
    "current": 1,
    "pages": 50
  },
  "timestamp": 1714300000000
}
```

**列表展示字段：**
| 字段 | 说明 |
|------|------|
| adminName | 管理员姓名 |
| operation | 操作描述（格式：模块 - 操作） |
| requestMethod | 请求方法 |
| result | 操作结果 |
| ip | IP地址 |
| createdAt | 操作时间 |

---

#### 2. 获取操作日志详情
```
GET /api/v1/admin/system/logs/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 日志ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "adminId": 9876543210,
    "adminName": "admin",
    "operation": "用户管理 - 修改用户状态",
    "requestPath": "/api/v1/admin/user/123/status",
    "requestMethod": "PUT",
    "requestParams": "{\"status\":\"disabled\"}",
    "result": "SUCCESS",
    "errorMsg": null,
    "ip": "192.168.1.100",
    "createdAt": "2026-05-09T14:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

**详情展示字段（全部）：**
| 字段 | 说明 |
|------|------|
| id | 日志ID |
| adminId | 管理员ID |
| adminName | 管理员姓名 |
| operation | 操作描述 |
| requestPath | 请求路径 |
| requestMethod | 请求方法 |
| requestParams | 请求参数（敏感字段已脱敏，超过500字符自动截断） |
| result | 操作结果 |
| errorMsg | 错误信息（失败时有值） |
| ip | IP地址 |
| createdAt | 操作时间 |

---

#### 3. 批量删除操作日志
```
POST /api/v1/admin/system/logs/batch
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 是 | 删除类型：`ids` / `lastMonth` / `all` |
| ids | List\<Long\> | 条件 | 要删除的ID列表（type=ids时必填，不能为空，最多100条） |

**删除类型说明：**
| type值 | 说明 |
|--------|------|
| ids | 按ID批量删除（需提供ids数组） |
| lastMonth | 删除一个月前的日志 |
| all | 删除全部日志 |

**请求示例 - 按ID删除：**
```json
{
  "type": "ids",
  "ids": [1234567890123456789, 1234567890123456790, 1234567890123456791]
}
```

**请求示例 - 删除一个月前：**
```json
{
  "type": "lastMonth"
}
```

**请求示例 - 删除全部：**
```json
{
  "type": "all"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 150,
  "timestamp": 1714300000000
}
```

**响应说明：** `data` 返回删除的记录数（Integer类型）

---

## 数据库变更

### t_member 表新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| wechat_id | VARCHAR(255) | 微信号（AES加密存储） |
| wechat_id_index | VARCHAR(64) | 微信号盲索引（SHA-256哈希） |

**新增索引：**
```sql
CREATE INDEX idx_member_wechat_index ON t_member(wechat_id_index) WHERE is_deleted = FALSE;
```

### admin_logs 表（已存在，本次完善功能）

> 操作日志表，通过 AOP 切面自动记录管理员操作

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键（雪花算法生成） |
| admin_id | BIGINT | 管理员ID |
| admin_name | VARCHAR(50) | 管理员姓名 |
| operation | VARCHAR(100) | 操作描述（格式：模块 - 操作） |
| request_path | VARCHAR(200) | 请求路径 |
| request_method | VARCHAR(10) | 请求方法 |
| request_params | TEXT | 请求参数（敏感字段已脱敏） |
| result | VARCHAR(20) | 操作结果：SUCCESS/FAIL |
| error_msg | TEXT | 错误信息 |
| ip | VARCHAR(50) | IP地址 |
| created_at | TIMESTAMPTZ | 创建时间 |

**索引：**
```sql
CREATE INDEX idx_admin_logs_admin ON admin_logs(admin_id);
CREATE INDEX idx_admin_logs_created ON admin_logs(created_at);
```

---

### system_settings 表（新建）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键（单例模式，固定为1） |
| site_name | VARCHAR(50) | 网站名称 |
| site_url | VARCHAR(100) | 网站Logo URL |
| site_icp | VARCHAR(100) | ICP备案号 |
| site_description | TEXT | 网站描述 |
| api_number | INTEGER | 每日AI API调用次数上限，默认3 |
| provider_name | VARCHAR(50) | AI服务商名称 |
| model_name | VARCHAR(100) | AI模型名称 |
| pro_price | INTEGER | Pro会员价格，默认199 |
| vip_price | INTEGER | VIP会员价格，默认599 |
| pro_commission_rate | SMALLINT | Pro会员提成比例（0-100），默认10 |
| vip_commission_rate | SMALLINT | VIP会员提成比例（0-100），默认15 |
| seo_title | VARCHAR(200) | SEO标题 |
| seo_keywords | VARCHAR(100) | SEO关键词 |
| seo_description | TEXT | SEO描述 |
| contact_url | JSONB | 社交媒体链接 |
| basic_message | JSONB | 基本联系信息 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

---

## 安全说明

### 密钥管理

AES 加密密钥和盲索引盐值从环境变量读取：

```yaml
# application-dev.yml
haifeng:
  security:
    aes-key: ${HAIFENG_AES_KEY:haifeng_aes_key_16}
    hash-salt: ${HAIFENG_HASH_SALT:haifeng_blind_index_salt_2024}
```

**生产环境必须：**
1. 设置强随机密钥（AES密钥必须是16/24/32位）
2. 密钥不得硬编码或提交到代码仓库
3. 密钥变更后，已加密数据需要重新加密

### 敏感操作审计

以下操作强制记录操作日志：
- 修改用户状态
- 查看用户微信明文
- 更新系统设置
- 更新服务商和模型

---

## 文件清单

### haifeng-common 新增

| 文件 | 职责 |
|------|------|
| config/SecurityProperties.java | AES密钥和盐值配置 |
| util/SpringContextHolder.java | 静态获取Spring Bean |
| util/CryptoUtil.java | AES加解密 + SHA-256盲索引 |
| util/DesensitizeUtil.java | 数据脱敏工具 |
| handler/AESEncryptTypeHandler.java | MyBatis-Plus自动加解密 |
| annotation/OperationLog.java | 操作日志注解 |
| aspect/OperationLogAspect.java | 操作日志切面（持久化到数据库） |
| entity/system/SystemSettings.java | 系统设置实体 |
| entity/system/ContactUrl.java | JSONB映射类 |
| entity/system/BasicMessage.java | JSONB映射类 |
| mapper/system/SystemSettingsMapper.java | 系统设置Mapper |
| mapper/system/AdminLogMapper.java | 操作日志Mapper |

### haifeng-common 修改

| 文件 | 修改内容 |
|------|------|
| entity/user/Member.java | 新增 wechatId, wechatIdIndex 字段 |

### haifeng-admin 新增

| 文件 | 职责 |
|------|------|
| dto/user/MemberQueryDTO.java | 用户查询参数 |
| dto/user/MemberStatusDTO.java | 状态修改参数 |
| dto/user/MemberUpgradeDTO.java | 会员升级参数 |
| vo/user/MemberListVO.java | 用户列表VO |
| vo/user/MemberDetailVO.java | 用户详情VO |
| service/user/MemberService.java | 用户管理接口 |
| service/impl/user/MemberServiceImpl.java | 用户管理实现 |
| controller/user/MemberController.java | 用户管理Controller |
| dto/system/SystemSettingsUpdateDTO.java | 设置更新参数 |
| dto/system/ProviderModelUpdateDTO.java | 服务商和模型更新参数 |
| vo/system/SystemSettingsVO.java | 设置VO |
| service/system/SystemSettingsService.java | 系统设置接口 |
| service/impl/system/SystemSettingsServiceImpl.java | 系统设置实现 |
| controller/system/SystemSettingsController.java | 系统设置Controller |
| dto/system/AdminLogQueryDTO.java | 操作日志查询参数 |
| dto/system/AdminLogBatchDeleteDTO.java | 操作日志批量删除参数 |
| vo/system/AdminLogListVO.java | 操作日志列表VO |
| vo/system/AdminLogDetailVO.java | 操作日志详情VO |
| service/system/AdminLogService.java | 操作日志接口 |
| service/impl/system/AdminLogServiceImpl.java | 操作日志实现 |
| controller/system/AdminLogController.java | 操作日志Controller |

### 数据库迁移

| 文件 | 说明 |
|------|------|
| V1__create_admin_tables.sql | 修改：t_member添加微信字段 |
| V2__create_system_settings.sql | 新增：系统设置表 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在（用户不存在/系统设置不存在） |
| 500 | 服务器内部错误 |
