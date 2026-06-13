## 1、统一岗位索引表 t_job_index（全局搜索核心）

> 所有岗位表的公共搜索层，首页搜索、筛选、排行、推荐全靠它

SQL

```
-- ============================================================
-- 统一岗位索引表 (t_job_index)
-- 描述：所有类型岗位的聚合索引，用于全局搜索/筛选/排序/推荐
-- 数据来源：各岗位表新增/更新时同步写入
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_job_index (
    id                      SERIAL          PRIMARY KEY,

    -- ==================== 来源定位 ====================
    source_type             VARCHAR(30)     NOT NULL,
        -- civil       = 公务员
        -- institution = 事业编
        -- military    = 部队文职
        -- enterprise  = 企业岗位
        -- selection   = 选调生
        -- teacher     = 教师招聘
        -- healthcare  = 医疗卫生
        -- finance     = 银行金融
    source_id               INTEGER         NOT NULL,           -- 来源表中的ID

    -- ==================== 展示信息 ====================
    category_label          VARCHAR(50)     NOT NULL,           -- 前端标签（公务员/事业编/选调生/教师/...）
    position_name           VARCHAR(200)    NOT NULL,           -- 岗位名称
    organization_name       VARCHAR(200),                       -- 招录单位/企业名称
    organization_logo       VARCHAR(500),                       -- 单位Logo（企业有，体制内可为空）

    -- ==================== 筛选维度 ====================
    province                VARCHAR(50),                        -- 省份
    city                    VARCHAR(50),                        -- 城市
    education_requirement   VARCHAR(50),                        -- 学历要求
    recruitment_count       INTEGER,                            -- 招录/招聘人数
    recruitment_type        VARCHAR(30),                        -- 招聘类型（国考/省考/校招/社招/春招/秋招...）

    -- ==================== 薪资（统一展示） ====================
    salary_min              INTEGER,                            -- 最低薪资（k/月），无则NULL
    salary_max              INTEGER,                            -- 最高薪资（k/月）
    salary_text             VARCHAR(50),                        -- 薪资文本（如"面议"、"8-12k"、"参照当地标准"）

    -- ==================== 时间 ====================
    publish_date            TIMESTAMPTZ,                        -- 发布日期
    reg_deadline            TIMESTAMPTZ,                        -- 报名截止日期

    -- ==================== 热度/排序 ====================
    is_hot                  BOOLEAN         DEFAULT FALSE,      -- 是否热门（运营手动标记 或 算法）
    view_count              INTEGER         DEFAULT 0,          -- 浏览量
    apply_count             INTEGER         DEFAULT 0,          -- 报名/申请人数

    -- ==================== 状态 ====================
    position_status         VARCHAR(20)     DEFAULT '招聘中',    -- 招聘中/已结束/即将开始

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    -- 同一来源表中的同一条记录只能出现一次
    CONSTRAINT uk_job_index_source UNIQUE (source_type, source_id),

    CONSTRAINT chk_job_source_type CHECK (
        source_type IN (
            'civil', 'institution', 'military', 'enterprise',
            'selection', 'teacher', 'healthcare', 'finance'
        )
    ),
    CONSTRAINT chk_job_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引（这张表的索引是全站性能的命脉）
-- ----------------------------------------------------------

-- 全站搜索：岗位名称
CREATE INDEX idx_job_position_name
    ON t_job_index USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 全站搜索：单位名称
CREATE INDEX idx_job_org_name
    ON t_job_index USING btree (organization_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按类型筛选（首页Tab切换）
CREATE INDEX idx_job_source_type
    ON t_job_index (source_type)
    WHERE is_deleted = FALSE;

-- 按标签筛选
CREATE INDEX idx_job_category
    ON t_job_index (category_label)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_job_location
    ON t_job_index (province, city)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_job_education
    ON t_job_index (education_requirement)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_job_status
    ON t_job_index (position_status)
    WHERE is_deleted = FALSE;

-- 热门排行（浏览量倒序）
CREATE INDEX idx_job_hot
    ON t_job_index (view_count DESC)
    WHERE is_deleted = FALSE AND is_hot = TRUE;

-- 最新发布
CREATE INDEX idx_job_publish_date
    ON t_job_index (publish_date DESC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 即将截止（deadline 正序，最近截止的排前面）
CREATE INDEX idx_job_deadline
    ON t_job_index (reg_deadline ASC NULLS LAST)
    WHERE is_deleted = FALSE AND position_status = '招聘中';

-- 组合筛选：类型 + 地区 + 学历（最常见的多条件筛选）
CREATE INDEX idx_job_composite_filter
    ON t_job_index (source_type, province, education_requirement)
    WHERE is_deleted = FALSE;

-- 薪资筛选
CREATE INDEX idx_job_salary
    ON t_job_index (salary_min, salary_max)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_job_index_updated_at
    BEFORE UPDATE ON t_job_index
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_job_index                           IS '统一岗位索引表（全局搜索/筛选/排序/推荐）';
COMMENT ON COLUMN t_job_index.source_type               IS '来源类型：civil/institution/military/enterprise/selection/teacher/healthcare/finance';
COMMENT ON COLUMN t_job_index.source_id                 IS '来源表中的主键ID';
COMMENT ON COLUMN t_job_index.category_label            IS '前端展示标签（公务员/事业编/选调生/教师/...）';
COMMENT ON COLUMN t_job_index.position_name             IS '岗位名称';
COMMENT ON COLUMN t_job_index.organization_name         IS '招录单位/企业名称';
COMMENT ON COLUMN t_job_index.organization_logo         IS '单位Logo地址';
COMMENT ON COLUMN t_job_index.province                  IS '省份';
COMMENT ON COLUMN t_job_index.city                      IS '城市';
COMMENT ON COLUMN t_job_index.education_requirement     IS '学历要求';
COMMENT ON COLUMN t_job_index.recruitment_count         IS '招录/招聘人数';
COMMENT ON COLUMN t_job_index.recruitment_type          IS '招聘类型（国考/省考/校招/社招/春招/秋招等）';
COMMENT ON COLUMN t_job_index.salary_min                IS '最低月薪（单位：k）';
COMMENT ON COLUMN t_job_index.salary_max                IS '最高月薪（单位：k）';
COMMENT ON COLUMN t_job_index.salary_text               IS '薪资文本展示';
COMMENT ON COLUMN t_job_index.publish_date              IS '发布日期';
COMMENT ON COLUMN t_job_index.reg_deadline              IS '报名截止日期';
COMMENT ON COLUMN t_job_index.is_hot                    IS '是否热门';
COMMENT ON COLUMN t_job_index.view_count                IS '浏览量';
COMMENT ON COLUMN t_job_index.apply_count               IS '报名/申请人数';
COMMENT ON COLUMN t_job_index.position_status           IS '状态（招聘中/已结束/即将开始）';

COMMIT;
```

### 索引表数据流转逻辑（后端同步策略）

text

```
┌──────────────────────────────────────────────────────────┐
│  新增/更新任意岗位表时                                      │
│                                                          │
│  t_civil_position ──┐                                    │
│  t_institution_pos ─┤                                    │
│  t_military_pos ────┤     INSERT / UPDATE                │
│  t_enterprise_pos ──┼──────────────────→  t_job_index    │
│  t_selection_pos ───┤                                    │
│  t_teacher_pos ─────┤    (Service 层同步写入)              │
│  t_healthcare_pos ──┤                                    │
│  t_finance_pos ─────┘                                    │
└──────────────────────────────────────────────────────────┘
```

---

### 更新 t_job_index 约束

> 新增 3 个 source_type 值

SQL

```
-- ============================================================
-- 更新 t_job_index 的 source_type 约束（新增类型）
-- ============================================================

ALTER TABLE t_job_index
    DROP CONSTRAINT IF EXISTS chk_job_source_type;

ALTER TABLE t_job_index
    ADD CONSTRAINT chk_job_source_type CHECK (
        source_type IN (
            'civil',            -- 公务员
            'institution',      -- 事业编
            'military',         -- 部队文职
            'enterprise',       -- 企业
            'selection',        -- 选调生
            'teacher',          -- 教师招聘
            'healthcare',       -- 医疗卫生
            'finance',          -- 银行金融
            'grassroots',       -- 基层服务（三支一扶/西部计划）
            'community',        -- 社区工作者
            'public_welfare'    -- 公益性岗位
        )
    );
```

## 





## 2、统一备考指南表 t_exam_guide

> 替换原来的 `t_civil_methodology`、`t_institution_guide` 两张表  
> 本质都是文章/指南，仅类别不同

SQL

```
-- ============================================================
-- 统一备考指南表 (t_exam_guide)
-- 描述：全平台备考方法、学习技巧、经验分享等指导文章
-- 替代：t_civil_methodology + t_institution_guide + 未来军队文职备考
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_exam_guide (
    id                      SERIAL          PRIMARY KEY,

    -- ==================== 分类 ====================
    guide_category          VARCHAR(30)     NOT NULL,
        -- civil         = 公务员备考
        -- institution   = 事业编备考
        -- military      = 部队文职备考
        -- selection     = 选调生备考
        -- teacher       = 教师招聘备考
        -- healthcare    = 医疗卫生备考
        -- finance       = 银行金融备考
        -- grassroots    = 基层服务项目备考
        -- community     = 社区工作者备考
        -- general       = 通用（如面试技巧、心态调整）

    guide_type              VARCHAR(30)     DEFAULT '备考攻略',
        -- 备考攻略/科目指导/真题解析/面试技巧
        -- 时事热点/经验分享/政策解读/学习计划

    -- ==================== 内容 ====================
    title                   VARCHAR(300)    NOT NULL,           -- 文章标题
    subtitle                VARCHAR(300),                       -- 副标题
    cover_image             VARCHAR(500),                       -- 封面图片URL
    icon_class              VARCHAR(100),                       -- 图标CSS类名（Font Awesome）
    summary                 TEXT,                               -- 摘要
    content                 TEXT            NOT NULL,           -- 详细内容（支持HTML）

    -- ==================== 标签 ====================
    tags                    TEXT[]          DEFAULT '{}',       -- 标签（如：{行测, 数量关系, 高频考点}）
    difficulty_level        VARCHAR(10),                        -- 难度（入门/进阶/高阶）
    target_audience         VARCHAR(50),                        -- 目标读者（如：零基础、在职备考、应届生）

    -- ==================== 作者信息 ====================
    author_name             VARCHAR(50),                        -- 作者名（可为笔名）
    author_title            VARCHAR(100),                       -- 作者头衔（如：xx机构名师、上岸学长）

    -- ==================== 展示控制 ====================
    is_top                  BOOLEAN         DEFAULT FALSE,      -- 是否置顶
    is_recommended          BOOLEAN         DEFAULT FALSE,      -- 是否推荐（编辑精选）
    sort_order              INTEGER         DEFAULT 0,          -- 排序权重（越大越靠前）
    view_count              INTEGER         DEFAULT 0,          -- 阅读量
    like_count              INTEGER         DEFAULT 0,          -- 点赞数

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

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按类别筛选（最核心：公务员备考/事业编备考/...）
CREATE INDEX idx_guide_category
    ON t_exam_guide (guide_category)
    WHERE is_deleted = FALSE;

-- 类别 + 类型 组合
CREATE INDEX idx_guide_cat_type
    ON t_exam_guide (guide_category, guide_type)
    WHERE is_deleted = FALSE;

-- 推荐文章（首页/频道页精选展示）
CREATE INDEX idx_guide_recommended
    ON t_exam_guide (guide_category, sort_order DESC)
    WHERE is_deleted = FALSE AND is_recommended = TRUE;

-- 置顶 + 最新
CREATE INDEX idx_guide_top_date
    ON t_exam_guide (is_top DESC, created_at DESC)
    WHERE is_deleted = FALSE;

-- 标题搜索
CREATE INDEX idx_guide_title
    ON t_exam_guide USING btree (title varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 标签 GIN 索引
CREATE INDEX idx_guide_tags
    ON t_exam_guide USING gin (tags)
    WHERE is_deleted = FALSE;

-- 最新发布
CREATE INDEX idx_guide_created
    ON t_exam_guide (created_at DESC)
    WHERE is_deleted = FALSE;

-- 热门文章（阅读量排序）
CREATE INDEX idx_guide_popular
    ON t_exam_guide (view_count DESC)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_exam_guide_updated_at
    BEFORE UPDATE ON t_exam_guide
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_exam_guide                      IS '统一备考指南表（全平台备考文章/经验/技巧）';
COMMENT ON COLUMN t_exam_guide.guide_category       IS '指南类别';
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

COMMIT;
```

