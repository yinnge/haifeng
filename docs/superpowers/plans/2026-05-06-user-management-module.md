# 用户管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现用户管理模块的订单管理、佣金管理、消息通知、提现管理功能

**Architecture:** 采用单事务同步处理架构，会员开通/续费时在同一事务内完成用户更新、订单创建、佣金计算、通知发送。Entity/Mapper 放 haifeng-common，Controller/Service/DTO/VO 放 haifeng-admin。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL, Flyway, 雪花ID

**设计文档:** `docs/superpowers/specs/2026-05-06-user-management-module-design.md`

---

## 文件结构

### 新建文件（35个）

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

### 修改文件（4个）

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/user/MemberController.java      # 新增 upgrade 接口
├── service/user/MemberService.java            # 新增 upgradeMember 方法
└── service/impl/user/MemberServiceImpl.java   # 实现 upgradeMember
```

---

## Task 1: 数据库迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V3__create_member_tables.sql`

- [ ] **Step 1: 创建 V3 迁移文件**

```sql
-- ============================================================
-- V3: 用户管理模块表
-- ============================================================

-- 1. 会员订单表
CREATE TABLE IF NOT EXISTS member_orders (
    id                  BIGINT          PRIMARY KEY,
    order_no            VARCHAR(50)     NOT NULL UNIQUE,
    member_id           BIGINT          NOT NULL,
    member_name         VARCHAR(50)     NOT NULL,
    phone               VARCHAR(20)     NOT NULL,
    wechat_id           VARCHAR(255),
    wechat_id_index     VARCHAR(64),
    order_type          VARCHAR(20)     NOT NULL,
    before_type         VARCHAR(20),
    after_type          VARCHAR(20)     NOT NULL,
    duration_months     INTEGER         NOT NULL,
    amount              DECIMAL(10,2)   NOT NULL,
    before_expire_at    TIMESTAMPTZ,
    after_expire_at     TIMESTAMPTZ     NOT NULL,
    operator_id         BIGINT          NOT NULL,
    operator_name       VARCHAR(50)     NOT NULL,
    remark              TEXT,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_type CHECK (order_type IN ('new', 'renewal')),
    CONSTRAINT chk_order_before_type CHECK (before_type IS NULL OR before_type IN ('normal', 'pro', 'vip')),
    CONSTRAINT chk_order_after_type CHECK (after_type IN ('pro', 'vip'))
);

CREATE INDEX idx_orders_member_id ON member_orders(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_orders_order_no ON member_orders(order_no);
CREATE INDEX idx_orders_created_at ON member_orders(created_at DESC);
CREATE INDEX idx_orders_wechat_index ON member_orders(wechat_id_index) WHERE wechat_id_index IS NOT NULL;

COMMENT ON TABLE member_orders IS '会员订单表';
COMMENT ON COLUMN member_orders.order_no IS '订单编号（ORD+年月日+雪花ID后8位）';
COMMENT ON COLUMN member_orders.order_type IS '订单类型：new-新开通，renewal-续费';

-- 2. 推荐佣金表
CREATE TABLE IF NOT EXISTS t_referral_commission (
    id                  BIGINT          PRIMARY KEY,
    referrer_id         BIGINT          NOT NULL,
    referrer_name       VARCHAR(50),
    referrer_phone      VARCHAR(50),
    referee_id          BIGINT,
    referee_name        VARCHAR(50),
    referee_phone       VARCHAR(50),
    order_id            BIGINT,
    order_amount        DECIMAL(10,2)   NOT NULL,
    commission_rate     DECIMAL(5,2)    NOT NULL,
    commission_amount   DECIMAL(10,2)   NOT NULL,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_commission_amount CHECK (commission_amount >= 0)
);

CREATE INDEX idx_commission_referrer ON t_referral_commission(referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_commission_referee ON t_referral_commission(referee_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_commission_order ON t_referral_commission(order_id);
CREATE INDEX idx_commission_created ON t_referral_commission(created_at DESC);

COMMENT ON TABLE t_referral_commission IS '推荐佣金表';
COMMENT ON COLUMN t_referral_commission.commission_rate IS '佣金比例（如10.00表示10%）';

-- 3. 消息通知表
CREATE TABLE IF NOT EXISTS t_member_notification (
    id                  BIGINT          PRIMARY KEY,
    member_id           BIGINT          NOT NULL,
    notification_type   VARCHAR(30)     NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    content             TEXT,
    related_id          BIGINT,
    is_read             BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMPTZ,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notification_type CHECK (
        notification_type IN (
            'member_expire_soon',
            'member_expired',
            'commission_earned',
            'commission_paid',
            'system_notice',
            'member_renewed',
            'member_activation_success'
        )
    )
);

CREATE INDEX idx_notification_member ON t_member_notification(member_id, is_read, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_notification_type ON t_member_notification(notification_type);

COMMENT ON TABLE t_member_notification IS '消息通知表';
COMMENT ON COLUMN t_member_notification.notification_type IS '消息类型';
COMMENT ON COLUMN t_member_notification.related_id IS '关联业务ID（订单ID/提现ID）';

-- 4. 提现记录表
CREATE TABLE IF NOT EXISTS t_withdraw_record (
    id                  BIGINT          PRIMARY KEY,
    member_id           BIGINT          NOT NULL,
    member_name         VARCHAR(50)     NOT NULL,
    phone               VARCHAR(20)     NOT NULL,
    wechat_id           VARCHAR(255)    NOT NULL,
    wechat_id_index     VARCHAR(64)     NOT NULL,
    amount              DECIMAL(10,2)   NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'pending',
    operator_id         BIGINT,
    operator_name       VARCHAR(50),
    remark              TEXT,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_withdraw_status CHECK (status IN ('pending', 'paid', 'rejected')),
    CONSTRAINT chk_withdraw_amount CHECK (amount IN (50.00, 100.00))
);

CREATE INDEX idx_withdraw_member ON t_withdraw_record(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_status ON t_withdraw_record(status);
CREATE INDEX idx_withdraw_wechat ON t_withdraw_record(wechat_id_index);
CREATE INDEX idx_withdraw_created ON t_withdraw_record(created_at DESC);

COMMENT ON TABLE t_withdraw_record IS '提现记录表';
COMMENT ON COLUMN t_withdraw_record.status IS '状态：pending-待处理，paid-已打款，rejected-已拒绝';
```

