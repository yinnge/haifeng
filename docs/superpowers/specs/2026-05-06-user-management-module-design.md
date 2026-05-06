# 用户管理模块设计文档

## Context

### 背景
海峰未来规划院是一个教育规划平台，核心功能为高考志愿填报。平台采用线下收费、线上管理的模式，管理员在后台为用户开通/续费会员，并管理推荐佣金和提现。

### 需求来源
- 需求文档：`Need/系统管理3.md`
- 数据库设计：`haifeng-admin/src/main/resources/db/migration/table/DataTable3.md`

### 目标
实现后台管理模块：**用户管理[父模块]** 下的子模块：
1. 用户列表（已实现，本次扩展会员开通/续费功能）
2. 订单/续费记录
3. 推荐佣金列表
4. 消息通知
5. 提现记录

---

## 一、数据库设计

### 1.1 新建迁移文件

**文件**：`V3__create_member_tables.sql`

**包含表**：

| 表名 | 说明 |
|------|------|
| member_orders | 会员订单表 |
| t_referral_commission | 推荐佣金表 |
| t_member_notification | 消息通知表 |
| t_withdraw_record | 提现记录表 |

### 1.2 表结构

#### member_orders（会员订单表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 雪花ID |
| order_no | VARCHAR(50) UNIQUE | 订单编号（ORD+年月日+雪花ID后8位） |
| member_id | BIGINT NOT NULL | 会员ID |
| member_name | VARCHAR(50) | 会员用户名（快照） |
| phone | VARCHAR(20) | 手机号（快照） |
| wechat_id | VARCHAR(255) | 微信号（AES加密，快照） |
| wechat_id_index | VARCHAR(64) | 微信号盲索引 |
| order_type | VARCHAR(20) | 订单类型：new/renewal |
| before_type | VARCHAR(20) | 操作前会员类型 |
| after_type | VARCHAR(20) | 操作后会员类型 |
| duration_months | INTEGER | 时长（月） |
| amount | DECIMAL(10,2) | 实收金额 |
| before_expire_at | TIMESTAMPTZ | 操作前到期时间 |
| after_expire_at | TIMESTAMPTZ | 操作后到期时间 |
| operator_id | BIGINT | 操作管理员ID |
| operator_name | VARCHAR(50) | 操作管理员姓名 |
| remark | TEXT | 备注 |
| is_deleted | BOOLEAN DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

#### t_referral_commission（推荐佣金表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 雪花ID |
| referrer_id | BIGINT NOT NULL | 推荐人ID |
| referrer_name | VARCHAR(50) | 推荐人姓名 |
| referrer_phone | VARCHAR(50) | 推荐人电话 |
| referee_id | BIGINT | 被推荐人ID |
| referee_name | VARCHAR(50) | 被推荐人姓名 |
| referee_phone | VARCHAR(50) | 被推荐人电话 |
| order_id | BIGINT | 关联订单ID |
| order_amount | DECIMAL(10,2) | 订单金额 |
| commission_rate | DECIMAL(5,2) | 佣金比例（如 10.00 表示 10%） |
| commission_amount | DECIMAL(10,2) | 佣金金额 |
| is_deleted | BOOLEAN DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

#### t_member_notification（消息通知表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 雪花ID |
| member_id | BIGINT NOT NULL | 接收人ID |
| notification_type | VARCHAR(30) | 消息类型 |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| related_id | BIGINT | 关联业务ID |
| is_read | BOOLEAN DEFAULT FALSE | 是否已读 |
| read_at | TIMESTAMPTZ | 已读时间 |
| is_deleted | BOOLEAN DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | 创建时间 |

**notification_type 枚举值**：
- `member_expire_soon` - 会员即将到期
- `member_expired` - 会员已过期
- `commission_earned` - 佣金到账
- `commission_paid` - 佣金已发放
- `system_notice` - 系统公告
- `member_renewed` - 会员续费成功
- `member_activation_success` - 会员开通成功

#### t_withdraw_record（提现记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 雪花ID |
| member_id | BIGINT NOT NULL | 用户ID |
| member_name | VARCHAR(50) | 用户姓名 |
| phone | VARCHAR(20) | 手机号（快照） |
| wechat_id | VARCHAR(255) | 微信号（AES加密，快照） |
| wechat_id_index | VARCHAR(64) | 微信号盲索引 |
| amount | DECIMAL(10,2) | 提现金额 |
| status | VARCHAR(20) DEFAULT 'pending' | 状态：pending/paid/rejected |
| operator_id | BIGINT | 操作管理员ID |
| operator_name | VARCHAR(50) | 操作管理员姓名 |
| remark | TEXT | 备注/拒绝原因 |
| is_deleted | BOOLEAN DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ | 申请时间 |
| updated_at | TIMESTAMPTZ | 处理时间 |

---

## 二、实体类与 Mapper

### 2.1 实体类

**位置**：`haifeng-common/src/main/java/com/haifeng/common/entity/user/`

