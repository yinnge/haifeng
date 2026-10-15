package com.haifeng.admin.service.impl.fileload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.fileload.BizArticleAddDTO;
import com.haifeng.admin.dto.fileload.BizArticleQueryDTO;
import com.haifeng.admin.dto.fileload.BizArticleUpdateDTO;
import com.haifeng.admin.service.fileload.BizArticleService;
import com.haifeng.admin.vo.fileload.BizArticleDetailVO;
import com.haifeng.admin.vo.fileload.BizArticleListVO;
import com.haifeng.common.entity.article.BizArticle;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.article.BizArticleMapper;
import com.haifeng.common.security.AuthUser;
import com.haifeng.common.util.HtmlSanitizerUtil;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 热点资讯 ServiceImpl
 * <p>t_biz_article 无逻辑删除列/乐观锁列，直接使用 MyBatis-Plus 标准 CRUD。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizArticleServiceImpl implements BizArticleService {

    private final BizArticleMapper bizArticleMapper;

    @Override
    public IPage<BizArticleListVO> page(BizArticleQueryDTO dto) {
        LambdaQueryWrapper<BizArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getModuleName()), BizArticle::getModuleName, dto.getModuleName())
                .eq(StringUtils.hasText(dto.getProvince()), BizArticle::getProvince, dto.getProvince())
                .eq(dto.getYear() != null, BizArticle::getYear, dto.getYear())
                .eq(StringUtils.hasText(dto.getColumnName()), BizArticle::getColumnName, dto.getColumnName())
                .like(StringUtils.hasText(dto.getTitle()), BizArticle::getTitle, dto.getTitle())
                .orderByDesc(BizArticle::getSortOrder)
                .orderByDesc(BizArticle::getCreateTime)
                .orderByDesc(BizArticle::getId);

        return bizArticleMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()), wrapper)
                .convert(this::toListVO);
    }

    @Override
    public BizArticleDetailVO detail(Long id) {
        BizArticle article = bizArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(404, "资讯不存在");
        }
        return toDetailVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(BizArticleAddDTO dto) {
        BizArticle entity = new BizArticle();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        // 富文本正文入库前白名单清洗，防存储型 XSS
        entity.setContent(HtmlSanitizerUtil.sanitizeRichText(dto.getContent()));
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        entity.setCreateBy(currentUser != null ? currentUser.getUserId() : null);

        bizArticleMapper.insert(entity);
        log.info("新增热点资讯成功，id={}, module={}, title={}", entity.getId(), entity.getModuleName(), entity.getTitle());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, BizArticleUpdateDTO dto) {
        BizArticle existing = bizArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "资讯不存在");
        }

        BizArticle entity = new BizArticle();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        if (entity.getStatus() == null) {
            entity.setStatus(existing.getStatus());
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(existing.getSortOrder());
        }
        // 富文本正文入库前白名单清洗，防存储型 XSS
        entity.setContent(HtmlSanitizerUtil.sanitizeRichText(dto.getContent()));
        // update_time 走 MetaObjectHandler INSERT_UPDATE 自动填充

        bizArticleMapper.updateById(entity);
        log.info("修改热点资讯成功，id={}, module={}, title={}", id, entity.getModuleName(), entity.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BizArticle existing = bizArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "资讯不存在");
        }
        // 表无逻辑删除列（无 @TableLogic），deleteById 即物理删除
        bizArticleMapper.deleteById(id);
        log.info("删除热点资讯成功，id={}, title={}", id, existing.getTitle());
    }

    private BizArticleListVO toListVO(BizArticle article) {
        return BizArticleListVO.builder()
                .id(article.getId())
                .moduleName(article.getModuleName())
                .province(article.getProvince())
                .year(article.getYear())
                .columnName(article.getColumnName())
                .title(article.getTitle())
                .summary(article.getSummary())
                .status(article.getStatus())
                .sortOrder(article.getSortOrder())
                .viewCount(article.getViewCount())
                .publishTime(article.getPublishTime())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private BizArticleDetailVO toDetailVO(BizArticle article) {
        return BizArticleDetailVO.builder()
                .id(article.getId())
                .moduleName(article.getModuleName())
                .province(article.getProvince())
                .year(article.getYear())
                .columnName(article.getColumnName())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .status(article.getStatus())
                .sortOrder(article.getSortOrder())
                .viewCount(article.getViewCount())
                .publishTime(article.getPublishTime())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }
}
