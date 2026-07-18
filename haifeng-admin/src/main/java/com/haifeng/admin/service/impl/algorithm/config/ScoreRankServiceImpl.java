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

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreRankServiceImpl implements ScoreRankService {

    private static final int MAX_ERROR_ROWS = 20;
    private final ScoreRankMapper scoreRankMapper;

    @Override
    public IPage<ScoreRankListVO> page(ScoreRankQueryDTO dto) {
        Page<ScoreRank> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ScoreRank> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(ScoreRank::getProvince, dto.getProvince());
        }
        if (dto.getYear() != null) {
            wrapper.eq(ScoreRank::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(ScoreRank::getSubjectType, dto.getSubjectType());
        }
        if (dto.getScore() != null) {
            wrapper.eq(ScoreRank::getScore, dto.getScore());
        }
        if (dto.getRank() != null) {
            wrapper.eq(ScoreRank::getRank, dto.getRank());
        }

        wrapper.orderByAsc(ScoreRank::getProvince)
               .orderByDesc(ScoreRank::getYear)
               .orderByDesc(ScoreRank::getScore);

        IPage<ScoreRank> resultPage = scoreRankMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ScoreRankDetailVO detail(Long id) {
        ScoreRank entity = scoreRankMapper.selectById(id);
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
        ScoreRank existing = scoreRankMapper.selectById(id);
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

        scoreRankMapper.updateById(existing);
        log.info("修改一分一段记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ScoreRank entity = scoreRankMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        scoreRankMapper.deleteById(id);
        log.info("删除一分一段记录，id={}", id);
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
    public Integer importData(MultipartFile file) {
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
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
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

        // 构建批量查询参数（直接用原始字段，避免 split 解析 bug）
        Map<String, Object[]> businessKeyFields = new LinkedHashMap<>();
        for (ScoreRankImportDTO dto : dataList) {
            if (dto.getProvince() != null && dto.getYear() != null
                    && dto.getSubjectType() != null && dto.getScore() != null) {
                String key = String.format("%s_%d_%s_%d",
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
                businessKeyFields.put(key, new Object[]{dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore()});
            }
        }

        // 批量查询数据库中已存在的业务键（未删除）
        Set<String> dbExistingKeys = new HashSet<>();
        // P8: 批量查询数据库中软删除的业务键（可恢复）
        Set<String> dbDeletedKeys = new HashSet<>();
        // P8: 软删除记录的 id 映射，用于恢复（消除 N+1 查询）
        Map<String, Long> deletedKeyToId = new HashMap<>();

        if (!businessKeyFields.isEmpty()) {
            List<Map<String, Object>> queryKeys = new ArrayList<>();
            for (Object[] fields : businessKeyFields.values()) {
                Map<String, Object> m = new HashMap<>();
                m.put("province", fields[0]);
                m.put("year", fields[1]);
                m.put("subjectType", fields[2]);
                m.put("score", fields[3]);
                queryKeys.add(m);
            }
            if (!queryKeys.isEmpty()) {
                List<String> existingActiveKeys = scoreRankMapper.selectExistingKeys(queryKeys);
                dbExistingKeys.addAll(existingActiveKeys);

                // P8: 批量查询软删除记录（含 ID），消除 N+1 查询
                List<ScoreRank> deleted = scoreRankMapper.selectDeletedByKeys(queryKeys);
                for (ScoreRank rank : deleted) {
                    String key = String.format("%s_%d_%s_%d",
                            rank.getProvince(), rank.getYear(), rank.getSubjectType(), rank.getScore());
                    dbDeletedKeys.add(key);
                    deletedKeyToId.put(key, rank.getId());
                }
            }
        }

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

            if (dbExistingKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: 数据库已存在该记录（省份=" + dto.getProvince() +
                        ", 年份=" + dto.getYear() + ", 科类=" + dto.getSubjectType() +
                        ", 分数=" + dto.getScore() + "）");
                continue;
            }

            // 软删除记录不报错，标记为可恢复，在插入阶段处理

            if (errors.size() >= MAX_ERROR_ROWS) {
                break;
            }
        }

        // P6: 错误信息显示总数
        if (!errors.isEmpty()) {
            String detail = errors.size() > MAX_ERROR_ROWS
                    ? String.join("; ", errors.subList(0, MAX_ERROR_ROWS)) + " 等" + errors.size() + "条错误"
                    : String.join("; ", errors);
            throw new BusinessException(400, "数据校验失败：" + detail);
        }

        // ==================== 插入阶段 ====================
        int insertCount = 0;
        int restoreCount = 0;
        for (ScoreRankImportDTO dto : dataList) {
            String businessKey = String.format("%s_%d_%s_%d",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());

            // P8: 若存在软删除记录，恢复并更新（从 Map 取 ID，无需逐条查询）
            if (dbDeletedKeys.contains(businessKey)) {
                Long deletedId = deletedKeyToId.get(businessKey);
                ScoreRank deleted = scoreRankMapper.selectByIdIgnoreDeleted(deletedId);
                if (deleted != null) {
                    deleted.setIsDeleted(false);
                    deleted.setRank(dto.getRank());
                    deleted.setSameScoreCount(dto.getSameScoreCount());
                    deleted.setCumulativeCount(dto.getCumulativeCount());
                    scoreRankMapper.updateById(deleted);
                    restoreCount++;
                    continue;
                }
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
            insertCount++;
        }

        log.info("导入一分一段数据成功: 恢复记录={}条, 新增记录={}条", restoreCount, insertCount);
        return restoreCount + insertCount;
    }

    private ScoreRankListVO convertToListVO(ScoreRank entity) {
        ScoreRankListVO vo = new ScoreRankListVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setYear(entity.getYear());
        vo.setSubjectType(entity.getSubjectType());
        vo.setScore(entity.getScore());
        vo.setRank(entity.getRank());
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
