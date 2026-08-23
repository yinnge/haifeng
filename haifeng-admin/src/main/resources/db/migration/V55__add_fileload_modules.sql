-- V55__add_fileload_modules.sql
-- 首页管理下新增「初中资源」「高中资源」模块，并授权给超级管理员角色
-- 对应后端接口：
--   初中：/api/v1/admin/fileload/middle  -> @RequireAdminModule("fileload_middle")
--   高中：/api/v1/admin/fileload/high    -> @RequireAdminModule("fileload_high")

-- 模块：初中资源（挂在「首页管理」顶级模块下，parent_id=2074728249027596302）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249700000001, '初中资源', 'fileload_middle', 2074728249027596302, 2, '/home/middle-school', 4)
ON CONFLICT (module_code) DO NOTHING;

-- 模块：高中资源（挂在「首页管理」顶级模块下，parent_id=2074728249027596302）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249700000002, '高中资源', 'fileload_high', 2074728249027596302, 2, '/home/high-school', 5)
ON CONFLICT (module_code) DO NOTHING;

-- 角色-模块绑定：超级管理员 -> 初中资源
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES (2074728249700000003, 2074728248943710208, 2074728249700000001)
ON CONFLICT (role_id, module_id) DO NOTHING;

-- 角色-模块绑定：超级管理员 -> 高中资源
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES (2074728249700000004, 2074728248943710208, 2074728249700000002)
ON CONFLICT (role_id, module_id) DO NOTHING;
