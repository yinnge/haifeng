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

    private final ScoreRankMapper scoreRankMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<ScoreRankListVO> page(ScoreRankQueryDTO dto) {
        Page<ScoreRank> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ScoreRank> wrapper = new LambdaQueryWrapper<>();

        // 精准查询条件
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
    public Long add(ScoreRankAddDTO dto) {
        // 检查唯一约束(province+year+subjectType+score)
        Long existingId = scoreRankMapper.selectIdByBusinessKey(
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        if (existingId != null) {
            throw new BusinessException(400, "该一分一段记录已存在（相同省份、年份、科类、分数）");
        }

        ScoreRank entity = ScoreRank.builder()
                .id(snowflakeIdGenerator.nextId())
                .province(dto.getProvince())
                .year(dto.getYear())
                .subjectType(dto.getSubjectType())
                .score(dto.getScore())
                .rank(dto.getRank())
                .sameScoreCount(dto.getSameScoreCount())
                .cumulativeCount(dto.getCumulativeCount())
                .build();

        scoreRankMapper.insert(entity);
        log.info("新增一分一段记录，province={}, year={}, subjectType={}, score={}",
                dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
        return entity.getId();
    }

    @Override
    public void update(Long id, ScoreRankAddDTO dto) {
        ScoreRank existing = scoreRankMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "一分一段记录不存在");
        }

        // 检查唯一约束（排除自身）
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
    public void delete(Long id) {
        int rows = scoreRankMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(404, "一分一段记录不存在");
        }
        log.info("删除一分一段记录，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        scoreRankMapper.deleteBatchIds(ids);
        log.info("批量删除一分一段记录，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
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

        // ==================== 第一次遍历：校验 ====================
        List<String> errors = new ArrayList<>();
        Set<String> validSubjectTypes = Set.of("理科", "物理类", "文科", "历史类", "不分文理");
        // 用于检查Excel内重复
        Set<String> excelKeys = new HashSet<>();
        // 用于批量检查数据库重复
        List<String> businessKeys = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            ScoreRankImportDTO dto = dataList.get(i);

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
            if (dto.getScore() == null) {
                errors.add("第" + rowNum + "行: 分数不能为空");
                continue;
            }
            if (dto.getRank() == null) {
                errors.add("第" + rowNum + "行: 位次不能为空");
                continue;
            }

            // 枚举值校验
            if (!validSubjectTypes.contains(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
                continue;
            }

            // 检查Excel内重复
            String businessKey = String.format("%s_%d_%s_%d",
                    dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录（相同省份、年份、科类、分数）");
                continue;
            }
            excelKeys.add(businessKey);
            businessKeys.add(businessKey);
        }

        // 检查数据库是否存在重复记录
        if (!businessKeys.isEmpty() && errors.isEmpty()) {
            for (int i = 0; i < dataList.size(); i++) {
                ScoreRankImportDTO dto = dataList.get(i);
                int count = scoreRankMapper.countByBusinessKey(
                        dto.getProvince(), dto.getYear(), dto.getSubjectType(), dto.getScore());
                if (count > 0) {
                    errors.add("第" + (i + 2) + "行: 数据库已存在该记录（省份=" + dto.getProvince() +
                            ", 年份=" + dto.getYear() + ", 科类=" + dto.getSubjectType() +
                            ", 分数=" + dto.getScore() + "）");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        // ==================== 第二次遍历：批量插入 ====================
        int insertCount = 0;
        for (ScoreRankImportDTO dto : dataList) {
            ScoreRank entity = ScoreRank.builder()
                    .id(snowflakeIdGenerator.nextId())
                    .province(dto.getProvince())
                    .year(dto.getYear())
                    .subjectType(dto.getSubjectType())
                    .score(dto.getScore())
                    .rank(dto.getRank())
                    .sameScoreCount(dto.getSameScoreCount())
                    .cumulativeCount(dto.getCumulativeCount())
                    .build();
            scoreRankMapper.insert(entity);
            insertCount++;
        }

        log.info("导入一分一段数据成功: 新增记录={}条", insertCount);
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
        return vo;
    }
}
