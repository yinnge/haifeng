#### 需要用到的表

##### 用户表-已实现了entity和mapper
D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\vo\member\MemberInfoVO.java
```
-- 6. 会员表
-- member_type: normal(普通版) -> pro(专业版) -> vip(旗舰版)
CREATE TABLE IF NOT EXISTS t_member (
id                          BIGSERIAL PRIMARY KEY,
username                    VARCHAR(50) NOT NULL UNIQUE,
password                    VARCHAR(100) NOT NULL,
avatar                      VARCHAR(500),
phone                       VARCHAR(20) UNIQUE NOT NULL,
invite_code                 VARCHAR(8) UNIQUE,
member_type                 VARCHAR(20) DEFAULT 'normal',
expire_at                   TIMESTAMPTZ,
referrer_id                 BIGINT,
referrer_username           VARCHAR(50),
commission_balance          DECIMAL(10,2) DEFAULT 0.00,
commission_total_earned     DECIMAL(10,2) DEFAULT 0.00,
commission_total_paid       DECIMAL(10,2) DEFAULT 0.00,
status                      VARCHAR(20) DEFAULT 'active',
last_login_at               TIMESTAMPTZ,
last_login_ip               VARCHAR(50),
wechat_id                   VARCHAR(255),                       -- 微信号(AES加密存储)
wechat_id_index             VARCHAR(64),                        -- 微信号盲索引(SHA-256)
is_deleted                  BOOLEAN NOT NULL DEFAULT FALSE,
created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
CONSTRAINT chk_member_type CHECK (member_type IN ('normal', 'pro', 'vip')),
CONSTRAINT chk_member_status CHECK (status IN ('active', 'disabled')),
CONSTRAINT chk_commission_balance CHECK (commission_balance >= 0)
);

CREATE INDEX idx_member_phone ON t_member(phone) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_type ON t_member(member_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_referrer ON t_member(referrer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_wechat_index ON t_member(wechat_id_index) WHERE is_deleted = FALSE;
```

##### 高考档案表-已实现了entity和mapper
D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\entity\algorithm\MemberGaokao.java
```
-- ============================================================
-- 用户高考档案表 (t_member_gaokao)
-- 描述：一个用户一条记录，存储高考的所有业务信息
--       系统推荐算法的核心输入数据
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_gaokao (
id                      BIGINT          PRIMARY KEY,
member_id               BIGINT          NOT NULL UNIQUE,

    -- 一、高考基本信息（必填）
    gaokao_year             SMALLINT,
    gaokao_province         VARCHAR(30),
    score                   INTEGER,
    rank                    INTEGER,

    -- 二、改革模式（系统根据省份+年份自动判断）
    reform_model            VARCHAR(20),

    -- 三、选科信息（必填，与分数字段一一对应）
    subject_type            VARCHAR(20),
    second_subject_type     VARCHAR(20),
    third_subject_type      VARCHAR(20),

    -- 四、各科成绩（可选）
    score_chinese           INTEGER,
    score_math              INTEGER,
    score_english           INTEGER,
    score_subject_1         INTEGER,
    score_subject_2         INTEGER,
    score_subject_3         INTEGER,

    -- 五、外语语种（可选）
    foreign_language        VARCHAR(20),

    -- 六、身体视觉条件（可选，全部允许 NULL）
    is_color_blind          BOOLEAN,
    is_color_weak           BOOLEAN,
    vision_left             NUMERIC(3,1),
    vision_right            NUMERIC(3,1),
    has_smell_disorder      BOOLEAN,

    -- 七、身体指标（可选）
    height_cm               INTEGER,
    weight_kg               NUMERIC(5,1),
    is_left_handed          BOOLEAN,
    has_tattoo              BOOLEAN,
    has_scar                BOOLEAN,
    has_stutter             BOOLEAN,

    -- 八、身份条件（可选）
    is_fresh_graduate       BOOLEAN,
    political_status        VARCHAR(20),
    household_type          VARCHAR(20),
    is_poverty_county       BOOLEAN,

    -- 九、批次与线差
    batch                   VARCHAR(50),
    batch_data_year         SMALLINT,
    batch_line_score        INTEGER,
    score_above_line        INTEGER,

    -- 审计字段
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```
##### 专业组相关
```
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
    constraints_description TEXT[]          DEFAULT '{}',
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

-- ===========================================================
-- 2. t_admission_major_score（专业录取明细表）
-- ===========================================================
CREATE TABLE IF NOT EXISTS t_admission_major_score (
    id                      SERIAL          PRIMARY KEY,
    group_id                INTEGER         NOT NULL REFERENCES t_admission_group(id) ON DELETE CASCADE,
    major_id                BIGINT,
    major_code              VARCHAR(20)     NOT NULL,
    major_name              VARCHAR(100)    NOT NULL,
    education_level         VARCHAR(20),
    duration                VARCHAR(20),
    tuition                 VARCHAR(50),
    description             TEXT,
    admission_count         INTEGER,
    min_score               INTEGER,
    min_rank                INTEGER,
    max_score               INTEGER,
    max_rank                INTEGER,
    avg_score               NUMERIC(6,2),
    avg_rank                INTEGER,
    constraints             TEXT[]          DEFAULT '{}',
    constraints_description TEXT[]          DEFAULT '{}',
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_group_major UNIQUE (group_id, major_code)
);

```
##### 大学表-已实现了entity和mapper

