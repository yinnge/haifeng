-- V18__province_config.sql
-- 省份算法配置表

CREATE TABLE IF NOT EXISTS t_province_config (
    province                  VARCHAR(20)     PRIMARY KEY,
    density_k                 NUMERIC(4,3)    DEFAULT 0.150,
    line_steepness            NUMERIC(4,2)    DEFAULT 2.80,
    rank_steepness            NUMERIC(4,2)    DEFAULT 2.40,
    created_at                TIMESTAMPTZ     DEFAULT NOW(),
    updated_at                TIMESTAMPTZ     DEFAULT NOW(),
    version                   INTEGER         DEFAULT 0
);

COMMENT ON TABLE t_province_config IS '省份算法配置表';
COMMENT ON COLUMN t_province_config.province IS '省份名称（主键）';
COMMENT ON COLUMN t_province_config.density_k IS '同分密度惩罚系数，默认0.15';
COMMENT ON COLUMN t_province_config.line_steepness IS '线差Sigmoid陡度，默认2.8';
COMMENT ON COLUMN t_province_config.rank_steepness IS '位次Sigmoid陡度，默认2.4';
COMMENT ON COLUMN t_province_config.created_at IS '创建时间';

-- 初始化所有省份，使用默认值
INSERT INTO t_province_config (province)
SELECT DISTINCT province FROM t_province_reform
ON CONFLICT (province) DO NOTHING;


CREATE TABLE IF NOT EXISTS gaokao_config(
    id                       SMALLINT         PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    default_density_k        NUMERIC(4,3)     DEFAULT 0.150,
    default_line_steepness   NUMERIC(4,2)     DEFAULT 2.80,
    default_rank_steepness   NUMERIC(4,2)     DEFAULT 2.40,
    new_gaokao_line_weight   NUMERIC(4,2)     DEFAULT 0.42,
    new_gaokao_rank_weight   NUMERIC(4,2)     DEFAULT 0.50,
    old_gaokao_line_weight   NUMERIC(4,2)     DEFAULT 0.62,
    old_gaokao_rank_weight   NUMERIC(4,2)     DEFAULT 0.30,
    weight_soft_group        NUMERIC(3,1)     DEFAULT 0.6,
    weight_soft_both         NUMERIC(3,1)     DEFAULT 0.3,
    year_weights             NUMERIC(3,2)[]   DEFAULT ARRAY[1.00,0.80,0.60,0.40,0.20]::NUMERIC(3,2)[],
    updated_at               TIMESTAMPTZ      DEFAULT NOW(),
    version                  INTEGER          DEFAULT 0,
    created_at               TIMESTAMPTZ      DEFAULT NOW()
);

COMMENT ON TABLE  gaokao_config IS '高考算法全局参数表（单例，仅 id=1 一行）';
COMMENT ON COLUMN gaokao_config.id IS '主键，固定为 1（单例约束）';
COMMENT ON COLUMN gaokao_config.default_density_k IS '同分密度惩罚系数默认值（省份未配置时回退使用），用于 ScoreBasedCalculator';
COMMENT ON COLUMN gaokao_config.default_line_steepness IS '线差 Sigmoid 陡度默认值';
COMMENT ON COLUMN gaokao_config.default_rank_steepness IS '位次 Sigmoid 陡度默认值';
COMMENT ON COLUMN gaokao_config.new_gaokao_line_weight IS '新高考省份"线差"权重（ScoreBasedCalculator 加权使用）';
COMMENT ON COLUMN gaokao_config.new_gaokao_rank_weight IS '新高考省份"位次"权重';
COMMENT ON COLUMN gaokao_config.old_gaokao_line_weight IS '旧高考省份"线差"权重';
COMMENT ON COLUMN gaokao_config.old_gaokao_rank_weight IS '旧高考省份"位次"权重';
COMMENT ON COLUMN gaokao_config.weight_soft_group IS '仅专业组命中软约束时的权重折扣（ConstraintWeightCalculator）';
COMMENT ON COLUMN gaokao_config.weight_soft_both IS '专业组与专业同时命中软约束时的权重折扣';
COMMENT ON COLUMN gaokao_config.year_weights IS '近 5 年历史录取数据的衰减权重数组（下标 0 对应"距今 1 年"）';
COMMENT ON COLUMN gaokao_config.updated_at IS '更新时间';
COMMENT ON COLUMN gaokao_config.version IS '乐观锁版本号';
COMMENT ON COLUMN gaokao_config.created_at IS '创建时间';

INSERT INTO gaokao_config (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

COMMIT;