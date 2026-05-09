### 本科专业-考研方向 关联表 

```
-- ============================================================
-- 本科专业-考研方向 关联表 (t_major_postgrad_direction)
-- 描述：本科专业 ↔ 考研专业 的多对多推荐关系
-- ============================================================

CREATE TABLE IF NOT EXISTS t_major_postgrad_direction (

    id                  SERIAL          PRIMARY KEY,
    major_id            BIGINT          NOT NULL,               -- 本科专业ID
    postgrad_major_id   INTEGER         NOT NULL,               -- 考研专业ID
    major_name          VARCHAR(100)    NOT NULL,
    postgrad_major_name VARCHAR(100)    NOT NULL,
    sort_order          INTEGER         DEFAULT 0,              -- 推荐排序

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_major_postgrad
        UNIQUE (major_id, postgrad_major_id)
);

CREATE INDEX idx_mpd_major ON t_major_postgrad_direction (major_id);
CREATE INDEX idx_mpd_postgrad ON t_major_postgrad_direction (postgrad_major_id);
```