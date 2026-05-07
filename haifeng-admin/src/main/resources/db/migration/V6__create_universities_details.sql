-- ============================================
-- V6__create_universities_details.sql
-- 院校次表：实验室、院系、学科评估
-- ============================================

-- 1. 实验室表 (laboratories)
CREATE TABLE laboratories (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL,
    university_name       VARCHAR(50)   NOT NULL,
    name                  VARCHAR(200)  NOT NULL,
    lab_type              VARCHAR(100)  NOT NULL,
    established_year      VARCHAR(20),
    region                VARCHAR(100),
    department            VARCHAR(100),
    director              VARCHAR(50),
    staff_count           VARCHAR(50),
    student_count         VARCHAR(50),
    email                 VARCHAR(200),
    phone                 VARCHAR(50),
    introduction          TEXT,
    research_description  TEXT,
    lab_space             TEXT,
    open_topics           TEXT,
    cooperation           TEXT,
    visiting_scholars     TEXT,
    research_fields       TEXT[],
    statistics            JSONB         DEFAULT '[]'::JSONB,
    major_equipment       TEXT[],
    core_team             JSONB         DEFAULT '[]'::JSONB,
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_labs_university_id ON laboratories(university_id);
CREATE INDEX idx_labs_lab_type ON laboratories(lab_type);
CREATE INDEX idx_labs_region ON laboratories(region);
CREATE INDEX idx_labs_status ON laboratories(status);
CREATE INDEX idx_labs_name ON laboratories(name);

COMMENT ON TABLE laboratories IS '实验室表';
COMMENT ON COLUMN laboratories.id IS '实验室ID（雪花算法）';
COMMENT ON COLUMN laboratories.university_id IS '所属院校ID';
COMMENT ON COLUMN laboratories.university_name IS '院校名称（冗余）';
COMMENT ON COLUMN laboratories.name IS '实验室名称';
COMMENT ON COLUMN laboratories.lab_type IS '实验室类型';
COMMENT ON COLUMN laboratories.statistics IS '统计数据JSONB';
COMMENT ON COLUMN laboratories.core_team IS '核心团队JSONB';

-- 2. 院系主表 (t_department)
CREATE TABLE t_department (
    id                    BIGINT        PRIMARY KEY,
    university_id         BIGINT        NOT NULL,
    university_name       VARCHAR(50)   NOT NULL,
    department_name       VARCHAR(100)  NOT NULL,
    department_type       VARCHAR(100)  NOT NULL,
    page_title            VARCHAR(200),
    tags                  TEXT[],
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dept_university_id ON t_department(university_id);
CREATE INDEX idx_dept_status ON t_department(status);
CREATE INDEX idx_dept_name ON t_department(department_name);
CREATE INDEX idx_dept_type ON t_department(department_type);

COMMENT ON TABLE t_department IS '院系主表';
COMMENT ON COLUMN t_department.id IS '院系ID（雪花算法）';
COMMENT ON COLUMN t_department.university_id IS '所属院校ID';
COMMENT ON COLUMN t_department.department_name IS '院系名称';
COMMENT ON COLUMN t_department.department_type IS '院系类型';

-- 3. 院系分析报告表 (department_reports)
CREATE TABLE department_reports (
    id                    BIGINT        PRIMARY KEY,
    department_id         BIGINT        NOT NULL UNIQUE,
    subtitle              VARCHAR(200),
    overview              JSONB         DEFAULT '{}'::JSONB,
    subjects_detail       JSONB         DEFAULT '[]'::JSONB,
    postgraduate          JSONB         DEFAULT '{}'::JSONB,
    city_salary           JSONB         DEFAULT '[]'::JSONB,
    salary                JSONB         DEFAULT '[]'::JSONB,
    career                JSONB         DEFAULT '[]'::JSONB,
    trends                JSONB         DEFAULT '{}'::JSONB,
    prospects             JSONB         DEFAULT '{}'::JSONB,
    disclaimer            JSONB         DEFAULT '{}'::JSONB,
    major_compose         JSONB         DEFAULT '[]'::JSONB,
    sort_order            INTEGER       DEFAULT 0,
    status                SMALLINT      DEFAULT 1 NOT NULL,
    created_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dept_reports_department_id ON department_reports(department_id);
CREATE INDEX idx_dept_reports_status ON department_reports(status);

COMMENT ON TABLE department_reports IS '院系分析报告表（与院系1:1）';
COMMENT ON COLUMN department_reports.department_id IS '关联院系ID';
COMMENT ON COLUMN department_reports.overview IS '学院定位JSONB';
COMMENT ON COLUMN department_reports.subjects_detail IS '专业详情JSONB数组';
COMMENT ON COLUMN department_reports.major_compose IS '专业组成JSONB数组';

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
