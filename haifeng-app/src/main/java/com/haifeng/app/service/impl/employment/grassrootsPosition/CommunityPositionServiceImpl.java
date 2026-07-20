package com.haifeng.app.service.impl.employment.grassrootsPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.CommunityPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.CommunityPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPositionServiceImpl implements CommunityPositionService {

    private final CommunityPositionMapper communityPositionMapper;

    @Override
    public IPage<CommunityPositionListVO> page(CommunityPositionSearchDTO dto) {
        LambdaQueryWrapper<CommunityPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getPositionName())) {
            wrapper.like(CommunityPosition::getPositionName, dto.getPositionName());
        }
        if (StrUtil.isNotBlank(dto.getStreetOffice())) {
            wrapper.like(CommunityPosition::getStreetOffice, dto.getStreetOffice());
        }
        if (StrUtil.isNotBlank(dto.getCommunityName())) {
            wrapper.like(CommunityPosition::getCommunityName, dto.getCommunityName());
        }
        if (StrUtil.isNotBlank(dto.getSupervisingDept())) {
            wrapper.like(CommunityPosition::getSupervisingDept, dto.getSupervisingDept());
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), CommunityPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getEmploymentType()), CommunityPosition::getEmploymentType, dto.getEmploymentType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), CommunityPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), CommunityPosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), CommunityPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), CommunityPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), CommunityPosition::getMajorRequirement, dto.getMajorRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getWorkExperience()), CommunityPosition::getWorkExperience, dto.getWorkExperience());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), CommunityPosition::getPositionStatus, dto.getPositionStatus());

        if (dto.getAgeLimitMin() != null) {
            wrapper.ge(CommunityPosition::getAgeLimit, dto.getAgeLimitMin());
        }
        if (dto.getAgeLimitMax() != null) {
            wrapper.le(CommunityPosition::getAgeLimit, dto.getAgeLimitMax());
        }

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<CommunityPosition> page = new Page<>(dto.getPage(), dto.getSize());
        communityPositionMapper.selectPage(page, wrapper);

        return page.convert(p -> CommunityPositionListVO.builder()
                .id(p.getId())
                .communityName(p.getCommunityName())
                .district(p.getDistrict())
                .positionName(p.getPositionName())
                .positionType(p.getPositionType())
                .province(p.getProvince())
                .city(p.getCity())
                .educationRequirement(p.getEducationRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .majorRequirement(p.getMajorRequirement())
                .workExperience(p.getWorkExperience())
                .build());
    }

    @Override
    public CommunityPositionDetailVO detail(Long id) {
        LambdaQueryWrapper<CommunityPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPosition::getId, id);
        wrapper.eq(CommunityPosition::getIsDeleted, false);
        CommunityPosition p = communityPositionMapper.selectOne(wrapper);
        if (p == null) {
            log.warn("社区工作者岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return CommunityPositionDetailVO.builder()
                .id(p.getId())
                .streetOffice(p.getStreetOffice())
                .communityName(p.getCommunityName())
                .supervisingDept(p.getSupervisingDept())
                .district(p.getDistrict())
                .positionName(p.getPositionName())
                .positionType(p.getPositionType())
                .employmentType(p.getEmploymentType())
                .province(p.getProvince())
                .city(p.getCity())
                .workLocation(p.getWorkLocation())
                .educationRequirement(p.getEducationRequirement())
                .ageLimit(p.getAgeLimit())
                .recruitmentCount(p.getRecruitmentCount())
                .majorRequirement(p.getMajorRequirement())
                .householdRequirement(p.getHouseholdRequirement())
                .politicalStatus(p.getPoliticalStatus())
                .workExperience(p.getWorkExperience())
                .socialWorkCert(p.getSocialWorkCert())
                .communityExperience(p.getCommunityExperience())
                .residenceRequirement(p.getResidenceRequirement())
                .salaryRange(p.getSalaryRange())
                .salaryComposition(p.getSalaryComposition())
                .benefits(p.getBenefits())
                .examContent(p.getExamContent())
                .interviewForm(p.getInterviewForm())
                .regStartDate(p.getRegStartDate())
                .regEndDate(p.getRegEndDate())
                .examTime(p.getExamTime())
                .positionStatus(p.getPositionStatus())
                .applyLink(p.getApplyLink())
                .applyMethod(p.getApplyMethod())
                .contactPhone(desensitizePhone(p.getContactPhone()))
                .contactAddress(p.getContactAddress())
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
