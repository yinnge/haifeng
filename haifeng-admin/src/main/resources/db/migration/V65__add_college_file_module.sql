-- V65__add_college_file_module.sql
-- 新增「大学资源」模块（挂在「文件管理」一级模块下，与初中/高中资源平级），并授权超级管理员
-- 对应后端接口：
--   /api/v1/admin/fileload/college  -> @RequireAdminModule("fileload_college")
-- 说明：复用 t_file_info 表，以 target_audience='college' 区分人群（与 middle_school/high_school 同一设计，不新建物理表）
--       模块层级结构由 V56 确立：一级「文件管理」(fileload, id=2074728249800000001)，
--       其下二级模块 初中(fileload_middle)/高中(fileload_high)，本迁移新增 大学(fileload_college)

-- 1) 预置面向人群字典数据：大学生（已删除状态，仅作为枚举值参考，对齐 V54 的 初中生/高中生 预置行）
INSERT INTO t_file_info (id, file_name, file_url, file_type, target_audience, version, is_deleted)
VALUES (9000000003, '大学生', '', '', 'college', 0, true)
ON CONFLICT (id) DO NOTHING;

-- 2) 模块：大学资源（挂在「文件管理」一级模块下，parent_id=2074728249800000001，level=2，路径对齐 /fileload/college，sort 紧随高中=5 之后取 6）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249650000001, '大学资源', 'fileload_college', 2074728249800000001, 2, '/fileload/college', 6)
ON CONFLICT (module_code) DO NOTHING;

-- 3) 角色-模块绑定：超级管理员 -> 大学资源
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES (2074728249650000002, 2074728248943710208, 2074728249650000001)
ON CONFLICT (role_id, module_id) DO NOTHING;
