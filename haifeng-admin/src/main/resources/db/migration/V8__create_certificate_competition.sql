-- ============================================
-- V8__create_certificate_competition.sql
-- 竞赛证书管理模块：证书表、竞赛表、竞赛详情表、竞赛-专业关联表
-- ============================================

-- ===========================================================
-- 1. 证书表 (t_certificate)
-- 描述：各类职业资格证书、等级考试证书信息
-- ===========================================================
CREATE TABLE t_certificate (
    id                      BIGINT          PRIMARY KEY,
    cert_name               VARCHAR(150)    NOT NULL,
    category                VARCHAR(50),
    cert_level              VARCHAR(50),
    applicable_major        VARCHAR(200),
    registration_time       VARCHAR(100),
    exam_time               VARCHAR(100),
    exam_fee                INTEGER,
    cert_intro              TEXT,
    exam_requirements       TEXT[]          DEFAULT '{}',
    exam_arrangement        TEXT,
    official_website        VARCHAR(500),
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_cert_name UNIQUE (cert_name),
    CONSTRAINT chk_cert_exam_fee CHECK (exam_fee IS NULL OR exam_fee >= 0)
);

-- 索引
CREATE INDEX idx_cert_category ON t_certificate (category) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_level ON t_certificate (cert_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_name_search ON t_certificate USING btree (cert_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_major ON t_certificate USING btree (applicable_major varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_cert_requirements ON t_certificate USING gin (exam_requirements) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_certificate_updated_at
    BEFORE UPDATE ON t_certificate
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_certificate IS '证书信息表：存储各类职业资格证书、等级考试证书信息';
COMMENT ON COLUMN t_certificate.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_certificate.cert_name IS '证书名称';
COMMENT ON COLUMN t_certificate.category IS '证书分类（如：IT类、财会类、语言类、工程类）';
COMMENT ON COLUMN t_certificate.cert_level IS '证书等级（如：初级、中级、高级）';
COMMENT ON COLUMN t_certificate.applicable_major IS '适用专业（如：计算机类、金融类）';
COMMENT ON COLUMN t_certificate.registration_time IS '报名时间（如：每年3月/9月）';
COMMENT ON COLUMN t_certificate.exam_time IS '考试时间（如：5月中旬、11月上旬）';
COMMENT ON COLUMN t_certificate.exam_fee IS '考试费用（元）';
COMMENT ON COLUMN t_certificate.cert_intro IS '证书简介';
COMMENT ON COLUMN t_certificate.exam_requirements IS '报考条件列表（数组）';
COMMENT ON COLUMN t_certificate.exam_arrangement IS '考试安排详情';
COMMENT ON COLUMN t_certificate.official_website IS '官方网站链接';
COMMENT ON COLUMN t_certificate.is_deleted IS '是否删除：FALSE=正常，TRUE=已删除';
COMMENT ON COLUMN t_certificate.created_at IS '创建时间';
COMMENT ON COLUMN t_certificate.updated_at IS '更新时间';


-- ===========================================================
-- 2. 竞赛表 (t_competition)
-- 描述：各类学科竞赛、创新创业大赛基本信息
-- ===========================================================
CREATE TABLE t_competition (
    id                      BIGINT          PRIMARY KEY,
    comp_name               VARCHAR(200)    NOT NULL,
    comp_level              VARCHAR(50),
    registration_time       VARCHAR(100),
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_comp_name UNIQUE (comp_name)
);

-- 索引
CREATE INDEX idx_comp_level ON t_competition (comp_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_comp_name_search ON t_competition USING btree (comp_name varchar_pattern_ops) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_competition_updated_at
    BEFORE UPDATE ON t_competition
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_competition IS '竞赛基本信息表：存储各类学科竞赛、创新创业大赛基本信息';
COMMENT ON COLUMN t_competition.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_competition.comp_name IS '竞赛名称';
COMMENT ON COLUMN t_competition.comp_level IS '竞赛级别（如：国家级、省级、校级）';
COMMENT ON COLUMN t_competition.registration_time IS '报名时间';
COMMENT ON COLUMN t_competition.is_deleted IS '是否删除：FALSE=正常，TRUE=已删除';
COMMENT ON COLUMN t_competition.created_at IS '创建时间';
COMMENT ON COLUMN t_competition.updated_at IS '更新时间';


-- ===========================================================
-- 3. 竞赛详情表 (t_competition_detail) - 与 t_competition 1:1
-- 关联关系说明（不使用外键约束，性能考虑）：
--   competition_id -> t_competition.id
-- ===========================================================
CREATE TABLE t_competition_detail (
    id                      BIGINT          PRIMARY KEY,
    competition_id          BIGINT          NOT NULL,
    basic_info              JSONB           DEFAULT '{}'::JSONB,
    awards                  TEXT[]          DEFAULT '{}',
    background              TEXT,
    purposes                TEXT[]          DEFAULT '{}',
    competition_rules       JSONB           DEFAULT '[]'::JSONB,
    scoring_criteria        TEXT[]          DEFAULT '{}',
    notices                 TEXT[]          DEFAULT '{}',
    process_guide           JSONB           DEFAULT '[]'::JSONB,
    awards_display          JSONB           DEFAULT '[]'::JSONB,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_competition_detail_competition_id UNIQUE (competition_id)
);

-- 索引（competition_id 已通过 UNIQUE 约束自动创建索引）
CREATE INDEX idx_comp_detail_awards ON t_competition_detail USING gin (awards) WHERE is_deleted = FALSE;
CREATE INDEX idx_comp_detail_basic_info ON t_competition_detail USING gin (basic_info) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_competition_detail_updated_at
    BEFORE UPDATE ON t_competition_detail
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_competition_detail IS '竞赛详情表：与 t_competition 一对一，存储竞赛完整信息。关联：competition_id -> t_competition.id';
COMMENT ON COLUMN t_competition_detail.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_competition_detail.competition_id IS '关联竞赛表ID（关联 t_competition.id，一对一）';
COMMENT ON COLUMN t_competition_detail.basic_info IS '基本信息（JSONB，包含：主办方、举办时间、参赛对象、参赛形式、级别、报名费、官网、联系邮箱、联系电话）';
COMMENT ON COLUMN t_competition_detail.awards IS '奖项设置列表（如：国家一等奖、国家二等奖等）';
COMMENT ON COLUMN t_competition_detail.background IS '竞赛背景与意义';
COMMENT ON COLUMN t_competition_detail.purposes IS '竞赛目的列表';
COMMENT ON COLUMN t_competition_detail.competition_rules IS '竞赛规则（JSONB数组，每项含title和content字段）';
COMMENT ON COLUMN t_competition_detail.scoring_criteria IS '评分标准列表';
COMMENT ON COLUMN t_competition_detail.notices IS '注意事项列表';
COMMENT ON COLUMN t_competition_detail.process_guide IS '参赛流程指南（JSONB数组，每项含title和content字段）';
COMMENT ON COLUMN t_competition_detail.awards_display IS '奖项设置展示（JSONB数组，每项含title和content字段）';
COMMENT ON COLUMN t_competition_detail.is_deleted IS '是否删除：FALSE=正常，TRUE=已删除';
COMMENT ON COLUMN t_competition_detail.created_at IS '创建时间';
COMMENT ON COLUMN t_competition_detail.updated_at IS '更新时间';


-- ===========================================================
-- 4. 竞赛-专业关联表 (t_competition_major) - 多对多
-- 关联关系说明（不使用外键约束，性能考虑）：
--   competition_id -> t_competition.id
--   major_id -> t_major.id
-- ===========================================================
CREATE TABLE t_competition_major (
    id                      BIGINT          PRIMARY KEY,
    competition_id          BIGINT          NOT NULL,
    major_id                BIGINT          NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    competition_name        VARCHAR(200)    NOT NULL,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_comp_major UNIQUE (competition_id, major_id)
);

-- 索引
-- 竞赛详情页：该竞赛适合哪些专业
CREATE INDEX idx_cm_competition ON t_competition_major (competition_id);
-- 专业详情页：该专业能参加哪些竞赛（反向查询）
CREATE INDEX idx_cm_major ON t_competition_major (major_id);

-- 注释
COMMENT ON TABLE t_competition_major IS '竞赛-专业关联表：多对多，记录竞赛适合哪些专业。关联：competition_id -> t_competition.id, major_id -> t_major.id';
COMMENT ON COLUMN t_competition_major.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_competition_major.competition_id IS '竞赛ID（关联 t_competition.id）';
COMMENT ON COLUMN t_competition_major.major_id IS '专业ID（关联 t_major.id）';
COMMENT ON COLUMN t_competition_major.major_name IS '专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_competition_major.competition_name IS '竞赛名称（冗余，方便展示）';
COMMENT ON COLUMN t_competition_major.created_at IS '创建时间';
