-- V61__drop_admission_group_constraints.sql
-- 删除已弃用的「专业组约束」字段（前端/后端全链路移除，不再使用）：
--   1. t_admission_group.constraints            （专业组约束数组，随索引一并删除）
--   2. t_wish_group_snapshot.constraints_description（志愿表专业组快照不再冻结该字段）
-- 注意：t_admission_major_score.constraints（专业明细约束）属于独立功能，保留不动。

BEGIN;

-- 1. 专业组约束字段 + 其 GIN 索引（DROP COLUMN 会自动删索引，这里显式删更清晰）
DROP INDEX IF EXISTS idx_ag_constraints_gin;
ALTER TABLE t_admission_group DROP COLUMN IF EXISTS constraints;

-- 2. 志愿表专业组快照约束描述字段
ALTER TABLE t_wish_group_snapshot DROP COLUMN IF EXISTS constraints_description;

COMMIT;
