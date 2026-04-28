-- ============================================================
-- 通用函数：自动更新 updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 会员表 (t_member)
-- 描述：核心用户表，管理员创建，用户登录后可完善资料
-- ============================================================
CREATE TABLE t_member (
    id                          BIGINT          PRIMARY KEY,
    username                    VARCHAR(50)     NOT NULL UNIQUE,
    password                    VARCHAR(100)    NOT NULL,
    avatar                      VARCHAR(500),
    phone                       VARCHAR(20)     UNIQUE,
    invite_code                 VARCHAR(20)     UNIQUE,

    -- ==================== 会员信息 ====================
    member_type                 VARCHAR(20)     DEFAULT 'normal' NOT NULL,
    expire_at                   TIMESTAMPTZ,

    -- ==================== 推荐关系 ====================
    referrer_id                 BIGINT,
    referrer_username           VARCHAR(50),

    -- ==================== 佣金统计（冗余字段，提高查询性能） ====================
    commission_balance          DECIMAL(10,2)   DEFAULT 0.00,
    commission_total_earned     DECIMAL(10,2)   DEFAULT 0.00,
    commission_total_paid       DECIMAL(10,2)   DEFAULT 0.00,

    -- ==================== 状态与审计 ====================
    status                      VARCHAR(20)     DEFAULT 'active' NOT NULL,
    last_login_at               TIMESTAMPTZ,
    last_login_ip               VARCHAR(50),
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_member_type CHECK (member_type IN ('normal', 'vip')),
    CONSTRAINT chk_member_status CHECK (status IN ('active', 'disabled')),
    CONSTRAINT chk_commission_balance CHECK (commission_balance >= 0)
);

-- 索引（带条件索引，排除已删除数据）
CREATE INDEX idx_member_username    ON t_member (username)    WHERE is_deleted = FALSE;
CREATE INDEX idx_member_phone       ON t_member (phone)       WHERE is_deleted = FALSE;
CREATE INDEX idx_member_type        ON t_member (member_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_referrer    ON t_member (referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_expire      ON t_member (expire_at)   WHERE is_deleted = FALSE;
CREATE INDEX idx_member_invite_code ON t_member (invite_code) WHERE is_deleted = FALSE;

-- ============================================================
-- 邀请码自动生成函数
-- ============================================================
CREATE OR REPLACE FUNCTION fn_generate_invite_code()
RETURNS VARCHAR AS $$
DECLARE
    chars TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    code VARCHAR(8);
    code_exists BOOLEAN;
BEGIN
    LOOP
        code := '';
        FOR i IN 1..8 LOOP
            code := code || substr(chars, floor(random() * length(chars) + 1)::int, 1);
        END LOOP;
        SELECT EXISTS(SELECT 1 FROM t_member WHERE invite_code = code) INTO code_exists;
        EXIT WHEN NOT code_exists;
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

-- 触发器：插入时自动生成邀请码
CREATE TRIGGER trg_member_invite_code
    BEFORE INSERT ON t_member
    FOR EACH ROW
    EXECUTE FUNCTION fn_auto_invite_code();

-- 触发器：更新时自动更新 updated_at
CREATE TRIGGER trg_member_updated_at
    BEFORE UPDATE ON t_member
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member                          IS '会员表';
COMMENT ON COLUMN t_member.id                       IS '会员ID（雪花算法）';
COMMENT ON COLUMN t_member.username                 IS '用户名（唯一，登录账号）';
COMMENT ON COLUMN t_member.password                 IS '密码（BCrypt加密）';
COMMENT ON COLUMN t_member.avatar                   IS '头像URL';
COMMENT ON COLUMN t_member.phone                    IS '手机号（唯一）';
COMMENT ON COLUMN t_member.invite_code              IS '邀请码（唯一，自动生成）';
COMMENT ON COLUMN t_member.member_type              IS '会员类型: normal-普通, vip-VIP';
COMMENT ON COLUMN t_member.expire_at                IS '会员到期时间';
COMMENT ON COLUMN t_member.referrer_id              IS '推荐人ID';
COMMENT ON COLUMN t_member.referrer_username        IS '推荐人用户名（冗余）';
COMMENT ON COLUMN t_member.commission_balance       IS '可提现佣金余额';
COMMENT ON COLUMN t_member.commission_total_earned  IS '累计获得佣金';
COMMENT ON COLUMN t_member.commission_total_paid    IS '累计已发放佣金';
COMMENT ON COLUMN t_member.status                   IS '账号状态: active-正常, disabled-禁用';
COMMENT ON COLUMN t_member.last_login_at            IS '最后登录时间';
COMMENT ON COLUMN t_member.last_login_ip            IS '最后登录IP';
COMMENT ON COLUMN t_member.is_deleted               IS '是否删除';

-- ============================================================
-- 用户资料表 (t_member_profile)
-- 描述：与 t_member 一对一，存储用户的个人资料（可选填）
-- ============================================================
CREATE TABLE t_member_profile (
    id                      BIGINT          PRIMARY KEY,
    member_id               BIGINT          NOT NULL UNIQUE,

    -- ==================== 个人信息 ====================
    real_name               VARCHAR(50),
    email                   VARCHAR(100),
    identity                VARCHAR(100),
    province                VARCHAR(30),
    city                    VARCHAR(50),
    major                   VARCHAR(100),
    grade                   VARCHAR(20),
    education_level         VARCHAR(20),

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
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_profile                  IS '用户资料表：与 t_member 一对一';
COMMENT ON COLUMN t_member_profile.id               IS '资料ID（雪花算法）';
COMMENT ON COLUMN t_member_profile.member_id        IS '关联会员表ID';
COMMENT ON COLUMN t_member_profile.real_name        IS '真实姓名';
COMMENT ON COLUMN t_member_profile.email            IS '邮箱';
COMMENT ON COLUMN t_member_profile.identity         IS '身份（如：北京大学）';
COMMENT ON COLUMN t_member_profile.province         IS '省份';
COMMENT ON COLUMN t_member_profile.city             IS '城市';
COMMENT ON COLUMN t_member_profile.major            IS '专业';
COMMENT ON COLUMN t_member_profile.grade            IS '年级（大三/研一）';
COMMENT ON COLUMN t_member_profile.education_level  IS '学历层次（大学生/研究生）';
