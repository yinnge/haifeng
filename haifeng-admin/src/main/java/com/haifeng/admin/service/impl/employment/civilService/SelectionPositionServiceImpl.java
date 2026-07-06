package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionUpdateDTO;
import com.haifeng.admin.excel.employment.civilService.SelectionPositionExcelDTO;
import com.haifeng.admin.service.employment.civilService.SelectionPositionService;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionListVO;
import com.haifeng.common.entity.employment.civilService.SelectionPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.SelectionPositionMapper;
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
public class SelectionPositionServiceImpl implements SelectionPositionService {

    private final SelectionPositionMapper selectionPositionMapper;

    @Override
    public IPage<SelectionPositionListVO> page(SelectionPositionQueryDTO dto) {
        Page<SelectionPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SelectionPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelectionPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(SelectionPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getTargetUnit())) {
            wrapper.like(SelectionPosition::getTargetUnit, dto.getTargetUnit());
        }
        if (StringUtils.hasText(dto.getOrganizingDept())) {
            wrapper.like(SelectionPosition::getOrganizingDept, dto.getOrganizingDept());
        }
        if (StringUtils.hasText(dto.getSelectionType())) {
            wrapper.eq(SelectionPosition::getSelectionType, dto.getSelectionType());
        }
        if (StringUtils.hasText(dto.getYear())) {
            wrapper.eq(SelectionPosition::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(SelectionPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getPoliticalStatus())) {
            wrapper.eq(SelectionPosition::getPoliticalStatus, dto.getPoliticalStatus());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(SelectionPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(SelectionPosition::getSortOrder).orderByDesc(SelectionPosition::getUpdatedAt);

        IPage<SelectionPosition> entityPage = selectionPositionMapper.selectPage(page, wrapper);
        return entityPage.convert(entity -> {
            SelectionPositionListVO vo = new SelectionPositionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public SelectionPositionDetailVO detail(Long id) {
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        SelectionPositionDetailVO vo = new SelectionPositionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, SelectionPositionUpdateDTO dto) {
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        selectionPositionMapper.updateById(entity);
        log.info("更新选调生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        selectionPositionMapper.deleteById(id);
        log.info("硬删除选调生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        selectionPositionMapper.updateById(entity);
        log.info("更新选调生岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            SelectionPosition entity = selectionPositionMapper.selectById(id);
            if (entity != null) {
                selectionPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除选调生岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<SelectionPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (SelectionPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (StringUtils.hasText(dto.getProvince()) && !ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
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
        List<SelectionPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (SelectionPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (StringUtils.hasText(dto.getProvince()) && !ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (SelectionPositionExcelDTO dto : list) {
            SelectionPosition entity = SelectionPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .positionName(dto.getPositionName())
                    .selectionType(dto.getSelectionType())
                    .year(dto.getYear())
                    .province(dto.getProvince())
                    .organizingDept(dto.getOrganizingDept())
                    .targetUnit(dto.getTargetUnit())
                    .workLocation(dto.getWorkLocation())
                    .trainingDirection(dto.getTrainingDirection())
                    .grassrootsServiceYears(dto.getGrassrootsServiceYears())
                    .trainingPlan(dto.getTrainingPlan())
                    .educationRequirement(dto.getEducationRequirement())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .majorCategories(dto.getMajorCategories())
                    .universityRequirement(dto.getUniversityRequirement())
                    .targetUniversities(dto.getTargetUniversities())
                    .politicalStatus(dto.getPoliticalStatus())
                    .studentCadreRequirement(dto.getStudentCadreRequirement())
                    .awardsRequirement(dto.getAwardsRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .examSubjects(dto.getExamSubjects())
                    .interviewForm(dto.getInterviewForm())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .applyLink(dto.getApplyLink())
                    .positionStatus(dto.getPositionStatus())
                    .remark(dto.getRemark())
                    .contactPhone(dto.getContactPhone())
                    .officialLink(dto.getOfficialLink())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            selectionPositionMapper.insert(entity);
        }
        log.info("导入选调生岗位成功: count={}", list.size());
    }

    private List<SelectionPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(SelectionPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
