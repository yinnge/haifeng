-- ============================================
-- V7__create_major_tables.sql
-- 专业管理模块：专业主表、专业详情、考研专业、考研专业-大学关联表
-- ============================================

-- ----------------------------------------------------------
-- 0. 创建通用的 updated_at 触发器函数（如果不存在）
-- ----------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================
-- 1. 专业主表 (t_major)
-- ===========================================================
CREATE TABLE t_major (
    id                  BIGINT          PRIMARY KEY,
    major_code          VARCHAR(20)     NOT NULL,
    major_name          VARCHAR(100)    NOT NULL,
    discipline_name     VARCHAR(100),
    major_type          VARCHAR(30)     NOT NULL,
    major_category      VARCHAR(50),
    parent_category     VARCHAR(50),
    major_tags          VARCHAR(50),
    degree_awarded      VARCHAR(50),
    employment_rate     NUMERIC(5, 2),
    salary_min          INTEGER,
    salary_max          INTEGER,
    description         TEXT,
    status              SMALLINT        NOT NULL DEFAULT 1,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_major_code UNIQUE (major_code),
    CONSTRAINT chk_major_employment_rate CHECK (
        employment_rate IS NULL OR (employment_rate >= 0 AND employment_rate <= 100)
    ),
    CONSTRAINT chk_major_salary_range CHECK (
        (salary_min IS NULL AND salary_max IS NULL)
        OR (salary_min IS NULL)
        OR (salary_max IS NULL)
        OR (salary_min >= 0 AND salary_max >= 0 AND salary_min <= salary_max)
    )
);

-- 索引
CREATE INDEX idx_major_name ON t_major USING btree (major_name varchar_pattern_ops) WHERE status = 1;
CREATE INDEX idx_major_category ON t_major (major_category) WHERE status = 1;
CREATE INDEX idx_major_type ON t_major (major_type) WHERE status = 1;
CREATE INDEX idx_major_employment_rate ON t_major (employment_rate DESC NULLS LAST) WHERE status = 1;
CREATE INDEX idx_major_parent_category ON t_major (parent_category) WHERE status = 1;

-- 触发器
CREATE TRIGGER trg_major_updated_at
    BEFORE UPDATE ON t_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_major IS '专业主表：存储高校专业基本信息、分类及就业指标';
COMMENT ON COLUMN t_major.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_major.major_code IS '专业代码（教育部国标6位代码，如 080901）';
COMMENT ON COLUMN t_major.major_name IS '专业名称（如：计算机科学与技术）';
COMMENT ON COLUMN t_major.discipline_name IS '学科名称';
COMMENT ON COLUMN t_major.major_type IS '专业类型（如：本科、专科）';
COMMENT ON COLUMN t_major.major_category IS '专业类别 - 学科门类（如：工学、理学、农学）';
COMMENT ON COLUMN t_major.parent_category IS '所属类别 - 专业类（如：计算机类、电子信息类）';
COMMENT ON COLUMN t_major.major_tags IS '专业标签（如：热门、新工科、国家特色专业）';
COMMENT ON COLUMN t_major.degree_awarded IS '授予学位（如：工学学士、理学学士）';
COMMENT ON COLUMN t_major.employment_rate IS '就业率，百分比（如 92.50 代表 92.50%）';
COMMENT ON COLUMN t_major.salary_min IS '薪资范围下限（单位：元/月）';
COMMENT ON COLUMN t_major.salary_max IS '薪资范围上限（单位：元/月）';
COMMENT ON COLUMN t_major.description IS '专业描述（培养目标、核心课程等）';
COMMENT ON COLUMN t_major.status IS '状态：0=禁用，1=启用';
COMMENT ON COLUMN t_major.created_at IS '创建时间';
COMMENT ON COLUMN t_major.updated_at IS '更新时间';


