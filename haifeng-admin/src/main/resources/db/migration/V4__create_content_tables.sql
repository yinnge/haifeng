-- V4__create_content_tables.sql
-- 海峰未来规划院 - 首页管理模块（公告、规划师、培训机构）

-- 1. 公告表
CREATE TABLE t_announcements (
    id                  BIGINT PRIMARY KEY,
    title               VARCHAR(100) NOT NULL,
    content             TEXT NOT NULL,
    tag                 VARCHAR(20),
    status              SMALLINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_announcements_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_announcements_title ON t_announcements(title) WHERE is_deleted = FALSE;
CREATE INDEX idx_announcements_status ON t_announcements(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_announcements IS '公告表';
COMMENT ON COLUMN t_announcements.id IS '公告ID(雪花算法)';
COMMENT ON COLUMN t_announcements.title IS '公告标题';
COMMENT ON COLUMN t_announcements.content IS '公告内容';
COMMENT ON COLUMN t_announcements.tag IS '公告标签';
COMMENT ON COLUMN t_announcements.status IS '状态: 0-下架, 1-展示';
COMMENT ON COLUMN t_announcements.is_deleted IS '是否删除';
COMMENT ON COLUMN t_announcements.created_at IS '创建时间';
COMMENT ON COLUMN t_announcements.updated_at IS '更新时间';

-- 2. 规划师表
CREATE TABLE t_planners (
    id                      BIGINT PRIMARY KEY,
    name                    VARCHAR(50) NOT NULL,
    position                VARCHAR(50),
    region                  VARCHAR(20),
    avatar                  VARCHAR(100),
    specialty               VARCHAR(100),
    douyin_name             VARCHAR(100),
    douyin_url              VARCHAR(100),
    personal_description    TEXT,
    experience_job          TEXT,
    achievements            TEXT[],
    expertise_areas         TEXT[],
    sort_order              INTEGER NOT NULL DEFAULT 0,
    status                  SMALLINT NOT NULL DEFAULT 1,
    is_deleted              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_planners_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_planners_name ON t_planners(name) WHERE is_deleted = FALSE;
CREATE INDEX idx_planners_status ON t_planners(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_planners_sort ON t_planners(sort_order) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_planners IS '规划师表';
COMMENT ON COLUMN t_planners.id IS '规划师ID(雪花算法)';
COMMENT ON COLUMN t_planners.name IS '规划师姓名';
COMMENT ON COLUMN t_planners.position IS '职位';
COMMENT ON COLUMN t_planners.region IS '所在地区';
COMMENT ON COLUMN t_planners.avatar IS '头像URL';
COMMENT ON COLUMN t_planners.specialty IS '专业特长';
COMMENT ON COLUMN t_planners.douyin_name IS '抖音名称';
COMMENT ON COLUMN t_planners.douyin_url IS '抖音链接';
COMMENT ON COLUMN t_planners.personal_description IS '个人简介';
COMMENT ON COLUMN t_planners.experience_job IS '工作经历';
COMMENT ON COLUMN t_planners.achievements IS '成就列表(数组)';
COMMENT ON COLUMN t_planners.expertise_areas IS '擅长领域(数组)';
COMMENT ON COLUMN t_planners.sort_order IS '排序值(越小越靠前)';
COMMENT ON COLUMN t_planners.status IS '状态: 0-下架, 1-展示';
COMMENT ON COLUMN t_planners.is_deleted IS '是否删除';
COMMENT ON COLUMN t_planners.created_at IS '创建时间';
COMMENT ON COLUMN t_planners.updated_at IS '更新时间';

-- 3. 培训机构表
CREATE TABLE t_institutions (
    id                  BIGINT PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    address             VARCHAR(100),
    description         TEXT,
    courses             TEXT[],
    images              TEXT[],
    logo                VARCHAR(200),
    sort_order          INTEGER NOT NULL DEFAULT 0,
    status              SMALLINT NOT NULL DEFAULT 1,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_institutions_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_institutions_name ON t_institutions(name) WHERE is_deleted = FALSE;
CREATE INDEX idx_institutions_type ON t_institutions(type) WHERE is_deleted = FALSE;
CREATE INDEX idx_institutions_status ON t_institutions(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_institutions_sort ON t_institutions(sort_order) WHERE is_deleted = FALSE;

COMMENT ON TABLE t_institutions IS '培训机构表';
COMMENT ON COLUMN t_institutions.id IS '机构ID(雪花算法)';
COMMENT ON COLUMN t_institutions.name IS '机构名称';
COMMENT ON COLUMN t_institutions.type IS '机构类型';
COMMENT ON COLUMN t_institutions.phone IS '联系电话';
COMMENT ON COLUMN t_institutions.address IS '机构地址';
COMMENT ON COLUMN t_institutions.description IS '机构简介';
COMMENT ON COLUMN t_institutions.courses IS '课程列表(数组)';
COMMENT ON COLUMN t_institutions.images IS '机构图片(数组)';
COMMENT ON COLUMN t_institutions.logo IS '机构Logo URL';
COMMENT ON COLUMN t_institutions.sort_order IS '排序值(越小越靠前)';
COMMENT ON COLUMN t_institutions.status IS '状态: 0-下架, 1-展示';
COMMENT ON COLUMN t_institutions.is_deleted IS '是否删除';
COMMENT ON COLUMN t_institutions.created_at IS '创建时间';
COMMENT ON COLUMN t_institutions.updated_at IS '更新时间';
