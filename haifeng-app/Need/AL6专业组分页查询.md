# 总的项目需求

## 数据库表：

所有数据库表都已建好，下面我粘贴过来了

-- ===========================================================
-- 1. t_admission_group（专业组录取表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_group(
id                      SERIAL          PRIMARY KEY,
university_id           BIGINT          NOT NULL,
university_name         VARCHAR(50)     NOT NULL,
city_name               VARCHAR(50)     NOT NULL,
year                    SMALLINT        NOT NULL,
province                VARCHAR(20)     NOT NULL,
batch                   VARCHAR(50)     NOT NULL,
enrollment_code         VARCHAR(30),
group_code              VARCHAR(30)     NOT NULL,
group_name              VARCHAR(100),
subjects                TEXT[]          DEFAULT '{}',
requirement_type        VARCHAR(10)     DEFAULT '不限',
description             TEXT,
constraints             TEXT[]          DEFAULT '{}',
major_count             INTEGER         DEFAULT 0,
category_count          INTEGER         DEFAULT 0,
admission_count         INTEGER,
min_score               INTEGER,
min_rank                INTEGER,
max_score               INTEGER,
max_rank                INTEGER,
avg_score               NUMERIC(6,2),
avg_rank                INTEGER,
is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_admission_group UNIQUE (university_id, year, province, batch, group_code),
    CONSTRAINT chk_req_type CHECK (requirement_type IN ('不限', '2选1', '3选1', '必选1', '必选2', '必选3'))
);


-- 注释
COMMENT ON TABLE t_admission_group IS '专业组录取表：记录高校各专业组的录取信息';
COMMENT ON COLUMN t_admission_group.id IS '主键ID（自增）';
COMMENT ON COLUMN t_admission_group.university_id IS '院校ID（关联 t_university.id）';
COMMENT ON COLUMN t_admission_group.year IS '招生年份';
COMMENT ON COLUMN t_admission_group.province IS '招生省份';
COMMENT ON COLUMN t_admission_group.batch IS '录取批次（如：本科批、提前批、专科批）';
COMMENT ON COLUMN t_admission_group.subjects IS '选科科目数组（如：{物理,化学}）';
COMMENT ON COLUMN t_admission_group.requirement_type IS '选科要求类型：不限/2选1/3选1/必选1/必选2/必选3';
COMMENT ON COLUMN t_admission_group.enrollment_code IS '招生代码';
COMMENT ON COLUMN t_admission_group.group_code IS '专业组代码';
COMMENT ON COLUMN t_admission_group.group_name IS '专业组名称';
COMMENT ON COLUMN t_admission_group.description IS '专业组说明';
COMMENT ON COLUMN t_admission_group.constraints IS '约束条件数组（如：{只招男生,色盲不可报考}）';
COMMENT ON COLUMN t_admission_group.major_count IS '包含专业数量';
COMMENT ON COLUMN t_admission_group.category_count IS '包含专业类数量';
COMMENT ON COLUMN t_admission_group.admission_count IS '录取人数';
COMMENT ON COLUMN t_admission_group.min_score IS '最低分';
COMMENT ON COLUMN t_admission_group.min_rank IS '最低分对应位次';
COMMENT ON COLUMN t_admission_group.max_score IS '最高分';
COMMENT ON COLUMN t_admission_group.max_rank IS '最高分对应位次';
COMMENT ON COLUMN t_admission_group.avg_score IS '平均分';
COMMENT ON COLUMN t_admission_group.avg_rank IS '平均位次';
COMMENT ON COLUMN t_admission_group.is_deleted IS '是否删除：FALSE=正常，TRUE=已删除';
COMMENT ON COLUMN t_admission_group.created_at IS '创建时间';
COMMENT ON COLUMN t_admission_group.updated_at IS '更新时间';

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
updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
CONSTRAINT uk_city_name UNIQUE (city_name)
);

