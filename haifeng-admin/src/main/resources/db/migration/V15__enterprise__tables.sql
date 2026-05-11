-- V15__enterprise__tables.sql
-- 企业管理模块数据库表

BEGIN;

-- ============================================================
-- 1. 企业表 (t_enterprise)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_enterprise (
    id                  BIGINT          PRIMARY KEY,
    city_name           VARCHAR(50),
    enterprise_name     VARCHAR(200)    NOT NULL UNIQUE,
    enterprise_nature   VARCHAR(30)     NOT NULL,
    enterprise_type     VARCHAR(50),
    logo_url            VARCHAR(500),
    official_website    VARCHAR(500),
    region              VARCHAR(100),
    enterprise_scale    VARCHAR(50),
    main_business       VARCHAR(500),
    enterprise_intro    TEXT,
    recruitment_status  VARCHAR(20)     DEFAULT '招聘中',
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ent_nature CHECK (enterprise_nature IS NULL OR enterprise_nature IN ('央企', '国企', '民企', '外企', '合资'))
);

COMMENT ON TABLE t_enterprise IS '企业表';
COMMENT ON COLUMN t_enterprise.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_enterprise.city_name IS '城市名称';
COMMENT ON COLUMN t_enterprise.enterprise_name IS '企业名称';
COMMENT ON COLUMN t_enterprise.enterprise_nature IS '企业性质: 央企/国企/民企/外企/合资';
COMMENT ON COLUMN t_enterprise.enterprise_type IS '企业类型';
COMMENT ON COLUMN t_enterprise.logo_url IS '企业LOGO地址';
COMMENT ON COLUMN t_enterprise.official_website IS '官方网站';
COMMENT ON COLUMN t_enterprise.region IS '所在地区';
COMMENT ON COLUMN t_enterprise.enterprise_scale IS '企业规模';
COMMENT ON COLUMN t_enterprise.main_business IS '主营业务';
COMMENT ON COLUMN t_enterprise.enterprise_intro IS '企业简介';
COMMENT ON COLUMN t_enterprise.recruitment_status IS '招聘状态';
COMMENT ON COLUMN t_enterprise.is_deleted IS '是否删除';
COMMENT ON COLUMN t_enterprise.created_at IS '创建时间';
COMMENT ON COLUMN t_enterprise.updated_at IS '更新时间';

