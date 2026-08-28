-- 扩大招生电话字段长度：50 → 200
ALTER TABLE t_universities_detail
    ALTER COLUMN admission_phone TYPE VARCHAR(200);
