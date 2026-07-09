-- ============================================================
-- PDF 报告记录表 (t_pdf_report)
-- 描述：存储 AI 分析结果（Map 阶段逐校简评 + Reduce 阶段全局研判）
--       PDF 文件不落盘，用户重新打开时用存储的 AI 结果 + wish 快照表重新渲染
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_pdf_report (
    id              SERIAL          PRIMARY KEY,
    member_id       BIGINT          NOT NULL,
    plan_id         INTEGER         NOT NULL,
    status          SMALLINT        NOT NULL DEFAULT 0,
    map_results     JSONB,
    reduce_result   JSONB,
    plan_snapshot   JSONB,
    fail_reason     VARCHAR(500),
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_pdf_report_status CHECK (status IN (0, 1, 2))
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_pdf_report_member
    ON t_pdf_report (member_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_pdf_report_member_plan
    ON t_pdf_report (member_id, plan_id) WHERE is_deleted = FALSE;

-- 触发器
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_trigger
        WHERE tgname = 'trg_pdf_report_updated_at'
          AND tgrelid = 't_pdf_report'::regclass
    ) THEN
        CREATE TRIGGER trg_pdf_report_updated_at
            BEFORE UPDATE ON t_pdf_report
            FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
    END IF;
END
$$;

-- 注释
COMMENT ON TABLE  t_pdf_report                IS 'PDF报告记录表：存储AI分析结果，PDF按需重新渲染';
COMMENT ON COLUMN t_pdf_report.id             IS '主键ID';
COMMENT ON COLUMN t_pdf_report.member_id      IS '用户ID';
COMMENT ON COLUMN t_pdf_report.plan_id        IS '志愿方案ID（t_wish_plan.id）';
COMMENT ON COLUMN t_pdf_report.status         IS '状态：0=生成中，1=成功，2=失败';
COMMENT ON COLUMN t_pdf_report.map_results    IS 'Map阶段结果JSONB数组：[{universityId,universityName,cityName,majors:[{majorName,safetyLevel,levelShort}],commentary,success}]';
COMMENT ON COLUMN t_pdf_report.reduce_result  IS 'Reduce阶段全局研判JSONB：{globalAnalysis,swot,recommendation}';
COMMENT ON COLUMN t_pdf_report.plan_snapshot   IS '封面页数据快照JSONB：{planYear,planProvince,reformModel,userScore,userRank,planBatch}';
COMMENT ON COLUMN t_pdf_report.fail_reason    IS '失败原因（status=2时填写）';
COMMENT ON COLUMN t_pdf_report.is_deleted     IS '软删除';
COMMENT ON COLUMN t_pdf_report.created_at     IS '创建时间';
COMMENT ON COLUMN t_pdf_report.updated_at     IS '更新时间';

COMMIT;
