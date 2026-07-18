-- 乐观锁版本号字段
ALTER TABLE t_campus_gallery ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE t_department ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE department_reports ADD COLUMN version INT NOT NULL DEFAULT 0;