## 3、统一公告表 t_notice

> 替换原来的 `t_recruitment_notice`、`t_institution_notice`、`t_military_notice` 三张表  
> 公告结构本质相同，只是类别不同，合一张表是正确的

SQL

```
-- ============================================================
-- 统一公告表 (t_notice)
-- 描述：全平台招考/招聘公告信息（替代原3张分散公告表）
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_notice (
    id                      SERIAL          PRIMARY KEY,

    -- ==================== 分类 ====================
    notice_category         VARCHAR(30)     NOT NULL,
        -- civil         = 公务员公告
        -- institution   = 事业编公告
        -- military      = 部队文职公告
        -- selection     = 选调生公告
        -- teacher       = 教师招聘公告
        -- healthcare    = 医疗卫生公告
        -- finance       = 银行金融公告
        -- grassroots    = 基层服务项目公告（三支一扶/西部计划）
        -- community     = 社区工作者公告
        -- public_welfare= 公益性岗位公告
        -- enterprise    = 企业招聘公告（央国企等重大招聘）
        -- general       = 综合公告（政策解读/通用类）

    notice_type             VARCHAR(50),                        -- 公告类型
        -- 招聘公告/招录公告/补录公告/调剂公告
        -- 成绩公示/面试通知/体检通知/录用公示
        -- 报名指南/考试大纲/政策解读

    -- ==================== 内容 ====================
    title                   VARCHAR(500)    NOT NULL,           -- 公告标题
    summary                 VARCHAR(500),                       -- 摘要（列表页展示，不超过200字）
    content                 TEXT            NOT NULL,           -- 公告内容（支持HTML格式）

    -- ==================== 标签/地区 ====================
    province                VARCHAR(30),                        -- 省份（如有地域性）
    city                    VARCHAR(50),                        -- 城市
    tags                    TEXT[]          DEFAULT '{}',       -- 标签（如：{2025国考, 热门, 重要}）
    year                    VARCHAR(10),                        -- 年份（如：2025）

    -- ==================== 来源信息 ====================
    source                  VARCHAR(200),                       -- 发布来源（如：国家公务员局、xx省人社厅）
    source_url              VARCHAR(500),                       -- 原文链接
    publish_date            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),  -- 发布日期
    publish_unit            VARCHAR(200),                       -- 发布单位

    -- ==================== 关联报名时间（部分公告包含） ====================
    reg_start_date          TIMESTAMPTZ,                        -- 报名开始
    reg_end_date            TIMESTAMPTZ,                        -- 报名结束
    exam_time               TIMESTAMPTZ,                        -- 考试时间
    recruitment_count       INTEGER,                            -- 本次招录/招聘总人数

    -- ==================== 展示控制 ====================
    is_top                  BOOLEAN         DEFAULT FALSE,      -- 是否置顶
    is_important            BOOLEAN         DEFAULT FALSE,      -- 是否重要（前端加红/加标）
    view_count              INTEGER         DEFAULT 0,          -- 阅读量

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

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按类别筛选（首页Tab切换：公务员公告/事业编公告/...）
CREATE INDEX idx_notice_category
    ON t_notice (notice_category)
    WHERE is_deleted = FALSE;

-- 按公告类型筛选
CREATE INDEX idx_notice_type
    ON t_notice (notice_type)
    WHERE is_deleted = FALSE;

-- 类别 + 类型 组合
CREATE INDEX idx_notice_cat_type
    ON t_notice (notice_category, notice_type)
    WHERE is_deleted = FALSE;

-- 按发布日期排序（最新公告）
CREATE INDEX idx_notice_publish_date
    ON t_notice (publish_date DESC)
    WHERE is_deleted = FALSE;

-- 置顶 + 最新（首页展示用）
CREATE INDEX idx_notice_top_date
    ON t_notice (is_top DESC, publish_date DESC)
    WHERE is_deleted = FALSE;

-- 按省份筛选
CREATE INDEX idx_notice_province
    ON t_notice (province)
    WHERE is_deleted = FALSE;

-- 类别 + 省份 + 年份（常见组合）
CREATE INDEX idx_notice_cat_province_year
    ON t_notice (notice_category, province, year)
    WHERE is_deleted = FALSE;

-- 标题搜索
CREATE INDEX idx_notice_title
    ON t_notice USING btree (title varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 标签 GIN 索引
CREATE INDEX idx_notice_tags
    ON t_notice USING gin (tags)
    WHERE is_deleted = FALSE;

-- 按来源筛选
CREATE INDEX idx_notice_source
    ON t_notice (source)
    WHERE is_deleted = FALSE;

-- 按年份筛选
CREATE INDEX idx_notice_year
    ON t_notice (year)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_notice_updated_at
    BEFORE UPDATE ON t_notice
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
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
```

---

## 3、选调生岗位表 t_selection_position

> 选调生 ≠ 公务员，核心区别：面向应届毕业生、有高校范围限制、有基层培养要求

SQL

```
-- ============================================================
-- 选调生岗位表 (t_selection_position)
-- 描述：各省定向/非定向选调生招录岗位信息
-- 说明：选调生由省委组织部统一组织，面向应届优秀大学毕业生
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_selection_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 招录基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称（如：乡镇综合管理岗）
    selection_type              VARCHAR(50)     NOT NULL,           -- 选调类型
        -- 定向选调：仅面向指定高校
        -- 非定向选调：面向所有符合条件的高校
        -- 急需紧缺专业选调
    year                        VARCHAR(10)     NOT NULL,           -- 招录年份（如：2025）
    province                    VARCHAR(30)     NOT NULL,           -- 招录省份（哪个省组织的选调）
    organizing_dept             VARCHAR(200),                       -- 组织单位（通常为省委组织部）
    target_unit                 VARCHAR(200),                       -- 录用去向单位（如：xx市xx区）
    work_location               VARCHAR(200),                       -- 具体工作地点

    -- ==================== 培养信息（选调生独有） ====================
    training_direction          VARCHAR(100),                       -- 培养方向（基层锻炼/省直机关/市直机关）
    grassroots_service_years    VARCHAR(30),                        -- 基层最低服务年限（如：2年、3年）
    training_plan               TEXT,                               -- 培养计划说明

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30)     NOT NULL,           -- 学历要求（本科/硕士/博士）
    degree_requirement          VARCHAR(30),                        -- 学位要求
    major_requirement           VARCHAR(500),                       -- 专业要求
    major_categories            TEXT[]          DEFAULT '{}',       -- 专业大类（如：{法学类,经济学类,计算机类}）

    -- ===== 选调生特殊要求（区别于普通公务员的核心字段） =====
    university_requirement      VARCHAR(100),                       -- 高校要求（985/211/双一流/普通高校）
    target_universities         TEXT[]          DEFAULT '{}',       -- 定向高校名单（定向选调用）
    political_status            VARCHAR(30)     NOT NULL DEFAULT '中共党员',  -- 政治面貌（选调生通常要求党员）
    student_cadre_requirement   VARCHAR(200),                       -- 学生干部经历要求（如：校级及以上学生干部）
    awards_requirement          TEXT,                               -- 荣誉/奖学金要求
    age_limit                   INTEGER,                            -- 年龄上限（周岁）

    -- ==================== 考试信息 ====================
    recruitment_count           INTEGER         DEFAULT 1,          -- 招录人数
    exam_subjects               VARCHAR(200),                       -- 考试科目（如：行测+申论、综合能力测试）
    interview_form              VARCHAR(100),                       -- 面试形式（结构化/无领导小组/半结构化）

    -- ==================== 报名时间 ====================
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名结束
    exam_time                   TIMESTAMPTZ,                        -- 笔试时间
    apply_link                  VARCHAR(500),                       -- 报名链接

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '报名中',    -- 岗位状态
    remark                      TEXT,                               -- 备注
    contact_phone               VARCHAR(50),                        -- 联系电话
    official_link               VARCHAR(500),                       -- 官方公告链接
    content                     TEXT,                               -- 详细说明（支持HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_sel_type CHECK (
        selection_type IN ('定向选调', '非定向选调', '急需紧缺专业选调')
    ),
    CONSTRAINT chk_sel_education CHECK (
        education_requirement IN ('本科', '硕士', '博士', '本科及以上', '硕士及以上')
    ),
    CONSTRAINT chk_sel_political CHECK (
        political_status IS NULL
        OR political_status IN ('中共党员', '中共预备党员', '共青团员', '不限')
    ),
    CONSTRAINT chk_sel_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 40)
    ),
    CONSTRAINT chk_sel_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_sel_status CHECK (
        position_status IS NULL
        OR position_status IN ('报名中', '笔试阶段', '面试阶段', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按省份筛选（最核心维度：每个省独立招录）
CREATE INDEX idx_sel_province
    ON t_selection_position (province)
    WHERE is_deleted = FALSE;

-- 按选调类型筛选
CREATE INDEX idx_sel_type
    ON t_selection_position (selection_type)
    WHERE is_deleted = FALSE;

-- 按年份筛选
CREATE INDEX idx_sel_year
    ON t_selection_position (year)
    WHERE is_deleted = FALSE;

-- 省份 + 年份 + 类型 组合（最常见查询）
CREATE INDEX idx_sel_province_year_type
    ON t_selection_position (province, year, selection_type)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_sel_education
    ON t_selection_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 按高校要求筛选
CREATE INDEX idx_sel_university
    ON t_selection_position (university_requirement)
    WHERE is_deleted = FALSE;

-- 定向高校列表 GIN 索引
CREATE INDEX idx_sel_target_unis
    ON t_selection_position USING gin (target_universities)
    WHERE is_deleted = FALSE;

-- 专业大类 GIN 索引
CREATE INDEX idx_sel_major_cats
    ON t_selection_position USING gin (major_categories)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_sel_pos_name
    ON t_selection_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_sel_status
    ON t_selection_position (position_status)
    WHERE is_deleted = FALSE;

-- 按报名截止排序
CREATE INDEX idx_sel_reg_end
    ON t_selection_position (reg_end_date ASC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_sel_position_updated_at
    BEFORE UPDATE ON t_selection_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_selection_position                              IS '选调生岗位表';
COMMENT ON COLUMN t_selection_position.position_name                IS '岗位名称';
COMMENT ON COLUMN t_selection_position.selection_type               IS '选调类型（定向选调/非定向选调/急需紧缺专业选调）';
COMMENT ON COLUMN t_selection_position.year                         IS '招录年份';
COMMENT ON COLUMN t_selection_position.province                     IS '招录省份';
COMMENT ON COLUMN t_selection_position.organizing_dept              IS '组织单位（通常为省委组织部）';
COMMENT ON COLUMN t_selection_position.target_unit                  IS '录用去向单位';
COMMENT ON COLUMN t_selection_position.work_location                IS '工作地点';
COMMENT ON COLUMN t_selection_position.training_direction           IS '培养方向（基层锻炼/省直机关/市直机关）';
COMMENT ON COLUMN t_selection_position.grassroots_service_years     IS '基层最低服务年限';
COMMENT ON COLUMN t_selection_position.training_plan                IS '培养计划说明';
COMMENT ON COLUMN t_selection_position.education_requirement        IS '学历要求';
COMMENT ON COLUMN t_selection_position.degree_requirement           IS '学位要求';
COMMENT ON COLUMN t_selection_position.major_requirement            IS '专业要求';
COMMENT ON COLUMN t_selection_position.major_categories             IS '专业大类列表';
COMMENT ON COLUMN t_selection_position.university_requirement       IS '高校层次要求（985/211/双一流/普通高校）';
COMMENT ON COLUMN t_selection_position.target_universities          IS '定向高校名单';
COMMENT ON COLUMN t_selection_position.political_status             IS '政治面貌要求';
COMMENT ON COLUMN t_selection_position.student_cadre_requirement    IS '学生干部经历要求';
COMMENT ON COLUMN t_selection_position.awards_requirement           IS '荣誉/奖学金要求';
COMMENT ON COLUMN t_selection_position.age_limit                    IS '年龄上限（周岁）';
COMMENT ON COLUMN t_selection_position.recruitment_count            IS '招录人数';
COMMENT ON COLUMN t_selection_position.exam_subjects                IS '考试科目';
COMMENT ON COLUMN t_selection_position.interview_form               IS '面试形式';
COMMENT ON COLUMN t_selection_position.reg_start_date               IS '报名开始日期';
COMMENT ON COLUMN t_selection_position.reg_end_date                 IS '报名截止日期';
COMMENT ON COLUMN t_selection_position.exam_time                    IS '笔试时间';
COMMENT ON COLUMN t_selection_position.apply_link                   IS '报名链接';
COMMENT ON COLUMN t_selection_position.position_status              IS '状态（报名中/笔试阶段/面试阶段/已结束/即将开始）';
COMMENT ON COLUMN t_selection_position.content                      IS '详细说明（支持HTML）';

COMMIT;
```

