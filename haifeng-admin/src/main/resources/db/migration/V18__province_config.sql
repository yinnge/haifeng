-- V18__province_config.sql
-- 省份算法配置表

CREATE TABLE IF NOT EXISTS t_province_config (
    province        VARCHAR(20)     PRIMARY KEY,
    density_k       NUMERIC(4,3)    DEFAULT 0.150,
    line_steepness  NUMERIC(4,2)    DEFAULT 2.80,
    rank_steepness  NUMERIC(4,2)    DEFAULT 2.40,
    created_at      TIMESTAMPTZ     DEFAULT NOW()
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