| 文件 | 说明 |
|------|------|
| MemberOrder.java | 订单实体 |
| ReferralCommission.java | 佣金实体 |
| MemberNotification.java | 通知实体 |
| WithdrawRecord.java | 提现实体 |

### 2.2 枚举类

**位置**：`haifeng-common/src/main/java/com/haifeng/common/enums/`

| 枚举 | 值 |
|------|-----|
| OrderType | NEW, RENEWAL |
| MemberType | NORMAL, PRO, VIP |
| NotificationType | MEMBER_EXPIRE_SOON, MEMBER_EXPIRED, COMMISSION_EARNED, COMMISSION_PAID, SYSTEM_NOTICE, MEMBER_RENEWED, MEMBER_ACTIVATION_SUCCESS |
| WithdrawStatus | PENDING, PAID, REJECTED |

### 2.3 Mapper

**位置**：`haifeng-common/src/main/java/com/haifeng/common/mapper/user/`

| 文件 | 说明 |
|------|------|
| MemberOrderMapper.java | 继承 BaseMapper<MemberOrder> |
| ReferralCommissionMapper.java | 继承 BaseMapper<ReferralCommission> |
| MemberNotificationMapper.java | 继承 BaseMapper<MemberNotification> |
| WithdrawRecordMapper.java | 继承 BaseMapper<WithdrawRecord> |

---

## 三、API 接口设计

### 3.1 会员开通/续费（扩展 MemberController）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/admin/user/{id}/upgrade` | 开通/升级/续费会员 |

**请求体**：
```json
{
  "targetType": "pro",
  "durationMonths": 12,
  "amount": 199.00,
  "remark": "备注"
}
```

### 3.2 订单管理（MemberOrderController）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/user/order/list` | 订单列表 |
| GET | `/api/v1/admin/user/order/{id}` | 订单详情 |
| GET | `/api/v1/admin/user/order/{id}/wechat` | 查看订单微信明文 |
| DELETE | `/api/v1/admin/user/order/{id}` | 删除订单 |

### 3.3 佣金管理（CommissionController）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/user/commission/list` | 佣金列表 |
| DELETE | `/api/v1/admin/user/commission/{id}` | 删除记录 |

### 3.4 消息通知（NotificationController）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/user/notification/list` | 通知列表 |
| POST | `/api/v1/admin/user/notification/broadcast` | 群发系统公告 |
| DELETE | `/api/v1/admin/user/notification/{id}` | 删除通知 |

### 3.5 提现管理（WithdrawController）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/user/withdraw/list` | 提现列表 |
| GET | `/api/v1/admin/user/withdraw/{id}/wechat` | 查看提现微信明文 |
| PUT | `/api/v1/admin/user/withdraw/{id}/process` | 处理提现 |
| DELETE | `/api/v1/admin/user/withdraw/{id}` | 删除记录 |

---

## 四、核心业务流程

### 4.1 会员开通/续费流程

```
@Transactional 事务内执行：

1. 校验用户存在且未删除
2. 校验目标类型合法性：
   - normal → pro/vip ✅
   - pro → pro(续费)/vip(升级) ✅
   - vip → vip(续费) ✅
   - vip → pro ❌ (不允许降级)

3. 计算到期时间：
   - 未过期：newExpire = 原到期时间 + 时长
   - 已过期/新开通：newExpire = 当前时间 + 时长

4. 更新用户表 (t_member)

5. 创建订单记录 (member_orders)：
   - order_no = "ORD" + yyyyMMdd + 雪花ID后8位
   - order_type = 同类型用 RENEWAL，否则 NEW

6. 处理佣金（如有推荐人）：
   - 佣金比例：按最终会员类型取（pro用pro_rate，vip用vip_rate）
   - 更新推荐人：commission_balance += 佣金
   - 创建佣金记录

7. 创建通知：
   - 给用户：开通成功/续费成功
   - 给推荐人（如有）：佣金到账
```

### 4.2 提现处理流程

```
@Transactional 事务内执行：

1. 校验提现记录存在且状态为 PENDING

2. 根据 action 处理：
   【PAID - 通过打款】
   - 状态 → PAID
   - 用户 commission_total_paid += amount
   - 通知：佣金已发放

   【REJECTED - 拒绝】
   - 状态 → REJECTED
   - 退还余额：commission_balance += amount
   - 通知：提现被拒绝

3. 记录操作人信息
```

### 4.3 系统公告群发流程

```
1. 查询所有有效用户 (is_deleted=false, status=active)
2. 批量插入通知记录 (notification_type = SYSTEM_NOTICE)
3. 返回发送数量
```

---

## 五、DTO/VO 设计

### 5.1 位置
- DTO：`haifeng-admin/src/main/java/com/haifeng/admin/dto/user/`
- VO：`haifeng-admin/src/main/java/com/haifeng/admin/vo/user/`

### 5.2 文件清单

| 类型 | 文件 |
|------|------|
| DTO | MemberUpgradeDTO, OrderQueryDTO, CommissionQueryDTO, NotificationQueryDTO, NotificationBroadcastDTO, WithdrawQueryDTO, WithdrawProcessDTO |
| VO | OrderListVO, OrderDetailVO, CommissionListVO, NotificationListVO, WithdrawListVO |

