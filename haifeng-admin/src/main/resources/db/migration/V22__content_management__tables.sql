-- ============================================================
-- V22__content_management__tables.sql
-- 模块五：招聘内容管理
-- 包含：统一备考指南、统一公告
-- 共性：纯内容型（文章+公告），不涉及岗位业务逻辑，独立管理后台
-- ============================================================

BEGIN;

-- ============================================================
-- 1. 统一备考指南表 (t_exam_guide)
-- 说明：替代 t_civil_methodology + t_institution_guide + 未来军队文职备考
-- ============================================================
CREATE TABLE IF NOT EXISTS t_exam_guide (
    id                      SERIAL          PRIMARY KEY,

    -- ==================== 分类 ====================
    guide_category          VARCHAR(30)     NOT NULL,
    guide_type              VARCHAR(30)     DEFAULT '备考攻略',

    -- ==================== 内容 ====================
    title                   VARCHAR(300)    NOT NULL,
    subtitle                VARCHAR(300),
    cover_image             VARCHAR(500),
    icon_class              VARCHAR(100),
    summary                 TEXT,
    content                 TEXT            NOT NULL,

    -- ==================== 标签 ====================
    tags                    TEXT[]          DEFAULT '{}',
    difficulty_level        VARCHAR(10),
    target_audience         VARCHAR(50),

    -- ==================== 作者信息 ====================
    author_name             VARCHAR(50),
    author_title            VARCHAR(100),

    -- ==================== 展示控制 ====================
    is_top                  BOOLEAN         DEFAULT FALSE,
    is_recommended          BOOLEAN         DEFAULT FALSE,
    sort_order              INTEGER         DEFAULT 0,
    view_count              INTEGER         DEFAULT 0,
    like_count              INTEGER         DEFAULT 0,

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_guide_category CHECK (
        guide_category IN (
            'civil', 'institution', 'military', 'selection',
            'teacher', 'healthcare', 'finance', 'grassroots',
            'community', 'general'
        )
    ),
    CONSTRAINT chk_guide_type CHECK (
        guide_type IS NULL OR guide_type IN (
            '备考攻略', '科目指导', '真题解析', '面试技巧',
            '时事热点', '经验分享', '政策解读', '学习计划'
        )
    ),
    CONSTRAINT chk_guide_difficulty CHECK (
        difficulty_level IS NULL
        OR difficulty_level IN ('入门', '进阶', '高阶')
    )
);

