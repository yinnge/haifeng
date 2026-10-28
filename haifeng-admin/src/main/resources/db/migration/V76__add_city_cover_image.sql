-- 城市背景图（C 端城市列表卡片背景 / 排行榜封面）
ALTER TABLE t_city ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500);

COMMENT ON COLUMN t_city.cover_image_url IS '城市背景图URL（Excel导入列「城市背景图」）';
