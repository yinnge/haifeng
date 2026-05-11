package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionDetailDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.service.certificate.CompetitionService;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
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
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionMapper competitionMapper;
    private final CompetitionDetailMapper competitionDetailMapper;
    private final CompetitionMajorMapper competitionMajorMapper;

    @Override
    public IPage<CompetitionListVO> listCompetitions(CompetitionQueryDTO queryDTO) {
        Page<Competition> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getIsDeleted, false);

        if (StringUtils.hasText(queryDTO.getCompName())) {
            wrapper.like(Competition::getCompName, queryDTO.getCompName());
        }
        if (StringUtils.hasText(queryDTO.getCompLevel())) {
            wrapper.eq(Competition::getCompLevel, queryDTO.getCompLevel());
        }

        wrapper.orderByDesc(Competition::getCreatedAt);

        IPage<Competition> result = competitionMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public CompetitionDetailVO getCompetitionDetail(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null || competition.getIsDeleted()) {
            throw new BusinessException(404, "竞赛不存在");
        }

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);

        return convertToDetailVO(competition, detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCompetition(CompetitionAddDTO addDTO) {
        // 检查竞赛名称是否重复
        if (competitionMapper.existsByCompName(addDTO.getCompName())) {
            throw new BusinessException(400, "竞赛名称已存在");
        }

        // 创建竞赛主表记录
        Competition competition = new Competition();
        BeanUtils.copyProperties(addDTO, competition);
        competition.setIsDeleted(false);
        competitionMapper.insert(competition);

        // 创建详情记录
        CompetitionDetail detail = new CompetitionDetail();
        detail.setCompetitionId(competition.getId());
        detail.setIsDeleted(false);

        if (addDTO.getDetail() != null) {
            CompetitionDetailDTO detailDTO = addDTO.getDetail();
            detail.setBasicInfo(detailDTO.getBasicInfo());
            detail.setAwards(detailDTO.getAwards());
            detail.setBackground(detailDTO.getBackground());
            detail.setPurposes(detailDTO.getPurposes());
            detail.setCompetitionRules(detailDTO.getCompetitionRules());
            detail.setScoringCriteria(detailDTO.getScoringCriteria());
            detail.setNotices(detailDTO.getNotices());
            detail.setProcessGuide(detailDTO.getProcessGuide());
            detail.setAwardsDisplay(detailDTO.getAwardsDisplay());
        }

        competitionDetailMapper.insert(detail);

        log.info("新增竞赛成功，id={}, compName={}", competition.getId(), competition.getCompName());
        return competition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompetition(CompetitionUpdateDTO updateDTO) {
        Competition existing = competitionMapper.selectById(updateDTO.getId());
        if (existing == null || existing.getIsDeleted()) {
            throw new BusinessException(404, "竞赛不存在");
        }

        // 如果修改了名称，检查是否重复
        if (StringUtils.hasText(updateDTO.getCompName())
                && !updateDTO.getCompName().equals(existing.getCompName())) {
            if (competitionMapper.existsByCompName(updateDTO.getCompName())) {
                throw new BusinessException(400, "竞赛名称已存在");
            }
        }

        // 更新主表
        BeanUtils.copyProperties(updateDTO, existing, "id");
        competitionMapper.updateById(existing);

        // 更新详情表
        if (updateDTO.getDetail() != null) {
            CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(updateDTO.getId());
            if (detail != null) {
                CompetitionDetailDTO detailDTO = updateDTO.getDetail();
                detail.setBasicInfo(detailDTO.getBasicInfo());
                detail.setAwards(detailDTO.getAwards());
                detail.setBackground(detailDTO.getBackground());
                detail.setPurposes(detailDTO.getPurposes());
                detail.setCompetitionRules(detailDTO.getCompetitionRules());
                detail.setScoringCriteria(detailDTO.getScoringCriteria());
                detail.setNotices(detailDTO.getNotices());
                detail.setProcessGuide(detailDTO.getProcessGuide());
                detail.setAwardsDisplay(detailDTO.getAwardsDisplay());
                competitionDetailMapper.updateById(detail);
            }
        }

        log.info("更新竞赛成功，id={}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteCompetition(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null || competition.getIsDeleted()) {
            throw new BusinessException(404, "竞赛不存在");
        }

        // 软删除主表
        competition.setIsDeleted(true);
        competitionMapper.updateById(competition);

        // 软删除详情表
        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            detail.setIsDeleted(true);
            competitionDetailMapper.updateById(detail);
        }

        log.info("软删除竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCompetition(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        // 删除关联的专业记录
        competitionMajorMapper.deleteByCompetitionId(id);

        // 删除详情记录
        competitionDetailMapper.deleteByCompetitionId(id);

        // 删除主表记录
        competitionMapper.deleteById(id);

        log.info("硬删除竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDeleteCompetitions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            competitionMajorMapper.deleteByCompetitionId(id);
            competitionDetailMapper.deleteByCompetitionId(id);
        }
        competitionMapper.deleteBatchIds(ids);

        log.info("批量硬删除竞赛成功，ids={}", ids);
    }

    private CompetitionListVO convertToListVO(Competition competition) {
        return CompetitionListVO.builder()
                .id(competition.getId())
                .compName(competition.getCompName())
                .compLevel(competition.getCompLevel())
                .registrationTime(competition.getRegistrationTime())
                .createdAt(competition.getCreatedAt())
                .updatedAt(competition.getUpdatedAt())
                .build();
    }

    private CompetitionDetailVO convertToDetailVO(Competition competition, CompetitionDetail detail) {
        CompetitionDetailVO.CompetitionDetailVOBuilder builder = CompetitionDetailVO.builder()
                .id(competition.getId())
                .compName(competition.getCompName())
                .compLevel(competition.getCompLevel())
                .registrationTime(competition.getRegistrationTime())
                .createdAt(competition.getCreatedAt())
                .updatedAt(competition.getUpdatedAt());

        if (detail != null) {
            builder.detailId(detail.getId())
                   .basicInfo(detail.getBasicInfo())
                   .awards(detail.getAwards())
                   .background(detail.getBackground())
                   .purposes(detail.getPurposes())
                   .competitionRules(detail.getCompetitionRules())
                   .scoringCriteria(detail.getScoringCriteria())
                   .notices(detail.getNotices())
                   .processGuide(detail.getProcessGuide())
                   .awardsDisplay(detail.getAwardsDisplay());
        }

        return builder.build();
    }
}
