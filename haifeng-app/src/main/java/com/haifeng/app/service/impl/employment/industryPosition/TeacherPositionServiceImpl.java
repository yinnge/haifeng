package com.haifeng.app.service.impl.employment.industryPosition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.industryPosition.TeacherPositionSearchDTO;
import com.haifeng.app.service.employment.industryPosition.TeacherPositionService;
import com.haifeng.app.vo.employment.industryPosition.TeacherPositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.TeacherPositionListVO;
import com.haifeng.common.entity.employment.industryPosition.TeacherPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.TeacherPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherPositionServiceImpl implements TeacherPositionService {

    private final TeacherPositionMapper teacherPositionMapper;

    @Override
    public IPage<TeacherPositionListVO> page(TeacherPositionSearchDTO dto) {
        LambdaQueryWrapper<TeacherPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(TeacherPosition::getSchoolName, dto.getKeyword())
                    .or()
                    .like(TeacherPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(TeacherPosition::getMajorRequirement, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getSchoolType()), TeacherPosition::getSchoolType, dto.getSchoolType());
        wrapper.eq(StrUtil.isNotBlank(dto.getSchoolNature()), TeacherPosition::getSchoolNature, dto.getSchoolNature());
        wrapper.eq(StrUtil.isNotBlank(dto.getSubject()), TeacherPosition::getSubject, dto.getSubject());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), TeacherPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), TeacherPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), TeacherPosition::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getDistrict()), TeacherPosition::getDistrict, dto.getDistrict());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), TeacherPosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(TeacherPosition::getCreatedAt);

        Page<TeacherPosition> page = new Page<>(dto.getPage(), dto.getSize());
        teacherPositionMapper.selectPage(page, wrapper);

        return page.convert(pos -> TeacherPositionListVO.builder()
                .id(pos.getId())
                .schoolName(pos.getSchoolName())
                .schoolType(pos.getSchoolType())
                .schoolNature(pos.getSchoolNature())
                .positionName(pos.getPositionName())
                .subject(pos.getSubject())
                .recruitmentType(pos.getRecruitmentType())
                .province(pos.getProvince())
                .city(pos.getCity())
                .district(pos.getDistrict())
                .workExperience(pos.getWorkExperience())
                .positionStatus(pos.getPositionStatus())
                .build());
    }

    @Override
    public TeacherPositionDetailVO detail(Long id) {
        TeacherPosition pos = teacherPositionMapper.selectById(id);
        if (pos == null || Boolean.TRUE.equals(pos.getIsDeleted())) {
            log.warn("教师招聘岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return TeacherPositionDetailVO.builder()
                .id(pos.getId())
                .schoolName(pos.getSchoolName())
                .schoolType(pos.getSchoolType())
                .schoolNature(pos.getSchoolNature())
                .supervisingDept(pos.getSupervisingDept())
                .positionName(pos.getPositionName())
                .subject(pos.getSubject())
                .recruitmentType(pos.getRecruitmentType())
                .province(pos.getProvince())
                .city(pos.getCity())
                .district(pos.getDistrict())
                .educationRequirement(pos.getEducationRequirement())
                .degreeRequirement(pos.getDegreeRequirement())
                .majorRequirement(pos.getMajorRequirement())
                .ageLimit(pos.getAgeLimit())
                .recruitmentCount(pos.getRecruitmentCount())
                .teacherCertRequirement(pos.getTeacherCertRequirement())
                .teacherCertSubject(pos.getTeacherCertSubject())
                .putonghuaLevel(pos.getPutonghuaLevel())
                .otherCertRequirement(pos.getOtherCertRequirement())
                .workExperience(pos.getWorkExperience())
                .isNormalMajor(pos.getIsNormalMajor())
                .salaryRange(pos.getSalaryRange())
                .benefits(pos.getBenefits())
                .examContent(pos.getExamContent())
                .interviewForm(pos.getInterviewForm())
                .regStartDate(pos.getRegStartDate())
                .regEndDate(pos.getRegEndDate())
                .examTime(pos.getExamTime())
                .positionStatus(pos.getPositionStatus())
                .applyLink(pos.getApplyLink())
                .contactPhone(pos.getContactPhone())
                .remark(pos.getRemark())
                .content(pos.getContent())
                .build();
    }
}
