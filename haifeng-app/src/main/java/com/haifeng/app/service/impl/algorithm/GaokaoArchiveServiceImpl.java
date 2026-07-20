package com.haifeng.app.service.impl.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.*;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.ScoreRank;
import com.haifeng.common.enums.ReformModelEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.BatchScoreLineMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.algorithm.ProvinceReformService;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaokaoArchiveServiceImpl implements GaokaoArchiveService {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ProvinceReformService provinceReformService;
    private final ScoreRankMapper scoreRankMapper;
    private final BatchScoreLineMapper batchScoreLineMapper;

    // 6选3科目池
    private static final List<String> SUBJECTS_6 = Arrays.asList("物理", "化学", "生物", "政治", "历史", "地理");
    // 3+1+2 首选科目
    private static final List<String> SUBJECTS_FIRST_312 = Arrays.asList("物理", "历史");
    // 3+1+2 再选科目
    private static final List<String> SUBJECTS_SECOND_312 = Arrays.asList("化学", "生物", "政治", "地理");
    // 传统文理选项
    private static final List<String> SUBJECTS_TRADITIONAL = Arrays.asList("文科", "理科");

    @Override
    public ReformModelVO getReformModel(String province, Integer year) {
        String reformModel = provinceReformService.getReformModel(province, year.shortValue());
        Map<String, List<String>> subjects = buildSubjectOptions(reformModel);

        return ReformModelVO.builder()
                .reformModel(reformModel)
                .subjects(subjects)
                .build();
    }

    @Override
    public ScoreRankVO getRank(String province, Integer year, String subjectType, Integer score) {
        ScoreRank rank = scoreRankMapper.selectOne(
                new LambdaQueryWrapper<ScoreRank>()
                        .eq(ScoreRank::getProvince, province)
                        .eq(ScoreRank::getYear, year.shortValue())
                        .eq(ScoreRank::getSubjectType, subjectType)
                        .eq(ScoreRank::getScore, score.shortValue())
                        .eq(ScoreRank::getIsDeleted, false)
        );

        if (rank == null) {
            return null;
        }

        return ScoreRankVO.builder()
                .rank(rank.getRank())
                .sameScoreCount(rank.getSameScoreCount())
                .build();
    }

    @Override
    public BatchLineListVO getBatchLines(String province, Integer year, String subjectType) {
        // 1. 先查当年数据（仅查未删除记录）
        List<BatchScoreLine> lines = batchScoreLineMapper.selectList(
                new LambdaQueryWrapper<BatchScoreLine>()
                        .eq(BatchScoreLine::getProvince, province)
                        .eq(BatchScoreLine::getYear, year.shortValue())
                        .eq(BatchScoreLine::getSubjectType, subjectType)
                        .eq(BatchScoreLine::getIsDeleted, false)
                        .orderByAsc(BatchScoreLine::getScoreLine)
        );

        if (!lines.isEmpty()) {
            return BatchLineListVO.builder()
                    .dataYear(year)
                    .isCurrentYear(true)
                    .batches(convertToBatchLineVOs(lines))
                    .build();
        }

        // 2. 当年无数据，查最近5年（仅查未删除记录）
        lines = batchScoreLineMapper.selectList(
                new LambdaQueryWrapper<BatchScoreLine>()
                        .eq(BatchScoreLine::getProvince, province)
                        .eq(BatchScoreLine::getSubjectType, subjectType)
                        .eq(BatchScoreLine::getIsDeleted, false)
                        .ge(BatchScoreLine::getYear, (short) (year - 5))
                        .orderByDesc(BatchScoreLine::getYear)
                        .orderByAsc(BatchScoreLine::getScoreLine)
        );

        Integer dataYear = lines.isEmpty() ? null : lines.get(0).getYear().intValue();

        // 只返回最新年份的数据
        if (dataYear != null) {
            final Integer finalDataYear = dataYear;
            lines = lines.stream()
                    .filter(l -> l.getYear().intValue() == finalDataYear)
                    .collect(Collectors.toList());
        }

        return BatchLineListVO.builder()
                .dataYear(dataYear)
                .isCurrentYear(false)
                .batches(convertToBatchLineVOs(lines))
                .build();
    }

    @Override
    @Transactional
    public Long saveArchive(GaokaoArchiveSaveDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        String reformModel = provinceReformService.getReformModel(
                dto.getGaokaoProvince(), dto.getGaokaoYear());

        validateSubjects(dto, reformModel);
        processTraditionalSubjects(dto, reformModel);

        MemberGaokao existing = memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
        );

        if (existing == null) {
            MemberGaokao entity = convertToEntity(dto);
            entity.setId(SnowflakeIdGenerator.nextId());
            entity.setMemberId(memberId);
            entity.setReformModel(reformModel);
            entity.setCreatedAt(OffsetDateTime.now());
            entity.setUpdatedAt(OffsetDateTime.now());
            if (dto.getScore() != null && dto.getBatchLineScore() != null) {
                entity.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
            }
            memberGaokaoMapper.insert(entity);
            log.info("创建高考档案成功: memberId={}, archiveId={}", memberId, entity.getId());
            return entity.getId();
        } else {
            updateExistingFromDto(existing, dto, reformModel);
            existing.setUpdatedAt(OffsetDateTime.now());
            memberGaokaoMapper.updateById(existing);
            log.info("更新高考档案成功: memberId={}, archiveId={}", memberId, existing.getId());
            return existing.getId();
        }
    }

    @Override
    public GaokaoArchiveVO getMyArchive() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberGaokao archive = memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
        );

        if (archive == null) {
            log.info("用户高考档案不存在: memberId={}", memberId);
            return null;
        }

        return convertToVO(archive);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建可选科目选项
     */
    private Map<String, List<String>> buildSubjectOptions(String reformModel) {
        Map<String, List<String>> subjects = new LinkedHashMap<>();

        if (ReformModelEnum.THREE_PLUS_THREE.getValue().equals(reformModel)) {
            // 3+3: 6选3
            subjects.put("first", new ArrayList<>(SUBJECTS_6));
        } else if (ReformModelEnum.THREE_PLUS_ONE_PLUS_TWO.getValue().equals(reformModel)) {
            // 3+1+2: 首选2选1，再选4选2
            subjects.put("first", new ArrayList<>(SUBJECTS_FIRST_312));
            subjects.put("second", new ArrayList<>(SUBJECTS_SECOND_312));
        } else {
            // 传统文理
            subjects.put("first", new ArrayList<>(SUBJECTS_TRADITIONAL));
        }

        return subjects;
    }

    /**
     * 处理传统文理模式的科目映射
     * 文科 → 政治、历史、地理
     * 理科 → 物理、化学、生物
     * 注意：此方法会直接修改入参 DTO 的科目字段（副作用）
     */
    private void processTraditionalSubjects(GaokaoArchiveSaveDTO dto, String reformModel) {
        if (!ReformModelEnum.TRADITIONAL.getValue().equals(reformModel)) {
            return;
        }

        String subjectType = dto.getSubjectType();
        if ("文科".equals(subjectType)) {
            dto.setSubjectType("政治");
            dto.setSecondSubjectType("历史");
            dto.setThirdSubjectType("地理");
        } else if ("理科".equals(subjectType)) {
            dto.setSubjectType("物理");
            dto.setSecondSubjectType("化学");
            dto.setThirdSubjectType("生物");
        }
    }

    /**
     * 校验科目是否符合改革模式的合法选项
     */
    private void validateSubjects(GaokaoArchiveSaveDTO dto, String reformModel) {
        if (ReformModelEnum.TRADITIONAL.getValue().equals(reformModel)) {
            if (!SUBJECTS_TRADITIONAL.contains(dto.getSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "传统文理模式下科目只能是文科或理科");
            }
            return;
        }

        if (ReformModelEnum.THREE_PLUS_ONE_PLUS_TWO.getValue().equals(reformModel)) {
            if (!SUBJECTS_FIRST_312.contains(dto.getSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "3+1+2模式首选科目只能是物理或历史");
            }
            if (dto.getSecondSubjectType() != null && !SUBJECTS_SECOND_312.contains(dto.getSecondSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "3+1+2模式再选科目只能是化学、生物、政治、地理");
            }
            if (dto.getThirdSubjectType() != null && !SUBJECTS_SECOND_312.contains(dto.getThirdSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "3+1+2模式再选科目只能是化学、生物、政治、地理");
            }
            return;
        }

        if (ReformModelEnum.THREE_PLUS_THREE.getValue().equals(reformModel)) {
            if (!SUBJECTS_6.contains(dto.getSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "3+3模式科目只能是物理、化学、生物、政治、历史、地理");
            }
            if (dto.getSecondSubjectType() != null && !SUBJECTS_6.contains(dto.getSecondSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "3+3模式科目只能是物理、化学、生物、政治、历史、地理");
            }
            if (dto.getThirdSubjectType() != null && !SUBJECTS_6.contains(dto.getThirdSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "3+3模式科目只能是物理、化学、生物、政治、历史、地理");
            }

            Set<String> selected = new HashSet<>();
            selected.add(dto.getSubjectType());
            if (dto.getSecondSubjectType() != null && !selected.add(dto.getSecondSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "科目不能重复");
            }
            if (dto.getThirdSubjectType() != null && !selected.add(dto.getThirdSubjectType())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "科目不能重复");
            }
        }
    }

    /**
     * 转换为批次线 VO 列表
     */
    private List<BatchLineVO> convertToBatchLineVOs(List<BatchScoreLine> lines) {
        return lines.stream()
                .map(line -> BatchLineVO.builder()
                        .batch(line.getBatch())
                        .scoreLine(line.getScoreLine())
                        .rankLine(line.getRankLine())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * DTO 转 Entity（仅用于新增）
     */
    private MemberGaokao convertToEntity(GaokaoArchiveSaveDTO dto) {
        return MemberGaokao.builder()
                .gaokaoYear(dto.getGaokaoYear())
                .gaokaoProvince(dto.getGaokaoProvince())
                .score(dto.getScore())
                .rank(dto.getRank())
                .subjectType(dto.getSubjectType())
                .secondSubjectType(dto.getSecondSubjectType())
                .thirdSubjectType(dto.getThirdSubjectType())
                .scoreChinese(dto.getScoreChinese())
                .scoreMath(dto.getScoreMath())
                .scoreEnglish(dto.getScoreEnglish())
                .scoreSubject1(dto.getScoreSubject1())
                .scoreSubject2(dto.getScoreSubject2())
                .scoreSubject3(dto.getScoreSubject3())
                .foreignLanguage(dto.getForeignLanguage())
                .isColorBlind(dto.getIsColorBlind())
                .isColorWeak(dto.getIsColorWeak())
                .visionLeft(dto.getVisionLeft())
                .visionRight(dto.getVisionRight())
                .hasSmellDisorder(dto.getHasSmellDisorder())
                .heightCm(dto.getHeightCm())
                .weightKg(dto.getWeightKg())
                .isLeftHanded(dto.getIsLeftHanded())
                .hasTattoo(dto.getHasTattoo())
                .hasScar(dto.getHasScar())
                .hasStutter(dto.getHasStutter())
                .isFreshGraduate(dto.getIsFreshGraduate())
                .politicalStatus(dto.getPoliticalStatus())
                .householdType(dto.getHouseholdType())
                .isPovertyCounty(dto.getIsPovertyCounty())
                .batch(dto.getBatch())
                .batchDataYear(dto.getBatchDataYear())
                .batchLineScore(dto.getBatchLineScore())
                .build();
    }

    /**
     * 更新已有档案：将 DTO 字段设置到 existing 实体
     */
    private void updateExistingFromDto(MemberGaokao existing, GaokaoArchiveSaveDTO dto, String reformModel) {
        existing.setGaokaoYear(dto.getGaokaoYear());
        existing.setGaokaoProvince(dto.getGaokaoProvince());
        existing.setScore(dto.getScore());
        existing.setRank(dto.getRank());
        existing.setReformModel(reformModel);
        existing.setSubjectType(dto.getSubjectType());
        existing.setSecondSubjectType(dto.getSecondSubjectType());
        existing.setThirdSubjectType(dto.getThirdSubjectType());
        existing.setScoreChinese(dto.getScoreChinese());
        existing.setScoreMath(dto.getScoreMath());
        existing.setScoreEnglish(dto.getScoreEnglish());
        existing.setScoreSubject1(dto.getScoreSubject1());
        existing.setScoreSubject2(dto.getScoreSubject2());
        existing.setScoreSubject3(dto.getScoreSubject3());
        existing.setForeignLanguage(dto.getForeignLanguage());
        existing.setIsColorBlind(dto.getIsColorBlind());
        existing.setIsColorWeak(dto.getIsColorWeak());
        existing.setVisionLeft(dto.getVisionLeft());
        existing.setVisionRight(dto.getVisionRight());
        existing.setHasSmellDisorder(dto.getHasSmellDisorder());
        existing.setHeightCm(dto.getHeightCm());
        existing.setWeightKg(dto.getWeightKg());
        existing.setIsLeftHanded(dto.getIsLeftHanded());
        existing.setHasTattoo(dto.getHasTattoo());
        existing.setHasScar(dto.getHasScar());
        existing.setHasStutter(dto.getHasStutter());
        existing.setIsFreshGraduate(dto.getIsFreshGraduate());
        existing.setPoliticalStatus(dto.getPoliticalStatus());
        existing.setHouseholdType(dto.getHouseholdType());
        existing.setIsPovertyCounty(dto.getIsPovertyCounty());
        existing.setBatch(dto.getBatch());
        existing.setBatchDataYear(dto.getBatchDataYear());
        existing.setBatchLineScore(dto.getBatchLineScore());
        if (dto.getScore() != null && dto.getBatchLineScore() != null) {
            existing.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
        }
    }

    /**
     * Entity 转 VO
     */
    private GaokaoArchiveVO convertToVO(MemberGaokao entity) {
        return GaokaoArchiveVO.builder()
                .id(entity.getId())
                .gaokaoYear(entity.getGaokaoYear())
                .gaokaoProvince(entity.getGaokaoProvince())
                .score(entity.getScore())
                .rank(entity.getRank())
                .reformModel(entity.getReformModel())
                .subjectType(entity.getSubjectType())
                .secondSubjectType(entity.getSecondSubjectType())
                .thirdSubjectType(entity.getThirdSubjectType())
                .scoreChinese(entity.getScoreChinese())
                .scoreMath(entity.getScoreMath())
                .scoreEnglish(entity.getScoreEnglish())
                .scoreSubject1(entity.getScoreSubject1())
                .scoreSubject2(entity.getScoreSubject2())
                .scoreSubject3(entity.getScoreSubject3())
                .foreignLanguage(entity.getForeignLanguage())
                .isColorBlind(entity.getIsColorBlind())
                .isColorWeak(entity.getIsColorWeak())
                .visionLeft(entity.getVisionLeft())
                .visionRight(entity.getVisionRight())
                .hasSmellDisorder(entity.getHasSmellDisorder())
                .heightCm(entity.getHeightCm())
                .weightKg(entity.getWeightKg())
                .isLeftHanded(entity.getIsLeftHanded())
                .hasTattoo(entity.getHasTattoo())
                .hasScar(entity.getHasScar())
                .hasStutter(entity.getHasStutter())
                .isFreshGraduate(entity.getIsFreshGraduate())
                .politicalStatus(entity.getPoliticalStatus())
                .householdType(entity.getHouseholdType())
                .isPovertyCounty(entity.getIsPovertyCounty())
                .batch(entity.getBatch())
                .batchDataYear(entity.getBatchDataYear())
                .batchLineScore(entity.getBatchLineScore())
                .scoreAboveLine(entity.getScoreAboveLine())
                .build();
    }
}
