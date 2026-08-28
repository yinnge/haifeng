-- ============================================
-- V62__alter_university_name_en_length.sql
-- 院校英文名扩长：部分院校英文全称超过 50 字符
-- （PostgreSQL 9.2+ 加宽 varchar 为元数据变更，不重写表）
-- ============================================

ALTER TABLE t_universities ALTER COLUMN name_en TYPE VARCHAR(100);

COMMENT ON COLUMN t_universities.name_en IS '院校英文名称';
