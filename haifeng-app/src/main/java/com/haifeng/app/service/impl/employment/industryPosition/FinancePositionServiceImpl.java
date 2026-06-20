package com.haifeng.app.service.impl.employment.industryPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.industryPosition.FinancePositionSearchDTO;
import com.haifeng.app.service.employment.industryPosition.FinancePositionService;
import com.haifeng.app.vo.employment.industryPosition.FinancePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.FinancePositionListVO;
import com.haifeng.common.entity.employment.industryPosition.FinancePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.FinancePositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancePositionServiceImpl implements FinancePositionService {

    private final FinancePositionMapper financePositionMapper;

    @Override
    public IPage<FinancePositionListVO> page(FinancePositionSearchDTO dto) {
        LambdaQueryWrapper<FinancePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancePosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(FinancePosition::getInstitutionName, dto.getKeyword())
                    .or()
                    .like(FinancePosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(FinancePosition::getMajorRequirement, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getInstitutionCategory()), FinancePosition::getInstitutionCategory, dto.getInstitutionCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getInstitutionType()), FinancePosition::getInstitutionType, dto.getInstitutionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getBranchName()), FinancePosition::getBranchName, dto.getBranchName());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionCategory()), FinancePosition::getPositionCategory, dto.getPositionCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getRecruitmentType()), FinancePosition::getRecruitmentType, dto.getRecruitmentType());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), FinancePosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), FinancePosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), FinancePosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), FinancePosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(FinancePosition::getCreatedAt);

        Page<FinancePosition> page = new Page<>(dto.getPage(), dto.getSize());
        financePositionMapper.selectPage(page, wrapper);

        return page.convert(pos -> FinancePositionListVO.builder()
                .id(pos.getId())
                .institutionName(pos.getInstitutionName())
                .institutionCategory(pos.getInstitutionCategory())
                .positionName(pos.getPositionName())
                .positionCategory(pos.getPositionCategory())
                .recruitmentType(pos.getRecruitmentType())
                .province(pos.getProvince())
                .city(pos.getCity())
                .educationRequirement(pos.getEducationRequirement())
                .degreeRequirement(pos.getDegreeRequirement())
                .majorRequirement(pos.getMajorRequirement())
                .ageLimit(pos.getAgeLimit())
                .workExperience(pos.getWorkExperience())
                .salaryMin(pos.getSalaryMin())
                .salaryMax(pos.getSalaryMax())
                .regStartDate(pos.getRegStartDate())
                .regEndDate(pos.getRegEndDate())
                .build());
    }

    @Override
    public FinancePositionDetailVO detail(Long id) {
        FinancePosition pos = financePositionMapper.selectById(id);
        if (pos == null || Boolean.TRUE.equals(pos.getIsDeleted())) {
            log.warn("金融招聘岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return FinancePositionDetailVO.builder()
                .id(pos.getId())
                .institutionName(pos.getInstitutionName())
                .institutionCategory(pos.getInstitutionCategory())
                .institutionType(pos.getInstitutionType())
                .institutionLogo(pos.getInstitutionLogo())
                .branchName(pos.getBranchName())
                .positionName(pos.getPositionName())
                .positionCategory(pos.getPositionCategory())
                .recruitmentType(pos.getRecruitmentType())
                .province(pos.getProvince())
                .city(pos.getCity())
                .workLocation(pos.getWorkLocation())
                .isRemote(pos.getIsRemote())
                .educationRequirement(pos.getEducationRequirement())
                .degreeRequirement(pos.getDegreeRequirement())
                .majorRequirement(pos.getMajorRequirement())
                .majorPreference(pos.getMajorPreference())
                .ageLimit(pos.getAgeLimit())
                .workExperience(pos.getWorkExperience())
                .recruitmentCount(pos.getRecruitmentCount())
                .certRequirements(pos.getCertRequirements())
                .languageRequirement(pos.getLanguageRequirement())
                .computerRequirement(pos.getComputerRequirement())
                .otherRequirement(pos.getOtherRequirement())
                .salaryMin(pos.getSalaryMin())
                .salaryMax(pos.getSalaryMax())
                .salaryText(pos.getSalaryText())
                .benefits(pos.getBenefits())
                .examContent(pos.getExamContent())
                .examTime(pos.getExamTime())
                .interviewRounds(pos.getInterviewRounds())
                .regStartDate(pos.getRegStartDate())
                .regEndDate(pos.getRegEndDate())
                .applyLink(pos.getApplyLink())
                .positionStatus(pos.getPositionStatus())
                .contactInfo(pos.getContactInfo())
                .remark(pos.getRemark())
                .content(pos.getContent())
                .build();
    }
}
