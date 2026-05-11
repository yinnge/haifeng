## 二、企业表 (t_enterprise)


```
-- ============================================================
-- 企业表 (t_enterprise)
-- 描述：央企、国企、民企、外企等企业主体信息
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_enterprise (

    id                      SERIAL          PRIMARY KEY,
    
-- ==================== 关联城市 ====================
    city_id                 INTEGER         ,  -- 所在城市（关联城市表）
    city_name               VARCHAR(50),                            -- 城市名称（冗余，列表免JOIN）

    -- ==================== 基本信息 ====================
    enterprise_name         VARCHAR(200)    NOT NULL,                -- 企业名称（如：腾讯科技、中国烟草）
    enterprise_nature       VARCHAR(30)     NOT NULL,                -- 企业性质（央企/国企/民企/外企/合资）
    enterprise_type         VARCHAR(50),                             -- 企业类型（更细分，如：地方国企、互联网大厂、独角兽）
    logo_url                VARCHAR(500),                            -- 企业 Logo 图片地址
    official_website        VARCHAR(500),                            -- 企业官网
    
    
    -- ==================== 地区信息 ====================
    region                  VARCHAR(100),                            -- 总部地区（如：广东省深圳市）

    -- ==================== 描述信息 ====================
    enterprise_scale        VARCHAR(50),                             -- 企业规模（如：10000人以上）
    main_business           VARCHAR(500),                            -- 主营业务
    enterprise_intro        TEXT,                                    -- 企业简介

    -- ==================== 状态 ====================
    recruitment_status      VARCHAR(20)     DEFAULT '招聘中',         -- 招聘状态

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_enterprise_name UNIQUE (enterprise_name),

    CONSTRAINT chk_enterprise_nature CHECK (
        enterprise_nature IN ('央企', '国企', '民企', '外企', '合资')
    )
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------


-- 城市详情页最核心索引：按城市+性质+类型筛选
CREATE INDEX idx_ent_city_nature_type
    ON t_enterprise (city_id, enterprise_nature, enterprise_type)
    WHERE is_deleted = FALSE;

-- 按企业性质筛选（最核心的筛选维度：区分央国企/民企模块）
CREATE INDEX idx_ent_nature
    ON t_enterprise (enterprise_nature)
    WHERE is_deleted = FALSE;

-- 按行业筛选
CREATE INDEX idx_ent_industry
    ON t_enterprise (industry)
    WHERE is_deleted = FALSE;

-- 企业名称模糊搜索
CREATE INDEX idx_ent_name_pattern
    ON t_enterprise USING btree (enterprise_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_ent_region
    ON t_enterprise (region)
    WHERE is_deleted = FALSE;


-- ----------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------
CREATE TRIGGER trg_enterprise_updated_at
    BEFORE UPDATE ON t_enterprise
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_enterprise                          IS '企业主体表（央企/国企/民企/外企）';
COMMENT ON COLUMN t_enterprise.enterprise_name          IS '企业名称';
COMMENT ON COLUMN t_enterprise.enterprise_nature        IS '企业性质：央企、国企、民企、外企、合资';
COMMENT ON COLUMN t_enterprise.enterprise_type          IS '企业类型（细分，如：地方国企、互联网大厂）';
COMMENT ON COLUMN t_enterprise.industry                 IS '所属行业';
COMMENT ON COLUMN t_enterprise.logo_url                 IS '企业Logo图片地址';
COMMENT ON COLUMN t_enterprise.official_website         IS '企业官网';
COMMENT ON COLUMN t_enterprise.region                   IS '总部所在地区';
COMMENT ON COLUMN t_enterprise.enterprise_scale         IS '企业规模';
COMMENT ON COLUMN t_enterprise.main_business            IS '主营业务';
COMMENT ON COLUMN t_enterprise.enterprise_intro         IS '企业简介';
COMMENT ON COLUMN t_enterprise.recruitment_status       IS '招聘状态';

COMMIT;
```

---

## 三、企业岗位表 (t_enterprise_position)

> 央国企 + 民企的岗位**共用这一张表**

SQL

