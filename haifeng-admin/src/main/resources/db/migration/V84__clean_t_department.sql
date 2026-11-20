-- ============================================================
-- V84: 清理 t_department 与 department_reports 的脏数据（保留表结构）
--
-- ⚠️ 警告：这是 Flyway 版本迁移，会在应用启动时【自动且仅执行一次】。
--    清空的是整表数据，本地/测试/生产都会被清空，请确认后再发布。
--    （本地若那 1 条有效数据需要保留，先备份再发布本迁移。）
-- ============================================================

-- 0. 表结构核查结论（无需在本脚本内处理）：
--    - t_department.id 为雪花算法生成，无自增序列 → TRUNCATE 无需 RESTART IDENTITY
--    - department_reports.department_id 仅为 UNIQUE + 普通索引，无外键约束
--      → TRUNCATE 不会被外键拦截，也不会级联，两张表需分别清理
--    - 全仓无向这两表写入 seed 数据的迁移脚本 → 清空后不会被重新填充

-- 1. 先清空院系主表。TRUNCATE 只清数据，不动表结构/索引/注释/约束。
TRUNCATE TABLE t_department;

-- 2. 再删孤儿报告：主表清空后，department_reports 中所有记录的 department_id
--    都找不到对应院系，即全部成为孤儿，这条 NOT EXISTS 会把它们全部删除。
--    ⚠️ 顺序不能反：若先删孤儿再 TRUNCATE，主表里有对应院系的报告不会被删，
--       清空主表后就会残留孤儿。
DELETE FROM department_reports r
 WHERE NOT EXISTS (
     SELECT 1 FROM t_department d WHERE d.id = r.department_id
 );

-- 3. 结果：两表均为空。验证语句（手动执行）：
--    SELECT count(*) AS dept_count FROM t_department;         -- 期望 0
--    SELECT count(*) AS report_count FROM department_reports; -- 期望 0
