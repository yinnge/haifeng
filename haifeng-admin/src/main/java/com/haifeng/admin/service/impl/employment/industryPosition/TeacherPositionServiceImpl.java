package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionUpdateDTO;
import com.haifeng.admin.excel.employment.industryPosition.TeacherPositionExcelDTO;
import com.haifeng.admin.service.employment.industryPosition.TeacherPositionService;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionListVO;
import com.haifeng.common.entity.employment.industryPosition.TeacherPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.TeacherPositionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherPositionServiceImpl implements TeacherPositionService {

    private final TeacherPositionMapper teacherPositionMapper;

    @Override
    public IPage<TeacherPositionListVO> page(TeacherPositionQueryDTO dto) {
        Page<TeacherPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<TeacherPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getSchoolName())) {
            wrapper.like(TeacherPosition::getSchoolName, dto.getSchoolName());
        }
        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(TeacherPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getSchoolType())) {
            wrapper.eq(TeacherPosition::getSchoolType, dto.getSchoolType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(TeacherPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(TeacherPosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getDistrict())) {
            wrapper.eq(TeacherPosition::getDistrict, dto.getDistrict());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(TeacherPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByDesc(TeacherPosition::getSortOrder).orderByDesc(TeacherPosition::getCreatedAt);

        IPage<TeacherPosition> teacherPositionPage = teacherPositionMapper.selectPage(page, wrapper);

        return teacherPositionPage.convert(teacherPosition -> {
            TeacherPositionListVO vo = new TeacherPositionListVO();
            BeanUtils.copyProperties(teacherPosition, vo);
            return vo;
        });
    }

    @Override
    public TeacherPositionDetailVO detail(Long id) {
        TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
        if (teacherPosition == null || teacherPosition.getIsDeleted()) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        TeacherPositionDetailVO vo = new TeacherPositionDetailVO();
        BeanUtils.copyProperties(teacherPosition, vo);
        return vo;
    }

    @Override
    public void update(Long id, TeacherPositionUpdateDTO dto) {
        TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
        if (teacherPosition == null || teacherPosition.getIsDeleted()) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        BeanUtils.copyProperties(dto, teacherPosition);
        teacherPosition.setUpdatedAt(OffsetDateTime.now());
        teacherPositionMapper.updateById(teacherPosition);
        log.info("更新教师招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
        if (teacherPosition == null) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        teacherPositionMapper.deleteById(id);
        log.info("硬删除教师招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
        if (teacherPosition == null) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        teacherPosition.setIsDeleted(status == 0);
        teacherPosition.setUpdatedAt(OffsetDateTime.now());
        teacherPositionMapper.updateById(teacherPosition);
        log.info("更新教师招聘岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
            if (teacherPosition != null) {
                teacherPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除教师招聘岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<TeacherPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (TeacherPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getSchoolName())) {
                errors.add("学校名称不能为空");
            }
            if (!StringUtils.hasText(dto.getSchoolType())) {
                errors.add("学校类型不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getSubject())) {
                errors.add("学科不能为空");
            }
            if (!StringUtils.hasText(dto.getRecruitmentType())) {
                errors.add("招聘类型不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ");
                errorMsg.append(String.join("; ", errors));
                errorMsg.append("\n");
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<TeacherPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (TeacherPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getSchoolName())) errors.add("学校名称不能为空");
            if (!StringUtils.hasText(dto.getSchoolType())) errors.add("学校类型不能为空");
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getSubject())) errors.add("学科不能为空");
            if (!StringUtils.hasText(dto.getRecruitmentType())) errors.add("招聘类型不能为空");
            if (!StringUtils.hasText(dto.getProvince())) errors.add("省份不能为空");
            else if (!ProvinceEnum.isValid(dto.getProvince())) errors.add("省份不合法: " + dto.getProvince());
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (TeacherPositionExcelDTO dto : list) {
            TeacherPosition entity = TeacherPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .schoolName(dto.getSchoolName())
                    .schoolType(dto.getSchoolType())
                    .schoolNature(dto.getSchoolNature())
                    .supervisingDept(dto.getSupervisingDept())
                    .positionName(dto.getPositionName())
                    .subject(dto.getSubject())
                    .recruitmentType(dto.getRecruitmentType())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .district(dto.getDistrict())
                    .educationRequirement(dto.getEducationRequirement())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .teacherCertRequirement(dto.getTeacherCertRequirement())
                    .teacherCertSubject(dto.getTeacherCertSubject())
                    .putonghuaLevel(dto.getPutonghuaLevel())
                    .otherCertRequirement(dto.getOtherCertRequirement())
                    .workExperience(dto.getWorkExperience())
                    .isNormalMajor(dto.getIsNormalMajor())
                    .salaryRange(dto.getSalaryRange())
                    .benefits(dto.getBenefits())
                    .examContent(dto.getExamContent())
                    .interviewForm(dto.getInterviewForm())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .positionStatus(dto.getPositionStatus())
                    .applyLink(dto.getApplyLink())
                    .contactPhone(dto.getContactPhone())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            teacherPositionMapper.insert(entity);
        }
        log.info("导入教师招聘岗位成功: count={}", list.size());
    }

    private List<TeacherPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(TeacherPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
