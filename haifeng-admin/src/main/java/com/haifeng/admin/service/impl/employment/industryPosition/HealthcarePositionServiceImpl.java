package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.industryPosition.healthcare.HealthcarePositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.healthcare.HealthcarePositionUpdateDTO;
import com.haifeng.admin.excel.employment.industryPosition.HealthcarePositionExcelDTO;
import com.haifeng.admin.service.employment.industryPosition.HealthcarePositionService;
import com.haifeng.admin.vo.employment.industryPosition.healthcare.HealthcarePositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.healthcare.HealthcarePositionListVO;
import com.haifeng.common.entity.employment.industryPosition.HealthcarePosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.HealthcarePositionMapper;
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
public class HealthcarePositionServiceImpl implements HealthcarePositionService {

    private final HealthcarePositionMapper healthcarePositionMapper;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束", "即将开始");
    private static final Set<String> VALID_INSTITUTION_TYPES = Set.of(
            "综合医院", "专科医院", "中医医院", "社区卫生服务中心",
            "疾控中心", "妇幼保健院", "卫生监督所", "急救中心",
            "血站", "精神卫生中心", "康复中心", "其他");
    private static final Set<String> VALID_INSTITUTION_LEVELS = Set.of(
            "三级甲等", "三级乙等", "二级甲等", "二级乙等", "一级", "未定级", "社区");
    private static final Set<String> VALID_INSTITUTION_NATURES = Set.of("公立", "民营");
    private static final Set<String> VALID_POSITION_CATEGORIES = Set.of(
            "临床医师", "护理", "药学", "医技", "公共卫生", "行政后勤", "科研");
    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of(
            "编制", "合同制", "人事代理", "规培", "进修");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of(
            "不限", "大专", "本科", "硕士", "博士");
    private static final Set<String> VALID_TITLE_REQUIREMENTS = Set.of(
            "不限", "初级", "中级", "副高级", "正高级");
    private static final int MAX_ERROR_DISPLAY = 20;

