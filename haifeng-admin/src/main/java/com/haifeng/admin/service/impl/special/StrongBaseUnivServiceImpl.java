package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.service.special.StrongBaseUnivService;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseUniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseUnivServiceImpl implements StrongBaseUnivService {

    private final StrongBaseUniversityMapper strongBaseUniversityMapper;

    @Override
    public IPage<StrongBaseUnivListVO> page(StrongBaseUnivQueryDTO dto) {
        Page<StrongBaseUniversity> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<StrongBaseUniversity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(StrongBaseUniversity::getUniversityName, dto.getUniversityName());
        }
        if (dto.getIsPilot() != null) {
            wrapper.eq(StrongBaseUniversity::getIsPilot, dto.getIsPilot());
        }
        if (dto.getPilotYear() != null) {
            wrapper.eq(StrongBaseUniversity::getPilotYear, dto.getPilotYear());
        }
        if (dto.getTestBeforeScore() != null) {
            wrapper.eq(StrongBaseUniversity::getTestBeforeScore, dto.getTestBeforeScore());
        }
        wrapper.orderByDesc(StrongBaseUniversity::getCreatedAt);
        IPage<StrongBaseUniversity> result = strongBaseUniversityMapper.selectPage(page, wrapper);
        return result.convert(entity -> StrongBaseUnivListVO.builder()
                .id(entity.getId())
                .universityName(entity.getUniversityName())
                .isPilot(entity.getIsPilot())
                .pilotYear(entity.getPilotYear())
                .testBeforeScore(entity.getTestBeforeScore())
                .build());
    }

    @Override
    public StrongBaseUnivDetailVO detail(Long id) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        return StrongBaseUnivDetailVO.builder()
                .id(entity.getId())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .isPilot(entity.getIsPilot())
                .pilotYear(entity.getPilotYear())
                .officialUrl(entity.getOfficialUrl())
                .signupUrl(entity.getSignupUrl())
                .testBeforeScore(entity.getTestBeforeScore())
                .defaultEntryRatio(entity.getDefaultEntryRatio())
                .defaultAdmissionFormula(entity.getDefaultAdmissionFormula())
                .availableMajors(entity.getAvailableMajors())
                .specialNotes(entity.getSpecialNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StrongBaseUnivAddDTO dto) {
        if (strongBaseUniversityMapper.countByUniversityId(dto.getUniversityId()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该大学的强基配置已存在");
        }
        StrongBaseUniversity entity = StrongBaseUniversity.builder()
                .id(SnowflakeIdGenerator.nextId())
                .universityId(dto.getUniversityId())
                .universityName(dto.getUniversityName())
                .isPilot(dto.getIsPilot() != null ? dto.getIsPilot() : true)
                .pilotYear(dto.getPilotYear())
                .officialUrl(dto.getOfficialUrl())
                .signupUrl(dto.getSignupUrl())
                .testBeforeScore(dto.getTestBeforeScore() != null ? dto.getTestBeforeScore() : false)
                .defaultEntryRatio(dto.getDefaultEntryRatio() != null ? dto.getDefaultEntryRatio() : "1:5")
                .defaultAdmissionFormula(dto.getDefaultAdmissionFormula() != null ? dto.getDefaultAdmissionFormula() : "高考成绩×85%+校测成绩×15%")
                .availableMajors(dto.getAvailableMajors())
                .specialNotes(dto.getSpecialNotes())
                .build();
        strongBaseUniversityMapper.insert(entity);
        log.info("新增强基院校配置: universityId={}, universityName={}", dto.getUniversityId(), dto.getUniversityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StrongBaseUnivAddDTO dto) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        if (!Objects.equals(entity.getUniversityId(), dto.getUniversityId()) && strongBaseUniversityMapper.countByUniversityIdExclude(dto.getUniversityId(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该大学的强基配置已存在");
        }
        entity.setUniversityId(dto.getUniversityId());
        entity.setUniversityName(dto.getUniversityName());
        if (dto.getIsPilot() != null) entity.setIsPilot(dto.getIsPilot());
        if (dto.getPilotYear() != null) entity.setPilotYear(dto.getPilotYear());
        if (dto.getOfficialUrl() != null) entity.setOfficialUrl(dto.getOfficialUrl());
        if (dto.getSignupUrl() != null) entity.setSignupUrl(dto.getSignupUrl());
        if (dto.getTestBeforeScore() != null) entity.setTestBeforeScore(dto.getTestBeforeScore());
        if (dto.getDefaultEntryRatio() != null) entity.setDefaultEntryRatio(dto.getDefaultEntryRatio());
        if (dto.getDefaultAdmissionFormula() != null) entity.setDefaultAdmissionFormula(dto.getDefaultAdmissionFormula());
        if (dto.getAvailableMajors() != null) entity.setAvailableMajors(dto.getAvailableMajors());
        if (dto.getSpecialNotes() != null) entity.setSpecialNotes(dto.getSpecialNotes());
        strongBaseUniversityMapper.updateById(entity);
        log.info("修改强基院校配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        strongBaseUniversityMapper.deleteById(id);
        log.info("删除强基院校配置: id={}, universityId={}, universityName={}", id, entity.getUniversityId(), entity.getUniversityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        strongBaseUniversityMapper.deleteBatchIds(ids);
        log.info("批量删除强基院校配置: count={}", ids.size());
    }
}
