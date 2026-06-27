package com.haifeng.app.service.impl.employment.contentManagement.examGuide;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import java.util.List;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper;
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
        wrapper.orderByDesc(ExamGuide::getSortOrder).last("NULLS LAST");
        wrapper.orderByDesc(ExamGuide::getCreatedAt);

        List<ExamGuide> list = examGuideMapper.selectList(wrapper);
        return list.stream().map(this::convertToDetailVO).toList();
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
