-- 文件信息表：新增文档简介与标签字段
ALTER TABLE t_file_info ADD COLUMN description TEXT;
ALTER TABLE t_file_info ADD COLUMN tag VARCHAR(100);

COMMENT ON COLUMN t_file_info.description IS '文档简介（备考指南、政策说明等）';
COMMENT ON COLUMN t_file_info.tag IS '标签（备考指南/就业辅导等，用于精准查询）';

-- 标签精准查询索引
CREATE INDEX idx_file_tag ON t_file_info(tag);
