## 一、城市表


```
-- ============================================================
-- 城市表 (t_city)
-- 描述：城市基本信息，用于列表展示、筛选、排序
-- ============================================================



CREATE TABLE IF NOT EXISTS t_city (

    id                      SERIAL          PRIMARY KEY,
    city_name               VARCHAR(50)     NOT NULL,           -- 城市名称
    province                VARCHAR(30)     NOT NULL,           -- 省份（如：广东省）
    region                  VARCHAR(20),                        -- 所属地区（华东/华南/华北/华中/东北/西南/西北）
    
    city_intro              TEXT,                               -- 城市简介
    college_count           INTEGER         DEFAULT 0,          -- 高校数量
    key_college_count       INTEGER         DEFAULT 0,          -- 重点高校数量
    resident_population     NUMERIC(8, 2),                      -- 常住人口（万人）
    gdp                     NUMERIC(10, 2),                     -- GDP（亿元）
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_city_name UNIQUE (city_name),
    
);


-- 索引
CREATE INDEX idx_city_province ON t_city (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_region   ON t_city (region)   WHERE is_deleted = FALSE;

CREATE INDEX idx_city_gdp
    ON t_city (gdp DESC NULLS LAST) WHERE is_deleted = FALSE;

CREATE INDEX idx_city_population
    ON t_city (resident_population DESC NULLS LAST) WHERE is_deleted = FALSE;

CREATE INDEX idx_city_name_search
    ON t_city USING btree (city_name varchar_pattern_ops) WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_city_updated_at
    BEFORE UPDATE ON t_city
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_city                        IS '城市基本信息表';
COMMENT ON COLUMN t_city.city_name              IS '城市名称';
COMMENT ON COLUMN t_city.province               IS '所属省份';
COMMENT ON COLUMN t_city.region                 IS '所属地区（华东/华南/华北/华中/东北/西南/西北/港澳台）';
COMMENT ON COLUMN t_city.area                   IS '面积（平方公里）';
COMMENT ON COLUMN t_city.city_intro             IS '城市简介';
COMMENT ON COLUMN t_city.college_count          IS '高校数量';
COMMENT ON COLUMN t_city.key_college_count      IS '重点高校数量（985/211/双一流）';
COMMENT ON COLUMN t_city.resident_population    IS '常住人口（万人）';
COMMENT ON COLUMN t_city.gdp                    IS 'GDP（亿元）';

COMMIT;
```

---

## 二、城市详情表


