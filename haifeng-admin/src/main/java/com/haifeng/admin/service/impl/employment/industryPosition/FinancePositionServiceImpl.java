package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionUpdateDTO;
import com.haifeng.admin.excel.employment.industryPosition.FinancePositionExcelDTO;
import com.haifeng.admin.service.employment.industryPosition.FinancePositionService;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionListVO;
import com.haifeng.common.entity.employment.industryPosition.FinancePosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.FinancePositionMapper;
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
public class FinancePositionServiceImpl implements FinancePositionService {

    private final FinancePositionMapper financePositionMapper;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束", "即将开始");
    private static final Set<String> VALID_INSTITUTION_CATEGORIES = Set.of(
            "银行", "证券", "保险", "基金", "信托", "期货", "监管机构", "金融科技");
    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of(
            "秋招", "春招", "社招", "实习", "定向");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of(
            "不限", "大专", "本科", "硕士", "博士");
    private static final int MAX_ERROR_DISPLAY = 20;

    @Override
    public IPage<FinancePositionListVO> page(FinancePositionQueryDTO dto) {
        Page<FinancePosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<FinancePosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancePosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getInstitutionName())) {
            wrapper.like(FinancePosition::getInstitutionName, dto.getInstitutionName());
        }
        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(FinancePosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getInstitutionCategory())) {
            wrapper.eq(FinancePosition::getInstitutionCategory, dto.getInstitutionCategory());
        }
        if (StringUtils.hasText(dto.getInstitutionType())) {
            wrapper.eq(FinancePosition::getInstitutionType, dto.getInstitutionType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(FinancePosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(FinancePosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(FinancePosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByDesc(FinancePosition::getSortOrder).orderByDesc(FinancePosition::getCreatedAt);

        IPage<FinancePosition> financePositionPage = financePositionMapper.selectPage(page, wrapper);

        return financePositionPage.convert(entity -> {
            FinancePositionListVO vo = new FinancePositionListVO();
            vo.setId(entity.getId());
            vo.setInstitutionName(entity.getInstitutionName());
            vo.setInstitutionCategory(entity.getInstitutionCategory());
            vo.setPositionName(entity.getPositionName());
            vo.setPositionCategory(entity.getPositionCategory());
            vo.setRecruitmentType(entity.getRecruitmentType());
            vo.setProvince(entity.getProvince());
            vo.setCity(entity.getCity());
            vo.setPositionStatus(entity.getPositionStatus());
            vo.setUpdatedAt(entity.getUpdatedAt());
            return vo;
        });
    }

    @Override
    public FinancePositionDetailVO detail(Long id) {
        FinancePosition entity = financePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        FinancePositionDetailVO vo = new FinancePositionDetailVO();
        vo.setId(entity.getId());
        vo.setInstitutionName(entity.getInstitutionName());
        vo.setInstitutionCategory(entity.getInstitutionCategory());
        vo.setInstitutionType(entity.getInstitutionType());
        vo.setInstitutionLogo(entity.getInstitutionLogo());
        vo.setBranchName(entity.getBranchName());
        vo.setPositionName(entity.getPositionName());
        vo.setPositionCategory(entity.getPositionCategory());
        vo.setRecruitmentType(entity.getRecruitmentType());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setWorkLocation(entity.getWorkLocation());
        vo.setIsRemote(entity.getIsRemote());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setDegreeRequirement(entity.getDegreeRequirement());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setMajorPreference(entity.getMajorPreference());
        vo.setAgeLimit(entity.getAgeLimit());
        vo.setWorkExperience(entity.getWorkExperience());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setCertRequirements(entity.getCertRequirements());
        vo.setLanguageRequirement(entity.getLanguageRequirement());
        vo.setComputerRequirement(entity.getComputerRequirement());
        vo.setOtherRequirement(entity.getOtherRequirement());
        vo.setSalaryMin(entity.getSalaryMin());
        vo.setSalaryMax(entity.getSalaryMax());
        vo.setSalaryText(entity.getSalaryText());
        vo.setBenefits(entity.getBenefits());
        vo.setExamContent(entity.getExamContent());
        vo.setExamTime(entity.getExamTime());
        vo.setInterviewRounds(entity.getInterviewRounds());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setApplyLink(entity.getApplyLink());
        vo.setPositionStatus(entity.getPositionStatus());
        vo.setContactInfo(entity.getContactInfo());
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
    public void update(Long id, FinancePositionUpdateDTO dto) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null || financePosition.getIsDeleted()) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        if (dto.getInstitutionName() != null) financePosition.setInstitutionName(dto.getInstitutionName());
        if (dto.getInstitutionCategory() != null) financePosition.setInstitutionCategory(dto.getInstitutionCategory());
        if (dto.getInstitutionType() != null) financePosition.setInstitutionType(dto.getInstitutionType());
        if (dto.getInstitutionLogo() != null) financePosition.setInstitutionLogo(dto.getInstitutionLogo());
        if (dto.getBranchName() != null) financePosition.setBranchName(dto.getBranchName());
        if (dto.getPositionName() != null) financePosition.setPositionName(dto.getPositionName());
        if (dto.getPositionCategory() != null) financePosition.setPositionCategory(dto.getPositionCategory());
        if (dto.getRecruitmentType() != null) financePosition.setRecruitmentType(dto.getRecruitmentType());
        if (dto.getProvince() != null) financePosition.setProvince(dto.getProvince());
        if (dto.getCity() != null) financePosition.setCity(dto.getCity());
        if (dto.getWorkLocation() != null) financePosition.setWorkLocation(dto.getWorkLocation());
        if (dto.getIsRemote() != null) financePosition.setIsRemote(dto.getIsRemote());
        if (dto.getEducationRequirement() != null) financePosition.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getDegreeRequirement() != null) financePosition.setDegreeRequirement(dto.getDegreeRequirement());
        if (dto.getMajorRequirement() != null) financePosition.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getMajorPreference() != null) financePosition.setMajorPreference(dto.getMajorPreference());
        if (dto.getAgeLimit() != null) financePosition.setAgeLimit(dto.getAgeLimit());
        if (dto.getWorkExperience() != null) financePosition.setWorkExperience(dto.getWorkExperience());
        if (dto.getRecruitmentCount() != null) financePosition.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getCertRequirements() != null) financePosition.setCertRequirements(dto.getCertRequirements());
        if (dto.getLanguageRequirement() != null) financePosition.setLanguageRequirement(dto.getLanguageRequirement());
        if (dto.getComputerRequirement() != null) financePosition.setComputerRequirement(dto.getComputerRequirement());
        if (dto.getOtherRequirement() != null) financePosition.setOtherRequirement(dto.getOtherRequirement());
        if (dto.getSalaryMin() != null) financePosition.setSalaryMin(dto.getSalaryMin());
        if (dto.getSalaryMax() != null) financePosition.setSalaryMax(dto.getSalaryMax());
        if (dto.getSalaryText() != null) financePosition.setSalaryText(dto.getSalaryText());
        if (dto.getBenefits() != null) financePosition.setBenefits(dto.getBenefits());
        if (dto.getExamContent() != null) financePosition.setExamContent(dto.getExamContent());
        if (dto.getExamTime() != null) financePosition.setExamTime(dto.getExamTime());
        if (dto.getInterviewRounds() != null) financePosition.setInterviewRounds(dto.getInterviewRounds());
        if (dto.getRegStartDate() != null) financePosition.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) financePosition.setRegEndDate(dto.getRegEndDate());
        if (dto.getApplyLink() != null) financePosition.setApplyLink(dto.getApplyLink());
        if (dto.getPositionStatus() != null) financePosition.setPositionStatus(dto.getPositionStatus());
        if (dto.getContactInfo() != null) financePosition.setContactInfo(dto.getContactInfo());
        if (dto.getRemark() != null) financePosition.setRemark(dto.getRemark());
        if (dto.getContent() != null) financePosition.setContent(dto.getContent());
        if (dto.getSortOrder() != null) financePosition.setSortOrder(dto.getSortOrder());
        financePositionMapper.updateById(financePosition);
        log.info("更新银行/金融招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null || financePosition.getIsDeleted()) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        financePosition.setIsDeleted(true);
        financePositionMapper.updateById(financePosition);
        log.info("软删除银行/金融招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态只能是: 招聘中、已结束、即将开始");
        }
        FinancePosition entity = financePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        financePositionMapper.updateById(entity);
        log.info("更新银行/金融招聘岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = financePositionMapper.update(null,
                Wrappers.lambdaUpdate(FinancePosition.class)
                        .set(FinancePosition::getIsDeleted, true)
                        .eq(FinancePosition::getIsDeleted, false)
                        .in(FinancePosition::getId, ids));
        log.info("批量删除银行/金融招聘岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<FinancePositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<FinancePositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<FinancePosition> entities = new ArrayList<>();
        for (FinancePositionExcelDTO dto : list) {
            FinancePosition entity = FinancePosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .institutionName(dto.getInstitutionName())
                    .institutionCategory(dto.getInstitutionCategory())
                    .institutionType(dto.getInstitutionType())
                    .institutionLogo(dto.getInstitutionLogo())
                    .branchName(dto.getBranchName())
                    .positionName(dto.getPositionName())
                    .positionCategory(dto.getPositionCategory())
                    .recruitmentType(dto.getRecruitmentType())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .workLocation(dto.getWorkLocation())
                    .isRemote(dto.getIsRemote())
                    .educationRequirement(dto.getEducationRequirement())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .majorPreference(dto.getMajorPreference())
                    .ageLimit(dto.getAgeLimit())
                    .workExperience(dto.getWorkExperience())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .certRequirements(dto.getCertRequirements())
                    .languageRequirement(dto.getLanguageRequirement())
                    .computerRequirement(dto.getComputerRequirement())
                    .otherRequirement(dto.getOtherRequirement())
                    .salaryMin(dto.getSalaryMin())
                    .salaryMax(dto.getSalaryMax())
                    .salaryText(dto.getSalaryText())
                    .benefits(dto.getBenefits())
                    .examContent(dto.getExamContent())
                    .examTime(dto.getExamTime())
                    .interviewRounds(dto.getInterviewRounds())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .applyLink(dto.getApplyLink())
                    .positionStatus(dto.getPositionStatus())
                    .contactInfo(dto.getContactInfo())
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
        log.info("导入银行/金融招聘岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<FinancePositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (FinancePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getInstitutionName())) {
                errors.add("机构名称不能为空");
            }
            if (StringUtils.hasText(dto.getInstitutionCategory())) {
                if (!VALID_INSTITUTION_CATEGORIES.contains(dto.getInstitutionCategory())) {
                    errors.add("机构大类不合法: " + dto.getInstitutionCategory());
                }
            } else {
                errors.add("机构大类不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (StringUtils.hasText(dto.getRecruitmentType())) {
                if (!VALID_RECRUITMENT_TYPES.contains(dto.getRecruitmentType())) {
                    errors.add("招聘类型不合法: " + dto.getRecruitmentType());
                }
            } else {
                errors.add("招聘类型不能为空");
            }
            if (StringUtils.hasText(dto.getProvince())
                    && !ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (StringUtils.hasText(dto.getEducationRequirement())
                    && !VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
                errors.add("学历要求只能是: 不限、大专、本科、硕士、博士");
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

    private List<FinancePositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(FinancePositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