CREATE INDEX idx_guide_category     ON t_exam_guide (guide_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_guide_cat_type     ON t_exam_guide (guide_category, guide_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_guide_recommended  ON t_exam_guide (guide_category, sort_order DESC) WHERE is_deleted = FALSE AND is_recommended = TRUE;
CREATE INDEX idx_guide_top_date     ON t_exam_guide (is_top DESC, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_guide_title        ON t_exam_guide USING btree (title varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_guide_tags         ON t_exam_guide USING gin (tags) WHERE is_deleted = FALSE;
CREATE INDEX idx_guide_created      ON t_exam_guide (created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_guide_popular      ON t_exam_guide (view_count DESC) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_exam_guide_updated_at
    BEFORE UPDATE ON t_exam_guide
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON TABLE  t_exam_guide                      IS '统一备考指南表（全平台备考文章/经验/技巧）';
COMMENT ON COLUMN t_exam_guide.guide_category       IS '指南类别：civil/institution/military/selection/teacher/healthcare/finance/grassroots/community/general';
COMMENT ON COLUMN t_exam_guide.guide_type           IS '指南类型（备考攻略/科目指导/真题解析/面试技巧等）';
COMMENT ON COLUMN t_exam_guide.title                IS '文章标题';
COMMENT ON COLUMN t_exam_guide.subtitle             IS '副标题';
COMMENT ON COLUMN t_exam_guide.cover_image          IS '封面图片URL';
COMMENT ON COLUMN t_exam_guide.icon_class           IS '图标CSS类名（Font Awesome）';
COMMENT ON COLUMN t_exam_guide.summary              IS '摘要';
COMMENT ON COLUMN t_exam_guide.content              IS '详细内容（支持HTML格式）';
COMMENT ON COLUMN t_exam_guide.tags                 IS '标签列表';
COMMENT ON COLUMN t_exam_guide.difficulty_level     IS '难度（入门/进阶/高阶）';
COMMENT ON COLUMN t_exam_guide.target_audience      IS '目标读者';
COMMENT ON COLUMN t_exam_guide.author_name          IS '作者名';
COMMENT ON COLUMN t_exam_guide.author_title         IS '作者头衔';
COMMENT ON COLUMN t_exam_guide.is_top               IS '是否置顶';
COMMENT ON COLUMN t_exam_guide.is_recommended       IS '是否编辑推荐';
COMMENT ON COLUMN t_exam_guide.sort_order           IS '排序权重';
COMMENT ON COLUMN t_exam_guide.view_count           IS '阅读量';
COMMENT ON COLUMN t_exam_guide.like_count           IS '点赞数';


-- ============================================================
-- 2. 统一公告表 (t_notice)
-- 说明：替代 t_recruitment_notice + t_institution_notice + t_military_notice
-- ============================================================
CREATE TABLE IF NOT EXISTS t_notice (
    id                      SERIAL          PRIMARY KEY,

    -- ==================== 分类 ====================
    notice_category         VARCHAR(30)     NOT NULL,
    notice_type             VARCHAR(50),

    -- ==================== 内容 ====================
    title                   VARCHAR(500)    NOT NULL,
    summary                 VARCHAR(500),
    content                 TEXT            NOT NULL,

    -- ==================== 标签/地区 ====================
    province                VARCHAR(30),
    city                    VARCHAR(50),
    tags                    TEXT[]          DEFAULT '{}',
    year                    VARCHAR(10),

    -- ==================== 来源信息 ====================
    source                  VARCHAR(200),
    source_url              VARCHAR(500),
    publish_date            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    publish_unit            VARCHAR(200),

    -- ==================== 关联报名时间 ====================
    reg_start_date          TIMESTAMPTZ,
    reg_end_date            TIMESTAMPTZ,
    exam_time               TIMESTAMPTZ,
    recruitment_count       INTEGER,

    -- ==================== 展示控制 ====================
    is_top                  BOOLEAN         DEFAULT FALSE,
    is_important            BOOLEAN         DEFAULT FALSE,
    view_count              INTEGER         DEFAULT 0,

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_notice_category CHECK (
        notice_category IN (
            'civil', 'institution', 'military', 'selection',
            'teacher', 'healthcare', 'finance', 'grassroots',
            'community', 'public_welfare', 'enterprise', 'general'
        )
    ),
    CONSTRAINT chk_notice_type CHECK (
        notice_type IS NULL OR notice_type IN (
            '招聘公告', '招录公告', '补录公告', '调剂公告',
            '成绩公示', '面试通知', '体检通知', '录用公示',
            '报名指南', '考试大纲', '政策解读'
        )
    ),
    CONSTRAINT chk_notice_count CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    )
);

CREATE INDEX idx_notice_category          ON t_notice (notice_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_type              ON t_notice (notice_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_cat_type          ON t_notice (notice_category, notice_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_publish_date      ON t_notice (publish_date DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_top_date          ON t_notice (is_top DESC, publish_date DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_province          ON t_notice (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_cat_province_year ON t_notice (notice_category, province, year) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_title             ON t_notice USING btree (title varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_tags              ON t_notice USING gin (tags) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_source            ON t_notice (source) WHERE is_deleted = FALSE;
CREATE INDEX idx_notice_year              ON t_notice (year) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_notice_updated_at
    BEFORE UPDATE ON t_notice
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON TABLE  t_notice                          IS '统一公告表（全平台招考/招聘公告）';
COMMENT ON COLUMN t_notice.notice_category          IS '公告类别：civil/institution/military/selection/teacher/healthcare/finance/grassroots/community/public_welfare/enterprise/general';
COMMENT ON COLUMN t_notice.notice_type              IS '公告类型（招聘公告/成绩公示/面试通知/政策解读等）';
COMMENT ON COLUMN t_notice.title                    IS '公告标题';
COMMENT ON COLUMN t_notice.summary                  IS '摘要（列表页展示）';
COMMENT ON COLUMN t_notice.content                  IS '公告内容（支持HTML格式）';
COMMENT ON COLUMN t_notice.province                 IS '省份';
COMMENT ON COLUMN t_notice.city                     IS '城市';
COMMENT ON COLUMN t_notice.tags                     IS '标签列表';
COMMENT ON COLUMN t_notice.year                     IS '年份';
COMMENT ON COLUMN t_notice.source                   IS '发布来源';
COMMENT ON COLUMN t_notice.source_url               IS '原文链接';
COMMENT ON COLUMN t_notice.publish_date             IS '发布日期';
COMMENT ON COLUMN t_notice.publish_unit             IS '发布单位';
COMMENT ON COLUMN t_notice.reg_start_date           IS '报名开始日期';
COMMENT ON COLUMN t_notice.reg_end_date             IS '报名结束日期';
COMMENT ON COLUMN t_notice.exam_time                IS '考试时间';
COMMENT ON COLUMN t_notice.recruitment_count        IS '本次招录总人数';
COMMENT ON COLUMN t_notice.is_top                   IS '是否置顶';
COMMENT ON COLUMN t_notice.is_important             IS '是否重要';
COMMENT ON COLUMN t_notice.view_count               IS '阅读量';

COMMIT;