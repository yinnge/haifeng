13. 特殊通道模块[父模块]--特殊招生通道列表[子模块]--通道详情[子模块]--通道-大学[子模块]--强基计划列表[子模块]

### 2.1 特殊通道内容表



```
-- ============================================================
-- 特殊招生通道内容表 (t_special_channel)
-- 每个通道一条记录，存介绍文章（HTML富文本）
-- 管理员在后台用富文本编辑器维护
-- ============================================================

CREATE TABLE IF NOT EXISTS t_special_channel (

    id                  SERIAL          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL UNIQUE,    -- 通道代码
    channel_name        VARCHAR(50)     NOT NULL,           -- 通道名称
    subtitle            VARCHAR(200),                       -- 副标题
    
    -- ==================== 父节点 ====================
    
    parent_code         VARCHAR(30),                 -- 父级通道代码（用于分组）
    filter_label        VARCHAR(30);                 -- 筛选按钮上的文字
    
    -- ==================== 展示方式 ====================
    
    display_type        VARCHAR(20)     NOT NULL,           -- 展示类型
    -- 'UNIVERSITY_LIST'  = 展示大学列表（专项/综评/港澳）
    -- 'ARTICLE_ONLY'     = 只展示文章（民族班）
    -- 'MAJOR_DATA'       = 展示专业级数据（强基计划）

    -- ==================== 内容 ====================
    content             TEXT,                               -- 富文本内容（HTML）
    
    
    -- ==================== 排序与状态 ====================
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
-- ============================================================
-- 特殊招生通道数据插入
-- 注意：父节点需要先插入，子节点后插入
-- ============================================================

-- 第一组：无父节点的记录
INSERT INTO t_special_channel (channel_code, channel_name, subtitle, parent_code, filter_label, display_type, content, sort_order, is_active) VALUES
-- 强基计划
('STRONG_BASE',
 '强基计划',
 '聚焦基础学科，培养拔尖创新人才',
 NULL,
 NULL,
 'MAJOR_DATA',
 '<p>强基计划主要选拔培养有志于服务国家重大战略需求且综合素质优秀或基础学科拔尖的学生。</p>',
 1,
 TRUE),

-- 综合评价
('COMPREHENSIVE_EVAL',
 '综合评价',
 '高考成绩+综合素质评价，多元录取',
 NULL,
 NULL,
 'UNIVERSITY_LIST',
 '<p>综合评价招生是高考改革的重要探索，综合考察考生的高考成绩、高校考核成绩和综合素质评价等。</p>',
 2,
 TRUE),

-- 专项计划（父节点）
('SPECIAL_PROGRAM',
 '专项计划',
 '面向农村和贫困地区考生的专项招生',
 NULL,
 NULL,
 'GROUP',
 '<p>专项计划是面向农村和贫困地区学生的专项招生计划，包括国家专项、高校专项和地方专项三种类型。</p>',
 3,
 TRUE),

-- 港澳院校招生
('HK_MACAU',
 '港澳院校招生',
 '香港、澳门高校内地招生',
 NULL,
 NULL,
 'UNIVERSITY_LIST',
 '<p>香港、澳门特别行政区高校在内地招生，为考生提供国际化教育机会。</p>',
 4,
 TRUE),

-- 民族班
('ETHNIC_MINORITY',
 '民族班',
 '面向少数民族考生的特殊招生',
 NULL,
 NULL,
 'ARTICLE_ONLY',
 '<p>民族班是部分高校面向少数民族考生开设的特殊班级，旨在培养少数民族人才。</p>',
 5,
 TRUE),

-- 全国联招
('NATIONAL_JA',
 '全国联招',
 '面向华侨、港澳台学生的联合招生',
 NULL,
 NULL,
 'UNIVERSITY_LIST',
 '<p>中华人民共和国普通高等院校联合招收华侨、港澳地区及台湾省学生。</p>',
 6,
 TRUE),

-- 两校联招
('TWO_SCHOOL_JA',
 '两校联招',
 '暨南大学、华侨大学联合招生',
 NULL,
 NULL,
 'UNIVERSITY_LIST',
 '<p>暨南大学、华侨大学联合招收港澳台和华侨学生。</p>',
 7,
 TRUE);

-- 第二组：专项计划的子节点（必须在父节点插入后）
INSERT INTO t_special_channel (channel_code, channel_name, subtitle, parent_code, filter_label, display_type, content, sort_order, is_active) VALUES
-- 国家专项计划
('SPECIAL_NATIONAL',
 '国家专项计划',
 '面向贫困县农村考生，95所重点高校',
 'SPECIAL_PROGRAM',
 '国家专项',
 'UNIVERSITY_LIST',
 '<p>国家专项计划定向招收贫困地区学生，由中央部门和地方本科一批招生为主的学校承担。</p><p>招生学校：中央部门所属高校和各省(区、市)所属重点高校，共95所。</p><p>参考链接：<a href="https://gaokao.chsi.com.cn/gkzt/zxjh" target="_blank">https://gaokao.chsi.com.cn/gkzt/zxjh</a></p>',
 1,
 TRUE),

-- 高校专项计划
('SPECIAL_UNIVERSITY',
 '高校专项计划',
 '面向农村学生单独招生，教育部直属高校等',
 'SPECIAL_PROGRAM',
 '高校专项',
 'UNIVERSITY_LIST',
 '<p>高校专项计划又称"农村学生单独招生"，由教育部直属高校和其他试点高校承担，主要招收边远、原贫困、民族等地区县(含县级市)以下高中勤奋好学、成绩优良的农村学生。</p>',
 2,
 TRUE),

-- 地方专项计划
('SPECIAL_LOCAL',
 '地方专项计划',
 '省属重点高校招收本省农村考生',
 'SPECIAL_PROGRAM',
 '地方专项',
 'UNIVERSITY_LIST',
 '<p>地方专项计划定向招收各省(区、市)实施区域的农村学生，由各省(区、市)所属重点高校承担。</p>',
 3,
 TRUE);

| channel_code       | channel_name | parent_code     | filter_label | display_type    |
| ------------------ | ------------ | --------------- | ------------ | --------------- |
| STRONG_BASE        | 强基计划         | NULL            | NULL         | MAJOR_DATA      |
| COMPREHENSIVE_EVAL | 综合评价         | NULL            | NULL         | UNIVERSITY_LIST |
| SPECIAL_PROGRAM    | 专项计划         | NULL            | NULL         | GROUP           |
| SPECIAL_NATIONAL   | 国家专项计划       | SPECIAL_PROGRAM | 国家专项         | UNIVERSITY_LIST |
| SPECIAL_UNIVERSITY | 高校专项计划       | SPECIAL_PROGRAM | 高校专项         | UNIVERSITY_LIST |
| SPECIAL_LOCAL      | 地方专项计划       | SPECIAL_PROGRAM | 地方专项         | UNIVERSITY_LIST |
| HK_MACAU           | 港澳院校招生       | NULL            | NULL         | UNIVERSITY_LIST |
| ETHNIC_MINORITY    | 民族班          | NULL            | NULL         | ARTICLE_ONLY    |
| NATIONAL_JA        | 全国联招         | NULL            | NULL         | UNIVERSITY_LIST |
| TWO_SCHOOL_JA      | 两校联招         | NULL            | NULL         | UNIVERSITY_LIST |


### 1.1 通道-大学关联表`t_special_channel_university`


```
-- ============================================================
-- 通道-大学关联表 (t_special_channel_university) — 完善版
-- 每所大学在每个通道下的招生简章、报名信息
-- ============================================================

