-- ============================================
-- V12__algorithm_config.sql
-- 算法配置管理模块
-- ============================================

BEGIN;

-- ===========================================================
-- 1. t_province_reform（省份高考改革配置表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_province_reform (
    id                  BIGINT          PRIMARY KEY,
    province            VARCHAR(20)     NOT NULL UNIQUE,
    reform_year         SMALLINT,
    reform_model        VARCHAR(20),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 注释
COMMENT ON TABLE t_province_reform IS '省份高考改革配置表：记录各省份的高考改革年份和模式';
COMMENT ON COLUMN t_province_reform.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_province_reform.province IS '省份名称（唯一）';
COMMENT ON COLUMN t_province_reform.reform_year IS '改革实施年份（NULL表示尚未改革）';
COMMENT ON COLUMN t_province_reform.reform_model IS '改革模式（如：3+3、3+1+2，NULL表示传统文理）';
COMMENT ON COLUMN t_province_reform.created_at IS '创建时间';


-- ===========================================================
-- 2. t_score_rank（一分一段位次表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_score_rank (
    id                  BIGINT          PRIMARY KEY,
    province            VARCHAR(20)     NOT NULL,
    year                SMALLINT        NOT NULL,
    subject_type        VARCHAR(20)     NOT NULL,
    score               SMALLINT        NOT NULL,
    rank                INTEGER         NOT NULL,
    same_score_count    INTEGER,
    cumulative_count    INTEGER,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_score_rank UNIQUE (province, year, subject_type, score)
);

-- 索引
CREATE INDEX idx_sr_lookup ON t_score_rank (province, year, subject_type, score);
CREATE INDEX idx_sr_rank_lookup ON t_score_rank (province, year, subject_type, rank);

-- 注释
COMMENT ON TABLE t_score_rank IS '一分一段位次表：记录各省份各年份的一分一段数据';
COMMENT ON COLUMN t_score_rank.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_score_rank.province IS '省份名称';
COMMENT ON COLUMN t_score_rank.year IS '年份';
COMMENT ON COLUMN t_score_rank.subject_type IS '科类/选科类型（如：物理类、历史类、理科、文科）';
COMMENT ON COLUMN t_score_rank.score IS '分数';
COMMENT ON COLUMN t_score_rank.rank IS '位次（名次）';
COMMENT ON COLUMN t_score_rank.same_score_count IS '同分人数';
COMMENT ON COLUMN t_score_rank.cumulative_count IS '累计人数';
COMMENT ON COLUMN t_score_rank.created_at IS '创建时间';


-- ===========================================================
-- 3. t_batch_score_line（批次分数线表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_batch_score_line (
    id                  BIGINT          PRIMARY KEY,
    province            VARCHAR(20)     NOT NULL,
    year                SMALLINT        NOT NULL,
    subject_type        VARCHAR(20)     NOT NULL,
    batch               VARCHAR(50)     NOT NULL,
    score_line          INTEGER         NOT NULL,
    rank_line           INTEGER,
    remark              VARCHAR(200),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_batch_score_line UNIQUE (province, year, subject_type, batch)
);

-- 索引
CREATE INDEX idx_bsl_lookup ON t_batch_score_line (province, year, subject_type);
CREATE INDEX idx_bsl_year ON t_batch_score_line (year);

-- 注释
COMMENT ON TABLE t_batch_score_line IS '批次分数线表：记录各省份各年份各批次的录取分数线';
COMMENT ON COLUMN t_batch_score_line.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_batch_score_line.province IS '省份名称';
COMMENT ON COLUMN t_batch_score_line.year IS '年份';
COMMENT ON COLUMN t_batch_score_line.subject_type IS '科类/选科类型（如：物理类、历史类、理科、文科）';
COMMENT ON COLUMN t_batch_score_line.batch IS '批次名称（如：本科一批、本科提前批）';
COMMENT ON COLUMN t_batch_score_line.score_line IS '分数线';
COMMENT ON COLUMN t_batch_score_line.rank_line IS '分数线对应位次';
COMMENT ON COLUMN t_batch_score_line.remark IS '备注说明';
COMMENT ON COLUMN t_batch_score_line.created_at IS '创建时间';


-- ===========================================================
-- 4. 初始化数据：省份高考改革配置
-- ===========================================================
INSERT INTO t_province_reform (id, province, reform_year, reform_model)
VALUES
    -- 第一批 3+3：上海、浙江（2017）
    (1890000000000001, '上海', 2017, '3+3'),
    (1890000000000002, '浙江', 2017, '3+3'),

    -- 第二批 3+3：北京、天津、山东、海南（2020）
    (1890000000000003, '北京', 2020, '3+3'),
    (1890000000000004, '天津', 2020, '3+3'),
    (1890000000000005, '山东', 2020, '3+3'),
    (1890000000000006, '海南', 2020, '3+3'),

    -- 第三批 3+1+2：广东、福建、河北、辽宁、湖北、湖南、重庆、江苏（2021）
    (1890000000000007, '广东', 2021, '3+1+2'),
    (1890000000000008, '福建', 2021, '3+1+2'),
    (1890000000000009, '河北', 2021, '3+1+2'),
    (1890000000000010, '辽宁', 2021, '3+1+2'),
    (1890000000000011, '湖北', 2021, '3+1+2'),
    (1890000000000012, '湖南', 2021, '3+1+2'),
    (1890000000000013, '重庆', 2021, '3+1+2'),
    (1890000000000014, '江苏', 2021, '3+1+2'),

    -- 第四批 3+1+2：吉林、黑龙江、安徽、江西、广西、贵州、甘肃（2024）
    (1890000000000015, '吉林', 2024, '3+1+2'),
    (1890000000000016, '黑龙江', 2024, '3+1+2'),
    (1890000000000017, '安徽', 2024, '3+1+2'),
    (1890000000000018, '江西', 2024, '3+1+2'),
    (1890000000000019, '广西', 2024, '3+1+2'),
    (1890000000000020, '贵州', 2024, '3+1+2'),
    (1890000000000021, '甘肃', 2024, '3+1+2'),

    -- 第五批 3+1+2：山西、河南、四川、云南、内蒙古、陕西、青海、宁夏（2025）
    (1890000000000022, '山西', 2025, '3+1+2'),
    (1890000000000023, '河南', 2025, '3+1+2'),
    (1890000000000024, '四川', 2025, '3+1+2'),
    (1890000000000025, '云南', 2025, '3+1+2'),
    (1890000000000026, '内蒙古', 2025, '3+1+2'),
    (1890000000000027, '陕西', 2025, '3+1+2'),
    (1890000000000028, '青海', 2025, '3+1+2'),
    (1890000000000029, '宁夏', 2025, '3+1+2'),

    -- 尚未改革：西藏、新疆（传统文理）
    (1890000000000030, '西藏', NULL, NULL),
    (1890000000000031, '新疆', NULL, NULL)

ON CONFLICT (province) DO NOTHING;


COMMIT;
