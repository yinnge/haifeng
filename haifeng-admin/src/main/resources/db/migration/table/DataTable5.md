```
-- ============================================
-- 院校表 (universities)
-- 说明：全站院校核心表，只存最基本的信息
--       其他详细数据通过 university_id 关联其他表
-- ============================================

CREATE TABLE universities (
    id                SERIAL        PRIMARY KEY                            ,-- 院校ID
    name              VARCHAR(50)  NOT NULL                               ,-- 院校名称
    name_en           VARCHAR(50)  NOT NULL                               ,-- 院校名称英文
    province_name     VARCHAR(50)  NOT NULL,
    
    city_name         VARCHAR(50)   NOT NULL,
    
    region            VARCHAR(50)   NOT NULL                               ,-- 所属地区
    category          VARCHAR(50)   NOT NULL                               ,-- 院校类别（综合/理工/师范...）
    major_count       INTEGER       DEFAULT 0                              ,-- 专业数量
    education_level   VARCHAR(50)                                          ,-- 办学层次（本科/专科/本专兼招）
    nature            VARCHAR(50)                                          ,-- 院校性质（公办/民办/中外合作）
    recommendation_rate DECIMAL(5,2)
               ,--'推免率（百分比）'
    recommendation_year INT
               ,--'推免年份'
                  
    has_doctorate     BOOLEAN       DEFAULT false                          ,-- 是否有博士点
    has_doctorate     BOOLEAN       DEFAULT false                          ,-- 是否有硕士点
    department        VARCHAR(100)                                         ,-- 隶属部门
    tags              TEXT[]                                               ,-- 院校层次（多选）（985，211，双一流，一流大学A，一流大学B，一流学科，强基计划，C9联盟，E9联盟，G7联盟，Z14联盟，101计划，卓越工程师，卓越医生，卓越法律，卓越农林，保研资格，港澳院校，军校）
    famous_union       VARCHAR(50)                                         ,-- 知名联盟  
    image_url         VARCHAR(500)                                         ,-- 院校图片URL
    introduction      TEXT                                                 ,-- 院校简介
    
    -- 通用字段
    sort_order        INTEGER       DEFAULT 0                              ,-- 排序
    status            SMALLINT      DEFAULT 1          NOT NULL            ,-- 状态: 0-下架 1-展示
    created_at        TIMESTAMPZ    DEFAULT CURRENT_TIMESTAMP   NOT NULL   ,-- 创建时间
    updated_at        TIMESTAMPZ    DEFAULT CURRENT_TIMESTAMP   NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_univ_name     ON universities(name);
CREATE INDEX idx_univ_region   ON universities(region);
CREATE INDEX idx_univ_category ON universities(category);
CREATE INDEX idx_univ_status   ON universities(status);
CREATE INDEX IF NOT EXISTS idx_univ_city
    ON universities (city_name)
    WHERE status = 1;
-- 隶属部门（多选）
CREATE INDEX IF NOT EXISTS idx_univ_department
    ON universities (department)
    WHERE status = 1;


CREATE INDEX idx_univ_tags     ON universities USING GIN(tags);

-- 注释
COMMENT ON TABLE  universities                    IS '院校主表，存储最基本的院校信息';
COMMENT ON COLUMN universities.id                 IS '院校ID，全站唯一标识，其他表通过此ID关联';
COMMENT ON COLUMN universities.name               IS '院校名称，如：清华大学';
COMMENT ON COLUMN universities.region             IS '所属地区，如：北京';
COMMENT ON COLUMN universities.category           IS '院校类别: comprehensive-综合 science-理工 normal-师范 art-艺术 finance-财经 political-政法 language-语言 agriculture-农林 medical-医药 sports-体育 nationality-民族 military-军事';
COMMENT ON COLUMN universities.major_count        IS '专业数量';
COMMENT ON COLUMN universities.education_level    IS '办学层次: undergraduate-本科 specialist-专科 both-本专兼招';
COMMENT ON COLUMN universities.nature             IS '院校性质: public-公办 private-民办 cooperative-中外合作';
COMMENT ON COLUMN universities.has_doctorate      IS '是否有博士点';
COMMENT ON COLUMN universities.department         IS '隶属部门，如：教育部、省教育厅';
COMMENT ON COLUMN universities.tags               IS '院校标签，多选，如：["985","211","一流大学","一流学科"]';
COMMENT ON COLUMN universities.image_url          IS '院校图片URL';
COMMENT ON COLUMN universities.introduction       IS '院校简介';
COMMENT ON COLUMN universities.sort_order         IS '排序权重';
COMMENT ON COLUMN universities.status             IS '状态: 0-下架 1-展示';
```

