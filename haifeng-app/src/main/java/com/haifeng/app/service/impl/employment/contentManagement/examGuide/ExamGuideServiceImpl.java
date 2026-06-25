package com.haifeng.app.service.impl.employment.contentManagement.examGuide;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGuideServiceImpl implements ExamGuideService {

    private final ExamGuideMapper examGuideMapper;

    @Override
    public IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto) {
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSubtitle())) {
            wrapper.and(w -> {
                boolean hasPrev = false;
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(ExamGuide::getTitle, dto.getTitle());
                    hasPrev = true;
                }
                if (StrUtil.isNotBlank(dto.getSubtitle())) {
                    if (hasPrev) { w.or(); }
                    w.like(ExamGuide::getSubtitle, dto.getSubtitle());
                }
            });
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getGuideCategory()), ExamGuide::getGuideCategory, dto.getGuideCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getGuideType()), ExamGuide::getGuideType, dto.getGuideType());
        wrapper.eq(StrUtil.isNotBlank(dto.getDifficultyLevel()), ExamGuide::getDifficultyLevel, dto.getDifficultyLevel());
        wrapper.eq(StrUtil.isNotBlank(dto.getAuthorTitle()), ExamGuide::getAuthorTitle, dto.getAuthorTitle());
        wrapper.eq(StrUtil.isNotBlank(dto.getAuthorName()), ExamGuide::getAuthorName, dto.getAuthorName());

        wrapper.orderByDesc(ExamGuide::getSortOrder).last("NULLS LAST");
        wrapper.orderByDesc(ExamGuide::getCreatedAt);

        Page<ExamGuide> page = new Page<>(dto.getPage(), dto.getSize());
        examGuideMapper.selectPage(page, wrapper);

        return page.convert(guide -> ExamGuideListVO.builder()
                .id(guide.getId())
                .guideCategory(guide.getGuideCategory())
                .guideType(guide.getGuideType())
                .title(guide.getTitle())
                .subtitle(guide.getSubtitle())
                .tags(guide.getTags())
                .authorName(guide.getAuthorName())
                .authorTitle(guide.getAuthorTitle())
                .build());
    }

    @Override
    public IPage<ExamGuideDetailVO> pageDetail(ExamGuideQueryDTO dto) {
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSubtitle())) {
            wrapper.and(w -> {
                boolean hasPrev = false;
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(ExamGuide::getTitle, dto.getTitle());
                    hasPrev = true;
                }
                if (StrUtil.isNotBlank(dto.getSubtitle())) {
                    if (hasPrev) { w.or(); }
                    w.like(ExamGuide::getSubtitle, dto.getSubtitle());
                }
            });
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getGuideCategory()), ExamGuide::getGuideCategory, dto.getGuideCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getGuideType()), ExamGuide::getGuideType, dto.getGuideType());
        wrapper.eq(StrUtil.isNotBlank(dto.getDifficultyLevel()), ExamGuide::getDifficultyLevel, dto.getDifficultyLevel());
        wrapper.eq(StrUtil.isNotBlank(dto.getAuthorTitle()), ExamGuide::getAuthorTitle, dto.getAuthorTitle());
        wrapper.eq(StrUtil.isNotBlank(dto.getAuthorName()), ExamGuide::getAuthorName, dto.getAuthorName());

        wrapper.orderByDesc(ExamGuide::getSortOrder).last("NULLS LAST");
        wrapper.orderByDesc(ExamGuide::getCreatedAt);

        Page<ExamGuide> page = new Page<>(dto.getPage(), dto.getSize());
        examGuideMapper.selectPage(page, wrapper);

        return page.convert(guide -> ExamGuideDetailVO.builder()
                .id(guide.getId())
                .guideCategory(guide.getGuideCategory())
                .guideType(guide.getGuideType())
                .title(guide.getTitle())
                .subtitle(guide.getSubtitle())
                .coverImage(guide.getCoverImage())
                .iconClass(guide.getIconClass())
                .summary(guide.getSummary())
                .content(guide.getContent())
                .tags(guide.getTags())
                .difficultyLevel(guide.getDifficultyLevel())
                .targetAudience(guide.getTargetAudience())
                .authorName(guide.getAuthorName())
                .authorTitle(guide.getAuthorTitle())
                .isTop(guide.getIsTop())
                .isRecommended(guide.getIsRecommended())
                .sortOrder(guide.getSortOrder())
                .viewCount(guide.getViewCount())
                .likeCount(guide.getLikeCount())
                .createdAt(guide.getCreatedAt())
                .updatedAt(guide.getUpdatedAt())
                .build());
    }

    @Override
    public ExamGuideDetailVO detail(Long id) {
        ExamGuide guide = examGuideMapper.selectById(id);
        if (guide == null || Boolean.TRUE.equals(guide.getIsDeleted())) {
            log.warn("备考指南不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return ExamGuideDetailVO.builder()
                .id(guide.getId())
                .guideCategory(guide.getGuideCategory())
                .guideType(guide.getGuideType())
                .title(guide.getTitle())
                .subtitle(guide.getSubtitle())
                .coverImage(guide.getCoverImage())
                .iconClass(guide.getIconClass())
                .summary(guide.getSummary())
                .content(guide.getContent())
                .tags(guide.getTags())
                .difficultyLevel(guide.getDifficultyLevel())
                .targetAudience(guide.getTargetAudience())
                .authorName(guide.getAuthorName())
                .authorTitle(guide.getAuthorTitle())
                .isTop(guide.getIsTop())
                .isRecommended(guide.getIsRecommended())
                .sortOrder(guide.getSortOrder())
                .viewCount(guide.getViewCount())
                .likeCount(guide.getLikeCount())
                .createdAt(guide.getCreatedAt())
                .updatedAt(guide.getUpdatedAt())
                .build();
    }
}
