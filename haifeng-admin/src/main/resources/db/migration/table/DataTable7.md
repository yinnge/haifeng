### 专业表 (t_major)

```
-- ============================================================
-- 专业表 (t_major)
-- 描述：存储高校专业的基本信息、分类体系及就业相关指标数据
-- 作者：DBA
-- 创建日期：2025-01-01
-- ============================================================


-- ----------------------------------------------------------
-- 2. 创建专业信息主表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_major (

    -- ==================== 基础标识 ====================
    id                  BIGSERIAL       PRIMARY KEY,
    major_code          VARCHAR(20)     NOT NULL,           -- 专业代码（教育部国标代码，如 080901）
    major_name          VARCHAR(100)    NOT NULL,           -- 专业名称（如 计算机科学与技术）
    
    discipline_name     VARCHAR(100),                       -- 学科名称
    
    -- ==================== 分类体系 ====================
    major_type          VARCHAR(30)     NOT NULL,           -- 专业类型（本科 / 专科 ）
    major_category      VARCHAR(50),                        -- 专业类别·学科门类（如 工学、农学、理学）
    parent_category     VARCHAR(50),                       -- 所属类别·专业类（如 计算机类、电子信息类）
    major_tags          VARCHAR(50),                       -- 专业标签（如 {"热门","新工科","国家特色"}）
    degree_awarded      VARCHAR(50),                        -- 授予学位（如 工学学士、理学学士）
    study_duration      VARCHAR(20),                        -- 学制（如 四年、五年、4-5年）
                            
    -- ==================== 就业与评价指标 ====================
    employment_rate     NUMERIC(5, 2),                      -- 就业率（%），如 92.50 表示 92.50%
    salary_min          INTEGER,                            -- 薪资范围下限（元/月）
    salary_max          INTEGER,                            -- 薪资范围上限（元/月）
    
    -- ==================== 详细描述 ====================
    description         TEXT,                               -- 专业描述（培养目标、核心课程等）

    -- ==================== 审计字段 ====================
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,   -- 软删除标记
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),   -- 创建时间
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),   -- 最后更新时间

    -- ==================== 约束 ====================
    -- 专业代码唯一（同一专业不可重复录入）
    CONSTRAINT uk_major_code
        UNIQUE (major_code),

    -- 就业率范围：0% ~ 100%
    CONSTRAINT chk_employment_rate
        CHECK (employment_rate IS NULL OR (employment_rate >= 0 AND employment_rate <= 100)),

    -- 薪资逻辑：下限 ≤ 上限，且不为负
    CONSTRAINT chk_salary_range
        CHECK (
            (salary_min IS NULL AND salary_max IS NULL)
            OR (salary_min IS NULL)
            OR (salary_max IS NULL)
            OR (salary_min >= 0 AND salary_max >= 0 AND salary_min <= salary_max)
        )
    
);


-- ----------------------------------------------------------
-- 3. 创建索引（针对高频查询场景优化）
-- ----------------------------------------------------------

-- 按专业名称模糊搜索（支持 LIKE '计算机%' 查询）
CREATE INDEX idx_major_name
    ON t_major USING btree (major_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按学科门类筛选（如：查所有工学专业）
CREATE INDEX idx_major_category
    ON t_major (major_category)
    WHERE is_deleted = FALSE;

-- 按专业类型 + 层次组合筛选
CREATE INDEX idx_major_type_level
    ON t_major (major_type, level)
    WHERE is_deleted = FALSE;

-- 按推荐指数排序（热门专业排行榜）
CREATE INDEX idx_major_recommendation
    ON t_major (recommendation_idx DESC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 按就业率排序
CREATE INDEX idx_major_employment_rate
    ON t_major (employment_rate DESC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 专业标签 GIN 索引（支持数组包含查询）
CREATE INDEX idx_major_tags
    ON t_major USING gin (major_tags)
    WHERE is_deleted = FALSE;

-- 按所属类别筛选（如：查所有"计算机类"专业）
CREATE INDEX idx_major_parent_category
    ON t_major (parent_category)
    WHERE is_deleted = FALSE;


-- ----------------------------------------------------------
-- 4. 触发器：自动更新 updated_at
-- ----------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_major_updated_at
    BEFORE UPDATE ON t_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 5. 表与字段注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_major                       IS '专业信息表：存储高校专业基本信息、分类及就业指标';

COMMENT ON COLUMN t_major.id                    IS '主键ID，自增';
COMMENT ON COLUMN t_major.major_code            IS '专业代码（教育部国标6位代码，如 080901）';
COMMENT ON COLUMN t_major.major_name            IS '专业名称（如：计算机科学与技术）';
COMMENT ON COLUMN t_major.major_type            IS '专业类型（如：本科、专科）';
COMMENT ON COLUMN t_major.major_category        IS '专业类别 - 学科门类（如：工学、理学、农学）';
COMMENT ON COLUMN t_major.parent_category       IS '所属类别 - 专业类（如：计算机类、电子信息类）';
COMMENT ON COLUMN t_major.major_tags            IS '专业标签，数组存储（如：热门、新工科、国家特色专业）';
COMMENT ON COLUMN t_major.degree_awarded        IS '授予学位（如：工学学士、理学学士、管理学学士）';
COMMENT ON COLUMN t_major.study_duration        IS '学制（如：四年、五年、4-5年）';
COMMENT ON COLUMN t_major.level                 IS '层次（如：本科、专科、本科一批）';
COMMENT ON COLUMN t_major.employment_rate       IS '就业率，百分比（如 92.50 代表 92.50%）';
COMMENT ON COLUMN t_major.salary_min            IS '薪资范围下限（单位：元/月）';
COMMENT ON COLUMN t_major.salary_max            IS '薪资范围上限（单位：元/月）';
COMMENT ON COLUMN t_major.difficulty            IS '学习难度（低/中/高）';
COMMENT ON COLUMN t_major.job_satisfaction      IS '就业满意度评分（0.0 ~ 5.0）';
COMMENT ON COLUMN t_major.recommendation_idx    IS '推荐指数（0.0 ~ 5.0）';
COMMENT ON COLUMN t_major.description           IS '专业描述（培养目标、核心课程、发展方向等）';
COMMENT ON COLUMN t_major.is_deleted            IS '软删除标记（FALSE=正常，TRUE=已删除）';
COMMENT ON COLUMN t_major.created_at            IS '记录创建时间';
COMMENT ON COLUMN t_major.updated_at            IS '记录最后更新时间';

COMMIT;
```

