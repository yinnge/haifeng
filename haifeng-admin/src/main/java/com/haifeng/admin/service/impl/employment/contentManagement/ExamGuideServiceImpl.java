package com.haifeng.admin.service.impl.employment.contentManagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideUpdateDTO;
import com.haifeng.admin.service.employment.contentManagement.ExamGuideService;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideListVO;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGuideServiceImpl implements ExamGuideService {

    private final ExamGuideMapper examGuideMapper;

    @Override
    public IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto) {
        Page<ExamGuide> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ExamGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamGuide::getIsDeleted, false);

        if (StringUtils.hasText(dto.getGuideCategory())) {
            wrapper.eq(ExamGuide::getGuideCategory, dto.getGuideCategory());
        }
        if (StringUtils.hasText(dto.getGuideType())) {
            wrapper.eq(ExamGuide::getGuideType, dto.getGuideType());
        }
        if (dto.getIsTop() != null) {
            wrapper.eq(ExamGuide::getIsTop, dto.getIsTop());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.like(ExamGuide::getTitle, dto.getTitle());
        }
        if (StringUtils.hasText(dto.getSubtitle())) {
            wrapper.like(ExamGuide::getSubtitle, dto.getSubtitle());
        }

        wrapper.orderByDesc(ExamGuide::getSortOrder).orderByDesc(ExamGuide::getCreatedAt);

        IPage<ExamGuide> examGuidePage = examGuideMapper.selectPage(page, wrapper);

        return examGuidePage.convert(examGuide -> {
            ExamGuideListVO vo = new ExamGuideListVO();
            BeanUtils.copyProperties(examGuide, vo);
            return vo;
        });
    }

    @Override
    public ExamGuideDetailVO detail(Long id) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null || examGuide.getIsDeleted()) {
            throw new BusinessException(404, "备考指南不存在");
        }
        ExamGuideDetailVO vo = new ExamGuideDetailVO();
        BeanUtils.copyProperties(examGuide, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ExamGuideUpdateDTO dto) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null || examGuide.getIsDeleted()) {
            throw new BusinessException(404, "备考指南不存在");
        }
        if (dto.getGuideCategory() != null) examGuide.setGuideCategory(dto.getGuideCategory());
        if (dto.getGuideType() != null) examGuide.setGuideType(dto.getGuideType());
        if (dto.getTitle() != null) examGuide.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) examGuide.setSubtitle(dto.getSubtitle());
        if (dto.getCoverImage() != null) examGuide.setCoverImage(dto.getCoverImage());
        if (dto.getIconClass() != null) examGuide.setIconClass(dto.getIconClass());
        if (dto.getSummary() != null) examGuide.setSummary(dto.getSummary());
        if (dto.getContent() != null) examGuide.setContent(dto.getContent());
        if (dto.getTags() != null) examGuide.setTags(dto.getTags());
        if (dto.getDifficultyLevel() != null) examGuide.setDifficultyLevel(dto.getDifficultyLevel());
        if (dto.getTargetAudience() != null) examGuide.setTargetAudience(dto.getTargetAudience());
        if (dto.getAuthorName() != null) examGuide.setAuthorName(dto.getAuthorName());
        if (dto.getAuthorTitle() != null) examGuide.setAuthorTitle(dto.getAuthorTitle());
        if (dto.getIsTop() != null) examGuide.setIsTop(dto.getIsTop());
        if (dto.getIsRecommended() != null) examGuide.setIsRecommended(dto.getIsRecommended());
        if (dto.getSortOrder() != null) examGuide.setSortOrder(dto.getSortOrder());
        examGuideMapper.updateById(examGuide);
        log.info("更新备考指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null || examGuide.getIsDeleted()) {
            throw new BusinessException(404, "备考指南不存在");
        }
        examGuide.setIsDeleted(true);
        examGuideMapper.updateById(examGuide);
        log.info("软删除备考指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null) {
            throw new BusinessException(404, "备考指南不存在");
        }
        examGuide.setIsDeleted(status == 0);
        examGuideMapper.updateById(examGuide);
        log.info("更新备考指南状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        examGuideMapper.update(null,
                Wrappers.lambdaUpdate(ExamGuide.class)
                        .set(ExamGuide::getIsDeleted, true)
                        .in(ExamGuide::getId, ids));
        log.info("批量删除备考指南成功: count={}", ids.size());
    }

}
