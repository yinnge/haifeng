-- V2__create_system_settings.sql
-- 系统设置表（单例模式）

CREATE TABLE system_settings (
    id                BIGSERIAL PRIMARY KEY,
    site_name         VARCHAR(50),
    site_url          VARCHAR(100),
    site_icp          VARCHAR(100),
    site_description  TEXT,
    university_api_number        INTEGER DEFAULT 1,
    major_api_number        INTEGER DEFAULT 1,
    city_api_number        INTEGER DEFAULT 1,
    provider_name     VARCHAR(50),
    model_name        VARCHAR(100),
    pro_price         INTEGER DEFAULT 199,
    vip_price         INTEGER DEFAULT 599,
    reach_high_count  INTEGER NOT NULL DEFAULT 1,
    reach_count       INTEGER NOT NULL DEFAULT 2,
    match_count       INTEGER NOT NULL DEFAULT 3,
    safe_count        INTEGER NOT NULL DEFAULT 2,
    floor_count       INTEGER NOT NULL DEFAULT 1,
    pro_commission_rate   SMALLINT DEFAULT 10 CHECK (pro_commission_rate >= 0 AND pro_commission_rate <= 100),
    vip_commission_rate   SMALLINT DEFAULT 15 CHECK (vip_commission_rate >= 0 AND vip_commission_rate <= 100),
    seo_title         VARCHAR(200),
    seo_keywords      VARCHAR(100),
    seo_description   TEXT,
    contact_url       JSONB DEFAULT '{}',
    basic_message     JSONB DEFAULT '{}',
    created_at        TIMESTAMPTZ DEFAULT NOW(),
    updated_at        TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE system_settings IS '系统设置表（单例）';
COMMENT ON COLUMN system_settings.pro_commission_rate IS 'Pro会员提成比例（0-100），代表0%到100%';
COMMENT ON COLUMN system_settings.vip_commission_rate IS 'VIP会员提成比例（0-100），代表0%到100%';
COMMENT ON COLUMN system_settings.contact_url IS 'JSON格式：{wechat, weibo, zhihu, douyin, bilibili}';
COMMENT ON COLUMN system_settings.basic_message IS 'JSON格式：{address, phone, email, consultationTime}';

-- 插入默认配置（单例模式，只有一行）
INSERT INTO system_settings (id, site_name, site_description, contact_url, basic_message)
VALUES (
    1,
    '海峰未来规划院',
    '专业的高考志愿填报平台',
    '{"wechat": "", "weibo": "", "zhihu": "", "douyin": "", "bilibili": ""}',
    '{"address": "", "phone": "", "email": "", "consultationTime": ""}'
);
