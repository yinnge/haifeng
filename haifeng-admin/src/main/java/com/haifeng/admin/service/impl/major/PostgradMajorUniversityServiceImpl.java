package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.PostgradMajorUniversityAddDTO;
import com.haifeng.admin.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.admin.excel.major.PostgradMajorUniversityImportDTO;
import com.haifeng.admin.service.major.PostgradMajorUniversityService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorUniversityListVO;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.entity.major.PostgradMajorUniversity;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 考研专业-大学关联Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostgradMajorUniversityServiceImpl implements PostgradMajorUniversityService {

    private final PostgradMajorUniversityMapper postgradMajorUniversityMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final int MAX_ERROR_DISPLAY = 50;
    private final PostgradMajorMapper postgradMajorMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<PostgradMajorUniversityListVO> list(PostgradMajorUniversityQueryDTO queryDTO) {
        Page<PostgradMajorUniversity> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<PostgradMajorUniversity> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询
        if (StringUtils.hasText(queryDTO.getUniversityName())) {
            wrapper.like(PostgradMajorUniversity::getUniversityName, queryDTO.getUniversityName());
        }
        if (StringUtils.hasText(queryDTO.getPostgradMajorName())) {
            wrapper.like(PostgradMajorUniversity::getPostgradMajorName, queryDTO.getPostgradMajorName());
        }

        // 精确匹配
        if (queryDTO.getStatus() != null) {
            wrapper.eq(PostgradMajorUniversity::getStatus, queryDTO.getStatus());
        }

        // 排序
        wrapper.orderByAsc(PostgradMajorUniversity::getSortOrder)
               .orderByDesc(PostgradMajorUniversity::getCreatedAt);

        Page<PostgradMajorUniversity> resultPage = postgradMajorUniversityMapper.selectPage(page, wrapper);

        return resultPage.convert(entity -> {
            PostgradMajorUniversityListVO vo = new PostgradMajorUniversityListVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setStatus(entity.getStatus() != null ? entity.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(PostgradMajorUniversityAddDTO dto) {
        // 1. 外键校验：考研专业必须存在且启用
        PostgradMajor postgradMajor = postgradMajorMapper.selectById(dto.getPostgradMajorId());
        if (postgradMajor == null || postgradMajor.getStatus() == null || postgradMajor.getStatus() != 1) {
            throw new BusinessException(400, "考研专业不存在或已禁用");
        }

        // 2. 外键校验：大学必须存在且启用
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == null || university.getStatus() != 1) {
            throw new BusinessException(400, "大学不存在或已禁用");
        }

        // 3. 唯一性校验：同一(考研专业, 大学)组合不能重复
        if (postgradMajorUniversityMapper.existsByRelation(dto.getPostgradMajorId(), dto.getUniversityId())) {
            throw new BusinessException(400, "该考研专业与大学的关联已存在");
        }

        // 4. 组装实体并插入（冗余写入名称，与列表/导入保持一致）
        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();
        PostgradMajorUniversity entity = PostgradMajorUniversity.builder()
                .id(id)
                .postgradMajorId(dto.getPostgradMajorId())
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .postgradMajorName(postgradMajor.getMajorName())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        postgradMajorUniversityMapper.insert(entity);

        log.info("新增考研专业-大学关联成功: id={}, postgradMajorId={}, universityId={}",
                id, dto.getPostgradMajorId(), dto.getUniversityId());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        PostgradMajorUniversity entity = postgradMajorUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "考研专业-大学关联记录不存在");
        }

        LambdaUpdateWrapper<PostgradMajorUniversity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PostgradMajorUniversity::getId, id)
               .set(PostgradMajorUniversity::getStatus, (short) 0)
               .set(PostgradMajorUniversity::getUpdatedAt, OffsetDateTime.now());
        postgradMajorUniversityMapper.update(null, wrapper);

        log.info("软删除考研专业-大学关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        PostgradMajorUniversity entity = postgradMajorUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "考研专业-大学关联记录不存在");
        }

        postgradMajorUniversityMapper.deleteById(id);

        log.info("硬删除考研专业-大学关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSoftDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        OffsetDateTime now = OffsetDateTime.now();
        LambdaUpdateWrapper<PostgradMajorUniversity> wrapper = new LambdaUpdateWrapper<PostgradMajorUniversity>()
                .in(PostgradMajorUniversity::getId, ids)
                .set(PostgradMajorUniversity::getStatus, (short) 0)
                .set(PostgradMajorUniversity::getUpdatedAt, now);
        int updated = postgradMajorUniversityMapper.update(null, wrapper);

        log.info("批量软删除考研专业-大学关联完成: 请求数量={}, 实际更新={}", ids.size(), updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        int deleted = postgradMajorUniversityMapper.deleteByIds(ids);

        log.info("批量硬删除考研专业-大学关联完成: 请求数量={}, 实际删除={}", ids.size(), deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importPostgradMajorUniversity(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<PostgradMajorUniversityImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(PostgradMajorUniversityImportDTO.class)
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
        // 用于检查文件内是否有重复的(universityName, postgradMajorCode)组合
        Set<String> relationKeysInFile = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;
        int updatedCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2; // Excel行号（从2开始，1是表头）
            PostgradMajorUniversityImportDTO dto = dataList.get(i);
            // 去除首尾空格，避免 Excel 单元格空格导致大学/专业匹配误判
            String universityName = dto.getUniversityName() == null ? null : dto.getUniversityName().trim();
            String postgradMajorCode = dto.getPostgradMajorCode() == null ? null : dto.getPostgradMajorCode().trim();

            // 校验必填字段
            if (!StringUtils.hasText(universityName)) {
                errors.add("第" + rowNum + "行: 大学名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(postgradMajorCode)) {
                errors.add("第" + rowNum + "行: 考研专业代码不能为空");
                continue;
            }

            // 检查文件内是否有重复组合
            String relationKey = universityName + "|" + postgradMajorCode;
            if (relationKeysInFile.contains(relationKey)) {
                errors.add("第" + rowNum + "行: [" + universityName + ", " + postgradMajorCode + "]组合在文件中重复");
                continue;
            }
            relationKeysInFile.add(relationKey);

            // 根据大学名称查询university_id
            Long universityId = universityMapper.selectIdByName(universityName);
            if (universityId == null) {
                errors.add("第" + rowNum + "行: 大学[" + universityName + "]不存在");
                continue;
            }

            // 根据考研专业代码查询postgrad_major_id和postgrad_major_name
            Long postgradMajorId = postgradMajorMapper.selectIdByMajorCode(postgradMajorCode);
            if (postgradMajorId == null) {
                errors.add("第" + rowNum + "行: 考研专业[" + postgradMajorCode + "]不存在");
                continue;
            }

            String postgradMajorName = postgradMajorMapper.selectNameByMajorCode(postgradMajorCode);

            // 查询是否已存在该关联
            PostgradMajorUniversity existing = postgradMajorUniversityMapper.selectOne(
                    new LambdaQueryWrapper<PostgradMajorUniversity>()
                            .eq(PostgradMajorUniversity::getPostgradMajorId, postgradMajorId)
                            .eq(PostgradMajorUniversity::getUniversityId, universityId));

            if (existing == null) {
                // ===== 数据库不存在：新增 =====
                Long id = SnowflakeIdGenerator.nextId();
                PostgradMajorUniversity entity = PostgradMajorUniversity.builder()
                        .id(id)
                        .postgradMajorId(postgradMajorId)
                        .universityId(universityId)
                        .universityName(universityName)
                        .postgradMajorName(postgradMajorName)
                        .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                        .status((short) 1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                try {
                    postgradMajorUniversityMapper.insert(entity);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + rowNum + "行: 保存失败[大学=" + universityName + ", 专业代码=" + postgradMajorCode + "]: " + e.getMessage());
                }
            } else {
                // ===== 数据库已存在：仅补齐为空的列（仅排序权重），已有数据的列绝不覆盖 =====
                boolean changed = mergePostgradMajorUniversityIfBlank(existing, dto);
                boolean saved = true;
                if (changed) {
                    existing.setUpdatedAt(now);
                    try {
                        postgradMajorUniversityMapper.updateById(existing);
                        updatedCount++;
                    } catch (Exception e) {
                        errors.add("第" + rowNum + "行: 保存失败[大学=" + universityName + ", 专业代码=" + postgradMajorCode + "]: " + e.getMessage());
                        saved = false;
                    }
                }
                if (saved) {
                    successCount++;
                }
            }
        }

        if (!errors.isEmpty()) {
            String detail = errors.size() > MAX_ERROR_DISPLAY
                    ? String.join("; ", errors.subList(0, MAX_ERROR_DISPLAY)) + " 等" + errors.size() + "条错误"
                    : String.join("; ", errors);
            throw new BusinessException(400, "导入失败，共" + errors.size() + "行数据存在错误，已全部回滚：" + detail);
        }

        log.info("导入考研专业-大学关联数据完成: 新增/补齐{}条, 其中补齐{}条", successCount, updatedCount);
        return ImportResultVO.builder()
                .total(dataList.size())
                .success(successCount)
                .failed(0)
                .updated(updatedCount)
                .errors(null)
                .build();
    }

    /**
     * 关联表合并策略：仅当数据库字段为 null 且上传数据有值时，才用上传值填补；
     * 数据库已有数据的列（无论上传是否有值）一律保留，不覆盖。
     */
    private boolean mergePostgradMajorUniversityIfBlank(PostgradMajorUniversity existing, PostgradMajorUniversityImportDTO dto) {
        boolean changed = false;
        if (existing.getSortOrder() == null && dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
            changed = true;
        }
        return changed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        PostgradMajorUniversity entity = postgradMajorUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "考研专业-大学关联记录不存在");
        }

        LambdaUpdateWrapper<PostgradMajorUniversity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PostgradMajorUniversity::getId, id)
               .set(PostgradMajorUniversity::getStatus, (short) 1)
               .set(PostgradMajorUniversity::getUpdatedAt, OffsetDateTime.now());
        postgradMajorUniversityMapper.update(null, wrapper);

        log.info("恢复考研专业-大学关联成功: id={}", id);
    }
}