---

## 4、教师招聘岗位表 t_teacher_position

> 教师招聘是事业编中**最大的细分市场**，用户群体极大，必须独立且做深

SQL

```
-- ============================================================
-- 教师招聘岗位表 (t_teacher_position)
-- 描述：中小学/高校/幼儿园等教师招聘岗位
-- 说明：教师招聘虽属事业编，但用户群体庞大、筛选维度独特（学段、学科），需独立设计
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_teacher_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 学校信息 ====================
    school_name                 VARCHAR(200)    NOT NULL,           -- 学校名称
    school_type                 VARCHAR(30)     NOT NULL,           -- 学校类型
        -- 幼儿园/小学/初中/高中/中职/高职/大学/特殊教育学校
    school_nature               VARCHAR(20)     DEFAULT '公办',     -- 学校性质（公办/民办）
    supervising_dept            VARCHAR(200),                       -- 主管部门（如：xx区教育局）

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称（如：初中数学教师）
    subject                     VARCHAR(50)     NOT NULL,           -- 学科
        -- 语文/数学/英语/物理/化学/生物/历史/地理/政治
        -- 音乐/美术/体育/信息技术/心理健康/通用技术/科学
        -- 道德与法治/综合实践/学前教育/特殊教育/其他
    recruitment_type             VARCHAR(30)     NOT NULL,           -- 招聘类型
        -- 编制（有编制的正式教师）
        -- 合同制（合同聘用）
        -- 特岗教师（国家特岗计划）
        -- 人事代理
        -- 编外聘用

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30)     NOT NULL,           -- 省份
    city                        VARCHAR(50),                        -- 城市
    district                    VARCHAR(50),                        -- 区/县

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30),                        -- 学历要求
    degree_requirement          VARCHAR(30),                        -- 学位要求
    major_requirement           VARCHAR(500),                       -- 专业要求
    age_limit                   INTEGER,                            -- 年龄上限（周岁）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招聘人数

    -- ===== 教师岗位特殊要求（区别于其他岗位的核心字段） =====
    teacher_cert_requirement    VARCHAR(100),                       -- 教师资格证要求（如：高中及以上教师资格证）
    teacher_cert_subject        VARCHAR(50),                        -- 资格证学科要求（如：数学）
    putonghua_level             VARCHAR(30),                        -- 普通话等级要求（二级甲等/二级乙等/一级乙等）
    other_cert_requirement      VARCHAR(200),                       -- 其他证书（如：英语专八、计算机二级）
    work_experience             VARCHAR(50),                        -- 教学经验（如：2年以上、不限）
    is_normal_major             VARCHAR(20),                        -- 是否要求师范专业（要求/优先/不限）

    -- ==================== 待遇信息 ====================
    salary_range                VARCHAR(50),                        -- 薪资待遇（如：6-10k/月）
    benefits                    TEXT,                               -- 福利待遇说明

    -- ==================== 考试信息 ====================
    exam_content                VARCHAR(500),                       -- 笔试内容（如：教育综合知识+学科专业知识）
    interview_form              VARCHAR(100),                       -- 面试形式
        -- 试讲/说课/结构化面试/答辩/才艺展示（学前）
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名截止
    exam_time                   TIMESTAMPTZ,                        -- 考试时间

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 状态
    apply_link                  VARCHAR(500),                       -- 报名链接
    contact_phone               VARCHAR(50),                        -- 联系电话
    remark                      TEXT,                               -- 备注
    content                     TEXT,                               -- 详细说明（HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_tch_school_type CHECK (
        school_type IN (
            '幼儿园', '小学', '初中', '高中', '中职',
            '高职', '大学', '特殊教育学校'
        )
    ),
    CONSTRAINT chk_tch_school_nature CHECK (
        school_nature IS NULL OR school_nature IN ('公办', '民办')
    ),
    CONSTRAINT chk_tch_subject CHECK (
        subject IN (
            '语文', '数学', '英语', '物理', '化学', '生物',
            '历史', '地理', '政治', '音乐', '美术', '体育',
            '信息技术', '心理健康', '通用技术', '科学',
            '道德与法治', '综合实践', '学前教育', '特殊教育', '其他'
        )
    ),
    CONSTRAINT chk_tch_recruit_type CHECK (
        recruitment_type IN ('编制', '合同制', '特岗教师', '人事代理', '编外聘用')
    ),
    CONSTRAINT chk_tch_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_tch_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 60)
    ),
    CONSTRAINT chk_tch_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_tch_putonghua CHECK (
        putonghua_level IS NULL
        OR putonghua_level IN ('不限', '二级乙等', '二级甲等', '一级乙等', '一级甲等')
    ),
    CONSTRAINT chk_tch_normal CHECK (
        is_normal_major IS NULL
        OR is_normal_major IN ('要求', '优先', '不限')
    ),
    CONSTRAINT chk_tch_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按学科筛选（教师招聘最核心维度）
CREATE INDEX idx_tch_subject
    ON t_teacher_position (subject)
    WHERE is_deleted = FALSE;

-- 按学段（学校类型）筛选
CREATE INDEX idx_tch_school_type
    ON t_teacher_position (school_type)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_tch_location
    ON t_teacher_position (province, city, district)
    WHERE is_deleted = FALSE;

-- 按招聘类型筛选（编制/特岗/合同...用户最关心）
CREATE INDEX idx_tch_recruit_type
    ON t_teacher_position (recruitment_type)
    WHERE is_deleted = FALSE;

-- 组合：省份 + 学段 + 学科（教师招聘最常见查询）
CREATE INDEX idx_tch_province_type_subject
    ON t_teacher_position (province, school_type, subject)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_tch_education
    ON t_teacher_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 按学校性质筛选
CREATE INDEX idx_tch_school_nature
    ON t_teacher_position (school_nature)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_tch_pos_name
    ON t_teacher_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 学校名称搜索
CREATE INDEX idx_tch_school_name
    ON t_teacher_position USING btree (school_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_tch_status
    ON t_teacher_position (position_status)
    WHERE is_deleted = FALSE;

-- 按报名截止排序
CREATE INDEX idx_tch_reg_end
    ON t_teacher_position (reg_end_date ASC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_tch_position_updated_at
    BEFORE UPDATE ON t_teacher_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_teacher_position                            IS '教师招聘岗位表';
COMMENT ON COLUMN t_teacher_position.school_name                IS '学校名称';
COMMENT ON COLUMN t_teacher_position.school_type                IS '学校类型（幼儿园/小学/初中/高中/中职/高职/大学/特教）';
COMMENT ON COLUMN t_teacher_position.school_nature              IS '学校性质（公办/民办）';
COMMENT ON COLUMN t_teacher_position.supervising_dept           IS '主管教育部门';
COMMENT ON COLUMN t_teacher_position.position_name              IS '岗位名称';
COMMENT ON COLUMN t_teacher_position.subject                    IS '学科';
COMMENT ON COLUMN t_teacher_position.recruitment_type           IS '招聘类型（编制/合同制/特岗教师/人事代理/编外聘用）';
COMMENT ON COLUMN t_teacher_position.province                   IS '省份';
COMMENT ON COLUMN t_teacher_position.city                       IS '城市';
COMMENT ON COLUMN t_teacher_position.district                   IS '区/县';
COMMENT ON COLUMN t_teacher_position.teacher_cert_requirement   IS '教师资格证要求';
COMMENT ON COLUMN t_teacher_position.teacher_cert_subject       IS '资格证学科要求';
COMMENT ON COLUMN t_teacher_position.putonghua_level            IS '普通话等级要求';
COMMENT ON COLUMN t_teacher_position.is_normal_major            IS '是否要求师范专业（要求/优先/不限）';
COMMENT ON COLUMN t_teacher_position.exam_content               IS '笔试内容';
COMMENT ON COLUMN t_teacher_position.interview_form             IS '面试形式（试讲/说课/结构化/答辩）';
COMMENT ON COLUMN t_teacher_position.content                    IS '详细说明（支持HTML）';

COMMIT;
```

---

## 5、医疗卫生招聘岗位表 t_healthcare_position

> 医疗卫生系统有极强的行业特殊性：科室、执业资格、职称体系，必须单独设计

SQL

