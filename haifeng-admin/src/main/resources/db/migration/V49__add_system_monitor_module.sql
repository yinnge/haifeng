-- V49__add_system_monitor_module.sql
-- 新增「系统监控」模块，并授权给超级管理员角色
-- 用途：后端检测 本地/服务器 主机的 CPU 与内存使用率（跨平台，Windows/Linux 通用）

-- 模块：系统监控（挂在「系统管理」顶级模块下）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249600000001, '系统监控', 'system_monitor', 2074728249027596288, 2, '/system/monitor', 4)
ON CONFLICT (module_code) DO NOTHING;

-- 角色-模块绑定：超级管理员 -> 系统监控
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES (2074728249600000002, 2074728248943710208, 2074728249600000001)
ON CONFLICT (role_id, module_id) DO NOTHING;
