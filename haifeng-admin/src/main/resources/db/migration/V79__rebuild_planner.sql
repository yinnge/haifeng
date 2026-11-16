-- =============================================================
-- V79：规划师表 t_planners 重建（2026-11-16）
-- 由 Flyway 管理，已执行后禁止修改本文件；后续变更请新增 V80+
--
-- 说明：
--   1. 旧数据全部不要，先 DROP 再 CREATE（本脚本不可逆）
--   2. status 字段删除：上下架语义由 is_deleted 软删除承担
--   3. 禁用 @TableLogic：应用层所有查询/删除手动写 is_deleted 条件
--   4. version 列保留但不启用乐观锁注解
--   5. user_id 逻辑关联 t_member.id，不建物理外键
--   6. phone 不加唯一约束（允许重复）
--   7. 头像/二维码存本地静态资源相对路径
-- =============================================================

DROP TABLE IF EXISTS t_planners;

CREATE TABLE t_planners (
    id                BIGINT          PRIMARY KEY,            -- 雪花ID（应用层生成）
    user_id           BIGINT          NOT NULL,               -- 逻辑关联 t_member.id（规划师登录账号）
    ---------- 基础信息 ----------
    name              VARCHAR(64)     NOT NULL,               -- 规划师姓名
    service_target    VARCHAR(32)     NOT NULL,               -- 服务对象：高考 / 考研（后端枚举校验，DB不加CHECK）
    position          VARCHAR(64),                            -- 职位：首席
    province          VARCHAR(30),                            -- 所属省份（ProvinceEnum 裸值，如 北京 / 广西）
    ---------- 列表页展示 ----------
    list_desc         VARCHAR(512),                           -- 列表描述
    list_tags         TEXT[]          NOT NULL DEFAULT '{}',  -- 列表tag：{陕西新高考,位次精算}
    avatar_url        VARCHAR(512),                           -- svg头像相对路径（可空）
    ---------- 详情页展示 ----------
    work_years        VARCHAR(32),                            -- 从业年限：11年
    served_count      VARCHAR(32),                            -- 服务考生：3200+
    draft_sla         VARCHAR(32),                            -- 初稿时效：48h
    cert_desc         VARCHAR(256),                           -- 认证描述：持证生涯规划师
    description       TEXT,                                   -- 详情大段介绍
    detail_tags       TEXT[]          NOT NULL DEFAULT '{}',  -- 详情tag：{陕西新高考,专业组梯度}
    services          JSONB           NOT NULL DEFAULT '[]',  -- 服务内容：[{"title":"首次诊断","content":"..."}]
    ---------- 预约信息（均可空，有就填） ----------
    email             VARCHAR(128),                           -- 邮箱（可空）
    phone             VARCHAR(20),                            -- 手机号（可空，不唯一）
    address           VARCHAR(256),                           -- 地址（可空）
    wechat_qrcode_url VARCHAR(512),                           -- 微信二维码相对路径（可空）
    ---------- 系统字段 ----------
    is_deleted        BOOLEAN         NOT NULL DEFAULT FALSE, -- 软删除（手动维护，无 @TableLogic）
    sort_order        INT             NOT NULL DEFAULT 0,     -- 展示排序，越小越靠前
    version           INT             NOT NULL DEFAULT 0,     -- 乐观锁版本号列（未启用拦截器）
    created_by        BIGINT,                                 -- 创建人（后台用户ID，不加外键）
    updated_by        BIGINT,                                 -- 更新人（后台用户ID，不加外键）
    created_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  t_planners                IS '规划师';
COMMENT ON COLUMN t_planners.user_id        IS '逻辑关联 t_member.id，不建物理外键';
COMMENT ON COLUMN t_planners.service_target IS '服务对象：高考 / 考研，后端枚举校验';
COMMENT ON COLUMN t_planners.avatar_url     IS 'svg头像本地静态资源相对路径，可空';
COMMENT ON COLUMN t_planners.wechat_qrcode_url IS '微信二维码本地静态资源相对路径，可空';
COMMENT ON COLUMN t_planners.services       IS '服务内容数组，元素结构 {title,content}';
COMMENT ON COLUMN t_planners.is_deleted     IS '软删除标记，应用层手动维护，禁用 @TableLogic';

-- services 必须是数组（内部 title/content 结构由后端 DTO 校验）
ALTER TABLE t_planners
ADD CONSTRAINT chk_planner_services_is_array
CHECK (jsonb_typeof(services) = 'array');

-- 索引：app 端列表按 service_target + province 过滤，按 is_deleted + sort_order 排序
CREATE INDEX idx_planner_app_list ON t_planners (service_target, province, is_deleted, sort_order);
-- 索引：admin 端姓名模糊/分页兜底（按 is_deleted 过滤）
CREATE INDEX idx_planner_admin_list ON t_planners (is_deleted, sort_order, created_at);
