package com.haifeng.admin.service.impl.employment.contentManagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideAddDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideUpdateDTO;
import com.haifeng.admin.service.employment.contentManagement.ExamGuideService;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideListVO;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamGuideServiceImpl implements ExamGuideService {

    private final ExamGuideMapper examGuideMapper;

    @Override
    public IPage<ExamGuideListVO> page(ExamGuideQueryDTO dto) {
        Page<ExamGuide> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<ExamGuide> examGuidePage = examGuideMapper.selectGuidePage(page,
                dto.getTitle(), dto.getSubtitle(), dto.getGuideCategory(), dto.getGuideType(), dto.getIsTop(), dto.getStatus());

        return examGuidePage.convert(examGuide -> {
            ExamGuideListVO vo = new ExamGuideListVO();
            BeanUtils.copyProperties(examGuide, vo);
            return vo;
        });
    }

    @Override
    public ExamGuideDetailVO detail(Long id) {
        ExamGuide examGuide = examGuideMapper.selectGuideById(id);
        if (examGuide == null) {
            throw new BusinessException(404, "备考指南不存在");
        }
        ExamGuideDetailVO vo = new ExamGuideDetailVO();
        BeanUtils.copyProperties(examGuide, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ExamGuideAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        ExamGuide entity = ExamGuide.builder()
                .id(SnowflakeIdGenerator.nextId())
                .guideCategory(dto.getGuideCategory())
                .guideType(dto.getGuideType())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .coverImage(dto.getCoverImage())
                .iconClass(dto.getIconClass())
                .summary(dto.getSummary())
                .content(dto.getContent())
                .tags(dto.getTags())
                .difficultyLevel(dto.getDifficultyLevel())
                .targetAudience(dto.getTargetAudience())
                .authorName(dto.getAuthorName())
                .authorTitle(dto.getAuthorTitle())
                .isTop(dto.getIsTop())
                .isRecommended(dto.getIsRecommended())
                .viewCount(0)
                .likeCount(0)
                .sortOrder(dto.getSortOrder())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        examGuideMapper.insert(entity);
        log.info("新增备考指南成功: id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ExamGuideUpdateDTO dto) {
        ExamGuide examGuide = examGuideMapper.selectGuideById(id);
        if (examGuide == null) {
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
        examGuideMapper.updateGuide(examGuide);
        log.info("更新备考指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 物理删除不受逻辑删除过滤，直接删除并检查影响行数（禁用记录也应可删除）
        int deleted = examGuideMapper.physicalDeleteById(id);
        if (deleted == 0) {
            throw new BusinessException(404, "备考指南不存在");
        }
        log.info("物理删除备考指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        // 用自定义 @Update 直写 is_deleted：MP 的 update(wrapper)/updateById 都会自动注入
        // WHERE is_deleted=false，导致已禁用记录（is_deleted=true）无法更新（0 行）误报 404
        // status 语义与查询一致：0=启用（is_deleted=false），1=禁用（is_deleted=true）
        int updated = examGuideMapper.updateIsDeleted(id, status != 0);
        if (updated == 0) {
            throw new BusinessException(404, "备考指南不存在");
        }
        log.info("更新备考指南状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int deleted = examGuideMapper.physicalDeleteBatchIds(ids);
        log.info("批量物理删除备考指南成功: requested={}, actual={}", ids.size(), deleted);
    }

}
