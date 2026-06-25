## 需要用到的表

### 志愿表-已实现了entity和mapper

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
    description             TEXT,
    admission_count         INTEGER,
    safety_level            NUMERIC(3,2),
    level_short             VARCHAR(10)     NOT NULL,
    history_scores          JSONB,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

## 需求
用户将志愿表和对应的快照表的信息打印成xlsx文件

#### 前提
1. 需要实现接口，在D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\WishPlanController.java中补充和续写，属于同一种模块

###### 核心诉求
1. 前端实现对专业组表的group_sort_order排序，也可以实现专业明细里面对应的group_snapshot_id排序。【专业明细排序只能是在一个对应的专业组中的排序】
2. 专业明细有一个is_exported字段，这个字段是boolean，默认值是true，那么这个专业明细就可以导出到excel里面去，如果这个字段是false，那么这个专业明细就不可以导出到excel里面去
3. 前端专业组旁边也有一个加号，点击取消代表着对应的专业明细也要全部取消is_exported字段。
4. 还有一个问题需要注意，就是如果一个专业组下面对应的专业明细的is_exported都是false，那么这个专业组也不要传到xlsx里面去【我怎么感觉专业组需要加一个is_exported字段字段呢？】
5. 需要将is_exported字段是true的都导出成xlsx，专业组也要，除非下面所有的专业明细的is_exported都是false。
6. 是交给浏览器导出
7. 我有easyExcel的包
8. 前端需要是实现一个针对于专业组导出xlsx的进度条，这个可以采用sse来是实现导出xlsx进度的展示

##### 待实现接口

1. 根据t_wish_group_snapshot表设置一个接口，修改对应的group_sort_order字段【不存redis，已经实现了专业组对group_sort_order的分页展示】
限制:限制用户需要登录
2. 根据t_wish_major_snapshot表设置一个接口，修改对应的major_sort_order字段【不存redis，已经实现了专业明细对group_sort_order的分页展示】
限制:限制用户需要登录
3. 根据t_wish_major_snapshot表设置一个接口，修改对应的is_exported字段
需要实现最后提交的时候才操作是数据库设置is_exported字段的值,可以用redis
限制:限制用户需要登录
4. 实现一个接口根据t_wish_group_snapshot表查询对应的t_wish_major_snapshot表，修改is_exported字段的值
【对应前端实现点击专业组旁边的按钮实现对专业明细的全选或者不选，也是用redis，最后统一导出xslx的时候再统一设置is_exported字段的值】
限制:限制用户需要登录
5. 返回一个导出xlsx的一个接口
- 只有专业明细是true的才可以导出，用sse和easyExcel来实现，实现前端实时看到专业组的进度【先统计专业组的id个数，然后返回已实现了几个专业组】
- 根据t_wish_group_snapshot和t_wish_major_snapshot的major_sort_order的顺序来导出成xlsx
限制:至少需要pro用户才可以导出xlsx，pro对应的注解意思是允许pro和vip都可以访问，所以一个pro注解就够了。

#### xlsx表结构
- 采用 EasyExcel 的 CellWriteHandler + 实体模型定义 的组合拳来实现。
1. xlsx表的第一行是：【plan_name】【plan_year】【plan_province】【plan_batch】【reform_model】 user_score/user_rank 导出xlsx的年月，时间
宋体， 16号，居中，背景色为绿色,跟下面的列对其，合并这一行并居中
举例：【我的志愿方案1】 【2020】 【广东】 【本科批】 【理科】 471分/78039名 2025-06-27 15:28:27
对应字段：t_wish_plan的plan_name, plan_year, plan_province, plan_batch, reform_model, user_score, user_rank
2. 第2行：【对应专业组快照表的】 组号【id】 大学信息【展示字段university_name+ city_name +category+nature+tags】 院校组代码【group_code】 院校组名称【对应具体的值：group_name + enrollment_code + subject】 描述【对应的值：description + constraints_description】 专业数量【major_count】 推免年份【recommendation_year】 推免率【recommendation_rate】 【下面是对应专业明细表的：】 序号【id】 专业名称【major_name + major_code + description】 学费/学制【duration+tuition】 年份【JSONB】 计划招生人数【JSONB】 最低分【JSONB】 最低位次【min_rank】 平均分【JSONB】 平均位次【JSONB】 最高分【JSONB】 最高位次【JSONB】
- 对应的表： t_wish_group_snapshot表 和 t_wish_major_snapshot表
- 对应的JSONB字段在：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\vo\algorithm\admission\YearScoreVO.java
宋体，13号，居中，背景色为绿色，每个单元格居中
3. 下面的数据：
- SONB只有5年的数据，也就是5行，JSONB的是5行。但是其他列是5行合并
- 宋体，12号，每个单元格居中，问题：可以根据内容来动态调整行宽和列的宽度大小吗？
- 有多个字段展示的，需要在居中的前提下换行就可以了【在同一个单元格内】

#### 说明
1. redis的配置在D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\config\RedisConfig.java
2. 身份注解在：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\annotation：