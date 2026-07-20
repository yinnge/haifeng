package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.CivilPositionService;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;
import com.haifeng.common.entity.employment.civilService.CivilPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.CivilPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CivilPositionServiceImpl implements CivilPositionService {

    private final CivilPositionMapper civilPositionMapper;

    @Override
    public IPage<CivilPositionListVO> page(CivilPositionSearchDTO dto) {
        LambdaQueryWrapper<CivilPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CivilPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(CivilPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(CivilPosition::getRecruitingDept, dto.getKeyword())
                    .or()
                    .like(CivilPosition::getWorkLocation, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getExamType()), CivilPosition::getExamType, dto.getExamType());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionCode()), CivilPosition::getPositionCode, dto.getPositionCode());
        wrapper.eq(StrUtil.isNotBlank(dto.getDeptCode()), CivilPosition::getDeptCode, dto.getDeptCode());
        wrapper.eq(StrUtil.isNotBlank(dto.getMinEducation()), CivilPosition::getMinEducation, dto.getMinEducation());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), CivilPosition::getMajorRequirement, dto.getMajorRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), CivilPosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), CivilPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getExamCategory()), CivilPosition::getExamCategory, dto.getExamCategory());

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<CivilPosition> page = new Page<>(dto.getPage(), dto.getSize());
        civilPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> CivilPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .examType(item.getExamType())
                .recruitingDept(item.getRecruitingDept())
                .majorRequirement(item.getMajorRequirement())
                .minEducation(item.getMinEducation())
                .degreeRequirement(item.getDegreeRequirement())
                .politicalStatus(item.getPoliticalStatus())
                .examCategory(item.getExamCategory())
                .workLocation(item.getWorkLocation())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .regStatus(item.getRegStatus())
                .applicantCount(item.getApplicantCount())
                .build());
    }

    @Override
    public CivilPositionDetailVO detail(Long id) {
        CivilPosition item = civilPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("公务员职位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return CivilPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .examType(item.getExamType())
                .recruitingDept(item.getRecruitingDept())
                .deptCode(item.getDeptCode())
                .positionCode(item.getPositionCode())
                .affiliatedBureau(item.getAffiliatedBureau())
                .majorRequirement(item.getMajorRequirement())
                .minEducation(item.getMinEducation())
                .degreeRequirement(item.getDegreeRequirement())
                .politicalStatus(item.getPoliticalStatus())
                .workExperience(item.getWorkExperience())
                .grassrootsExperience(item.getGrassrootsExperience())
                .examCategory(item.getExamCategory())
                .interviewRatio(item.getInterviewRatio())
                .recruitmentCount(item.getRecruitmentCount())
                .hasProfessionalTest(item.getHasProfessionalTest())
                .workLocation(item.getWorkLocation())
                .workLocationDetail(item.getWorkLocationDetail())
                .householdRequirement(item.getHouseholdRequirement())
                .householdLocation(item.getHouseholdLocation())
                .positionIntro(item.getPositionIntro())
                .remark(item.getRemark())
                .officialWebsite(item.getOfficialWebsite())
                .contactPhone(desensitizePhone(item.getContactPhone()))
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .regStatus(item.getRegStatus())
                .applicantCount(item.getApplicantCount())
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
        return phone.charAt(0) + "****" + phone.charAt(phone.length() - 1);
    }
}
