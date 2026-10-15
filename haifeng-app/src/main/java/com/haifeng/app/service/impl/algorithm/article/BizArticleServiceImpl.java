package com.haifeng.app.service.impl.algorithm.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.algorithm.article.BizArticleQueryDTO;
import com.haifeng.app.service.algorithm.article.BizArticleService;
import com.haifeng.app.vo.algorithm.article.BizArticleDetailVO;
import com.haifeng.app.vo.algorithm.article.BizArticleListVO;
import com.haifeng.common.entity.article.BizArticle;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.article.BizArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 热点资讯 - C 端 ServiceImpl（免登录公开查询）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizArticleServiceImpl implements BizArticleService {

    /** 已发布状态值（见 t_biz_article.status：0已发布 1下架） */
    private static final int STATUS_PUBLISHED = 0;

    private final BizArticleMapper bizArticleMapper;

    @Override
    public IPage<BizArticleListVO> pagePublished(String moduleName, BizArticleQueryDTO dto) {
        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizArticle::getModuleName, moduleName)
                .eq(StringUtils.hasText(dto.getProvince()), BizArticle::getProvince, dto.getProvince())
                .eq(BizArticle::getStatus, STATUS_PUBLISHED)
                .orderByDesc(BizArticle::getSortOrder)
                .orderByDesc(BizArticle::getCreateTime)
                .orderByDesc(BizArticle::getId);

        return bizArticleMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper)
                .convert(BizArticleServiceImpl::toListVO);
    }

    @Override
    public BizArticleDetailVO getPublishedDetail(String moduleName, Long id) {
        BizArticle article = bizArticleMapper.selectById(id);
        // 模块不归属 / 已下架 / 不存在，统一按 404 处理，不暴露存在性信息
        if (article == null
                || !moduleName.equals(article.getModuleName())
                || article.getStatus() == null
                || article.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(404, "资讯不存在或已下架");
        }
        return toDetailVO(article);
    }

    private static BizArticleListVO toListVO(BizArticle article) {
        return BizArticleListVO.builder()
                .id(article.getId())
                .columnName(article.getColumnName())
                .title(article.getTitle())
                .summary(article.getSummary())
                .build();
    }

    private static BizArticleDetailVO toDetailVO(BizArticle article) {
        return BizArticleDetailVO.builder()
                .province(article.getProvince())
                .year(article.getYear())
                .columnName(article.getColumnName())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .viewCount(article.getViewCount())
                .publishTime(article.getPublishTime())
                .build();
    }
}
