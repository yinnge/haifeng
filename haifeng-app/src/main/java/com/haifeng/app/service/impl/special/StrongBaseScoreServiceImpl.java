package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.app.service.special.StrongBaseScoreService;
import com.haifeng.app.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.app.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.entity.special.StrongBaseScore;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseScoreMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseScoreServiceImpl implements StrongBaseScoreService {

    private final StrongBaseScoreMapper strongBaseScoreMapper;

    @Override
    public IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto) {
        if (!ProvinceEnum.isValid(dto.getProvince())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "省份参数不合法");
        }
        Page<StrongBaseScore> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<StrongBaseScore> wrapper = new LambdaQueryWrapper<StrongBaseScore>()
                .eq(StrongBaseScore::getIsActive, true)
                .eq(dto.getYear() != null, StrongBaseScore::getYear, dto.getYear())
                .eq(StringUtils.hasText(dto.getProvince()), StrongBaseScore::getProvince, dto.getProvince())
                .eq(StringUtils.hasText(dto.getSubjectType()), StrongBaseScore::getSubjectType, dto.getSubjectType())
                .eq(StringUtils.hasText(dto.getEntryScoreType()), StrongBaseScore::getEntryScoreType, dto.getEntryScoreType())
                .like(StringUtils.hasText(dto.getUniversityName()), StrongBaseScore::getUniversityName, dto.getUniversityName())
                .like(StringUtils.hasText(dto.getMajorName()), StrongBaseScore::getMajorName, dto.getMajorName())
                .like(StringUtils.hasText(dto.getMajorCode()), StrongBaseScore::getMajorCode, dto.getMajorCode())
                .orderByDesc(StrongBaseScore::getYear)
                .orderByDesc(StrongBaseScore::getId);
        return strongBaseScoreMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public StrongBaseScoreDetailVO detail(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsActive())) {
            log.debug("强基数据不存在或已禁用, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "强基数据不存在");
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
                .build();
    }

    private StrongBaseScoreListVO toListVO(StrongBaseScore e) {
        return StrongBaseScoreListVO.builder()
                .id(e.getId())
                .universityId(e.getUniversityId())
                .universityName(e.getUniversityName())
                .year(e.getYear())
                .province(e.getProvince())
                .subjectType(e.getSubjectType())
                .majorName(e.getMajorName())
                .majorCode(e.getMajorCode())
                .entryScore(e.getEntryScore())
                .entryScoreType(e.getEntryScoreType())
                .entryRatio(e.getEntryRatio())
                .admissionScore(e.getAdmissionScore())
                .planCount(e.getPlanCount())
                .admissionCount(e.getAdmissionCount())
                .build();
    }
}
