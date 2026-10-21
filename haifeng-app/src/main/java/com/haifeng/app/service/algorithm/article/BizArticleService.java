package com.haifeng.app.service.algorithm.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.article.BizArticleQueryDTO;
import com.haifeng.app.dto.algorithm.article.BizArticleSearchDTO;
import com.haifeng.app.vo.algorithm.article.BizArticleDetailVO;
import com.haifeng.app.vo.algorithm.article.BizArticleListVO;
import com.haifeng.app.vo.algorithm.article.BizArticleSearchItemVO;

import java.util.List;

/**
 * 热点资讯 - C 端 Service（高考/考公/考研/中考/英语四六级五模块共用，按 moduleName 区分）
 * <p>C 端全部免登录，仅暴露已发布（status=0）的资讯。
 */
public interface BizArticleService {

    /**
     * 分页查询某模块的已发布资讯列表（精准省份 + 模块固定 + status=0，不做专栏过滤，
     * 前端根据返回的 columnName 自行归类排版）
     *
     * @param moduleName 模块名（高考/考公/考研/中考/英语四六级，Controller 固定传入）
     * @param dto        分页 + 省份（精准）
     */
    IPage<BizArticleListVO> pagePublished(String moduleName, BizArticleQueryDTO dto);

    /**
     * 查询某模块已发布资讯详情（校验模块归属 + status=0，下架/跨模块一律 404 不暴露）
     */
    BizArticleDetailVO getPublishedDetail(String moduleName, Long id);

    /**
     * 标题关键字搜索（免登录，去噪对齐匹配，AND 语义，按 sort_order DESC → publish_time DESC
     * 取前 limit 条并附高亮区间；province 空白或「全国」= 不限省份）。
     * <p>moduleName 非空时仅查该类型；为 null 时跨全部类型（高考/考公/考研/中考/英语四六级）查询。
     * 高考模块与新首页搜索共用本方法，避免重复搜索编排逻辑。
     */
    List<BizArticleSearchItemVO> searchPublished(String moduleName, BizArticleSearchDTO dto);
}
