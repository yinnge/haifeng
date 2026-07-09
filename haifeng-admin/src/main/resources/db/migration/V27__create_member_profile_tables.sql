-- V27__create_member_profile_tables.sql
-- 用户资料表 (t_member_profile)
-- 描述：与 t_member 一对一，存储用户的个人资料（可选填）

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_profile (
    id                      BIGINT          PRIMARY KEY,
    member_id               BIGINT          NOT NULL UNIQUE,

    -- 个人信息
    real_name               VARCHAR(50),
    email                   VARCHAR(100),
    gender                  VARCHAR(10),
    school_name             VARCHAR(100),
    province                VARCHAR(30),
    city                    VARCHAR(50),
    major                   VARCHAR(100),
    identity                VARCHAR(20),
    grade                   VARCHAR(20),
    education_level         VARCHAR(20),

    -- 统计字段
    favorite_count          INTEGER         DEFAULT 0 NOT NULL,
    view_count              INTEGER         DEFAULT 0 NOT NULL,

    -- 审计字段
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_profile_member_id ON t_member_profile (member_id);
CREATE INDEX IF NOT EXISTS idx_profile_identity  ON t_member_profile (identity);
CREATE INDEX IF NOT EXISTS idx_profile_province  ON t_member_profile (province);

-- 外键（仅当不存在时添加）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_profile_member'
          AND conrelid = 't_member_profile'::regclass
    ) THEN
        ALTER TABLE t_member_profile
            ADD CONSTRAINT fk_profile_member
            FOREIGN KEY (member_id) REFERENCES t_member(id);
    END IF;
END
$$;

-- 触发器（仅当不存在时创建）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger
        WHERE tgname = 'trg_member_profile_updated_at'
          AND tgrelid = 't_member_profile'::regclass
    ) THEN
        CREATE TRIGGER trg_member_profile_updated_at
            BEFORE UPDATE ON t_member_profile
            FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
    END IF;
END
$$;

-- 注释
COMMENT ON TABLE  t_member_profile              IS '用户资料表：与 t_member 一对一';
COMMENT ON COLUMN t_member_profile.member_id    IS '关联会员表ID';
COMMENT ON COLUMN t_member_profile.real_name    IS '真实姓名';
COMMENT ON COLUMN t_member_profile.email        IS '邮箱';
COMMENT ON COLUMN t_member_profile.gender       IS '性别（男/女）';
COMMENT ON COLUMN t_member_profile.school_name  IS '学校名称';
COMMENT ON COLUMN t_member_profile.province     IS '省份';
COMMENT ON COLUMN t_member_profile.city         IS '城市';
COMMENT ON COLUMN t_member_profile.major        IS '专业';
COMMENT ON COLUMN t_member_profile.identity     IS '身份（高中生/大学生/研究生/其他）';
COMMENT ON COLUMN t_member_profile.grade        IS '年级';
COMMENT ON COLUMN t_member_profile.education_level IS '学历层次';
COMMENT ON COLUMN t_member_profile.favorite_count  IS '收藏数';
COMMENT ON COLUMN t_member_profile.view_count      IS '浏览数';

COMMIT;
