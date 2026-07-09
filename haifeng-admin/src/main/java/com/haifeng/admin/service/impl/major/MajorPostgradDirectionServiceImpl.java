package com.haifeng.admin.service.impl.major;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.major.MajorPostgradDirectionAddDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionImportDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionQueryDTO;
import com.haifeng.admin.dto.major.MajorPostgradDirectionUpdateDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorPostgradDirectionServiceImpl implements MajorPostgradDirectionService {

    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;
    private final MajorMapper majorMapper;
    private final PostgradMajorMapper postgradMajorMapper;

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

        int deleted = majorPostgradDirectionMapper.deleteBatchIds(ids);

        log.info("批量删除本科专业-考研方向关联完成: 请求数量={}, 实际删除={}", ids.size(), deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> relationKeysInFile = new HashSet<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

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

            // 检查数据库中是否已存在该关联
            if (majorPostgradDirectionMapper.existsByRelation(major.getId(), postgradMajorId)) {
                errors.add("第" + rowNum + "行: [" + dto.getMajorName() + ", " + dto.getPostgradMajorName() + "]关联已存在");
                continue;
            }

            // 构建实体并插入
            MajorPostgradDirection entity = MajorPostgradDirection.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .majorId(major.getId())
                    .postgradMajorId(postgradMajorId)
                    .majorName(dto.getMajorName())
                    .postgradMajorName(dto.getPostgradMajorName())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .createdAt(now)
                    .build();

            majorPostgradDirectionMapper.insert(entity);
            successCount++;
        }

        if (!errors.isEmpty()) {
            log.warn("导入本科专业-考研方向关联数据部分失败: 成功{}条, 失败{}条", successCount, errors.size());
        } else {
            log.info("导入本科专业-考研方向关联数据成功: 共{}条", successCount);
        }

        return ImportResultVO.builder()
                .total(dataList.size())
                .success(successCount)
                .failed(errors.size())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }
}
