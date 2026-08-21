-- 文件信息表
CREATE TABLE t_file_info (
  id BIGINT PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(1024) NOT NULL,
  file_preview_url VARCHAR(1024),
  file_type VARCHAR(50),
  file_size BIGINT DEFAULT 0,
  file_md5 VARCHAR(64),
  bucket_name VARCHAR(100),
  target_audience VARCHAR(20),
  applicable_stage VARCHAR(20),
  subject VARCHAR(50),
  version INTEGER NOT NULL DEFAULT 0,
  create_by VARCHAR(64),
  create_time TIMESTAMPTZ DEFAULT NOW(),
  update_by VARCHAR(64),
  update_time TIMESTAMPTZ DEFAULT NOW(),
  is_deleted BOOLEAN DEFAULT FALSE
);

COMMENT ON TABLE t_file_info IS '文件信息表';
COMMENT ON COLUMN t_file_info.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_file_info.file_name IS '文件名称（包含后缀）';
COMMENT ON COLUMN t_file_info.file_url IS '文件访问/下载URL（OSS预签名URL）';
COMMENT ON COLUMN t_file_info.file_preview_url IS 'KKFileView预览地址';
COMMENT ON COLUMN t_file_info.file_type IS '文件类型（doc, pdf, xlsx）';
COMMENT ON COLUMN t_file_info.file_size IS '文件大小（字节）';
COMMENT ON COLUMN t_file_info.file_md5 IS '文件MD5值，用于秒传或校验';
COMMENT ON COLUMN t_file_info.bucket_name IS '存储桶名称';
COMMENT ON COLUMN t_file_info.target_audience IS '面向人群（middle_school:初中生, high_school:高中生）';
COMMENT ON COLUMN t_file_info.applicable_stage IS '适合人群（初一/初二/高一/高二等）';
COMMENT ON COLUMN t_file_info.subject IS '学科（数学/语文/英语等）';
COMMENT ON COLUMN t_file_info.version IS '乐观锁版本号';
COMMENT ON COLUMN t_file_info.is_deleted IS '逻辑删除标记';

-- MD5唯一索引，只索引未删除的记录
CREATE UNIQUE INDEX uk_file_md5 ON t_file_info(file_md5) WHERE is_deleted = FALSE;

-- 文件类型索引
CREATE INDEX idx_file_type ON t_file_info(file_type);

-- 面向人群索引
CREATE INDEX idx_target_audience ON t_file_info(target_audience);

-- 学科索引
CREATE INDEX idx_subject ON t_file_info(subject);

-- 创建时间索引
CREATE INDEX idx_file_create_time ON t_file_info(create_time);

-- 预置面向人群字典数据（已删除状态，仅作为枚举值参考）
INSERT INTO t_file_info (id, file_name, file_url, file_type, target_audience, version, is_deleted) VALUES
(9000000001, '初中生', '', '', 'middle_school', 0, true),
(9000000002, '高中生', '', '', 'high_school', 0, true);
