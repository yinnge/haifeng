-- V3__create_member_tables.sql
-- 海峰未来规划院 - 会员相关业务表

-- 1. 会员订单表
CREATE TABLE member_orders (
    id                  BIGINT PRIMARY KEY,
    order_no            VARCHAR(50) NOT NULL UNIQUE,
    member_id           BIGINT NOT NULL,
    member_name         VARCHAR(50),
    phone               VARCHAR(20),
    wechat_id           VARCHAR(255),
    wechat_id_index     VARCHAR(64),
    order_type          VARCHAR(20) NOT NULL,
    before_type         VARCHAR(20) NOT NULL,
    after_type          VARCHAR(20) NOT NULL,
    duration_months     INTEGER NOT NULL,
    amount              DECIMAL(10,2) NOT NULL,
    before_expire_at    TIMESTAMPTZ,
    after_expire_at     TIMESTAMPTZ NOT NULL,
    operator_id         BIGINT,
    operator_name       VARCHAR(50),
    remark              VARCHAR(500),
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    version             INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_type CHECK (order_type IN ('new', 'renewal')),
    CONSTRAINT chk_before_type CHECK (before_type IN ('normal', 'pro', 'vip')),
    CONSTRAINT chk_after_type CHECK (after_type IN ('normal', 'pro', 'vip'))
);

