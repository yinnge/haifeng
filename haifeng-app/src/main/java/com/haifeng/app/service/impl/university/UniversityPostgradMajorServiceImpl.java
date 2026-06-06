package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityPostgradMajorQueryDTO;
import com.haifeng.app.service.university.UniversityPostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityPostgradMajorServiceImpl implements UniversityPostgradMajorService {

    private final PostgradMajorUniversityMapper postgradMajorUniversityMapper;

    @Override
    public IPage<PostgradMajorBriefVO> page(Long universityId, UniversityPostgradMajorQueryDTO dto) {
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage = postgradMajorUniversityMapper.selectPostgradMajorsByUniversity(
                page, universityId, dto.getDegreeType());
        return mapPage.convert(this::toBriefVO);
    }

    private PostgradMajorBriefVO toBriefVO(Map<String, Object> row) {
        return PostgradMajorBriefVO.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
                .majorName(row.get("majorName") != null ? String.valueOf(row.get("majorName")) : null)
                .degreeType(row.get("degreeType") != null ? String.valueOf(row.get("degreeType")) : null)
                .build();
    }
}