-- ===========================================================
-- 2. 专业详情表 (t_major_detail) - 与 t_major 1:1
-- 关联关系说明（不使用外键约束，性能考虑）：
--   major_id -> t_major.id
-- ===========================================================
CREATE TABLE t_major_detail (
    id                      BIGINT          PRIMARY KEY,
    major_id                BIGINT          NOT NULL,
    course_count            INTEGER,
    graduate_scale          VARCHAR(20),
    male_ratio              NUMERIC(5, 2),
    female_ratio            NUMERIC(5, 2),
    major_description       TEXT,
    training_objective      TEXT,
    training_requirement    TEXT,
    subject_requirement     TEXT,
    career_prospect         TEXT,
    main_courses            TEXT[]          DEFAULT '{}',
    knowledge_skills        TEXT[]          DEFAULT '{}',
    status                  SMALLINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_major_detail_major_id UNIQUE (major_id),
    CONSTRAINT chk_major_detail_male_ratio CHECK (
        male_ratio IS NULL OR (male_ratio >= 0 AND male_ratio <= 100)
    ),
    CONSTRAINT chk_major_detail_female_ratio CHECK (
        female_ratio IS NULL OR (female_ratio >= 0 AND female_ratio <= 100)
    )
);

-- 索引（major_id 已通过 UNIQUE 约束自动创建索引）
CREATE INDEX idx_major_detail_courses ON t_major_detail USING gin (main_courses) WHERE status = 1;

-- 触发器
CREATE TRIGGER trg_major_detail_updated_at
    BEFORE UPDATE ON t_major_detail
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_major_detail IS '专业详情表：与 t_major 一对一，存储专业详细描述。关联：major_id -> t_major.id';
COMMENT ON COLUMN t_major_detail.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_major_detail.major_id IS '关联专业表ID（关联 t_major.id，一对一）';
COMMENT ON COLUMN t_major_detail.course_count IS '课程数量';
COMMENT ON COLUMN t_major_detail.graduate_scale IS '毕业生规模（如：50000-60000人）';
COMMENT ON COLUMN t_major_detail.male_ratio IS '男生比例（%）';
COMMENT ON COLUMN t_major_detail.female_ratio IS '女生比例（%）';
COMMENT ON COLUMN t_major_detail.major_description IS '专业描述';
COMMENT ON COLUMN t_major_detail.training_objective IS '培养目标';
COMMENT ON COLUMN t_major_detail.training_requirement IS '培养要求';
COMMENT ON COLUMN t_major_detail.subject_requirement IS '学科要求';
COMMENT ON COLUMN t_major_detail.career_prospect IS '就业前景';
COMMENT ON COLUMN t_major_detail.main_courses IS '主要课程列表（数组）';
COMMENT ON COLUMN t_major_detail.knowledge_skills IS '知识能力列表（数组）';
COMMENT ON COLUMN t_major_detail.status IS '状态：0=禁用，1=启用';
COMMENT ON COLUMN t_major_detail.created_at IS '创建时间';
COMMENT ON COLUMN t_major_detail.updated_at IS '更新时间';


-- ===========================================================
-- 3. 考研专业表 (t_postgrad_major)
-- ===========================================================
CREATE TABLE t_postgrad_major (
    id                      BIGINT          PRIMARY KEY,
    major_name              VARCHAR(100)    NOT NULL,
    major_code              VARCHAR(20)     NOT NULL,
    degree_type             VARCHAR(20)     NOT NULL,
    discipline_category     VARCHAR(50)     NOT NULL,
    popularity              VARCHAR(10),
    difficulty              VARCHAR(10),
    brief                   TEXT,
    introduction            TEXT,
    exam_subjects           TEXT[]          DEFAULT '{}',
    admission_requirements  TEXT[]          DEFAULT '{}',
    cross_exam_difficulty   VARCHAR(10),
    cross_exam_description  TEXT,
    cross_exam_factors      TEXT[]          DEFAULT '{}',
    status                  SMALLINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_postgrad_major_code UNIQUE (major_code),
    CONSTRAINT chk_postgrad_degree_type CHECK (degree_type IN ('学术学位', '专业学位')),
    CONSTRAINT chk_postgrad_popularity CHECK (popularity IS NULL OR popularity IN ('热门', '一般', '冷门')),
    CONSTRAINT chk_postgrad_difficulty CHECK (difficulty IS NULL OR difficulty IN ('高', '中', '低')),
    CONSTRAINT chk_postgrad_cross_exam_difficulty CHECK (cross_exam_difficulty IS NULL OR cross_exam_difficulty IN ('较易', '中等', '较难'))
);

-- 索引
CREATE INDEX idx_postgrad_major_name ON t_postgrad_major USING btree (major_name varchar_pattern_ops) WHERE status = 1;
CREATE INDEX idx_postgrad_discipline_category ON t_postgrad_major (discipline_category) WHERE status = 1;
CREATE INDEX idx_postgrad_degree_type ON t_postgrad_major (degree_type) WHERE status = 1;
CREATE INDEX idx_postgrad_popularity ON t_postgrad_major (popularity) WHERE status = 1;
CREATE INDEX idx_postgrad_difficulty ON t_postgrad_major (difficulty) WHERE status = 1;
CREATE INDEX idx_postgrad_exam_subjects ON t_postgrad_major USING gin (exam_subjects) WHERE status = 1;

