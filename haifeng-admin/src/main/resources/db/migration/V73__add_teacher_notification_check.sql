-- V73__add_teacher_notification_check.sql
-- 修复 chk_notification_type 约束：补充老师身份授予/撤销通知类型。
-- 背景：MemberServiceImpl.setTeacher 会向 t_member_notification 写入 teacher_granted / teacher_revoked，
--       此前 V39 的 CHECK 约束未包含这两个值，导致 insert 触发
--       "violates check constraint chk_notification_type" → 事务回滚 → 老师身份未生效且接口 500。

ALTER TABLE t_member_notification DROP CONSTRAINT IF EXISTS chk_notification_type;
ALTER TABLE t_member_notification ADD CONSTRAINT chk_notification_type CHECK (notification_type IN (
    'member_expire_soon',
    'member_expired',
    'commission_earned',
    'commission_paid',
    'commission_rejected',
    'system_notice',
    'member_renewed',
    'member_activation_success',
    'member_revoked',
    'commission_reversed',
    'teacher_granted',
    'teacher_revoked'
));
