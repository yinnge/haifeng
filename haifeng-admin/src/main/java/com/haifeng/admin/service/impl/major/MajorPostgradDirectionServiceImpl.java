package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
import com.haifeng.admin.excel.major.MajorPostgradDirectionImportDTO;
import com.haifeng.admin.service.major.MajorPostgradDirectionService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionDetailVO;
import com.haifeng.admin.vo.major.MajorPostgradDirectionListVO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorPostgradDirection;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorPostgradDirectionServiceImpl implements MajorPostgradDirectionService {

    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final int MAX_ERROR_DISPLAY = 50;
    private final MajorMapper majorMapper;
    private final PostgradMajorMapper postgradMajorMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public IPage<MajorPostgradDirectionListVO> list(MajorPostgradDirectionQueryDTO queryDTO) {
        Page<MajorPostgradDirection> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<MajorPostgradDirection> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getMajorName())) {
            wrapper.like(MajorPostgradDirection::getMajorName, queryDTO.getMajorName());
        }
        if (StringUtils.hasText(queryDTO.getPostgradMajorName())) {
            wrapper.like(MajorPostgradDirection::getPostgradMajorName, queryDTO.getPostgradMajorName());
        }

        wrapper.orderByAsc(MajorPostgradDirection::getSortOrder)
               .orderByDesc(MajorPostgradDirection::getCreatedAt);

        Page<MajorPostgradDirection> resultPage = majorPostgradDirectionMapper.selectPage(page, wrapper);

        return resultPage.convert(entity -> {
            MajorPostgradDirectionListVO vo = new MajorPostgradDirectionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public MajorPostgradDirectionDetailVO getDetail(Long id) {
        MajorPostgradDirection entity = majorPostgradDirectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        MajorPostgradDirectionDetailVO vo = new MajorPostgradDirectionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MajorPostgradDirectionAddDTO dto) {
        // 查询本科专业
        Major major = majorMapper.selectById(dto.getMajorId());
        if (major == null) {
            throw new BusinessException(400, "本科专业不存在");
        }

        // 查询考研专业
        PostgradMajor postgradMajor = postgradMajorMapper.selectById(dto.getPostgradMajorId());
        if (postgradMajor == null) {
            throw new BusinessException(400, "考研专业不存在");
        }

        // 检查是否已存在
        if (majorPostgradDirectionMapper.existsByRelation(dto.getMajorId(), dto.getPostgradMajorId())) {
            throw new BusinessException(400, "该关联已存在");
        }

        MajorPostgradDirection entity = MajorPostgradDirection.builder()
                .id(SnowflakeIdGenerator.nextId())
                .majorId(dto.getMajorId())
                .postgradMajorId(dto.getPostgradMajorId())
                .majorName(major.getMajorName())
                .postgradMajorName(postgradMajor.getMajorName())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .createdAt(OffsetDateTime.now())
                .build();

        majorPostgradDirectionMapper.insert(entity);
        log.info("新增本科专业-考研方向关联成功: majorId={}, postgradMajorId={}", dto.getMajorId(), dto.getPostgradMajorId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MajorPostgradDirectionUpdateDTO dto) {
        MajorPostgradDirection entity = majorPostgradDirectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        // 如果修改了关联关系，检查新关联是否已存在
        if (!entity.getMajorId().equals(dto.getMajorId()) ||
            !entity.getPostgradMajorId().equals(dto.getPostgradMajorId())) {

            if (majorPostgradDirectionMapper.existsByRelation(dto.getMajorId(), dto.getPostgradMajorId())) {
                throw new BusinessException(400, "该关联已存在");
            }

            // 查询新的名称
            Major major = majorMapper.selectById(dto.getMajorId());
            if (major == null) {
                throw new BusinessException(400, "本科专业不存在");
            }

            PostgradMajor postgradMajor = postgradMajorMapper.selectById(dto.getPostgradMajorId());
            if (postgradMajor == null) {
                throw new BusinessException(400, "考研专业不存在");
            }

            entity.setMajorId(dto.getMajorId());
            entity.setPostgradMajorId(dto.getPostgradMajorId());
            entity.setMajorName(major.getMajorName());
            entity.setPostgradMajorName(postgradMajor.getMajorName());
        }

        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }

        majorPostgradDirectionMapper.updateById(entity);
        log.info("修改本科专业-考研方向关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MajorPostgradDirection entity = majorPostgradDirectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "关联记录不存在");
        }

        majorPostgradDirectionMapper.deleteById(id);
        log.info("删除本科专业-考研方向关联成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        int deleted = majorPostgradDirectionMapper.deleteByIds(ids);

        log.info("批量删除本科专业-考研方向关联完成: 请求数量={}, 实际删除={}", ids.size(), deleted);
    }

    @Override
    public ImportResultVO importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<MajorPostgradDirectionImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(MajorPostgradDirectionImportDTO.class)
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
        Set<String> relationKeysInFile = new HashSet<>();
        int[] updatedCount = {0};
        OffsetDateTime now = OffsetDateTime.now();

        new TransactionTemplate(transactionManager).execute(status -> {
            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = i + 2;
                MajorPostgradDirectionImportDTO dto = dataList.get(i);

                // 校验必填字段
                if (!StringUtils.hasText(dto.getMajorName())) {
                    errors.add("第" + rowNum + "行: 本科专业名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(dto.getPostgradMajorName())) {
                    errors.add("第" + rowNum + "行: 考研专业名称不能为空");
                    continue;
                }

                // 检查文件内是否有重复组合
                String relationKey = dto.getMajorName() + "|" + dto.getPostgradMajorName();
                if (relationKeysInFile.contains(relationKey)) {
                    errors.add("第" + rowNum + "行: [" + dto.getMajorName() + ", " + dto.getPostgradMajorName() + "]组合在文件中重复");
                    continue;
                }
                relationKeysInFile.add(relationKey);

                // 根据本科专业名称查询major_id
                Major major = majorMapper.findByMajorName(dto.getMajorName());
                if (major == null) {
                    errors.add("第" + rowNum + "行: 本科专业[" + dto.getMajorName() + "]不存在");
                    continue;
                }

                // 根据考研专业名称查询postgrad_major_id
                Long postgradMajorId = postgradMajorMapper.selectIdByName(dto.getPostgradMajorName());
                if (postgradMajorId == null) {
                    errors.add("第" + rowNum + "行: 考研专业[" + dto.getPostgradMajorName() + "]不存在");
                    continue;
                }

                // 查询是否已存在该关联
                MajorPostgradDirection existing = majorPostgradDirectionMapper.selectOne(
                        new LambdaQueryWrapper<MajorPostgradDirection>()
                                .eq(MajorPostgradDirection::getMajorId, major.getId())
                                .eq(MajorPostgradDirection::getPostgradMajorId, postgradMajorId));

                if (existing == null) {
                    // ===== 数据库不存在：新增 =====
                    MajorPostgradDirection entity = MajorPostgradDirection.builder()
                            .id(SnowflakeIdGenerator.nextId())
                            .majorId(major.getId())
                            .postgradMajorId(postgradMajorId)
                            .majorName(dto.getMajorName())
                            .postgradMajorName(dto.getPostgradMajorName())
                            .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                            .createdAt(now)
                            .build();

                    try {
                        majorPostgradDirectionMapper.insert(entity);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        errors.add("第" + rowNum + "行: 关联保存失败[" + dto.getMajorName() + "->" + dto.getPostgradMajorName() + "]: " + e.getMessage());
                    }
                } else {
                    // ===== 数据库已存在：仅补齐为空的列（仅排序权重），已有数据的列绝不覆盖 =====
                    boolean changed = mergeMajorPostgradDirectionIfBlank(existing, dto);
                    if (changed) {
                        try {
                            majorPostgradDirectionMapper.updateById(existing);
                            updatedCount[0]++;
                        } catch (Exception e) {
                            status.setRollbackOnly();
                            errors.add("第" + rowNum + "行: 关联更新失败[" + dto.getMajorName() + "->" + dto.getPostgradMajorName() + "]: " + e.getMessage());
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
        log.info("导入本科专业-考研方向关联数据完成: 总行数={}, 补空更新={}", total, updatedCount[0]);
        return ImportResultVO.builder()
                .total(total)
                .success(total)
                .failed(0)
                .updated(updatedCount[0])
                .errors(Collections.emptyList())
                .build();
    }

    /**
     * 关联表合并策略：仅当数据库字段为 null 且上传数据有值时，才用上传值填补；
     * 数据库已有数据的列（无论上传是否有值）一律保留，不覆盖。
     */
    private boolean mergeMajorPostgradDirectionIfBlank(MajorPostgradDirection existing, MajorPostgradDirectionImportDTO dto) {
        boolean changed = false;
        if (existing.getSortOrder() == null && dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
            changed = true;
        }
        return changed;
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
