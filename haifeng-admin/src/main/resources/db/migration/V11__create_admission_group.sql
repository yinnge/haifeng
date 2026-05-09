-- ============================================
-- V11__create_admission_group.sql
-- 专业组录取管理模块
-- ============================================

BEGIN;

-- ===========================================================
-- 1. t_subject_req_dict（选科要求字典表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_subject_req_dict (
    id                  SERIAL          PRIMARY KEY,
    code                VARCHAR(50)     NOT NULL UNIQUE,
    display_name        VARCHAR(100)    NOT NULL,
    requirement_level   SMALLINT        NOT NULL DEFAULT 0,
    subjects            TEXT[]          NOT NULL DEFAULT '{}',
    requirement_type    VARCHAR(10)     NOT NULL DEFAULT 'NONE',
    sort_order          INTEGER         DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_req_type CHECK (requirement_type IN ('NONE', 'ANY', 'ALL'))
);

-- 索引
CREATE INDEX idx_subject_req_level ON t_subject_req_dict (requirement_level);
CREATE INDEX idx_subject_req_sort ON t_subject_req_dict (sort_order);

-- 注释
COMMENT ON TABLE t_subject_req_dict IS '选科要求字典表：定义各种选科要求的编码、展示名称和限制等级';
COMMENT ON COLUMN t_subject_req_dict.id IS '主键ID（自增）';
COMMENT ON COLUMN t_subject_req_dict.code IS '选科要求编码（唯一）';
COMMENT ON COLUMN t_subject_req_dict.display_name IS '展示名称（如：物理必选、物理+化学均须）';
COMMENT ON COLUMN t_subject_req_dict.requirement_level IS '限制等级（0=不限，数值越大限制越严格）';
COMMENT ON COLUMN t_subject_req_dict.subjects IS '涉及科目数组（如：{物理,化学}）';
COMMENT ON COLUMN t_subject_req_dict.requirement_type IS '要求类型：NONE=不限，ANY=任选其一，ALL=均须选择';
COMMENT ON COLUMN t_subject_req_dict.sort_order IS '排序序号（数值越小越靠前）';
COMMENT ON COLUMN t_subject_req_dict.created_at IS '创建时间';
COMMENT ON COLUMN t_subject_req_dict.updated_at IS '更新时间';

-- 触发器：自动更新 updated_at
CREATE TRIGGER trg_subject_req_dict_updated
    BEFORE UPDATE ON t_subject_req_dict
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ===========================================================
-- 2. t_admission_group（专业组录取表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_group (
    id                      SERIAL          PRIMARY KEY,
    university_id           BIGINT          NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    subject_type            VARCHAR(20)     NOT NULL,
    batch                   VARCHAR(50)     NOT NULL,
    enrollment_code         VARCHAR(30),
    group_code              VARCHAR(30)     NOT NULL,
    group_name              VARCHAR(100),
    description             TEXT,
    subject_requirements    VARCHAR(50),
    requirement_level       SMALLINT        DEFAULT 0,
    constraints             TEXT[]          DEFAULT '{}',
    major_count             INTEGER         DEFAULT 0,
    category_count          INTEGER         DEFAULT 0,
    admission_count         INTEGER,
    min_score               INTEGER,
    min_rank                INTEGER,
    max_score               INTEGER,
    max_rank                INTEGER,
    avg_score               NUMERIC(6,2),
    avg_rank                INTEGER,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_admission_group UNIQUE (university_id, year, province, subject_type, batch, group_code)
);

