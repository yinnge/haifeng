-- V33__fix_algorithm_config.sql
-- 修复：添加缺失的 updated_at / is_deleted / version 字段

BEGIN;

-- t_batch_score_line: 添加 updated_at 和 is_deleted
ALTER TABLE t_batch_score_line ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE t_batch_score_line ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN t_batch_score_line.updated_at IS '更新时间';
COMMENT ON COLUMN t_batch_score_line.is_deleted IS '软删除标记';

-- gaokao_config: 添加 updated_at 和 version（乐观锁）
ALTER TABLE gaokao_config ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE gaokao_config ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 0;

COMMENT ON COLUMN gaokao_config.updated_at IS '更新时间';
COMMENT ON COLUMN gaokao_config.version IS '乐观锁版本号';

COMMIT;
