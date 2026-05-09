## 五、消息通知表（新增）（会员即将到期，佣金已转等会记录）

SQL

```
-- ============================================================
-- 消息通知表 (t_member_notification)
-- 描述：系统消息（会员到期提醒/佣金到账通知）
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_notification (

    id                      SERIAL          PRIMARY KEY,
    member_id               INTEGER         NOT NULL,               -- 接收人ID

    -- ==================== 消息内容 ====================
    notification_type       VARCHAR(30)     NOT NULL,               -- 消息类型
    title                   VARCHAR(200)    NOT NULL,               -- 标题
    content                 TEXT,                                   -- 内容
    related_id              INTEGER,                                -- 关联业务ID（订单ID/佣金ID）

    -- ==================== 状态 ====================
    is_read                 BOOLEAN         DEFAULT FALSE,          -- 是否已读

    -- ==================== 时间 ====================
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    read_at                 TIMESTAMPTZ,                            -- 已读时间

    -- ==================== 约束 ====================
    CONSTRAINT chk_notification_type CHECK (
    notification_type IN (
        'member_expire_soon',       -- 会员即将到期
        'member_expired',           -- 会员已过期
        'commission_earned',        -- 佣金到账
        'commission_paid',          -- 佣金已发放
        'system_notice',            -- 系统公告
        'member_renewed',           -- 会员续费
        'member_activation_success' -- 会员开通成功
    )
 )
);

-- 索引
-- 按用户查未读消息
CREATE INDEX idx_notification_member_unread
    ON t_member_notification (member_id, is_read, created_at DESC);

-- 按消息类型
CREATE INDEX idx_notification_type
    ON t_member_notification (notification_type);

-- 触发器
CREATE TRIGGER trg_notification_updated_at
    BEFORE UPDATE ON t_member_notification
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_notification                     IS '用户消息通知表';
COMMENT ON COLUMN t_member_notification.member_id           IS '接收人ID';
COMMENT ON COLUMN t_member_notification.notification_type   IS '消息类型（会员到期/佣金到账等）';
COMMENT ON COLUMN t_member_notification.title               IS '消息标题';
COMMENT ON COLUMN t_member_notification.content             IS '消息内容';
COMMENT ON COLUMN t_member_notification.related_id          IS '关联业务ID（订单/佣金记录ID）';
COMMENT ON COLUMN t_member_notification.is_read             IS '是否已读';
COMMENT ON COLUMN t_member_notification.read_at             IS '已读时间';

COMMIT;
```



### 表2：订单/续费记录表 (member_orders)（用户续费会员会记录时间等）


```
-- ============================================
-- 会员订单表 (member_orders)
-- 说明：记录每一笔新开通/续费的订单流水
--       每次管理员操作「添加会员」或「续费」都会生成一条记录
-- ============================================

CREATE TABLE member_orders (
    id               SERIAL        PRIMARY KEY                              ,-- 订单ID
    order_no         VARCHAR(50)   NOT NULL UNIQUE                          ,-- 订单编号（如：ORD20250701001）
    member_id        INTEGER       NOT NULL 
 ,-- 会员ID
    member_name      VARCHAR(50)   NOT NULL                                 ,-- 会员username
    phone                       VARCHAR(20) UNIQUE NOT NULL,
    wechat_id                   VARCHAR(255),                                -- 微信号(AES加密存储)
    wechat_id_index             VARCHAR(64),                                 -- 微信号盲索引(SHA-256)
    -- 订单信息
    order_type       VARCHAR(20)   NOT NULL                                 ,-- 订单类型: new-新开通 / renewal-续费
    before_type      VARCHAR(20)                                            ,-- 操作前会员类型（续费时记录）
    after_type       VARCHAR(20)   NOT NULL                                 ,-- 操作后会员类型
    duration_months  INTEGER       NOT NULL                                 ,-- 时长（月），12=1年 24=2年
    amount           DECIMAL(10,2) NOT NULL                                 ,-- 订单金额（实收金额）
    
    -- 到期时间变更记录
    before_expire_at TIMESTAMP                                              ,-- 操作前到期时间
    after_expire_at  TIMESTAMP     NOT NULL                                 ,-- 操作后到期时间
    
    -- 操作人
    operator_id      INTEGER       NOT NULL                                 ,-- 操作管理员ID
    operator_name    VARCHAR(50)   NOT NULL                                 ,-- 操作管理员姓名
    remark           TEXT                                                   ,-- 备注
    
    created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL       -- 操作时间
);

-- 索引
CREATE INDEX idx_member_orders_member_id  ON member_orders(member_id);
CREATE INDEX idx_member_orders_order_type ON member_orders(order_type);
CREATE INDEX idx_member_orders_created_at ON member_orders(created_at);
CREATE INDEX idx_member_orders_member_name ON member_orders(member_name);

-- 注释
COMMENT ON TABLE  member_orders                    IS '会员订单表，记录每一笔开通/续费流水';
COMMENT ON COLUMN member_orders.id                 IS '订单ID';
COMMENT ON COLUMN member_orders.order_no           IS '订单编号，格式：ORD+年月日+序号';
COMMENT ON COLUMN member_orders.member_id          IS '会员ID';
COMMENT ON COLUMN member_orders.order_type         IS '订单类型: new-新会员开通 renewal-续费';
COMMENT ON COLUMN member_orders.before_type        IS '操作前的会员类型，新开通时为空';
COMMENT ON COLUMN member_orders.after_type         IS '操作后的会员类型: normal / vip';
COMMENT ON COLUMN member_orders.duration_months    IS '购买时长，单位：月，如12=1年 24=2年';
COMMENT ON COLUMN member_orders.amount             IS '实收金额，单位：元';
COMMENT ON COLUMN member_orders.before_expire_at   IS '操作前的到期时间，新开通时为空';
COMMENT ON COLUMN member_orders.after_expire_at    IS '操作后的到期时间';
COMMENT ON COLUMN member_orders.operator_id        IS '操作管理员的ID';
COMMENT ON COLUMN member_orders.operator_name      IS '操作管理员的姓名（冗余存储，方便查看）';
COMMENT ON COLUMN member_orders.remark             IS '备注信息';
COMMENT ON COLUMN member_orders.created_at         IS '操作时间';
```


