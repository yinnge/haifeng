-- V28__create_member_gaokao_tables.sql
-- 用户高考档案表 (t_member_gaokao)
-- 描述：一个用户一条记录，存储高考的所有业务信息
--       系统推荐算法的核心输入数据

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_gaokao (
    id                      BIGINT          PRIMARY KEY,
    member_id               BIGINT          NOT NULL UNIQUE,

    -- 一、高考基本信息（必填）
    gaokao_year             SMALLINT,
    gaokao_province         VARCHAR(30),
    score                   INTEGER,
    rank                    INTEGER,

    -- 二、改革模式（系统根据省份+年份自动判断）
    reform_model            VARCHAR(20),

    -- 三、选科信息（必填，与分数字段一一对应）
    subject_type            VARCHAR(20),
    second_subject_type     VARCHAR(20),
    third_subject_type      VARCHAR(20),

    -- 四、各科成绩（可选）
    score_chinese           INTEGER,
    score_math              INTEGER,
    score_english           INTEGER,
    score_subject_1         INTEGER,
    score_subject_2         INTEGER,
    score_subject_3         INTEGER,

    -- 五、外语语种（可选）
    foreign_language        VARCHAR(20),

    -- 六、身体视觉条件（可选，全部允许 NULL）
    is_color_blind          BOOLEAN,
    is_color_weak           BOOLEAN,
    vision_left             NUMERIC(3,1),
    vision_right            NUMERIC(3,1),
    has_smell_disorder      BOOLEAN,

    -- 七、身体指标（可选）
    height_cm               INTEGER,
    weight_kg               NUMERIC(5,1),
    is_left_handed          BOOLEAN,
    has_tattoo              BOOLEAN,
    has_scar                BOOLEAN,
    has_stutter             BOOLEAN,

    -- 八、身份条件（可选）
    is_fresh_graduate       BOOLEAN,
    political_status        VARCHAR(20),
    household_type          VARCHAR(20),
    is_poverty_county       BOOLEAN,

    -- 九、批次与线差
    batch                   VARCHAR(50),
    batch_data_year         SMALLINT,
    batch_line_score        INTEGER,
    score_above_line        INTEGER,

    -- 审计字段
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_mg_member ON t_member_gaokao (member_id);
CREATE INDEX IF NOT EXISTS idx_mg_province_year ON t_member_gaokao (gaokao_province, gaokao_year);
CREATE INDEX IF NOT EXISTS idx_mg_score ON t_member_gaokao (score DESC NULLS LAST);

-- 外键（仅当不存在时添加）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_mg_member'
          AND conrelid = 't_member_gaokao'::regclass
    ) THEN
        ALTER TABLE t_member_gaokao
            ADD CONSTRAINT fk_mg_member
            FOREIGN KEY (member_id) REFERENCES t_member(id);
    END IF;
END
$$;

-- 触发器（仅当不存在时创建）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger
        WHERE tgname = 'trg_mg_updated_at'
          AND tgrelid = 't_member_gaokao'::regclass
    ) THEN
        CREATE TRIGGER trg_mg_updated_at
            BEFORE UPDATE ON t_member_gaokao
            FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();
    END IF;
END
$$;

-- 注释
COMMENT ON TABLE  t_member_gaokao IS '用户高考档案表：一人一条，志愿算法核心输入';
COMMENT ON COLUMN t_member_gaokao.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_member_gaokao.member_id IS '关联会员表ID（唯一）';
COMMENT ON COLUMN t_member_gaokao.gaokao_year IS '高考年份';
COMMENT ON COLUMN t_member_gaokao.gaokao_province IS '高考省份';
COMMENT ON COLUMN t_member_gaokao.score IS '高考总分';
COMMENT ON COLUMN t_member_gaokao.rank IS '位次（系统查询或用户自定义）';
COMMENT ON COLUMN t_member_gaokao.reform_model IS '改革模式（3+3/3+1+2/传统文理）';
COMMENT ON COLUMN t_member_gaokao.subject_type IS '第一科目';
COMMENT ON COLUMN t_member_gaokao.second_subject_type IS '第二科目';
COMMENT ON COLUMN t_member_gaokao.third_subject_type IS '第三科目';
COMMENT ON COLUMN t_member_gaokao.score_chinese IS '语文成绩';
COMMENT ON COLUMN t_member_gaokao.score_math IS '数学成绩';
COMMENT ON COLUMN t_member_gaokao.score_english IS '外语成绩';
COMMENT ON COLUMN t_member_gaokao.score_subject_1 IS '第一科目分数';
COMMENT ON COLUMN t_member_gaokao.score_subject_2 IS '第二科目分数';
COMMENT ON COLUMN t_member_gaokao.score_subject_3 IS '第三科目分数';
COMMENT ON COLUMN t_member_gaokao.foreign_language IS '外语语种';
COMMENT ON COLUMN t_member_gaokao.is_color_blind IS '是否色盲';
COMMENT ON COLUMN t_member_gaokao.is_color_weak IS '是否色弱';
COMMENT ON COLUMN t_member_gaokao.vision_left IS '左眼视力';
COMMENT ON COLUMN t_member_gaokao.vision_right IS '右眼视力';
COMMENT ON COLUMN t_member_gaokao.has_smell_disorder IS '是否嗅觉迟钝';
COMMENT ON COLUMN t_member_gaokao.height_cm IS '身高（厘米）';
COMMENT ON COLUMN t_member_gaokao.weight_kg IS '体重（公斤）';
COMMENT ON COLUMN t_member_gaokao.is_left_handed IS '是否左利手';
COMMENT ON COLUMN t_member_gaokao.has_tattoo IS '是否有纹身';
COMMENT ON COLUMN t_member_gaokao.has_scar IS '是否有面部疤痕';
COMMENT ON COLUMN t_member_gaokao.has_stutter IS '是否口吃';
COMMENT ON COLUMN t_member_gaokao.is_fresh_graduate IS '是否应届生';
COMMENT ON COLUMN t_member_gaokao.political_status IS '政治面貌';
COMMENT ON COLUMN t_member_gaokao.household_type IS '户籍类型';
COMMENT ON COLUMN t_member_gaokao.is_poverty_county IS '是否贫困县户籍';
COMMENT ON COLUMN t_member_gaokao.batch IS '所在批次名称';
COMMENT ON COLUMN t_member_gaokao.batch_data_year IS '批次数据来源年份';
COMMENT ON COLUMN t_member_gaokao.batch_line_score IS '批次省控线';
COMMENT ON COLUMN t_member_gaokao.score_above_line IS '线差（总分-省控线）';
COMMENT ON COLUMN t_member_gaokao.created_at IS '创建时间';
COMMENT ON COLUMN t_member_gaokao.updated_at IS '更新时间';

COMMIT;
