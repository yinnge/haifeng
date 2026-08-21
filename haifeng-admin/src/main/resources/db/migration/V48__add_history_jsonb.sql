-- ============================================
-- V48__add_history_jsonb.sql
-- 专业组历史分数 jsonb 改造
-- ============================================

BEGIN;

-- ===========================================================
-- 1. t_admission_major_score：加 history jsonb，删旧分数列，改唯一键
-- ===========================================================

-- 1.1 加 history jsonb 列
ALTER TABLE t_admission_major_score
  ADD COLUMN history jsonb NOT NULL DEFAULT '[]'::jsonb;

COMMENT ON COLUMN t_admission_major_score.history IS
  '历史分数数组: [{year, admissionCount, minScore, minRank, avgScore, avgRank, maxScore, maxRank}, ...]';

-- 1.2 删除旧分数列（history jsonb 替代）
ALTER TABLE t_admission_major_score
  DROP COLUMN IF EXISTS min_score,
  DROP COLUMN IF EXISTS min_rank,
  DROP COLUMN IF EXISTS max_score,
  DROP COLUMN IF EXISTS max_rank,
  DROP COLUMN IF EXISTS avg_score,
  DROP COLUMN IF EXISTS avg_rank,
  DROP COLUMN IF EXISTS admission_count;

-- 1.3 唯一键：去掉 year（同专业在同组下只有一行，history 跨年积累）
ALTER TABLE t_admission_major_score
  DROP CONSTRAINT IF EXISTS uk_group_major,
  ADD CONSTRAINT uk_group_major UNIQUE (group_id, major_code);

-- 1.4 删除旧索引（列已删）
DROP INDEX IF EXISTS idx_ams_min_score;

-- 1.5 新索引：支持按 history jsonb 内的 year 查询
CREATE INDEX idx_ams_history_gin ON t_admission_major_score USING GIN (history);


-- ===========================================================
-- 2. t_admission_group：加 history jsonb 列
-- ===========================================================

ALTER TABLE t_admission_group
  ADD COLUMN history jsonb NOT NULL DEFAULT '[]'::jsonb;

COMMENT ON COLUMN t_admission_group.history IS
  '历史分数数组（由专业明细聚合计算）: [{year, admissionCount, minScore, minRank, avgScore, avgRank, maxScore, maxRank}, ...]';


-- ===========================================================
-- 3. 重写 fn_recalc_group
-- ===========================================================

CREATE OR REPLACE FUNCTION fn_recalc_group(p_group_id INTEGER)
RETURNS VOID AS $$
DECLARE
    v_major_count       INTEGER;
    v_category_count    INTEGER;
    v_admission_count   INTEGER;
    v_min_score         INTEGER;
    v_min_rank          INTEGER;
    v_max_score         INTEGER;
    v_max_rank          INTEGER;
    v_avg_score         NUMERIC(6,2);
    v_avg_rank          INTEGER;
    v_group_year        INTEGER;
    v_history           jsonb;
BEGIN
    -- 获取当前组的年份
    SELECT year INTO v_group_year
    FROM t_admission_group WHERE id = p_group_id;

    -- 1. 聚合当前年的统计字段（从 major 当前年的 history 条目中提取）
    SELECT
        COUNT(*),
        COUNT(DISTINCT LEFT(major_code, 4)),
        SUM((h->>'admissionCount')::int),
        MIN((h->>'minScore')::int),
        MAX((h->>'minRank')::int),
        MAX((h->>'maxScore')::int),
        MIN((h->>'maxRank')::int),
        ROUND(AVG((h->>'avgScore')::numeric), 2),
        ROUND(AVG((h->>'avgRank')::int))
    INTO
        v_major_count, v_category_count, v_admission_count,
        v_min_score, v_min_rank, v_max_score, v_max_rank,
        v_avg_score, v_avg_rank
    FROM t_admission_major_score ams,
         jsonb_array_elements(ams.history) h
    WHERE ams.group_id = p_group_id
      AND ams.is_deleted = FALSE
      AND (h->>'year')::int = v_group_year;

    -- 2. 从 major history 聚合 group 的全量历史（子查询先按 year 分组，外层 jsonb_agg 包装）
    SELECT jsonb_agg(sub ORDER BY (sub->>'year')::int DESC)
    INTO v_history
    FROM (
        SELECT jsonb_build_object(
            'year',           h->>'year',
            'admissionCount', SUM((h->>'admissionCount')::int),
            'minScore',       MIN((h->>'minScore')::int),
            'minRank',        MAX((h->>'minRank')::int),
            'maxScore',       MAX((h->>'maxScore')::int),
            'maxRank',        MIN((h->>'maxRank')::int),
            'avgScore',       ROUND(AVG((h->>'avgScore')::numeric), 2),
            'avgRank',        ROUND(AVG((h->>'avgRank')::int))
        ) AS sub
        FROM t_admission_major_score ams,
             jsonb_array_elements(ams.history) h
        WHERE ams.group_id = p_group_id
          AND ams.is_deleted = FALSE
        GROUP BY h->>'year'
    ) sub;

    -- 3. 写入
    UPDATE t_admission_group
    SET
        major_count     = COALESCE(v_major_count, 0),
        category_count  = COALESCE(v_category_count, 0),
        admission_count = v_admission_count,
        min_score       = v_min_score,
        min_rank        = v_min_rank,
        max_score       = v_max_score,
        max_rank        = v_max_rank,
        avg_score       = v_avg_score,
        avg_rank        = v_avg_rank,
        history         = COALESCE(v_history, '[]'::jsonb),
        updated_at      = NOW()
    WHERE id = p_group_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_recalc_group(INTEGER) IS '重算单个专业组的聚合统计数据（专业数、录取人数、分数/位次等）及 history jsonb';


COMMIT;
