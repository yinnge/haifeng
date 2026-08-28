-- ============================================================
-- V60: 下线「专业约束关联」功能
-- 该功能的专业↔安全系数绑定已迁移至专业组明细（admission major detail），
-- 独立的 t_major_constraint 表不再需要，予以下线。
-- ============================================================

-- 1. 删除专业约束关联表（CASCADE 会一并删除其触发器/索引/唯一约束；
--    共享的 fn_update_timestamp() 函数由 V11 定义，不在此删除）
DROP TABLE IF EXISTS t_major_constraint CASCADE;

-- 2. 清理后台菜单中「约束专业关联管理」模块（避免死链）
-- 2.1 先删角色-模块绑定（V26 中插入了 super_admin → 该模块 的绑定，引用了待删模块的 id，否则会留下悬空外键）
DELETE FROM sys_role_module WHERE module_id = (SELECT id FROM sys_module WHERE module_code = 'algo_constraint_mjr');
-- 2.2 再删模块本身
DELETE FROM sys_module WHERE module_code = 'algo_constraint_mjr';
