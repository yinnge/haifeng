package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.finance.FinancePositionUpdateDTO;
import com.haifeng.admin.excel.employment.industryPosition.FinancePositionExcelDTO;
import com.haifeng.admin.service.employment.industryPosition.FinancePositionService;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.finance.FinancePositionListVO;
import com.haifeng.common.entity.employment.industryPosition.FinancePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.FinancePositionMapper;
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
public class FinancePositionServiceImpl implements FinancePositionService {

    private final FinancePositionMapper financePositionMapper;

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

        return financePositionPage.convert(financePosition -> {
            FinancePositionListVO vo = new FinancePositionListVO();
            BeanUtils.copyProperties(financePosition, vo);
            return vo;
        });
    }

    @Override
    public FinancePositionDetailVO detail(Long id) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null || financePosition.getIsDeleted()) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        FinancePositionDetailVO vo = new FinancePositionDetailVO();
        BeanUtils.copyProperties(financePosition, vo);
        return vo;
    }

    @Override
    public void update(Long id, FinancePositionUpdateDTO dto) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null || financePosition.getIsDeleted()) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        BeanUtils.copyProperties(dto, financePosition);
        financePosition.setUpdatedAt(OffsetDateTime.now());
        financePositionMapper.updateById(financePosition);
        log.info("更新银行/金融招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        financePositionMapper.deleteById(id);
        log.info("硬删除银行/金融招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        FinancePosition financePosition = financePositionMapper.selectById(id);
        if (financePosition == null) {
            throw new BusinessException(404, "银行/金融招聘岗位不存在");
        }
        financePosition.setIsDeleted(status == 0);
        financePosition.setUpdatedAt(OffsetDateTime.now());
        financePositionMapper.updateById(financePosition);
        log.info("更新银行/金融招聘岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            FinancePosition financePosition = financePositionMapper.selectById(id);
            if (financePosition != null) {
                financePositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除银行/金融招聘岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<FinancePositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (FinancePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getInstitutionName())) {
                errors.add("机构名称不能为空");
            }
            if (!StringUtils.hasText(dto.getInstitutionCategory())) {
                errors.add("机构大类不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getRecruitmentType())) {
                errors.add("招聘类型不能为空");
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
        List<FinancePositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (FinancePositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getInstitutionName())) errors.add("机构名称不能为空");
            if (!StringUtils.hasText(dto.getInstitutionCategory())) errors.add("机构大类不能为空");
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getRecruitmentType())) errors.add("招聘类型不能为空");
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
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
            financePositionMapper.insert(entity);
        }
        log.info("导入银行/金融招聘岗位成功: count={}", list.size());
    }

    private List<FinancePositionExcelDTO> readExcel(MultipartFile file) {
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
