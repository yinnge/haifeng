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
需实现三个接口，
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

##### 任务一【删除多余接口】
**路径：** D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm\pdf\PdfPlanController.java
**任务：** 这下面有四个接口，变成一个接口，我们需要返回一个pdf的接口就够了，把其他的给删除，以及关联的service等给删除
**原因：** 因为家长买你的账，不是为了看三份割裂的说明书，而是为了看一个“顶级规划师”把这三个维度交叉在一起给出的终极博弈结论（例如：“某大学虽然学校牌子响，但你考这个分数只能进它的夕阳专业，且该城市目前面临支柱产业转型，强烈建议降一档学校，去另一个处于风口产业城市的强王牌专业”）。

##### 任务二【理解整体框架】
用户已经有了每个专业的录取分数表，但是静态的，我们需要ai综合分析出城市，大学，专业的程度，结合用户资料，来判断哪个合适，生成PDF

解决方案：基于 Map-Reduce（分片提炼 $\rightarrow$ 全局统筹）的“全景志愿档案”为了兼顾“长文本上下文限制”、“Token防爆炸”和“全局宏观对比”，我们不能把所有原始数据一次性全倒给AI。我们要采用“两步走”的漏斗模型。
第一步：Map 阶段（分片诊断——解决“Token爆炸与精细度”问题）不要一次性调用AI去分析所有大学。Java 在串行循环拉取每一所大学和对应专业组的数据时，针对单所大学进行一次极轻量的AI调用：给AI的硬上下文：只有这一所大学、这个城市、这5个专业的名字，以及 调用AI自身的能力的知识 带回来的最新产业大势。
彻底不给AI看5年的分数、位次、招生人数等死数据。让AI返回什么：不要让它长篇大论，用 Prompt 限制它只吐出 300字以内的“院校专业组地缘客观研判”（纯文字，例如：“该校软件工程在华东大厂认可度极高，但城市落户门槛今年提高，结合行业降本增效大势，建议非卷王体质慎选”）。结果：Java 拿着这个AI生成的 300字简评，和这所学校的所有分数、概率 DTO 绑定在一起，形成一个精炼的 UniversityReportDTO。

##### 任务三 【Reduce阶段】
第二步：Reduce 阶段（全局博弈——解决“综合考虑与对比”问题）
当所有单所大学的 300字简评被提炼出来后，我们让“总控首席专家智能体”登场，做最后的全局大对比。

如何防止Token爆炸？
在调用最后这个总控AI时，把那长达5年的录取分数大表、几十行的招生人数、繁琐的数据库字段全部扔掉！（因为这些死数据，一会儿直接由 Java 填进 HTML 表格里 ，根本不需要AI去数数和对比）。

只喂给总控AI高浓度的“大牌堆”：

JSON
[
{"大学": "北京交通大学", "城市": "北京", "专业": ["自动化", "智能芯片"], "录取概率": "78% (稳)", "AI片诊断结论": "北交大自动化依托轨交红利保研率高，但传统工科在京就业卷..."},
{"大学": "上海理工大学", "城市": "上海", "专业": ["计算机", "软件工程"], "录取概率": "92% (保)", "AI片诊断结论": "地处上海集成电路高地，专业红利吃满，但学校双非光环略弱..."}
]
让总控AI最终输出什么？
总控AI面对这个被你脱水、过滤、高浓度提炼后的“黄金大牌堆”，它不需要去看几万字的原始数字，上下文只占用了极少的 Token。它唯一的任务就是施展大模型的“推理、总结、权衡”圣光，为你产出整份报告中最值钱的灵魂章节：

志愿表 SWOT 全局象限分析（分析哪些属于“高风险高收益”，哪些属于“性价比之王”）。

城市地域红利 VS 学校名气光环的博弈辩证逻辑。

最终的“海枫强烈推荐填报梯队顺序”（它会综合考虑概率、城市、行业，给出一个排兵布阵的专家建议）。

##### 了解事项
页眉、页脚、页码、版权声明怎么做？
在 W3C 标准中，有一种专门控制打印排版的 CSS 规范叫 Paged Media（分页媒体）。OpenHTMLtoPDF 完美支持它。你只需要在 HTML 的 <style> 标签里写几行描述性的 CSS：

告诉它：全盘统一规定，页面的右下角（@bottom-right）自动显示页码；页面的正下方（@bottom-center）固定显示文案“海枫未来规划院@版权所有” 。

