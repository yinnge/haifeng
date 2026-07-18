-- V34__add_version_batch_score_line.sql
-- 修复：t_batch_score_line 实体有 @Version 但 DB 缺少 version 列

ALTER TABLE t_batch_score_line ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

COMMENT ON COLUMN t_batch_score_line.version IS '乐观锁版本号';
