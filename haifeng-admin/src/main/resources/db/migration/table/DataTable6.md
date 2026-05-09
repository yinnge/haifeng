## 二、实验室表 (laboratories)

```
-- ============================================
-- 实验室表 (laboratories)
-- 说明：高校实验室/科研平台信息
-- ============================================

CREATE TABLE laboratories (
    id                    SERIAL        PRIMARY KEY                           ,-- 实验室ID
    university_id         INTEGER       NOT NULL                              ,-- 所属院校ID（关联院校表）
    university_name       varchar(50)       NOT NULL                              ,-- 所属院校名（冗余）
    
    -- 基本信息
    name                  VARCHAR(200)  NOT NULL                              ,-- 实验室名称
    lab_type              VARCHAR(100)  NOT NULL                              ,-- 实验室类型
    established_year      VARCHAR(20)                                         ,-- 成立时间（年份）
    region                VARCHAR(100)                                        ,-- 所在地区
    department            VARCHAR(100)                                        ,-- 主管部门
    director              VARCHAR(50)                                         ,-- 实验室主任
    
    -- 规模信息
    staff_count           VARCHAR(50)                                         ,-- 人员规模
    student_count         VARCHAR(50)                                         ,-- 学生规模
     
    -- 联系方式
    email                 VARCHAR(200)                                        ,-- 联系邮箱
    phone                 VARCHAR(50)                                         ,-- 联系电话
    
    -- 详细介绍（纯文本/富文本）
    introduction          TEXT                                                ,-- 实验室简介
    research_description  TEXT                                                ,-- 研究方向描述
    lab_space             TEXT                                                ,-- 实验室空间描述
    open_topics           TEXT                                                ,-- 开放课题
    cooperation           TEXT                                                ,-- 合作交流
    visiting_scholars     TEXT                                                ,-- 访问学者
    
    -- 复杂数据
    research_fields       text[]         
  ,-- 研究领域
    statistics            JSONB         DEFAULT '[]'::JSONB                   ,-- 统计数据(varchar(50) tag,Integer amount)
    major_equipment       text[]         
   ,-- 主要设备
    core_team             JSONB         DEFAULT '[]'::JSONB                   ,-- 核心团队(core_team:varchar(50) name,varchar(50) job,varchar(50) job_name)
    
    -- 通用字段
    sort_order            INTEGER       DEFAULT 0                             ,-- 排序
    status                SMALLINT      DEFAULT 1             NOT NULL        ,-- 状态: 0-下架 1-展示
    created_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL   ,-- 创建时间
    updated_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_labs_university_id ON laboratories(university_id);
CREATE INDEX idx_labs_lab_type      ON laboratories(lab_type);
CREATE INDEX idx_labs_region        ON laboratories(region);
CREATE INDEX idx_labs_status        ON laboratories(status);

-- ================================
-- 字段注释
-- ================================
COMMENT ON TABLE  laboratories                         IS '实验室表，记录高校实验室/科研平台信息';
COMMENT ON COLUMN laboratories.id                      IS '实验室ID';
COMMENT ON COLUMN laboratories.university_id           IS '所属院校ID，关联院校表，一个院校可有多个实验室';
COMMENT ON COLUMN laboratories.name                    IS '实验室名称，如：摩擦学国家重点实验室';
COMMENT ON COLUMN laboratories.lab_type                IS '实验室类型，如：国家重点实验室、国家工程研究中心、教育部重点实验室';
COMMENT ON COLUMN laboratories.established_year        IS '成立时间，如：1988年';
COMMENT ON COLUMN laboratories.region                  IS '所在地区，如：北京市';
COMMENT ON COLUMN laboratories.department              IS '主管部门，如：教育部、科技部';
COMMENT ON COLUMN laboratories.director                IS '实验室主任，如：雒建斌 院士';
COMMENT ON COLUMN laboratories.staff_count             IS '人员规模，如：85人（用VARCHAR因为可能写"约85人"）';
COMMENT ON COLUMN laboratories.student_count           IS '学生规模，如：200余人';
COMMENT ON COLUMN laboratories.email                   IS '联系邮箱';
COMMENT ON COLUMN laboratories.phone                   IS '联系电话';
COMMENT ON COLUMN laboratories.introduction            IS '实验室简介，支持富文本';
COMMENT ON COLUMN laboratories.research_description    IS '研究方向描述，支持富文本';
COMMENT ON COLUMN laboratories.lab_space               IS '实验室空间描述';
COMMENT ON COLUMN laboratories.open_topics             IS '开放课题信息';
COMMENT ON COLUMN laboratories.cooperation             IS '合作交流信息';
COMMENT ON COLUMN laboratories.visiting_scholars       IS '访问学者信息';

COMMENT ON COLUMN laboratories.research_fields    IS '研究领域，JSONB数组，如：["摩擦学","表面工程","纳米制造技术"]';
COMMENT ON COLUMN laboratories.statistics         IS '统计数据，JSONB数组，如：[{"label":"发表论文","count":500},{"label":"授权专利","count":120}]';
COMMENT ON COLUMN laboratories.major_equipment    IS '主要设备，JSONB数组，如：["扫描电子显微镜","原子力显微镜","X射线衍射仪"]';
COMMENT ON COLUMN laboratories.core_team          IS '核心团队，JSONB数组，如：[{"name":"张三","position":"主任","title":"教授/院士"}]';

COMMENT ON COLUMN laboratories.sort_order         IS '排序权重，数字越小越靠前';
COMMENT ON COLUMN laboratories.status             IS '状态: 0-下架不展示 1-正常展示';
COMMENT ON COLUMN laboratories.created_at         IS '创建时间';
COMMENT ON COLUMN laboratories.updated_at         IS '更新时间';
```

