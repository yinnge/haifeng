-- 佣金记录增加 status 字段，区分业务状态和软删除
-- active = 正常佣金, revoked = 订单撤销回退

ALTER TABLE t_referral_commission ADD COLUMN status VARCHAR(20) DEFAULT 'active';

ALTER TABLE t_referral_commission ADD CONSTRAINT chk_commission_status
    CHECK (status IN ('active', 'revoked'));

-- 回填：已软删除的记录标记为 revoked
UPDATE t_referral_commission SET status = 'revoked' WHERE is_deleted = true;
