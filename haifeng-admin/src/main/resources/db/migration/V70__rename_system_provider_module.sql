-- V70__rename_system_provider_module.sql
-- 将「模型供应商配置」模块更名为「模型配置」
--
-- 背景：前端把「服务商与模型配置」列表从「系统设置」页迁到了独立页面 /system/provider，
--       菜单标题由「AI余额查询」改为「模型配置」。
--       DB 中 sys_module.module_name 仍为旧名，会导致「模块菜单管理」页显示与前端菜单不一致，
--       故此处同步改名。
--
-- 权限说明（重要）：
--   * sys_module 中 system_provider 模块自 V26 起已存在，module_code 不变，
--     因此前端路由 moduleCode、后端 @RequireAdminModule("system_provider") 均无需改动。
--   * 超级管理员的模块授权在 V26 已绑定（sys_role_module），无需新增。
--   * 下方 SQL 仅做「兜底补授权」：若某些非超管角色此前能访问系统设置页
--     （即拥有 system_setting），但没有 system_provider，迁移后访问模型配置页会被拦截，
--     故把这些角色一并补上 system_provider 授权，避免升级后出现 403。
--   * sys_role_module 无业务字段，id 由雪花号段生成；用 NOT EXISTS 保证不重复插入。

-- ========== 1. 模块更名 ==========
UPDATE sys_module
SET module_name = '模型配置',
    updated_at = NOW()
WHERE module_code = 'system_provider'
  AND module_name IS DISTINCT FROM '模型配置';

-- ========== 2. 兜底：凡拥有 system_setting 的角色，补上 system_provider 授权 ==========
INSERT INTO sys_role_module (id, role_id, module_id, created_at, updated_at)
SELECT 2074728249700000000 + ROW_NUMBER() OVER (ORDER BY rm.role_id),
       rm.role_id,
       m.id,
       NOW(),
       NOW()
FROM sys_role_module rm
JOIN sys_module m ON m.module_code = 'system_provider'
WHERE EXISTS (
        SELECT 1
        FROM sys_role_module rm2
        JOIN sys_module m2 ON m2.id = rm2.module_id
        WHERE rm2.role_id = rm.role_id
          AND m2.module_code = 'system_setting'
      )
  AND NOT EXISTS (
        SELECT 1
        FROM sys_role_module rm3
        WHERE rm3.role_id = rm.role_id
          AND rm3.module_id = m.id
      );

COMMENT ON TABLE sys_module IS '模块表';