```

-- ============================================
-- 院校详情表 (universities_detail)
-- ============================================

CREATE TABLE universities_detail (
    id                    SERIAL        PRIMARY KEY                           ,-- 院校ID
    university_id         INTEGER       NOT NULL UNIQUE                       ,-- 关联院校ID（一对一）
    -- ========== 基本信息 ==========
    address               VARCHAR(200)                                        ,-- 学校地址
    
    -- ========== 联系信息 ==========
    admission_phone       VARCHAR(50)                                         ,-- 招生电话
    website               VARCHAR(500)                                        ,-- 官方网站
    
    -- ========== 院校标签 ==========
    history_group_score           Integer                   ,-- 本科批历史组
    science_group_score           Integer                   ,-- 本科批物理组
    
    -- ========== 轮播图片 ==========
    carousel_images       TEXT[]                   ,-- 轮播图片URL列表
    
    -- ========== 院校简介 ==========
    introduction          TEXT                                                ,-- 院校简介
    
    -- ========== 排名 ==========
    rankings              JSONB         DEFAULT '{}'::JSONB                   ,-- 排名信息(软科排名,校友会排名,武书连排名,QS排名,U.S.NEWS排名(都是Integer))
    
    -- ========== 数据统计 ==========
    
    abroad_rate      VARCHAR(10)                   ,--出国比例
    gender_ratio     VARCHAR(10)                   ,--男女比例
    
     
    -- ========== 通用字段 ==========
    sort_order            INTEGER       DEFAULT 0                             ,-- 排序
    status                SMALLINT      DEFAULT 1             NOT NULL        ,-- 状态: 0-下架 1-展示
    created_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL   ,-- 创建时间
    updated_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_univ_name            ON universities(name);
CREATE INDEX idx_univ_region          ON universities(region);
CREATE INDEX idx_univ_type            ON universities(university_type);
CREATE INDEX idx_univ_status          ON universities(status);
CREATE INDEX idx_univ_official_tags   ON universities USING GIN(official_tags);

-- ================================
-- 注释
-- ================================
COMMENT ON TABLE  universities                          IS '院校详情表，全站院校主表';
COMMENT ON COLUMN universities.id                       IS '院校ID';
COMMENT ON COLUMN universities.name                     IS '院校名称，如：清华大学';
COMMENT ON COLUMN universities.name_en                  IS '英文名称，如：Tsinghua University';
COMMENT ON COLUMN universities.region                   IS '所属地区，如：北京';
COMMENT ON COLUMN universities.university_type          IS '院校类型: comprehensive-综合类 science-理工类 normal-师范类 art-艺术类 等';
COMMENT ON COLUMN universities.department               IS '隶属部门，如：教育部';
COMMENT ON COLUMN universities.major_count              IS '专业数量';
COMMENT ON COLUMN universities.address                  IS '学校地址，如：北京市海淀区清华园1号';
COMMENT ON COLUMN universities.admission_phone          IS '招生电话，如：010-62770334';
COMMENT ON COLUMN universities.website                  IS '官方网站URL';
COMMENT ON COLUMN universities.official_tags            IS '官方标签，如：["985工程","211工程","双一流"]';
COMMENT ON COLUMN universities.custom_tags              IS '自定义标签，如：["顶尖学府","人文社科强校","QS排名前列"]';
COMMENT ON COLUMN universities.carousel_images          IS '轮播图片URL数组，如：["/uploads/u/1.jpg","/uploads/u/2.jpg"]';
COMMENT ON COLUMN universities.introduction             IS '院校简介，纯文本';
COMMENT ON COLUMN universities.score_lines              IS '分数线信息，含各批次各科目分数';
COMMENT ON COLUMN universities.rankings                 IS '排名信息，含软科/校友会/武书连/QS/USNEWS';
COMMENT ON COLUMN universities.statistics               IS '数据统计，含保研比例/出国比例/男女比例等';
COMMENT ON COLUMN universities.subject_evaluation       IS '学科评估，含各等级学科数量';
COMMENT ON COLUMN universities.first_level_subjects     IS '一级学科列表，字符串数组';
COMMENT ON COLUMN universities.featured_majors          IS '特色专业列表，字符串数组';
COMMENT ON COLUMN universities.graduate_programs        IS '研究生专业，含学术硕士和专业硕士列表';
COMMENT ON COLUMN universities.research_platforms       IS '科研平台，含国家重点实验室和工程研究中心';
COMMENT ON COLUMN universities.departments              IS '院系设置，按理工科/人文社科分类';
```

