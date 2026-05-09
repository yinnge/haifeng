## **1.1 公告表 (t_announcements)**（新的公告会展示在网页）


| 字段名        | 类型           | 约束                        | 说明   |
| ---------- | ------------ | ------------------------- | ---- |
| id         | SERIAL       | PRIMARY KEY               | 公告ID |
| title      | VARCHAR(100) | NOT NULL                  | 公告标题 |
| content    | TEXT         | NOT NULL                  | 公告内容 |
| Tag        | VARCHAR(20)  |                           | 公告类型 |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 更新时间 |


## 一、规划师表（展示规划老师的信息）


```
-- ============================================
-- 规划师表 (t_planners)
-- 说明：高考志愿填报规划师信息
-- ============================================

CREATE TABLE t_planners (
    id               SERIAL       PRIMARY KEY                              ,-- 规划师ID
    name             VARCHAR(50)  NOT NULL                                 ,-- 姓名
    position         VARCHAR(50)                                          ,-- 职位（如：高级规划师/首席规划师）
    region           VARCHAR(20)                                         ,-- 所在地区（如：广东）
    avatar           VARCHAR(100)                                          ,-- 头像图片URL
    specialty        VARCHAR(100)                                          ,-- 专长领域（简短描述，列表页展示用）
    douyin_name      VARCHAR(100)                                          ,-- 抖音号名称（如：未来规划院）
    douyin_url       VARCHAR(100)                                          ,-- 抖音主页链接
    personal_descripitoon              TEXT                                                  ,-- 个人简介
    experience_job                     TEXT                                ,-- 从业经验（年）
    achievements     text[]       
                          ,-- 主要成就
    expertise_areas  text[]           
                      ,-- 擅长领域
    sort_order       INTEGER      DEFAULT 0                                ,-- 排序（越小越靠前）
    status           SMALLINT     DEFAULT 1        NOT NULL                ,-- 状态: 0-下架 1-展示
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP     NOT NULL   ,-- 创建时间
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP     NOT NULL    -- 更新时间
);

-- 表注释
COMMENT ON TABLE  planners                  IS '规划师表';
COMMENT ON COLUMN planners.id               IS '规划师ID';
COMMENT ON COLUMN planners.name             IS '姓名';
COMMENT ON COLUMN planners.position         IS '职位，如：高级规划师、首席规划师';
COMMENT ON COLUMN planners.region           IS '所在地区，如：广东广州';
COMMENT ON COLUMN planners.avatar           IS '头像图片URL';
COMMENT ON COLUMN planners.specialty        IS '专长领域，简短描述，用于列表页展示';
COMMENT ON COLUMN planners.douyin_name      IS '抖音号名称，如：未来规划院，前端显示为可点击链接';
COMMENT ON COLUMN planners.douyin_url       IS '抖音主页链接，点击跳转';
COMMENT ON COLUMN planners.bio             IS '个人简介';
COMMENT ON COLUMN planners.experience_years IS '从业经验，单位：年';
COMMENT ON COLUMN planners.achievements     IS '主要成就，JSON数组格式，如：["帮助500+学生录取985","省级优秀规划师"]';
COMMENT ON COLUMN planners.expertise_areas  IS '擅长领域，JSON数组格式，如：["新高考选科","艺术类志愿","强基计划"]';
COMMENT ON COLUMN planners.sort_order       IS '排序权重，数字越小越靠前';
COMMENT ON COLUMN planners.status           IS '状态: 0-下架不展示 1-正常展示';
COMMENT ON COLUMN planners.created_at       IS '创建时间';
COMMENT ON COLUMN planners.updated_at       IS '更新时间';
```


#### （展示培训机构信息，合作的可以展示）
```
-- ============================================
-- 培训机构表 (t_institutions)
-- 说明：合作培训机构信息
-- ============================================

CREATE TABLE t_institutions (
    id               SERIAL       PRIMARY KEY                              ,-- 机构ID
    name             VARCHAR(100) NOT NULL                                 ,-- 机构名称
    type             VARCHAR(100)  NOT NULL                                 ,-- 机构类型
    phone            VARCHAR(20)                                           ,-- 联系电话
    address          VARCHAR(100)                                          ,-- 机构地址
    description      TEXT                                                  ,-- 机构简介
    courses          TEXT[]                            
     ,-- 课程列表
    images           TEXT[]                           
    ,-- 机构图片URL列表
    logo             VARCHAR(200)                                          ,-- 机构Logo URL
    sort_order       INTEGER      DEFAULT 0                                ,-- 排序（越小越靠前）
    status           SMALLINT     DEFAULT 1        NOT NULL                ,-- 状态: 0-下架 1-展示
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP     NOT NULL   ,-- 创建时间
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP     NOT NULL    -- 更新时间
);

-- 索引
CREATE INDEX idx_institutions_type   ON institutions(type);
CREATE INDEX idx_institutions_status ON institutions(status);

-- 表注释
COMMENT ON TABLE  institutions                IS '培训机构表';
COMMENT ON COLUMN institutions.id             IS '机构ID';
COMMENT ON COLUMN institutions.name           IS '机构名称';
COMMENT ON COLUMN institutions.type           IS '机构类型: language-语言培训 / civil-公职考试 / it-IT培训 / art-艺术培训 / academic-学科辅导 / other-其他';
COMMENT ON COLUMN institutions.phone          IS '联系电话';
COMMENT ON COLUMN institutions.address        IS '机构地址';
COMMENT ON COLUMN institutions.description    IS '机构简介';
COMMENT ON COLUMN institutions.courses        IS '课程列表，JSON数组格式，如：["雅思冲刺班","托福强化班","日语N1考前班"]';
COMMENT ON COLUMN institutions.images         IS '机构图片URL列表，JSON数组格式，如：["/uploads/inst/1.jpg","/uploads/inst/2.jpg"]';
COMMENT ON COLUMN institutions.logo           IS '机构Logo图片URL';
COMMENT ON COLUMN institutions.sort_order     IS '排序权重，数字越小越靠前';
COMMENT ON COLUMN institutions.status         IS '状态: 0-下架不展示 1-正常展示';
COMMENT ON COLUMN institutions.created_at     IS '创建时间';
COMMENT ON COLUMN institutions.updated_at     IS '更新时间';
```