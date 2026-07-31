-- V34__add_updated_at_to_t_member_notification.sql
-- 修复 t_member_notification 表建表时遗漏 updated_at 列的问题
-- 实体类 MemberNotification.java 有 updated_at 字段，MyBatis-Plus INSERT 时会包含该列

ALTER TABLE t_member_notification ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

COMMENT ON COLUMN t_member_notification.updated_at IS '更新时间';
