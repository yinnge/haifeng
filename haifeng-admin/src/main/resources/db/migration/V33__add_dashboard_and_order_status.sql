-- V33__add_dashboard_and_order_status.sql
-- 1. member_orders 新增 status 字段
ALTER TABLE member_orders 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending';

ALTER TABLE member_orders 
ADD CONSTRAINT chk_member_orders_status 
CHECK (status IN ('pending', 'completed', 'cancelled'));

CREATE INDEX idx_member_orders_status ON member_orders(status) WHERE is_deleted = FALSE;

COMMENT ON COLUMN member_orders.status IS '订单状态: pending-待处理, completed-已完成, cancelled-已取消';

-- 2. system_settings 新增 total_amount 字段
ALTER TABLE system_settings 
ADD COLUMN total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00;

COMMENT ON COLUMN system_settings.total_amount IS '累计订单总金额（仅已完成订单）';

-- 3. 新增控制面板模块（1级目录，sort_order=0，排在最前面）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order) 
VALUES (2074728249027596287, '控制面板', 'dashboard', NULL, 1, '/dashboard', 0)
ON CONFLICT (module_code) DO NOTHING;

-- 4. 超级管理员绑定控制面板模块
INSERT INTO sys_role_module (id, role_id, module_id) 
VALUES (2074728249031790591, 2074728248943710208, 2074728249027596287)
ON CONFLICT (role_id, module_id) DO NOTHING;
