-- V64__drop_resource_module.sql
-- 删除「学习资源」功能：
--   1) 清理角色-模块关联（sys_role_module 中 module_id 为逻辑外键，需手动删除）
--   2) 删除菜单模块（按 V26 实际插入的 id 精确删除：
--        - 2074728249027596345 = 父模块 'resource'（/resource）
--        - 2074728249027596346 = 子模块 'resource_info'（/resource/info））
--   3) 删除 t_resource 表及其索引、触发器（表实际建于 V8，此处 DROP 即可，无需改动 V8）
-- 沿用 V60__drop_major_constraint.sql 的清理模式

-- 1. 先清理角色-模块关联（子模块 resource_info 与父模块 resource）
DELETE FROM sys_role_module
WHERE module_id IN (2074728249027596345, 2074728249027596346);

-- 2. 删除菜单模块（按 V26 实际插入 id 精确删除，parent_id 为 ON DELETE CASCADE）
DELETE FROM sys_module WHERE id IN (2074728249027596345, 2074728249027596346);

-- 3. 删除资源表（CASCADE 会一并删除其索引与触发器 trg_resource_updated_at）
DROP TABLE IF EXISTS t_resource CASCADE;
