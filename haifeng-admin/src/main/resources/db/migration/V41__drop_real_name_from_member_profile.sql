-- 删除 t_member_profile.real_name 列，username 统一由 t_member 管理
ALTER TABLE t_member_profile DROP COLUMN IF EXISTS real_name;
