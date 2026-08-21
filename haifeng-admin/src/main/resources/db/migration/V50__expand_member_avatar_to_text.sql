-- 头像列扩容：预设头像在生产构建下会被 Vite 内联为 data URI（>500字符），VARCHAR(500) 溢出导致 DataIntegrityViolationException
ALTER TABLE t_member ALTER COLUMN avatar TYPE TEXT;
