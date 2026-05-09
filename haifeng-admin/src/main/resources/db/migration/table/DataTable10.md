## 一、证书表


```
-- ============================================================
-- 证书表 (t_certificate)
-- 描述：各类职业资格证书、等级考试证书信息
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_certificate (

    id                      SERIAL          PRIMARY KEY,
    cert_name               VARCHAR(150)    NOT NULL,           -- 证书名称
    category                VARCHAR(50),                        -- 分类（如：IT类、财会类、语言类、工程类）
    cert_level              VARCHAR(50),                        -- 证书等级（如：初级、中级、高级）
    applicable_major        VARCHAR(200),                       -- 适用专业（如：计算机类、金融类）
    registration_time       VARCHAR(100),                       -- 报名时间（如：每年3月/9月）
    exam_time               VARCHAR(100),                       -- 考试时间（如：5月中旬、11月上旬）
    exam_fee                INTEGER,                            -- 考试费用（元）
    cert_intro              TEXT,                               -- 证书简介
    exam_requirements       TEXT[]          DEFAULT '{}',        -- 报考条件
    exam_arrangement        TEXT,                               -- 考试安排详情
    official_website        VARCHAR(500),                       -- 官方网站链接

    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_cert_name UNIQUE (cert_name),

    CONSTRAINT chk_exam_fee CHECK (exam_fee IS NULL OR exam_fee >= 0)
);


-- 索引
CREATE INDEX idx_cert_category
    ON t_certificate (category) WHERE is_deleted = FALSE;

CREATE INDEX idx_cert_level
    ON t_certificate (cert_level) WHERE is_deleted = FALSE;

CREATE INDEX idx_cert_name_search
    ON t_certificate USING btree (cert_name varchar_pattern_ops) WHERE is_deleted = FALSE;

CREATE INDEX idx_cert_major
    ON t_certificate USING btree (applicable_major varchar_pattern_ops) WHERE is_deleted = FALSE;

CREATE INDEX idx_cert_requirements
    ON t_certificate USING gin (exam_requirements) WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_certificate_updated_at
    BEFORE UPDATE ON t_certificate
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_certificate                        IS '证书信息表';
COMMENT ON COLUMN t_certificate.cert_name              IS '证书名称';
COMMENT ON COLUMN t_certificate.category               IS '证书分类（如：IT类、财会类、语言类）';
COMMENT ON COLUMN t_certificate.cert_level             IS '证书等级（如：初级、中级、高级）';
COMMENT ON COLUMN t_certificate.applicable_major       IS '适用专业';
COMMENT ON COLUMN t_certificate.registration_time      IS '报名时间（如：每年3月/9月）';
COMMENT ON COLUMN t_certificate.exam_time              IS '考试时间（如：5月中旬）';
COMMENT ON COLUMN t_certificate.exam_fee               IS '考试费用（元）';
COMMENT ON COLUMN t_certificate.cert_intro             IS '证书简介';
COMMENT ON COLUMN t_certificate.exam_requirements      IS '报考条件列表';
COMMENT ON COLUMN t_certificate.exam_arrangement       IS '考试安排详情';
COMMENT ON COLUMN t_certificate.official_website       IS '官方网站链接';

COMMIT;
```

---


## 二、竞赛表



```
-- ============================================================
-- 竞赛表 (t_competition)
-- 描述：各类学科竞赛、创新创业大赛基本信息
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_competition (

    id                      SERIAL          PRIMARY KEY,
    comp_name               VARCHAR(200)    NOT NULL,           -- 竞赛名称
    comp_level              VARCHAR(50),                        -- 竞赛级别（如：国家级、省级、校级）
    
    registration_time       VARCHAR(100),                       -- 报名时间

    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_comp_name UNIQUE (comp_name)
);


-- 索引
CREATE INDEX idx_comp_level
    ON t_competition (comp_level) WHERE is_deleted = FALSE;

CREATE INDEX idx_comp_name_search
    ON t_competition USING btree (comp_name varchar_pattern_ops) WHERE is_deleted = FALSE;

CREATE INDEX idx_comp_major
    ON t_competition USING btree (applicable_major varchar_pattern_ops) WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_competition_updated_at
    BEFORE UPDATE ON t_competition
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- 注释
COMMENT ON TABLE  t_competition                        IS '竞赛基本信息表';
COMMENT ON COLUMN t_competition.comp_name              IS '竞赛名称';
COMMENT ON COLUMN t_competition.comp_level             IS '竞赛级别（国家级/省级/校级）';
COMMENT ON COLUMN t_competition.applicable_major       IS '适合专业';
COMMENT ON COLUMN t_competition.registration_time      IS '报名时间';

COMMIT;
```


