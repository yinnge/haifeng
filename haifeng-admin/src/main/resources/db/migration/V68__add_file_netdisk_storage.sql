-- 文件存储方式扩展：支持 OSS 上传之外，额外支持「百度网盘」大文件存储
-- 说明：大文件走 OSS 开销高，运营改为上传百度网盘后在此录入分享链接 + 提取码。
--       BAIDU 行不产生 OSS 对象：file_url/file_md5/file_size 等 OSS 语义列为 NULL。
--       封面 cover_url 可空：默认由前端按 storage_type 渲染网盘封面，有真图时再填 URL。

ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS storage_type VARCHAR(20) NOT NULL DEFAULT 'OSS';
ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS netdisk_url   VARCHAR(1024);
ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS access_code   VARCHAR(20);
ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS cover_url     VARCHAR(500);

-- 网盘行没有 OSS 对象 key，file_url 允许为空（存量 OSS 数据不受影响）
ALTER TABLE t_file_info ALTER COLUMN file_url DROP NOT NULL;

COMMENT ON COLUMN t_file_info.storage_type IS '存储方式：OSS-对象存储（上传文件） BAIDU-百度网盘（分享链接+提取码）';
COMMENT ON COLUMN t_file_info.netdisk_url IS '百度网盘分享链接（storage_type=BAIDU 时使用）';
COMMENT ON COLUMN t_file_info.access_code IS '百度网盘提取码（storage_type=BAIDU 时使用，可空）';
COMMENT ON COLUMN t_file_info.cover_url IS '封面图URL（网盘资源默认封面由前端渲染，可空；有真图时填写覆盖）';

-- 存储方式取值约束（存量行默认 OSS，自动满足）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_file_storage_type' AND conrelid = 't_file_info'::regclass
    ) THEN
        ALTER TABLE t_file_info ADD CONSTRAINT chk_file_storage_type
            CHECK (storage_type IN ('OSS', 'BAIDU'));
    END IF;
END $$;

-- 存储方式索引：便于区分 OSS/网盘条目
CREATE INDEX IF NOT EXISTS idx_file_storage_type ON t_file_info(storage_type);
