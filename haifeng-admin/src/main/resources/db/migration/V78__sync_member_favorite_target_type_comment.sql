-- ============================================
-- V78__sync_member_favorite_target_type_comment.sql
-- 同步 t_member_favorite.target_type 的表注释，与后端枚举 FavoriteTargetType 对齐。
-- 新增：file_info（专栏文件，t_file_info）、postgrad_major（考研专业，t_postgrad_major）。
--
-- 说明：target_type 为 VARCHAR(32) 且无 CHECK 约束，新增类型无需改列定义或回填存量数据，
-- 本迁移仅更新 COMMENT，幂等可重复执行。
-- ============================================

COMMENT ON TABLE t_member_favorite IS '会员收藏表（多态：院校/本科专业/考研专业/城市/竞赛/证书/企业/行业/文件）';
COMMENT ON COLUMN t_member_favorite.target_type IS '收藏目标类型：university/major/postgrad_major/city/competition/certificate/enterprise/industry/file_info';
