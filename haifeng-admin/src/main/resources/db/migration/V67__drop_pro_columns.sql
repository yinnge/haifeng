-- V67__drop_pro_columns.sql
-- Pro 会员已并入 VIP 会员：删除 system_settings 中的 Pro 价格与 Pro 佣金比例列

ALTER TABLE system_settings DROP COLUMN IF EXISTS pro_price;
ALTER TABLE system_settings DROP COLUMN IF EXISTS pro_commission_rate;

COMMENT ON COLUMN system_settings.vip_commission_rate IS 'VIP会员提成比例（0-100），代表0%到100%（Pro已并入VIP，统一使用此比例）';
