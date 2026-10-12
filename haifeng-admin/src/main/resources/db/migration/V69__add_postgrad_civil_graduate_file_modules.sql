-- V69__add_postgrad_civil_graduate_file_modules.sql
-- 新增「考研资源 / 考公资源 / 研究生资源」三个文件模块（挂在「文件管理」一级模块下，与初中/高中/大学资源平级），并授权超级管理员
-- 对应后端接口：
--   /api/v1/admin/fileload/postgrad-exam  -> @RequireAdminModule("fileload_postgrad_exam")
--   /api/v1/admin/fileload/civil-service  -> @RequireAdminModule("fileload_civil_service")
--   /api/v1/admin/fileload/graduate       -> @RequireAdminModule("fileload_graduate")
-- 说明：复用 t_file_info 表，以 target_audience='postgrad_exam' / 'civil_service' / 'graduate' 区分人群
--       （与 middle_school/high_school/college 同一设计，不新建物理表）。
--       模块层级：一级「文件管理」(fileload, id=2074728249800000001，见 V56)，
--       其下二级 初中(fileload_middle)/高中(fileload_high)/大学(fileload_college, V65, sort=6)，
--       本迁移新增三个二级模块，sort 取 7/8/9。

-- 1) 预置面向人群字典数据（已删除状态，仅作为枚举值参考，对齐 V54 初中生/高中生、V65 大学生 预置行）
INSERT INTO t_file_info (id, file_name, file_url, file_type, target_audience, version, is_deleted) VALUES
(9000000004, '考研人', '', '', 'postgrad_exam', 0, true),
(9000000005, '考公人', '', '', 'civil_service', 0, true),
(9000000006, '研究生', '', '', 'graduate',      0, true)
ON CONFLICT (id) DO NOTHING;

-- 2) 模块：考研/考公/研究生资源（parent_id=2074728249800000001「文件管理」，level=2，路径对齐后端 @RequestMapping）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES
(2074728249650000011, '考研资源',   'fileload_postgrad_exam', 2074728249800000001, 2, '/fileload/postgrad-exam', 7),
(2074728249650000021, '考公资源',   'fileload_civil_service', 2074728249800000001, 2, '/fileload/civil-service', 8),
(2074728249650000031, '研究生资源', 'fileload_graduate',      2074728249800000001, 2, '/fileload/graduate',      9)
ON CONFLICT (module_code) DO NOTHING;

-- 3) 角色-模块绑定：超级管理员(role_id=2074728248943710208) -> 三个新模块
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES
(2074728249650000012, 2074728248943710208, 2074728249650000011),
(2074728249650000022, 2074728248943710208, 2074728249650000021),
(2074728249650000032, 2074728248943710208, 2074728249650000031)
ON CONFLICT (role_id, module_id) DO NOTHING;
