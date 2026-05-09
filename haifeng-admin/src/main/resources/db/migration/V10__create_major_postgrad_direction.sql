-- ============================================
-- V10__create_major_postgrad_direction.sql
-- 本科专业-考研方向关联表
-- ============================================

-- ===========================================================
-- 本科专业-考研方向 关联表 (t_major_postgrad_direction)
-- 描述：本科专业 ↔ 考研专业 的多对多推荐关系
-- 关联关系说明（不使用外键约束，性能考虑）：
--   major_id -> t_major.id
--   postgrad_major_id -> t_postgrad_major.id
-- ===========================================================

CREATE TABLE IF NOT EXISTS t_major_postgrad_direction (
    id                      BIGINT          PRIMARY KEY,
    major_id                BIGINT          NOT NULL,
    postgrad_major_id       BIGINT          NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    postgrad_major_name     VARCHAR(100)    NOT NULL,
    sort_order              INTEGER         DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_major_postgrad UNIQUE (major_id, postgrad_major_id)
);

-- 索引
CREATE INDEX idx_mpd_major ON t_major_postgrad_direction (major_id);
CREATE INDEX idx_mpd_postgrad ON t_major_postgrad_direction (postgrad_major_id);
CREATE INDEX idx_mpd_major_name ON t_major_postgrad_direction USING btree (major_name varchar_pattern_ops);
CREATE INDEX idx_mpd_postgrad_name ON t_major_postgrad_direction USING btree (postgrad_major_name varchar_pattern_ops);

-- 注释
COMMENT ON TABLE t_major_postgrad_direction IS '本科专业-考研方向关联表：多对多，记录本科专业推荐的考研方向。关联：major_id -> t_major.id, postgrad_major_id -> t_postgrad_major.id';
COMMENT ON COLUMN t_major_postgrad_direction.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_major_postgrad_direction.major_id IS '本科专业ID（关联 t_major.id）';
COMMENT ON COLUMN t_major_postgrad_direction.postgrad_major_id IS '考研专业ID（关联 t_postgrad_major.id）';
COMMENT ON COLUMN t_major_postgrad_direction.major_name IS '本科专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_major_postgrad_direction.postgrad_major_name IS '考研专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_major_postgrad_direction.sort_order IS '推荐排序（数值越小越靠前）';
COMMENT ON COLUMN t_major_postgrad_direction.created_at IS '创建时间';
