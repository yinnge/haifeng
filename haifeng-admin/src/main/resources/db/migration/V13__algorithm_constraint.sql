-- ============================================================
-- V13: 算法约束模块
-- ============================================================

-- ============================================================
-- 1. 约束条件字典表 (t_constraint_dict)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_constraint_dict (
    code                VARCHAR(50)     PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL UNIQUE,
    category            VARCHAR(30)     NOT NULL,
    description         TEXT,
    severity            VARCHAR(10)     NOT NULL DEFAULT 'HARD',
    check_field         VARCHAR(50),
    check_operator      VARCHAR(20),
    check_value         VARCHAR(100),
    extra_field         VARCHAR(50),
    extra_operator      VARCHAR(20),
    extra_value         VARCHAR(100),
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_severity CHECK (severity IN ('HARD', 'SOFT'))
);

COMMENT ON TABLE t_constraint_dict IS '约束条件字典表';
COMMENT ON COLUMN t_constraint_dict.code IS '约束代码（主键）';
COMMENT ON COLUMN t_constraint_dict.name IS '约束名称';
COMMENT ON COLUMN t_constraint_dict.category IS '约束大类';
COMMENT ON COLUMN t_constraint_dict.severity IS 'HARD=硬限制/SOFT=软提示';
COMMENT ON COLUMN t_constraint_dict.check_field IS '对应t_member_gaokao表字段';
COMMENT ON COLUMN t_constraint_dict.check_operator IS '判断运算符';
COMMENT ON COLUMN t_constraint_dict.check_value IS '判断值';

CREATE INDEX idx_cd_category ON t_constraint_dict (category);
CREATE INDEX idx_cd_is_active ON t_constraint_dict (is_active);

CREATE TRIGGER trg_constraint_dict_updated_at
    BEFORE UPDATE ON t_constraint_dict
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ============================================================
-- 2. 专业约束关联表 (t_major_constraint)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_major_constraint (
    id                  BIGINT          PRIMARY KEY,
    major_code          VARCHAR(20)     NOT NULL,
    major_name          VARCHAR(100)    NOT NULL,
    constraint_code     VARCHAR(50)     NOT NULL,
    constraint_name     VARCHAR(100)    NOT NULL,
    remark              VARCHAR(200),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_major_constraint UNIQUE (major_code, constraint_code)
);

COMMENT ON TABLE t_major_constraint IS '专业约束关联表';
COMMENT ON COLUMN t_major_constraint.major_code IS '专业代码';
COMMENT ON COLUMN t_major_constraint.major_name IS '专业名称';
COMMENT ON COLUMN t_major_constraint.constraint_code IS '约束代码';
COMMENT ON COLUMN t_major_constraint.constraint_name IS '约束名称';

CREATE INDEX idx_mc_major ON t_major_constraint (major_code);
CREATE INDEX idx_mc_constraint ON t_major_constraint (constraint_code);

-- ============================================================
-- 3. 安全系数等级字典 (t_safety_level_dict)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_safety_level_dict (
    level               SMALLINT        PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL UNIQUE,
    name                VARCHAR(30)     NOT NULL,
    name_short          VARCHAR(10)     NOT NULL,
    min_coefficient     NUMERIC(3,2)    NOT NULL,
    max_coefficient     NUMERIC(3,2)    NOT NULL,
    color               VARCHAR(20),
    confidence          VARCHAR(20),
    confidence_reason   VARCHAR(150),
    description         TEXT,
    CONSTRAINT chk_coeff_range CHECK (min_coefficient < max_coefficient)
);

COMMENT ON TABLE t_safety_level_dict IS '安全系数等级字典';
COMMENT ON COLUMN t_safety_level_dict.level IS '等级编号1-5';
COMMENT ON COLUMN t_safety_level_dict.code IS '代码';
COMMENT ON COLUMN t_safety_level_dict.name IS '中文名称';
COMMENT ON COLUMN t_safety_level_dict.name_short IS '简称';
COMMENT ON COLUMN t_safety_level_dict.min_coefficient IS '系数下界';
COMMENT ON COLUMN t_safety_level_dict.max_coefficient IS '系数上界';
