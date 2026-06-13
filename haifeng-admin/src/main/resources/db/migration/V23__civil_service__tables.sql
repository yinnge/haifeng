-- ============================================================
-- V23__civil_service__tables.sql
-- 模块二：体制内招录
-- 包含：公务员、事业编、部队文职、选调生
-- 共性：国考/省考/事业编/部队文职/选调生，统一考试体系，按 exam_type 区分
-- ============================================================

BEGIN;

-- ============================================================
-- 1. 公务员考试职位表 (t_civil_position)
-- 描述：国考/省考招录职位信息
-- ============================================================
CREATE TABLE IF NOT EXISTS t_civil_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 职位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    exam_type                   VARCHAR(20)     NOT NULL,
    recruiting_dept             VARCHAR(200),
    dept_code                   VARCHAR(30),
    position_code               VARCHAR(30),
    affiliated_bureau           VARCHAR(200),

    -- ==================== 报考要求 ====================
    major_requirement           VARCHAR(500),
    min_education               VARCHAR(20),
    degree_requirement          VARCHAR(20),
    political_status            VARCHAR(30),
    work_experience             VARCHAR(50),
    grassroots_experience       VARCHAR(50),

    -- ==================== 考试信息 ====================
    exam_category               VARCHAR(50),
    interview_ratio             VARCHAR(20),
    recruitment_count           INTEGER         DEFAULT 1,
    has_professional_test       BOOLEAN         DEFAULT FALSE,

    -- ==================== 工作地点 ====================
    work_location               VARCHAR(100),
    work_location_detail        VARCHAR(200),
    household_requirement       VARCHAR(100),
    household_location          VARCHAR(100),

    -- ==================== 其他信息 ====================
    position_intro              TEXT,
    remark                      TEXT,
    official_website            VARCHAR(500),
    contact_phone               VARCHAR(50),

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    reg_end_date                TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    reg_status                  VARCHAR(20)     DEFAULT '即将开始',
    applicant_count             INTEGER         DEFAULT 0,

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT uk_position_code UNIQUE (exam_type, dept_code, position_code),
    CONSTRAINT chk_exam_type CHECK (exam_type IN ('国考', '省考')),
    CONSTRAINT chk_min_education CHECK (
        min_education IS NULL OR min_education IN ('不限', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_degree_requirement CHECK (
        degree_requirement IS NULL OR degree_requirement IN ('不限', '学士', '硕士', '博士')
    ),
    CONSTRAINT chk_political_status CHECK (
        political_status IS NULL OR political_status IN ('不限', '中共党员', '共青团员', '群众')
    ),
    CONSTRAINT chk_reg_status CHECK (
        reg_status IS NULL OR reg_status IN ('报名中', '已结束', '即将开始')
    ),
    CONSTRAINT chk_recruitment_count CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_applicant_count CHECK (
        applicant_count IS NULL OR applicant_count >= 0
    )
);

CREATE INDEX idx_civil_exam_type        ON t_civil_position (exam_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_dept             ON t_civil_position (recruiting_dept) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_education        ON t_civil_position (min_education) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_location         ON t_civil_position (work_location) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_reg_status       ON t_civil_position (reg_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_position_name    ON t_civil_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_major            ON t_civil_position USING btree (major_requirement varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_applicant_count  ON t_civil_position (applicant_count DESC NULLS LAST) WHERE is_deleted = FALSE;
CREATE INDEX idx_civil_competition_ratio ON t_civil_position (applicant_count DESC, recruitment_count ASC) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_civil_position_updated_at
    BEFORE UPDATE ON t_civil_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

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


-- ============================================================
-- 2. 事业编职位表 (t_institution_position)
-- 描述：事业单位招聘职位信息
-- ============================================================
CREATE TABLE IF NOT EXISTS t_institution_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 职位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    supervising_dept            VARCHAR(200),
    institution                 VARCHAR(200),
    work_location               VARCHAR(100),
    province                    VARCHAR(30),
    exam_category               VARCHAR(50),
    position_type               VARCHAR(50),
    sub_category                VARCHAR(50),

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(20),
    degree_requirement          VARCHAR(20),
    age_limit                   INTEGER,
    recruitment_count           INTEGER         DEFAULT 1,
    salary_range                VARCHAR(50),
    reg_deadline                VARCHAR(30),
    major_requirements          TEXT[]          DEFAULT '{}',
    special_position            VARCHAR(100),
    other_requirement           VARCHAR(500),
    other_requirement_desc      TEXT,

    -- ==================== 备注信息 ====================
    remark_type                 VARCHAR(50),
    remark_desc                 TEXT,
    consultation_phone          VARCHAR(50),
    supervision_phone           VARCHAR(50),

    -- ==================== 状态与标签 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',
    position_tag                VARCHAR(20)     DEFAULT '无',
    tag_text                    VARCHAR(50),

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_inst_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('无要求', '大专', '本科', '硕士', '博士')
    ),
    CONSTRAINT chk_inst_degree CHECK (
        degree_requirement IS NULL
        OR degree_requirement IN ('无要求', '学士', '硕士', '博士')
    ),
    CONSTRAINT chk_inst_age CHECK (
        age_limit IS NULL OR (age_limit >= 18 AND age_limit <= 65)
    ),
    CONSTRAINT chk_inst_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_inst_status CHECK (
        position_status IS NULL OR position_status IN ('招聘中', '已结束')
    ),
    CONSTRAINT chk_inst_tag CHECK (
        position_tag IS NULL OR position_tag IN ('热门', '无', '急招')
    )
);

CREATE INDEX idx_inst_pos_name      ON t_institution_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_province  ON t_institution_position (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_location  ON t_institution_position (work_location) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_exam_cat  ON t_institution_position (exam_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_type      ON t_institution_position (position_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_education ON t_institution_position (education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_status    ON t_institution_position (position_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_tag       ON t_institution_position (position_tag) WHERE is_deleted = FALSE AND position_tag <> '无';
CREATE INDEX idx_inst_pos_dept      ON t_institution_position (supervising_dept) WHERE is_deleted = FALSE;
CREATE INDEX idx_inst_pos_majors    ON t_institution_position USING gin (major_requirements) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_inst_position_updated_at
    BEFORE UPDATE ON t_institution_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

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


-- ============================================================
-- 3. 部队文职岗位表 (t_military_position)
-- 描述：军队文职人员招聘岗位信息
-- ============================================================
CREATE TABLE IF NOT EXISTS t_military_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 岗位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    employer_unit               VARCHAR(200),
    department                  VARCHAR(200),
    position_type               VARCHAR(50),
    work_location               VARCHAR(100),
    salary_range                VARCHAR(50),

    -- ==================== 报考要求 ====================
    major_requirement           VARCHAR(500),
    education_requirement       VARCHAR(30),
    reg_deadline                VARCHAR(30),

    -- ==================== 状态 ====================
    position_status             VARCHAR(20)     DEFAULT '进行中',

    -- ==================== 描述信息 ====================
    position_description        TEXT,
    responsibilities            TEXT[]          DEFAULT '{}',
    qualifications              TEXT[]          DEFAULT '{}',

    -- ==================== 审计字段 ====================
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_mil_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('本科及以上', '硕士及以上', '博士')
    ),
    CONSTRAINT chk_mil_status CHECK (
        position_status IS NULL OR position_status IN ('进行中', '已结束')
    )
);

CREATE INDEX idx_mil_pos_name           ON t_military_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_unit           ON t_military_position (employer_unit) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_location       ON t_military_position (work_location) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_education      ON t_military_position (education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_status         ON t_military_position (position_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_type           ON t_military_position (position_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_responsibilities ON t_military_position USING gin (responsibilities) WHERE is_deleted = FALSE;
CREATE INDEX idx_mil_pos_qualifications ON t_military_position USING gin (qualifications) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_mil_position_updated_at
    BEFORE UPDATE ON t_military_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

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


-- ============================================================
-- 4. 选调生岗位表 (t_selection_position)
-- 描述：各省定向/非定向选调生招录岗位信息
-- 说明：选调生由省委组织部统一组织，面向应届优秀大学毕业生
-- ============================================================
CREATE TABLE IF NOT EXISTS t_selection_position (
    id                          SERIAL          PRIMARY KEY,

    -- ==================== 招录基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    selection_type              VARCHAR(50)     NOT NULL,
    year                        VARCHAR(10)     NOT NULL,
    province                    VARCHAR(30)     NOT NULL,
    organizing_dept             VARCHAR(200),
    target_unit                 VARCHAR(200),
    work_location               VARCHAR(200),

    -- ==================== 培养信息 ====================
    training_direction          VARCHAR(100),
    grassroots_service_years    VARCHAR(30),
    training_plan               TEXT,

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30)     NOT NULL,
    degree_requirement          VARCHAR(30),
    major_requirement           VARCHAR(500),
    major_categories            TEXT[]          DEFAULT '{}',

    -- ==================== 选调生特殊要求 ====================
    university_requirement      VARCHAR(100),
    target_universities         TEXT[]          DEFAULT '{}',
    political_status            VARCHAR(30)     NOT NULL DEFAULT '中共党员',
    student_cadre_requirement   VARCHAR(200),
    awards_requirement          TEXT,
    age_limit                   INTEGER,

    -- ==================== 考试信息 ====================
    recruitment_count           INTEGER         DEFAULT 1,
    exam_subjects               VARCHAR(200),
    interview_form              VARCHAR(100),

    -- ==================== 报名时间 ====================
    reg_start_date              TIMESTAMPTZ,
    reg_end_date                TIMESTAMPTZ,
    exam_time                   TIMESTAMPTZ,
    apply_link                  VARCHAR(500),

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '报名中',
    remark                      TEXT,
    contact_phone               VARCHAR(50),
    official_link               VARCHAR(500),
    content                     TEXT,

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

CREATE INDEX idx_sel_province         ON t_selection_position (province) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_type             ON t_selection_position (selection_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_year             ON t_selection_position (year) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_province_year_type ON t_selection_position (province, year, selection_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_education        ON t_selection_position (education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_university       ON t_selection_position (university_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_target_unis      ON t_selection_position USING gin (target_universities) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_major_cats       ON t_selection_position USING gin (major_categories) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_pos_name         ON t_selection_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_status           ON t_selection_position (position_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_sel_reg_end          ON t_selection_position (reg_end_date ASC NULLS LAST) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_sel_position_updated_at
    BEFORE UPDATE ON t_selection_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

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
COMMENT ON COLUMN t_selection_position.remark                       IS '备注';
COMMENT ON COLUMN t_selection_position.contact_phone                IS '联系电话';
COMMENT ON COLUMN t_selection_position.official_link                IS '官方公告链接';
COMMENT ON COLUMN t_selection_position.content                      IS '详细说明（支持HTML）';

COMMIT;