-- V45: 重构选考科目分数字段为命名列
-- 将 score_subject_1/2/3（位置型）改为 score_physics/chemistry/biology/politics/history/geography（命名型）
-- 删除 second_subject_type、third_subject_type（选科信息可从命名分数字段推导）
-- 保留 subject_type（用于文/理/物/史科类 API 查询）

-- 1. 新增 6 个命名分数字段
ALTER TABLE t_member_gaokao ADD COLUMN score_physics INTEGER;
ALTER TABLE t_member_gaokao ADD COLUMN score_chemistry INTEGER;
ALTER TABLE t_member_gaokao ADD COLUMN score_biology INTEGER;
ALTER TABLE t_member_gaokao ADD COLUMN score_politics INTEGER;
ALTER TABLE t_member_gaokao ADD COLUMN score_history INTEGER;
ALTER TABLE t_member_gaokao ADD COLUMN score_geography INTEGER;

-- 2. 迁移数据：传统文理模式
-- 文科：政治/历史/地理
UPDATE t_member_gaokao SET
  score_politics  = score_subject_1,
  score_history   = score_subject_2,
  score_geography = score_subject_3
WHERE subject_type = '文科';

-- 理科：物理/化学/生物
UPDATE t_member_gaokao SET
  score_physics   = score_subject_1,
  score_chemistry = score_subject_2,
  score_biology   = score_subject_3
WHERE subject_type = '理科';

-- 3. 迁移数据：3+1+2 和 3+3 模式（subject_type 存科目名）
-- 根据 subject_type / second_subject_type / third_subject_type 匹配科目名找到对应分数
UPDATE t_member_gaokao SET
  score_physics = CASE
    WHEN subject_type = '物理'       THEN score_subject_1
    WHEN second_subject_type = '物理' THEN score_subject_2
    WHEN third_subject_type = '物理'  THEN score_subject_3
  END,
  score_chemistry = CASE
    WHEN subject_type = '化学'       THEN score_subject_1
    WHEN second_subject_type = '化学' THEN score_subject_2
    WHEN third_subject_type = '化学'  THEN score_subject_3
  END,
  score_biology = CASE
    WHEN subject_type = '生物'       THEN score_subject_1
    WHEN second_subject_type = '生物' THEN score_subject_2
    WHEN third_subject_type = '生物'  THEN score_subject_3
  END,
  score_politics = CASE
    WHEN subject_type = '政治'       THEN score_subject_1
    WHEN second_subject_type = '政治' THEN score_subject_2
    WHEN third_subject_type = '政治'  THEN score_subject_3
  END,
  score_history = CASE
    WHEN subject_type = '历史'       THEN score_subject_1
    WHEN second_subject_type = '历史' THEN score_subject_2
    WHEN third_subject_type = '历史'  THEN score_subject_3
  END,
  score_geography = CASE
    WHEN subject_type = '地理'       THEN score_subject_1
    WHEN second_subject_type = '地理' THEN score_subject_2
    WHEN third_subject_type = '地理'  THEN score_subject_3
  END
WHERE subject_type NOT IN ('文科', '理科')
  AND subject_type IS NOT NULL;

-- 4. 删除旧的位置型字段
ALTER TABLE t_member_gaokao DROP COLUMN second_subject_type;
ALTER TABLE t_member_gaokao DROP COLUMN third_subject_type;
ALTER TABLE t_member_gaokao DROP COLUMN score_subject_1;
ALTER TABLE t_member_gaokao DROP COLUMN score_subject_2;
ALTER TABLE t_member_gaokao DROP COLUMN score_subject_3;