# 专业详情表（t_major_detail）


```
-- ============================================================
-- 专业详情表 (t_major_detail)
-- 描述：与 t_major 一对一，存储专业的详细描述、课程、院校等信息
-- ============================================================


CREATE TABLE IF NOT EXISTS t_major_detail (

    -- ==================== 基础标识 ====================
    id                  BIGSERIAL       PRIMARY KEY,
    major_id            BIGINT          NOT NULL,           -- 关联专业表主键（一对一）
    -- ==================== 统计数据 ====================
    course_count        INTEGER,                            -- 课程数量
    graduate_scale      VARCHAR(20),                        -- 毕业生规模（如：50000-60000人）
    male_ratio          NUMERIC(5, 2),                      -- 男生比例（%），如 62.30
    female_ratio        NUMERIC(5, 2),                      -- 女生比例（%），如 37.70
    -- ==================== 描述信息（各自独立 TEXT） ====================
    major_description   TEXT,                               -- 专业描述
    training_objective  TEXT,                               -- 培养目标
    training_requirement TEXT,                              -- 培养要求
    subject_requirement TEXT,                               -- 学科要求
    career_prospect     TEXT,                               -- 就业前景
    
    -- ==================== 列表信息（TEXT 数组） ====================
    main_courses        TEXT[]          DEFAULT '{}',        -- 主要课程
    knowledge_skills    TEXT[]          DEFAULT '{}',        -- 知识能力
    
    
    -- ==================== 审计字段 ====================
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    -- 一对一：一个专业只有一条详情
    CONSTRAINT uk_major_detail_major_id
        UNIQUE (major_id),
    -- 比例范围校验
    CONSTRAINT chk_male_ratio
        CHECK (male_ratio IS NULL OR (male_ratio >= 0 AND male_ratio <= 100)),

    CONSTRAINT chk_female_ratio
        CHECK (female_ratio IS NULL OR (female_ratio >= 0 AND female_ratio <= 100))
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- major_id 查询（一对一关联查询核心索引，UNIQUE 已自动创建）
-- 无需额外创建


-- 主要课程数组 GIN 索引
CREATE INDEX idx_major_detail_courses
    ON t_major_detail USING gin (main_courses)
    WHERE is_deleted = FALSE;


-- ----------------------------------------------------------
-- 触发器：自动更新 updated_at
-- ----------------------------------------------------------
CREATE TRIGGER trg_major_detail_updated_at
    BEFORE UPDATE ON t_major_detail
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();  -- 复用之前创建的函数


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_major_detail                          IS '专业详情表：与 t_major 一对一，存储详细描述和院校信息';

COMMENT ON COLUMN t_major_detail.id                       IS '主键ID';
COMMENT ON COLUMN t_major_detail.major_id                 IS '关联专业表ID（一对一）';
COMMENT ON COLUMN t_major_detail.course_count             IS '课程数量';
COMMENT ON COLUMN t_major_detail.graduate_scale           IS '毕业生规模（如：50000-60000人）';
COMMENT ON COLUMN t_major_detail.male_ratio               IS '男生比例（%）';
COMMENT ON COLUMN t_major_detail.female_ratio             IS '女生比例（%）';
COMMENT ON COLUMN t_major_detail.major_description        IS '专业描述';
COMMENT ON COLUMN t_major_detail.training_objective       IS '培养目标';
COMMENT ON COLUMN t_major_detail.training_requirement     IS '培养要求';
COMMENT ON COLUMN t_major_detail.subject_requirement      IS '学科要求';
COMMENT ON COLUMN t_major_detail.career_prospect          IS '就业前景';
COMMENT ON COLUMN t_major_detail.main_courses             IS '主要课程列表（数组）';
COMMENT ON COLUMN t_major_detail.knowledge_skills         IS '知识能力列表（数组）';
COMMENT ON COLUMN t_major_detail.postgrad_directions      IS '考研方向列表（数组）';
COMMENT ON COLUMN t_major_detail.career_directions        IS '就业方向列表（数组）';
COMMENT ON COLUMN t_major_detail.college_info             IS '开设院校信息，结构：[{"name":"xx大学","location":"xx","min_score":654}]';
COMMENT ON COLUMN t_major_detail.is_deleted               IS '软删除标记';
COMMENT ON COLUMN t_major_detail.created_at               IS '创建时间';
COMMENT ON COLUMN t_major_detail.updated_at               IS '更新时间';

COMMIT;
```