```
-- ============================================================
-- 医疗卫生招聘岗位表 (t_healthcare_position)
-- 描述：医院、疾控、社区卫生等医疗卫生机构岗位
-- 说明：医疗行业有独特的执业资格、职称体系、科室分类，独立设计
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_healthcare_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 机构信息 ====================
    institution_name            VARCHAR(200)    NOT NULL,           -- 单位名称（如：北京协和医院）
    institution_type            VARCHAR(50)     NOT NULL,           -- 机构类型
        -- 综合医院/专科医院/中医医院/社区卫生服务中心
        -- 疾控中心/妇幼保健院/卫生监督所/急救中心
        -- 血站/精神卫生中心/康复中心/其他
    institution_level           VARCHAR(30),                        -- 机构等级
        -- 三级甲等/三级乙等/二级甲等/二级乙等/一级/未定级/社区
    institution_nature          VARCHAR(20)     DEFAULT '公立',     -- 机构性质

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称（如：心内科主治医师）
    department                  VARCHAR(100),                       -- 科室
        -- 内科/外科/骨科/妇产科/儿科/急诊科/ICU/神经科
        -- 眼科/耳鼻喉科/皮肤科/口腔科/中医科/康复科
        -- 检验科/影像科/药剂科/病理科/麻醉科/超声科
        -- 行政/后勤/护理部/院感科/公共卫生/其他
    position_category           VARCHAR(30)     NOT NULL,           -- 岗位类别
        -- 临床医师/护理/药学/医技/公共卫生/行政后勤/科研
    recruitment_type            VARCHAR(30),                        -- 招聘类型
        -- 编制/合同制/人事代理/规培（住院医师规范化培训）/进修

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30)     NOT NULL,           -- 省份
    city                        VARCHAR(50),                        -- 城市
    district                    VARCHAR(50),                        -- 区县

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30),                        -- 学历要求
    degree_requirement          VARCHAR(30),                        -- 学位要求
    major_requirement           VARCHAR(500),                       -- 专业要求
    age_limit                   INTEGER,                            -- 年龄上限（周岁）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招聘人数
    work_experience             VARCHAR(50),                        -- 工作经验（如：3年以上、不限）

    -- ===== 医疗行业特殊要求（核心区分字段） =====
    license_requirement         VARCHAR(100),                       -- 执业资格要求
        -- 执业医师资格证/执业助理医师/执业护士资格证
        -- 执业药师/医学检验师/医学影像技师/放射技师/康复治疗师
        -- 公共卫生执业医师/口腔执业医师/中医执业医师
    title_requirement           VARCHAR(30),                        -- 职称要求
        -- 不限/初级(师)/中级(主治/主管)/副高级(副主任)/正高级(主任)
    规培_requirement            VARCHAR(50),                        -- 规培要求
        -- 已完成规培/规培合格证/不要求
    research_requirement        TEXT,                               -- 科研要求（如：发表SCI论文X篇）

    -- ==================== 待遇信息 ====================
    salary_range                VARCHAR(50),                        -- 薪资待遇
    benefits                    TEXT,                               -- 福利待遇（如：安家费、科研启动金、子女入学）
    housing_subsidy             VARCHAR(100),                       -- 住房补贴/安家费

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名截止
    exam_time                   TIMESTAMPTZ,                        -- 考试时间
    exam_content                VARCHAR(500),                       -- 考试内容
    apply_link                  VARCHAR(500),                       -- 报名链接

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 状态
    contact_phone               VARCHAR(50),                        -- 联系电话
    contact_person              VARCHAR(50),                        -- 联系人
    remark                      TEXT,                               -- 备注
    content                     TEXT,                               -- 详细说明（HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_hc_inst_type CHECK (
        institution_type IN (
            '综合医院', '专科医院', '中医医院', '社区卫生服务中心',
            '疾控中心', '妇幼保健院', '卫生监督所', '急救中心',
            '血站', '精神卫生中心', '康复中心', '其他'
        )
    ),
    CONSTRAINT chk_hc_inst_level CHECK (
        institution_level IS NULL
        OR institution_level IN ('三级甲等', '三级乙等', '二级甲等', '二级乙等', '一级', '未定级', '社区')
    ),
    CONSTRAINT chk_hc_inst_nature CHECK (
        institution_nature IS NULL OR institution_nature IN ('公立', '民营')
    ),
    CONSTRAINT chk_hc_pos_category CHECK (
        position_category IN ('临床医师', '护理', '药学', '医技', '公共卫生', '行政后勤', '科研')
    ),
    CONSTRAINT chk_hc_recruit_type CHECK (
        recruitment_type IS NULL
        OR recruitment_type IN ('编制', '合同制', '人事代理', '规培', '进修')
    ),
    CONSTRAINT chk_hc_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_hc_title CHECK (
        title_requirement IS NULL
        OR title_requirement IN ('不限', '初级', '中级', '副高级', '正高级')
    ),
    CONSTRAINT chk_hc_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 65)
    ),
    CONSTRAINT chk_hc_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_hc_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按岗位类别筛选（医师/护理/药学...最核心维度）
CREATE INDEX idx_hc_pos_category
    ON t_healthcare_position (position_category)
    WHERE is_deleted = FALSE;

-- 按机构类型筛选
CREATE INDEX idx_hc_inst_type
    ON t_healthcare_position (institution_type)
    WHERE is_deleted = FALSE;

-- 按机构等级筛选（三甲是核心筛选条件）
CREATE INDEX idx_hc_inst_level
    ON t_healthcare_position (institution_level)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_hc_location
    ON t_healthcare_position (province, city)
    WHERE is_deleted = FALSE;

-- 按科室筛选
CREATE INDEX idx_hc_department
    ON t_healthcare_position (department)
    WHERE is_deleted = FALSE;

-- 按招聘类型筛选
CREATE INDEX idx_hc_recruit_type
    ON t_healthcare_position (recruitment_type)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_hc_education
    ON t_healthcare_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 按职称要求筛选
CREATE INDEX idx_hc_title
    ON t_healthcare_position (title_requirement)
    WHERE is_deleted = FALSE;

-- 组合：省份 + 岗位类别 + 机构等级（最常见查询）
CREATE INDEX idx_hc_composite
    ON t_healthcare_position (province, position_category, institution_level)
    WHERE is_deleted = FALSE;

-- 机构名称搜索
CREATE INDEX idx_hc_inst_name
    ON t_healthcare_position USING btree (institution_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_hc_pos_name
    ON t_healthcare_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_hc_status
    ON t_healthcare_position (position_status)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_hc_position_updated_at
    BEFORE UPDATE ON t_healthcare_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_healthcare_position                             IS '医疗卫生招聘岗位表';
COMMENT ON COLUMN t_healthcare_position.institution_name            IS '医疗机构名称';
COMMENT ON COLUMN t_healthcare_position.institution_type            IS '机构类型';
COMMENT ON COLUMN t_healthcare_position.institution_level           IS '机构等级（三甲/三乙/二甲/...）';
COMMENT ON COLUMN t_healthcare_position.institution_nature          IS '机构性质（公立/民营）';
COMMENT ON COLUMN t_healthcare_position.position_name               IS '岗位名称';
COMMENT ON COLUMN t_healthcare_position.department                  IS '科室';
COMMENT ON COLUMN t_healthcare_position.position_category           IS '岗位类别（临床医师/护理/药学/医技/公卫/行政/科研）';
COMMENT ON COLUMN t_healthcare_position.recruitment_type            IS '招聘类型（编制/合同制/人事代理/规培/进修）';
COMMENT ON COLUMN t_healthcare_position.license_requirement         IS '执业资格证要求';
COMMENT ON COLUMN t_healthcare_position.title_requirement           IS '职称要求（不限/初级/中级/副高级/正高级）';
COMMENT ON COLUMN t_healthcare_position."规培_requirement"           IS '规培要求';
COMMENT ON COLUMN t_healthcare_position.research_requirement        IS '科研要求';
COMMENT ON COLUMN t_healthcare_position.housing_subsidy             IS '住房补贴/安家费';
COMMENT ON COLUMN t_healthcare_position.content                     IS '详细说明（支持HTML）';

COMMIT;
```

---

## 6、银行/金融系统岗位表 t_finance_position

> 银行金融虽是企业，但有独立考试体系（银行秋招/春招/人行/银保监），必须单列

SQL

