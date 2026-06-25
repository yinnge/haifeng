package com.haifeng.app.service.impl.employment.grassrootsPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.GrassrootsProjectPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.GrassrootsProjectPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrassrootsProjectPositionServiceImpl implements GrassrootsProjectPositionService {

    private final GrassrootsProjectPositionMapper grassrootsProjectPositionMapper;

    @Override
    public IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionSearchDTO dto) {
        LambdaQueryWrapper<GrassrootsProjectPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrassrootsProjectPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(GrassrootsProjectPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(GrassrootsProjectPosition::getOrganizingDept, dto.getKeyword())
                    .or()
                    .like(GrassrootsProjectPosition::getServiceUnit, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getProjectType()), GrassrootsProjectPosition::getProjectType, dto.getProjectType());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), GrassrootsProjectPosition::getYear, dto.getYear());
        wrapper.eq(StrUtil.isNotBlank(dto.getServiceType()), GrassrootsProjectPosition::getServiceType, dto.getServiceType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), GrassrootsProjectPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), GrassrootsProjectPosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getCounty()), GrassrootsProjectPosition::getCounty, dto.getCounty());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), GrassrootsProjectPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), GrassrootsProjectPosition::getMajorRequirement, dto.getMajorRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getGradYearRequirement()), GrassrootsProjectPosition::getGradYearRequirement, dto.getGradYearRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), GrassrootsProjectPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), GrassrootsProjectPosition::getPositionStatus, dto.getPositionStatus());

        if (dto.getAgeLimitMin() != null) {
            wrapper.ge(GrassrootsProjectPosition::getAgeLimit, dto.getAgeLimitMin());
        }
        if (dto.getAgeLimitMax() != null) {
            wrapper.le(GrassrootsProjectPosition::getAgeLimit, dto.getAgeLimitMax());
        }

        wrapper.orderByDesc(GrassrootsProjectPosition::getSortOrder);
        wrapper.orderByDesc(GrassrootsProjectPosition::getCreatedAt);

        Page<GrassrootsProjectPosition> page = new Page<>(dto.getPage(), dto.getSize());
        grassrootsProjectPositionMapper.selectPage(page, wrapper);

        return page.convert(p -> GrassrootsProjectPositionListVO.builder()
                .id(p.getId())
                .projectType(p.getProjectType())
                .year(p.getYear())
                .positionName(p.getPositionName())
                .serviceType(p.getServiceType())
                .organizingDept(p.getOrganizingDept())
                .serviceUnit(p.getServiceUnit())
                .province(p.getProvince())
                .city(p.getCity())
                .county(p.getCounty())
                .township(p.getTownship())
                .servicePeriod(p.getServicePeriod())
                .educationRequirement(p.getEducationRequirement())
                .majorRequirement(p.getMajorRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .politicalStatus(p.getPoliticalStatus())
                .build());
    }

    @Override
    public GrassrootsProjectPositionDetailVO detail(Long id) {
        GrassrootsProjectPosition p = grassrootsProjectPositionMapper.selectById(id);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
            log.warn("基层服务项目岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return GrassrootsProjectPositionDetailVO.builder()
                .id(p.getId())
                .projectType(p.getProjectType())
                .year(p.getYear())
                .positionName(p.getPositionName())
                .serviceType(p.getServiceType())
                .organizingDept(p.getOrganizingDept())
                .serviceUnit(p.getServiceUnit())
                .province(p.getProvince())
                .city(p.getCity())
                .county(p.getCounty())
                .township(p.getTownship())
                .servicePeriod(p.getServicePeriod())
                .serviceStartDate(p.getServiceStartDate())
                .serviceEndDate(p.getServiceEndDate())
                .educationRequirement(p.getEducationRequirement())
                .majorRequirement(p.getMajorRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .gradYearRequirement(p.getGradYearRequirement())
                .householdRequirement(p.getHouseholdRequirement())
                .otherRequirement(p.getOtherRequirement())
                .politicalStatus(p.getPoliticalStatus())
                .examContent(p.getExamContent())
                .examTime(p.getExamTime())
                .interviewForm(p.getInterviewForm())
                .monthlySubsidy(p.getMonthlySubsidy())
                .socialInsurance(p.getSocialInsurance())
                .housingInfo(p.getHousingInfo())
                .otherBenefits(p.getOtherBenefits())
                .afterServicePolicy(p.getAfterServicePolicy())
                .canTransferToCivil(p.getCanTransferToCivil())
                .canTransferToInstitution(p.getCanTransferToInstitution())
                .examBonusPoints(p.getExamBonusPoints())
                .tuitionCompensation(p.getTuitionCompensation())
                .postgradBonus(p.getPostgradBonus())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .applyLink(p.getApplyLink())
                .positionStatus(p.getPositionStatus())
                .contactPhone(p.getContactPhone())
                .remark(p.getRemark())
                .content(p.getContent())
                .build();
    }
}
