算法约束模块[父模块]--约束字典[子模块]--专业约束关联[子模块]--安全系数[子模块]

```

-- ============================================================
-- 约束条件字典表 (t_constraint_dict) — 完全重新设计
-- 核心：把判断规则存成数据，后端动态执行
-- ============================================================

DROP TABLE IF EXISTS t_constraint_dict CASCADE;

CREATE TABLE IF NOT EXISTS t_constraint_dict (

    code                VARCHAR(50)     PRIMARY KEY,            -- 约束代码（唯一标识）
    name                VARCHAR(100)    NOT NULL UNIQUE,               -- 前端显示名称
    category            VARCHAR(30)     NOT NULL,               -- 约束大类（身体视觉/身体指标/语种限制/...）
    description         TEXT,                                   -- 详细说明（给用户看的）
    severity            VARCHAR(10)     NOT NULL DEFAULT 'HARD',-- HARD=硬限制 / SOFT=软提示

    -- ==================== 判断规则（后端动态执行的核心） ====================
    -- 对应 t_member_gaokao 表的哪个字段
    check_field         VARCHAR(50),                            -- 如：is_color_blind / height_cm / score_english

    -- 判断运算符
    check_operator      VARCHAR(20),                            -- EQ/NEQ/LT/LTE/GT/GTE/IS_TRUE/IS_FALSE/IN/NOT_IN

    -- 判断值（字符串，后端转换成对应类型）
    check_value         VARCHAR(100),                           -- 如：true / 170 / 英语 / 农村,城镇

    -- 附加条件（可选，用于"男生身高<170"这种带性别的判断）
    extra_field         VARCHAR(50),                            -- 如：gender
    extra_operator      VARCHAR(20),                            -- EQ
    extra_value         VARCHAR(100),                           -- 如：男

    -- ==================== 展示 ====================
    sort_order          INTEGER         DEFAULT 0,
    is_active           BOOLEAN         DEFAULT TRUE,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_severity CHECK (severity IN ('HARD', 'SOFT'))
);
```

### 3.2 专业约束关联表


```
-- ============================================================
-- 专业约束关联表 (t_major_constraint)
-- 描述：哪些专业有哪些报考限制
--       t_major 与 t_constraint_dict 的多对多关系
-- ============================================================

CREATE TABLE IF NOT EXISTS t_major_constraint (

    id                  SERIAL          PRIMARY KEY,
    major_code          VARCHAR(20)     NOT NULL,           -- 专业代码 → t_major.major_code
    major_name          VARCHAR(50)     NOT NULL,           -- 专业名称 → t_major.major_name
    constraint_code     VARCHAR(50)     NOT NULL,           -- 约束代码 → t_constraint_dict.code
    constraint_name     VARCHAR(50)     NOT NULL,           -- 约束代码 → t_constraint_dict.name
    remark              VARCHAR(200)    NOT NULL,           -- 备注（如具体原因）

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_major_constraint
        UNIQUE (major_code, constraint_code)
);

CREATE INDEX idx_mc_major ON t_major_constraint (major_code);
CREATE INDEX idx_mc_constraint ON t_major_constraint (constraint_code);


-- ----------------------------------------------------------
-- 示例数据示例（flyway忽略）
-- ----------------------------------------------------------
INSERT INTO t_major_constraint (major_code, constraint_code, remark) VALUES

    -- ===== 临床医学 (100201) =====
    ('100201', 'NO_COLOR_BLIND_WEAK',   '体检标准要求'),
    ('100201', 'NO_SMELL_DISORDER',     '临床诊断需要嗅觉'),
    ('100201', 'ONLY_FRESH',            '部分学校本硕连读只招应届'),
    
    ('130309', 'NO_SCAR',               '镜前形象要求');
```

### 2.1 安全系数等级字典


```
-- ============================================================
-- 安全系数等级字典 (t_safety_level_dict)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_safety_level_dict (

    level               SMALLINT        PRIMARY KEY,            -- 等级编号 1-5
    code                VARCHAR(20)     NOT NULL UNIQUE,        -- 代码
    name                VARCHAR(30)     NOT NULL,               -- 中文名称
    name_short          VARCHAR(10)     NOT NULL,               -- 简称（前端标签用）
    min_coefficient     NUMERIC(3,2)    NOT NULL,               -- 系数下界（含）
    max_coefficient     NUMERIC(3,2)    NOT NULL,               -- 系数上界（不含）
    color               VARCHAR(20),                            -- 前端显示颜色
    confidence          VARCHAR(20),
   -- 置信度（HIGH / MEDIUM / LOW）
    confidenceReason    VARCHAR(150),
   -- 置信度说明
    description         TEXT,                                   -- 说明

    CONSTRAINT chk_coeff_range CHECK (min_coefficient < max_coefficient)
);


INSERT INTO t_safety_level_dict
    (level, code, name, name_short, min_coefficient, max_coefficient, color, description)
VALUES
    (1, 'REACH_HIGH',   '大胆冲刺',   '搏',   0.00, 0.30,
     '#FF4D4F',
     '录取概率极低，属于"彩票"志愿。历年数据显示您的位次远低于该校录取位次。' ||
     '建议最多填1-2个冲刺志愿，且必须搭配稳妥志愿。'),

    (2, 'REACH',        '可以冲击',   '冲',   0.30, 0.50,
     '#FFA940',
     '有一定录取可能，但风险较大。适合放在志愿表靠前位置。' ||
     '如果该校当年报考热度下降或扩招，有希望录取。'),

    (3, 'MATCH',        '较为稳妥',   '稳',   0.50, 0.70,
     '#FADB14',
     '录取概率中等偏上，属于"正常发挥"就能录取的范围。' ||
     '建议作为志愿表的核心区域，多填几个此档位的志愿。'),

    (4, 'SAFE',         '比较安全',   '保',   0.70, 0.85,
     '#52C41A',
     '录取概率较高，除非出现大小年极端波动，基本能录取。' ||
     '建议至少填3-5个此档位的志愿作为安全保障。'),

    (5, 'FLOOR',        '高度保底',   '垫',   0.85, 1.00,
     '#1890FF',
     '录取概率极高，几乎确定能录取。用于防止"滑档"（所有志愿都录不上）。' ||
     '建议至少放1-2个垫底志愿在最后。');
```
