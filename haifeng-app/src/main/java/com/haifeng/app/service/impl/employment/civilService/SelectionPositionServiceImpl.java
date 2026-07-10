package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.SelectionPositionService;
import com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionListVO;
import com.haifeng.common.entity.employment.civilService.SelectionPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.SelectionPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectionPositionServiceImpl implements SelectionPositionService {

    private final SelectionPositionMapper selectionPositionMapper;

    @Override
    public IPage<SelectionPositionListVO> page(SelectionPositionSearchDTO dto) {
        LambdaQueryWrapper<SelectionPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelectionPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(SelectionPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(SelectionPosition::getTargetUnit, dto.getKeyword())
                    .or()
                    .like(SelectionPosition::getWorkLocation, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getSelectionType()), SelectionPosition::getSelectionType, dto.getSelectionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), SelectionPosition::getYear, dto.getYear());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), SelectionPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), SelectionPosition::getMajorRequirement, dto.getMajorRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getUniversityRequirement()), SelectionPosition::getUniversityRequirement, dto.getUniversityRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), SelectionPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), SelectionPosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), SelectionPosition::getPoliticalStatus, dto.getPoliticalStatus());
        if (dto.getAgeLimit() != null) {
            wrapper.ge(SelectionPosition::getAgeLimit, dto.getAgeLimit());
        }
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), SelectionPosition::getPositionStatus, dto.getPositionStatus());

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<SelectionPosition> page = new Page<>(dto.getPage(), dto.getSize());
        selectionPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> SelectionPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .selectionType(item.getSelectionType())
                .year(item.getYear())
                .province(item.getProvince())
                .organizingDept(item.getOrganizingDept())
                .targetUnit(item.getTargetUnit())
                .workLocation(item.getWorkLocation())
                .trainingDirection(item.getTrainingDirection())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .majorRequirement(item.getMajorRequirement())
                .universityRequirement(item.getUniversityRequirement())
                .politicalStatus(item.getPoliticalStatus())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .positionStatus(item.getPositionStatus())
                .build());
    }

    @Override
    public SelectionPositionDetailVO detail(Long id) {
        SelectionPosition item = selectionPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("选调生岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return SelectionPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .selectionType(item.getSelectionType())
                .year(item.getYear())
                .province(item.getProvince())
                .organizingDept(item.getOrganizingDept())
                .targetUnit(item.getTargetUnit())
                .workLocation(item.getWorkLocation())
                .trainingDirection(item.getTrainingDirection())
                .grassrootsServiceYears(item.getGrassrootsServiceYears())
                .trainingPlan(item.getTrainingPlan())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .majorRequirement(item.getMajorRequirement())
                .majorCategories(item.getMajorCategories())
                .universityRequirement(item.getUniversityRequirement())
                .targetUniversities(item.getTargetUniversities())
                .politicalStatus(item.getPoliticalStatus())
                .studentCadreRequirement(item.getStudentCadreRequirement())
                .awardsRequirement(item.getAwardsRequirement())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .examSubjects(item.getExamSubjects())
                .interviewForm(item.getInterviewForm())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .examTime(item.getExamTime())
                .applyLink(item.getApplyLink())
                .positionStatus(item.getPositionStatus())
                .remark(item.getRemark())
                .contactPhone(desensitizePhone(item.getContactPhone()))
                .officialLink(item.getOfficialLink())
                .content(item.getContent())
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
