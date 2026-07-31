-- V39__allow_negative_balance_and_add_commission_reversed_notification.sql
-- 1. 允许 commission_balance 为负（撤销订单时佣金已提现，余额可为负，代表推荐人欠款）
ALTER TABLE t_member DROP CONSTRAINT IF EXISTS chk_commission_balance;

-- 2. chk_notification_type 加入 commission_reversed
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
    'commission_reversed'
));
