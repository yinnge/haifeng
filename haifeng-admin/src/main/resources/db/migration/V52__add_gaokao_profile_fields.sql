-- V52__add_gaokao_profile_fields.sql
-- 为 t_member_gaokao 添加考生画像与约束条件字段
-- 用于 PDF 报告第一部分「考生基础画像与现实约束条件」AI 分析

BEGIN;

-- 1.1 其他疾病（用户自述，文本输入）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS other_health_conditions TEXT;

-- 1.2 政审情况（军校/公安/司法等特殊院校需要）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS political_review_status VARCHAR(200);

-- 1.3 性别（枚举：男/女）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS gender VARCHAR(10);

-- 1.4 性格特质
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS personality_traits TEXT;

-- 1.5 接受度：基层岗位
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS accept_grassroot BOOLEAN;

-- 1.6 接受度：倒班
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS accept_shift_work BOOLEAN;

-- 1.7 接受度：夜班
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS accept_night_work BOOLEAN;

-- 1.8 接受度：长期出差
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS accept_business_trip BOOLEAN;

-- 1.9 接受度：异地工作
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS accept_relocation BOOLEAN;

-- 1.10 兴趣倾向
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS interest_direction TEXT;

-- 1.11 排斥行业/岗位
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS rejected_industries TEXT;

-- 2.1 学费承受度（每年可承担的学费上限）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS tuition_affordability VARCHAR(50);

-- 2.2 地域约束：是否必须留本省
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS stay_in_province BOOLEAN;

-- 2.3 家庭资源（体制内亲属、行业资源等）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS family_resources TEXT;

-- 3.1 发展定位（本科就业/考研深造/并行）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS career_dev_path VARCHAR(50);

-- 3.2 排斥方向
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS rejected_directions TEXT;

-- 注释
COMMENT ON COLUMN t_member_gaokao.other_health_conditions IS '其他疾病（用户自述）';
COMMENT ON COLUMN t_member_gaokao.political_review_status IS '政审情况（军校/公安/司法等特殊院校）';
COMMENT ON COLUMN t_member_gaokao.gender IS '性别（男/女）';
COMMENT ON COLUMN t_member_gaokao.personality_traits IS '性格特质';
COMMENT ON COLUMN t_member_gaokao.accept_grassroot IS '是否接受基层岗位';
COMMENT ON COLUMN t_member_gaokao.accept_shift_work IS '是否接受倒班';
COMMENT ON COLUMN t_member_gaokao.accept_night_work IS '是否接受夜班';
COMMENT ON COLUMN t_member_gaokao.accept_business_trip IS '是否接受长期出差';
COMMENT ON COLUMN t_member_gaokao.accept_relocation IS '是否接受异地工作';
COMMENT ON COLUMN t_member_gaokao.interest_direction IS '兴趣倾向';
COMMENT ON COLUMN t_member_gaokao.rejected_industries IS '排斥行业/岗位';
COMMENT ON COLUMN t_member_gaokao.tuition_affordability IS '学费承受度（每年可承担上限）';
COMMENT ON COLUMN t_member_gaokao.stay_in_province IS '是否必须留本省';
COMMENT ON COLUMN t_member_gaokao.family_resources IS '家庭资源（体制内亲属、行业资源等）';
COMMENT ON COLUMN t_member_gaokao.career_dev_path IS '发展定位（本科就业/考研深造/并行）';
COMMENT ON COLUMN t_member_gaokao.rejected_directions IS '排斥方向';

COMMIT;
