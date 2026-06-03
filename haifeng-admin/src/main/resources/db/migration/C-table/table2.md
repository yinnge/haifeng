### 1.4 `t_member_gaokao` 高考档案表 —【全新创建】



```
-- ============================================================
-- 用户高考档案表 (t_member_gaokao)
-- 描述：一个用户一条记录，存储高考的所有业务信息
--       系统推荐算法的核心输入数据
-- ============================================================

CREATE TABLE IF NOT EXISTS t_member_gaokao (

    id                      SERIAL          PRIMARY KEY,
    member_id               INTEGER         NOT NULL UNIQUE,    -- 关联 t_member（一对一）

    -- ============================================================
    -- 一、高考基本信息（必填，推荐算法核心输入）
    -- ============================================================
    gaokao_year             SMALLINT,                           -- 高考年份（2025/2026）
    gaokao_province         VARCHAR(30),                        -- 高考省份（四川/广东...）
    score                   INTEGER,                            -- 高考总分
    rank                    INTEGER,                            -- 位次（可自动计算）

    -- ============================================================
    -- 二、选科信息
    -- ============================================================
    -- 首选科目（3+1+2 的"1"）
    subject_type            VARCHAR(20),                        -- 第一科目
    second_subject_type     VARCHAR(20),                        -- 第二科目
    third_subject_type      VARCHAR(20),                        -- 第三科目

    -- ============================================================
    -- 三、各科成绩（用于匹配单科分数要求）
    -- ============================================================
    score_chinese           INTEGER,                            -- 语文
    score_math              INTEGER,                            -- 数学
    score_english           INTEGER,                            -- 外语
    score_subject_1         INTEGER,                            -- 首选科目分数（物理/历史原始分）
    score_subject_2         INTEGER,                            -- 再选科目1分数（赋分后）
    score_subject_3         INTEGER,                            -- 再选科目2分数（赋分后）

    -- ============================================================
    -- 四、外语语种
    -- ============================================================
    foreign_language        VARCHAR(20)     DEFAULT '英语',      -- 英语/日语/俄语/德语/法语/西班牙语/其他

    -- ============================================================
    -- 五、身体视觉条件
    -- ============================================================
    is_color_blind          BOOLEAN         DEFAULT FALSE,       -- 色盲
    is_color_weak           BOOLEAN         DEFAULT FALSE,       -- 色弱
    vision_left             NUMERIC(3,1),                        -- 左眼裸眼视力（如 4.8）
    vision_right            NUMERIC(3,1),                        -- 右眼裸眼视力
    has_smell_disorder      BOOLEAN         DEFAULT FALSE,       -- 嗅觉迟钝

    -- ============================================================
    -- 六、身体指标
    -- ============================================================
    height_cm               INTEGER,                             -- 身高（厘米）
    weight_kg               NUMERIC(5,1),                        -- 体重（公斤）
    is_left_handed          BOOLEAN         DEFAULT FALSE,       -- 左利手
    has_tattoo              BOOLEAN         DEFAULT FALSE,       -- 纹身
    has_scar                BOOLEAN         DEFAULT FALSE,       -- 面部明显疤痕
    has_stutter             BOOLEAN         DEFAULT FALSE,       -- 口吃

    -- ============================================================
    -- 七、身份条件
    -- ============================================================
    is_fresh_graduate       BOOLEAN         DEFAULT TRUE,        -- 应届生（TRUE=应届，FALSE=往届/复读）
    political_status        VARCHAR(20)     DEFAULT '群众',      -- 政治面貌（群众/共青团员/中共党员）
    household_type          VARCHAR(20),                         -- 户籍类型（城镇/农村）
    is_poverty_county       BOOLEAN         DEFAULT FALSE,       -- 是否国家级贫困县户籍

    -- ============================================================
    -- 八、计算字段（系统自动填充）
    -- ============================================================
    batch_line_score        INTEGER,                             -- 所在批次的省控线（系统查表填入）
    score_above_line        INTEGER,                             -- 线差 = score - batch_line_score

    -- ============================================================
    -- 审计字段
    -- ============================================================
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------
CREATE INDEX idx_mg_member ON t_member_gaokao (member_id);
CREATE INDEX idx_mg_province_year ON t_member_gaokao (gaokao_province, gaokao_year);
CREATE INDEX idx_mg_score ON t_member_gaokao (score DESC NULLS LAST);


-- ----------------------------------------------------------
-- 触发器：自动更新 updated_at
-- ----------------------------------------------------------
CREATE TRIGGER trg_mg_updated_at
    BEFORE UPDATE ON t_member_gaokao
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
```
