-- ============================================================
-- V43__fix_major_category_null.sql
-- 清理 t_major.major_category 脏数据
--
-- 背景：专业数据导入时，部分记录的 major_category 被写成字面字符串 'null'
--      （而非真正的 NULL），导致前端「专业类别」下拉出现写着 "null" 的选项。
-- 修复：将这些字面字符串 'null' 置为真正的 NULL。
--
-- 幂等：仅更新 major_category = 'null' 的行；二次执行时无匹配行，UPDATE 0 行不报错。
-- 前端 fetchStats 已对 null / 'null' 做过滤兜底，本脚本为数据层根治。
-- ============================================================

UPDATE t_major
SET major_category = NULL,
    updated_at    = NOW()
WHERE major_category = 'null';
