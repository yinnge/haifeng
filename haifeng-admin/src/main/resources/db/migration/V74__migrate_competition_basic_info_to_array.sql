ALTER TABLE t_competition_detail
    ALTER COLUMN basic_info SET DEFAULT '[]'::JSONB;

UPDATE t_competition_detail
SET basic_info = COALESCE(
    (
        SELECT jsonb_agg(
            jsonb_build_object('title', e.k, 'content', e.v)
            ORDER BY e.k
        )
        FROM jsonb_each_text(basic_info) AS e(k, v)
    ),
    '[]'::JSONB
)
WHERE basic_info IS NOT NULL
  AND jsonb_typeof(basic_info) = 'object';

COMMENT ON COLUMN t_competition_detail.basic_info IS '基本信息（JSONB数组，每项含title和content）';