```
-- ============================================================
-- 城市详情表 (t_city_detail)
-- 描述：与 t_city 一对一，存储城市各维度详细数据
--
-- JSONB 字段结构说明见下方注释块
-- ============================================================


CREATE TABLE IF NOT EXISTS t_city_detail (

    id                      SERIAL          PRIMARY KEY,
    city_id                 INTEGER         NOT NULL,           -- 关联城市表（一对一，代码层面约束）

    -- ==================== 基础信息 ====================
    city_name               VARCHAR(50)     NOT NULL,           -- 城市名称（冗余，方便查询）
    area                    NUMERIC(10, 2),                     -- 面积（平方公里）
    subtitle                VARCHAR(200),                       -- 副标题
    city_level              VARCHAR(20),                        -- 城市级别
    admin_code              VARCHAR(20),                        -- 行政区划代码（如：110000）

    -- ==================== 人口与经济指标（独立列，支持排序筛选）====================
    per_capita_gdp          NUMERIC(8, 2),                      -- 人均GDP（万元）
    urbanization_rate       NUMERIC(5, 2),                      -- 城镇化率（%）
    rural_pop_ratio         NUMERIC(5, 2),                      -- 农村人口比例（%）
    aging_rate              NUMERIC(5, 2),                      -- 老龄化率（%）
    migrant_pop_ratio       NUMERIC(5, 2),                      -- 外来人口比例（%）
    gdp_growth_rate         NUMERIC(5, 2),                      -- GDP增长率（%）
    fortune_500_count       INTEGER         DEFAULT 0,          -- 世界500强企业数量
    
    -- ==================== 产业信息 ====================
    industry_structure      JSONB           DEFAULT '{}'::JSONB, -- 产业结构占比
    industry_description    TEXT,                                -- 产业描述
    main_industries         TEXT[]          DEFAULT '{}',        -- 主要产业
    emerging_industries     TEXT[]          DEFAULT '{}',        -- 新兴产业

    -- ==================== 未来规划 ====================
    future_plan             JSONB           DEFAULT '{}'::JSONB, -- 未来规划

    -- ==================== 教育资源 ====================
    high_education               JSONB           DEFAULT '{}'::JSONB, -- 教育资源（高等）
    basic_education               JSONB           DEFAULT '{}'::JSONB, -- 教育资源（基础）

    -- ==================== 企业统计 ====================
    enterprise_stats        JSONB           DEFAULT '{}'::JSONB, -- 企业统计
    
    -- ==================== 住房数据（购房+租房+政策） ====================
    housing_price_level     JSONB           DEFAULT '{}'::JSONB, -- 房价水平
    rental_cost             JSONB           DEFAULT '{}'::JSONB, -- 租房成本
    housing_policy          JSONB           DEFAULT '{}'::JSONB, -- 住房政策

    -- ==================== 消费数据 ====================
    consumption             JSONB           DEFAULT '{}'::JSONB, -- 消费相关数据

    -- ==================== 就业数据 ====================
    employment              JSONB           DEFAULT '{}'::JSONB, -- 就业相关数据

    -- ==================== 交通数据 ====================
    transportation          JSONB           DEFAULT '{}'::JSONB, -- 交通相关数据

    -- ==================== 医疗数据 ====================
    medical                 JSONB           DEFAULT '{}'::JSONB, -- 医疗相关数据

    -- ==================== 文化数据 ====================
    culture                 JSONB           DEFAULT '{}'::JSONB, -- 文化旅游数据

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
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


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------
-- city_id 唯一索引（UNIQUE 已自动创建）

CREATE INDEX idx_city_detail_level
    ON t_city_detail (city_level) WHERE is_deleted = FALSE;

CREATE INDEX idx_city_detail_per_capita_gdp
    ON t_city_detail (per_capita_gdp DESC NULLS LAST) WHERE is_deleted = FALSE;

CREATE INDEX idx_city_detail_gdp_growth
    ON t_city_detail (gdp_growth_rate DESC NULLS LAST) WHERE is_deleted = FALSE;

CREATE INDEX idx_city_detail_industries
    ON t_city_detail USING gin (main_industries) WHERE is_deleted = FALSE;


-- ----------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------
CREATE TRIGGER trg_city_detail_updated_at
    BEFORE UPDATE ON t_city_detail
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 表注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_city_detail IS '城市详情表：与 t_city 一对一，存储各维度详细数据';

COMMENT ON COLUMN t_city_detail.city_id              IS '关联城市表ID（一对一）';
COMMENT ON COLUMN t_city_detail.city_name            IS '城市名称（冗余字段）';
COMMENT ON COLUMN t_city_detail.subtitle             IS '副标题';
COMMENT ON COLUMN t_city_detail.city_level           IS '城市级别（直辖市/省会城市/地级市/县级市）';
COMMENT ON COLUMN t_city_detail.admin_code           IS '行政区划代码';
COMMENT ON COLUMN t_city_detail.per_capita_gdp       IS '人均GDP（万元）';
COMMENT ON COLUMN t_city_detail.urbanization_rate    IS '城镇化率（%）';
COMMENT ON COLUMN t_city_detail.rural_pop_ratio      IS '农村人口比例（%）';
COMMENT ON COLUMN t_city_detail.aging_rate           IS '老龄化率（%）';
COMMENT ON COLUMN t_city_detail.migrant_pop_ratio    IS '外来人口比例（%）';
COMMENT ON COLUMN t_city_detail.gdp_growth_rate      IS 'GDP增长率（%）';
COMMENT ON COLUMN t_city_detail.fortune_500_count    IS '世界500强企业数量';
COMMENT ON COLUMN t_city_detail.industry_structure   IS '产业结构占比（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.industry_description IS '产业描述';
COMMENT ON COLUMN t_city_detail.main_industries      IS '主要产业列表';
COMMENT ON COLUMN t_city_detail.emerging_industries  IS '新兴产业列表';
COMMENT ON COLUMN t_city_detail.future_plan          IS '未来规划（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.education            IS '教育资源（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.enterprise_stats     IS '企业统计（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.housing              IS '住房数据（JSONB），含购房/租房/政策';
COMMENT ON COLUMN t_city_detail.consumption          IS '消费数据（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.employment           IS '就业数据（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.transportation       IS '交通数据（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.medical              IS '医疗数据（JSONB），结构见下方说明';
COMMENT ON COLUMN t_city_detail.culture              IS '文化旅游数据（JSONB），结构见下方说明';

COMMIT;
```


## 一、行业表