D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\entity\university\University.java
```
CREATE TABLE t_universities (
    id                  BIGINT        PRIMARY KEY,
    category            VARCHAR(50)   NOT NULL,
    major_count         INTEGER       DEFAULT 0,
    nature              VARCHAR(50),
    recommendation_rate DECIMAL(5,2),
    recommendation_year INTEGER,
    tags                TEXT[],
    ....
);
```

##### 志愿表-已实现了entity和mapper

D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\entity\algorithm\wish
```
-- ============================================================
-- 志愿方案模块 (t_wish_plan, t_wish_group_snapshot, t_wish_major_snapshot)
-- apps_V18 - 高考志愿填报方案快照表
-- ============================================================

BEGIN;

-- 1. 志愿方案主表
CREATE TABLE IF NOT EXISTS t_wish_plan (
id                      SERIAL          PRIMARY KEY,
member_id               BIGINT          NOT NULL,
plan_name               VARCHAR(100)    DEFAULT '我的志愿方案1' NOT NULL,
plan_year               SMALLINT        NOT NULL,
plan_province           VARCHAR(30)     NOT NULL,
reform_model            VARCHAR(20)     NOT NULL,
plan_batch              VARCHAR(50)     NOT NULL,
user_score              INTEGER         NOT NULL,
user_rank               INTEGER         NOT NULL,
bo_limit                INTEGER         DEFAULT 0 NOT NULL,
chong_limit             INTEGER         DEFAULT 0 NOT NULL,
wen_limit               INTEGER         DEFAULT 0 NOT NULL,
bao_limit               INTEGER         DEFAULT 0 NOT NULL,
die_limit               INTEGER         DEFAULT 0 NOT NULL,
is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wp_member ON t_wish_plan (member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_wp_member_year ON t_wish_plan (member_id, plan_year) WHERE is_deleted = FALSE;

-- 2. 志愿方案-专业组快照表
CREATE TABLE IF NOT EXISTS t_wish_group_snapshot (
id                      SERIAL          PRIMARY KEY,
plan_id                 INTEGER         NOT NULL,
group_id                INTEGER         NOT NULL,
group_sort_order        INTEGER         NOT NULL DEFAULT 0,
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
description             TEXT,
constraints_description TEXT[]          DEFAULT '{}',
category                VARCHAR(50)   NOT NULL,
major_count             INTEGER       DEFAULT 0,
nature                  VARCHAR(50),
recommendation_rate     DECIMAL(5,2),
recommendation_year     INTEGER,
tags                    TEXT[],
created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_twg_plan ON t_wish_group_snapshot (plan_id, group_sort_order);

-- 3. 志愿方案-专业明细快照表
CREATE TABLE IF NOT EXISTS t_wish_major_snapshot (
id                      SERIAL          PRIMARY KEY,
plan_id                 INTEGER         NOT NULL,
group_snapshot_id       INTEGER         NOT NULL,
major_id                BIGINT,
major_sort_order        INTEGER         NOT NULL DEFAULT 0,
is_exported             BOOLEAN         NOT NULL DEFAULT TRUE,
major_code              VARCHAR(30)     NOT NULL,
major_name              TEXT            NOT NULL,
duration                VARCHAR(20),
tuition                 NUMERIC(10,2),
admission_count         INTEGER,
safety_level            NUMERIC(3,2),
level_short             VARCHAR(10)     NOT NULL,
history_scores          JSONB,
created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_twm_group ON t_wish_major_snapshot (group_snapshot_id, major_sort_order);
CREATE INDEX idx_twm_plan ON t_wish_major_snapshot (plan_id) WHERE is_exported = TRUE;

COMMIT;
```



