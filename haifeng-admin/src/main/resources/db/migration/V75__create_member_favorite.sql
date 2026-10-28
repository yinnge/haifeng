-- ============================================
-- V75__create_member_favorite.sql
-- 会员收藏模块：多态收藏表
-- 目标类型：university/major/city/competition/certificate/enterprise/industry
-- ============================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_favorite (
    id          BIGINT       PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    target_type VARCHAR(32)  NOT NULL,
    target_id   BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 同一会员对同一目标只能收藏一次
CREATE UNIQUE INDEX IF NOT EXISTS uk_member_favorite_target
    ON t_member_favorite (member_id, target_type, target_id);

-- 个人中心：按收藏时间倒序查询全部收藏
CREATE INDEX IF NOT EXISTS idx_member_favorite_member_created
    ON t_member_favorite (member_id, created_at DESC);

-- 各模块收藏页：按类型 + 收藏时间倒序
CREATE INDEX IF NOT EXISTS idx_member_favorite_member_type_created
    ON t_member_favorite (member_id, target_type, created_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger
        WHERE tgname = 'trg_member_favorite_updated_at'
          AND tgrelid = 't_member_favorite'::regclass
    ) THEN
        CREATE TRIGGER trg_member_favorite_updated_at
            BEFORE UPDATE ON t_member_favorite
            FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
    END IF;
END
$$;

COMMENT ON TABLE t_member_favorite IS '会员收藏表（多态：院校/专业/城市/竞赛/证书/企业/行业）';
COMMENT ON COLUMN t_member_favorite.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_member_favorite.member_id IS '会员ID';
COMMENT ON COLUMN t_member_favorite.target_type IS '收藏目标类型：university/major/city/competition/certificate/enterprise/industry';
COMMENT ON COLUMN t_member_favorite.target_id IS '收藏目标ID（对应各业务表主键）';
COMMENT ON COLUMN t_member_favorite.created_at IS '收藏时间';
COMMENT ON COLUMN t_member_favorite.updated_at IS '更新时间';

COMMIT;
