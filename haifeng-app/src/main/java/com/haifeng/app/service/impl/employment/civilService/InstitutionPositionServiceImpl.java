package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.InstitutionPositionService;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.common.entity.employment.civilService.InstitutionPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.InstitutionPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionPositionServiceImpl implements InstitutionPositionService {

    private final InstitutionPositionMapper institutionPositionMapper;

    @Override
    public IPage<InstitutionPositionListVO> page(InstitutionPositionSearchDTO dto) {
        LambdaQueryWrapper<InstitutionPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InstitutionPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(InstitutionPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(InstitutionPosition::getSupervisingDept, dto.getKeyword())
                    .or()
                    .like(InstitutionPosition::getInstitution, dto.getKeyword())
                    .or()
                    .like(InstitutionPosition::getWorkLocation, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), InstitutionPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getExamCategory()), InstitutionPosition::getExamCategory, dto.getExamCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), InstitutionPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), InstitutionPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), InstitutionPosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), InstitutionPosition::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getSpecialPosition()), InstitutionPosition::getSpecialPosition, dto.getSpecialPosition());
        if (dto.getAgeLimit() != null) {
            wrapper.ge(InstitutionPosition::getAgeLimit, dto.getAgeLimit());
        }

        wrapper.last("ORDER BY sort_order DESC NULLS LAST, created_at DESC NULLS LAST");

        Page<InstitutionPosition> page = new Page<>(dto.getPage(), dto.getSize());
        institutionPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> InstitutionPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .supervisingDept(item.getSupervisingDept())
                .institution(item.getInstitution())
                .workLocation(item.getWorkLocation())
                .province(item.getProvince())
                .examCategory(item.getExamCategory())
                .positionType(item.getPositionType())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .salaryRange(item.getSalaryRange())
                .regDeadline(item.getRegDeadline())
                .specialPosition(item.getSpecialPosition())
                .positionStatus(item.getPositionStatus())
                .build());
    }

    @Override
    public InstitutionPositionDetailVO detail(Long id) {
        InstitutionPosition item = institutionPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("事业编职位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return InstitutionPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .supervisingDept(item.getSupervisingDept())
                .institution(item.getInstitution())
                .workLocation(item.getWorkLocation())
                .province(item.getProvince())
                .examCategory(item.getExamCategory())
                .positionType(item.getPositionType())
                .subCategory(item.getSubCategory())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .salaryRange(item.getSalaryRange())
                .regDeadline(item.getRegDeadline())
                .majorRequirements(item.getMajorRequirements())
                .specialPosition(item.getSpecialPosition())
                .otherRequirement(item.getOtherRequirement())
                .otherRequirementDesc(item.getOtherRequirementDesc())
                .remarkType(item.getRemarkType())
                .remarkDesc(item.getRemarkDesc())
                .consultationPhone(desensitizePhone(item.getConsultationPhone()))
                .supervisionPhone(desensitizePhone(item.getSupervisionPhone()))
                .positionStatus(item.getPositionStatus())
                .positionTag(item.getPositionTag())
                .tagText(item.getTagText())
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