### 1.2 佣金表

SQL

```
-- ============================================================
-- 推荐佣金表 (t_referral_commission) — 修复版
-- ============================================================

DROP TABLE IF EXISTS referral_commissions CASCADE;

CREATE TABLE IF NOT EXISTS t_referral_commission (

    id                  SERIAL          PRIMARY KEY,

    -- ==================== 推荐关系 ====================
    referrer_id         INTEGER         NOT NULL,               -- 推荐人ID（收钱的人）
    referrer_name       VARCHAR(50),                            -- 推荐人姓名（冗余）
    referrer_phone       VARCHAR(50),                            -- 推荐人电话（冗余）
    referee_id          INTEGER,                                -- 被推荐人ID（付钱的人）
    referee_name        VARCHAR(50),                            -- 被推荐人姓名（冗余）
    referrer_phone       VARCHAR(50),                            -- 被推荐人电话（冗余）

    -- ==================== 订单关联 ====================
    order_id            INTEGER,                                -- 关联订单ID（如果有订单表的话）

    -- ==================== 佣金计算 ====================
    order_amount        DECIMAL(10,2)   NOT NULL,               -- 订单金额
    commission_rate     DECIMAL(5,4)    NOT NULL,               -- 佣金比例（0.1000 = 10%）
    commission_amount   DECIMAL(10,2)   NOT NULL,               -- 佣金金额
    
    -- ==================== 审计字段 ====================
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_commission_amount
        CHECK (commission_amount >= 0)
    
-- 索引
CREATE INDEX idx_rc_referrer   ON t_referral_commission (referrer_id);
CREATE INDEX idx_rc_referee    ON t_referral_commission (referee_id);
CREATE INDEX idx_rc_created    ON t_referral_commission (created_at DESC);
```


```
-- ============================================================
-- 提现记录表 (t_withdraw_record)
-- ============================================================

CREATE TABLE t_withdraw_record (
    id                  BIGINT          PRIMARY KEY,            -- 雪花ID
    member_id           BIGINT          NOT NULL,               -- 用户ID
    member_name         VARCHAR(50)     NOT NULL,

    -- 用户快照信息（记录提现时的联系方式，防止用户后续修改导致无法对账）
    phone               VARCHAR(20)     NOT NULL,               -- 用户手机号
    wechat_id           VARCHAR(255)    NOT NULL,               -- 微信号（AES加密）
    wechat_id_index     VARCHAR(64)     NOT NULL,               -- 微信号盲索引（用于搜索）

    -- 金额信息
    amount              DECIMAL(10,2)   NOT NULL,               -- 本次提现金额
    
    -- 状态流转
    status              VARCHAR(20)     NOT NULL DEFAULT 'pending', -- pending(待审核),  rejected(已驳回), paid(已打款)
    
    -- 操作人信息
    operator_id         BIGINT,                                 -- 审核/发放管理员ID
    operator_name       VARCHAR(50),                            -- 管理员姓名（冗余）
    
    -- 备注与审计
    remark              TEXT,                                   -- 驳回原因或打款备注
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(), -- 申请时间
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()  -- 最后更新时间
);

-- 索引：提高查询效率
CREATE INDEX idx_withdraw_member_id ON t_withdraw_record(member_id);
CREATE INDEX idx_withdraw_status    ON t_withdraw_record(status);
CREATE INDEX idx_withdraw_wx_index  ON t_withdraw_record(wechat_id_index);
CREATE INDEX idx_withdraw_created   ON t_withdraw_record(created_at DESC);

-- 注释
COMMENT ON TABLE  t_withdraw_record                 IS '提现记录表';
COMMENT ON COLUMN t_withdraw_record.wechat_id_index IS '微信号盲索引，支持管理员模糊/精确搜索';
COMMENT ON COLUMN t_withdraw_record.status          IS '状态：pending-待审核, approved-已通过, rejected-已驳回, paid-已打款';
```