    @Override
    public IPage<HealthcarePositionListVO> page(HealthcarePositionQueryDTO dto) {
        Page<HealthcarePosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<HealthcarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthcarePosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getInstitutionName())) {
            wrapper.like(HealthcarePosition::getInstitutionName, dto.getInstitutionName());
        }
        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(HealthcarePosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getInstitutionNature())) {
            wrapper.eq(HealthcarePosition::getInstitutionNature, dto.getInstitutionNature());
        }
        if (StringUtils.hasText(dto.getDepartment())) {
            wrapper.eq(HealthcarePosition::getDepartment, dto.getDepartment());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(HealthcarePosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(HealthcarePosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getDistrict())) {
            wrapper.eq(HealthcarePosition::getDistrict, dto.getDistrict());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(HealthcarePosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByDesc(HealthcarePosition::getSortOrder).orderByDesc(HealthcarePosition::getCreatedAt);

        IPage<HealthcarePosition> healthcarePositionPage = healthcarePositionMapper.selectPage(page, wrapper);

        return healthcarePositionPage.convert(entity -> {
            HealthcarePositionListVO vo = new HealthcarePositionListVO();
            vo.setId(entity.getId());
            vo.setInstitutionName(entity.getInstitutionName());
            vo.setPositionName(entity.getPositionName());
            vo.setDepartment(entity.getDepartment());
            vo.setPositionCategory(entity.getPositionCategory());
            vo.setProvince(entity.getProvince());
            vo.setCity(entity.getCity());
            vo.setDistrict(entity.getDistrict());
            vo.setPositionStatus(entity.getPositionStatus());
            vo.setUpdatedAt(entity.getUpdatedAt());
            return vo;
        });
    }

    @Override
    public HealthcarePositionDetailVO detail(Long id) {
        HealthcarePosition entity = healthcarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        HealthcarePositionDetailVO vo = new HealthcarePositionDetailVO();
        vo.setId(entity.getId());
        vo.setInstitutionName(entity.getInstitutionName());
        vo.setInstitutionType(entity.getInstitutionType());
        vo.setInstitutionLevel(entity.getInstitutionLevel());
        vo.setInstitutionNature(entity.getInstitutionNature());
        vo.setPositionName(entity.getPositionName());
        vo.setDepartment(entity.getDepartment());
        vo.setPositionCategory(entity.getPositionCategory());
        vo.setRecruitmentType(entity.getRecruitmentType());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setDegreeRequirement(entity.getDegreeRequirement());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setAgeLimit(entity.getAgeLimit());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setWorkExperience(entity.getWorkExperience());
        vo.setLicenseRequirement(entity.getLicenseRequirement());
        vo.setTitleRequirement(entity.getTitleRequirement());
        vo.setInternshipRequirement(entity.getInternshipRequirement());
        vo.setResearchRequirement(entity.getResearchRequirement());
        vo.setSalaryRange(entity.getSalaryRange());
        vo.setBenefits(entity.getBenefits());
        vo.setHousingSubsidy(entity.getHousingSubsidy());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setExamTime(entity.getExamTime());
        vo.setExamContent(entity.getExamContent());
        vo.setApplyLink(entity.getApplyLink());
        vo.setPositionStatus(entity.getPositionStatus());
        vo.setContactPhone(entity.getContactPhone());
        vo.setContactPerson(entity.getContactPerson());
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
    public void update(Long id, HealthcarePositionUpdateDTO dto) {
        HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
        if (healthcarePosition == null || healthcarePosition.getIsDeleted()) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        if (dto.getInstitutionName() != null) healthcarePosition.setInstitutionName(dto.getInstitutionName());
        if (dto.getInstitutionType() != null) healthcarePosition.setInstitutionType(dto.getInstitutionType());
        if (dto.getInstitutionLevel() != null) healthcarePosition.setInstitutionLevel(dto.getInstitutionLevel());
        if (dto.getInstitutionNature() != null) healthcarePosition.setInstitutionNature(dto.getInstitutionNature());
        if (dto.getPositionName() != null) healthcarePosition.setPositionName(dto.getPositionName());
        if (dto.getDepartment() != null) healthcarePosition.setDepartment(dto.getDepartment());
        if (dto.getPositionCategory() != null) healthcarePosition.setPositionCategory(dto.getPositionCategory());
        if (dto.getRecruitmentType() != null) healthcarePosition.setRecruitmentType(dto.getRecruitmentType());
        if (dto.getProvince() != null) healthcarePosition.setProvince(dto.getProvince());
        if (dto.getCity() != null) healthcarePosition.setCity(dto.getCity());
        if (dto.getDistrict() != null) healthcarePosition.setDistrict(dto.getDistrict());
        if (dto.getEducationRequirement() != null) healthcarePosition.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getDegreeRequirement() != null) healthcarePosition.setDegreeRequirement(dto.getDegreeRequirement());
        if (dto.getMajorRequirement() != null) healthcarePosition.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getAgeLimit() != null) healthcarePosition.setAgeLimit(dto.getAgeLimit());
        if (dto.getRecruitmentCount() != null) healthcarePosition.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getWorkExperience() != null) healthcarePosition.setWorkExperience(dto.getWorkExperience());
        if (dto.getLicenseRequirement() != null) healthcarePosition.setLicenseRequirement(dto.getLicenseRequirement());
        if (dto.getTitleRequirement() != null) healthcarePosition.setTitleRequirement(dto.getTitleRequirement());
        if (dto.getInternshipRequirement() != null) healthcarePosition.setInternshipRequirement(dto.getInternshipRequirement());
        if (dto.getResearchRequirement() != null) healthcarePosition.setResearchRequirement(dto.getResearchRequirement());
        if (dto.getSalaryRange() != null) healthcarePosition.setSalaryRange(dto.getSalaryRange());
        if (dto.getBenefits() != null) healthcarePosition.setBenefits(dto.getBenefits());
        if (dto.getHousingSubsidy() != null) healthcarePosition.setHousingSubsidy(dto.getHousingSubsidy());
        if (dto.getRegStartDate() != null) healthcarePosition.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) healthcarePosition.setRegEndDate(dto.getRegEndDate());
        if (dto.getExamTime() != null) healthcarePosition.setExamTime(dto.getExamTime());
        if (dto.getExamContent() != null) healthcarePosition.setExamContent(dto.getExamContent());
        if (dto.getApplyLink() != null) healthcarePosition.setApplyLink(dto.getApplyLink());
        if (dto.getPositionStatus() != null) healthcarePosition.setPositionStatus(dto.getPositionStatus());
        if (dto.getContactPhone() != null) healthcarePosition.setContactPhone(dto.getContactPhone());
        if (dto.getContactPerson() != null) healthcarePosition.setContactPerson(dto.getContactPerson());
        if (dto.getRemark() != null) healthcarePosition.setRemark(dto.getRemark());
        if (dto.getContent() != null) healthcarePosition.setContent(dto.getContent());
        if (dto.getSortOrder() != null) healthcarePosition.setSortOrder(dto.getSortOrder());
        healthcarePositionMapper.updateById(healthcarePosition);
        log.info("更新医疗卫生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
        if (healthcarePosition == null || healthcarePosition.getIsDeleted()) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        healthcarePosition.setIsDeleted(true);
        healthcarePositionMapper.updateById(healthcarePosition);
        log.info("软删除医疗卫生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态只能是: 招聘中、已结束、即将开始");
        }
        HealthcarePosition entity = healthcarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        healthcarePositionMapper.updateById(entity);
        log.info("更新医疗卫生岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = healthcarePositionMapper.update(null,
                Wrappers.lambdaUpdate(HealthcarePosition.class)
                        .set(HealthcarePosition::getIsDeleted, true)
                        .eq(HealthcarePosition::getIsDeleted, false)
                        .in(HealthcarePosition::getId, ids));
        log.info("批量删除医疗卫生岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<HealthcarePositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<HealthcarePositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<HealthcarePosition> entities = new ArrayList<>();
        for (HealthcarePositionExcelDTO dto : list) {
            HealthcarePosition entity = HealthcarePosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .institutionName(dto.getInstitutionName())
                    .institutionType(dto.getInstitutionType())
                    .institutionLevel(dto.getInstitutionLevel())
                    .institutionNature(dto.getInstitutionNature())
                    .positionName(dto.getPositionName())
                    .department(dto.getDepartment())
                    .positionCategory(dto.getPositionCategory())
                    .recruitmentType(dto.getRecruitmentType())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .district(dto.getDistrict())
                    .educationRequirement(dto.getEducationRequirement())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .workExperience(dto.getWorkExperience())
                    .licenseRequirement(dto.getLicenseRequirement())
                    .titleRequirement(dto.getTitleRequirement())
                    .internshipRequirement(dto.getInternshipRequirement())
                    .researchRequirement(dto.getResearchRequirement())
                    .salaryRange(dto.getSalaryRange())
                    .benefits(dto.getBenefits())
                    .housingSubsidy(dto.getHousingSubsidy())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .examContent(dto.getExamContent())
                    .applyLink(dto.getApplyLink())
                    .positionStatus(dto.getPositionStatus())
                    .contactPhone(dto.getContactPhone())
                    .contactPerson(dto.getContactPerson())
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
        log.info("导入医疗卫生岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<HealthcarePositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (HealthcarePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getInstitutionName())) {
                errors.add("医疗机构名称不能为空");
            }
            if (StringUtils.hasText(dto.getInstitutionType())) {
                if (!VALID_INSTITUTION_TYPES.contains(dto.getInstitutionType())) {
                    errors.add("机构类型不合法: " + dto.getInstitutionType());
                }
            } else {
                errors.add("机构类型不能为空");
            }
            if (StringUtils.hasText(dto.getInstitutionLevel())
                    && !VALID_INSTITUTION_LEVELS.contains(dto.getInstitutionLevel())) {
                errors.add("机构等级只能是: 三级甲等、三级乙等、二级甲等、二级乙等、一级、未定级、社区");
            }
            if (StringUtils.hasText(dto.getInstitutionNature())
                    && !VALID_INSTITUTION_NATURES.contains(dto.getInstitutionNature())) {
                errors.add("机构性质只能是: 公立、民营");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (StringUtils.hasText(dto.getPositionCategory())) {
                if (!VALID_POSITION_CATEGORIES.contains(dto.getPositionCategory())) {
                    errors.add("岗位类别不合法: " + dto.getPositionCategory());
                }
            } else {
                errors.add("岗位类别不能为空");
            }
            if (StringUtils.hasText(dto.getRecruitmentType())
                    && !VALID_RECRUITMENT_TYPES.contains(dto.getRecruitmentType())) {
                errors.add("招聘类型只能是: 编制、合同制、人事代理、规培、进修");
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
            if (StringUtils.hasText(dto.getTitleRequirement())
                    && !VALID_TITLE_REQUIREMENTS.contains(dto.getTitleRequirement())) {
                errors.add("职称要求只能是: 不限、初级、中级、副高级、正高级");
            }
            if (StringUtils.hasText(dto.getPositionStatus())
                    && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
                errors.add("状态只能是: 招聘中、已结束、即将开始");
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

    private List<HealthcarePositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(HealthcarePositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