打印引擎在渲染时，只要发现这一页到了 A4 纸的底部，它在切页的同时，会自动在每一页的那个固定屁股位置，把你的版权和页码“盖章”上去，完全不需要你在 Java 里去算坐标 。

封面第一页和第 2 页、第 3 页怎么精准割开？

利用 CSS 的 page-break-after: always;（强制后分页） 。

你的 HTML 结构就像搭积木一样：


块盒子 1：封面内容（放大 LOGO，炫酷的标题，介绍文案）。在这个盒子的 CSS 里加上 page-break-after: always;。打印引擎读到这，就会硬生生把纸切断，哪怕封面只有三个字，后面也绝对留白，剩下的数据强行塞进第 2 页 。
块盒子 2：大学数据大表。同样加上分页符。【举例】
块盒子 3：城市数据大表。同样加上分页符。【举例】
关于首尾页没有页眉页脚的特权：你可以用 CSS 选择器（如 @page :first）单独规定：第一页（封面）把页眉页脚的边距设为 0，且不显示任何页码。这样，封面就是一个干净完整的纯美观封面了。

**注意**： logo的url：https://img.imgos.cn/cdn/21/20260513/852338961e39f515618e03596c4b2e71.png

##### PDF生成格式

利用前面提到的 Thymeleaf + CSS 强行分页（page-break-after: always;） ，我们把这份统一的 PDF 档案设计成“总—分—总”的完美长卷：

📊 第 1 页：专属封面（静态 HTML - 零 AI 成本）
高大上的海枫未来规划院品牌 Logo 与版权页眉页脚 。

考生的基本高考画像（2026年、XX省、物理类、615分、位次8500） 。

🧠 第 2 🚀 3 页：全局宏观全景研判（灵魂核心 - 总控 AI 产出的结果）
把总控AI在 Reduce 阶段 产出的全局对比、SWOT 象限图、AI大势总结 、以及最终的填报梯队建议，用漂亮的 Markdown 转 HTML 组件（Flexmark）优雅地渲染在这里 。

家长一翻开报告，前两页直接看到高价值的“大师级总括结论”。

📐 第 4 页：全盘静态明细汇总大表（静态数据 - 零 AI 成本）
Java 利用 Thymeleaf 的 th:each 循环，直接把用户选中的所有大学、专业、你自己算法算出的录取概率，生成一张规整、紧凑的汇总 Table。不让 AI 参与，样式绝对不会乱。

🔍 第 5 页及以后：一页一校的“解剖麻雀式”深度透视（Map 阶段的动静结合）
由于使用了强制分页 ，每一页只解剖一所大学【超过就下一页】：

上半页（死数据大表 - Java 渲染）：展示该大学详尽的硬实力指标（推免率、博硕士点） ，以及该组下所有专业近5年的录取最低分、招生人数、最低位次大表格 。
下半页（动态灵魂 - 单校 Micro-AI 评语）：紧接着大表下方，展示该校该专业在该城市的 300 字深度行业与地缘研判。

【页数不够换页继续】

##### 需要用之前的service
1. 用户信息
package com.haifeng.app.service.impl.algorithm;
public GaokaoArchiveVO getMyArchive() 
2. 城市详情
package com.haifeng.app.service.impl.city;
   public CityDetailVO detail(Long cityId)
3. 院校
package com.haifeng.app.service.impl.university;
public UniversityDetailVO detail(Long universityId)
4. 专业
package com.haifeng.app.service.impl.university;
public UniversityDetailVO detail(Long universityId)
5. 专业组与专业明细【前端会展示这些分页的数据，然后呢，会返回必要的id】
在：package com.haifeng.app.service.algorithm.wish;WishPlanService
有对应的方法：WishGroupSnapshot getExportGroupSnapshot(Integer groupSnapshotId);等
6. 最后将对象转换成JSONB，后续为给ai
**提示：** 
1. 可以看一下vo等看有哪些返回字段
2. 可以根据字段写和拼接一些固定好的字符串返回静态数据

#### 注意事项
1. 每个专业的近五年数据在：专业明细WishExportMajorVO里面的historyScores
2. WishExportMajorVO里面的安全系数，说白了类似于录取概率，safetyLevel和levelShort对应。他是我通过算法经过位次，最低分，CV修正等算法算出来的，越大录取概率越大，ai可以作为参考



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


