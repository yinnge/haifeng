-- 文件下载水印：下载统一输出「带水印 PDF」，生成结果按文件缓存复用
-- 说明：水印文本为固定内容（海枫未来规划院），与用户无关，因此同一个源文件
--       只需生成一次带水印 PDF，后续下载直接复用 OSS 上的缓存对象，无需每次重新计算。

ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS watermarked_file_url VARCHAR(1024);
ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS watermark_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
ALTER TABLE t_file_info ADD COLUMN IF NOT EXISTS watermark_fail_reason TEXT;

COMMENT ON COLUMN t_file_info.watermarked_file_url IS '带水印PDF的OSS对象key（生成成功后缓存复用）';
COMMENT ON COLUMN t_file_info.watermark_status IS '水印PDF生成状态：NONE-未生成 PENDING-生成中 READY-已就绪 FAILED-生成失败（降级返回原文件）';
COMMENT ON COLUMN t_file_info.watermark_fail_reason IS '水印PDF生成失败原因（用于排查，不对外暴露）';

-- 状态取值约束
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_file_watermark_status' AND conrelid = 't_file_info'::regclass
    ) THEN
        ALTER TABLE t_file_info ADD CONSTRAINT chk_file_watermark_status
            CHECK (watermark_status IN ('NONE', 'PENDING', 'READY', 'FAILED'));
    END IF;
END $$;

-- 状态索引：便于后台排查失败/未生成的文件
CREATE INDEX IF NOT EXISTS idx_file_watermark_status ON t_file_info(watermark_status);
