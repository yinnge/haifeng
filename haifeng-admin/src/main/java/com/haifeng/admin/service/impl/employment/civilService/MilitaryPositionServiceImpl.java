package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionAddDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionUpdateDTO;
import com.haifeng.admin.excel.employment.civilService.MilitaryPositionExcelDTO;
import com.haifeng.admin.service.employment.civilService.MilitaryPositionService;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilitaryPositionServiceImpl implements MilitaryPositionService {

    private final MilitaryPositionMapper militaryPositionMapper;

    private static final int MAX_IMPORT_ROWS = 1000;

    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("本科及以上", "硕士及以上", "博士");
    private static final Set<String> VALID_POSITION_STATUSES = Set.of("进行中", "已结束");
    private static final int MAX_ERROR_DISPLAY = 50;

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
    public Long add(MilitaryPositionAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
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
        militaryPositionMapper.insert(entity);
        log.info("新增部队文职岗位成功: id={}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        militaryPositionMapper.physicalDeleteById(id);
        log.info("物理删除部队文职岗位成功: id={}", id);
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
        int deleted = militaryPositionMapper.physicalDeleteBatchIds(ids);
        log.info("批量物理删除部队文职岗位成功: requested={}, actual={}", ids.size(), deleted);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<MilitaryPositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importExcel(MultipartFile file) {
        List<MilitaryPositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<String> rowErrors = new ArrayList<>();
        int rowNum = 1;
        int updatedCount = 0;
        int insertCount = 0;

        for (MilitaryPositionExcelDTO dto : list) {
            rowNum++;
            try {
                LambdaQueryWrapper<MilitaryPosition> q = Wrappers.lambdaQuery(MilitaryPosition.class)
                        .eq(MilitaryPosition::getPositionName, dto.getPositionName())
                        .eq(MilitaryPosition::getIsDeleted, false);
                if (StringUtils.hasText(dto.getEmployerUnit())) {
                    q.eq(MilitaryPosition::getEmployerUnit, dto.getEmployerUnit());
                }
                if (StringUtils.hasText(dto.getDepartment())) {
                    q.eq(MilitaryPosition::getDepartment, dto.getDepartment());
                }
                q.last("LIMIT 1");
                List<MilitaryPosition> existingList = militaryPositionMapper.selectList(q);
                if (!existingList.isEmpty()) {
                    // 已存在：仅补空不覆盖（业务键 positionName 不参与）
                    boolean changed = fillMilitaryGaps(existingList.get(0), dto, now);
                    if (changed) {
                        militaryPositionMapper.updateById(existingList.get(0));
                        updatedCount++;
                    }
                    continue;
                }
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
                militaryPositionMapper.insert(entity);
                insertCount++;
            } catch (Exception e) {
                rowErrors.add("第" + rowNum + "行: 数据库操作失败[" + dto.getPositionName() + "]: " + e.getMessage());
            }
        }

        if (!rowErrors.isEmpty()) {
            throw new BusinessException(400, joinErrors(rowErrors));
        }

        log.info("导入部队文职岗位成功: 新增={}, 补空更新={}", insertCount, updatedCount);
        int total = list.size();
        int failed = 0; // 整批回滚，无部分成功
        int success = total - failed;
        return ImportResultVO.builder()
                .total(total)
                .success(success)
                .failed(failed)
                .updated(updatedCount)
                .errors(rowErrors)
                .build();
    }

    /**
     * 已存在记录补空不覆盖：仅当 DB 列为 null/空串 且 导入有值时才写入，业务键(positionName)不参与。
     * 返回是否真的补到了空列（用于 updated 计数）。
     */
    private boolean fillMilitaryGaps(MilitaryPosition e, MilitaryPositionExcelDTO dto, OffsetDateTime now) {
        boolean changed = false;
        if (!StringUtils.hasText(e.getEmployerUnit()) && StringUtils.hasText(dto.getEmployerUnit())) { e.setEmployerUnit(dto.getEmployerUnit()); changed = true; }
        if (!StringUtils.hasText(e.getDepartment()) && StringUtils.hasText(dto.getDepartment())) { e.setDepartment(dto.getDepartment()); changed = true; }
        if (!StringUtils.hasText(e.getPositionType()) && StringUtils.hasText(dto.getPositionType())) { e.setPositionType(dto.getPositionType()); changed = true; }
        if (!StringUtils.hasText(e.getWorkLocation()) && StringUtils.hasText(dto.getWorkLocation())) { e.setWorkLocation(dto.getWorkLocation()); changed = true; }
        if (!StringUtils.hasText(e.getSalaryRange()) && StringUtils.hasText(dto.getSalaryRange())) { e.setSalaryRange(dto.getSalaryRange()); changed = true; }
        if (!StringUtils.hasText(e.getMajorRequirement()) && StringUtils.hasText(dto.getMajorRequirement())) { e.setMajorRequirement(dto.getMajorRequirement()); changed = true; }
        if (!StringUtils.hasText(e.getEducationRequirement()) && StringUtils.hasText(dto.getEducationRequirement())) { e.setEducationRequirement(dto.getEducationRequirement()); changed = true; }
        if (!StringUtils.hasText(e.getRegDeadline()) && StringUtils.hasText(dto.getRegDeadline())) { e.setRegDeadline(dto.getRegDeadline()); changed = true; }
        if (!StringUtils.hasText(e.getPositionStatus()) && StringUtils.hasText(dto.getPositionStatus())) { e.setPositionStatus(dto.getPositionStatus()); changed = true; }
        if (!StringUtils.hasText(e.getPositionDescription()) && StringUtils.hasText(dto.getPositionDescription())) { e.setPositionDescription(dto.getPositionDescription()); changed = true; }
        if (e.getResponsibilities() == null && dto.getResponsibilities() != null && dto.getResponsibilities().length > 0) { e.setResponsibilities(dto.getResponsibilities()); changed = true; }
        if (e.getQualifications() == null && dto.getQualifications() != null && dto.getQualifications().length > 0) { e.setQualifications(dto.getQualifications()); changed = true; }
        if (e.getSortOrder() == null && dto.getSortOrder() != null) { e.setSortOrder(dto.getSortOrder()); changed = true; }
        if (changed) e.setUpdatedAt(now);
        return changed;
    }

    private String joinErrors(List<String> errors) {
        int display = Math.min(errors.size(), MAX_ERROR_DISPLAY);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < display; i++) {
            sb.append(errors.get(i)).append("\n");
        }
        if (errors.size() > MAX_ERROR_DISPLAY) {
            sb.append("...共").append(errors.size()).append("条错误，仅显示前").append(MAX_ERROR_DISPLAY).append("条，详见后端日志");
        }
        return sb.toString();
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
            List<MilitaryPositionExcelDTO> importList = EasyExcel.read(file.getInputStream())
                    .head(MilitaryPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
            if (importList == null || importList.isEmpty()) {
                throw new BusinessException(400, "Excel文件中没有数据");
            }
            if (importList.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
            }
            // 归一化字符串首尾空格：业务键 / 枚举字段，避免校验误判与脏数据
            for (MilitaryPositionExcelDTO dto : importList) {
                if (dto.getPositionName() != null) dto.setPositionName(dto.getPositionName().trim());
                if (dto.getEducationRequirement() != null) dto.setEducationRequirement(dto.getEducationRequirement().trim());
                if (dto.getPositionStatus() != null) dto.setPositionStatus(dto.getPositionStatus().trim());
            }
            return importList;
        } catch (Exception e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }
    }
}