```
-- ============================================================
-- 行业表 (t_industry)
-- 描述：行业基本信息，用于列表展示、筛选
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_industry (

    id                      SERIAL          PRIMARY KEY,
    industry_name           VARCHAR(100)    NOT NULL,           -- 行业名称
    category                VARCHAR(50),                        -- 行业分类（如：信息技术、金融、制造业）
    icon_class              VARCHAR(100),                       -- 图标类名（如：fa-solid fa-microchip）
    description             TEXT,                               -- 行业描述

    -- ==================== 核心指标 ====================
    annual_growth_rate      NUMERIC(5, 2),                      -- 年增长率（%）
    market_scale            VARCHAR(50),                        -- 市场规模（如：1.8万亿）
    talent_gap              VARCHAR(50),                        -- 人才缺口（如：120万）
    investment_heat         NUMERIC(5, 2),                      -- 投资热度（%）

    -- ==================== 趋势（涨跌方向） ====================
    growth_trend            VARCHAR(10),                        -- 增长趋势
    market_trend            VARCHAR(10),                        -- 市场趋势
    talent_trend            VARCHAR(10),                        -- 人才趋势
    investment_trend        VARCHAR(10),                        -- 投资趋势

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_industry_name UNIQUE (industry_name),

    CONSTRAINT chk_growth_trend     CHECK (growth_trend     IS NULL OR growth_trend     IN ('上升','稳定','下降')),
    CONSTRAINT chk_market_trend     CHECK (market_trend     IS NULL OR market_trend     IN ('上升','稳定','下降')),
    CONSTRAINT chk_talent_trend     CHECK (talent_trend     IS NULL OR talent_trend     IN ('上升','稳定','下降')),
    CONSTRAINT chk_investment_trend CHECK (investment_trend  IS NULL OR investment_trend  IN ('上升','稳定','下降')),

    CONSTRAINT chk_annual_growth    CHECK (annual_growth_rate IS NULL OR annual_growth_rate BETWEEN -100 AND 1000),
    CONSTRAINT chk_investment_heat  CHECK (investment_heat    IS NULL OR investment_heat    BETWEEN 0 AND 100)
);


-- 索引
CREATE INDEX idx_industry_category
    ON t_industry (category) WHERE is_deleted = FALSE;

CREATE INDEX idx_industry_growth
    ON t_industry (annual_growth_rate DESC NULLS LAST) WHERE is_deleted = FALSE;

CREATE INDEX idx_industry_name_search
    ON t_industry USING btree (industry_name varchar_pattern_ops) WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_industry_updated_at
    BEFORE UPDATE ON t_industry
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_industry                       IS '行业基本信息表';
COMMENT ON COLUMN t_industry.industry_name         IS '行业名称';
COMMENT ON COLUMN t_industry.category              IS '行业分类（如：信息技术、金融、制造业）';
COMMENT ON COLUMN t_industry.icon_class            IS '图标CSS类名（Font Awesome，如：fa-solid fa-microchip）';
COMMENT ON COLUMN t_industry.description           IS '行业描述';
COMMENT ON COLUMN t_industry.annual_growth_rate    IS '年增长率（%）';
COMMENT ON COLUMN t_industry.market_scale          IS '市场规模（如：1.8万亿）';
COMMENT ON COLUMN t_industry.talent_gap            IS '人才缺口（如：120万）';
COMMENT ON COLUMN t_industry.investment_heat       IS '投资热度（%）';
COMMENT ON COLUMN t_industry.growth_trend          IS '增长趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.market_trend          IS '市场趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.talent_trend          IS '人才趋势（上升/稳定/下降）';
COMMENT ON COLUMN t_industry.investment_trend      IS '投资趋势（上升/稳定/下降）';

COMMIT;
```

---

## 二、行业详情表


