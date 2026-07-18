package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
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
public class SelectionPositionServiceImpl implements SelectionPositionService {

    private final SelectionPositionMapper selectionPositionMapper;

    private static final Set<String> VALID_SELECTION_TYPES = Set.of("定向选调", "非定向选调", "急需紧缺专业选调");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("本科", "硕士", "博士", "本科及以上", "硕士及以上");
    private static final Set<String> VALID_POLITICAL_STATUSES = Set.of("中共党员", "中共预备党员", "共青团员", "不限");
    private static final Set<String> VALID_POSITION_STATUSES = Set.of("报名中", "笔试阶段", "面试阶段", "已结束", "即将开始");
    private static final int MAX_ERROR_DISPLAY = 20;

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
            vo.setId(entity.getId());
            vo.setPositionName(entity.getPositionName());
            vo.setSelectionType(entity.getSelectionType());
            vo.setYear(entity.getYear());
            vo.setProvince(entity.getProvince());
            vo.setOrganizingDept(entity.getOrganizingDept());
            vo.setTargetUnit(entity.getTargetUnit());
            vo.setWorkLocation(entity.getWorkLocation());
            vo.setPoliticalStatus(entity.getPoliticalStatus());
            vo.setRegStartDate(entity.getRegStartDate());
            vo.setRegEndDate(entity.getRegEndDate());
            vo.setPositionStatus(entity.getPositionStatus());
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
        vo.setId(entity.getId());
        vo.setPositionName(entity.getPositionName());
        vo.setSelectionType(entity.getSelectionType());
        vo.setYear(entity.getYear());
        vo.setProvince(entity.getProvince());
        vo.setOrganizingDept(entity.getOrganizingDept());
        vo.setTargetUnit(entity.getTargetUnit());
        vo.setWorkLocation(entity.getWorkLocation());
        vo.setTrainingDirection(entity.getTrainingDirection());
        vo.setGrassrootsServiceYears(entity.getGrassrootsServiceYears());
        vo.setTrainingPlan(entity.getTrainingPlan());
        vo.setEducationRequirement(entity.getEducationRequirement());
        vo.setDegreeRequirement(entity.getDegreeRequirement());
        vo.setMajorRequirement(entity.getMajorRequirement());
        vo.setMajorCategories(entity.getMajorCategories());
        vo.setUniversityRequirement(entity.getUniversityRequirement());
        vo.setTargetUniversities(entity.getTargetUniversities());
        vo.setPoliticalStatus(entity.getPoliticalStatus());
        vo.setStudentCadreRequirement(entity.getStudentCadreRequirement());
        vo.setAwardsRequirement(entity.getAwardsRequirement());
        vo.setAgeLimit(entity.getAgeLimit());
        vo.setRecruitmentCount(entity.getRecruitmentCount());
        vo.setExamSubjects(entity.getExamSubjects());
        vo.setInterviewForm(entity.getInterviewForm());
        vo.setRegStartDate(entity.getRegStartDate());
        vo.setRegEndDate(entity.getRegEndDate());
        vo.setExamTime(entity.getExamTime());
        vo.setApplyLink(entity.getApplyLink());
        vo.setPositionStatus(entity.getPositionStatus());
        vo.setRemark(entity.getRemark());
        vo.setContactPhone(entity.getContactPhone());
        vo.setOfficialLink(entity.getOfficialLink());
        vo.setContent(entity.getContent());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SelectionPositionUpdateDTO dto) {
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        if (dto.getPositionName() != null) entity.setPositionName(dto.getPositionName());
        if (dto.getSelectionType() != null) entity.setSelectionType(dto.getSelectionType());
        if (dto.getYear() != null) entity.setYear(dto.getYear());
        if (dto.getProvince() != null) entity.setProvince(dto.getProvince());
        if (dto.getOrganizingDept() != null) entity.setOrganizingDept(dto.getOrganizingDept());
        if (dto.getTargetUnit() != null) entity.setTargetUnit(dto.getTargetUnit());
        if (dto.getWorkLocation() != null) entity.setWorkLocation(dto.getWorkLocation());
        if (dto.getTrainingDirection() != null) entity.setTrainingDirection(dto.getTrainingDirection());
        if (dto.getGrassrootsServiceYears() != null) entity.setGrassrootsServiceYears(dto.getGrassrootsServiceYears());
        if (dto.getTrainingPlan() != null) entity.setTrainingPlan(dto.getTrainingPlan());
        if (dto.getEducationRequirement() != null) entity.setEducationRequirement(dto.getEducationRequirement());
        if (dto.getDegreeRequirement() != null) entity.setDegreeRequirement(dto.getDegreeRequirement());
        if (dto.getMajorRequirement() != null) entity.setMajorRequirement(dto.getMajorRequirement());
        if (dto.getMajorCategories() != null) entity.setMajorCategories(dto.getMajorCategories());
        if (dto.getUniversityRequirement() != null) entity.setUniversityRequirement(dto.getUniversityRequirement());
        if (dto.getTargetUniversities() != null) entity.setTargetUniversities(dto.getTargetUniversities());
        if (dto.getPoliticalStatus() != null) entity.setPoliticalStatus(dto.getPoliticalStatus());
        if (dto.getStudentCadreRequirement() != null) entity.setStudentCadreRequirement(dto.getStudentCadreRequirement());
        if (dto.getAwardsRequirement() != null) entity.setAwardsRequirement(dto.getAwardsRequirement());
        if (dto.getAgeLimit() != null) entity.setAgeLimit(dto.getAgeLimit());
        if (dto.getRecruitmentCount() != null) entity.setRecruitmentCount(dto.getRecruitmentCount());
        if (dto.getExamSubjects() != null) entity.setExamSubjects(dto.getExamSubjects());
        if (dto.getInterviewForm() != null) entity.setInterviewForm(dto.getInterviewForm());
        if (dto.getRegStartDate() != null) entity.setRegStartDate(dto.getRegStartDate());
        if (dto.getRegEndDate() != null) entity.setRegEndDate(dto.getRegEndDate());
        if (dto.getExamTime() != null) entity.setExamTime(dto.getExamTime());
        if (dto.getApplyLink() != null) entity.setApplyLink(dto.getApplyLink());
        if (dto.getPositionStatus() != null) entity.setPositionStatus(dto.getPositionStatus());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        if (dto.getContactPhone() != null) entity.setContactPhone(dto.getContactPhone());
        if (dto.getOfficialLink() != null) entity.setOfficialLink(dto.getOfficialLink());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        selectionPositionMapper.updateById(entity);
        log.info("更新选调生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        entity.setIsDeleted(true);
        selectionPositionMapper.updateById(entity);
        log.info("软删除选调生岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String positionStatus) {
        if (!VALID_POSITION_STATUSES.contains(positionStatus)) {
            throw new BusinessException(400, "状态只能是: 报名中、笔试阶段、面试阶段、已结束、即将开始");
        }
        SelectionPosition entity = selectionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "选调生岗位不存在");
        }
        entity.setPositionStatus(positionStatus);
        selectionPositionMapper.updateById(entity);
        log.info("更新选调生岗位状态成功: id={}, positionStatus={}", id, positionStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int updated = selectionPositionMapper.update(null,
                Wrappers.lambdaUpdate(SelectionPosition.class)
                        .set(SelectionPosition::getIsDeleted, true)
                        .eq(SelectionPosition::getIsDeleted, false)
                        .in(SelectionPosition::getId, ids));
        log.info("批量删除选调生岗位成功: requested={}, actual={}", ids.size(), updated);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<SelectionPositionExcelDTO> list = readExcel(file);
        return validateExcelRows(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<SelectionPositionExcelDTO> list = readExcel(file);
        String errors = validateExcelRows(list);
        if (errors != null) {
            throw new BusinessException(400, errors);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<SelectionPosition> entities = new ArrayList<>();
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
            entities.add(entity);
        }
        Db.saveBatch(entities);
        log.info("导入选调生岗位成功: count={}", list.size());
    }

    private String validateExcelRows(List<SelectionPositionExcelDTO> list) {
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        int errorCount = 0;
        for (SelectionPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getSelectionType())) {
                errors.add("选调类型不能为空");
            } else if (!VALID_SELECTION_TYPES.contains(dto.getSelectionType())) {
                errors.add("选调类型只能是: 定向选调、非定向选调、急需紧缺专业选调");
            }
            if (!StringUtils.hasText(dto.getYear())) {
                errors.add("年份不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!StringUtils.hasText(dto.getEducationRequirement())) {
                errors.add("学历要求不能为空");
            } else if (!VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
                errors.add("学历要求只能是: 本科、硕士、博士、本科及以上、硕士及以上");
            }
            if (StringUtils.hasText(dto.getPoliticalStatus())
                    && !VALID_POLITICAL_STATUSES.contains(dto.getPoliticalStatus())) {
                errors.add("政治面貌只能是: 中共党员、中共预备党员、共青团员、不限");
            }
            if (dto.getAgeLimit() != null && (dto.getAgeLimit() < 18 || dto.getAgeLimit() > 40)) {
                errors.add("年龄上限必须在18-40之间");
            }
            if (StringUtils.hasText(dto.getPositionStatus())
                    && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
                errors.add("状态只能是: 报名中、笔试阶段、面试阶段、已结束、即将开始");
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

    private List<SelectionPositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException(400, "文件类型只能是xlsx或xls");
        }
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
