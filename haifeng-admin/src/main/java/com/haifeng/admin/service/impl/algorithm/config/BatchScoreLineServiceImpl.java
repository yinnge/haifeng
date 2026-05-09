package com.haifeng.admin.service.impl.algorithm.config;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.excel.algorithm.config.BatchScoreLineImportDTO;
import com.haifeng.admin.service.algorithm.config.BatchScoreLineService;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.BatchScoreLineMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchScoreLineServiceImpl implements BatchScoreLineService {

    private final BatchScoreLineMapper batchScoreLineMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<BatchScoreLineListVO> page(BatchScoreLineQueryDTO dto) {
        Page<BatchScoreLine> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<BatchScoreLine> wrapper = new LambdaQueryWrapper<>();

        // 精准查询条件
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(BatchScoreLine::getProvince, dto.getProvince());
        }
        if (dto.getYear() != null) {
            wrapper.eq(BatchScoreLine::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(BatchScoreLine::getSubjectType, dto.getSubjectType());
        }
        if (StringUtils.hasText(dto.getBatch())) {
            wrapper.eq(BatchScoreLine::getBatch, dto.getBatch());
        }
        if (dto.getScoreLine() != null) {
            wrapper.eq(BatchScoreLine::getScoreLine, dto.getScoreLine());
        }

        wrapper.orderByAsc(BatchScoreLine::getProvince)
               .orderByDesc(BatchScoreLine::getYear)
               .orderByAsc(BatchScoreLine::getBatch);

        IPage<BatchScoreLine> resultPage = batchScoreLineMapper.selectPage(page, wrapper);

        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public BatchScoreLineDetailVO detail(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Long add(BatchScoreLineAddDTO dto) {
        // 检查唯一约束(province+year+subjectType+batch)
        Long existingId = batchScoreLineMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        if (existingId != null) {
            throw new BusinessException(400, "该批次分数线记录已存在（相同省份、年份、科类、批次）");
        }

        BatchScoreLine entity = BatchScoreLine.builder()
                .id(snowflakeIdGenerator.nextId())
                .province(dto.getProvince())
                .year(dto.getYear())
                .subjectType(dto.getSubjectType())
                .batch(dto.getBatch())
                .scoreLine(dto.getScoreLine())
                .rankLine(dto.getRankLine())
                .remark(dto.getRemark())
                .build();

        batchScoreLineMapper.insert(entity);
        log.info("新增批次分数线记录，province={}, year={}, subjectType={}, batch={}",
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        return entity.getId();
    }

    @Override
    public void update(Long id, BatchScoreLineAddDTO dto) {
        BatchScoreLine existing = batchScoreLineMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }

        // 检查唯一约束（排除自身）
        Long existingId = batchScoreLineMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该批次分数线记录已存在（相同省份、年份、科类、批次）");
        }

        existing.setProvince(dto.getProvince());
        existing.setYear(dto.getYear());
        existing.setSubjectType(dto.getSubjectType());
        existing.setBatch(dto.getBatch());
        existing.setScoreLine(dto.getScoreLine());
        existing.setRankLine(dto.getRankLine());
        existing.setRemark(dto.getRemark());

        batchScoreLineMapper.updateById(existing);
        log.info("修改批次分数线记录，id={}", id);
    }

    @Override
    public void delete(Long id) {
        int rows = batchScoreLineMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }
        log.info("删除批次分数线记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        batchScoreLineMapper.deleteBatchIds(ids);
        log.info("批量删除批次分数线记录，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<BatchScoreLineImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(BatchScoreLineImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        // ==================== 第一次遍历：校验 ====================
        List<String> errors = new ArrayList<>();
        Set<String> validSubjectTypes = Set.of("理科", "物理类", "文科", "历史类", "不分文理");
        // 用于检查Excel内重复
        Set<String> excelKeys = new HashSet<>();
        // 用于批量检查数据库重复
        List<String> businessKeys = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            BatchScoreLineImportDTO dto = dataList.get(i);

            // 必填字段校验
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份不能为空");
                continue;
            }
            if (dto.getYear() == null) {
                errors.add("第" + rowNum + "行: 年份不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次不能为空");
                continue;
            }
            if (dto.getScoreLine() == null) {
                errors.add("第" + rowNum + "行: 分数线不能为空");
                continue;
            }

            // 枚举值校验
            if (!validSubjectTypes.contains(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
                continue;
            }

            // 检查Excel内重复
            String businessKey = String.format("%s_%d_%s_%s",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（相同省份、年份、科类、批次）");
                continue;
            }
            excelKeys.add(businessKey);
            businessKeys.add(businessKey);
        }

        // 检查数据库是否存在重复记录
        if (!businessKeys.isEmpty() && errors.isEmpty()) {
            for (int i = 0; i < dataList.size(); i++) {
                BatchScoreLineImportDTO dto = dataList.get(i);
                int count = batchScoreLineMapper.countByBusinessKey(
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
                if (count > 0) {
                    errors.add("第" + (i + 2) + "行: 数据库已存在该记录（省份=" + dto.getProvince() +
                            ", 年份=" + dto.getYear() + ", 科类=" + dto.getSubjectType() +
                            ", 批次=" + dto.getBatch() + "）");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        // ==================== 第二次遍历：批量插入 ====================
        int insertCount = 0;
        for (BatchScoreLineImportDTO dto : dataList) {
            BatchScoreLine entity = BatchScoreLine.builder()
                    .id(snowflakeIdGenerator.nextId())
                    .province(dto.getProvince())
                    .year(dto.getYear())
                    .subjectType(dto.getSubjectType())
                    .batch(dto.getBatch())
                    .scoreLine(dto.getScoreLine())
                    .rankLine(dto.getRankLine())
                    .remark(dto.getRemark())
                    .build();
            batchScoreLineMapper.insert(entity);
            insertCount++;
        }

        log.info("导入批次分数线数据成功: 新增记录={}条", insertCount);
    }

    private BatchScoreLineListVO convertToListVO(BatchScoreLine entity) {
        BatchScoreLineListVO vo = new BatchScoreLineListVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setYear(entity.getYear());
        vo.setSubjectType(entity.getSubjectType());
        vo.setBatch(entity.getBatch());
        vo.setScoreLine(entity.getScoreLine());
        return vo;
    }

    private BatchScoreLineDetailVO convertToDetailVO(BatchScoreLine entity) {
        BatchScoreLineDetailVO vo = new BatchScoreLineDetailVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setYear(entity.getYear());
        vo.setSubjectType(entity.getSubjectType());
        vo.setBatch(entity.getBatch());
        vo.setScoreLine(entity.getScoreLine());
        vo.setRankLine(entity.getRankLine());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
