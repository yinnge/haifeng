package com.haifeng.admin.service.impl.employment.contentManagement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideUpdateDTO;
import com.haifeng.admin.excel.employment.contentManagement.ExamGuideExcelDTO;
import com.haifeng.admin.service.employment.contentManagement.ExamGuideService;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideListVO;
import com.haifeng.common.entity.employment.contentManagement.ExamGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.contentManagement.ExamGuideMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    public void update(Long id, ExamGuideUpdateDTO dto) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null || examGuide.getIsDeleted()) {
            throw new BusinessException(404, "备考指南不存在");
        }
        BeanUtils.copyProperties(dto, examGuide);
        examGuide.setUpdatedAt(OffsetDateTime.now());
        examGuideMapper.updateById(examGuide);
        log.info("更新备考指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null) {
            throw new BusinessException(404, "备考指南不存在");
        }
        examGuideMapper.deleteById(id);
        log.info("硬删除备考指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ExamGuide examGuide = examGuideMapper.selectById(id);
        if (examGuide == null) {
            throw new BusinessException(404, "备考指南不存在");
        }
        examGuide.setIsDeleted(status == 0);
        examGuide.setUpdatedAt(OffsetDateTime.now());
        examGuideMapper.updateById(examGuide);
        log.info("更新备考指南状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            ExamGuide examGuide = examGuideMapper.selectById(id);
            if (examGuide != null) {
                examGuideMapper.deleteById(id);
            }
        }
        log.info("批量删除备考指南成功: count={}", ids.size());
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<ExamGuideExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (ExamGuideExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getGuideCategory())) {
                errors.add("指南类别不能为空");
            }
            if (!StringUtils.hasText(dto.getTitle())) {
                errors.add("标题不能为空");
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ");
                errorMsg.append(String.join("; ", errors));
                errorMsg.append("\n");
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<ExamGuideExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (ExamGuideExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getGuideCategory())) errors.add("指南类别不能为空");
            if (!StringUtils.hasText(dto.getTitle())) errors.add("标题不能为空");
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (ExamGuideExcelDTO dto : list) {
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
                    .tags(StringUtils.hasText(dto.getTags()) ? dto.getTags().split(",") : new String[0])
                    .difficultyLevel(dto.getDifficultyLevel())
                    .targetAudience(dto.getTargetAudience())
                    .authorName(dto.getAuthorName())
                    .authorTitle(dto.getAuthorTitle())
                    .isTop(dto.getIsTop() != null && dto.getIsTop())
                    .isRecommended(dto.getIsRecommended() != null && dto.getIsRecommended())
                    .sortOrder(dto.getSortOrder())
                    .viewCount(0)
                    .likeCount(0)
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            examGuideMapper.insert(entity);
        }
        log.info("导入备考指南成功: count={}", list.size());
    }

    private List<ExamGuideExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(ExamGuideExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
