## 一、专业组录取表(t_admission_group)


```
-- ============================================================
-- 专业组录取表 (t_admission_group)
-- 描述：大学按 年份+省份+科类+批次 维度的专业组录取汇总数据
--       前端大学详情页的录取分数表格直接读这张表
-- ============================================================


CREATE TABLE IF NOT EXISTS t_admission_group (

    id                      SERIAL          PRIMARY KEY,
    university_id           INTEGER         NOT NULL,           -- 大学ID
    
    -- ==================== 录取维度（决定唯一性） ====================
    year                    SMALLINT        NOT NULL,           -- 年份（2024/2023...）
    province                VARCHAR(20)     NOT NULL,           -- 省份（四川/广东/湖北...）
    subject_type            VARCHAR(20)     NOT NULL,           -- 科类（
'理科', '物理类','文科', '历史类', '不分文理'）
    batch                   VARCHAR(50)     NOT NULL,           -- 批次（本科批/提前批/专科批）
     
     major_count             INTEGER         DEFAULT 0,          -- 该组下包含的专业数量
    
    description             TEXT,                               -- 专业组简介 / 备注说明
    
    constraints   TEXT[] DEFAULT '{}',  --静态限制
    
    
    -- ==================== 招生代码与专业组 ====================
    enrollment_code         VARCHAR(30),                        -- 省招代码（如：5137，按省+年变化）
    group_code              VARCHAR(30),                        -- 专业组代码（如：5128001，「省份 + 年份 + 批次 + 选科要求」有影响）
    group_name              VARCHAR(100),                       -- 专业组名称（如：历史第001组）
    subject_requirements    VARCHAR(50),                       -- 选课要求（如：不限/化学/物理+化学）
    requirement_level SMALLINT DEFAULT 0,
    
    
    
    -- ==================== 汇总录取数据（由专业明细聚合计算） ====================
    admission_count         INTEGER,                            -- 录取总人数（= SUM 各专业录取人数）

    min_score               INTEGER,                            -- 最低分（= MIN 各专业最低分）
    min_rank                INTEGER,                            -- 最低位次（= MAX 各专业最低位次数值）

    avg_score               NUMERIC(6, 2),                      -- 平均分
    avg_rank                INTEGER,                            -- 平均位次

    max_score               INTEGER,                            -- 最高分（= MAX 各专业最高分）
    max_rank                INTEGER,                            -- 最高位次（= MIN 各专业最高位次数值）

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    -- 同一大学 + 年份 + 省份 + 科类 + 批次 + 专业组代码 → 唯一
    CONSTRAINT uk_admission_group
        UNIQUE (university_id, year, province, subject_type, batch, group_code)
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 核心查询：某大学的录取数据（分页表格）
CREATE INDEX idx_ag_university_year
    ON t_admission_group (university_id, year DESC, province)
    WHERE is_deleted = FALSE;
    

CREATE UNIQUE INDEX IF NOT EXISTS uk_ag_business_key
    ON t_admission_group (business_key)
    WHERE business_key IS NOT NULL AND is_deleted = FALSE;


-- 按省份筛选
CREATE INDEX idx_ag_province
    ON t_admission_group (province, year DESC)
    WHERE is_deleted = FALSE;

-- 按科类筛选
CREATE INDEX idx_ag_subject
    ON t_admission_group (subject_type)
    WHERE is_deleted = FALSE;

-- 按批次筛选
CREATE INDEX idx_ag_batch
    ON t_admission_group (batch)
    WHERE is_deleted = FALSE;

-- 按最低分排序
CREATE INDEX idx_ag_min_score
    ON t_admission_group (min_score DESC NULLS LAST)
    WHERE is_deleted = FALSE;

-- 按年份筛选
CREATE INDEX idx_ag_year
    ON t_admission_group (year DESC)
    WHERE is_deleted = FALSE;


-- 索引：按通道筛选
CREATE INDEX IF NOT EXISTS idx_ag_channel
    ON t_admission_group (admission_channel)
    WHERE is_deleted = FALSE;

-- GIN 索引（支持数组查询）
CREATE INDEX IF NOT EXISTS idx_ag_constraints_gin
    ON t_admission_group USING GIN (constraints)
    WHERE is_deleted = FALSE;
    


-- 触发器
CREATE TRIGGER trg_admission_group_updated_at
    BEFORE UPDATE ON t_admission_group
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_admission_group                         IS '专业组录取汇总表（大学详情页表格数据源）';
COMMENT ON COLUMN t_admission_group.university_id           IS '大学ID';
COMMENT ON COLUMN t_admission_group.year                    IS '录取年份';
COMMENT ON COLUMN t_admission_group.province                IS '省份（录取数据按省不同）';
COMMENT ON COLUMN t_admission_group.subject_type            IS '科类（物理类/历史类/文科/理科）';
COMMENT ON COLUMN t_admission_group.batch                   IS '批次（本科批/提前批/国家专项计划）';
COMMENT ON COLUMN t_admission_group.enrollment_code         IS '省招代码（按省+年变化）';
COMMENT ON COLUMN t_admission_group.group_code              IS '专业组代码';
COMMENT ON COLUMN t_admission_group.group_name              IS '专业组名称';
COMMENT ON COLUMN t_admission_group.subject_requirements    IS '选课要求';
COMMENT ON COLUMN t_admission_group.admission_count         IS '录取总人数（各专业汇总）';
COMMENT ON COLUMN t_admission_group.admission_channel IS
    '招生通道：NORMAL=统招, INDEPENDENT=独立招生, JOINT_NATIONAL=全国联招, JOINT_TWO=两校联招, COMPREHENSIVE=综合评价, STRONG_BASE=强基计划';
COMMENT ON COLUMN t_admission_group.min_score               IS '最低分（各专业中最低）';
COMMENT ON COLUMN t_admission_group.min_rank                IS '最低位次';
COMMENT ON COLUMN t_admission_group.avg_score               IS '平均分';
COMMENT ON COLUMN t_admission_group.avg_rank                IS '平均位次';
COMMENT ON COLUMN t_admission_group.max_score               IS '最高分（各专业中最高）';
COMMENT ON COLUMN t_admission_group.max_rank                IS '最高位次';

COMMIT;
```

