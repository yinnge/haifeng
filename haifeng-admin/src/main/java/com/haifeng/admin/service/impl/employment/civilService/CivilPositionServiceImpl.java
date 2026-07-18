package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.civilService.CivilPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.CivilPositionUpdateDTO;
import com.haifeng.admin.excel.employment.civilService.CivilPositionExcelDTO;
import com.haifeng.admin.service.employment.civilService.CivilPositionService;
import com.haifeng.admin.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.CivilPositionListVO;
import com.haifeng.common.entity.employment.civilService.CivilPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.CivilPositionMapper;
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
public class CivilPositionServiceImpl implements CivilPositionService {

    private final CivilPositionMapper civilPositionMapper;

    @Override
    public IPage<CivilPositionListVO> page(CivilPositionQueryDTO dto) {
        Page<CivilPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<CivilPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CivilPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(CivilPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getRecruitingDept())) {
            wrapper.like(CivilPosition::getRecruitingDept, dto.getRecruitingDept());
        }
        if (StringUtils.hasText(dto.getWorkLocation())) {
            wrapper.like(CivilPosition::getWorkLocation, dto.getWorkLocation());
        }
        if (StringUtils.hasText(dto.getExamType())) {
            wrapper.eq(CivilPosition::getExamType, dto.getExamType());
        }
        if (StringUtils.hasText(dto.getRegStatus())) {
            wrapper.eq(CivilPosition::getRegStatus, dto.getRegStatus());
        }
        if (StringUtils.hasText(dto.getMinEducation())) {
            wrapper.eq(CivilPosition::getMinEducation, dto.getMinEducation());
        }

        wrapper.orderByAsc(CivilPosition::getSortOrder).orderByDesc(CivilPosition::getUpdatedAt);