```
-- ============================================================
-- 银行/金融系统岗位表 (t_finance_position)
-- 描述：银行、证券、保险、基金等金融机构招聘岗位
-- 说明：金融行业有独立招聘周期(秋招/春招)、从业资格体系，独立设计
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_finance_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 机构信息 ====================
    institution_name            VARCHAR(200)    NOT NULL,           -- 机构名称（如：中国工商银行）
    institution_category        VARCHAR(30)     NOT NULL,           -- 机构大类
        -- 银行/证券/保险/基金/信托/期货/监管机构/金融科技
    institution_type            VARCHAR(50),                        -- 机构细分
        -- 银行：国有大行/股份制银行/城商行/农商行/农信社/外资银行/政策性银行
        -- 证券：头部券商/中小券商
        -- 保险：寿险/财险
        -- 监管：人民银行/银保监局/证监局
    institution_logo            VARCHAR(500),                       -- 机构Logo
    branch_name                 VARCHAR(200),                       -- 分支机构（如：北京分行、深圳营业部）

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称（如：科技菁英岗）
    position_category           VARCHAR(50),                        -- 岗位类别
        -- 柜员/客户经理/理财经理/信贷经理/风控/合规/IT科技
        -- 管培生/投行/研究/交易/运营/人力/财务/法务/综合岗
    recruitment_type            VARCHAR(30)     NOT NULL,           -- 招聘类型
        -- 秋招/春招/社招/实习/定向

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30),                        -- 省份
    city                        VARCHAR(50),                        -- 城市
    work_location               VARCHAR(200),                       -- 详细工作地点
    is_remote                   BOOLEAN         DEFAULT FALSE,      -- 是否支持远程

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30),                        -- 学历要求
    degree_requirement          VARCHAR(30),                        -- 学位要求
    major_requirement           VARCHAR(500),                       -- 专业要求
    major_preference            TEXT[]          DEFAULT '{}',       -- 优先专业列表
    age_limit                   INTEGER,                            -- 年龄上限（周岁）
    work_experience             VARCHAR(50),                        -- 工作经验要求
    recruitment_count           INTEGER,                            -- 招聘人数

    -- ===== 金融行业特殊要求（核心区分字段） =====
    cert_requirements           TEXT[]          DEFAULT '{}',       -- 证书要求列表
        -- {银行从业, 证券从业, 基金从业, 期货从业, CPA, CFA, FRM, ACCA, 精算师}
    language_requirement        VARCHAR(100),                       -- 语言要求（如：英语六级425分以上、雅思6.5）
    computer_requirement        VARCHAR(100),                       -- 计算机要求（如：计算机二级、熟悉Python）
    other_requirement           TEXT,                               -- 其他要求

    -- ==================== 薪资待遇 ====================
    salary_min                  INTEGER,                            -- 最低薪资（k/月）
    salary_max                  INTEGER,                            -- 最高薪资（k/月）
    salary_text                 VARCHAR(100),                       -- 薪资说明文本
    benefits                    TEXT,                               -- 福利待遇

    -- ==================== 考试信息（银行有统一笔试） ====================
    exam_content                VARCHAR(500),                       -- 考试内容
        -- 银行：行测+英语+综合知识+性格测试
        -- 人行：行测+申论+金融/经济
        -- 券商：专业知识+综合素质
    exam_time                   TIMESTAMPTZ,                        -- 考试时间
    interview_rounds            VARCHAR(100),                       -- 面试轮次（如：初面+终面、群面+单面）

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名截止
    apply_link                  VARCHAR(500),                       -- 报名/网申链接

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 状态
    contact_info                VARCHAR(200),                       -- 联系方式
    remark                      TEXT,                               -- 备注
    content                     TEXT,                               -- 详细说明（HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_fin_inst_category CHECK (
        institution_category IN (
            '银行', '证券', '保险', '基金', '信托', '期货', '监管机构', '金融科技'
        )
    ),
    CONSTRAINT chk_fin_recruit_type CHECK (
        recruitment_type IN ('秋招', '春招', '社招', '实习', '定向')
    ),
    CONSTRAINT chk_fin_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_fin_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 45)
    ),
    CONSTRAINT chk_fin_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_fin_salary CHECK (
        salary_min IS NULL OR salary_max IS NULL
        OR salary_min <= salary_max
    ),
    CONSTRAINT chk_fin_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按机构大类筛选（银行/证券/保险...最核心维度）
CREATE INDEX idx_fin_inst_category
    ON t_finance_position (institution_category)
    WHERE is_deleted = FALSE;

-- 按机构细分类型筛选
CREATE INDEX idx_fin_inst_type
    ON t_finance_position (institution_type)
    WHERE is_deleted = FALSE;

-- 按招聘类型筛选（秋招/春招/社招）
CREATE INDEX idx_fin_recruit_type
    ON t_finance_position (recruitment_type)
    WHERE is_deleted = FALSE;

-- 按岗位类别筛选
CREATE INDEX idx_fin_pos_category
    ON t_finance_position (position_category)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_fin_location
    ON t_finance_position (province, city)
    WHERE is_deleted = FALSE;

-- 组合：机构大类 + 招聘类型 + 省份（最常见查询）
CREATE INDEX idx_fin_composite
    ON t_finance_position (institution_category, recruitment_type, province)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_fin_education
    ON t_finance_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 证书要求 GIN 索引
CREATE INDEX idx_fin_certs
    ON t_finance_position USING gin (cert_requirements)
    WHERE is_deleted = FALSE;

-- 优先专业 GIN 索引
CREATE INDEX idx_fin_major_pref
    ON t_finance_position USING gin (major_preference)
    WHERE is_deleted = FALSE;

-- 机构名称搜索
CREATE INDEX idx_fin_inst_name
    ON t_finance_position USING btree (institution_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_fin_pos_name
    ON t_finance_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按薪资筛选
CREATE INDEX idx_fin_salary
    ON t_finance_position (salary_min, salary_max)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_fin_status
    ON t_finance_position (position_status)
    WHERE is_deleted = FALSE;

-- 按报名截止排序
CREATE INDEX idx_fin_reg_end
    ON t_finance_position (reg_end_date ASC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_fin_position_updated_at
    BEFORE UPDATE ON t_finance_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_finance_position                            IS '银行/金融系统岗位表';
COMMENT ON COLUMN t_finance_position.institution_name           IS '金融机构名称';
COMMENT ON COLUMN t_finance_position.institution_category       IS '机构大类（银行/证券/保险/基金/信托/期货/监管/金融科技）';
COMMENT ON COLUMN t_finance_position.institution_type           IS '机构细分类型';
COMMENT ON COLUMN t_finance_position.institution_logo           IS '机构Logo';
COMMENT ON COLUMN t_finance_position.branch_name                IS '分支机构名称';
COMMENT ON COLUMN t_finance_position.position_name              IS '岗位名称';
COMMENT ON COLUMN t_finance_position.position_category          IS '岗位类别';
COMMENT ON COLUMN t_finance_position.recruitment_type           IS '招聘类型（秋招/春招/社招/实习/定向）';
COMMENT ON COLUMN t_finance_position.cert_requirements          IS '证书要求列表';
COMMENT ON COLUMN t_finance_position.language_requirement       IS '语言要求';
COMMENT ON COLUMN t_finance_position.computer_requirement       IS '计算机要求';
COMMENT ON COLUMN t_finance_position.exam_content               IS '考试内容';
COMMENT ON COLUMN t_finance_position.interview_rounds           IS '面试轮次说明';
COMMENT ON COLUMN t_finance_position.apply_link                 IS '网申/报名链接';
COMMENT ON COLUMN t_finance_position.content                    IS '详细说明（支持HTML）';

COMMIT;
```

---

## 7、全平台表全景图（当前进度）

text

