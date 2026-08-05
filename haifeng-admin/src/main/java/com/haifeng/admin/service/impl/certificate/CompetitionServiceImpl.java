package com.haifeng.admin.service.impl.certificate;

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
        IPage<Competition> result = competitionMapper.selectPageIgnoreLogicDelete(
                page, queryDTO.getIsDeleted(), queryDTO.getCompName(), queryDTO.getCompLevel());
        return result.convert(this::convertToListVO);
    }

    @Override
    public CompetitionDetailVO getCompetitionDetail(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
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
    public void enableCompetition(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }
        if (!competition.getIsDeleted()) {
            throw new BusinessException(400, "竞赛已启用");
        }

        competitionMapper.updateIsDeletedById(id, false);

        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            competitionDetailMapper.updateIsDeletedById(detail.getId(), false);
        }

        // 同步恢复该竞赛的关联专业（启用后用户端可再次看到）
        competitionMajorMapper.enableByCompetitionId(id);

        log.info("启用竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCompetition(CompetitionUpdateDTO updateDTO) {
        Competition existing = competitionMapper.findByIdIgnoreLogicDelete(updateDTO.getId());
        if (existing == null) {
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
        existing.setCompName(updateDTO.getCompName());
        existing.setCompLevel(updateDTO.getCompLevel());
        existing.setRegistrationTime(updateDTO.getRegistrationTime());
        competitionMapper.updateIgnoreLogicDelete(existing);

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
                competitionDetailMapper.updateIgnoreLogicDelete(detail);
            }
        }

        log.info("更新竞赛成功，id={}", updateDTO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteCompetition(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }
        if (competition.getIsDeleted()) {
            throw new BusinessException(400, "竞赛已禁用");
        }

        // 禁用主表
        competitionMapper.updateIsDeletedById(id, true);

        // 禁用详情表
        CompetitionDetail detail = competitionDetailMapper.findByCompetitionId(id);
        if (detail != null) {
            competitionDetailMapper.updateIsDeletedById(detail.getId(), true);
        }

        // 禁用竞赛-专业关联
        competitionMajorMapper.softDeleteByCompetitionId(id);

        log.info("禁用竞赛成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCompetition(Long id) {
        Competition competition = competitionMapper.findByIdIgnoreLogicDelete(id);
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在");
        }

        // 删除关联的专业记录
        competitionMajorMapper.deleteByCompetitionId(id);

        // 删除详情记录
        competitionDetailMapper.deleteByCompetitionId(id);

        // 物理删除主表记录
        competitionMapper.physicalDeleteById(id);

        log.info("删除竞赛成功，id={}", id);
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
        competitionMapper.physicalDeleteBatchByIds(ids);

        log.info("批量删除竞赛成功，ids={}", ids);
    }

    private CompetitionListVO convertToListVO(Competition competition) {
        return CompetitionListVO.builder()
                .id(competition.getId())
                .compName(competition.getCompName())
                .compLevel(competition.getCompLevel())
                .registrationTime(competition.getRegistrationTime())
                .isDeleted(competition.getIsDeleted())
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
