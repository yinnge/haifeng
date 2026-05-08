-- V1__create_admin_tables.sql
-- 海峰未来规划院 - 管理端数据库表

-- 1. 角色表
CREATE TABLE sys_role (
    id              BIGSERIAL PRIMARY KEY,
    role_name       VARCHAR(50) NOT NULL,
    role_code       VARCHAR(50) NOT NULL,
    description     VARCHAR(100),
    status          SMALLINT DEFAULT 1,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_role_name ON sys_role(role_name) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uk_role_code ON sys_role(role_code) WHERE is_deleted = FALSE;
CREATE INDEX idx_role_status ON sys_role(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.status IS '状态: 0-禁用, 1-启用';

-- 2. 模块表（支持父子层级）
CREATE TABLE sys_module (
    id            BIGSERIAL PRIMARY KEY,
    module_name   VARCHAR(50) NOT NULL,
    module_code   VARCHAR(50) NOT NULL UNIQUE,
    parent_id     BIGINT REFERENCES sys_module(id) ON DELETE CASCADE,
    path          VARCHAR(200),
    icon          VARCHAR(50),
    sort_order    INTEGER DEFAULT 0,
    level         SMALLINT NOT NULL,
    description   VARCHAR(255),
    status        SMALLINT DEFAULT 1,
    is_deleted    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_module_name ON sys_module(module_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_module_parent ON sys_module(parent_id);
CREATE INDEX idx_module_status ON sys_module(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE sys_module IS '模块表';
COMMENT ON COLUMN sys_module.parent_id IS '父模块ID，NULL表示顶级';
COMMENT ON COLUMN sys_module.level IS '1=父模块 2=子模块';

-- 3. 角色-模块关联表
CREATE TABLE sys_role_module (
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    module_id   BIGINT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(role_id, module_id)
);

CREATE INDEX idx_role_module_role ON sys_role_module(role_id);
CREATE INDEX idx_role_module_module ON sys_role_module(module_id);

COMMENT ON TABLE sys_role_module IS '角色-模块关联表';

-- 4. 管理员表
CREATE TABLE sys_admin (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    real_name       VARCHAR(50),
    phone           VARCHAR(20) NOT NULL UNIQUE,
    email           VARCHAR(100),
    avatar          VARCHAR(500),
    role_id         BIGINT NOT NULL,
    role_name       VARCHAR(50),
    status          SMALLINT DEFAULT 1,
    last_login_at   TIMESTAMPTZ,
    last_login_ip   VARCHAR(50),
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    totp_secret     VARCHAR(64),
    is_totp_enabled BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_admin_role ON sys_admin(role_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_admin_status ON sys_admin(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE sys_admin IS '管理员表';
COMMENT ON COLUMN sys_admin.phone IS '手机号（用于登录）';
COMMENT ON COLUMN sys_admin.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN sys_admin.totp_secret IS 'TOTP动态口令密钥(Base32编码)';
COMMENT ON COLUMN sys_admin.is_totp_enabled IS '是否已开启双因素认证';

-- 5. 操作日志表
CREATE TABLE admin_logs (
    id              BIGSERIAL PRIMARY KEY,
    admin_id        BIGINT NOT NULL,
    admin_name      VARCHAR(50),
    operation       VARCHAR(100) NOT NULL,
    request_path    VARCHAR(200),
    request_method  VARCHAR(10),
    request_params  TEXT,
    result          VARCHAR(20),
    error_msg       TEXT,
    ip              VARCHAR(50),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_admin_logs_admin ON admin_logs(admin_id);
CREATE INDEX idx_admin_logs_created ON admin_logs(created_at);

COMMENT ON TABLE admin_logs IS '操作日志表';

-- 6. 会员表
-- member_type: normal(普通版) -> pro(专业版) -> vip(旗舰版)
CREATE TABLE IF NOT EXISTS t_member (
    id                          BIGSERIAL PRIMARY KEY,
    username                    VARCHAR(50) NOT NULL UNIQUE,
    password                    VARCHAR(100) NOT NULL,
    avatar                      VARCHAR(500),
    phone                       VARCHAR(20) UNIQUE NOT NULL,
    invite_code                 VARCHAR(8) UNIQUE,
    member_type                 VARCHAR(20) DEFAULT 'normal',
    expire_at                   TIMESTAMPTZ,
    referrer_id                 BIGINT,
    referrer_username           VARCHAR(50),
    commission_balance          DECIMAL(10,2) DEFAULT 0.00,
    commission_total_earned     DECIMAL(10,2) DEFAULT 0.00,
    commission_total_paid       DECIMAL(10,2) DEFAULT 0.00,
    status                      VARCHAR(20) DEFAULT 'active',
    last_login_at               TIMESTAMPTZ,
    last_login_ip               VARCHAR(50),
    wechat_id                   VARCHAR(255),                       -- 微信号(AES加密存储)
    wechat_id_index             VARCHAR(64),                        -- 微信号盲索引(SHA-256)
    is_deleted                  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_member_type CHECK (member_type IN ('normal', 'pro', 'vip')),
    CONSTRAINT chk_member_status CHECK (status IN ('active', 'disabled')),
    CONSTRAINT chk_commission_balance CHECK (commission_balance >= 0)
);

CREATE INDEX idx_member_phone ON t_member(phone) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_type ON t_member(member_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_referrer ON t_member(referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_wechat_index ON t_member(wechat_id_index) WHERE is_deleted = FALSE;

-- 邀请码由 Java 层 Hashids 生成（基于雪花ID，绝对唯一）
-- UNIQUE 约束作为兜底保护

COMMENT ON TABLE t_member IS '会员表';
COMMENT ON COLUMN t_member.phone IS '手机号（用于登录，必填）';
COMMENT ON COLUMN t_member.invite_code IS '邀请码（8位，自动生成）';
COMMENT ON COLUMN t_member.wechat_id IS '微信号(AES加密存储)';
COMMENT ON COLUMN t_member.wechat_id_index IS '微信号盲索引(SHA-256哈希，用于等值查询)';

-- 默认管理员（密码：Admin123）
INSERT INTO sys_role (id, role_name, role_code, description, status)
VALUES (1, '超级管理员', 'super_admin', '拥有所有权限', 1);

INSERT INTO sys_admin (id, username, password, real_name, phone, role_id, role_name, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctLkRc2xqV8k1u7QwcEVyRZCJ.', '超级管理员', '13800000000', 1, '超级管理员', 1);