## 二、专业录取明细表(t_admission_major_score)


```
-- ============================================================
-- 专业录取明细表 (t_admission_major_score)
-- 描述：专业组内每个专业的具体录取分数
--       前端点击「专业详情」展开后显示的子表格
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_admission_major_score (

    id                      SERIAL          PRIMARY KEY,  
    group_id                INTEGER         NOT NULL,           -- 所属专业组ID 
    
    -- ==================== 专业信息 ====================
    major_id                INTEGER,                            -- 关联 t_major 表ID
    major_code              VARCHAR(20)     NOT NULL,           -- 专业代码（如：020401）
    major_name              VARCHAR(100)    NOT NULL,           -- 专业名称（如：国际经济与贸易）
    subject_requirements    VARCHAR(200),                       -- 该专业的选课要求（可能和组不同）
    
    
    -- ==================== 招生属性 ====================
    education_level         VARCHAR(20),                        -- 层次（本科/专科，可选）
    duration                VARCHAR(20),                        -- 学制（4年 / 3年 / 5年）
    tuition                 VARCHAR(50),                        -- 学费（5000元/年 / 89000港元/年）
    
    
    requirement_level SMALLINT DEFAULT 0,                       --等级
    description             TEXT,                               -- 简介（左侧大段介绍文字）

    -- ==================== 录取数据 ====================
    admission_count         INTEGER,                            -- 录取人数

    min_score               INTEGER,                            -- 最低分
    min_rank                INTEGER,                            -- 最低位次

    avg_score               NUMERIC(6, 2),                      -- 平均分
    avg_rank                INTEGER,                            -- 平均位次

    max_score               INTEGER,                            -- 最高分
    max_rank                INTEGER,                            -- 最高位次
    
    constraints TEXT[] DEFAULT '{}',  --动态限制


    -- ==================== 审计字段 ====================
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
   
-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 核心查询：按专业组查明细（展开子表格）
CREATE INDEX idx_ams_group_id
    ON t_admission_major_score (group_id);

-- 按专业ID查（查某专业在各校的录取情况）
CREATE INDEX idx_ams_major_id
    ON t_admission_major_score (major_id)
    WHERE major_id IS NOT NULL;

-- 按专业代码查
CREATE INDEX idx_ams_major_code
    ON t_admission_major_score (major_code);

-- 按最低分排序
CREATE INDEX idx_ams_min_score
    ON t_admission_major_score (min_score DESC NULLS LAST);
       
CREATE INDEX IF NOT EXISTS idx_ams_constraints_gin
    ON t_admission_major_score USING GIN (constraints);
    

CREATE INDEX idx_ams_constraints_gin
    ON t_admission_major_score USING GIN (constraints);
    



-- 触发器
CREATE TRIGGER trg_ams_updated_at
    BEFORE UPDATE ON t_admission_major_score
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_admission_major_score                       IS '专业录取明细表（专业组展开后的子表格数据源）';
COMMENT ON COLUMN t_admission_major_score.group_id              IS '所属专业组ID（关联 t_admission_group）';
COMMENT ON COLUMN t_admission_major_score.major_id              IS '关联专业表ID（t_major.id，可选）';
COMMENT ON COLUMN t_admission_major_score.major_code            IS '专业代码（如：020401）';
COMMENT ON COLUMN t_admission_major_score.major_name            IS '专业名称（冗余，方便展示）';
COMMENT ON COLUMN t_admission_major_score.subject_requirements  IS '选课要求';
COMMENT ON COLUMN t_admission_major_score.admission_count       IS '录取人数';
COMMENT ON COLUMN t_admission_major_score.min_score             IS '最低分';
COMMENT ON COLUMN t_admission_major_score.min_rank              IS '最低位次';
COMMENT ON COLUMN t_admission_major_score.avg_score             IS '平均分';
COMMENT ON COLUMN t_admission_major_score.avg_rank              IS '平均位次';
COMMENT ON COLUMN t_admission_major_score.max_score             IS '最高分';
COMMENT ON COLUMN t_admission_major_score.max_rank              IS '最高位次';

COMMIT;
```

