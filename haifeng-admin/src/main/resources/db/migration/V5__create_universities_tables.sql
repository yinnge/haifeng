-- ============================================
-- V5__create_universities_tables.sql
-- 院校管理模块数据库表
-- ============================================

-- 院校主表
CREATE TABLE universities (
    id                  BIGINT        PRIMARY KEY,
    name                VARCHAR(50)   NOT NULL,
    name_en             VARCHAR(50)   NOT NULL,
    province_name       VARCHAR(50)   NOT NULL,
    city_name           VARCHAR(50)   NOT NULL,
    region              VARCHAR(50)   NOT NULL,
    category            VARCHAR(50)   NOT NULL,
    major_count         INTEGER       DEFAULT 0,
    education_level     VARCHAR(50),
    nature              VARCHAR(50),
    recommendation_rate DECIMAL(5,2),
    recommendation_year INTEGER,
    has_doctorate       BOOLEAN       DEFAULT false,
    has_master          BOOLEAN       DEFAULT false,
    department          VARCHAR(100),
    tags                TEXT[],
    famous_union        VARCHAR(50),
    image_url           VARCHAR(500),
    introduction        TEXT,
    sort_order          INTEGER       DEFAULT 0,
    status              SMALLINT      DEFAULT 1 NOT NULL,
    created_at          TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 院校主表索引
CREATE INDEX idx_univ_name ON universities(name);
CREATE INDEX idx_univ_province ON universities(province_name);
CREATE INDEX idx_univ_category ON universities(category);
CREATE INDEX idx_univ_status ON universities(status);
CREATE INDEX idx_univ_tags ON universities USING GIN(tags);
CREATE UNIQUE INDEX idx_univ_name_unique ON universities(name) WHERE status = 1;

-- 院校主表注释
COMMENT ON TABLE universities IS '院校主表';
COMMENT ON COLUMN universities.id IS '院校ID（雪花算法）';
COMMENT ON COLUMN universities.name IS '院校名称';
COMMENT ON COLUMN universities.name_en IS '院校英文名称';
COMMENT ON COLUMN universities.province_name IS '省份';
COMMENT ON COLUMN universities.city_name IS '城市';
COMMENT ON COLUMN universities.region IS '所属地区';
COMMENT ON COLUMN universities.category IS '院校类别（综合/理工/师范等）';
COMMENT ON COLUMN universities.major_count IS '专业数量';
COMMENT ON COLUMN universities.education_level IS '办学层次（本科/专科/本专兼招）';
COMMENT ON COLUMN universities.nature IS '院校性质（公办/民办/中外合作）';
COMMENT ON COLUMN universities.recommendation_rate IS '推免率（百分比）';
COMMENT ON COLUMN universities.recommendation_year IS '推免年份';
COMMENT ON COLUMN universities.has_doctorate IS '是否有博士点';
COMMENT ON COLUMN universities.has_master IS '是否有硕士点';
COMMENT ON COLUMN universities.department IS '隶属部门';
COMMENT ON COLUMN universities.tags IS '院校标签数组';
COMMENT ON COLUMN universities.famous_union IS '知名联盟';
COMMENT ON COLUMN universities.image_url IS '院校图片URL';
COMMENT ON COLUMN universities.introduction IS '院校简介';
COMMENT ON COLUMN universities.sort_order IS '排序权重';
COMMENT ON COLUMN universities.status IS '状态: 0-下架 1-展示';

-- 院校详情表
CREATE TABLE universities_detail (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL UNIQUE,
    address               VARCHAR(200),
    admission_phone       VARCHAR(50),
    website               VARCHAR(500),
    history_group_score   INTEGER,
    science_group_score   INTEGER,
    carousel_images       TEXT[],
    introduction          TEXT,
    rankings              JSONB         DEFAULT '{}'::JSONB,
    abroad_rate           VARCHAR(10),
    gender_ratio          VARCHAR(10),
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 院校详情表索引
CREATE INDEX idx_univ_detail_university_id ON universities_detail(university_id);

-- 院校详情表注释
COMMENT ON TABLE universities_detail IS '院校详情表（与院校主表1:1）';
COMMENT ON COLUMN universities_detail.university_id IS '关联院校ID';
COMMENT ON COLUMN universities_detail.address IS '学校地址';
COMMENT ON COLUMN universities_detail.admission_phone IS '招生电话';
COMMENT ON COLUMN universities_detail.website IS '官方网站';
COMMENT ON COLUMN universities_detail.history_group_score IS '本科批历史组分数线';
COMMENT ON COLUMN universities_detail.science_group_score IS '本科批物理组分数线';
COMMENT ON COLUMN universities_detail.carousel_images IS '轮播图片URL数组';
COMMENT ON COLUMN universities_detail.introduction IS '院校详细介绍';
COMMENT ON COLUMN universities_detail.rankings IS '排名信息JSONB（软科/校友会/武书连/QS/USNEWS）';
COMMENT ON COLUMN universities_detail.abroad_rate IS '出国比例';
COMMENT ON COLUMN universities_detail.gender_ratio IS '男女比例';

-- 校园图册表
CREATE TABLE t_campus_gallery (
    id                  BIGINT          PRIMARY KEY,
    university_id       BIGINT          NOT NULL,
    university_name     VARCHAR(50)     NOT NULL,
    image_type          VARCHAR(30)     NOT NULL,
    image_url           VARCHAR(500)    NOT NULL,
    sort_order          INTEGER         DEFAULT 0,
    status              SMALLINT        DEFAULT 1 NOT NULL,
    created_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 校园图册表索引
CREATE INDEX idx_gallery_university_id ON t_campus_gallery(university_id);
CREATE INDEX idx_gallery_type ON t_campus_gallery(image_type);
CREATE INDEX idx_gallery_status ON t_campus_gallery(status);

-- 校园图册表注释
COMMENT ON TABLE t_campus_gallery IS '校园图册表（与院校主表1:N）';
COMMENT ON COLUMN t_campus_gallery.university_id IS '关联院校ID';
COMMENT ON COLUMN t_campus_gallery.university_name IS '院校名称（冗余字段）';
COMMENT ON COLUMN t_campus_gallery.image_type IS '图片类型（教学楼/宿舍/食堂等）';
COMMENT ON COLUMN t_campus_gallery.image_url IS '图片URL';
COMMENT ON COLUMN t_campus_gallery.sort_order IS '排序权重';

-- 院校适应指南表
CREATE TABLE university_guides (
    id                          BIGINT        PRIMARY KEY,
    university_id               BIGINT        NOT NULL UNIQUE,
    custom_tags                 TEXT[],
    campus_facilities           JSONB         DEFAULT '{}'::JSONB,
    dormitory_services          JSONB         DEFAULT '{}'::JSONB,
    campus_transportation       JSONB         DEFAULT '{}'::JSONB,
    academic_guidance           JSONB         DEFAULT '{}'::JSONB,
    major_transfer_guidelines   JSONB         DEFAULT '{}'::JSONB,
    major_transfer_constriction JSONB         DEFAULT '{}'::JSONB,
    academic_support_resources  JSONB         DEFAULT '{}'::JSONB,
    student_organizations       JSONB         DEFAULT '{}'::JSONB,
    campus_events               JSONB         DEFAULT '{}'::JSONB,
    class_dorm_social           JSONB         DEFAULT '{}'::JSONB,
    financial_aid               JSONB         DEFAULT '{}'::JSONB,
    campus_security             JSONB         DEFAULT '{}'::JSONB,
    health_services             JSONB         DEFAULT '{}'::JSONB,
    life_services               JSONB         DEFAULT '{}'::JSONB,
    remark                      TEXT,
    status                      SMALLINT      DEFAULT 1 NOT NULL,
    created_at                  TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at                  TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 院校适应指南表索引
CREATE INDEX idx_guides_university_id ON university_guides(university_id);
CREATE INDEX idx_guides_status ON university_guides(status);

-- 院校适应指南表注释
COMMENT ON TABLE university_guides IS '院校适应指南表（与院校主表1:1）';
COMMENT ON COLUMN university_guides.university_id IS '关联院校ID';
COMMENT ON COLUMN university_guides.custom_tags IS '自定义标签数组';
COMMENT ON COLUMN university_guides.campus_facilities IS '校园设施JSONB';
COMMENT ON COLUMN university_guides.dormitory_services IS '水电网与宿舍管理JSONB';
COMMENT ON COLUMN university_guides.campus_transportation IS '校园通勤与校外交通JSONB';
COMMENT ON COLUMN university_guides.academic_guidance IS '专业与课程核心信息JSONB';
COMMENT ON COLUMN university_guides.major_transfer_guidelines IS '转专业原则JSONB';
COMMENT ON COLUMN university_guides.major_transfer_constriction IS '转专业限制JSONB';
COMMENT ON COLUMN university_guides.academic_support_resources IS '学习支持资源JSONB';
COMMENT ON COLUMN university_guides.student_organizations IS '学生组织与社团JSONB';
COMMENT ON COLUMN university_guides.campus_events IS '校园活动与竞赛JSONB';
COMMENT ON COLUMN university_guides.class_dorm_social IS '班级与宿舍社交JSONB';
COMMENT ON COLUMN university_guides.financial_aid IS '奖助勤贷与权益保障JSONB';
COMMENT ON COLUMN university_guides.campus_security IS '校园安全与应急处理JSONB';
COMMENT ON COLUMN university_guides.health_services IS '医保与心理健康JSONB';
COMMENT ON COLUMN university_guides.life_services IS '生活服务JSONB';
COMMENT ON COLUMN university_guides.remark IS '备注';
