-- V40: 会员升级暂存机制（Pro→VIP时保留Pro剩余时间）
ALTER TABLE t_member ADD COLUMN IF NOT EXISTS suspended_member_type VARCHAR(20) DEFAULT NULL;
ALTER TABLE t_member ADD COLUMN IF NOT EXISTS suspended_expire_at TIMESTAMPTZ DEFAULT NULL;
ALTER TABLE t_member ADD COLUMN IF NOT EXISTS suspended_remaining_months INTEGER DEFAULT NULL;

COMMENT ON COLUMN t_member.suspended_member_type IS '被挂起的会员类型（如Pro升级VIP时暂存pro）';
COMMENT ON COLUMN t_member.suspended_expire_at IS '被挂起会员的到期时间';
COMMENT ON COLUMN t_member.suspended_remaining_months IS '被挂起会员的剩余月数';
