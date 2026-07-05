package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicWelfarePositionServiceImpl implements PublicWelfarePositionService {

    private final PublicWelfarePositionMapper publicWelfarePositionMapper;

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
            BeanUtils.copyProperties(entity, vo);
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
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, PublicWelfarePositionUpdateDTO dto) {
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        publicWelfarePositionMapper.updateById(entity);
        log.info("更新公益性岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        publicWelfarePositionMapper.deleteById(id);
        log.info("硬删除公益性岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公益性岗位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        publicWelfarePositionMapper.updateById(entity);
        log.info("更新公益性岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            PublicWelfarePosition entity = publicWelfarePositionMapper.selectById(id);
            if (entity != null) {
                publicWelfarePositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除公益性岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<PublicWelfarePositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (PublicWelfarePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getDevelopingUnit())) {
                errors.add("开发单位不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!StringUtils.hasText(dto.getPositionCategory())) {
                errors.add("岗位类别不能为空");
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
        List<PublicWelfarePositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (PublicWelfarePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getDevelopingUnit())) errors.add("开发单位不能为空");
            if (!StringUtils.hasText(dto.getProvince())) errors.add("省份不能为空");
            else if (!ProvinceEnum.isValid(dto.getProvince())) errors.add("省份不合法: " + dto.getProvince());
            if (!StringUtils.hasText(dto.getPositionCategory())) errors.add("岗位类别不能为空");
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
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
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            publicWelfarePositionMapper.insert(entity);
        }
        log.info("导入公益性岗位成功: count={}", list.size());
    }

    private List<PublicWelfarePositionExcelDTO> readExcel(MultipartFile file) {
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
