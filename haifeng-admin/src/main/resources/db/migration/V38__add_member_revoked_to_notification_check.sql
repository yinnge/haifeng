-- V38__add_member_revoked_to_notification_check.sql
-- 修复 chk_notification_type 约束：遗漏了 member_revoked 类型

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
    'member_revoked'
));