```
-- ============================================================
-- 行业详情表 (t_industry_detail)
-- 描述：与 t_industry 一对一，存储行业深度分析数据
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_industry_detail (

    id                          SERIAL          PRIMARY KEY,
    industry_id                 INTEGER         NOT NULL,           -- 关联行业表（一对一）
    industry_name               VARCHAR(100)    NOT NULL,           -- 行业名称（冗余）

    -- ==================== 描述信息 ====================
    short_description           VARCHAR(500),                       -- 简短描述
    detailed_description        TEXT,                               -- 详细描述

    -- ==================== 核心指标（展示卡片） =================
    
    industry_scale           JSONB          DEFAULT '{}'::JSONB,        -- 发展规模
    industry_talent_demand   JSONB          DEFAULT '{}'::JSONB,        -- 人才需求
    industry_salary          JSONB          DEFAULT '{}'::JSONB,        -- 行业薪资

    -- ==================== 政策信息 ====================
    policy_info                 JSONB           DEFAULT '{}'::JSONB,

    -- ==================== 发展地域与城市支持 ====================
    development_support_info    JSONB           DEFAULT '{}'::JSONB,

    -- ==================== 人才需求分析 ====================
    talent_analysis             JSONB           DEFAULT '{}'::JSONB,

    -- ==================== 人才政策 ====================
    talent_policy               JSONB           DEFAULT '{}'::JSONB,

    -- ==================== 薪资数据（分布 + 分析合并） ====================
    salary_data                 JSONB           DEFAULT '{}'::JSONB,

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_industry_detail_id UNIQUE (industry_id)
);


-- 索引
CREATE INDEX idx_industry_detail_name
    ON t_industry_detail (industry_name) WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_industry_detail_updated_at
    BEFORE UPDATE ON t_industry_detail
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_industry_detail                            IS '行业详情表：与 t_industry 一对一';
COMMENT ON COLUMN t_industry_detail.industry_id                IS '关联行业表ID';
COMMENT ON COLUMN t_industry_detail.industry_name              IS '行业名称（冗余字段）';
COMMENT ON COLUMN t_industry_detail.short_description          IS '简短描述';
COMMENT ON COLUMN t_industry_detail.detailed_description       IS '详细描述';
COMMENT ON COLUMN t_industry_detail.core_metrics               IS '核心指标（JSONB），结构见规范';
COMMENT ON COLUMN t_industry_detail.scale_description          IS '发展规模描述段落';
COMMENT ON COLUMN t_industry_detail.talent_demand_description  IS '人才需求描述段落';
COMMENT ON COLUMN t_industry_detail.salary_description         IS '行业薪资描述段落';
COMMENT ON COLUMN t_industry_detail.talent_analysis            IS '人才需求分析（JSONB），结构见规范';
COMMENT ON COLUMN t_industry_detail.policy_info                IS '政策信息（JSONB），结构见规范';
COMMENT ON COLUMN t_industry_detail.talent_policy              IS '人才政策（JSONB），结构见规范';
COMMENT ON COLUMN t_industry_detail.salary_data                IS '薪资数据（JSONB），含分布与分析';

COMMIT;
```

## 资源表

```
-- ============================================================
-- 资源表 (t_resource)
-- 描述：学习资源（真题/教材/视频等），链接指向百度网盘
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_resource (

    id                      SERIAL          PRIMARY KEY,
    resource_name           VARCHAR(200)    NOT NULL,           -- 资源名称
    cover_url               VARCHAR(500),                       -- 封面图URL
    description             TEXT,                               -- 资源描述
    resource_url            VARCHAR(500)    NOT NULL,           -- 资源链接（百度网盘）
    access_code             VARCHAR(20),                        -- 提取码（如：ab1c）
    category                VARCHAR(50),                        -- 分类（如：考研真题/四六级/公务员/专业课）
    file_type               VARCHAR(20),                        -- 文件类型（PDF/视频/压缩包）
    view_count              INTEGER         DEFAULT 0,          -- 浏览统计（点击链接+1）
    sort_order              INTEGER         DEFAULT 0,          -- 排序权重

    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_view_count CHECK (view_count >= 0)
);


-- 索引
CREATE INDEX idx_resource_category
    ON t_resource (category)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_resource_sort
    ON t_resource (sort_order, created_at DESC)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_resource_name_search
    ON t_resource USING btree (resource_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_resource_view_count
    ON t_resource (view_count DESC)
    WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_resource_updated_at
    BEFORE UPDATE ON t_resource
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_resource                    IS '学习资源表（百度网盘链接）';
COMMENT ON COLUMN t_resource.resource_name      IS '资源名称';
COMMENT ON COLUMN t_resource.cover_url          IS '封面图URL';
COMMENT ON COLUMN t_resource.description        IS '资源描述';
COMMENT ON COLUMN t_resource.resource_url       IS '资源链接（百度网盘地址）';
COMMENT ON COLUMN t_resource.access_code        IS '百度网盘提取码';
COMMENT ON COLUMN t_resource.category           IS '分类（考研真题/四六级/公务员/专业课）';
COMMENT ON COLUMN t_resource.file_type          IS '文件类型（PDF/视频/压缩包）';
COMMENT ON COLUMN t_resource.view_count         IS '浏览统计（点击+1）';
COMMENT ON COLUMN t_resource.sort_order         IS '排序权重';

COMMIT;
```


