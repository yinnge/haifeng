-- ============================================
-- V77__add_sort_order_to_reference_tables.sql
-- 为 C 端热点数据优先展示，给 7 张字典/内容表增加 sort_order 排序权重。
-- 范式对齐已存在的 t_resource（INTEGER NOT NULL DEFAULT 0 + 复合索引 (sort_order, created_at DESC)）。
-- 存量数据因 DEFAULT 0 自动落 0，无需回填。
-- ============================================

-- 城市
ALTER TABLE t_city ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_city.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_city_sort ON t_city (sort_order, created_at DESC) WHERE is_deleted = FALSE;

-- 专业
ALTER TABLE t_major ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_major.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_major_sort ON t_major (sort_order, created_at DESC) WHERE status = 1;

-- 考研专业
ALTER TABLE t_postgrad_major ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_postgrad_major.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_postgrad_major_sort ON t_postgrad_major (sort_order, created_at DESC) WHERE status = 1;

-- 行业
ALTER TABLE t_industry ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_industry.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_industry_sort ON t_industry (sort_order, created_at DESC) WHERE is_deleted = FALSE;

-- 企业
ALTER TABLE t_enterprise ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_enterprise.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_enterprise_sort ON t_enterprise (sort_order, created_at DESC) WHERE is_deleted = FALSE;

-- 竞赛
ALTER TABLE t_competition ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_competition.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_competition_sort ON t_competition (sort_order, created_at DESC) WHERE is_deleted = FALSE;

-- 证书
ALTER TABLE t_certificate ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
COMMENT ON COLUMN t_certificate.sort_order IS '排序权重（越大越靠前，C 端热点优先展示）';
CREATE INDEX IF NOT EXISTS idx_certificate_sort ON t_certificate (sort_order, created_at DESC) WHERE is_deleted = FALSE;