### 2.2 标准化存储方案

#### 选科要求字典表


> **录入数据时只需写短代码**（如 `化学+生物`），触发器自动设 `requirement_level = 3`，父级自动推导。


```
-- ============================================================
-- 选科要求字典表 (t_subject_req_dict)
-- 定义所有合法的选科要求值 + 严格等级
-- ============================================================
CREATE TABLE IF NOT EXISTS t_subject_req_dict (

    id                  SERIAL          PRIMARY KEY,
    code                VARCHAR(50)     NOT NULL UNIQUE,     -- 标准代码（存入业务表的值）
    display_name        VARCHAR(100)    NOT NULL,            -- 前端展示名称
    requirement_level   SMALLINT        NOT NULL DEFAULT 0,  -- 严格等级（越小越宽松）
    subjects            TEXT[]          NOT NULL DEFAULT '{}',-- 涉及的科目
    requirement_type    VARCHAR(10)     NOT NULL DEFAULT 'NONE', -- NONE/ANY/ALL
    sort_order          INTEGER         DEFAULT 0,

    -- ===== 说明 =====
    -- requirement_level:
    --   0 = 不限（最宽松，任何人都能报）
    --   1 = 2选1（两个科目选一个就行）
    --   2 = 必选（必须选了某一个科目）
    --   3 = 均须选考（两个科目都要选）

    -- requirement_type:
    --   NONE = 不限
    --   ANY  = 多个科目选一个即可
    --   ALL  = 所有列出科目都必须选
    
    CONSTRAINT chk_req_type CHECK (requirement_type IN ('NONE', 'ANY', 'ALL'))
);

```

