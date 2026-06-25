-- ============================================================
-- V21__grassroots_position__tables.sql
-- 模块四：基层/社区/公益岗
-- 包含：基层服务项目（三支一扶+西部计划）、社区工作者、公益性岗位
-- 共性：门槛低、政策性岗位（服务期/期满政策/补贴标准）
-- ============================================================

BEGIN;

-- ============================================================
-- 1. 基层服务项目岗位表 (t_grassroots_project_position)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_grassroots_project_position (
    id                          BIGINT          PRIMARY KEY,

    -- ==================== 项目分类 ====================
    project_type                VARCHAR(30)     NOT NULL,
    year                        VARCHAR(10)     NOT NULL,

    -- ==================== 岗位基本信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    service_type                VARCHAR(50)     NOT NULL,
    organizing_dept             VARCHAR(200),
    service_unit                VARCHAR(200),

    -- ==================== 服务地点 ====================
    province                    VARCHAR(30)     NOT NULL,
    city                        VARCHAR(50),
    county                      VARCHAR(50),
    township                    VARCHAR(100),

    -- ==================== 服务期信息 ====================
    service_period              VARCHAR(30)     NOT NULL,
    service_start_date          VARCHAR(30),
    service_end_date            VARCHAR(30),

    -- ==================== 报名要求 ====================
    education_requirement       VARCHAR(30)     NOT NULL,
    major_requirement           VARCHAR(500),
    age_limit                   INTEGER,
    recruitment_count           INTEGER         DEFAULT 1,
    grad_year_requirement       VARCHAR(50),
    household_requirement       VARCHAR(100),
    other_requirement           TEXT,
    political_status            VARCHAR(30),

    -- ==================== 考试信息 ====================
    exam_content                VARCHAR(500),
    exam_time                   TIMESTAMPTZ,
    interview_form              VARCHAR(100),

    -- ==================== 待遇政策 ====================
    monthly_subsidy             VARCHAR(50),
    social_insurance            VARCHAR(200),
    housing_info                VARCHAR(200),
    other_benefits              TEXT,

    -- ==================== 期满政策 ====================
    after_service_policy        TEXT,
    can_transfer_to_civil       BOOLEAN         DEFAULT FALSE,
    can_transfer_to_institution BOOLEAN         DEFAULT FALSE,
    exam_bonus_points           VARCHAR(50),
    tuition_compensation        VARCHAR(100),
    postgrad_bonus              VARCHAR(100),

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ,
    reg_end_date                TIMESTAMPTZ,
    apply_link                  VARCHAR(500),
    position_status             VARCHAR(20)     DEFAULT '招募中',

    -- ==================== 补充信息 ====================
    contact_phone               VARCHAR(50),
    remark                      TEXT,
    content                     TEXT,

    -- ==================== 审计字段 ====================
    sort_order                   INTEGER         DEFAULT 0,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_gp_project_type CHECK (
        project_type IN ('三支一扶', '西部计划')
    ),
    CONSTRAINT chk_gp_service_type CHECK (
        service_type IN (
            '支教', '支农', '支医', '帮扶乡村振兴',
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
        position_status IS NULL OR position_status IN ('招募中', '已结束', '即将开始')
    )
);

CREATE INDEX idx_gp_project_type       ON t_grassroots_project_position (project_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_service_type       ON t_grassroots_project_position (service_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_location           ON t_grassroots_project_position (province, city, county) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_type_province_year ON t_grassroots_project_position (project_type, province, year) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_year               ON t_grassroots_project_position (year) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_education          ON t_grassroots_project_position (education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_pos_name           ON t_grassroots_project_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_status             ON t_grassroots_project_position (position_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_gp_can_transfer       ON t_grassroots_project_position (can_transfer_to_institution) WHERE is_deleted = FALSE AND can_transfer_to_institution = TRUE;

CREATE TRIGGER trg_gp_position_updated_at
    BEFORE UPDATE ON t_grassroots_project_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON TABLE  t_grassroots_project_position                                 IS '基层服务项目岗位表（三支一扶+西部计划）';
COMMENT ON COLUMN t_grassroots_project_position.project_type                    IS '项目类型（三支一扶/西部计划）';
COMMENT ON COLUMN t_grassroots_project_position.year                            IS '招募年份';
COMMENT ON COLUMN t_grassroots_project_position.position_name                   IS '岗位名称';
COMMENT ON COLUMN t_grassroots_project_position.service_type                    IS '服务类型';
COMMENT ON COLUMN t_grassroots_project_position.organizing_dept                 IS '组织单位';
COMMENT ON COLUMN t_grassroots_project_position.service_unit                    IS '服务单位';
COMMENT ON COLUMN t_grassroots_project_position.province                        IS '省份';
COMMENT ON COLUMN t_grassroots_project_position.city                            IS '城市';
COMMENT ON COLUMN t_grassroots_project_position.county                          IS '区/县';
COMMENT ON COLUMN t_grassroots_project_position.township                        IS '乡镇/街道';
COMMENT ON COLUMN t_grassroots_project_position.service_period                  IS '服务期限';
COMMENT ON COLUMN t_grassroots_project_position.service_start_date              IS '服务开始日期';
COMMENT ON COLUMN t_grassroots_project_position.service_end_date                IS '服务结束日期';
COMMENT ON COLUMN t_grassroots_project_position.education_requirement           IS '学历要求';
COMMENT ON COLUMN t_grassroots_project_position.major_requirement               IS '专业要求';
COMMENT ON COLUMN t_grassroots_project_position.age_limit                       IS '年龄上限（周岁）';
COMMENT ON COLUMN t_grassroots_project_position.recruitment_count               IS '招募人数';
COMMENT ON COLUMN t_grassroots_project_position.grad_year_requirement           IS '毕业年份要求';
COMMENT ON COLUMN t_grassroots_project_position.household_requirement           IS '户籍要求';
COMMENT ON COLUMN t_grassroots_project_position.political_status                IS '政治面貌';
COMMENT ON COLUMN t_grassroots_project_position.exam_content                    IS '笔试内容';
COMMENT ON COLUMN t_grassroots_project_position.exam_time                       IS '考试时间';
COMMENT ON COLUMN t_grassroots_project_position.interview_form                  IS '面试形式';
COMMENT ON COLUMN t_grassroots_project_position.monthly_subsidy                 IS '月补贴标准';
COMMENT ON COLUMN t_grassroots_project_position.social_insurance                IS '社保缴纳说明';
COMMENT ON COLUMN t_grassroots_project_position.housing_info                    IS '住房安排';
COMMENT ON COLUMN t_grassroots_project_position.after_service_policy            IS '期满政策综述';
COMMENT ON COLUMN t_grassroots_project_position.can_transfer_to_civil           IS '期满可否定向考公';
COMMENT ON COLUMN t_grassroots_project_position.can_transfer_to_institution     IS '期满可否转事业编';
COMMENT ON COLUMN t_grassroots_project_position.exam_bonus_points               IS '考试加分政策';
COMMENT ON COLUMN t_grassroots_project_position.tuition_compensation            IS '学费补偿/助学贷款代偿';
COMMENT ON COLUMN t_grassroots_project_position.postgrad_bonus                  IS '考研加分';
COMMENT ON COLUMN t_grassroots_project_position.reg_start_date                  IS '报名开始';
COMMENT ON COLUMN t_grassroots_project_position.reg_end_date                    IS '报名截止';
COMMENT ON COLUMN t_grassroots_project_position.apply_link                      IS '报名链接';
COMMENT ON COLUMN t_grassroots_project_position.position_status                 IS '状态（招募中/已结束/即将开始）';
COMMENT ON COLUMN t_grassroots_project_position.contact_phone                   IS '联系电话';
COMMENT ON COLUMN t_grassroots_project_position.remark                          IS '备注';
COMMENT ON COLUMN t_grassroots_project_position.content                         IS '详细说明（支持HTML）';


-- ============================================================
-- 2. 社区工作者岗位表 (t_community_position)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_community_position (
    id                          BIGINT          PRIMARY KEY,

    -- ==================== 招聘单位 ====================
    street_office               VARCHAR(200)    NOT NULL,
    community_name              VARCHAR(200),
    supervising_dept            VARCHAR(200),
    district                    VARCHAR(100),

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    position_type               VARCHAR(50)     NOT NULL,
    employment_type             VARCHAR(30)     NOT NULL,

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30)     NOT NULL,
    city                        VARCHAR(50)     NOT NULL,
    work_location               VARCHAR(200),

    -- ==================== 报考要求 ====================
    education_requirement       VARCHAR(30),
    age_limit                   INTEGER,
    recruitment_count           INTEGER         DEFAULT 1,
    major_requirement           VARCHAR(500),
    household_requirement       VARCHAR(100),
    political_status            VARCHAR(30),
    work_experience             VARCHAR(50),

    -- ==================== 社区岗位特殊要求 ====================
    social_work_cert            VARCHAR(50),
    community_experience        VARCHAR(100),
    residence_requirement       VARCHAR(200),

    -- ==================== 待遇信息 ====================
    salary_range                VARCHAR(50),
    salary_composition          VARCHAR(200),
    benefits                    TEXT,

    -- ==================== 考试信息 ====================
    exam_content                VARCHAR(500),
    interview_form              VARCHAR(100),
    reg_start_date              TIMESTAMPTZ,
    reg_end_date                TIMESTAMPTZ,
    exam_time                   TIMESTAMPTZ,

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',
    apply_link                  VARCHAR(500),
    apply_method                TEXT,
    contact_phone               VARCHAR(50),
    contact_address             VARCHAR(200),
    remark                      TEXT,
    content                     TEXT,

    -- ==================== 审计字段 ====================
    sort_order                   INTEGER         DEFAULT 0,
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
        position_status IS NULL OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

CREATE INDEX idx_com_pos_type    ON t_community_position (position_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_employ_type ON t_community_position (employment_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_location    ON t_community_position (province, city, district) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_city_type   ON t_community_position (province, city, position_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_education   ON t_community_position (education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_social_cert ON t_community_position (social_work_cert) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_pos_name    ON t_community_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_street      ON t_community_position USING btree (street_office varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_com_status      ON t_community_position (position_status) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_com_position_updated_at
    BEFORE UPDATE ON t_community_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON TABLE  t_community_position                          IS '社区工作者岗位表';
COMMENT ON COLUMN t_community_position.street_office            IS '街道办事处/乡镇';
COMMENT ON COLUMN t_community_position.community_name           IS '社区名称';
COMMENT ON COLUMN t_community_position.supervising_dept         IS '主管部门';
COMMENT ON COLUMN t_community_position.district                 IS '区/县';
COMMENT ON COLUMN t_community_position.position_name            IS '岗位名称';
COMMENT ON COLUMN t_community_position.position_type            IS '岗位类型';
COMMENT ON COLUMN t_community_position.employment_type          IS '用工形式（事业编/合同/购买服务/公益岗）';
COMMENT ON COLUMN t_community_position.province                 IS '省份';
COMMENT ON COLUMN t_community_position.city                     IS '城市';
COMMENT ON COLUMN t_community_position.work_location            IS '详细工作地点';
COMMENT ON COLUMN t_community_position.education_requirement    IS '学历要求';
COMMENT ON COLUMN t_community_position.age_limit                IS '年龄上限（周岁）';
COMMENT ON COLUMN t_community_position.recruitment_count        IS '招聘人数';
COMMENT ON COLUMN t_community_position.major_requirement        IS '专业要求';
COMMENT ON COLUMN t_community_position.household_requirement    IS '户籍要求';
COMMENT ON COLUMN t_community_position.political_status         IS '政治面貌';
COMMENT ON COLUMN t_community_position.work_experience          IS '工作经验';
COMMENT ON COLUMN t_community_position.social_work_cert         IS '社工证要求';
COMMENT ON COLUMN t_community_position.community_experience     IS '社区工作经验要求';
COMMENT ON COLUMN t_community_position.residence_requirement    IS '居住地要求';
COMMENT ON COLUMN t_community_position.salary_range             IS '薪资待遇';
COMMENT ON COLUMN t_community_position.salary_composition       IS '薪资构成说明';
COMMENT ON COLUMN t_community_position.benefits                 IS '福利待遇';
COMMENT ON COLUMN t_community_position.exam_content             IS '笔试内容';
COMMENT ON COLUMN t_community_position.interview_form           IS '面试形式';
COMMENT ON COLUMN t_community_position.reg_start_date           IS '报名开始';
COMMENT ON COLUMN t_community_position.reg_end_date             IS '报名截止';
COMMENT ON COLUMN t_community_position.exam_time                IS '考试时间';
COMMENT ON COLUMN t_community_position.position_status          IS '状态';
COMMENT ON COLUMN t_community_position.apply_link               IS '报名链接';
COMMENT ON COLUMN t_community_position.apply_method             IS '报名方式（在线/现场）';
COMMENT ON COLUMN t_community_position.contact_phone            IS '联系电话';
COMMENT ON COLUMN t_community_position.contact_address          IS '现场报名地址';
COMMENT ON COLUMN t_community_position.remark                   IS '备注';
COMMENT ON COLUMN t_community_position.content                  IS '详细说明（支持HTML）';


-- ============================================================
-- 3. 公益性岗位表 (t_public_welfare_position)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_public_welfare_position (
    id                          BIGINT          PRIMARY KEY,

    -- ==================== 开发单位 ====================
    developing_unit             VARCHAR(200)    NOT NULL,
    employing_unit              VARCHAR(200),

    -- ==================== 岗位信息 ====================
    position_name               VARCHAR(200)    NOT NULL,
    position_category           VARCHAR(50)     NOT NULL,
    work_content                TEXT,

    -- ==================== 地区信息 ====================
    province                    VARCHAR(30)     NOT NULL,
    city                        VARCHAR(50)     NOT NULL,
    district                    VARCHAR(50),
    work_location               VARCHAR(200),

    -- ==================== 面向人群 ====================
    target_group                TEXT[]          DEFAULT '{}',

    -- ==================== 报名要求 ====================
    education_requirement       VARCHAR(30)     DEFAULT '不限',
    age_range                   VARCHAR(50),
    health_requirement          VARCHAR(200),
    recruitment_count           INTEGER         DEFAULT 1,
    household_requirement       VARCHAR(100),
    employment_difficulty_cert  BOOLEAN         DEFAULT FALSE,
    other_requirement           TEXT,

    -- ==================== 岗位期限与待遇 ====================
    contract_period             VARCHAR(30)     NOT NULL,
    is_renewable                BOOLEAN         DEFAULT FALSE,
    max_service_years           INTEGER,
    monthly_salary              VARCHAR(50),
    salary_source               VARCHAR(100),
    subsidy_standard            VARCHAR(200),
    social_insurance_info       VARCHAR(200),
    other_benefits              TEXT,

    -- ==================== 工作时间 ====================
    work_schedule               VARCHAR(100),
    is_shift_work               BOOLEAN         DEFAULT FALSE,

    -- ==================== 报名信息 ====================
    reg_start_date              TIMESTAMPTZ,
    reg_end_date                TIMESTAMPTZ,
    apply_method                TEXT,
    apply_address               VARCHAR(200),
    required_documents          TEXT,

    -- ==================== 补充信息 ====================
    position_status             VARCHAR(20)     DEFAULT '招聘中',
    contact_phone               VARCHAR(50),
    contact_person              VARCHAR(50),
    remark                      TEXT,
    content                     TEXT,

    -- ==================== 审计字段 ====================
    sort_order                   INTEGER         DEFAULT 0,
    is_deleted                  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ==================== 约束 ====================
    CONSTRAINT chk_pw_category CHECK (
        position_category IN ('公共管理类', '公共服务类', '公共环境类', '公共安全类', '设施维护类', '其他')
    ),
    CONSTRAINT chk_pw_education CHECK (
        education_requirement IS NULL
        OR education_requirement IN ('不限', '初中', '高中', '大专', '本科')
    ),
    CONSTRAINT chk_pw_recruitment CHECK (
        recruitment_count IS NULL OR recruitment_count > 0
    ),
    CONSTRAINT chk_pw_max_service CHECK (
        max_service_years IS NULL OR max_service_years > 0
    ),
    CONSTRAINT chk_pw_status CHECK (
        position_status IS NULL OR position_status IN ('招聘中', '已结束', '即将开始')
    )
);

CREATE INDEX idx_pw_category       ON t_public_welfare_position (position_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_location       ON t_public_welfare_position (province, city, district) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_target_group   ON t_public_welfare_position USING gin (target_group) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_education      ON t_public_welfare_position (education_requirement) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_city_category  ON t_public_welfare_position (province, city, position_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_pos_name       ON t_public_welfare_position USING btree (position_name varchar_pattern_ops) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_status         ON t_public_welfare_position (position_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_pw_difficulty_cert ON t_public_welfare_position (employment_difficulty_cert) WHERE is_deleted = FALSE;

CREATE TRIGGER trg_pw_position_updated_at
    BEFORE UPDATE ON t_public_welfare_position
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

COMMENT ON TABLE  t_public_welfare_position                             IS '公益性岗位表';
COMMENT ON COLUMN t_public_welfare_position.developing_unit             IS '岗位开发单位';
COMMENT ON COLUMN t_public_welfare_position.employing_unit              IS '实际用工单位';
COMMENT ON COLUMN t_public_welfare_position.position_name               IS '岗位名称';
COMMENT ON COLUMN t_public_welfare_position.position_category           IS '岗位类别（公共管理/公共服务/公共环境/公共安全/设施维护/其他）';
COMMENT ON COLUMN t_public_welfare_position.work_content                IS '工作内容描述';
COMMENT ON COLUMN t_public_welfare_position.province                    IS '省份';
COMMENT ON COLUMN t_public_welfare_position.city                        IS '城市';
COMMENT ON COLUMN t_public_welfare_position.district                    IS '区/县';
COMMENT ON COLUMN t_public_welfare_position.work_location               IS '详细工作地点';
COMMENT ON COLUMN t_public_welfare_position.target_group                IS '面向人群列表';
COMMENT ON COLUMN t_public_welfare_position.education_requirement       IS '学历要求';
COMMENT ON COLUMN t_public_welfare_position.age_range                   IS '年龄范围';
COMMENT ON COLUMN t_public_welfare_position.health_requirement          IS '身体条件要求';
COMMENT ON COLUMN t_public_welfare_position.recruitment_count           IS '招聘人数';
COMMENT ON COLUMN t_public_welfare_position.household_requirement       IS '户籍要求';
COMMENT ON COLUMN t_public_welfare_position.employment_difficulty_cert  IS '是否需要就业困难认定证明';
COMMENT ON COLUMN t_public_welfare_position.contract_period             IS '合同期限';
COMMENT ON COLUMN t_public_welfare_position.is_renewable                IS '是否可续签';
COMMENT ON COLUMN t_public_welfare_position.max_service_years           IS '最长服务年限';
COMMENT ON COLUMN t_public_welfare_position.monthly_salary              IS '月工资标准';
COMMENT ON COLUMN t_public_welfare_position.salary_source               IS '工资来源';
COMMENT ON COLUMN t_public_welfare_position.subsidy_standard            IS '岗位补贴标准';
COMMENT ON COLUMN t_public_welfare_position.social_insurance_info       IS '社保缴纳说明';
COMMENT ON COLUMN t_public_welfare_position.work_schedule               IS '工作时间安排';
COMMENT ON COLUMN t_public_welfare_position.is_shift_work               IS '是否倒班';
COMMENT ON COLUMN t_public_welfare_position.reg_start_date              IS '报名开始';
COMMENT ON COLUMN t_public_welfare_position.reg_end_date                IS '报名截止';
COMMENT ON COLUMN t_public_welfare_position.apply_method                IS '报名方式（通常现场报名）';
COMMENT ON COLUMN t_public_welfare_position.apply_address               IS '报名地点';
COMMENT ON COLUMN t_public_welfare_position.required_documents          IS '报名需携带材料';
COMMENT ON COLUMN t_public_welfare_position.position_status             IS '状态';
COMMENT ON COLUMN t_public_welfare_position.contact_phone               IS '联系电话';
COMMENT ON COLUMN t_public_welfare_position.contact_person              IS '联系人';
COMMENT ON COLUMN t_public_welfare_position.remark                      IS '备注';
COMMENT ON COLUMN t_public_welfare_position.content                     IS '详细说明（支持HTML）';

COMMIT;