```
╔══════════════════════════════════════════════════════════════════╗
║                    全国就业信息平台 - 数据架构                      ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  ┌─────────────────────────────────────────────────────────┐    ║
║  │              t_job_index（统一搜索索引）          ✅ NEW  │    ║
║  │  ← 所有岗位表同步写入，支撑全站搜索/筛选/排行/推荐       │    ║
║  └───────────────────────┬─────────────────────────────────┘    ║
║                          │ source_type + source_id               ║
║          ┌───────┬───────┼───────┬──────┬──────┬──────┬──────┐  ║
║          ▼       ▼       ▼       ▼      ▼      ▼      ▼      ▼  ║
║                                                                  ║
║  ┌─── 体制内 ────────────────────────────────────────────────┐  ║
║  │                                                            │  ║
║  │  t_civil_position        公务员（国考/省考）         ✅ 已有 │  ║
║  │  t_institution_position  事业编                     ✅ 已有 │  ║
║  │  t_military_position     部队文职                   ✅ 已有 │  ║
║  │  t_selection_position    选调生                     ✅ NEW  │  ║
║  │                                                            │  ║
║  └────────────────────────────────────────────────────────────┘  ║
║                                                                  ║
║  ┌─── 行业专项 ──────────────────────────────────────────────┐  ║
║  │                                                            │  ║
║  │  t_teacher_position      教师招聘                   ✅ NEW  │  ║
║  │  t_healthcare_position   医疗卫生                   ✅ NEW  │  ║
║  │  t_finance_position      银行/金融                  ✅ NEW  │  ║
║  │                                                            │  ║
║  └────────────────────────────────────────────────────────────┘  ║
║                                                                  ║
║  ┌─── 企业 ──────────────────────────────────────────────────┐  ║
║  │                                                            │  ║
║  │  t_enterprise            企业主体（央/国/民/外企）   ✅ 已有 │  ║
║  │  t_enterprise_position   企业岗位                   ✅ 已有 │  ║
║  │                                                            │  ║
║  └────────────────────────────────────────────────────────────┘  ║
║                                                                  ║
║  ┌─── 待开发（下一轮） ──────────────────────────────────────┐  ║
║  │                                                            │  ║
║  │  t_notice            统一公告表（替换3张公告表）     🔜 下轮 │  ║
║  │  t_exam_guide        统一备考指南（替换2张方法论表） 🔜 下轮 │  ║
║  │  三支一扶 / 西部计划 / 社区工作者 / 公益岗位        🔜 下轮 │  ║
║  │                                                            │  ║
║  └────────────────────────────────────────────────────────────┘  ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

---





---

## 8、基层服务项目岗位表 t_grassroots_project_position

> **三支一扶 + 西部计划** 合为一张表  
> 二者都是国家基层服务项目，结构 90% 相同，用 `project_type` 区分

SQL

```
-- ============================================================
-- 基层服务项目岗位表 (t_grassroots_project_position)
-- 描述：三支一扶、大学生志愿服务西部计划等基层服务项目岗位
-- 设计理由：二者都是面向高校毕业生的基层服务项目，结构高度相似
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_grassroots_project_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 项目分类 ====================
    project_type                VARCHAR(30)     NOT NULL,
        -- 三支一扶
        -- 西部计划
    year                        VARCHAR(10)     NOT NULL,           -- 招募年份（如：2025）

    -- ==================== 岗位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称（如：支教教师、乡镇农技推广）
    service_type                VARCHAR(50)     NOT NULL,           -- 服务类型
        -- 三支一扶：支教/支农/支医/帮扶乡村振兴
        -- 西部计划：基础教育/服务三农/医疗卫生/基层青年工作
        --          基层社会管理/服务新疆/服务西藏
    organizing_dept             VARCHAR(200),                       -- 组织单位
        -- 三支一扶：省人社厅
        -- 西部计划：团省委/团中央
    service_unit                VARCHAR(200),                       -- 服务单位（如：xx县xx乡卫生院）

    -- ==================== 服务地点 ====================
    province                    VARCHAR(30)     NOT NULL,           -- 省份
    city                        VARCHAR(50),                        -- 市
    county                      VARCHAR(50),                        -- 区/县
    township                    VARCHAR(100),                       -- 乡镇/街道（基层服务精确到乡镇）

    -- ==================== 服务期信息（基层项目独有核心字段） ====================
    service_period              VARCHAR(30)     NOT NULL,           -- 服务期限（如：2年、1-3年）
    service_start_date          VARCHAR(30),                        -- 服务开始日期
    service_end_date            VARCHAR(30),                        -- 服务结束日期

    -- ==================== 报名要求 ====================
    education_requirement       VARCHAR(30)     NOT NULL,           -- 学历要求
    major_requirement           VARCHAR(500),                       -- 专业要求
    age_limit                   INTEGER,                            -- 年龄上限（周岁）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招募人数
    grad_year_requirement       VARCHAR(50),                        -- 毕业年份要求（如：2024/2025届）
    household_requirement       VARCHAR(100),                       -- 户籍要求（如：本省生源或本省高校毕业）
    other_requirement           TEXT,                               -- 其他要求
    political_status            VARCHAR(30),                        -- 政治面貌

    -- ==================== 考试信息 ====================
    exam_content                VARCHAR(500),                       -- 笔试内容
        -- 三支一扶：职业能力测验 + 综合知识（各省不同）
        -- 西部计划：通常免笔试，面试/综合考察
    exam_time                   TIMESTAMPTZ,                        -- 考试时间
    interview_form              VARCHAR(100),                       -- 面试形式

    -- ==================== 待遇政策（基层项目核心关注点） ====================
    monthly_subsidy             VARCHAR(50),                        -- 月补贴标准（如：3500元/月）
    social_insurance            VARCHAR(200),                       -- 社保缴纳说明
    housing_info                VARCHAR(200),                       -- 住房安排（如：提供免费宿舍）
    other_benefits              TEXT,                               -- 其他福利

    -- ===== 期满政策（用户最关心） =====
    after_service_policy        TEXT,                               -- 期满政策综述
    can_transfer_to_civil       BOOLEAN         DEFAULT FALSE,      -- 期满可否定向考公务员
    can_transfer_to_institution BOOLEAN         DEFAULT FALSE,      -- 期满可否转事业编
    exam_bonus_points           VARCHAR(50),                        -- 考试加分政策（如：笔试加5分）
    tuition_compensation        VARCHAR(100),                       -- 学费补偿/助学贷款代偿
    postgrad_bonus              VARCHAR(100),                       -- 考研加分（如：初试加10分）

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名截止
    apply_link                  VARCHAR(500),                       -- 报名链接
    position_status             VARCHAR(20)     DEFAULT '招募中',    -- 状态

    -- ==================== 补充信息 ====================
    contact_phone               VARCHAR(50),                        -- 联系电话
    remark                      TEXT,                               -- 备注
    content                     TEXT,                               -- 详细说明（HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_gp_project_type CHECK (
        project_type IN ('三支一扶', '西部计划')
    ),
    CONSTRAINT chk_gp_service_type CHECK (
        service_type IN (
            -- 三支一扶
            '支教', '支农', '支医', '帮扶乡村振兴',
            -- 西部计划
            '基础教育', '服务三农', '医疗卫生', '基层青年工作',
            '基层社会管理', '服务新疆', '服务西藏'
        )
    ),
    CONSTRAINT chk_gp_education CHECK (
        education_requirement IN ('大专', '本科', '硕士', '大专及以上', '本科及以上')
    ),
    CONSTRAINT chk_gp_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 35)
    ),
    CONSTRAINT chk_gp_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_gp_status CHECK (
        position_status IS NULL
        OR position_status IN ('招募中', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按项目类型筛选（三支一扶 vs 西部计划）
CREATE INDEX idx_gp_project_type
    ON t_grassroots_project_position (project_type)
    WHERE is_deleted = FALSE;

-- 按服务类型筛选（支教/支农/支医/...）
CREATE INDEX idx_gp_service_type
    ON t_grassroots_project_position (service_type)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_gp_location
    ON t_grassroots_project_position (province, city, county)
    WHERE is_deleted = FALSE;

-- 组合：项目类型 + 省份 + 年份
CREATE INDEX idx_gp_type_province_year
    ON t_grassroots_project_position (project_type, province, year)
    WHERE is_deleted = FALSE;

-- 按年份筛选
CREATE INDEX idx_gp_year
    ON t_grassroots_project_position (year)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_gp_education
    ON t_grassroots_project_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_gp_pos_name
    ON t_grassroots_project_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_gp_status
    ON t_grassroots_project_position (position_status)
    WHERE is_deleted = FALSE;

-- 期满可转编的（用户高度关注）
CREATE INDEX idx_gp_can_transfer
    ON t_grassroots_project_position (can_transfer_to_institution)
    WHERE is_deleted = FALSE AND can_transfer_to_institution = TRUE;

-- 触发器
CREATE TRIGGER trg_gp_position_updated_at
    BEFORE UPDATE ON t_grassroots_project_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_grassroots_project_position                                 IS '基层服务项目岗位表（三支一扶+西部计划）';
COMMENT ON COLUMN t_grassroots_project_position.project_type                    IS '项目类型（三支一扶/西部计划）';
COMMENT ON COLUMN t_grassroots_project_position.year                            IS '招募年份';
COMMENT ON COLUMN t_grassroots_project_position.service_type                    IS '服务类型';
COMMENT ON COLUMN t_grassroots_project_position.organizing_dept                 IS '组织单位';
COMMENT ON COLUMN t_grassroots_project_position.service_unit                    IS '服务单位';
COMMENT ON COLUMN t_grassroots_project_position.township                        IS '乡镇/街道';
COMMENT ON COLUMN t_grassroots_project_position.service_period                  IS '服务期限';
COMMENT ON COLUMN t_grassroots_project_position.monthly_subsidy                 IS '月补贴标准';
COMMENT ON COLUMN t_grassroots_project_position.after_service_policy            IS '期满政策综述';
COMMENT ON COLUMN t_grassroots_project_position.can_transfer_to_civil           IS '期满可否定向考公';
COMMENT ON COLUMN t_grassroots_project_position.can_transfer_to_institution     IS '期满可否转事业编';
COMMENT ON COLUMN t_grassroots_project_position.exam_bonus_points               IS '考试加分政策';
COMMENT ON COLUMN t_grassroots_project_position.tuition_compensation            IS '学费补偿/助学贷款代偿';
COMMENT ON COLUMN t_grassroots_project_position.postgrad_bonus                  IS '考研加分';
COMMENT ON COLUMN t_grassroots_project_position.content                         IS '详细说明（支持HTML）';

COMMIT;
```

---

## 9、社区工作者岗位表 t_community_position

> 社区工作者是中国基层治理的重要力量，近年来招聘量井喷式增长

SQL

```
-- ============================================================
-- 社区工作者岗位表 (t_community_position)
-- 描述：社区工作者/网格员/社工招聘岗位
-- 说明：社区岗位有独特的服务属性（网格化管理、社区治理），单独设计
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_community_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 招聘单位 ====================
    street_office               VARCHAR(200)    NOT NULL,           -- 街道办事处/乡镇名称
    community_name              VARCHAR(200),                       -- 社区名称（具体社区）
    supervising_dept            VARCHAR(200),                       -- 主管部门（如：区委组织部、区民政局）
    district                    VARCHAR(100),                       -- 区/县

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称
    position_type               VARCHAR(50)     NOT NULL,           -- 岗位类型
        -- 社区党务工作者/社区服务工作者/社区网格员
        -- 社区调解员/社区安全员/社区文化专干
        -- 社会工作师/综合岗/其他
    employment_type             VARCHAR(30)     NOT NULL,           -- 用工形式
        -- 事业编制/合同制/政府购买服务/公益性岗位

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30)     NOT NULL,           -- 省份
    city                        VARCHAR(50)     NOT NULL,           -- 城市
    work_location               VARCHAR(200),                       -- 详细工作地点

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30),                        -- 学历要求
    age_limit                   INTEGER,                            -- 年龄上限（周岁，社区岗通常较宽松）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招聘人数
    major_requirement           VARCHAR(500),                       -- 专业要求（通常不限或偏社工类）
    household_requirement       VARCHAR(100),                       -- 户籍要求（社区岗通常要求本地户籍）
    political_status            VARCHAR(30),                        -- 政治面貌
    work_experience             VARCHAR(50),                        -- 工作经验

    -- ===== 社区岗位特殊要求 =====
    social_work_cert            VARCHAR(50),                        -- 社工证要求
        -- 不要求/初级社工师/中级社工师/高级社工师/优先
    community_experience        VARCHAR(100),                       -- 社区工作经验（如：有社区工作经历优先）
    residence_requirement       VARCHAR(200),                       -- 居住要求（如：需在本社区居住）

    -- ==================== 待遇信息 ====================
    salary_range                VARCHAR(50),                        -- 薪资待遇（如：4-6k/月）
    salary_composition          VARCHAR(200),                       -- 薪资构成（如：基本工资+绩效+五险一金）
    benefits                    TEXT,                               -- 福利待遇说明

    -- ==================== 考试信息 ====================
    exam_content                VARCHAR(500),                       -- 笔试内容
        -- 通常：社区工作基础知识、综合能力、时事政治
    interview_form              VARCHAR(100),                       -- 面试形式
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名截止
    exam_time                   TIMESTAMPTZ,                        -- 考试时间

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 状态
    apply_link                  VARCHAR(500),                       -- 报名链接
    apply_method                TEXT,                               -- 报名方式（有些是现场报名）
    contact_phone               VARCHAR(50),                        -- 联系电话
    contact_address             VARCHAR(200),                       -- 联系地址（现场报名需要）
    remark                      TEXT,                               -- 备注
    content                     TEXT,                               -- 详细说明（HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_com_pos_type CHECK (
        position_type IN (
            '社区党务工作者', '社区服务工作者', '社区网格员',
            '社区调解员', '社区安全员', '社区文化专干',
            '社会工作师', '综合岗', '其他'
        )
    ),
    CONSTRAINT chk_com_employ_type CHECK (
        employment_type IN ('事业编制', '合同制', '政府购买服务', '公益性岗位')
    ),
    CONSTRAINT chk_com_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '高中', '大专', '本科', '硕士')
    ),
    CONSTRAINT chk_com_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 55)
    ),
    CONSTRAINT chk_com_social_cert CHECK (
        social_work_cert IS NULL
        OR social_work_cert IN ('不要求', '初级社工师', '中级社工师', '高级社工师', '优先')
    ),
    CONSTRAINT chk_com_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_com_status CHECK (
        position_status IS NULL
        OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按岗位类型筛选（网格员/社工/综合岗...）
CREATE INDEX idx_com_pos_type
    ON t_community_position (position_type)
    WHERE is_deleted = FALSE;

-- 按用工形式筛选（用户最关心是否有编制）
CREATE INDEX idx_com_employ_type
    ON t_community_position (employment_type)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_com_location
    ON t_community_position (province, city, district)
    WHERE is_deleted = FALSE;

-- 组合：省份 + 城市 + 岗位类型
CREATE INDEX idx_com_city_type
    ON t_community_position (province, city, position_type)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_com_education
    ON t_community_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 按社工证要求筛选
CREATE INDEX idx_com_social_cert
    ON t_community_position (social_work_cert)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_com_pos_name
    ON t_community_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 街道名称搜索
CREATE INDEX idx_com_street
    ON t_community_position USING btree (street_office varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_com_status
    ON t_community_position (position_status)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_com_position_updated_at
    BEFORE UPDATE ON t_community_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_community_position                          IS '社区工作者岗位表';
COMMENT ON COLUMN t_community_position.street_office            IS '街道办事处/乡镇';
COMMENT ON COLUMN t_community_position.community_name           IS '社区名称';
COMMENT ON COLUMN t_community_position.supervising_dept         IS '主管部门';
COMMENT ON COLUMN t_community_position.position_type            IS '岗位类型';
COMMENT ON COLUMN t_community_position.employment_type          IS '用工形式（事业编/合同/购买服务/公益岗）';
COMMENT ON COLUMN t_community_position.social_work_cert         IS '社工证要求';
COMMENT ON COLUMN t_community_position.community_experience     IS '社区工作经验要求';
COMMENT ON COLUMN t_community_position.residence_requirement    IS '居住地要求';
COMMENT ON COLUMN t_community_position.salary_composition       IS '薪资构成说明';
COMMENT ON COLUMN t_community_position.apply_method             IS '报名方式（在线/现场）';
COMMENT ON COLUMN t_community_position.contact_address          IS '现场报名地址';
COMMENT ON COLUMN t_community_position.content                  IS '详细说明（支持HTML）';

COMMIT;
```

---

## 10、公益性岗位表 t_public_welfare_position

> 政府出资开发，面向就业困难群体的托底性岗位，有明确的岗位期限和补贴标准

SQL

```
-- ============================================================
-- 公益性岗位表 (t_public_welfare_position)
-- 描述：政府出资开发的公益性就业岗位
-- 说明：面向就业困难人员、零就业家庭等群体的托底性岗位
-- ============================================================
BEGIN;

CREATE TABLE IF NOT EXISTS t_public_welfare_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 开发单位 ====================
    developing_unit             VARCHAR(200)    NOT NULL,           -- 岗位开发单位（如：xx区人社局、xx街道办）
    employing_unit              VARCHAR(200),                       -- 实际用工单位（如：xx社区、xx公园管理处）

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称
    position_category           VARCHAR(50)     NOT NULL,           -- 岗位类别
        -- 公共管理类：交通协管/城管协管/治安协管/市场协管
        -- 公共服务类：社区服务/养老助残/就业援助/法律援助
        -- 公共环境类：环卫保洁/绿化养护/河道管护
        -- 公共安全类：保安/消防协管/校园安全
        -- 设施维护类：道路养护/公共设施维护
        -- 其他
    work_content                TEXT,                               -- 工作内容描述

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30)     NOT NULL,           -- 省份
    city                        VARCHAR(50)     NOT NULL,           -- 城市
    district                    VARCHAR(50),                        -- 区/县
    work_location               VARCHAR(200),                       -- 详细工作地点

    -- ==================== 面向人群（公益岗独有核心字段） ====================
    target_group                TEXT[]          DEFAULT '{}',       -- 面向人群
        -- {就业困难人员, 零就业家庭成员, 大龄失业人员,
        --  残疾人, 退役军人, 建档立卡贫困户,
        --  高校毕业生(离校2年未就业), 低保家庭成员, 刑满释放人员}

    -- ==================== 报名要求（通常门槛较低） ====================
    education_requirement       VARCHAR(30)     DEFAULT '不限',     -- 学历要求（通常不限或初中）
    age_range                   VARCHAR(50),                        -- 年龄范围（如：18-55周岁，公益岗年龄宽松）
    health_requirement          VARCHAR(200),                       -- 身体条件（如：身体健康、能胜任户外工作）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招聘人数
    household_requirement       VARCHAR(100),                       -- 户籍要求（通常要求本地户籍）
    employment_difficulty_cert  BOOLEAN         DEFAULT FALSE,      -- 是否需要就业困难人员认定证明
    other_requirement           TEXT,                               -- 其他要求

    -- ==================== 岗位期限与待遇（公益岗核心字段） ====================
    contract_period             VARCHAR(30)     NOT NULL,           -- 合同期限（如：1年、3年、最长不超过3年）
    is_renewable                BOOLEAN         DEFAULT FALSE,      -- 是否可续签
    max_service_years           INTEGER,                            -- 最长服务年限
    monthly_salary              VARCHAR(50),                        -- 月工资标准（如：当地最低工资标准）
    salary_source               VARCHAR(100),                       -- 工资来源（如：财政全额拨款）
    subsidy_standard            VARCHAR(200),                       -- 岗位补贴标准
    social_insurance_info       VARCHAR(200),                       -- 社保缴纳说明
    other_benefits              TEXT,                               -- 其他福利

    -- ==================== 工作时间（公益岗特有） ====================
    work_schedule               VARCHAR(100),                       -- 工作时间安排（如：早7:00-晚5:00、轮班制）
    is_shift_work               BOOLEAN         DEFAULT FALSE,      -- 是否倒班

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ,                        -- 报名开始
    reg_end_date                TIMESTAMPTZ,                        -- 报名截止
    apply_method                TEXT,                               -- 报名方式（通常现场报名）
    apply_address               VARCHAR(200),                       -- 报名地点
    required_documents          TEXT,                               -- 需携带材料（身份证/户口本/就业困难认定/...）

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 状态
    contact_phone               VARCHAR(50),                        -- 联系电话
    contact_person              VARCHAR(50),                        -- 联系人
    remark                      TEXT,                               -- 备注
    content                     TEXT,                               -- 详细说明（HTML）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
);