```
-- ============================================================
-- 补充 3+3 模式（上海/浙江等）所需的选科要求字典数据
-- 覆盖所有常见单科必选、2选1、2科均须场景
-- ============================================================

-- 1. Level 2: 单科必选（6门全量）
INSERT INTO t_subject_req_dict
    (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    ('物理',           '物理(必选)',                   2, '{物理}',          'ALL',  40),
    ('化学',           '化学(必选)',                   2, '{化学}',          'ALL',  41),
    ('生物',           '生物(必选)',                   2, '{生物}',          'ALL',  42),
    ('历史',           '历史(必选)',                   2, '{历史}',          'ALL',  43),
    ('政治',        '思想政治(必选)',               2, '{思想政治}',          'ALL',  44),
    ('地理',           '地理(必选)',                   2, '{地理}',          'ALL',  45)
ON CONFLICT (code) DO NOTHING;


-- 2. Level 1: 2选1（C(6,2)=15种组合，全部用 ANY）
INSERT INTO t_subject_req_dict
    (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    ('物理或化学',      '物理、化学(2选1)',             1, '{物理,化学}',      'ANY',  50),
    ('物理或生物',      '物理、生物(2选1)',             1, '{物理,生物}',      'ANY',  51),
    ('物理或历史',      '物理、历史(2选1)',             1, '{物理,历史}',      'ANY',  52),
    ('物理或政治',   '物理、思想政治(2选1)',          1, '{物理,思想政治}',     'ANY',  53),
    ('物理或地理',      '物理、地理(2选1)',             1, '{物理,地理}',      'ANY',  54),

    ('化学或生物',      '化学、生物(2选1)',             1, '{化学,生物}',      'ANY',  55),
    ('化学或历史',      '化学、历史(2选1)',             1, '{化学,历史}',      'ANY',  56),
    ('化学或政治',   '化学、思想政治(2选1)',          1, '{化学,思想政治}',   'ANY',  57),
    ('化学或地理',      '化学、地理(2选1)',             1, '{化学,地理}',      'ANY',  58),

    ('生物或历史',      '生物、历史(2选1)',             1, '{生物,历史}',      'ANY',  59),
    ('生物或政治',   '生物、思想政治(2选1)',          1, '{生物,思想政治}',   'ANY',  60),
    ('生物或地理',      '生物、地理(2选1)',             1, '{生物,地理}',      'ANY',  61),

    ('历史或政治',   '历史、思想政治(2选1)',          1, '{历史,思想政治}',   'ANY',  62),
    ('历史或地理',      '历史、地理(2选1)',             1, '{历史,地理}',      'ANY',  63),

    ('政治或地理',   '思想政治、地理(2选1)',          1, '{思想政治,地理}',   'ANY',  64)
ON CONFLICT (code) DO NOTHING;


-- 3. Level 3: 2科均须选考（C(6,2)=15种组合，全部用 ALL）
INSERT INTO t_subject_req_dict
    (code, display_name, requirement_level, subjects, requirement_type, sort_order)
VALUES
    ('物理+化学',       '物理+化学(均须选考)',          3, '{物理,化学}',      'ALL',  70),
    ('物理+生物',       '物理+生物(均须选考)',          3, '{物理,生物}',      'ALL',  71),
    ('物理+历史',       '物理+历史(均须选考)',          3, '{物理,历史}',      'ALL',  72),
    ('物理+政治',    '物理+思想政治(均须选考)',       3, '{物理,思想政治}',   'ALL',  73),
    ('物理+地理',       '物理+地理(均须选考)',          3, '{物理,地理}',      'ALL',  74),

    ('化学+生物',       '化学+生物(均须选考)',          3, '{化学,生物}',      'ALL',  75),
    ('化学+历史',       '化学+历史(均须选考)',          3, '{化学,历史}',      'ALL',  76),
    ('化学+政治',    '化学+思想政治(均须选考)',       3, '{化学,思想政治}',   'ALL',  77),
    ('化学+地理',       '化学+地理(均须选考)',          3, '{化学,地理}',      'ALL',  78),

    ('生物+历史',       '生物+历史(均须选考)',          3, '{生物,历史}',      'ALL',  79),
    ('生物+政治',    '生物+思想政治(均须选考)',       3, '{生物,思想政治}',   'ALL',  80),
    ('生物+地理',       '生物+地理(均须选考)',          3, '{生物,地理}',      'ALL',  81),

    ('历史+政治',    '历史+思想政治(均须选考)',       3, '{历史,思想政治}',   'ALL',  82),
    ('历史+地理',       '历史+地理(均须选考)',          3, '{历史,地理}',      'ALL',  83),

    ('政治+地理',    '思想政治+地理(均须选考)',       3, '{思想政治,地理}',   'ALL',  84)
ON CONFLICT (code) DO NOTHING;

```