### 竞赛详情表（一对一）


```
-- ============================================================
-- 竞赛详情表 (t_competition_detail)
-- 描述：与 t_competition 一对一，存储竞赛完整信息
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_competition_detail (

    id                      SERIAL          PRIMARY KEY,
    competition_id          INTEGER         NOT NULL UNIQUE,        -- 关联竞赛表（一对一）

    -- ==================== 基本详情 ====================
    basic_info              JSONB           DEFAULT '{}'::JSONB,
    /*
    {
        "organizer":        "教育部高等教育司",
        "hold_time":        "每年9月-11月",
        "target":           "全日制在校本科生及研究生",
        "participation_form": "团队",
        "level":            "国家级",
        "reg_fee":          "免费",
        "official_website":  "https://xxx.edu.cn",
        "contact_email":    "xxx@edu.cn",
        "contact_phone":    "010-12345678"
    }
    */

    -- ==================== 奖项设置（列表展示）====================
    awards                  TEXT[]          DEFAULT '{}',
    -- ["国家一等奖", "国家二等奖", "国家三等奖", "省级一等奖", ...]

    -- ==================== 竞赛介绍 ====================
    background              TEXT,                                   -- 竞赛背景与意义
    purposes                TEXT[]          DEFAULT '{}',            -- 竞赛目的

    -- ==================== 规则与标准 ====================
    competition_rules       JSONB           DEFAULT '[]'::JSONB,
    /*
    [
        { "title": "参赛资格", "content": "全日制在校生，不限专业..." },
        { "title": "组队要求", "content": "每队3-5人，需指导教师1名..." },
        { "title": "作品要求", "content": "原创作品，不得抄袭..." }
    ]
    */

    scoring_criteria        TEXT[]          DEFAULT '{}',            -- 评分标准
    notices                 TEXT[]          DEFAULT '{}',            -- 注意事项

    -- ==================== 流程与奖项展示 ====================
    process_guide           JSONB           DEFAULT '[]'::JSONB,
    /*
    [
        { "title": "报名阶段", "content": "登录官网注册报名..." },
        { "title": "初赛阶段", "content": "提交作品，线上评审..." },
        { "title": "决赛阶段", "content": "现场答辩..." }
    ]
    */

    awards_display          JSONB           DEFAULT '[]'::JSONB,
    /*
    [
        { "title": "国家一等奖", "content": "奖金5000元 + 证书 + 保研加分" },
        { "title": "国家二等奖", "content": "奖金3000元 + 证书" }
    ]
    */

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_competition_detail_updated_at
    BEFORE UPDATE ON t_competition_detail
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON TABLE  t_competition_detail                  IS '竞赛详情表（与竞赛表一对一）';
COMMENT ON COLUMN t_competition_detail.competition_id   IS '关联竞赛ID';
COMMENT ON COLUMN t_competition_detail.basic_info       IS '基本信息（主办方、时间、对象等）';
COMMENT ON COLUMN t_competition_detail.competition_rules IS '竞赛规则（title+content数组）';
COMMENT ON COLUMN t_competition_detail.process_guide    IS '参赛流程指南（title+content数组）';
COMMENT ON COLUMN t_competition_detail.awards_display   IS '奖项设置展示（title+content数组）';

COMMIT;
```

### 竞赛-适合专业 关联表(t_competition_major)



```
-- ============================================================
-- 竞赛-专业 关联表 (t_competition_major)
-- 描述：多对多，记录竞赛适合哪些专业
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_competition_major (

    id                  SERIAL          PRIMARY KEY,
    competition_id      INTEGER         NOT NULL,               -- 竞赛ID
    major_id            BIGINT          NOT NULL,               -- 专业ID
    major_name          VARCHAR(100)    NOT NULL,               -- 专业名称（冗余）
    competition_name    VARCHAR(100)    NOT NULL,               -- 竞赛名称
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_comp_major
        UNIQUE (competition_id, major_id)
);

-- 竞赛详情页：该竞赛适合哪些专业
CREATE INDEX idx_cm_competition
    ON t_competition_major (competition_id);

-- 专业详情页：该专业能参加哪些竞赛（反向查询）
CREATE INDEX idx_cm_major
    ON t_competition_major (major_id);

COMMENT ON TABLE  t_competition_major               IS '竞赛-专业关联表（多对多）';
COMMENT ON COLUMN t_competition_major.competition_id IS '竞赛ID';
COMMENT ON COLUMN t_competition_major.major_id      IS '适合的专业ID';

COMMIT;
```
