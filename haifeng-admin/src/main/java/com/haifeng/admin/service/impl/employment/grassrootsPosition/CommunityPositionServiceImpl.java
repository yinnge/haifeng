package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionUpdateDTO;
import com.haifeng.admin.excel.employment.grassrootsPosition.CommunityPositionExcelDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.CommunityPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.CommunityPositionMapper;
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
public class CommunityPositionServiceImpl implements CommunityPositionService {

    private final CommunityPositionMapper communityPositionMapper;

    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束", "即将开始");
    private static final Set<String> VALID_POSITION_TYPES = Set.of("社区党务工作者", "社区服务工作者", "社区网格员", "社区调解员", "社区安全员", "社区文化专干", "社会工作师", "综合岗", "其他");
    private static final Set<String> VALID_EMPLOYMENT_TYPES = Set.of("事业编制", "合同制", "政府购买服务", "公益性岗位");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("不限", "高中", "大专", "本科", "硕士");
    private static final Set<String> VALID_SOCIAL_WORK_CERTS = Set.of("不要求", "初级社工师", "中级社工师", "高级社工师", "优先");
    private static final int MAX_ERROR_DISPLAY = 20;

    @Override
    public IPage<CommunityPositionListVO> page(CommunityPositionQueryDTO dto) {
        Page<CommunityPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<CommunityPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(CommunityPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getCommunityName())) {
            wrapper.like(CommunityPosition::getCommunityName, dto.getCommunityName());
        }
        if (StringUtils.hasText(dto.getSupervisingDept())) {
            wrapper.like(CommunityPosition::getSupervisingDept, dto.getSupervisingDept());
        }
        if (StringUtils.hasText(dto.getPositionType())) {
            wrapper.eq(CommunityPosition::getPositionType, dto.getPositionType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(CommunityPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(CommunityPosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(CommunityPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(CommunityPosition::getSortOrder).orderByDesc(CommunityPosition::getUpdatedAt);

        IPage<CommunityPosition> entityPage = communityPositionMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            CommunityPositionListVO vo = new CommunityPositionListVO();
            vo.setId(entity.getId());
            vo.setCommunityName(entity.getCommunityName());
            vo.setPositionName(entity.getPositionName());
            vo.setSupervisingDept(entity.getSupervisingDept());
            vo.setPositionType(entity.getPositionType());
            vo.setProvince(entity.getProvince());
            vo.setCity(entity.getCity());
            vo.setPositionStatus(entity.getPositionStatus());
            return vo;
        });
    }

    @Override
    public CommunityPositionDetailVO detail(Long id) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        CommunityPositionDetailVO vo = new CommunityPositionDetailVO();
        vo.setId(entity.getId());
        vo.setStreetOffice(entity.getStreetOffice());
        vo.setCommunityName(entity.getCommunityName());
        vo.setSupervisingDept(entity.getSupervisingDept());
        vo.setDistrict(entity.getDistrict());
        vo.setPositionName(entity.getPositionName());
        vo.setPositionType(entity.getPositionType());
        vo.setEmploymentType(entity.getEmploymentType());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setWorkLocation(entity.getWorkLocation());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setAgeLimit(entity.getAgeLimit());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setHouseholdRequirement(entity.getHouseholdRequirement());
        vo.setPoliticalStatus(entity.getPoliticalStatus());
        vo.setWorkExperience(entity.getWorkExperience());
        vo.setSocialWorkCert(entity.getSocialWorkCert());
        vo.setCommunityExperience(entity.getCommunityExperience());
        vo.setResidenceRequirement(entity.getResidenceRequirement());
        vo.setSalaryRange(entity.getSalaryRange());
        vo.setSalaryComposition(entity.getSalaryComposition());
        vo.setBenefits(entity.getBenefits());
        vo.setExamContent(entity.getExamContent());
        vo.setInterviewForm(entity.getInterviewForm());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setExamTime(entity.getExamTime());
        vo.setPositionStatus(entity.getPositionStatus());
        vo.setApplyLink(entity.getApplyLink());
        vo.setApplyMethod(entity.getApplyMethod());
        vo.setContactPhone(entity.getContactPhone());
        vo.setContactAddress(entity.getContactAddress());
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
    public void update(Long id, CommunityPositionUpdateDTO dto) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        if (dto.getStreetOffice() != null) entity.setStreetOffice(dto.getStreetOffice());
        if (dto.getCommunityName() != null) entity.setCommunityName(dto.getCommunityName());
        if (dto.getSupervisingDept() != null) entity.setSupervisingDept(dto.getSupervisingDept());
        if (dto.getDistrict() != null) entity.setDistrict(dto.getDistrict());
        if (dto.getPositionName() != null) entity.setPositionName(dto.getPositionName());
        if (dto.getPositionType() != null) entity.setPositionType(dto.getPositionType());
        if (dto.getEmploymentType() != null) entity.setEmploymentType(dto.getEmploymentType());
        if (dto.getProvince() != null) entity.setProvince(dto.getProvince());
        if (dto.getCity() != null) entity.setCity(dto.getCity());
        if (dto.getWorkLocation() != null) entity.setWorkLocation(dto.getWorkLocation());
        if (dto.getEducationRequirement() != null) entity.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getAgeLimit() != null) entity.setAgeLimit(dto.getAgeLimit());
        if (dto.getRecruitmentCount() != null) entity.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getMajorRequirement() != null) entity.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getHouseholdRequirement() != null) entity.setHouseholdRequirement(dto.getHouseholdRequirement());
        if (dto.getPoliticalStatus() != null) entity.setPoliticalStatus(dto.getPoliticalStatus());
        if (dto.getWorkExperience() != null) entity.setWorkExperience(dto.getWorkExperience());
        if (dto.getSocialWorkCert() != null) entity.setSocialWorkCert(dto.getSocialWorkCert());
        if (dto.getCommunityExperience() != null) entity.setCommunityExperience(dto.getCommunityExperience());
        if (dto.getResidenceRequirement() != null) entity.setResidenceRequirement(dto.getResidenceRequirement());
        if (dto.getSalaryRange() != null) entity.setSalaryRange(dto.getSalaryRange());
        if (dto.getSalaryComposition() != null) entity.setSalaryComposition(dto.getSalaryComposition());
        if (dto.getBenefits() != null) entity.setBenefits(dto.getBenefits());
        if (dto.getExamContent() != null) entity.setExamContent(dto.getExamContent());
        if (dto.getInterviewForm() != null) entity.setInterviewForm(dto.getInterviewForm());
        if (dto.getRegStartDate() != null) entity.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) entity.setRegEndDate(dto.getRegEndDate());
        if (dto.getExamTime() != null) entity.setExamTime(dto.getExamTime());
        if (dto.getPositionStatus() != null) entity.setPositionStatus(dto.getPositionStatus());
        if (dto.getApplyLink() != null) entity.setApplyLink(dto.getApplyLink());
        if (dto.getApplyMethod() != null) entity.setApplyMethod(dto.getApplyMethod());
        if (dto.getContactPhone() != null) entity.setContactPhone(dto.getContactPhone());
        if (dto.getContactAddress() != null) entity.setContactAddress(dto.getContactAddress());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        communityPositionMapper.updateById(entity);
        log.info("更新社区工作者岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        entity.setIsDeleted(true);
        communityPositionMapper.updateById(entity);
        log.info("软删除社区工作者岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态不合法");
        }
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        communityPositionMapper.updateById(entity);
        log.info("更新社区工作者岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = communityPositionMapper.update(null,
                Wrappers.lambdaUpdate(CommunityPosition.class)
                        .set(CommunityPosition::getIsDeleted, true)
                        .eq(CommunityPosition::getIsDeleted, false)
                        .in(CommunityPosition::getId, ids));
        log.info("批量删除社区工作者岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<CommunityPositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<CommunityPositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        List<CommunityPosition> entities = new ArrayList<>();
        for (CommunityPositionExcelDTO dto : list) {
            CommunityPosition entity = CommunityPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .streetOffice(dto.getStreetOffice())
                    .communityName(dto.getCommunityName())
                    .supervisingDept(dto.getSupervisingDept())
                    .district(dto.getDistrict())
                    .positionName(dto.getPositionName())
                    .positionType(dto.getPositionType())
                    .employmentType(dto.getEmploymentType())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .workLocation(dto.getWorkLocation())
                    .educationRequirement(dto.getEducationRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .majorRequirement(dto.getMajorRequirement())
                    .householdRequirement(dto.getHouseholdRequirement())
                    .politicalStatus(dto.getPoliticalStatus())
                    .workExperience(dto.getWorkExperience())
                    .socialWorkCert(dto.getSocialWorkCert())
                    .communityExperience(dto.getCommunityExperience())
                    .residenceRequirement(dto.getResidenceRequirement())
                    .salaryRange(dto.getSalaryRange())
                    .salaryComposition(dto.getSalaryComposition())
                    .benefits(dto.getBenefits())
                    .examContent(dto.getExamContent())
                    .interviewForm(dto.getInterviewForm())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .positionStatus(dto.getPositionStatus())
                    .applyLink(dto.getApplyLink())
                    .applyMethod(dto.getApplyMethod())
                    .contactPhone(dto.getContactPhone())
                    .contactAddress(dto.getContactAddress())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .build();
            entities.add(entity);
        }
        Db.saveBatch(entities);
        log.info("导入社区工作者岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<CommunityPositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (CommunityPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            validateString(errors, dto.getPositionName(), "岗位名称", 200, true);
            validateString(errors, dto.getStreetOffice(), "街道办事处/乡镇", 200, true);
            validateString(errors, dto.getPositionType(), "岗位类型", 50, true, VALID_POSITION_TYPES);
            validateString(errors, dto.getEmploymentType(), "用工形式", 30, true, VALID_EMPLOYMENT_TYPES);
            validateString(errors, dto.getCity(), "城市", 50, true);
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            validateString(errors, dto.getCommunityName(), "社区名称", 200, false);
            validateString(errors, dto.getSupervisingDept(), "主管部门", 200, false);
            validateString(errors, dto.getDistrict(), "区/县", 100, false);
            validateString(errors, dto.getWorkLocation(), "工作地点", 200, false);
            validateString(errors, dto.getMajorRequirement(), "专业要求", 500, false);
            validateString(errors, dto.getHouseholdRequirement(), "户籍要求", 100, false);
            validateString(errors, dto.getPoliticalStatus(), "政治面貌", 30, false);
            validateString(errors, dto.getWorkExperience(), "工作经验", 50, false);
            validateString(errors, dto.getCommunityExperience(), "社区经验要求", 100, false);
            validateString(errors, dto.getResidenceRequirement(), "居住地要求", 200, false);
            validateString(errors, dto.getSalaryRange(), "薪资待遇", 50, false);
            validateString(errors, dto.getSalaryComposition(), "薪资构成", 200, false);
            validateString(errors, dto.getExamContent(), "笔试内容", 500, false);
            validateString(errors, dto.getInterviewForm(), "面试形式", 100, false);
            validateString(errors, dto.getApplyLink(), "报名链接", 500, false);
            validateString(errors, dto.getContactPhone(), "联系电话", 50, false);
            validateString(errors, dto.getContactAddress(), "报名地址", 200, false);
            validateString(errors, dto.getEducationRequirement(), "学历要求", 30, false, VALID_EDUCATION_REQUIREMENTS);
            validateString(errors, dto.getSocialWorkCert(), "社工证要求", 50, false, VALID_SOCIAL_WORK_CERTS);
            validateString(errors, dto.getPositionStatus(), "状态", 20, false, VALID_POSITION_STATUSES);
            validateInteger(errors, dto.getAgeLimit(), "年龄上限", 18, 55);
            validateInteger(errors, dto.getRecruitmentCount(), "招聘人数", 1, null);
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

    private List<CommunityPositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(CommunityPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
