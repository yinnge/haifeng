package com.haifeng.app.service.impl.competition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.competition.CompetitionService;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionMapper competitionMapper;
    private final CompetitionDetailMapper competitionDetailMapper;
    private final CompetitionMajorMapper competitionMajorMapper;

    @Override
    public IPage<CompetitionListVO> page(BasePageQueryDTO dto) {
        Page<Competition> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<Competition>()
                .eq(Competition::getIsDeleted, false)
                .orderByAsc(Competition::getId);

        IPage<Competition> entityPage = competitionMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public CompetitionDetailVO detail(Long compId) {
        Competition competition = competitionMapper.selectOne(
                new LambdaQueryWrapper<Competition>()
                        .eq(Competition::getId, compId)
                        .eq(Competition::getIsDeleted, false));
        if (competition == null) {
            log.debug("竞赛不存在或已删除, compId={}", compId);
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }

        CompetitionDetail detail = competitionDetailMapper.findActiveByCompetitionId(compId);

        CompetitionDetailVO.CompetitionDetailVOBuilder builder = CompetitionDetailVO.builder()
                .id(competition.getId())
                .competitionId(competition.getId());

        if (detail != null) {
            builder.basicInfo(detail.getBasicInfo())
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

    @Override
    public IPage<CompetitionMajorBriefVO> majors(Long compId, BasePageQueryDTO dto) {
        Competition competition = competitionMapper.selectOne(
                new LambdaQueryWrapper<Competition>()
                        .eq(Competition::getId, compId)
                        .eq(Competition::getIsDeleted, false));
        if (competition == null) {
            log.debug("竞赛不存在或已删除, compId={}", compId);
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }

        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                competitionMajorMapper.selectMajorsByCompetitionId(page, compId);
        return mapPage.convert(row -> CompetitionMajorBriefVO.builder()
                .majorId(row.get("majorId") != null
                        ? ((Number) row.get("majorId")).longValue() : null)
                .majorName(row.get("majorName") != null
                        ? String.valueOf(row.get("majorName")) : null)
                .build());
    }

    private CompetitionListVO toListVO(Competition e) {
        return CompetitionListVO.builder()
                .id(e.getId())
                .compName(e.getCompName())
                .compLevel(e.getCompLevel())
                .registrationTime(e.getRegistrationTime())
                .build();
    }
}