CREATE INDEX idx_ent_city_nature_type ON t_enterprise(city_name, enterprise_nature, enterprise_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_ent_nature ON t_enterprise(enterprise_nature) WHERE is_deleted = FALSE;
CREATE INDEX idx_ent_name_pattern ON t_enterprise USING btree (enterprise_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_ent_region ON t_enterprise(region) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_enterprise_updated_at
    BEFORE UPDATE ON t_enterprise
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ============================================================
-- 2. 企业岗位表 (t_enterprise_position)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_enterprise_position (
    id                      BIGINT          PRIMARY KEY,
    enterprise_id           BIGINT          NOT NULL,
    position_name           VARCHAR(200)    NOT NULL,
    recruitment_type        VARCHAR(30),
    position_requirement    TEXT,
    position_tags           TEXT[]          DEFAULT '{}',
    province                VARCHAR(30),
    city                    VARCHAR(50),
    work_location           VARCHAR(200),
    education_requirement   VARCHAR(30),
    major_requirement       VARCHAR(500),
    work_experience         VARCHAR(50),
    salary_min              INTEGER,
    salary_max              INTEGER,
    apply_link              VARCHAR(500),
    deadline                TIMESTAMPTZ,
    position_status         VARCHAR(20)     DEFAULT '招聘中',
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ep_recruitment_type CHECK (recruitment_type IS NULL OR recruitment_type IN ('校招', '社招', '实习')),
    CONSTRAINT chk_ep_education CHECK (education_requirement IS NULL OR education_requirement IN ('不限', '大专', '本科', '硕士', '博士')),
    CONSTRAINT chk_ep_position_status CHECK (position_status IS NULL OR position_status IN ('招聘中', '已结束')),
    CONSTRAINT chk_ep_salary_range CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

COMMENT ON TABLE t_enterprise_position IS '企业岗位表';
COMMENT ON COLUMN t_enterprise_position.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_enterprise_position.enterprise_id IS '企业ID(逻辑外键)';
COMMENT ON COLUMN t_enterprise_position.position_name IS '岗位名称';
COMMENT ON COLUMN t_enterprise_position.recruitment_type IS '招聘类型: 校招/社招/实习';
COMMENT ON COLUMN t_enterprise_position.position_requirement IS '岗位要求';
COMMENT ON COLUMN t_enterprise_position.position_tags IS '岗位标签数组';
COMMENT ON COLUMN t_enterprise_position.province IS '省份';
COMMENT ON COLUMN t_enterprise_position.city IS '城市';
COMMENT ON COLUMN t_enterprise_position.work_location IS '工作地点详情';
COMMENT ON COLUMN t_enterprise_position.education_requirement IS '学历要求: 不限/大专/本科/硕士/博士';
COMMENT ON COLUMN t_enterprise_position.major_requirement IS '专业要求';
COMMENT ON COLUMN t_enterprise_position.work_experience IS '工作经验要求';
COMMENT ON COLUMN t_enterprise_position.salary_min IS '最低薪资(元/月)';
COMMENT ON COLUMN t_enterprise_position.salary_max IS '最高薪资(元/月)';
COMMENT ON COLUMN t_enterprise_position.apply_link IS '申请链接';
COMMENT ON COLUMN t_enterprise_position.deadline IS '截止日期';
COMMENT ON COLUMN t_enterprise_position.position_status IS '岗位状态: 招聘中/已结束';
COMMENT ON COLUMN t_enterprise_position.is_deleted IS '是否删除';
COMMENT ON COLUMN t_enterprise_position.created_at IS '创建时间';
COMMENT ON COLUMN t_enterprise_position.updated_at IS '更新时间';

CREATE INDEX idx_ep_enterprise ON t_enterprise_position(enterprise_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_name_pattern ON t_enterprise_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_recruitment_type ON t_enterprise_position(recruitment_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_location ON t_enterprise_position(province, city) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_education ON t_enterprise_position(education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_deadline ON t_enterprise_position(deadline) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_tags ON t_enterprise_position USING GIN (position_tags) WHERE is_deleted = FALSE;
CREATE INDEX idx_ep_salary ON t_enterprise_position(salary_min, salary_max) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_enterprise_position_updated_at
    BEFORE UPDATE ON t_enterprise_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ============================================================
-- 3. 企业-行业关联表 (t_enterprise_industry)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_enterprise_industry (
    id                  BIGINT          PRIMARY KEY,
    enterprise_id       BIGINT          NOT NULL,
    enterprise_name     VARCHAR(200)    NOT NULL,
    industry_id         BIGINT          NOT NULL,
    industry_name       VARCHAR(100)    NOT NULL,
    is_primary          BOOLEAN         NOT NULL DEFAULT FALSE,
    sort_order          SMALLINT        NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_enterprise_industry UNIQUE (enterprise_id, industry_id)
);

COMMENT ON TABLE t_enterprise_industry IS '企业-行业关联表';
COMMENT ON COLUMN t_enterprise_industry.id IS '主键ID(雪花算法)';
COMMENT ON COLUMN t_enterprise_industry.enterprise_id IS '企业ID';
COMMENT ON COLUMN t_enterprise_industry.enterprise_name IS '企业名称(冗余)';
COMMENT ON COLUMN t_enterprise_industry.industry_id IS '行业ID';
COMMENT ON COLUMN t_enterprise_industry.industry_name IS '行业名称(冗余)';
COMMENT ON COLUMN t_enterprise_industry.is_primary IS '是否主要行业';
COMMENT ON COLUMN t_enterprise_industry.sort_order IS '排序权重';
COMMENT ON COLUMN t_enterprise_industry.created_at IS '创建时间';

CREATE INDEX idx_ei_enterprise ON t_enterprise_industry(enterprise_id);
CREATE INDEX idx_ei_industry ON t_enterprise_industry(industry_id);

COMMIT;
