package com.haifeng.app.controller.university;

import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.UniversityBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/university/brief")
@RequiredArgsConstructor
@RequireLogin
public class UniversityBriefController {

    private final UniversityService universityService;

    @GetMapping
    public R<UniversityBriefVO> getByName(@RequestParam String name) {
        return R.ok(universityService.getByName(name));
    }
}
