-- =============================================================
-- V81：咨询留言表 + 通知类型扩展（2026-11-16）
-- 由 Flyway 管理，已执行后禁止修改本文件；后续变更请新增 V82+
--
-- 1. 新建 t_consultation：C 端用户向规划师/培训机构提交预约留言
-- 2. t_member_notification 新增 consultation_received 类型
-- 3. 新增 Admin 只读模块「咨询留言」并授权超级管理员
-- =============================================================

CREATE TABLE t_consultation (
    id                BIGINT       PRIMARY KEY,            -- 雪花ID
    biz_type          VARCHAR(32)  NOT NULL,               -- planner / institution（后端枚举校验，不加DB CHECK）
    biz_id            BIGINT       NOT NULL,               -- t_planners.id / t_institutions.id
    biz_name          VARCHAR(128) NOT NULL,               -- 提交时业务名称快照
    teacher_member_id BIGINT       NOT NULL,               -- 接收方教师账号（t_member.id）
    sender_member_id  BIGINT,                              -- 提交人（可空，未登录为 NULL）
    message           TEXT         NOT NULL,               -- 留言
    wechat            VARCHAR(64),                         -- 微信号（可选）
    phone             VARCHAR(20)  NOT NULL,               -- 电话（必填）
    client_ip         VARCHAR(64),                         -- 提交IP（防刷排查用）
    status            SMALLINT     NOT NULL DEFAULT 0,     -- 0:待处理 1:已处理
    version           INT          NOT NULL DEFAULT 0,     -- 保留列，不启用 @Version
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_consultation_status CHECK (status IN (0, 1))
);

COMMENT ON TABLE  t_consultation                  IS '咨询留言表：C端用户向规划师/培训机构提交的预约留言';
COMMENT ON COLUMN t_consultation.biz_type         IS '业务类型：planner / institution，后端枚举校验';
COMMENT ON COLUMN t_consultation.biz_id           IS '业务ID：t_planners.id / t_institutions.id，不建物理外键';
COMMENT ON COLUMN t_consultation.biz_name         IS '提交时的业务名称快照，避免业务表改名后列表显示旧名';
COMMENT ON COLUMN t_consultation.teacher_member_id IS '接收方教师账号（逻辑关联 t_member.id，不建物理外键）';
COMMENT ON COLUMN t_consultation.sender_member_id IS '提交人会员ID，未登录提交时为空';
COMMENT ON COLUMN t_consultation.phone            IS '联系电话（必填）';
COMMENT ON COLUMN t_consultation.wechat           IS '微信号（可选）';
COMMENT ON COLUMN t_consultation.client_ip        IS '提交IP，防刷排查用';
COMMENT ON COLUMN t_consultation.status           IS '0:待处理 1:已处理（教师端标记已联系）';
COMMENT ON COLUMN t_consultation.version          IS '保留列，未启用乐观锁';

-- 教师后台：查「我收到的留言」（主查询）
CREATE INDEX idx_consultation_member ON t_consultation (teacher_member_id, status, created_at DESC);

-- 按业务维度查：某个规划师/培训机构下的所有留言
CREATE INDEX idx_consultation_biz ON t_consultation (biz_type, biz_id, created_at DESC);

-- 通知类型：新增「收到咨询留言」
ALTER TABLE t_member_notification DROP CONSTRAINT IF EXISTS chk_notification_type;
ALTER TABLE t_member_notification ADD CONSTRAINT chk_notification_type CHECK (notification_type IN (
    'member_expire_soon',
    'member_expired',
    'commission_earned',
    'commission_paid',
    'commission_rejected',
    'system_notice',
    'member_renewed',
    'member_activation_success',
    'member_revoked',
    'commission_reversed',
    'teacher_granted',
    'teacher_revoked',
    'consultation_received'
));

-- 模块：咨询留言（挂在「首页管理」下）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249600000101, '咨询留言', 'home_consultation', 2074728249027596302, 2, '/home/consultation', 4)
ON CONFLICT (module_code) DO NOTHING;

-- 角色-模块绑定：超级管理员 -> 咨询留言
INSERT INTO sys_role_module (id, role_id, module_id)
SELECT 2074728249600000102, 2074728248943710208, id
FROM sys_module
WHERE module_code = 'home_consultation'
ON CONFLICT (role_id, module_id) DO NOTHING;
