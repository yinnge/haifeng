
```
-- 1. 角色表（管理员的的角色）
```
CREATE TABLE sys_role (
    id          SERIAL PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
-- 2. 模块表（后端的模块，不同的角色对模块编辑有权限）
```

-- 2. 模块表（支持父子层级）
CREATE TABLE sys_module (
    id            BIGSERIAL PRIMARY KEY,
    module_name   VARCHAR(50) NOT NULL,          
    module_code   VARCHAR(50) NOT NULL UNIQUE,   -- 全局唯一
    parent_id     BIGINT REFERENCES sys_module(id) ON DELETE CASCADE,
    path          VARCHAR(200),                  -- 前端路由 /admin/system/setting
    icon          VARCHAR(50),
    sort_order    INTEGER DEFAULT 0,
    level         SMALLINT NOT NULL,             -- 1=父模块 2=子模块
    description   VARCHAR(255),
    status        SMALLINT DEFAULT 1,
    is_deleted    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON COLUMN sys_module.parent_id IS '父模块ID，NULL表示顶级';
COMMENT ON COLUMN sys_module.level     IS '1父2子，用于快速判断';

CREATE INDEX idx_module_parent ON sys_module(parent_id);
```

-- 3. 角色-模块关联表（中间表）
```
CREATE TABLE sys_role_module (
    id          SERIAL PRIMARY KEY,
    role_id     INTEGER NOT NULL,   -- 逻辑外键，指向 sys_role.id
    module_id   INTEGER NOT NULL,   -- 逻辑外键，指向 sys_module.id
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```
-- 4. 管理员表

```
CREATE TABLE sys_admin (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    role_id     INTEGER NOT NULL,   -- 逻辑外键，指向 sys_role.id
    
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```
-- ============================================================
-- 用户表 (t_member)
-- 描述：核心用户表，管理员创建，用户登录后可完善资料
-- ============================================================

BEGIN;



CREATE TABLE IF NOT EXISTS t_member (

    id                          BIGSERIAL       PRIMARY KEY,
    username                    VARCHAR(50)     NOT NULL UNIQUE,    -- 用户名（登录账号）
    password                    VARCHAR(100)    NOT NULL,           -- 密码（加密存储）
    avatar                      VARCHAR(500),                       -- 头像URL
    phone                       VARCHAR(20)     UNIQUE,             -- 手机号（唯一，用于登录或找回密码） 
    invite_code            VARCHAR(20) UNIQUE,         -- 邀请码
    
    -- ==================== 会员信息 ====================
    member_type                 VARCHAR(20)     DEFAULT 'normal',   -- 会员类型（normal/pro/vip）
    expire_at                   TIMESTAMPTZ,                        -- 会员到期时间

    -- ==================== 推荐关系 ====================
    referrer_id                 INTEGER,                            -- 推荐人ID
    referrer_username           VARCHAR(50),                        -- 推荐人用户名（冗余，方便查询）

    -- ==================== 佣金统计（冗余字段，提高查询性能） ====================
    commission_balance          DECIMAL(10,2)   DEFAULT 0.00,       -- 可提现余额
    commission_total_earned     DECIMAL(10,2)   DEFAULT 0.00,       -- 累计获得佣金
    commission_total_paid       DECIMAL(10,2)   DEFAULT 0.00,       -- 累计已发放佣金

    -- ==================== 状态 ====================
    status                      VARCHAR(20)     DEFAULT 'active',   -- 账号状态（active/disabled）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_member_type CHECK (
        member_type IN ('normal', 'pro', 'vip')
    ),
    CONSTRAINT chk_member_status CHECK (
        status IN ('active', 'disabled')
    ),
    CONSTRAINT chk_commission_balance CHECK (
        commission_balance >= 0
    )
);

-- 索引
CREATE INDEX idx_member_phone      ON t_member (phone)       WHERE is_deleted = FALSE;
CREATE INDEX idx_member_type       ON t_member (member_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_referrer   ON t_member (referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_expire     ON t_member (expire_at)   WHERE is_deleted = FALSE;

-- 邀请码由 Java 层 Hashids 生成（基于雪花ID，绝对唯一）
-- UNIQUE 约束作为兜底保护

-- 更新时间触发器
CREATE TRIGGER trg_member_updated_at
BEFORE UPDATE ON t_member
FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member                          IS '会员表（管理员创建，用户登录使用）';
COMMENT ON COLUMN t_member.username                 IS '用户名（唯一，登录账号）';
COMMENT ON COLUMN t_member.password                 IS '密码（加密存储）';
COMMENT ON COLUMN t_member.avatar                   IS '头像URL';
COMMENT ON COLUMN t_member.phone                    IS '手机号（唯一）';
COMMENT ON COLUMN t_member.member_type              IS '会员类型（normal/vip）';
COMMENT ON COLUMN t_member.expire_at                IS '会员到期时间';
COMMENT ON COLUMN t_member.referrer_id              IS '推荐人ID';
COMMENT ON COLUMN t_member.referrer_username        IS '推荐人用户名（冗余）';
COMMENT ON COLUMN t_member.commission_balance       IS '可提现佣金余额';
COMMENT ON COLUMN t_member.commission_total_earned  IS '累计获得佣金';
COMMENT ON COLUMN t_member.commission_total_paid    IS '累计已发放佣金';
COMMENT ON COLUMN t_member.status                   IS '账号状态（active/disabled）';

COMMIT;
```