## 需求一

### 任务一

1. 我们实现了专业组和专业明细的分页+安全系数的自动计算展示，在专业明细的左边我们有一个添加框按钮，可以将多个专业明细添加到志愿表中。
D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\admission\AdmissionQueryController.java
      @GetMapping("/group/page")
      public R<IPage<AdmissionGroupPageVO>> pageGroups(@Valid AdmissionGroupQueryDTO dto) {
      return R.ok(admissionQueryService.pageGroups(dto));
      }

   /**
    * 分页查询专业明细
      */
      @GetMapping("/major/page")
      public R<IPage<AdmissionMajorPageVO>> pageMajors(@Valid AdmissionMajorQueryDTO dto) {
      return R.ok(admissionQueryService.pageMajors(dto));
      }

2. 根据已有接口的数据：
D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\WishPlanController.java
      @GetMapping("/default-limits")
      public R<WishPlanLimitVO> getDefaultLimits() {
      return R.ok(wishPlanService.getDefaultLimits());
      }
限制添加的专业明细的个数，专业明细的接口会返回levelShort和safetyLevel【等级简称：搏/冲/稳/保/垫/禁(禁直接不允许选择和添加到支援组)】，根据选择的专业明细的safetyLevel，跟getDefaultLimits返回的【（搏/冲/稳/保/垫）的个数】对应的个数做个比较，大致意思就是用户选择专业明细的个数，要根据对应的level判断，只能选limit返回的数值的专业明细的个数，否则提示具体的报错信息，ok？

#### 问题

1. 这个专业明细分页返回的level是禁的话是不允许进入志愿表的，那这个判断是前端还是后端做的事情？

## 需求二
当用户点击查看选中的志愿表的时候，跳转，会显示选中的专业明细和对应的专业组的分页，

### 限制
普通用户只能有一个志愿表
pro用户只能有五个志愿表
vip用户只能有十个志愿表
用户可以删除现有的志愿表，那么将对应的快照表也删除
【提示：志愿表后续可以在个人中心跳转到对应的分页】

### 任务一 先实现志愿方案主表
生成一个接口：查看用户相应的志愿表
1. member_id对应t_member用户表的id
2. plan_name的话需要写一个算法，生成名称：`我的志愿方案{id}`
3. plan_year到user_rank对应t_member_gaokao的-- 一、高考基本信息（必填），reform_model对应：二、改革模式（系统根据省份+年份自动判断）,plan_batch对应batch
4. bo_limit到die_limit的值，根据用户选择的专业明细的安全系数的level会动态计算【不存redis，会经常变】

#### 限制
1. 必须登录也就是至少是普通用户才可以看和访问

