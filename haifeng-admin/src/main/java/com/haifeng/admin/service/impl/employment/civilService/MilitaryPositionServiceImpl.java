package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionUpdateDTO;
import com.haifeng.admin.excel.employment.civilService.MilitaryPositionExcelDTO;
import com.haifeng.admin.service.employment.civilService.MilitaryPositionService;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.common.entity.employment.civilService.MilitaryPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.MilitaryPositionMapper;
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
public class MilitaryPositionServiceImpl implements MilitaryPositionService {

    private final MilitaryPositionMapper militaryPositionMapper;

    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("本科及以上", "硕士及以上", "博士");
    private static final Set<String> VALID_POSITION_STATUSES = Set.of("进行中", "已结束");
    private static final int MAX_ERROR_DISPLAY = 20;

    @Override
    public IPage<MilitaryPositionListVO> page(MilitaryPositionQueryDTO dto) {
        Page<MilitaryPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MilitaryPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MilitaryPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(MilitaryPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getEmployerUnit())) {
            wrapper.like(MilitaryPosition::getEmployerUnit, dto.getEmployerUnit());
        }
        if (StringUtils.hasText(dto.getDepartment())) {
            wrapper.like(MilitaryPosition::getDepartment, dto.getDepartment());
        }
        if (StringUtils.hasText(dto.getPositionType())) {
            wrapper.eq(MilitaryPosition::getPositionType, dto.getPositionType());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(MilitaryPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(MilitaryPosition::getSortOrder).orderByDesc(MilitaryPosition::getUpdatedAt);

        IPage<MilitaryPosition> entityPage = militaryPositionMapper.selectPage(page, wrapper);
        return entityPage.convert(entity -> {
            MilitaryPositionListVO vo = new MilitaryPositionListVO();
            vo.setId(entity.getId());
            vo.setPositionName(entity.getPositionName());
            vo.setEmployerUnit(entity.getEmployerUnit());
            vo.setDepartment(entity.getDepartment());
            vo.setPositionType(entity.getPositionType());
            vo.setWorkLocation(entity.getWorkLocation());
            vo.setSalaryRange(entity.getSalaryRange());
            vo.setRegDeadline(entity.getRegDeadline());
            vo.setPositionStatus(entity.getPositionStatus());
            return vo;
        });
    }

    @Override
    public MilitaryPositionDetailVO detail(Long id) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        MilitaryPositionDetailVO vo = new MilitaryPositionDetailVO();
        vo.setId(entity.getId());
        vo.setPositionName(entity.getPositionName());
        vo.setEmployerUnit(entity.getEmployerUnit());
        vo.setDepartment(entity.getDepartment());
        vo.setPositionType(entity.getPositionType());
        vo.setWorkLocation(entity.getWorkLocation());
        vo.setSalaryRange(entity.getSalaryRange());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setRegDeadline(entity.getRegDeadline());
        vo.setPositionStatus(entity.getPositionStatus());
        vo.setPositionDescription(entity.getPositionDescription());
        vo.setResponsibilities(entity.getResponsibilities());
        vo.setQualifications(entity.getQualifications());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MilitaryPositionUpdateDTO dto) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        if (dto.getPositionName() != null) entity.setPositionName(dto.getPositionName());
        if (dto.getEmployerUnit() != null) entity.setEmployerUnit(dto.getEmployerUnit());
        if (dto.getDepartment() != null) entity.setDepartment(dto.getDepartment());
        if (dto.getPositionType() != null) entity.setPositionType(dto.getPositionType());
        if (dto.getWorkLocation() != null) entity.setWorkLocation(dto.getWorkLocation());
        if (dto.getSalaryRange() != null) entity.setSalaryRange(dto.getSalaryRange());
        if (dto.getMajorRequirement() != null) entity.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getEducationRequirement() != null) entity.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getRegDeadline() != null) entity.setRegDeadline(dto.getRegDeadline());
        if (dto.getPositionStatus() != null) entity.setPositionStatus(dto.getPositionStatus());
        if (dto.getPositionDescription() != null) entity.setPositionDescription(dto.getPositionDescription());
        if (dto.getResponsibilities() != null) entity.setResponsibilities(dto.getResponsibilities());
        if (dto.getQualifications() != null) entity.setQualifications(dto.getQualifications());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        militaryPositionMapper.updateById(entity);
        log.info("更新部队文职岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        entity.setIsDeleted(true);
        militaryPositionMapper.updateById(entity);
        log.info("软删除部队文职岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态只能是: 进行中、已结束");
        }
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        militaryPositionMapper.updateById(entity);
        log.info("更新部队文职岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = militaryPositionMapper.update(null,
                Wrappers.lambdaUpdate(MilitaryPosition.class)
                        .set(MilitaryPosition::getIsDeleted, true)
                        .eq(MilitaryPosition::getIsDeleted, false)
                        .in(MilitaryPosition::getId, ids));
        log.info("批量删除部队文职岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<MilitaryPositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<MilitaryPositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<MilitaryPosition> entities = new ArrayList<>();
        for (MilitaryPositionExcelDTO dto : list) {
            MilitaryPosition entity = MilitaryPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .positionName(dto.getPositionName())
                    .employerUnit(dto.getEmployerUnit())
                    .department(dto.getDepartment())
                    .positionType(dto.getPositionType())
                    .workLocation(dto.getWorkLocation())
                    .salaryRange(dto.getSalaryRange())
                    .majorRequirement(dto.getMajorRequirement())
                    .educationRequirement(dto.getEducationRequirement())
                    .regDeadline(dto.getRegDeadline())
                    .positionStatus(dto.getPositionStatus())
                    .positionDescription(dto.getPositionDescription())
                    .responsibilities(dto.getResponsibilities())
                    .qualifications(dto.getQualifications())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            entities.add(entity);
        }
        Db.saveBatch(entities);
        log.info("导入部队文职岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<MilitaryPositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (MilitaryPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (StringUtils.hasText(dto.getEducationRequirement())
                    && !VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
                errors.add("学历要求只能是: 本科及以上、硕士及以上、博士");
            }
            if (StringUtils.hasText(dto.getPositionStatus())
                    && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
                errors.add("状态只能是: 进行中、已结束");
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

    private List<MilitaryPositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(MilitaryPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
