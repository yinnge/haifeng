package com.haifeng.app.service.algorithm.article;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.article.BizArticleQueryDTO;
import com.haifeng.app.vo.algorithm.article.BizArticleDetailVO;
import com.haifeng.app.vo.algorithm.article.BizArticleListVO;

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
}
