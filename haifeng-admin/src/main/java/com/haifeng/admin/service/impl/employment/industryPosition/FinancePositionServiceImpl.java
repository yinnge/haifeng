package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionAddDTO;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionUpdateDTO;
import com.haifeng.admin.excel.employment.industryPosition.FinancePositionExcelDTO;
import com.haifeng.admin.service.employment.industryPosition.FinancePositionService;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancePositionServiceImpl implements FinancePositionService {

    private final FinancePositionMapper financePositionMapper;

    private static final int MAX_IMPORT_ROWS = 1000;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束", "即将开始");
    private static final Set<String> VALID_INSTITUTION_CATEGORIES = Set.of(
            "银行", "证券", "保险", "基金", "信托", "期货", "监管机构", "金融科技");
    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of(
            "秋招", "春招", "社招", "实习", "定向");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of(
            "不限", "大专", "本科", "硕士", "博士");
    private static final int MAX_ERROR_DISPLAY = 50;

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
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
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
        if (financePosition == null || Boolean.TRUE.equals(financePosition.getIsDeleted())) {
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
    public Long add(FinancePositionAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
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
        financePositionMapper.insert(entity);
        log.info("新增银行/金融岗位成功: id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null || Boolean.TRUE.equals(financePosition.getIsDeleted())) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        financePositionMapper.physicalDeleteById(id);
        log.info("物理删除银行/金融招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态只能是: 招聘中、已结束、即将开始");
        }
        FinancePosition entity = financePositionMapper.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        financePositionMapper.updateById(entity);
        log.info("更新银行/金融招聘岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int deleted = financePositionMapper.physicalDeleteBatchIds(ids);
        log.info("批量物理删除银行/金融招聘岗位成功: requested={}, actual={}", ids.size(), deleted);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<FinancePositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importExcel(MultipartFile file) {
        List<FinancePositionExcelDTO> list = readExcel(file);
        String preErrors = validateExcelRows(list);
        if (preErrors != null) {
            throw new BusinessException(400, preErrors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<String> errors = new ArrayList<>();
        int success = 0;
        int updated = 0;
        int row = 1;
        for (FinancePositionExcelDTO dto : list) {
            row++;
            String org = StringUtils.hasText(dto.getInstitutionName()) ? dto.getInstitutionName() : "(机构名称空)";
            String pos = StringUtils.hasText(dto.getPositionName()) ? dto.getPositionName() : "(岗位名称空)";
            try {
                LambdaQueryWrapper<FinancePosition> wrapper = Wrappers.<FinancePosition>lambdaQuery()
                        .eq(FinancePosition::getInstitutionName, dto.getInstitutionName())
                        .eq(FinancePosition::getPositionName, dto.getPositionName())
                        .eq(FinancePosition::getIsDeleted, false);
                FinancePosition existing = financePositionMapper.selectOne(wrapper);
                if (existing != null) {
                    boolean changed = mergeFinanceIfBlank(existing, dto);
                    if (changed) {
                        existing.setUpdatedAt(now);
                        financePositionMapper.updateById(existing);
                        updated++;
                    }
                    success++;
                } else {
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
                    financePositionMapper.insert(entity);
                    success++;
                }
            } catch (Exception e) {
                errors.add("第" + row + "行: 数据库操作失败[" + org + " / " + pos + "]: " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            int show = Math.min(errors.size(), MAX_ERROR_DISPLAY);
            StringBuilder msg = new StringBuilder(String.join("\n", errors.subList(0, show)));
            if (errors.size() > MAX_ERROR_DISPLAY) {
                msg.append("\n...共").append(errors.size()).append("条错误，仅显示前")
                        .append(MAX_ERROR_DISPLAY).append("条，详见后端日志");
            }
            throw new BusinessException(400, msg.toString());
        }
        return ImportResultVO.builder()
                .total(list.size())
                .success(success)
                .failed(0)
                .updated(updated)
                .errors(Collections.emptyList())
                .build();
    }

    private boolean mergeFinanceIfBlank(FinancePosition existing, FinancePositionExcelDTO dto) {
        boolean changed = false;
        if (existing.getInstitutionName() == null && StringUtils.hasText(dto.getInstitutionName())) { existing.setInstitutionName(dto.getInstitutionName()); changed = true; }
        if (existing.getInstitutionCategory() == null && StringUtils.hasText(dto.getInstitutionCategory())) { existing.setInstitutionCategory(dto.getInstitutionCategory()); changed = true; }
        if (existing.getInstitutionType() == null && StringUtils.hasText(dto.getInstitutionType())) { existing.setInstitutionType(dto.getInstitutionType()); changed = true; }
        if (existing.getInstitutionLogo() == null && StringUtils.hasText(dto.getInstitutionLogo())) { existing.setInstitutionLogo(dto.getInstitutionLogo()); changed = true; }
        if (existing.getBranchName() == null && StringUtils.hasText(dto.getBranchName())) { existing.setBranchName(dto.getBranchName()); changed = true; }
        if (existing.getPositionName() == null && StringUtils.hasText(dto.getPositionName())) { existing.setPositionName(dto.getPositionName()); changed = true; }
        if (existing.getPositionCategory() == null && StringUtils.hasText(dto.getPositionCategory())) { existing.setPositionCategory(dto.getPositionCategory()); changed = true; }
        if (existing.getRecruitmentType() == null && StringUtils.hasText(dto.getRecruitmentType())) { existing.setRecruitmentType(dto.getRecruitmentType()); changed = true; }
        if (existing.getProvince() == null && StringUtils.hasText(dto.getProvince())) { existing.setProvince(dto.getProvince()); changed = true; }
        if (existing.getCity() == null && StringUtils.hasText(dto.getCity())) { existing.setCity(dto.getCity()); changed = true; }
        if (existing.getWorkLocation() == null && StringUtils.hasText(dto.getWorkLocation())) { existing.setWorkLocation(dto.getWorkLocation()); changed = true; }
        if (existing.getIsRemote() == null && dto.getIsRemote() != null) { existing.setIsRemote(dto.getIsRemote()); changed = true; }
        if (existing.getEducationRequirement() == null && StringUtils.hasText(dto.getEducationRequirement())) { existing.setEducationRequirement(dto.getEducationRequirement()); changed = true; }
        if (existing.getDegreeRequirement() == null && StringUtils.hasText(dto.getDegreeRequirement())) { existing.setDegreeRequirement(dto.getDegreeRequirement()); changed = true; }
        if (existing.getMajorRequirement() == null && StringUtils.hasText(dto.getMajorRequirement())) { existing.setMajorRequirement(dto.getMajorRequirement()); changed = true; }
        if (existing.getMajorPreference() == null || existing.getMajorPreference().isEmpty()) {
            if (dto.getMajorPreference() != null && !dto.getMajorPreference().isEmpty()) { existing.setMajorPreference(dto.getMajorPreference()); changed = true; }
        }
        if (existing.getAgeLimit() == null && dto.getAgeLimit() != null) { existing.setAgeLimit(dto.getAgeLimit()); changed = true; }
        if (existing.getWorkExperience() == null && StringUtils.hasText(dto.getWorkExperience())) { existing.setWorkExperience(dto.getWorkExperience()); changed = true; }
        if (existing.getRecruitmentCount() == null && dto.getRecruitmentCount() != null) { existing.setRecruitmentCount(dto.getRecruitmentCount()); changed = true; }
        if (existing.getCertRequirements() == null || existing.getCertRequirements().isEmpty()) {
            if (dto.getCertRequirements() != null && !dto.getCertRequirements().isEmpty()) { existing.setCertRequirements(dto.getCertRequirements()); changed = true; }
        }
        if (existing.getLanguageRequirement() == null && StringUtils.hasText(dto.getLanguageRequirement())) { existing.setLanguageRequirement(dto.getLanguageRequirement()); changed = true; }
        if (existing.getComputerRequirement() == null && StringUtils.hasText(dto.getComputerRequirement())) { existing.setComputerRequirement(dto.getComputerRequirement()); changed = true; }
        if (existing.getOtherRequirement() == null && StringUtils.hasText(dto.getOtherRequirement())) { existing.setOtherRequirement(dto.getOtherRequirement()); changed = true; }
        if (existing.getSalaryMin() == null && dto.getSalaryMin() != null) { existing.setSalaryMin(dto.getSalaryMin()); changed = true; }
        if (existing.getSalaryMax() == null && dto.getSalaryMax() != null) { existing.setSalaryMax(dto.getSalaryMax()); changed = true; }
        if (existing.getSalaryText() == null && StringUtils.hasText(dto.getSalaryText())) { existing.setSalaryText(dto.getSalaryText()); changed = true; }
        if (existing.getBenefits() == null && StringUtils.hasText(dto.getBenefits())) { existing.setBenefits(dto.getBenefits()); changed = true; }
        if (existing.getExamContent() == null && StringUtils.hasText(dto.getExamContent())) { existing.setExamContent(dto.getExamContent()); changed = true; }
        if (existing.getExamTime() == null && dto.getExamTime() != null) { existing.setExamTime(dto.getExamTime()); changed = true; }
        if (existing.getInterviewRounds() == null && StringUtils.hasText(dto.getInterviewRounds())) { existing.setInterviewRounds(dto.getInterviewRounds()); changed = true; }
        if (existing.getRegStartDate() == null && dto.getRegStartDate() != null) { existing.setRegStartDate(dto.getRegStartDate()); changed = true; }
        if (existing.getRegEndDate() == null && dto.getRegEndDate() != null) { existing.setRegEndDate(dto.getRegEndDate()); changed = true; }
        if (existing.getApplyLink() == null && StringUtils.hasText(dto.getApplyLink())) { existing.setApplyLink(dto.getApplyLink()); changed = true; }
        if (existing.getPositionStatus() == null && StringUtils.hasText(dto.getPositionStatus())) { existing.setPositionStatus(dto.getPositionStatus()); changed = true; }
        if (existing.getContactInfo() == null && StringUtils.hasText(dto.getContactInfo())) { existing.setContactInfo(dto.getContactInfo()); changed = true; }
        if (existing.getRemark() == null && StringUtils.hasText(dto.getRemark())) { existing.setRemark(dto.getRemark()); changed = true; }
        if (existing.getContent() == null && StringUtils.hasText(dto.getContent())) { existing.setContent(dto.getContent()); changed = true; }
        if (existing.getSortOrder() == null && dto.getSortOrder() != null) { existing.setSortOrder(dto.getSortOrder()); changed = true; }
        return changed;
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
            if (dto.getAgeLimit() != null && (dto.getAgeLimit() < 18 || dto.getAgeLimit() > 45)) {
                errors.add("年龄上限须在18-45之间");
            }
            if (dto.getRecruitmentCount() != null && dto.getRecruitmentCount() <= 0) {
                errors.add("招聘人数必须大于0");
            }
            if (dto.getSalaryMin() != null && dto.getSalaryMax() != null && dto.getSalaryMin() > dto.getSalaryMax()) {
                errors.add("最低月薪不能高于最高月薪");
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
            List<FinancePositionExcelDTO> importList = EasyExcel.read(file.getInputStream())
                    .head(FinancePositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
            if (importList == null || importList.isEmpty()) {
                throw new BusinessException(400, "Excel文件中没有数据");
            }
            if (importList.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
            }
            // 归一化字符串字段首尾空格：province 防 ProvinceEnum 误报，枚举字段(institutionCategory/recruitmentType/educationRequirement/positionStatus)防误报
            for (FinancePositionExcelDTO dto : importList) {
                if (dto.getProvince() != null) dto.setProvince(dto.getProvince().trim());
                if (dto.getInstitutionCategory() != null) dto.setInstitutionCategory(dto.getInstitutionCategory().trim());
                if (dto.getRecruitmentType() != null) dto.setRecruitmentType(dto.getRecruitmentType().trim());
                if (dto.getEducationRequirement() != null) dto.setEducationRequirement(dto.getEducationRequirement().trim());
                if (dto.getPositionStatus() != null) dto.setPositionStatus(dto.getPositionStatus().trim());
            }
            return importList;
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }
    }
}