### 5.3 微信号查询说明

微信号采用 AES 加密存储，**不支持模糊查询**，只能通过盲索引精确匹配：
```java
String blindIndex = CryptoUtil.blindIndex(inputWechatId, hashSalt);
wrapper.eq(Entity::getWechatIdIndex, blindIndex);
```

---

## 六、错误处理

### 6.1 业务校验

| 场景 | 错误码 | 错误信息 |
|------|--------|----------|
| 用户不存在 | 404 | 用户不存在 |
| 会员降级 | 400 | VIP会员不允许降级为Pro |
| VIP选择Pro | 400 | VIP会员只能续费VIP |
| 提现已处理 | 400 | 该提现记录已处理 |
| 处理动作无效 | 400 | 处理动作无效 |

### 6.2 操作日志

所有写操作添加 `@OperationLog` 注解，自动记录到 admin_logs 表。

---

## 七、文件结构

### 7.1 新增文件（约35个）

```
haifeng-admin/src/main/resources/db/migration/
└── V3__create_member_tables.sql

haifeng-common/src/main/java/com/haifeng/common/
├── entity/user/
│   ├── MemberOrder.java
│   ├── ReferralCommission.java
│   ├── MemberNotification.java
│   └── WithdrawRecord.java
├── mapper/user/
│   ├── MemberOrderMapper.java
│   ├── ReferralCommissionMapper.java
│   ├── MemberNotificationMapper.java
│   └── WithdrawRecordMapper.java
└── enums/
    ├── OrderType.java
    ├── MemberType.java
    ├── NotificationType.java
    └── WithdrawStatus.java

haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/user/
│   ├── MemberOrderController.java
│   ├── CommissionController.java
│   ├── NotificationController.java
│   └── WithdrawController.java
├── service/user/
│   ├── MemberOrderService.java
│   ├── CommissionService.java
│   ├── NotificationService.java
│   └── WithdrawService.java
├── service/impl/user/
│   ├── MemberOrderServiceImpl.java
│   ├── CommissionServiceImpl.java
│   ├── NotificationServiceImpl.java
│   └── WithdrawServiceImpl.java
├── dto/user/
│   ├── MemberUpgradeDTO.java
│   ├── OrderQueryDTO.java
│   ├── CommissionQueryDTO.java
│   ├── NotificationQueryDTO.java
│   ├── NotificationBroadcastDTO.java
│   ├── WithdrawQueryDTO.java
│   └── WithdrawProcessDTO.java
└── vo/user/
    ├── OrderListVO.java
    ├── OrderDetailVO.java
    ├── CommissionListVO.java
    ├── NotificationListVO.java
    └── WithdrawListVO.java
```

### 7.2 修改文件（2个）

- `MemberController.java` - 新增 upgrade 接口
- `MemberService.java` / `MemberServiceImpl.java` - 新增 upgradeMember 方法

---

## 八、验证方案

### 8.1 编译验证
```bash
mvn compile
```

### 8.2 启动验证
- Flyway 迁移成功（查看启动日志）
- 数据库表结构正确

### 8.3 接口测试用例

| 序号 | 测试场景 | 预期结果 |
|------|---------|---------|
| 1 | normal → Pro | 成功，生成订单 |
| 2 | normal → VIP | 成功，生成订单 |
| 3 | pro → Pro 续费 | 成功，order_type=RENEWAL |
| 4 | pro → VIP 升级 | 成功，order_type=NEW |
| 5 | vip → VIP 续费 | 成功，到期时间延长 |
| 6 | vip → Pro 降级 | 失败，返回 400 |
| 7 | 有推荐人开通会员 | 推荐人佣金增加，收到通知 |
| 8 | 无推荐人开通会员 | 不生成佣金记录 |
| 9 | 已过期用户续费 | 从当前时间计算到期 |
| 10 | 提现通过 | 状态 PAID，累计发放增加 |
| 11 | 提现拒绝 | 状态 REJECTED，余额退还 |
| 12 | 群发系统公告 | 所有活跃用户收到通知 |
| 13 | 微信号精确查询 | 能查到对应记录 |
| 14 | 查看微信明文 | 返回解密后的微信号 |

---

## 九、设计决策记录

| 问题 | 决策 | 理由 |
|------|------|------|
| Pro 是否可续费 | 可以续费也可以升级 | 用户需求 |
| 升级佣金比例 | 按最终会员类型 | 升级到 VIP 就用 VIP 比例 |
| 升级订单类型 | 记为 NEW | Pro→VIP 是开通新类型 |
| 过期续费起算 | 从当前时间 | 过期期间不补偿 |
| 提现拒绝处理 | 退还余额 | 用户体验友好 |
| 订单号格式 | ORD + 年月日 + 雪花ID后8位 | 简单唯一 |
| 架构方案 | 单事务同步处理 | 业务量不大，强一致性优先 |
