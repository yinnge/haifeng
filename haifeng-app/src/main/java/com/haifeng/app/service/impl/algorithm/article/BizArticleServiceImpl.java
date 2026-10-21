package com.haifeng.app.service.impl.algorithm.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.algorithm.article.BizArticleQueryDTO;
import com.haifeng.app.dto.algorithm.article.BizArticleSearchDTO;
import com.haifeng.app.service.algorithm.article.BizArticleService;
import com.haifeng.app.util.algorithm.article.TitleAlignMatcher;
import com.haifeng.app.vo.algorithm.article.BizArticleDetailVO;
import com.haifeng.app.vo.algorithm.article.BizArticleListVO;
import com.haifeng.app.vo.algorithm.article.BizArticleSearchItemVO;
import com.haifeng.common.entity.article.BizArticle;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.article.BizArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点资讯 - C 端 ServiceImpl（免登录公开查询）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizArticleServiceImpl implements BizArticleService {

    /** 已发布状态值（见 t_biz_article.status：0已发布 1下架） */
    private static final int STATUS_PUBLISHED = 0;
    /** 搜索候选防御性上限（selectList 一次拉取的最大行数） */
    private static final int SEARCH_MAX_CANDIDATES = 2000;
    /** 搜索默认返回条数 */
    private static final int SEARCH_DEFAULT_LIMIT = 10;

    private final BizArticleMapper bizArticleMapper;

    @Override
    public IPage<BizArticleListVO> pagePublished(String moduleName, BizArticleQueryDTO dto) {
        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizArticle::getModuleName, moduleName)
                // C 端按档案省份查询资讯：命中「指定省份」或「全国(national)」或「未设省份(admin 清空)」三类
                .and(StringUtils.hasText(dto.getProvince()), w -> w
                        .eq(BizArticle::getProvince, dto.getProvince())
                        .or().eq(BizArticle::getProvince, "全国")
                        .or().isNull(BizArticle::getProvince))
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

    @Override
    public List<BizArticleSearchItemVO> searchPublished(String moduleName, BizArticleSearchDTO dto) {
        int limit = dto.getLimit() == null
                ? SEARCH_DEFAULT_LIMIT
                : Math.min(Math.max(dto.getLimit(), 1), SEARCH_DEFAULT_LIMIT);
        String rawProvince = dto.getProvince() == null ? "" : dto.getProvince().trim();
        // 空白或字面量「全国」= 不限省份
        String province = (rawProvince.isEmpty() || "全国".equals(rawProvince)) ? null : rawProvince;

        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BizArticle::getId, BizArticle::getColumnName, BizArticle::getProvince,
                        BizArticle::getTitle, BizArticle::getSortOrder, BizArticle::getPublishTime)
                // moduleName 为 null 时跨全部类型查询；非空时仅查该类型
                .eq(moduleName != null, BizArticle::getModuleName, moduleName)
                .eq(BizArticle::getStatus, STATUS_PUBLISHED)
                .eq(province != null, BizArticle::getProvince, province)
                .orderByDesc(BizArticle::getSortOrder)
                .orderByDesc(BizArticle::getPublishTime)
                .orderByDesc(BizArticle::getId)
                .last("LIMIT " + SEARCH_MAX_CANDIDATES);

        // SQL 已按权重→时间排好序：按序内存匹配、命中 limit 条即退出，最终顺序即 SQL 顺序
        List<BizArticle> candidates = bizArticleMapper.selectList(wrapper);
        List<BizArticleSearchItemVO> result = new ArrayList<>();
        for (BizArticle article : candidates) {
            TitleAlignMatcher.AlignResult align = TitleAlignMatcher.align(dto.getKeyword(), article.getTitle());
            if (!align.accepted()) {
                continue;
            }
            List<BizArticleSearchItemVO.HighlightSpan> spans = align.getSpans().stream()
                    .map(s -> BizArticleSearchItemVO.HighlightSpan.builder()
                            .start(s.getStart())
                            .end(s.getEnd())
                            .build())
                    .collect(Collectors.toList());
            result.add(BizArticleSearchItemVO.builder()
                    .id(article.getId())
                    .columnName(article.getColumnName())
                    .province(article.getProvince())
                    .title(article.getTitle())
                    .highlight(spans)
                    .build());
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
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
