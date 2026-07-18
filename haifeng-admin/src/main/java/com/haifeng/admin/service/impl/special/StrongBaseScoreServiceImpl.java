package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.service.special.StrongBaseScoreService;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.entity.special.StrongBaseScore;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseScoreMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseScoreServiceImpl implements StrongBaseScoreService {

    private final StrongBaseScoreMapper strongBaseScoreMapper;

    @Override
    public IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto) {
        Page<StrongBaseScore> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<StrongBaseScore> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(StrongBaseScore::getUniversityName, dto.getUniversityName());
        }
        if (dto.getYear() != null) {
            wrapper.eq(StrongBaseScore::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(StrongBaseScore::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(StrongBaseScore::getSubjectType, dto.getSubjectType());
        }
        wrapper.orderByDesc(StrongBaseScore::getYear).orderByDesc(StrongBaseScore::getCreatedAt);
        IPage<StrongBaseScore> result = strongBaseScoreMapper.selectPage(page, wrapper);
        return result.convert(entity -> StrongBaseScoreListVO.builder()
                .id(entity.getId())
                .universityName(entity.getUniversityName())
                .year(entity.getYear())
                .province(entity.getProvince())
                .subjectType(entity.getSubjectType())
                .majorName(entity.getMajorName())
                .isActive(entity.getIsActive())
                .build());
    }

    @Override
    public StrongBaseScoreDetailVO detail(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        return StrongBaseScoreDetailVO.builder()
                .id(entity.getId())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .year(entity.getYear())
                .province(entity.getProvince())
                .subjectType(entity.getSubjectType())
                .majorName(entity.getMajorName())
                .majorCode(entity.getMajorCode())
                .entryScore(entity.getEntryScore())
                .entryScoreType(entity.getEntryScoreType())
                .entryFormula(entity.getEntryFormula())
                .entryRatio(entity.getEntryRatio())
                .admissionScore(entity.getAdmissionScore())
                .admissionFormula(entity.getAdmissionFormula())
                .planCount(entity.getPlanCount())
                .admissionCount(entity.getAdmissionCount())
                .remark(entity.getRemark())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StrongBaseScoreAddDTO dto) {
        if (strongBaseScoreMapper.countByUnique(dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getSubjectType(), dto.getMajorName()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该强基计划数据已存在");
        }
        StrongBaseScore entity = StrongBaseScore.builder()
                .id(SnowflakeIdGenerator.nextId())
                .universityId(dto.getUniversityId())
                .universityName(dto.getUniversityName())
                .year(dto.getYear())
                .province(dto.getProvince())
                .subjectType(dto.getSubjectType())
                .majorName(dto.getMajorName())
                .majorCode(dto.getMajorCode())
                .entryScore(dto.getEntryScore())
                .entryScoreType(dto.getEntryScoreType())
                .entryFormula(dto.getEntryFormula())
                .entryRatio(dto.getEntryRatio())
                .admissionScore(dto.getAdmissionScore())
                .admissionFormula(dto.getAdmissionFormula())
                .planCount(dto.getPlanCount())
                .admissionCount(dto.getAdmissionCount())
                .remark(dto.getRemark())
                .isActive(true)
                .build();
        if (entity.getEntryScoreType() == null) {
            entity.setEntryScoreType("高考成绩");
        }
        if (entity.getAdmissionFormula() == null) {
            entity.setAdmissionFormula("高考成绩×85%+校测成绩×15%");
        }
        strongBaseScoreMapper.insert(entity);
        log.info("新增强基计划数据: universityId={}, year={}, province={}, majorName={}", dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getMajorName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StrongBaseScoreAddDTO dto) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        boolean keyChanged = !Objects.equals(entity.getUniversityId(), dto.getUniversityId())
                || !Objects.equals(entity.getYear(), dto.getYear())
                || !Objects.equals(entity.getProvince(), dto.getProvince())
                || !Objects.equals(entity.getSubjectType(), dto.getSubjectType())
                || !Objects.equals(entity.getMajorName(), dto.getMajorName());
        if (keyChanged && strongBaseScoreMapper.countByUniqueExclude(dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getSubjectType(), dto.getMajorName(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该强基计划数据已存在");
        }
        entity.setUniversityId(dto.getUniversityId());
        entity.setUniversityName(dto.getUniversityName());
        entity.setYear(dto.getYear());
        entity.setProvince(dto.getProvince());
        entity.setSubjectType(dto.getSubjectType());
        entity.setMajorName(dto.getMajorName());
        if (dto.getMajorCode() != null) entity.setMajorCode(dto.getMajorCode());
        if (dto.getEntryScore() != null) entity.setEntryScore(dto.getEntryScore());
        if (dto.getEntryScoreType() != null) entity.setEntryScoreType(dto.getEntryScoreType());
        if (dto.getEntryFormula() != null) entity.setEntryFormula(dto.getEntryFormula());
        if (dto.getEntryRatio() != null) entity.setEntryRatio(dto.getEntryRatio());
        if (dto.getAdmissionScore() != null) entity.setAdmissionScore(dto.getAdmissionScore());
        if (dto.getAdmissionFormula() != null) entity.setAdmissionFormula(dto.getAdmissionFormula());
        if (dto.getPlanCount() != null) entity.setPlanCount(dto.getPlanCount());
        if (dto.getAdmissionCount() != null) entity.setAdmissionCount(dto.getAdmissionCount());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (entity.getEntryScoreType() == null) {
            entity.setEntryScoreType("高考成绩");
        }
        if (entity.getAdmissionFormula() == null) {
            entity.setAdmissionFormula("高考成绩×85%+校测成绩×15%");
        }
        strongBaseScoreMapper.updateById(entity);
        log.info("修改强基计划数据: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        entity.setIsActive(!entity.getIsActive());
        strongBaseScoreMapper.updateById(entity);
        log.info("切换强基计划数据状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (strongBaseScoreMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        strongBaseScoreMapper.deleteById(id);
        log.info("删除强基计划数据: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        strongBaseScoreMapper.deleteBatchIds(ids);
        log.info("批量删除强基计划数据: count={}", ids.size());
    }
}
