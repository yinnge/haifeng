package com.haifeng.app.service.impl.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.search.SearchService;
import com.haifeng.app.vo.search.SearchResultVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;

    private final UniversityMapper universityMapper;
    private final CityMapper cityMapper;
    private final MajorMapper majorMapper;

    @Override
    public List<SearchResultVO> searchUniversity(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(University::getName, keyword)
                .orderByAsc(University::getName);

        Page<University> page = new Page<>(1, actualLimit, false);
        List<University> list = universityMapper.selectPage(page, wrapper).getRecords();

        return list.stream()
                .map(u -> SearchResultVO.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultVO> searchCity(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(City::getCityName, keyword)
                .orderByAsc(City::getCityName);

        Page<City> page = new Page<>(1, actualLimit, false);
        List<City> list = cityMapper.selectPage(page, wrapper).getRecords();

        return list.stream()
                .map(c -> SearchResultVO.builder()
                        .id(c.getId())
                        .name(c.getCityName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultVO> searchMajor(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Major::getMajorName, keyword)
                .orderByAsc(Major::getMajorName);

        Page<Major> page = new Page<>(1, actualLimit, false);
        List<Major> list = majorMapper.selectPage(page, wrapper).getRecords();

        return list.stream()
                .map(m -> SearchResultVO.builder()
                        .id(m.getId())
                        .name(m.getMajorName())
                        .build())
                .collect(Collectors.toList());
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