# 院系主表 (t_department)

```
-- ============================================
-- 院系主表 (t_department)
-- 说明：院系基本信息主表
--       存储院系的核心属性
-- ============================================

CREATE TABLE t_department (
    id                    SERIAL        PRIMARY KEY                           ,-- 院系ID
    university_id         INTEGER       NOT NULL                              ,-- 所属院校ID（关联院校表）
    university_name       VARCHAR(30)  NOT NULL                              ,-- 院校名称（冗余）
    -- ========== 基本信息 ==========
    department_name       VARCHAR(100)  NOT NULL                              ,-- 院系名称
    department_type       VARCHAR(100)  NOT NULL                              ,-- 院系类型
    page_title            VARCHAR(200)                                        ,-- 页面主标题
    tags                  TEXT[]                                              ,-- 院系标签

    -- ========== 通用字段 ==========
    sort_order            INTEGER       DEFAULT 0                             ,-- 排序
    status                SMALLINT      DEFAULT 1             NOT NULL        ,-- 状态: 0-下架 1-展示
    created_at            TIMESTAMPZ     DEFAULT CURRENT_TIMESTAMP  NOT NULL   ,-- 创建时间
    updated_at            TIMESTAMPZ     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_department_university_id ON t_department(university_id);
CREATE INDEX idx_department_status        ON t_department(status);
CREATE INDEX idx_department_dept_name     ON t_department(department_name);
CREATE INDEX idx_department_dept_type     ON t_department(department_type);

-- ================================
-- 表和字段注释
-- ================================
COMMENT ON TABLE  t_department                        IS '院系主表，存储院系基本信息';
COMMENT ON COLUMN t_department.id                     IS '院系ID';
COMMENT ON COLUMN t_department.university_id          IS '所属院校ID，关联院校表';
COMMENT ON COLUMN t_department.department_name        IS '院系名称，如：数学科学学院';
COMMENT ON COLUMN t_department.department_type        IS '院系类型，如：理学院';
COMMENT ON COLUMN t_department.page_title             IS '页面主标题，如：北京大学数学科学学院';
COMMENT ON COLUMN t_department.tags                   IS '院系标签，JSON字符串数组';
COMMENT ON COLUMN t_department.sort_order             IS '排序权重，数字越小越靠前';
COMMENT ON COLUMN t_department.status                 IS '状态: 0-下架 1-展示';
COMMENT ON COLUMN t_department.created_at             IS '创建时间';
COMMENT ON COLUMN t_department.updated_at             IS '更新时间';
```

# 院系分析报告表 (department_reports)