-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按岗位类别筛选
CREATE INDEX idx_pw_category
    ON t_public_welfare_position (position_category)
    WHERE is_deleted = FALSE;

-- 按地区筛选
CREATE INDEX idx_pw_location
    ON t_public_welfare_position (province, city, district)
    WHERE is_deleted = FALSE;

-- 面向人群 GIN 索引（如：查所有面向退役军人的岗位）
CREATE INDEX idx_pw_target_group
    ON t_public_welfare_position USING gin (target_group)
    WHERE is_deleted = FALSE;

-- 按学历筛选基层服务
CREATE INDEX idx_pw_education
    ON t_public_welfare_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 组合：省份 + 城市 + 岗位类别
CREATE INDEX idx_pw_city_category
    ON t_public_welfare_position (province, city, position_category)
    WHERE is_deleted = FALSE;

-- 岗位名称搜索
CREATE INDEX idx_pw_pos_name
    ON t_public_welfare_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按状态筛选
CREATE INDEX idx_pw_status
    ON t_public_welfare_position (position_status)
    WHERE is_deleted = FALSE;

-- 是否需要就业困难认定
CREATE INDEX idx_pw_difficulty_cert
    ON t_public_welfare_position (employment_difficulty_cert)
    WHERE is_deleted = FALSE;

-- 触发器
CREATE TRIGGER trg_pw_position_updated_at
    BEFORE UPDATE ON t_public_welfare_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_public_welfare_position                             IS '公益性岗位表';
COMMENT ON COLUMN t_public_welfare_position.developing_unit             IS '岗位开发单位';
COMMENT ON COLUMN t_public_welfare_position.employing_unit              IS '实际用工单位';
COMMENT ON COLUMN t_public_welfare_position.position_category           IS '岗位类别（公共管理/公共服务/公共环境/公共安全/设施维护/其他）';
COMMENT ON COLUMN t_public_welfare_position.work_content                IS '工作内容描述';
COMMENT ON COLUMN t_public_welfare_position.target_group                IS '面向人群列表';
COMMENT ON COLUMN t_public_welfare_position.age_range                   IS '年龄范围';
COMMENT ON COLUMN t_public_welfare_position.health_requirement          IS '身体条件要求';
COMMENT ON COLUMN t_public_welfare_position.employment_difficulty_cert  IS '是否需要就业困难认定证明';
COMMENT ON COLUMN t_public_welfare_position.contract_period             IS '合同期限';
COMMENT ON COLUMN t_public_welfare_position.is_renewable                IS '是否可续签';
COMMENT ON COLUMN t_public_welfare_position.max_service_years           IS '最长服务年限';
COMMENT ON COLUMN t_public_welfare_position.monthly_salary              IS '月工资标准';
COMMENT ON COLUMN t_public_welfare_position.salary_source               IS '工资来源';
COMMENT ON COLUMN t_public_welfare_position.subsidy_standard            IS '岗位补贴标准';
COMMENT ON COLUMN t_public_welfare_position.work_schedule               IS '工作时间安排';
COMMENT ON COLUMN t_public_welfare_position.is_shift_work               IS '是否倒班';
COMMENT ON COLUMN t_public_welfare_position.apply_method                IS '报名方式（通常现场报名）';
COMMENT ON COLUMN t_public_welfare_position.apply_address               IS '报名地点';
COMMENT ON COLUMN t_public_welfare_position.required_documents          IS '报名需携带材料';
COMMENT ON COLUMN t_public_welfare_position.content                     IS '详细说明（支持HTML）';

COMMIT;
```

---

## 11、公务员考试职位表




```
-- ============================================================
-- 公务员考试职位表 (t_civil_position)
-- 描述：国考/省考招录职位信息
-- ============================================================



