package com.haifeng.app.controller.university;

import com.haifeng.app.vo.university.UniversityBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
@RequireLogin
public class UniversityBriefController {

    private final UniversityMapper universityMapper;

    @GetMapping("/brief")
    public R<UniversityBriefVO> getByName(@RequestParam String name) {
        University university = universityMapper.selectByName(name);
        if (university == null) {
            throw new BusinessException(404, "院校不存在");
        }

        return R.ok(UniversityBriefVO.builder()
                .name(university.getName())
                .provinceName(university.getProvinceName())
                .cityName(university.getCityName())
                .region(university.getRegion())
                .category(university.getCategory())
                .educationLevel(university.getEducationLevel())
                .nature(university.getNature())
                .recommendationRate(university.getRecommendationRate())
                .department(university.getDepartment())
                .tags(university.getTags())
                .imageUrl(university.getImageUrl())
                .build());
    }
}
