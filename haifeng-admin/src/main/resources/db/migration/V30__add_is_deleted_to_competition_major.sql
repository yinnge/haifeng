-- V30__add_is_deleted_to_competition_major.sql
-- 为 t_competition_major 添加软删除支持和 updated_at 字段

ALTER TABLE t_competition_major
    ADD COLUMN is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- 添加组合唯一索引中的软删除过滤（保留原唯一约束，新增部分索引）
CREATE INDEX idx_cm_competition_active ON t_competition_major (competition_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_cm_major_active ON t_competition_major (major_id) WHERE is_deleted = FALSE;

-- 添加触发器
CREATE TRIGGER trg_competition_major_updated_at
    BEFORE UPDATE ON t_competition_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON COLUMN t_competition_major.is_deleted IS '是否删除：FALSE=正常，TRUE=已删除';
COMMENT ON COLUMN t_competition_major.updated_at IS '更新时间';
