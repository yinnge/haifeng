package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherPositionServiceImpl implements TeacherPositionService {

    private final TeacherPositionMapper teacherPositionMapper;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束", "即将开始");
    private static final Set<String> VALID_SCHOOL_TYPES = Set.of(
            "幼儿园", "小学", "初中", "高中", "中职", "高职", "大学", "特殊教育学校");
    private static final Set<String> VALID_SCHOOL_NATURES = Set.of("公办", "民办");
    private static final Set<String> VALID_SUBJECTS = Set.of(
            "语文", "数学", "英语", "物理", "化学", "生物",
            "历史", "地理", "政治", "音乐", "美术", "体育",
            "信息技术", "心理健康", "通用技术", "科学",
            "道德与法治", "综合实践", "学前教育", "特殊教育", "其他");
    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of(
            "编制", "合同制", "特岗教师", "人事代理", "编外聘用");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of(
            "不限", "大专", "本科", "硕士", "博士");
    private static final Set<String> VALID_PUTONGHUA_LEVELS = Set.of(
            "不限", "二级乙等", "二级甲等", "一级乙等", "一级甲等");
    private static final Set<String> VALID_NORMAL_MAJOR = Set.of("要求", "优先", "不限");
    private static final int MAX_ERROR_DISPLAY = 20;

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
        if (StringUtils.hasText(dto.getSchoolNature())) {
            wrapper.eq(TeacherPosition::getSchoolNature, dto.getSchoolNature());
        }
        if (StringUtils.hasText(dto.getRecruitmentType())) {
            wrapper.eq(TeacherPosition::getRecruitmentType, dto.getRecruitmentType());
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

        return teacherPositionPage.convert(entity -> {
            TeacherPositionListVO vo = new TeacherPositionListVO();
            vo.setId(entity.getId());
            vo.setSchoolName(entity.getSchoolName());
            vo.setSchoolType(entity.getSchoolType());
            vo.setSchoolNature(entity.getSchoolNature());
            vo.setPositionName(entity.getPositionName());
            vo.setRecruitmentType(entity.getRecruitmentType());
            vo.setProvince(entity.getProvince());
            vo.setCity(entity.getCity());
            vo.setDistrict(entity.getDistrict());
            vo.setPositionStatus(entity.getPositionStatus());
            vo.setUpdatedAt(entity.getUpdatedAt());
            return vo;
        });
    }

    @Override
    public TeacherPositionDetailVO detail(Long id) {
        TeacherPosition entity = teacherPositionMapper.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        TeacherPositionDetailVO vo = new TeacherPositionDetailVO();
        vo.setId(entity.getId());
        vo.setSchoolName(entity.getSchoolName());
        vo.setSchoolType(entity.getSchoolType());
        vo.setSchoolNature(entity.getSchoolNature());
        vo.setSupervisingDept(entity.getSupervisingDept());
        vo.setPositionName(entity.getPositionName());
        vo.setSubject(entity.getSubject());
        vo.setRecruitmentType(entity.getRecruitmentType());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setDegreeRequirement(entity.getDegreeRequirement());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setAgeLimit(entity.getAgeLimit());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setTeacherCertRequirement(entity.getTeacherCertRequirement());
        vo.setTeacherCertSubject(entity.getTeacherCertSubject());
        vo.setPutonghuaLevel(entity.getPutonghuaLevel());
        vo.setOtherCertRequirement(entity.getOtherCertRequirement());
        vo.setWorkExperience(entity.getWorkExperience());
        vo.setIsNormalMajor(entity.getIsNormalMajor());
        vo.setSalaryRange(entity.getSalaryRange());
        vo.setBenefits(entity.getBenefits());
        vo.setExamContent(entity.getExamContent());
        vo.setInterviewForm(entity.getInterviewForm());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setExamTime(entity.getExamTime());
        vo.setPositionStatus(entity.getPositionStatus());
        vo.setApplyLink(entity.getApplyLink());
        vo.setContactPhone(entity.getContactPhone());
        vo.setRemark(entity.getRemark());
        vo.setContent(entity.getContent());
        vo.setSortOrder(entity.getSortOrder());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TeacherPositionUpdateDTO dto) {
        TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
        if (teacherPosition == null || Boolean.TRUE.equals(teacherPosition.getIsDeleted())) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        if (dto.getSchoolName() != null) teacherPosition.setSchoolName(dto.getSchoolName());
        if (dto.getSchoolType() != null) teacherPosition.setSchoolType(dto.getSchoolType());
        if (dto.getSchoolNature() != null) teacherPosition.setSchoolNature(dto.getSchoolNature());
        if (dto.getSupervisingDept() != null) teacherPosition.setSupervisingDept(dto.getSupervisingDept());
        if (dto.getPositionName() != null) teacherPosition.setPositionName(dto.getPositionName());
        if (dto.getSubject() != null) teacherPosition.setSubject(dto.getSubject());
        if (dto.getRecruitmentType() != null) teacherPosition.setRecruitmentType(dto.getRecruitmentType());
        if (dto.getProvince() != null) teacherPosition.setProvince(dto.getProvince());
        if (dto.getCity() != null) teacherPosition.setCity(dto.getCity());
        if (dto.getDistrict() != null) teacherPosition.setDistrict(dto.getDistrict());
        if (dto.getEducationRequirement() != null) teacherPosition.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getDegreeRequirement() != null) teacherPosition.setDegreeRequirement(dto.getDegreeRequirement());
        if (dto.getMajorRequirement() != null) teacherPosition.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getAgeLimit() != null) teacherPosition.setAgeLimit(dto.getAgeLimit());
        if (dto.getRecruitmentCount() != null) teacherPosition.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getTeacherCertRequirement() != null) teacherPosition.setTeacherCertRequirement(dto.getTeacherCertRequirement());
        if (dto.getTeacherCertSubject() != null) teacherPosition.setTeacherCertSubject(dto.getTeacherCertSubject());
        if (dto.getPutonghuaLevel() != null) teacherPosition.setPutonghuaLevel(dto.getPutonghuaLevel());
        if (dto.getOtherCertRequirement() != null) teacherPosition.setOtherCertRequirement(dto.getOtherCertRequirement());
        if (dto.getWorkExperience() != null) teacherPosition.setWorkExperience(dto.getWorkExperience());
        if (dto.getIsNormalMajor() != null) teacherPosition.setIsNormalMajor(dto.getIsNormalMajor());
        if (dto.getSalaryRange() != null) teacherPosition.setSalaryRange(dto.getSalaryRange());
        if (dto.getBenefits() != null) teacherPosition.setBenefits(dto.getBenefits());
        if (dto.getExamContent() != null) teacherPosition.setExamContent(dto.getExamContent());
        if (dto.getInterviewForm() != null) teacherPosition.setInterviewForm(dto.getInterviewForm());
        if (dto.getRegStartDate() != null) teacherPosition.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) teacherPosition.setRegEndDate(dto.getRegEndDate());
        if (dto.getExamTime() != null) teacherPosition.setExamTime(dto.getExamTime());
        if (dto.getPositionStatus() != null) teacherPosition.setPositionStatus(dto.getPositionStatus());
        if (dto.getApplyLink() != null) teacherPosition.setApplyLink(dto.getApplyLink());
        if (dto.getContactPhone() != null) teacherPosition.setContactPhone(dto.getContactPhone());
        if (dto.getRemark() != null) teacherPosition.setRemark(dto.getRemark());
        if (dto.getContent() != null) teacherPosition.setContent(dto.getContent());
        if (dto.getSortOrder() != null) teacherPosition.setSortOrder(dto.getSortOrder());
        teacherPositionMapper.updateById(teacherPosition);
        log.info("更新教师招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TeacherPosition teacherPosition = teacherPositionMapper.selectById(id);
        if (teacherPosition == null || Boolean.TRUE.equals(teacherPosition.getIsDeleted())) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        teacherPosition.setIsDeleted(true);
        teacherPositionMapper.updateById(teacherPosition);
        log.info("软删除教师招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态只能是: 招聘中、已结束、即将开始");
        }
        TeacherPosition entity = teacherPositionMapper.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        teacherPositionMapper.updateById(entity);
        log.info("更新教师招聘岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = teacherPositionMapper.update(null,
                Wrappers.lambdaUpdate(TeacherPosition.class)
                        .set(TeacherPosition::getIsDeleted, true)
                        .eq(TeacherPosition::getIsDeleted, false)
                        .in(TeacherPosition::getId, ids));
        log.info("批量删除教师招聘岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<TeacherPositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<TeacherPositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<TeacherPosition> entities = new ArrayList<>();
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
            entities.add(entity);
        }
        Db.saveBatch(entities);
        log.info("导入教师招聘岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<TeacherPositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (TeacherPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getSchoolName())) {
                errors.add("学校名称不能为空");
            }
            if (StringUtils.hasText(dto.getSchoolType())) {
                if (!VALID_SCHOOL_TYPES.contains(dto.getSchoolType())) {
                    errors.add("学校类型不合法: " + dto.getSchoolType());
                }
            } else {
                errors.add("学校类型不能为空");
            }
            if (StringUtils.hasText(dto.getSchoolNature())
                    && !VALID_SCHOOL_NATURES.contains(dto.getSchoolNature())) {
                errors.add("学校性质只能是: 公办、民办");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (StringUtils.hasText(dto.getSubject())) {
                if (!VALID_SUBJECTS.contains(dto.getSubject())) {
                    errors.add("学科不合法: " + dto.getSubject());
                }
            } else {
                errors.add("学科不能为空");
            }
            if (StringUtils.hasText(dto.getRecruitmentType())) {
                if (!VALID_RECRUITMENT_TYPES.contains(dto.getRecruitmentType())) {
                    errors.add("招聘类型不合法: " + dto.getRecruitmentType());
                }
            } else {
                errors.add("招聘类型不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (StringUtils.hasText(dto.getEducationRequirement())
                    && !VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
                errors.add("学历要求只能是: 不限、大专、本科、硕士、博士");
            }
            if (StringUtils.hasText(dto.getPutonghuaLevel())
                    && !VALID_PUTONGHUA_LEVELS.contains(dto.getPutonghuaLevel())) {
                errors.add("普通话等级只能是: 不限、二级乙等、二级甲等、一级乙等、一级甲等");
            }
            if (StringUtils.hasText(dto.getIsNormalMajor())
                    && !VALID_NORMAL_MAJOR.contains(dto.getIsNormalMajor())) {
                errors.add("是否师范专业只能是: 要求、优先、不限");
            }
            if (StringUtils.hasText(dto.getPositionStatus())
                    && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
                errors.add("状态只能是: 招聘中、已结束、即将开始");
            }
            if (dto.getAgeLimit() != null && (dto.getAgeLimit() < 18 || dto.getAgeLimit() > 60)) {
                errors.add("年龄上限须在18-60之间");
            }
            if (dto.getRecruitmentCount() != null && dto.getRecruitmentCount() <= 0) {
                errors.add("招聘人数必须大于0");
            }
            if (!errors.isEmpty()) {
                errorCount++;
                if (errorCount <= MAX_ERROR_DISPLAY) {
                    errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
                }
            }
        }
        if (errorCount > MAX_ERROR_DISPLAY) {
            errorMsg.append("...共").append(errorCount).append("条错误，仅显示前").append(MAX_ERROR_DISPLAY).append("条");
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    private List<TeacherPositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
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