-- 触发器
CREATE TRIGGER trg_postgrad_major_updated_at
    BEFORE UPDATE ON t_postgrad_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_postgrad_major IS '考研专业表：研究生招生专业目录信息';
COMMENT ON COLUMN t_postgrad_major.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_postgrad_major.major_name IS '专业名称（如：金融学）';
COMMENT ON COLUMN t_postgrad_major.major_code IS '专业代码（如：020204）';
COMMENT ON COLUMN t_postgrad_major.degree_type IS '学位类型（学术学位/专业学位）';
COMMENT ON COLUMN t_postgrad_major.discipline_category IS '学科门类（如：经济学、工学、管理学）';
COMMENT ON COLUMN t_postgrad_major.popularity IS '热度（热门/一般/冷门）';
COMMENT ON COLUMN t_postgrad_major.difficulty IS '难度（高/中/低）';
COMMENT ON COLUMN t_postgrad_major.brief IS '专业简介（简短概括）';
COMMENT ON COLUMN t_postgrad_major.introduction IS '专业介绍（详细描述）';
COMMENT ON COLUMN t_postgrad_major.exam_subjects IS '考试科目列表';
COMMENT ON COLUMN t_postgrad_major.admission_requirements IS '报考要求列表';
COMMENT ON COLUMN t_postgrad_major.cross_exam_difficulty IS '跨考难度（较易/中等/较难）';
COMMENT ON COLUMN t_postgrad_major.cross_exam_description IS '跨考难度描述';
COMMENT ON COLUMN t_postgrad_major.cross_exam_factors IS '跨考影响因素列表';
COMMENT ON COLUMN t_postgrad_major.status IS '状态：0=禁用，1=启用';
COMMENT ON COLUMN t_postgrad_major.created_at IS '创建时间';
COMMENT ON COLUMN t_postgrad_major.updated_at IS '更新时间';


-- ===========================================================
-- 4. 考研专业-大学关联表 (t_postgrad_major_university)
-- 关联关系说明（不使用外键约束，性能考虑）：
--   postgrad_major_id -> t_postgrad_major.id
--   university_id -> t_university.id
-- ===========================================================
CREATE TABLE t_postgrad_major_university (
    id                      BIGINT          PRIMARY KEY,
    postgrad_major_id       BIGINT          NOT NULL,
    university_id           BIGINT          NOT NULL,
    university_name         VARCHAR(100)    NOT NULL,
    postgrad_major_name     VARCHAR(100)    NOT NULL,
    sort_order              INTEGER         DEFAULT 0,
    status                  SMALLINT        NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- 约束
    CONSTRAINT uk_postgrad_major_university UNIQUE (postgrad_major_id, university_id)
);

-- 索引
CREATE INDEX idx_pmu_major ON t_postgrad_major_university (postgrad_major_id, sort_order) WHERE status = 1;
CREATE INDEX idx_pmu_university ON t_postgrad_major_university (university_id) WHERE status = 1;

-- 触发器
CREATE TRIGGER trg_postgrad_major_university_updated_at
    BEFORE UPDATE ON t_postgrad_major_university
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_postgrad_major_university IS '考研专业-大学 多对多关联表。关联：postgrad_major_id -> t_postgrad_major.id, university_id -> t_university.id';
COMMENT ON COLUMN t_postgrad_major_university.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_postgrad_major_university.postgrad_major_id IS '考研专业ID（关联 t_postgrad_major.id）';
COMMENT ON COLUMN t_postgrad_major_university.university_id IS '大学ID（关联 t_university.id）';
COMMENT ON COLUMN t_postgrad_major_university.university_name IS '大学名称（冗余，方便展示）';
COMMENT ON COLUMN t_postgrad_major_university.postgrad_major_name IS '考研专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_postgrad_major_university.sort_order IS '排序权重';
COMMENT ON COLUMN t_postgrad_major_university.status IS '状态：0=禁用，1=启用';
COMMENT ON COLUMN t_postgrad_major_university.created_at IS '创建时间';
COMMENT ON COLUMN t_postgrad_major_university.updated_at IS '更新时间';