# 考研专业表 (t_postgrad_major)


```
-- ============================================================
-- 考研专业表 (t_postgrad_major)
-- 描述：研究生招生专业目录，包含考试科目、跨考信息、招生院校等
-- ============================================================


CREATE TABLE IF NOT EXISTS t_postgrad_major (

    -- ==================== 基础标识 ====================
    id                      SERIAL          PRIMARY KEY,
    major_name              VARCHAR(100)    NOT NULL,           -- 专业名称（如：金融学）
    major_code              VARCHAR(20)     NOT NULL,           -- 专业代码（如：020204）
    -- ==================== 分类信息 ====================
    degree_type             VARCHAR(20)     NOT NULL,           -- 学位类型（学术学位 / 专业学位）
    discipline_category     VARCHAR(50)     NOT NULL,           -- 学科门类（如：经济学、工学）
    -- ==================== 热度与难度 ====================
    popularity              VARCHAR(10),                        -- 热度（热门 / 一般 / 冷门）
    difficulty              VARCHAR(10),                        -- 难度（高 / 中 / 低）
    -- ==================== 描述信息 ====================
    brief                   TEXT,                               -- 专业简介（简短概括）
    introduction            TEXT,                               -- 专业介绍（详细内容）
    
    -- ==================== 考试与报考 ====================
    exam_subjects           TEXT[]          DEFAULT '{}',        -- 考试科目
    admission_requirements  TEXT[]          DEFAULT '{}',        -- 报考要求

    -- ==================== 跨考信息 ====================
    cross_exam_difficulty   VARCHAR(10),                        -- 跨考难度（较易 / 中等 / 较难）
    cross_exam_description  TEXT,                               -- 跨考难度描述
    
    cross_exam_factors      TEXT[]          DEFAULT '{}',        -- 跨考影响因素
    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    -- 专业代码唯一
    CONSTRAINT uk_postgrad_major_code
        UNIQUE (major_code),

    -- 学位类型校验
    CONSTRAINT chk_degree_type
        CHECK (degree_type IN ('学术学位', '专业学位')),

    -- 热度校验
    CONSTRAINT chk_popularity
        CHECK (popularity IS NULL OR popularity IN ('热门', '一般', '冷门')),

    -- 难度校验
    CONSTRAINT chk_pg_difficulty
        CHECK (difficulty IS NULL OR difficulty IN ('高', '中', '低')),

    -- 跨考难度校验
    CONSTRAINT chk_cross_exam_difficulty
        CHECK (cross_exam_difficulty IS NULL OR cross_exam_difficulty IN ('较易', '中等', '较难'))
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按专业名称搜索
CREATE INDEX idx_pg_major_name
    ON t_postgrad_major USING btree (major_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按学科门类筛选
CREATE INDEX idx_pg_discipline_category
    ON t_postgrad_major (discipline_category)
    WHERE is_deleted = FALSE;

-- 按学位类型筛选
CREATE INDEX idx_pg_degree_type
    ON t_postgrad_major (degree_type)
    WHERE is_deleted = FALSE;

-- 按热度筛选
CREATE INDEX idx_pg_popularity
    ON t_postgrad_major (popularity)
    WHERE is_deleted = FALSE;

-- 按难度筛选
CREATE INDEX idx_pg_difficulty
    ON t_postgrad_major (difficulty)
    WHERE is_deleted = FALSE;

-- 考试科目数组 GIN 索引
CREATE INDEX idx_pg_exam_subjects
    ON t_postgrad_major USING gin (exam_subjects)
    WHERE is_deleted = FALSE;

-- 可跨考专业数组 GIN 索引
CREATE INDEX idx_pg_cross_exam_majors
    ON t_postgrad_major USING gin (cross_exam_majors)
    WHERE is_deleted = FALSE;

-- ----------------------------------------------------------
-- 触发器：自动更新 updated_at（复用已有函数）
-- ----------------------------------------------------------
CREATE TRIGGER trg_postgrad_major_updated_at
    BEFORE UPDATE ON t_postgrad_major
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_postgrad_major                              IS '考研专业表：研究生招生专业目录信息';

COMMENT ON COLUMN t_postgrad_major.id                           IS '主键ID，自增';
COMMENT ON COLUMN t_postgrad_major.major_name                   IS '专业名称（如：金融学）';
COMMENT ON COLUMN t_postgrad_major.major_code                   IS '专业代码（如：020204）';
COMMENT ON COLUMN t_postgrad_major.degree_type                  IS '学位类型（学术学位/专业学位）';
COMMENT ON COLUMN t_postgrad_major.discipline_category          IS '学科门类（如：经济学、工学、管理学）';
COMMENT ON COLUMN t_postgrad_major.popularity                   IS '热度（热门/一般/冷门）';
COMMENT ON COLUMN t_postgrad_major.difficulty                   IS '难度（高/中/低）';
COMMENT ON COLUMN t_postgrad_major.brief                        IS '专业简介（简短概括）';
COMMENT ON COLUMN t_postgrad_major.introduction                 IS '专业介绍（详细描述）';
COMMENT ON COLUMN t_postgrad_major.exam_subjects                IS '考试科目列表';
COMMENT ON COLUMN t_postgrad_major.admission_requirements       IS '报考要求列表';
COMMENT ON COLUMN t_postgrad_major.cross_exam_difficulty        IS '跨考难度（较易/中等/较难）';
COMMENT ON COLUMN t_postgrad_major.cross_exam_description       IS '跨考难度描述';
COMMENT ON COLUMN t_postgrad_major.cross_exam_majors            IS '可跨考专业列表';
COMMENT ON COLUMN t_postgrad_major.cross_exam_factors           IS '跨考影响因素列表';
COMMENT ON COLUMN t_postgrad_major.is_deleted                   IS '软删除标记';
COMMENT ON COLUMN t_postgrad_major.created_at                   IS '创建时间';
COMMENT ON COLUMN t_postgrad_major.updated_at                   IS '更新时间';

COMMIT;
```

