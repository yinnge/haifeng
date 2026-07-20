package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.excel.employment.grassrootsPosition.GrassrootsProjectPositionExcelDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.GrassrootsProjectPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.GrassrootsProjectPositionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrassrootsProjectPositionServiceImpl implements GrassrootsProjectPositionService {

    private final GrassrootsProjectPositionMapper grassrootsProjectPositionMapper;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招募中", "已结束", "即将开始");
    private static final Set<String> VALID_PROJECT_TYPES = Set.of("三支一扶", "西部计划");
    private static final Set<String> VALID_SERVICE_TYPES = Set.of("支教", "支农", "支医", "帮扶乡村振兴", "基础教育", "服务三农", "医疗卫生", "基层青年工作", "基层社会管理", "服务新疆", "服务西藏");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("大专", "本科", "硕士", "大专及以上", "本科及以上");
    private static final int MAX_ERROR_DISPLAY = 20;

    @Override
    public IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionQueryDTO dto) {
        Page<GrassrootsProjectPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<GrassrootsProjectPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrassrootsProjectPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(GrassrootsProjectPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getOrganizingDept())) {
            wrapper.like(GrassrootsProjectPosition::getOrganizingDept, dto.getOrganizingDept());
        }
        if (StringUtils.hasText(dto.getServiceUnit())) {
            wrapper.like(GrassrootsProjectPosition::getServiceUnit, dto.getServiceUnit());
        }
        if (StringUtils.hasText(dto.getProjectType())) {
            wrapper.eq(GrassrootsProjectPosition::getProjectType, dto.getProjectType());
        }
        if (StringUtils.hasText(dto.getYear())) {
            wrapper.eq(GrassrootsProjectPosition::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getServiceType())) {
            wrapper.eq(GrassrootsProjectPosition::getServiceType, dto.getServiceType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(GrassrootsProjectPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(GrassrootsProjectPosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getCounty())) {
            wrapper.eq(GrassrootsProjectPosition::getCounty, dto.getCounty());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(GrassrootsProjectPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(GrassrootsProjectPosition::getSortOrder).orderByDesc(GrassrootsProjectPosition::getUpdatedAt);

        IPage<GrassrootsProjectPosition> entityPage = grassrootsProjectPositionMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            GrassrootsProjectPositionListVO vo = new GrassrootsProjectPositionListVO();
            vo.setId(entity.getId());
            vo.setProjectType(entity.getProjectType());
            vo.setYear(entity.getYear());
            vo.setPositionName(entity.getPositionName());
            vo.setServiceType(entity.getServiceType());
            vo.setOrganizingDept(entity.getOrganizingDept());
            vo.setServiceUnit(entity.getServiceUnit());
            vo.setProvince(entity.getProvince());
            vo.setCity(entity.getCity());
            vo.setCounty(entity.getCounty());
            vo.setPositionStatus(entity.getPositionStatus());
            return vo;
        });
    }

    @Override
    public GrassrootsProjectPositionDetailVO detail(Long id) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        GrassrootsProjectPositionDetailVO vo = new GrassrootsProjectPositionDetailVO();
        vo.setId(entity.getId());
        vo.setProjectType(entity.getProjectType());
        vo.setYear(entity.getYear());
        vo.setPositionName(entity.getPositionName());
        vo.setServiceType(entity.getServiceType());
        vo.setOrganizingDept(entity.getOrganizingDept());
        vo.setServiceUnit(entity.getServiceUnit());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setCounty(entity.getCounty());
        vo.setTownship(entity.getTownship());
        vo.setServicePeriod(entity.getServicePeriod());
        vo.setServiceStartDate(entity.getServiceStartDate());
        vo.setServiceEndDate(entity.getServiceEndDate());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setAgeLimit(entity.getAgeLimit());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setGradYearRequirement(entity.getGradYearRequirement());
        vo.setHouseholdRequirement(entity.getHouseholdRequirement());
        vo.setPoliticalStatus(entity.getPoliticalStatus());
        vo.setOtherRequirement(entity.getOtherRequirement());
        vo.setExamContent(entity.getExamContent());
        vo.setExamTime(entity.getExamTime());
        vo.setInterviewForm(entity.getInterviewForm());
        vo.setMonthlySubsidy(entity.getMonthlySubsidy());
        vo.setSocialInsurance(entity.getSocialInsurance());
        vo.setHousingInfo(entity.getHousingInfo());
        vo.setOtherBenefits(entity.getOtherBenefits());
        vo.setAfterServicePolicy(entity.getAfterServicePolicy());
        vo.setCanTransferToCivil(entity.getCanTransferToCivil());
        vo.setCanTransferToInstitution(entity.getCanTransferToInstitution());
        vo.setExamBonusPoints(entity.getExamBonusPoints());
        vo.setTuitionCompensation(entity.getTuitionCompensation());
        vo.setPostgradBonus(entity.getPostgradBonus());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setApplyLink(entity.getApplyLink());
        vo.setPositionStatus(entity.getPositionStatus());
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
    public void update(Long id, GrassrootsProjectPositionUpdateDTO dto) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        if (dto.getPositionStatus() != null && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
            throw new BusinessException(400, "状态不合法");
        }
        if (dto.getProjectType() != null && !VALID_PROJECT_TYPES.contains(dto.getProjectType())) {
            throw new BusinessException(400, "项目类型不合法");
        }
        if (dto.getServiceType() != null && !VALID_SERVICE_TYPES.contains(dto.getServiceType())) {
            throw new BusinessException(400, "服务类型不合法");
        }
        if (dto.getEducationRequirement() != null && !VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
            throw new BusinessException(400, "学历要求不合法");
        }
        if (dto.getProjectType() != null) entity.setProjectType(dto.getProjectType());
        if (dto.getYear() != null) entity.setYear(dto.getYear());
        if (dto.getPositionName() != null) entity.setPositionName(dto.getPositionName());
        if (dto.getServiceType() != null) entity.setServiceType(dto.getServiceType());
        if (dto.getOrganizingDept() != null) entity.setOrganizingDept(dto.getOrganizingDept());
        if (dto.getServiceUnit() != null) entity.setServiceUnit(dto.getServiceUnit());
        if (dto.getProvince() != null) entity.setProvince(dto.getProvince());
        if (dto.getCity() != null) entity.setCity(dto.getCity());
        if (dto.getCounty() != null) entity.setCounty(dto.getCounty());
        if (dto.getTownship() != null) entity.setTownship(dto.getTownship());
        if (dto.getServicePeriod() != null) entity.setServicePeriod(dto.getServicePeriod());
        if (dto.getServiceStartDate() != null) entity.setServiceStartDate(dto.getServiceStartDate());
        if (dto.getServiceEndDate() != null) entity.setServiceEndDate(dto.getServiceEndDate());
        if (dto.getEducationRequirement() != null) entity.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getMajorRequirement() != null) entity.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getAgeLimit() != null) entity.setAgeLimit(dto.getAgeLimit());
        if (dto.getRecruitmentCount() != null) entity.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getGradYearRequirement() != null) entity.setGradYearRequirement(dto.getGradYearRequirement());
        if (dto.getHouseholdRequirement() != null) entity.setHouseholdRequirement(dto.getHouseholdRequirement());
        if (dto.getPoliticalStatus() != null) entity.setPoliticalStatus(dto.getPoliticalStatus());
        if (dto.getOtherRequirement() != null) entity.setOtherRequirement(dto.getOtherRequirement());
        if (dto.getExamContent() != null) entity.setExamContent(dto.getExamContent());
        if (dto.getExamTime() != null) entity.setExamTime(dto.getExamTime());
        if (dto.getInterviewForm() != null) entity.setInterviewForm(dto.getInterviewForm());
        if (dto.getMonthlySubsidy() != null) entity.setMonthlySubsidy(dto.getMonthlySubsidy());
        if (dto.getSocialInsurance() != null) entity.setSocialInsurance(dto.getSocialInsurance());
        if (dto.getHousingInfo() != null) entity.setHousingInfo(dto.getHousingInfo());
        if (dto.getOtherBenefits() != null) entity.setOtherBenefits(dto.getOtherBenefits());
        if (dto.getAfterServicePolicy() != null) entity.setAfterServicePolicy(dto.getAfterServicePolicy());
        if (dto.getCanTransferToCivil() != null) entity.setCanTransferToCivil(dto.getCanTransferToCivil());
        if (dto.getCanTransferToInstitution() != null) entity.setCanTransferToInstitution(dto.getCanTransferToInstitution());
        if (dto.getExamBonusPoints() != null) entity.setExamBonusPoints(dto.getExamBonusPoints());
        if (dto.getTuitionCompensation() != null) entity.setTuitionCompensation(dto.getTuitionCompensation());
        if (dto.getPostgradBonus() != null) entity.setPostgradBonus(dto.getPostgradBonus());
        if (dto.getRegStartDate() != null) entity.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) entity.setRegEndDate(dto.getRegEndDate());
        if (dto.getApplyLink() != null) entity.setApplyLink(dto.getApplyLink());
        if (dto.getPositionStatus() != null) entity.setPositionStatus(dto.getPositionStatus());
        if (dto.getContactPhone() != null) entity.setContactPhone(dto.getContactPhone());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("更新基层服务项目岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        entity.setIsDeleted(true);
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("软删除基层服务项目岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态不合法");
        }
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("更新基层服务项目岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = grassrootsProjectPositionMapper.update(null,
                Wrappers.lambdaUpdate(GrassrootsProjectPosition.class)
                        .set(GrassrootsProjectPosition::getIsDeleted, true)
                        .eq(GrassrootsProjectPosition::getIsDeleted, false)
                        .in(GrassrootsProjectPosition::getId, ids));
        log.info("批量删除基层服务项目岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<GrassrootsProjectPositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<GrassrootsProjectPositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        List<GrassrootsProjectPosition> entities = new ArrayList<>();
        List<String> duplicateWarnings = new ArrayList<>();
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            boolean exists = grassrootsProjectPositionMapper.selectCount(
                    Wrappers.lambdaQuery(GrassrootsProjectPosition.class)
                            .eq(GrassrootsProjectPosition::getPositionName, dto.getPositionName())
                            .eq(GrassrootsProjectPosition::getYear, dto.getYear())
                            .eq(GrassrootsProjectPosition::getProjectType, dto.getProjectType())
                            .eq(GrassrootsProjectPosition::getIsDeleted, false)) > 0;
            if (exists) {
                duplicateWarnings.add(dto.getPositionName() + "(" + dto.getYear() + "/" + dto.getProjectType() + ")");
                continue;
            }
            GrassrootsProjectPosition entity = GrassrootsProjectPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .projectType(dto.getProjectType())
                    .year(dto.getYear())
                    .positionName(dto.getPositionName())
                    .serviceType(dto.getServiceType())
                    .organizingDept(dto.getOrganizingDept())
                    .serviceUnit(dto.getServiceUnit())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .county(dto.getCounty())
                    .township(dto.getTownship())
                    .servicePeriod(dto.getServicePeriod())
                    .serviceStartDate(dto.getServiceStartDate())
                    .serviceEndDate(dto.getServiceEndDate())
                    .educationRequirement(dto.getEducationRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .gradYearRequirement(dto.getGradYearRequirement())
                    .householdRequirement(dto.getHouseholdRequirement())
                    .politicalStatus(dto.getPoliticalStatus())
                    .otherRequirement(dto.getOtherRequirement())
                    .examContent(dto.getExamContent())
                    .examTime(dto.getExamTime())
                    .interviewForm(dto.getInterviewForm())
                    .monthlySubsidy(dto.getMonthlySubsidy())
                    .socialInsurance(dto.getSocialInsurance())
                    .housingInfo(dto.getHousingInfo())
                    .otherBenefits(dto.getOtherBenefits())
                    .afterServicePolicy(dto.getAfterServicePolicy())
                    .canTransferToCivil(dto.getCanTransferToCivil())
                    .canTransferToInstitution(dto.getCanTransferToInstitution())
                    .examBonusPoints(dto.getExamBonusPoints())
                    .tuitionCompensation(dto.getTuitionCompensation())
                    .postgradBonus(dto.getPostgradBonus())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .applyLink(dto.getApplyLink())
                    .positionStatus(dto.getPositionStatus())
                    .contactPhone(dto.getContactPhone())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .build();
            entities.add(entity);
        }
        Db.saveBatch(entities);
        if (!duplicateWarnings.isEmpty()) {
            log.info("导入基层服务项目岗位: 跳过{}条重复记录: {}", duplicateWarnings.size(), String.join(", ", duplicateWarnings));
        }
        log.info("导入基层服务项目岗位成功: count={}", entities.size());
    }

    private String validateExcelRows(List<GrassrootsProjectPositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            validateString(errors, dto.getProjectType(), "项目类型", 30, true, VALID_PROJECT_TYPES);
            validateString(errors, dto.getYear(), "年份", 10, true);
            validateString(errors, dto.getPositionName(), "岗位名称", 200, true);
            validateString(errors, dto.getServiceType(), "服务类型", 50, true, VALID_SERVICE_TYPES);
            validateString(errors, dto.getServicePeriod(), "服务期限", 30, true);
            validateString(errors, dto.getEducationRequirement(), "学历要求", 30, true, VALID_EDUCATION_REQUIREMENTS);
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            validateString(errors, dto.getOrganizingDept(), "组织单位", 200, false);
            validateString(errors, dto.getServiceUnit(), "服务单位", 200, false);
            validateString(errors, dto.getCity(), "城市", 50, false);
            validateString(errors, dto.getCounty(), "区/县", 50, false);
            validateString(errors, dto.getTownship(), "乡镇/街道", 100, false);
            validateString(errors, dto.getMajorRequirement(), "专业要求", 500, false);
            validateString(errors, dto.getGradYearRequirement(), "毕业年份要求", 50, false);
            validateString(errors, dto.getHouseholdRequirement(), "户籍要求", 100, false);
            validateString(errors, dto.getPoliticalStatus(), "政治面貌", 30, false);
            validateString(errors, dto.getExamContent(), "笔试内容", 500, false);
            validateString(errors, dto.getInterviewForm(), "面试形式", 100, false);
            validateString(errors, dto.getMonthlySubsidy(), "月补贴标准", 50, false);
            validateString(errors, dto.getSocialInsurance(), "社保缴纳", 200, false);
            validateString(errors, dto.getHousingInfo(), "住房安排", 200, false);
            validateString(errors, dto.getExamBonusPoints(), "考试加分", 50, false);
            validateString(errors, dto.getTuitionCompensation(), "学费补偿", 100, false);
            validateString(errors, dto.getPostgradBonus(), "考研加分", 100, false);
            validateString(errors, dto.getApplyLink(), "报名链接", 500, false);
            validateString(errors, dto.getContactPhone(), "联系电话", 50, false);
            validateString(errors, dto.getPositionStatus(), "状态", 20, false, VALID_POSITION_STATUSES);
            validateInteger(errors, dto.getAgeLimit(), "年龄上限", 18, 35);
            validateInteger(errors, dto.getRecruitmentCount(), "招募人数", 1, null);
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

    private void validateString(List<String> errors, String value, String label, int maxLen, boolean required) {
        if (!StringUtils.hasText(value)) {
            if (required) errors.add(label + "不能为空");
            return;
        }
        if (value.length() > maxLen) errors.add(label + "长度不能超过" + maxLen);
    }

    private void validateString(List<String> errors, String value, String label, int maxLen, boolean required, Set<String> validValues) {
        if (!StringUtils.hasText(value)) {
            if (required) errors.add(label + "不能为空");
            return;
        }
        if (value.length() > maxLen) errors.add(label + "长度不能超过" + maxLen);
        else if (!validValues.contains(value)) errors.add(label + "不合法: " + value);
    }

    private void validateInteger(List<String> errors, Integer value, String label, Integer min, Integer max) {
        if (value == null) return;
        if (min != null && value < min) errors.add(label + "不能小于" + min);
        if (max != null && value > max) errors.add(label + "不能大于" + max);
    }

    private List<GrassrootsProjectPositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(GrassrootsProjectPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