CREATE INDEX idx_member_orders_member ON member_orders(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_orders_phone ON member_orders(phone) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_orders_wechat_index ON member_orders(wechat_id_index) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_orders_order_type ON member_orders(order_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_orders_created ON member_orders(created_at) WHERE is_deleted = FALSE;

COMMENT ON TABLE member_orders IS '会员订单表';
COMMENT ON COLUMN member_orders.order_no IS '订单编号（唯一）';
COMMENT ON COLUMN member_orders.member_id IS '会员ID';
COMMENT ON COLUMN member_orders.member_name IS '会员名称';
COMMENT ON COLUMN member_orders.phone IS '手机号';
COMMENT ON COLUMN member_orders.wechat_id IS '微信号(AES加密存储)';
COMMENT ON COLUMN member_orders.wechat_id_index IS '微信号盲索引(SHA-256哈希，用于等值查询)';
COMMENT ON COLUMN member_orders.order_type IS '订单类型: new-新购, renewal-续费';
COMMENT ON COLUMN member_orders.before_type IS '变更前会员类型: normal-普通版, pro-专业版, vip-旗舰版';
COMMENT ON COLUMN member_orders.after_type IS '变更后会员类型: normal-普通版, pro-专业版, vip-旗舰版';
COMMENT ON COLUMN member_orders.duration_months IS '购买时长(月)';
COMMENT ON COLUMN member_orders.amount IS '订单金额';
COMMENT ON COLUMN member_orders.before_expire_at IS '变更前到期时间';
COMMENT ON COLUMN member_orders.after_expire_at IS '变更后到期时间';
COMMENT ON COLUMN member_orders.operator_id IS '操作人ID';
COMMENT ON COLUMN member_orders.operator_name IS '操作人姓名';
COMMENT ON COLUMN member_orders.remark IS '备注';

-- 2. 推荐佣金表
CREATE TABLE t_referral_commission (
    id                  BIGINT PRIMARY KEY,
    referrer_id         BIGINT NOT NULL,
    referrer_name       VARCHAR(50),
    referrer_phone      VARCHAR(20),
    referee_id          BIGINT NOT NULL,
    referee_name        VARCHAR(50),
    referee_phone       VARCHAR(20),
    order_id            BIGINT NOT NULL,
    order_amount        DECIMAL(10,2) NOT NULL,
    commission_rate     DECIMAL(5,2) NOT NULL,
    commission_amount   DECIMAL(10,2) NOT NULL,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_referral_commission_referrer ON t_referral_commission(referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_referral_commission_referee ON t_referral_commission(referee_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_referral_commission_order ON t_referral_commission(order_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_referral_commission_created ON t_referral_commission(created_at) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_referral_commission IS '推荐佣金表';
COMMENT ON COLUMN t_referral_commission.referrer_id IS '推荐人ID';
COMMENT ON COLUMN t_referral_commission.referrer_name IS '推荐人姓名';
COMMENT ON COLUMN t_referral_commission.referrer_phone IS '推荐人手机号';
COMMENT ON COLUMN t_referral_commission.referee_id IS '被推荐人ID';
COMMENT ON COLUMN t_referral_commission.referee_name IS '被推荐人姓名';
COMMENT ON COLUMN t_referral_commission.referee_phone IS '被推荐人手机号';
COMMENT ON COLUMN t_referral_commission.order_id IS '关联订单ID';
COMMENT ON COLUMN t_referral_commission.order_amount IS '订单金额';
COMMENT ON COLUMN t_referral_commission.commission_rate IS '佣金比例(%)';
COMMENT ON COLUMN t_referral_commission.commission_amount IS '佣金金额';

-- 3. 消息通知表
CREATE TABLE t_member_notification (
    id                  BIGINT PRIMARY KEY,
    member_id           BIGINT NOT NULL,
    notification_type   VARCHAR(30) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    content             TEXT,
    related_id          BIGINT,
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMPTZ,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notification_type CHECK (notification_type IN (
        'member_expire_soon',       -- 会员即将到期
        'member_expired',           -- 会员已过期
        'commission_earned',        -- 佣金到账
        'commission_paid',          -- 佣金已发放
        'system_notice',            -- 系统公告
        'member_renewed',           -- 会员续费成功
        'member_activation_success' -- 会员开通成功
    ))
);

CREATE INDEX idx_member_notification_member ON t_member_notification(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_notification_type ON t_member_notification(notification_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_notification_read ON t_member_notification(is_read) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_notification_created ON t_member_notification(created_at) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_member_notification IS '消息通知表';
COMMENT ON COLUMN t_member_notification.member_id IS '会员ID';
COMMENT ON COLUMN t_member_notification.notification_type IS '通知类型: member_expire_soon-会员即将到期, member_expired-会员已过期, commission_earned-佣金到账, commission_paid-佣金已发放, system_notice-系统公告, member_renewed-会员续费成功, member_activation_success-会员开通成功';
COMMENT ON COLUMN t_member_notification.title IS '通知标题';
COMMENT ON COLUMN t_member_notification.content IS '通知内容';
COMMENT ON COLUMN t_member_notification.related_id IS '关联业务ID';
COMMENT ON COLUMN t_member_notification.is_read IS '是否已读';
COMMENT ON COLUMN t_member_notification.read_at IS '阅读时间';

-- 4. 提现记录表
CREATE TABLE t_withdraw_record (
    id                  BIGINT PRIMARY KEY,
    member_id           BIGINT NOT NULL,
    member_name         VARCHAR(50),
    phone               VARCHAR(20),
    wechat_id           VARCHAR(255),
    wechat_id_index     VARCHAR(64),
    amount              DECIMAL(10,2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending',
    operator_id         BIGINT,
    operator_name       VARCHAR(50),
    remark              VARCHAR(500),
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_withdraw_amount CHECK (amount IN (50.00, 100.00)),
    CONSTRAINT chk_withdraw_status CHECK (status IN ('pending', 'paid', 'rejected'))
);

CREATE INDEX idx_withdraw_record_member ON t_withdraw_record(member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_record_phone ON t_withdraw_record(phone) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_record_wechat_index ON t_withdraw_record(wechat_id_index) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_record_status ON t_withdraw_record(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_withdraw_record_created ON t_withdraw_record(created_at) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_withdraw_record IS '提现记录表';
COMMENT ON COLUMN t_withdraw_record.member_id IS '会员ID';
COMMENT ON COLUMN t_withdraw_record.member_name IS '会员名称';
COMMENT ON COLUMN t_withdraw_record.phone IS '手机号';
COMMENT ON COLUMN t_withdraw_record.wechat_id IS '微信号(AES加密存储)';
COMMENT ON COLUMN t_withdraw_record.wechat_id_index IS '微信号盲索引(SHA-256哈希，用于等值查询)';
COMMENT ON COLUMN t_withdraw_record.amount IS '提现金额(仅支持50.00或100.00)';
COMMENT ON COLUMN t_withdraw_record.status IS '状态: pending-待处理, paid-已打款, rejected-已拒绝';
COMMENT ON COLUMN t_withdraw_record.operator_id IS '操作人ID';
COMMENT ON COLUMN t_withdraw_record.operator_name IS '操作人姓名';
COMMENT ON COLUMN t_withdraw_record.remark IS '备注';