DROP TABLE IF EXISTS t_special_channel_university;

CREATE TABLE IF NOT EXISTS t_special_channel_university (

    id                  SERIAL          PRIMARY KEY,
    channel_code        VARCHAR(30)     NOT NULL,           -- 通道代码
    channel_name        VARCHAR(50)     NOT NULL,           -- 通道名称
    university_id       INTEGER         NOT NULL,           -- 大学ID
    university_name     VARCHAR(50)     NOT NULL,           -- 大学名称-冗余
    year                SMALLINT,                           -- 招生年份
    
    -- ==================== 地区标签（用于港澳筛选） ====================
    region_tag          VARCHAR(20),                        -- 地区标签：香港/澳门/NULL

    -- ==================== 报名信息 ====================
    signup_start        TIMESTAMPTZ,                        -- 报名开始时间
    signup_end          TIMESTAMPTZ,                        -- 报名截止时间
    official_url        VARCHAR(500),                       -- 报名官网URL（右侧按钮跳转）

    -- ==================== 招生简章内容 ====================
    brochure_title      VARCHAR(200),                       -- 简章标题
    brochure_content    TEXT,                               -- 简章正文（HTML富文本）

    -- ==================== 排序 ====================
    sort_order          INTEGER         DEFAULT 0,

    -- ==================== 审计 ====================
    is_active          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_channel_univ UNIQUE (channel_code, university_id, year)
);

CREATE INDEX idx_scu_channel ON t_special_channel_university (channel_code) WHERE is_deleted = FALSE;
CREATE INDEX idx_scu_region  ON t_special_channel_university (channel_code, region_tag) WHERE is_deleted = FALSE;
```

### 3.2 强基计划数据表


```
-- ============================================================
-- 强基计划入围/录取数据表 (t_strong_base_score)
-- 按 大学 + 年份 + 省份 + 科类 + 专业 维度存储
-- ============================================================

