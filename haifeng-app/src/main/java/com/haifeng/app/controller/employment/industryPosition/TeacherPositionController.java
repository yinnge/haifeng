package com.haifeng.app.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.industryPosition.TeacherPositionSearchDTO;
import com.haifeng.app.service.employment.industryPosition.TeacherPositionService;
import com.haifeng.app.vo.employment.industryPosition.TeacherPositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.TeacherPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/teacher")
@RequiredArgsConstructor
public class TeacherPositionController {

    private final TeacherPositionService teacherPositionService;

    @GetMapping("/list")
    public R<IPage<TeacherPositionListVO>> list(@Valid TeacherPositionSearchDTO dto) {
        return R.ok(teacherPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<TeacherPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(teacherPositionService.detail(id));
    }
}
