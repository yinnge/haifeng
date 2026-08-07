-- V47__add_study_duration_to_major.sql
-- t_major 表补全 design spec 中遗漏的 study_duration（学制）字段

ALTER TABLE t_major ADD COLUMN study_duration VARCHAR(20);

COMMENT ON COLUMN t_major.study_duration IS '学制（如：四年、三年）';
