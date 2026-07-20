-- V29__create_wish_plan_tables.sql
-- 志愿方案模块 (t_wish_plan, t_wish_group_snapshot, t_wish_major_snapshot)

BEGIN;

-- 1. 志愿方案主表
CREATE TABLE IF NOT EXISTS t_wish_plan (
    id                      SERIAL          PRIMARY KEY,
    member_id               BIGINT          NOT NULL,
    plan_name               VARCHAR(100)    DEFAULT '我的志愿方案1' NOT NULL,
    plan_year               SMALLINT        NOT NULL,
    plan_province           VARCHAR(30)     NOT NULL,
    reform_model            VARCHAR(20)     NOT NULL,
    plan_batch              VARCHAR(50)     NOT NULL,
    user_score              INTEGER         NOT NULL,
    user_rank               INTEGER         NOT NULL,
    bo_limit                INTEGER         DEFAULT 0 NOT NULL,
    chong_limit             INTEGER         DEFAULT 0 NOT NULL,
    wen_limit               INTEGER         DEFAULT 0 NOT NULL,
    bao_limit               INTEGER         DEFAULT 0 NOT NULL,
    die_limit               INTEGER         DEFAULT 0 NOT NULL,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wp_member ON t_wish_plan (member_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_wp_member_year ON t_wish_plan (member_id, plan_year) WHERE is_deleted = FALSE;

-- 2. 志愿方案-专业组快照表
CREATE TABLE IF NOT EXISTS t_wish_group_snapshot (
    id                      SERIAL          PRIMARY KEY,
    plan_id                 INTEGER         NOT NULL,
    group_id                INTEGER         NOT NULL,
    group_sort_order        INTEGER         NOT NULL DEFAULT 0,
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
    description             TEXT,
    constraints_description TEXT[]          DEFAULT '{}',
    category                VARCHAR(50)   NOT NULL,
    major_count             INTEGER       DEFAULT 0,
    nature                  VARCHAR(50),
    recommendation_rate     DECIMAL(5,2),
    recommendation_year     INTEGER,
    tags                    TEXT[],
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_twg_plan ON t_wish_group_snapshot (plan_id, group_sort_order);

-- 3. 志愿方案-专业明细快照表
CREATE TABLE IF NOT EXISTS t_wish_major_snapshot (
    id                      SERIAL          PRIMARY KEY,
    plan_id                 INTEGER         NOT NULL,
    group_snapshot_id       INTEGER         NOT NULL,
    major_id                BIGINT,
    major_sort_order        INTEGER         NOT NULL DEFAULT 0,
    is_exported             BOOLEAN         NOT NULL DEFAULT TRUE,
    major_code              VARCHAR(30)     NOT NULL,
    major_name              TEXT            NOT NULL,
    duration                VARCHAR(20),
    tuition                 VARCHAR(50),
    description             TEXT,
    admission_count         INTEGER,
    safety_level            NUMERIC(3,2),
    level_short             VARCHAR(10)     NOT NULL,
    history_scores          JSONB,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_twm_group ON t_wish_major_snapshot (group_snapshot_id, major_sort_order);
CREATE INDEX IF NOT EXISTS idx_twm_plan ON t_wish_major_snapshot (plan_id) WHERE is_exported = TRUE;

COMMIT;
