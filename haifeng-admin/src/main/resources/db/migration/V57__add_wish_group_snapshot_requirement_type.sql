-- V57__add_wish_group_snapshot_requirement_type.sql
-- 志愿方案-专业组快照表新增选科要求类型字段（不限/2选1/3选1/必选1/必选2/必选3）

BEGIN;

ALTER TABLE t_wish_group_snapshot
    ADD COLUMN IF NOT EXISTS requirement_type VARCHAR(10);

COMMIT;
