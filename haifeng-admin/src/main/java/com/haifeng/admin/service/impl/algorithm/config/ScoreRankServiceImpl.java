package com.haifeng.admin.service.impl.algorithm.config;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.ScoreRankAddDTO;
import com.haifeng.admin.dto.algorithm.config.ScoreRankQueryDTO;
import com.haifeng.admin.excel.algorithm.config.ScoreRankImportDTO;
import com.haifeng.admin.service.algorithm.config.ScoreRankService;
import com.haifeng.admin.vo.algorithm.config.ScoreRankDetailVO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.entity.algorithm.ScoreRank;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
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
public class ScoreRankServiceImpl implements ScoreRankService {

    private static final int MAX_ERROR_DISPLAY = 50;
    private final ScoreRankMapper scoreRankMapper;

    @Override
    public IPage<ScoreRankListVO> page(ScoreRankQueryDTO dto) {
        Page<ScoreRank> page = new Page<>(dto.getPage(), dto.getSize());
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
        if (dto.getScore() != null) {
            params.put("score", dto.getScore());
        }
        if (dto.getRank() != null) {
            params.put("rank", dto.getRank());
        }
        IPage<ScoreRank> resultPage = scoreRankMapper.selectPageCustom(page, params);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ScoreRankDetailVO detail(Long id) {
        ScoreRank entity = scoreRankMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ScoreRankAddDTO dto) {
        Long deletedId = scoreRankMapper.selectDeletedIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        if (deletedId != null) {
            ScoreRank deleted = scoreRankMapper.selectByIdIgnoreDeleted(deletedId);
            deleted.setIsDeleted(false);
            deleted.setRank(dto.getRank());
            deleted.setSameScoreCount(dto.getSameScoreCount());
            deleted.setCumulativeCount(dto.getCumulativeCount());
            scoreRankMapper.updateById(deleted);
            log.info("恢复一分一段记录，id={}", deletedId);
            return deletedId;
        }

        Long existingId = scoreRankMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        if (existingId != null) {
            throw new BusinessException(400, "该一分一段记录已存在（相同省份、年份、科类、分数）");
        }

        ScoreRank entity = ScoreRank.builder()
                .id(SnowflakeIdGenerator.nextId())
                .province(dto.getProvince())
                .year(dto.getYear())
                .subjectType(dto.getSubjectType())
                .score(dto.getScore())
                .rank(dto.getRank())
                .sameScoreCount(dto.getSameScoreCount())
                .cumulativeCount(dto.getCumulativeCount())
                .isDeleted(false)
                .build();

        scoreRankMapper.insert(entity);
        log.info("新增一分一段记录，province={}, year={}, subjectType={}, score={}",
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ScoreRankAddDTO dto) {
        ScoreRank existing = scoreRankMapper.selectByIdCustom(id);
        if (existing == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }

        Long existingId = scoreRankMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该一分一段记录已存在（相同省份、年份、科类、分数）");
        }

        existing.setProvince(dto.getProvince());
        existing.setYear(dto.getYear());
        existing.setSubjectType(dto.getSubjectType());
        existing.setScore(dto.getScore());
        existing.setRank(dto.getRank());
        existing.setSameScoreCount(dto.getSameScoreCount());
        existing.setCumulativeCount(dto.getCumulativeCount());

        scoreRankMapper.updateByIdCustom(existing);
        log.info("修改一分一段记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ScoreRank entity = scoreRankMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        scoreRankMapper.deleteById(id);
        log.info("删除一分一段记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Boolean isDeleted) {
        ScoreRank entity = scoreRankMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        scoreRankMapper.updateIsDeletedById(id, isDeleted);
        log.info("更新一分一段状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        scoreRankMapper.batchSoftDelete(ids);
        log.info("批量删除一分一段记录，count={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        // P1: 文件类型校验
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }

        List<ScoreRankImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(ScoreRankImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "Excel文件读取失败，请检查文件格式与单元格数据类型");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        // 归一化字符串字段首尾空格，避免业务键因空格产生静默重复或匹配失败
        for (ScoreRankImportDTO dto : dataList) {
            if (dto.getProvince() != null) dto.setProvince(dto.getProvince().trim());
            if (dto.getSubjectType() != null) dto.setSubjectType(dto.getSubjectType().trim());
        }

        // P2: 行数上限
        if (dataList.size() > 1000) {
            throw new BusinessException(400, "单次导入不能超过1000条记录");
        }

        // ==================== 校验阶段 ====================
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
            ScoreRankImportDTO dto = dataList.get(i);

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
            if (dto.getScore() == null) {
                errors.add("第" + rowNum + "行: 分数不能为空");
                continue;
            }
            if (dto.getRank() == null) {
                errors.add("第" + rowNum + "行: 位次不能为空");
                continue;
            }

            // P4: 年份范围校验
            if (dto.getYear() < 2000 || dto.getYear() > 2100) {
                errors.add("第" + rowNum + "行: 年份[" + dto.getYear() + "]不合法，只允许2000-2100");
                continue;
            }

            // P3: 省份枚举校验
            if (!validProvinces.contains(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份[" + dto.getProvince() + "]不合法");
                continue;
            }

            if (!validSubjectTypes.contains(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
                continue;
            }

            // P5: 数值非负校验
            if (dto.getRank() != null && dto.getRank() < 0) {
                errors.add("第" + rowNum + "行: 位次不能为负数");
                continue;
            }
            if (dto.getSameScoreCount() != null && dto.getSameScoreCount() < 0) {
                errors.add("第" + rowNum + "行: 同分人数不能为负");
                continue;
            }
            if (dto.getCumulativeCount() != null && dto.getCumulativeCount() < 0) {
                errors.add("第" + rowNum + "行: 累计人数不能为负");
                continue;
            }

            String businessKey = String.format("%s_%d_%s_%d",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（相同省份、年份、科类、分数）");
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
        for (ScoreRankImportDTO dto : dataList) {
            rowNum++;
            String businessKey = String.format("%s_%d_%s_%d",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
            try {
                // 1) 软删除记录：恢复并补空
                Long deletedId = scoreRankMapper.selectDeletedIdByBusinessKey(
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
                if (deletedId != null) {
                    ScoreRank deleted = scoreRankMapper.selectByIdIgnoreDeleted(deletedId);
                    if (deleted != null) {
                        deleted.setIsDeleted(false);
                        fillScoreRankGaps(deleted, dto);
                        scoreRankMapper.updateById(deleted);
                        updatedCount++;
                        continue;
                    }
                }

                // 2) 已存在（未删除）记录：仅补空不覆盖
                Long existingId = scoreRankMapper.selectIdByBusinessKey(
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
                if (existingId != null) {
                    ScoreRank existing = scoreRankMapper.selectByIdCustom(existingId);
                    if (existing != null) {
                        boolean changed = fillScoreRankGaps(existing, dto);
                        if (changed) {
                            scoreRankMapper.updateById(existing);
                        }
                        updatedCount++;
                        continue;
                    }
                }

                // 3) 新增
                ScoreRank entity = ScoreRank.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .province(dto.getProvince())
                        .year(dto.getYear())
                        .subjectType(dto.getSubjectType())
                        .score(dto.getScore())
                        .rank(dto.getRank())
                        .sameScoreCount(dto.getSameScoreCount())
                        .cumulativeCount(dto.getCumulativeCount())
                        .isDeleted(false)
                        .build();
                scoreRankMapper.insert(entity);
                insertCount++;
            } catch (Exception e) {
                rowErrors.add("第" + rowNum + "行: 保存失败[" + dto.getProvince() + "/" + dto.getYear() + "]: " + e.getMessage());
            }
        }

        if (!rowErrors.isEmpty()) {
            throw new BusinessException(400, joinErrors(rowErrors));
        }

        log.info("导入一分一段数据完成: 新增={}条, 补空/恢复={}条", insertCount, updatedCount);
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
     * 业务键(province/year/subjectType/score)不参与，避免覆盖匹配依据。
     */
    private boolean fillScoreRankGaps(ScoreRank e, ScoreRankImportDTO dto) {
        boolean changed = false;
        if (e.getRank() == null && dto.getRank() != null) { e.setRank(dto.getRank()); changed = true; }
        if (e.getSameScoreCount() == null && dto.getSameScoreCount() != null) { e.setSameScoreCount(dto.getSameScoreCount()); changed = true; }
        if (e.getCumulativeCount() == null && dto.getCumulativeCount() != null) { e.setCumulativeCount(dto.getCumulativeCount()); changed = true; }
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

    private ScoreRankListVO convertToListVO(ScoreRank entity) {
        ScoreRankListVO vo = new ScoreRankListVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setYear(entity.getYear());
        vo.setSubjectType(entity.getSubjectType());
        vo.setScore(entity.getScore());
        vo.setRank(entity.getRank());
        vo.setIsDeleted(entity.getIsDeleted());
        return vo;
    }

    private ScoreRankDetailVO convertToDetailVO(ScoreRank entity) {
        ScoreRankDetailVO vo = new ScoreRankDetailVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setYear(entity.getYear());
        vo.setSubjectType(entity.getSubjectType());
        vo.setScore(entity.getScore());
        vo.setRank(entity.getRank());
        vo.setSameScoreCount(entity.getSameScoreCount());
        vo.setCumulativeCount(entity.getCumulativeCount());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setVersion(entity.getVersion());
        return vo;
    }
}