```
-- ============================================
-- 院系分析报告表 (department_reports)
-- 说明：院系专业就业与薪资深度分析报告
--       每个院系一条记录，包含完整的分析报告数据
-- ============================================

CREATE TABLE department_reports (
    id                    SERIAL        PRIMARY KEY                           ,-- 报告ID
    department_id         INTEGER       NOT NULL                              ,-- 院系ID（关联院系主表 t_department）

    -- ========== 基本信息 ==========
    subtitle              VARCHAR(200)                                        ,-- 副标题
    -- ========== 学院概况 ==========
    overview          JSONB         DEFAULT '{}'::JSONB                   ,-- 学院定位（varchar(50) title,TEXT[] content ）

    -- ========== 学科体系构成 ==========
    majors_detail         JSONB         DEFAULT '[]'::JSONB                   ,-- 专业详情列表（varchar(50) title,JSONB Compose(varchar(50) subject_name,NUMERIC(5,2) percent)）
    
    -- ========== 专业体系概览 ==========
    subjects_detail         JSONB         DEFAULT '[]'::JSONB                   ,-- 专业详情列表（varchar(50) title,TEXT[] content）
    
    -- ========== 专业详情 (majors数组) ==========
    subjects_detail         JSONB         DEFAULT '[]'::JSONB                   ,-- 专业详情列表(varchar(50) subject_name,TEXT[] tags,JSONB structure(varchar(50) core,varchar(50) suppory,varchar(50) positioning), 
    TEXT[] courses,TEXT[] abilities,TEXT[] certificates)
    
    -- ========== 考研分析 ==========
    postgraduate          JSONB         DEFAULT '{}'::JSONB                   ,-- 考研分析(varchar(50) title,context text[],)

    -- ========== 城市等级薪资数据 ==========
    city_salary                JSONB         DEFAULT '{}'::JSONB                   ,-- 不同城市薪资对比数据（title(varchar(20),最低薪资(单位：万)(integer)，最高薪资(integer))）
    
    -- ========== 不同专业薪资数据 ==========
    salary                JSONB         DEFAULT '{}'::JSONB                   ,-- 专业薪资对比数据（title(varchar(20),最低薪资(单位：万)(integer)，最高薪资(integer))）

    -- ========== 职业发展路径 ==========
    career                JSONB         DEFAULT '[]'::JSONB                   ,-- 职业发展路径（title(varchar 20),description(varchar 100),小标题，年限（varchar），职位（varchar 100）,核心目标（varchar 100），薪资范围（varchar 100））
    
    -- ========== 行业趋势 ==========
    trends                JSONB         DEFAULT '[]'::JSONB                   ,-- 行业趋势卡片（高速增长赛道，核心政策导向，就业环境分析（类型全是text[]））

    -- ========== 数据概览 ==========
    prospects             JSONB         DEFAULT '[]'::JSONB                   ,-- 数据概览卡片(综合就业率,硕士平均起薪,继续深造率,进入世界500强,年薪增长率,海外深造占比(这些全填写字符串（10）))

    -- ========== 免责声明 ==========
    disclaimer            JSONB         DEFAULT '{}'::JSONB                   ,-- 免责声明(text(text),更新时间（varchar 20），报告版本（varchar 20）,编制单位（varchar 30）)

    -- ========== 专业组成 ==========
    major_compose         JSONB         DEFAULT '{}'::JSONB                   ,-- 专业组成

    -- ========== 通用字段 ==========
    sort_order            INTEGER       DEFAULT 0                             ,-- 排序
    status                SMALLINT      DEFAULT 1             NOT NULL        ,-- 状态: 0-下架 1-展示
    created_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL   ,-- 创建时间
    updated_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_dept_reports_department_id ON department_reports(department_id);
CREATE INDEX idx_dept_reports_status        ON department_reports(status);

-- ================================
-- 表和字段注释
-- ================================
COMMENT ON TABLE  department_reports                        IS '院系分析报告表，每个院系一份完整的就业薪资分析报告';
COMMENT ON COLUMN department_reports.id                     IS '报告ID';
COMMENT ON COLUMN department_reports.department_id          IS '院系ID，关联院系主表 t_department';
COMMENT ON COLUMN department_reports.subtitle               IS '副标题，如：专业就业与薪资深度分析报告（2025版）';
COMMENT ON COLUMN department_reports.report_version         IS '报告版本，如：V3.0';
COMMENT ON COLUMN department_reports.data_update_time       IS '数据更新时间，如：2025年10月';
COMMENT ON COLUMN department_reports.overview               IS '学院概况，含定位特色/学科体系/专业体系三个子板块';
COMMENT ON COLUMN department_reports.majors_detail          IS '专业详情数组，每个专业含标签/课程/能力/证书等';
COMMENT ON COLUMN department_reports.employment             IS '就业方向卡片数组，每个专业的就业方向和企业分类';
COMMENT ON COLUMN department_reports.postgraduate           IS '考研分析，含必考/可选/优势三个子板块';
COMMENT ON COLUMN department_reports.salary                 IS '薪资数据，含城市对比/专业对比及数据来源';
COMMENT ON COLUMN department_reports.career                 IS '职业发展路径数组，每条路径含阶段信息';
COMMENT ON COLUMN department_reports.assessment             IS '专业评估卡片数组，含评分/标签/优势/适合人群';
COMMENT ON COLUMN department_reports.trends                 IS '行业趋势卡片数组，含趋势分类和具体条目';
COMMENT ON COLUMN department_reports.prospects              IS '数据概览卡片数组，含数值/标签/来源';
COMMENT ON COLUMN department_reports.disclaimer             IS '免责声明，含标题/内容/页脚';
COMMENT ON COLUMN department_reports.sort_order             IS '排序权重，数字越小越靠前';
COMMENT ON COLUMN department_reports.status                 IS '状态: 0-下架 1-展示';
COMMENT ON COLUMN department_reports.created_at             IS '创建时间';
COMMENT ON COLUMN department_reports.updated_at             IS '更新时间';
```

