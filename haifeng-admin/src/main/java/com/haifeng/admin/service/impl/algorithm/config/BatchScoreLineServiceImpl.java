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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchScoreLineServiceImpl implements BatchScoreLineService {

    private static final int MAX_ERROR_ROWS = 20;

    private final BatchScoreLineMapper batchScoreLineMapper;

    @Override
    public IPage<BatchScoreLineListVO> page(BatchScoreLineQueryDTO dto) {
        Page<BatchScoreLine> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<BatchScoreLine> wrapper = new LambdaQueryWrapper<>();

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
    @Transactional(rollbackFor = Exception.class)
    public Long add(BatchScoreLineAddDTO dto) {
        Long existingId = batchScoreLineMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        if (existingId != null) {
            throw new BusinessException(400, "该批次分数线记录已存在（相同省份、年份、科类、批次）");
        }

        Long deletedId = batchScoreLineMapper.selectDeletedIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        if (deletedId != null) {
            BatchScoreLine deleted = batchScoreLineMapper.selectByIdIgnoreDeleted(deletedId);
            if (deleted != null) {
                deleted.setIsDeleted(false);
                deleted.setScoreLine(dto.getScoreLine());
                deleted.setRankLine(dto.getRankLine());
                deleted.setRemark(dto.getRemark());
                batchScoreLineMapper.updateById(deleted);
                log.info("恢复已删除的批次分数线记录，id={}", deletedId);
                return deletedId;
            }
        }

        BatchScoreLine entity = BatchScoreLine.builder()
                .id(SnowflakeIdGenerator.nextId())
                .province(dto.getProvince())
                .year(dto.getYear())
                .subjectType(dto.getSubjectType())
                .batch(dto.getBatch())
                .scoreLine(dto.getScoreLine())
                .rankLine(dto.getRankLine())
                .remark(dto.getRemark())
                .isDeleted(false)
                .build();

        batchScoreLineMapper.insert(entity);
        log.info("新增批次分数线记录，province={}, year={}, subjectType={}, batch={}",
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, BatchScoreLineAddDTO dto) {
        BatchScoreLine existing = batchScoreLineMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }

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

        int rows = batchScoreLineMapper.updateById(existing);
        if (rows == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }
        log.info("修改批次分数线记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }
        batchScoreLineMapper.deleteById(id);
        log.info("删除批次分数线记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectByIdIgnoreDeleted(id);
        if (entity == null) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }
        batchScoreLineMapper.hardDeleteById(id);
        log.info("硬删除批次分数线记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        int deleted = batchScoreLineMapper.batchSoftDelete(ids);
        log.info("批量删除批次分数线记录，请求删除={}条，实际删除={}条", ids.size(), deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        int deleted = batchScoreLineMapper.batchHardDelete(ids);
        log.info("批量硬删除批次分数线记录，请求删除={}条，实际删除={}条", ids.size(), deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        // P1: 文件类型校验（仅允许 xlsx / xls）
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }

        List<BatchScoreLineImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(BatchScoreLineImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        // P2: 行数上限
        if (dataList.size() > 1000) {
            throw new BusinessException(400, "单次导入不能超过1000条记录");
        }

        // ==================== 校验阶段（只校验，不插入） ====================
        List<String> errors = new ArrayList<>();
        Set<String> validSubjectTypes = Set.of("理科", "物理类", "文科", "历史类", "不分文理");
        // P3: 省份枚举校验
        Set<String> validProvinces = Set.of(
                "北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
                "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
                "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州",
                "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆"
        );
        Set<String> excelKeys = new HashSet<>();

        // 构建批量查询参数（直接用原始字段，避免 split 解析 bug）
        Map<String, Object[]> businessKeyFields = new LinkedHashMap<>();
        for (BatchScoreLineImportDTO dto : dataList) {
            if (dto.getProvince() != null && dto.getYear() != null
                    && dto.getSubjectType() != null && dto.getBatch() != null) {
                String key = String.format("%s_%d_%s_%s",
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
                businessKeyFields.put(key, new Object[]{dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch()});
            }
        }

        // 批量查询数据库中已存在的业务键（未删除）
        Set<String> dbExistingKeys = new HashSet<>();
        // P5: 批量查询数据库中软删除的业务键（可恢复）
        Set<String> dbDeletedKeys = new HashSet<>();
        // P5: 软删除记录的 id 映射，用于恢复
        Map<String, Long> deletedKeyToId = new HashMap<>();

        if (!businessKeyFields.isEmpty()) {
            List<Map<String, Object>> queryKeys = new ArrayList<>();
            for (Object[] fields : businessKeyFields.values()) {
                Map<String, Object> m = new HashMap<>();
                m.put("province", fields[0]);
                m.put("year", fields[1]);
                m.put("subjectType", fields[2]);
                m.put("batch", fields[3]);
                queryKeys.add(m);
            }
            if (!queryKeys.isEmpty()) {
                List<BatchScoreLine> existing = batchScoreLineMapper.selectExistingByKeys(queryKeys);
                for (BatchScoreLine line : existing) {
                    String key = String.format("%s_%d_%s_%s",
                            line.getProvince(), line.getYear(), line.getSubjectType(), line.getBatch());
                    dbExistingKeys.add(key);
                }

                // P5: 查询软删除记录
                List<BatchScoreLine> deleted = batchScoreLineMapper.selectDeletedByKeys(queryKeys);
                for (BatchScoreLine line : deleted) {
                    String key = String.format("%s_%d_%s_%s",
                            line.getProvince(), line.getYear(), line.getSubjectType(), line.getBatch());
                    dbDeletedKeys.add(key);
                    deletedKeyToId.put(key, line.getId());
                }
            }
        }

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            BatchScoreLineImportDTO dto = dataList.get(i);

            // ---- 必填 + 长度校验（长度对应 DB 列定义） ----
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份不能为空");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (dto.getProvince().length() > 20) {
                errors.add("第" + rowNum + "行: 省份长度不能超过20");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (dto.getYear() == null) {
                errors.add("第" + rowNum + "行: 年份不能为空");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (!StringUtils.hasText(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类不能为空");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (dto.getSubjectType().length() > 20) {
                errors.add("第" + rowNum + "行: 科类长度不能超过20");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (!StringUtils.hasText(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次不能为空");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (dto.getBatch().length() > 50) {
                errors.add("第" + rowNum + "行: 批次长度不能超过50");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (dto.getScoreLine() == null) {
                errors.add("第" + rowNum + "行: 分数线不能为空");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (dto.getRemark() != null && dto.getRemark().length() > 200) {
                errors.add("第" + rowNum + "行: 备注长度不能超过200");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }

            // ---- 业务范围 / 枚举校验 ----
            // P6: 年份范围校验
            if (dto.getYear() < 2000 || dto.getYear() > 2100) {
                errors.add("第" + rowNum + "行: 年份[" + dto.getYear() + "]不合法，只允许2000-2100");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            // P3: 省份枚举校验
            if (!validProvinces.contains(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份[" + dto.getProvince() + "]不合法");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            if (!validSubjectTypes.contains(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            // P4: 分数线范围校验（0-900）
            if (dto.getScoreLine() < 0 || dto.getScoreLine() > 900) {
                errors.add("第" + rowNum + "行: 分数线[" + dto.getScoreLine() + "]不合法，只允许0-900");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            // P4: 位次线范围校验（0-9999999，可选字段）
            if (dto.getRankLine() != null && (dto.getRankLine() < 0 || dto.getRankLine() > 9999999)) {
                errors.add("第" + rowNum + "行: 位次线[" + dto.getRankLine() + "]不合法，只允许0-9999999");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }

            // ---- 业务键唯一性校验 ----
            String businessKey = String.format("%s_%d_%s_%s",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（相同省份、年份、科类、批次）");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }
            excelKeys.add(businessKey);

            if (dbExistingKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: 数据库已存在该记录（省份=" + dto.getProvince() +
                        ", 年份=" + dto.getYear() + ", 科类=" + dto.getSubjectType() +
                        ", 批次=" + dto.getBatch() + "）");
                if (errors.size() >= MAX_ERROR_ROWS) break;
                continue;
            }

            // P5: 软删除记录不报错，标记为可恢复，在插入阶段处理
        }

        // P7: 错误信息显示总数
        if (!errors.isEmpty()) {
            String detail = errors.size() > MAX_ERROR_ROWS
                    ? String.join("; ", errors.subList(0, MAX_ERROR_ROWS)) + " 等" + errors.size() + "条错误"
                    : String.join("; ", errors);
            throw new BusinessException(400, "数据校验失败：" + detail);
        }

        // ==================== 插入阶段（校验全过后才执行） ====================
        int insertCount = 0;
        int restoreCount = 0;
        for (BatchScoreLineImportDTO dto : dataList) {
            String businessKey = String.format("%s_%d_%s_%s",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());

            // P5: 若存在软删除记录，恢复并更新
            if (dbDeletedKeys.contains(businessKey)) {
                Long deletedId = deletedKeyToId.get(businessKey);
                BatchScoreLine deleted = batchScoreLineMapper.selectByIdIgnoreDeleted(deletedId);
                if (deleted != null) {
                    deleted.setIsDeleted(false);
                    deleted.setScoreLine(dto.getScoreLine());
                    deleted.setRankLine(dto.getRankLine());
                    deleted.setRemark(dto.getRemark());
                    batchScoreLineMapper.updateById(deleted);
                    restoreCount++;
                    continue;
                }
            }

            BatchScoreLine entity = BatchScoreLine.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .province(dto.getProvince())
                    .year(dto.getYear())
                    .subjectType(dto.getSubjectType())
                    .batch(dto.getBatch())
                    .scoreLine(dto.getScoreLine())
                    .rankLine(dto.getRankLine())
                    .remark(dto.getRemark())
                    .isDeleted(false)
                    .build();
            batchScoreLineMapper.insert(entity);
            insertCount++;
        }

        log.info("导入批次分数线数据成功: 新增={}条, 恢复={}条", insertCount, restoreCount);
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
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
