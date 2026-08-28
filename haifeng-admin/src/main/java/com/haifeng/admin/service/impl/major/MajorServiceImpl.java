package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.excel.major.MajorDetailImportDTO;
import com.haifeng.admin.excel.major.MajorImportDTO;
import com.haifeng.admin.service.major.MajorService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorDetailVO;
import com.haifeng.admin.vo.major.MajorListVO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 专业管理Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {

    private final MajorMapper majorMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final int MAX_ERROR_DISPLAY = 50;
    private final MajorDetailMapper majorDetailMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public IPage<MajorListVO> list(MajorQueryDTO queryDTO) {
        Page<Major> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询
        if (StringUtils.hasText(queryDTO.getMajorCode())) {
            wrapper.like(Major::getMajorCode, queryDTO.getMajorCode());
        }
        if (StringUtils.hasText(queryDTO.getMajorName())) {
            wrapper.like(Major::getMajorName, queryDTO.getMajorName());
        }
        if (StringUtils.hasText(queryDTO.getDisciplineName())) {
            wrapper.like(Major::getDisciplineName, queryDTO.getDisciplineName());
        }

        // 精确匹配
        if (StringUtils.hasText(queryDTO.getMajorType())) {
            wrapper.eq(Major::getMajorType, queryDTO.getMajorType());
        }
        if (StringUtils.hasText(queryDTO.getMajorCategory())) {
            wrapper.eq(Major::getMajorCategory, queryDTO.getMajorCategory());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Major::getStatus, queryDTO.getStatus());
        }

        // 排序
        wrapper.orderByDesc(Major::getCreatedAt);

        Page<Major> majorPage = majorMapper.selectPage(page, wrapper);

        return majorPage.convert(major -> {
            MajorListVO vo = new MajorListVO();
            BeanUtils.copyProperties(major, vo);
            vo.setStatus(major.getStatus() != null ? major.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    public MajorDetailVO getById(Long id) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        MajorDetailVO vo = new MajorDetailVO();
        BeanUtils.copyProperties(major, vo);
        vo.setStatus(major.getStatus() != null ? major.getStatus().intValue() : null);

        // 查询详情表
        MajorDetail detail = majorDetailMapper.selectByMajorId(id);
        if (detail != null) {
            vo.setDetailId(detail.getId());
            vo.setCourseCount(detail.getCourseCount());
            // graduateScale在Entity中是String，在VO中也保持一致（VO中是Integer，需要转换）
            if (detail.getGraduateScale() != null) {
                try {
                    vo.setGraduateScale(Integer.parseInt(detail.getGraduateScale()));
                } catch (NumberFormatException e) {
                    // 如果无法解析为Integer，则保持null
                    vo.setGraduateScale(null);
                }
            }
            vo.setMaleRatio(detail.getMaleRatio());
            vo.setFemaleRatio(detail.getFemaleRatio());
            vo.setMajorDescription(detail.getMajorDescription());
            vo.setTrainingObjective(detail.getTrainingObjective());
            vo.setTrainingRequirement(detail.getTrainingRequirement());
            vo.setSubjectRequirement(detail.getSubjectRequirement());
            vo.setCareerProspect(detail.getCareerProspect());
            vo.setMainCourses(detail.getMainCourses());
            vo.setKnowledgeSkills(detail.getKnowledgeSkills());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(MajorAddDTO addDTO) {
        // 检查majorCode是否存在
        if (majorMapper.existsByMajorCode(addDTO.getMajorCode())) {
            throw new BusinessException(400, "专业代码已存在");
        }

        // 检查salaryMin <= salaryMax
        if (addDTO.getSalaryMin() != null && addDTO.getSalaryMax() != null
                && addDTO.getSalaryMin() > addDTO.getSalaryMax()) {
            throw new BusinessException(400, "薪资下限不能大于薪资上限");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Major major = Major.builder()
                .id(id)
                .majorCode(addDTO.getMajorCode())
                .majorName(addDTO.getMajorName())
                .disciplineName(addDTO.getDisciplineName())
                .majorType(addDTO.getMajorType())
                .majorCategory(addDTO.getMajorCategory())
                .parentCategory(addDTO.getParentCategory())
                .majorTags(addDTO.getMajorTags())
                .degreeAwarded(addDTO.getDegreeAwarded())
                .studyDuration(addDTO.getStudyDuration())
                .employmentRate(addDTO.getEmploymentRate())
                .salaryMin(addDTO.getSalaryMin())
                .salaryMax(addDTO.getSalaryMax())
                .description(addDTO.getDescription())
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        majorMapper.insert(major);

        log.info("新增专业成功: id={}, majorCode={}, majorName={}", id, addDTO.getMajorCode(), addDTO.getMajorName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MajorUpdateDTO updateDTO) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        // 检查salaryMin <= salaryMax
        Integer salaryMin = updateDTO.getSalaryMin() != null ? updateDTO.getSalaryMin() : major.getSalaryMin();
        Integer salaryMax = updateDTO.getSalaryMax() != null ? updateDTO.getSalaryMax() : major.getSalaryMax();
        if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
            throw new BusinessException(400, "薪资下限不能大于薪资上限");
        }

        // 手动复制字段，排除id, majorCode, status, createdAt
        if (updateDTO.getMajorName() != null) {
            major.setMajorName(updateDTO.getMajorName());
        }
        if (updateDTO.getDisciplineName() != null) {
            major.setDisciplineName(updateDTO.getDisciplineName());
        }
        if (updateDTO.getMajorType() != null) {
            major.setMajorType(updateDTO.getMajorType());
        }
        if (updateDTO.getMajorCategory() != null) {
            major.setMajorCategory(updateDTO.getMajorCategory());
        }
        if (updateDTO.getParentCategory() != null) {
            major.setParentCategory(updateDTO.getParentCategory());
        }
        if (updateDTO.getMajorTags() != null) {
            major.setMajorTags(updateDTO.getMajorTags());
        }
        if (updateDTO.getDegreeAwarded() != null) {
            major.setDegreeAwarded(updateDTO.getDegreeAwarded());
        }
        if (updateDTO.getStudyDuration() != null) {
            major.setStudyDuration(updateDTO.getStudyDuration());
        }
        if (updateDTO.getEmploymentRate() != null) {
            major.setEmploymentRate(updateDTO.getEmploymentRate());
        }
        if (updateDTO.getSalaryMin() != null) {
            major.setSalaryMin(updateDTO.getSalaryMin());
        }
        if (updateDTO.getSalaryMax() != null) {
            major.setSalaryMax(updateDTO.getSalaryMax());
        }
        if (updateDTO.getDescription() != null) {
            major.setDescription(updateDTO.getDescription());
        }
        major.setUpdatedAt(OffsetDateTime.now());

        majorMapper.updateById(major);

        log.info("修改专业成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        major.setStatus(status);
        major.setUpdatedAt(OffsetDateTime.now());
        majorMapper.updateById(major);

        log.info("修改专业状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        updateStatus(id, (short) 0);
        log.info("软删除专业成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        // 先删除关联的详情记录
        LambdaQueryWrapper<MajorDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(MajorDetail::getMajorId, id);
        majorDetailMapper.delete(detailWrapper);

        // 硬删除主表
        majorMapper.deleteById(id);

        log.info("硬删除专业成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSoftDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        OffsetDateTime now = OffsetDateTime.now();
        LambdaUpdateWrapper<Major> wrapper = new LambdaUpdateWrapper<Major>()
                .in(Major::getId, ids)
                .set(Major::getStatus, (short) 0)
                .set(Major::getUpdatedAt, now);
        int updated = majorMapper.update(null, wrapper);

        log.info("批量软删除专业完成: 请求数量={}, 实际更新={}", ids.size(), updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        // 批量删除关联的详情
        LambdaQueryWrapper<MajorDetail> detailWrapper = new LambdaQueryWrapper<MajorDetail>()
                .in(MajorDetail::getMajorId, ids);
        majorDetailMapper.delete(detailWrapper);

        // 批量删除主表
        int deleted = majorMapper.deleteByIds(ids);

        log.info("批量硬删除专业完成: 请求数量={}, 实际删除={}", ids.size(), deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(Long id, MajorDetailUpdateDTO detailDTO) {
        Major major = majorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "专业不存在");
        }

        MajorDetail detail = majorDetailMapper.selectByMajorId(id);
        OffsetDateTime now = OffsetDateTime.now();

        if (detail == null) {
            // 新建详情记录
            Long detailId = SnowflakeIdGenerator.nextId();

            detail = MajorDetail.builder()
                    .id(detailId)
                    .majorId(id)
                    .courseCount(detailDTO.getCourseCount())
                    .graduateScale(detailDTO.getGraduateScale())
                    .maleRatio(detailDTO.getMaleRatio())
                    .femaleRatio(detailDTO.getFemaleRatio())
                    .majorDescription(detailDTO.getMajorDescription())
                    .trainingObjective(detailDTO.getTrainingObjective())
                    .trainingRequirement(detailDTO.getTrainingRequirement())
                    .subjectRequirement(detailDTO.getSubjectRequirement())
                    .careerProspect(detailDTO.getCareerProspect())
                    .mainCourses(detailDTO.getMainCourses())
                    .knowledgeSkills(detailDTO.getKnowledgeSkills())
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            majorDetailMapper.insert(detail);
            log.info("新增专业详情成功: majorId={}, detailId={}", id, detailId);
        } else {
            // 更新详情记录
            if (detailDTO.getCourseCount() != null) {
                detail.setCourseCount(detailDTO.getCourseCount());
            }
            if (detailDTO.getGraduateScale() != null) {
                detail.setGraduateScale(detailDTO.getGraduateScale());
            }
            if (detailDTO.getMaleRatio() != null) {
                detail.setMaleRatio(detailDTO.getMaleRatio());
            }
            if (detailDTO.getFemaleRatio() != null) {
                detail.setFemaleRatio(detailDTO.getFemaleRatio());
            }
            if (detailDTO.getMajorDescription() != null) {
                detail.setMajorDescription(detailDTO.getMajorDescription());
            }
            if (detailDTO.getTrainingObjective() != null) {
                detail.setTrainingObjective(detailDTO.getTrainingObjective());
            }
            if (detailDTO.getTrainingRequirement() != null) {
                detail.setTrainingRequirement(detailDTO.getTrainingRequirement());
            }
            if (detailDTO.getSubjectRequirement() != null) {
                detail.setSubjectRequirement(detailDTO.getSubjectRequirement());
            }
            if (detailDTO.getCareerProspect() != null) {
                detail.setCareerProspect(detailDTO.getCareerProspect());
            }
            if (detailDTO.getMainCourses() != null) {
                detail.setMainCourses(detailDTO.getMainCourses());
            }
            if (detailDTO.getKnowledgeSkills() != null) {
                detail.setKnowledgeSkills(detailDTO.getKnowledgeSkills());
            }
            detail.setUpdatedAt(now);

            majorDetailMapper.updateById(detail);
            log.info("修改专业详情成功: majorId={}, detailId={}", id, detail.getId());
        }
    }

    @Override
    public ImportResultVO importMajor(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<MajorImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(MajorImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "Excel文件解析失败，请确认文件为有效的.xlsx且表头正确: " + e.getMessage());
        }

        if (dataList != null && dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> majorCodesInFile = new HashSet<>();
        int[] updatedCount = {0};
        OffsetDateTime now = OffsetDateTime.now();

        new TransactionTemplate(transactionManager).execute(status -> {
            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = i + 2; // Excel行号（从2开始，1是表头）
                MajorImportDTO dto = dataList.get(i);

                // 校验必填字段（专业代码用于匹配，必须存在）
                if (!StringUtils.hasText(dto.getMajorCode())) {
                    errors.add("第" + rowNum + "行: 专业代码不能为空");
                    continue;
                }

                // 检查文件内重复
                if (majorCodesInFile.contains(dto.getMajorCode())) {
                    errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在文件中重复");
                    continue;
                }
                majorCodesInFile.add(dto.getMajorCode());

                Major existing = majorMapper.selectByMajorCode(dto.getMajorCode());

                if (existing == null) {
                    // ===== 数据库不存在：新增 =====
                    if (!StringUtils.hasText(dto.getMajorName())) {
                        errors.add("第" + rowNum + "行: 专业名称不能为空");
                        continue;
                    }
                    if (!StringUtils.hasText(dto.getMajorType())) {
                        errors.add("第" + rowNum + "行: 专业类型不能为空");
                        continue;
                    }
                    // 校验就业率范围
                    if (dto.getEmploymentRate() != null) {
                        BigDecimal rate = dto.getEmploymentRate();
                        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("100")) > 0) {
                            errors.add("第" + rowNum + "行: 就业率必须在0-100之间");
                            continue;
                        }
                    }
                    // 校验薪资范围
                    if (dto.getSalaryMin() != null && dto.getSalaryMax() != null
                            && dto.getSalaryMin() > dto.getSalaryMax()) {
                        errors.add("第" + rowNum + "行: 薪资下限不能大于薪资上限");
                        continue;
                    }

                    // 构建实体并插入
                    Long id = SnowflakeIdGenerator.nextId();
                    Major major = Major.builder()
                            .id(id)
                            .majorCode(dto.getMajorCode())
                            .majorName(dto.getMajorName())
                            .disciplineName(dto.getDisciplineName())
                            .majorType(dto.getMajorType())
                            .majorCategory(dto.getMajorCategory())
                            .parentCategory(dto.getParentCategory())
                            .majorTags(dto.getMajorTags())
                            .degreeAwarded(dto.getDegreeAwarded())
                            .studyDuration(dto.getStudyDuration())
                            .employmentRate(dto.getEmploymentRate())
                            .salaryMin(dto.getSalaryMin())
                            .salaryMax(dto.getSalaryMax())
                            .description(dto.getDescription())
                            .status((short) 1)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    try {
                        majorMapper.insert(major);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        errors.add("第" + rowNum + "行: 专业保存失败[" + dto.getMajorCode() + "]: " + e.getMessage());
                    }
                } else {
                    // ===== 数据库已存在：仅补齐为空的列，已有数据的列绝不覆盖 =====
                    boolean changed = mergeMajorIfBlank(existing, dto);
                    // 合并后校验薪资范围（仅当两列都非空）
                    if (existing.getSalaryMin() != null && existing.getSalaryMax() != null
                            && existing.getSalaryMin() > existing.getSalaryMax()) {
                        errors.add("第" + rowNum + "行: 薪资下限不能大于薪资上限");
                        continue;
                    }
                    // 合并后校验就业率（仅当本次上传填补了该列）
                    if (dto.getEmploymentRate() != null && existing.getEmploymentRate() != null
                            && (existing.getEmploymentRate().compareTo(BigDecimal.ZERO) < 0
                            || existing.getEmploymentRate().compareTo(new BigDecimal("100")) > 0)) {
                        errors.add("第" + rowNum + "行: 就业率必须在0-100之间");
                        continue;
                    }
                    if (changed) {
                        try {
                            existing.setUpdatedAt(now);
                            majorMapper.updateById(existing);
                            updatedCount[0]++;
                        } catch (Exception e) {
                            status.setRollbackOnly();
                            errors.add("第" + rowNum + "行: 专业更新失败[" + dto.getMajorCode() + "]: " + e.getMessage());
                        }
                    }
                }
            }

            if (!errors.isEmpty()) {
                throw new BusinessException(400, "数据校验失败：" + joinErrors(errors));
            }
            return null;
        });

        int total = dataList.size();
        log.info("导入专业数据完成: 总行数={}, 补空更新={}", total, updatedCount[0]);
        return ImportResultVO.builder()
                .total(total)
                .success(total)
                .failed(0)
                .updated(updatedCount[0])
                .errors(Collections.emptyList())
                .build();
    }

    /**
     * 合并策略：仅当数据库字段为 null 且上传数据有值时，才用上传值填补；
     * 数据库已有数据的列（无论上传是否有值）一律保留，不覆盖。
     *
     * @return 是否有任意列被填补（用于判定是否需要 UPDATE）
     */
    private boolean mergeMajorIfBlank(Major existing, MajorImportDTO dto) {
        boolean changed = false;
        if (existing.getMajorName() == null && StringUtils.hasText(dto.getMajorName())) {
            existing.setMajorName(dto.getMajorName());
            changed = true;
        }
        if (existing.getDisciplineName() == null && StringUtils.hasText(dto.getDisciplineName())) {
            existing.setDisciplineName(dto.getDisciplineName());
            changed = true;
        }
        if (existing.getMajorType() == null && StringUtils.hasText(dto.getMajorType())) {
            existing.setMajorType(dto.getMajorType());
            changed = true;
        }
        if (existing.getMajorCategory() == null && StringUtils.hasText(dto.getMajorCategory())) {
            existing.setMajorCategory(dto.getMajorCategory());
            changed = true;
        }
        if (existing.getParentCategory() == null && StringUtils.hasText(dto.getParentCategory())) {
            existing.setParentCategory(dto.getParentCategory());
            changed = true;
        }
        if (existing.getMajorTags() == null && StringUtils.hasText(dto.getMajorTags())) {
            existing.setMajorTags(dto.getMajorTags());
            changed = true;
        }
        if (existing.getDegreeAwarded() == null && StringUtils.hasText(dto.getDegreeAwarded())) {
            existing.setDegreeAwarded(dto.getDegreeAwarded());
            changed = true;
        }
        if (existing.getStudyDuration() == null && StringUtils.hasText(dto.getStudyDuration())) {
            existing.setStudyDuration(dto.getStudyDuration());
            changed = true;
        }
        if (existing.getEmploymentRate() == null && dto.getEmploymentRate() != null) {
            existing.setEmploymentRate(dto.getEmploymentRate());
            changed = true;
        }
        if (existing.getSalaryMin() == null && dto.getSalaryMin() != null) {
            existing.setSalaryMin(dto.getSalaryMin());
            changed = true;
        }
        if (existing.getSalaryMax() == null && dto.getSalaryMax() != null) {
            existing.setSalaryMax(dto.getSalaryMax());
            changed = true;
        }
        if (existing.getDescription() == null && StringUtils.hasText(dto.getDescription())) {
            existing.setDescription(dto.getDescription());
            changed = true;
        }
        return changed;
    }

    @Override
    public ImportResultVO importMajorDetail(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<MajorDetailImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(MajorDetailImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "Excel文件解析失败，请确认文件为有效的.xlsx且表头正确: " + e.getMessage());
        }

        if (dataList != null && dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<Long> majorIdsInFile = new HashSet<>();
        int[] updatedCount = {0};
        OffsetDateTime now = OffsetDateTime.now();

        new TransactionTemplate(transactionManager).execute(status -> {
            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = i + 2;
                MajorDetailImportDTO dto = dataList.get(i);

                // 校验必填字段（专业代码用于匹配，必须存在）
                if (!StringUtils.hasText(dto.getMajorCode())) {
                    errors.add("第" + rowNum + "行: 专业代码不能为空");
                    continue;
                }

                // 根据专业代码查找专业ID
                Long majorId = majorMapper.selectIdByMajorCode(dto.getMajorCode());
                if (majorId == null) {
                    errors.add("第" + rowNum + "行: 专业[" + dto.getMajorCode() + "]不存在");
                    continue;
                }

                // 检查文件内majorId是否重复（1:1关系）
                if (majorIdsInFile.contains(majorId)) {
                    errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在文件中重复");
                    continue;
                }
                majorIdsInFile.add(majorId);

                // 查询是否已存在详情（忽略状态，保证 1:1）
                MajorDetail existing = majorDetailMapper.selectOne(
                        new LambdaQueryWrapper<MajorDetail>().eq(MajorDetail::getMajorId, majorId));

                if (existing == null) {
                    // ===== 数据库不存在：新增详情 =====
                    Long detailId = SnowflakeIdGenerator.nextId();
                    MajorDetail detail = MajorDetail.builder()
                            .id(detailId)
                            .majorId(majorId)
                            .courseCount(dto.getCourseCount())
                            .graduateScale(dto.getGraduateScale())
                            .maleRatio(dto.getMaleRatio())
                            .femaleRatio(dto.getFemaleRatio())
                            .majorDescription(dto.getMajorDescription())
                            .trainingObjective(dto.getTrainingObjective())
                            .trainingRequirement(dto.getTrainingRequirement())
                            .subjectRequirement(dto.getSubjectRequirement())
                            .careerProspect(dto.getCareerProspect())
                            .mainCourses(dto.getMainCourses())
                            .knowledgeSkills(dto.getKnowledgeSkills())
                            .status((short) 1)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    try {
                        majorDetailMapper.insert(detail);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        errors.add("第" + rowNum + "行: 专业详情保存失败[" + dto.getMajorCode() + "]: " + e.getMessage());
                    }
                } else {
                    // ===== 数据库已存在：仅补齐为空的列，已有数据的列绝不覆盖 =====
                    boolean changed = mergeMajorDetailIfBlank(existing, dto);
                    if (changed) {
                        try {
                            existing.setUpdatedAt(now);
                            majorDetailMapper.updateById(existing);
                            updatedCount[0]++;
                        } catch (Exception e) {
                            status.setRollbackOnly();
                            errors.add("第" + rowNum + "行: 专业详情更新失败[" + dto.getMajorCode() + "]: " + e.getMessage());
                        }
                    }
                }
            }

            if (!errors.isEmpty()) {
                throw new BusinessException(400, "数据校验失败：" + joinErrors(errors));
            }
            return null;
        });

        int total = dataList.size();
        log.info("导入专业详情数据完成: 总行数={}, 补空更新={}", total, updatedCount[0]);
        return ImportResultVO.builder()
                .total(total)
                .success(total)
                .failed(0)
                .updated(updatedCount[0])
                .errors(Collections.emptyList())
                .build();
    }

    /**
     * 详情表合并策略：仅当数据库字段为 null 且上传数据有值时，才用上传值填补；
     * 数据库已有数据的列（无论上传是否有值）一律保留，不覆盖。
     *
     * @return 是否有任意列被填补（用于判定是否需要 UPDATE）
     */
    private boolean mergeMajorDetailIfBlank(MajorDetail existing, MajorDetailImportDTO dto) {
        boolean changed = false;
        if (existing.getCourseCount() == null && dto.getCourseCount() != null) {
            existing.setCourseCount(dto.getCourseCount());
            changed = true;
        }
        if (existing.getGraduateScale() == null && StringUtils.hasText(dto.getGraduateScale())) {
            existing.setGraduateScale(dto.getGraduateScale());
            changed = true;
        }
        if (existing.getMaleRatio() == null && dto.getMaleRatio() != null) {
            existing.setMaleRatio(dto.getMaleRatio());
            changed = true;
        }
        if (existing.getFemaleRatio() == null && dto.getFemaleRatio() != null) {
            existing.setFemaleRatio(dto.getFemaleRatio());
            changed = true;
        }
        if (existing.getMajorDescription() == null && StringUtils.hasText(dto.getMajorDescription())) {
            existing.setMajorDescription(dto.getMajorDescription());
            changed = true;
        }
        if (existing.getTrainingObjective() == null && StringUtils.hasText(dto.getTrainingObjective())) {
            existing.setTrainingObjective(dto.getTrainingObjective());
            changed = true;
        }
        if (existing.getTrainingRequirement() == null && StringUtils.hasText(dto.getTrainingRequirement())) {
            existing.setTrainingRequirement(dto.getTrainingRequirement());
            changed = true;
        }
        if (existing.getSubjectRequirement() == null && StringUtils.hasText(dto.getSubjectRequirement())) {
            existing.setSubjectRequirement(dto.getSubjectRequirement());
            changed = true;
        }
        if (existing.getCareerProspect() == null && StringUtils.hasText(dto.getCareerProspect())) {
            existing.setCareerProspect(dto.getCareerProspect());
            changed = true;
        }
        if (existing.getMainCourses() == null && dto.getMainCourses() != null) {
            existing.setMainCourses(dto.getMainCourses());
            changed = true;
        }
        if (existing.getKnowledgeSkills() == null && dto.getKnowledgeSkills() != null) {
            existing.setKnowledgeSkills(dto.getKnowledgeSkills());
            changed = true;
        }
        return changed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        updateStatus(id, (short) 1);
        log.info("恢复专业成功: id={}", id);
    }

    /**
     * 将错误列表拼接为单行文本，超过 MAX_ERROR_DISPLAY 条时截断并提示总数。
     */
    private String joinErrors(List<String> errs) {
        if (errs == null || errs.isEmpty()) {
            return null;
        }
        int shown = Math.min(errs.size(), MAX_ERROR_DISPLAY);
        String joined = String.join("; ", errs.subList(0, shown));
        if (errs.size() > MAX_ERROR_DISPLAY) {
            joined += "; ...仅显示前" + MAX_ERROR_DISPLAY + "条，共" + errs.size() + "行存在错误";
        }
        return joined;
    }
}