### rankings JSONB -- 排名信息

|字段名|类型|示例|说明|
|---|---|---|---|
|软科排名|INTEGER|15|软科中国大学排名|
|校友会排名|INTEGER|18|校友会中国大学排名|
|武书连排名|INTEGER|20|武书连中国大学排名|
|QS排名|INTEGER|250|QS世界大学排名|
|U.S.NEWS排名|INTEGER|280|U.S.NEWS世界大学排名|




# 校园图册表（t_campus_gallery）

```
CREATE TABLE IF NOT EXISTS t_campus_gallery (

    id                  SERIAL          PRIMARY KEY,
    university_id       INTEGER         NOT NULL,           -- 大学ID（外键）
    university_name     VARCHAR(30)     NOT NULL,           -- 冗余展示
    image_type          VARCHAR(30)     NOT NULL,           -- 图片类型
    image_url           VARCHAR(500)    NOT NULL,           -- 图片URL
    
    sort_order          INTEGER         DEFAULT 0,          -- 排序权重（越小越靠前）
    
    created_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 创建时间
    updated_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 更新时间
    
);

-- 索引
CREATE INDEX idx_gallery_university 
    ON t_campus_gallery (university_id, image_type, sort_order);

CREATE INDEX idx_gallery_type 
    ON t_campus_gallery (image_type);

-- 注释
COMMENT ON TABLE  t_campus_gallery              IS '校园图册表';
COMMENT ON COLUMN t_campus_gallery.university_id IS '大学ID（外键）';
COMMENT ON COLUMN t_campus_gallery.image_type    IS '图片类型（教学楼/宿舍/食堂等）';
COMMENT ON COLUMN t_campus_gallery.image_url     IS '图片URL';
COMMENT ON COLUMN t_campus_gallery.sort_order    IS '排序权重';

COMMIT;
```




