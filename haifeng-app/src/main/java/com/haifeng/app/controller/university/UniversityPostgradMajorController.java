package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityPostgradMajorQueryDTO;
import com.haifeng.app.service.university.UniversityPostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端大学→考研专业列表（spec 任务3接口1）
 * 需 Pro 及以上
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class UniversityPostgradMajorController {

    private final UniversityPostgradMajorService universityPostgradMajorService;

    /** 任务3接口1：大学 → 考研专业列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{universityId}/postgrad-majors")
    public R<IPage<PostgradMajorBriefVO>> list(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId,
            @Valid UniversityPostgradMajorQueryDTO dto) {
        return R.ok(universityPostgradMajorService.page(universityId, dto));
    }
}
