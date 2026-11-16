-- =============================================================
-- V83：补齐演示机构的轮播图（2026-11-17）
-- 由 Flyway 管理，已执行后禁止修改本文件；后续变更请新增 V84+
--
-- 说明：
--   1. V82 建演示数据时公共读 bucket 尚未就绪，carousel_images 留空，
--      导致 C 端机构详情「轮播图」区块整块不渲染（原型是 1 大图 + 4 缩略图）。
--   2. 本迁移为 4 家演示机构各补 4 张相册图，URL 指向 haifeng-public 公共读 bucket。
--      图片为程序化生成的 4 种主题占位图（campus / class / desk / chart），
--      与原型一致，可后续整批替换为实拍图。
--   3. 幂等：直接 UPDATE 固定值，重复执行结果一致。
--   4. 若部署环境更换了公开 bucket，需同步调整 oss.public-url-prefix 与这里的 URL。
-- =============================================================

-- 启航高中冲刺小班
UPDATE t_institutions
SET carousel_images = ARRAY[
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/qihang-1-campus.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/qihang-2-class.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/qihang-3-desk.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/qihang-4-chart.svg'
],
    updated_at = NOW()
WHERE id = 2084000000000000001;

-- 上岸公考培训中心
UPDATE t_institutions
SET carousel_images = ARRAY[
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/shangan-1-campus.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/shangan-2-class.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/shangan-3-desk.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/shangan-4-chart.svg'
],
    updated_at = NOW()
WHERE id = 2084000000000000002;

-- 国企之路培训中心
UPDATE t_institutions
SET carousel_images = ARRAY[
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/guoqi-1-campus.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/guoqi-2-class.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/guoqi-3-desk.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/guoqi-4-chart.svg'
],
    updated_at = NOW()
WHERE id = 2084000000000000003;

-- 匠心职业培训学校
UPDATE t_institutions
SET carousel_images = ARRAY[
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/jiangxin-1-campus.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/jiangxin-2-class.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/jiangxin-3-desk.svg',
    'https://haifeng-public.oss-cn-beijing.aliyuncs.com/haifeng/institutions/jiangxin-4-chart.svg'
],
    updated_at = NOW()
WHERE id = 2084000000000000004;
