-- V36__add_broadcast_id_to_notification.sql
-- 群发公告批次ID：同一批系统公告的所有接收记录共享同一个 broadcast_id，
-- 用于 admin 端「整批撤回 / 整批恢复」群发公告（避免逐条操作导致一对多语义错配）。

ALTER TABLE t_member_notification ADD COLUMN broadcast_id BIGINT;

COMMENT ON COLUMN t_member_notification.broadcast_id
    IS '群发批次ID（system_notice 公告专用，同一批广播相同；个人通知为 NULL）';

CREATE INDEX idx_member_notification_broadcast
    ON t_member_notification (broadcast_id) WHERE broadcast_id IS NOT NULL;

-- 历史群发数据回填：同一 (title, content, created_at) 的 system_notice 归为一批，
-- 使用组内最小 id 作为 broadcast_id，使历史公告也能整批撤回/恢复。
WITH grp AS (
    SELECT id,
           MIN(id) OVER (PARTITION BY title, content, created_at) AS bid
    FROM t_member_notification
    WHERE notification_type = 'system_notice'
      AND broadcast_id IS NULL
)
UPDATE t_member_notification t
SET broadcast_id = g.bid
FROM grp g
WHERE t.id = g.id;