-- 索引
CREATE INDEX idx_city_province ON t_city (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_region ON t_city (region) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_gdp ON t_city (gdp DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_city_population ON t_city (resident_population DESC NULLS LAST) WHERE is_deleted = FALSE;
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

-- 院校主表
CREATE TABLE t_universities (
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
CREATE INDEX idx_univ_name ON t_universities(name);
CREATE INDEX idx_univ_province ON t_universities(province_name);
CREATE INDEX idx_univ_category ON t_universities(category);
CREATE INDEX idx_univ_status ON t_universities(status);
CREATE INDEX idx_univ_tags ON t_universities USING GIN(tags);
CREATE UNIQUE INDEX idx_univ_name_unique ON t_universities(name) WHERE status = 1;

-- 院校主表注释
COMMENT ON TABLE t_universities IS '院校主表';
COMMENT ON COLUMN t_universities.id IS '院校ID（雪花算法）';
COMMENT ON COLUMN t_universities.name IS '院校名称';
COMMENT ON COLUMN t_universities.name_en IS '院校英文名称';
COMMENT ON COLUMN t_universities.province_name IS '省份';
COMMENT ON COLUMN t_universities.city_name IS '城市';
COMMENT ON COLUMN t_universities.region IS '所属地区';
COMMENT ON COLUMN t_universities.category IS '院校类别（综合/理工/师范等）';
COMMENT ON COLUMN t_universities.major_count IS '专业数量';
COMMENT ON COLUMN t_universities.education_level IS '办学层次（本科/专科/本专兼招）';
COMMENT ON COLUMN t_universities.nature IS '院校性质（公办/民办/中外合作）';
COMMENT ON COLUMN t_universities.recommendation_rate IS '推免率（百分比）';
COMMENT ON COLUMN t_universities.recommendation_year IS '推免年份';
COMMENT ON COLUMN t_universities.has_doctorate IS '是否有博士点';
COMMENT ON COLUMN t_universities.has_master IS '是否有硕士点';
COMMENT ON COLUMN t_universities.department IS '隶属部门';
COMMENT ON COLUMN t_universities.tags IS '院校标签数组';
COMMENT ON COLUMN t_universities.famous_union IS '知名联盟';
COMMENT ON COLUMN t_universities.image_url IS '院校图片URL';
COMMENT ON COLUMN t_universities.introduction IS '院校简介';
COMMENT ON COLUMN t_universities.sort_order IS '排序权重';
COMMENT ON COLUMN t_universities.status IS '状态: 0-下架 1-展示';

-- 4. 学科评估表 (t_subject_evaluation)
CREATE TABLE t_subject_evaluation (
id                    BIGINT        PRIMARY KEY,
university_id         BIGINT        NOT NULL,
university_name       VARCHAR(100)  NOT NULL,
discipline_code       VARCHAR(20)   NOT NULL,
discipline_name       VARCHAR(100)  NOT NULL,
evaluation_round      VARCHAR(20)   DEFAULT '第四轮',
evaluation_grade      VARCHAR(5)    NOT NULL,
sort_order            INTEGER       DEFAULT 0,
status                SMALLINT      DEFAULT 1 NOT NULL,
created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT chk_eval_grade CHECK (
evaluation_grade IN ('A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C', 'C-')
)
);

CREATE INDEX idx_se_university ON t_subject_evaluation(university_id);
CREATE INDEX idx_se_discipline ON t_subject_evaluation(discipline_name, evaluation_grade);
CREATE INDEX idx_se_grade ON t_subject_evaluation(evaluation_grade);
CREATE INDEX idx_se_status ON t_subject_evaluation(status);

COMMENT ON TABLE t_subject_evaluation IS '学科评估表';
COMMENT ON COLUMN t_subject_evaluation.discipline_code IS '学科代码（4位）';
COMMENT ON COLUMN t_subject_evaluation.discipline_name IS '学科名称';
COMMENT ON COLUMN t_subject_evaluation.evaluation_round IS '评估轮次';
COMMENT ON COLUMN t_subject_evaluation.evaluation_grade IS '评估等级（A+/A/A-/B+/B/B-/C+/C/C-）';


#### 任务一 【安全限制计算安全系数权重】

10.在专业组的上面会有一些查询的条件，点特定的条件会筛选特定的专业组
> [!NOTE] 技术指南
> 数据库：院校标签字典表 (t_university_tag_dict)，院校表 (universities)，学科评估表 (t_subject_evaluation)，专业组录取表 (t_admission_group)
> 前端：会展示多个筛选框，有：
> 一、大学相关
> 1. 院校层次：985，211，一流大学，一流学科，本专兼招，101计划，卓越工程师，国优计划，双高A档，双高B档，双高C档，国家骨干高职，国家示范高职
> 2. 院校性质：公办，民办，中外合作
> 3. 知名联盟：C9联盟，E9联盟，G7联盟，Z14联盟
> 4. 院校类别：综合类，理工类，师范类，医药类，财经类，政法类，农林类，语言类，艺术类，体育类，民族类，军事类，其他
> 5. 办学层次：本科，专科，本专兼招
> 6. 隶属部门：教育部，工信部，公安部，司法部，外交部，应急管理部，国家卫健委，体育总局，国家民委，交通运输部，民航总局，海关总署，中科院，中央军委，其他
> 二、城市相关
> 6. 所属省份
> 7. 所属地区
> 8. 常住人口数量：大于小于等于，2000万人，3000万人，4000万人
> 三、专业相关
> 10. 专业类别：工学，理学，医学，经济学，管理学，文学，法学，教育学，艺术学，农学，历史学，哲学
> 11. 专业标签：热门专业，新兴专业，传统专业
> 12. 专业类型：本科，专科，不限
> 四、学科评估（9个值）：A、B、C（+、-）(例如：A、B-、C+)
> 一共四个维度，大学里面的院校层次是多选的，然后当用户选完这些之后，他会筛选出大学名来，然后你点击大学名，会显示只跟这个大学有关的专业组。同理城市，也是联表查询显示出大学名，再根据大学去查，专业很重要，筛选出一些专业供用户选择，选择好了以后，会显示对应的专业组，学科评估也是一样会显示对应的院校。
> 五、有一个搜索框：可以输入大学名称，模糊查询专业组的大学名，显示对应的专业组
> 六、第一个专业组的最上面有一个筛选安全系数的范围，点击范围筛选即可（redis已实现）
> 后端：
> 13. 大学相关里面院校层次对应tags（多选），知名联盟对应famous_union，院校性质对应nature，和院校类别对应：category，办学层次对应education_level，隶属部门对应department。从而筛选出对应的大学id，前端展示大学名，多选大学然后展示专业组，精准查询
> 14. 城市对应的是在大学表里面有一个city_name的字段，筛选的时候筛选出对应的很多的大学id,那么用户点击哪个大学就显示哪个大学的，可以多选
> 16. 学科评估表 (t_subject_evaluation)里有一个大学的外键（一对多），直接展示大学即可。
> 17. 用户输入大学名模糊查询专业组列表，这个很好实现，就是模糊查询即可。
> 18. 匹配度的查询后面将。

##### 用户限制：
普通用户仅仅可以查询10条数据，其他的都是占位符，提示需要充值升级身份，然后升级之后需要刷新才能看到新的。


#### 注意事项
1. 一、大学相关的、二、城市相关的、三、专业相关、四、学科评估里面的筛选条件他们都是独立的，不需要and符号，也就是他们里面只能筛选一个
2. 一，二，三，四作为一个主体他们之间可以and
3. 五和六，模糊查询的大学名称和redis可以互补，进行进一步的筛选
4. 一，二，三，四可以跟五和六and