```
-- ============================================
-- 院校适应指南表 (university_guides)
-- 说明：新生入学适应指南，每个院校一条记录
--       通过 university_id 关联院校主表
--       不重复存储院校名称/地区等基本信息
-- ============================================

CREATE TABLE university_guides (
    id                    SERIAL        PRIMARY KEY                           ,-- 指南ID
    university_id         INTEGER       NOT NULL UNIQUE                       ,-- 关联院校ID（一对一）
    custom_tags           TEXT[],                --自定义标签
      
    -- ========== 校园设施与生活环境 ==========
    campus_facilities     JSONB         DEFAULT '{}'::JSONB                   ,-- 校园设施（教学楼分布，实验楼与图书馆，宿舍区与食堂，生活配套设施（text[]））
    dormitory_services     JSONB         DEFAULT '{}'::JSONB                   ,-- 水电网与宿舍管理(水电费缴纳,宿舍规章制度(text[]))
    campus_transportation     JSONB         DEFAULT '{}'::JSONB                   ,-- 校园通勤与校外交通(校内通勤,校外出行(text[]))
    
    -- ========== 学业指导 ==========
    academic_guidance     JSONB         DEFAULT '{}'::JSONB                   ,--      专业与课程核心信息(专业培养方案,选课系统(text[]))
    major_transfer_guidelines     JSONB         DEFAULT '{}'::JSONB                   ,-- 转专业原则(基本申请条件,申请时间与流程(text[]))
    
    major_transfer_constriction   JSONB         DEFAULT '{}'::JSONB                   ,-- 转专业限制(限制类型(varchar(20)),具体说明(varhcar(50)))
    
    academic_support_resources     JSONB         DEFAULT '{}'::JSONB                   ,-- 学习支持资源(师资力量,学习场所,学业帮扶(text[]))

    -- ========== 社交融入 ==========
    student_organizations    JSONB         DEFAULT '{}'::JSONB                   ,-- 学生组织与社团(官方组织(text[]),社团类型(text[]))
    campus_events    JSONB         DEFAULT '{}'::JSONB                   ,--  校园活动与竞赛(院校品牌活动(text[]),学科与技能竞赛(text[]))
    class_dorm_social    JSONB         DEFAULT '{}'::JSONB                   ,-- 班级与宿舍社交(班级管理(text[]),宿舍相处(text[]))

    -- ========== 权益与安全 ==========
    financial_aid         JSONB         DEFAULT '{}'::JSONB                   ,-- 奖助勤贷与权益保障(奖助学金(text[]),勤工俭学(text[]),权益申诉(text[]))
    campus_security         JSONB         DEFAULT '{}'::JSONB                   ,-- 校园安全与应急处理(安全设施(text[]),安全规则(text[]))
    health_services         JSONB         DEFAULT '{}'::JSONB                   ,-- 医保与心理健康(医保报销(text[]),心理健康(text[]))

    -- ========== 生活服务 ==========
    life_services         JSONB         DEFAULT '{}'::JSONB                   ,-- 生活服务(生活服务(text[]),医疗资源(text[]),兼职与实习(text[]))

    
    -- ========== 通用字段 ==========
    remark                TEXT                                                ,-- 备注
    status                SMALLINT      DEFAULT 1             NOT NULL        ,-- 状态: 0-下架 1-展示
    created_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL   ,-- 创建时间
    updated_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP  NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_guides_university_id ON university_guides(university_id);
CREATE INDEX idx_guides_status        ON university_guides(status);

-- ================================
-- 注释
-- ================================
COMMENT ON TABLE  university_guides                         IS '院校适应指南表，每个院校一条，通过university_id关联院校主表';
COMMENT ON COLUMN university_guides.id                      IS '指南ID';
COMMENT ON COLUMN university_guides.university_id           IS '关联院校ID，UNIQUE约束保证一对一';
COMMENT ON COLUMN university_guides.recommendation          IS '推荐信息：适合学生类型/分数线要求/推荐专业/就业前景/院校优势/详细位置/学科类型';
COMMENT ON COLUMN university_guides.campus_facilities       IS '校园设施：教学楼/实验楼与图书馆/宿舍与食堂/生活配套/水电网/宿舍规章/校内通勤/校外出行，均为HTML';
COMMENT ON COLUMN university_guides.academic_guidance       IS '学业指导：培养方案/选课系统/转专业/师资力量/学习场所/学业帮扶，均为HTML';
COMMENT ON COLUMN university_guides.social_integration      IS '社交融入：官方组织/社团类型/品牌活动/学科竞赛/班级管理/宿舍相处，均为HTML';
COMMENT ON COLUMN university_guides.rights_safety           IS '权益安全：奖助学金/勤工俭学/权益申诉/安全设施/安全规则/医保报销/心理健康，均为HTML';
COMMENT ON COLUMN university_guides.life_services           IS '生活服务：生活服务/医疗资源/兼职实习/周边生活建议，均为HTML';
COMMENT ON COLUMN university_guides.tips                    IS '新生提示：选课技巧/宿舍关系建议/诈骗警告，均为HTML';
COMMENT ON COLUMN university_guides.remark                  IS '备注，其他补充信息';
COMMENT ON COLUMN university_guides.status                  IS '状态: 0-下架 1-展示';
```

### campus_facilities JSONB -- 校园设施

|字段名|类型|示例|说明|
|---|---|---|---|
|教学楼分布|TEXT[]|["主教学区位于中心区域","共有10栋教学楼"]|教学楼分布情况|
|实验楼与图书馆|TEXT[]|["图书馆藏书300万册","24小时自习室"]|实验楼与图书馆信息|
|宿舍区与食堂|TEXT[]|["4个宿舍区","8个食堂"]|宿舍区与食堂分布|
|生活配套设施|TEXT[]|["校医院","超市","快递站"]|生活配套设施|

---

### dormitory_services JSONB -- 水电网与宿舍管理

|字段名|类型|示例|说明|
|---|---|---|---|
|水电费缴纳|TEXT[]|["通过校园卡缴纳","每月免费额度30度电"]|水电费缴纳方式|
|宿舍规章制度|TEXT[]|["晚11点熄灯","禁止使用违规电器"]|宿舍管理规定|

---

### campus_transportation JSONB -- 校园通勤与校外交通

