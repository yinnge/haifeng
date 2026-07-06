package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class GrassrootsProjectPositionServiceImpl implements GrassrootsProjectPositionService {

    private final GrassrootsProjectPositionMapper grassrootsProjectPositionMapper;

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
            BeanUtils.copyProperties(entity, vo);
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
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, GrassrootsProjectPositionUpdateDTO dto) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("更新基层服务项目岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        grassrootsProjectPositionMapper.deleteById(id);
        log.info("硬删除基层服务项目岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("更新基层服务项目岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
            if (entity != null) {
                grassrootsProjectPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除基层服务项目岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<GrassrootsProjectPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getProjectType())) {
                errors.add("项目类型不能为空");
            }
            if (!StringUtils.hasText(dto.getYear())) {
                errors.add("年份不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getServiceType())) {
                errors.add("服务类型不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!StringUtils.hasText(dto.getEducationRequirement())) {
                errors.add("学历要求不能为空");
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<GrassrootsProjectPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getProjectType())) errors.add("项目类型不能为空");
            if (!StringUtils.hasText(dto.getYear())) errors.add("年份不能为空");
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getServiceType())) errors.add("服务类型不能为空");
            if (!StringUtils.hasText(dto.getProvince())) errors.add("省份不能为空");
            else if (!ProvinceEnum.isValid(dto.getProvince())) errors.add("省份不合法: " + dto.getProvince());
            if (!StringUtils.hasText(dto.getEducationRequirement())) errors.add("学历要求不能为空");
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (GrassrootsProjectPositionExcelDTO dto : list) {
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
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            grassrootsProjectPositionMapper.insert(entity);
        }
        log.info("导入基层服务项目岗位成功: count={}", list.size());
    }

    private List<GrassrootsProjectPositionExcelDTO> readExcel(MultipartFile file) {
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
