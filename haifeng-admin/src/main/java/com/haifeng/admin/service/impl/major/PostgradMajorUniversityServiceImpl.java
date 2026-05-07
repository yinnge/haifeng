package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.PostgradMajorUniversityImportDTO;
import com.haifeng.admin.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.admin.service.major.PostgradMajorUniversityService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorUniversityListVO;
import com.haifeng.common.entity.major.PostgradMajorUniversity;
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
    private final PostgradMajorMapper postgradMajorMapper;
    private final UniversityMapper universityMapper;

    @Override
    public Page<PostgradMajorUniversityListVO> list(PostgradMajorUniversityQueryDTO queryDTO) {
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

        for (Long id : ids) {
            try {
                softDelete(id);
            } catch (BusinessException e) {
                log.warn("批量软删除跳过不存在的考研专业-大学关联: id={}", id);
            }
        }

        log.info("批量软删除考研专业-大学关联完成: 请求数量={}", ids.size());
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
                log.warn("批量硬删除跳过不存在的考研专业-大学关联: id={}", id);
            }
        }

        log.info("批量硬删除考研专业-大学关联完成: 请求数量={}", ids.size());
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
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        // 用于检查文件内是否有重复的(universityName, postgradMajorCode)组合
        Set<String> relationKeysInFile = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2; // Excel行号（从2开始，1是表头）
            PostgradMajorUniversityImportDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 大学名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getPostgradMajorCode())) {
                errors.add("第" + rowNum + "行: 考研专业代码不能为空");
                continue;
            }

            // 检查文件内是否有重复组合
            String relationKey = dto.getUniversityName() + "|" + dto.getPostgradMajorCode();
            if (relationKeysInFile.contains(relationKey)) {
                errors.add("第" + rowNum + "行: [" + dto.getUniversityName() + ", " + dto.getPostgradMajorCode() + "]组合在文件中重复");
                continue;
            }
            relationKeysInFile.add(relationKey);

            // 根据大学名称查询university_id
            Long universityId = universityMapper.selectIdByName(dto.getUniversityName());
            if (universityId == null) {
                errors.add("第" + rowNum + "行: 大学[" + dto.getUniversityName() + "]不存在");
                continue;
            }

            // 根据考研专业代码查询postgrad_major_id和postgrad_major_name
            Long postgradMajorId = postgradMajorMapper.selectIdByMajorCode(dto.getPostgradMajorCode());
            if (postgradMajorId == null) {
                errors.add("第" + rowNum + "行: 考研专业[" + dto.getPostgradMajorCode() + "]不存在");
                continue;
            }

            String postgradMajorName = postgradMajorMapper.selectNameByMajorCode(dto.getPostgradMajorCode());

            // 检查数据库中是否已存在该关联
            if (postgradMajorUniversityMapper.existsByRelation(postgradMajorId, universityId)) {
                errors.add("第" + rowNum + "行: [" + dto.getUniversityName() + ", " + dto.getPostgradMajorCode() + "]关联已存在");
                continue;
            }

            // 构建实体并插入
            Long id = SnowflakeIdGenerator.nextId();
            PostgradMajorUniversity entity = PostgradMajorUniversity.builder()
                    .id(id)
                    .postgradMajorId(postgradMajorId)
                    .universityId(universityId)
                    .universityName(dto.getUniversityName())
                    .postgradMajorName(postgradMajorName)
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            postgradMajorUniversityMapper.insert(entity);
            successCount++;
        }

        if (!errors.isEmpty()) {
            log.warn("导入考研专业-大学关联数据部分失败: 成功{}条, 失败{}条", successCount, errors.size());
        } else {
            log.info("导入考研专业-大学关联数据成功: 共{}条", successCount);
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
