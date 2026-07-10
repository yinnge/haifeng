package com.haifeng.app.service.impl.employment.industryPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.industryPosition.HealthcarePositionSearchDTO;
import com.haifeng.app.service.employment.industryPosition.HealthcarePositionService;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionListVO;
import com.haifeng.common.entity.employment.industryPosition.HealthcarePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.HealthcarePositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthcarePositionServiceImpl implements HealthcarePositionService {

    private final HealthcarePositionMapper healthcarePositionMapper;

    @Override
    public IPage<HealthcarePositionListVO> page(HealthcarePositionSearchDTO dto) {
        LambdaQueryWrapper<HealthcarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthcarePosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(HealthcarePosition::getInstitutionName, dto.getKeyword())
                    .or()
                    .like(HealthcarePosition::getPositionName, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getInstitutionType()), HealthcarePosition::getInstitutionType, dto.getInstitutionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getInstitutionLevel()), HealthcarePosition::getInstitutionLevel, dto.getInstitutionLevel());
        wrapper.eq(StrUtil.isNotBlank(dto.getInstitutionNature()), HealthcarePosition::getInstitutionNature, dto.getInstitutionNature());
        wrapper.eq(StrUtil.isNotBlank(dto.getDepartment()), HealthcarePosition::getDepartment, dto.getDepartment());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionCategory()), HealthcarePosition::getPositionCategory, dto.getPositionCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), HealthcarePosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), HealthcarePosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getDistrict()), HealthcarePosition::getDistrict, dto.getDistrict());
        wrapper.ge(dto.getAgeLimit() != null, HealthcarePosition::getAgeLimit, dto.getAgeLimit());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), HealthcarePosition::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), HealthcarePosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), HealthcarePosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), HealthcarePosition::getMajorRequirement, dto.getMajorRequirement());

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<HealthcarePosition> page = new Page<>(dto.getPage(), dto.getSize());
        healthcarePositionMapper.selectPage(page, wrapper);

        return page.convert(pos -> HealthcarePositionListVO.builder()
                .id(pos.getId())
                .institutionName(pos.getInstitutionName())
                .institutionType(pos.getInstitutionType())
                .institutionLevel(pos.getInstitutionLevel())
                .institutionNature(pos.getInstitutionNature())
                .positionName(pos.getPositionName())
                .department(pos.getDepartment())
                .positionCategory(pos.getPositionCategory())
                .province(pos.getProvince())
                .city(pos.getCity())
                .district(pos.getDistrict())
                .ageLimit(pos.getAgeLimit())
                .recruitmentCount(pos.getRecruitmentCount())
                .salaryRange(pos.getSalaryRange())
                .workExperience(pos.getWorkExperience())
                .positionStatus(pos.getPositionStatus())
                .educationRequirement(pos.getEducationRequirement())
                .degreeRequirement(pos.getDegreeRequirement())
                .majorRequirement(pos.getMajorRequirement())
                .build());
    }

    @Override
    public HealthcarePositionDetailVO detail(Long id) {
        LambdaQueryWrapper<HealthcarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthcarePosition::getId, id);
        wrapper.eq(HealthcarePosition::getIsDeleted, false);
        HealthcarePosition pos = healthcarePositionMapper.selectOne(wrapper);
        if (pos == null) {
            log.warn("医疗卫生招聘岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return HealthcarePositionDetailVO.builder()
                .id(pos.getId())
                .institutionName(pos.getInstitutionName())
                .institutionType(pos.getInstitutionType())
                .institutionLevel(pos.getInstitutionLevel())
                .institutionNature(pos.getInstitutionNature())
                .positionName(pos.getPositionName())
                .department(pos.getDepartment())
                .positionCategory(pos.getPositionCategory())
                .recruitmentType(pos.getRecruitmentType())
                .province(pos.getProvince())
                .city(pos.getCity())
                .district(pos.getDistrict())
                .educationRequirement(pos.getEducationRequirement())
                .degreeRequirement(pos.getDegreeRequirement())
                .majorRequirement(pos.getMajorRequirement())
                .ageLimit(pos.getAgeLimit())
                .recruitmentCount(pos.getRecruitmentCount())
                .workExperience(pos.getWorkExperience())
                .licenseRequirement(pos.getLicenseRequirement())
                .titleRequirement(pos.getTitleRequirement())
                .internshipRequirement(pos.getInternshipRequirement())
                .researchRequirement(pos.getResearchRequirement())
                .salaryRange(pos.getSalaryRange())
                .benefits(pos.getBenefits())
                .housingSubsidy(pos.getHousingSubsidy())
                .regStartDate(pos.getRegStartDate())
                .regEndDate(pos.getRegEndDate())
                .examTime(pos.getExamTime())
                .examContent(pos.getExamContent())
                .applyLink(pos.getApplyLink())
                .positionStatus(pos.getPositionStatus())
                .contactPhone(desensitizePhone(pos.getContactPhone()))
                .contactPerson(pos.getContactPerson())
                .remark(pos.getRemark())
                .content(pos.getContent())
                .build();
    }

    private String desensitizePhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        if (phone.contains("@")) {
            int atIndex = phone.indexOf("@");
            String prefix = phone.substring(0, atIndex);
            String domain = phone.substring(atIndex);
            if (prefix.length() <= 2) {
                return prefix.charAt(0) + "***" + domain;
            }
            return prefix.substring(0, 2) + "***" + domain;
        }
        if (phone.contains("-")) {
            String[] parts = phone.split("-", 2);
            if (parts[1].length() >= 4) {
                return parts[0] + "-" + "****" + parts[1].substring(parts[1].length() - 4);
            }
            return parts[0] + "-****";
        }
        if (phone.length() >= 11) {
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }
        return phone.substring(0, 1) + "****" + phone.substring(phone.length() - 1);
    }
}
