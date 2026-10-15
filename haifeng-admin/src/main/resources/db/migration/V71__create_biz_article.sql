-- V71__create_biz_article.sql
-- 热点资讯：高考 / 考公 / 考研 / 中考 / 英语四六级 五大模块共用的资讯文章表（admin 端管理，app 端后续消费）
-- 对应后端接口：/api/v1/admin/fileload/article -> @RequireAdminModule("fileload_article")
-- 模块挂「文件管理」一级模块下（fileload, id=2074728249800000001，见 V56），sort=10
-- （对齐 V69：初中/高中/大学/考研/考公/研究生资源 sort 1..9 之后）

-- 1) 热点资讯表
CREATE TABLE t_biz_article (
    id            BIGINT PRIMARY KEY,                -- 雪花ID（MyBatis-Plus ASSIGN_ID）
    module_name   VARCHAR(32)  NOT NULL,            -- 所属模块：高考/考公/考研/中考/英语四六级（直接存中文）
    province      VARCHAR(16),                       -- 省份裸值（无 省/市 后缀，如 山西）；全国 统一存「全国」；NULL=不限省份
    year          SMALLINT,                          -- 年份（如 2026）；NULL=不分年份
    column_name   VARCHAR(64)  NOT NULL,            -- 专栏名
    title         VARCHAR(255) NOT NULL,             -- 标题
    summary       VARCHAR(500),                      -- 列表页摘要
    content       TEXT         NOT NULL,             -- 富文本 HTML 正文
    status        SMALLINT     DEFAULT 0,            -- 0已发布 1下架
    sort_order    INT          DEFAULT 0,            -- 排序权重（越大越靠前）
    view_count    BIGINT       DEFAULT 0,            -- 浏览量
    publish_time  TIMESTAMPTZ,                       -- 发布时间
    create_by     BIGINT,                            -- 创建人（sys_admin.id）
    create_time   TIMESTAMPTZ  DEFAULT now(),
    update_time   TIMESTAMPTZ  DEFAULT now(),
    CONSTRAINT ck_biz_article_status CHECK (status IN (0, 1))
);

COMMENT ON TABLE  t_biz_article                IS '热点资讯表（高考/考公/考研/中考/英语四六级五模块共用）';
COMMENT ON COLUMN t_biz_article.module_name    IS '所属模块：高考/考公/考研/中考/英语四六级';
COMMENT ON COLUMN t_biz_article.province       IS '省份裸值（无省/市后缀，如 山西）；全国=全国；NULL=不限省份';
COMMENT ON COLUMN t_biz_article.year           IS '年份（如 2026）；NULL=不分年份';
COMMENT ON COLUMN t_biz_article.column_name    IS '专栏名';
COMMENT ON COLUMN t_biz_article.summary        IS '列表页摘要';
COMMENT ON COLUMN t_biz_article.content        IS '富文本 HTML 正文';
COMMENT ON COLUMN t_biz_article.status         IS '状态：0已发布 1下架';
COMMENT ON COLUMN t_biz_article.sort_order     IS '排序权重（越大越靠前）';
COMMENT ON COLUMN t_biz_article.view_count     IS '浏览量';
COMMENT ON COLUMN t_biz_article.publish_time   IS '发布时间';
COMMENT ON COLUMN t_biz_article.create_by      IS '创建人（sys_admin.id）';

-- 按典型查询路径建索引：模块+省份+年份+专栏 → 列表
-- （修正原稿：索引列名对齐实际列 module_name / province，原稿误写 module_code / province_code）
CREATE INDEX idx_biz_article_query ON t_biz_article
    (module_name, province, year, column_name, status, sort_order DESC);

-- 2) 模块注册：热点资讯（parent_id=2074728249800000001「文件管理」，level=2，路径对齐后端 @RequestMapping）
INSERT INTO sys_module (id, module_name, module_code, parent_id, level, path, sort_order)
VALUES (2074728249650000041, '热点资讯', 'fileload_article', 2074728249800000001, 2, '/fileload/article', 10)
ON CONFLICT (module_code) DO NOTHING;

-- 3) 角色-模块绑定：超级管理员(role_id=2074728248943710208) -> 热点资讯
--    （管理员-模块权限链路：sys_admin.role_id -> sys_role_module -> sys_module，无需改 sys_admin 行）
INSERT INTO sys_role_module (id, role_id, module_id)
VALUES (2074728249650000042, 2074728248943710208, 2074728249650000041)
ON CONFLICT (role_id, module_id) DO NOTHING;
