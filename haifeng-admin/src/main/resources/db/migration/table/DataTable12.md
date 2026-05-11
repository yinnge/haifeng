算法配置管理[父模块]--建省份改革配置[子模块]--一分一段位次[子模块]--批次分数线[子模块]

## 建省份改革配置表


> 解决文理科变成3+1+2的兼容方案,需要支持增删改查，防止后面有的省份b

```
-- ============================================================
-- 省份高考改革配置表（t_province_reform）
-- 记录每个省份从哪一年开始使用 3+1+2 新高考
-- ============================================================
CREATE TABLE IF NOT EXISTS t_province_reform (

    id              SERIAL          PRIMARY KEY,
    province        VARCHAR(20)     NOT NULL UNIQUE,    -- 省份
    reform_year     SMALLINT,                           -- 新高考首届高考年份（NULL=尚未改革）
    reform_model    VARCHAR(20),                        -- 改革模式（3+1+2 / 3+3 / 传统文理）
    created_at      TIMESTAMPTZ     DEFAULT NOW()
);


-- 预置数据
INSERT INTO t_province_reform (province, reform_year, reform_model) VALUES
-- 第一批 3+3（上海、浙江）
('上海',   2017, '3+3'),
('浙江',   2017, '3+3'),

-- 第二批 3+3（北京、天津、山东、海南）
('北京',   2020, '3+3'),
('天津',   2020, '3+3'),
('山东',   2020, '3+3'),
('海南',   2020, '3+3'),

-- 第三批 3+1+2
('广东',   2021, '3+1+2'),
('福建',   2021, '3+1+2'),
('河北',   2021, '3+1+2'),
('辽宁',   2021, '3+1+2'),
('湖北',   2021, '3+1+2'),
('湖南',   2021, '3+1+2'),
('重庆',   2021, '3+1+2'),
('江苏',   2021, '3+1+2'),
('香港',   2021, '3+1+2'),

-- 第四批 3+1+2
('吉林',   2024, '3+1+2'),
('黑龙江', 2024, '3+1+2'),
('安徽',   2024, '3+1+2'),
('江西',   2024, '3+1+2'),
('广西',   2024, '3+1+2'),
('贵州',   2024, '3+1+2'),
('甘肃',   2024, '3+1+2'),

-- 第五批 3+1+2
('山西',   2025, '3+1+2'),
('河南',   2025, '3+1+2'),
('四川',   2025, '3+1+2'),
('云南',   2025, '3+1+2'),
('内蒙古', 2025, '3+1+2'),
('陕西',   2025, '3+1+2'),
('青海',   2025, '3+1+2'),
('宁夏',   2025, '3+1+2'),

-- 尚未改革（传统文理）
('西藏',   NULL, '传统文理'),
('新疆',   NULL, '传统文理');
```

## 五、`t_score_rank` 一分一段位次表 

用于用户输入分数后自动计算位次：


```
-- ============================================================
-- 一分一段位次表 (t_score_rank)
-- 描述：各省份、各年份、各科类的分数-位次对照表
--       用户输入分数后自动查出对应位次
-- ============================================================

CREATE TABLE IF NOT EXISTS t_score_rank (

    id              SERIAL          PRIMARY KEY,
    province        VARCHAR(20)     NOT NULL,           -- 省份（四川/广东/湖北...）
    year            SMALLINT        NOT NULL,           -- 年份
    subject_type    VARCHAR(20)     NOT NULL,           -- 科类（物理类/历史类/文科/理科/不分文理）
    
    score           SMALLINT        NOT NULL,           -- 分数
    rank            INTEGER         NOT NULL,           -- 该分数对应的位次
    same_score_count INTEGER,                           -- 同分人数（可选）
    cumulative_count INTEGER,                           -- 累计人数（可选）

    -- ==================== 审计 ====================
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_score_rank
        UNIQUE (province, year, subject_type, score)
);


-- ----------------------------------------------------------
-- 索引：精确查位次
-- ----------------------------------------------------------
CREATE INDEX idx_sr_lookup
    ON t_score_rank (province, year, subject_type, score);

-- 索引：按位次反查分数
CREATE INDEX idx_sr_rank_lookup
    ON t_score_rank (province, year, subject_type, rank);
```
## 二、批次分数线表 —【全新创建】


> [!NOTE] Title
> 不管你是本科一批还是本科二批，直接传本科批


```
-- ============================================================
-- 批次分数线表 (t_batch_score_line)
-- 描述：各省各年各科类的省控线（一本线/本科线/专科线等）
--       用途：① 判断考生是否有资格报某批次
--            ② 计算"线差"用于跨年可比分析
-- ============================================================

CREATE TABLE IF NOT EXISTS t_batch_score_line (

    id                  SERIAL          PRIMARY KEY,

    province            VARCHAR(20)     NOT NULL,           -- 省份
    year                SMALLINT        NOT NULL,           -- 年份
    subject_type        VARCHAR(20)     NOT NULL,           -- 科类（物理类/历史类/理科/文科/综合改革/不分文理）
    batch               VARCHAR(50)     NOT NULL,           -- 批次名称

    score_line          INTEGER         NOT NULL,           -- 省控分数线
    rank_line           INTEGER,                            -- 省控线对应位次（可选）

    -- ==================== 说明字段 ====================
    remark              VARCHAR(200),                       -- 备注（如：合并批次说明）

    -- ==================== 审计字段 ====================
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_batch_score_line
        UNIQUE (province, year, subject_type, batch)
);


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------
CREATE INDEX idx_bsl_lookup
    ON t_batch_score_line (province, year, subject_type);

CREATE INDEX idx_bsl_year
    ON t_batch_score_line (year DESC);


```