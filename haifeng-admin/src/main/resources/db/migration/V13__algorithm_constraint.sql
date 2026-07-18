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
    is_deleted          BOOLEAN         DEFAULT FALSE,
    version             INTEGER         DEFAULT 0,
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
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN         DEFAULT FALSE,
    version             INTEGER         DEFAULT 0,
    CONSTRAINT uk_major_constraint UNIQUE (major_code, constraint_code)
);

COMMENT ON TABLE t_major_constraint IS '专业约束关联表';
COMMENT ON COLUMN t_major_constraint.major_code IS '专业代码';
COMMENT ON COLUMN t_major_constraint.major_name IS '专业名称';
COMMENT ON COLUMN t_major_constraint.constraint_code IS '约束代码';
COMMENT ON COLUMN t_major_constraint.constraint_name IS '约束名称';

CREATE INDEX idx_mc_major ON t_major_constraint (major_code);
CREATE INDEX idx_mc_constraint ON t_major_constraint (constraint_code);

CREATE TRIGGER trg_major_constraint_updated_at
    BEFORE UPDATE ON t_major_constraint
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

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
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    is_deleted          BOOLEAN         DEFAULT FALSE,
    version             INTEGER         DEFAULT 0,
    CONSTRAINT chk_coeff_range CHECK (min_coefficient < max_coefficient)
);

COMMENT ON TABLE t_safety_level_dict IS '安全系数等级字典';
COMMENT ON COLUMN t_safety_level_dict.level IS '等级编号1-5';
COMMENT ON COLUMN t_safety_level_dict.code IS '代码';
COMMENT ON COLUMN t_safety_level_dict.name IS '中文名称';
COMMENT ON COLUMN t_safety_level_dict.name_short IS '简称';
COMMENT ON COLUMN t_safety_level_dict.min_coefficient IS '系数下界';
COMMENT ON COLUMN t_safety_level_dict.max_coefficient IS '系数上界';

CREATE TRIGGER trg_safety_level_dict_updated_at
    BEFORE UPDATE ON t_safety_level_dict
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 初始化安全系数等级数据
INSERT INTO t_safety_level_dict
    (level, code, name, name_short, min_coefficient, max_coefficient, color, description)
VALUES
    (1, 'REACH_HIGH',   '大胆冲刺',   '搏',   0.00, 0.30,
     '#FF4D4F',
     '录取概率极低，属于"彩票"志愿。历年数据显示您的位次远低于该校录取位次。' ||
     '建议最多填1-2个冲刺志愿，且必须搭配稳妥志愿。'),

    (2, 'REACH',        '可以冲击',   '冲',   0.30, 0.50,
     '#FFA940',
     '有一定录取可能，但风险较大。适合放在志愿表靠前位置。' ||
     '如果该校当年报考热度下降或扩招，有希望录取。'),

    (3, 'MATCH',        '较为稳妥',   '稳',   0.50, 0.70,
     '#FADB14',
     '录取概率中等偏上，属于"正常发挥"就能录取的范围。' ||
     '建议作为志愿表的核心区域，多填几个此档位的志愿。'),

    (4, 'SAFE',         '比较安全',   '保',   0.70, 0.85,
     '#52C41A',
     '录取概率较高，除非出现大小年极端波动，基本能录取。' ||
     '建议至少填3-5个此档位的志愿作为安全保障。'),

    (5, 'FLOOR',        '高度保底',   '垫',   0.85, 1.00,
     '#1890FF',
     '录取概率极高，几乎确定能录取。用于防止"滑档"（所有志愿都录不上）。' ||
     '建议至少放1-2个垫底志愿在最后。');
