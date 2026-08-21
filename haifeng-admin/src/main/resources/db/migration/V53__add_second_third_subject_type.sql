-- V53__add_second_third_subject_type.sql
-- 为 t_member_gaokao 补齐「第二科目 / 第三科目」列
-- 选科匹配（必选化学等）、限报约束与查询过滤需要用到再选科目，
-- V28 已定义这两列，但 dev 环境 flyway 关闭、运行库漏建，此处幂等补齐。
-- 生产环境 flyway 开启时会自动重放本脚本。

BEGIN;

-- 第二科目（3+1+2 再选科目之一 / 3+3 选考科目之一）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS second_subject_type VARCHAR(20);

-- 第三科目（3+3 选考科目之一）
ALTER TABLE t_member_gaokao
    ADD COLUMN IF NOT EXISTS third_subject_type VARCHAR(20);

-- 注释
COMMENT ON COLUMN t_member_gaokao.second_subject_type IS '第二科目（再选/选考科目之一）';
COMMENT ON COLUMN t_member_gaokao.third_subject_type IS '第三科目（3+3 选考科目之一）';

COMMIT;
