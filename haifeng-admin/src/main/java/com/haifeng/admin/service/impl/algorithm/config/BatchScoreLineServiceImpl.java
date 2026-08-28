package com.haifeng.admin.service.impl.algorithm.config;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.excel.algorithm.config.BatchScoreLineImportDTO;
import com.haifeng.admin.service.algorithm.config.BatchScoreLineService;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
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

    private static final int MAX_ERROR_DISPLAY = 50;

    private final BatchScoreLineMapper batchScoreLineMapper;

    @Override
    public IPage<BatchScoreLineListVO> page(BatchScoreLineQueryDTO dto) {
        Page<BatchScoreLine> page = new Page<>(dto.getPage(), dto.getSize());

        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", dto.getIsDeleted());
        if (StringUtils.hasText(dto.getProvince())) {
            params.put("province", dto.getProvince());
        }
        if (dto.getYear() != null) {
            params.put("year", dto.getYear());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            params.put("subjectType", dto.getSubjectType());
        }
        if (StringUtils.hasText(dto.getBatch())) {
            params.put("batch", dto.getBatch());
        }
        if (dto.getScoreLine() != null) {
            params.put("scoreLine", dto.getScoreLine());
        }

        IPage<BatchScoreLine> resultPage = batchScoreLineMapper.selectPageCustom(page, params);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public BatchScoreLineDetailVO detail(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectByIdCustom(id);
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
        BatchScoreLine existing = batchScoreLineMapper.selectByIdCustom(id);
        if (existing == null || existing.getIsDeleted()) {
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
    public void updateStatus(Long id, Boolean isDeleted) {
        BatchScoreLine entity = batchScoreLineMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }
        batchScoreLineMapper.updateIsDeletedById(id, isDeleted);
        log.info("更新批次分数线状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectByIdCustom(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "批次分数线记录不存在");
        }
        batchScoreLineMapper.deleteById(id);
        log.info("删除批次分数线记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        BatchScoreLine entity = batchScoreLineMapper.selectByIdCustom(id);
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
    public ImportResultVO importData(MultipartFile file) {
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

        // 归一化字符串字段首尾空格，避免业务键/落库因空格产生静默重复或匹配失败
        for (BatchScoreLineImportDTO dto : dataList) {
            if (dto.getProvince() != null) dto.setProvince(dto.getProvince().trim());
            if (dto.getSubjectType() != null) dto.setSubjectType(dto.getSubjectType().trim());
            if (dto.getBatch() != null) dto.setBatch(dto.getBatch().trim());
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

        // 注：已存在/软删除记录的判定与处理移至导入阶段逐行进行（补空不覆盖）

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            BatchScoreLineImportDTO dto = dataList.get(i);

            // ---- 必填 + 长度校验（长度对应 DB 列定义） ----
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getProvince().length() > 20) {
                errors.add("第" + rowNum + "行: 省份长度不能超过20");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getYear() == null) {
                errors.add("第" + rowNum + "行: 年份不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (!StringUtils.hasText(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getSubjectType().length() > 20) {
                errors.add("第" + rowNum + "行: 科类长度不能超过20");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (!StringUtils.hasText(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getBatch().length() > 50) {
                errors.add("第" + rowNum + "行: 批次长度不能超过50");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getScoreLine() == null) {
                errors.add("第" + rowNum + "行: 分数线不能为空");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (dto.getRemark() != null && dto.getRemark().length() > 200) {
                errors.add("第" + rowNum + "行: 备注长度不能超过200");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }

            // ---- 业务范围 / 枚举校验 ----
            // P6: 年份范围校验
            if (dto.getYear() < 2000 || dto.getYear() > 2100) {
                errors.add("第" + rowNum + "行: 年份[" + dto.getYear() + "]不合法，只允许2000-2100");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            // P3: 省份枚举校验
            if (!validProvinces.contains(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份[" + dto.getProvince() + "]不合法");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            if (!validSubjectTypes.contains(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            // P4: 分数线范围校验（0-900）
            if (dto.getScoreLine() < 0 || dto.getScoreLine() > 900) {
                errors.add("第" + rowNum + "行: 分数线[" + dto.getScoreLine() + "]不合法，只允许0-900");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            // P4: 位次线范围校验（0-9999999，可选字段）
            if (dto.getRankLine() != null && (dto.getRankLine() < 0 || dto.getRankLine() > 9999999)) {
                errors.add("第" + rowNum + "行: 位次线[" + dto.getRankLine() + "]不合法，只允许0-9999999");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }

            // ---- 业务键唯一性校验 ----
            String businessKey = String.format("%s_%d_%s_%s",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（相同省份、年份、科类、批次）");
                if (errors.size() >= MAX_ERROR_DISPLAY) break;
                continue;
            }
            excelKeys.add(businessKey);

            // 数据库已存在/软删除记录不再在此阶段拒绝，导入阶段按"补空不覆盖"处理
        }

        // ==================== 导入阶段（补空不覆盖 + 软删除恢复；整批成功，任意行失败整批回滚） ====================
        List<String> rowErrors = new ArrayList<>();
        int insertCount = 0;
        int updatedCount = 0;   // 已存在记录补空 + 软删除记录恢复
        int rowNum = 1;
        for (BatchScoreLineImportDTO dto : dataList) {
            rowNum++;
            String businessKey = String.format("%s_%d_%s_%s",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
            try {
                // 1) 软删除记录：恢复并补空
                Long deletedId = batchScoreLineMapper.selectDeletedIdByBusinessKey(
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
                if (deletedId != null) {
                    BatchScoreLine deleted = batchScoreLineMapper.selectByIdIgnoreDeleted(deletedId);
                    if (deleted != null) {
                        deleted.setIsDeleted(false);
                        fillBatchScoreLineGaps(deleted, dto);
                        batchScoreLineMapper.updateById(deleted);
                        updatedCount++;
                        continue;
                    }
                }

                // 2) 已存在（未删除）记录：仅补空不覆盖
                Long existingId = batchScoreLineMapper.selectIdByBusinessKey(
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getBatch());
                if (existingId != null) {
                    BatchScoreLine existing = batchScoreLineMapper.selectByIdCustom(existingId);
                    if (existing != null) {
                        boolean changed = fillBatchScoreLineGaps(existing, dto);
                        if (changed) {
                            batchScoreLineMapper.updateById(existing);
                        }
                        updatedCount++;
                        continue;
                    }
                }

                // 3) 新增
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
            } catch (Exception e) {
                rowErrors.add("第" + rowNum + "行: 保存失败[" + dto.getProvince() + "/" + dto.getYear() + "]: " + e.getMessage());
            }
        }

        if (!rowErrors.isEmpty()) {
            throw new BusinessException(400, joinErrors(rowErrors));
        }

        log.info("导入批次分数线数据完成: 新增={}条, 补空/恢复={}条", insertCount, updatedCount);
        int total = dataList.size();
        int failed = 0; // 整批回滚，无部分成功
        return ImportResultVO.builder()
                .total(total)
                .success(total - failed)
                .failed(failed)
                .updated(updatedCount)
                .errors(rowErrors)
                .build();
    }

    /**
     * 已存在记录补空不覆盖：仅当 DB 列为 null/空字符串 且 导入有值时才写入，已有真实数据保留。
     * 业务键(province/year/subjectType/batch)不参与，避免覆盖匹配依据。
     */
    private boolean fillBatchScoreLineGaps(BatchScoreLine e, BatchScoreLineImportDTO dto) {
        boolean changed = false;
        if (e.getScoreLine() == null && dto.getScoreLine() != null) { e.setScoreLine(dto.getScoreLine()); changed = true; }
        if (e.getRankLine() == null && dto.getRankLine() != null) { e.setRankLine(dto.getRankLine()); changed = true; }
        if (!StringUtils.hasText(e.getRemark()) && StringUtils.hasText(dto.getRemark())) { e.setRemark(dto.getRemark()); changed = true; }
        return changed;
    }

    private String joinErrors(List<String> errList) {
        if (errList == null || errList.isEmpty()) return null;
        int shown = Math.min(errList.size(), MAX_ERROR_DISPLAY);
        String joined = String.join("; ", errList.subList(0, shown));
        if (errList.size() > MAX_ERROR_DISPLAY) {
            joined += "; ...仅显示前" + MAX_ERROR_DISPLAY + "条，共" + errList.size() + "行错误，详见后端日志";
        }
        return joined;
    }

    private BatchScoreLineListVO convertToListVO(BatchScoreLine entity) {
        BatchScoreLineListVO vo = new BatchScoreLineListVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setYear(entity.getYear());
        vo.setSubjectType(entity.getSubjectType());
        vo.setBatch(entity.getBatch());
        vo.setScoreLine(entity.getScoreLine());
        vo.setIsDeleted(entity.getIsDeleted());
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
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
