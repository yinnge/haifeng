-- ============================================
-- V8__create_cities_industries.sql
-- 城市管理、行业管理、资源管理模块数据库表
-- ============================================

-- ============================================================
-- 1. 城市主表 (t_city)
-- 描述：城市基本信息，用于列表展示、筛选、排序
-- ============================================================

CREATE TABLE t_city (
    id                      BIGINT          PRIMARY KEY,
    city_name               VARCHAR(50)     NOT NULL,
    province                VARCHAR(30)     NOT NULL,
    region                  VARCHAR(20),
    city_intro              TEXT,
    college_count           INTEGER         DEFAULT 0,
    key_college_count       INTEGER         DEFAULT 0,
    resident_population     NUMERIC(8, 2),
    gdp                     NUMERIC(10, 2),
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
    -- 唯一约束见下方部分唯一索引 uk_city_name_active
);

-- 索引
CREATE INDEX idx_city_province ON t_city (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_region ON t_city (region) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_gdp ON t_city (gdp DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_population ON t_city (resident_population DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uk_city_name_active ON t_city(city_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_name_search ON t_city USING btree (city_name varchar_pattern_ops) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_city_updated_at
    BEFORE UPDATE ON t_city
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_city IS '城市基本信息表';
COMMENT ON COLUMN t_city.id IS '城市ID（雪花算法）';
COMMENT ON COLUMN t_city.city_name IS '城市名称';
COMMENT ON COLUMN t_city.province IS '所属省份';
COMMENT ON COLUMN t_city.region IS '所属地区（华东/华南/华北/华中/东北/西南/西北/港澳台）';
COMMENT ON COLUMN t_city.city_intro IS '城市简介';
COMMENT ON COLUMN t_city.college_count IS '高校数量';
COMMENT ON COLUMN t_city.key_college_count IS '重点高校数量（985/211/双一流）';
COMMENT ON COLUMN t_city.resident_population IS '常住人口（万人）';
COMMENT ON COLUMN t_city.gdp IS 'GDP（亿元）';
COMMENT ON COLUMN t_city.is_deleted IS '是否删除';
COMMENT ON COLUMN t_city.created_at IS '创建时间';
COMMENT ON COLUMN t_city.updated_at IS '更新时间';


-- ============================================================
-- 2. 城市详情表 (t_city_detail)
-- 描述：与 t_city 一对一，存储城市各维度详细数据
-- ============================================================

CREATE TABLE t_city_detail (
    id                      BIGINT          PRIMARY KEY,
    city_id                 BIGINT          NOT NULL,
    city_name               VARCHAR(50)     NOT NULL,
    area                    NUMERIC(10, 2),
    subtitle                VARCHAR(200),
    city_level              VARCHAR(20),
    admin_code              VARCHAR(20),
    per_capita_gdp          NUMERIC(8, 2),
    urbanization_rate       NUMERIC(5, 2),
    rural_pop_ratio         NUMERIC(5, 2),
    aging_rate              NUMERIC(5, 2),
    migrant_pop_ratio       NUMERIC(5, 2),
    gdp_growth_rate         NUMERIC(5, 2),
    fortune_500_count       INTEGER         DEFAULT 0,
    industry_structure      JSONB           DEFAULT '{}'::JSONB,
    industry_description    TEXT,
    main_industries         TEXT[]          DEFAULT '{}',
    emerging_industries     TEXT[]          DEFAULT '{}',
    future_plan             JSONB           DEFAULT '{}'::JSONB,
    high_education          JSONB           DEFAULT '{}'::JSONB,
    basic_education         JSONB           DEFAULT '{}'::JSONB,
    enterprise_stats        JSONB           DEFAULT '{}'::JSONB,
    housing_price_level     JSONB           DEFAULT '{}'::JSONB,
    rental_cost             JSONB           DEFAULT '{}'::JSONB,
    housing_policy          JSONB           DEFAULT '{}'::JSONB,
    consumption             JSONB           DEFAULT '{}'::JSONB,
    employment              JSONB           DEFAULT '{}'::JSONB,
    transportation          JSONB           DEFAULT '{}'::JSONB,
    medical                 JSONB           DEFAULT '{}'::JSONB,
    culture                 JSONB           DEFAULT '{}'::JSONB,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_city_detail_city_id UNIQUE (city_id),
    CONSTRAINT chk_city_level CHECK (
        city_level IS NULL OR city_level IN ('直辖市','省会城市','地级市','县级市')
    ),
    CONSTRAINT chk_urbanization_rate CHECK (
        urbanization_rate IS NULL OR (urbanization_rate >= 0 AND urbanization_rate <= 100)
    ),
    CONSTRAINT chk_rural_pop_ratio CHECK (
        rural_pop_ratio IS NULL OR (rural_pop_ratio >= 0 AND rural_pop_ratio <= 100)
    ),
    CONSTRAINT chk_aging_rate CHECK (
        aging_rate IS NULL OR (aging_rate >= 0 AND aging_rate <= 100)
    ),
    CONSTRAINT chk_migrant_pop_ratio CHECK (
        migrant_pop_ratio IS NULL OR (migrant_pop_ratio >= 0 AND migrant_pop_ratio <= 100)
    )
);

-- 索引
CREATE INDEX idx_city_detail_level ON t_city_detail (city_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_detail_per_capita_gdp ON t_city_detail (per_capita_gdp DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_detail_gdp_growth ON t_city_detail (gdp_growth_rate DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_detail_industries ON t_city_detail USING gin (main_industries) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_city_detail_updated_at
    BEFORE UPDATE ON t_city_detail
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_city_detail IS '城市详情表：与 t_city 一对一，存储各维度详细数据';
COMMENT ON COLUMN t_city_detail.id IS '详情ID（雪花算法）';
COMMENT ON COLUMN t_city_detail.city_id IS '关联城市表ID（一对一）';
COMMENT ON COLUMN t_city_detail.city_name IS '城市名称（冗余字段）';
COMMENT ON COLUMN t_city_detail.area IS '面积（平方公里）';
COMMENT ON COLUMN t_city_detail.subtitle IS '副标题';
COMMENT ON COLUMN t_city_detail.city_level IS '城市级别（直辖市/省会城市/地级市/县级市）';
COMMENT ON COLUMN t_city_detail.admin_code IS '行政区划代码';
COMMENT ON COLUMN t_city_detail.per_capita_gdp IS '人均GDP（万元）';
COMMENT ON COLUMN t_city_detail.urbanization_rate IS '城镇化率（%）';
COMMENT ON COLUMN t_city_detail.rural_pop_ratio IS '农村人口比例（%）';
COMMENT ON COLUMN t_city_detail.aging_rate IS '老龄化率（%）';
COMMENT ON COLUMN t_city_detail.migrant_pop_ratio IS '外来人口比例（%）';
COMMENT ON COLUMN t_city_detail.gdp_growth_rate IS 'GDP增长率（%）';
COMMENT ON COLUMN t_city_detail.fortune_500_count IS '世界500强企业数量';
COMMENT ON COLUMN t_city_detail.industry_structure IS '产业结构占比（JSONB）';
COMMENT ON COLUMN t_city_detail.industry_description IS '产业描述';
COMMENT ON COLUMN t_city_detail.main_industries IS '主要产业列表';
COMMENT ON COLUMN t_city_detail.emerging_industries IS '新兴产业列表';
COMMENT ON COLUMN t_city_detail.future_plan IS '未来规划（JSONB）';
COMMENT ON COLUMN t_city_detail.high_education IS '高等教育资源（JSONB）';
COMMENT ON COLUMN t_city_detail.basic_education IS '基础教育资源（JSONB）';
COMMENT ON COLUMN t_city_detail.enterprise_stats IS '企业统计（JSONB）';
COMMENT ON COLUMN t_city_detail.housing_price_level IS '房价水平（JSONB）';
COMMENT ON COLUMN t_city_detail.rental_cost IS '租房成本（JSONB）';
COMMENT ON COLUMN t_city_detail.housing_policy IS '住房政策（JSONB）';
COMMENT ON COLUMN t_city_detail.consumption IS '消费数据（JSONB）';
COMMENT ON COLUMN t_city_detail.employment IS '就业数据（JSONB）';
COMMENT ON COLUMN t_city_detail.transportation IS '交通数据（JSONB）';
COMMENT ON COLUMN t_city_detail.medical IS '医疗数据（JSONB）';
COMMENT ON COLUMN t_city_detail.culture IS '文化旅游数据（JSONB）';
COMMENT ON COLUMN t_city_detail.is_deleted IS '是否删除';
COMMENT ON COLUMN t_city_detail.created_at IS '创建时间';
COMMENT ON COLUMN t_city_detail.updated_at IS '更新时间';


-- ============================================================
-- 3. 行业主表 (t_industry)
-- 描述：行业基本信息，用于列表展示、筛选
-- ============================================================

CREATE TABLE t_industry (
    id                      BIGINT          PRIMARY KEY,
    industry_name           VARCHAR(100)    NOT NULL,
    category                VARCHAR(50),
    icon_class              VARCHAR(100),
    description             TEXT,
    annual_growth_rate      NUMERIC(5, 2),
    market_scale            VARCHAR(50),
    talent_gap              VARCHAR(50),
    investment_heat         NUMERIC(5, 2),
    growth_trend            VARCHAR(10),
    market_trend            VARCHAR(10),
    talent_trend            VARCHAR(10),
    investment_trend        VARCHAR(10),
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_industry_name UNIQUE (industry_name),
    CONSTRAINT chk_growth_trend CHECK (growth_trend IS NULL OR growth_trend IN ('上升','稳定','下降')),
    CONSTRAINT chk_market_trend CHECK (market_trend IS NULL OR market_trend IN ('上升','稳定','下降')),
    CONSTRAINT chk_talent_trend CHECK (talent_trend IS NULL OR talent_trend IN ('上升','稳定','下降')),
    CONSTRAINT chk_investment_trend CHECK (investment_trend IS NULL OR investment_trend IN ('上升','稳定','下降')),
    CONSTRAINT chk_annual_growth CHECK (annual_growth_rate IS NULL OR annual_growth_rate BETWEEN -100 AND 1000),
    CONSTRAINT chk_investment_heat CHECK (investment_heat IS NULL OR investment_heat BETWEEN 0 AND 100)
);

-- 索引
CREATE INDEX idx_industry_category ON t_industry (category) WHERE is_deleted = FALSE;
CREATE INDEX idx_industry_growth ON t_industry (annual_growth_rate DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_industry_name_search ON t_industry USING btree (industry_name varchar_pattern_ops) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_industry_updated_at
    BEFORE UPDATE ON t_industry
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_industry IS '行业基本信息表';
COMMENT ON COLUMN t_industry.id IS '行业ID（雪花算法）';
COMMENT ON COLUMN t_industry.industry_name IS '行业名称';
COMMENT ON COLUMN t_industry.category IS '行业分类（如：信息技术、金融、制造业）';
COMMENT ON COLUMN t_industry.icon_class IS '图标CSS类名（Font Awesome，如：fa-solid fa-microchip）';
COMMENT ON COLUMN t_industry.description IS '行业描述';
COMMENT ON COLUMN t_industry.annual_growth_rate IS '年增长率（%）';
COMMENT ON COLUMN t_industry.market_scale IS '市场规模（如：1.8万亿）';
COMMENT ON COLUMN t_industry.talent_gap IS '人才缺口（如：120万）';
COMMENT ON COLUMN t_industry.investment_heat IS '投资热度（%）';
COMMENT ON COLUMN t_industry.growth_trend IS '增长趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.market_trend IS '市场趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.talent_trend IS '人才趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.investment_trend IS '投资趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.is_deleted IS '是否删除';
COMMENT ON COLUMN t_industry.created_at IS '创建时间';
COMMENT ON COLUMN t_industry.updated_at IS '更新时间';


-- ============================================================
-- 4. 行业详情表 (t_industry_detail)
-- 描述：与 t_industry 一对一，存储行业深度分析数据
-- ============================================================

CREATE TABLE t_industry_detail (
    id                          BIGINT          PRIMARY KEY,
    industry_id                 BIGINT          NOT NULL,
    industry_name               VARCHAR(100)    NOT NULL,
    short_description           VARCHAR(500),
    detailed_description        TEXT,
    industry_scale              JSONB           DEFAULT '{}'::JSONB,
    industry_talent_demand      JSONB           DEFAULT '{}'::JSONB,
    industry_salary             JSONB           DEFAULT '{}'::JSONB,
    policy_info                 JSONB           DEFAULT '{}'::JSONB,
    development_support_info    JSONB           DEFAULT '{}'::JSONB,
    talent_analysis             JSONB           DEFAULT '{}'::JSONB,
    talent_policy               JSONB           DEFAULT '{}'::JSONB,
    salary_data                 JSONB           DEFAULT '{}'::JSONB,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_industry_detail_id UNIQUE (industry_id)
);

-- 索引
CREATE INDEX idx_industry_detail_name ON t_industry_detail (industry_name) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_industry_detail_updated_at
    BEFORE UPDATE ON t_industry_detail
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_industry_detail IS '行业详情表：与 t_industry 一对一';
COMMENT ON COLUMN t_industry_detail.id IS '详情ID（雪花算法）';
COMMENT ON COLUMN t_industry_detail.industry_id IS '关联行业表ID';
COMMENT ON COLUMN t_industry_detail.industry_name IS '行业名称（冗余字段）';
COMMENT ON COLUMN t_industry_detail.short_description IS '简短描述';
COMMENT ON COLUMN t_industry_detail.detailed_description IS '详细描述';
COMMENT ON COLUMN t_industry_detail.industry_scale IS '发展规模（JSONB）';
COMMENT ON COLUMN t_industry_detail.industry_talent_demand IS '人才需求（JSONB）';
COMMENT ON COLUMN t_industry_detail.industry_salary IS '行业薪资（JSONB）';
COMMENT ON COLUMN t_industry_detail.policy_info IS '政策信息（JSONB）';
COMMENT ON COLUMN t_industry_detail.development_support_info IS '发展地域与城市支持（JSONB）';
COMMENT ON COLUMN t_industry_detail.talent_analysis IS '人才需求分析（JSONB）';
COMMENT ON COLUMN t_industry_detail.talent_policy IS '人才政策（JSONB）';
COMMENT ON COLUMN t_industry_detail.salary_data IS '薪资数据（JSONB），含分布与分析';
COMMENT ON COLUMN t_industry_detail.is_deleted IS '是否删除';
COMMENT ON COLUMN t_industry_detail.created_at IS '创建时间';
COMMENT ON COLUMN t_industry_detail.updated_at IS '更新时间';


-- ============================================================
-- 5. 资源表 (t_resource)
-- 描述：学习资源（真题/教材/视频等），链接指向百度网盘
-- ============================================================

CREATE TABLE t_resource (
    id                      BIGINT          PRIMARY KEY,
    resource_name           VARCHAR(200)    NOT NULL,
    cover_url               VARCHAR(500),
    description             TEXT,
    resource_url            VARCHAR(500)    NOT NULL,
    access_code             VARCHAR(20),
    category                VARCHAR(50),
    file_type               VARCHAR(20),
    view_count              INTEGER         DEFAULT 0,
    sort_order              INTEGER         DEFAULT 0,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_view_count CHECK (view_count >= 0)
);

-- 索引
CREATE INDEX idx_resource_category ON t_resource (category) WHERE is_deleted = FALSE;
CREATE INDEX idx_resource_sort ON t_resource (sort_order, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_resource_name_search ON t_resource USING btree (resource_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_resource_view_count ON t_resource (view_count DESC) WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_resource_updated_at
    BEFORE UPDATE ON t_resource
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE t_resource IS '学习资源表（百度网盘链接）';
COMMENT ON COLUMN t_resource.id IS '资源ID（雪花算法）';
COMMENT ON COLUMN t_resource.resource_name IS '资源名称';
COMMENT ON COLUMN t_resource.cover_url IS '封面图URL';
COMMENT ON COLUMN t_resource.description IS '资源描述';
COMMENT ON COLUMN t_resource.resource_url IS '资源链接（百度网盘地址）';
COMMENT ON COLUMN t_resource.access_code IS '百度网盘提取码';
COMMENT ON COLUMN t_resource.category IS '分类（考研真题/四六级/公务员/专业课）';
COMMENT ON COLUMN t_resource.file_type IS '文件类型（PDF/视频/压缩包）';
COMMENT ON COLUMN t_resource.view_count IS '浏览统计（点击+1）';
COMMENT ON COLUMN t_resource.sort_order IS '排序权重';
COMMENT ON COLUMN t_resource.is_deleted IS '是否删除';
COMMENT ON COLUMN t_resource.created_at IS '创建时间';
COMMENT ON COLUMN t_resource.updated_at IS '更新时间';
