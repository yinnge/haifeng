-- V59__wish_major_snapshot_add_education_level_constraints.sql
-- 志愿专业快照表补齐 education_level、constraints 两个字段（与 AdmissionMajorScore 对齐）

BEGIN;

-- 1. 加列
ALTER TABLE t_wish_major_snapshot
    ADD COLUMN education_level VARCHAR(20),
    ADD COLUMN constraints     TEXT[] DEFAULT '{}';

COMMENT ON COLUMN t_wish_major_snapshot.education_level IS '学历层次（如：本科、专科），来源于 t_admission_major_score';
COMMENT ON COLUMN t_wish_major_snapshot.constraints     IS '约束条件数组（如：{只招男生,色盲不可报考}），来源于 t_admission_major_score';

-- 2. 回填存量数据：通过 major_id 关联 t_admission_major_score 回填
UPDATE t_wish_major_snapshot wms
SET education_level = ams.education_level,
    constraints     = ams.constraints
FROM t_admission_major_score ams
WHERE wms.major_id = ams.id
  AND (wms.education_level IS NULL OR wms.education_level = '');

COMMIT;
