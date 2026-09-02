package com.haifeng.admin.service.impl.university;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haifeng.admin.dto.university.SubjectEvaluationAddDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationUpdateDTO;
import com.haifeng.admin.excel.university.SubjectEvaluationExcelDTO;
import com.haifeng.admin.service.university.SubjectEvaluationService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.university.SubjectEvaluationDetailVO;
import com.haifeng.admin.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.entity.university.SubjectEvaluation;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.SubjectEvaluationMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectEvaluationServiceImpl extends ServiceImpl<SubjectEvaluationMapper, SubjectEvaluation> implements SubjectEvaluationService {

    private final SubjectEvaluationMapper subjectEvaluationMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final int MAX_ERROR_DISPLAY = 50;
    private final UniversityMapper universityMapper;

    private static final Set<String> VALID_GRADES = Set.of("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-");

    @Override
    public IPage<SubjectEvaluationListVO> page(SubjectEvaluationQueryDTO dto) {
        Page<SubjectEvaluation> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SubjectEvaluation> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(SubjectEvaluation::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getDisciplineCode())) {
            wrapper.eq(SubjectEvaluation::getDisciplineCode, dto.getDisciplineCode());
        }
        if (StringUtils.hasText(dto.getDisciplineName())) {
            wrapper.like(SubjectEvaluation::getDisciplineName, dto.getDisciplineName());
        }
        if (StringUtils.hasText(dto.getEvaluationRound())) {
            wrapper.eq(SubjectEvaluation::getEvaluationRound, dto.getEvaluationRound());
        }
        if (StringUtils.hasText(dto.getEvaluationGrade())) {
            wrapper.eq(SubjectEvaluation::getEvaluationGrade, dto.getEvaluationGrade());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SubjectEvaluation::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(SubjectEvaluation::getSortOrder).orderByDesc(SubjectEvaluation::getCreatedAt);

        IPage<SubjectEvaluation> evalPage = subjectEvaluationMapper.selectPage(page, wrapper);

        return evalPage.convert(eval -> SubjectEvaluationListVO.builder()
                .id(eval.getId())
                .universityId(eval.getUniversityId())
                .universityName(eval.getUniversityName())
                .disciplineCode(eval.getDisciplineCode())
                .disciplineName(eval.getDisciplineName())
                .evaluationRound(eval.getEvaluationRound())
                .evaluationGrade(eval.getEvaluationGrade())
                .status(eval.getStatus() != null ? eval.getStatus().intValue() : null)
                .createdAt(eval.getCreatedAt())
                .build());
    }

    @Override
    public SubjectEvaluationDetailVO detail(Long id) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学科评估记录不存在");
        }

        return SubjectEvaluationDetailVO.builder()
                .id(eval.getId())
                .universityId(eval.getUniversityId())
                .universityName(eval.getUniversityName())
                .disciplineCode(eval.getDisciplineCode())
                .disciplineName(eval.getDisciplineName())
                .evaluationRound(eval.getEvaluationRound())
                .evaluationGrade(eval.getEvaluationGrade())
                .sortOrder(eval.getSortOrder())
                .status(eval.getStatus() != null ? eval.getStatus().intValue() : null)
                .createdAt(eval.getCreatedAt())
                .updatedAt(eval.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(SubjectEvaluationAddDTO dto) {
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(400, "院校不存在");
        }

        String round = dto.getEvaluationRound() != null ? dto.getEvaluationRound() : "第四轮";
        if (subjectEvaluationMapper.existsByUniversityAndDiscipline(dto.getUniversityId(), dto.getDisciplineCode(), round)) {
            throw new BusinessException(400, "该院校在此轮次下的学科评估记录已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        SubjectEvaluation eval = SubjectEvaluation.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .disciplineCode(dto.getDisciplineCode())
                .disciplineName(dto.getDisciplineName())
                .evaluationRound(round)
                .evaluationGrade(dto.getEvaluationGrade())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        subjectEvaluationMapper.insert(eval);
        log.info("新增学科评估成功，id={}", id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SubjectEvaluationUpdateDTO dto) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学科评估记录不存在");
        }

        if (dto.getDisciplineCode() != null) eval.setDisciplineCode(dto.getDisciplineCode());
        if (dto.getDisciplineName() != null) eval.setDisciplineName(dto.getDisciplineName());
        if (dto.getEvaluationRound() != null) eval.setEvaluationRound(dto.getEvaluationRound());
        if (dto.getEvaluationGrade() != null) eval.setEvaluationGrade(dto.getEvaluationGrade());
        if (dto.getSortOrder() != null) eval.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) eval.setStatus(dto.getStatus().shortValue());

        eval.setUpdatedAt(OffsetDateTime.now());
        subjectEvaluationMapper.updateById(eval);
        log.info("更新学科评估成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学科评估记录不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效，只允许0或1");
        }

        LambdaUpdateWrapper<SubjectEvaluation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SubjectEvaluation::getId, id)
               .set(SubjectEvaluation::getStatus, status.shortValue())
               .set(SubjectEvaluation::getUpdatedAt, OffsetDateTime.now());
        subjectEvaluationMapper.update(null, wrapper);
        log.info("更新学科评估状态，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updateStatus(id, 0);
        log.info("禁用学科评估，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        SubjectEvaluation eval = subjectEvaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学科评估记录不存在");
        }
        subjectEvaluationMapper.deleteById(id);
        log.info("硬删除学科评估，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        int count = subjectEvaluationMapper.selectBatchIds(ids).size();
        if (count != ids.size()) {
            throw new BusinessException(400, "部分记录不存在，共查询到" + count + "条");
        }
        LambdaUpdateWrapper<SubjectEvaluation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(SubjectEvaluation::getId, ids)
               .set(SubjectEvaluation::getStatus, (short) 0)
               .set(SubjectEvaluation::getUpdatedAt, OffsetDateTime.now());
        subjectEvaluationMapper.update(null, wrapper);
        log.info("批量禁用学科评估，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<SubjectEvaluation> records = subjectEvaluationMapper.selectBatchIds(ids);
        if (records.size() != ids.size()) {
            throw new BusinessException(400, "部分记录不存在");
        }
        subjectEvaluationMapper.deleteByIds(ids);
        log.info("批量硬删除学科评估，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importSubjectEvaluations(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            List<SubjectEvaluationExcelDTO> dataList;
            try {
                dataList = EasyExcel.read(file.getInputStream())
                        .head(SubjectEvaluationExcelDTO.class)
                        .sheet("学科评估")
                        .doReadSync();
            } catch (RuntimeException e) {
                throw new BusinessException(400, "读取『学科评估』Sheet失败，请确认sheet名称为『学科评估』且表头为：院校名称/学科代码/学科名称/评估轮次/评估等级/排序/状态");
            }
            if (dataList.isEmpty()) {
                throw new BusinessException(400, "『学科评估』Sheet中未读取到任何数据，请确认表头是否正确");
            }
            if (dataList.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
            }

            Map<String, Long> universityIdCache = new HashMap<>();
            Map<String, String> universityNameCache = new HashMap<>();
            int addedCount = 0;
            int updatedCount = 0;
            OffsetDateTime now = OffsetDateTime.now();

            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = i + 2;
                SubjectEvaluationExcelDTO data = dataList.get(i);

                if (!StringUtils.hasText(data.getUniversityName())) {
                    errorMsgs.add("第" + rowNum + "行：院校名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getDisciplineCode())) {
                    errorMsgs.add("第" + rowNum + "行：学科代码不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getDisciplineName())) {
                    errorMsgs.add("第" + rowNum + "行：学科名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getEvaluationGrade())) {
                    errorMsgs.add("第" + rowNum + "行：评估等级不能为空");
                    continue;
                }
                if (!VALID_GRADES.contains(data.getEvaluationGrade())) {
                    errorMsgs.add("第" + rowNum + "行：评估等级'" + data.getEvaluationGrade() + "'格式不正确");
                    continue;
                }

                Long universityId = universityIdCache.get(data.getUniversityName());
                if (universityId == null) {
                    LambdaQueryWrapper<University> uniWrapper = new LambdaQueryWrapper<>();
                    uniWrapper.eq(University::getName, data.getUniversityName()).eq(University::getStatus, (short) 1);
                    University university = universityMapper.selectOne(uniWrapper);
                    if (university == null) {
                        errorMsgs.add("第" + rowNum + "行：院校名称'" + data.getUniversityName() + "'不存在");
                        continue;
                    }
                    universityId = university.getId();
                    universityIdCache.put(data.getUniversityName(), universityId);
                    universityNameCache.put(data.getUniversityName(), university.getName());
                }

                String round = data.getEvaluationRound() != null ? data.getEvaluationRound() : "第四轮";

                // 同院校+同学科代码+同轮次：已存在则只补空，不再报"已存在"
                List<SubjectEvaluation> existingList = subjectEvaluationMapper.selectList(
                        new LambdaQueryWrapper<SubjectEvaluation>()
                                .eq(SubjectEvaluation::getUniversityId, universityId)
                                .eq(SubjectEvaluation::getDisciplineCode, data.getDisciplineCode())
                                .eq(SubjectEvaluation::getEvaluationRound, round)
                                .eq(SubjectEvaluation::getStatus, (short) 1));
                SubjectEvaluation existing = existingList.isEmpty() ? null : existingList.get(0);

                try {
                    if (existing != null) {
                        fillEvaluationGaps(existing, data, round, universityNameCache.get(data.getUniversityName()), now);
                        subjectEvaluationMapper.updateById(existing);
                        updatedCount++;
                    } else {
                        SubjectEvaluation eval = SubjectEvaluation.builder()
                                .id(SnowflakeIdGenerator.nextId())
                                .universityId(universityId)
                                .universityName(universityNameCache.get(data.getUniversityName()))
                                .disciplineCode(data.getDisciplineCode())
                                .disciplineName(data.getDisciplineName())
                                .evaluationRound(round)
                                .evaluationGrade(data.getEvaluationGrade())
                                .sortOrder(data.getSortOrder() != null ? data.getSortOrder() : 0)
                                .status(data.getStatus() != null ? data.getStatus().shortValue() : (short) 1)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();
                        subjectEvaluationMapper.insert(eval);
                        addedCount++;
                    }
                } catch (Exception e) {
                    errorMsgs.add("第" + rowNum + "行：保存失败[院校=" + data.getUniversityName()
                            + ", 学科代码=" + data.getDisciplineCode() + "]：" + e.getMessage());
                }
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入学科评估失败，已全部回滚：" + joinErrors(errorMsgs));
            }

            log.info("导入学科评估成功: 新增{}条, 补齐{}条", addedCount, updatedCount);
            return ImportResultVO.builder()
                    .total(dataList.size())
                    .success(dataList.size())
                    .failed(0)
                    .updated(updatedCount)
                    .errors(Collections.emptyList())
                    .build();

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }

    /**
     * 已有学科评估记录：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖。
     */
    private void fillEvaluationGaps(SubjectEvaluation db, SubjectEvaluationExcelDTO data,
                                    String round, String universityName, OffsetDateTime now) {
        if (db.getUniversityName() == null) {
            db.setUniversityName(universityName);
        }
        if (db.getDisciplineName() == null) {
            db.setDisciplineName(data.getDisciplineName());
        }
        if (db.getEvaluationRound() == null) {
            db.setEvaluationRound(round);
        }
        if (db.getEvaluationGrade() == null) {
            db.setEvaluationGrade(data.getEvaluationGrade());
        }
        if (db.getSortOrder() == null) {
            db.setSortOrder(data.getSortOrder() != null ? data.getSortOrder() : 0);
        }
        db.setUpdatedAt(now);
    }

    private String joinErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) return null;
        int shown = Math.min(errors.size(), MAX_ERROR_DISPLAY);
        String joined = String.join("; ", errors.subList(0, shown));
        if (errors.size() > MAX_ERROR_DISPLAY) {
            joined += "; ...仅显示前" + MAX_ERROR_DISPLAY + "条，共" + errors.size() + "行存在错误";
        }
        return joined;
    }
}
