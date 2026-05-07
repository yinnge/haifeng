package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.service.major.PostgradMajorService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorDetailVO;
import com.haifeng.admin.vo.major.PostgradMajorListVO;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * 考研专业管理Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostgradMajorServiceImpl implements PostgradMajorService {

    private final PostgradMajorMapper postgradMajorMapper;

    /**
     * 有效的学位类型
     */
    private static final Set<String> VALID_DEGREE_TYPES = Set.of("学术学位", "专业学位");

    /**
     * 有效的热门程度
     */
    private static final Set<String> VALID_POPULARITY = Set.of("热门", "一般", "冷门");

    /**
     * 有效的难度等级
     */
    private static final Set<String> VALID_DIFFICULTY = Set.of("高", "中", "低");

    /**
     * 有效的跨考难度
     */
    private static final Set<String> VALID_CROSS_EXAM_DIFFICULTY = Set.of("较易", "中等", "较难");

    @Override
    public Page<PostgradMajorListVO> list(PostgradMajorQueryDTO queryDTO) {
        Page<PostgradMajor> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<PostgradMajor> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询
        if (StringUtils.hasText(queryDTO.getMajorName())) {
            wrapper.like(PostgradMajor::getMajorName, queryDTO.getMajorName());
        }
        if (StringUtils.hasText(queryDTO.getMajorCode())) {
            wrapper.like(PostgradMajor::getMajorCode, queryDTO.getMajorCode());
        }

        // 精确匹配
        if (StringUtils.hasText(queryDTO.getDegreeType())) {
            wrapper.eq(PostgradMajor::getDegreeType, queryDTO.getDegreeType());
        }
        if (StringUtils.hasText(queryDTO.getDisciplineCategory())) {
            wrapper.eq(PostgradMajor::getDisciplineCategory, queryDTO.getDisciplineCategory());
        }
        if (StringUtils.hasText(queryDTO.getPopularity())) {
            wrapper.eq(PostgradMajor::getPopularity, queryDTO.getPopularity());
        }
        if (StringUtils.hasText(queryDTO.getDifficulty())) {
            wrapper.eq(PostgradMajor::getDifficulty, queryDTO.getDifficulty());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(PostgradMajor::getStatus, queryDTO.getStatus());
        }

        // 排序
        wrapper.orderByDesc(PostgradMajor::getCreatedAt);

        Page<PostgradMajor> majorPage = postgradMajorMapper.selectPage(page, wrapper);

        return majorPage.convert(major -> {
            PostgradMajorListVO vo = new PostgradMajorListVO();
            BeanUtils.copyProperties(major, vo);
            vo.setStatus(major.getStatus() != null ? major.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    public PostgradMajorDetailVO getById(Long id) {
        PostgradMajor major = postgradMajorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "考研专业不存在");
        }

        PostgradMajorDetailVO vo = new PostgradMajorDetailVO();
        BeanUtils.copyProperties(major, vo);
        vo.setStatus(major.getStatus() != null ? major.getStatus().intValue() : null);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(PostgradMajorAddDTO addDTO) {
        // 检查majorCode是否存在
        if (postgradMajorMapper.existsByMajorCode(addDTO.getMajorCode())) {
            throw new BusinessException(400, "专业代码已存在");
        }

        // 检查degreeType
        if (!VALID_DEGREE_TYPES.contains(addDTO.getDegreeType())) {
            throw new BusinessException(400, "学位类型必须为学术学位或专业学位");
        }

        // 检查popularity（可为空）
        if (StringUtils.hasText(addDTO.getPopularity()) && !VALID_POPULARITY.contains(addDTO.getPopularity())) {
            throw new BusinessException(400, "热门程度必须为热门、一般或冷门");
        }

        // 检查difficulty（可为空）
        if (StringUtils.hasText(addDTO.getDifficulty()) && !VALID_DIFFICULTY.contains(addDTO.getDifficulty())) {
            throw new BusinessException(400, "难度等级必须为高、中或低");
        }

        // 检查crossExamDifficulty（可为空）
        if (StringUtils.hasText(addDTO.getCrossExamDifficulty()) && !VALID_CROSS_EXAM_DIFFICULTY.contains(addDTO.getCrossExamDifficulty())) {
            throw new BusinessException(400, "跨考难度必须为较易、中等或较难");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        PostgradMajor major = PostgradMajor.builder()
                .id(id)
                .majorName(addDTO.getMajorName())
                .majorCode(addDTO.getMajorCode())
                .degreeType(addDTO.getDegreeType())
                .disciplineCategory(addDTO.getDisciplineCategory())
                .popularity(addDTO.getPopularity())
                .difficulty(addDTO.getDifficulty())
                .brief(addDTO.getBrief())
                .introduction(addDTO.getIntroduction())
                .examSubjects(addDTO.getExamSubjects())
                .admissionRequirements(addDTO.getAdmissionRequirements())
                .crossExamFactors(addDTO.getCrossExamFactors())
                .crossExamDifficulty(addDTO.getCrossExamDifficulty())
                .crossExamDescription(addDTO.getCrossExamDescription())
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        postgradMajorMapper.insert(major);

        log.info("新增考研专业成功: id={}, majorCode={}, majorName={}", id, addDTO.getMajorCode(), addDTO.getMajorName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PostgradMajorUpdateDTO updateDTO) {
        PostgradMajor major = postgradMajorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "考研专业不存在");
        }

        // 检查degreeType（可为空，不为空时校验）
        if (StringUtils.hasText(updateDTO.getDegreeType()) && !VALID_DEGREE_TYPES.contains(updateDTO.getDegreeType())) {
            throw new BusinessException(400, "学位类型必须为学术学位或专业学位");
        }

        // 检查popularity（可为空）
        if (StringUtils.hasText(updateDTO.getPopularity()) && !VALID_POPULARITY.contains(updateDTO.getPopularity())) {
            throw new BusinessException(400, "热门程度必须为热门、一般或冷门");
        }

        // 检查difficulty（可为空）
        if (StringUtils.hasText(updateDTO.getDifficulty()) && !VALID_DIFFICULTY.contains(updateDTO.getDifficulty())) {
            throw new BusinessException(400, "难度等级必须为高、中或低");
        }

        // 检查crossExamDifficulty（可为空）
        if (StringUtils.hasText(updateDTO.getCrossExamDifficulty()) && !VALID_CROSS_EXAM_DIFFICULTY.contains(updateDTO.getCrossExamDifficulty())) {
            throw new BusinessException(400, "跨考难度必须为较易、中等或较难");
        }

        // 手动复制字段，排除id, majorCode, createdAt
        if (updateDTO.getMajorName() != null) {
            major.setMajorName(updateDTO.getMajorName());
        }
        if (updateDTO.getDegreeType() != null) {
            major.setDegreeType(updateDTO.getDegreeType());
        }
        if (updateDTO.getDisciplineCategory() != null) {
            major.setDisciplineCategory(updateDTO.getDisciplineCategory());
        }
        if (updateDTO.getPopularity() != null) {
            major.setPopularity(updateDTO.getPopularity());
        }
        if (updateDTO.getDifficulty() != null) {
            major.setDifficulty(updateDTO.getDifficulty());
        }
        if (updateDTO.getBrief() != null) {
            major.setBrief(updateDTO.getBrief());
        }
        if (updateDTO.getIntroduction() != null) {
            major.setIntroduction(updateDTO.getIntroduction());
        }
        if (updateDTO.getExamSubjects() != null) {
            major.setExamSubjects(updateDTO.getExamSubjects());
        }
        if (updateDTO.getAdmissionRequirements() != null) {
            major.setAdmissionRequirements(updateDTO.getAdmissionRequirements());
        }
        if (updateDTO.getCrossExamFactors() != null) {
            major.setCrossExamFactors(updateDTO.getCrossExamFactors());
        }
        if (updateDTO.getCrossExamDifficulty() != null) {
            major.setCrossExamDifficulty(updateDTO.getCrossExamDifficulty());
        }
        if (updateDTO.getCrossExamDescription() != null) {
            major.setCrossExamDescription(updateDTO.getCrossExamDescription());
        }
        if (updateDTO.getStatus() != null) {
            major.setStatus(updateDTO.getStatus());
        }
        major.setUpdatedAt(OffsetDateTime.now());

        postgradMajorMapper.updateById(major);

        log.info("修改考研专业成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        PostgradMajor major = postgradMajorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "考研专业不存在");
        }

        major.setStatus(status);
        major.setUpdatedAt(OffsetDateTime.now());
        postgradMajorMapper.updateById(major);

        log.info("修改考研专业状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        updateStatus(id, (short) 0);
        log.info("软删除考研专业成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        PostgradMajor major = postgradMajorMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(404, "考研专业不存在");
        }

        postgradMajorMapper.deleteById(id);

        log.info("硬删除考研专业成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSoftDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        for (Long id : ids) {
            try {
                softDelete(id);
            } catch (BusinessException e) {
                // 忽略不存在的记录，继续处理其他记录
                log.warn("批量软删除跳过不存在的考研专业: id={}", id);
            }
        }

        log.info("批量软删除考研专业完成: 请求数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        for (Long id : ids) {
            try {
                hardDelete(id);
            } catch (BusinessException e) {
                // 忽略不存在的记录，继续处理其他记录
                log.warn("批量硬删除跳过不存在的考研专业: id={}", id);
            }
        }

        log.info("批量硬删除考研专业完成: 请求数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importPostgradMajor(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<PostgradMajorImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(PostgradMajorImportDTO.class)
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
            PostgradMajorImportDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getMajorName())) {
                errors.add("第" + rowNum + "行: 专业名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getDegreeType())) {
                errors.add("第" + rowNum + "行: 学位类型不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getDisciplineCategory())) {
                errors.add("第" + rowNum + "行: 学科门类不能为空");
                continue;
            }

            // 校验学位类型枚举
            if (!VALID_DEGREE_TYPES.contains(dto.getDegreeType())) {
                errors.add("第" + rowNum + "行: 学位类型必须为学术学位或专业学位");
                continue;
            }

            // 校验热门程度枚举（可为空）
            if (StringUtils.hasText(dto.getPopularity()) && !VALID_POPULARITY.contains(dto.getPopularity())) {
                errors.add("第" + rowNum + "行: 热门程度必须为热门、一般或冷门");
                continue;
            }

            // 校验难度等级枚举（可为空）
            if (StringUtils.hasText(dto.getDifficulty()) && !VALID_DIFFICULTY.contains(dto.getDifficulty())) {
                errors.add("第" + rowNum + "行: 难度等级必须为高、中或低");
                continue;
            }

            // 校验跨考难度枚举（可为空）
            if (StringUtils.hasText(dto.getCrossExamDifficulty()) && !VALID_CROSS_EXAM_DIFFICULTY.contains(dto.getCrossExamDifficulty())) {
                errors.add("第" + rowNum + "行: 跨考难度必须为较易、中等或较难");
                continue;
            }

            // 检查文件内重复
            if (majorCodesInFile.contains(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在文件中重复");
                continue;
            }
            majorCodesInFile.add(dto.getMajorCode());

            // 检查数据库中是否已存在
            if (postgradMajorMapper.existsByMajorCode(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]已存在");
                continue;
            }

            // 解析数组字段（以逗号分隔）
            String[] examSubjects = parseArrayField(dto.getExamSubjects());
            String[] admissionRequirements = parseArrayField(dto.getAdmissionRequirements());
            String[] crossExamFactors = parseArrayField(dto.getCrossExamFactors());

            // 构建实体并插入
            Long id = SnowflakeIdGenerator.nextId();
            PostgradMajor major = PostgradMajor.builder()
                    .id(id)
                    .majorName(dto.getMajorName())
                    .majorCode(dto.getMajorCode())
                    .degreeType(dto.getDegreeType())
                    .disciplineCategory(dto.getDisciplineCategory())
                    .popularity(dto.getPopularity())
                    .difficulty(dto.getDifficulty())
                    .brief(dto.getBrief())
                    .introduction(dto.getIntroduction())
                    .examSubjects(examSubjects)
                    .admissionRequirements(admissionRequirements)
                    .crossExamFactors(crossExamFactors)
                    .crossExamDifficulty(dto.getCrossExamDifficulty())
                    .crossExamDescription(dto.getCrossExamDescription())
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            postgradMajorMapper.insert(major);
            successCount++;
        }

        if (!errors.isEmpty()) {
            log.warn("导入考研专业数据部分失败: 成功{}条, 失败{}条", successCount, errors.size());
        } else {
            log.info("导入考研专业数据成功: 共{}条", successCount);
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
        log.info("恢复考研专业成功: id={}", id);
    }

    /**
     * 解析数组字段（以逗号或顿号分隔）
     */
    private String[] parseArrayField(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        // 支持逗号、顿号分隔
        return Arrays.stream(value.split("[,、]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }
}
