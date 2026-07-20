package com.haifeng.app.service.impl.employment.grassrootsPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.PublicWelfarePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.PublicWelfarePositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicWelfarePositionServiceImpl implements PublicWelfarePositionService {

    private final PublicWelfarePositionMapper publicWelfarePositionMapper;

    @Override
    public IPage<PublicWelfarePositionListVO> page(PublicWelfarePositionSearchDTO dto) {
        LambdaQueryWrapper<PublicWelfarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublicWelfarePosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getPositionName())) {
            wrapper.like(PublicWelfarePosition::getPositionName, dto.getPositionName());
        }
        if (StrUtil.isNotBlank(dto.getDevelopingUnit())) {
            wrapper.like(PublicWelfarePosition::getDevelopingUnit, dto.getDevelopingUnit());
        }
        if (StrUtil.isNotBlank(dto.getEmployingUnit())) {
            wrapper.like(PublicWelfarePosition::getEmployingUnit, dto.getEmployingUnit());
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionCategory()), PublicWelfarePosition::getPositionCategory, dto.getPositionCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), PublicWelfarePosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), PublicWelfarePosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getDistrict()), PublicWelfarePosition::getDistrict, dto.getDistrict());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), PublicWelfarePosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), PublicWelfarePosition::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getHouseholdRequirement()), PublicWelfarePosition::getHouseholdRequirement, dto.getHouseholdRequirement());
        if (dto.getMaxServiceYears() != null) {
            wrapper.eq(PublicWelfarePosition::getMaxServiceYears, dto.getMaxServiceYears());
        }

        if (dto.getAgeRangeMin() != null) {
            wrapper.apply("age_range IS NOT NULL AND (string_to_array(age_range, '-'))[1]::int >= {0}", dto.getAgeRangeMin());
        }
        if (dto.getAgeRangeMax() != null) {
            wrapper.apply("age_range IS NOT NULL AND (string_to_array(age_range, '-'))[2]::int <= {0}", dto.getAgeRangeMax());
        }

        if (StrUtil.isNotBlank(dto.getTargetGroup())) {
            wrapper.apply("target_group @> ARRAY[{0}]::text[]", dto.getTargetGroup());
        }

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<PublicWelfarePosition> page = new Page<>(dto.getPage(), dto.getSize());
        publicWelfarePositionMapper.selectPage(page, wrapper);

        return page.convert(p -> PublicWelfarePositionListVO.builder()
                .id(p.getId())
                .developingUnit(p.getDevelopingUnit())
                .employingUnit(p.getEmployingUnit())
                .positionName(p.getPositionName())
                .positionCategory(p.getPositionCategory())
                .province(p.getProvince())
                .city(p.getCity())
                .district(p.getDistrict())
                .educationRequirement(p.getEducationRequirement())
                .recruitmentCount(p.getRecruitmentCount())
                .monthlySalary(p.getMonthlySalary())
                .contractPeriod(p.getContractPeriod())
                .maxServiceYears(p.getMaxServiceYears())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .build());
    }

    @Override
    public PublicWelfarePositionDetailVO detail(Long id) {
        LambdaQueryWrapper<PublicWelfarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublicWelfarePosition::getId, id);
        wrapper.eq(PublicWelfarePosition::getIsDeleted, false);
        PublicWelfarePosition p = publicWelfarePositionMapper.selectOne(wrapper);
        if (p == null) {
            log.warn("公益性岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return PublicWelfarePositionDetailVO.builder()
                .id(p.getId())
                .developingUnit(p.getDevelopingUnit())
                .employingUnit(p.getEmployingUnit())
                .positionName(p.getPositionName())
                .positionCategory(p.getPositionCategory())
                .workContent(p.getWorkContent())
                .province(p.getProvince())
                .city(p.getCity())
                .district(p.getDistrict())
                .workLocation(p.getWorkLocation())
                .targetGroup(p.getTargetGroup())
                .educationRequirement(p.getEducationRequirement())
                .ageRange(p.getAgeRange())
                .healthRequirement(p.getHealthRequirement())
                .recruitmentCount(p.getRecruitmentCount())
                .householdRequirement(p.getHouseholdRequirement())
                .employmentDifficultyCert(p.getEmploymentDifficultyCert())
                .otherRequirement(p.getOtherRequirement())
                .contractPeriod(p.getContractPeriod())
                .isRenewable(p.getIsRenewable())
                .maxServiceYears(p.getMaxServiceYears())
                .monthlySalary(p.getMonthlySalary())
                .salarySource(p.getSalarySource())
                .subsidyStandard(p.getSubsidyStandard())
                .socialInsuranceInfo(p.getSocialInsuranceInfo())
                .otherBenefits(p.getOtherBenefits())
                .workSchedule(p.getWorkSchedule())
                .isShiftWork(p.getIsShiftWork())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .applyMethod(p.getApplyMethod())
                .applyAddress(p.getApplyAddress())
                .requiredDocuments(p.getRequiredDocuments())
                .positionStatus(p.getPositionStatus())
                .contactPhone(desensitizePhone(p.getContactPhone()))
                .contactPerson(p.getContactPerson())
                .remark(p.getRemark())
                .content(p.getContent())
                .build();
    }

    private String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone)) return phone;
        if (phone.contains("@")) {
            int atIndex = phone.indexOf("@");
            if (atIndex > 2) {
                return phone.substring(0, 2) + "***" + phone.substring(atIndex);
            }
            return phone.substring(0, 1) + "***" + phone.substring(atIndex);
        }
        if (phone.length() >= 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        } else if (phone.length() >= 8) {
            return phone.substring(0, 2) + "****" + phone.substring(6);
        } else if (phone.length() >= 4) {
            return phone.substring(0, 1) + "****" + phone.substring(phone.length() - 1);
        }
        return phone;
    }
}
