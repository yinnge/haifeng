-- =============================================================
-- V80：培训机构表 t_institutions 重建（2026-11-16）
-- 由 Flyway 管理，已执行后禁止修改本文件；后续变更请新增 V81+
--
-- 背景：V4 建的是旧结构（type/courses/images/logo/description/is_deleted），
--      新结构此前只手工执行过，未进入迁移脚本，导致新环境用 Flyway 初始化会与代码不一致。
--
-- 兼容两种现状：
--   1) 旧结构（存在 type 列）       → DROP 后按新结构重建（旧数据不要）
--   2) 已是新结构（手工执行过）     → 整体跳过，不丢数据
-- =============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 't_institutions'
          AND column_name = 'type'
    ) THEN
        DROP TABLE public.t_institutions;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS public.t_institutions (
    id                bigint       NOT NULL,
    category          character varying(32)  NOT NULL,
    user_id           bigint       NOT NULL,
    name              character varying(128) NOT NULL,
    province          character varying(30)  NOT NULL,
    founded_year      character varying(32),
    carousel_images   text[]       DEFAULT '{}'::text[] NOT NULL,
    tags              text[]       DEFAULT '{}'::text[] NOT NULL,
    introduction      text,
    main_direction    character varying(64),
    class_size        character varying(32),
    teacher_config    character varying(256),
    course_form       character varying(64),
    class_types       jsonb        DEFAULT '[]'::jsonb NOT NULL,
    notice            character varying(512),
    advantages        text[]       DEFAULT '{}'::text[] NOT NULL,
    address           character varying(256),
    wechat_qrcode_url character varying(512),
    email             character varying(128),
    phone             character varying(20),
    status            smallint     DEFAULT 1 NOT NULL,
    sort_order        integer      DEFAULT 0 NOT NULL,
    version           integer      DEFAULT 0 NOT NULL,
    created_by        bigint,
    updated_by        bigint,
    created_at        timestamp with time zone DEFAULT now() NOT NULL,
    updated_at        timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT t_institutions_pkey PRIMARY KEY (id),
    CONSTRAINT chk_institutions_status CHECK ((status = ANY (ARRAY[0, 1]))),
    CONSTRAINT chk_institutions_class_types_array CHECK ((jsonb_typeof(class_types) = 'array'::text))
);

COMMENT ON TABLE public.t_institutions IS '培训机构表';
COMMENT ON COLUMN public.t_institutions.id IS '机构ID(雪花算法)';
COMMENT ON COLUMN public.t_institutions.category IS '分类: 高中冲刺小班/考公考编机构/央国企培训/职业培训机构(后端校验)';
COMMENT ON COLUMN public.t_institutions.user_id IS '管理账号ID(关联t_member, 必须is_teacher=true, 全局唯一)';
COMMENT ON COLUMN public.t_institutions.name IS '机构名称';
COMMENT ON COLUMN public.t_institutions.province IS '省份(中文, ProvinceEnum)';
COMMENT ON COLUMN public.t_institutions.founded_year IS '创办时间, 如 2013年';
COMMENT ON COLUMN public.t_institutions.carousel_images IS '轮播图片(服务器相对路径或http(s) URL)';
COMMENT ON COLUMN public.t_institutions.tags IS '标签';
COMMENT ON COLUMN public.t_institutions.introduction IS '机构介绍';
COMMENT ON COLUMN public.t_institutions.main_direction IS '主打方向, 如 美术/音乐/传媒文化课';
COMMENT ON COLUMN public.t_institutions.class_size IS '班级规模, 如 15人以内';
COMMENT ON COLUMN public.t_institutions.teacher_config IS '师资配置';
COMMENT ON COLUMN public.t_institutions.course_form IS '上课形式, 如 联考后全日制';
COMMENT ON COLUMN public.t_institutions.class_types IS '主打班型JSON数组: [{title,introduction,tag}]';
COMMENT ON COLUMN public.t_institutions.notice IS '说明/温馨提示';
COMMENT ON COLUMN public.t_institutions.advantages IS '机构优势';
COMMENT ON COLUMN public.t_institutions.address IS '地址';
COMMENT ON COLUMN public.t_institutions.wechat_qrcode_url IS '微信二维码(服务器相对路径)';
COMMENT ON COLUMN public.t_institutions.email IS '邮箱';
COMMENT ON COLUMN public.t_institutions.phone IS '联系电话';
COMMENT ON COLUMN public.t_institutions.status IS '状态: 0-下架, 1-上架';
COMMENT ON COLUMN public.t_institutions.sort_order IS '排序(越小越靠前)';
COMMENT ON COLUMN public.t_institutions.version IS '乐观锁版本号';
COMMENT ON COLUMN public.t_institutions.created_by IS '创建人(sys_admin.id)';
COMMENT ON COLUMN public.t_institutions.updated_by IS '更新人(sys_admin.id)';
COMMENT ON COLUMN public.t_institutions.created_at IS '创建时间';
COMMENT ON COLUMN public.t_institutions.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_institutions_user ON public.t_institutions USING btree (user_id);
CREATE INDEX IF NOT EXISTS idx_institutions_cat_prov ON public.t_institutions USING btree (category, province, status, sort_order);
CREATE INDEX IF NOT EXISTS idx_institutions_prov ON public.t_institutions USING btree (province, status, sort_order);
