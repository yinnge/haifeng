package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class HealthcarePositionServiceImpl implements HealthcarePositionService {

    private final HealthcarePositionMapper healthcarePositionMapper;

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

        return healthcarePositionPage.convert(healthcarePosition -> {
            HealthcarePositionListVO vo = new HealthcarePositionListVO();
            BeanUtils.copyProperties(healthcarePosition, vo);
            return vo;
        });
    }

    @Override
    public HealthcarePositionDetailVO detail(Long id) {
        HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
        if (healthcarePosition == null || healthcarePosition.getIsDeleted()) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        HealthcarePositionDetailVO vo = new HealthcarePositionDetailVO();
        BeanUtils.copyProperties(healthcarePosition, vo);
        return vo;
    }

    @Override
    public void update(Long id, HealthcarePositionUpdateDTO dto) {
        HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
        if (healthcarePosition == null || healthcarePosition.getIsDeleted()) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        BeanUtils.copyProperties(dto, healthcarePosition);
        healthcarePosition.setUpdatedAt(OffsetDateTime.now());
        healthcarePositionMapper.updateById(healthcarePosition);
        log.info("更新医疗卫生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
        if (healthcarePosition == null) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        healthcarePositionMapper.deleteById(id);
        log.info("硬删除医疗卫生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
        if (healthcarePosition == null) {
            throw new BusinessException(404, "医疗卫生岗位不存在");
        }
        healthcarePosition.setIsDeleted(status == 0);
        healthcarePosition.setUpdatedAt(OffsetDateTime.now());
        healthcarePositionMapper.updateById(healthcarePosition);
        log.info("更新医疗卫生岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            HealthcarePosition healthcarePosition = healthcarePositionMapper.selectById(id);
            if (healthcarePosition != null) {
                healthcarePositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除医疗卫生岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<HealthcarePositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (HealthcarePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getInstitutionName())) {
                errors.add("医疗机构名称不能为空");
            }
            if (!StringUtils.hasText(dto.getInstitutionType())) {
                errors.add("机构类型不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getDepartment())) {
                errors.add("科室不能为空");
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
        List<HealthcarePositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (HealthcarePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getInstitutionName())) errors.add("医疗机构名称不能为空");
            if (!StringUtils.hasText(dto.getInstitutionType())) errors.add("机构类型不能为空");
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getDepartment())) errors.add("科室不能为空");
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
            healthcarePositionMapper.insert(entity);
        }
        log.info("导入医疗卫生岗位成功: count={}", list.size());
    }

    private List<HealthcarePositionExcelDTO> readExcel(MultipartFile file) {
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
