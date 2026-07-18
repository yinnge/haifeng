-- 会员表和订单表加乐观锁版本号字段
ALTER TABLE t_member ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE member_orders ADD COLUMN version INT NOT NULL DEFAULT 0;
