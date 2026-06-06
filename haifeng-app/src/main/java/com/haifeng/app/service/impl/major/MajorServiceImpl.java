package com.haifeng.app.service.impl.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.service.major.MajorService;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.app.vo.major.CompetitionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {

    private static final short STATUS_PUBLISHED = 1;

    /** sortBy 参数 → 数据库列名映射 */
    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "employmentRate", "employment_rate",
            "salaryMin", "salary_min",
            "salaryMax", "salary_max"
    );

    private final MajorMapper majorMapper;
    private final MajorDetailMapper majorDetailMapper;
    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;
    private final CompetitionMajorMapper competitionMajorMapper;

    @Override
    public IPage<MajorListVO> page(MajorListQueryDTO dto) {
        Page<Major> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<Major>()
                .eq(Major::getStatus, STATUS_PUBLISHED)
                .like(dto.getName() != null && !dto.getName().isBlank(),
                        Major::getMajorName, dto.getName())
                .like(dto.getCode() != null && !dto.getCode().isBlank(),
                        Major::getMajorCode, dto.getCode())
                .eq(dto.getMajorType() != null && !dto.getMajorType().isBlank(),
                        Major::getMajorType, dto.getMajorType())
                .eq(dto.getMajorCategory() != null && !dto.getMajorCategory().isBlank(),
                        Major::getMajorCategory, dto.getMajorCategory())
                .orderByDesc(Major::getId);

        IPage<Major> entityPage = majorMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public MajorDetailVO detail(Long majorId) {
        // 1. 查主表
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getId, majorId)
                        .eq(Major::getStatus, STATUS_PUBLISHED));
        if (major == null) {
            log.debug("专业不存在或已下架, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "专业不存在");
        }

        // 2. 查详情表
        MajorDetail detail = majorDetailMapper.selectByMajorId(majorId);
        if (detail == null) {
            log.debug("专业详情不存在, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "专业详情不存在");
        }

        // 3. 合并返回
        return MajorDetailVO.builder()
                // t_major
                .majorName(major.getMajorName())
                .majorCode(major.getMajorCode())
                .disciplineName(major.getDisciplineName())
                .majorCategory(major.getMajorCategory())
                .parentCategory(major.getParentCategory())
                .majorTags(major.getMajorTags())
                .degreeAwarded(major.getDegreeAwarded())
                .employmentRate(major.getEmploymentRate())
                .salaryMin(major.getSalaryMin())
                .salaryMax(major.getSalaryMax())
                .description(major.getDescription())
                // t_major_detail
                .courseCount(detail.getCourseCount())
                .graduateScale(detail.getGraduateScale())
                .maleRatio(detail.getMaleRatio())
                .femaleRatio(detail.getFemaleRatio())
                .majorDescription(detail.getMajorDescription())
                .trainingObjective(detail.getTrainingObjective())
                .trainingRequirement(detail.getTrainingRequirement())
                .subjectRequirement(detail.getSubjectRequirement())
                .careerProspect(detail.getCareerProspect())
                .mainCourses(detail.getMainCourses())
                .knowledgeSkills(detail.getKnowledgeSkills())
                .build();
    }

    @Override
    public List<MajorCategoryStatVO> categoryStats() {
        List<Map<String, Object>> rows = majorMapper.countByCategory();
        return rows.stream()
                .map(row -> MajorCategoryStatVO.builder()
                        .majorCategory(String.valueOf(row.get("majorCategory")))
                        .count(((Number) row.get("count")).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public IPage<MajorListVO> ranking(MajorRankingQueryDTO dto) {
        Page<Major> page = new Page<>(dto.getPage(), dto.getSize());

        // 构建 WHERE 条件
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<Major>()
                .eq(Major::getStatus, STATUS_PUBLISHED)
                .like(dto.getName() != null && !dto.getName().isBlank(),
                        Major::getMajorName, dto.getName())
                .eq(dto.getMajorCategory() != null && !dto.getMajorCategory().isBlank(),
                        Major::getMajorCategory, dto.getMajorCategory());

        // 排序：sortBy 映射到数据库列名 + sortOrder + NULLS LAST
        String column = SORT_COLUMN_MAP.get(dto.getSortBy());
        String direction = "asc".equalsIgnoreCase(dto.getSortOrder()) ? "ASC" : "DESC";
        String nulls = "NULLS LAST";
        wrapper.last("ORDER BY " + column + " " + direction + " " + nulls + ", id DESC");

        IPage<Major> entityPage = majorMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public IPage<PostgradMajorDirectionBriefVO> postgradDirections(Long majorId, BasePageQueryDTO dto) {
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(page, majorId);
        return mapPage.convert(row -> PostgradMajorDirectionBriefVO.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
                .postgradMajorName(row.get("postgradMajorName") != null
                        ? String.valueOf(row.get("postgradMajorName")) : null)
                .build());
    }

    @Override
    public IPage<CompetitionBriefVO> competitions(Long majorId, BasePageQueryDTO dto) {
        // 1. 校验专业存在且上架
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getId, majorId)
                        .eq(Major::getStatus, STATUS_PUBLISHED));
        if (major == null) {
            log.debug("专业不存在或已下架, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "专业不存在");
        }
        // 2. 联表分页
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                competitionMajorMapper.selectCompetitionsByMajorId(page, majorId);
        return mapPage.convert(row -> CompetitionBriefVO.builder()
                .competitionId(row.get("competitionId") != null
                        ? ((Number) row.get("competitionId")).longValue() : null)
                .competitionName(row.get("competitionName") != null
                        ? String.valueOf(row.get("competitionName")) : null)
                .build());
    }

    private MajorListVO toListVO(Major e) {
        return MajorListVO.builder()
                .id(e.getId())
                .majorCode(e.getMajorCode())
                .majorName(e.getMajorName())
                .disciplineName(e.getDisciplineName())
                .majorCategory(e.getMajorCategory())
                .parentCategory(e.getParentCategory())
                .majorTags(e.getMajorTags())
                .degreeAwarded(e.getDegreeAwarded())
                .employmentRate(e.getEmploymentRate())
                .salaryMin(e.getSalaryMin())
                .salaryMax(e.getSalaryMax())
                .description(e.getDescription())
                .build();
    }
}
