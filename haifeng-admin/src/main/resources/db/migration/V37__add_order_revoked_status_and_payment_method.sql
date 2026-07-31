-- V37__add_order_revoked_status_and_payment_method.sql

-- 1. 更新现有订单状态为 completed（旧升级逻辑直接完成，未设置 status，默认为了 pending）
UPDATE member_orders SET status = 'completed' WHERE status = 'pending';

-- 2. 替换 CHECK 约束，增加 revoked 状态
ALTER TABLE member_orders DROP CONSTRAINT IF EXISTS chk_member_orders_status;
ALTER TABLE member_orders
ADD CONSTRAINT chk_member_orders_status
CHECK (status IN ('pending', 'completed', 'cancelled', 'revoked'));

-- 3. 新增 payment_method 列
ALTER TABLE member_orders
ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'offline';

COMMENT ON COLUMN member_orders.payment_method IS '支付方式: offline-线下转账, wechat-微信支付';

-- 4. 更新 status 列注释
COMMENT ON COLUMN member_orders.status IS '订单状态: pending-待支付, completed-已完成, cancelled-已取消, revoked-已撤销';
