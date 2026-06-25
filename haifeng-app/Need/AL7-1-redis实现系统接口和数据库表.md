
## 已有表
以实现entity
### 系统设置表
```
CREATE TABLE system_settings (
id                BIGSERIAL PRIMARY KEY,
site_name         VARCHAR(50),
site_url          VARCHAR(100),
site_icp          VARCHAR(100),
site_description  TEXT,
api_number        INTEGER DEFAULT 3,
reach_high_count  INTEGER NOT NULL DEFAULT 1,
reach_count       INTEGER NOT NULL DEFAULT 2,
match_count       INTEGER NOT NULL DEFAULT 3,
safe_count        INTEGER NOT NULL DEFAULT 2,
floor_count       INTEGER NOT NULL DEFAULT 1,
pro_price         INTEGER DEFAULT 199,
vip_price         INTEGER DEFAULT 599,
pro_commission_rate   SMALLINT DEFAULT 10 CHECK (pro_commission_rate >= 0 AND pro_commission_rate <= 100),
vip_commission_rate   SMALLINT DEFAULT 15 CHECK (vip_commission_rate >= 0 AND vip_commission_rate <= 100),
seo_title         VARCHAR(200),
seo_keywords      VARCHAR(100),
seo_description   TEXT,
contact_url       JSONB DEFAULT '{}',
basic_message     JSONB DEFAULT '{}',
created_at        TIMESTAMPTZ DEFAULT NOW(),
updated_at        TIMESTAMPTZ DEFAULT NOW(),
CONSTRAINT chk_safety_count CHECK (
reach_high_count >= 0 AND
reach_count >= 0 AND
match_count >= 0 AND
safe_count >= 0 AND
floor_count >= 0
)
);

COMMENT ON TABLE system_settings IS '系统设置表（单例）';
COMMENT ON COLUMN system_settings.reach_high_count IS '搏(大胆冲刺)档默认推荐志愿数（≥0）。用户进入志愿填报页时，后端返回该值作为每档可选数量上限，提交时按此值校验';
COMMENT ON COLUMN system_settings.reach_count IS '冲(可以冲击)档默认推荐志愿数（≥0）。用户进入志愿填报页时，后端返回该值作为每档可选数量上限，提交时按此值校验';
COMMENT ON COLUMN system_settings.match_count IS '稳(较为稳妥)档默认推荐志愿数（≥0）。用户进入志愿填报页时，后端返回该值作为每档可选数量上限，提交时按此值校验';
COMMENT ON COLUMN system_settings.safe_count IS '保(比较安全)档默认推荐志愿数（≥0）。用户进入志愿填报页时，后端返回该值作为每档可选数量上限，提交时按此值校验';
COMMENT ON COLUMN system_settings.floor_count IS '垫(高度保底)档默认推荐志愿数（≥0）。用户进入志愿填报页时，后端返回该值作为每档可选数量上限，提交时按此值校验';
COMMENT ON COLUMN system_settings.pro_commission_rate IS 'Pro会员提成比例（0-100），代表0%到100%';
COMMENT ON COLUMN system_settings.vip_commission_rate IS 'VIP会员提成比例（0-100），代表0%到100%';
COMMENT ON COLUMN system_settings.contact_url IS 'JSON格式：{wechat, weibo, zhihu, douyin, bilibili}';
COMMENT ON COLUMN system_settings.basic_message IS 'JSON格式：{address, phone, email, consultationTime}';

```

## 任务一
1. 创建一个接口，返回：
   reach_high_count  INTEGER NOT NULL DEFAULT 1,
   reach_count       INTEGER NOT NULL DEFAULT 2,
   match_count       INTEGER NOT NULL DEFAULT 3,
   safe_count        INTEGER NOT NULL DEFAULT 2,
   floor_count       INTEGER NOT NULL DEFAULT 1,
返回这五个字段，将结果存储到redis里面

### 说明
1. 用户限制：必须登录才可以看到
2. 在D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\controller\algorithm的下面创建一个WishPlanController.java
3. 已有entity和mapper，在这里：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\entity\system\SystemSettings.java
## 任务二
### 创建数据表结构

在：D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-admin\src\main\resources\db\migration\apps_V18__t_wish_plans_tables.sql的下面创建数据库表，flyway还没有启动，直接创建原始表

#### 志愿方案主表 (t_wish_plan)