## 一、学科评估表 (t_subject_evaluation)


```
-- ============================================================
-- 学科评估表 (t_subject_evaluation)
-- 描述：教育部学科评估结果（第四轮/第五轮）
--       记录每所大学的各学科评估等级
-- ============================================================



CREATE TABLE IF NOT EXISTS t_subject_evaluation (

    id                      SERIAL          PRIMARY KEY,
    discipline_code VARCHAR(20) NOT NULL, -- 一级学科（4位代码）
    evaluation_round VARCHAR(20) NOT NULL DEFAULT '第四轮',
    university_id           INTEGER         NOT NULL,           -- 大学ID
    university_name         VARCHAR(100)    NOT NULL,           -- 大学名称（冗余）
    

    -- ==================== 学科信息 ====================
    discipline_name         VARCHAR(100)    NOT NULL,           -- 学科名称（如：计算机科学与技术）
    
    evaluation_grade        VARCHAR(5)      NOT NULL,           -- 评估等级

    -- ==================== 审计字段 ====================
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    
    -- 评估等级校验（9个固定值）
    CONSTRAINT chk_eval_grade CHECK (
        evaluation_grade IN ('A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C', 'C-')
    );


-- ----------------------------------------------------------
-- 索引
-- ----------------------------------------------------------

-- 按大学查所有学科评估
CREATE INDEX idx_se_university
    ON t_subject_evaluation (university_id)
    WHERE is_deleted = FALSE;

-- 按学科名查所有大学的评估（核心：专业页面查询）
CREATE INDEX idx_se_discipline
    ON t_subject_evaluation (discipline_name, evaluation_grade)
    WHERE is_deleted = FALSE;

-- 按评估等级筛选
CREATE INDEX idx_se_grade
    ON t_subject_evaluation (evaluation_grade)
    WHERE is_deleted = FALSE;

-- 学科名称搜索
CREATE INDEX idx_se_discipline_search
    ON t_subject_evaluation USING btree (discipline_name varchar_pattern_ops)
    WHERE is_deleted = FALSE;


-- 触发器
CREATE TRIGGER trg_subject_eval_updated_at
    BEFORE UPDATE ON t_subject_evaluation
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();


-- ----------------------------------------------------------
-- 注释
-- ----------------------------------------------------------
COMMENT ON TABLE  t_subject_evaluation                      IS '学科评估表（教育部学科评估结果）';
COMMENT ON COLUMN t_subject_evaluation.university_id        IS '大学ID';
COMMENT ON COLUMN t_subject_evaluation.university_name      IS '大学名称（冗余）';
COMMENT ON COLUMN t_subject_evaluation.discipline_name      IS '学科名称（一级学科，如：计算机科学与技术）';
COMMENT ON COLUMN t_subject_evaluation.evaluation_grade     IS '评估等级（A+/A/A-/B+/B/B-/C+/C/C-）';

COMMIT;
```