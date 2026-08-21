-- 失败原因列扩容：failReason 拼接异常链 message（嵌套异常很容易超过500字符），
-- VARCHAR(500) 溢出导致 PSQLException: value too long for type character varying(500)
ALTER TABLE t_pdf_report ALTER COLUMN fail_reason TYPE TEXT;
