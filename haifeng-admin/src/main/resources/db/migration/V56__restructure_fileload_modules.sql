-- V56__restructure_fileload_modules.sql
-- 「文件管理」提升为一级模块，初中/高中资源改挂其下（原挂「首页管理」下，见 V55）
-- 后端接口不变：/api/v1/admin/fileload/middle|high
--   @RequireAdminModule("fileload_middle" / "fileload_high") 绑定 module_code，与模块层级无关，无需改动
-- 说明：V26/V55 为已执行迁移不可修改，此处用新迁移调整数据结构

-- 1) 首页管理(sort_order=4)之后的一级模块 sort_order 整体后移一位，为「文件管理」腾出 5
UPDATE sys_module
SET sort_order = sort_order + 1
WHERE parent_id IS NULL AND level = 1 AND sort_order >= 5;

-- 2) 新增一级模块「文件管理」（挂在顶级，level=1，sort_order=5 紧随首页管理）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249800000001, '文件管理', 'fileload', NULL, 1, '/fileload', 5)
ON CONFLICT (module_code) DO NOTHING;

-- 3) 初中资源改挂到「文件管理」下（level 保持 2，路径同步更新）
UPDATE sys_module
SET parent_id = 2074728249800000001, path = '/fileload/middle-school'
WHERE module_code = 'fileload_middle';

-- 4) 高中资源改挂到「文件管理」下（level 保持 2，路径同步更新）
UPDATE sys_module
SET parent_id = 2074728249800000001, path = '/fileload/high-school'
WHERE module_code = 'fileload_high';

-- 5) 角色-模块绑定：超级管理员 -> 文件管理
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES (2074728249800000002, 2074728248943710208, 2074728249800000001)
ON CONFLICT (role_id, module_id) DO NOTHING;
