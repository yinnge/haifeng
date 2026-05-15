package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.vo.major.MajorBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/app/major")
@RequiredArgsConstructor
@RequireLogin
public class MajorBriefController {

    private final MajorMapper majorMapper;

    @GetMapping("/brief")
    public R<List<MajorBriefVO>> getByName(@RequestParam String name) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getMajorName, name)
               .eq(Major::getStatus, 1);

        List<Major> majors = majorMapper.selectList(wrapper);

        List<MajorBriefVO> voList = majors.stream()
                .map(m -> MajorBriefVO.builder()
                        .majorCode(m.getMajorCode())
                        .majorName(m.getMajorName())
                        .disciplineName(m.getDisciplineName())
                        .majorType(m.getMajorType())
                        .majorCategory(m.getMajorCategory())
                        .parentCategory(m.getParentCategory())
                        .majorTags(m.getMajorTags())
                        .degreeAwarded(m.getDegreeAwarded())
                        .description(m.getDescription())
                        .build())
                .collect(Collectors.toList());

        return R.ok(voList);
    }
}
