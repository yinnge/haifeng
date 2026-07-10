package com.haifeng.app.service.impl.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.service.major.PostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgradMajorServiceImpl implements PostgradMajorService {

    private static final short STATUS_PUBLISHED = 1;

    private final PostgradMajorMapper postgradMajorMapper;
    private final PostgradMajorUniversityMapper postgradMajorUniversityMapper;
    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;

    @Override
    public IPage<PostgradMajorListVO> page(PostgradMajorListQueryDTO dto) {
        Page<PostgradMajor> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<PostgradMajor> wrapper = new LambdaQueryWrapper<PostgradMajor>()
                .eq(PostgradMajor::getStatus, STATUS_PUBLISHED)
                .like(dto.getName() != null && !dto.getName().isBlank(),
                        PostgradMajor::getMajorName, dto.getName())
                .like(dto.getCode() != null && !dto.getCode().isBlank(),
                        PostgradMajor::getMajorCode, dto.getCode())
                .eq(dto.getDegreeType() != null && !dto.getDegreeType().isBlank(),
                        PostgradMajor::getDegreeType, dto.getDegreeType())
                .eq(dto.getDisciplineCategory() != null && !dto.getDisciplineCategory().isBlank(),
                        PostgradMajor::getDisciplineCategory, dto.getDisciplineCategory())
                .eq(dto.getPopularity() != null && !dto.getPopularity().isBlank(),
                        PostgradMajor::getPopularity, dto.getPopularity())
                .eq(dto.getDifficulty() != null && !dto.getDifficulty().isBlank(),
                        PostgradMajor::getDifficulty, dto.getDifficulty())
                .orderByDesc(PostgradMajor::getId);

        IPage<PostgradMajor> entityPage = postgradMajorMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public PostgradMajorDetailVO detail(Long majorId) {
        PostgradMajor e = postgradMajorMapper.selectOne(
                new LambdaQueryWrapper<PostgradMajor>()
                        .eq(PostgradMajor::getId, majorId)
                        .eq(PostgradMajor::getStatus, STATUS_PUBLISHED));
        if (e == null) {
            log.debug("考研专业不存在或已下架, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "考研专业不存在");
        }

        return PostgradMajorDetailVO.builder()
                .majorName(e.getMajorName())
                .majorCode(e.getMajorCode())
                .degreeType(e.getDegreeType())
                .disciplineCategory(e.getDisciplineCategory())
                .popularity(e.getPopularity())
                .difficulty(e.getDifficulty())
                .introduction(e.getIntroduction())
                .examSubjects(e.getExamSubjects())
                .admissionRequirements(e.getAdmissionRequirements())
                .crossExamDifficulty(e.getCrossExamDifficulty())
                .crossExamDescription(e.getCrossExamDescription())
                .crossExamFactors(e.getCrossExamFactors())
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPage<UniversityBriefForPostgradVO> universities(Long majorId, PostgradMajorUniversityQueryDTO dto) {
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage = postgradMajorUniversityMapper.selectUniversitiesByPostgradMajor(
                page, majorId, dto.getCategory());
        return mapPage.convert(this::toUniversityBriefVO);
    }

    @Override
    public IPage<UndergraduateMajorDirectionBriefVO> undergraduateMajors(Long postgradMajorId, BasePageQueryDTO dto) {
        PostgradMajor postgradMajor = postgradMajorMapper.selectOne(
                new LambdaQueryWrapper<PostgradMajor>()
                        .eq(PostgradMajor::getId, postgradMajorId)
                        .eq(PostgradMajor::getStatus, STATUS_PUBLISHED));
        if (postgradMajor == null) {
            log.warn("考研专业不存在或已下架，返回空分页, postgradMajorId={}", postgradMajorId);
            return new Page<>(dto.getPage(), dto.getSize());
        }
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(page, postgradMajorId);
        return mapPage.convert(row -> UndergraduateMajorDirectionBriefVO.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
                .majorName(row.get("majorName") != null
                        ? String.valueOf(row.get("majorName")) : null)
                .build());
    }

    private UniversityBriefForPostgradVO toUniversityBriefVO(Map<String, Object> row) {
        return UniversityBriefForPostgradVO.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
                .name(row.get("name") != null ? String.valueOf(row.get("name")) : null)
                .category(row.get("category") != null ? String.valueOf(row.get("category")) : null)
                .build();
    }

    private PostgradMajorListVO toListVO(PostgradMajor e) {
        return PostgradMajorListVO.builder()
                .id(e.getId())
                .majorName(e.getMajorName())
                .majorCode(e.getMajorCode())
                .degreeType(e.getDegreeType())
                .disciplineCategory(e.getDisciplineCategory())
                .popularity(e.getPopularity())
                .difficulty(e.getDifficulty())
                .brief(e.getBrief())
                .examSubjects(e.getExamSubjects())
                .build();
    }
}
