package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private final MajorDetailMapper majorDetailMapper;

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
        int deleted = majorMapper.deleteBatchIds(ids);

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
    @Transactional(rollbackFor = Exception.class)
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
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> majorCodesInFile = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2; // Excel行号（从2开始，1是表头）
            MajorImportDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getMajorName())) {
                errors.add("第" + rowNum + "行: 专业名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getMajorType())) {
                errors.add("第" + rowNum + "行: 专业类型不能为空");
                continue;
            }

            // 检查文件内重复
            if (majorCodesInFile.contains(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在文件中重复");
                continue;
            }
            majorCodesInFile.add(dto.getMajorCode());

            // 检查数据库中是否已存在
            if (majorMapper.existsByMajorCode(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]已存在");
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
                    .employmentRate(dto.getEmploymentRate())
                    .salaryMin(dto.getSalaryMin())
                    .salaryMax(dto.getSalaryMax())
                    .description(dto.getDescription())
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            majorMapper.insert(major);
            successCount++;
        }

        if (!errors.isEmpty()) {
            log.warn("导入专业数据部分失败: 成功{}条, 失败{}条", successCount, errors.size());
        } else {
            log.info("导入专业数据成功: 共{}条", successCount);
        }

        return ImportResultVO.builder()
                .total(dataList.size())
                .success(successCount)
                .failed(errors.size())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<Long> majorIdsInFile = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            MajorDetailImportDTO dto = dataList.get(i);

            // 校验必填字段
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

            // 检查数据库中是否已存在详情
            if (majorDetailMapper.existsByMajorId(majorId)) {
                errors.add("第" + rowNum + "行: 专业[" + dto.getMajorCode() + "]已有详情记录");
                continue;
            }

            // 构建实体并插入
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

            majorDetailMapper.insert(detail);
            successCount++;
        }

        if (!errors.isEmpty()) {
            log.warn("导入专业详情数据部分失败: 成功{}条, 失败{}条", successCount, errors.size());
        } else {
            log.info("导入专业详情数据成功: 共{}条", successCount);
        }

        return ImportResultVO.builder()
                .total(dataList.size())
                .success(successCount)
                .failed(errors.size())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        updateStatus(id, (short) 1);
        log.info("恢复专业成功: id={}", id);
    }
}