CREATE TABLE IF NOT EXISTS t_civil_position (

    id                          SERIAL          PRIMARY KEY,

    -- ==================== 职位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 职位名称
    exam_type                   VARCHAR(20)     NOT NULL,           -- 考试类型（国考/省考）
    recruiting_dept             VARCHAR(200),                       -- 招录部门
    dept_code                   VARCHAR(30),                        -- 部门代码
    position_code               VARCHAR(30),                        -- 职位代码
    affiliated_bureau           VARCHAR(200),                       -- 所属司局

    -- ==================== 报考要求 ====================
    major_requirement           VARCHAR(500),                       -- 专业要求
    min_education               VARCHAR(20),                        -- 最低学历要求
    degree_requirement          VARCHAR(20),                        -- 学位要求
    political_status            VARCHAR(30),                        -- 政治面貌
    work_experience             VARCHAR(50),                        -- 工作年限（如：2年以上、不限）
    grassroots_experience       VARCHAR(50),                        -- 基层经验年限（如：2年、不限）

    -- ==================== 考试信息 ====================
    exam_category               VARCHAR(50),                        -- 考试类别（如：行政执法类、综合管理类）
    interview_ratio             VARCHAR(20),                        -- 面试比例（如：3:1、5:1）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招录人数
    has_professional_test       BOOLEAN         DEFAULT FALSE,      -- 专业测试（是/否）

    -- ==================== 工作地点 ====================
    work_location               VARCHAR(100),                       -- 工作地点（如：北京市）
    work_location_detail        VARCHAR(200),                       -- 具体工作地点（如：北京市海淀区xx路xx号）
    household_requirement       VARCHAR(100),                       -- 户籍要求（如：不限、本省户籍）
    household_location          VARCHAR(100),                       -- 户籍地

    -- ==================== 其他信息 ====================
    position_intro              TEXT,                               -- 职位简介
    remark                      TEXT,                               -- 备注
    official_website            VARCHAR(500),                       -- 官方网站
    contact_phone               VARCHAR(50),                        -- 联系电话

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                -- 报名开始日期（如：2025-10-15）
    reg_end_date                TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                -- 报名结束日期（如：2025-10-24）
    reg_status                  VARCHAR(20)     DEFAULT '即将开始',  -- 报名状态
    applicant_count             INTEGER         DEFAULT 0,          -- 报名人数

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_position_code
        UNIQUE (exam_type, dept_code, position_code),

    CONSTRAINT chk_exam_type CHECK (
        exam_type IN ('国考', '省考')
    ),
    CONSTRAINT chk_min_education CHECK (
        min_education IS NULL OR min_education IN ('不限','大专','本科','硕士','博士')
    ),
    CONSTRAINT chk_degree_requirement CHECK (
        degree_requirement IS NULL OR degree_requirement IN ('不限','学士','硕士','博士')
    ),
    CONSTRAINT chk_political_status CHECK (
        political_status IS NULL OR political_status IN ('不限','中共党员','共青团员','群众')
    ),
    CONSTRAINT chk_reg_status CHECK (
        reg_status IS NULL OR reg_status IN ('报名中','已结束','即将开始')
    ),
    CONSTRAINT chk_recruitment_count CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_applicant_count CHECK (
        applicant_count IS NULL OR applicant_count >= 0
    )
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按考试类型筛选
CREATE INDEX idx_civil_exam_type
    ON t_civil_position (exam_type) WHERE is_deleted = FALSE;

-- 按招录部门筛选
CREATE INDEX idx_civil_dept
    ON t_civil_position (recruiting_dept) WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_civil_education
    ON t_civil_position (min_education) WHERE is_deleted = FALSE;

-- 按工作地点筛选
CREATE INDEX idx_civil_location
    ON t_civil_position (work_location) WHERE is_deleted = FALSE;

-- 按报名状态筛选
CREATE INDEX idx_civil_reg_status
    ON t_civil_position (reg_status) WHERE is_deleted = FALSE;

-- 职位名称搜索
CREATE INDEX idx_civil_position_name
    ON t_civil_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 专业要求搜索
CREATE INDEX idx_civil_major
    ON t_civil_position USING btree (major_requirement varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按报名人数排序（热门职位）
CREATE INDEX idx_civil_applicant_count
    ON t_civil_position (applicant_count DESC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 竞争比排序（报名人数/招录人数）
CREATE INDEX idx_civil_competition_ratio
    ON t_civil_position (applicant_count DESC, recruitment_count ASC)
    WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_civil_position_updated_at
    BEFORE UPDATE ON t_civil_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_civil_position                              IS '公务员考试职位表';
COMMENT ON COLUMN t_civil_position.position_name                IS '职位名称';
COMMENT ON COLUMN t_civil_position.exam_type                    IS '考试类型（国考/省考）';
COMMENT ON COLUMN t_civil_position.recruiting_dept              IS '招录部门';
COMMENT ON COLUMN t_civil_position.dept_code                    IS '部门代码';
COMMENT ON COLUMN t_civil_position.position_code                IS '职位代码';
COMMENT ON COLUMN t_civil_position.affiliated_bureau            IS '所属司局';
COMMENT ON COLUMN t_civil_position.major_requirement            IS '专业要求';
COMMENT ON COLUMN t_civil_position.min_education                IS '最低学历要求（不限/大专/本科/硕士/博士）';
COMMENT ON COLUMN t_civil_position.degree_requirement           IS '学位要求（不限/学士/硕士/博士）';
COMMENT ON COLUMN t_civil_position.political_status             IS '政治面貌（不限/中共党员/共青团员/群众）';
COMMENT ON COLUMN t_civil_position.work_experience              IS '工作年限要求';
COMMENT ON COLUMN t_civil_position.grassroots_experience        IS '基层经验年限要求';
COMMENT ON COLUMN t_civil_position.exam_category                IS '考试类别（行政执法类/综合管理类等）';
COMMENT ON COLUMN t_civil_position.interview_ratio              IS '面试比例（如：3:1）';
COMMENT ON COLUMN t_civil_position.recruitment_count            IS '招录人数';
COMMENT ON COLUMN t_civil_position.has_professional_test        IS '是否有专业测试';
COMMENT ON COLUMN t_civil_position.work_location                IS '工作地点（市级）';
COMMENT ON COLUMN t_civil_position.work_location_detail         IS '具体工作地址';
COMMENT ON COLUMN t_civil_position.household_requirement        IS '户籍要求';
COMMENT ON COLUMN t_civil_position.household_location           IS '户籍地';
COMMENT ON COLUMN t_civil_position.position_intro               IS '职位简介';
COMMENT ON COLUMN t_civil_position.remark                       IS '备注';
COMMENT ON COLUMN t_civil_position.official_website             IS '官方网站';
COMMENT ON COLUMN t_civil_position.contact_phone                IS '联系电话';
COMMENT ON COLUMN t_civil_position.reg_start_date               IS '报名开始日期';
COMMENT ON COLUMN t_civil_position.reg_end_date                 IS '报名结束日期';
COMMENT ON COLUMN t_civil_position.reg_status                   IS '报名状态（报名中/已结束/即将开始）';
COMMENT ON COLUMN t_civil_position.applicant_count              IS '报名人数';

COMMIT;
```

---


## 12、事业编职位表



```
-- ============================================================
-- 事业编职位表 (t_institution_position)
-- 描述：事业单位招聘职位信息
-- ============================================================


CREATE TABLE IF NOT EXISTS t_institution_position (

    id                          SERIAL          PRIMARY KEY,

    -- ==================== 职位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 职位名称
    supervising_dept            VARCHAR(200),                       -- 主管部门
    institution                 VARCHAR(200),                       -- 单位
    work_location               VARCHAR(100),                       -- 工作地点
    province                    VARCHAR(30),                        -- 省份
    exam_category               VARCHAR(50),                        -- 考试类别（如：综合类、教育类、卫生类）
    position_type               VARCHAR(50),                        -- 职位类型（如：管理岗、专技岗、工勤岗）
    sub_category                VARCHAR(50),                        -- 子类别

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(20),                        -- 学历要求
    degree_requirement          VARCHAR(20),                        -- 学位要求
    age_limit                   INTEGER,                            -- 年龄限制（周岁）
    recruitment_count           INTEGER         DEFAULT 1,          -- 招聘人数
    salary_range                VARCHAR(50),                        -- 薪资待遇（如：8-12k）
    reg_deadline                VARCHAR(30),                        -- 报名截止日期
    major_requirements          TEXT[]          DEFAULT '{}',        -- 专业要求
    special_position            VARCHAR(100),                       -- 特殊岗位（如：紧缺岗位、定向岗位）
    other_requirement           VARCHAR(500),                       -- 其他要求
    other_requirement_desc      TEXT,                               -- 其他要求说明

    -- ==================== 备注信息 ====================
    remark_type                 VARCHAR(50),                        -- 备注类型（如：需要加班、需要夜班）
    remark_desc                 TEXT,                               -- 备注说明
    consultation_phone          VARCHAR(50),                        -- 咨询电话
    supervision_phone           VARCHAR(50),                        -- 监督电话

    -- ==================== 状态与标签 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',    -- 职位状态
    position_tag                VARCHAR(20)     DEFAULT '无',        -- 标签
    tag_text                    VARCHAR(50),                        -- 标签文字（前端展示用）

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_inst_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('无要求','大专','本科','硕士','博士')
    ),
    CONSTRAINT chk_inst_degree CHECK (
        degree_requirement IS NULL
        OR degree_requirement IN ('无要求','学士','硕士','博士')
    ),
    CONSTRAINT chk_inst_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 65)
    ),
    CONSTRAINT chk_inst_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_inst_status CHECK (
        position_status IS NULL OR position_status IN ('招聘中','已结束')
    ),
    CONSTRAINT chk_inst_tag CHECK (
        position_tag IS NULL OR position_tag IN ('热门','无','急招')
    )
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 职位名称搜索
CREATE INDEX idx_inst_pos_name
    ON t_institution_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

-- 按省份筛选
CREATE INDEX idx_inst_pos_province
    ON t_institution_position (province)
    WHERE is_deleted = FALSE;

-- 按工作地点筛选
CREATE INDEX idx_inst_pos_location
    ON t_institution_position (work_location)
    WHERE is_deleted = FALSE;

-- 按考试类别筛选
CREATE INDEX idx_inst_pos_exam_cat
    ON t_institution_position (exam_category)
    WHERE is_deleted = FALSE;

-- 按职位类型筛选
CREATE INDEX idx_inst_pos_type
    ON t_institution_position (position_type)
    WHERE is_deleted = FALSE;

-- 按学历筛选
CREATE INDEX idx_inst_pos_education
    ON t_institution_position (education_requirement)
    WHERE is_deleted = FALSE;

-- 按职位状态筛选
CREATE INDEX idx_inst_pos_status
    ON t_institution_position (position_status)
    WHERE is_deleted = FALSE;

-- 按标签筛选（热门/急招）
CREATE INDEX idx_inst_pos_tag
    ON t_institution_position (position_tag)
    WHERE is_deleted = FALSE AND position_tag != '无';

-- 按主管部门筛选
CREATE INDEX idx_inst_pos_dept
    ON t_institution_position (supervising_dept)
    WHERE is_deleted = FALSE;

-- 专业要求数组 GIN 索引
CREATE INDEX idx_inst_pos_majors
    ON t_institution_position USING gin (major_requirements)
    WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_inst_position_updated_at
    BEFORE UPDATE ON t_institution_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_institution_position                            IS '事业编职位表';
COMMENT ON COLUMN t_institution_position.position_name              IS '职位名称';
COMMENT ON COLUMN t_institution_position.supervising_dept           IS '主管部门';
COMMENT ON COLUMN t_institution_position.institution                IS '招聘单位';
COMMENT ON COLUMN t_institution_position.work_location              IS '工作地点';
COMMENT ON COLUMN t_institution_position.province                   IS '省份';
COMMENT ON COLUMN t_institution_position.exam_category              IS '考试类别（综合类/教育类/卫生类）';
COMMENT ON COLUMN t_institution_position.position_type              IS '职位类型（管理岗/专技岗/工勤岗）';
COMMENT ON COLUMN t_institution_position.sub_category               IS '子类别';
COMMENT ON COLUMN t_institution_position.education_requirement      IS '学历要求（无要求/大专/本科/硕士/博士）';
COMMENT ON COLUMN t_institution_position.degree_requirement         IS '学位要求（无要求/学士/硕士/博士）';
COMMENT ON COLUMN t_institution_position.age_limit                  IS '年龄限制（周岁）';
COMMENT ON COLUMN t_institution_position.recruitment_count          IS '招聘人数';
COMMENT ON COLUMN t_institution_position.salary_range               IS '薪资待遇（如：8-12k）';
COMMENT ON COLUMN t_institution_position.reg_deadline               IS '报名截止日期';
COMMENT ON COLUMN t_institution_position.major_requirements         IS '专业要求列表';
COMMENT ON COLUMN t_institution_position.special_position           IS '特殊岗位标记（紧缺岗位/定向岗位）';
COMMENT ON COLUMN t_institution_position.other_requirement          IS '其他要求';
COMMENT ON COLUMN t_institution_position.other_requirement_desc     IS '其他要求说明';
COMMENT ON COLUMN t_institution_position.remark_type                IS '备注类型（需要加班/需要夜班）';
COMMENT ON COLUMN t_institution_position.remark_desc                IS '备注说明';
COMMENT ON COLUMN t_institution_position.consultation_phone         IS '咨询电话';
COMMENT ON COLUMN t_institution_position.supervision_phone          IS '监督电话';
COMMENT ON COLUMN t_institution_position.position_status            IS '职位状态（招聘中/已结束）';
COMMENT ON COLUMN t_institution_position.position_tag               IS '标签（热门/无/急招）';
COMMENT ON COLUMN t_institution_position.tag_text                   IS '标签文字（前端展示用）';

COMMIT;
```

---



## 13、部队文职岗位表



```
-- ============================================================
-- 部队文职岗位表 (t_military_position)
-- 描述：军队文职人员招聘岗位信息
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_military_position (

    id                          SERIAL          PRIMARY KEY,

    -- ==================== 岗位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,           -- 岗位名称
    employer_unit               VARCHAR(200),                       -- 用人单位
    department                  VARCHAR(200),                       -- 所属部门
    position_type               VARCHAR(50),                        -- 岗位类型（如：专业技术岗、管理岗）
    work_location               VARCHAR(100),                       -- 工作地点
    salary_range                VARCHAR(50),                        -- 薪资待遇（如：9-15k）

    -- ==================== 报考要求 ====================
    major_requirement           VARCHAR(500),                       -- 专业要求
    education_requirement       VARCHAR(30),                        -- 学历要求
    reg_deadline                VARCHAR(30),                        -- 报名截止日期

    -- ==================== 状态 ====================
    position_status             VARCHAR(20)     DEFAULT '进行中',    -- 岗位状态

    -- ==================== 描述信息 ====================
    position_description        TEXT,                               -- 岗位描述
    responsibilities            TEXT[]          DEFAULT '{}',        -- 岗位职责
    qualifications              TEXT[]          DEFAULT '{}',        -- 任职要求

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_mil_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('本科及以上','硕士及以上','博士')
    ),
    CONSTRAINT chk_mil_status CHECK (
        position_status IS NULL OR position_status IN ('进行中','已结束')
    )
);


-- 索引
CREATE INDEX idx_mil_pos_name
    ON t_military_position USING btree (position_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_unit
    ON t_military_position (employer_unit)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_location
    ON t_military_position (work_location)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_education
    ON t_military_position (education_requirement)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_status
    ON t_military_position (position_status)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_type
    ON t_military_position (position_type)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_responsibilities
    ON t_military_position USING gin (responsibilities)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_mil_pos_qualifications
    ON t_military_position USING gin (qualifications)
    WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_mil_position_updated_at
    BEFORE UPDATE ON t_military_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_military_position                           IS '部队文职岗位表';
COMMENT ON COLUMN t_military_position.position_name             IS '岗位名称';
COMMENT ON COLUMN t_military_position.employer_unit             IS '用人单位';
COMMENT ON COLUMN t_military_position.department                IS '所属部门';
COMMENT ON COLUMN t_military_position.position_type             IS '岗位类型（专业技术岗/管理岗）';
COMMENT ON COLUMN t_military_position.work_location             IS '工作地点';
COMMENT ON COLUMN t_military_position.salary_range              IS '薪资待遇';
COMMENT ON COLUMN t_military_position.major_requirement         IS '专业要求';
COMMENT ON COLUMN t_military_position.education_requirement     IS '学历要求（本科及以上/硕士及以上/博士）';
COMMENT ON COLUMN t_military_position.reg_deadline              IS '报名截止日期';
COMMENT ON COLUMN t_military_position.position_status           IS '岗位状态（进行中/已结束）';
COMMENT ON COLUMN t_military_position.position_description      IS '岗位描述';
COMMENT ON COLUMN t_military_position.responsibilities          IS '岗位职责列表';
COMMENT ON COLUMN t_military_position.qualifications            IS '任职要求列表';

COMMIT;
```

