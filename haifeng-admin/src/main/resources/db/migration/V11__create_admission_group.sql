-- ============================================
-- V11__create_admission_group.sql
-- 专业组录取管理模块
-- ============================================

BEGIN;

-- ===========================================================
-- 1. t_admission_group（专业组录取表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_group(
    id                      SERIAL          PRIMARY KEY,
    university_id           BIGINT          NOT NULL,
    university_name         VARCHAR(50)     NOT NULL,
    city_name               VARCHAR(50)     NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    batch                   VARCHAR(50)     NOT NULL,
    enrollment_code         VARCHAR(30),
    group_code              VARCHAR(30)     NOT NULL,
    group_name              VARCHAR(100),
    subjects                TEXT[]          DEFAULT '{}',
    requirement_type        VARCHAR(10)     DEFAULT '不限',
    description             TEXT,
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

    CONSTRAINT uk_admission_group UNIQUE (university_id, year, province, batch, group_code),
    CONSTRAINT chk_req_type CHECK (requirement_type IN ('不限', '2选1', '3选1', '必选1', '必选2', '必选3'))
);

-- 索引
CREATE INDEX idx_ag_university_year ON t_admission_group (university_id, year) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_university_name ON t_admission_group (university_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_city_name ON t_admission_group (city_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_province ON t_admission_group (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_batch ON t_admission_group (batch) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_min_score ON t_admission_group (min_score) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_year ON t_admission_group (year) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_req_type ON t_admission_group (requirement_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_ag_subjects_gin ON t_admission_group USING GIN (subjects);
CREATE INDEX idx_ag_constraints_gin ON t_admission_group USING GIN (constraints);

-- 注释
COMMENT ON TABLE t_admission_group IS '专业组录取表：记录高校各专业组的录取信息';
COMMENT ON COLUMN t_admission_group.id IS '主键ID（自增）';
COMMENT ON COLUMN t_admission_group.university_id IS '院校ID（关联 t_university.id）';
COMMENT ON COLUMN t_admission_group.year IS '招生年份';
COMMENT ON COLUMN t_admission_group.province IS '招生省份';
COMMENT ON COLUMN t_admission_group.batch IS '录取批次（如：本科批、提前批、专科批）';
COMMENT ON COLUMN t_admission_group.subjects IS '选科科目数组（如：{物理,化学}）';
COMMENT ON COLUMN t_admission_group.requirement_type IS '选科要求类型：不限/2选1/3选1/必选1/必选2/必选3';
COMMENT ON COLUMN t_admission_group.enrollment_code IS '招生代码';
COMMENT ON COLUMN t_admission_group.group_code IS '专业组代码';
COMMENT ON COLUMN t_admission_group.group_name IS '专业组名称';
COMMENT ON COLUMN t_admission_group.description IS '专业组说明';
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
-- 2. t_admission_major_score（专业录取明细表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_major_score (
    id                      SERIAL          PRIMARY KEY,
    group_id                INTEGER         NOT NULL REFERENCES t_admission_group(id) ON DELETE CASCADE,
    major_id                BIGINT,
    major_code              VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
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
-- 3. 函数和触发器
-- ===========================================================

-- 3.1 fn_recalc_group：重算单个专业组的聚合数据
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


-- 3.2 fn_recalc_all_groups：全量重算所有专业组
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


-- 3.3 fn_on_major_score_changed：明细表变更后自动触发重算
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


COMMIT;