```
-- ============================================================
-- 企业岗位表 (t_enterprise_position)
-- 描述：央国企 + 民企的招聘岗位，通过 enterprise_id 关联企业
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_enterprise_position (

    id                          SERIAL          PRIMARY KEY,

    -- ==================== 关联企业 ====================
    enterprise_id               INTEGER         NOT NULL,

    -- ==================== 岗位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称（如：软件工程师）
    recruitment_type            VARCHAR(30),                        -- 招聘类型（校招/社招/实习）
    position_requirement        TEXT,                               -- 岗位要求（详细描述）
    position_tags               TEXT[]          DEFAULT '{}',        -- 岗位标签（如：五险一金、带薪年假、弹性工作）

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30),                        -- 省份
    city                        VARCHAR(50),                        -- 城市
    work_location               VARCHAR(200),                       -- 工作地点（更详细，如：北京市海淀区中关村）

    -- ==================== 招聘要求 ====================
    education_requirement       VARCHAR(30),                        -- 学历要求（不限/大专/本科/硕士/博士）
    major_requirement           VARCHAR(500),                       -- 专业要求
    work_experience             VARCHAR(50),                        -- 工作经验（如：不限、应届、1-3年）

    -- ==================== 薪资信息 ====================
    salary_min                  INTEGER,                            -- 最低薪资（单位：k/月）
    salary_max                  INTEGER,                            -- 最高薪资（单位：k/月）

    -- ==================== 报名信息 ====================
    apply_link                  VARCHAR(500),                       -- 申请/报名链接
    deadline                    TIMESTAMPTZ,                        -- 截止日期

    -- ==================== 状态 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 岗位状态

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_ep_recruitment_type CHECK (
        recruitment_type IS NULL
        OR recruitment_type IN ('校招', '社招', '实习')
    ),
    CONSTRAINT chk_ep_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_ep_position_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束')
    ),
    CONSTRAINT chk_ep_salary CHECK (
        salary_min IS NULL
        OR salary_max IS NULL
        OR salary_min <= salary_max
    )
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按企业查岗位（最常用：企业详情页）
CREATE INDEX idx_ep_enterprise
    ON t_enterprise_position (enterprise_id)
    WHERE is_deleted = FALSE;

-- 岗位名称模糊搜索
CREATE INDEX idx_ep_position_name
    ON t_enterprise_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按招聘类型筛选
CREATE INDEX idx_ep_recruitment_type
    ON t_enterprise_position (recruitment_type)
    WHERE is_deleted = FALSE;

-- 按省份+城市筛选
CREATE INDEX idx_ep_location
    ON t_enterprise_position (province, city)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_ep_education
    ON t_enterprise_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 按截止日期排序（即将截止优先）
CREATE INDEX idx_ep_deadline
    ON t_enterprise_position (deadline ASC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 岗位标签 GIN 索引（支持标签筛选）
CREATE INDEX idx_ep_tags
    ON t_enterprise_position USING gin (position_tags)
    WHERE is_deleted = FALSE;

-- 按薪资范围筛选
CREATE INDEX idx_ep_salary
    ON t_enterprise_position (salary_min, salary_max)
    WHERE is_deleted = FALSE;


-- ----------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------
CREATE TRIGGER trg_enterprise_position_updated_at
    BEFORE UPDATE ON t_enterprise_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_enterprise_position                             IS '企业岗位表（央国企+民企共用）';
COMMENT ON COLUMN t_enterprise_position.enterprise_id               IS '关联企业ID';
COMMENT ON COLUMN t_enterprise_position.position_name               IS '岗位名称';
COMMENT ON COLUMN t_enterprise_position.recruitment_type            IS '招聘类型：校招/社招/实习';
COMMENT ON COLUMN t_enterprise_position.position_requirement        IS '岗位要求详细描述';
COMMENT ON COLUMN t_enterprise_position.position_tags               IS '岗位标签（如：五险一金、弹性工作）';
COMMENT ON COLUMN t_enterprise_position.province                    IS '省份';
COMMENT ON COLUMN t_enterprise_position.city                        IS '城市';
COMMENT ON COLUMN t_enterprise_position.work_location               IS '详细工作地点';
COMMENT ON COLUMN t_enterprise_position.education_requirement       IS '学历要求';
COMMENT ON COLUMN t_enterprise_position.major_requirement           IS '专业要求';
COMMENT ON COLUMN t_enterprise_position.work_experience             IS '工作经验要求';
COMMENT ON COLUMN t_enterprise_position.salary_min                  IS '最低月薪（单位：k）';
COMMENT ON COLUMN t_enterprise_position.salary_max                  IS '最高月薪（单位：k）';
COMMENT ON COLUMN t_enterprise_position.apply_link                  IS '申请链接';
COMMENT ON COLUMN t_enterprise_position.deadline                    IS '报名截止日期';
COMMENT ON COLUMN t_enterprise_position.position_status             IS '岗位状态';

COMMIT;
```


## 1. 企业-行业关联表 (t_enterprise_industry)


```
-- ============================================================
-- 企业-行业关联表 (t_enterprise_industry)
-- 描述：多对多，一个企业可属于多个行业
-- ============================================================

CREATE TABLE IF NOT EXISTS t_enterprise_industry (

    id              SERIAL      PRIMARY KEY,
    enterprise_id   INTEGER     NOT NULL ,  --逻辑外键
    enterprise_name VARCHAR(200)    NOT NULL,           -- 企业名称（如：腾讯科技、中国烟草）
    industry_id     INTEGER     NOT NULL ,
    industry_name   VARCHAR(100)    NOT NULL,           -- 行业名称（冗余）
    is_primary      BOOLEAN     NOT NULL DEFAULT FALSE,  -- 是否主行业（每企业只有一个）
    sort_order      SMALLINT    NOT NULL DEFAULT 0,      -- 排序

    -- 审计
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 唯一约束：同一企业不能重复关联同一行业
    CONSTRAINT uk_ent_ind UNIQUE (enterprise_id, industry_id)
);

-- 核心索引：行业详情页 → 查该行业下所有企业
CREATE INDEX idx_ei_industry
    ON t_enterprise_industry (industry_id);

-- 企业详情页 → 查该企业所属的所有行业
CREATE INDEX idx_ei_enterprise
    ON t_enterprise_industry (enterprise_id);

COMMENT ON TABLE  t_enterprise_industry              IS '企业-行业多对多关联表';
COMMENT ON COLUMN t_enterprise_industry.is_primary   IS '是否为主行业（用于列表只显示一个行业标签）';
COMMENT ON COLUMN t_enterprise_industry.sort_order   IS '排序权重';
```