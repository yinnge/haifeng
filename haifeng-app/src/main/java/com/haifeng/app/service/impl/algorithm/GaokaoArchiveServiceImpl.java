package com.haifeng.app.service.impl.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.*;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.ScoreRank;
import com.haifeng.common.enums.ReformModelEnum;
import com.haifeng.common.mapper.algorithm.BatchScoreLineMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
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
        String reformModel = determineReformModel(province, year);
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

        // 以服务端 ProvinceReform 表为准，自动确定改革模式，前端不提供此字段
        String reformModel = provinceReformService.getReformModel(
                dto.getGaokaoProvince(), dto.getGaokaoYear());

        processTraditionalSubjects(dto, reformModel);

        MemberGaokao existing = memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
        );

        MemberGaokao entity = convertToEntity(dto);
        entity.setMemberId(memberId);
        entity.setReformModel(reformModel);

        // 自动计算线差
        if (dto.getScore() != null && dto.getBatchLineScore() != null) {
            entity.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
        }

        if (existing == null) {
            entity.setId(SnowflakeIdGenerator.nextId());
            memberGaokaoMapper.insert(entity);
            log.info("创建高考档案成功: memberId={}, archiveId={}", memberId, entity.getId());
        } else {
            entity.setId(existing.getId());
            memberGaokaoMapper.updateById(entity);
            log.info("更新高考档案成功: memberId={}, archiveId={}", memberId, entity.getId());
        }

        return entity.getId();
    }

    @Override
    public GaokaoArchiveVO getMyArchive() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberGaokao archive = memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
        );

        if (archive == null) {
            return null;
        }

        return convertToVO(archive);
    }

    // ==================== 私有方法 ====================

    /**
     * 判断改革模式（委托给 ProvinceReformService）
     */
    private String determineReformModel(String province, Integer gaokaoYear) {
        return provinceReformService.getReformModel(province, gaokaoYear.shortValue());
    }

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
     * DTO 转 Entity
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
