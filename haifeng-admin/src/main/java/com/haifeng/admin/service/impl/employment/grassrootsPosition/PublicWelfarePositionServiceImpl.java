package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionUpdateDTO;
import com.haifeng.admin.excel.employment.grassrootsPosition.PublicWelfarePositionExcelDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.PublicWelfarePosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.PublicWelfarePositionMapper;
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
public class PublicWelfarePositionServiceImpl implements PublicWelfarePositionService {

    private final PublicWelfarePositionMapper publicWelfarePositionMapper;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束", "即将开始");
    private static final Set<String> VALID_POSITION_CATEGORIES = Set.of("公共管理类", "公共服务类", "公共环境类", "公共安全类", "设施维护类", "其他");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("不限", "初中", "高中", "大专", "本科");
    private static final int MAX_ERROR_DISPLAY = 20;

    @Override
    public IPage<PublicWelfarePositionListVO> page(PublicWelfarePositionQueryDTO dto) {
        Page<PublicWelfarePosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<PublicWelfarePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublicWelfarePosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(PublicWelfarePosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getDevelopingUnit())) {
            wrapper.like(PublicWelfarePosition::getDevelopingUnit, dto.getDevelopingUnit());
        }
        if (StringUtils.hasText(dto.getEmployingUnit())) {
            wrapper.like(PublicWelfarePosition::getEmployingUnit, dto.getEmployingUnit());
        }
        if (StringUtils.hasText(dto.getPositionCategory())) {
            wrapper.eq(PublicWelfarePosition::getPositionCategory, dto.getPositionCategory());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(PublicWelfarePosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(PublicWelfarePosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getDistrict())) {
            wrapper.eq(PublicWelfarePosition::getDistrict, dto.getDistrict());
        }
        if (dto.getMaxServiceYears() != null) {
            wrapper.eq(PublicWelfarePosition::getMaxServiceYears, dto.getMaxServiceYears());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(PublicWelfarePosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(PublicWelfarePosition::getSortOrder).orderByDesc(PublicWelfarePosition::getUpdatedAt);

        IPage<PublicWelfarePosition> entityPage = publicWelfarePositionMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            PublicWelfarePositionListVO vo = new PublicWelfarePositionListVO();
            vo.setId(entity.getId());
            vo.setDevelopingUnit(entity.getDevelopingUnit());
            vo.setEmployingUnit(entity.getEmployingUnit());
            vo.setPositionName(entity.getPositionName());
            vo.setPositionCategory(entity.getPositionCategory());
            vo.setProvince(entity.getProvince());
            vo.setCity(entity.getCity());
            vo.setDistrict(entity.getDistrict());
            vo.setMonthlySalary(entity.getMonthlySalary());
            vo.setRegStartDate(entity.getRegStartDate());
            vo.setRegEndDate(entity.getRegEndDate());
            vo.setPositionStatus(entity.getPositionStatus());
            return vo;
        });
    }

