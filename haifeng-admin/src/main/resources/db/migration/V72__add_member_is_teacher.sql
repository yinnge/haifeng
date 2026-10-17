-- V72__add_member_is_teacher.sql
-- 新增「老师身份」标记：叠加在会员体系之上，独立于 member_type（不并入 normal/pro/vip）。
-- 设计要点：
--   1. 与 VIP 计时解耦：老师身份天然可逆（管理员随时撤销），不影响会员到期时间；
--   2. 历史数据全部置为 false（列默认值兜底，此处显式保证语义明确）；
--   3. 权限实时生效：授予/撤销后由 AuthAspect.checkTeacher 回退 DB 查询，无需用户重登（与 VIP 一致）。

ALTER TABLE t_member ADD COLUMN IF NOT EXISTS is_teacher BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN t_member.is_teacher IS '是否老师身份（叠加在会员体系之上，由管理员授予/撤销，不并入 member_type）';

-- 历史数据全部置为 false（ADD COLUMN ... DEFAULT FALSE 已对存量行兜底，此处显式保证）
UPDATE t_member SET is_teacher = FALSE WHERE is_teacher IS NULL;

CREATE INDEX IF NOT EXISTS idx_member_is_teacher ON t_member(is_teacher) WHERE is_deleted = FALSE;
