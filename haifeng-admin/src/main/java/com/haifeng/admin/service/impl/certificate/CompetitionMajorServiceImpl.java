package com.haifeng.admin.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.certificate.CompetitionMajorAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorQueryDTO;
import com.haifeng.admin.service.certificate.CompetitionMajorService;
import com.haifeng.admin.vo.certificate.CompetitionMajorVO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionMajor;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionMajorServiceImpl implements CompetitionMajorService {

    private final CompetitionMajorMapper competitionMajorMapper;
    private final CompetitionMapper competitionMapper;
    private final MajorMapper majorMapper;

    @Override
    public IPage<CompetitionMajorVO> listCompetitionMajors(CompetitionMajorQueryDTO queryDTO) {
        Page<CompetitionMajor> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<CompetitionMajor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionMajor::getIsDeleted, false);

        if (queryDTO.getCompetitionId() != null) {
            wrapper.eq(CompetitionMajor::getCompetitionId, queryDTO.getCompetitionId());
        }
        if (queryDTO.getMajorId() != null) {
            wrapper.eq(CompetitionMajor::getMajorId, queryDTO.getMajorId());
        }
        if (StringUtils.hasText(queryDTO.getCompetitionName())) {
            wrapper.like(CompetitionMajor::getCompetitionName, queryDTO.getCompetitionName());
        }
        if (StringUtils.hasText(queryDTO.getMajorName())) {
            wrapper.like(CompetitionMajor::getMajorName, queryDTO.getMajorName());
        }

        wrapper.orderByDesc(CompetitionMajor::getCreatedAt);

        IPage<CompetitionMajor> result = competitionMajorMapper.selectPage(page, wrapper);

        return result.convert(this::convertToVO);
    }

    @Override
    public List<CompetitionMajorVO> listByCompetitionId(Long competitionId) {
        LambdaQueryWrapper<CompetitionMajor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionMajor::getCompetitionId, competitionId);
        wrapper.eq(CompetitionMajor::getIsDeleted, false);
        wrapper.orderByDesc(CompetitionMajor::getCreatedAt);

        List<CompetitionMajor> list = competitionMajorMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CompetitionMajorVO> listByMajorId(Long majorId) {
        LambdaQueryWrapper<CompetitionMajor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitionMajor::getMajorId, majorId);
        wrapper.eq(CompetitionMajor::getIsDeleted, false);
        wrapper.orderByDesc(CompetitionMajor::getCreatedAt);

        List<CompetitionMajor> list = competitionMajorMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCompetitionMajor(CompetitionMajorAddDTO addDTO) {
        // 通过名称查找竞赛
        Competition competition = competitionMapper.findByCompName(addDTO.getCompetitionName());
        if (competition == null) {
            throw new BusinessException(404, "竞赛不存在：" + addDTO.getCompetitionName());
        }

        // 通过名称查找专业
        Major major = majorMapper.findByMajorName(addDTO.getMajorName());
        if (major == null) {
            throw new BusinessException(404, "专业不存在：" + addDTO.getMajorName());
        }

        // 检查关联是否已存在
        if (competitionMajorMapper.existsByCompetitionIdAndMajorId(competition.getId(), major.getId())) {
            throw new BusinessException(400, "该竞赛与专业的关联已存在");
        }

        CompetitionMajor competitionMajor = new CompetitionMajor();
        competitionMajor.setCompetitionId(competition.getId());
        competitionMajor.setMajorId(major.getId());
        competitionMajor.setCompetitionName(competition.getCompName());
        competitionMajor.setMajorName(major.getMajorName());
        competitionMajor.setIsDeleted(false);

        competitionMajorMapper.insert(competitionMajor);

        log.info("新增竞赛-专业关联成功，id={}, competitionId={}, majorId={}",
                competitionMajor.getId(), competition.getId(), major.getId());
        return competitionMajor.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompetitionMajor(Long id) {
        CompetitionMajor existing = competitionMajorMapper.selectById(id);
        if (existing == null || existing.getIsDeleted()) {
            throw new BusinessException(404, "关联记录不存在");
        }

        competitionMajorMapper.softDeleteById(id);
        log.info("软删除竞赛-专业关联成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCompetitionMajors(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            competitionMajorMapper.softDeleteById(id);
        }
        log.info("批量软删除竞赛-专业关联成功，ids={}", ids);
    }

    private CompetitionMajorVO convertToVO(CompetitionMajor entity) {
        return CompetitionMajorVO.builder()
                .id(entity.getId())
                .competitionId(entity.getCompetitionId())
                .majorId(entity.getMajorId())
                .competitionName(entity.getCompetitionName())
                .majorName(entity.getMajorName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
