
# 操作日志表（记录管理员操作人的信息）


```
CREATE TABLE admin_logs (
    id              SERIAL        PRIMARY KEY,
    operator_id     INTEGER       NOT NULL,
    operator_name   VARCHAR(50)   NOT NULL,
    action          VARCHAR(100)  NOT NULL,
    ip              VARCHAR(50),
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_admin_logs_operator_id ON admin_logs(operator_id);
CREATE INDEX idx_admin_logs_created_at  ON admin_logs(created_at);

COMMENT ON TABLE  admin_logs               IS '管理员操作日志表';
COMMENT ON COLUMN admin_logs.id            IS '日志ID';
COMMENT ON COLUMN admin_logs.operator_id   IS '操作人ID';
COMMENT ON COLUMN admin_logs.operator_name IS '操作人姓名';
COMMENT ON COLUMN admin_logs.action        IS '操作内容，如：编辑了院校"清华大学"信息';
COMMENT ON COLUMN admin_logs.ip            IS '操作时的IP地址';
COMMENT ON COLUMN admin_logs.created_at    IS '操作时间';
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
CREATE TABLE sys_module (
    id          SERIAL PRIMARY KEY,
    module_name VARCHAR(50) NOT NULL,          
    module_code VARCHAR(50) NOT NULL UNIQUE,   --模块标识
    description VARCHAR(255),
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
-- 3. 角色-模块关联表（中间表）
```
CREATE TABLE sys_role_module (
    id          SERIAL PRIMARY KEY,
    role_id     INTEGER NOT NULL,   -- 逻辑外键，指向 sys_role.id
    module_id   INTEGER NOT NULL,   -- 逻辑外键，指向 sys_module.id
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    -- 可选：添加联合唯一约束，避免重复关联
    CONSTRAINT uk_role_module UNIQUE (role_id, module_id)
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
    role_name    VARCHAR(50)   NOT NULL,-- 角色名姓名
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```
-- ============================================================
-- 会员表 (t_member)
-- 描述：核心用户表，管理员创建，用户登录后可完善资料
-- ============================================================

BEGIN;



CREATE TABLE IF NOT EXISTS t_member (

    id                          SERIAL          PRIMARY KEY,
    username                    VARCHAR(50)     NOT NULL UNIQUE,    -- 用户名（登录账号）
    password                    VARCHAR(100)    NOT NULL,           -- 密码（加密存储）
    avatar                      VARCHAR(500),                       -- 头像URL
    phone                       VARCHAR(20)     UNIQUE,             -- 手机号（唯一，用于登录或找回密码） 
    invite_code            VARCHAR(20) UNIQUE,         -- 邀请码
    
    -- ==================== 会员信息 ====================
    member_type                 VARCHAR(20)     DEFAULT 'normal',   -- 会员类型（normal/vip）
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
        member_type IN ('normal', 'vip')
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

-- 触发器
CREATE OR REPLACE FUNCTION fn_generate_invite_code()
RETURNS VARCHAR AS $$
DECLARE
    chars TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; -- 去掉易混淆的 0OI1
    code VARCHAR(8);
    exists BOOLEAN;
BEGIN
    LOOP
        -- 生成 8 位随机码
        code := '';
        FOR i IN 1..8 LOOP
            code := code || substr(chars, floor(random() * length(chars) + 1)::int, 1);
        END LOOP;

        -- 检查是否已存在
        SELECT EXISTS(SELECT 1 FROM t_member WHERE invite_code = code) INTO exists;
        EXIT WHEN NOT exists;
    END LOOP;

    RETURN code;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_auto_invite_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.invite_code IS NULL THEN
        NEW.invite_code := fn_generate_invite_code();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_member_invite_code
    BEFORE INSERT ON t_member
    FOR EACH ROW
    EXECUTE FUNCTION fn_auto_invite_code();
    
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

```
-- ============================================================
-- 用户资料表 (t_member_profile)
-- 描述：与 t_member 一对一，存储用户的个人资料（可选填）
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_profile (

    id                      SERIAL          PRIMARY KEY,
    member_id               INTEGER         NOT NULL UNIQUE,        -- 关联会员表（一对一）

    -- ==================== 个人信息 ====================
    real_name               VARCHAR(50),                            -- 真实姓名
    email                   VARCHAR(100),                           -- 邮箱
    identity                VARCHAR(100),                           -- 身份（如：北京大学）
    province                VARCHAR(30),                            -- 省份
    city                    VARCHAR(50),                            -- 城市
    major                   VARCHAR(100),                           -- 专业
    grade                   VARCHAR(20),                            -- 年级（如：大三、研一）
    education_level         VARCHAR(20),                            -- 学历层次（如：大学生、研究生）
    
    -- ==================== 审计字段 ====================
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_profile_member_id ON t_member_profile (member_id);
CREATE INDEX idx_profile_identity  ON t_member_profile (identity);
CREATE INDEX idx_profile_city      ON t_member_profile (city);

-- 触发器
CREATE TRIGGER trg_member_profile_updated_at
    BEFORE UPDATE ON t_member_profile
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_profile                  IS '用户资料表：与 t_member 一对一';
COMMENT ON COLUMN t_member_profile.member_id        IS '关联会员表ID';
COMMENT ON COLUMN t_member_profile.real_name        IS '真实姓名';
COMMENT ON COLUMN t_member_profile.email            IS '邮箱';
COMMENT ON COLUMN t_member_profile.identity         IS '身份（如：北京大学）';
COMMENT ON COLUMN t_member_profile.province         IS '省份';
COMMENT ON COLUMN t_member_profile.city             IS '城市';
COMMENT ON COLUMN t_member_profile.major            IS '专业';
COMMENT ON COLUMN t_member_profile.grade            IS '年级（大三/研一）';
COMMENT ON COLUMN t_member_profile.education_level  IS '学历层次（大学生/研究生）';

COMMIT;
```