```
-- ============================================================
-- 1. 志愿方案主表 (t_wish_plan)
-- ============================================================
CREATE TABLE IF NOT EXISTS t_wish_plan (
     id                      SERIAL          PRIMARY KEY,
     member_id               INTEGER         NOT NULL,           -- 用户ID
     plan_name               VARCHAR(100)    DEFAULT '我的志愿方案1' NOT NULL,
     plan_year               SMALLINT        NOT NULL,           -- 高考年份
     plan_province           VARCHAR(30)     NOT NULL,           -- 高考省份
     reform_model            VARCHAR(20)     NOT NULL,           -- 改革模式
     plan_batch              VARCHAR(50)     NOT NULL,           -- 批次（本科批等）
     
    -- 用户分数快照
    user_score              INTEGER         NOT NULL,
    user_rank               INTEGER         NOT NULL,
    
    -- 额度限制 (Integer 字段，在最开始大库查询添加明细时由后端拦截)
    bo_limit                INTEGER         DEFAULT 0 NOT NULL, -- 搏 的最大允许个数
    chong_limit             INTEGER         DEFAULT 0 NOT NULL, -- 冲
    wen_limit               INTEGER         DEFAULT 0 NOT NULL, -- 稳
    bao_limit               INTEGER         DEFAULT 0 NOT NULL, -- 保
    die_limit               INTEGER         DEFAULT 0 NOT NULL, -- 垫
    jin_limit               INTEGER         DEFAULT 0 NOT NULL, -- 禁
    
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

#### 志愿方案-专业组快照表 (t_wish_group_snapshot)
```
-- ============================================================
-- 2. 志愿方案-专业组快照表 (t_wish_group_snapshot)
-- 映射关：1个志愿方案 包含 多个专业组
-- ============================================================
CREATE TABLE IF NOT EXISTS t_wish_group_snapshot (
    id                      SERIAL          PRIMARY KEY,
    plan_id                 INTEGER         NOT NULL,           -- 关联主表
    group_id                INTEGER          NOT NULL,           -- 大库原始专业组雪花ID

    -- 排序字段
    group_sort_order        INTEGER         NOT NULL DEFAULT 0, -- 专业组在整个方案中的全局排序序号

    -- 专业组关键快照数据 ( t_admission_group 表的数据)
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
    constraints_description TEXT[],
    
    -- 大学表外键冗余快照 (Q6)
    recommendation_year     SMALLINT,                           -- 推免年份
    recommendation_rate     NUMERIC(5,2),                       -- 推免率 (例如 24.50)
   
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
```

#### 志愿方案-专业明细快照表 (t_wish_major_snapshot)

```
-- ============================================================
-- 3. 志愿方案-专业明细快照表 (t_wish_major_snapshot)
-- 映射关系：1个专业组快照 包含 多个专业明细快照
-- ============================================================
    CREATE TABLE IF NOT EXISTS t_wish_major_snapshot (
    id                      SERIAL          PRIMARY KEY,
    plan_id                 INTEGER         NOT NULL,           -- 关联主表
    group_snapshot_id       INTEGER         NOT NULL,           -- 关联组快照表id

    -- 排序与导出控制
    major_sort_order        INTEGER         NOT NULL DEFAULT 0, -- 组内明细独立排序序号
    is_exported             BOOLEAN         NOT NULL DEFAULT TRUE, -- 需求5：是否勾选导出（页面上的加号/减号控制）
    
    -- 专业明细关键数据快照
    major_code              VARCHAR(30)     NOT NULL,           -- 招生专业代码
    major_name              TEXT            NOT NULL,           -- 专业名称（含内含专业细分文本）
    duration                VARCHAR(20),                        -- 学制 (如 "4")
    tuition                 NUMERIC(10,2),                      -- 学费
    admission_count              INTEGER,                       -- 该专业招生计划人数
    
    -- 安全系数快照存储 (Q2)
    safety_level            NUMERIC(3,2),                       -- 算法动态计算出的安全系数 (0.00~1.00)
    level_short             VARCHAR(10)     NOT NULL,           -- 档位简称 (搏/冲/稳/保/垫/禁)
            
    -- 历史五年录取分快照 (Q10: 采用 JSONB 落地存储，完美兼容动态年数且防止 OOM)
    history_scores          JSONB,                              
    
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 4. 索引优化 (保障流式高频拉取性能)
-- ============================================================
CREATE INDEX idx_twg_plan ON t_wish_group_snapshot (plan_id, group_sort_order);
CREATE INDEX idx_twm_group ON t_wish_major_snapshot (group_snapshot_id, major_sort_order);
CREATE INDEX idx_twm_plan ON t_wish_major_snapshot (plan_id) WHERE is_exported = TRUE;
```

## 任务三
  
  1. 根据这些数据表，创还能mapper，entity等公共类

### 注意事项

  2. 所有的身份都已实现注解，在：D:\0code\haifeng\backend\haifeng\haifeng-common\src\main\java\com\haifeng\common\annotation
  3. 不要使用git命令提交，最后修改的统一提交，遇到冲突的需要给我判决，一般都不会遇到。