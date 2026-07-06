-- ============================================================
-- 模型服务商配置表 (t_model_provider)
-- 描述：存储 AI 模型服务商、模型名称和 API Key 配置
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_model_provider (
    id              BIGSERIAL       PRIMARY KEY,
    api_key         TEXT            NOT NULL,
    model_name      VARCHAR(100)    NOT NULL,
    provider_name   VARCHAR(50)     NOT NULL,
    type            VARCHAR(50)     NOT NULL DEFAULT 'model',
    description     VARCHAR(255),
    status          SMALLINT        NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_model_provider_status CHECK (status IN (0, 1))
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_model_provider_status ON t_model_provider (status);
CREATE INDEX IF NOT EXISTS idx_model_provider_provider_model ON t_model_provider (provider_name, model_name);

-- 触发器
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_trigger
        WHERE tgname = 'trg_model_provider_updated_at'
          AND tgrelid = 't_model_provider'::regclass
    ) THEN
        CREATE TRIGGER trg_model_provider_updated_at
            BEFORE UPDATE ON t_model_provider
            FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
    END IF;
END
$$;

-- 注释
COMMENT ON TABLE  t_model_provider               IS '服务商配置表：存储 AI 模型/短信等服务商、模型名称和 API Key 配置';
COMMENT ON COLUMN t_model_provider.id            IS '主键ID';
COMMENT ON COLUMN t_model_provider.api_key       IS 'API Key 密钥';
COMMENT ON COLUMN t_model_provider.model_name    IS '模型名称/服务标识';
COMMENT ON COLUMN t_model_provider.provider_name IS '服务商名称';
COMMENT ON COLUMN t_model_provider.type          IS '类型：model=AI模型，message=短信';
COMMENT ON COLUMN t_model_provider.description   IS '描述说明';
COMMENT ON COLUMN t_model_provider.status        IS '状态：0=禁用，1=启用';
COMMENT ON COLUMN t_model_provider.created_at    IS '创建时间';
COMMENT ON COLUMN t_model_provider.updated_at    IS '更新时间';

COMMIT;