CREATE TABLE IF NOT EXISTS t_strong_base_score (

    id                      SERIAL          PRIMARY KEY,
    university_id           INTEGER         NOT NULL,
    university_name     VARCHAR(50)         NOT NULL,           -- 大学名称-冗余
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,       -- 省份（具体到省）
    subject_type            VARCHAR(20)     NOT NULL,       -- 物理类/历史类/理科/文科/综合改革

    -- ==================== 专业信息 ====================
    major_name              VARCHAR(100)    NOT NULL,       -- 专业名称（数学与应用数学/物理学...）
    major_code              VARCHAR(20),                    -- 专业代码（可选）

    -- ==================== 入围数据 ====================
    entry_score             NUMERIC(7,2),                   -- 入围分数线（可能是加权分）
    entry_score_type        VARCHAR(30)     DEFAULT '高考成绩',
                                                            -- 入围分数类型：
                                                            -- '高考成绩' = 直接用高考分
                                                            -- '加权成绩' = 高考重点科目×1.2+其他
                                                            -- '校测初试' = 高考前校测（复旦等）
    entry_formula           VARCHAR(500),                   -- 入围计算公式（如：重点科目×1.2+其他科目之和）
    entry_ratio             VARCHAR(20),                    -- 入围比例（如：1:3 / 1:5 / 1:6）

    -- ==================== 录取数据 ====================
    admission_score         NUMERIC(7,2),                   -- 录取综合分
    admission_formula       VARCHAR(500)    DEFAULT '高考成绩×85%+校测成绩×15%',
                                                            -- 录取综合分计算公式
    plan_count              INTEGER,                        -- 该专业招生计划数
    admission_count         INTEGER,                        -- 实际录取人数

    -- ==================== 备注 ====================
    remark                  VARCHAR(500),                   -- 备注（如：破格入围不计入此线）

    -- ==================== 审计字段 ====================
    is_active              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_strong_base
        UNIQUE (university_id, year, province, subject_type, major_name)
);


CREATE INDEX idx_sb_univ_year
ON t_strong_base_score (university_id, year DESC);

CREATE INDEX idx_sb_province
ON t_strong_base_score (province, year DESC, subject_type);
```

### 3.3 强基计划大学配置表


```
-- ============================================================
-- 强基计划院校配置表 (t_strong_base_university)
-- 39所试点院校的强基计划配置信息
-- ============================================================

CREATE TABLE IF NOT EXISTS t_strong_base_university (

    id                      SERIAL          PRIMARY KEY,
    university_id           INTEGER         NOT NULL UNIQUE,    -- 大学ID
    university_name     VARCHAR(50)         NOT NULL,           -- 大学名称-冗余

    -- ==================== 基本信息 ====================
    is_pilot                BOOLEAN         DEFAULT TRUE,       -- 是否强基试点校
    pilot_year              SMALLINT,                           -- 首次试点年份（2020）
    
    -- ==================== 链接 ====================
    official_url            VARCHAR(500),                       -- 强基计划官方页面URL
    signup_url              VARCHAR(500),                       -- 报名入口URL（阳光高考平台）

    -- ==================== 校测信息 ====================
    test_before_score       BOOLEAN         DEFAULT FALSE,      -- 是否高考出分前校测
                                                                -- TRUE: 复旦/上交/南大/浙大/中科大等
                                                                -- FALSE: 大部分学校
    default_entry_ratio     VARCHAR(20)     DEFAULT '1:5',      -- 默认入围比例
    default_admission_formula VARCHAR(500)  DEFAULT '高考成绩×85%+校测成绩×15%',

    -- ==================== 招生专业 ====================
    available_majors        TEXT[],                             -- 可选专业列表

    -- ==================== 特殊说明 ====================
    special_notes           TEXT,                               -- 特殊说明

    -- ==================== 审计 ====================
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);


-- 预置39所试点校数据（示例）
INSERT INTO t_strong_base_university
(university_id, pilot_year, official_url, test_before_score,
default_entry_ratio, available_majors, special_notes)
VALUES
-- 复旦大学（高考出分前校测）
(10246, 2020,
'https://bkzsw.fudan.edu.cn/xxgk/jqjh.htm',
TRUE,
'1:3',
ARRAY['数学与应用数学','物理学','化学','生物科学','基础医学',
'汉语言(古文字学方向)','哲学','历史学'],
'高考出分前组织校测初试，无传统入围线'),

    -- 四川大学（标准流程）
    (10610, 2020,
     'https://zs.scu.edu.cn/',
     FALSE,
     '1:5',
     ARRAY['数学与应用数学','物理学','化学','生物科学',
           '汉语言文学','历史学','哲学'],
     NULL);
```

