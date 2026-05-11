-- V14__t_special_channel__tables.sql
-- 特殊通道模块数据库表

-- ============================================================
-- 1. 特殊招生通道内容表 (t_special_channel)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_special_channel (
    id                  BIGINT          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL UNIQUE,
    channel_name        VARCHAR(50)     NOT NULL,
    subtitle            VARCHAR(200),
    parent_code         VARCHAR(30),
    filter_label        VARCHAR(30),
    display_type        VARCHAR(20)     NOT NULL,
    content             TEXT,
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_special_channel IS '特殊招生通道内容表';
COMMENT ON COLUMN t_special_channel.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_special_channel.channel_code IS '通道代码(唯一)';
COMMENT ON COLUMN t_special_channel.channel_name IS '通道名称';
COMMENT ON COLUMN t_special_channel.subtitle IS '副标题';
COMMENT ON COLUMN t_special_channel.parent_code IS '父级通道代码';
COMMENT ON COLUMN t_special_channel.filter_label IS '筛选按钮文字';
COMMENT ON COLUMN t_special_channel.display_type IS '展示类型: UNIVERSITY_LIST/ARTICLE_ONLY/MAJOR_DATA/GROUP';
COMMENT ON COLUMN t_special_channel.content IS '富文本内容(HTML)';
COMMENT ON COLUMN t_special_channel.sort_order IS '排序权重';
COMMENT ON COLUMN t_special_channel.is_active IS '是否启用';

CREATE INDEX idx_sc_display_type ON t_special_channel(display_type) WHERE is_active = TRUE;
CREATE INDEX idx_sc_parent ON t_special_channel(parent_code) WHERE is_active = TRUE;
CREATE INDEX idx_sc_name ON t_special_channel(channel_name);

-- ============================================================
-- 2. 通道-大学关联表 (t_special_channel_university)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_special_channel_university (
    id                  BIGINT          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL,
    channel_name        VARCHAR(50)     NOT NULL,
    university_id       BIGINT          NOT NULL,
    university_name     VARCHAR(50)     NOT NULL,
    year                SMALLINT,
    region_tag          VARCHAR(20),
    signup_start        TIMESTAMPTZ,
    signup_end          TIMESTAMPTZ,
    official_url        VARCHAR(500),
    brochure_title      VARCHAR(200),
    brochure_content    TEXT,
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_channel_univ UNIQUE (channel_code, university_id, year)
);

COMMENT ON TABLE t_special_channel_university IS '通道-大学关联表';
COMMENT ON COLUMN t_special_channel_university.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_special_channel_university.channel_code IS '通道代码';
COMMENT ON COLUMN t_special_channel_university.channel_name IS '通道名称(冗余)';
COMMENT ON COLUMN t_special_channel_university.university_id IS '大学ID';
COMMENT ON COLUMN t_special_channel_university.university_name IS '大学名称(冗余)';
COMMENT ON COLUMN t_special_channel_university.year IS '招生年份';
COMMENT ON COLUMN t_special_channel_university.region_tag IS '地区标签: 香港/澳门/NULL';
COMMENT ON COLUMN t_special_channel_university.signup_start IS '报名开始时间';
COMMENT ON COLUMN t_special_channel_university.signup_end IS '报名截止时间';
COMMENT ON COLUMN t_special_channel_university.official_url IS '报名官网URL';
COMMENT ON COLUMN t_special_channel_university.brochure_title IS '简章标题';
COMMENT ON COLUMN t_special_channel_university.brochure_content IS '简章正文(HTML)';
COMMENT ON COLUMN t_special_channel_university.sort_order IS '排序权重';
COMMENT ON COLUMN t_special_channel_university.is_active IS '是否启用';

CREATE INDEX idx_scu_channel ON t_special_channel_university(channel_code) WHERE is_active = TRUE;
CREATE INDEX idx_scu_region ON t_special_channel_university(channel_code, region_tag) WHERE is_active = TRUE;

-- ============================================================
-- 3. 强基计划入围/录取数据表 (t_strong_base_score)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_strong_base_score (
    id                      BIGINT          PRIMARY KEY,
    university_id           BIGINT          NOT NULL,
    university_name         VARCHAR(50)     NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    subject_type            VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    major_code              VARCHAR(20),
    entry_score             NUMERIC(7,2),
    entry_score_type        VARCHAR(30)     DEFAULT '高考成绩',
    entry_formula           VARCHAR(500),
    entry_ratio             VARCHAR(20),
    admission_score         NUMERIC(7,2),
    admission_formula       VARCHAR(500)    DEFAULT '高考成绩×85%+校测成绩×15%',
    plan_count              INTEGER,
    admission_count         INTEGER,
    remark                  VARCHAR(500),
    is_active               BOOLEAN         DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_strong_base UNIQUE (university_id, year, province, subject_type, major_name)
);

COMMENT ON TABLE t_strong_base_score IS '强基计划入围/录取数据表';
COMMENT ON COLUMN t_strong_base_score.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_strong_base_score.university_id IS '大学ID';
COMMENT ON COLUMN t_strong_base_score.university_name IS '大学名称(冗余)';
COMMENT ON COLUMN t_strong_base_score.year IS '年份';
COMMENT ON COLUMN t_strong_base_score.province IS '省份';
COMMENT ON COLUMN t_strong_base_score.subject_type IS '科类: 物理类/历史类/理科/文科/综合改革';
COMMENT ON COLUMN t_strong_base_score.major_name IS '专业名称';
COMMENT ON COLUMN t_strong_base_score.major_code IS '专业代码';
COMMENT ON COLUMN t_strong_base_score.entry_score IS '入围分数线';
COMMENT ON COLUMN t_strong_base_score.entry_score_type IS '入围分数类型: 高考成绩/加权成绩/校测初试';
COMMENT ON COLUMN t_strong_base_score.entry_formula IS '入围计算公式';
COMMENT ON COLUMN t_strong_base_score.entry_ratio IS '入围比例';
COMMENT ON COLUMN t_strong_base_score.admission_score IS '录取综合分';
COMMENT ON COLUMN t_strong_base_score.admission_formula IS '录取综合分计算公式';
COMMENT ON COLUMN t_strong_base_score.plan_count IS '招生计划数';
COMMENT ON COLUMN t_strong_base_score.admission_count IS '实际录取人数';
COMMENT ON COLUMN t_strong_base_score.remark IS '备注';
COMMENT ON COLUMN t_strong_base_score.is_active IS '是否启用';

CREATE INDEX idx_sbs_univ_year ON t_strong_base_score(university_id, year DESC);
CREATE INDEX idx_sbs_province ON t_strong_base_score(province, year DESC, subject_type);

-- ============================================================
-- 4. 强基计划院校配置表 (t_strong_base_university)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_strong_base_university (
    id                          BIGINT          PRIMARY KEY,
    university_id               BIGINT          NOT NULL UNIQUE,
    university_name             VARCHAR(50)     NOT NULL,
    is_pilot                    BOOLEAN         DEFAULT TRUE,
    pilot_year                  SMALLINT,
    official_url                VARCHAR(500),
    signup_url                  VARCHAR(500),
    test_before_score           BOOLEAN         DEFAULT FALSE,
    default_entry_ratio         VARCHAR(20)     DEFAULT '1:5',
    default_admission_formula   VARCHAR(500)    DEFAULT '高考成绩×85%+校测成绩×15%',
    available_majors            TEXT[],
    special_notes               TEXT,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE t_strong_base_university IS '强基计划院校配置表';
COMMENT ON COLUMN t_strong_base_university.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_strong_base_university.university_id IS '大学ID(唯一)';
COMMENT ON COLUMN t_strong_base_university.university_name IS '大学名称(冗余)';
COMMENT ON COLUMN t_strong_base_university.is_pilot IS '是否强基试点校';
COMMENT ON COLUMN t_strong_base_university.pilot_year IS '首次试点年份';
COMMENT ON COLUMN t_strong_base_university.official_url IS '强基计划官方页面URL';
COMMENT ON COLUMN t_strong_base_university.signup_url IS '报名入口URL';
COMMENT ON COLUMN t_strong_base_university.test_before_score IS '是否高考出分前校测';
COMMENT ON COLUMN t_strong_base_university.default_entry_ratio IS '默认入围比例';
COMMENT ON COLUMN t_strong_base_university.default_admission_formula IS '默认录取综合分公式';
COMMENT ON COLUMN t_strong_base_university.available_majors IS '可选专业列表';
COMMENT ON COLUMN t_strong_base_university.special_notes IS '特殊说明';

CREATE INDEX idx_sbu_pilot ON t_strong_base_university(is_pilot);
CREATE INDEX idx_sbu_name ON t_strong_base_university(university_name);