### 任务二 实现志愿-专业组快照表
生成一个接口：分页查看用户志愿表中选中的专业明细对应外键的志愿组的信息【应该要先存数据后分页】
1. plan_id对应t_wish_plan的id
2. group_id对应选中的专业明细对应t_admission_major_score的外键group_id【要删除重复的】
3. university_id,university_name到constraints_description都有对应的
4. bo_limit到die_limit的值，根据用户选择的专业明细的安全系数的level会动态计算【不存redis，会经常变】
5. 针对group_sort_order后续再说，默认值就行
6. 对于category到tags，需要联表专业组的外键university的id，去找到对应的t_universities里面有对应的字段

#### 限制
1. 必须登录也就是至少是普通用户才可以看和访问

### 任务三 实现志愿方案-专业明细快照表
生成一个接口：分页查看用户志愿表中选中的专业明细【应该要先存储专业明细后分页】
1. plan_id对应t_wish_plan的id
2. group_snapshot_id对应t_wish_group_snapshot的id
3. major_id到history_scores都有数据
4. is_exported和major_sort_order先不管

#### 说明
history_scores是JSONB，在原始分页里面会展示近五年的数据
格式是：year，admission_count，min_score，min_rank，max_score，max_rank，avg_score，avg_rank
这个呢，我记得接口返回的也是JSONB，那这样的话直接拿JSONB的就可以

#### 限制
1. 必须登录也就是至少是普通用户才可以看和访问

### 补充任务【前提】
1. 实现对limit接口的个数的限制，可以少或等于，不能多
2. 看一下这个VO返回值为禁的level字段实现添加到志愿表不允许的操作，是后端实现还是前端

### 最新任务
实现一个接口，将用户选中的志愿表导出成excel文件
#### 前提
1. 需要实现四个接口，在D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\WishPlanController.java
###### 核心诉求
1. 前端实现对专业组表的group_sort_order排序，也可以实现专业明细里面对应的group_snapshot_id排序。【专业明细排序只能是在一个对应的专业组中的排序】
2. 专业明细有一个is_exported字段，这个字段是boolean，默认值是true，那么这个专业明细就可以导出到excel里面去，如果这个字段是false，那么这个专业明细就不可以导出到excel里面去
3. 前端专业组旁边也有一个加号，点击取消代表着对应的专业明细也要全部取消is_exported字段。
4. 还有一个问题需要注意，就是如果一个专业组下面对应的专业明细的is_exported都是false，那么这个专业组也不要传到xlsx里面去【我怎么感觉专业组需要加一个is_exported字段字段呢？】
5. 需要将is_exported字段是true的都导出成xlsx，专业组也要，除非下面所有的专业明细的is_exported都是false。
6. 是交给浏览器导出
7. 我有easyExcel的包

##### 四个接口
1. 根据t_wish_group_snapshot表设置一个接口，修改对应的group_sort_order字段
###### 限制
限制用户需要登录
2. 根据t_wish_major_snapshot表设置一个接口，修改对应的group_snapshot_id字段
###### 限制
限制用户需要登录
3. 根据t_wish_major_snapshot表设置一个接口，修改对应的is_exported字段
###### 限制
限制用户需要登录
4. 返回一个导出xlsx的一个接口
###### 限制
至少需要pro用户才可以导出xlsx，pro对应的注解意思是允许pro和vip都可以访问，所以一个注解就够了。

#### xlsx表结构
1. 采用 EasyExcel 的 CellWriteHandler + 实体模型定义 的组合拳来实现。

### 注意事项
1. 所有实现的接口都续写在一个模块下【service等也一样续写】：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\WishPlanController.java
2. 所有的身份都已实现注解，在：D:\0code\haifeng\backend\haifeng\haifeng-common\src\main\java\com\haifeng\common\annotation
3. 不要使用git命令提交，最后修改的统一提交，遇到冲突的需要给我判决，一般都不会遇到。