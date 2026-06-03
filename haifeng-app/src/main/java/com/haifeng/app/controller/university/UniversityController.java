package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端院校管理 - 列表（公开）+ 详情（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    /** 任务 1：分页查询院校列表，无需登录 */
    @GetMapping("/list")
    public R<IPage<UniversityListVO>> list(@Valid UniversityQueryDTO dto) {
        return R.ok(universityService.page(dto));
    }
}