### 考研专业-大学 关联中间表


```
-- ============================================================
-- 考研专业-大学 关联表 (t_postgrad_major_university)
-- 描述：多对多关系，记录哪些大学招收哪些考研专业
-- ============================================================


CREATE TABLE IF NOT EXISTS t_postgrad_major_university (

    id                      SERIAL          PRIMARY KEY,
    postgrad_major_id       INTEGER         NOT NULL,           -- 考研专业ID
    university_id           INTEGER         NOT NULL,           -- 大学ID
    university_name         VARCHAR(100)    NOT NULL,           -- 大学名称（冗余，方便展示）
    postgrad_major_name     VARCHAR(100)    NOT NULL,           -- 考研大学名称（冗余，方便展示）
    
    sort_order              INTEGER         DEFAULT 0,          -- 排序权重
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    
    -- 同一对关系不重复
    CONSTRAINT uk_pg_major_university
        UNIQUE (postgrad_major_id, university_id)
);


-- 索引
-- 按考研专业查大学（专业详情页展示）
CREATE INDEX idx_pmu_major
    ON t_postgrad_major_university (postgrad_major_id, sort_order);

-- 按大学查考研专业（大学详情页展示）
CREATE INDEX idx_pmu_university
    ON t_postgrad_major_university (university_id);

-- 按标签筛选
CREATE INDEX idx_pmu_tag
    ON t_postgrad_major_university (university_tag);


-- 注释
COMMENT ON TABLE  t_postgrad_major_university                       IS '考研专业-大学 多对多关联表';
COMMENT ON COLUMN t_postgrad_major_university.postgrad_major_id     IS '考研专业ID';
COMMENT ON COLUMN t_postgrad_major_university.university_id         IS '大学ID';
COMMENT ON COLUMN t_postgrad_major_university.university_name       IS '大学名称（冗余）';

COMMENT ON COLUMN t_postgrad_major_university.sort_order            IS '排序权重';

COMMIT;
```