- [ ] **Step 2: 提交迁移文件**

```bash
git add haifeng-admin/src/main/resources/db/migration/V3__create_member_tables.sql
git commit -m "feat(db): 添加用户管理模块数据表

- member_orders: 会员订单表
- t_referral_commission: 推荐佣金表
- t_member_notification: 消息通知表
- t_withdraw_record: 提现记录表"
```

---

## Task 2: 枚举类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/OrderType.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/MemberType.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/NotificationType.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/WithdrawStatus.java`

- [ ] **Step 1: 创建 OrderType 枚举**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OrderType {

    NEW("new", "新开通"),
    RENEWAL("renewal", "续费");

    @EnumValue
    private final String value;
    private final String desc;

    OrderType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
```

- [ ] **Step 2: 创建 MemberType 枚举**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MemberType {

    NORMAL("normal", "普通用户"),
    PRO("pro", "Pro会员"),
    VIP("vip", "VIP会员");

    @EnumValue
    private final String value;
    private final String desc;

    MemberType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MemberType fromValue(String value) {
        for (MemberType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return NORMAL;
    }
}
```

- [ ] **Step 3: 创建 NotificationType 枚举**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NotificationType {

    MEMBER_EXPIRE_SOON("member_expire_soon", "会员即将到期"),
    MEMBER_EXPIRED("member_expired", "会员已过期"),
    COMMISSION_EARNED("commission_earned", "佣金到账"),
    COMMISSION_PAID("commission_paid", "佣金已发放"),
    SYSTEM_NOTICE("system_notice", "系统公告"),
    MEMBER_RENEWED("member_renewed", "会员续费成功"),
    MEMBER_ACTIVATION_SUCCESS("member_activation_success", "会员开通成功");

    @EnumValue
    private final String value;
    private final String desc;

    NotificationType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
```

- [ ] **Step 4: 创建 WithdrawStatus 枚举**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum WithdrawStatus {

    PENDING("pending", "待处理"),
    PAID("paid", "已打款"),
    REJECTED("rejected", "已拒绝");

    @EnumValue
    private final String value;
    private final String desc;

    WithdrawStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
```

- [ ] **Step 5: 提交枚举类**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/enums/OrderType.java
git add haifeng-common/src/main/java/com/haifeng/common/enums/MemberType.java
git add haifeng-common/src/main/java/com/haifeng/common/enums/NotificationType.java
git add haifeng-common/src/main/java/com/haifeng/common/enums/WithdrawStatus.java
git commit -m "feat(common): 添加用户管理模块枚举类

- OrderType: 订单类型（new/renewal）
- MemberType: 会员类型（normal/pro/vip）
- NotificationType: 通知类型（7种）
- WithdrawStatus: 提现状态（pending/paid/rejected）"
```

---

## Task 3: Entity 实体类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberOrder.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/user/ReferralCommission.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberNotification.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/user/WithdrawRecord.java`

- [ ] **Step 1: 创建 MemberOrder 实体**

```java
package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.enums.MemberType;
import com.haifeng.common.enums.OrderType;
import com.haifeng.common.handler.AESEncryptTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "member_orders", autoResultMap = true)
public class MemberOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Long memberId;

    private String memberName;

    private String phone;

    @TableField(typeHandler = AESEncryptTypeHandler.class)
    private String wechatId;

    private String wechatIdIndex;

    private OrderType orderType;

    private MemberType beforeType;

    private MemberType afterType;

    private Integer durationMonths;

    private BigDecimal amount;

    private OffsetDateTime beforeExpireAt;

    private OffsetDateTime afterExpireAt;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 ReferralCommission 实体**

```java
package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_referral_commission")
public class ReferralCommission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long referrerId;

    private String referrerName;

    private String referrerPhone;

    private Long refereeId;

    private String refereeName;

    private String refereePhone;

    private Long orderId;

    private BigDecimal orderAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 MemberNotification 实体**

```java
package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_notification")
public class MemberNotification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    private NotificationType notificationType;

    private String title;

    private String content;

    private Long relatedId;

    private Boolean isRead;

    private OffsetDateTime readAt;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 创建 WithdrawRecord 实体**

```java
package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.enums.WithdrawStatus;
import com.haifeng.common.handler.AESEncryptTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_withdraw_record", autoResultMap = true)
public class WithdrawRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    private String memberName;

    private String phone;

    @TableField(typeHandler = AESEncryptTypeHandler.class)
    private String wechatId;

    private String wechatIdIndex;

    private BigDecimal amount;

    private WithdrawStatus status;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: 提交实体类**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberOrder.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/ReferralCommission.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberNotification.java
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/WithdrawRecord.java
git commit -m "feat(common): 添加用户管理模块实体类

- MemberOrder: 会员订单实体
- ReferralCommission: 推荐佣金实体
- MemberNotification: 消息通知实体
- WithdrawRecord: 提现记录实体"
```

---

## Task 4: Mapper 接口

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberOrderMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/user/ReferralCommissionMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberNotificationMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/user/WithdrawRecordMapper.java`

- [ ] **Step 1: 创建 MemberOrderMapper**

```java
package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.MemberOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberOrderMapper extends BaseMapper<MemberOrder> {
}
```

- [ ] **Step 2: 创建 ReferralCommissionMapper**

```java
package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.ReferralCommission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReferralCommissionMapper extends BaseMapper<ReferralCommission> {
}
```

- [ ] **Step 3: 创建 MemberNotificationMapper**

```java
package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.MemberNotification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberNotificationMapper extends BaseMapper<MemberNotification> {
}
```

- [ ] **Step 4: 创建 WithdrawRecordMapper**

```java
package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.WithdrawRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WithdrawRecordMapper extends BaseMapper<WithdrawRecord> {
}
```

- [ ] **Step 5: 提交 Mapper 接口**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberOrderMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/user/ReferralCommissionMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberNotificationMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/user/WithdrawRecordMapper.java
git commit -m "feat(common): 添加用户管理模块Mapper接口"
```

---

## Task 5: DTO 类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/MemberUpgradeDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/OrderQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/CommissionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/NotificationQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/NotificationBroadcastDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/WithdrawQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/user/WithdrawProcessDTO.java`

- [ ] **Step 1: 创建 MemberUpgradeDTO**

```java
package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberUpgradeDTO {

    @NotBlank(message = "目标会员类型不能为空")
    @Pattern(regexp = "pro|vip", message = "目标会员类型只能是pro或vip")
    private String targetType;

    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长最少1个月")
    @Max(value = 120, message = "时长最多120个月")
    private Integer durationMonths;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    private String remark;
}
```

- [ ] **Step 2: 创建 OrderQueryDTO**

```java
package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends BasePageQueryDTO {

    private String phone;

    private String wechatId;

    private String operatorName;

    private String orderType;
}
```

- [ ] **Step 3: 创建 CommissionQueryDTO**

```java
package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionQueryDTO extends BasePageQueryDTO {

    private String referrerPhone;

    private String referrerName;

    private String refereePhone;

    private String refereeName;

    private String orderNo;
}
```

- [ ] **Step 4: 创建 NotificationQueryDTO**

```java
package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends BasePageQueryDTO {

    private String notificationType;

    private Long memberId;

    private Boolean isRead;
}
```

- [ ] **Step 5: 创建 NotificationBroadcastDTO**

```java
package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationBroadcastDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 5000, message = "内容最多5000字符")
    private String content;
}
```

- [ ] **Step 6: 创建 WithdrawQueryDTO**

```java
package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawQueryDTO extends BasePageQueryDTO {

    private String memberName;

    private String phone;

    private String wechatId;

    private String status;
}
```

- [ ] **Step 7: 创建 WithdrawProcessDTO**

```java
package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WithdrawProcessDTO {

    @NotBlank(message = "处理动作不能为空")
    @Pattern(regexp = "paid|rejected", message = "处理动作只能是paid或rejected")
    private String action;

    private String remark;
}
```

- [ ] **Step 8: 提交 DTO 类**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/MemberUpgradeDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/OrderQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/CommissionQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/NotificationQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/NotificationBroadcastDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/WithdrawQueryDTO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/user/WithdrawProcessDTO.java
git commit -m "feat(admin): 添加用户管理模块DTO类

- MemberUpgradeDTO: 会员开通/续费请求
- OrderQueryDTO: 订单查询条件
- CommissionQueryDTO: 佣金查询条件
- NotificationQueryDTO: 通知查询条件
- NotificationBroadcastDTO: 群发公告请求
- WithdrawQueryDTO: 提现查询条件
- WithdrawProcessDTO: 提现处理请求"
```

---

## Task 6: VO 类

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/OrderListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/OrderDetailVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/CommissionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/NotificationListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/user/WithdrawListVO.java`

- [ ] **Step 1: 创建 OrderListVO**

```java
package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class OrderListVO {

    private Long id;

    private String orderNo;

    private String memberName;

    private String phone;

    private String wechatId;

    private String orderType;

    private String beforeType;

    private String afterType;

    private Integer durationMonths;

    private BigDecimal amount;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: 创建 OrderDetailVO**

```java
package com.haifeng.admin.vo.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderListVO {

    private Long memberId;

    private OffsetDateTime beforeExpireAt;

    private OffsetDateTime afterExpireAt;

    private Long operatorId;

    private String operatorName;

    private String remark;
}
```

- [ ] **Step 3: 创建 CommissionListVO**

```java
package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CommissionListVO {

    private Long id;

    private String referrerName;

    private String referrerPhone;

    private String refereeName;

    private String refereePhone;

    private Long orderId;

    private BigDecimal orderAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 4: 创建 NotificationListVO**

```java
package com.haifeng.admin.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationListVO {

    private Long id;

    private Long memberId;

    private String memberName;

    private String notificationType;

    private String title;

    private String content;

    private Boolean isRead;

    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;
}
```

- [ ] **Step 5: 创建 WithdrawListVO**

```java
package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class WithdrawListVO {

    private Long id;

    private Long memberId;

    private String memberName;

    private String phone;

    private String wechatId;

    private BigDecimal amount;

    private String status;

    private String operatorName;

    private String remark;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: 提交 VO 类**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/user/OrderListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/user/OrderDetailVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/user/CommissionListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/user/NotificationListVO.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/user/WithdrawListVO.java
git commit -m "feat(admin): 添加用户管理模块VO类

- OrderListVO/OrderDetailVO: 订单列表/详情
- CommissionListVO: 佣金列表
- NotificationListVO: 通知列表
- WithdrawListVO: 提现列表"
```

---

## Task 7: Service 接口

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/user/MemberOrderService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/user/CommissionService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/user/NotificationService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/user/WithdrawService.java`

- [ ] **Step 1: 创建 MemberOrderService**

```java
package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;

public interface MemberOrderService {

    IPage<OrderListVO> page(OrderQueryDTO dto);

    OrderDetailVO detail(Long id);

    String getWechatPlaintext(Long id);

    void delete(Long id);
}
```

- [ ] **Step 2: 创建 CommissionService**

```java
package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.CommissionQueryDTO;
import com.haifeng.admin.vo.user.CommissionListVO;

public interface CommissionService {

    IPage<CommissionListVO> page(CommissionQueryDTO dto);

    void delete(Long id);
}
```

- [ ] **Step 3: 创建 NotificationService**

```java
package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.enums.NotificationType;

public interface NotificationService {

    IPage<NotificationListVO> page(NotificationQueryDTO dto);

    int broadcast(NotificationBroadcastDTO dto);

    void delete(Long id);

    void sendNotification(Long memberId, NotificationType type, String title, String content, Long relatedId);
}
```

- [ ] **Step 4: 创建 WithdrawService**

```java
package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.vo.user.WithdrawListVO;

public interface WithdrawService {

    IPage<WithdrawListVO> page(WithdrawQueryDTO dto);

    String getWechatPlaintext(Long id);

    void process(Long id, WithdrawProcessDTO dto);

    void delete(Long id);
}
```

- [ ] **Step 5: 提交 Service 接口**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/user/MemberOrderService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/user/CommissionService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/user/NotificationService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/user/WithdrawService.java
git commit -m "feat(admin): 添加用户管理模块Service接口"
```

---

## Task 8: MemberOrderServiceImpl 实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/MemberOrderServiceImpl.java`

- [ ] **Step 1: 创建 MemberOrderServiceImpl**

```java
package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.service.user.MemberOrderService;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberOrderMapper;
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
public class MemberOrderServiceImpl implements MemberOrderService {

    private final MemberOrderMapper memberOrderMapper;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<OrderListVO> page(OrderQueryDTO dto) {
        Page<MemberOrder> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getDeleted, false);

        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(MemberOrder::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(MemberOrder::getWechatIdIndex, blindIndex);
        }
        if (StringUtils.hasText(dto.getOperatorName())) {
            wrapper.like(MemberOrder::getOperatorName, dto.getOperatorName());
        }
        if (StringUtils.hasText(dto.getOrderType())) {
            wrapper.eq(MemberOrder::getOrderType, dto.getOrderType());
        }

        wrapper.orderByDesc(MemberOrder::getCreatedAt);

        IPage<MemberOrder> orderPage = memberOrderMapper.selectPage(page, wrapper);

        return orderPage.convert(order -> {
            OrderListVO vo = new OrderListVO();
            BeanUtils.copyProperties(order, vo);
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(order.getWechatId()));
            vo.setOrderType(order.getOrderType() != null ? order.getOrderType().getValue() : null);
            vo.setBeforeType(order.getBeforeType() != null ? order.getBeforeType().getValue() : null);
            vo.setAfterType(order.getAfterType() != null ? order.getAfterType().getValue() : null);
            return vo;
        });
    }

    @Override
    public OrderDetailVO detail(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setWechatId(DesensitizeUtil.desensitizeWechat(order.getWechatId()));
        vo.setOrderType(order.getOrderType() != null ? order.getOrderType().getValue() : null);
        vo.setBeforeType(order.getBeforeType() != null ? order.getBeforeType().getValue() : null);
        vo.setAfterType(order.getAfterType() != null ? order.getAfterType().getValue() : null);
        return vo;
    }

    @Override
    public String getWechatPlaintext(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(404, "订单不存在");
        }
        return order.getWechatId();
    }

    @Override
    public void delete(Long id) {
        MemberOrder order = memberOrderMapper.selectById(id);
        if (order == null || order.getDeleted()) {
            throw new BusinessException(404, "订单不存在");
        }

        order.setDeleted(true);
        order.setUpdatedAt(OffsetDateTime.now());
        memberOrderMapper.updateById(order);

        log.info("删除订单: orderId={}", id);
    }
}
```

- [ ] **Step 2: 提交 MemberOrderServiceImpl**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/MemberOrderServiceImpl.java
git commit -m "feat(admin): 实现MemberOrderServiceImpl

- 订单分页查询（支持手机号/微信号/操作员/类型筛选）
- 订单详情
- 查看微信明文
- 软删除"
```

---

## Task 9: CommissionServiceImpl 实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/CommissionServiceImpl.java`

- [ ] **Step 1: 创建 CommissionServiceImpl**

```java
package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.CommissionQueryDTO;
import com.haifeng.admin.service.user.CommissionService;
import com.haifeng.admin.vo.user.CommissionListVO;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.user.ReferralCommission;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final ReferralCommissionMapper commissionMapper;
    private final MemberOrderMapper orderMapper;

    @Override
    public IPage<CommissionListVO> page(CommissionQueryDTO dto) {
        Page<ReferralCommission> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<ReferralCommission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReferralCommission::getDeleted, false);

        if (StringUtils.hasText(dto.getReferrerPhone())) {
            wrapper.like(ReferralCommission::getReferrerPhone, dto.getReferrerPhone());
        }
        if (StringUtils.hasText(dto.getReferrerName())) {
            wrapper.like(ReferralCommission::getReferrerName, dto.getReferrerName());
        }
        if (StringUtils.hasText(dto.getRefereePhone())) {
            wrapper.like(ReferralCommission::getRefereePhone, dto.getRefereePhone());
        }
        if (StringUtils.hasText(dto.getRefereeName())) {
            wrapper.like(ReferralCommission::getRefereeName, dto.getRefereeName());
        }
        if (StringUtils.hasText(dto.getOrderNo())) {
            MemberOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<MemberOrder>()
                    .eq(MemberOrder::getOrderNo, dto.getOrderNo())
                    .eq(MemberOrder::getDeleted, false)
            );
            if (order != null) {
                wrapper.eq(ReferralCommission::getOrderId, order.getId());
            } else {
                wrapper.eq(ReferralCommission::getId, -1L);
            }
        }

        wrapper.orderByDesc(ReferralCommission::getCreatedAt);

        IPage<ReferralCommission> commissionPage = commissionMapper.selectPage(page, wrapper);

        return commissionPage.convert(commission -> {
            CommissionListVO vo = new CommissionListVO();
            BeanUtils.copyProperties(commission, vo);
            return vo;
        });
    }

    @Override
    public void delete(Long id) {
        ReferralCommission commission = commissionMapper.selectById(id);
        if (commission == null || commission.getDeleted()) {
            throw new BusinessException(404, "佣金记录不存在");
        }

        commission.setDeleted(true);
        commission.setUpdatedAt(OffsetDateTime.now());
        commissionMapper.updateById(commission);

        log.info("删除佣金记录: commissionId={}", id);
    }
}
```

- [ ] **Step 2: 提交 CommissionServiceImpl**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/CommissionServiceImpl.java
git commit -m "feat(admin): 实现CommissionServiceImpl

- 佣金分页查询（支持推荐人/被推荐人/订单号筛选）
- 软删除"
```

---

## Task 10: NotificationServiceImpl 实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/NotificationServiceImpl.java`

- [ ] **Step 1: 创建 NotificationServiceImpl**

```java
package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberNotificationMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MemberNotificationMapper notificationMapper;
    private final MemberMapper memberMapper;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public IPage<NotificationListVO> page(NotificationQueryDTO dto) {
        Page<MemberNotification> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<MemberNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberNotification::getDeleted, false);

        if (StringUtils.hasText(dto.getNotificationType())) {
            wrapper.eq(MemberNotification::getNotificationType, dto.getNotificationType());
        }
        if (dto.getMemberId() != null) {
            wrapper.eq(MemberNotification::getMemberId, dto.getMemberId());
        }
        if (dto.getIsRead() != null) {
            wrapper.eq(MemberNotification::getIsRead, dto.getIsRead());
        }

        wrapper.orderByDesc(MemberNotification::getCreatedAt);

        IPage<MemberNotification> notificationPage = notificationMapper.selectPage(page, wrapper);

        return notificationPage.convert(notification -> {
            NotificationListVO vo = new NotificationListVO();
            BeanUtils.copyProperties(notification, vo);
            vo.setNotificationType(notification.getNotificationType() != null
                ? notification.getNotificationType().getValue() : null);

            Member member = memberMapper.selectById(notification.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getUsername());
            }
            return vo;
        });
    }

    @Override
    @Transactional
    public int broadcast(NotificationBroadcastDTO dto) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, false);
        wrapper.eq(Member::getStatus, "active");

        List<Member> members = memberMapper.selectList(wrapper);

        if (members.isEmpty()) {
            return 0;
        }

        List<MemberNotification> notifications = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (Member member : members) {
            MemberNotification notification = MemberNotification.builder()
                .id(idGenerator.nextId())
                .memberId(member.getId())
                .notificationType(NotificationType.SYSTEM_NOTICE)
                .title(dto.getTitle())
                .content(dto.getContent())
                .isRead(false)
                .deleted(false)
                .createdAt(now)
                .build();
            notifications.add(notification);
        }

        for (MemberNotification notification : notifications) {
            notificationMapper.insert(notification);
        }

        log.info("系统公告群发: title={}, count={}", dto.getTitle(), notifications.size());
        return notifications.size();
    }

    @Override
    public void delete(Long id) {
        MemberNotification notification = notificationMapper.selectById(id);
        if (notification == null || notification.getDeleted()) {
            throw new BusinessException(404, "通知记录不存在");
        }

        notification.setDeleted(true);
        notificationMapper.updateById(notification);

        log.info("删除通知: notificationId={}", id);
    }

    @Override
    public void sendNotification(Long memberId, NotificationType type, String title, String content, Long relatedId) {
        MemberNotification notification = MemberNotification.builder()
            .id(idGenerator.nextId())
            .memberId(memberId)
            .notificationType(type)
            .title(title)
            .content(content)
            .relatedId(relatedId)
            .isRead(false)
            .deleted(false)
            .createdAt(OffsetDateTime.now())
            .build();
        notificationMapper.insert(notification);
    }
}
```

- [ ] **Step 2: 提交 NotificationServiceImpl**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/NotificationServiceImpl.java
git commit -m "feat(admin): 实现NotificationServiceImpl

- 通知分页查询（支持类型/用户/已读状态筛选）
- 系统公告群发
- 内部发送通知方法
- 软删除"
```

---

## Task 11: WithdrawServiceImpl 实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/WithdrawServiceImpl.java`

- [ ] **Step 1: 创建 WithdrawServiceImpl**

```java
package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.service.user.WithdrawService;
import com.haifeng.admin.vo.user.WithdrawListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.WithdrawRecord;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.enums.WithdrawStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.WithdrawRecordMapper;
import com.haifeng.common.security.SecurityUtil;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final WithdrawRecordMapper withdrawMapper;
    private final MemberMapper memberMapper;
    private final NotificationService notificationService;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<WithdrawListVO> page(WithdrawQueryDTO dto) {
        Page<WithdrawRecord> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<WithdrawRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WithdrawRecord::getDeleted, false);

        if (StringUtils.hasText(dto.getMemberName())) {
            wrapper.like(WithdrawRecord::getMemberName, dto.getMemberName());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(WithdrawRecord::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(WithdrawRecord::getWechatIdIndex, blindIndex);
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(WithdrawRecord::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(WithdrawRecord::getCreatedAt);

        IPage<WithdrawRecord> withdrawPage = withdrawMapper.selectPage(page, wrapper);

        return withdrawPage.convert(withdraw -> {
            WithdrawListVO vo = new WithdrawListVO();
            BeanUtils.copyProperties(withdraw, vo);
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(withdraw.getWechatId()));
            vo.setStatus(withdraw.getStatus() != null ? withdraw.getStatus().getValue() : null);
            return vo;
        });
    }

    @Override
    public String getWechatPlaintext(Long id) {
        WithdrawRecord withdraw = withdrawMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(404, "提现记录不存在");
        }
        return withdraw.getWechatId();
    }

    @Override
    @Transactional
    public void process(Long id, WithdrawProcessDTO dto) {
        WithdrawRecord withdraw = withdrawMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(404, "提现记录不存在");
        }
        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new BusinessException(400, "该提现记录已处理");
        }

        Member member = memberMapper.selectById(withdraw.getMemberId());
        if (member == null) {
            throw new BusinessException(404, "用户不存在");
        }

        Long operatorId = SecurityUtil.getCurrentAdminId();
        String operatorName = SecurityUtil.getCurrentAdminName();
        OffsetDateTime now = OffsetDateTime.now();

        if ("paid".equals(dto.getAction())) {
            withdraw.setStatus(WithdrawStatus.PAID);
            member.setCommissionTotalPaid(member.getCommissionTotalPaid().add(withdraw.getAmount()));
            memberMapper.updateById(member);

            notificationService.sendNotification(
                member.getId(),
                NotificationType.COMMISSION_PAID,
                "佣金已发放",
                String.format("您申请的%.2f元佣金已发放至您的微信，请注意查收。", withdraw.getAmount()),
                withdraw.getId()
            );

            log.info("提现通过: withdrawId={}, memberId={}, amount={}", id, member.getId(), withdraw.getAmount());

        } else if ("rejected".equals(dto.getAction())) {
            withdraw.setStatus(WithdrawStatus.REJECTED);
            member.setCommissionBalance(member.getCommissionBalance().add(withdraw.getAmount()));
            memberMapper.updateById(member);

            String rejectReason = StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "审核未通过";
            notificationService.sendNotification(
                member.getId(),
                NotificationType.COMMISSION_PAID,
                "提现申请被拒绝",
                String.format("您申请的%.2f元提现被拒绝，原因：%s。金额已退还至您的佣金余额。",
                    withdraw.getAmount(), rejectReason),
                withdraw.getId()
            );

            log.info("提现拒绝: withdrawId={}, memberId={}, amount={}, reason={}",
                id, member.getId(), withdraw.getAmount(), rejectReason);

        } else {
            throw new BusinessException(400, "处理动作无效");
        }

        withdraw.setOperatorId(operatorId);
        withdraw.setOperatorName(operatorName);
        withdraw.setRemark(dto.getRemark());
        withdraw.setUpdatedAt(now);
        withdrawMapper.updateById(withdraw);
    }

    @Override
    public void delete(Long id) {
        WithdrawRecord withdraw = withdrawMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(404, "提现记录不存在");
        }

        withdraw.setDeleted(true);
        withdraw.setUpdatedAt(OffsetDateTime.now());
        withdrawMapper.updateById(withdraw);

        log.info("删除提现记录: withdrawId={}", id);
    }
}
```

- [ ] **Step 2: 提交 WithdrawServiceImpl**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/WithdrawServiceImpl.java
git commit -m "feat(admin): 实现WithdrawServiceImpl

- 提现分页查询（支持姓名/手机号/微信号/状态筛选）
- 查看微信明文
- 提现处理（通过打款/拒绝退还余额）
- 发送通知
- 软删除"
```

---

## Task 12: MemberController 扩展 - upgradeMember

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/user/MemberService.java`
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/MemberServiceImpl.java`
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberController.java`

- [ ] **Step 1: 扩展 MemberService 接口**

在 `MemberService.java` 添加方法：

```java
import com.haifeng.admin.dto.user.MemberUpgradeDTO;

Long upgradeMember(Long id, MemberUpgradeDTO dto);
```

- [ ] **Step 2: 实现 upgradeMember 方法**

在 `MemberServiceImpl.java` 添加以下依赖和方法：

```java
// 新增 imports
import com.haifeng.admin.dto.user.MemberUpgradeDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.common.entity.user.MemberOrder;
import com.haifeng.common.entity.user.ReferralCommission;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.enums.MemberType;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.enums.OrderType;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import com.haifeng.common.security.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

// 新增依赖注入
private final MemberOrderMapper orderMapper;
private final ReferralCommissionMapper commissionMapper;
private final SystemSettingsMapper settingsMapper;
private final NotificationService notificationService;
private final SnowflakeIdGenerator idGenerator;

// 实现方法
@Override
@Transactional
public Long upgradeMember(Long id, MemberUpgradeDTO dto) {
    Member member = memberMapper.selectById(id);
    if (member == null || member.getDeleted()) {
        throw new BusinessException(404, "用户不存在");
    }

    String currentType = member.getMemberType();
    String targetType = dto.getTargetType();

    if ("vip".equals(currentType) && "pro".equals(targetType)) {
        throw new BusinessException(400, "VIP会员不允许降级为Pro");
    }

    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime oldExpireAt = member.getExpireAt();
    OffsetDateTime newExpireAt;

    if (oldExpireAt != null && oldExpireAt.isAfter(now)) {
        newExpireAt = oldExpireAt.plusMonths(dto.getDurationMonths());
    } else {
        newExpireAt = now.plusMonths(dto.getDurationMonths());
    }

    OrderType orderType;
    if (currentType.equals(targetType)) {
        orderType = OrderType.RENEWAL;
    } else {
        orderType = OrderType.NEW;
    }

    member.setMemberType(targetType);
    member.setExpireAt(newExpireAt);
    member.setUpdatedAt(now);
    memberMapper.updateById(member);

    Long operatorId = SecurityUtil.getCurrentAdminId();
    String operatorName = SecurityUtil.getCurrentAdminName();

    Long orderId = idGenerator.nextId();
    String orderNo = generateOrderNo(orderId);

    MemberOrder order = MemberOrder.builder()
        .id(orderId)
        .orderNo(orderNo)
        .memberId(member.getId())
        .memberName(member.getUsername())
        .phone(member.getPhone())
        .wechatId(member.getWechatId())
        .wechatIdIndex(member.getWechatIdIndex())
        .orderType(orderType)
        .beforeType(MemberType.fromValue(currentType))
        .afterType(MemberType.fromValue(targetType))
        .durationMonths(dto.getDurationMonths())
        .amount(dto.getAmount())
        .beforeExpireAt(oldExpireAt)
        .afterExpireAt(newExpireAt)
        .operatorId(operatorId)
        .operatorName(operatorName)
        .remark(dto.getRemark())
        .deleted(false)
        .createdAt(now)
        .updatedAt(now)
        .build();
    orderMapper.insert(order);

    if (member.getReferrerId() != null) {
        processCommission(member, order, targetType);
    }

    NotificationType notifType = orderType == OrderType.NEW
        ? NotificationType.MEMBER_ACTIVATION_SUCCESS
        : NotificationType.MEMBER_RENEWED;
    String notifTitle = orderType == OrderType.NEW ? "会员开通成功" : "会员续费成功";
    String notifContent = String.format("您的%s会员已成功%s，有效期至%s。",
        targetType.toUpperCase(),
        orderType == OrderType.NEW ? "开通" : "续费",
        newExpireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    notificationService.sendNotification(member.getId(), notifType, notifTitle, notifContent, orderId);

    log.info("会员开通/续费成功: userId={}, type={}->{}, months={}, amount={}",
        id, currentType, targetType, dto.getDurationMonths(), dto.getAmount());

    return orderId;
}

private String generateOrderNo(Long orderId) {
    String dateStr = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String idSuffix = String.valueOf(orderId);
    if (idSuffix.length() > 8) {
        idSuffix = idSuffix.substring(idSuffix.length() - 8);
    }
    return "ORD" + dateStr + idSuffix;
}

private void processCommission(Member referee, MemberOrder order, String targetType) {
    Member referrer = memberMapper.selectById(referee.getReferrerId());
    if (referrer == null || referrer.getDeleted()) {
        return;
    }

    SystemSettings settings = settingsMapper.selectOne(null);
    if (settings == null) {
        log.warn("系统设置不存在，跳过佣金计算");
        return;
    }

    Integer ratePercent = "vip".equals(targetType)
        ? settings.getVipCommissionRate()
        : settings.getProCommissionRate();
    if (ratePercent == null || ratePercent <= 0) {
        return;
    }

    BigDecimal rate = new BigDecimal(ratePercent);
    BigDecimal commission = order.getAmount()
        .multiply(rate)
        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

    referrer.setCommissionBalance(referrer.getCommissionBalance().add(commission));
    referrer.setCommissionTotalEarned(referrer.getCommissionTotalEarned().add(commission));
    referrer.setUpdatedAt(OffsetDateTime.now());
    memberMapper.updateById(referrer);

    ReferralCommission commissionRecord = ReferralCommission.builder()
        .id(idGenerator.nextId())
        .referrerId(referrer.getId())
        .referrerName(referrer.getUsername())
        .referrerPhone(referrer.getPhone())
        .refereeId(referee.getId())
        .refereeName(referee.getUsername())
        .refereePhone(referee.getPhone())
        .orderId(order.getId())
        .orderAmount(order.getAmount())
        .commissionRate(rate)
        .commissionAmount(commission)
        .deleted(false)
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
    commissionMapper.insert(commissionRecord);

    notificationService.sendNotification(
        referrer.getId(),
        NotificationType.COMMISSION_EARNED,
        "佣金到账",
        String.format("您推荐的用户%s已成功开通会员，您获得%.2f元佣金奖励。",
            referee.getUsername(), commission),
        order.getId()
    );

    log.info("佣金计算: referrerId={}, refereeId={}, amount={}, rate={}%, commission={}",
        referrer.getId(), referee.getId(), order.getAmount(), ratePercent, commission);
}
```

- [ ] **Step 3: 扩展 MemberController**

在 `MemberController.java` 添加接口：

```java
import com.haifeng.admin.dto.user.MemberUpgradeDTO;

/**
 * 开通/升级/续费会员
 */
@PostMapping("/{id}/upgrade")
@OperationLog(module = "用户管理", action = "会员开通/续费")
public R<Long> upgradeMember(@PathVariable Long id, @Valid @RequestBody MemberUpgradeDTO dto) {
    return R.ok(memberService.upgradeMember(id, dto));
}
```

- [ ] **Step 4: 提交 MemberController 扩展**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/user/MemberService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/user/MemberServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberController.java
git commit -m "feat(admin): 实现会员开通/续费功能

- POST /api/v1/admin/user/{id}/upgrade
- 支持 normal→pro/vip, pro→pro/vip, vip→vip
- 自动计算到期时间
- 自动计算推荐人佣金
- 自动发送通知"
```

---

## Task 13: MemberOrderController

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberOrderController.java`

- [ ] **Step 1: 创建 MemberOrderController**

```java
package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.service.user.MemberOrderService;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/order")
@RequiredArgsConstructor
public class MemberOrderController {

    private final MemberOrderService orderService;

    @GetMapping("/list")
    public R<IPage<OrderListVO>> list(@Valid OrderQueryDTO dto) {
        return R.ok(orderService.page(dto));
    }

    @GetMapping("/{id}")
    public R<OrderDetailVO> detail(@PathVariable Long id) {
        return R.ok(orderService.detail(id));
    }

    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看订单微信明文")
    public R<String> getWechat(@PathVariable Long id) {
        return R.ok(orderService.getWechatPlaintext(id));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除订单")
    public R<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交 MemberOrderController**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/user/MemberOrderController.java
git commit -m "feat(admin): 添加MemberOrderController

- GET /list: 订单列表
- GET /{id}: 订单详情
- GET /{id}/wechat: 查看微信明文
- DELETE /{id}: 删除订单"
```

---

## Task 14: CommissionController

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/CommissionController.java`

- [ ] **Step 1: 创建 CommissionController**

```java
package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.CommissionQueryDTO;
import com.haifeng.admin.service.user.CommissionService;
import com.haifeng.admin.vo.user.CommissionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/commission")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping("/list")
    public R<IPage<CommissionListVO>> list(@Valid CommissionQueryDTO dto) {
        return R.ok(commissionService.page(dto));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除佣金记录")
    public R<Void> delete(@PathVariable Long id) {
        commissionService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交 CommissionController**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/user/CommissionController.java
git commit -m "feat(admin): 添加CommissionController

- GET /list: 佣金列表
- DELETE /{id}: 删除佣金记录"
```

---

## Task 15: NotificationController

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/NotificationController.java`

- [ ] **Step 1: 创建 NotificationController**

```java
package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public R<IPage<NotificationListVO>> list(@Valid NotificationQueryDTO dto) {
        return R.ok(notificationService.page(dto));
    }

    @PostMapping("/broadcast")
    @OperationLog(module = "用户管理", action = "群发系统公告")
    public R<Integer> broadcast(@Valid @RequestBody NotificationBroadcastDTO dto) {
        return R.ok(notificationService.broadcast(dto));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除通知")
    public R<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交 NotificationController**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/user/NotificationController.java
git commit -m "feat(admin): 添加NotificationController

- GET /list: 通知列表
- POST /broadcast: 群发系统公告
- DELETE /{id}: 删除通知"
```

---

## Task 16: WithdrawController

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/user/WithdrawController.java`

- [ ] **Step 1: 创建 WithdrawController**

```java
package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.service.user.WithdrawService;
import com.haifeng.admin.vo.user.WithdrawListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/withdraw")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @GetMapping("/list")
    public R<IPage<WithdrawListVO>> list(@Valid WithdrawQueryDTO dto) {
        return R.ok(withdrawService.page(dto));
    }

    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看提现微信明文")
    public R<String> getWechat(@PathVariable Long id) {
        return R.ok(withdrawService.getWechatPlaintext(id));
    }

    @PutMapping("/{id}/process")
    @OperationLog(module = "用户管理", action = "处理提现")
    public R<Void> process(@PathVariable Long id, @Valid @RequestBody WithdrawProcessDTO dto) {
        withdrawService.process(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除提现记录")
    public R<Void> delete(@PathVariable Long id) {
        withdrawService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 提交 WithdrawController**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/user/WithdrawController.java
git commit -m "feat(admin): 添加WithdrawController

- GET /list: 提现列表
- GET /{id}/wechat: 查看微信明文
- PUT /{id}/process: 处理提现
- DELETE /{id}: 删除提现记录"
```

---

## Task 17: 编译验证

- [ ] **Step 1: 执行 Maven 编译**

```bash
cd D:/exeProject/ideaProjects/Project-HaiFeng
mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

根据编译错误信息修复代码问题。

---

## Task 18: 启动验证

- [ ] **Step 1: 启动应用**

```bash
cd D:/exeProject/ideaProjects/Project-HaiFeng/haifeng-admin
mvn spring-boot:run
```

- [ ] **Step 2: 验证 Flyway 迁移**

查看启动日志，确认：
```
Successfully applied 1 migration to schema "public" (execution time 00:00.xxx s)
```

- [ ] **Step 3: 验证数据库表**

连接 PostgreSQL 验证 4 张表已创建：
- member_orders
- t_referral_commission
- t_member_notification
- t_withdraw_record

- [ ] **Step 4: 提交最终验证**

```bash
git add .
git commit -m "chore: 完成用户管理模块实现

实现功能：
- 会员开通/续费（含佣金计算）
- 订单管理（列表/详情/删除）
- 佣金管理（列表/删除）
- 消息通知（列表/群发/删除）
- 提现管理（列表/处理/删除）

数据库：V3迁移文件（4张新表）
接口：12个新接口"
```

---

## 验证检查清单

| 序号 | 验证项 | 预期结果 |
|------|--------|----------|
| 1 | `mvn compile` | BUILD SUCCESS |
| 2 | 应用启动 | 无报错 |
| 3 | Flyway 迁移 | V3 成功应用 |
| 4 | 4张表创建 | 存在且结构正确 |
| 5 | normal→Pro | 成功，生成订单 |
| 6 | pro→VIP | 成功，order_type=NEW |
| 7 | vip→Pro | 失败，返回400 |
| 8 | 有推荐人 | 佣金记录生成 |
| 9 | 群发公告 | 所有用户收到 |
| 10 | 提现通过 | 状态PAID |
| 11 | 提现拒绝 | 余额退还 |