    @Override
    public PublicWelfarePositionDetailVO detail(Long id) {
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        PublicWelfarePositionDetailVO vo = new PublicWelfarePositionDetailVO();
        vo.setId(entity.getId());
        vo.setDevelopingUnit(entity.getDevelopingUnit());
        vo.setEmployingUnit(entity.getEmployingUnit());
        vo.setPositionName(entity.getPositionName());
        vo.setPositionCategory(entity.getPositionCategory());
        vo.setWorkContent(entity.getWorkContent());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setWorkLocation(entity.getWorkLocation());
        vo.setTargetGroup(entity.getTargetGroup());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setAgeRange(entity.getAgeRange());
        vo.setHealthRequirement(entity.getHealthRequirement());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setHouseholdRequirement(entity.getHouseholdRequirement());
        vo.setEmploymentDifficultyCert(entity.getEmploymentDifficultyCert());
        vo.setOtherRequirement(entity.getOtherRequirement());
        vo.setContractPeriod(entity.getContractPeriod());
        vo.setIsRenewable(entity.getIsRenewable());
        vo.setMaxServiceYears(entity.getMaxServiceYears());
        vo.setMonthlySalary(entity.getMonthlySalary());
        vo.setSalarySource(entity.getSalarySource());
        vo.setSubsidyStandard(entity.getSubsidyStandard());
        vo.setSocialInsuranceInfo(entity.getSocialInsuranceInfo());
        vo.setOtherBenefits(entity.getOtherBenefits());
        vo.setWorkSchedule(entity.getWorkSchedule());
        vo.setIsShiftWork(entity.getIsShiftWork());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setApplyMethod(entity.getApplyMethod());
        vo.setApplyAddress(entity.getApplyAddress());
        vo.setRequiredDocuments(entity.getRequiredDocuments());
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
    public void update(Long id, PublicWelfarePositionUpdateDTO dto) {
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        if (dto.getDevelopingUnit() != null) entity.setDevelopingUnit(dto.getDevelopingUnit());
        if (dto.getEmployingUnit() != null) entity.setEmployingUnit(dto.getEmployingUnit());
        if (dto.getPositionName() != null) entity.setPositionName(dto.getPositionName());
        if (dto.getPositionCategory() != null) entity.setPositionCategory(dto.getPositionCategory());
        if (dto.getWorkContent() != null) entity.setWorkContent(dto.getWorkContent());
        if (dto.getProvince() != null) entity.setProvince(dto.getProvince());
        if (dto.getCity() != null) entity.setCity(dto.getCity());
        if (dto.getDistrict() != null) entity.setDistrict(dto.getDistrict());
        if (dto.getWorkLocation() != null) entity.setWorkLocation(dto.getWorkLocation());
        if (dto.getTargetGroup() != null) entity.setTargetGroup(dto.getTargetGroup());
        if (dto.getEducationRequirement() != null) entity.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getAgeRange() != null) entity.setAgeRange(dto.getAgeRange());
        if (dto.getHealthRequirement() != null) entity.setHealthRequirement(dto.getHealthRequirement());
        if (dto.getRecruitmentCount() != null) entity.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getHouseholdRequirement() != null) entity.setHouseholdRequirement(dto.getHouseholdRequirement());
        if (dto.getEmploymentDifficultyCert() != null) entity.setEmploymentDifficultyCert(dto.getEmploymentDifficultyCert());
        if (dto.getOtherRequirement() != null) entity.setOtherRequirement(dto.getOtherRequirement());
        if (dto.getContractPeriod() != null) entity.setContractPeriod(dto.getContractPeriod());
        if (dto.getIsRenewable() != null) entity.setIsRenewable(dto.getIsRenewable());
        if (dto.getMaxServiceYears() != null) entity.setMaxServiceYears(dto.getMaxServiceYears());
        if (dto.getMonthlySalary() != null) entity.setMonthlySalary(dto.getMonthlySalary());
        if (dto.getSalarySource() != null) entity.setSalarySource(dto.getSalarySource());
        if (dto.getSubsidyStandard() != null) entity.setSubsidyStandard(dto.getSubsidyStandard());
        if (dto.getSocialInsuranceInfo() != null) entity.setSocialInsuranceInfo(dto.getSocialInsuranceInfo());
        if (dto.getOtherBenefits() != null) entity.setOtherBenefits(dto.getOtherBenefits());
        if (dto.getWorkSchedule() != null) entity.setWorkSchedule(dto.getWorkSchedule());
        if (dto.getIsShiftWork() != null) entity.setIsShiftWork(dto.getIsShiftWork());
        if (dto.getRegStartDate() != null) entity.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) entity.setRegEndDate(dto.getRegEndDate());
        if (dto.getApplyMethod() != null) entity.setApplyMethod(dto.getApplyMethod());
        if (dto.getApplyAddress() != null) entity.setApplyAddress(dto.getApplyAddress());
        if (dto.getRequiredDocuments() != null) entity.setRequiredDocuments(dto.getRequiredDocuments());
        if (dto.getPositionStatus() != null) entity.setPositionStatus(dto.getPositionStatus());
        if (dto.getContactPhone() != null) entity.setContactPhone(dto.getContactPhone());
        if (dto.getContactPerson() != null) entity.setContactPerson(dto.getContactPerson());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        publicWelfarePositionMapper.updateById(entity);
        log.info("更新公益性岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        entity.setIsDeleted(true);
        publicWelfarePositionMapper.updateById(entity);
        log.info("软删除公益性岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态不合法");
        }
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        publicWelfarePositionMapper.updateById(entity);
        log.info("更新公益性岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = publicWelfarePositionMapper.update(null,
                Wrappers.lambdaUpdate(PublicWelfarePosition.class)
                        .set(PublicWelfarePosition::getIsDeleted, true)
                        .eq(PublicWelfarePosition::getIsDeleted, false)
                        .in(PublicWelfarePosition::getId, ids));
        log.info("批量删除公益性岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<PublicWelfarePositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<PublicWelfarePositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        List<PublicWelfarePosition> entities = new ArrayList<>();
        for (PublicWelfarePositionExcelDTO dto : list) {
            PublicWelfarePosition entity = PublicWelfarePosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .developingUnit(dto.getDevelopingUnit())
                    .employingUnit(dto.getEmployingUnit())
                    .positionName(dto.getPositionName())
                    .positionCategory(dto.getPositionCategory())
                    .workContent(dto.getWorkContent())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .district(dto.getDistrict())
                    .workLocation(dto.getWorkLocation())
                    .targetGroup(dto.getTargetGroup())
                    .educationRequirement(dto.getEducationRequirement())
                    .ageRange(dto.getAgeRange())
                    .healthRequirement(dto.getHealthRequirement())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .householdRequirement(dto.getHouseholdRequirement())
                    .employmentDifficultyCert(dto.getEmploymentDifficultyCert())
                    .otherRequirement(dto.getOtherRequirement())
                    .contractPeriod(dto.getContractPeriod())
                    .isRenewable(dto.getIsRenewable())
                    .maxServiceYears(dto.getMaxServiceYears())
                    .monthlySalary(dto.getMonthlySalary())
                    .salarySource(dto.getSalarySource())
                    .subsidyStandard(dto.getSubsidyStandard())
                    .socialInsuranceInfo(dto.getSocialInsuranceInfo())
                    .otherBenefits(dto.getOtherBenefits())
                    .workSchedule(dto.getWorkSchedule())
                    .isShiftWork(dto.getIsShiftWork())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .applyMethod(dto.getApplyMethod())
                    .applyAddress(dto.getApplyAddress())
                    .requiredDocuments(dto.getRequiredDocuments())
                    .positionStatus(dto.getPositionStatus())
                    .contactPhone(dto.getContactPhone())
                    .contactPerson(dto.getContactPerson())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .build();
            entities.add(entity);
        }
        Db.saveBatch(entities);
        log.info("导入公益性岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<PublicWelfarePositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (PublicWelfarePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            validateString(errors, dto.getPositionCategory(), "岗位类别", 50, true, VALID_POSITION_CATEGORIES);
            validateString(errors, dto.getDevelopingUnit(), "开发单位", 200, true);
            validateString(errors, dto.getPositionName(), "岗位名称", 200, true);
            validateString(errors, dto.getCity(), "城市", 50, true);
            validateString(errors, dto.getContractPeriod(), "合同期限", 30, true);
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            validateString(errors, dto.getEmployingUnit(), "用工单位", 200, false);
            validateString(errors, dto.getDistrict(), "区/县", 50, false);
            validateString(errors, dto.getWorkLocation(), "工作地点", 200, false);
            validateString(errors, dto.getAgeRange(), "年龄范围", 50, false);
            validateString(errors, dto.getHealthRequirement(), "身体条件", 200, false);
            validateString(errors, dto.getHouseholdRequirement(), "户籍要求", 100, false);
            validateString(errors, dto.getMonthlySalary(), "月工资", 50, false);
            validateString(errors, dto.getSalarySource(), "工资来源", 100, false);
            validateString(errors, dto.getSubsidyStandard(), "补贴标准", 200, false);
            validateString(errors, dto.getSocialInsuranceInfo(), "社保缴纳", 200, false);
            validateString(errors, dto.getWorkSchedule(), "工作时间", 100, false);
            validateString(errors, dto.getApplyAddress(), "报名地址", 200, false);
            validateString(errors, dto.getContactPhone(), "联系电话", 50, false);
            validateString(errors, dto.getContactPerson(), "联系人", 50, false);
            validateString(errors, dto.getEducationRequirement(), "学历要求", 30, false, VALID_EDUCATION_REQUIREMENTS);
            validateString(errors, dto.getPositionStatus(), "状态", 20, false, VALID_POSITION_STATUSES);
            validateInteger(errors, dto.getRecruitmentCount(), "招聘人数", 1, null);
            validateInteger(errors, dto.getMaxServiceYears(), "最长服务年限", 1, null);
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

    private List<PublicWelfarePositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(PublicWelfarePositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
