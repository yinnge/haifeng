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

## 总需求

用户将志愿表等信息导出成PDF，接入大模型分析数据，来引导用户该选择什么专业

### 本次需求

注册接口成对应的工具类，设计多智能体编排，先调用大模型返回结果，之后再将md转成pdf的事情

#### 前提

1. 需要实现的项目放在在：在haifeng-app\src\main\java\com\haifeng\app\controller\algorithm下创建一个PDF的文件夹，放PdfPlanController.java【都有一个PDF文件夹，本次实现都放在下面】
2. 只有身份为vip的才可以调用这个接口【已实现注解】
3. 已经实现了志愿表查询的两个分页接口：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\WishPlanController.java

###### 已经实现
1. 配置deepseek的ai的基础和配置类（openai协议）
2. 每个用户登录每天调用api的次数要根据system_settings表的api_number字段绑定，每天每个用户的最多调用次数【可以将字段存入redis】
3. 需要设置ai的算法，我们这里需要配置多个api-key，需要如果一个挂了换另一个
4. 写一个算法，根据用户名绑定api-key【为了命中缓存，你比如说根据用户id绑定【自己在看还收什么绑定同一个api的算法，比如哈希算法等】，如果失败就绑定别的】
5. 现在先在controller什么的，就写一个简单的流式返回即可，提示词空下

##### 本次任务
1. 设计多智能体编排架构【需要springai框架】
3. 为每个智能体写提示词和功能约束
4. 多智能体协作导出pdf【Thymeleaf + CSS 强行分页(前面是宏观分析，后面是汇总表，最后是“一个专业明细是一页”)】【后续实现】
5. 利用 OpenHTMLtoPDF 直接接收处理好的 HTML，一键转为 PDF 字节流，完美支持 A4 尺寸及 CSS 分页逻辑 。【后续实现】

##### 任务一【实现一个接口】
pdf只允许导出一个专业组和对应的专业明细和对应的多个专业，大学和城市信息
1. 前端会返回一个快照专业组的id
2. 你需要将查询t_wish_group_snapshot【WishPlanService.getExportGroupSnapshot(Integer groupSnapshotId) 】
3. 调用之前写好的service，
（1）package com.haifeng.app.service.impl.university;
- public UniversityDetailVO detail(Long universityId) {
（2）package com.haifeng.app.service.impl.city;CityServiceImpl【CityService.detailByName(String cityName)
  】
- public CityDetailVO detail(Long cityId) {
4. 前端会返回一个id你需要查询t_wish_major_snapshot的group_snapshot_id字段【 WishPlanService.getExportableMajorIds(Integer groupSnapshotId)】，查出关联的多个major_id
将每一个major的id都调用
（3）package com.haifeng.app.service.impl.major;的MajorServiceImpl
- public MajorDetailVO detail(Long majorId)

##### 任务二【接口调用service】


1. 前端会传1个专业组ID，调用getExportGroupSnapshot返回university_id + city_name
2. 拿到数据分开调用university和city的service，返回dto数据
3. 这些数据是生成PDF的第2页


#### 注意点
1. ai要求sse流式输出【以实现】，前端左边返回流式的数据，右边返回已生成PDF，请查看【后续实现导出pdf】
2. 注册工具类是service不是接口，让jvm去做处理


#### 说明
1. redis的配置在D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\config\RedisConfig.java
2. 身份注解在：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\annotation：
3. api-key有很多个，后续我会创建api数据库，目前先不用动，或者用.env的
4. deepseek请求体
```
{
  "messages": [
    {
      "content": "You are a helpful assistant",
      "role": "system"
    },
    {
      "content": "Hi",
      "role": "user"
    }
  ],
  "model": "deepseek-v4-pro",
  "thinking": {
    "type": "enabled"
  },
  "reasoning_effort": "high",
  "max_tokens": 4096,
  "response_format": {
    "type": "text"
  },
  "stop": null,
  "stream": false,
  "stream_options": null,
  "temperature": 1,
  "top_p": 1,
  "tools": null,
  "tool_choice": "none",
  "logprobs": false,
  "top_logprobs": null
}
```


