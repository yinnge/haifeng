package com.haifeng.app.service.impl.employment.contentManagement.examGuide;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import java.util.List;
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
    public List<ExamGuideDetailVO> listByCategoryAndType(String guideCategory, String guideType) {
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getIsDeleted, false);
        wrapper.eq(ExamGuide::getGuideCategory, guideCategory);
        wrapper.eq(StrUtil.isNotBlank(guideType), ExamGuide::getGuideType, guideType);
        wrapper.orderByDesc(ExamGuide::getSortOrder, ExamGuide::getCreatedAt);

        List<ExamGuide> list = examGuideMapper.selectList(wrapper);
        return list.stream().map(this::convertToDetailVO).toList();
    }

    @Override
    public IPage<ExamGuideDetailVO> page(ExamGuideQueryDTO dto) {
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getIsDeleted, false);
        wrapper.eq(StrUtil.isNotBlank(dto.getGuideCategory()), ExamGuide::getGuideCategory, dto.getGuideCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getGuideType()), ExamGuide::getGuideType, dto.getGuideType());
        wrapper.eq(StrUtil.isNotBlank(dto.getDifficultyLevel()), ExamGuide::getDifficultyLevel, dto.getDifficultyLevel());

        // 关键词搜索：前端把 keyword 同时映射为 title / subtitle / authorTitle / authorName，OR 匹配
        if (StrUtil.isNotBlank(dto.getTitle()) || StrUtil.isNotBlank(dto.getSubtitle())
                || StrUtil.isNotBlank(dto.getAuthorTitle()) || StrUtil.isNotBlank(dto.getAuthorName())) {
            wrapper.and(w -> {
                if (StrUtil.isNotBlank(dto.getTitle())) {
                    w.like(ExamGuide::getTitle, dto.getTitle());
                }
                if (StrUtil.isNotBlank(dto.getSubtitle())) {
                    w.or().like(ExamGuide::getSubtitle, dto.getSubtitle());
                }
                if (StrUtil.isNotBlank(dto.getAuthorTitle())) {
                    w.or().like(ExamGuide::getAuthorTitle, dto.getAuthorTitle());
                }
                if (StrUtil.isNotBlank(dto.getAuthorName())) {
                    w.or().like(ExamGuide::getAuthorName, dto.getAuthorName());
                }
            });
        }

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<ExamGuide> page = new Page<>(dto.getPage(), dto.getSize());
        examGuideMapper.selectPage(page, wrapper);

        return page.convert(this::convertToDetailVO);
    }

    @Override
    public ExamGuideDetailVO detail(Long id) {
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getId, id);
        wrapper.eq(ExamGuide::getIsDeleted, false);
        ExamGuide guide = examGuideMapper.selectOne(wrapper);
        if (guide == null) {
            log.warn("备考指南不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToDetailVO(guide);
    }

    private ExamGuideDetailVO convertToDetailVO(ExamGuide guide) {
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