|字段名|类型|示例|说明|
|---|---|---|---|
|校内通勤|TEXT[]|["校园巴士5条线路","共享单车服务"]|校内通勤方式|
|校外出行|TEXT[]|["地铁2号线直达","多条公交线路"]|校外交通情况|

---

### academic_guidance JSONB -- 专业与课程核心信息

|字段名|类型|示例|说明|
|---|---|---|---|
|专业培养方案|TEXT[]|["培养方案可在教务系统查询","必修课128学分"]|专业培养方案说明|
|选课系统|TEXT[]|["每学期第15周开放选课","采用志愿优先原则"]|选课系统使用说明|

---

### Major_Transfer_Guidelines JSONB -- 转专业原则

|字段名|类型|示例|说明|
|---|---|---|---|
|基本申请条件|TEXT[]|["大一下学期可申请","GPA要求3.0以上"]|转专业基本条件|
|申请时间与流程|TEXT[]|["每年3月提交申请","通过考核后转入"]|申请时间与流程|

---

### Major_Transfer_constriction JSONB -- 转专业限制

|字段名|类型|示例|说明|
|---|---|---|---|
|限制类型|VARCHAR(20)|"艺术类专业"|限制类型|
|具体说明|VARCHAR(50)|"艺术类专业不接受转入"|具体限制说明|

---

### academic_support_resources JSONB -- 学习支持资源

|字段名|类型|示例|说明|
|---|---|---|---|
|师资力量|TEXT[]|["博士学位教师占比90%","国家级教学名师5人"]|师资力量介绍|
|学习场所|TEXT[]|["图书馆","自习室","实验室开放时间"]|学习场所信息|
|学业帮扶|TEXT[]|["学业导师制度","免费辅导课程"]|学业帮扶措施|

---

### student_organizations JSONB -- 学生组织与社团

|字段名|类型|示例|说明|
|---|---|---|---|
|官方组织|TEXT[]|["学生会","团委","社团联合会"]|官方学生组织|
|社团类型|TEXT[]|["学术科技类","文化艺术类","体育竞技类"]|社团分类|

---

### campus_events JSONB -- 校园活动与竞赛

|字段名|类型|示例|说明|
|---|---|---|---|
|院校品牌活动|TEXT[]|["校园文化节","迎新晚会"]|学校品牌活动|
|学科与技能竞赛|TEXT[]|["ACM程序设计竞赛","数学建模竞赛"]|学科竞赛活动|

---

### class_dorm_social JSONB -- 班级与宿舍社交

|字段名|类型|示例|说明|
|---|---|---|---|
|班级管理|TEXT[]|["班委选举制度","班级活动经费支持"]|班级管理方式|
|宿舍相处|TEXT[]|["宿舍公约","定期宿舍活动"]|宿舍社交建议|

---

### financial_aid JSONB -- 奖助勤贷与权益保障

|字段名|类型|示例|说明|
|---|---|---|---|
|奖助学金|TEXT[]|["国家奖学金8000元/年","校级奖学金覆盖率30%"]|奖助学金政策|
|勤工俭学|TEXT[]|["图书馆助理","实验室助管"]|勤工俭学岗位|
|权益申诉|TEXT[]|["学生事务中心","权益热线"]|权益申诉渠道|

---

### campus_security JSONB -- 校园安全与应急处理

|字段名|类型|示例|说明|
|---|---|---|---|
|安全设施|TEXT[]|["24小时监控","校园110"]|安全设施介绍|
|安全规则|TEXT[]|["外来人员登记制度","夜间结伴出行提示"]|安全规则说明|

---

### health_services JSONB -- 医保与心理健康

|字段名|类型|示例|说明|
|---|---|---|---|
|医保报销|TEXT[]|["大学生医保报销比例70%","校医院就诊流程"]|医保报销政策|
|心理健康|TEXT[]|["心理咨询中心","免费心理辅导"]|心理健康服务|

---

### life_services JSONB -- 生活服务

|字段名|类型|示例|说明|
|---|---|---|---|
|生活服务|TEXT[]|["快递服务站","理发店","洗衣房"]|校园生活服务|
|医疗资源|TEXT[]|["校医院","周边三甲医院"]|医疗资源介绍|
|兼职与实习|TEXT[]|["就业指导中心","校企合作实习基地"]|兼职实习资源|

---

