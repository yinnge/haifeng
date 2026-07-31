-- V35__fix_notification_check_constraint.sql
-- 修复 t_member_notification 的 CHECK 约束：V3 建表时遗漏了 commission_rejected

ALTER TABLE t_member_notification DROP CONSTRAINT IF EXISTS chk_notification_type;
ALTER TABLE t_member_notification ADD CONSTRAINT chk_notification_type CHECK (notification_type IN (
    'member_expire_soon',
    'member_expired',
    'commission_earned',
    'commission_paid',
    'commission_rejected',
    'system_notice',
    'member_renewed',
    'member_activation_success'
));