        IPage<CivilPosition> entityPage = civilPositionMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            CivilPositionListVO vo = new CivilPositionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public CivilPositionDetailVO detail(Long id) {
        CivilPosition entity = civilPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公务员职位不存在");
        }
        CivilPositionDetailVO vo = new CivilPositionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CivilPositionUpdateDTO dto) {
        CivilPosition entity = civilPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "公务员职位不存在");
        }
        if (dto.getPositionName() != null) entity.setPositionName(dto.getPositionName());
        if (dto.getExamType() != null) entity.setExamType(dto.getExamType());
        if (dto.getRecruitingDept() != null) entity.setRecruitingDept(dto.getRecruitingDept());
        if (dto.getDeptCode() != null) entity.setDeptCode(dto.getDeptCode());
        if (dto.getPositionCode() != null) entity.setPositionCode(dto.getPositionCode());
        if (dto.getAffiliatedBureau() != null) entity.setAffiliatedBureau(dto.getAffiliatedBureau());
        if (dto.getMajorRequirement() != null) entity.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getMinEducation() != null) entity.setMinEducation(dto.getMinEducation());
        if (dto.getDegreeRequirement() != null) entity.setDegreeRequirement(dto.getDegreeRequirement());
        if (dto.getPoliticalStatus() != null) entity.setPoliticalStatus(dto.getPoliticalStatus());
        if (dto.getWorkExperience() != null) entity.setWorkExperience(dto.getWorkExperience());
        if (dto.getGrassrootsExperience() != null) entity.setGrassrootsExperience(dto.getGrassrootsExperience());
        if (dto.getExamCategory() != null) entity.setExamCategory(dto.getExamCategory());
        if (dto.getInterviewRatio() != null) entity.setInterviewRatio(dto.getInterviewRatio());
        if (dto.getRecruitmentCount() != null) entity.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getHasProfessionalTest() != null) entity.setHasProfessionalTest(dto.getHasProfessionalTest());
        if (dto.getWorkLocation() != null) entity.setWorkLocation(dto.getWorkLocation());
        if (dto.getWorkLocationDetail() != null) entity.setWorkLocationDetail(dto.getWorkLocationDetail());
        if (dto.getHouseholdRequirement() != null) entity.setHouseholdRequirement(dto.getHouseholdRequirement());
        if (dto.getHouseholdLocation() != null) entity.setHouseholdLocation(dto.getHouseholdLocation());
        if (dto.getPositionIntro() != null) entity.setPositionIntro(dto.getPositionIntro());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (dto.getOfficialWebsite() != null) entity.setOfficialWebsite(dto.getOfficialWebsite());
        if (dto.getContactPhone() != null) entity.setContactPhone(dto.getContactPhone());
        if (dto.getRegStartDate() != null) entity.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) entity.setRegEndDate(dto.getRegEndDate());
        if (dto.getRegStatus() != null) entity.setRegStatus(dto.getRegStatus());
        if (dto.getApplicantCount() != null) entity.setApplicantCount(dto.getApplicantCount());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        civilPositionMapper.updateById(entity);
        log.info("更新公务员职位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CivilPosition entity = civilPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公务员职位不存在");
        }
        entity.setIsDeleted(true);
        entity.setUpdatedAt(OffsetDateTime.now());
        civilPositionMapper.updateById(entity);
        log.info("软删除公务员职位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        CivilPosition entity = civilPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "公务员职位不存在");
        }
        if (status == 0) {
            entity.setRegStatus("报名中");
        } else if (status == 1) {
            entity.setRegStatus("已结束");
        } else {
            throw new BusinessException(400, "状态值不合法，0=报名中，1=已结束");
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        civilPositionMapper.updateById(entity);
        log.info("更新公务员职位状态成功: id={}, status={}", id, entity.getRegStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = civilPositionMapper.update(null,
                Wrappers.lambdaUpdate(CivilPosition.class)
                        .set(CivilPosition::getIsDeleted, true)
                        .set(CivilPosition::getUpdatedAt, OffsetDateTime.now())
                        .in(CivilPosition::getId, ids));
        log.info("批量删除公务员职位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<CivilPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = validateExcelRows(list);
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<CivilPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = validateExcelRows(list);
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        int imported = 0;
        for (CivilPositionExcelDTO dto : list) {
            if (StringUtils.hasText(dto.getDeptCode()) && StringUtils.hasText(dto.getPositionCode())) {
                long count = civilPositionMapper.selectCount(
                        Wrappers.lambdaQuery(CivilPosition.class)
                                .eq(CivilPosition::getExamType, dto.getExamType())
                                .eq(CivilPosition::getDeptCode, dto.getDeptCode())
                                .eq(CivilPosition::getPositionCode, dto.getPositionCode()));
                if (count > 0) {
                    continue;
                }
            }
            CivilPosition entity = CivilPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .positionName(dto.getPositionName())
                    .examType(dto.getExamType())
                    .recruitingDept(dto.getRecruitingDept())
                    .deptCode(dto.getDeptCode())
                    .positionCode(dto.getPositionCode())
                    .affiliatedBureau(dto.getAffiliatedBureau())
                    .majorRequirement(dto.getMajorRequirement())
                    .minEducation(dto.getMinEducation())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .politicalStatus(dto.getPoliticalStatus())
                    .workExperience(dto.getWorkExperience())
                    .grassrootsExperience(dto.getGrassrootsExperience())
                    .examCategory(dto.getExamCategory())
                    .interviewRatio(dto.getInterviewRatio())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .hasProfessionalTest(dto.getHasProfessionalTest())
                    .workLocation(dto.getWorkLocation())
                    .workLocationDetail(dto.getWorkLocationDetail())
                    .householdRequirement(dto.getHouseholdRequirement())
                    .householdLocation(dto.getHouseholdLocation())
                    .positionIntro(dto.getPositionIntro())
                    .remark(dto.getRemark())
                    .officialWebsite(dto.getOfficialWebsite())
                    .contactPhone(dto.getContactPhone())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .regStatus(dto.getRegStatus())
                    .applicantCount(dto.getApplicantCount())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            civilPositionMapper.insert(entity);
            imported++;
        }
        log.info("导入公务员职位成功: total={}, imported={}", list.size(), imported);
    }

    private StringBuilder validateExcelRows(List<CivilPositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (CivilPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("职位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getExamType())) {
                errors.add("考试类型不能为空");
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        return errorMsg;
    }

    private List<CivilPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(CivilPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