-- 索引
CREATE INDEX idx_ag_university_year ON t_admission_group (university_id, year) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_province ON t_admission_group (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_subject ON t_admission_group (subject_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_batch ON t_admission_group (batch) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_min_score ON t_admission_group (min_score) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_year ON t_admission_group (year) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_constraints_gin ON t_admission_group USING GIN (constraints);

-- 注释
COMMENT ON TABLE t_admission_group IS '专业组录取表：记录高校各专业组的录取信息';
COMMENT ON COLUMN t_admission_group.id IS '主键ID（自增）';
COMMENT ON COLUMN t_admission_group.university_id IS '院校ID（关联 t_university.id）';
COMMENT ON COLUMN t_admission_group.year IS '招生年份';
COMMENT ON COLUMN t_admission_group.province IS '招生省份';
COMMENT ON COLUMN t_admission_group.subject_type IS '科类/选科类型（如：物理类、历史类、理科、文科）';
COMMENT ON COLUMN t_admission_group.batch IS '录取批次（如：本科一批、本科提前批）';
COMMENT ON COLUMN t_admission_group.enrollment_code IS '招生代码';
COMMENT ON COLUMN t_admission_group.group_code IS '专业组代码';
COMMENT ON COLUMN t_admission_group.group_name IS '专业组名称';
COMMENT ON COLUMN t_admission_group.description IS '专业组说明';
COMMENT ON COLUMN t_admission_group.subject_requirements IS '选科要求（关联 t_subject_req_dict.code）';
COMMENT ON COLUMN t_admission_group.requirement_level IS '选科要求限制等级（冗余字段，便于排序筛选）';
COMMENT ON COLUMN t_admission_group.constraints IS '约束条件数组（如：{只招男生,色盲不可报考}）';
COMMENT ON COLUMN t_admission_group.major_count IS '包含专业数量';
COMMENT ON COLUMN t_admission_group.category_count IS '包含专业类数量';
COMMENT ON COLUMN t_admission_group.admission_count IS '录取人数';
COMMENT ON COLUMN t_admission_group.min_score IS '最低分';
COMMENT ON COLUMN t_admission_group.min_rank IS '最低分对应位次';
COMMENT ON COLUMN t_admission_group.max_score IS '最高分';
COMMENT ON COLUMN t_admission_group.max_rank IS '最高分对应位次';
COMMENT ON COLUMN t_admission_group.avg_score IS '平均分';
COMMENT ON COLUMN t_admission_group.avg_rank IS '平均位次';
COMMENT ON COLUMN t_admission_group.is_deleted IS '是否删除：FALSE=正常，TRUE=已删除';
COMMENT ON COLUMN t_admission_group.created_at IS '创建时间';
COMMENT ON COLUMN t_admission_group.updated_at IS '更新时间';

-- 触发器：自动更新 updated_at
CREATE TRIGGER trg_admission_group_updated
    BEFORE UPDATE ON t_admission_group
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ===========================================================
-- 3. t_admission_major_score（专业录取明细表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_major_score (
    id                      SERIAL          PRIMARY KEY,
    group_id                INTEGER         NOT NULL REFERENCES t_admission_group(id) ON DELETE CASCADE,
    major_id                BIGINT,
    major_code              VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    subject_requirements    VARCHAR(200),
    requirement_level       SMALLINT        DEFAULT 0,
    education_level         VARCHAR(20),
    duration                VARCHAR(20),
    tuition                 VARCHAR(50),
    description             TEXT,
    admission_count         INTEGER,
    min_score               INTEGER,
    min_rank                INTEGER,
    max_score               INTEGER,
    max_rank                INTEGER,
    avg_score               NUMERIC(6,2),
    avg_rank                INTEGER,
    constraints             TEXT[]          DEFAULT '{}',
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_group_major UNIQUE (group_id, major_code)
);

-- 索引
CREATE INDEX idx_ams_group ON t_admission_major_score (group_id);
CREATE INDEX idx_ams_major ON t_admission_major_score (major_id);
CREATE INDEX idx_ams_major_code ON t_admission_major_score (major_code);
CREATE INDEX idx_ams_min_score ON t_admission_major_score (min_score);
CREATE INDEX idx_ams_constraints_gin ON t_admission_major_score USING GIN (constraints);

-- 注释
COMMENT ON TABLE t_admission_major_score IS '专业录取明细表：记录专业组内各专业的录取详情';
COMMENT ON COLUMN t_admission_major_score.id IS '主键ID（自增）';
COMMENT ON COLUMN t_admission_major_score.group_id IS '专业组ID（关联 t_admission_group.id）';
COMMENT ON COLUMN t_admission_major_score.major_id IS '专业ID（关联 t_major.id，可为空）';
COMMENT ON COLUMN t_admission_major_score.major_code IS '专业代码';
COMMENT ON COLUMN t_admission_major_score.major_name IS '专业名称';
COMMENT ON COLUMN t_admission_major_score.subject_requirements IS '专业级选科要求';
COMMENT ON COLUMN t_admission_major_score.requirement_level IS '选科要求限制等级';
COMMENT ON COLUMN t_admission_major_score.education_level IS '学历层次（如：本科、专科）';
COMMENT ON COLUMN t_admission_major_score.duration IS '学制（如：4年、5年）';
COMMENT ON COLUMN t_admission_major_score.tuition IS '学费信息';
COMMENT ON COLUMN t_admission_major_score.description IS '专业说明/备注';
COMMENT ON COLUMN t_admission_major_score.admission_count IS '录取人数';
COMMENT ON COLUMN t_admission_major_score.min_score IS '最低分';
COMMENT ON COLUMN t_admission_major_score.min_rank IS '最低分对应位次';
COMMENT ON COLUMN t_admission_major_score.max_score IS '最高分';
COMMENT ON COLUMN t_admission_major_score.max_rank IS '最高分对应位次';
COMMENT ON COLUMN t_admission_major_score.avg_score IS '平均分';
COMMENT ON COLUMN t_admission_major_score.avg_rank IS '平均位次';
COMMENT ON COLUMN t_admission_major_score.constraints IS '约束条件数组';
COMMENT ON COLUMN t_admission_major_score.created_at IS '创建时间';
COMMENT ON COLUMN t_admission_major_score.updated_at IS '更新时间';

-- 触发器：自动更新 updated_at
CREATE TRIGGER trg_admission_major_score_updated
    BEFORE UPDATE ON t_admission_major_score
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ===========================================================
-- 4. 函数和触发器
-- ===========================================================

-- 4.1 fn_get_requirement_level：根据选科要求编码查询限制等级
CREATE OR REPLACE FUNCTION fn_get_requirement_level(p_req VARCHAR)
RETURNS SMALLINT AS $$
DECLARE
    v_level SMALLINT;
BEGIN
    IF p_req IS NULL OR p_req = '' THEN
        RETURN 0;
    END IF;

    SELECT requirement_level INTO v_level
    FROM t_subject_req_dict
    WHERE code = p_req;

    RETURN COALESCE(v_level, 0);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_get_requirement_level(VARCHAR) IS '根据选科要求编码查询限制等级，找不到返回0';


-- 4.2 fn_auto_set_req_level：明细表INSERT/UPDATE时自动设置requirement_level
CREATE OR REPLACE FUNCTION fn_auto_set_req_level()
RETURNS TRIGGER AS $$
BEGIN
    NEW.requirement_level := fn_get_requirement_level(NEW.subject_requirements);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_auto_set_req_level() IS '触发器函数：自动设置选科要求的限制等级';

-- 触发器：明细表自动设置 requirement_level
CREATE TRIGGER trg_ams_auto_req_level
    BEFORE INSERT OR UPDATE OF subject_requirements ON t_admission_major_score
    FOR EACH ROW EXECUTE FUNCTION fn_auto_set_req_level();


-- 4.3 fn_recalc_group：重算单个专业组的聚合数据
CREATE OR REPLACE FUNCTION fn_recalc_group(p_group_id INTEGER)
RETURNS VOID AS $$
DECLARE
    v_major_count       INTEGER;
    v_category_count    INTEGER;
    v_admission_count   INTEGER;
    v_min_score         INTEGER;
    v_min_rank          INTEGER;
    v_max_score         INTEGER;
    v_max_rank          INTEGER;
    v_avg_score         NUMERIC(6,2);
    v_avg_rank          INTEGER;
BEGIN
    SELECT
        COUNT(*),
        COUNT(DISTINCT LEFT(major_code, 4)),
        SUM(admission_count),
        MIN(min_score),
        MAX(min_rank),
        MAX(max_score),
        MIN(max_rank),
        ROUND(AVG(avg_score)::NUMERIC, 2),
        ROUND(AVG(avg_rank))::INTEGER
    INTO
        v_major_count,
        v_category_count,
        v_admission_count,
        v_min_score,
        v_min_rank,
        v_max_score,
        v_max_rank,
        v_avg_score,
        v_avg_rank
    FROM t_admission_major_score
    WHERE group_id = p_group_id;

    UPDATE t_admission_group
    SET
        major_count = COALESCE(v_major_count, 0),
        category_count = COALESCE(v_category_count, 0),
        admission_count = v_admission_count,
        min_score = v_min_score,
        min_rank = v_min_rank,
        max_score = v_max_score,
        max_rank = v_max_rank,
        avg_score = v_avg_score,
        avg_rank = v_avg_rank,
        updated_at = NOW()
    WHERE id = p_group_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_recalc_group(INTEGER) IS '重算单个专业组的聚合统计数据（专业数、录取人数、分数/位次等）';


-- 4.4 fn_recalc_all_groups：全量重算所有专业组
CREATE OR REPLACE FUNCTION fn_recalc_all_groups()
RETURNS TABLE(recalced_count INTEGER) AS $$
DECLARE
    v_group_id INTEGER;
    v_count INTEGER := 0;
BEGIN
    FOR v_group_id IN SELECT id FROM t_admission_group WHERE is_deleted = FALSE
    LOOP
        PERFORM fn_recalc_group(v_group_id);
        v_count := v_count + 1;
    END LOOP;

    RETURN QUERY SELECT v_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_recalc_all_groups() IS '全量重算所有专业组的聚合数据，返回处理数量';


-- 4.5 fn_on_major_score_changed：明细表变更后自动触发重算
CREATE OR REPLACE FUNCTION fn_on_major_score_changed()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM fn_recalc_group(OLD.group_id);
        RETURN OLD;
    ELSE
        PERFORM fn_recalc_group(NEW.group_id);
        -- 如果 group_id 变了，也要重算旧组
        IF TG_OP = 'UPDATE' AND OLD.group_id <> NEW.group_id THEN
            PERFORM fn_recalc_group(OLD.group_id);
        END IF;
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_on_major_score_changed() IS '触发器函数：明细表变更后自动重算所属专业组的聚合数据';

-- 触发器：明细表变更后自动重算
CREATE TRIGGER trg_ams_recalc_group
    AFTER INSERT OR UPDATE OR DELETE ON t_admission_major_score
    FOR EACH ROW EXECUTE FUNCTION fn_on_major_score_changed();


-- ===========================================================
-- 5. 初始化数据：选科要求字典
-- ===========================================================
INSERT INTO t_subject_req_dict (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    -- 不限
    ('NONE', '不限', 0, '{}', 'NONE', 0),

    -- 单科必选
    ('PHY_REQUIRED', '物理必选', 10, '{物理}', 'ALL', 10),
    ('CHE_REQUIRED', '化学必选', 10, '{化学}', 'ALL', 11),
    ('BIO_REQUIRED', '生物必选', 10, '{生物}', 'ALL', 12),
    ('HIS_REQUIRED', '历史必选', 10, '{历史}', 'ALL', 13),
    ('GEO_REQUIRED', '地理必选', 10, '{地理}', 'ALL', 14),
    ('POL_REQUIRED', '政治必选', 10, '{政治}', 'ALL', 15),

    -- 2选1
    ('PHY_CHE_ANY', '物理/化学（2选1）', 5, '{物理,化学}', 'ANY', 20),
    ('PHY_BIO_ANY', '物理/生物（2选1）', 5, '{物理,生物}', 'ANY', 21),
    ('PHY_GEO_ANY', '物理/地理（2选1）', 5, '{物理,地理}', 'ANY', 22),
    ('CHE_BIO_ANY', '化学/生物（2选1）', 5, '{化学,生物}', 'ANY', 23),
    ('CHE_GEO_ANY', '化学/地理（2选1）', 5, '{化学,地理}', 'ANY', 24),
    ('HIS_GEO_ANY', '历史/地理（2选1）', 5, '{历史,地理}', 'ANY', 25),
    ('HIS_POL_ANY', '历史/政治（2选1）', 5, '{历史,政治}', 'ANY', 26),
    ('GEO_POL_ANY', '地理/政治（2选1）', 5, '{地理,政治}', 'ANY', 27),

    -- 2科均须
    ('PHY_CHE_ALL', '物理+化学均须', 20, '{物理,化学}', 'ALL', 30),
    ('PHY_BIO_ALL', '物理+生物均须', 20, '{物理,生物}', 'ALL', 31),
    ('PHY_GEO_ALL', '物理+地理均须', 20, '{物理,地理}', 'ALL', 32),
    ('CHE_BIO_ALL', '化学+生物均须', 20, '{化学,生物}', 'ALL', 33),
    ('CHE_GEO_ALL', '化学+地理均须', 20, '{化学,地理}', 'ALL', 34),
    ('HIS_GEO_ALL', '历史+地理均须', 20, '{历史,地理}', 'ALL', 35),
    ('HIS_POL_ALL', '历史+政治均须', 20, '{历史,政治}', 'ALL', 36),
    ('GEO_POL_ALL', '地理+政治均须', 20, '{地理,政治}', 'ALL', 37),

    -- 3选1
    ('PHY_CHE_BIO_ANY', '物理/化学/生物（3选1）', 3, '{物理,化学,生物}', 'ANY', 40),
    ('HIS_GEO_POL_ANY', '历史/地理/政治（3选1）', 3, '{历史,地理,政治}', 'ANY', 41),
    ('PHY_CHE_GEO_ANY', '物理/化学/地理（3选1）', 3, '{物理,化学,地理}', 'ANY', 42),
    ('CHE_BIO_GEO_ANY', '化学/生物/地理（3选1）', 3, '{化学,生物,地理}', 'ANY', 43)

ON CONFLICT (code) DO NOTHING;


COMMIT;
