package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private static final short STATUS_PUBLISHED = 1;

    private final UniversityMapper universityMapper;

    @Override
    public IPage<UniversityListVO> page(UniversityQueryDTO dto) {
        Page<University> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<University>()
                .eq(University::getStatus, STATUS_PUBLISHED)
                .like(StringUtils.hasText(dto.getName()), University::getName, dto.getName())
                .eq(StringUtils.hasText(dto.getProvinceName()), University::getProvinceName, dto.getProvinceName())
                .eq(StringUtils.hasText(dto.getNature()), University::getNature, dto.getNature())
                .eq(StringUtils.hasText(dto.getCategory()), University::getCategory, dto.getCategory())
                .eq(StringUtils.hasText(dto.getDepartment()), University::getDepartment, dto.getDepartment())
                .eq(StringUtils.hasText(dto.getEducationLevel()), University::getEducationLevel, dto.getEducationLevel())
                .eq(dto.getHasDoctorate() != null, University::getHasDoctorate, dto.getHasDoctorate())
                .eq(dto.getHasMaster() != null, University::getHasMaster, dto.getHasMaster())
                .orderByAsc(University::getSortOrder)
                .orderByDesc(University::getId);

        IPage<University> entityPage = universityMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    private UniversityListVO toListVO(University e) {
        return UniversityListVO.builder()
                .id(e.getId())
                .name(e.getName())
                .tags(e.getTags())
                .cityName(e.getCityName())
                .educationLevel(e.getEducationLevel())
                .provinceName(e.getProvinceName())
                .introduction(e.getIntroduction())
                .imageUrl(e.getImageUrl())
                .nature(e.getNature())
                .category(e.getCategory())
                .majorCount(e.getMajorCount())
                .hasDoctorate(e.getHasDoctorate())
                .hasMaster(e.getHasMaster())
                .department(e.getDepartment())
                .build();
    }
}
