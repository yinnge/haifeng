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

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(CommunityPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getStreetOffice, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getCommunityName, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getSupervisingDept, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getDistrict, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getWorkLocation, dto.getKeyword())
                    .or()
                    .like(CommunityPosition::getMajorRequirement, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), CommunityPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getEmploymentType()), CommunityPosition::getEmploymentType, dto.getEmploymentType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), CommunityPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), CommunityPosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), CommunityPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), CommunityPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getWorkExperience()), CommunityPosition::getWorkExperience, dto.getWorkExperience());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), CommunityPosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(CommunityPosition::getCreatedAt);

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
                .positionStatus(p.getPositionStatus())
                .build());
    }

    @Override
    public CommunityPositionDetailVO detail(Long id) {
        CommunityPosition p = communityPositionMapper.selectById(id);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
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
                .contactPhone(p.getContactPhone())
                .contactAddress(p.getContactAddress())
                .remark(p.getRemark())
                .content(p.getContent())
                .build();
    }
}
